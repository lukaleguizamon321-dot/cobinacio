package com.mutantraisedead.crab;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * "Paralisis" (rayo amarillo): no te podes mover ni saltar. La velocidad baja a cero con un modificador;
 * el salto y los controles del jugador se frenan en EffectEvents y ClientEvents.
 */
public class ParalysisEffect extends MobEffect {
    public ParalysisEffect() {
        super(MobEffectCategory.HARMFUL, 0xF5D31A);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "5d1f6b2a-91c4-4f3e-8a77-c3b2e0a41d90", -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
        }
        entity.setSprinting(false);
        if (entity.tickCount % 8 == 0 && entity.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    3, 0.3D, 0.5D, 0.3D, 0.1D);
        }
    }
}
