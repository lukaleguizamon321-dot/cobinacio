package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Sonidos propios del mod. El archivo esta en assets/mutantraisedead/sounds/ y se declara en sounds.json. */
public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MutantRaiseDead.MODID);

    /** Musica del Cangrejo Titan de Cobre ("Colossus Beneath the Crags"): suena mientras el jefe esta vivo. */
    public static final RegistryObject<SoundEvent> COPPER_BOSS_MUSIC = SOUNDS.register("copper_boss_music", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "copper_boss_music")));

    private ModSounds() {
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
