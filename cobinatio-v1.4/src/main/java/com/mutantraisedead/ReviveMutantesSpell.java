package com.mutantraisedead;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.entity.mobs.SummonedSkeleton;
import io.redspace.ironsspellbooks.entity.mobs.SummonedZombie;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Copia de "Revivir Muertos" de Iron's Spells 'n Spellbooks, pero cada zombi/esqueleto
 * puede salir mutante segun el nivel mutante (1 = X, 2 = Y, 3 = Z).
 */
public class ReviveMutantesSpell extends AbstractSpell {
    private final int tier;
    private final ResourceLocation spellId;
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(ModRegistry.SCHOOL_ID)
            .setMaxLevel(6)
            .setCooldownSeconds(150)
            .setAllowCrafting(false)
            .build();

    public ReviveMutantesSpell(int tier, String name) {
        this.tier = tier;
        this.spellId = ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, name);
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 3;
        this.castTime = 30;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        int percent = (int) Math.round(Config.chanceForTier(tier) * 100.0D);
        return List.of(
                Component.translatable("ui.irons_spellbooks.summon_count", getSummonCount(spellLevel, caster)),
                Component.translatable("ui.mutantraisedead.mutant_chance", percent));
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
        return Optional.of(SoundRegistry.RAISE_DEAD_START.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    public int getSummonCount(int spellLevel, LivingEntity caster) {
        return spellLevel + 2;
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
            SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();
            int summonTime = 20 * 60 * 10;
            int count = getSummonCount(spellLevel, entity);
            float radius = 1.5f + .185f * count;
            double chance = Config.chanceForTier(tier);

            for (int i = 0; i < count; i++) {
                boolean isSkeleton = Utils.random.nextDouble() < .3;
                var equipment = getEquipment(getSpellPower(spellLevel, entity), Utils.random);

                Mob undead = null;
                boolean mutant = false;
                if (entity instanceof ServerPlayer && Utils.random.nextDouble() < chance) {
                    undead = createMutant(serverLevel, !isSkeleton);
                    mutant = undead != null;
                }
                if (undead == null) {
                    undead = isSkeleton ? new SummonedSkeleton(world, entity, true) : new SummonedZombie(world, entity, true);
                }

                var yrot = 6.281f / count * i + entity.getYRot() * Mth.DEG_TO_RAD;
                Vec3 spawn = Utils.moveToRelativeGroundLevel(world, entity.getEyePosition().add(new Vec3(radius * Mth.cos(yrot), 0, radius * Mth.sin(yrot))), 10);
                spawn = world.clip(new ClipContext(entity.getEyePosition(), spawn, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getLocation();
                if (!world.noCollision(undead.getBoundingBox().move(spawn))) {
                    spawn = Utils.moveToRelativeGroundLevel(world, spawn.add(entity.getEyePosition().subtract(spawn).normalize().scale(entity.getBbWidth() * 1.1)), 3);
                }

                if (mutant) {
                    // Los mutantes son grandes: si no entran, los subimos un poco.
                    boolean placed = false;
                    for (int dy = 0; dy <= 3; dy++) {
                        if (world.noCollision(undead, undead.getBoundingBox().move(spawn.x - undead.getX(), spawn.y + dy - undead.getY(), spawn.z - undead.getZ()))) {
                            spawn = new Vec3(spawn.x, spawn.y + dy, spawn.z);
                            placed = true;
                            break;
                        }
                    }
                    if (!placed) {
                        // No entra: en su lugar sale el invocado normal.
                        undead.discard();
                        undead = isSkeleton ? new SummonedSkeleton(world, entity, true) : new SummonedZombie(world, entity, true);
                        mutant = false;
                    }
                }

                undead.setPos(spawn.x, spawn.y, spawn.z);
                undead.finalizeSpawn(serverLevel, world.getCurrentDifficultyAt(undead.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
                if (!mutant) {
                    equip(undead, equipment);
                } else {
                    undead.setPersistenceRequired();
                    GuardHandler.mark(undead, entity, serverLevel.getGameTime() + summonTime + 200L);
                }
                undead.setYRot(entity.getYRot());
                undead.setOldPosAndRot();
                world.addFreshEntity(undead);
                SummonManager.initSummon(entity, undead, summonTime, summonedEntitiesCastData);
            }

            RecastInstance recastInstance = new RecastInstance(this.getSpellId(), spellLevel, getRecastCount(spellLevel, entity), summonTime, castSource, summonedEntitiesCastData);
            recasts.addRecast(recastInstance, playerMagicData);
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.RAISE_DEAD_FINISH.get(), entity.getSoundSource(), 2.0f, .9f + Utils.random.nextFloat() * .2f);
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    /** Crea un mutante de la lista del config, o null si no hay ninguno valido. */
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
        Entity created = type.create(level);
        if (created instanceof Mob mob) {
            return mob;
        }
        if (created != null) {
            created.discard();
        }
        return null;
    }

    private void equip(Mob mob, ItemStack[] equipment) {
        mob.setItemSlot(EquipmentSlot.FEET, equipment[0]);
        mob.setItemSlot(EquipmentSlot.LEGS, equipment[1]);
        mob.setItemSlot(EquipmentSlot.CHEST, equipment[2]);
        mob.setItemSlot(EquipmentSlot.HEAD, equipment[3]);
        mob.setDropChance(EquipmentSlot.FEET, 0.0F);
        mob.setDropChance(EquipmentSlot.LEGS, 0.0F);
        mob.setDropChance(EquipmentSlot.CHEST, 0.0F);
        mob.setDropChance(EquipmentSlot.HEAD, 0.0F);
        mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
    }

    private ItemStack[] getEquipment(float power, RandomSource random) {
        Item[] leather = {Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET};
        Item[] chain = {Items.CHAINMAIL_BOOTS, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_HELMET};
        Item[] iron = {Items.IRON_BOOTS, Items.IRON_LEGGINGS, Items.IRON_CHESTPLATE, Items.IRON_HELMET};

        int minQuality = 12;
        int maxQuality = getMaxLevel() * spellPowerPerLevel + 15;

        ItemStack[] result = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            float quality = Mth.clamp((power + (random.nextIntBetweenInclusive(-3, 8)) - minQuality) / (maxQuality - minQuality), 0, .95f);
            if (random.nextDouble() < quality * quality) {
                if (quality > .85) {
                    result[i] = new ItemStack(iron[i]);
                } else if (quality > .65) {
                    result[i] = new ItemStack(chain[i]);
                } else if (quality > .15) {
                    result[i] = new ItemStack(leather[i]);
                } else {
                    result[i] = ItemStack.EMPTY;
                }
            } else {
                result[i] = ItemStack.EMPTY;
            }
        }
        return result;
    }
}
