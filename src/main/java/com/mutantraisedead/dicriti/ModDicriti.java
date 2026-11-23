package com.mutantraisedead.dicriti;

import com.mutantraisedead.MutantRaiseDead;
import com.mutantraisedead.TipItem;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Dicriti (v1.22): material nuevo. La mena aparece en el mundo normal, muy rara y solo cerca de la bedrock; se funde en el
 * alto horno y da el lingote. El lingote, combinado con un pico en la mesa de crafteo, deja al pico lanzable.
 */
public final class ModDicriti {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MutantRaiseDead.MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MutantRaiseDead.MODID);
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MutantRaiseDead.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MutantRaiseDead.MODID);

    /** Mena de dicriti. Hace falta un pico de hierro o mejor; con Toque de seda se la lleva entera. */
    public static final RegistryObject<Block> DICRITI_ORE = BLOCKS.register("dicriti_ore", () -> new DropExperienceBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(4.5F, 3.0F)
                    .sound(SoundType.DEEPSLATE),
            UniformInt.of(3, 7)));

    public static final RegistryObject<Item> DICRITI_ORE_ITEM =
            ITEMS.register("dicriti_ore", () -> new BlockItem(DICRITI_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> RAW_DICRITI = ITEMS.register("raw_dicriti", () ->
            new TipItem(new Item.Properties(), "item.mutantraisedead.raw_dicriti.tip"));

    public static final RegistryObject<Item> DICRITI_INGOT = ITEMS.register("dicriti_ingot", () ->
            new TipItem(new Item.Properties().rarity(Rarity.UNCOMMON), "item.mutantraisedead.dicriti_ingot.tip"));

    /** El pico mientras vuela (no se guarda en la lista de invocables: solo lo crea el clic derecho). */
    public static final RegistryObject<EntityType<ThrownDicritiPickEntity>> THROWN_PICK = ENTITIES.register("thrown_dicriti_pick", () ->
            EntityType.Builder.<ThrownDicritiPickEntity>of(ThrownDicritiPickEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build(MutantRaiseDead.MODID + ":thrown_dicriti_pick"));

    /** Receta: un pico + un lingote de dicriti (sin forma fija) = el mismo pico, con sus encantamientos y su desgaste, lanzable. */
    public static final RegistryObject<RecipeSerializer<DicritiPickRecipe>> PICK_RECIPE =
            RECIPES.register("dicriti_pick", () -> new SimpleCraftingRecipeSerializer<>(DicritiPickRecipe::new));

    private ModDicriti() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        RECIPES.register(bus);
    }
}
