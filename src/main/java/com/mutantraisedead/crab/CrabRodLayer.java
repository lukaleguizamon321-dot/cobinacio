package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;

/**
 * Dibuja el pararrayos (el bloque del juego, con las texturas de tu paquete) parado sobre el caparazon del cangrejo de cobre.
 * Sigue al caparazon cuando respira o se sacude. Las medidas son pixeles de Blockbench, las mismas que CrabFurnaceLayer.
 */
public class CrabRodLayer extends RenderLayer<GrottolVariantEntity, GrottolVariantModel> {
    private static final float UNIT = 1.0F / 16.0F;
    private static final float FLOOR = 1.501F;
    /** Centro de la tapa del caparazon (x, y, z) en pixeles de Blockbench: ahi se apoya la base del pararrayos. */
    private static final float CX = 0.0F;
    private static final float CY = 16.85F;
    private static final float CZ = 4.56F;
    /** Tamano del pararrayos (1 = un bloque entero de alto). */
    private static final float SCALE = 0.8F;

    public CrabRodLayer(RenderLayerParent<GrottolVariantEntity, GrottolVariantModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, GrottolVariantEntity crab, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!crab.hasRod() || crab.isInvisible()) {
            return;
        }
        poseStack.pushPose();
        // sigue al caparazon
        getParentModel().applyShellPose(poseStack);
        // base del pararrayos (en el modelo de Minecraft Y apunta hacia abajo)
        poseStack.translate(-CX * UNIT, FLOOR - CY * UNIT, CZ * UNIT);
        // el renderer dio vuelta los ejes: se los vuelve a dar vuelta para que el bloque quede parado
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.scale(SCALE, SCALE, SCALE);
        // el bloque se dibuja con una esquina en el origen: se lo centra
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.LIGHTNING_ROD.defaultBlockState(), poseStack, buffers, light, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
