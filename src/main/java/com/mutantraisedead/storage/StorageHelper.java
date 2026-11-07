package com.mutantraisedead.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

/** Cosas en comun del Doble Almacen (del lado del servidor). */
public final class StorageHelper {
    /** Un cofre normal: 3 filas de 9. */
    public static final int SLOTS = 27;
    /** Donde se guardan los objetos dentro del amuleto. */
    static final String KEY = "DobleAlmacen";

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

    /** Abre la ventana de 27 espacios (si el jugador tiene puesto el amuleto). */
    public static void open(ServerPlayer player) {
        ItemStack charm = findCharm(player);
        if (charm.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.mutantraisedead.doble_almacen_missing"), true);
            return;
        }
        StorageContainer container = new StorageContainer(player, charm);
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> ChestMenu.threeRows(id, inventory, container),
                Component.translatable("container.mutantraisedead.doble_almacen")));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SHULKER_BOX_OPEN, SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    /** Si la ventana ya esta abierta se usa esa misma (asi no se pisan los cambios); si no, se carga una nueva. */
    static Container containerFor(ServerPlayer player, ItemStack charm) {
        if (player.containerMenu instanceof ChestMenu menu && menu.getContainer() instanceof StorageContainer open) {
            return open;
        }
        return new StorageContainer(player, charm);
    }

    /** Mete la pila en el contenedor (primero completando pilas iguales). Devuelve lo que no entro. */
    static ItemStack insert(Container container, ItemStack stack) {
        for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
            ItemStack slot = container.getItem(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameTags(slot, stack)) {
                int space = Math.min(slot.getMaxStackSize(), container.getMaxStackSize()) - slot.getCount();
                if (space > 0) {
                    int moved = Math.min(space, stack.getCount());
                    slot.grow(moved);
                    stack.shrink(moved);
                    container.setChanged();
                }
            }
        }
        for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
            if (container.getItem(i).isEmpty()) {
                int moved = Math.min(Math.min(stack.getMaxStackSize(), container.getMaxStackSize()), stack.getCount());
                container.setItem(i, stack.split(moved));
            }
        }
        return stack;
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
