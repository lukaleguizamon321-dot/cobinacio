package com.mutantraisedead.dicriti;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Un pico + un lingote de dicriti en la mesa de crafteo = el mismo pico, con la habilidad de lanzarlo. */
public class DicritiPickRecipe extends CustomRecipe {
    public DicritiPickRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return !build(container).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return build(container);
    }

    /** Exactamente un pico (que todavia no tenga la habilidad) y un lingote de dicriti, nada mas. */
    private static ItemStack build(CraftingContainer container) {
        ItemStack pick = ItemStack.EMPTY;
        boolean ingot = false;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModDicriti.DICRITI_INGOT.get())) {
                if (ingot) {
                    return ItemStack.EMPTY;
                }
                ingot = true;
            } else if (DicritiPick.isPickaxe(stack) && !DicritiPick.hasAbility(stack)) {
                if (!pick.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                pick = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (pick.isEmpty() || !ingot) {
            return ItemStack.EMPTY;
        }
        return DicritiPick.withAbility(pick);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModDicriti.PICK_RECIPE.get();
    }
}
