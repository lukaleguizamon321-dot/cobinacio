package com.mutantraisedead.crab;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Menu del horno del cangrejo de carbon: igual que el del horno pero SIN ranura de combustible
 * (el cangrejo pone el calor). Ranuras: 0 = lo que se cocina, 1 = resultado, 2..28 = mochila, 29..37 = barra rapida.
 */
public class CrabFurnaceMenu extends AbstractContainerMenu {
    private static final int INPUT = 0;
    private static final int OUTPUT = 1;
    private static final int INV_START = 2;
    private static final int INV_END = 29;
    private static final int HOTBAR_END = 38;

    private final Container container;
    private final ContainerData data;
    private final GrottolVariantEntity crab;
    private final Level level;

    /** Lado cliente: el servidor manda solo el numero del cangrejo; el resto llega por los paquetes de ranuras y datos. */
    public CrabFurnaceMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, findCrab(inventory.player.level(), buf.readVarInt()), new SimpleContainer(2), new SimpleContainerData(3));
    }

    /** Lado servidor: usa el inventario y los datos reales del cangrejo. */
    public CrabFurnaceMenu(int id, Inventory inventory, GrottolVariantEntity crab) {
        this(id, inventory, crab, crab.getFurnaceInventory(), crab.furnaceData);
    }

    private CrabFurnaceMenu(int id, Inventory inventory, GrottolVariantEntity crab, Container container, ContainerData data) {
        super(ModMenus.CRAB_FURNACE.get(), id);
        checkContainerSize(container, 2);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;
        this.crab = crab;
        this.level = inventory.player.level();

        this.addSlot(new Slot(container, INPUT, 56, 17));
        this.addSlot(new OutputSlot(container, OUTPUT, 116, 35));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
        this.addDataSlots(data);
    }

    private static GrottolVariantEntity findCrab(Level level, int entityId) {
        Entity entity = level.getEntity(entityId);
        return entity instanceof GrottolVariantEntity crab ? crab : null;
    }

    /** true mientras esta cocinando (la llama de la pantalla se prende). */
    public boolean isCooking() {
        return data.get(2) != 0;
    }

    /** Cuanto del arrow de progreso hay que dibujar (0 a 24). */
    public int getBurnProgress() {
        int progress = data.get(0);
        int total = data.get(1);
        return total != 0 && progress != 0 ? Mth.ceil(progress * 24.0F / total) : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return crab != null && crab.isAlive() && crab.hasFurnace() && player.distanceToSqr(crab) <= 64.0D;
    }

    private boolean hasRecipe(ItemStack stack) {
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), level).isPresent();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index == OUTPUT) {
            if (!this.moveItemStackTo(stack, INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else if (index != INPUT) {
            if (hasRecipe(stack)) {
                if (!this.moveItemStackTo(stack, INPUT, INPUT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < INV_END) {
                if (!this.moveItemStackTo(stack, INV_END, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, INV_START, INV_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, INV_START, HOTBAR_END, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    /** Resultado: no se puede poner nada a mano, y al sacarlo se cobra la experiencia que se fue juntando. */
    private class OutputSlot extends Slot {
        OutputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (player instanceof ServerPlayer serverPlayer && crab != null) {
                crab.releaseFurnaceXp(serverPlayer);
            }
            super.onTake(player, stack);
        }
    }
}
