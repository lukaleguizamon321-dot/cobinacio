package com.mutantraisedead.crab;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

/**
 * Los cangrejos de materiales. Cada uno tiene su propio mob, textura y pasiva.
 */
public enum GrottolVariant {
    IRON("grottol_iron", Shape.METAL, 64, () -> Items.RAW_IRON, 2, 4, 0x555558, 0xC8B4A4, true),
    GOLD("grottol_gold", Shape.METAL, 32, () -> Items.RAW_GOLD, 2, 4, 0x555558, 0xF5C93C, true),
    COPPER("grottol_copper", Shape.METAL, 48, () -> Items.RAW_COPPER, 2, 5, 0x555558, 0xC86E46, true),
    COAL("grottol_coal", Shape.LUMP, 100, () -> Items.COAL, 3, 6, 0x555558, 0x1E1E20, true),
    LAPIS("grottol_lapis", Shape.CRYSTAL, 16, () -> Items.LAPIS_LAZULI, 4, 8, 0x555558, 0x2F55C8, true),
    AMETHYST("grottol_amethyst", Shape.CRYSTAL, 40, () -> Items.AMETHYST_SHARD, 2, 4, 0x555558, 0x9664D2, true),
    /** Los tres nuevos (v1.7) no se entierran: se quedan a la vista. */
    REDSTONE("grottol_redstone", Shape.CRYSTAL, 16, () -> Items.REDSTONE, 4, 8, 0x555558, 0xE02008, false),
    DIAMOND("grottol_diamond", Shape.CRYSTAL, 16, () -> Items.DIAMOND, 1, 2, 0x555558, 0x65F5E3, false),
    EMERALD("grottol_emerald", Shape.CRYSTAL, 256, () -> Items.EMERALD, 1, 3, 0x555558, 0x17C544, false);

    /** Forma del modelo: metales lisos, cristales puntiagudos, carbon en trozos irregulares. */
    public enum Shape {
        METAL,
        CRYSTAL,
        LUMP
    }

    public final String id;
    public final Shape shape;
    public final int maxSpawnY;
    public final Supplier<Item> drop;
    public final int dropMin;
    public final int dropMax;
    public final int eggBase;
    public final int eggSpot;
    /** Si es false, el cangrejo nunca se entierra ni huye cavando. */
    public final boolean burrows;

    GrottolVariant(String id, Shape shape, int maxSpawnY, Supplier<Item> drop, int dropMin, int dropMax, int eggBase, int eggSpot, boolean burrows) {
        this.id = id;
        this.shape = shape;
        this.maxSpawnY = maxSpawnY;
        this.drop = drop;
        this.dropMin = dropMin;
        this.dropMax = dropMax;
        this.eggBase = eggBase;
        this.eggSpot = eggSpot;
        this.burrows = burrows;
    }
}
