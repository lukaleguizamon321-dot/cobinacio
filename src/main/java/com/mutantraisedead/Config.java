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
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ZOMBIE_MUTANTS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SKELETON_MUTANTS;
    public static final ForgeConfigSpec.IntValue REQUIRED_ROOT_LEVEL;
    public static final ForgeConfigSpec.DoubleValue PLANT_DAMAGE;
    public static final ForgeConfigSpec.BooleanValue CRAB_ABILITIES;
    public static final ForgeConfigSpec.IntValue GIANT_CRAB_HITS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("mutacion");

        REQUIRED_SPELL_LEVEL = b
                .comment("Nivel que tiene que tener el pergamino de Revivir Muertos para poder convertirlo en el yunque arcano (6 = maximo).")
                .defineInRange("requiredSpellLevel", 6, 1, 20);

        CHANCE_TIER_1 = b
                .comment("Revive Mutantes (X), con Quimica X de Mutant Monsters: probabilidad (0.0 a 1.0) de que CADA zombi/esqueleto invocado salga mutante.")
                .defineInRange("chanceTier1", 0.25D, 0.0D, 1.0D);
        CHANCE_TIER_2 = b
                .comment("Revive Mutantes (Y), con Formula Y de Mutant More: probabilidad de que cada invocado salga mutante.")
                .defineInRange("chanceTier2", 0.50D, 0.0D, 1.0D);
        CHANCE_TIER_3 = b
                .comment("Revive Mutantes (Z), con Compuesto Z de Mutant More: probabilidad de que cada invocado salga mutante.")
                .defineInRange("chanceTier3", 1.00D, 0.0D, 1.0D);

        ZOMBIE_MUTANTS = b
                .comment("Mutantes que pueden reemplazar a un zombi invocado. Si hay varios, se elige uno al azar.",
                        "Para agregar otros, pone su id (por ejemplo el de un mutante de Mutant More).")
                .defineListAllowEmpty("zombieMutants", List.of("mutantmonsters:mutant_zombie"), o -> o instanceof String);

        SKELETON_MUTANTS = b
                .comment("Mutantes que pueden reemplazar a un esqueleto invocado. Si hay varios, se elige uno al azar.")
                .defineListAllowEmpty("skeletonMutants", List.of("mutantmonsters:mutant_skeleton"), o -> o instanceof String);

        b.pop();

        b.push("enraizar");

        REQUIRED_ROOT_LEVEL = b
                .comment("Nivel que tiene que tener el pergamino de Enraizar para poder convertirlo en el yunque arcano (10 = maximo).")
                .defineInRange("requiredRootLevel", 10, 1, 20);
        PLANT_DAMAGE = b
                .comment("Dano de cada mordisco de las plantas de Enraizar Plantas (corazones x2; 4.0 = 2 corazones).")
                .defineInRange("plantDamage", 4.0D, 0.0D, 100.0D);

        b.pop();

        b.push("cangrejos");

        CRAB_ABILITIES = b
                .comment("Si es false, los cangrejos de carbon, lapislazuli y cobre no atacan a distancia (fuego, rocas, rayos).")
                .define("abilitiesEnabled", true);
        GIANT_CRAB_HITS = b
                .comment("Cuantos golpes de pico hacen falta para matar al cangrejo gigante de hierro.")
                .defineInRange("giantCrabHits", 20, 1, 500);

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
