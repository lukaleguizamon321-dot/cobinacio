package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Bloques nido del cangrejo gigante y su generacion en el mundo. */
public final class ModBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MutantRaiseDead.MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MutantRaiseDead.MODID);
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MutantRaiseDead.MODID);

    public static final RegistryObject<Block> NEST =
            BLOCKS.register("iron_crab_nest", () -> new GrottolNestBlock(BlockBehaviour.Properties.copy(Blocks.IRON_ORE)));
    public static final RegistryObject<Block> DEEPSLATE_NEST =
            BLOCKS.register("deepslate_iron_crab_nest", () -> new GrottolNestBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_IRON_ORE)));

    public static final RegistryObject<Item> NEST_ITEM =
            ITEMS.register("iron_crab_nest", () -> new BlockItem(NEST.get(), new Item.Properties()));
    public static final RegistryObject<Item> DEEPSLATE_NEST_ITEM =
            ITEMS.register("deepslate_iron_crab_nest", () -> new BlockItem(DEEPSLATE_NEST.get(), new Item.Properties()));

    public static final RegistryObject<NestFeature> NEST_FEATURE =
            FEATURES.register("iron_crab_nest", () -> new NestFeature(NoneFeatureConfiguration.CODEC));

    private ModBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        FEATURES.register(bus);
    }
}
