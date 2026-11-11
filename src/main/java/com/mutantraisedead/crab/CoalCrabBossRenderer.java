package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer PROVISORIO del jefe de carbon: usa el modelo y la textura del cangrejo de carbon comun,
 * agrandado hasta los 3 bloques de ancho y estirado hasta los 6 de alto (la caja del jefe mide 3 x 6).
 * Cuando haya un modelo propio, solo hay que cambiar el modelo, la textura y estos numeros.
 */
public class CoalCrabBossRenderer extends MobRenderer<CoalCrabBossEntity, CoalCrabBossModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "textures/entity/grottol_coal.png");

    /**
     * El modelo comun mide 1.6 de ancho x 1.18 de alto: 1.9 lo deja en 3 bloques de ancho y 5.1 de alto lo estira
     * hasta los 6 bloques de la caja del jefe (asi lo que se ve es lo que se golpea). Se ve alargado a proposito:
     * es provisorio hasta que haya modelo propio.
     */
    private static final float SCALE_XZ = 1.9F;
    private static final float SCALE_Y = 5.1F;

    public CoalCrabBossRenderer(EntityRendererProvider.Context context) {
        super(context, new CoalCrabBossModel(), 2.2F);
    }

    @Override
    protected void scale(CoalCrabBossEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE_XZ, SCALE_Y, SCALE_XZ);
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
