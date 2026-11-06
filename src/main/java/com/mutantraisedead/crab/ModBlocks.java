package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** Bloques nido de los cangrejos (el de hierro suelto en cuevas, los demas en las guaridas) y su generacion. */
public final class ModBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MutantRaiseDead.MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MutantRaiseDead.MODID);
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MutantRaiseDead.MODID);

    public static final RegistryObject<Block> NEST =
            BLOCKS.register("iron_crab_nest", () -> new GrottolNestBlock(BlockBehaviour.Properties.copy(Blocks.IRON_ORE), GrottolVariant.IRON));
    public static final RegistryObject<Block> DEEPSLATE_NEST =
            BLOCKS.register("deepslate_iron_crab_nest", () -> new GrottolNestBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_IRON_ORE), GrottolVariant.IRON));

    public static final RegistryObject<Item> NEST_ITEM =
            ITEMS.register("iron_crab_nest", () -> new BlockItem(NEST.get(), new Item.Properties()));
    public static final RegistryObject<Item> DEEPSLATE_NEST_ITEM =
            ITEMS.register("deepslate_iron_crab_nest", () -> new BlockItem(DEEPSLATE_NEST.get(), new Item.Properties()));

    /** Nidos de las guaridas (v1.9): uno por cada cangrejo menos el de hierro, que usa los de arriba. */
    public static final Map<GrottolVariant, RegistryObject<Block>> LAIR_NESTS = new EnumMap<>(GrottolVariant.class);
    public static final Map<GrottolVariant, RegistryObject<Item>> LAIR_NEST_ITEMS = new EnumMap<>(GrottolVariant.class);

    static {
        for (GrottolVariant variant : GrottolVariant.values()) {
            if (variant == GrottolVariant.IRON) {
                continue;
            }
            String name = nestName(variant);
            // Propiedades explicitas: copiar las de las menas de redstone/etc. arrastra funciones de luz que leen
            // propiedades que estos bloques no tienen.
            RegistryObject<Block> block = BLOCKS.register(name, () -> new GrottolNestBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 3.0F)
                            .sound(variant == GrottolVariant.AMETHYST ? SoundType.AMETHYST : SoundType.STONE),
                    variant));
            LAIR_NESTS.put(variant, block);
            LAIR_NEST_ITEMS.put(variant, ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties())));
        }
    }

    public static final RegistryObject<NestFeature> NEST_FEATURE =
            FEATURES.register("iron_crab_nest", () -> new NestFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<LairFeature> LAIR_FEATURE =
            FEATURES.register("crab_lair", () -> new LairFeature(NoneFeatureConfiguration.CODEC));

    private ModBlocks() {
    }

    /** Nombre del bloque nido de ese cangrejo, por ejemplo "coal_crab_nest". */
    public static String nestName(GrottolVariant variant) {
        return variant.name().toLowerCase(Locale.ROOT) + "_crab_nest";
    }

    /** Bloque nido de la guarida para ese cangrejo (el de hierro usa el de mena de hierro normal). */
    public static Block lairNest(GrottolVariant variant) {
        if (variant == GrottolVariant.IRON) {
            return NEST.get();
        }
        return LAIR_NESTS.get(variant).get();
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        FEATURES.register(bus);
    }
}
