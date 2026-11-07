package com.mutantraisedead.crab;

import com.mojang.serialization.Codec;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registro de los modificadores de botin (el de Desafortunado). */
public final class ModLoot {
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MutantRaiseDead.MODID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> UNLUCKY_ORES =
            MODIFIERS.register("unlucky_ores", UnluckyLootModifier.CODEC);
    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_ITEM =
            MODIFIERS.register("add_item", AddItemLootModifier.CODEC);

    private ModLoot() {
    }

    public static void register(IEventBus bus) {
        MODIFIERS.register(bus);
    }
}
