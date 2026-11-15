package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer del jefe de cobre: el modelo del cangrejo de cobre chico, agrandado.
 * La textura es textures/entity/copper_crab_boss.png (por ahora es una copia de la del cangrejo de cobre chico;
 * cuando hagas la tuya con el mismo nombre, la reemplaza).
 */
public class CopperCrabBossRenderer extends MobRenderer<CopperCrabBossEntity, CopperCrabBossModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "textures/entity/copper_crab_boss.png");

    public CopperCrabBossRenderer(EntityRendererProvider.Context context) {
        super(context, new CopperCrabBossModel(), 2.6F);
    }

    /** El modelo chico mide 1,6 de ancho y 1,2 de alto: x4 queda de 6,4 por 4,7 bloques. */
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
