package com.mutantraisedead.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Los 27 espacios del Doble Almacen. Se cargan del amuleto al abrir y se guardan en el amuleto
 * cada vez que algo cambia. Si te sacas el amuleto, la ventana se cierra sola.
 */
public class StorageContainer extends SimpleContainer {
    private final Player owner;
    private final ItemStack charm;

    public StorageContainer(Player owner, ItemStack charm) {
        super(StorageHelper.SLOTS);
        this.owner = owner;
        this.charm = charm;

        CompoundTag tag = charm.getTag();
        if (tag != null && tag.contains(StorageHelper.KEY, Tag.TAG_LIST)) {
            ListTag list = tag.getList(StorageHelper.KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                int slot = entry.getByte("Slot") & 255;
                if (slot < getContainerSize()) {
                    setItem(slot, ItemStack.of(entry));
                }
            }
        }
        // recien ahora, para que cargar no cuente como un cambio
        addListener(container -> save());
    }

    private void save() {
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

    @Override
    public boolean stillValid(Player player) {
        return player == owner && !StorageHelper.findCharm(player).isEmpty();
    }

    @Override
    public void stopOpen(Player player) {
        if (!player.level().isClientSide) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SHULKER_BOX_CLOSE, SoundSource.PLAYERS, 0.5F, 1.0F);
        }
        super.stopOpen(player);
    }
}
