package com.mutantraisedead.dicriti;

import com.mutantraisedead.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Lo que pasa cuando el pico de dicriti pega contra un bloque.
 * Cada lanzamiento "pica" dicritiPickPower segundos (5 por defecto) como si lo picaras con ese mismo pico en la mano
 * (cuenta el material del pico y Eficiencia). Si con eso alcanza para romper el bloque, se rompe al instante y suelta lo
 * que soltaria con ese pico (Fortuna y Toque de seda incluidos). Si no alcanza (obsidiana, por ejemplo), el bloque queda
 * agrietado y el progreso se acumula de un lanzamiento al siguiente (se pierde si pasan 20 segundos sin pegarle).
 */
public final class DicritiMining {
    public enum Result {
        /** El bloque se rompio. */
        BROKEN,
        /** El bloque se agrieto pero todavia aguanta. */
        CRACKED,
        /** El pico no puede con ese bloque (irrompible, o hace falta un pico mejor, o esta protegido). */
        FAILED
    }

    /** 400 ticks = 20 segundos; el cliente tambien borra las grietas despues de ese tiempo. */
    private static final long KEEP_TICKS = 400L;

    private static final Map<GlobalPos, Progress> PROGRESS = new HashMap<>();
    private static int nextCrackId;

    private static final class Progress {
        double seconds;
        long lastTick;
        int crackId;
    }

    private DicritiMining() {
    }

    public static Result strike(ServerLevel level, ServerPlayer player, ItemStack tool, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getBlock() instanceof LiquidBlock) {
            return Result.FAILED;
        }
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F) {
            return Result.FAILED;
        }
        if (state.requiresCorrectToolForDrops() && !tool.isCorrectToolForDrops(state)) {
            return Result.FAILED;
        }
        if (!player.getAbilities().mayBuild || !level.mayInteract(player, pos)) {
            return Result.FAILED;
        }
        if (MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player))) {
            return Result.FAILED;
        }

        float speed = tool.getDestroySpeed(state);
        if (speed > 1.0F) {
            int efficiency = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, tool);
            if (efficiency > 0) {
                speed += efficiency * efficiency + 1;
            }
        }
        speed = Math.max(speed, 1.0F);
        double secondsNeeded = hardness * 1.5D / speed;

        long now = level.getGameTime();
        PROGRESS.values().removeIf(p -> now < p.lastTick || now - p.lastTick > KEEP_TICKS);
        GlobalPos key = GlobalPos.of(level.dimension(), pos.immutable());
        Progress progress = PROGRESS.get(key);
        if (progress == null) {
            progress = new Progress();
            progress.crackId = 1_000_000 + (nextCrackId++ & 0xFFFF);
            PROGRESS.put(key, progress);
        }
        progress.seconds += Config.DICRITI_POWER.get();
        progress.lastTick = now;

        if (progress.seconds + 1.0E-6D >= secondsNeeded) {
            PROGRESS.remove(key);
            level.destroyBlockProgress(progress.crackId, pos, -1);
            breakBlock(level, player, tool, pos, state);
            return Result.BROKEN;
        }
        int stage = (int) Math.min(9.0D, Math.floor(progress.seconds / secondsNeeded * 10.0D));
        level.destroyBlockProgress(progress.crackId, pos, stage);
        return Result.CRACKED;
    }

    private static void breakBlock(ServerLevel level, ServerPlayer player, ItemStack tool, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!player.isCreative()) {
            Block.dropResources(state, level, pos, blockEntity, player, tool);
        }
        level.destroyBlock(pos, false, player);
        player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
        tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
    }

    public static void clear() {
        PROGRESS.clear();
    }
}
