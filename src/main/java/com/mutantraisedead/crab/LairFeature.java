package com.mutantraisedead.crab;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
        COAL, COPPER
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
