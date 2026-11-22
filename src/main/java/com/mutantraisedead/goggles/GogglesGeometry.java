package com.mutantraisedead.goggles;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** De donde sale el laser de las gafas y hasta donde llega. El servidor (dano) y el cliente (dibujo) usan lo mismo. */
public final class GogglesGeometry {
    /** Alcance maximo del laser, en bloques. */
    public static final double RANGE = 40.0D;

    private GogglesGeometry() {
    }

    /** El rayo sale un poco adelante y debajo de los ojos, para no taparte la vista, y apunta a donde mira la mira. */
    public static Vec3 origin(Vec3 eye, Vec3 look) {
        Vec3 right = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-4D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize();
        Vec3 up = right.cross(look).normalize();
        return eye.add(look.scale(0.6D)).add(up.scale(-0.25D));
    }

    /** El punto donde termina el rayo: el primer bloque que encuentra la mira o, si no hay, el alcance maximo. */
    public static Vec3 target(Level level, Entity owner, Vec3 eye, Vec3 look) {
        Vec3 end = eye.add(look.scale(RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
    }
}
