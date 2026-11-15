package com.mutantraisedead.crab;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** "Mana anulado" (rayo azul): mientras dura, el mana del jugador se queda en 0 y no puede lanzar hechizos de Iron's. */
public class ManaLockEffect extends MobEffect {
    public ManaLockEffect() {
        super(MobEffectCategory.HARMFUL, 0x3373FF);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof ServerPlayer player) {
            MagicData.getPlayerMagicData(player).setMana(0);
        }
    }
}
