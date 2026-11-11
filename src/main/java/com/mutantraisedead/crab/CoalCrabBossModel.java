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
 * Modelo PROVISORIO del jefe de carbon: es el modelo del cangrejo comun (el de Blockbench, "cangry.bbmodel"),
 * que el renderer agranda y estira. Cuando haya un modelo propio del jefe, se cambia este archivo.
 * Mismas piezas moviles que el cangrejo: body, shell, leg0..leg3, clawP / clawN.
 * Las animaciones usan las mismas direcciones que en Blockbench (Y arriba, el frente es -Z).
 */
public class CoalCrabBossModel extends EntityModel<CoalCrabBossEntity> {
    private static final float UNIT = 1.0F / 16.0F;
    private static final float FLOOR = 1.501F;
    private static final String[] ORDER = {"body", "shell", "leg0", "leg1", "leg2", "leg3", "clawP", "clawN"};

    /** Cuanto se hunde (en bloques del modelo, antes de agrandarlo) mientras sale del suelo. */
    private static final float AWAKEN_DEPTH = 1.3F;

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

    private static final Group[] GROUPS = parse(CrabMeshData.DATA);

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
    public void setupAnim(CoalCrabBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        for (int i = 0; i < ORDER.length; i++) {
            rx[i] = 0.0F;
            ry[i] = 0.0F;
            rz[i] = 0.0F;
        }
        int action = entity.getAction();
        int t = entity.clientActionTicks;
        float walk = Mth.clamp(limbSwingAmount * 1.8F, 0.0F, 1.0F);
        float legSpeed = action == CoalCrabBossEntity.ACTION_CHARGING ? 1.9F : 1.1F;

        // Patas: de a pares en diagonal
        for (int i = 0; i < 4; i++) {
            int idx = 2 + i;
            float side = i < 2 ? 1.0F : -1.0F;
            float phase = (i == 0 || i == 2) ? 0.0F : Mth.PI;
            float s = limbSwing * legSpeed + phase;
            ry[idx] = side * Mth.cos(s) * 0.35F * walk;
            rz[idx] = side * (Math.max(0.0F, Mth.sin(s)) * 0.30F * walk + Mth.sin(ageInTicks * 0.1F + i * 1.7F) * 0.02F);
        }

        // Pinzas y caparazon, segun lo que esta haciendo
        for (int i = 0; i < 2; i++) {
            int idx = 6 + i;
            float side = i == 0 ? 1.0F : -1.0F;
            switch (action) {
                case CoalCrabBossEntity.ACTION_AWAKEN -> {
                    // pinzas bien abiertas y en alto, como un rugido
                    rx[idx] = 1.0F + Mth.sin(ageInTicks * 0.9F + i) * 0.1F;
                    ry[idx] = side * (0.55F + Mth.sin(ageInTicks * 0.5F) * 0.15F);
                }
                case CoalCrabBossEntity.ACTION_FIREBALL -> {
                    rx[idx] = 0.9F + Mth.sin(ageInTicks * 1.4F + i) * 0.1F;
                    ry[idx] = side * (0.2F + Mth.sin(ageInTicks * 1.6F) * 0.2F);
                }
                case CoalCrabBossEntity.ACTION_CHARGE_WINDUP -> {
                    // pinzas abiertas hacia los costados, temblando
                    rx[idx] = 0.4F;
                    ry[idx] = side * (0.7F + Mth.sin(ageInTicks * 2.2F) * 0.06F);
                }
                case CoalCrabBossEntity.ACTION_CHARGING -> {
                    // pinzas juntas hacia adelante
                    rx[idx] = 0.15F;
                    ry[idx] = side * -0.1F;
                }
                case CoalCrabBossEntity.ACTION_FLAME_WINDUP, CoalCrabBossEntity.ACTION_FLAMING -> {
                    rx[idx] = 1.1F + Mth.sin(ageInTicks * 0.8F + i) * 0.05F;
                    ry[idx] = side * 0.6F;
                }
                case CoalCrabBossEntity.ACTION_STUNNED -> {
                    // pinzas caidas
                    rx[idx] = -0.15F;
                    ry[idx] = side * 0.25F;
                }
                default -> {
                    boolean alert = entity.hurtTime > 0;
                    if (alert) {
                        rx[idx] = 0.8F + Mth.sin(ageInTicks * 1.4F + i) * 0.12F;
                        ry[idx] = side * (0.2F + Mth.sin(ageInTicks * 1.6F) * 0.3F);
                    } else {
                        rx[idx] = 0.1F + Mth.sin(ageInTicks * 0.12F + i * 1.3F) * 0.04F;
                        ry[idx] = side * (0.05F + Mth.sin(ageInTicks * 0.09F + i * 1.3F) * 0.12F);
                    }
                }
            }
        }

        // Caparazon: respira, tiembla al prepararse y se sacude al aturdirse
        rx[1] = Mth.sin(ageInTicks * 0.07F) * 0.025F;
        rz[1] = Mth.sin(limbSwing * 1.1F) * 0.03F * walk;
        switch (action) {
            case CoalCrabBossEntity.ACTION_AWAKEN, CoalCrabBossEntity.ACTION_CHARGE_WINDUP ->
                    rz[1] += Mth.sin(ageInTicks * 2.4F) * 0.06F;
            case CoalCrabBossEntity.ACTION_FLAMING -> rz[1] += Mth.sin(ageInTicks * 1.8F) * 0.03F;
            case CoalCrabBossEntity.ACTION_STUNNED -> rz[1] += Mth.sin(ageInTicks * 0.7F) * 0.1F;
            default -> {
            }
        }

        // Rebote al caminar
        bob = Math.abs(Mth.cos(limbSwing * legSpeed)) * 0.02F * walk;

        // Al despertar sale del suelo
        if (action == CoalCrabBossEntity.ACTION_AWAKEN) {
            float emerge = 1.0F - Mth.clamp((t + (ageInTicks - (int) ageInTicks)) / (float) CoalCrabBossEntity.AWAKEN_TICKS, 0.0F, 1.0F);
            sink = emerge * AWAKEN_DEPTH;
        } else {
            sink = 0.0F;
        }
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
