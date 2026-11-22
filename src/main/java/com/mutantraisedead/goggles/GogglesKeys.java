package com.mutantraisedead.goggles;

import com.mojang.blaze3d.platform.InputConstants;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** La tecla de las Gafas de amatista (por defecto G; se cambia en Opciones > Controles > Cobinatio). Solo existe en el cliente. */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GogglesKeys {
    public static final KeyMapping ACTIVATE = new KeyMapping("key.mutantraisedead.goggles", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, InputConstants.KEY_G, "key.categories.mutantraisedead");

    private GogglesKeys() {
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(ACTIVATE);
    }
}
