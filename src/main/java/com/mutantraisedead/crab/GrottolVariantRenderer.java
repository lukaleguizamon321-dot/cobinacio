package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GrottolVariantRenderer extends MobRenderer<GrottolVariantEntity, GrottolVariantModel> {
    private final ResourceLocation texture;

    public GrottolVariantRenderer(EntityRendererProvider.Context context, GrottolVariant variant) {
        super(context, new GrottolVariantModel(context.bakeLayer(GrottolVariantModel.layer(variant.shape)), variant.shape), 0.6F);
        this.texture = ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "textures/entity/" + variant.id + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(GrottolVariantEntity entity) {
        return texture;
    }

    @Override
    protected float getFlipDegrees(GrottolVariantEntity entity) {
        return 0.0F;
    }
}
