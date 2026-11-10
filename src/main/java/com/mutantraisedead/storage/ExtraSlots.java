package com.mutantraisedead.storage;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Method;

/**
 * Agrega los 27 espacios extra al final de los espacios del inventario del jugador
 * (tanto en el servidor como en el cliente, tienen que estar en los dos lados).
 */
public final class ExtraSlots {
    private static Method addSlotMethod;
    private static boolean addSlotFailed;

    private ExtraSlots() {
    }

    /** El contenedor extra de este jugador, o null si todavia no se le agrego. */
    @Nullable
    public static ExtraContainer get(Player player) {
        var slots = player.inventoryMenu.slots;
        for (int i = slots.size() - 1; i >= 0; i--) {
            if (slots.get(i) instanceof ExtraSlot slot) {
                return slot.extraContainer();
            }
        }
        return null;
    }

    /** Agrega los espacios si todavia no estan. Se puede llamar todas las veces que haga falta. */
    public static void install(Player player) {
        if (get(player) != null) {
            return;
        }
        AbstractContainerMenu menu = player.inventoryMenu;
        ExtraContainer container = new ExtraContainer(player);
        for (int i = 0; i < StorageHelper.SLOTS; i++) {
            int x = StorageHelper.SLOT_X + (i % 9) * 18;
            int y = StorageHelper.SLOT_Y + (i / 9) * 18;
            if (!addSlot(menu, new ExtraSlot(container, i, x, y))) {
                return;
            }
        }
    }

    /** AbstractContainerMenu.addSlot es protegido; se llama por reflexion (m_38897_ es su nombre interno). */
    private static boolean addSlot(AbstractContainerMenu menu, Slot slot) {
        if (addSlotFailed) {
            return false;
        }
        try {
            if (addSlotMethod == null) {
                addSlotMethod = ObfuscationReflectionHelper.findMethod(AbstractContainerMenu.class, "m_38897_", Slot.class);
            }
            addSlotMethod.invoke(menu, slot);
            return true;
        } catch (Exception | LinkageError e) {
            addSlotFailed = true;
            MutantRaiseDead.LOGGER.error("Doble Almacen: no se pudieron agregar los espacios extra al inventario", e);
            return false;
        }
    }
}
