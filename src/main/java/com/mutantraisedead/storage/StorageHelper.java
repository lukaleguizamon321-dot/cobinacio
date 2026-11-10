package com.mutantraisedead.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

/** Cosas en comun del Doble Almacen. */
public final class StorageHelper {
    /** Un cofre normal: 3 filas de 9. */
    public static final int SLOTS = 27;
    /** Donde se guardan los objetos dentro del amuleto. */
    static final String KEY = "DobleAlmacen";
    /** Numero de identidad del amuleto (para no mezclar el contenido de dos amuletos). */
    static final String ID_KEY = "DobleAlmacenId";

    /** Posicion (dentro de la pantalla del inventario) del primer espacio extra. */
    public static final int SLOT_X = 8;
    public static final int SLOT_Y = 176;

    private StorageHelper() {
    }

    /** El amuleto que tiene puesto el jugador (en cualquier ranura de Curios), o vacio si no lo tiene. */
    public static ItemStack findCharm(Player player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.findFirstCurio(ModStorage.DOUBLE_STORAGE.get()))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }

    /** Cuantas pilas de objetos tiene guardadas ese amuleto (para el texto del item). */
    public static int storedStacks(ItemStack charm) {
        CompoundTag tag = charm.getTag();
        return tag == null ? 0 : tag.getList(KEY, Tag.TAG_COMPOUND).size();
    }

    /** True si el inventario normal todavia puede recibir algo de esa pila. */
    static boolean inventoryHasRoom(Inventory inventory, ItemStack stack) {
        if (inventory.getFreeSlot() != -1) {
            return true;
        }
        for (ItemStack existing : inventory.items) {
            if (existing.getCount() < existing.getMaxStackSize() && ItemStack.isSameItemSameTags(existing, stack)) {
                return true;
            }
        }
        ItemStack offhand = inventory.offhand.get(0);
        return offhand.getCount() < offhand.getMaxStackSize() && ItemStack.isSameItemSameTags(offhand, stack);
    }
}
