package com.mutantraisedead.goggles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Lado del cliente de las Gafas de amatista: manda la tecla al servidor y dibuja el laser de cada jugador que lo tiene
 * encendido (el mismo rayo del jefe: brillo ancho, centro de color y nucleo casi blanco, pero mas finito).
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID, value = Dist.CLIENT)
public final class GogglesClientEvents {
    private record ClientBeam(int rgb, long endTime) {
    }

    /** Jugadores (por numero de entidad) con el laser encendido. */
    private static final Map<Integer, ClientBeam> BEAMS = new HashMap<>();

    private GogglesClientEvents() {
    }

    /** Llega del servidor: rayo encendido (ticks mas de duracion) o apagado (ticks = 0). */
    public static void receive(int entityId, int rgb, int ticks) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (ticks <= 0) {
            BEAMS.remove(entityId);
        } else {
            BEAMS.put(entityId, new ClientBeam(rgb, level.getGameTime() + ticks));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        while (GogglesKeys.ACTIVATE.consumeClick()) {
            if (mc.player != null && mc.screen == null) {
                GogglesNetwork.sendKeyToServer();
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        BEAMS.clear();
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || BEAMS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        float partialTick = event.getPartialTick();
        long now = level.getGameTime();
        Vec3 cam = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(RenderType.lightning());

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f pose = poseStack.last().pose();
        Iterator<Map.Entry<Integer, ClientBeam>> it = BEAMS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ClientBeam> entry = it.next();
            ClientBeam active = entry.getValue();
            Entity entity = level.getEntity(entry.getKey());
            if (now >= active.endTime() || (entity != null && !entity.isAlive())) {
                it.remove();
                continue;
            }
            if (!(entity instanceof Player player)) {
                continue;
            }
            Vec3 eye = player.getEyePosition(partialTick);
            Vec3 look = player.getViewVector(partialTick);
            Vec3 origin = GogglesGeometry.origin(eye, look);
            Vec3 target = GogglesGeometry.target(level, player, eye, look);
            beam(vc, pose, origin, target, active.rgb(), now + partialTick + entry.getKey());
        }
        poseStack.popPose();
        buffers.endBatch(RenderType.lightning());
    }

    /** Un rayo de origin a end: brillo ancho, color del tinte y nucleo casi blanco. */
    private static void beam(VertexConsumer vc, Matrix4f pose, Vec3 origin, Vec3 end, int rgb, float time) {
        Vec3 delta = end.subtract(origin);
        double length = delta.length();
        if (length < 0.3D) {
            return;
        }
        Vec3 dir = delta.scale(1.0D / length);
        Vec3 helper = Math.abs(dir.y) > 0.95D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 u = dir.cross(helper).normalize();
        Vec3 v = dir.cross(u).normalize();
        float flicker = 0.85F + 0.15F * Mth.sin(time * 1.9F);
        float pulse = 1.0F + 0.12F * Mth.sin(time * 0.9F);
        float r = ((rgb >> 16) & 255) / 255.0F;
        float g = ((rgb >> 8) & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;
        strip(vc, pose, origin, end, u, 0.20F * pulse, r, g, b, 0.16F * flicker);
        strip(vc, pose, origin, end, v, 0.20F * pulse, r, g, b, 0.16F * flicker);
        strip(vc, pose, origin, end, u, 0.11F * pulse, r, g, b, 0.40F * flicker);
        strip(vc, pose, origin, end, v, 0.11F * pulse, r, g, b, 0.40F * flicker);
        float cr = Mth.lerp(0.8F, r, 1.0F);
        float cg = Mth.lerp(0.8F, g, 1.0F);
        float cb = Mth.lerp(0.8F, b, 1.0F);
        strip(vc, pose, origin, end, u, 0.045F, cr, cg, cb, 0.95F * flicker);
        strip(vc, pose, origin, end, v, 0.045F, cr, cg, cb, 0.95F * flicker);
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
