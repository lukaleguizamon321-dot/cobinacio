package com.mutantraisedead.crab;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Guarida de los cangrejos: una sala cilindrica cerrada, enterrada en las cuevas como un dungeon.
 *
 * Medidas (v1.14): radio interior ~15,5 bloques, 14 bloques de alto (del suelo al techo), pared de 2 bloques.
 * Es grande a proposito: el jefe de carbon mide 6 de alto y con las patas abiertas ocupa unos 12 de ancho,
 * y la pelea necesita lugar para correr y esquivar la embestida.
 * En el centro hay un pedestal con el huevo del jefe y en la pared hay un nido
 * (cubo de 2x2x2) para cada material: carbon, hierro, cobre, oro, lapislazuli, amatista, redstone,
 * diamante y esmeralda. Entre los nidos hay pilares con un farol arriba.
 *
 * Tiene una puerta de 5 de ancho por 6 de alto en uno de los cuatro lados; del otro lado de la pared
 * hay roca, asi que hay que cavar (o cruzar una cueva) para llegar a ella.
 * El origen (context.origin()) es el centro; el primer bloque de aire del interior esta a la altura del origen.
 *
 * Al generarse con el mundo, el centro se mueve al medio del chunk: Minecraft solo deja escribir bloques hasta
 * 16 bloques fuera del chunk que se esta generando, y la sala llega a 17,5 desde el centro.
 *
 * v1.16: hay dos temas. COAL es la guarida de piedra de siempre (jefe de carbon, un nido de cada material).
 * COPPER es la guarida del Cangrejo Titan de Cobre: misma sala pero toda de cobre (cobre cortado encerado, con tramos
 * oxidados y verdosos), con el huevo de cobre en el pedestal, los 9 nidos de cangrejo de cobre, pilares con luz verde
 * y pararrayos, y 4 columnas de cobre con pararrayos en la sala (el jefe se aturde si se las lleva por delante en una embestida).
 *
 * v1.18: AMETHYST es la guarida del Cangrejo Titan de Amatista: una geoda gigante aplastada (mas ancha que alta, ovalada).
 * Interior de 19 x 16 bloques de radio y 13 de alto en el centro, con piso plano; la cascara tiene 3 capas como una geoda
 * de verdad (amatista adentro, calcita, basalto liso afuera), cristales (racimos de amatista) pegados a las paredes,
 * al piso y al techo, y pilares de cristal con luz. Hasta 22 bloques del centro: entra en lo que Minecraft deja escribir
 * al generar (el centro queda en el medio del chunk).
 */
public class LairFeature extends Feature<NoneFeatureConfiguration> {
    /** Alto del interior en bloques. */
    public static final int HEIGHT = 14;
    /** Distancia al cuadrado (dx*dx + dz*dz) hasta donde llega el interior: radio ~15,5. */
    private static final int INTERIOR_D2 = 240;
    /** Hasta donde llega la pared (radio ~17,5). */
    private static final int SHELL_D2 = 306;
    private static final int REACH = 18;
    /** Radio al que quedan los nidos y los pilares (pegados a la pared). */
    private static final double NEST_RADIUS = 15.4D;
    private static final double PILLAR_RADIUS = 14.3D;

    /** Para que jefe es la guarida. */
    public enum Theme {
        COAL, COPPER, AMETHYST
    }

    /** Orden de los nidos alrededor de la sala (guarida de carbon: uno de cada material). */
    private static final GrottolVariant[] NEST_ORDER = {
            GrottolVariant.COAL, GrottolVariant.IRON, GrottolVariant.COPPER,
            GrottolVariant.GOLD, GrottolVariant.LAPIS, GrottolVariant.AMETHYST,
            GrottolVariant.REDSTONE, GrottolVariant.DIAMOND, GrottolVariant.EMERALD
    };

    /**
     * Bloques con los que se arma la sala (de piedra arriba, de pizarra profunda abajo de Y=0, o de cobre).
     * light = lo que se pone arriba de cada pilar; topExtra = lo que va arriba de la luz (aire si no hay nada).
     */
    private record Palette(BlockState brick, BlockState cracked, BlockState alt, BlockState rubble,
                           BlockState inner, BlockState ring, BlockState chiseled, BlockState pillar,
                           BlockState light, BlockState topExtra) {
    }

    private final Theme theme;

    public LairFeature(Codec<NoneFeatureConfiguration> codec, Theme theme) {
        super(codec);
        this.theme = theme;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos o = context.origin();
        // al medio del chunk: asi la sala (hasta 17,5 bloques) nunca pasa del limite de escritura (16 bloques fuera del chunk)
        BlockPos center = new BlockPos((o.getX() >> 4 << 4) + 8, o.getY(), (o.getZ() >> 4 << 4) + 8);
        return build(context.level(), center, context.random(), false, theme);
    }

    private static Palette palette(Theme theme, boolean deep) {
        if (theme == Theme.COPPER) {
            return new Palette(
                    Blocks.WAXED_CUT_COPPER.defaultBlockState(),
                    Blocks.WAXED_EXPOSED_CUT_COPPER.defaultBlockState(),
                    Blocks.WAXED_WEATHERED_CUT_COPPER.defaultBlockState(),
                    Blocks.WAXED_OXIDIZED_CUT_COPPER.defaultBlockState(),
                    Blocks.WAXED_CUT_COPPER.defaultBlockState(),
                    Blocks.WAXED_OXIDIZED_CUT_COPPER.defaultBlockState(),
                    Blocks.WAXED_COPPER_BLOCK.defaultBlockState(),
                    Blocks.WAXED_EXPOSED_CUT_COPPER.defaultBlockState(),
                    Blocks.VERDANT_FROGLIGHT.defaultBlockState(),
                    Blocks.LIGHTNING_ROD.defaultBlockState());
        }
        if (deep) {
            return new Palette(
                    Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                    Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(),
                    Blocks.DEEPSLATE_TILES.defaultBlockState(),
                    Blocks.COBBLED_DEEPSLATE.defaultBlockState(),
                    Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                    Blocks.DEEPSLATE_TILES.defaultBlockState(),
                    Blocks.CHISELED_DEEPSLATE.defaultBlockState(),
                    Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                    Blocks.SOUL_LANTERN.defaultBlockState(),
                    Blocks.AIR.defaultBlockState());
        }
        return new Palette(
                Blocks.STONE_BRICKS.defaultBlockState(),
                Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
                Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                Blocks.COBBLESTONE.defaultBlockState(),
                Blocks.POLISHED_ANDESITE.defaultBlockState(),
                Blocks.SMOOTH_STONE.defaultBlockState(),
                Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),
                Blocks.POLISHED_ANDESITE.defaultBlockState(),
                Blocks.LANTERN.defaultBlockState(),
                Blocks.AIR.defaultBlockState());
    }

    /**
     * Construye la guarida. Si force es true (comando de prueba) no se fija si esta enterrada ni si hay agua.
     * Devuelve false si el lugar no sirve.
     */
    public static boolean build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean force) {
        return build(level, origin, random, force, Theme.COAL);
    }

    /** Igual que arriba, pero eligiendo para que jefe es la guarida (Theme.COAL o Theme.COPPER). */
    public static boolean build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean force, Theme theme) {
        if (theme == Theme.AMETHYST) {
            return buildGeode(level, origin, random, force);
        }
        int y0 = origin.getY();
        if (y0 - 3 < level.getMinBuildHeight() || y0 + HEIGHT + 3 >= level.getMaxBuildHeight()) {
            return false;
        }
        if (!force && !isGoodSpot(level, origin)) {
            return false;
        }

        boolean copper = theme == Theme.COPPER;
        Palette p = palette(theme, y0 < 0);
        int cx = origin.getX();
        int cz = origin.getZ();

        // 1) Pared, piso, techo y el interior vacio
        for (int dx = -REACH; dx <= REACH; dx++) {
            for (int dz = -REACH; dz <= REACH; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > SHELL_D2) {
                    continue;
                }
                boolean inside = d2 <= INTERIOR_D2;
                set(level, cx + dx, y0 - 2, cz + dz, wall(p, random));
                set(level, cx + dx, y0 - 1, cz + dz, floor(p, d2, random));
                set(level, cx + dx, y0 + HEIGHT, cz + dz, wall(p, random));
                set(level, cx + dx, y0 + HEIGHT + 1, cz + dz, wall(p, random));
                for (int dy = 0; dy < HEIGHT; dy++) {
                    set(level, cx + dx, y0 + dy, cz + dz, inside ? Blocks.AIR.defaultBlockState() : wall(p, random));
                }
            }
        }

        // 2) Pedestal en el centro
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 2) {
                    set(level, cx + dx, y0, cz + dz, p.ring());
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(level, cx + dx, y0 + 1, cz + dz, p.inner());
            }
        }
        set(level, cx, y0 + 2, cz, p.chiseled());
        // El huevo del jefe (de carbon o de cobre) va arriba del pedestal; al acercarte despierta (ver CrabEggBlock)
        Block egg = copper ? ModBlocks.COPPER_EGG.get() : ModBlocks.COAL_EGG.get();
        set(level, cx, y0 + 3, cz, egg.defaultBlockState().setValue(CrabEggBlock.PEDESTAL, true));

        // Guarida de cobre: 4 columnas con pararrayos en las diagonales de la sala (cubren de los rayos y frenan la embestida)
        if (copper) {
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    int colX = cx + sx * 7;
                    int colZ = cz + sz * 7;
                    for (int dy = 0; dy < 7; dy++) {
                        set(level, colX, y0 + dy, colZ, dy == 0 ? p.chiseled() : p.pillar());
                    }
                    set(level, colX, y0 + 7, colZ, p.light());
                    set(level, colX, y0 + 8, colZ, p.topExtra());
                }
            }
        }

        // 3) Puerta de 5x6 hacia uno de los cuatro lados (atraviesa la pared)
        int doorQuarter = random.nextInt(4);
        int ux = doorQuarter == 0 ? 1 : doorQuarter == 1 ? -1 : 0;
        int uz = doorQuarter == 2 ? 1 : doorQuarter == 3 ? -1 : 0;
        for (int t = 14; t <= REACH; t++) {
            for (int lateral = -2; lateral <= 2; lateral++) {
                int dx = ux * t - uz * lateral;
                int dz = uz * t + ux * lateral;
                if (dx * dx + dz * dz > SHELL_D2) {
                    continue;
                }
                for (int dy = 0; dy < 6; dy++) {
                    set(level, cx + dx, y0 + dy, cz + dz, Blocks.AIR.defaultBlockState());
                }
            }
        }

        // 4) Nidos en la pared, con un pilar con farol entre cada par. La puerta queda en el hueco que sobra.
        double doorAngle = Math.atan2(uz, ux);
        for (int k = 0; k < NEST_ORDER.length; k++) {
            // guarida de cobre: los 9 nidos son de cangrejo de cobre
            GrottolVariant variant = copper ? GrottolVariant.COPPER : NEST_ORDER[k];
            BlockState nest = variant == GrottolVariant.IRON && y0 < 0
                    ? ModBlocks.DEEPSLATE_NEST.get().defaultBlockState()
                    : ModBlocks.lairNest(variant).defaultBlockState();
            double nestAngle = doorAngle + Math.toRadians(20 + 40 * k);
            // el cubo de 2x2x2 queda pegado a la pared, un poco hacia adentro para verse
            int ox = cx + (int) Math.round(Math.cos(nestAngle) * NEST_RADIUS - 0.5D);
            int oz = cz + (int) Math.round(Math.sin(nestAngle) * NEST_RADIUS - 0.5D);
            for (int a = 0; a < 2; a++) {
                for (int b = 0; b < 2; b++) {
                    for (int c = 0; c < 2; c++) {
                        set(level, ox + a, y0 + c, oz + b, nest);
                    }
                }
            }

            if (k == NEST_ORDER.length - 1) {
                continue; // ahi esta la puerta
            }
            double pillarAngle = nestAngle + Math.toRadians(20);
            int px = cx + (int) Math.round(Math.cos(pillarAngle) * PILLAR_RADIUS);
            int pz = cz + (int) Math.round(Math.sin(pillarAngle) * PILLAR_RADIUS);
            for (int dy = 0; dy < HEIGHT - 2; dy++) {
                set(level, px, y0 + dy, pz, dy == 0 || dy == HEIGHT - 3 ? p.chiseled() : p.pillar());
            }
            set(level, px, y0 + HEIGHT - 2, pz, p.light());
            set(level, px, y0 + HEIGHT - 1, pz, p.topExtra());
        }
        return true;
    }


    // ------------------------------------------------------------------
    // Guarida geoda (Cangrejo Titan de Amatista, v1.18)
    // ------------------------------------------------------------------

    /** Semiejes del interior de la geoda: ancho (X), fondo (Z) y alto (arriba del piso). Mas ancha que alta, como una geoda aplastada. */
    private static final int GEO_A = 19;
    private static final int GEO_B = 16;
    private static final int GEO_C = 13;
    /** Capas de la cascara, de adentro para afuera: 1 amatista, 2 calcita, 3 basalto liso. */
    private static final int GEO_SHELL = 3;
    /** Numero de capa del piso (es de amatista y calcita mezcladas; abajo tiene calcita y basalto liso). */
    private static final int LAYER_FLOOR = 4;
    /** Hasta donde se escribe, a cada lado del centro (Minecraft deja escribir hasta 23 bloques desde el medio del chunk). */
    private static final int GEO_REACH_X = GEO_A + GEO_SHELL;
    private static final int GEO_REACH_Z = GEO_B + GEO_SHELL + 1;

    /** Que tan adentro del elipsoide esta la celda (<= 1 es adentro); grow agranda los semiejes para medir cada capa de la cascara. */
    private static double geoE(int dx, int dy, int dz, int grow) {
        double a = GEO_A + grow;
        double b = GEO_B + grow;
        double c = GEO_C + grow;
        return (dx / a) * (dx / a) + (dz / b) * (dz / b) + (dy / c) * (dy / c);
    }

    /** Deformacion chiquita (+-3%) para que la pared no sea un elipsoide perfecto. Cuadraditos de 2x2x2. */
    private static double geoRough(int x, int y, int z, int seed) {
        long h = seed + (x >> 1) * 341873128712L + (y >> 1) * 132897987541L + (z >> 1) * 42317861L;
        h ^= h >>> 13;
        h *= 0x5DEECE66DL;
        h ^= h >>> 17;
        return (((h >>> 8) & 1023L) / 511.5D - 1.0D) * 0.06D;
    }

    /**
     * A que capa pertenece la celda (dx, dy, dz) respecto del centro (dy = 0 es el primer bloque de aire arriba del piso):
     * 0 = interior vacio, 1 = amatista, 2 = calcita, 3 = basalto liso, 4 = piso, -1 = fuera de la geoda.
     */
    private static int geoLayer(int dx, int dy, int dz, int seed) {
        double n = 1.0D + geoRough(dx, dy, dz, seed);
        if (dy >= 0) {
            for (int g = 0; g <= GEO_SHELL; g++) {
                if (geoE(dx, dy, dz, g) * n <= 1.0D) {
                    return g;
                }
            }
            return -1;
        }
        if (dy < -GEO_SHELL) {
            return -1;
        }
        // por debajo del piso: losa plana (piso, calcita, basalto) y la cascara que sigue bajando
        for (int g = 0; g <= GEO_SHELL; g++) {
            double a = GEO_A + g;
            double b = GEO_B + g;
            if (((dx / a) * (dx / a) + (dz / b) * (dz / b)) * n <= 1.0D) {
                if (g > 0) {
                    return g;
                }
                return dy == -1 ? LAYER_FLOOR : (dy == -2 ? 2 : 3);
            }
        }
        return -1;
    }

    private static BlockState geoBlock(int layer, RandomSource random) {
        switch (layer) {
            case 0:
                return Blocks.AIR.defaultBlockState();
            case 1:
                return random.nextInt(100) < 12 ? Blocks.BUDDING_AMETHYST.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState();
            case 2:
                return Blocks.CALCITE.defaultBlockState();
            case 3:
                return Blocks.SMOOTH_BASALT.defaultBlockState();
            default: {
                int roll = random.nextInt(100);
                if (roll < 55) {
                    return Blocks.AMETHYST_BLOCK.defaultBlockState();
                }
                return roll < 80 ? Blocks.CALCITE.defaultBlockState() : Blocks.SMOOTH_BASALT.defaultBlockState();
            }
        }
    }

    private static BlockState geoCluster(RandomSource random, Direction facing) {
        int roll = random.nextInt(100);
        Block block = roll < 35 ? Blocks.AMETHYST_CLUSTER
                : roll < 60 ? Blocks.LARGE_AMETHYST_BUD
                : roll < 80 ? Blocks.MEDIUM_AMETHYST_BUD
                : Blocks.SMALL_AMETHYST_BUD;
        return block.defaultBlockState().setValue(BlockStateProperties.FACING, facing);
    }

    /** Arma la geoda alrededor de origin (el centro: arriba del piso). Devuelve false si el lugar no sirve. */
    private static boolean buildGeode(WorldGenLevel level, BlockPos origin, RandomSource random, boolean force) {
        int y0 = origin.getY();
        if (y0 - GEO_SHELL - 1 < level.getMinBuildHeight() || y0 + GEO_C + GEO_SHELL + 3 >= level.getMaxBuildHeight()) {
            return false;
        }
        if (!force && !isGoodGeodeSpot(level, origin)) {
            return false;
        }
        int cx = origin.getX();
        int cz = origin.getZ();
        int seed = random.nextInt();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1) La cascara (amatista, calcita, basalto liso), el piso y el interior vacio
        for (int dx = -GEO_REACH_X; dx <= GEO_REACH_X; dx++) {
            for (int dz = -GEO_REACH_Z; dz <= GEO_REACH_Z; dz++) {
                for (int dy = -GEO_SHELL; dy <= GEO_C + GEO_SHELL; dy++) {
                    int layer = geoLayer(dx, dy, dz, seed);
                    if (layer >= 0) {
                        set(level, cx + dx, y0 + dy, cz + dz, geoBlock(layer, random));
                    }
                }
            }
        }

        // 2) Cristales pegados a las paredes, al techo y al piso (menos alrededor del pedestal)
        for (int dx = -GEO_A; dx <= GEO_A; dx++) {
            for (int dz = -GEO_B; dz <= GEO_B; dz++) {
                if (dx * dx + dz * dz <= 20) {
                    continue;
                }
                for (int dy = 0; dy <= GEO_C; dy++) {
                    if (geoLayer(dx, dy, dz, seed) != 0 || random.nextInt(100) >= 10) {
                        continue;
                    }
                    Direction support = null;
                    int found = 0;
                    for (Direction d : Direction.values()) {
                        int neighbor = geoLayer(dx + d.getStepX(), dy + d.getStepY(), dz + d.getStepZ(), seed);
                        if (neighbor == 1 || neighbor == LAYER_FLOOR) {
                            found++;
                            if (random.nextInt(found) == 0) {
                                support = d;
                            }
                        }
                    }
                    if (support != null) {
                        set(level, cx + dx, y0 + dy, cz + dz, geoCluster(random, support.getOpposite()));
                    }
                }
            }
        }

        // 3) Pedestal en el centro con el huevo del jefe (al acercarte despierta, ver CrabEggBlock)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 2) {
                    set(level, cx + dx, y0, cz + dz, Blocks.AMETHYST_BLOCK.defaultBlockState());
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                set(level, cx + dx, y0 + 1, cz + dz, Blocks.CALCITE.defaultBlockState());
            }
        }
        set(level, cx, y0 + 2, cz, Blocks.SMOOTH_BASALT.defaultBlockState());
        set(level, cx, y0 + 3, cz, ModBlocks.AMETHYST_EGG.get().defaultBlockState().setValue(CrabEggBlock.PEDESTAL, true));
        // lo que estaba arriba del pedestal tiene que estar vacio
        for (int dy = 4; dy <= 7; dy++) {
            set(level, cx, y0 + dy, cz, air);
        }

        // 4) Puerta de 5x6 hacia uno de los cuatro lados (solo atraviesa la cascara; del otro lado hay roca)
        int doorQuarter = random.nextInt(4);
        int ux = doorQuarter == 0 ? 1 : doorQuarter == 1 ? -1 : 0;
        int uz = doorQuarter == 2 ? 1 : doorQuarter == 3 ? -1 : 0;
        int semi = ux != 0 ? GEO_A : GEO_B;
        for (int t = semi - 2; t <= semi + GEO_SHELL + 1; t++) {
            for (int lateral = -2; lateral <= 2; lateral++) {
                int dx = ux * t - uz * lateral;
                int dz = uz * t + ux * lateral;
                for (int dy = 0; dy < 6; dy++) {
                    if (geoLayer(dx, dy, dz, seed) >= 0) {
                        set(level, cx + dx, y0 + dy, cz + dz, air);
                    }
                }
            }
        }

        // 5) Un nido de cangrejo de amatista por cada uno de los 9 que hay (todos de amatista), pegados a la pared,
        //    y pilares de cristal con luz entre ellos. La puerta queda en el hueco que sobra.
        double doorParam = Math.atan2(uz, ux);
        BlockState nest = ModBlocks.lairNest(GrottolVariant.AMETHYST).defaultBlockState();
        for (int k = 0; k < NEST_ORDER.length; k++) {
            double t = doorParam + Math.toRadians(20 + 40 * k);
            int ox = cx + (int) Math.round((GEO_A - 1.4D) * Math.cos(t) - 0.5D);
            int oz = cz + (int) Math.round((GEO_B - 1.4D) * Math.sin(t) - 0.5D);
            for (int a = 0; a < 2; a++) {
                for (int b = 0; b < 2; b++) {
                    for (int c = 0; c < 2; c++) {
                        set(level, ox + a, y0 + c, oz + b, nest);
                    }
                }
            }
            if (k == NEST_ORDER.length - 1) {
                continue; // ahi esta la puerta
            }
            double pillarParam = t + Math.toRadians(20);
            int px = (int) Math.round(GEO_A * 0.78D * Math.cos(pillarParam));
            int pz = (int) Math.round(GEO_B * 0.78D * Math.sin(pillarParam));
            // hasta donde llega el aire en esa columna (la geoda es mas baja hacia los costados)
            int hmax = -1;
            for (int dy = 0; dy <= GEO_C; dy++) {
                if (geoLayer(px, dy, pz, seed) == 0) {
                    hmax = dy;
                } else {
                    break;
                }
            }
            if (hmax < 6) {
                continue;
            }
            int height = Math.max(3, Math.min(7, hmax - 3));
            for (int dy = 0; dy < height; dy++) {
                set(level, cx + px, y0 + dy, cz + pz, dy < 2 ? Blocks.AMETHYST_BLOCK.defaultBlockState() : Blocks.CALCITE.defaultBlockState());
            }
            set(level, cx + px, y0 + height, cz + pz, Blocks.CRYING_OBSIDIAN.defaultBlockState());
            set(level, cx + px, y0 + height + 1, cz + pz, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
        }
        return true;
    }

    /** Enterrada (al menos 4 bloques de roca arriba de la cascara), sin agua ni lava adentro y con bastante roca alrededor. */
    private static boolean isGoodGeodeSpot(WorldGenLevel level, BlockPos origin) {
        int y0 = origin.getY();
        int needed = y0 + GEO_C + GEO_SHELL + 4;
        int[][] probes = {{0, 0}, {GEO_A, 0}, {-GEO_A, 0}, {0, GEO_B}, {0, -GEO_B}, {13, 10}, {13, -10}, {-13, 10}, {-13, -10}};
        for (int[] probe : probes) {
            if (level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX() + probe[0], origin.getZ() + probe[1]) < needed) {
                return false;
            }
        }
        int total = 0;
        int solid = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -GEO_A; dx <= GEO_A; dx++) {
            for (int dz = -GEO_B; dz <= GEO_B; dz++) {
                for (int dy = -1; dy <= GEO_C; dy++) {
                    if (geoE(dx, Math.max(dy, 0), dz, 0) > 1.0D) {
                        continue;
                    }
                    pos.set(origin.getX() + dx, y0 + dy, origin.getZ() + dz);
                    if (!level.getFluidState(pos).isEmpty()) {
                        return false;
                    }
                    total++;
                    if (!level.getBlockState(pos).isAir()) {
                        solid++;
                    }
                }
            }
        }
        return solid * 100 >= total * 35;
    }

    /** Enterrada (al menos 4 bloques de roca arriba), sin agua ni lava adentro y con bastante roca alrededor. */
    private static boolean isGoodSpot(WorldGenLevel level, BlockPos origin) {
        int y0 = origin.getY();
        int needed = y0 + HEIGHT + 5;
        int[][] probes = {{0, 0}, {16, 0}, {-16, 0}, {0, 16}, {0, -16}, {11, 11}, {11, -11}, {-11, 11}, {-11, -11}};
        for (int[] probe : probes) {
            if (level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX() + probe[0], origin.getZ() + probe[1]) < needed) {
                return false;
            }
        }
        int total = 0;
        int solid = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -REACH; dx <= REACH; dx++) {
            for (int dz = -REACH; dz <= REACH; dz++) {
                if (dx * dx + dz * dz > INTERIOR_D2) {
                    continue;
                }
                for (int dy = -1; dy <= HEIGHT; dy++) {
                    pos.set(origin.getX() + dx, y0 + dy, origin.getZ() + dz);
                    if (!level.getFluidState(pos).isEmpty()) {
                        return false;
                    }
                    total++;
                    if (!level.getBlockState(pos).isAir()) {
                        solid++;
                    }
                }
            }
        }
        return solid * 100 >= total * 35;
    }

    private static BlockState wall(Palette p, RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 55) {
            return p.brick();
        }
        if (roll < 75) {
            return p.cracked();
        }
        if (roll < 90) {
            return p.alt();
        }
        return p.rubble();
    }

    private static BlockState floor(Palette p, int d2, RandomSource random) {
        if (d2 <= 110) {
            return d2 >= 45 && d2 <= 68 ? p.ring() : p.inner();
        }
        return wall(p, random);
    }

    private static void set(WorldGenLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, 2);
    }
}
