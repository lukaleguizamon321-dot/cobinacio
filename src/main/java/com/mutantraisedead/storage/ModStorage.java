package com.mutantraisedead.storage;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registro del artefacto Doble Almacen. */
public final class ModStorage {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MutantRaiseDead.MODID);

    public static final RegistryObject<Item> DOUBLE_STORAGE = ITEMS.register("doble_almacen", DoubleStorageItem::new);

    private ModStorage() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
