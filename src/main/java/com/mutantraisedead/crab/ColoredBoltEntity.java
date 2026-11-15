package com.mutantraisedead.crab;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Rayo de color del jefe de cobre: solo se ve (el dano y los efectos los aplica el jefe, ver CopperCrabBossEntity).
 * Dura medio segundo y despues desaparece. Se dibuja en ColoredBoltRenderer.
 */
public class ColoredBoltEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(ColoredBoltEntity.class, EntityDataSerializers.INT);

    /** Cuantos ticks se ve el rayo (10 = medio segundo). */
    public static final int LIFETIME = 10;

    /** Forma del rayo (cada uno sale distinto). */
    public final long seed;

    public ColoredBoltEntity(EntityType<? extends ColoredBoltEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
        this.noPhysics = true;
        this.seed = this.random.nextLong();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_COLOR, 0);
    }

    public void setColor(BoltColor color) {
        this.entityData.set(DATA_COLOR, color.ordinal());
    }

    public BoltColor getColor() {
        return BoltColor.byId(this.entityData.get(DATA_COLOR));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount >= LIFETIME) {
            discard();
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 160.0D * 160.0D;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
