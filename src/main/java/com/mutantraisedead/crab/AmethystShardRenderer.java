package com.mutantraisedead.crab;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Dibuja la amatista que tira el jefe: un fragmento de amatista grande, girando y brillando. */
public class AmethystShardRenderer extends EntityRenderer<AmethystShardEntity> {
    private final ItemRenderer itemRenderer;
    private final ItemStack stack = new ItemStack(Items.AMETHYST_SHARD);

    public AmethystShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public ResourceLocation getTextureLocation(AmethystShardEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(AmethystShardEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float age = entity.tickCount + partialTick;
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.25D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(age * 24.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(35.0F));
        poseStack.scale(2.4F, 2.4F, 2.4F);
        this.itemRenderer.renderStatic(this.stack, ItemDisplayContext.GROUND, 0xF000F0, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
    }
}
