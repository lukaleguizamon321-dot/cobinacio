package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Cangrejo gigante: cuerpo de piedra cubierto con armadura de hierro en bruto,
 * patas de hierro en bruto y dos tenazas que son bloques de hierro en bruto.
 * Textura 256x128: izquierda = piedra, derecha = hierro en bruto.
 */
public class GiantIronCrabModel extends EntityModel<GiantIronCrabEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "giant_iron_crab"), "main");

    private final ModelPart root;
    private final ModelPart[] legs = new ModelPart[6];
    private final ModelPart clawLeft;
    private final ModelPart clawRight;
    private final ModelPart[] plates = new ModelPart[3];

    public GiantIronCrabModel(ModelPart root) {
        this.root = root;
        for (int i = 0; i < 6; i++) {
            legs[i] = root.getChild("leg" + i);
        }
        clawLeft = root.getChild("claw_left");
        clawRight = root.getChild("claw_right");
        for (int i = 0; i < 3; i++) {
            plates[i] = root.getChild("plate" + i);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Cuerpo de piedra
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-9.0F, -16.0F, -9.0F, 18.0F, 10.0F, 18.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // Armadura de hierro en bruto: placas apiladas arriba (lisas, suben y bajan suave)
        float[][] plates = {{18, 4}, {12, 4}, {6, 3}};
        float y = -16.0F;
        for (int i = 0; i < plates.length; i++) {
            float w = plates[i][0];
            float h = plates[i][1];
            root.addOrReplaceChild("plate" + i, CubeListBuilder.create()
                            .texOffs(128, 0).addBox(-w / 2, -h, -w / 2, w, h, w),
                    PartPose.offset(0.0F, 24.0F + y, 0.0F));
            y -= h;
        }
        // Placas laterales y trasera
        root.addOrReplaceChild("armor_sides", CubeListBuilder.create()
                        .texOffs(128, 0).addBox(-11.0F, -16.0F, -7.0F, 2.0F, 9.0F, 14.0F)
                        .texOffs(128, 0).addBox(9.0F, -16.0F, -7.0F, 2.0F, 9.0F, 14.0F)
                        .texOffs(128, 0).addBox(-7.0F, -16.0F, 9.0F, 14.0F, 9.0F, 2.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // 6 patas de hierro en bruto
        for (int i = 0; i < 6; i++) {
            float side = i < 3 ? 1.0F : -1.0F;
            float z = (i % 3 - 1) * 6.0F;
            root.addOrReplaceChild("leg" + i, CubeListBuilder.create()
                            .texOffs(128, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                    PartPose.offsetAndRotation(side * 10.0F, 17.0F, z, 0.0F, 0.0F, -side * 1.0F));
        }

        // Tenazas: brazo + bloque de hierro en bruto, al frente
        for (int i = 0; i < 2; i++) {
            float side = i == 0 ? 1.0F : -1.0F;
            root.addOrReplaceChild(i == 0 ? "claw_left" : "claw_right", CubeListBuilder.create()
                            .texOffs(128, 0).addBox(-2.0F, -2.0F, -10.0F, 4.0F, 4.0F, 10.0F)
                            .texOffs(128, 0).addBox(-4.0F, -4.0F, -18.0F, 8.0F, 8.0F, 8.0F)
                            .texOffs(128, 0).addBox(-2.0F, -7.0F, -17.0F, 4.0F, 3.0F, 6.0F),
                    PartPose.offset(side * 8.0F, 14.0F, -9.0F));
        }
        return LayerDefinition.create(mesh, 256, 128);
    }

    @Override
    public void setupAnim(GiantIronCrabEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Saliendo del suelo
        float emerge = entity.getEmergeTicks() / (float) GiantIronCrabEntity.EMERGE_TICKS;
        root.y = emerge * 24.0F;

        for (int i = 0; i < 6; i++) {
            float side = i < 3 ? 1.0F : -1.0F;
            float baseYaw = (i % 3 - 1) * 0.3F * side;
            legs[i].yRot = baseYaw + Mth.cos(limbSwing * 0.7F + (i % 2) * Mth.PI) * 0.5F * limbSwingAmount;
        }

        // Armadura lisa: respira suave
        for (int i = 0; i < plates.length; i++) {
            plates[i].y = 24.0F - 16.0F - (i == 0 ? 0 : i == 1 ? 4 : 8) + Mth.sin(ageInTicks * 0.06F) * 0.6F;
        }

        // Tenazas: golpe hacia abajo al atacar, y se abren/cierran al descansar
        float swing = Mth.sin(entity.attackAnim * Mth.PI);
        float idle = Mth.sin(ageInTicks * 0.08F) * 0.08F;
        clawLeft.xRot = -0.45F + swing * 1.0F + idle;
        clawRight.xRot = -0.45F + swing * 1.0F - idle;
        clawLeft.yRot = 0.15F + swing * 0.3F;
        clawRight.yRot = -0.15F - swing * 0.3F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
