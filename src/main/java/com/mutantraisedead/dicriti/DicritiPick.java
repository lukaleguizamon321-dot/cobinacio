package com.mutantraisedead.dicriti;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

/** Marca que se le pone a un pico cuando se lo combina con un lingote de dicriti. */
public final class DicritiPick {
    /** Etiqueta (NBT) del pico que lo marca como lanzable. */
    public static final String TAG = "CobinatioDicriti";

    private DicritiPick() {
    }

    /** true si es un pico (de cualquier material o mod: todo lo que este en la etiqueta minecraft:pickaxes). */
    public static boolean isPickaxe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ItemTags.PICKAXES);
    }

    /** true si es un pico que ya tiene la habilidad del dicriti. */
    public static boolean hasAbility(ItemStack stack) {
        return isPickaxe(stack) && stack.hasTag() && stack.getTag().getBoolean(TAG);
    }

    /** Copia del pico (una sola unidad) con la habilidad puesta. Conserva encantamientos, nombre y desgaste. */
    public static ItemStack withAbility(ItemStack pick) {
        ItemStack copy = pick.copy();
        copy.setCount(1);
        copy.getOrCreateTag().putBoolean(TAG, true);
        return copy;
    }
}
