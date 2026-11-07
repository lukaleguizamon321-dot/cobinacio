package com.mutantraisedead.crab;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * Agrega un item al botin con cierta probabilidad. Se usa para que el Doble Almacen salga en los mimicos
 * y en los cofres de estructuras. "tables" es la lista de botines donde se agrega.
 */
public class AddItemLootModifier extends LootModifier {
    public static final Supplier<Codec<AddItemLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst).and(inst.group(
                    ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item),
                    Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance),
                    ResourceLocation.CODEC.listOf().fieldOf("tables").forGetter(m -> m.tables)
            )).apply(inst, AddItemLootModifier::new)));

    private final Item item;
    private final float chance;
    private final List<ResourceLocation> tables;

    public AddItemLootModifier(LootItemCondition[] conditions, Item item, float chance, List<ResourceLocation> tables) {
        super(conditions);
        this.item = item;
        this.chance = chance;
        this.tables = tables;
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!tables.contains(context.getQueriedLootTableId())) {
            return generatedLoot;
        }
        if (context.getRandom().nextFloat() < chance) {
            generatedLoot.add(new ItemStack(item));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
