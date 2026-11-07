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
    public static final ForgeConfigSpec.IntValue DETECTED_SECONDS;
    public static final ForgeConfigSpec.IntValue DETECTED_RANGE;
    public static final ForgeConfigSpec.IntValue UNLUCKY_SECONDS;
    public static final ForgeConfigSpec.DoubleValue UNLUCKY_LOSS_CHANCE;
    public static final ForgeConfigSpec.IntValue EMERALD_RANGE;
    public static final ForgeConfigSpec.IntValue EMERALD_DISCOUNT;
    public static final ForgeConfigSpec.BooleanValue DIAMOND_ATTACKS;
    public static final ForgeConfigSpec.IntValue REQUIRED_CRAB_LEVEL;
    public static final ForgeConfigSpec.DoubleValue SUMMONED_CRAB_MAX_HEALTH;
    public static final ForgeConfigSpec.IntValue REQUIRED_CREEPER_LEVEL;
    public static final ForgeConfigSpec.IntValue REQUIRED_BLAZE_LEVEL;
    public static final ForgeConfigSpec.DoubleValue BLAZE_CHANCE_X;
    public static final ForgeConfigSpec.DoubleValue BLAZE_CHANCE_Y;
    public static final ForgeConfigSpec.IntValue MAX_MUTANT_BLAZES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLAZE_MUTANTS;
    public static final ForgeConfigSpec.BooleanValue DOUBLE_STORAGE_OVERFLOW;

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

        b.push("invocaciones");

        REQUIRED_CRAB_LEVEL = b
                .comment("Invocar Cangrejo: nivel que tiene que tener el pergamino para convertirlo con un bloque de cobre (carbon a cobre) o de hierro (cobre a hierro). 5 = maximo.")
                .defineInRange("requiredCrabLevel", 5, 1, 5);
        SUMMONED_CRAB_MAX_HEALTH = b
                .comment("Invocar Cangrejo nivel 5 (tinta legendaria): vida de cada cangrejo invocado (20 es la vida normal).")
                .defineInRange("crabLevel5Health", 25.0D, 1.0D, 1000.0D);
        REQUIRED_CREEPER_LEVEL = b
                .comment("Cabeza de Creeper Mutante: nivel que tiene que tener el pergamino de Cabeza de Creeper para convertirlo con la Quimica X (10 = maximo).")
                .defineInRange("requiredCreeperLevel", 10, 1, 20);
        REQUIRED_BLAZE_LEVEL = b
                .comment("Invocar Blaze Mutante: nivel que tiene que tener el pergamino de Invocar Blaze para convertirlo con la Quimica X o la Formula Y (2 = maximo).")
                .defineInRange("requiredBlazeLevel", 2, 1, 2);
        BLAZE_CHANCE_X = b
                .comment("Invocar Blaze Mutante (X): probabilidad (0.0 a 1.0) de que cada blaze invocado salga mutante.")
                .defineInRange("blazeChanceX", 0.25D, 0.0D, 1.0D);
        BLAZE_CHANCE_Y = b
                .comment("Invocar Blaze Mutante (Y): probabilidad de que cada blaze invocado salga mutante.")
                .defineInRange("blazeChanceY", 0.50D, 0.0D, 1.0D);
        MAX_MUTANT_BLAZES = b
                .comment("Maximo de blazes mutantes por lanzamiento. Si salen mas, el resto queda como blaze normal.")
                .defineInRange("maxMutantBlazes", 2, 0, 4);
        BLAZE_MUTANTS = b
                .comment("Mutantes que pueden reemplazar a un blaze invocado (Mutant More). Si el id no existe, sale un blaze normal.")
                .defineListAllowEmpty("blazeMutants", List.of("mutantmore:mutant_blaze"), o -> o instanceof String);

        b.pop();

        b.push("doble_almacen");

        DOUBLE_STORAGE_OVERFLOW = b
                .comment("Doble Almacen: si es true, cuando tu inventario esta lleno lo que recojas del suelo se guarda solo en el amuleto.")
                .define("overflowPickup", true);

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
        DETECTED_SECONDS = b
                .comment("Cangrejo de redstone: segundos que dura el efecto Detectado (300 = 5 minutos).")
                .defineInRange("detectedSeconds", 300, 1, 36000);
        DETECTED_RANGE = b
                .comment("Cangrejo de redstone: a cuantos bloques los monstruos te detectan, incluso a traves de paredes.")
                .defineInRange("detectedRange", 25, 1, 128);
        UNLUCKY_SECONDS = b
                .comment("Cangrejo de diamante: segundos que dura el efecto Desafortunado al matarlo (180 = 3 minutos).")
                .defineInRange("unluckySeconds", 180, 1, 36000);
        UNLUCKY_LOSS_CHANCE = b
                .comment("Con Desafortunado, probabilidad (0.0 a 1.0) de que se pierda cada item que suelta una mena.")
                .defineInRange("unluckyLossChance", 0.5D, 0.0D, 1.0D);
        EMERALD_RANGE = b
                .comment("Cangrejo de esmeralda: a cuantos bloques de un aldeano le baja los precios.")
                .defineInRange("emeraldRange", 10, 1, 64);
        EMERALD_DISCOUNT = b
                .comment("Cangrejo de esmeralda: porcentaje maximo de descuento en los precios de los aldeanos.")
                .defineInRange("emeraldDiscountPercent", 25, 0, 90);
        DIAMOND_ATTACKS = b
                .comment("Cangrejo de diamante: si es true ataca al jugador que se le acerca; si es false se queda quieto y solo da Desafortunado al morir.")
                .define("diamondCrabAttacks", true);

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
