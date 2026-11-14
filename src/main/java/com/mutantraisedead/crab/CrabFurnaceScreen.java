package com.mutantraisedead.crab;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Pantalla del horno del cangrejo: usa el dibujo del horno comun (asi respeta tu paquete de texturas),
 * tapa la ranura de combustible y deja solo la llama, que se prende mientras cocina.
 */
public class CrabFurnaceScreen extends AbstractContainerScreen<CrabFurnaceMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/furnace.png");
    /** Gris del fondo de las ventanas de Minecraft. */
    private static final int BACKGROUND_GRAY = 0xFFC6C6C6;

    public CrabFurnaceScreen(CrabFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        // sin ranura de combustible: se tapa el cuadrito de abajo (el dibujo del horno lo trae a 55,52 y mide 18x18)
        graphics.fill(x + 55, y + 52, x + 73, y + 70, BACKGROUND_GRAY);
        if (this.menu.isCooking()) {
            // llama prendida, siempre entera: el calor lo pone el cangrejo
            graphics.blit(TEXTURE, x + 56, y + 36, 176, 0, 14, 14);
        }
        int arrow = Mth.clamp(this.menu.getBurnProgress(), 0, 24);
        graphics.blit(TEXTURE, x + 79, y + 34, 176, 14, arrow, 16);
    }
}
