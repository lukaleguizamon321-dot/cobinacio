package com.mutantraisedead.goggles;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Unas Gafas de amatista + un tinte = unas gafas con el laser de ese color (la mesa de crafteo comun). */
public class GogglesDyeRecipe extends CustomRecipe {
    public GogglesDyeRecipe(ResourceLocation id, CraftingBookCategory category) {
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

    /** Exactamente unas gafas y un tinte, nada mas. */
    private static ItemStack build(CraftingContainer container) {
        ItemStack goggles = ItemStack.EMPTY;
        DyeColor dye = null;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModGoggles.GOGGLES.get())) {
                if (!goggles.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                goggles = stack;
            } else if (stack.getItem() instanceof DyeItem dyeItem) {
                if (dye != null) {
                    return ItemStack.EMPTY;
                }
                dye = dyeItem.getDyeColor();
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (goggles.isEmpty() || dye == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = goggles.copy();
        result.setCount(1);
        GogglesColors.setDye(result, dye);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModGoggles.DYE_RECIPE.get();
    }
}
