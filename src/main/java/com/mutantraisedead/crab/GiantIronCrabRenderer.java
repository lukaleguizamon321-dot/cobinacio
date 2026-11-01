package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GiantIronCrabRenderer extends MobRenderer<GiantIronCrabEntity, GiantIronCrabModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "textures/entity/giant_iron_crab.png");

    /** El modelo de Blockbench mide unos 3.7 bloques de ancho y 2.8 de alto; se achica para que quede en la caja del mob. */
    private static final float MODEL_SCALE = 0.6F;

    public GiantIronCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new GiantIronCrabModel(), 1.5F);
    }

    @Override
    protected void scale(GiantIronCrabEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
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
