package com.mutantraisedead.storage;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * Doble Almacen: amuleto (ranura "charm" de Curios) que agrega un cofre mas al inventario.
 * Los objetos se guardan dentro del propio amuleto, como en una caja de shulker.
 */
public class DoubleStorageItem extends Item implements ICurioItem {
    public DoubleStorageItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.mutantraisedead.doble_almacen.slots", StorageHelper.SLOTS)
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.mutantraisedead.doble_almacen.open")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.mutantraisedead.doble_almacen.overflow")
                .withStyle(ChatFormatting.GRAY));
        int stored = StorageHelper.storedStacks(stack);
        if (stored > 0) {
            tooltip.add(Component.translatable("tooltip.mutantraisedead.doble_almacen.stored", stored)
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}
