package com.mutantraisedead.storage;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * El inventario de siempre, pero mas alto: abajo aparecen los 27 espacios extra del Doble Almacen,
 * como si fuera un cofre doble. Todo lo demas (armadura, crafteo, libro de recetas) queda igual.
 */
@OnlyIn(Dist.CLIENT)
public class DoubleInventoryScreen extends InventoryScreen {
    /** Alto del inventario normal. */
    private static final int VANILLA_HEIGHT = 166;
    /** Alto total con el panel de abajo. */
    private static final int FULL_HEIGHT = 236;
    /** Desde donde se dibuja el panel (se pisa un poco el borde de abajo del inventario normal). */
    private static final int PANEL_TOP = 162;

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BODY = 0xFFC6C6C6;
    private static final int SHADOW = 0xFF555555;
    private static final int SLOT_DARK = 0xFF373737;
    private static final int SLOT_BODY = 0xFF8B8B8B;

    /** El boton del libro de recetas (el unico boton con imagen que arma el inventario normal). */
    @Nullable
    private ImageButton recipeButton;

    public DoubleInventoryScreen(Player player) {
        super(player);
    }

    @Override
    protected void init() {
        // Se arma todo como el inventario normal pero con la pantalla "mas baja", asi la ventana entera
        // (inventario + panel) queda centrada y el libro de recetas y su boton quedan alineados.
        int realHeight = this.height;
        this.imageHeight = VANILLA_HEIGHT;
        this.height = realHeight - (FULL_HEIGHT - VANILLA_HEIGHT);
        try {
            super.init();
        } finally {
            this.height = realHeight;
        }
        // Ahora si, el alto real de la ventana (sirve para saber si se hizo clic afuera)
        this.imageHeight = FULL_HEIGHT;

        // Se guarda el boton del libro de recetas ahora, antes de que otros mods agreguen los suyos
        this.recipeButton = null;
        for (GuiEventListener child : this.children()) {
            if (child instanceof ImageButton button) {
                this.recipeButton = button;
                break;
            }
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        // Si te sacan el amuleto con la pantalla abierta, se cierra
        LocalPlayer player = this.minecraft == null ? null : this.minecraft.player;
        if (player != null) {
            ExtraContainer container = ExtraSlots.get(player);
            if (container == null || !container.isEnabled()) {
                this.onClose();
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // El boton del libro de recetas se reubica solo cuando se aprieta; lo dejamos siempre en su lugar
        if (this.recipeButton != null) {
            this.recipeButton.setPosition(this.leftPos + 104, this.topPos + 61);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // El dibujo del inventario normal usa imageHeight, asi que se lo damos como si fuera el normal
        this.imageHeight = VANILLA_HEIGHT;
        try {
            super.renderBg(graphics, partialTick, mouseX, mouseY);
        } finally {
            this.imageHeight = FULL_HEIGHT;
        }
        drawPanel(graphics);
    }

    private void drawPanel(GuiGraphics g) {
        int x = this.leftPos;
        int w = this.imageWidth;
        int top = this.topPos + PANEL_TOP;
        int bottom = this.topPos + FULL_HEIGHT;

        // fondo y bordes, con el mismo estilo que los inventarios de Minecraft
        g.fill(x + 2, top, x + w - 2, bottom - 2, BODY);
        g.fill(x, top, x + 1, bottom, BLACK);
        g.fill(x + 1, top, x + 2, bottom - 2, WHITE);
        g.fill(x + w - 1, top, x + w, bottom, BLACK);
        g.fill(x + w - 2, top, x + w - 1, bottom - 1, SHADOW);
        g.fill(x, bottom - 1, x + w, bottom, BLACK);
        g.fill(x + 1, bottom - 2, x + w - 1, bottom - 1, SHADOW);

        // los 27 huecos (3 filas de 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = x + StorageHelper.SLOT_X - 1 + col * 18;
                int sy = this.topPos + StorageHelper.SLOT_Y - 1 + row * 18;
                g.fill(sx, sy, sx + 18, sy + 18, SLOT_BODY);
                g.fill(sx, sy, sx + 17, sy + 1, SLOT_DARK);
                g.fill(sx, sy, sx + 1, sy + 17, SLOT_DARK);
                g.fill(sx + 1, sy + 17, sx + 18, sy + 18, WHITE);
                g.fill(sx + 17, sy + 1, sx + 18, sy + 18, WHITE);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(this.font, Component.translatable("container.mutantraisedead.doble_almacen"),
                StorageHelper.SLOT_X, 165, 4210752, false);
    }
}
