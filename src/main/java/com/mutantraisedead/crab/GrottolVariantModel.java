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
 * Modelo del cangrejo hecho en Blockbench ("cangry.bbmodel").
 * La geometria esta en CrabMeshData y se divide en piezas moviles:
 *  body (cuerpo y ojos), shell (caparazon de arriba), leg0..leg3 (patas), clawP / clawN (pinzas).
 * Las animaciones se escriben con las mismas direcciones que en Blockbench
 * (Y arriba, el frente es -Z) y aca se pasan al sistema de Minecraft.
 */
public class GrottolVariantModel extends EntityModel<GrottolVariantEntity> {
    private static final float UNIT = 1.0F / 16.0F;
    private static final float FLOOR = 1.501F;
    private static final String[] ORDER = {"body", "shell", "leg0", "leg1", "leg2", "leg3", "clawP", "clawN"};

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

    private static final Group[] METAL_GROUPS = parse(CrabMeshData.DATA);
    private static final Group[] CRYSTAL_GROUPS = parse(CrabMeshData.CRYSTAL);

    private final Group[] groups;

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

    /** crystal = true para lapislazuli y amatista (llevan cristales en la espalda). */
    public GrottolVariantModel(boolean crystal) {
        this.groups = crystal ? CRYSTAL_GROUPS : METAL_GROUPS;
    }

    @Override
    public void setupAnim(GrottolVariantEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        for (int i = 0; i < ORDER.length; i++) {
            rx[i] = 0.0F;
            ry[i] = 0.0F;
            rz[i] = 0.0F;
        }
        float walk = Mth.clamp(limbSwingAmount * 1.8F, 0.0F, 1.0F);
        boolean alert = entity.clawTicks > 0 || entity.hurtTime > 0;

        // Patas: de a pares en diagonal, una sube mientras la otra apoya
        for (int i = 0; i < 4; i++) {
            int idx = 2 + i;
            float side = i < 2 ? 1.0F : -1.0F;
            float phase = (i == 0 || i == 2) ? 0.0F : Mth.PI;
            float t = limbSwing * 1.1F + phase;
            ry[idx] = side * Mth.cos(t) * 0.35F * walk;
            rz[idx] = side * (Math.max(0.0F, Mth.sin(t)) * 0.30F * walk + Mth.sin(ageInTicks * 0.1F + i * 1.7F) * 0.02F);
        }

        // Pinzas: quietas abren y cierran despacio; en alerta (habilidad o golpe) se levantan y chasquean
        for (int i = 0; i < 2; i++) {
            int idx = 6 + i;
            float side = i == 0 ? 1.0F : -1.0F;
            if (alert) {
                rx[idx] = 0.8F + Mth.sin(ageInTicks * 1.4F + i) * 0.12F;
                ry[idx] = side * (0.2F + Mth.sin(ageInTicks * 1.6F) * 0.3F);
            } else {
                rx[idx] = 0.1F + Mth.sin(ageInTicks * 0.12F + i * 1.3F) * 0.04F;
                ry[idx] = side * (0.05F + Mth.sin(ageInTicks * 0.09F + i * 1.3F) * 0.12F);
            }
        }

        // Caparazon: respira, y se sacude un poco al caminar
        rx[1] = Mth.sin(ageInTicks * 0.07F) * 0.025F;
        rz[1] = Mth.sin(limbSwing * 1.1F) * 0.03F * walk + (alert ? Mth.sin(ageInTicks * 2.0F) * 0.03F : 0.0F);

        // Rebote al caminar
        bob = Math.abs(Mth.cos(limbSwing * 1.1F)) * 0.02F * walk;

        // Enterrandose: se hunde en el suelo y las patas se agitan
        int burrow = entity.clientBurrowTicks;
        sink = burrow < 0 ? 0.0F : Mth.clamp(burrow / 18.0F, 0.0F, 1.0F) * 0.9F;
        if (burrow >= 0) {
            for (int i = 0; i < 4; i++) {
                int idx = 2 + i;
                float side = i < 2 ? 1.0F : -1.0F;
                rz[idx] = side * (0.2F + Mth.sin(ageInTicks * 1.2F + i) * 0.2F);
            }
        }
    }

    /**
     * Deja el PoseStack en el sistema de la pieza "shell" (caparazon), con el rebote y el temblor de ese momento.
     * Lo usa CrabFurnaceLayer para que el horno acompane al caparazon.
     */
    public void applyShellPose(PoseStack poseStack) {
        int i = indexOf("shell");
        Group g = groups[i];
        poseStack.translate(0.0F, sink - bob, 0.0F);
        float pvx = -g.px * UNIT;
        float pvy = FLOOR - g.py * UNIT;
        float pvz = g.pz * UNIT;
        poseStack.translate(pvx, pvy, pvz);
        poseStack.mulPose(new Quaternionf().rotationZYX(rz[i], -ry[i], -rx[i]));
        poseStack.translate(-pvx, -pvy, -pvz);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        for (int i = 0; i < groups.length; i++) {
            Group g = groups[i];
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
