package com.mutantraisedead.storage;

import com.mutantraisedead.Config;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Lado del servidor del Doble Almacen: agrega los espacios extra al inventario, los mantiene al dia
 * con el amuleto y, cuando el inventario esta lleno, guarda ahi lo que recoges del suelo.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class StorageEvents {
    private StorageEvents() {
    }

    /** Apenas el jugador entra al mundo (o cambia de dimension o revive) se le agregan los espacios. */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
            ExtraSlots.install(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player instanceof FakePlayer) {
            return;
        }
        ExtraContainer container = ExtraSlots.get(player);
        if (container == null) {
            ExtraSlots.install(player);
            container = ExtraSlots.get(player);
        }
        if (container != null) {
            container.tick();
        }
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
        ExtraContainer container = ExtraSlots.get(player);
        if (container == null || !container.isEnabled()) {
            return;
        }

        int before = stack.getCount();
        ItemStack rest = container.addItem(stack.copy());
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
