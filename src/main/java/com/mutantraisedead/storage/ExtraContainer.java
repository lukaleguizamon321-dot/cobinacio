package com.mutantraisedead.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * Los 27 espacios extra del inventario (el "cofre de abajo").
 *
 * - En el SERVIDOR se cargan desde el amuleto Doble Almacen cuando te lo pones, y se guardan de vuelta
 *   en el amuleto cada vez que algo cambia. Si te sacas el amuleto, los espacios quedan vacios y lo
 *   guardado se queda adentro del amuleto (nunca se pierde nada).
 * - En el CLIENTE solo se muestra lo que manda el servidor; aca solo se sabe si el amuleto esta puesto.
 */
public class ExtraContainer extends SimpleContainer {
    private final Player owner;
    /** True si el jugador lleva puesto el amuleto (y, en el servidor, ya se cargo su contenido). */
    private boolean enabled;
    /** Identidad del amuleto cuyo contenido esta cargado ahora (solo servidor). */
    @Nullable
    private UUID loadedId;
    /** Mientras se carga/vacia, los cambios no se guardan. */
    private boolean loading;

    public ExtraContainer(Player owner) {
        super(StorageHelper.SLOTS);
        this.owner = owner;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Se llama una vez por tick, en el servidor y en el cliente. */
    public void tick() {
        ItemStack charm = StorageHelper.findCharm(owner);
        boolean worn = !charm.isEmpty();

        if (owner.level().isClientSide) {
            enabled = worn;
            return;
        }

        UUID id = worn ? ensureId(charm) : null;
        if (id == null ? loadedId != null : !id.equals(loadedId)) {
            // Se puso, se saco o se cambio el amuleto: se vuelve a cargar desde cero
            loading = true;
            try {
                clearContent();
                if (id != null) {
                    readFrom(charm);
                }
            } finally {
                loading = false;
            }
            loadedId = id;
        }
        enabled = worn && loadedId != null;
    }

    private static UUID ensureId(ItemStack charm) {
        CompoundTag tag = charm.getOrCreateTag();
        if (!tag.hasUUID(StorageHelper.ID_KEY)) {
            tag.putUUID(StorageHelper.ID_KEY, UUID.randomUUID());
        }
        return tag.getUUID(StorageHelper.ID_KEY);
    }

    @Nullable
    private static UUID readId(ItemStack charm) {
        CompoundTag tag = charm.getTag();
        return tag != null && tag.hasUUID(StorageHelper.ID_KEY) ? tag.getUUID(StorageHelper.ID_KEY) : null;
    }

    private void readFrom(ItemStack charm) {
        CompoundTag tag = charm.getTag();
        if (tag == null || !tag.contains(StorageHelper.KEY, Tag.TAG_LIST)) {
            return;
        }
        ListTag list = tag.getList(StorageHelper.KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot < getContainerSize()) {
                setItem(slot, ItemStack.of(entry));
            }
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!loading) {
            save();
        }
    }

    /** Guarda el contenido en el amuleto que esta puesto (solo si es el mismo que se cargo). */
    private void save() {
        if (owner.level().isClientSide || loadedId == null) {
            return;
        }
        ItemStack charm = StorageHelper.findCharm(owner);
        if (charm.isEmpty() || !loadedId.equals(readId(charm))) {
            return;
        }
        ListTag list = new ListTag();
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("Slot", (byte) i);
                stack.save(entry);
                list.add(entry);
            }
        }
        if (list.isEmpty()) {
            charm.removeTagKey(StorageHelper.KEY);
        } else {
            charm.getOrCreateTag().put(StorageHelper.KEY, list);
        }
    }
}
