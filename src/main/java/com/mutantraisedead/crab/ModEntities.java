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
