package com.mutantraisedead.crab;

import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;

/**
 * Los 6 colores de rayo del Cangrejo Titan de Cobre. Cada uno hace algo distinto (ver CopperCrabBossEntity):
 * blanco = normal, violeta = Electrocucion, amarillo = Paralisis, rojo = mas dano, azul = Mana anulado,
 * verde = no te lastima: cura al jefe (y a quien este pegado a el).
 */
public enum BoltColor {
    WHITE(0.95F, 0.95F, 1.00F),
    VIOLET(0.66F, 0.28F, 1.00F),
    YELLOW(1.00F, 0.88F, 0.12F),
    RED(1.00F, 0.16F, 0.10F),
    BLUE(0.20F, 0.45F, 1.00F),
    GREEN(0.20F, 1.00F, 0.38F);

    public final float r;
    public final float g;
    public final float b;

    BoltColor(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static BoltColor byId(int id) {
        BoltColor[] all = values();
        return all[Math.floorMod(id, all.length)];
    }

    /** Particula de polvo de este color (la que sale de las pinzas y marca donde va a caer el rayo). */
    public DustParticleOptions dust(float scale) {
        return new DustParticleOptions(new Vector3f(r, g, b), scale);
    }
}
