package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Dibuja el pincho de cristal: un racimo de 3 cristales de amatista (prisma con punta) con la textura del bloque de amatista,
 * que sale del piso y se achica al desaparecer. Brilla (luz maxima).
 */
public class CrystalSpikeRenderer extends EntityRenderer<CrystalSpikeEntity> {
    private static final ResourceLocation AMETHYST = new ResourceLocation("minecraft", "block/amethyst_block");
    private static final int FULL_BRIGHT = 0xF000F0;

    public CrystalSpikeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(CrystalSpikeEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(CrystalSpikeEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float age = entity.tickCount + partialTick;
        float rise = Mth.clamp(age / CrystalSpikeEntity.WARMUP, 0.0F, 1.0F);
        rise = 1.0F - (1.0F - rise) * (1.0F - rise);
        float fade = Mth.clamp((CrystalSpikeEntity.LIFETIME - age) / 10.0F, 0.0F, 1.0F);
        float grow = rise * fade;
        if (grow <= 0.01F) {
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(AMETHYST);
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));

        // cada pincho sale distinto (segun el numero de la entidad)
        float spin = (entity.getId() * 67) % 360;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.scale(0.6F + 0.4F * grow, grow, 0.6F + 0.4F * grow);

        // cristal grande del medio, un poco inclinado
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(6.0F));
        crystal(poseStack, vc, sprite, 2.7F, 0.42F);
        poseStack.popPose();

        // cristal mediano
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.25D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-16.0F));
        crystal(poseStack, vc, sprite, 1.7F, 0.3F);
        poseStack.popPose();

        // cristal chico
        poseStack.pushPose();
        poseStack.translate(-0.4D, 0.0D, -0.35D);
        poseStack.mulPose(Axis.XP.rotationDegrees(-20.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(12.0F));
        crystal(poseStack, vc, sprite, 1.3F, 0.25F);
        poseStack.popPose();

        poseStack.popPose();
    }

    /** Un cristal: prisma cuadrado hasta el 60 % de la altura y despues una punta. */
    private static void crystal(PoseStack poseStack, VertexConsumer vc, TextureAtlasSprite sprite, float height, float w) {
        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();
        Matrix3f normal = last.normal();
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float vMid = v1 - (v1 - v0) * 0.55F;
        float mid = height * 0.6F;
        float[][] c = {{-w, -w}, {w, -w}, {w, w}, {-w, w}};
        for (int i = 0; i < 4; i++) {
            float[] a = c[i];
            float[] b = c[(i + 1) % 4];
            float nx = (a[0] + b[0]) * 0.5F;
            float nz = (a[1] + b[1]) * 0.5F;
            float nl = Mth.sqrt(nx * nx + nz * nz);
            nx /= nl;
            nz /= nl;
            // costado del prisma
            vertex(vc, pose, normal, a[0], 0.0F, a[1], u0, v1, nx, 0.0F, nz);
            vertex(vc, pose, normal, b[0], 0.0F, b[1], u1, v1, nx, 0.0F, nz);
            vertex(vc, pose, normal, b[0], mid, b[1], u1, vMid, nx, 0.0F, nz);
            vertex(vc, pose, normal, a[0], mid, a[1], u0, vMid, nx, 0.0F, nz);
            // punta (un triangulo, se repite el vertice de arriba)
            float tx = nx * 0.6F;
            float tz = nz * 0.6F;
            float ny = 0.7F;
            vertex(vc, pose, normal, a[0], mid, a[1], u0, vMid, tx, ny, tz);
            vertex(vc, pose, normal, b[0], mid, b[1], u1, vMid, tx, ny, tz);
            vertex(vc, pose, normal, 0.0F, height, 0.0F, (u0 + u1) * 0.5F, v0, tx, ny, tz);
            vertex(vc, pose, normal, 0.0F, height, 0.0F, (u0 + u1) * 0.5F, v0, tx, ny, tz);
        }
    }

    private static void vertex(VertexConsumer vc, Matrix4f pose, Matrix3f normal, float x, float y, float z, float u, float v,
                               float nx, float ny, float nz) {
        vc.vertex(pose, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(FULL_BRIGHT)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }
}
