package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Paralisis: el salto se anula (pasa en el cliente y en el servidor). */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class EffectEvents {
    private EffectEvents() {
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(ModEffects.PARALYSIS.get())) {
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, Math.min(motion.y, 0.0D), motion.z);
        }
    }
}
