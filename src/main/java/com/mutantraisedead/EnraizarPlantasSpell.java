package com.mutantraisedead;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.spells.root.RootEntity;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

/**
 * Copia de "Enraizar" de Iron's Spells 'n Spellbooks. Ademas de inmovilizar al objetivo,
 * aparecen 1 a 4 plantas Foliaath (Mowzie's Mobs) en forma de X, a un bloque de distancia,
 * que lo muerden mientras dure el hechizo.
 */
public class EnraizarPlantasSpell extends AbstractSpell {
    /** Posiciones de las plantas respecto al objetivo: una X de a un bloque. */
    private static final int[][] OFFSETS = {{1, 1}, {-1, -1}, {1, -1}, {-1, 1}};
    private static final ResourceLocation FOLIAATH = ResourceLocation.fromNamespaceAndPath("mowziesmobs", "foliaath");

    private final int tier;
    private final ResourceLocation spellId;
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(ModRegistry.SCHOOL_ID_PLANTS)
            .setMaxLevel(10)
            .setCooldownSeconds(35)
            .setAllowCrafting(false)
            .build();

    public EnraizarPlantasSpell(int tier, String name) {
        this.tier = tier;
        this.spellId = ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, name);
        this.manaCostPerLevel = 3;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 50;
        this.baseManaCost = 45;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.hp", Utils.stringTruncation(getRootHealth(spellLevel, caster), 1)),
                Component.translatable("ui.mutantraisedead.plant_count", tier));
    }

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
        return Optional.of(SoundEvents.EVOKER_PREPARE_ATTACK);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 32, .35f);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel serverLevel
                && playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castTargetingData) {
            LivingEntity target = castTargetingData.getTarget(serverLevel);
            if (target != null && !target.getType().is(ModTags.CANT_ROOT)) {
                Vec3 spawn = target.position();
                float health = getRootHealth(spellLevel, entity);
                int duration = getDuration(spellLevel, entity);
                RootEntity rootEntity = new RootEntity(level, entity);
                rootEntity.setDuration(duration);
                rootEntity.setTarget(target);
                rootEntity.moveTo(spawn);
                rootEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
                rootEntity.setHealth(health);
                level.addFreshEntity(rootEntity);
                target.stopRiding();
                target.startRiding(rootEntity, true);

                spawnPlants(serverLevel, entity, target, duration);
            }
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void spawnPlants(ServerLevel level, LivingEntity caster, LivingEntity target, int duration) {
        if (!ForgeRegistries.ENTITY_TYPES.containsKey(FOLIAATH)) {
            return; // Mowzie's Mobs no esta instalado: queda solo el Enraizar normal
        }
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(FOLIAATH);
        for (int i = 0; i < tier && i < OFFSETS.length; i++) {
            Entity created = type.create(level);
            if (!(created instanceof Mob plant)) {
                if (created != null) {
                    created.discard();
                }
                continue;
            }
            Vec3 pos = new Vec3(target.getX() + OFFSETS[i][0], target.getY(), target.getZ() + OFFSETS[i][1]);
            pos = Utils.moveToRelativeGroundLevel(level, pos, 3);
            plant.setPos(pos.x, pos.y, pos.z);
            boolean placed = false;
            for (int dy = 0; dy <= 2; dy++) {
                if (level.noCollision(plant, plant.getBoundingBox().move(0, dy, 0))) {
                    plant.setPos(pos.x, pos.y + dy, pos.z);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                plant.discard();
                continue;
            }
            plant.finalizeSpawn(level, level.getCurrentDifficultyAt(plant.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
            plant.setPersistenceRequired();
            var damage = plant.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damage != null) {
                damage.setBaseValue(Config.PLANT_DAMAGE.get());
            }
            GuardHandler.markPlant(plant, caster, target, level.getGameTime() + duration + 40L);
            level.addFreshEntity(plant);
        }
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * 20);
    }

    private float getRootHealth(int spellLevel, LivingEntity entity) {
        return 40 * getEntityPowerMultiplier(entity);
    }
}
