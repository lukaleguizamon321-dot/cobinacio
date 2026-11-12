package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer del jefe de carbon (Coal Crab Titan) con su modelo de Blockbench (boss.bbmodel) y su textura propia.
 */
public class CoalCrabBossRenderer extends MobRenderer<CoalCrabBossEntity, CoalCrabBossModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "textures/entity/coal_crab_boss.png");

    /**
     * El modelo, tal como lo hiciste en Blockbench, mide 5,25 bloques de alto (del suelo al techo del caparazon).
     * 1.14 lo deja en 6 bloques de alto, como pediste. Las patas y las pinzas abiertas salen mas anchas que la
     * caja del jefe (3 x 6): la caja es lo que se golpea, el modelo es lo que se ve.
     * Si lo queres mas grande o mas chico, solo hay que cambiar este numero.
     */
    private static final float SCALE = 1.14F;

    public CoalCrabBossRenderer(EntityRendererProvider.Context context) {
        super(context, new CoalCrabBossModel(), 3.0F);
    }

    @Override
    protected void scale(CoalCrabBossEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(CoalCrabBossEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees(CoalCrabBossEntity entity) {
        return 0.0F;
    }
}
