package com.mutantraisedead;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * Se puede editar en: config/mutantraisedead-common.toml
 */
public final class Config {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue REQUIRED_SPELL_LEVEL;
    public static final ForgeConfigSpec.DoubleValue CHANCE_TIER_1;
    public static final ForgeConfigSpec.DoubleValue CHANCE_TIER_2;
    public static final ForgeConfigSpec.DoubleValue CHANCE_TIER_3;
    public static final ForgeConfigSpec.IntValue GUARD_LIFETIME_SECONDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ZOMBIE_MUTANTS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SKELETON_MUTANTS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("mutacion");

        REQUIRED_SPELL_LEVEL = b
                .comment("Nivel que tiene que tener Revivir Muertos en el libro para poder mutarlo en el yunque arcano (6 = maximo por defecto).")
                .defineInRange("requiredSpellLevel", 6, 1, 20);

        CHANCE_TIER_1 = b
                .comment("Nivel 1 (Quimica X de Mutant Monsters): probabilidad (0.0 a 1.0) de que CADA zombi/esqueleto invocado salga mutante.")
                .defineInRange("chanceTier1", 0.25D, 0.0D, 1.0D);
        CHANCE_TIER_2 = b
                .comment("Nivel 2 (Formula Y de Mutant More): probabilidad de que cada invocado salga mutante.")
                .defineInRange("chanceTier2", 0.50D, 0.0D, 1.0D);
        CHANCE_TIER_3 = b
                .comment("Nivel 3 (Compuesto Z de Mutant More): probabilidad de que cada invocado salga mutante.")
                .defineInRange("chanceTier3", 1.00D, 0.0D, 1.0D);

        GUARD_LIFETIME_SECONDS = b
                .comment("Cuanto tiempo duran los mutantes guardianes (en segundos). Los zombis normales de Iron's duran 600 (10 min).")
                .defineInRange("guardLifetimeSeconds", 600, 10, 86400);

        ZOMBIE_MUTANTS = b
                .comment("Mutantes que pueden reemplazar a un zombi invocado. Si hay varios, se elige uno al azar.",
                        "Para agregar otros, pone su id (por ejemplo el de un mutante de Mutant More). Se ve el id con /summon.")
                .defineListAllowEmpty("zombieMutants", List.of("mutantmonsters:mutant_zombie"), o -> o instanceof String);

        SKELETON_MUTANTS = b
                .comment("Mutantes que pueden reemplazar a un esqueleto invocado. Si hay varios, se elige uno al azar.")
                .defineListAllowEmpty("skeletonMutants", List.of("mutantmonsters:mutant_skeleton"), o -> o instanceof String);

        b.pop();

        SPEC = b.build();
    }

    private Config() {
    }

    public static double chanceForTier(int tier) {
        return switch (tier) {
            case 1 -> CHANCE_TIER_1.get();
            case 2 -> CHANCE_TIER_2.get();
            case 3 -> CHANCE_TIER_3.get();
            default -> 0.0D;
        };
    }
}
