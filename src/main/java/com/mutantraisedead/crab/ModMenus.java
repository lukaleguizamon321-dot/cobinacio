package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Menus (ventanas) del mod. Por ahora solo la del horno del cangrejo de carbon. */
public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MutantRaiseDead.MODID);

    public static final RegistryObject<MenuType<CrabFurnaceMenu>> CRAB_FURNACE =
            MENUS.register("crab_furnace", () -> IForgeMenuType.<CrabFurnaceMenu>create((id, inventory, buf) -> new CrabFurnaceMenu(id, inventory, buf)));

    private ModMenus() {
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
