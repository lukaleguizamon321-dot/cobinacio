package com.mutantraisedead.crab;

import com.mutantraisedead.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Cada medio segundo mira si hay algun jugador cerca del huevo; si lo hay, despierta al jefe. */
public class CrabEggBlockEntity extends BlockEntity {
    public CrabEggBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CRAB_EGG_ENTITY.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel) || level.getGameTime() % 10L != 0L) {
            return;
        }
        double range = Config.BOSS_WAKE_RANGE.get();
        Player player = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, range, EntitySelector.NO_SPECTATORS);
        if (player != null) {
            CrabEggBlock.awaken(serverLevel, pos, state, player);
        }
    }
}
