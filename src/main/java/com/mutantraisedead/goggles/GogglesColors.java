package com.mutantraisedead.goggles;

import com.mutantraisedead.crab.BoltColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/**
 * Color del laser de las Gafas de amatista. Sin tinte es violeta (el del jefe); con un tinte, el color de ese tinte.
 * El tinte elegido se guarda en el propio objeto.
 */
public final class GogglesColors {
    private static final String TAG = "LaserDye";

    private GogglesColors() {
    }

    /** El tinte con el que se pintaron las gafas, o null si siguen violetas. */
    public static DyeColor dyeOf(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG, Tag.TAG_ANY_NUMERIC)) {
            return null;
        }
        return DyeColor.byId(tag.getInt(TAG));
    }

    public static void setDye(ItemStack stack, DyeColor dye) {
        stack.getOrCreateTag().putInt(TAG, dye.getId());
    }

    /** Color del rayo como 0xRRGGBB (se manda tal cual al cliente). */
    public static int rgbOf(ItemStack stack) {
        DyeColor dye = dyeOf(stack);
        if (dye == null) {
            return pack(BoltColor.VIOLET.r, BoltColor.VIOLET.g, BoltColor.VIOLET.b);
        }
        float[] c = dye.getTextureDiffuseColors();
        float max = Math.max(c[0], Math.max(c[1], c[2]));
        if (max < 0.25F) {
            // el negro no se veria nunca (el rayo brilla sumando luz): se pone un gris azulado oscuro
            return pack(0.45F, 0.45F, 0.55F);
        }
        // se sube el brillo manteniendo el tono, para que el rojo se vea rojo fuerte y no marron
        float scale = Math.min(1.0F / max, 1.6F);
        return pack(c[0] * scale, c[1] * scale, c[2] * scale);
    }

    private static int pack(float r, float g, float b) {
        int ri = Math.min(255, Math.round(r * 255.0F));
        int gi = Math.min(255, Math.round(g * 255.0F));
        int bi = Math.min(255, Math.round(b * 255.0F));
        return (ri << 16) | (gi << 8) | bi;
    }
}
