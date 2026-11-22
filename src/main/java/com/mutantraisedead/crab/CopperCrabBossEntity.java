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
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cangrejo Titan de Cobre: el jefe de la guarida de cobre (3 de ancho y 6 de alto, igual que el de carbon; el modelo
 * es el mismo modelo grande de Blockbench, con la textura de cobre). Duerme como un huevo en el centro de la guarida
 * (ver CrabEggBlock).
 *
 * Ataques:
 *  - Rayos de colores: junta energia en las pinzas (particulas del color que va a tirar), marca en el suelo donde va a caer
 *    y despues cae el rayo. Blanco = normal, violeta = Electrocucion, amarillo = Paralisis, rojo = mas dano,
 *    azul = Mana anulado, verde = no te lastima: cura al jefe (y a quien este pegado a el).
 *  - Embestida: se prepara, corre en linea recta y se lleva puesto a quien se cruce; si choca una pared se aturde.
 *  - Golpe normal cuando esta cerca.
 * Al morir suelta bloques de cobre y el huevo con imperfecciones (ver data/mutantraisedead/loot_tables/entities/copper_crab_boss.json).
 */
public class CopperCrabBossEntity extends PathfinderMob {
    public static final int ACTION_IDLE = 0;
    public static final int ACTION_AWAKEN = 1;
    public static final int ACTION_BOLT_WINDUP = 2;
    public static final int ACTION_BOLT_CAST = 3;
    public static final int ACTION_CHARGE_WINDUP = 4;
    public static final int ACTION_CHARGING = 5;
    public static final int ACTION_STUNNED = 6;

    /** Cuanto se agranda el modelo (el modelo mide 5,25 de alto; x1,14 queda en 6). El renderer y las particulas de las pinzas usan este numero. */
    public static final float MODEL_SCALE = 1.14F;
    /** Cuanto levanta las pinzas al juntar el rayo (radianes; es la misma pose que usa el jefe de carbon para las bolas de fuego). */
    public static final float CLAW_RAISE = 0.9F;

    private static final EntityDataAccessor<Integer> DATA_ACTION =
            SynchedEntityData.defineId(CopperCrabBossEntity.class, EntityDataSerializers.INT);

    /** Cuanto tarda en salir y rugir al despertar (3 segundos). */
    public static final int AWAKEN_TICKS = 60;

    // Ajustes de los ataques (se pueden tocar aca)
    private static final int BOLT_WINDUP = 36;
    private static final int BOLT_WINDUP_ENRAGED = 28;
    private static final int BOLT_AFTER = 12;
    private static final double BOLT_RADIUS = 2.4D;
    private static final float CHARGE_DAMAGE = 14.0F;
    private static final int CHARGE_WINDUP = 26;
    private static final int CHARGE_MAX_TICKS = 34;
    private static final int STUN_TICKS = 50;
    /** Distancia (en bloques) a la que el rayo verde tambien cura a un jugador pegado al jefe. */
    private static final double GREEN_NEAR = 4.0D;

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

    /** Solo cliente: ticks que lleva la accion actual (para animar el modelo). */
    public int clientActionTicks;
    private int clientAction = ACTION_IDLE;

    private int awakenLeft;
    private int globalCooldown = 40;
    private int boltCooldown = 30;
    private int chargeCooldown = 110;
    @Nullable
    private BlockPos homePos;

    public CopperCrabBossEntity(EntityType<? extends CopperCrabBossEntity> type, Level level) {
        super(type, level);
        this.xpReward = 150;
        this.setPersistenceRequired();
        if (!level.isClientSide) {
            AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.setBaseValue(Config.COPPER_BOSS_HEALTH.get());
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
        return 5.0F;
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
        this.goalSelector.addGoal(1, new BoltGoal());
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

    /**
     * Donde esta la punta de una pinza (side = 1 la derecha, -1 la izquierda) con las pinzas levantadas (CLAW_RAISE).
     * Son las medidas del modelo en pixeles de Blockbench (la punta esta a 17,4 al costado, 60,3 adelante y 84,8 de alto
     * con las pinzas levantadas 0,9 radianes), por el tamano del modelo.
     */
    private Vec3 clawTip(int side) {
        double yaw = Math.toRadians(this.yBodyRot);
        double unit = MODEL_SCALE / 16.0D;
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double rx = -Math.cos(yaw);
        double rz = -Math.sin(yaw);
        double ahead = 60.3D * unit;
        double lateral = 17.4D * unit * side;
        return new Vec3(getX() + fx * ahead + rx * lateral, getY() + 84.8D * unit, getZ() + fz * ahead + rz * lateral);
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
            playSound(SoundEvents.RAVAGER_ROAR, 3.0F, 0.6F);
        }

        @Override
        public void tick() {
            getNavigation().stop();
            awakenLeft--;
            if (level() instanceof ServerLevel serverLevel && awakenLeft % 4 == 0) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COPPER_BLOCK.defaultBlockState()),
                        getX(), getY() + 0.2D, getZ(), 24, 1.6D, 0.1D, 1.6D, 0.1D);
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 2.5D, getZ(), 16, 1.8D, 2.0D, 1.8D, 0.2D);
                playSound(SoundEvents.COPPER_BREAK, 2.0F, 0.5F);
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
            super(CopperCrabBossEntity.this, 1.0D, true);
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

    // ------------------------------------------------------------------
    // Ataque 1: rayos de colores
    // ------------------------------------------------------------------

    /** Elige el color del proximo rayo. El verde (curacion) solo sale si el jefe esta herido. */
    private BoltColor pickColor() {
        List<BoltColor> pool = new ArrayList<>();
        for (BoltColor color : BoltColor.values()) {
            int weight = 2;
            if (color == BoltColor.GREEN && getHealth() > getMaxHealth() * 0.9F) {
                weight = 0;
            }
            for (int i = 0; i < weight; i++) {
                pool.add(color);
            }
        }
        return pool.get(random.nextInt(pool.size()));
    }

    /**
     * Junta energia en las pinzas (con particulas del color del rayo), marca en el suelo donde va a caer
     * (hay tiempo para correr) y despues cae el rayo. Con menos de la mitad de vida tira dos rayos.
     */
    private class BoltGoal extends Goal {
        private int ticks;
        private boolean struck;
        private boolean finished;
        private BoltColor color = BoltColor.WHITE;
        private final List<Vec3> spots = new ArrayList<>();

        BoltGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            if (!readyForAbility(target) || boltCooldown > 0 || random.nextInt(3) != 0) {
                return false;
            }
            double distance = distanceTo(target);
            return distance >= 2.5D && distance <= 30.0D;
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
            struck = false;
            finished = false;
            spots.clear();
            color = pickColor();
            setAction(ACTION_BOLT_WINDUP);
            getNavigation().stop();
            playSound(SoundEvents.BEACON_ACTIVATE, 2.0F, 1.6F);
        }

        @Override
        public void tick() {
            getNavigation().stop();
            LivingEntity target = getTarget();
            ticks++;
            int windup = isEnraged() ? BOLT_WINDUP_ENRAGED : BOLT_WINDUP;

            if (!struck) {
                if (target != null && color != BoltColor.GREEN) {
                    faceToward(target, 20.0F);
                }
                if (level() instanceof ServerLevel serverLevel) {
                    if (ticks % 2 == 0) {
                        gatherEnergy(serverLevel);
                    }
                    if (ticks == windup - 16) {
                        lockSpots(target);
                    }
                    if (ticks > windup - 16 && ticks % 2 == 0) {
                        markSpots(serverLevel);
                    }
                    if (ticks % 12 == 0) {
                        playSound(SoundEvents.BEACON_AMBIENT, 1.5F, 1.8F);
                    }
                    if (ticks >= windup) {
                        if (spots.isEmpty()) {
                            lockSpots(target);
                        }
                        for (Vec3 spot : spots) {
                            strikeAt(serverLevel, color, spot);
                        }
                        struck = true;
                        ticks = 0;
                        setAction(ACTION_BOLT_CAST);
                    }
                }
                return;
            }
            if (ticks >= BOLT_AFTER) {
                finished = true;
            }
        }

        /** Particulas del color del rayo en las dos pinzas: asi se sabe cual viene. */
        private void gatherEnergy(ServerLevel serverLevel) {
            for (int side = -1; side <= 1; side += 2) {
                Vec3 tip = clawTip(side);
                serverLevel.sendParticles(color.dust(1.5F), tip.x, tip.y, tip.z, 5, 0.18D, 0.18D, 0.18D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, tip.x, tip.y, tip.z, 1, 0.12D, 0.12D, 0.12D, 0.03D);
            }
        }

        /** Se fija donde va a caer: donde esta el jugador (o, con poca vida, tambien un poco adelante de su camino). El verde cae sobre el jefe. */
        private void lockSpots(@Nullable LivingEntity target) {
            spots.clear();
            if (color == BoltColor.GREEN || target == null) {
                spots.add(position());
                return;
            }
            spots.add(target.position());
            if (isEnraged()) {
                Vec3 ahead = target.position().add(target.getDeltaMovement().scale(14.0D));
                spots.add(new Vec3(ahead.x, target.getY(), ahead.z));
            }
        }

        /** Circulo del color en el suelo donde va a caer el rayo. */
        private void markSpots(ServerLevel serverLevel) {
            for (Vec3 spot : spots) {
                for (int i = 0; i < 12; i++) {
                    double angle = Math.PI * 2.0D * i / 12.0D + ticks * 0.1D;
                    serverLevel.sendParticles(color.dust(1.0F), spot.x + Math.cos(angle) * BOLT_RADIUS, spot.y + 0.1D,
                            spot.z + Math.sin(angle) * BOLT_RADIUS, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }

        @Override
        public void stop() {
            setAction(ACTION_IDLE);
            boltCooldown = cooldown(80);
            globalCooldown = 25;
        }
    }

    /** Cae un rayo: se ve, suena y le hace su efecto a todos los que esten adentro del circulo. */
    private void strikeAt(ServerLevel level, BoltColor color, Vec3 pos) {
        ColoredBoltEntity bolt = ModEntities.COLORED_BOLT.get().create(level);
        if (bolt != null) {
            bolt.setColor(color);
            bolt.moveTo(pos.x, pos.y, pos.z);
            level.addFreshEntity(bolt);
        }
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 4.0F, 1.0F);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 3.0F, 1.0F);
        level.sendParticles(color.dust(2.0F), pos.x, pos.y + 0.5D, pos.z, 30, 0.8D, 0.8D, 0.8D, 0.0D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 0.5D, pos.z, 20, 1.0D, 0.8D, 1.0D, 0.3D);

        if (color == BoltColor.GREEN) {
            greenBolt(level);
            return;
        }
        AABB area = new AABB(pos.x - BOLT_RADIUS, pos.y - 1.0D, pos.z - BOLT_RADIUS, pos.x + BOLT_RADIUS, pos.y + 4.0D, pos.z + BOLT_RADIUS);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && !(e instanceof CopperCrabBossEntity))) {
            applyBolt(level, victim, color);
        }
    }

    /** El dano y el efecto de cada color (los tiempos se achican o se agrandan segun cuantas piezas de metal lleve puestas). */
    private void applyBolt(ServerLevel level, LivingEntity victim, BoltColor color) {
        double base = Config.COPPER_BOLT_DAMAGE.get();
        int metal = MetalArmor.pieces(victim);
        double damage = base;
        if (color == BoltColor.RED) {
            // 5 % mas de dano que el blanco, y hasta 10 % con armadura de metal
            damage = base * (1.0D + MetalArmor.scale(Config.RED_BONUS_PERCENT.get(), Config.RED_METAL_BONUS_PERCENT.get(), metal) / 100.0D);
        }
        victim.hurt(damageSources().mobAttack(this), (float) damage);
        if (!victim.isAlive()) {
            return;
        }
        switch (color) {
            case VIOLET -> {
                int seconds = (int) Math.round(MetalArmor.scale(Config.ELECTRO_SECONDS.get(), Config.ELECTRO_METAL_SECONDS.get(), metal));
                victim.addEffect(new MobEffectInstance(ModEffects.ELECTROCUTION.get(), seconds * 20));
            }
            case YELLOW -> {
                int seconds = (int) Math.round(MetalArmor.scale(Config.PARALYSIS_SECONDS.get(), Config.PARALYSIS_METAL_SECONDS.get(), metal));
                victim.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), seconds * 20));
            }
            case BLUE -> victim.addEffect(new MobEffectInstance(ModEffects.MANA_LOCK.get(), Config.MANA_LOCK_SECONDS.get() * 20));
            default -> {
            }
        }
        level.sendParticles(color.dust(1.6F), victim.getX(), victim.getY() + victim.getBbHeight() * 0.5D, victim.getZ(),
                14, 0.4D, 0.6D, 0.4D, 0.0D);
    }

    /** Rayo verde: cae sobre el propio jefe y lo cura; un jugador pegado al jefe tambien recibe el rayo y se cura. */
    private void greenBolt(ServerLevel level) {
        heal(getMaxHealth() * Config.GREEN_HEAL_PERCENT.get() / 100.0F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 3.0D, getZ(), 40, 1.6D, 2.2D, 1.6D, 0.0D);
        for (Player player : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(GREEN_NEAR, 1.5D, GREEN_NEAR),
                p -> p.isAlive() && !p.isSpectator())) {
            player.heal(Config.GREEN_PLAYER_HEAL.get().floatValue());
            level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + player.getBbHeight(), player.getZ(), 6, 0.4D, 0.4D, 0.4D, 0.0D);
        }
    }

    // ------------------------------------------------------------------
    // Ataque 2: embestida
    // ------------------------------------------------------------------

    /** Embestida: se prepara, corre en linea recta y se aturde si choca con una pared. */
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
            playSound(SoundEvents.RAVAGER_ROAR, 2.5F, 0.8F);
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
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 0.5D, getZ(), 10, 1.6D, 0.2D, 1.6D, 0.2D);
                    playSound(SoundEvents.COPPER_STEP, 1.5F, 0.5F);
                }
                if (ticks >= CHARGE_WINDUP) {
                    Vec3 f = forward();
                    direction = f.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : f.normalize();
                    setAction(ACTION_CHARGING);
                    ticks = 0;
                    playSound(SoundEvents.RAVAGER_ATTACK, 2.5F, 0.7F);
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
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COPPER_BLOCK.defaultBlockState()),
                            getX(), getY() + 0.1D, getZ(), 6, 1.4D, 0.1D, 1.4D, 0.05D);
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 0.6D, getZ(), 4, 1.4D, 0.5D, 1.4D, 0.2D);
                    if (ticks % 4 == 0) {
                        playSound(SoundEvents.COPPER_STEP, 2.0F, 0.5F);
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
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 5.0D, getZ(), 6, 0.9D, 0.4D, 0.9D, 0.1D);
            }
            if (stunLeft <= 0) {
                finished = true;
            }
        }

        private void hitWall() {
            setAction(ACTION_STUNNED);
            stunLeft = STUN_TICKS;
            setDeltaMovement(Vec3.ZERO);
            playSound(SoundEvents.GENERIC_EXPLODE, 2.5F, 0.7F);
            if (level() instanceof ServerLevel serverLevel) {
                Vec3 front = position().add(direction.x * 2.2D, 1.5D, direction.z * 2.2D);
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OXIDIZED_CUT_COPPER.defaultBlockState()),
                        front.x, front.y, front.z, 60, 1.0D, 1.2D, 1.0D, 0.15D);
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, front.x, front.y, front.z, 3, 0.8D, 0.8D, 0.8D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, front.x, front.y, front.z, 30, 1.0D, 1.0D, 1.0D, 0.4D);
            }
        }

        /** Se lleva puesto a quien este en el camino (una sola vez cada uno). */
        private void damageInPath() {
            AABB box = getBoundingBox().inflate(0.35D, 0.0D, 0.35D);
            for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != CopperCrabBossEntity.this && e.isAlive() && !(e instanceof CopperCrabBossEntity))) {
                if (!hit.add(victim.getId())) {
                    continue;
                }
                victim.hurt(damageSources().mobAttack(CopperCrabBossEntity.this), CHARGE_DAMAGE);
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

    // ------------------------------------------------------------------
    // Cada tick: tiempos de espera, barra de vida y chispas
    // ------------------------------------------------------------------

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (globalCooldown > 0) {
            globalCooldown--;
        }
        if (boltCooldown > 0) {
            boltCooldown--;
        }
        if (chargeCooldown > 0) {
            chargeCooldown--;
        }
        this.bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    /** Cliente: cuenta cuanto lleva la accion actual para animar el modelo, larga chispas sueltas y hace sonar la musica del jefe. */
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            BossMusicClient.tick(this, ModSounds.COPPER_BOSS_MUSIC.get());
            int action = getAction();
            if (action != clientAction) {
                clientAction = action;
                clientActionTicks = 0;
            } else {
                clientActionTicks++;
            }
            if (isAlive() && random.nextInt(6) == 0) {
                level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                        getX() + (random.nextDouble() - 0.5D) * 3.0D, getY() + 0.5D + random.nextDouble() * 5.0D,
                        getZ() + (random.nextDouble() - 0.5D) * 3.0D, 0.0D, 0.0D, 0.0D);
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
            playSound(SoundEvents.COPPER_BREAK, 2.0F, 0.6F);
        }
        return hit;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Mientras despierta es invencible; sin dano por fuego, caida ni rayos (el es el de los rayos)
        if (getAction() == ACTION_AWAKEN && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_LIGHTNING)) {
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
        return SoundEvents.COPPER_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.COPPER_STEP, 1.5F, 0.5F);
    }
}
