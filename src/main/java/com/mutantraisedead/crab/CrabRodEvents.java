package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Pararrayos de la mascota de cobre, con clic izquierdo (golpe) del dueno:
 *  - con un pararrayos en la mano y sin uno puesto: se lo pone arriba del caparazon (como el horno del cangrejo de carbon);
 *  - con el pararrayos ya puesto: cada golpe cambia de rayo (verde, violeta, amarillo), como sentar / seguir a un perro.
 * Estos golpes nunca lastiman al cangrejo.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class CrabRodEvents {
    private CrabRodEvents() {
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (!(event.getTarget() instanceof GrottolVariantEntity crab) || !crab.canCarryRod()) {
            return;
        }
        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();
        boolean holdsRod = held.is(Items.LIGHTNING_ROD);
        if (!crab.hasRod() && !holdsRod) {
            return; // sin pararrayos puesto ni en la mano no pasa nada especial
        }
        event.setCanceled(true);
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!crab.isOwnedBy(player)) {
            player.displayClientMessage(Component.translatable("message.mutantraisedead.crab_furnace_not_owner"), true);
            return;
        }
        if (!crab.hasRod()) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            crab.installRod();
            level.playSound(null, crab.getX(), crab.getY(), crab.getZ(), SoundEvents.COPPER_PLACE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            level.playSound(null, crab.getX(), crab.getY(), crab.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.NEUTRAL, 0.4F, 2.0F);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, crab.getX(), crab.getY() + 1.2D, crab.getZ(), 14, 0.3D, 0.3D, 0.3D, 0.1D);
            player.displayClientMessage(Component.translatable("message.mutantraisedead.crab_rod_placed"), true);
            return;
        }
        BoltColor color = crab.cycleRodMode();
        String name = switch (color) {
            case GREEN -> "message.mutantraisedead.crab_rod_green";
            case VIOLET -> "message.mutantraisedead.crab_rod_violet";
            default -> "message.mutantraisedead.crab_rod_yellow";
        };
        level.playSound(null, crab.getX(), crab.getY(), crab.getZ(), SoundEvents.LEVER_CLICK, SoundSource.NEUTRAL, 0.6F, 1.6F);
        level.sendParticles(color.dust(1.3F), crab.getX(), crab.getY() + 1.3D, crab.getZ(), 12, 0.25D, 0.25D, 0.25D, 0.0D);
        player.displayClientMessage(Component.translatable("message.mutantraisedead.crab_rod_mode", Component.translatable(name)), true);
    }
}
