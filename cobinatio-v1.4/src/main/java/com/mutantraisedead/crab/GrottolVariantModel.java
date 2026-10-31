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

import java.util.Locale;

/**
 * Modelo del cangrejo: cuerpo de piedra con 6 patas y, arriba, el material.
 * Textura 128x64: la mitad izquierda es la piedra, la derecha es el material.
 *  - METAL: placas lisas apiladas, con animacion suave
 *  - CRYSTAL: cristales puntiagudos, con animacion entrecortada
 *  - LUMP: trozos irregulares (carbon), con temblor
 */
public class GrottolVariantModel extends EntityModel<GrottolVariantEntity> {
    private static final int TOP_COUNT_METAL = 3;
    private static final int TOP_COUNT_CRYSTAL = 6;
    private static final int TOP_COUNT_LUMP = 5;

    private final GrottolVariant.Shape shape;
    private final ModelPart root;
    private final ModelPart[] legs = new ModelPart[6];
    private final ModelPart[] top;

    public GrottolVariantModel(ModelPart root, GrottolVariant.Shape shape) {
        this.root = root;
        this.shape = shape;
        for (int i = 0; i < 6; i++) {
            legs[i] = root.getChild("leg" + i);
        }
        int count = switch (shape) {
            case METAL -> TOP_COUNT_METAL;
            case CRYSTAL -> TOP_COUNT_CRYSTAL;
            case LUMP -> TOP_COUNT_LUMP;
        };
        top = new ModelPart[count];
        for (int i = 0; i < count; i++) {
            top[i] = root.getChild("top" + i);
        }
    }

    public static ModelLayerLocation layer(GrottolVariant.Shape shape) {
        return new ModelLayerLocation(
                ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "grottol_" + shape.name().toLowerCase(Locale.ROOT)), "main");
    }

    public static LayerDefinition createLayer(GrottolVariant.Shape shape) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Cuerpo de piedra (textura de la izquierda)
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 6.0F, 10.0F)
                        .texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 1.0F, 8.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        // 6 patas: 3 por lado, abiertas hacia afuera
        for (int i = 0; i < 6; i++) {
            float side = i < 3 ? 1.0F : -1.0F;
            float z = (i % 3 - 1) * 3.5F;
            root.addOrReplaceChild("leg" + i, CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
                    PartPose.offsetAndRotation(side * 5.0F, 20.0F, z, 0.0F, 0.0F, -side * 0.9F));
        }

        // Material de arriba (textura de la derecha)
        switch (shape) {
            case METAL -> {
                float[][] plates = {{8, 2}, {6, 2}, {4, 2}};
                float y = -10.0F;
                for (int i = 0; i < plates.length; i++) {
                    float w = plates[i][0];
                    root.addOrReplaceChild("top" + i, CubeListBuilder.create()
                                    .texOffs(64, 0).addBox(-w / 2, -2.0F, -w / 2, w, 2.0F, w),
                            PartPose.offset(0.0F, 24.0F + y, 0.0F));
                    y -= 2.0F;
                }
            }
            case CRYSTAL -> {
                float[] heights = {11, 8, 7, 9, 6, 7};
                for (int i = 0; i < TOP_COUNT_CRYSTAL; i++) {
                    float angle = i == 0 ? 0.0F : (i - 1) * (Mth.TWO_PI / 5.0F);
                    float radius = i == 0 ? 0.0F : 2.8F;
                    float px = Mth.cos(angle) * radius;
                    float pz = Mth.sin(angle) * radius;
                    float tilt = i == 0 ? 0.0F : 0.4F;
                    root.addOrReplaceChild("top" + i, CubeListBuilder.create()
                                    .texOffs(64, 0).addBox(-1.5F, -heights[i], -1.5F, 3.0F, heights[i], 3.0F),
                            PartPose.offsetAndRotation(px, 14.0F, pz, -Mth.sin(angle) * tilt, 0.0F, Mth.cos(angle) * tilt));
                }
            }
            case LUMP -> {
                // {x, y, z, ancho, alto, fondo, rotacion}
                float[][] lumps = {
                        {-1.5F, -10.0F, 0.5F, 6, 5, 6, 0.3F},
                        {2.0F, -10.0F, -2.5F, 5, 4, 5, -0.4F},
                        {-2.5F, -13.0F, -1.5F, 4, 6, 4, 0.7F},
                        {1.5F, -13.0F, 1.0F, 4, 3, 4, -0.2F},
                        {0.0F, -16.0F, 0.0F, 3, 3, 3, 0.5F}
                };
                for (int i = 0; i < lumps.length; i++) {
                    float[] l = lumps[i];
                    root.addOrReplaceChild("top" + i, CubeListBuilder.create()
                                    .texOffs(64, 0).addBox(-l[3] / 2, -l[4], -l[5] / 2, l[3], l[4], l[5]),
                            PartPose.offsetAndRotation(l[0], 24.0F + l[1], l[2], 0.0F, l[6], 0.0F));
                }
            }
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(GrottolVariantEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Patas: caminan de costado como un cangrejo
        for (int i = 0; i < 6; i++) {
            float side = i < 3 ? 1.0F : -1.0F;
            float baseYaw = (i % 3 - 1) * 0.35F * side;
            legs[i].yRot = baseYaw + Mth.cos(limbSwing * 0.9F + (i % 2) * Mth.PI) * 0.6F * limbSwingAmount;
        }

        switch (shape) {
            case METAL -> {
                // Suave y continuo: las placas giran despacio y "respiran"
                for (int i = 0; i < top.length; i++) {
                    top[i].yRot = Mth.sin(ageInTicks * 0.04F + i * 0.9F) * 0.35F;
                    top[i].y = 24.0F - 10.0F - i * 2.0F + Mth.sin(ageInTicks * 0.08F) * 0.5F;
                }
            }
            case CRYSTAL -> {
                // Entrecortado: cada cristal salta de posicion cada pocos ticks
                int step = (int) (ageInTicks / 5.0F);
                for (int i = 0; i < top.length; i++) {
                    float jitter = Mth.sin(step * 1.7F + i * 2.3F);
                    float baseX = i == 0 ? 0.0F : -Mth.sin((i - 1) * (Mth.TWO_PI / 5.0F)) * 0.4F;
                    float baseZ = i == 0 ? 0.0F : Mth.cos((i - 1) * (Mth.TWO_PI / 5.0F)) * 0.4F;
                    top[i].xRot = baseX + jitter * 0.08F;
                    top[i].zRot = baseZ + Mth.cos(step * 1.3F + i) * 0.08F;
                }
            }
            case LUMP -> {
                // Temblor rugoso del carbon
                int step = (int) (ageInTicks / 2.0F);
                for (int i = 0; i < top.length; i++) {
                    top[i].xRot = Mth.sin(step * 2.1F + i * 3.1F) * 0.05F;
                    top[i].zRot = Mth.cos(step * 1.9F + i * 1.7F) * 0.05F;
                }
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
