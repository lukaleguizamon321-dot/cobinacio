package com.mutantraisedead.crab;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Huevo del jefe de carbon. Esta sobre el pedestal del centro de la guarida y duerme.
 * Cuando alguien se acerca (o lo toca) el huevo desaparece, se destruye el pedestal y sale el Cangrejo Titan de Carbon.
 *
 * El huevo se rompe solo con el comando de creativo; en supervivencia no se puede picar (si no, se salteaban al jefe).
 * La propiedad "pedestal" vale true cuando lo puso la guarida: en ese caso se destruye el pedestal de abajo.
 */
public class CrabEggBlock extends Block implements EntityBlock {
    public static final BooleanProperty PEDESTAL = BooleanProperty.create("pedestal");
    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 15.0D, 13.0D);

    /** El pedestal de la guarida (ver LairFeature) tiene 3 capas debajo del huevo. */
    private static final int PEDESTAL_LAYERS = 3;

    public CrabEggBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PEDESTAL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PEDESTAL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrabEggBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlocks.CRAB_EGG_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof CrabEggBlockEntity egg) {
                egg.serverTick(lvl, pos, st);
            }
        };
    }

    /** Tocar el huevo tambien lo despierta. */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level instanceof ServerLevel serverLevel) {
            awaken(serverLevel, pos, state, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** El huevo desaparece, se destruye el pedestal del centro y aparece el jefe. */
    public static void awaken(ServerLevel level, BlockPos pos, BlockState state, @Nullable LivingEntity wakener) {
        if (!state.is(ModBlocks.COAL_EGG.get())) {
            return;
        }
        BlockPos floorPos = pos;
        level.removeBlock(pos, false);

        if (state.getValue(PEDESTAL)) {
            // el pedestal: un rombo en el piso, un cuadrado de 3x3 arriba y un bloque en el medio
            int baseY = pos.getY() - PEDESTAL_LAYERS;
            floorPos = new BlockPos(pos.getX(), baseY, pos.getZ());
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= 2) {
                        level.destroyBlock(new BlockPos(pos.getX() + dx, baseY, pos.getZ() + dz), false);
                    }
                }
            }
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    level.destroyBlock(new BlockPos(pos.getX() + dx, baseY + 1, pos.getZ() + dz), false);
                }
            }
            level.destroyBlock(new BlockPos(pos.getX(), baseY + 2, pos.getZ()), false);
        }

        CoalCrabBossEntity boss = ModEntities.COAL_BOSS.get().create(level);
        if (boss == null) {
            return;
        }
        boss.moveTo(floorPos.getX() + 0.5D, floorPos.getY(), floorPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        DifficultyInstance difficulty = level.getCurrentDifficultyAt(floorPos);
        boss.finalizeSpawn(level, difficulty, MobSpawnType.EVENT, null, null);
        if (wakener instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            boss.setTarget(player);
        }
        level.addFreshEntity(boss);

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, floorPos.getX() + 0.5D, floorPos.getY() + 1.0D, floorPos.getZ() + 0.5D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.playSound(null, floorPos, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 3.0F, 0.6F);
    }
}
