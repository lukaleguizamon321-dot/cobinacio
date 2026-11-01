package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GrottolVariantRenderer extends MobRenderer<GrottolVariantEntity, GrottolVariantModel> {
    /** El modelo de Blockbench mide casi 1.6 bloques de ancho; se achica para que entre en la caja del mob. */
    private static final float MODEL_SCALE = 0.75F;

    private final ResourceLocation texture;

    public GrottolVariantRenderer(EntityRendererProvider.Context context, GrottolVariant variant) {
        super(context, new GrottolVariantModel(variant.shape == GrottolVariant.Shape.CRYSTAL), 0.7F);
        this.texture = ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "textures/entity/" + variant.id + ".png");
    }

    @Override
    protected void scale(GrottolVariantEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
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
