package com.mutantraisedead;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Base de los hechizos que invocan guardianes (cangrejos, blazes, creeper mutante).
 * Funciona igual que Revivir Muertos: los invocados duran 10 minutos y volver a lanzar el hechizo los despide.
 */
public abstract class SummonSpellBase extends AbstractSpell {
    private final ResourceLocation spellId;
    private final DefaultConfig defaultConfig;
    private final SoundEvent startSound;

    protected SummonSpellBase(String name, DefaultConfig config, SoundEvent startSound) {
        this.spellId = ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, name);
        this.defaultConfig = config;
        this.startSound = startSound;
    }

    /** Cuantos invocados salen en este nivel. */
    public abstract int getSummonCount(int spellLevel);

    /** Crea (sin ubicar) los invocados de este lanzamiento. */
    protected abstract List<Mob> createSummons(ServerLevel level, LivingEntity caster, int spellLevel);

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.ofNullable(startSound);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (SummonManager.recastFinishedHelper(serverPlayer, recastInstance, recastResult, castDataSerializable)) {
            super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
        }
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new SummonedEntitiesCastData();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        var recasts = playerMagicData.getPlayerRecasts();
        if (!recasts.hasRecastForSpell(this) && world instanceof ServerLevel serverLevel) {
            SummonedEntitiesCastData castData = new SummonedEntitiesCastData();
            List<Mob> summons = createSummons(serverLevel, entity, spellLevel);
            int count = summons.size();
            float radius = 1.5f + .185f * Math.max(count, 1);
            for (int i = 0; i < count; i++) {
                Mob mob = summons.get(i);
                Vec3 spawn = SummonUtil.spawnPoint(world, entity, mob, i, count, radius);
                Vec3 lifted = SummonUtil.liftToFit(world, mob, spawn, 3);
                SummonUtil.finish(serverLevel, entity, mob, lifted != null ? lifted : spawn, castData);
            }
            if (count > 0) {
                RecastInstance recastInstance = new RecastInstance(this.getSpellId(), spellLevel, getRecastCount(spellLevel, entity), SummonUtil.SUMMON_TICKS, castSource, castData);
                recasts.addRecast(recastInstance, playerMagicData);
            }
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }
}
