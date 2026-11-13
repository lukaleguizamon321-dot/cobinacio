package com.mutantraisedead.crab;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Huevo de Cangrejo de Carbon con imperfecciones: lo suelta el Cangrejo Titan al morir.
 * No se puede colocar; hay que pulirlo en una piedra de afilar (ver EggEvents), y ahi sale el huevo pulido.
 */
public class RoughEggItem extends Item {
    public RoughEggItem(Properties properties) {
        super(properties);
    }

    /** Hace falta para que la piedra de afilar acepte este objeto en sus ranuras. */
    @Override
    public boolean canGrindstoneRepair(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.mutantraisedead.coal_crab_egg_rough.tip").withStyle(ChatFormatting.GRAY));
    }
}
