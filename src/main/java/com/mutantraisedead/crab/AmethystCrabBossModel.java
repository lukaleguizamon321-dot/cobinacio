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
 * Modelo del jefe de amatista (Cangrejo Titan de Amatista), hecho en Blockbench por vos
 * ("boss_amatista_panza_piedra_arreglada.bbmodel"), con su textura (textures/entity/amethyst_crab_boss.png, 512 x 512).
 * Piezas moviles: body (cuerpo con la panza de piedra), crystals (cristales de la espalda), leg0..leg3 (leg0/leg1 = lado +x,
 * delante/atras; leg2/leg3 = lado -x; los cristales sueltos de arriba de las patas se mueven con su pata) y clawP (+x) / clawN (-x).
 * Las pinzas son placas que apuntan hacia adelante y hacia el centro; para "levantarlas" se giran sobre su base (rx = punta arriba,
 * rz = lado de afuera arriba). Las animaciones usan las mismas direcciones que en Blockbench (Y arriba, el frente es -Z).
 * La geometria esta en AmethystMeshData; el renderer lo agranda con AmethystCrabBossEntity.MODEL_SCALE.
 */
public class AmethystCrabBossModel extends EntityModel<AmethystCrabBossEntity> {
    private static final float UNIT = 1.0F / 16.0F;
    private static final float FLOOR = 1.501F;
    private static final String[] ORDER = {"body", "crystals", "leg0", "leg1", "leg2", "leg3", "clawP", "clawN"};

    private static final int BODY = 0;
    private static final int CRYSTALS = 1;
    private static final int CLAW = 6;

    /** Cuanto se hunde (en bloques del modelo, antes de agrandarlo) mientras sale del suelo: todo el cuerpo con los cristales. */
    private static final float AWAKEN_DEPTH = 8.4F;

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

    private static final Group[] GROUPS = parse(AmethystMeshData.BOSS);

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

    private static Group[] parse(String[] chunks) {
        Group[] groups = new Group[ORDER.length];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = new Group();
        }
        for (String data : chunks) {
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
        }
        return groups;
    }

    /**
     * Levanta una pinza: tip = la punta sube, outer = el lado de afuera sube (side = 1 para clawP, -1 para clawN).
     * (En este modelo la pinza es una placa que cuelga adelante: para que la punta suba hay que girar al reves, por eso rx lleva signo menos.)
     */
    private void raiseClaw(int idx, float side, float tip, float outer) {
        rx[idx] = -tip;
        rz[idx] = side * outer;
    }

    @Override
    public void setupAnim(AmethystCrabBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        for (int i = 0; i < ORDER.length; i++) {
            rx[i] = 0.0F;
            ry[i] = 0.0F;
            rz[i] = 0.0F;
        }
        int action = entity.getAction();
        int t = entity.clientActionTicks;
        float walk = Mth.clamp(limbSwingAmount * 1.8F, 0.0F, 1.0F);

        // Patas: de a pares en diagonal
        for (int i = 0; i < 4; i++) {
            int idx = 2 + i;
            float side = i < 2 ? 1.0F : -1.0F;
            float phase = (i == 0 || i == 2) ? 0.0F : Mth.PI;
            float s = limbSwing * 1.1F + phase;
            ry[idx] = side * Mth.cos(s) * 0.20F * walk;
            rz[idx] = side * (Math.max(0.0F, Mth.sin(s)) * 0.17F * walk + Mth.sin(ageInTicks * 0.1F + i * 1.7F) * 0.012F);
        }

        // Pinzas, segun lo que esta haciendo
        int aimSide = entity.getAimSide();
        for (int i = 0; i < 2; i++) {
            int idx = CLAW + i;
            float side = i == 0 ? 1.0F : -1.0F;
            switch (action) {
                case AmethystCrabBossEntity.ACTION_AWAKEN -> {
                    // pinzas bien abiertas y en alto, como un rugido
                    raiseClaw(idx, side, 0.7F + Mth.sin(ageInTicks * 0.9F + i) * 0.08F, 0.55F + Mth.sin(ageInTicks * 0.5F) * 0.1F);
                }
                case AmethystCrabBossEntity.ACTION_LASER_AIM, AmethystCrabBossEntity.ACTION_LASER_FIRE -> {
                    if (i == aimSide) {
                        // la pinza que apunta: punta arriba y temblando mientras carga
                        float shake = action == AmethystCrabBossEntity.ACTION_LASER_AIM ? Mth.sin(ageInTicks * 2.2F) * 0.03F : 0.0F;
                        raiseClaw(idx, side, AmethystCrabBossEntity.AIM_TIP + shake, AmethystCrabBossEntity.AIM_OUTER + shake);
                    } else {
                        raiseClaw(idx, side, 0.1F, 0.05F);
                    }
                }
                case AmethystCrabBossEntity.ACTION_SPIKES -> {
                    // las dos pinzas suben y despues golpean el suelo
                    float up = t < 12 ? Mth.clamp(t / 10.0F, 0.0F, 1.0F) : Mth.clamp(1.0F - (t - 12) / 3.0F, 0.0F, 1.0F);
                    raiseClaw(idx, side, 0.8F * up, 0.6F * up);
                }
                case AmethystCrabBossEntity.ACTION_CENTER_CHARGE, AmethystCrabBossEntity.ACTION_CENTER_LASER -> {
                    // pinzas arriba y temblando: junta energia
                    raiseClaw(idx, side, 0.6F + Mth.sin(ageInTicks * 1.7F + i) * 0.04F, 0.5F + Mth.sin(ageInTicks * 2.0F) * 0.04F);
                }
                case AmethystCrabBossEntity.ACTION_CENTER_SHOWER -> {
                    // pinzas en alto, subiendo y bajando para tirar las amatistas
                    raiseClaw(idx, side, 0.9F + Mth.sin(ageInTicks * 0.6F + i * Mth.PI) * 0.25F, 0.7F + Mth.sin(ageInTicks * 0.6F + i * Mth.PI) * 0.12F);
                }
                default -> {
                    boolean alert = entity.hurtTime > 0;
                    if (alert) {
                        raiseClaw(idx, side, 0.4F + Mth.sin(ageInTicks * 1.4F + i) * 0.1F, 0.3F + Mth.sin(ageInTicks * 1.6F) * 0.1F);
                    } else {
                        raiseClaw(idx, side, 0.05F + Mth.sin(ageInTicks * 0.12F + i * 1.3F) * 0.03F,
                                0.03F + Mth.sin(ageInTicks * 0.09F + i * 1.3F) * 0.04F);
                    }
                }
            }
        }

        // Cristales de la espalda: titilan un poco; mas fuerte al juntar energia
        rz[CRYSTALS] = Mth.sin(ageInTicks * 0.09F) * 0.012F;
        rx[CRYSTALS] = Mth.sin(ageInTicks * 0.07F + 1.0F) * 0.012F;
        switch (action) {
            case AmethystCrabBossEntity.ACTION_AWAKEN, AmethystCrabBossEntity.ACTION_CENTER_CHARGE,
                    AmethystCrabBossEntity.ACTION_CENTER_SHOWER -> rz[CRYSTALS] += Mth.sin(ageInTicks * 2.6F) * 0.03F;
            case AmethystCrabBossEntity.ACTION_LASER_AIM -> rz[CRYSTALS] += Mth.sin(ageInTicks * 3.0F) * 0.02F;
            default -> {
            }
        }

        // Cuerpo: respira
        rx[BODY] = Mth.sin(ageInTicks * 0.07F) * 0.012F;

        // Rebote al caminar
        bob = Math.abs(Mth.cos(limbSwing * 1.1F)) * 0.06F * walk;

        // Al despertar sale del suelo
        if (action == AmethystCrabBossEntity.ACTION_AWAKEN) {
            float emerge = 1.0F - Mth.clamp((t + (ageInTicks - (int) ageInTicks)) / (float) AmethystCrabBossEntity.AWAKEN_TICKS, 0.0F, 1.0F);
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
