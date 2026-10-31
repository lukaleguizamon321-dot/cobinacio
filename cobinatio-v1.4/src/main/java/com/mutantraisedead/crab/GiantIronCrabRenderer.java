package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GiantIronCrabRenderer extends MobRenderer<GiantIronCrabEntity, GiantIronCrabModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "textures/entity/giant_iron_crab.png");

    public GiantIronCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new GiantIronCrabModel(context.bakeLayer(GiantIronCrabModel.LAYER)), 1.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(GiantIronCrabEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees(GiantIronCrabEntity entity) {
        return 0.0F;
    }
}
