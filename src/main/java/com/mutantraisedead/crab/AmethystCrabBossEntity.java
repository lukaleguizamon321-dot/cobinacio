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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Cangrejo Titan de Amatista: el jefe de la guarida geoda (basado en el jefe de cristal del Hollow Knight).
 * Duerme como un huevo en el centro de la guarida (ver CrabEggBlock). No tiene embestida.
 *
 * Ataques:
 *  - Laser lento: apunta al jugador con una pinza y junta energia (se ve y se oye). Despues dispara un rayo que sigue al
 *    jugador, pero gira despacio (grados por tick = LASER_TURN): si corres sin parar alrededor del jefe, lo esquivas.
 *  - Linea de pinchos: golpea el suelo y sale una fila de pinchos de cristal en linea recta hacia el jugador
 *    (son entidades, ver CrystalSpikeEntity; desaparecen a los 5 segundos).
 *  - Ataque del centro: se va al centro de la guarida (donde aparecio) y hace una de 3 variantes:
 *      1) un laser que gira (5 vueltas),  2) dos laseres que giran (5 vueltas),
 *      3) no gira: tira amatistas al azar en un area de 12 bloques (ver AmethystShardEntity).
 *  - Golpe normal cuando esta cerca.
 * Si algo le tapa el camino (por ejemplo al ir al centro) rompe los bloques que estorban.
 * Al morir suelta bloques de amatista y el huevo con imperfecciones (ver loot_tables/entities/amethyst_crab_boss.json).
 */
public class AmethystCrabBossEntity extends PathfinderMob {
    public static final int ACTION_IDLE = 0;
    public static final int ACTION_AWAKEN = 1;
    public static final int ACTION_LASER_AIM = 2;
    public static final int ACTION_LASER_FIRE = 3;
    public static final int ACTION_SPIKES = 4;
    public static final int ACTION_CENTER_WALK = 5;
    public static final int ACTION_CENTER_CHARGE = 6;
    public static final int ACTION_CENTER_LASER = 7;
    public static final int ACTION_CENTER_SHOWER = 8;

    /** Cuanto se agranda el modelo. 1 = tal como lo hiciste en Blockbench (mide unos 8 bloques de alto con los cristales). */
    public static final float MODEL_SCALE = 1.0F;
    /** Pose de la pinza que apunta el laser (el modelo la usa; el origen del rayo sale de estas medidas). */
    public static final float AIM_TIP = 0.3F;
    public static final float AIM_OUTER = 0.4F;
    /** Donde queda la punta de esa pinza (en bloques, con el modelo a escala 1): adelante, al costado y arriba. */
    private static final double CLAW_AHEAD = 4.93D;
    private static final double CLAW_SIDE = 0.96D;
    private static final double CLAW_UP = 3.13D;

    /** Valores de BEAM_MODE: ningun rayo, laser apuntado, un laser del centro, dos lasers del centro. */
    public static final int BEAM_NONE = 0;
    public static final int BEAM_AIM = 1;
    public static final int BEAM_CENTER_ONE = 2;
    public static final int BEAM_CENTER_TWO = 3;
    /** Hasta donde llega un rayo (si no choca antes con un bloque). */
    public static final double BEAM_RANGE = 48.0D;

    private static final EntityDataAccessor<Integer> DATA_ACTION =
            SynchedEntityData.defineId(AmethystCrabBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_AIM_SIDE =
            SynchedEntityData.defineId(AmethystCrabBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BEAM_MODE =
            SynchedEntityData.defineId(AmethystCrabBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_BEAM_YAW =
            SynchedEntityData.defineId(AmethystCrabBossEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_BEAM_PITCH =
            SynchedEntityData.defineId(AmethystCrabBossEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_BEAM_LEN =
            SynchedEntityData.defineId(AmethystCrabBossEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_BEAM_LEN2 =
            SynchedEntityData.defineId(AmethystCrabBossEntity.class, EntityDataSerializers.FLOAT);

    /** Cuanto tarda en salir y rugir al despertar (3 segundos). */
    public static final int AWAKEN_TICKS = 60;

    // Ajustes de los ataques (se pueden tocar aca)
    /** Laser apuntado: ticks de carga, ticks disparando, y cuantos grados por tick gira el rayo. Mas chico = mas facil de esquivar corriendo. */
    private static final int LASER_AIM_TICKS = 34;
    private static final int LASER_AIM_TICKS_ENRAGED = 26;
    private static final int LASER_FIRE_TICKS = 66;
    private static final float LASER_TURN = 0.8F;
    private static final float LASER_TURN_ENRAGED = 1.0F;
    /** Laser del centro: grados por tick que gira (360 / 3 = 120 ticks = 6 segundos por vuelta; 5 vueltas = 30 segundos). */
    private static final float CENTER_SPIN = 3.0F;
    private static final int CENTER_CHARGE_TICKS = 44;
    private static final int CENTER_SHOWER_TICKS = 160;
    /** Cada cuantos ticks tira una amatista en la variante 3. */
    private static final int SHOWER_INTERVAL = 4;
    private static final double SHOWER_RADIUS = 12.0D;
    /** Cuantos ticks tiene que esperar el rayo para volver a lastimar a la misma victima. */
    private static final int BEAM_REHIT_TICKS = 20;
    private static final int SPIKE_WINDUP = 14;

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    /** Solo cliente: ticks que lleva la accion actual (para animar el modelo). */
    public int clientActionTicks;
    private int clientAction = ACTION_IDLE;
    /** Solo cliente: angulos del rayo en el tick anterior (para dibujarlo suave). */
    public float beamYawO;
    public float beamPitchO;
    private float beamYawNow;
    private float beamPitchNow;

    private int awakenLeft;
    private int globalCooldown = 40;
    private int laserCooldown = 40;
    private int spikeCooldown = 90;
    private int centerCooldown = 500;
    private int stuckTicks;
    private final Map<Integer, Integer> beamHits = new HashMap<>();
    @Nullable
    private BlockPos homePos;

    public AmethystCrabBossEntity(EntityType<? extends AmethystCrabBossEntity> type, Level level) {
        super(type, level);
        this.xpReward = 180;
        this.setPersistenceRequired();
        if (!level.isClientSide) {
            AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.setBaseValue(Config.AMETHYST_BOSS_HEALTH.get());
                setHealth(getMaxHealth());
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
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
        this.entityData.define(DATA_AIM_SIDE, 0);
        this.entityData.define(DATA_BEAM_MODE, BEAM_NONE);
        this.entityData.define(DATA_BEAM_YAW, 0.0F);
        this.entityData.define(DATA_BEAM_PITCH, 0.0F);
        this.entityData.define(DATA_BEAM_LEN, 0.0F);
        this.entityData.define(DATA_BEAM_LEN2, 0.0F);
    }

    public int getAction() {
        return this.entityData.get(DATA_ACTION);
    }

    private void setAction(int action) {
        this.entityData.set(DATA_ACTION, action);
    }

    /** Que pinza apunta el laser: 0 = la derecha (clawP), 1 = la izquierda (clawN). */
    public int getAimSide() {
        return this.entityData.get(DATA_AIM_SIDE);
    }

    public int getBeamMode() {
        return this.entityData.get(DATA_BEAM_MODE);
    }

    public float getBeamYaw() {
        return this.entityData.get(DATA_BEAM_YAW);
    }

    public float getBeamPitch() {
        return this.entityData.get(DATA_BEAM_PITCH);
    }

    public float getBeamLength(int slot) {
        return this.entityData.get(slot == 0 ? DATA_BEAM_LEN : DATA_BEAM_LEN2);
    }

    private void clearBeam() {
        this.entityData.set(DATA_BEAM_MODE, BEAM_NONE);
        this.entityData.set(DATA_BEAM_LEN, 0.0F);
        this.entityData.set(DATA_BEAM_LEN2, 0.0F);
        beamHits.clear();
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return 5.0F;
    }

    @Override
    public float maxUpStep() {
        return 1.2F;
    }

    /** Los rayos son largos: que se sigan dibujando aunque el jefe quede fuera de la pantalla. */
    @Override
    public AABB getBoundingBoxForCulling() {
        return getBeamMode() == BEAM_NONE ? super.getBoundingBoxForCulling() : getBoundingBox().inflate(BEAM_RANGE);
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
    // Medidas y ayudas
    // ------------------------------------------------------------------

    /**
     * Donde esta la punta de la pinza que apunta (aimSide 0 = derecha, 1 = izquierda), respecto de los pies del jefe,
     * segun para donde mira el cuerpo. Son las medidas de la pose AIM_TIP / AIM_OUTER del modelo.
     */
    public static Vec3 clawTipOffset(float bodyYawDeg, int aimSide) {
        double yaw = Math.toRadians(bodyYawDeg);
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double rx = -Math.cos(yaw);
        double rz = -Math.sin(yaw);
        double side = aimSide == 0 ? 1.0D : -1.0D;
        double ahead = CLAW_AHEAD * MODEL_SCALE;
        double lateral = CLAW_SIDE * MODEL_SCALE * side;
        return new Vec3(fx * ahead + rx * lateral, CLAW_UP * MODEL_SCALE, fz * ahead + rz * lateral);
    }

    /** De donde sale un rayo del centro (el segundo sale al lado contrario): un poco afuera del cuerpo y bajo, pegado al piso. */
    public static Vec3 centerBeamOffset(float yawDeg) {
        double yaw = Math.toRadians(yawDeg);
        return new Vec3(-Math.sin(yaw) * 2.2D, 0.6D, Math.cos(yaw) * 2.2D);
    }

    public static Vec3 beamDirection(float yawDeg, float pitchDeg) {
        float yaw = yawDeg * ((float) Math.PI / 180.0F);
        float pitch = pitchDeg * ((float) Math.PI / 180.0F);
        float cosPitch = Mth.cos(pitch);
        return new Vec3(-Mth.sin(yaw) * cosPitch, -Mth.sin(pitch), Mth.cos(yaw) * cosPitch);
    }

    private static float yawTo(Vec3 v) {
        return (float) (Mth.atan2(v.z, v.x) * 57.29577951308232D) - 90.0F;
    }

    private static float pitchTo(Vec3 v) {
        double horizontal = Math.sqrt(v.x * v.x + v.z * v.z);
        return (float) -(Mth.atan2(v.y, horizontal) * 57.29577951308232D);
    }

    /** Acerca un angulo (en grados) a otro, como mucho max grados, por el camino mas corto. */
    private static float approachAngle(float current, float wanted, float max) {
        return current + Mth.clamp(Mth.wrapDegrees(wanted - current), -max, max);
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

    /** Gira el cuerpo hacia un angulo, como mucho maxStep grados por tick. */
    private void turnBodyTo(float wantedYaw, float maxStep) {
        float diff = Mth.clamp(Mth.wrapDegrees(wantedYaw - getYRot()), -maxStep, maxStep);
        float yaw = getYRot() + diff;
        setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    private void faceToward(double x, double z, float maxStep) {
        turnBodyTo((float) (Mth.atan2(z - getZ(), x - getX()) * 57.29577951308232D) - 90.0F, maxStep);
    }

    private void faceToward(LivingEntity target, float maxStep) {
        faceToward(target.getX(), target.getZ(), maxStep);
    }

    private boolean readyForAbility(@Nullable LivingEntity target) {
        return target != null && target.isAlive() && !target.isSpectator()
                && getAction() == ACTION_IDLE && globalCooldown <= 0;
    }

    /** Altura del piso cerca de (x, z): busca el primer hueco con suelo abajo, un poco arriba y abajo de los pies del jefe. Devuelve NaN si no hay. */
    private double groundAt(double x, double z, double aroundY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(aroundY) + 2, Mth.floor(z));
        for (int i = 0; i < 7; i++) {
            BlockState here = level().getBlockState(pos);
            BlockState below = level().getBlockState(pos.below());
            if (here.getCollisionShape(level(), pos).isEmpty() && !below.getCollisionShape(level(), pos.below()).isEmpty()) {
                return pos.getY();
            }
            pos.move(0, -1, 0);
        }
        return Double.NaN;
    }

    private DamageSource magicHit() {
        return damageSources().indirectMagic(this, this);
    }

    private boolean isVictim(Entity e) {
        return e instanceof LivingEntity living && living.isAlive() && !living.isSpectator() && !(e instanceof AmethystCrabBossEntity);
    }

    // ------------------------------------------------------------------
    // Rayos (laser): dano y largo
    // ------------------------------------------------------------------

    /**
     * Un rayo desde origin hacia (yaw, pitch): se corta en el primer bloque, lastima a todos los que toca
     * (una vez por segundo cada uno) y devuelve su largo.
     */
    private float fireBeam(ServerLevel serverLevel, Vec3 origin, float yaw, float pitch) {
        Vec3 dir = beamDirection(yaw, pitch);
        Vec3 end = origin.add(dir.scale(BEAM_RANGE));
        BlockHitResult hit = serverLevel.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 realEnd = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
        float length = (float) origin.distanceTo(realEnd);

        AABB bounds = new AABB(origin, realEnd).inflate(1.5D);
        float damage = Config.AMETHYST_LASER_DAMAGE.get().floatValue();
        for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, bounds, this::isVictim)) {
            AABB box = victim.getBoundingBox().inflate(0.35D);
            if (!box.contains(origin) && box.clip(origin, realEnd).isEmpty()) {
                continue;
            }
            Integer last = beamHits.get(victim.getId());
            if (last != null && tickCount - last < BEAM_REHIT_TICKS) {
                continue;
            }
            beamHits.put(victim.getId(), tickCount);
            victim.hurt(magicHit(), damage);
            serverLevel.sendParticles(BoltColor.VIOLET.dust(1.6F), victim.getX(), victim.getY() + victim.getBbHeight() * 0.5D, victim.getZ(),
                    14, 0.4D, 0.6D, 0.4D, 0.0D);
        }
        if (hit.getType() != HitResult.Type.MISS && tickCount % 2 == 0) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState()),
                    realEnd.x, realEnd.y, realEnd.z, 4, 0.25D, 0.25D, 0.25D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.END_ROD, realEnd.x, realEnd.y, realEnd.z, 1, 0.2D, 0.2D, 0.2D, 0.02D);
        }
        return length;
    }

    private void setBeam(int mode, float yaw, float pitch, float len, float len2) {
        this.entityData.set(DATA_BEAM_MODE, mode);
        this.entityData.set(DATA_BEAM_YAW, yaw);
        this.entityData.set(DATA_BEAM_PITCH, pitch);
        this.entityData.set(DATA_BEAM_LEN, len);
        this.entityData.set(DATA_BEAM_LEN2, len2);
    }

    // ------------------------------------------------------------------
    // Romper bloques cuando el camino esta tapado
    // ------------------------------------------------------------------

    /** Rompe lo que le tapa el paso (hacia donde mira dx, dz). Respeta la regla "los mobs rompen bloques". */
    private void breakBlocksAhead(double dx, double dz) {
        if (!(level() instanceof ServerLevel serverLevel) || !serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 1.0E-4D) {
            return;
        }
        double ux = dx / len;
        double uz = dz / len;
        double cx = getX() + ux * (getBbWidth() * 0.5D + 0.9D);
        double cz = getZ() + uz * (getBbWidth() * 0.5D + 0.9D);
        int minY = Mth.floor(getY());
        int maxY = Mth.floor(getY() + 4.5D);
        int broken = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = Mth.floor(cx - 1.5D); x <= Mth.floor(cx + 1.5D); x++) {
            for (int z = Mth.floor(cz - 1.5D); z <= Mth.floor(cz + 1.5D); z++) {
                for (int y = minY; y <= maxY; y++) {
                    pos.set(x, y, z);
                    BlockState state = serverLevel.getBlockState(pos);
                    if (state.isAir() || state.getDestroySpeed(serverLevel, pos) < 0.0F || state.is(Blocks.SPAWNER)) {
                        continue;
                    }
                    if (serverLevel.destroyBlock(pos, false, this)) {
                        broken++;
                    }
                }
            }
        }
        if (broken > 0) {
            playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 2.5F, 0.6F);
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState()),
                    cx, getY() + 1.5D, cz, 30, 1.2D, 1.2D, 1.2D, 0.1D);
        }
    }

    // ------------------------------------------------------------------
    // Metas (IA)
    // ------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new AwakenGoal());
        this.goalSelector.addGoal(1, new CenterGoal());
        this.goalSelector.addGoal(1, new LaserGoal());
        this.goalSelector.addGoal(1, new SpikesGoal());
        this.goalSelector.addGoal(3, new BossMeleeGoal());
        this.goalSelector.addGoal(4, new ReturnHomeGoal());
        this.goalSelector.addGoal(5, new FloatGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
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
            playSound(SoundEvents.RAVAGER_ROAR, 3.0F, 0.7F);
        }

        @Override
        public void tick() {
            getNavigation().stop();
            awakenLeft--;
            if (level() instanceof ServerLevel serverLevel && awakenLeft % 4 == 0) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState()),
                        getX(), getY() + 0.2D, getZ(), 24, 1.8D, 0.1D, 1.8D, 0.1D);
                serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 3.0D, getZ(), 10, 2.0D, 2.4D, 2.0D, 0.08D);
                playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 3.0F, 0.6F);
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
            super(AmethystCrabBossEntity.this, 1.0D, true);
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
    // Ataque 1: laser lento
    // ------------------------------------------------------------------

    /**
     * Apunta con una pinza y junta energia; despues dispara un rayo que sigue al jugador, pero gira despacio
     * (LASER_TURN grados por tick). Corriendo sin parar alrededor del jefe se esquiva.
     */
    private class LaserGoal extends Goal {
        private int ticks;
        private boolean firing;
        private boolean finished;
        private float beamYaw;
        private float beamPitch;

        LaserGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            if (!readyForAbility(target) || laserCooldown > 0 || random.nextInt(3) != 0) {
                return false;
            }
            double distance = distanceTo(target);
            return distance >= 5.0D && distance <= 36.0D && hasLineOfSight(target);
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
            firing = false;
            finished = false;
            beamHits.clear();
            LivingEntity target = getTarget();
            if (target != null) {
                // usa la pinza del lado donde esta el jugador
                float rad = getYRot() * ((float) Math.PI / 180.0F);
                Vec3 rel = target.position().subtract(position());
                double right = rel.x * -Mth.cos(rad) + rel.z * -Mth.sin(rad);
                entityData.set(DATA_AIM_SIDE, right >= 0.0D ? 0 : 1);
            }
            setAction(ACTION_LASER_AIM);
            getNavigation().stop();
            playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 3.0F, 0.6F);
        }

        @Override
        public void tick() {
            getNavigation().stop();
            LivingEntity target = getTarget();
            ticks++;
            Vec3 tipOffset = clawTipOffset(yBodyRot, getAimSide());
            Vec3 origin = position().add(tipOffset);

            if (!firing) {
                int windup = isEnraged() ? LASER_AIM_TICKS_ENRAGED : LASER_AIM_TICKS;
                if (target != null) {
                    faceToward(target, 25.0F);
                }
                if (level() instanceof ServerLevel serverLevel) {
                    float grow = 0.8F + 1.4F * ticks / windup;
                    serverLevel.sendParticles(BoltColor.VIOLET.dust(grow), origin.x, origin.y, origin.z, 4, 0.2D, 0.2D, 0.2D, 0.0D);
                    serverLevel.sendParticles(ParticleTypes.END_ROD, origin.x, origin.y, origin.z, 1, 0.15D, 0.15D, 0.15D, 0.02D);
                    if (ticks % 8 == 0) {
                        playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 2.0F, 0.5F + 1.0F * ticks / windup);
                    }
                    // en los ultimos ticks marca con puntitos para donde va a salir el rayo
                    if (target != null && ticks > windup - 12 && ticks % 2 == 0) {
                        Vec3 chest = target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D);
                        Vec3 dir = chest.subtract(origin).normalize();
                        for (int i = 2; i < 12; i += 2) {
                            Vec3 p = origin.add(dir.scale(i));
                            serverLevel.sendParticles(BoltColor.VIOLET.dust(0.8F), p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                        }
                    }
                }
                if (ticks >= windup) {
                    if (target == null || !target.isAlive()) {
                        finished = true;
                        return;
                    }
                    Vec3 chest = target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D).subtract(origin);
                    beamYaw = yawTo(chest);
                    beamPitch = pitchTo(chest);
                    firing = true;
                    ticks = 0;
                    setAction(ACTION_LASER_FIRE);
                    playSound(SoundEvents.BEACON_ACTIVATE, 3.0F, 1.8F);
                    playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 3.0F, 1.2F);
                }
                return;
            }

            // Disparando: el rayo sigue al jugador pero gira despacio
            if (target != null && target.isAlive()) {
                Vec3 chest = target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D).subtract(origin);
                float turn = isEnraged() ? LASER_TURN_ENRAGED : LASER_TURN;
                beamYaw = approachAngle(beamYaw, yawTo(chest), turn);
                beamPitch = approachAngle(beamPitch, pitchTo(chest), turn);
            }
            turnBodyTo(beamYaw, 4.0F);
            if (level() instanceof ServerLevel serverLevel) {
                float length = fireBeam(serverLevel, origin, beamYaw, beamPitch);
                setBeam(BEAM_AIM, beamYaw, beamPitch, length, 0.0F);
                if (ticks % 6 == 0) {
                    playSound(SoundEvents.BEACON_AMBIENT, 2.5F, 1.9F);
                }
            }
            if (ticks >= LASER_FIRE_TICKS) {
                finished = true;
            }
        }

        @Override
        public void stop() {
            clearBeam();
            setAction(ACTION_IDLE);
            laserCooldown = cooldown(100);
            globalCooldown = 30;
        }
    }

    // ------------------------------------------------------------------
    // Ataque 2: linea de pinchos
    // ------------------------------------------------------------------

    /** Golpea el suelo y sale una fila de pinchos de cristal en linea recta hacia el jugador. */
    private class SpikesGoal extends Goal {
        private int ticks;
        private int count;
        private boolean finished;
        private Vec3 direction = Vec3.ZERO;

        SpikesGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            if (!readyForAbility(target) || spikeCooldown > 0 || random.nextInt(3) != 0) {
                return false;
            }
            double distance = distanceTo(target);
            return distance >= 3.0D && distance <= 30.0D;
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
            count = 0;
            finished = false;
            setAction(ACTION_SPIKES);
            getNavigation().stop();
            playSound(SoundEvents.AMETHYST_CLUSTER_BREAK, 3.0F, 0.5F);
        }

        @Override
        public void tick() {
            getNavigation().stop();
            LivingEntity target = getTarget();
            ticks++;
            if (ticks < SPIKE_WINDUP && target != null) {
                faceToward(target, 25.0F);
            }
            if (ticks == SPIKE_WINDUP) {
                // la linea queda fija hacia donde esta el jugador en este momento
                Vec3 toTarget = target != null ? target.position().subtract(position()) : forward();
                Vec3 flat = new Vec3(toTarget.x, 0.0D, toTarget.z);
                direction = flat.lengthSqr() < 1.0E-4D ? forward() : flat.normalize();
                double distance = target != null ? flat.length() : 12.0D;
                count = Mth.clamp((int) (distance / 1.5D) + 4, 8, 22);
                playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 3.5F, 0.5F);
                playSound(SoundEvents.GENERIC_EXPLODE, 1.5F, 1.4F);
            }
            if (ticks > SPIKE_WINDUP && level() instanceof ServerLevel serverLevel) {
                int step = ticks - SPIKE_WINDUP;
                if (step % 2 == 0 && step / 2 <= count) {
                    int i = step / 2 - 1;
                    if (i >= 0) {
                        double x = getX() + direction.x * (2.8D + 1.5D * i);
                        double z = getZ() + direction.z * (2.8D + 1.5D * i);
                        double y = groundAt(x, z, getY());
                        if (!Double.isNaN(y)) {
                            CrystalSpikeEntity spike = ModEntities.CRYSTAL_SPIKE.get().create(serverLevel);
                            if (spike != null) {
                                spike.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
                                spike.setOwnerBoss(AmethystCrabBossEntity.this);
                                serverLevel.addFreshEntity(spike);
                            }
                        }
                    }
                }
            }
            if (ticks > SPIKE_WINDUP + count * 2 + 10) {
                finished = true;
            }
        }

        @Override
        public void stop() {
            setAction(ACTION_IDLE);
            spikeCooldown = cooldown(120);
            globalCooldown = 30;
        }
    }

    // ------------------------------------------------------------------
    // Ataque 3: el centro (3 variantes)
    // ------------------------------------------------------------------

    /**
     * Se va al centro de la guarida (donde aparecio) y, segun la variante: 1 = un laser que gira, 2 = dos lasers que giran,
     * 3 = no gira: tira amatistas al azar en un area de 12 bloques.
     */
    private class CenterGoal extends Goal {
        private static final int PHASE_WALK = 0;
        private static final int PHASE_CHARGE = 1;
        private static final int PHASE_FIRE = 2;

        private int phase;
        private int ticks;
        private int variant;
        private int spinTotal;
        private float spinDir;
        private float beamYaw;
        private int shardSide;
        private boolean finished;

        CenterGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            if (!readyForAbility(target) || centerCooldown > 0 || homePos == null) {
                return false;
            }
            double dx = homePos.getX() + 0.5D - getX();
            double dz = homePos.getZ() + 0.5D - getZ();
            return dx * dx + dz * dz < 45.0D * 45.0D && random.nextInt(4) == 0;
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
            phase = PHASE_WALK;
            ticks = 0;
            finished = false;
            stuckTicks = 0;
            variant = 1 + random.nextInt(3);
            beamHits.clear();
            setAction(ACTION_CENTER_WALK);
            getNavigation().stop();
            playSound(SoundEvents.RAVAGER_ROAR, 2.5F, 0.9F);
        }

        @Override
        public void tick() {
            ticks++;
            if (homePos == null) {
                finished = true;
                return;
            }
            double homeX = homePos.getX() + 0.5D;
            double homeZ = homePos.getZ() + 0.5D;

            if (phase == PHASE_WALK) {
                double dx = homeX - getX();
                double dz = homeZ - getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= 1.6D) {
                    phase = PHASE_CHARGE;
                    ticks = 0;
                    setAction(ACTION_CENTER_CHARGE);
                    getNavigation().stop();
                    setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
                    playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 3.5F, 0.5F);
                    return;
                }
                if (ticks > 260) {
                    finished = true; // no pudo llegar
                    return;
                }
                faceToward(homeX, homeZ, 20.0F);
                getMoveControl().setWantedPosition(homeX, homePos.getY(), homeZ, 1.5D);
                if (horizontalCollision) {
                    stuckTicks++;
                    if (stuckTicks >= 4) {
                        breakBlocksAhead(dx, dz);
                        stuckTicks = 0;
                    }
                } else {
                    stuckTicks = 0;
                }
                return;
            }

            if (phase == PHASE_CHARGE) {
                getNavigation().stop();
                int need = variant == 3 ? CENTER_CHARGE_TICKS / 2 : CENTER_CHARGE_TICKS;
                if (level() instanceof ServerLevel serverLevel) {
                    for (int side = 0; side < 2; side++) {
                        Vec3 tip = position().add(clawTipOffset(yBodyRot, side));
                        serverLevel.sendParticles(BoltColor.VIOLET.dust(1.6F), tip.x, tip.y, tip.z, 3, 0.2D, 0.2D, 0.2D, 0.0D);
                    }
                    if (ticks % 3 == 0) {
                        // anillo en el piso que se cierra: junta energia
                        double radius = 6.0D * (1.0D - (double) ticks / need);
                        for (int i = 0; i < 18; i++) {
                            double a = Math.PI * 2.0D * i / 18.0D;
                            serverLevel.sendParticles(BoltColor.VIOLET.dust(1.0F), getX() + Math.cos(a) * radius, getY() + 0.15D,
                                    getZ() + Math.sin(a) * radius, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                        }
                        playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 2.5F, 0.5F + 1.0F * ticks / need);
                    }
                }
                if (ticks >= need) {
                    phase = PHASE_FIRE;
                    ticks = 0;
                    if (variant == 3) {
                        setAction(ACTION_CENTER_SHOWER);
                    } else {
                        setAction(ACTION_CENTER_LASER);
                        beamYaw = getYRot();
                        spinDir = random.nextBoolean() ? 1.0F : -1.0F;
                        spinTotal = (int) Math.ceil(360.0D * Config.AMETHYST_CENTER_TURNS.get() / CENTER_SPIN);
                        playSound(SoundEvents.BEACON_ACTIVATE, 3.5F, 1.6F);
                    }
                }
                return;
            }

            // PHASE_FIRE
            getNavigation().stop();
            if (variant == 3) {
                int interval = isEnraged() ? SHOWER_INTERVAL - 1 : SHOWER_INTERVAL;
                if (ticks % interval == 0) {
                    double angle = random.nextDouble() * Math.PI * 2.0D;
                    double radius = SHOWER_RADIUS * Math.sqrt(random.nextDouble());
                    double x = homeX + Math.cos(angle) * radius;
                    double z = homeZ + Math.sin(angle) * radius;
                    double y = groundAt(x, z, homePos.getY());
                    if (Double.isNaN(y)) {
                        y = homePos.getY();
                    }
                    shardSide = 1 - shardSide;
                    launchShard(shardSide, new Vec3(x, y, z));
                }
                if (ticks >= CENTER_SHOWER_TICKS) {
                    finished = true;
                }
                return;
            }

            beamYaw = Mth.wrapDegrees(beamYaw + spinDir * CENTER_SPIN);
            turnBodyTo(beamYaw, 90.0F);
            if (level() instanceof ServerLevel serverLevel) {
                Vec3 base = position();
                float len = fireBeam(serverLevel, base.add(centerBeamOffset(beamYaw)), beamYaw, 0.0F);
                float len2 = 0.0F;
                if (variant == 2) {
                    float yaw2 = beamYaw + 180.0F;
                    len2 = fireBeam(serverLevel, base.add(centerBeamOffset(yaw2)), yaw2, 0.0F);
                }
                setBeam(variant == 2 ? BEAM_CENTER_TWO : BEAM_CENTER_ONE, beamYaw, 0.0F, len, len2);
                if (ticks % 6 == 0) {
                    playSound(SoundEvents.BEACON_AMBIENT, 3.0F, 1.7F);
                }
            }
            if (ticks >= spinTotal) {
                finished = true;
            }
        }

        @Override
        public void stop() {
            clearBeam();
            setAction(ACTION_IDLE);
            centerCooldown = cooldown(700);
            globalCooldown = 40;
            getNavigation().stop();
        }
    }

    /** Tira una amatista desde una pinza hasta un punto del suelo (ver AmethystShardEntity: tarda un poco en caer, hay tiempo para correr). */
    private void launchShard(int side, Vec3 target) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        AmethystShardEntity shard = ModEntities.AMETHYST_SHARD.get().create(serverLevel);
        if (shard == null) {
            return;
        }
        Vec3 start = position().add(clawTipOffset(yBodyRot, side));
        shard.launch(this, start, target, 24 + random.nextInt(10));
        serverLevel.addFreshEntity(shard);
        playSound(SoundEvents.AMETHYST_CLUSTER_HIT, 2.5F, 0.8F + random.nextFloat() * 0.4F);
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
        if (laserCooldown > 0) {
            laserCooldown--;
        }
        if (spikeCooldown > 0) {
            spikeCooldown--;
        }
        if (centerCooldown > 0) {
            centerCooldown--;
        }
        int action = getAction();
        if (getBeamMode() != BEAM_NONE && action != ACTION_LASER_FIRE && action != ACTION_CENTER_LASER) {
            clearBeam();
        }
        // Si va persiguiendo a alguien y se queda trabado contra algo, rompe lo que le tapa el paso
        LivingEntity target = getTarget();
        if (action == ACTION_IDLE && target != null && target.isAlive() && horizontalCollision
                && !getNavigation().isDone() && distanceToSqr(target) < 30.0D * 30.0D) {
            stuckTicks++;
            if (stuckTicks >= 14) {
                breakBlocksAhead(target.getX() - getX(), target.getZ() - getZ());
                stuckTicks = 0;
            }
        } else if (action == ACTION_IDLE) {
            stuckTicks = 0;
        }
        this.bossEvent.setProgress(getHealth() / getMaxHealth());
    }

    /** Cliente: cuenta cuanto lleva la accion actual para animar el modelo y guarda los angulos del rayo para dibujarlo suave. */
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
            beamYawO = beamYawNow;
            beamPitchO = beamPitchNow;
            beamYawNow = getBeamYaw();
            beamPitchNow = getBeamPitch();
            if (getBeamMode() == BEAM_NONE) {
                beamYawO = beamYawNow;
                beamPitchO = beamPitchNow;
            }
            if (isAlive() && random.nextInt(5) == 0) {
                level().addParticle(ParticleTypes.WITCH,
                        getX() + (random.nextDouble() - 0.5D) * 3.5D, getY() + 0.5D + random.nextDouble() * 6.0D,
                        getZ() + (random.nextDouble() - 0.5D) * 3.5D, 0.0D, 0.0D, 0.0D);
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
            playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 2.0F, 0.6F);
        }
        return hit;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Mientras despierta es invencible; sin dano por fuego ni caida
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
        return SoundEvents.AMETHYST_BLOCK_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.AMETHYST_BLOCK_STEP, 1.5F, 0.5F);
    }
}
