package com.mutantraisedead.crab;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Genera un cubo de 2x2x2 de "mena de hierro" dormida dentro de la piedra. */
public class NestFeature extends Feature<NoneFeatureConfiguration> {
    public NestFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        for (int dx = 0; dx < 2; dx++) {
            for (int dy = 0; dy < 2; dy++) {
                for (int dz = 0; dz < 2; dz++) {
                    if (!level.getBlockState(origin.offset(dx, dy, dz)).is(BlockTags.BASE_STONE_OVERWORLD)) {
                        return false;
                    }
                }
            }
        }
        for (int dx = 0; dx < 2; dx++) {
            for (int dy = 0; dy < 2; dy++) {
                for (int dz = 0; dz < 2; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState replaced = level.getBlockState(pos);
                    BlockState nest = replaced.is(Blocks.DEEPSLATE)
                            ? ModBlocks.DEEPSLATE_NEST.get().defaultBlockState()
                            : ModBlocks.NEST.get().defaultBlockState();
                    level.setBlock(pos, nest, 2);
                }
            }
        }
        return true;
    }
}
