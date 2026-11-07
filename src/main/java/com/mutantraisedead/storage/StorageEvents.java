package com.mutantraisedead.storage;

import com.mutantraisedead.Config;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Cuando el inventario esta lleno y llevas puesto el Doble Almacen, lo que recoges del suelo
 * se guarda en el amuleto (asi se siente como un inventario mas grande).
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class StorageEvents {
    private StorageEvents() {
    }

    @SubscribeEvent
    public static void onPickup(EntityItemPickupEvent event) {
        if (!Config.DOUBLE_STORAGE_OVERFLOW.get() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemEntity entity = event.getItem();
        ItemStack stack = entity.getItem();
        // El evento se dispara aunque el objeto todavia no se pueda recoger (recien tirado)
        if (stack.isEmpty() || entity.hasPickUpDelay() || StorageHelper.inventoryHasRoom(player.getInventory(), stack)) {
            return;
        }
        ItemStack charm = StorageHelper.findCharm(player);
        if (charm.isEmpty()) {
            return;
        }

        Container container = StorageHelper.containerFor(player, charm);
        int before = stack.getCount();
        ItemStack rest = StorageHelper.insert(container, stack.copy());
        int moved = before - rest.getCount();
        if (moved <= 0) {
            return;
        }
        stack.shrink(moved);
        player.take(entity, moved);
        if (stack.isEmpty()) {
            entity.discard();
        }
        event.setCanceled(true);
    }
}
