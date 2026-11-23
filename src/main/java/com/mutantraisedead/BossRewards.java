package com.mutantraisedead;

import com.mutantraisedead.crab.AmethystCrabBossEntity;
import com.mutantraisedead.crab.CopperCrabBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import com.mutantraisedead.goggles.ModGoggles;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Logros de los jefes y de los objetos que sueltan:
 * matar al Titan de Cobre = "Ni el rayo pudo contra mi", al de Amatista = "Su brillo no pudo contra mi",
 * craftear la Bobina = "100 voltios", conseguir las Gafas de amatista = "Ciclope".
 * (El logro secreto "Me proyecto" lo da el propio laser de las gafas: ver GogglesLaser.)
 * Los corazones y los discos salen de las tablas de botin de cada jefe.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class BossRewards {
    /** Los jugadores que estan cerca cuando muere el jefe tambien se llevan el logro (no solo el que dio el ultimo golpe). */
    private static final double SHARE_RANGE = 48.0D;

    private BossRewards() {
    }

    @SubscribeEvent
    public static void onBossDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level)) {
            return;
        }
        String achievement;
        if (dead instanceof CopperCrabBossEntity) {
            achievement = "ni_el_rayo";
        } else if (dead instanceof AmethystCrabBossEntity) {
            achievement = "su_brillo";
        } else {
            return;
        }
        Achievements.grant(event.getSource().getEntity(), achievement);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(dead) <= SHARE_RANGE * SHARE_RANGE) {
                Achievements.grant(player, achievement);
            }
        }
    }

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getCrafting().is(ModItems.BOBINA.get())) {
            Achievements.grant(event.getEntity(), "cien_voltios");
        }
        if (event.getCrafting().is(ModGoggles.GOGGLES.get())) {
            Achievements.grant(event.getEntity(), "ciclope");
        }
    }

    @SubscribeEvent
    public static void onPickup(PlayerEvent.ItemPickupEvent event) {
        if (event.getStack().is(ModGoggles.GOGGLES.get())) {
            Achievements.grant(event.getEntity(), "ciclope");
        }
    }
}
