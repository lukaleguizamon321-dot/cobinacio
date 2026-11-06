package com.mutantraisedead.crab;

import com.mutantraisedead.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Cangrejo gigante de hierro (2x2). Despierta al romper un nido, tiene armadura de hierro en bruto,
 * solo se lo puede matar con pico (muchos golpes) y cada golpe suyo te aturde 1.5 segundos
 * (controles de movimiento invertidos).
 */
public class GiantIronCrabEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> DATA_EMERGE =
            SynchedEntityData.defineId(GiantIronCrabEntity.class, EntityDataSerializers.INT);
    public static final int EMERGE_TICKS = 40;
    private static final int DIZZY_TICKS = 30; // 1.5 segundos

    private int lastPickaxeHitTick = -100;
    private boolean killedByPickaxe;
    private int fortuneLevel;

    public GiantIronCrabEntity(EntityType<? extends GiantIronCrabEntity> type, Level level) {
        super(type, level);
        this.xpReward = 60;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_EMERGE, 0);
    }

    /** Ticks que le faltan para terminar de salir del suelo (0 = ya salio). */
    public int getEmergeTicks() {
        return this.entityData.get(DATA_EMERGE);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        this.entityData.set(DATA_EMERGE, EMERGE_TICKS);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
    }

    @Override
    public float maxUpStep() {
        return 1.15F;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new EmergeGoal());
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Mientras sale del suelo no hace nada. */
    private class EmergeGoal extends Goal {
        EmergeGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return getEmergeTicks() > 0;
        }

        @Override
        public boolean canContinueToUse() {
            return getEmergeTicks() > 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            int left = getEmergeTicks() - 1;
            entityData.set(DATA_EMERGE, Math.max(0, left));
            getNavigation().stop();
            if (level() instanceof ServerLevel serverLevel && left % 4 == 0) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        getX(), getY() + 0.2D, getZ(), 10, 0.8D, 0.1D, 0.8D, 0.1D);
                playSound(SoundEvents.GRAVEL_BREAK, 1.5F, 0.5F);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    // ------------------------------------------------------------------
    // Ataque: cada golpe aturde
    // ------------------------------------------------------------------

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (target instanceof Player player && (hit || player.isCreative())) {
            player.addEffect(new MobEffectInstance(ModEffects.DIZZY.get(), DIZZY_TICKS, 0, false, true, true));
            playSound(SoundEvents.ANVIL_LAND, 1.0F, 0.6F);
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT, player.getX(), player.getEyeY(), player.getZ(), 12, 0.3D, 0.3D, 0.3D, 0.2D);
            }
        }
        return hit;
    }

    // ------------------------------------------------------------------
    // Danos: solo pico, muchos golpes
    // ------------------------------------------------------------------

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || getEmergeTicks() > 0) {
            return false;
        }
        // Sin dano por fuego ni por caida
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FALL)) {
            clearFire();
            return false;
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof Player player && !source.is(DamageTypeTags.IS_PROJECTILE)) {
            if (!GrottolVariantEntity.holdsPickaxe(player)) {
                playSound(SoundEvents.CHAIN_HIT, 0.7F, 1.2F);
                return false;
            }
            CrabGameEvents.markIronHit(player);
            if (tickCount - lastPickaxeHitTick < 8) {
                return false;
            }
            lastPickaxeHitTick = tickCount;
            setTarget(player);
            setLastHurtByMob(player);

            float perHit = getMaxHealth() / Config.GIANT_CRAB_HITS.get();
            if (getHealth() - perHit > 0.5F) {
                setHealth(getHealth() - perHit);
                this.lastHurtByPlayer = player;
                this.lastHurtByPlayerTime = 100;
                playSound(SoundEvents.ANVIL_PLACE, 0.8F, 0.8F);
                level().broadcastEntityEvent(this, (byte) 2);
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.RAW_IRON_BLOCK.defaultBlockState()),
                            getX(), getY() + 1.0D, getZ(), 16, 0.6D, 0.5D, 0.6D, 0.05D);
                }
                return true;
            }
            killedByPickaxe = true;
            fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, player.getMainHandItem());
            return super.hurt(source, 100000.0F);
        }
        if (attacker instanceof Mob) {
            return false;
        }
        return super.hurt(source, amount * 0.25F);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        int raw = 12 + random.nextInt(9);
        int blocks = 1 + random.nextInt(2);
        if (killedByPickaxe) {
            for (int i = 0; i < fortuneLevel; i++) {
                raw += random.nextInt(6);
            }
        } else {
            raw /= 2;
            blocks = 0;
        }
        spawnAtLocation(new ItemStack(Items.RAW_IRON, raw));
        if (blocks > 0) {
            spawnAtLocation(new ItemStack(Items.RAW_IRON_BLOCK, blocks));
        }
    }

    // ------------------------------------------------------------------
    // Sonidos
    // ------------------------------------------------------------------

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ANVIL_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DEEPSLATE_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.STONE_STEP, 0.6F, 0.6F);
    }
}
