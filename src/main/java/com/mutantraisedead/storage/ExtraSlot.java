package com.mutantraisedead.storage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;

/** Uno de los 27 espacios extra del inventario. Solo funciona si llevas puesto el Doble Almacen. */
public class ExtraSlot extends Slot {
    /**
     * Solo cliente: dice si la pantalla actual es la del inventario ampliado. Asi los espacios no se
     * dibujan en otras pantallas (por ejemplo el inventario del modo creativo).
     */
    static volatile BooleanSupplier screenAllows = () -> true;

    private final ExtraContainer extra;

    public ExtraSlot(ExtraContainer container, int index, int x, int y) {
        super(container, index, x, y);
        this.extra = container;
    }

    ExtraContainer extraContainer() {
        return extra;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // el amuleto no se puede guardar dentro de si mismo
        return extra.isEnabled() && !(stack.getItem() instanceof DoubleStorageItem);
    }

    @Override
    public boolean mayPickup(Player player) {
        return extra.isEnabled();
    }

    @Override
    public boolean isActive() {
        return extra.isEnabled() && screenAllows.getAsBoolean();
    }
}
