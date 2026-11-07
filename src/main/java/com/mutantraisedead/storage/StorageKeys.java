package com.mutantraisedead.storage;

import com.mojang.blaze3d.platform.InputConstants;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** La tecla para abrir el Doble Almacen (se puede cambiar en Controles). Solo cliente. */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class StorageKeys {
    public static final KeyMapping OPEN = new KeyMapping(
            "key.mutantraisedead.doble_almacen", InputConstants.KEY_B, "key.categories.mutantraisedead");

    private StorageKeys() {
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN);
    }
}
