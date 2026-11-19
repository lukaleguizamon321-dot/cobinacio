package com.mutantraisedead.crab;

import com.mutantraisedead.Config;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Amatista que tira el Cangrejo Titan de Amatista en la variante 3 del ataque del centro.
 * Vuela en arco desde la pinza hasta un punto del suelo (tarda 1 segundo y algo: se ve donde va a caer, hay tiempo para correr)
 * y al caer lastima a todos los que esten cerca (AMETHYST_SHARD_DAMAGE, 2 corazones). No rompe bloques ni se guarda con el mundo.
 */
public class AmethystShardEntity extends Entity {
    /** Radio de la explosion al caer. */
    private static final double BLAST_RADIUS = 1.9D;

    private Vec3 start = Vec3.ZERO;
    private Vec3 target = Vec3.ZERO;
    private int flight = 26;
    private double arc = 5.0D;
    @Nullable
    private Entity owner;

    public AmethystShardEntity(EntityType<? extends AmethystShardEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /** Sale de start y cae en target despues de flightTicks ticks. */
    public void launch(@Nullable Entity owner, Vec3 start, Vec3 target, int flightTicks) {
        this.owner = owner;
        this.start = start;
        this.target = target;
        this.flight = Math.max(8, flightTicks);
        this.arc = 3.5D + start.distanceTo(target) * 0.2D;
        setPos(start.x, start.y, start.z);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (tickCount % 2 == 0) {
                level().addParticle(ParticleTypes.WITCH, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
            }
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (tickCount >= flight) {
            explode(serverLevel);
            discard();
            return;
        }
        double f = (double) tickCount / flight;
        Vec3 flat = start.lerp(target, f);
        setPos(flat.x, flat.y + Math.sin(Math.PI * f) * arc, flat.z);
        // circulo en el suelo donde va a caer
        if (tickCount % 3 == 0) {
            for (int i = 0; i < 10; i++) {
                double a = Math.PI * 2.0D * i / 10.0D + tickCount * 0.15D;
                serverLevel.sendParticles(BoltColor.VIOLET.dust(1.0F), target.x + Math.cos(a) * BLAST_RADIUS, target.y + 0.1D,
                        target.z + Math.sin(a) * BLAST_RADIUS, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private void explode(ServerLevel serverLevel) {
        AABB area = new AABB(target.x - BLAST_RADIUS, target.y - 1.0D, target.z - BLAST_RADIUS,
                target.x + BLAST_RADIUS, target.y + 2.5D, target.z + BLAST_RADIUS);
        float damage = Config.AMETHYST_SHARD_DAMAGE.get().floatValue();
        for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && !e.isSpectator() && !(e instanceof AmethystCrabBossEntity))) {
            victim.hurt(damageSources().indirectMagic(this, owner), damage);
            victim.setDeltaMovement(victim.getDeltaMovement().add(0.0D, 0.35D, 0.0D));
            victim.hurtMarked = true;
        }
        serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.AMETHYST_SHARD)),
                target.x, target.y + 0.4D, target.z, 24, 0.7D, 0.4D, 0.7D, 0.15D);
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AMETHYST_BLOCK.defaultBlockState()),
                target.x, target.y + 0.2D, target.z, 20, 0.8D, 0.2D, 0.8D, 0.1D);
        serverLevel.sendParticles(ParticleTypes.END_ROD, target.x, target.y + 0.5D, target.z, 8, 0.6D, 0.3D, 0.6D, 0.06D);
        serverLevel.playSound(null, target.x, target.y, target.z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 2.5F, 0.8F);
        serverLevel.playSound(null, target.x, target.y, target.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 2.0F, 1.4F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
