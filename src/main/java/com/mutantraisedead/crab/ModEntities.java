package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

/** Registro de los 6 cangrejos y sus huevos de aparicion. */
public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MutantRaiseDead.MODID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MutantRaiseDead.MODID);

    public static final Map<GrottolVariant, RegistryObject<EntityType<GrottolVariantEntity>>> CRABS = new EnumMap<>(GrottolVariant.class);
    public static final Map<GrottolVariant, RegistryObject<Item>> EGGS = new EnumMap<>(GrottolVariant.class);

    public static final RegistryObject<EntityType<GiantIronCrabEntity>> GIANT_CRAB = TYPES.register("giant_iron_crab", () ->
            EntityType.Builder.<GiantIronCrabEntity>of(GiantIronCrabEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 1.6F)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build(MutantRaiseDead.MODID + ":giant_iron_crab"));
    public static final RegistryObject<Item> GIANT_CRAB_EGG = ITEMS.register("giant_iron_crab_spawn_egg", () ->
            new ForgeSpawnEggItem(GIANT_CRAB, 0x555558, 0xAA8670, new Item.Properties()));

    /** Jefe de la guarida: Cangrejo Titan de Carbon (3 bloques de ancho, 6 de alto). Sale del huevo del pedestal. */
    public static final RegistryObject<EntityType<CoalCrabBossEntity>> COAL_BOSS = TYPES.register("coal_crab_boss", () ->
            EntityType.Builder.<CoalCrabBossEntity>of(CoalCrabBossEntity::new, MobCategory.MONSTER)
                    .sized(3.0F, 6.0F)
                    .fireImmune()
                    .clientTrackingRange(16)
                    .build(MutantRaiseDead.MODID + ":coal_crab_boss"));
    public static final RegistryObject<Item> COAL_BOSS_EGG = ITEMS.register("coal_crab_boss_spawn_egg", () ->
            new ForgeSpawnEggItem(COAL_BOSS, 0x1B1B1F, 0xE0701A, new Item.Properties()));

    /** Jefe de la guarida de cobre: Cangrejo Titan de Cobre (3 de ancho, 6 de alto, igual que el de carbon). Sale del huevo del pedestal. */
    public static final RegistryObject<EntityType<CopperCrabBossEntity>> COPPER_BOSS = TYPES.register("copper_crab_boss", () ->
            EntityType.Builder.<CopperCrabBossEntity>of(CopperCrabBossEntity::new, MobCategory.MONSTER)
                    .sized(3.0F, 6.0F)
                    .fireImmune()
                    .clientTrackingRange(16)
                    .build(MutantRaiseDead.MODID + ":copper_crab_boss"));
    public static final RegistryObject<Item> COPPER_BOSS_EGG = ITEMS.register("copper_crab_boss_spawn_egg", () ->
            new ForgeSpawnEggItem(COPPER_BOSS, 0xC15A36, 0x3FB59B, new Item.Properties()));

    /**
     * Jefe de la guarida geoda: Cangrejo Titan de Amatista (3,6 de ancho y 6,4 de alto la caja que se golpea; el modelo,
     * con los cristales de la espalda y las patas abiertas, es mas grande). Sale del huevo del pedestal.
     */
    public static final RegistryObject<EntityType<AmethystCrabBossEntity>> AMETHYST_BOSS = TYPES.register("amethyst_crab_boss", () ->
            EntityType.Builder.<AmethystCrabBossEntity>of(AmethystCrabBossEntity::new, MobCategory.MONSTER)
                    .sized(3.6F, 6.4F)
                    .fireImmune()
                    .clientTrackingRange(16)
                    .build(MutantRaiseDead.MODID + ":amethyst_crab_boss"));
    public static final RegistryObject<Item> AMETHYST_BOSS_EGG = ITEMS.register("amethyst_crab_boss_spawn_egg", () ->
            new ForgeSpawnEggItem(AMETHYST_BOSS, 0x8A4FD8, 0x3A3A44, new Item.Properties()));

    /** Pincho de cristal de la linea de pinchos del jefe de amatista (dura 5 segundos). */
    public static final RegistryObject<EntityType<CrystalSpikeEntity>> CRYSTAL_SPIKE = TYPES.register("crystal_spike", () ->
            EntityType.Builder.<CrystalSpikeEntity>of(CrystalSpikeEntity::new, MobCategory.MISC)
                    .sized(1.4F, 2.6F)
                    .noSave()
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(16)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(MutantRaiseDead.MODID + ":crystal_spike"));

    /** Amatista que tira el jefe de amatista en el ataque del centro (variante 3). */
    public static final RegistryObject<EntityType<AmethystShardEntity>> AMETHYST_SHARD = TYPES.register("amethyst_shard", () ->
            EntityType.Builder.<AmethystShardEntity>of(AmethystShardEntity::new, MobCategory.MISC)
                    .sized(0.6F, 0.6F)
                    .noSave()
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .build(MutantRaiseDead.MODID + ":amethyst_shard"));

    /** Rayo de color del jefe de cobre (solo se ve; el dano lo hace el jefe). */
    public static final RegistryObject<EntityType<ColoredBoltEntity>> COLORED_BOLT = TYPES.register("colored_bolt", () ->
            EntityType.Builder.<ColoredBoltEntity>of(ColoredBoltEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .noSave()
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(16)
                    .updateInterval(Integer.MAX_VALUE)
                    .build(MutantRaiseDead.MODID + ":colored_bolt"));

    static {
        for (GrottolVariant variant : GrottolVariant.values()) {
            RegistryObject<EntityType<GrottolVariantEntity>> type = TYPES.register(variant.id, () ->
                    EntityType.Builder.<GrottolVariantEntity>of((t, level) -> new GrottolVariantEntity(t, level, variant), MobCategory.AMBIENT)
                            .sized(1.2F, 0.9F)
                            .fireImmune()
                            .clientTrackingRange(8)
                            .build(MutantRaiseDead.MODID + ":" + variant.id));
            CRABS.put(variant, type);
            EGGS.put(variant, ITEMS.register(variant.id + "_spawn_egg", () ->
                    new ForgeSpawnEggItem(type, variant.eggBase, variant.eggSpot, new Item.Properties())));
        }
    }

    private ModEntities() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        ITEMS.register(bus);
    }
}
