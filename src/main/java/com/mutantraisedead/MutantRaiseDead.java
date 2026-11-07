package com.mutantraisedead;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Agrega los hechizos "Revive Mutantes (X)", "(Y)" y "(Z)": Revivir Muertos de
 * Iron's Spells 'n Spellbooks, pero con probabilidad de invocar mutantes de Mutant Monsters.
 */
@Mod(MutantRaiseDead.MODID)
public class MutantRaiseDead {
    public static final String MODID = "mutantraisedead";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MutantRaiseDead() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModRegistry.register(modBus);
        com.mutantraisedead.crab.ModEntities.register(modBus);
        com.mutantraisedead.crab.ModBlocks.register(modBus);
        com.mutantraisedead.crab.ModEffects.register(modBus);
        com.mutantraisedead.crab.ModLoot.register(modBus);
        com.mutantraisedead.storage.ModStorage.register(modBus);
        com.mutantraisedead.storage.ModNetwork.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
