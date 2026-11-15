package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer del jefe de cobre: el mismo modelo grande del jefe de carbon (boss.bbmodel) con su textura de cobre
 * (textures/entity/copper_crab_boss.png).
 */
public class CopperCrabBossRenderer extends MobRenderer<CopperCrabBossEntity, CopperCrabBossModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "textures/entity/copper_crab_boss.png");

    public CopperCrabBossRenderer(EntityRendererProvider.Context context) {
        super(context, new CopperCrabBossModel(), 3.0F);
    }

    /**
     * El modelo, tal como lo hiciste en Blockbench, mide 5,25 bloques de alto; x1,14 lo deja en 6 bloques (igual que el de carbon).
     * Las patas y las pinzas abiertas salen mas anchas que la caja del jefe (3 x 6): la caja es lo que se golpea, el modelo es lo que se ve.
     */
    @Override
    protected void scale(CopperCrabBossEntity entity, PoseStack poseStack, float partialTick) {
        float s = CopperCrabBossEntity.MODEL_SCALE;
        poseStack.scale(s, s, s);
    }

    @Override
    public ResourceLocation getTextureLocation(CopperCrabBossEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees(CopperCrabBossEntity entity) {
        return 0.0F;
    }
}
