package com.mutantraisedead;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Logros de la pestana "Cobinatio". Los logros estan definidos en
 * data/mutantraisedead/advancements/*.json con un criterio "imposible":
 * solo se completan cuando el mod llama a grant().
 */
public final class Achievements {
    private Achievements() {
    }

    /** Da el logro (por ejemplo "karma") si la entidad es un jugador y todavia no lo tiene. */
    public static void grant(Entity entity, String id) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        Advancement advancement = server.getAdvancements().getAdvancement(ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, id));
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            return;
        }
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
