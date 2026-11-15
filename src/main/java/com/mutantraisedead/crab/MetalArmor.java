package com.mutantraisedead.crab;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

/**
 * Armadura que conduce la electricidad: hierro, oro, cota de malla y netherita.
 * Cuantas mas piezas lleves (0 a 4), mas dura el efecto de los rayos del jefe de cobre.
 * La lista esta en data/mutantraisedead/tags/items/conductive_armor.json (se puede agregar armaduras de otros mods).
 */
public final class MetalArmor {
    public static final TagKey<Item> CONDUCTIVE =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "conductive_armor"));

    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private MetalArmor() {
    }

    /** Cuantas piezas metalicas lleva puestas (0 a 4). */
    public static int pieces(LivingEntity entity) {
        int count = 0;
        for (EquipmentSlot slot : SLOTS) {
            if (entity.getItemBySlot(slot).is(CONDUCTIVE)) {
                count++;
            }
        }
        return count;
    }

    /** Va de base (sin piezas) a max (las 4 piezas), parejo. */
    public static double scale(double base, double max, int pieces) {
        return base + (max - base) * pieces / 4.0D;
    }
}
