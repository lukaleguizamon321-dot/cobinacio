package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.client.player.Input;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Con el efecto Aturdido, adelante/atras e izquierda/derecha quedan invertidos. */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!event.getEntity().hasEffect(ModEffects.DIZZY.get())) {
            return;
        }
        Input input = event.getInput();
        input.forwardImpulse = -input.forwardImpulse;
        input.leftImpulse = -input.leftImpulse;
        boolean tmp = input.up;
        input.up = input.down;
        input.down = tmp;
        tmp = input.left;
        input.left = input.right;
        input.right = tmp;
    }
}
