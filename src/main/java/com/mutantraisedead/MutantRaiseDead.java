package com.mutantraisedead;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Mod "puente" entre Iron's Spells 'n Spellbooks, Mutant Monsters y Mutant More.
 * No agrega items ni mobs nuevos: solo combina lo que ya existe.
 */
@Mod(MutantRaiseDead.MODID)
public class MutantRaiseDead {
    public static final String MODID = "mutantraisedead";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MutantRaiseDead() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(IronsBridge::init);
    }
}
