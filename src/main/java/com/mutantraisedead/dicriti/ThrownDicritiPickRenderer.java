package com.mutantraisedead.dicriti;

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
import net.minecraft.world.phys.Vec3;

/** Dibuja el pico (el que lanzaste, con sus encantamientos) girando como un boomerang. */
public class ThrownDicritiPickRenderer extends EntityRenderer<ThrownDicritiPickEntity> {
    private final ItemRenderer itemRenderer;

    public ThrownDicritiPickRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownDicritiPickEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(ThrownDicritiPickEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light) {
        ItemStack stack = entity.getStack();
        if (stack.isEmpty()) {
            return;
        }
        float age = entity.tickCount + partialTick;
        Vec3 velocity = entity.getDeltaMovement();
        // el dibujo plano del pico queda paralelo a la direccion en que viaja, y gira sobre si mismo
        float heading = (float) Math.toDegrees(Math.atan2(-velocity.z, velocity.x));
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.15D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(heading));
        poseStack.mulPose(Axis.ZP.rotationDegrees(age * 55.0F));
        poseStack.scale(1.7F, 1.7F, 1.7F);
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, 0xF000F0, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
    }
}
