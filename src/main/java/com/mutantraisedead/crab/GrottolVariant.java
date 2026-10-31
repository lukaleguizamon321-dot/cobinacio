package com.mutantraisedead.crab;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

/**
 * Los 6 cangrejos de materiales. Cada uno tiene su propio mob, textura y pasiva.
 */
public enum GrottolVariant {
    IRON("grottol_iron", Shape.METAL, 64, () -> Items.RAW_IRON, 2, 4, 0x555558, 0xC8B4A4),
    GOLD("grottol_gold", Shape.METAL, 32, () -> Items.RAW_GOLD, 2, 4, 0x555558, 0xF5C93C),
    COPPER("grottol_copper", Shape.METAL, 48, () -> Items.RAW_COPPER, 2, 5, 0x555558, 0xC86E46),
    COAL("grottol_coal", Shape.LUMP, 100, () -> Items.COAL, 3, 6, 0x555558, 0x1E1E20),
    LAPIS("grottol_lapis", Shape.CRYSTAL, 16, () -> Items.LAPIS_LAZULI, 4, 8, 0x555558, 0x2F55C8),
    AMETHYST("grottol_amethyst", Shape.CRYSTAL, 40, () -> Items.AMETHYST_SHARD, 2, 4, 0x555558, 0x9664D2);

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

    GrottolVariant(String id, Shape shape, int maxSpawnY, Supplier<Item> drop, int dropMin, int dropMax, int eggBase, int eggSpot) {
        this.id = id;
        this.shape = shape;
        this.maxSpawnY = maxSpawnY;
        this.drop = drop;
        this.dropMin = dropMin;
        this.dropMax = dropMax;
        this.eggBase = eggBase;
        this.eggSpot = eggSpot;
    }
}
