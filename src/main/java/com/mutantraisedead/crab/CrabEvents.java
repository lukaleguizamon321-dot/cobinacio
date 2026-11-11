package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Eventos del bus del mod: atributos y reglas de aparicion de los cangrejos. */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CrabEvents {
    private CrabEvents() {
    }

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        for (GrottolVariant variant : GrottolVariant.values()) {
            event.put(ModEntities.CRABS.get(variant).get(), GrottolVariantEntity.createAttributes(variant).build());
        }
    }

    @SubscribeEvent
    public static void onGiantAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.GIANT_CRAB.get(), GiantIronCrabEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onBossAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.COAL_BOSS.get(), CoalCrabBossEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onSpawnPlacements(SpawnPlacementRegisterEvent event) {
        for (GrottolVariant variant : GrottolVariant.values()) {
            event.register(ModEntities.CRABS.get(variant).get(),
                    SpawnPlacements.Type.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type, level, reason, pos, random) -> GrottolVariantEntity.checkSpawn(variant, level, pos),
                    SpawnPlacementRegisterEvent.Operation.OR);
        }
    }
}
