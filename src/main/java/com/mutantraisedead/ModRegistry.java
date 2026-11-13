package com.mutantraisedead;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.damage.ISSDamageTypes;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import com.mutantraisedead.crab.GrottolVariant;
import io.redspace.ironsspellbooks.util.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Todo lo que el mod registra: una escuela propia (solo para que el pergamino tenga su icono),
 * los tres hechizos y la pestana del modo creativo.
 */
public final class ModRegistry {
    public static final ResourceLocation SCHOOL_ID =
            ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "revive_mutantes");

    public static final ResourceLocation SCHOOL_ID_PLANTS =
            ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "enraizar_plantas");

    /** Escuela de Invocar Cangrejo: el foco es la esmeralda (se usa en la mesa de inscripcion de pergaminos). */
    public static final ResourceLocation SCHOOL_ID_CRABS =
            ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "invocar_cangrejo");
    public static final TagKey<Item> CRAB_FOCUS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "cangrejo_focus"));

    private static final DeferredRegister<SchoolType> SCHOOLS =
            DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, MutantRaiseDead.MODID);
    private static final DeferredRegister<AbstractSpell> SPELLS =
            DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, MutantRaiseDead.MODID);
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MutantRaiseDead.MODID);

    /** Igual que la escuela de sangre (mismo poder, resistencia, sonido y foco), pero con su propio pergamino. */
    public static final RegistryObject<SchoolType> SCHOOL = SCHOOLS.register("revive_mutantes", () -> new SchoolType(
            SCHOOL_ID,
            ModTags.BLOOD_FOCUS,
            Component.translatable("school.mutantraisedead.revive_mutantes").withStyle(ChatFormatting.DARK_RED),
            AttributeRegistry.BLOOD_SPELL_POWER,
            AttributeRegistry.BLOOD_MAGIC_RESIST,
            SoundRegistry.BLOOD_CAST,
            ISSDamageTypes.BLOOD_MAGIC,
            false,
            false));

    /** Igual que la escuela de naturaleza, pero con su propio pergamino. */
    public static final RegistryObject<SchoolType> SCHOOL_PLANTS = SCHOOLS.register("enraizar_plantas", () -> new SchoolType(
            SCHOOL_ID_PLANTS,
            ModTags.NATURE_FOCUS,
            Component.translatable("school.mutantraisedead.enraizar_plantas").withStyle(ChatFormatting.GREEN),
            AttributeRegistry.NATURE_SPELL_POWER,
            AttributeRegistry.NATURE_MAGIC_RESIST,
            SoundRegistry.NATURE_CAST,
            ISSDamageTypes.NATURE_MAGIC,
            false,
            false));

    /** Poder, resistencia y sonido de evocacion, pero con su propio pergamino y su propio foco (esmeralda). */
    public static final RegistryObject<SchoolType> SCHOOL_CRABS = SCHOOLS.register("invocar_cangrejo", () -> new SchoolType(
            SCHOOL_ID_CRABS,
            CRAB_FOCUS,
            Component.translatable("school.mutantraisedead.invocar_cangrejo").withStyle(ChatFormatting.GOLD),
            AttributeRegistry.EVOCATION_SPELL_POWER,
            AttributeRegistry.EVOCATION_MAGIC_RESIST,
            SoundRegistry.EVOCATION_CAST,
            ISSDamageTypes.EVOCATION_MAGIC,
            false,
            false));

    public static final RegistryObject<AbstractSpell> REVIVE_X =
            SPELLS.register("revive_mutantes_x", () -> new ReviveMutantesSpell(1, "revive_mutantes_x"));
    public static final RegistryObject<AbstractSpell> REVIVE_Y =
            SPELLS.register("revive_mutantes_y", () -> new ReviveMutantesSpell(2, "revive_mutantes_y"));
    public static final RegistryObject<AbstractSpell> REVIVE_Z =
            SPELLS.register("revive_mutantes_z", () -> new ReviveMutantesSpell(3, "revive_mutantes_z"));

    public static final RegistryObject<AbstractSpell> PLANTS_1 =
            SPELLS.register("enraizar_plantas_1", () -> new EnraizarPlantasSpell(1, "enraizar_plantas_1"));
    public static final RegistryObject<AbstractSpell> PLANTS_2 =
            SPELLS.register("enraizar_plantas_2", () -> new EnraizarPlantasSpell(2, "enraizar_plantas_2"));
    public static final RegistryObject<AbstractSpell> PLANTS_3 =
            SPELLS.register("enraizar_plantas_3", () -> new EnraizarPlantasSpell(3, "enraizar_plantas_3"));
    public static final RegistryObject<AbstractSpell> PLANTS_4 =
            SPELLS.register("enraizar_plantas_4", () -> new EnraizarPlantasSpell(4, "enraizar_plantas_4"));

    public static final RegistryObject<AbstractSpell> CRAB_COAL =
            SPELLS.register("invocar_cangrejo_carbon", () -> new InvocarCangrejoSpell(GrottolVariant.COAL, "invocar_cangrejo_carbon", true));
    public static final RegistryObject<AbstractSpell> CRAB_COPPER =
            SPELLS.register("invocar_cangrejo_cobre", () -> new InvocarCangrejoSpell(GrottolVariant.COPPER, "invocar_cangrejo_cobre", false));
    public static final RegistryObject<AbstractSpell> CRAB_IRON =
            SPELLS.register("invocar_cangrejo_hierro", () -> new InvocarCangrejoSpell(GrottolVariant.IRON, "invocar_cangrejo_hierro", false));

    public static final RegistryObject<AbstractSpell> CREEPER_MUTANT =
            SPELLS.register("cabeza_creeper_mutante", CabezaCreeperMutanteSpell::new);

    public static final RegistryObject<AbstractSpell> BLAZE =
            SPELLS.register("invocar_blaze", () -> new InvocarBlazeSpell(0, "invocar_blaze"));
    public static final RegistryObject<AbstractSpell> BLAZE_X =
            SPELLS.register("invocar_blaze_x", () -> new InvocarBlazeSpell(1, "invocar_blaze_x"));
    public static final RegistryObject<AbstractSpell> BLAZE_Y =
            SPELLS.register("invocar_blaze_y", () -> new InvocarBlazeSpell(2, "invocar_blaze_y"));

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("revive_mutantes", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mutantraisedead"))
                    .icon(() -> createScroll(3, 6))
                    .displayItems((parameters, output) -> {
                        output.accept(createScroll(1, 6));
                        output.accept(createScroll(2, 6));
                        output.accept(createScroll(3, 6));
                        for (int t = 1; t <= 4; t++) {
                            output.accept(createPlantScroll(t, 10));
                        }
                        for (GrottolVariant crab : new GrottolVariant[]{GrottolVariant.COAL, GrottolVariant.COPPER, GrottolVariant.IRON}) {
                            output.accept(createCrabScroll(crab, 1));
                            output.accept(createCrabScroll(crab, InvocarCangrejoSpell.MAX_LEVEL));
                        }
                        output.accept(createCreeperScroll());
                        output.accept(createBlazeScroll(0, 1));
                        output.accept(createBlazeScroll(0, 2));
                        output.accept(createBlazeScroll(1, 2));
                        output.accept(createBlazeScroll(2, 2));
                        for (var egg : com.mutantraisedead.crab.ModEntities.EGGS.values()) {
                            output.accept(egg.get());
                        }
                        output.accept(com.mutantraisedead.crab.ModEntities.GIANT_CRAB_EGG.get());
                        output.accept(com.mutantraisedead.crab.ModEntities.COAL_BOSS_EGG.get());
                        output.accept(com.mutantraisedead.crab.ModBlocks.COAL_EGG_ROUGH.get());
                        output.accept(com.mutantraisedead.crab.ModBlocks.COAL_EGG_ITEM.get());
                        output.accept(com.mutantraisedead.crab.ModBlocks.NEST_ITEM.get());
                        output.accept(com.mutantraisedead.crab.ModBlocks.DEEPSLATE_NEST_ITEM.get());
                        for (var nest : com.mutantraisedead.crab.ModBlocks.LAIR_NEST_ITEMS.values()) {
                            output.accept(nest.get());
                        }
                        output.accept(com.mutantraisedead.storage.ModStorage.DOUBLE_STORAGE.get());
                    })
                    .build());

    private ModRegistry() {
    }

    public static void register(IEventBus bus) {
        SCHOOLS.register(bus);
        SPELLS.register(bus);
        TABS.register(bus);
    }

    public static AbstractSpell spellForTier(int tier) {
        return switch (tier) {
            case 1 -> REVIVE_X.get();
            case 2 -> REVIVE_Y.get();
            default -> REVIVE_Z.get();
        };
    }

    /** 1 = (X), 2 = (Y), 3 = (Z), 0 = no es uno de nuestros hechizos. */
    public static int tierOfSpell(AbstractSpell spell) {
        if (spell == null) {
            return 0;
        }
        if (spell == REVIVE_X.get()) {
            return 1;
        }
        if (spell == REVIVE_Y.get()) {
            return 2;
        }
        if (spell == REVIVE_Z.get()) {
            return 3;
        }
        return 0;
    }

    /** Pergamino de Iron's con el hechizo Revive Mutantes del nivel pedido. */
    public static ItemStack createScroll(int tier, int level) {
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spellForTier(tier), level, stack);
        return stack;
    }

    /** Nivel mutante (1 a 3) del pergamino, o 0 si no es un pergamino de Revive Mutantes. */
    public static int tierOfScroll(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(ItemRegistry.SCROLL.get())
                || !ISpellContainer.isSpellContainer(stack)) {
            return 0;
        }
        ISpellContainer container = ISpellContainer.get(stack);
        if (container == null) {
            return 0;
        }
        return tierOfSpell(container.getSpellAtIndex(0).getSpell());
    }

    // ------------------------------------------------------------------
    // Invocar Cangrejo, Cabeza de Creeper Mutante, Invocar Blaze
    // ------------------------------------------------------------------

    public static AbstractSpell crabSpell(GrottolVariant variant) {
        return switch (variant) {
            case COPPER -> CRAB_COPPER.get();
            case IRON -> CRAB_IRON.get();
            default -> CRAB_COAL.get();
        };
    }

    /** CARBON, COBRE o HIERRO si es uno de nuestros hechizos de cangrejo; null si no. */
    public static GrottolVariant crabVariantOfSpell(AbstractSpell spell) {
        if (spell == null) {
            return null;
        }
        if (spell == CRAB_COAL.get()) {
            return GrottolVariant.COAL;
        }
        if (spell == CRAB_COPPER.get()) {
            return GrottolVariant.COPPER;
        }
        if (spell == CRAB_IRON.get()) {
            return GrottolVariant.IRON;
        }
        return null;
    }

    public static ItemStack createCrabScroll(GrottolVariant variant, int level) {
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(crabSpell(variant), level, stack);
        return stack;
    }

    public static ItemStack createCreeperScroll() {
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(CREEPER_MUTANT.get(), 1, stack);
        return stack;
    }

    /** tier 0 = Invocar Blaze, 1 = mutante (X), 2 = mutante (Y). */
    public static ItemStack createBlazeScroll(int tier, int level) {
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        AbstractSpell spell = switch (tier) {
            case 1 -> BLAZE_X.get();
            case 2 -> BLAZE_Y.get();
            default -> BLAZE.get();
        };
        ISpellContainer.createScrollContainer(spell, level, stack);
        return stack;
    }

    /** Los hechizos con mutantes: hay que juntar todos para el logro "Esta vaina es seria bro". */
    public static java.util.List<RegistryObject<AbstractSpell>> mutantSpells() {
        return java.util.List.of(REVIVE_X, REVIVE_Y, REVIVE_Z, CREEPER_MUTANT, BLAZE_X, BLAZE_Y);
    }

    // ------------------------------------------------------------------
    // Enraizar Plantas (1 a 4)
    // ------------------------------------------------------------------

    public static AbstractSpell plantSpellForTier(int tier) {
        return switch (tier) {
            case 1 -> PLANTS_1.get();
            case 2 -> PLANTS_2.get();
            case 3 -> PLANTS_3.get();
            default -> PLANTS_4.get();
        };
    }

    /** 1 a 4 = cantidad de plantas, 0 = no es uno de nuestros hechizos de plantas. */
    public static int plantTierOfSpell(AbstractSpell spell) {
        if (spell == null) {
            return 0;
        }
        if (spell == PLANTS_1.get()) {
            return 1;
        }
        if (spell == PLANTS_2.get()) {
            return 2;
        }
        if (spell == PLANTS_3.get()) {
            return 3;
        }
        if (spell == PLANTS_4.get()) {
            return 4;
        }
        return 0;
    }

    public static ItemStack createPlantScroll(int tier, int level) {
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(plantSpellForTier(tier), level, stack);
        return stack;
    }
}
