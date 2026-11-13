package com.mutantraisedead.crab;

import com.mutantraisedead.Config;
import com.mutantraisedead.GuardHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Cerebro del huevo.
 *  - Huevo natural (pedestal = true): cada medio segundo mira si hay un jugador cerca; si lo hay, despierta al jefe.
 *  - Huevo pulido puesto por un jugador (pedestal = false): incuba mientras este arriba de su bloque de carbon.
 *    Cuando se cumple el tiempo (10 minutos, ver eggIncubationSeconds) espera a que haya un jugador a menos de
 *    5 bloques; el primero que este cerca se vuelve el dueno del cangrejo chico que nace.
 */
public class CrabEggBlockEntity extends BlockEntity {
    /** A cuantos bloques tiene que estar un jugador para que nazca el cangrejo y lo tome de dueno. */
    public static final double OWNER_RANGE = 5.0D;

    /** Ticks que lleva incubando. */
    private int incubation;

    public CrabEggBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CRAB_EGG_ENTITY.get(), pos, state);
    }

    private static int neededTicks() {
        return Config.EGG_INCUBATION_SECONDS.get() * 20;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (state.getValue(CrabEggBlock.PEDESTAL)) {
            tickNatural(serverLevel, pos, state);
        } else {
            tickIncubating(serverLevel, pos);
        }
    }

    private void tickNatural(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.getGameTime() % 10L != 0L) {
            return;
        }
        double range = Config.BOSS_WAKE_RANGE.get();
        Player player = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, range, EntitySelector.NO_SPECTATORS);
        if (player != null) {
            CrabEggBlock.awaken(level, pos, state, player);
        }
    }

    private void tickIncubating(ServerLevel level, BlockPos pos) {
        // solo incuba arriba de su bloque de material puro
        if (!CrabEggBlock.isIncubator(level.getBlockState(pos.below()))) {
            return;
        }
        int needed = neededTicks();
        if (incubation < needed) {
            incubation++;
            if (incubation % 100 == 0) {
                setChanged();
            }
            if (incubation % 60 == 0) {
                level.sendParticles(ParticleTypes.SMALL_FLAME, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 2, 0.15D, 0.2D, 0.15D, 0.005D);
            }
            if (incubation == needed) {
                setChanged();
                level.playSound(null, pos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 1.0F, 0.6F);
            }
            return;
        }
        // listo para nacer: espera a un jugador cerca
        if (level.getGameTime() % 10L != 0L) {
            return;
        }
        Player owner = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, OWNER_RANGE, EntitySelector.NO_SPECTATORS);
        if (owner == null) {
            if (level.getGameTime() % 40L == 0L) {
                level.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D, 4, 0.2D, 0.2D, 0.2D, 0.05D);
            }
            return;
        }
        hatch(level, pos, owner);
    }

    private void hatch(ServerLevel level, BlockPos pos, Player owner) {
        GrottolVariantEntity crab = ModEntities.CRABS.get(GrottolVariant.COAL).get().create(level);
        if (crab == null) {
            return;
        }
        level.removeBlock(pos, false);
        crab.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        GuardHandler.markPet(crab, owner);
        level.addFreshEntity(crab);

        level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 12, 0.3D, 0.3D, 0.3D, 0.03D);
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 0.7F);
        owner.displayClientMessage(Component.translatable("message.mutantraisedead.egg_hatched"), true);
    }

    /** Clic derecho: dice cuanto falta (o que falta algo). */
    public void showStatus(Player player) {
        if (!CrabEggBlock.isIncubator(player.level().getBlockState(worldPosition.below()))) {
            player.displayClientMessage(Component.translatable("message.mutantraisedead.egg_needs_block"), true);
            return;
        }
        int left = neededTicks() - incubation;
        if (left <= 0) {
            player.displayClientMessage(Component.translatable("message.mutantraisedead.egg_ready"), true);
            return;
        }
        int seconds = (left + 19) / 20;
        player.displayClientMessage(Component.translatable("message.mutantraisedead.egg_incubating", seconds / 60, String.format("%02d", seconds % 60)), true);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Incubation", incubation);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        incubation = tag.getInt("Incubation");
    }
}
