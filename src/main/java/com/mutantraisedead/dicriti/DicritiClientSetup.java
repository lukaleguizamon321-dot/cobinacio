package com.mutantraisedead.dicriti;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Registra el dibujo del pico lanzado (solo en el cliente). */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DicritiClientSetup {
    private DicritiClientSetup() {
    }

    @SubscribeEvent
    public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModDicriti.THROWN_PICK.get(), ThrownDicritiPickRenderer::new);
    }
}
