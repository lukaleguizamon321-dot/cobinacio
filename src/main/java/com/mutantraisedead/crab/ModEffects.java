package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MutantRaiseDead.MODID);

    public static final RegistryObject<MobEffect> DIZZY = EFFECTS.register("dizzy", DizzyEffect::new);
    /** "Detectado": los monstruos cercanos te encuentran aunque haya paredes en el medio. */
    public static final RegistryObject<MobEffect> DETECTED = EFFECTS.register("detected", DetectedEffect::new);
    /** "Desafortunado": Fortuna al reves; las menas sueltan menos y la suerte baja. */
    public static final RegistryObject<MobEffect> UNLUCKY = EFFECTS.register("unlucky", UnluckyEffect::new);

    /** "Electrocucion" (rayo violeta): una descarga de un corazon cada 2 segundos. */
    public static final RegistryObject<MobEffect> ELECTROCUTION = EFFECTS.register("electrocution", ElectrocutionEffect::new);
    /** "Paralisis" (rayo amarillo): no te podes mover ni saltar. */
    public static final RegistryObject<MobEffect> PARALYSIS = EFFECTS.register("paralysis", ParalysisEffect::new);
    /** "Mana anulado" (rayo azul): el mana queda en 0 y no se pueden lanzar hechizos. */
    public static final RegistryObject<MobEffect> MANA_LOCK = EFFECTS.register("mana_lock", ManaLockEffect::new);

    private ModEffects() {
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
