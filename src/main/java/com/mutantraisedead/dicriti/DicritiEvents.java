package com.mutantraisedead.dicriti;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/** Clic derecho con un pico de dicriti = lo lanza. Tambien le agrega las lineas de ayuda al texto del pico. */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class DicritiEvents {
    private DicritiEvents() {
    }

    @SubscribeEvent
    public static void onUse(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!DicritiPick.hasAbility(stack)) {
            return;
        }
        Player player = event.getEntity();
        // Solo en el servidor: el cliente no hace nada y el servidor le devuelve el movimiento del brazo.
        if (player.isSpectator() || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        InteractionHand hand = event.getHand();
        int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
        ItemStack thrown = stack.copy();
        player.setItemInHand(hand, ItemStack.EMPTY);
        ThrownDicritiPickEntity pick = new ThrownDicritiPickEntity(ModDicriti.THROWN_PICK.get(), level);
        pick.launch(player, thrown, slot);
        level.addFreshEntity(pick);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.1F);
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!DicritiPick.hasAbility(event.getItemStack())) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        tooltip.add(Component.translatable("tooltip.mutantraisedead.dicriti_pick.1").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.mutantraisedead.dicriti_pick.2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.mutantraisedead.dicriti_pick.3").withStyle(ChatFormatting.GRAY));
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        DicritiMining.clear();
    }
}
