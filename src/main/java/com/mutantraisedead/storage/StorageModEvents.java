package com.mutantraisedead.storage;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Ademas de nuestra pestana, el Doble Almacen aparece en la pestana del mod Artifacts (si esta instalado). */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class StorageModEvents {
    private static final ResourceLocation ARTIFACTS_TAB = new ResourceLocation("artifacts", "main");

    private StorageModEvents() {
    }

    @SubscribeEvent
    public static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(ARTIFACTS_TAB)) {
            event.accept(ModStorage.DOUBLE_STORAGE.get());
        }
    }
}
