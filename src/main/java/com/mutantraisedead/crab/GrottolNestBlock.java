package com.mutantraisedead.crab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/**
 * Bloque "mena" que en realidad es un cangrejo dormido (se generan de a 2x2x2).
 * El de hierro despierta al cangrejo gigante; los de los otros materiales (de las guaridas) despiertan
 * un cangrejo normal de ese material. Cerca de uno se escuchan rocas moviendose.
 * Al romper cualquiera, los demas del mismo grupo desaparecen y el cangrejo despierta.
 */
public class GrottolNestBlock extends Block {
    private final GrottolVariant variant;

    public GrottolNestBlock(Properties properties, GrottolVariant variant) {
        super(properties);
        this.variant = variant;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Esto solo corre en el cliente, para bloques cerca del jugador (unos 15 bloques)
        if (random.nextInt(25) == 0) {
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    random.nextBoolean() ? SoundEvents.STONE_BREAK : SoundEvents.GRAVEL_BREAK,
                    SoundSource.BLOCKS, 0.8F, 0.4F + random.nextFloat() * 0.3F, false);
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble(),
                    0.0D, -0.05D, 0.0D);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            awaken(serverLevel, pos, player);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    /** Solo cuentan los nidos del mismo cangrejo (el de hierro tiene version normal y de pizarra profunda). */
    private boolean isSameNest(BlockState state) {
        return state.getBlock() instanceof GrottolNestBlock other && other.variant == this.variant;
    }

    /** Junta los bloques nido pegados, los borra y hace aparecer al cangrejo (gigante si es de hierro). */
    private void awaken(ServerLevel level, BlockPos origin, Player player) {
        Set<BlockPos> group = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        group.add(origin);
        queue.add(origin);
        while (!queue.isEmpty() && group.size() < 32) {
            BlockPos current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos next = current.offset(dx, dy, dz);
                        if (!group.contains(next) && isSameNest(level.getBlockState(next))) {
                            group.add(next);
                            queue.add(next);
                        }
                    }
                }
            }
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (BlockPos p : group) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxZ = Math.max(maxZ, p.getZ());
            if (!p.equals(origin)) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(p)),
                        p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D, 12, 0.4D, 0.4D, 0.4D, 0.05D);
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        if (variant != GrottolVariant.IRON) {
            awakenNormalCrab(level, player, (minX + maxX + 1) / 2.0D, minY, (minZ + maxZ + 1) / 2.0D);
            return;
        }

        GiantIronCrabEntity crab = ModEntities.GIANT_CRAB.get().create(level);
        if (crab == null) {
            return;
        }
        crab.moveTo((minX + maxX + 1) / 2.0D, minY, (minZ + maxZ + 1) / 2.0D, level.getRandom().nextFloat() * 360.0F, 0.0F);
        crab.finalizeSpawn(level, level.getCurrentDifficultyAt(crab.blockPosition()), MobSpawnType.EVENT, null, null);
        crab.setTarget(player);
        level.addFreshEntity(crab);
        level.playSound(null, crab.getX(), crab.getY(), crab.getZ(), SoundEvents.DEEPSLATE_BREAK, SoundSource.HOSTILE, 2.0F, 0.5F);
    }

    /** Nidos de las guaridas: sale un cangrejo normal del material (el de diamante va directo a pelear). */
    private void awakenNormalCrab(ServerLevel level, Player player, double x, double y, double z) {
        GrottolVariantEntity crab = ModEntities.CRABS.get(variant).get().create(level);
        if (crab == null) {
            return;
        }
        crab.moveTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        crab.finalizeSpawn(level, level.getCurrentDifficultyAt(crab.blockPosition()), MobSpawnType.EVENT, null, null);
        crab.setPersistenceRequired();
        if (variant == GrottolVariant.DIAMOND) {
            crab.setTarget(player);
        }
        level.addFreshEntity(crab);
        level.playSound(null, x, y, z, SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 1.5F, 0.5F);
    }
}
