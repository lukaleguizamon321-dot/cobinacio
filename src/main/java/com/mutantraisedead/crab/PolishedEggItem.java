package com.mutantraisedead.crab;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Huevo de Cangrejo (de Carbon, de Cobre o de Amatista) pulido. Solo se puede poner arriba de su bloque de material puro (carbon / cobre / amatista);
 * ahi incuba (10 minutos) y nace un cangrejo chico aliado. Si se pone en otro lado avisa y no se coloca.
 */
public class PolishedEggItem extends BlockItem {
    public PolishedEggItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos target = context.getClickedPos();
        if (getBlock() instanceof CrabEggBlock egg && !egg.canIncubateOn(level.getBlockState(target.below()))) {
            Player player = context.getPlayer();
            if (player != null && !level.isClientSide) {
                player.displayClientMessage(Component.translatable(egg.needsBlockKey()), true);
            }
            return InteractionResult.FAIL;
        }
        return super.place(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String key = getBlock() instanceof CrabEggBlock egg ? egg.tipKey() : "item.mutantraisedead.coal_crab_egg.tip";
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }
}
