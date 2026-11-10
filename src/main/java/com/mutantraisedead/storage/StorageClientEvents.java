package com.mutantraisedead.storage;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Lado del cliente del Doble Almacen: espacios extra, pantalla del inventario ampliada. */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID, value = Dist.CLIENT)
public final class StorageClientEvents {
    static {
        // Los espacios extra solo se muestran en la pantalla del inventario ampliado
        ExtraSlot.screenAllows = () -> Minecraft.getInstance().screen instanceof DoubleInventoryScreen;
    }

    private StorageClientEvents() {
    }

    /** Tienen que existir antes de que el servidor mande el contenido del inventario. */
    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        ExtraSlots.install(event.getPlayer());
    }

    @SubscribeEvent
    public static void onRespawn(ClientPlayerNetworkEvent.Clone event) {
        ExtraSlots.install(event.getNewPlayer());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
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

    /** Si llevas el amuleto, la tecla del inventario abre la version ampliada en vez de la normal. */
    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() == null || event.getNewScreen().getClass() != InventoryScreen.class) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isCreative() || player.isSpectator()) {
            return;
        }
        ExtraContainer container = ExtraSlots.get(player);
        if (container == null) {
            return;
        }
        container.tick();
        if (container.isEnabled()) {
            event.setNewScreen(new DoubleInventoryScreen(player));
        }
    }
}
