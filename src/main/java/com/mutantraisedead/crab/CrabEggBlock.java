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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Huevo de jefe de cangrejo (v1.16: hay uno de carbon y uno de cobre, es la misma clase con distinta variante).
 * Tiene dos usos segun la propiedad "pedestal":
 *
 *  - pedestal = true: el huevo NATURAL de la guarida. Duerme sobre el pedestal del centro; cuando alguien se acerca
 *    (o lo toca) desaparece, se destruye el pedestal y sale el Cangrejo Titan (de Carbon o de Cobre, segun la guarida).
 *    No se puede romper en supervivencia (si no, se salteaban al jefe).
 *  - pedestal = false: el huevo PULIDO que puso un jugador. Nunca invoca al jefe: si esta sobre su bloque de material
 *    puro (carbon / cobre), incuba (ver CrabEggBlockEntity) y nace un cangrejo chico aliado.
 *    Se puede romper y devuelve el huevo pulido.
 */
public class CrabEggBlock extends Block implements EntityBlock {
    public static final BooleanProperty PEDESTAL = BooleanProperty.create("pedestal");
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 11.0D, 12.0D);

    /** El pedestal de la guarida (ver LairFeature) tiene 3 capas debajo del huevo. */
    private static final int PEDESTAL_LAYERS = 3;

    private final GrottolVariant variant;

    /** variant: COAL o COPPER (de que cangrejo es el huevo). */
    public CrabEggBlock(Properties properties, GrottolVariant variant) {
        super(properties);
        this.variant = variant;
        registerDefaultState(stateDefinition.any().setValue(PEDESTAL, false));
    }

    public GrottolVariant variant() {
        return variant;
    }

    /**
     * El bloque de material puro sobre el que incuba el huevo: un bloque de carbon para el de carbon;
     * para el de cobre vale el bloque de cobre en cualquiera de sus estados (nuevo, oxidado, encerado).
     */
    public boolean canIncubateOn(BlockState below) {
        if (variant == GrottolVariant.COPPER) {
            return below.is(Blocks.COPPER_BLOCK) || below.is(Blocks.EXPOSED_COPPER)
                    || below.is(Blocks.WEATHERED_COPPER) || below.is(Blocks.OXIDIZED_COPPER)
                    || below.is(Blocks.WAXED_COPPER_BLOCK) || below.is(Blocks.WAXED_EXPOSED_COPPER)
                    || below.is(Blocks.WAXED_WEATHERED_COPPER) || below.is(Blocks.WAXED_OXIDIZED_COPPER);
        }
        return below.is(Blocks.COAL_BLOCK);
    }

    /** Clave del mensaje "hace falta el bloque de abajo" (el de cobre tiene el suyo). */
    public String needsBlockKey() {
        return variant == GrottolVariant.COPPER ? "message.mutantraisedead.egg_needs_block_copper" : "message.mutantraisedead.egg_needs_block";
    }

    /** Clave del mensaje "nacio tu cangrejo". */
    public String hatchedKey() {
        return variant == GrottolVariant.COPPER ? "message.mutantraisedead.egg_hatched_copper" : "message.mutantraisedead.egg_hatched";
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PEDESTAL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** El huevo natural es indestructible en supervivencia; el pulido se rompe facil. */
    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (state.getValue(PEDESTAL)) {
            return 0.0F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return state.getValue(PEDESTAL) ? 3600000.0F : super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return state.getValue(PEDESTAL) ? PushReaction.BLOCK : PushReaction.DESTROY;
    }

    /** El huevo pulido solo se sostiene arriba de su bloque de material puro. */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return state.getValue(PEDESTAL) || canIncubateOn(level.getBlockState(pos.below()));
    }

    /** Si le sacan el bloque de abajo, el huevo pulido se suelta como objeto. */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!level.isClientSide && !state.getValue(PEDESTAL) && !canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
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

    /** Tocar el huevo natural tambien lo despierta; el pulido cuenta cuanto falta para que nazca. */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level instanceof ServerLevel serverLevel) {
            if (state.getValue(PEDESTAL)) {
                awaken(serverLevel, pos, state, player);
            } else if (level.getBlockEntity(pos) instanceof CrabEggBlockEntity egg) {
                egg.showStatus(player);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** El huevo desaparece, se destruye el pedestal del centro y aparece el jefe. */
    public static void awaken(ServerLevel level, BlockPos pos, BlockState state, @Nullable LivingEntity wakener) {
        if (!(state.getBlock() instanceof CrabEggBlock eggBlock)) {
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

        Mob boss = eggBlock.variant == GrottolVariant.COPPER
                ? ModEntities.COPPER_BOSS.get().create(level)
                : ModEntities.COAL_BOSS.get().create(level);
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
