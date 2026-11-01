package com.mutantraisedead.crab;

import com.mutantraisedead.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Cangrejo de material (version propia del Grottol de Mowzie's Mobs):
 *  - pasivo, huye de los jugadores y se entierra si no puede escapar
 *  - solo muere de un golpe con un pico (el hierro aguanta 3 golpes)
 *  - aparece en cuevas
 *  - cada material tiene una pasiva distinta
 */
public class GrottolVariantEntity extends PathfinderMob {
    private final GrottolVariant variant;

    private int hitsLeft;
    private int lastPickaxeHitTick = -100;
    private boolean killedByPickaxe;
    private int fortuneLevel;

    private int fleeTicks;
    private int burrowTicks = -1;

    /** Solo en el cliente, para animar el modelo: pinzas en alto y enterrandose. */
    public int clawTicks;
    public int clientBurrowTicks = -1;

    private int abilityCooldown = 60;
    private int pendingTicks = -1;
    private Vec3 pendingPos = Vec3.ZERO;

    public GrottolVariantEntity(EntityType<? extends GrottolVariantEntity> type, Level level, GrottolVariant variant) {
        super(type, level);
        this.variant = variant;
        this.hitsLeft = variant == GrottolVariant.IRON ? 3 : 1;
        this.xpReward = 10;
    }

    @Override
    public float maxUpStep() {
        return 1.15F;
    }

    public GrottolVariant getVariant() {
        return variant;
    }

    public static AttributeSupplier.Builder createAttributes(GrottolVariant variant) {
        AttributeSupplier.Builder builder = Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
        if (variant == GrottolVariant.IRON) {
            builder.add(Attributes.ARMOR, 14.0D).add(Attributes.ARMOR_TOUGHNESS, 4.0D);
        }
        return builder;
    }

    public static boolean checkSpawn(GrottolVariant variant, LevelAccessor level, BlockPos pos) {
        return pos.getY() < variant.maxSpawnY
                && !level.canSeeSky(pos)
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                && level.getEntitiesOfClass(GrottolVariantEntity.class, new AABB(pos).inflate(20.0D)).isEmpty();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 16.0F, 0.9D, 1.2D,
                (LivingEntity e) -> !e.isSpectator()));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    // ------------------------------------------------------------------
    // Sonidos
    // ------------------------------------------------------------------

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.STONE_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return variant == GrottolVariant.AMETHYST ? SoundEvents.AMETHYST_BLOCK_BREAK : SoundEvents.STONE_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.STONE_STEP, 0.25F, 1.6F);
    }

    // ------------------------------------------------------------------
    // Danos: solo con pico + pasivas de reaccion
    // ------------------------------------------------------------------

    static boolean holdsPickaxe(Player player) {
        ItemStack stack = player.getMainHandItem();
        return stack.getItem() instanceof PickaxeItem || stack.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState());
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    /** El cangrejo de cobre es inmune a su propio rayo. */
    @Override
    public void thunderHit(ServerLevel level, LightningBolt bolt) {
        if (variant != GrottolVariant.COPPER) {
            super.thunderHit(level, bolt);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) {
            return false;
        }
        // Ningun cangrejo recibe dano por fuego ni por caida; el de cobre tampoco por rayos
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FALL)
                || (variant == GrottolVariant.COPPER && source.is(DamageTypeTags.IS_LIGHTNING))) {
            clearFire();
            return false;
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }

        Entity direct = source.getDirectEntity();
        if (direct instanceof LivingEntity melee && !source.is(DamageTypeTags.IS_PROJECTILE) && !source.is(DamageTypeTags.IS_EXPLOSION)) {
            reactToMelee(melee);
        }

        Entity attacker = source.getEntity();
        if (attacker instanceof Player player && !source.is(DamageTypeTags.IS_PROJECTILE)) {
            if (!holdsPickaxe(player)) {
                playSound(SoundEvents.CHAIN_HIT, 0.5F, 1.8F);
                return false;
            }
            if (tickCount - lastPickaxeHitTick < 8) {
                return false;
            }
            lastPickaxeHitTick = tickCount;
            hitsLeft--;
            if (hitsLeft > 0) {
                // El hierro aguanta varios golpes.
                playSound(SoundEvents.ANVIL_PLACE, 0.6F, 1.5F);
                level().broadcastEntityEvent(this, (byte) 2);
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()),
                            getX(), getY() + 0.5D, getZ(), 12, 0.3D, 0.3D, 0.3D, 0.05D);
                }
                return true;
            }
            killedByPickaxe = true;
            fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, player.getMainHandItem());
            return super.hurt(source, 1000.0F);
        }
        if (attacker instanceof Mob) {
            return false;
        }
        // Otros danos (fuego, caida, explosiones...): el hierro recibe la mitad.
        return super.hurt(source, variant == GrottolVariant.IRON ? amount * 0.5F : amount);
    }

    /** Pasivas que reaccionan al golpe: amatista = espinas, oro = deslumbra. */
    private void reactToMelee(LivingEntity attacker) {
        switch (variant) {
            case AMETHYST -> {
                attacker.hurt(damageSources().thorns(this), 3.0F);
                playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.0F);
            }
            case GOLD -> {
                attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLASH, getX(), getY() + 0.8D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0F, 1.6F);
            }
            default -> {
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        int count;
        if (killedByPickaxe) {
            count = variant.dropMin + random.nextInt(variant.dropMax - variant.dropMin + 1);
            for (int i = 0; i < fortuneLevel; i++) {
                count += random.nextInt(3);
            }
        } else {
            count = 1;
        }
        spawnAtLocation(new ItemStack(variant.drop.get(), count));
    }

    // ------------------------------------------------------------------
    // Comportamiento
    // ------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            clientParticles();
            if (clawTicks > 0) {
                clawTicks--;
            }
            if (clientBurrowTicks >= 0) {
                clientBurrowTicks++;
            }
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Enterrandose: desaparece despues de un ratito
        if (burrowTicks >= 0) {
            burrowTicks++;
            getNavigation().stop();
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            if (burrowTicks % 4 == 3) {
                BlockState below = level().getBlockState(blockPosition().below());
                playSound(SoundEvents.GRAVEL_BREAK, 1.0F, 0.8F + random.nextFloat() * 0.4F);
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, below),
                        getX(), getY() + 0.05D, getZ(), 8, 0.3D, 0.03D, 0.3D, 0.1D);
            }
            if (burrowTicks >= 20) {
                discard();
            }
            return;
        }

        Player nearest = level().getNearestPlayer(this, 16.0D);
        if (nearest != null && !nearest.isSpectator() && distanceToSqr(nearest) < 10.0D * 10.0D) {
            fleeTicks++;
        } else {
            fleeTicks = Math.max(0, fleeTicks - 2);
        }
        if (fleeTicks >= 60 && onGround() && isDiggable(level().getBlockState(blockPosition().below()))) {
            burrowTicks = 0;
            level().broadcastEntityEvent(this, (byte) 71);
            fleeTicks = 0;
            return;
        }

        tickAbilities(serverLevel, nearest);
    }

    private static boolean isDiggable(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.DIRT)
                || state.is(Blocks.GRAVEL) || state.is(BlockTags.SAND);
    }

    private void tickAbilities(ServerLevel level, Player target) {
        if (!Config.CRAB_ABILITIES.get()) {
            return;
        }
        if (abilityCooldown > 0) {
            abilityCooldown--;
        }

        // Ataque que ya se esta preparando (roca o rayo): el jugador tiene tiempo de esquivarlo
        if (pendingTicks >= 0) {
            pendingTicks--;
            if (variant == GrottolVariant.LAPIS) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        pendingPos.x, pendingPos.y + 0.1D, pendingPos.z, 4, 0.5D, 0.05D, 0.5D, 0.02D);
                if (pendingTicks % 5 == 0) {
                    playSound(SoundEvents.STONE_BREAK, 0.5F, 0.7F);
                }
            } else if (variant == GrottolVariant.COPPER) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pendingPos.x, pendingPos.y + 1.0D, pendingPos.z, 4, 0.6D, 1.0D, 0.6D, 0.1D);
            }
            if (pendingTicks < 0) {
                fireAbility(level);
            }
            return;
        }

        if (abilityCooldown > 0 || target == null || target.isSpectator() || !hasLineOfSight(target)) {
            return;
        }
        double distance = distanceTo(target);
        switch (variant) {
            case COAL -> {
                if (distance <= 9.0D) {
                    Vec3 dir = target.getEyePosition().subtract(position().add(0.0D, 0.8D, 0.0D));
                    SmallFireball fireball = new SmallFireball(level, this, dir.x, dir.y, dir.z);
                    fireball.setPos(getX(), getY() + 0.8D, getZ());
                    level.addFreshEntity(fireball);
                    level.playSound(null, getX(), getY(), getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F);
                    abilityCooldown = 50;
                    level.broadcastEntityEvent(this, (byte) 70);
                }
            }
            case LAPIS -> {
                if (distance <= 10.0D) {
                    pendingPos = target.position();
                    pendingTicks = 14;
                    abilityCooldown = 100;
                    level.broadcastEntityEvent(this, (byte) 70);
                }
            }
            case COPPER -> {
                if (distance <= 14.0D) {
                    pendingPos = target.position();
                    pendingTicks = 24;
                    abilityCooldown = 180;
                    level.broadcastEntityEvent(this, (byte) 70);
                    playSound(SoundEvents.LIGHTNING_BOLT_IMPACT, 0.3F, 2.0F);
                }
            }
            default -> {
            }
        }
    }

    private void fireAbility(ServerLevel level) {
        if (variant == GrottolVariant.LAPIS) {
            // Las rocas se levantan del suelo en el lugar marcado
            AABB area = new AABB(pendingPos.x - 1.5D, pendingPos.y - 0.5D, pendingPos.z - 1.5D,
                    pendingPos.x + 1.5D, pendingPos.y + 2.5D, pendingPos.z + 1.5D);
            List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, area, e -> !(e instanceof GrottolVariantEntity));
            for (LivingEntity victim : victims) {
                victim.hurt(damageSources().mobAttack(this), 6.0F);
                victim.setDeltaMovement(victim.getDeltaMovement().x, 0.9D, victim.getDeltaMovement().z);
                victim.hurtMarked = true;
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLESTONE.defaultBlockState()),
                    pendingPos.x, pendingPos.y + 0.5D, pendingPos.z, 40, 0.7D, 0.6D, 0.7D, 0.2D);
            level.playSound(null, pendingPos.x, pendingPos.y, pendingPos.z, SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 1.5F, 0.6F);
        } else if (variant == GrottolVariant.COPPER) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(pendingPos.x, pendingPos.y, pendingPos.z);
                level.addFreshEntity(bolt);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 70) {
            clawTicks = 24;
        } else if (id == 71) {
            clientBurrowTicks = 0;
        } else {
            super.handleEntityEvent(id);
        }
    }

    private void clientParticles() {
        if (!isAlive() || random.nextInt(15) != 0) {
            return;
        }
        double x = getX() + 0.5D * (2 * random.nextFloat() - 1.0D);
        double y = getY() + 0.9D + 0.3D * (2 * random.nextFloat() - 1.0D);
        double z = getZ() + 0.5D * (2 * random.nextFloat() - 1.0D);
        switch (variant) {
            case AMETHYST, LAPIS -> level().addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.0D, 0.0D);
            case COPPER -> level().addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0.0D, 0.0D, 0.0D);
            case COAL -> level().addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.02D, 0.0D);
            default -> {
            }
        }
    }
}
