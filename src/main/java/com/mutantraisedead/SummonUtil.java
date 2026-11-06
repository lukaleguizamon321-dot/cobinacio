package com.mutantraisedead;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/** Cosas en comun de los hechizos de invocacion: donde aparecen, como se crean y como se marcan. */
public final class SummonUtil {
    /** Duracion de los invocados: 10 minutos, igual que Revivir Muertos. */
    public static final int SUMMON_TICKS = 20 * 60 * 10;

    private SummonUtil() {
    }

    /** Punto donde poner al invocado numero index (de count) en un circulo alrededor del que lanza el hechizo. */
    public static Vec3 spawnPoint(Level world, LivingEntity caster, Mob mob, int index, int count, float radius) {
        double yrot = 6.281f / count * index + caster.getYRot() * Mth.DEG_TO_RAD;
        Vec3 spawn = Utils.moveToRelativeGroundLevel(world, caster.getEyePosition().add(new Vec3(radius * Mth.cos((float) yrot), 0, radius * Mth.sin((float) yrot))), 10);
        spawn = world.clip(new ClipContext(caster.getEyePosition(), spawn, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getLocation();
        if (!world.noCollision(mob.getBoundingBox().move(spawn))) {
            spawn = Utils.moveToRelativeGroundLevel(world, spawn.add(caster.getEyePosition().subtract(spawn).normalize().scale(caster.getBbWidth() * 1.1)), 3);
        }
        return spawn;
    }

    /** Si el bicho es grande y no entra, lo sube de a un bloque (hasta maxLift). Devuelve null si no hay lugar. */
    public static Vec3 liftToFit(Level world, Mob mob, Vec3 spawn, int maxLift) {
        for (int dy = 0; dy <= maxLift; dy++) {
            if (world.noCollision(mob, mob.getBoundingBox().move(spawn.x - mob.getX(), spawn.y + dy - mob.getY(), spawn.z - mob.getZ()))) {
                return new Vec3(spawn.x, spawn.y + dy, spawn.z);
            }
        }
        return null;
    }

    /** Termina de invocar: lo ubica, lo marca como guardian del jugador y lo registra en Iron's. */
    public static void finish(ServerLevel level, LivingEntity caster, Mob mob, Vec3 spawn, SummonedEntitiesCastData castData) {
        mob.setPos(spawn.x, spawn.y, spawn.z);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
        mob.setPersistenceRequired();
        GuardHandler.mark(mob, caster, level.getGameTime() + SUMMON_TICKS + 200L);
        mob.setYRot(caster.getYRot());
        mob.setOldPosAndRot();
        level.addFreshEntity(mob);
        SummonManager.initSummon(caster, mob, SUMMON_TICKS, castData);
    }

    /** Crea un mob al azar de la lista de ids (los que no existen se ignoran). Null si no hay ninguno valido. */
    public static Mob createFromIds(ServerLevel level, List<? extends String> ids) {
        List<EntityType<?>> valid = new ArrayList<>();
        for (String id : ids) {
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key != null && ForgeRegistries.ENTITY_TYPES.containsKey(key)) {
                valid.add(ForgeRegistries.ENTITY_TYPES.getValue(key));
            } else {
                MutantRaiseDead.LOGGER.warn("[Cobinatio] El mob '{}' del config no existe, lo ignoro.", id);
            }
        }
        if (valid.isEmpty()) {
            return null;
        }
        Entity created = valid.get(level.getRandom().nextInt(valid.size())).create(level);
        if (created instanceof Mob mob) {
            return mob;
        }
        if (created != null) {
            created.discard();
        }
        return null;
    }
}
