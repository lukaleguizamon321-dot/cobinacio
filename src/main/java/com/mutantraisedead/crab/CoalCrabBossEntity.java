package com.mutantraisedead.crab;

import com.mutantraisedead.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Cangrejo Titan de Carbon: el jefe de la guarida (3 bloques de ancho, 6 de alto).
 *
 * Duerme como un huevo en el centro de la guarida (ver CrabEggBlock). Cuando despierta tiene tres ataques:
 *  - bolas de fuego (como el cangrejo de carbon chico, pero de a varias),
 *  - embestida (se prepara, corre en linea recta y lleva puesto a quien se cruce; si choca una pared se aturde),
 *  - llamarada (un cono de fuego que va girando hacia el jugador).
 * Al morir suelta bloques de carbon (ver data/mutantraisedead/loot_tables/entities/coal_crab_boss.json).
 */
public class CoalCrabBossEntity extends PathfinderMob {
    // Acciones: se sincronizan con el cliente para animar el modelo
    public static final int ACTION_IDLE = 0;
    public static final int ACTION_AWAKEN = 1;
    public static final int ACTION_FIREBALL = 2;
    public static final int ACTION_CHARGE_WINDUP = 3;
    public static final int ACTION_CHARGING = 4;
    public static final int ACTION_FLAME_WINDUP = 5;
    public static final int ACTION_FLAMING = 6;
    public static final int ACTION_STUNNED = 7;

    private static final EntityDataAccessor<Integer> DATA_ACTION =
            SynchedEntityData.defineId(CoalCrabBossEntity.class, EntityDataSerializers.INT);

    /** Cuanto tarda en salir y rugir al despertar (3 segundos). */
    public static final int AWAKEN_TICKS = 60;

    // Ajustes de los ataques (se pueden tocar aca)
    private static final int MELEE_FIRE_SECONDS = 3;
    private static final int FIREBALL_WINDUP = 20;
    private static final float CHARGE_DAMAGE = 14.0F;
    private static final int CHARGE_WINDUP = 26;
    private static final int CHARGE_MAX_TICKS = 34;
    private static final int STUN_TICKS = 50;
    private static final float FLAME_DAMAGE = 3.0F;
    private static final double FLAME_RANGE = 11.0D;
    private static final int FLAME_WINDUP = 24;
    private static final int FLAME_TICKS = 55;

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    /** Solo cliente: ticks que lleva la accion actual (para animar el modelo). */
    public int clientActionTicks;
    private int clientAction = ACTION_IDLE;

    private int awakenLeft;
    private int globalCooldown = 40;
    private int fireballCooldown = 40;
    private int chargeCooldown = 100;
    private int flameCooldown = 140;
    @Nullable
    private BlockPos homePos;

    public CoalCrabBossEntity(EntityType<? extends CoalCrabBossEntity> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.setPersistenceRequired();
        if (!level.isClientSide) {
            AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.setBaseValue(Config.COAL_BOSS_HEALTH.get());
                setHealth(getMaxHealth());
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ACTION, ACTION_IDLE);
    }

    public int getAction() {
        return this.entityData.get(DATA_ACTION);
    }

    private void setAction(int action) {
        this.entityData.set(DATA_ACTION, action);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return 3.0F;
    }

    @Override
    public float maxUpStep() {
        return 1.2F;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        this.homePos = blockPosition();
        this.awakenLeft = AWAKEN_TICKS;
        setAction(ACTION_AWAKEN);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
    }

    // ------------------------------------------------------------------
    // Metas (IA)
    // ------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new AwakenGoal());
        this.goalSelector.addGoal(1, new ChargeGoal());
        this.goalSelector.addGoal(1, new FlameGoal());
        this.goalSelector.addGoal(2, new FireballGoal());
        this.goalSelector.addGoal(3, new BossMeleeGoal());
        this.goalSelector.addGoal(4, new ReturnHomeGoal());
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private boolean isEnraged() {
        return getHealth() < getMaxHealth() * 0.5F;
    }

    /** Con menos de la mitad de vida espera menos entre ataques. */
    private int cooldown(int base) {
        return isEnraged() ? base * 2 / 3 : base;
    }

    private Vec3 forward() {
        float rad = getYRot() * ((float) Math.PI / 180.0F);
        return new Vec3(-Mth.sin(rad), 0.0D, Mth.cos(rad));
    }

    /** Desde donde salen las bolas de fuego y la llamarada: la boca del modelo (2,6 bloques adelante, 3,9 de alto). */
    private Vec3 mouth() {
        Vec3 f = forward();
        return position().add(f.x * 2.6D, 3.9D, f.z * 2.6D);
    }

    /** Gira el cuerpo hacia un punto, como mucho maxStep grados por tick. */
    private void faceToward(double x, double z, float maxStep) {
        float wanted = (float) (Mth.atan2(z - getZ(), x - getX()) * 57.29577951308232D) - 90.0F;
        float diff = Mth.clamp(Mth.wrapDegrees(wanted - getYRot()), -maxStep, maxStep);
        float yaw = getYRot() + diff;
        setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    private void faceToward(LivingEntity target, float maxStep) {
        faceToward(target.getX(), target.getZ(), maxStep);
    }

    private boolean readyForAbility(@Nullable LivingEntity target) {
        return target != null && target.isAlive() && !target.isSpectator()
                && getAction() == ACTION_IDLE && globalCooldown <= 0;
    }

    /** Se despierta: sale del suelo y ruge. */
    private class AwakenGoal extends Goal {
        AwakenGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return getAction() == ACTION_AWAKEN;
        }

        @Override
        public boolean canContinueToUse() {
            return getAction() == ACTION_AWAKEN;
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            playSound(SoundEvents.RAVAGER_ROAR, 3.0F, 0.5F);
        }

        @Override
        public void tick() {
            getNavigation().stop();
            awakenLeft--;
            if (level() instanceof ServerLevel serverLevel && awakenLeft % 4 == 0) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COAL_BLOCK.defaultBlockState()),
                        getX(), getY() + 0.2D, getZ(), 24, 1.4D, 0.1D, 1.4D, 0.1D);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0D, getZ(), 6, 1.2D, 0.6D, 1.2D, 0.02D);
                playSound(SoundEvents.DEEPSLATE_BREAK, 2.0F, 0.5F);
            }
            if (awakenLeft <= 0) {
                setAction(ACTION_IDLE);
                globalCooldown = 20;
            }
        }
    }

    /** El golpe normal, cuando esta cerca. */
    private class BossMeleeGoal extends MeleeAttackGoal {
        BossMeleeGoal() {
            super(CoalCrabBossEntity.this, 1.0D, true);
        }

        @Override
        public boolean canUse() {
            return getAction() == ACTION_IDLE && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return getAction() == ACTION_IDLE && super.canContinueToUse();
        }
    }

    /** Si no tiene a quien perseguir, vuelve al centro de la guarida. */
    private class ReturnHomeGoal extends Goal {
        ReturnHomeGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return getAction() == ACTION_IDLE && getTarget() == null && homePos != null
                    && homePos.distSqr(blockPosition()) > 16.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return getAction() == ACTION_IDLE && getTarget() == null && !getNavigation().isDone();
        }

        @Override
        public void start() {
            if (homePos != null) {
                getNavigation().moveTo(homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D, 1.0D);
            }
        }
    }

    /** Ataque 1: bolas de fuego (un abanico de 3, o de 5 si esta furioso). */
    private class FireballGoal extends Goal {
        private int ticks;

        FireballGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            if (!readyForAbility(target) || fireballCooldown > 0 || random.nextInt(3) != 0) {
                return false;
            }
            double distance = distanceTo(target);
            return distance >= 3.0D && distance <= 22.0D && hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            return getTarget() != null && getTarget().isAlive() && ticks <= FIREBALL_WINDUP + 12;
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            ticks = 0;
            setAction(ACTION_FIREBALL);
            getNavigation().stop();
            playSound(SoundEvents.BLAZE_AMBIENT, 2.0F, 0.5F);
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (target == null) {
                return;
            }
            getNavigation().stop();
            faceToward(target, 25.0F);
            ticks++;
            if (ticks == FIREBALL_WINDUP && level() instanceof ServerLevel serverLevel) {
                shoot(serverLevel, target);
            }
        }

        private void shoot(ServerLevel serverLevel, LivingEntity target) {
            Vec3 origin = mouth();
            Vec3 aim = target.getEyePosition().subtract(origin).normalize();
            Vec3 side = new Vec3(-aim.z, 0.0D, aim.x);
            int half = isEnraged() ? 2 : 1;
            for (int i = -half; i <= half; i++) {
                Vec3 dir = aim.add(side.scale(0.14D * i));
                SmallFireball fireball = new SmallFireball(serverLevel, CoalCrabBossEntity.this, dir.x, dir.y, dir.z);
                fireball.setPos(origin.x, origin.y, origin.z);
                serverLevel.addFreshEntity(fireball);
            }
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, 0.7F);
        }

        @Override
        public void stop() {
            setAction(ACTION_IDLE);
            fireballCooldown = cooldown(70);
            globalCooldown = 25;
        }
    }

    /** Ataque 2: embestida. Se prepara, corre en linea recta y se aturde si choca con una pared. */
    private class ChargeGoal extends Goal {
        private int ticks;
        private int stunLeft;
        private Vec3 direction = Vec3.ZERO;
        private final Set<Integer> hit = new HashSet<>();
        private boolean finished;

        ChargeGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            if (!readyForAbility(target) || chargeCooldown > 0 || random.nextInt(4) != 0) {
                return false;
            }
            double distance = distanceTo(target);
            return distance >= 6.0D && distance <= 24.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return !finished;
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            ticks = 0;
            stunLeft = 0;
            finished = false;
            hit.clear();
            setAction(ACTION_CHARGE_WINDUP);
            getNavigation().stop();
            playSound(SoundEvents.RAVAGER_ROAR, 2.5F, 0.7F);
        }

        @Override
        public void tick() {
            ticks++;
            int action = getAction();
            LivingEntity target = getTarget();

            if (action == ACTION_CHARGE_WINDUP) {
                getNavigation().stop();
                if (target != null) {
                    faceToward(target, 20.0F);
                }
                if (level() instanceof ServerLevel serverLevel && ticks % 3 == 0) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.3D, getZ(), 8, 1.2D, 0.1D, 1.2D, 0.02D);
                    playSound(SoundEvents.STONE_STEP, 1.5F, 0.5F);
                }
                if (ticks >= CHARGE_WINDUP) {
                    Vec3 f = forward();
                    direction = f.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : f.normalize();
                    setAction(ACTION_CHARGING);
                    ticks = 0;
                    playSound(SoundEvents.RAVAGER_ATTACK, 2.5F, 0.6F);
                }
                return;
            }

            if (action == ACTION_CHARGING) {
                double speed = isEnraged() ? 0.85D : 0.7D;
                Vec3 motion = getDeltaMovement();
                setDeltaMovement(direction.x * speed, motion.y, direction.z * speed);
                faceToward(getX() + direction.x, getZ() + direction.z, 90.0F);
                damageInPath();
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COAL_BLOCK.defaultBlockState()),
                            getX(), getY() + 0.1D, getZ(), 6, 1.2D, 0.1D, 1.2D, 0.05D);
                    if (ticks % 4 == 0) {
                        playSound(SoundEvents.DEEPSLATE_STEP, 2.0F, 0.5F);
                    }
                }
                if (ticks > 4 && horizontalCollision) {
                    hitWall();
                } else if (ticks >= CHARGE_MAX_TICKS) {
                    setAction(ACTION_STUNNED);
                    stunLeft = 18;
                }
                return;
            }

            // Aturdido: queda quieto un rato (despues de chocar o de frenar)
            getNavigation().stop();
            stunLeft--;
            if (level() instanceof ServerLevel serverLevel && stunLeft % 6 == 0) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 4.0D, getZ(), 5, 0.8D, 0.4D, 0.8D, 0.01D);
            }
            if (stunLeft <= 0) {
                finished = true;
            }
        }

        private void hitWall() {
            setAction(ACTION_STUNNED);
            stunLeft = STUN_TICKS;
            setDeltaMovement(Vec3.ZERO);
            playSound(SoundEvents.GENERIC_EXPLODE, 2.5F, 0.6F);
            if (level() instanceof ServerLevel serverLevel) {
                Vec3 front = position().add(direction.x * 1.6D, 1.5D, direction.z * 1.6D);
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE_BRICKS.defaultBlockState()),
                        front.x, front.y, front.z, 60, 1.0D, 1.2D, 1.0D, 0.15D);
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, front.x, front.y, front.z, 3, 0.8D, 0.8D, 0.8D, 0.0D);
            }
        }

        /** Se lleva puesto a quien este en el camino (una sola vez cada uno). */
        private void damageInPath() {
            AABB box = getBoundingBox().inflate(0.35D, 0.0D, 0.35D);
            for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != CoalCrabBossEntity.this && e.isAlive() && !(e instanceof CoalCrabBossEntity))) {
                if (!hit.add(victim.getId())) {
                    continue;
                }
                victim.hurt(damageSources().mobAttack(CoalCrabBossEntity.this), CHARGE_DAMAGE);
                victim.knockback(1.8D, -direction.x, -direction.z);
                victim.setDeltaMovement(victim.getDeltaMovement().add(0.0D, 0.5D, 0.0D));
                victim.hurtMarked = true;
            }
        }

        @Override
        public void stop() {
            setAction(ACTION_IDLE);
            chargeCooldown = cooldown(170);
            globalCooldown = 30;
        }
    }

    /** Ataque 3: llamarada. Prepara el fuego y despues lanza un cono que va girando hacia el jugador. */
    private class FlameGoal extends Goal {
        private int ticks;

        FlameGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            if (!readyForAbility(target) || flameCooldown > 0 || random.nextInt(4) != 0) {
                return false;
            }
            double distance = distanceTo(target);
            return distance >= 2.0D && distance <= 10.0D && hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            return getTarget() != null && getTarget().isAlive() && ticks < FLAME_WINDUP + FLAME_TICKS;
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            ticks = 0;
            setAction(ACTION_FLAME_WINDUP);
            getNavigation().stop();
            playSound(SoundEvents.BLAZE_BURN, 2.5F, 0.5F);
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (target == null) {
                return;
            }
            getNavigation().stop();
            ticks++;
            if (ticks < FLAME_WINDUP) {
                faceToward(target, 20.0F);
                if (level() instanceof ServerLevel serverLevel && ticks % 3 == 0) {
                    Vec3 m = mouth();
                    serverLevel.sendParticles(ParticleTypes.FLAME, m.x, m.y, m.z, 6, 0.3D, 0.3D, 0.3D, 0.02D);
                    serverLevel.sendParticles(ParticleTypes.SMOKE, m.x, m.y, m.z, 4, 0.3D, 0.3D, 0.3D, 0.02D);
                }
                return;
            }
            if (getAction() != ACTION_FLAMING) {
                setAction(ACTION_FLAMING);
                playSound(SoundEvents.BLAZE_SHOOT, 3.0F, 0.5F);
            }
            // gira despacio, asi se puede esquivar corriendo hacia un costado
            faceToward(target, 3.5F);
            if (level() instanceof ServerLevel serverLevel) {
                breathe(serverLevel, target);
            }
        }

        private void breathe(ServerLevel serverLevel, LivingEntity target) {
            Vec3 origin = mouth();
            Vec3 f = forward();
            double horizontal = Math.max(1.0D, Math.sqrt(target.distanceToSqr(getX(), target.getY(), getZ())));
            double dy = Mth.clamp((target.getY(0.5D) - origin.y) / horizontal, -0.8D, 0.15D);
            Vec3 dir = new Vec3(f.x, dy, f.z).normalize();

            for (int i = 0; i < 7; i++) {
                double d = 1.0D + random.nextDouble() * (FLAME_RANGE - 1.0D);
                double spread = d * 0.16D;
                double px = origin.x + dir.x * d + (random.nextDouble() - 0.5D) * spread;
                double py = origin.y + dir.y * d + (random.nextDouble() - 0.5D) * spread;
                double pz = origin.z + dir.z * d + (random.nextDouble() - 0.5D) * spread;
                serverLevel.sendParticles(ParticleTypes.FLAME, px, py, pz, 0, dir.x, dir.y, dir.z, 0.15D);
            }
            if (ticks % 3 == 0) {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, origin.x + dir.x * 4.0D, origin.y + dir.y * 4.0D, origin.z + dir.z * 4.0D,
                        2, 0.6D, 0.4D, 0.6D, 0.01D);
            }
            if (ticks % 6 == 0) {
                playSound(SoundEvents.FIRECHARGE_USE, 1.5F, 0.6F);
            }
            if (ticks % 10 != 0) {
                return;
            }
            AABB area = new AABB(origin, origin).inflate(FLAME_RANGE);
            for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != CoalCrabBossEntity.this && e.isAlive() && !(e instanceof CoalCrabBossEntity))) {
                Vec3 to = victim.position().add(0.0D, victim.getBbHeight() * 0.5D, 0.0D).subtract(origin);
                double dist = to.length();
                if (dist > FLAME_RANGE || dist < 0.1D || to.normalize().dot(dir) < 0.85D) {
                    continue;
                }
                victim.hurt(damageSources().mobAttack(CoalCrabBossEntity.this), FLAME_DAMAGE);
                victim.setSecondsOnFire(4);
            }
        }

        @Override
        public void stop() {
            setAction(ACTION_IDLE);
            flameCooldown = cooldown(220);
            globalCooldown = 30;
        }
    }

    // ------------------------------------------------------------------
    // Cada tick (solo servidor): tiempos de espera y barra de vida
    // ------------------------------------------------------------------

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (globalCooldown > 0) {
            globalCooldown--;
        }
        if (fireballCooldown > 0) {
            fireballCooldown--;
        }
        if (chargeCooldown > 0) {
            chargeCooldown--;
        }
        if (flameCooldown > 0) {
            flameCooldown--;
        }
        this.bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    /** Cliente: cuenta cuanto lleva la accion actual para animar el modelo. */
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            int action = getAction();
            if (action != clientAction) {
                clientAction = action;
                clientActionTicks = 0;
            } else {
                clientActionTicks++;
            }
        }
    }

    // ------------------------------------------------------------------
    // Barra de vida del jefe
    // ------------------------------------------------------------------

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(getDisplayName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (hasCustomName()) {
            this.bossEvent.setName(getDisplayName());
        }
        if (tag.contains("HomeX")) {
            this.homePos = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.homePos != null) {
            tag.putInt("HomeX", this.homePos.getX());
            tag.putInt("HomeY", this.homePos.getY());
            tag.putInt("HomeZ", this.homePos.getZ());
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // ------------------------------------------------------------------
    // Golpes y danos
    // ------------------------------------------------------------------

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            target.setSecondsOnFire(MELEE_FIRE_SECONDS);
            playSound(SoundEvents.DEEPSLATE_BREAK, 2.0F, 0.6F);
        }
        return hit;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Mientras despierta es invencible; sin dano por fuego ni por caida
        if (getAction() == ACTION_AWAKEN && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FALL)) {
            clearFire();
            return false;
        }
        return super.hurt(source, amount);
    }

    // ------------------------------------------------------------------
    // Sonidos
    // ------------------------------------------------------------------

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.DEEPSLATE_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.DEEPSLATE_STEP, 1.5F, 0.5F);
    }
}
