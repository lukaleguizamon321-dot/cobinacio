package com.mutantraisedead.crab;

import com.mutantraisedead.GuardHandler;
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
 * Poner el horno: clic izquierdo (golpe) al cangrejo de carbon mascota con un horno en la mano.
 * El golpe no le hace dano: el horno se queda encima del cangrejo (se dibuja solo la mitad de arriba).
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class CrabFurnaceEvents {
    private CrabFurnaceEvents() {
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (!(event.getTarget() instanceof GrottolVariantEntity crab) || !crab.canCarryFurnace()) {
            return;
        }
        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();
        if (!held.is(Items.FURNACE)) {
            return;
        }
        // con un horno en la mano nunca se golpea al cangrejo
        event.setCanceled(true);
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!crab.isOwnedBy(player)) {
            player.displayClientMessage(Component.translatable("message.mutantraisedead.crab_furnace_not_owner"), true);
            return;
        }
        if (crab.hasFurnace()) {
            player.displayClientMessage(Component.translatable("message.mutantraisedead.crab_furnace_already"), true);
            return;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        crab.installFurnace();
        level.playSound(null, crab.getX(), crab.getY(), crab.getZ(), SoundEvents.STONE_PLACE, SoundSource.NEUTRAL, 1.0F, 0.8F);
        level.playSound(null, crab.getX(), crab.getY(), crab.getZ(), SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.POOF, crab.getX(), crab.getY() + 1.0D, crab.getZ(), 10, 0.3D, 0.2D, 0.3D, 0.02D);
        player.displayClientMessage(Component.translatable("message.mutantraisedead.crab_furnace_placed"), true);
    }
}
