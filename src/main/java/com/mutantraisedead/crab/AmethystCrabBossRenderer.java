package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Renderer del jefe de amatista: su modelo de Blockbench (textures/entity/amethyst_crab_boss.png, 512 x 512)
 * y, mientras dispara, el rayo laser (una columna violeta brillante con el centro casi blanco).
 */
public class AmethystCrabBossRenderer extends MobRenderer<AmethystCrabBossEntity, AmethystCrabBossModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "textures/entity/amethyst_crab_boss.png");

    public AmethystCrabBossRenderer(EntityRendererProvider.Context context) {
        super(context, new AmethystCrabBossModel(), 3.6F);
    }

    /** Las patas y las pinzas abiertas salen mas anchas que la caja del jefe: la caja es lo que se golpea, el modelo es lo que se ve. */
    @Override
    protected void scale(AmethystCrabBossEntity entity, PoseStack poseStack, float partialTick) {
        float s = AmethystCrabBossEntity.MODEL_SCALE;
        poseStack.scale(s, s, s);
    }

    @Override
    public ResourceLocation getTextureLocation(AmethystCrabBossEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees(AmethystCrabBossEntity entity) {
        return 0.0F;
    }

    @Override
    public void render(AmethystCrabBossEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTick, poseStack, buffer, light);
        int mode = entity.getBeamMode();
        if (mode == AmethystCrabBossEntity.BEAM_NONE) {
            return;
        }
        float time = entity.tickCount + partialTick;
        float beamYaw = Mth.rotLerp(partialTick, entity.beamYawO, entity.getBeamYaw());
        float beamPitch = Mth.rotLerp(partialTick, entity.beamPitchO, entity.getBeamPitch());
        VertexConsumer vc = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();

        if (mode == AmethystCrabBossEntity.BEAM_AIM) {
            float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
            Vec3 origin = AmethystCrabBossEntity.clawTipOffset(bodyYaw, entity.getAimSide());
            beam(vc, pose, origin, AmethystCrabBossEntity.beamDirection(beamYaw, beamPitch), entity.getBeamLength(0), time);
            return;
        }
        beam(vc, pose, AmethystCrabBossEntity.centerBeamOffset(beamYaw),
                AmethystCrabBossEntity.beamDirection(beamYaw, 0.0F), entity.getBeamLength(0), time);
        if (mode == AmethystCrabBossEntity.BEAM_CENTER_TWO) {
            float yaw2 = beamYaw + 180.0F;
            beam(vc, pose, AmethystCrabBossEntity.centerBeamOffset(yaw2),
                    AmethystCrabBossEntity.beamDirection(yaw2, 0.0F), entity.getBeamLength(1), time);
        }
    }

    /** Un rayo de origin hacia dir con largo length: brillo ancho, centro violeta y nucleo casi blanco. */
    private static void beam(VertexConsumer vc, Matrix4f pose, Vec3 origin, Vec3 dir, float length, float time) {
        if (length < 0.1F) {
            return;
        }
        Vec3 end = origin.add(dir.scale(length));
        Vec3 helper = Math.abs(dir.y) > 0.95D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 u = dir.cross(helper).normalize();
        Vec3 v = dir.cross(u).normalize();
        float flicker = 0.85F + 0.15F * Mth.sin(time * 1.9F);
        float pulse = 1.0F + 0.12F * Mth.sin(time * 0.9F);
        float r = BoltColor.VIOLET.r;
        float g = BoltColor.VIOLET.g;
        float b = BoltColor.VIOLET.b;
        strip(vc, pose, origin, end, u, 0.60F * pulse, r, g, b, 0.16F * flicker);
        strip(vc, pose, origin, end, v, 0.60F * pulse, r, g, b, 0.16F * flicker);
        strip(vc, pose, origin, end, u, 0.34F * pulse, r, g, b, 0.40F * flicker);
        strip(vc, pose, origin, end, v, 0.34F * pulse, r, g, b, 0.40F * flicker);
        float cr = Mth.lerp(0.8F, r, 1.0F);
        float cg = Mth.lerp(0.8F, g, 1.0F);
        float cb = Mth.lerp(0.8F, b, 1.0F);
        strip(vc, pose, origin, end, u, 0.14F, cr, cg, cb, 0.95F * flicker);
        strip(vc, pose, origin, end, v, 0.14F, cr, cg, cb, 0.95F * flicker);
    }

    /** Una tira de ancho 2w a lo largo del rayo, dibujada por los dos lados. */
    private static void strip(VertexConsumer vc, Matrix4f pose, Vec3 o, Vec3 e, Vec3 side, float w, float r, float g, float b, float a) {
        float sx = (float) side.x * w;
        float sy = (float) side.y * w;
        float sz = (float) side.z * w;
        float ox = (float) o.x;
        float oy = (float) o.y;
        float oz = (float) o.z;
        float ex = (float) e.x;
        float ey = (float) e.y;
        float ez = (float) e.z;
        vertex(vc, pose, ox - sx, oy - sy, oz - sz, r, g, b, a);
        vertex(vc, pose, ox + sx, oy + sy, oz + sz, r, g, b, a);
        vertex(vc, pose, ex + sx, ey + sy, ez + sz, r, g, b, a);
        vertex(vc, pose, ex - sx, ey - sy, ez - sz, r, g, b, a);
        // la misma tira dada vuelta
        vertex(vc, pose, ex - sx, ey - sy, ez - sz, r, g, b, a);
        vertex(vc, pose, ex + sx, ey + sy, ez + sz, r, g, b, a);
        vertex(vc, pose, ox + sx, oy + sy, oz + sz, r, g, b, a);
        vertex(vc, pose, ox - sx, oy - sy, oz - sz, r, g, b, a);
    }

    private static void vertex(VertexConsumer vc, Matrix4f pose, float x, float y, float z, float r, float g, float b, float a) {
        vc.vertex(pose, x, y, z).color(r, g, b, a).endVertex();
    }
}
