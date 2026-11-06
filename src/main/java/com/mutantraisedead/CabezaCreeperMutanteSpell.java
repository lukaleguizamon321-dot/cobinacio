package com.mutantraisedead;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Cabeza de Creeper Mutante: Cabeza de Creeper (Iron's) + Quimica X.
 * Un solo nivel: invoca un unico Creeper Mutante de Mutant Monsters que te protege.
 */
public class CabezaCreeperMutanteSpell extends SummonSpellBase {
    private static final ResourceLocation MUTANT_CREEPER = ResourceLocation.fromNamespaceAndPath("mutantmonsters", "mutant_creeper");

    public CabezaCreeperMutanteSpell() {
        super("cabeza_creeper_mutante", new DefaultConfig()
                        .setMinRarity(SpellRarity.EPIC)
                        .setSchoolResource(ModRegistry.SCHOOL_ID)
                        .setMaxLevel(1)
                        .setCooldownSeconds(120)
                        .setAllowCrafting(false)
                        .build(),
                SoundEvents.CREEPER_PRIMED);
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 0;
        this.castTime = 40;
        this.baseManaCost = 70;
    }

    @Override
    public int getSummonCount(int spellLevel) {
        return 1;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.summon_count", 1));
    }

    @Override
    protected List<Mob> createSummons(ServerLevel level, LivingEntity caster, int spellLevel) {
        List<Mob> result = new ArrayList<>();
        if (!ForgeRegistries.ENTITY_TYPES.containsKey(MUTANT_CREEPER)) {
            MutantRaiseDead.LOGGER.warn("[Cobinatio] No existe mutantmonsters:mutant_creeper");
            return result;
        }
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(MUTANT_CREEPER);
        Entity created = type == null ? null : type.create(level);
        if (created instanceof Mob mob) {
            result.add(mob);
        } else if (created != null) {
            created.discard();
        }
        return result;
    }
}
