package com.mutantraisedead.storage;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Cuando apretas la tecla, se le avisa al servidor. Solo cliente. */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID, value = Dist.CLIENT)
public final class StorageClientEvents {
    private StorageClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        while (StorageKeys.OPEN.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.screen == null) {
                ModNetwork.CHANNEL.sendToServer(new OpenStoragePacket());
            }
        }
    }
}
