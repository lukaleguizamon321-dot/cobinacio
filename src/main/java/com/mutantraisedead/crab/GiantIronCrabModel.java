package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * Cangrejo gigante hecho en Blockbench ("cangrejo_gigante.bbmodel"):
 * caparazon de 2x2 bloques de mineral, patas con mineral al frente y tenazas de bloque de hierro en bruto.
 * Piezas moviles: body (cuerpo y ojos), shell (caparazon), leg0..leg3 (patas), clawP / clawN (tenazas).
 * Las animaciones se escriben con las direcciones de Blockbench (Y arriba, el frente es -Z).
 */
public class GiantIronCrabModel extends EntityModel<GiantIronCrabEntity> {
    private static final float UNIT = 1.0F / 16.0F;
    private static final float FLOOR = 1.501F;
    private static final String[] ORDER = {"body", "shell", "leg0", "leg1", "leg2", "leg3", "clawP", "clawN"};

    /** Cuanto se hunde (en bloques del modelo) mientras sale del suelo. */
    private static final float EMERGE_DEPTH = 2.9F;

    private static final class Quad {
        float nx;
        float ny;
        float nz;
        final float[] v = new float[20];
    }

    private static final class Group {
        float px;
        float py;
        float pz;
        final List<Quad> quads = new ArrayList<>();
    }

    private static final Group[] GROUPS = parse(CrabMeshData.GIANT);

    private final float[] rx = new float[ORDER.length];
    private final float[] ry = new float[ORDER.length];
    private final float[] rz = new float[ORDER.length];
    private float bob;
    private float sink;

    private static int indexOf(String name) {
        for (int i = 0; i < ORDER.length; i++) {
            if (ORDER[i].equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException("Pieza desconocida en el modelo: " + name);
    }

    private static Group[] parse(String data) {
        Group[] groups = new Group[ORDER.length];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = new Group();
        }
        for (String raw : data.split("\n")) {
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] t = line.split("\\s+");
            Group g = groups[indexOf(t[1])];
            if (t[0].equals("G")) {
                g.px = Float.parseFloat(t[2]);
                g.py = Float.parseFloat(t[3]);
                g.pz = Float.parseFloat(t[4]);
            } else {
                Quad q = new Quad();
                q.nx = Float.parseFloat(t[2]);
                q.ny = Float.parseFloat(t[3]);
                q.nz = Float.parseFloat(t[4]);
                for (int k = 0; k < 20; k++) {
                    q.v[k] = Float.parseFloat(t[5 + k]);
                }
                g.quads.add(q);
            }
        }
        return groups;
    }

    @Override
    public void setupAnim(GiantIronCrabEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        for (int i = 0; i < ORDER.length; i++) {
            rx[i] = 0.0F;
            ry[i] = 0.0F;
            rz[i] = 0.0F;
        }
        float walk = Mth.clamp(limbSwingAmount * 1.8F, 0.0F, 1.0F);

        // Saliendo del suelo
        float emerge = entity.getEmergeTicks() / (float) GiantIronCrabEntity.EMERGE_TICKS;
        sink = emerge * EMERGE_DEPTH;

        // Patas: pesadas, de a pares en diagonal
        for (int i = 0; i < 4; i++) {
            int idx = 2 + i;
            float side = i < 2 ? 1.0F : -1.0F;
            float phase = (i == 0 || i == 2) ? 0.0F : Mth.PI;
            float t = limbSwing * 0.8F + phase;
            ry[idx] = side * Mth.cos(t) * 0.25F * walk;
            rz[idx] = side * Math.max(0.0F, Mth.sin(t)) * 0.2F * walk;
        }

        // Tenazas: al atacar se levantan y golpean hacia abajo; en reposo abren y cierran despacio
        float swing = entity.attackAnim;
        for (int i = 0; i < 2; i++) {
            int idx = 6 + i;
            float side = i == 0 ? 1.0F : -1.0F;
            float idle = Mth.sin(ageInTicks * 0.08F + i * 1.3F);
            if (swing > 0.0F) {
                rx[idx] = 0.15F + 0.8F * Mth.sin(swing * Mth.TWO_PI);
                ry[idx] = side * (0.1F + 0.3F * Mth.sin(swing * Mth.PI));
            } else {
                rx[idx] = 0.15F + idle * 0.05F;
                ry[idx] = side * (0.1F + idle * 0.08F);
            }
        }

        // Caparazon: respira y se sacude un poco al caminar
        rx[1] = Mth.sin(ageInTicks * 0.05F) * 0.015F;
        rz[1] = Mth.sin(limbSwing * 0.8F) * 0.02F * walk;

        bob = Math.abs(Mth.cos(limbSwing * 0.8F)) * 0.03F * walk;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        for (int i = 0; i < GROUPS.length; i++) {
            Group g = GROUPS[i];
            poseStack.pushPose();
            // En el modelo de Minecraft Y apunta hacia abajo, asi que "subir" es restar.
            poseStack.translate(0.0F, sink - bob, 0.0F);

            float pvx = -g.px * UNIT;
            float pvy = FLOOR - g.py * UNIT;
            float pvz = g.pz * UNIT;
            poseStack.translate(pvx, pvy, pvz);
            poseStack.mulPose(new Quaternionf().rotationZYX(rz[i], -ry[i], -rx[i]));
            poseStack.translate(-pvx, -pvy, -pvz);

            Matrix4f pose = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();
            for (Quad q : g.quads) {
                float nx = -q.nx;
                float ny = -q.ny;
                float nz = q.nz;
                for (int k = 0; k < 4; k++) {
                    int o = k * 5;
                    buffer.vertex(pose, -q.v[o] * UNIT, FLOOR - q.v[o + 1] * UNIT, q.v[o + 2] * UNIT)
                            .color(red, green, blue, alpha)
                            .uv(q.v[o + 3], q.v[o + 4])
                            .overlayCoords(packedOverlay)
                            .uv2(packedLight)
                            .normal(normal, nx, ny, nz)
                            .endVertex();
                }
            }
            poseStack.popPose();
        }
    }
}
