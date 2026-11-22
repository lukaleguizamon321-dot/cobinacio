package com.mutantraisedead.goggles;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registro de las Gafas de amatista: el objeto, la receta de teñirlas y el canal de red. */
public final class ModGoggles {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MutantRaiseDead.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MutantRaiseDead.MODID);

    public static final RegistryObject<Item> GOGGLES = ITEMS.register("amethyst_goggles", AmethystGogglesItem::new);

    public static final RegistryObject<RecipeSerializer<GogglesDyeRecipe>> DYE_RECIPE =
            RECIPES.register("goggles_dye", () -> new SimpleCraftingRecipeSerializer<>(GogglesDyeRecipe::new));

    private ModGoggles() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        RECIPES.register(bus);
        GogglesNetwork.register();
    }
}
