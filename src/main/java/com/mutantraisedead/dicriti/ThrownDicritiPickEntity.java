package com.mutantraisedead.dicriti;

import com.mutantraisedead.Config;
import com.mutantraisedead.GuardHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

/**
 * El pico de dicriti mientras vuela. Sale hacia donde mira el jugador en linea recta, hasta dicritiPickRange bloques.
 *  - Si choca con un bloque: lo pica (ver DicritiMining) y vuelve.
 *  - Si choca con una criatura: la agarra y la arrastra hacia el jugador mientras vuelve (las muy grandes y los jefes no se mueven).
 *  - Si no choca con nada: llega al final del recorrido y vuelve.
 * Al volver, el pico entra solo al inventario (a la ranura de donde salio). El pico va guardado adentro de esta entidad, con
 * todo su desgaste y sus encantamientos; si algo sale mal (el jugador murio, se desconecto, la entidad se borra) se suelta
 * como un objeto comun en el suelo, para que nunca se pierda.
 */
public class ThrownDicritiPickEntity extends Projectile {
    private static final EntityDataAccessor<ItemStack> DATA_STACK =
            SynchedEntityData.defineId(ThrownDicritiPickEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DATA_RETURNING =
            SynchedEntityData.defineId(ThrownDicritiPickEntity.class, EntityDataSerializers.BOOLEAN);

    /** Bloques por tick (20 ticks = 1 segundo) de ida, de vuelta y de vuelta arrastrando a una criatura. */
    private static final double OUT_SPEED = 1.8D;
    private static final double BACK_SPEED = 1.5D;
    private static final double DRAG_SPEED = 1.1D;
    /** A esta distancia del jugador el pico entra al inventario. */
    private static final double CATCH_DISTANCE = 1.5D;
    /** Si despues de este tiempo no llego (por ejemplo, el jugador se alejo muy rapido), entra igual. */
    private static final int MAX_RETURN_TICKS = 100;
    private static final int MAX_LIFE_TICKS = 400;

    /** Ranura del inventario de donde salio (0 a 8 = barra, 40 = mano izquierda). */
    private int slot = -1;
    private double travelled;
    private int returnTicks;
    /** Criatura que viene arrastrando (-1 = ninguna). */
    private int pulledId = -1;
    /** true cuando el pico ya volvio al inventario o ya se soltó en el suelo. */
    private boolean finished;

    public ThrownDicritiPickEntity(EntityType<? extends ThrownDicritiPickEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /** Lanza el pico (la copia que se saco de la mano) hacia donde mira el jugador. */
    public void launch(LivingEntity owner, ItemStack stack, int slot) {
        setOwner(owner);
        this.entityData.set(DATA_STACK, stack);
        this.slot = slot;
        Vec3 look = owner.getLookAngle();
        Vec3 eye = owner.getEyePosition();
        setPos(eye.x + look.x * 0.7D, eye.y - 0.25D + look.y * 0.7D, eye.z + look.z * 0.7D);
        setDeltaMovement(look.scale(OUT_SPEED));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_STACK, ItemStack.EMPTY);
        this.entityData.define(DATA_RETURNING, Boolean.FALSE);
    }

    public ItemStack getStack() {
        return this.entityData.get(DATA_STACK);
    }

    public boolean isReturning() {
        return this.entityData.get(DATA_RETURNING);
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    /** No se la puede golpear con la mano: solo vuela. Y no choca con vos, tus mascotas ni tus aliados. */
    @Override
    protected boolean canHitEntity(Entity target) {
        if (!super.canHitEntity(target)) {
            return false;
        }
        Entity owner = getOwner();
        if (!(target instanceof LivingEntity living) || target == owner || !living.isAlive() || living.isSpectator()) {
            return false;
        }
        if (target instanceof ArmorStand || GuardHandler.isAllied(target)) {
            return false;
        }
        if (owner != null) {
            if (GuardHandler.isPet(target) && owner.getUUID().equals(GuardHandler.ownerIdOf(target))) {
                return false;
            }
            if (target instanceof TamableAnimal pet && owner instanceof LivingEntity ownerLiving && pet.isOwnedBy(ownerLiving)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            // el cliente solo sigue la velocidad que le manda el servidor
            Vec3 v = getDeltaMovement();
            setPos(getX() + v.x, getY() + v.y, getZ() + v.z);
            if (tickCount % 2 == 0) {
                level().addParticle(ParticleTypes.SMALL_FLAME, getX(), getY() + 0.1D, getZ(), 0.0D, 0.0D, 0.0D);
            }
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (tickCount > MAX_LIFE_TICKS) {
            discard();
            return;
        }
        if (isReturning()) {
            tickReturn(serverLevel);
        } else {
            tickOut(serverLevel);
        }
    }

    private void tickOut(ServerLevel level) {
        Entity owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            discard();
            return;
        }
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() == HitResult.Type.BLOCK) {
            onBlockHit(level, owner, (BlockHitResult) hit);
            return;
        }
        if (hit.getType() == HitResult.Type.ENTITY) {
            onEntityHit(level, (EntityHitResult) hit);
            return;
        }
        Vec3 v = getDeltaMovement();
        setPos(getX() + v.x, getY() + v.y, getZ() + v.z);
        travelled += v.length();
        if (tickCount % 4 == 0) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.5F, 1.4F);
        }
        if (travelled >= Config.DICRITI_RANGE.get()) {
            startReturn();
        }
    }

    private void onBlockHit(ServerLevel level, Entity owner, BlockHitResult hit) {
        setPos(hit.getLocation());
        DicritiMining.Result result = DicritiMining.Result.FAILED;
        if (owner instanceof ServerPlayer player) {
            result = DicritiMining.strike(level, player, getStack(), hit.getBlockPos());
        }
        switch (result) {
            case BROKEN -> {
                level.playSound(null, getX(), getY(), getZ(), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.PLAYERS, 1.0F, 1.2F);
                level.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 12, 0.3D, 0.3D, 0.3D, 0.04D);
            }
            case CRACKED -> {
                level.playSound(null, getX(), getY(), getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 1.5F);
                level.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 10, 0.25D, 0.25D, 0.25D, 0.3D);
            }
            default -> {
                level.playSound(null, getX(), getY(), getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 1.3F);
                level.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 6, 0.2D, 0.2D, 0.2D, 0.02D);
            }
        }
        startReturn();
    }

    private void onEntityHit(ServerLevel level, EntityHitResult hit) {
        Entity target = hit.getEntity();
        setPos(hit.getLocation());
        level.playSound(null, getX(), getY(), getZ(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 8, 0.25D, 0.25D, 0.25D, 0.3D);
        if (Config.DICRITI_PULL.get() && target instanceof LivingEntity living && canPull(living)) {
            pulledId = living.getId();
        }
        startReturn();
    }

    /** Los jefes y las criaturas enormes (los titanes, un ghast...) no se dejan arrastrar. */
    private static boolean canPull(LivingEntity target) {
        return !target.getType().is(Tags.EntityTypes.BOSSES) && target.getBbWidth() <= 2.2F && target.getBbHeight() <= 4.0F;
    }

    private void startReturn() {
        this.entityData.set(DATA_RETURNING, Boolean.TRUE);
        this.returnTicks = 0;
    }

    private void tickReturn(ServerLevel level) {
        Entity owner = getOwner();
        if (owner == null || !owner.isAlive() || owner.level() != level) {
            discard();
            return;
        }
        returnTicks++;
        Vec3 goal = owner.position().add(0.0D, owner.getBbHeight() * 0.6D, 0.0D);
        Vec3 delta = goal.subtract(position());
        double distance = delta.length();
        if (distance <= CATCH_DISTANCE || returnTicks > MAX_RETURN_TICKS) {
            catchBy(level, owner);
            return;
        }
        double speed = pulledId >= 0 ? DRAG_SPEED : BACK_SPEED;
        Vec3 step = delta.scale(Math.min(speed, distance) / distance);
        setDeltaMovement(step);
        setPos(getX() + step.x, getY() + step.y, getZ() + step.z);
        dragTarget(level);
        if (tickCount % 4 == 0) {
            level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.5F, 1.6F);
        }
    }

    /** La criatura agarrada se mueve hacia el pico (que viene hacia el jugador) y no se lastima con la caida. */
    private void dragTarget(ServerLevel level) {
        if (pulledId < 0) {
            return;
        }
        Entity found = level.getEntity(pulledId);
        if (!(found instanceof LivingEntity target) || !target.isAlive()) {
            pulledId = -1;
            return;
        }
        Vec3 toPick = position().subtract(target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D));
        double distance = toPick.length();
        if (distance > 1.0D) {
            Vec3 pull = toPick.scale(Math.min(1.3D, distance) / distance);
            double lift = Mth.clamp(pull.y, -0.3D, 0.55D) + (target.onGround() ? 0.12D : 0.0D);
            target.setDeltaMovement(pull.x, lift, pull.z);
            target.hurtMarked = true;
        }
        target.fallDistance = 0.0F;
    }

    /** El pico llega al jugador: vuelve a la ranura de donde salio (o a cualquier ranura libre; si esta lleno, al suelo). */
    private void catchBy(ServerLevel level, Entity owner) {
        ItemStack stack = getStack();
        finished = true;
        if (!stack.isEmpty()) {
            if (owner instanceof Player player) {
                Inventory inventory = player.getInventory();
                if (slot >= 0 && slot < inventory.getContainerSize() && inventory.getItem(slot).isEmpty()) {
                    inventory.setItem(slot, stack);
                } else if (!inventory.add(stack)) {
                    player.drop(stack, false);
                }
            } else {
                spawnAtLocation(stack);
            }
            level.playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
        discard();
    }

    /** Pase lo que pase (jugador muerto o desconectado, /kill, etc.), el pico no se pierde: queda tirado donde esta. */
    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide && !finished && reason.shouldDestroy()) {
            finished = true;
            ItemStack stack = getStack();
            if (!stack.isEmpty()) {
                spawnAtLocation(stack);
            }
        }
        super.remove(reason);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Pick", getStack().save(new CompoundTag()));
        tag.putInt("Slot", slot);
        tag.putBoolean("Returning", isReturning());
        tag.putDouble("Travelled", travelled);
        tag.putInt("ReturnTicks", returnTicks);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_STACK, ItemStack.of(tag.getCompound("Pick")));
        this.slot = tag.getInt("Slot");
        this.entityData.set(DATA_RETURNING, tag.getBoolean("Returning"));
        this.travelled = tag.getDouble("Travelled");
        this.returnTicks = tag.getInt("ReturnTicks");
    }
}
