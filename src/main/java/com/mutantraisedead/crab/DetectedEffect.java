package com.mutantraisedead.crab;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** "Detectado": el efecto en si no hace nada; los monstruos se enteran por CrabGameEvents. */
public class DetectedEffect extends MobEffect {
    public DetectedEffect() {
        super(MobEffectCategory.HARMFUL, 0xE02008);
    }
}
