package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;

/**
 * Dibuja el rayo de color: una linea quebrada de arriba a abajo, con un brillo ancho del color,
 * un centro mas claro y un nucleo casi blanco. Usa el mismo tipo de dibujo que el rayo del juego.
 */
public class ColoredBoltRenderer extends EntityRenderer<ColoredBoltEntity> {
    private static final int SEGMENTS = 10;
    private static final float HEIGHT = 26.0F;

    public ColoredBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ColoredBoltEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public boolean shouldRender(ColoredBoltEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void render(ColoredBoltEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float age = entity.tickCount + partialTick;
        float fade = Mth.clamp(1.0F - age / (ColoredBoltEntity.LIFETIME + 1.0F), 0.0F, 1.0F);
        BoltColor color = entity.getColor();

        // el camino se vuelve a sortear cada 2 ticks: el rayo "titila"
        RandomSource random = RandomSource.create(entity.seed + entity.tickCount / 2);
        float[] xs = new float[SEGMENTS + 1];
        float[] zs = new float[SEGMENTS + 1];
        for (int k = 1; k <= SEGMENTS; k++) {
            xs[k] = xs[k - 1] + (random.nextFloat() - 0.5F) * 1.8F;
            zs[k] = zs[k - 1] + (random.nextFloat() - 0.5F) * 1.8F;
        }

        VertexConsumer vc = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();
        for (int k = 0; k < SEGMENTS; k++) {
            float y0 = HEIGHT * k / SEGMENTS;
            float y1 = HEIGHT * (k + 1) / SEGMENTS;
            // brillo ancho, centro y nucleo (el nucleo es casi blanco)
            ribbon(vc, pose, xs[k], y0, zs[k], xs[k + 1], y1, zs[k + 1], 0.55F, color.r, color.g, color.b, 0.16F * fade);
            ribbon(vc, pose, xs[k], y0, zs[k], xs[k + 1], y1, zs[k + 1], 0.28F, color.r, color.g, color.b, 0.40F * fade);
            ribbon(vc, pose, xs[k], y0, zs[k], xs[k + 1], y1, zs[k + 1], 0.10F,
                    Mth.lerp(0.75F, color.r, 1.0F), Mth.lerp(0.75F, color.g, 1.0F), Mth.lerp(0.75F, color.b, 1.0F), 0.95F * fade);
        }
    }

    /** Una tira de ancho w entre dos puntos, en dos planos cruzados y por los dos lados (asi se ve de cualquier lado). */
    private static void ribbon(VertexConsumer vc, Matrix4f pose, float x0, float y0, float z0, float x1, float y1, float z1,
                               float w, float r, float g, float b, float a) {
        for (int plane = 0; plane < 2; plane++) {
            float dx = plane == 0 ? w : 0.0F;
            float dz = plane == 0 ? 0.0F : w;
            vertex(vc, pose, x0 - dx, y0, z0 - dz, r, g, b, a);
            vertex(vc, pose, x0 + dx, y0, z0 + dz, r, g, b, a);
            vertex(vc, pose, x1 + dx, y1, z1 + dz, r, g, b, a);
            vertex(vc, pose, x1 - dx, y1, z1 - dz, r, g, b, a);
            // la misma tira dada vuelta
            vertex(vc, pose, x1 - dx, y1, z1 - dz, r, g, b, a);
            vertex(vc, pose, x1 + dx, y1, z1 + dz, r, g, b, a);
            vertex(vc, pose, x0 + dx, y0, z0 + dz, r, g, b, a);
            vertex(vc, pose, x0 - dx, y0, z0 - dz, r, g, b, a);
        }
    }

    private static void vertex(VertexConsumer vc, Matrix4f pose, float x, float y, float z, float r, float g, float b, float a) {
        vc.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
    }
}
