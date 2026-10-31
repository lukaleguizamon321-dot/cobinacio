package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Registra modelos y renderers de los cangrejos (solo en el cliente). */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (GrottolVariant.Shape shape : GrottolVariant.Shape.values()) {
            event.registerLayerDefinition(GrottolVariantModel.layer(shape), () -> GrottolVariantModel.createLayer(shape));
        }
        event.registerLayerDefinition(GiantIronCrabModel.LAYER, GiantIronCrabModel::createLayer);
    }

    @SubscribeEvent
    public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (GrottolVariant variant : GrottolVariant.values()) {
            EntityRendererProvider<GrottolVariantEntity> provider = context -> new GrottolVariantRenderer(context, variant);
            event.registerEntityRenderer(ModEntities.CRABS.get(variant).get(), provider);
        }
    }

    @SubscribeEvent
    public static void onGiantRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.GIANT_CRAB.get(), GiantIronCrabRenderer::new);
    }
}
