package com.mutantraisedead;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.spells.root.RootEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Se encarga de los mutantes guardianes:
 *  - hace que protejan a su dueno (en vez de atacarlo, como hacen normalmente)
 *  - los hace seguir al dueno y desaparecer pasado el tiempo
 *  - no sueltan botin ni experiencia (para que no sirva de granja)
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class GuardHandler {
    private static final String MARKER_TAG = "mutantraisedead_guard";
    private static final String DATA_OWNER = "mutantraisedead_owner";
    private static final String DATA_EXPIRE = "mutantraisedead_expire";
    private static final String DATA_PET = "mutantraisedead_pet";

    private static final String PLANT_TAG = "mutantraisedead_plant";
    private static final String DATA_PLANT_TARGET = "mutantraisedead_plant_target";

    private GuardHandler() {
    }

    /** Marca a una planta de Enraizar Plantas: solo muerde al objetivo inmovilizado. */
    public static void markPlant(Mob plant, Entity owner, Entity target, long expireGameTime) {
        plant.addTag(PLANT_TAG);
        CompoundTag data = plant.getPersistentData();
        data.putUUID(DATA_OWNER, owner.getUUID());
        data.putUUID(DATA_PLANT_TARGET, target.getUUID());
        data.putLong(DATA_EXPIRE, expireGameTime);
    }

    private static boolean isPlant(Entity entity) {
        return entity.getTags().contains(PLANT_TAG);
    }

    private static UUID plantTargetOf(Entity plant) {
        CompoundTag data = plant.getPersistentData();
        return data.hasUUID(DATA_PLANT_TARGET) ? data.getUUID(DATA_PLANT_TARGET) : null;
    }

    /** Marca a un mutante recien invocado como guardian de su dueno. */
    public static void mark(Mob mob, Entity owner, long expireGameTime) {
        mob.addTag(MARKER_TAG);
        CompoundTag data = mob.getPersistentData();
        data.putUUID(DATA_OWNER, owner.getUUID());
        data.putLong(DATA_EXPIRE, expireGameTime);
    }

    /**
     * Marca a un cangrejo nacido de un huevo como aliado permanente de su dueno: protege igual que un guardian,
     * pero no desaparece con el tiempo ni cuando el dueno se desconecta (se queda esperandolo).
     */
    public static void markPet(Mob mob, Entity owner) {
        mark(mob, owner, Long.MAX_VALUE);
        mob.getPersistentData().putBoolean(DATA_PET, true);
        mob.setPersistenceRequired();
    }

    /** true si es un cangrejo criado de un huevo (aliado permanente de su dueno). */
    public static boolean isPet(Entity entity) {
        return entity.getPersistentData().getBoolean(DATA_PET);
    }

    /** Dueno de un aliado marcado (o null). */
    public static UUID ownerIdOf(Entity entity) {
        return ownerOf(entity);
    }

    // ------------------------------------------------------------------
    // Reglas de combate: a quien pueden atacar y a quien no
    // ------------------------------------------------------------------

    private static boolean isGuard(Entity entity) {
        return entity.getTags().contains(MARKER_TAG);
    }

    /** true si es un guardian o una planta invocada por el jugador (para que otros efectos los dejen en paz). */
    public static boolean isAllied(Entity entity) {
        return isGuard(entity) || isPlant(entity);
    }

    private static UUID ownerOf(Entity guard) {
        CompoundTag data = guard.getPersistentData();
        return data.hasUUID(DATA_OWNER) ? data.getUUID(DATA_OWNER) : null;
    }

    /** Aliados que los guardianes nunca deben lastimar. */
    private static boolean isFriendly(Entity entity) {
        if (entity instanceof Player || isGuard(entity)) {
            return true;
        }
        if (entity instanceof TamableAnimal pet && pet.isTame()) {
            return true;
        }
        return SummonManager.getOwner(entity) instanceof Player;
    }

    /** Un guardian solo ataca monstruos hostiles; nunca jugadores, aldeanos, mascotas ni invocados. */
    private static boolean isValidTarget(UUID ownerId, LivingEntity target) {
        if (!target.isAlive() || target.getUUID().equals(ownerId)) {
            return false;
        }
        if (isFriendly(target)) {
            return false;
        }
        return target instanceof Enemy;
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity self = event.getEntity();
        LivingEntity target = event.getNewTarget();
        if (target == null || self.level().isClientSide) {
            return;
        }
        if (isPlant(self)) {
            // Las plantas solo pueden apuntar al objetivo inmovilizado.
            if (!target.getUUID().equals(plantTargetOf(self))) {
                event.setCanceled(true);
            }
            return;
        }
        if (!isGuard(self)) {
            return;
        }
        // Los mutantes de Mutant Monsters atacan jugadores, aldeanos y golems por defecto: lo frenamos.
        if (!isValidTarget(ownerOf(self), target)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker == null) {
            return;
        }
        if (isPlant(attacker)) {
            // Una planta solo lastima a su objetivo.
            if (!victim.getUUID().equals(plantTargetOf(attacker))) {
                event.setCanceled(true);
            }
            return;
        }
        if (isPlant(victim)) {
            // El dueno no lastima a sus plantas por accidente; el objetivo si puede defenderse.
            if (attacker.getUUID().equals(ownerOf(victim))) {
                event.setCanceled(true);
            }
            return;
        }
        // Un guardian no lastima a jugadores, otros guardianes ni a invocados de Iron's
        // (esto tambien cubre los golpes en area de los mutantes).
        if (isGuard(attacker) && isFriendly(victim)) {
            event.setCanceled(true);
            return;
        }
        // El dueno no lastima a sus propios guardianes por accidente.
        if (isGuard(victim) && attacker instanceof Player) {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------
    // Comportamiento: seguir al dueno, buscar enemigos, desaparecer
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide || (living.tickCount + living.getId()) % 10 != 0) {
            return;
        }
        if (living instanceof Mob plant && isPlant(plant) && plant.level() instanceof ServerLevel plantLevel) {
            tickPlant(plantLevel, plant);
            return;
        }
        if (!(living instanceof Mob mob) || !isGuard(mob) || !(mob.level() instanceof ServerLevel level)) {
            return;
        }

        UUID ownerId = ownerOf(mob);
        long expire = mob.getPersistentData().getLong(DATA_EXPIRE);
        ServerPlayer owner = ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null || !owner.isAlive() || owner.level() != level || level.getGameTime() >= expire) {
            if (isPet(mob)) {
                // un cangrejo criado no desaparece: se queda quieto esperando a su dueno
                if (mob.getTarget() != null && !mob.getTarget().isAlive()) {
                    mob.setTarget(null);
                }
                return;
            }
            vanish(level, mob);
            return;
        }

        LivingEntity current = mob.getTarget();
        if (current != null && (!isValidTarget(ownerId, current) || current.distanceToSqr(owner) > 40.0D * 40.0D)) {
            mob.setTarget(null);
            current = null;
        }
        if (current == null) {
            LivingEntity found = findTarget(level, mob, owner, ownerId);
            if (found != null) {
                mob.setTarget(found);
                current = found;
            }
        }

        if (current == null) {
            double distSqr = mob.distanceToSqr(owner);
            if (distSqr > 24.0D * 24.0D) {
                teleportNear(level, mob, owner);
            } else if (distSqr > 8.0D * 8.0D) {
                mob.getNavigation().moveTo(owner, 1.15D);
            }
        }
    }

    private static void tickPlant(ServerLevel level, Mob plant) {
        long expire = plant.getPersistentData().getLong(DATA_EXPIRE);
        UUID targetId = plantTargetOf(plant);
        Entity found = targetId == null ? null : level.getEntity(targetId);
        boolean rootGone = plant.tickCount > 60 && found != null && !(found.getVehicle() instanceof RootEntity);
        if (!(found instanceof LivingEntity target) || !target.isAlive() || level.getGameTime() >= expire || rootGone) {
            vanish(level, plant);
            return;
        }
        if (plant.getTarget() != target) {
            plant.setTarget(target);
        }
    }

    private static LivingEntity findTarget(ServerLevel level, Mob guard, ServerPlayer owner, UUID ownerId) {
        // 1) Quien acaba de lastimar al dueno
        LivingEntity attacker = owner.getLastHurtByMob();
        if (attacker != null && owner.tickCount - owner.getLastHurtByMobTimestamp() < 200 && isValidTarget(ownerId, attacker)) {
            return attacker;
        }
        // 2) Monstruos que estan apuntando al dueno
        AABB wide = owner.getBoundingBox().inflate(16.0D);
        List<Mob> hunters = level.getEntitiesOfClass(Mob.class, wide,
                m -> m.getTarget() == owner && isValidTarget(ownerId, m));
        if (!hunters.isEmpty()) {
            return hunters.stream().min(Comparator.comparingDouble(m -> m.distanceToSqr(owner))).get();
        }
        // 3) Lo que el dueno acaba de golpear
        LivingEntity victim = owner.getLastHurtMob();
        if (victim != null && owner.tickCount - owner.getLastHurtMobTimestamp() < 200 && isValidTarget(ownerId, victim)) {
            return victim;
        }
        // 4) Cualquier monstruo hostil cerca del dueno
        AABB near = owner.getBoundingBox().inflate(10.0D);
        List<Mob> nearby = level.getEntitiesOfClass(Mob.class, near, m -> isValidTarget(ownerId, m));
        if (!nearby.isEmpty()) {
            return nearby.stream().min(Comparator.comparingDouble(m -> m.distanceToSqr(owner))).get();
        }
        return null;
    }

    private static void teleportNear(ServerLevel level, Mob mob, ServerPlayer owner) {
        for (int i = 0; i < 12; i++) {
            double x = owner.getX() + (level.getRandom().nextDouble() - 0.5D) * 6.0D;
            double z = owner.getZ() + (level.getRandom().nextDouble() - 0.5D) * 6.0D;
            double y = owner.getY();
            AABB box = mob.getBoundingBox().move(x - mob.getX(), y - mob.getY(), z - mob.getZ());
            if (level.noCollision(mob, box)) {
                mob.moveTo(x, y, z, mob.getYRot(), mob.getXRot());
                mob.getNavigation().stop();
                return;
            }
        }
    }

    private static void vanish(ServerLevel level, Mob mob) {
        level.sendParticles(ParticleTypes.SOUL, mob.getX(), mob.getY() + 1.0D, mob.getZ(),
                20, 0.6D, 1.0D, 0.6D, 0.02D);
        SummonManager.removeSummon(mob);
        mob.discard();
    }

    /** Las explosiones de tus guardianes (por ejemplo el Creeper Mutante) no rompen bloques. */
    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Entity source = event.getExplosion().getDirectSourceEntity();
        if (source != null && isAllied(source)) {
            event.getAffectedBlocks().clear();
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide && (isGuard(entity) || isPlant(entity))) {
            SummonManager.removeSummon(entity);
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (isGuard(event.getEntity()) || isPlant(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExperience(LivingExperienceDropEvent event) {
        if (isGuard(event.getEntity()) || isPlant(event.getEntity())) {
            event.setDroppedExperience(0);
        }
    }
}
