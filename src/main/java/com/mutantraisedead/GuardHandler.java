package com.mutantraisedead;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Se encarga de los mutantes guardianes:
 *  - reemplaza al zombi/esqueleto normal de Iron's por el mutante
 *  - hace que protejan a su dueno (en vez de atacarlo, como hacen normalmente)
 *  - los hace seguir al dueno y desaparecer pasado el tiempo
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class GuardHandler {
    private static final String MARKER_TAG = "mutantraisedead_guard";
    private static final String DATA_OWNER = "mutantraisedead_owner";
    private static final String DATA_EXPIRE = "mutantraisedead_expire";

    private record Pending(UUID owner, Entity summon, boolean zombie) {
    }

    private static final List<Pending> PENDING = new ArrayList<>();

    private GuardHandler() {
    }

    // ------------------------------------------------------------------
    // Reemplazo del invocado normal por el mutante
    // ------------------------------------------------------------------

    public static void queueReplacement(ServerPlayer owner, Entity summon, boolean zombie) {
        // Se hace un tick despues, cuando Iron's ya termino de registrar al invocado.
        PENDING.add(new Pending(owner.getUUID(), summon, zombie));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }
        List<Pending> work = new ArrayList<>(PENDING);
        PENDING.clear();
        for (Pending pending : work) {
            try {
                replace(event.getServer(), pending);
            } catch (Throwable t) {
                MutantRaiseDead.LOGGER.error("[MutantRaiseDead] Error al crear el mutante guardian", t);
            }
        }
    }

    private static void replace(MinecraftServer server, Pending pending) {
        Entity old = pending.summon();
        if (old.isRemoved() || !(old.level() instanceof ServerLevel level)) {
            return;
        }
        ServerPlayer owner = server.getPlayerList().getPlayer(pending.owner());
        if (owner == null || !owner.isAlive() || owner.level() != level) {
            return;
        }

        Mob mutant = createMutant(level, pending.zombie());
        if (mutant == null) {
            return; // no hay un mutante valido: se queda el invocado normal
        }

        mutant.moveTo(old.getX(), old.getY(), old.getZ(), old.getYRot(), 0.0F);
        // Los mutantes son grandes: si no entran donde estaba el zombi, lo subimos un poco.
        boolean placed = false;
        for (int dy = 0; dy <= 3; dy++) {
            if (level.noCollision(mutant, mutant.getBoundingBox().move(0.0D, dy, 0.0D))) {
                mutant.moveTo(old.getX(), old.getY() + dy, old.getZ(), old.getYRot(), 0.0F);
                placed = true;
                break;
            }
        }
        if (!placed) {
            mutant.discard();
            return;
        }

        mutant.finalizeSpawn(level, level.getCurrentDifficultyAt(mutant.blockPosition()),
                MobSpawnType.MOB_SUMMONED, null, null);
        mutant.setPersistenceRequired();
        mutant.addTag(MARKER_TAG);
        CompoundTag data = mutant.getPersistentData();
        data.putUUID(DATA_OWNER, owner.getUUID());
        data.putLong(DATA_EXPIRE, level.getGameTime() + Config.GUARD_LIFETIME_SECONDS.get() * 20L);

        level.addFreshEntity(mutant);
        level.sendParticles(ParticleTypes.SOUL, mutant.getX(), mutant.getY() + 1.0D, mutant.getZ(),
                20, 0.6D, 1.0D, 0.6D, 0.02D);
        old.discard();

        MutantRaiseDead.LOGGER.info("[MutantRaiseDead] {} invoco un mutante guardian: {}",
                owner.getGameProfile().getName(), ForgeRegistries.ENTITY_TYPES.getKey(mutant.getType()));
    }

    private static Mob createMutant(ServerLevel level, boolean zombie) {
        List<? extends String> ids = zombie ? Config.ZOMBIE_MUTANTS.get() : Config.SKELETON_MUTANTS.get();
        List<EntityType<?>> valid = new ArrayList<>();
        for (String id : ids) {
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key != null && ForgeRegistries.ENTITY_TYPES.containsKey(key)) {
                valid.add(ForgeRegistries.ENTITY_TYPES.getValue(key));
            } else {
                MutantRaiseDead.LOGGER.warn("[MutantRaiseDead] El mob '{}' del config no existe, lo ignoro.", id);
            }
        }
        if (valid.isEmpty()) {
            return null;
        }
        EntityType<?> type = valid.get(level.getRandom().nextInt(valid.size()));
        Entity entity = type.create(level);
        if (entity instanceof Mob mob) {
            return mob;
        }
        if (entity != null) {
            entity.discard();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Reglas de combate: a quien pueden atacar y a quien no
    // ------------------------------------------------------------------

    private static boolean isGuard(Entity entity) {
        return entity.getTags().contains(MARKER_TAG);
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
        return IronsBridge.getSummonOwner(entity) instanceof Player;
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
        if (target == null || self.level().isClientSide || !isGuard(self)) {
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
        if (!(living instanceof Mob mob) || !isGuard(mob) || !(mob.level() instanceof ServerLevel level)) {
            return;
        }

        UUID ownerId = ownerOf(mob);
        long expire = mob.getPersistentData().getLong(DATA_EXPIRE);
        ServerPlayer owner = ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null || !owner.isAlive() || owner.level() != level || level.getGameTime() >= expire) {
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
        mob.discard();
    }
}
