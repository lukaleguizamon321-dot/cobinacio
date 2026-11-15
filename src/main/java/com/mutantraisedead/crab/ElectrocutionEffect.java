package com.mutantraisedead.crab;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** "Electrocucion" (rayo violeta): una descarga violeta cada 2 segundos, de un corazon de dano cada una. */
public class ElectrocutionEffect extends MobEffect {
    public ElectrocutionEffect() {
        super(MobEffectCategory.HARMFUL, 0xA447FF);
    }

    /** Pasa justo antes de cumplirse cada 2 segundos (40 ticks) de efecto. */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 1;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide || !entity.isAlive()) {
            return;
        }
        // dano magico: no lo frena la armadura, asi son siempre un corazon (2 de vida)
        entity.hurt(entity.damageSources().magic(), 2.0F);
        if (entity.level() instanceof ServerLevel level) {
            double y = entity.getY() + entity.getBbHeight() * 0.5D;
            level.sendParticles(BoltColor.VIOLET.dust(1.6F), entity.getX(), y, entity.getZ(), 16, 0.35D, 0.5D, 0.35D, 0.0D);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, entity.getX(), y, entity.getZ(), 10, 0.4D, 0.5D, 0.4D, 0.15D);
            level.playSound(null, entity.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.5F, 1.8F);
        }
    }
}
