package com.mutantraisedead;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;

/**
 * Invocar Blaze (fuego). Nivel 1 = 2 blazes (tinta epica), nivel 2 = 4 blazes (tinta legendaria).
 * Las versiones mutantes (Quimica X = 25 %, Formula Y = 50 %) pueden convertir blazes en Blaze Mutante
 * de Mutant More, con un maximo de 2 mutantes por lanzamiento.
 */
public class InvocarBlazeSpell extends SummonSpellBase {
    public static final int MAX_LEVEL = 2;

    /** 0 = normal, 1 = mutante (X), 2 = mutante (Y). */
    private final int mutantTier;

    public InvocarBlazeSpell(int mutantTier, String name) {
        super(name, new DefaultConfig()
                        .setMinRarity(SpellRarity.EPIC)
                        .setSchoolResource(mutantTier == 0
                                ? io.redspace.ironsspellbooks.api.registry.SchoolRegistry.FIRE_RESOURCE
                                : ModRegistry.SCHOOL_ID)
                        .setMaxLevel(MAX_LEVEL)
                        .setCooldownSeconds(60)
                        .setAllowCrafting(mutantTier == 0)
                        .build(),
                SoundEvents.BLAZE_AMBIENT);
        this.mutantTier = mutantTier;
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 1;
        this.castTime = 40;
        this.baseManaCost = 50;
    }

    public int getMutantTier() {
        return mutantTier;
    }

    /** Nivel 1 = tinta epica, nivel 2 = tinta legendaria. */
    @Override
    public SpellRarity getRarity(int level) {
        return level >= 2 ? SpellRarity.LEGENDARY : SpellRarity.EPIC;
    }

    @Override
    public int getMinLevelForRarity(SpellRarity rarity) {
        return switch (rarity) {
            case EPIC -> 1;
            case LEGENDARY -> 2;
            default -> 0;
        };
    }

    @Override
    public int getSummonCount(int spellLevel) {
        return spellLevel >= 2 ? 4 : 2;
    }

    private double chance() {
        return switch (mutantTier) {
            case 1 -> Config.BLAZE_CHANCE_X.get();
            case 2 -> Config.BLAZE_CHANCE_Y.get();
            default -> 0.0D;
        };
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        List<MutableComponent> info = new ArrayList<>();
        info.add(Component.translatable("ui.irons_spellbooks.summon_count", getSummonCount(spellLevel)));
        if (mutantTier > 0) {
            info.add(Component.translatable("ui.mutantraisedead.mutant_chance", (int) Math.round(chance() * 100.0D)));
            info.add(Component.translatable("ui.mutantraisedead.max_mutants", Config.MAX_MUTANT_BLAZES.get()));
        }
        return info;
    }

    @Override
    protected List<Mob> createSummons(ServerLevel level, LivingEntity caster, int spellLevel) {
        int count = getSummonCount(spellLevel);

        // Quienes sacaron mutante en la tirada; si son mas del maximo, el juego elige cuales se quedan normales.
        List<Integer> rolled = new ArrayList<>();
        if (mutantTier > 0 && caster instanceof ServerPlayer) {
            for (int i = 0; i < count; i++) {
                if (level.getRandom().nextDouble() < chance()) {
                    rolled.add(i);
                }
            }
            int max = Config.MAX_MUTANT_BLAZES.get();
            while (rolled.size() > max) {
                rolled.remove(level.getRandom().nextInt(rolled.size()));
            }
        }

        List<Mob> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Mob blaze = null;
            if (rolled.contains(i)) {
                blaze = SummonUtil.createFromIds(level, Config.BLAZE_MUTANTS.get());
            }
            if (blaze == null) {
                blaze = EntityType.BLAZE.create(level);
            }
            if (blaze != null) {
                result.add(blaze);
            }
        }
        return result;
    }
}
