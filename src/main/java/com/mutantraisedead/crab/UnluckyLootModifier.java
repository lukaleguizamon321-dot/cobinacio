package com.mutantraisedead.crab;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mutantraisedead.Config;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Efecto Desafortunado = Fortuna al reves: cuando el jugador rompe una mena
 * (sin toque de seda), cada item que suelta tiene una probabilidad de perderse.
 */
public class UnluckyLootModifier extends LootModifier {
    public static final Supplier<Codec<UnluckyLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, UnluckyLootModifier::new)));

    public UnluckyLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity living)
                || !living.hasEffect(ModEffects.UNLUCKY.get())) {
            return generatedLoot;
        }
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null || !state.is(Tags.Blocks.ORES)) {
            return generatedLoot;
        }
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool != null && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
            return generatedLoot;
        }
        double loss = Config.UNLUCKY_LOSS_CHANCE.get();
        ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
        for (ItemStack stack : generatedLoot) {
            int kept = 0;
            for (int i = 0; i < stack.getCount(); i++) {
                if (context.getRandom().nextDouble() >= loss) {
                    kept++;
                }
            }
            if (kept > 0) {
                ItemStack copy = stack.copy();
                copy.setCount(kept);
                result.add(copy);
            }
        }
        return result;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
