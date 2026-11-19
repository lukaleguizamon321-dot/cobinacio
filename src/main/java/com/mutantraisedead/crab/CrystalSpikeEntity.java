package com.mutantraisedead.crab;

import com.mutantraisedead.Config;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Pincho de cristal de la linea de pinchos del Cangrejo Titan de Amatista (ver AmethystCrabBossEntity).
 * Sale del piso (WARMUP ticks), lastima a quien toca (una vez por segundo cada uno) y desaparece a los 5 segundos.
 * No bloquea el paso ni se guarda con el mundo. Se dibuja en CrystalSpikeRenderer.
 */
public class CrystalSpikeEntity extends Entity {
    /** Ticks que tarda en salir del piso (todavia no lastima). */
    public static final int WARMUP = 8;
    /** Cuanto dura en total (100 ticks = 5 segundos). */
    public static final int LIFETIME = 100;
    /** Ticks que tiene que esperar para volver a lastimar a la misma victima. */
    private static final int REHIT_TICKS = 20;

    private final Map<Integer, Integer> hits = new HashMap<>();
    @Nullable
    private Entity owner;

    public CrystalSpikeEntity(EntityType<? extends CrystalSpikeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwnerBoss(@Nullable Entity boss) {
        this.owner = boss;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (tickCount == 1) {
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 1.6F, 0.7F + random.nextFloat() * 0.4F);
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState()),
                    getX(), getY() + 0.1D, getZ(), 10, 0.4D, 0.1D, 0.4D, 0.1D);
        }
        if (tickCount >= LIFETIME) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState()),
                    getX(), getY() + 1.0D, getZ(), 14, 0.4D, 0.8D, 0.4D, 0.08D);
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 1.0F, 1.2F);
            discard();
            return;
        }
        if (tickCount < WARMUP) {
            return;
        }
        AABB area = new AABB(getX() - 0.85D, getY(), getZ() - 0.85D, getX() + 0.85D, getY() + 2.6D, getZ() + 0.85D);
        float damage = Config.AMETHYST_SPIKE_DAMAGE.get().floatValue();
        for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && !e.isSpectator() && !(e instanceof AmethystCrabBossEntity))) {
            Integer last = hits.get(victim.getId());
            if (last != null && tickCount - last < REHIT_TICKS) {
                continue;
            }
            hits.put(victim.getId(), tickCount);
            victim.hurt(damageSources().indirectMagic(this, owner), damage);
            victim.setDeltaMovement(victim.getDeltaMovement().add(0.0D, 0.45D, 0.0D));
            victim.hurtMarked = true;
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
