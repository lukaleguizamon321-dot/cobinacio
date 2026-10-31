package com.mutantraisedead.crab;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** "Aturdido": invierte los controles de movimiento (el cambio se hace en el cliente, ver ClientEvents). */
public class DizzyEffect extends MobEffect {
    public DizzyEffect() {
        super(MobEffectCategory.HARMFUL, 0x8A6A4A);
    }
}
