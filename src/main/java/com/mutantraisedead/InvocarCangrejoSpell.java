package com.mutantraisedead;

import com.mutantraisedead.crab.GrottolVariant;
import com.mutantraisedead.crab.GrottolVariantEntity;
import com.mutantraisedead.crab.ModEntities;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Invocar Cangrejo (carbon / cobre / hierro). Nivel 1 a 4 = cantidad de cangrejos;
 * nivel 5 = 4 cangrejos con mas vida (25). La tinta de cada nivel: comun, poco comun, rara, epica, legendaria.
 */
public class InvocarCangrejoSpell extends SummonSpellBase {
    public static final int MAX_LEVEL = 5;

    private final GrottolVariant variant;

    public InvocarCangrejoSpell(GrottolVariant variant, String name, boolean craftable) {
        super(name, new DefaultConfig()
                        .setMinRarity(SpellRarity.COMMON)
                        .setSchoolResource(ModRegistry.SCHOOL_ID_CRABS)
                        .setMaxLevel(MAX_LEVEL)
                        .setCooldownSeconds(45)
                        .setAllowCrafting(craftable)
                        .build(),
                SoundEvents.EVOKER_PREPARE_SUMMON);
        this.variant = variant;
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 1;
        this.castTime = 30;
        this.baseManaCost = 30;
    }

    public GrottolVariant getCrabVariant() {
        return variant;
    }

    /** Cada nivel pide una tinta distinta para subir: 1 comun, 2 poco comun, 3 rara, 4 epica, 5 legendaria. */
    @Override
    public SpellRarity getRarity(int level) {
        return switch (Math.max(1, Math.min(level, MAX_LEVEL))) {
            case 1 -> SpellRarity.COMMON;
            case 2 -> SpellRarity.UNCOMMON;
            case 3 -> SpellRarity.RARE;
            case 4 -> SpellRarity.EPIC;
            default -> SpellRarity.LEGENDARY;
        };
    }

    /** En la mesa de inscripcion siempre sale el nivel 1; los demas se consiguen con tinta en el yunque arcano. */
    @Override
    public int getMinLevelForRarity(SpellRarity rarity) {
        return 1;
    }

    @Override
    public int getSummonCount(int spellLevel) {
        return Math.max(1, Math.min(spellLevel, 4));
    }

    public static float healthForLevel(int spellLevel) {
        return spellLevel >= MAX_LEVEL ? Config.SUMMONED_CRAB_MAX_HEALTH.get().floatValue() : 20.0F;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.summon_count", getSummonCount(spellLevel)),
                Component.translatable("ui.irons_spellbooks.hp", Math.round(healthForLevel(spellLevel))));
    }

    @Override
    protected List<Mob> createSummons(ServerLevel level, LivingEntity caster, int spellLevel) {
        List<Mob> result = new ArrayList<>();
        int count = getSummonCount(spellLevel);
        for (int i = 0; i < count; i++) {
            GrottolVariantEntity crab = ModEntities.CRABS.get(variant).get().create(level);
            if (crab == null) {
                continue;
            }
            float health = healthForLevel(spellLevel);
            AttributeInstance maxHealth = crab.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(health);
            }
            crab.setHealth(health);
            result.add(crab);
        }
        return result;
    }
}
