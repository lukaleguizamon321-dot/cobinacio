package com.mutantraisedead.crab;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * "Desafortunado": como Fortuna pero al reves. Baja la suerte (pesca, botin de cofres)
 * y UnluckyLootModifier hace que las menas suelten menos.
 */
public class UnluckyEffect extends MobEffect {
    public UnluckyEffect() {
        super(MobEffectCategory.HARMFUL, 0x3A7A3A);
        addAttributeModifier(Attributes.LUCK, "7A3C8D12-5B6E-4F90-A1C2-3D4E5F607182", -1.0D, AttributeModifier.Operation.ADDITION);
    }
}
