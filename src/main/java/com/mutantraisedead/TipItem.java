package com.mutantraisedead;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Objeto comun que muestra una linea de ayuda al pasar el mouse (por ejemplo, con que se craftea algo). */
public class TipItem extends Item {
    private final String tipKey;

    public TipItem(Properties properties, String tipKey) {
        super(properties);
        this.tipKey = tipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(tipKey).withStyle(ChatFormatting.GRAY));
    }
}
