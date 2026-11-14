package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.function.Function;

/**
 * Dibuja la MITAD DE ARRIBA de un horno (la parte de la boca) apoyada sobre el caparazon del cangrejo.
 * Usa las texturas del horno del juego (asi respeta tu paquete de texturas); cuando cocina muestra la boca prendida.
 * Las medidas estan en pixeles de Blockbench, las mismas que CrabMeshData: el caparazon tiene la tapa inclinada
 * 15 grados (mas alta adelante), y el horno se inclina igual para quedar apoyado parejo.
 */
public class CrabFurnaceLayer extends RenderLayer<GrottolVariantEntity, GrottolVariantModel> {
    private static final float UNIT = 1.0F / 16.0F;
    private static final float FLOOR = 1.501F;
    private static final float TILT = (float) Math.toRadians(15.0D);
    private static final float COS = (float) Math.cos(TILT);
    private static final float SIN = (float) Math.sin(TILT);
    /** Centro de la tapa del caparazon (x, y, z) en pixeles de Blockbench. */
    private static final float CX = 0.0F;
    private static final float CY = 16.85F;
    private static final float CZ = 4.56F;
    /** Medio ancho y alto del horno: 16 de ancho y 16 de largo, y solo 8 de alto (la mitad de arriba). */
    private static final float HALF = 8.0F;
    private static final float HEIGHT = 8.0F;

    private static final ResourceLocation FRONT = ResourceLocation.fromNamespaceAndPath("minecraft", "block/furnace_front");
    private static final ResourceLocation FRONT_ON = ResourceLocation.fromNamespaceAndPath("minecraft", "block/furnace_front_on");
    private static final ResourceLocation SIDE = ResourceLocation.fromNamespaceAndPath("minecraft", "block/furnace_side");
    private static final ResourceLocation TOP = ResourceLocation.fromNamespaceAndPath("minecraft", "block/furnace_top");

    public CrabFurnaceLayer(RenderLayerParent<GrottolVariantEntity, GrottolVariantModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int light, GrottolVariantEntity crab, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!crab.hasFurnace() || crab.isInvisible()) {
            return;
        }
        boolean lit = crab.isCooking();
        Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite front = atlas.apply(lit ? FRONT_ON : FRONT);
        TextureAtlasSprite side = atlas.apply(SIDE);
        TextureAtlasSprite top = atlas.apply(TOP);

        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        int overlay = LivingEntityRenderer.getOverlayCoords(crab, 0.0F);

        poseStack.pushPose();
        // sigue al caparazon cuando respira o se sacude
        getParentModel().applyShellPose(poseStack);
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // esquinas de cada cara, vistas desde afuera: arriba-izquierda, arriba-derecha, abajo-derecha, abajo-izquierda
        // frente = -Z (hacia donde mira el cangrejo)
        face(buffer, pose, normal, lit ? LightTexture.FULL_BRIGHT : light, overlay, front, true, 0.0F, 0.0F, -1.0F,
                HALF, HEIGHT, -HALF, -HALF, HEIGHT, -HALF, -HALF, 0.0F, -HALF, HALF, 0.0F, -HALF);
        // atras
        face(buffer, pose, normal, light, overlay, side, true, 0.0F, 0.0F, 1.0F,
                -HALF, HEIGHT, HALF, HALF, HEIGHT, HALF, HALF, 0.0F, HALF, -HALF, 0.0F, HALF);
        // costado +X
        face(buffer, pose, normal, light, overlay, side, true, 1.0F, 0.0F, 0.0F,
                HALF, HEIGHT, HALF, HALF, HEIGHT, -HALF, HALF, 0.0F, -HALF, HALF, 0.0F, HALF);
        // costado -X
        face(buffer, pose, normal, light, overlay, side, true, -1.0F, 0.0F, 0.0F,
                -HALF, HEIGHT, -HALF, -HALF, HEIGHT, HALF, -HALF, 0.0F, HALF, -HALF, 0.0F, -HALF);
        // tapa de arriba (la imagen entera)
        face(buffer, pose, normal, light, overlay, top, false, 0.0F, 1.0F, 0.0F,
                -HALF, HEIGHT, -HALF, HALF, HEIGHT, -HALF, HALF, HEIGHT, HALF, -HALF, HEIGHT, HALF);
        poseStack.popPose();
    }

    /**
     * Una cara del horno. halfTexture = true usa solo la mitad de arriba de la imagen (el horno esta cortado a la mitad).
     * (nx, ny, nz) es la normal de la cara; despues vienen las 4 esquinas (x, y, z) del horno ya sin inclinar.
     */
    private static void face(VertexConsumer buffer, Matrix4f pose, Matrix3f normal, int light, int overlay, TextureAtlasSprite sprite,
                             boolean halfTexture, float nx, float ny, float nz,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float vTop = sprite.getV0();
        float vBottom = halfTexture ? Mth.lerp(0.5F, sprite.getV0(), sprite.getV1()) : sprite.getV1();
        float[] cx = {x0, x1, x2, x3};
        float[] cy = {y0, y1, y2, y3};
        float[] cz = {z0, z1, z2, z3};
        float[] us = {u0, u1, u1, u0};
        float[] vs = {vTop, vTop, vBottom, vBottom};

        // la normal tambien se inclina, y despues se pasa al sistema del modelo (x e y invertidas)
        float tny = ny * COS - nz * SIN;
        float tnz = ny * SIN + nz * COS;
        for (int k = 0; k < 4; k++) {
            // pasa del horno derecho a la tapa inclinada del caparazon (pixeles de Blockbench)
            float bx = CX + cx[k];
            float by = CY + cy[k] * COS - cz[k] * SIN;
            float bz = CZ + cy[k] * SIN + cz[k] * COS;
            buffer.vertex(pose, -bx * UNIT, FLOOR - by * UNIT, bz * UNIT)
                    .color(1.0F, 1.0F, 1.0F, 1.0F)
                    .uv(us[k], vs[k])
                    .overlayCoords(overlay)
                    .uv2(light)
                    .normal(normal, -nx, -tny, tnz)
                    .endVertex();
        }
    }
}
