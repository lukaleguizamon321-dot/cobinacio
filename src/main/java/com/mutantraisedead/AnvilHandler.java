package com.mutantraisedead;

import com.mutantraisedead.crab.GrottolVariant;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Receta del yunque arcano de Iron's:
 *   pergamino de Revivir Muertos (nivel maximo) + Quimica X / Formula Y / Compuesto Z
 *   = pergamino de Revive Mutantes (X) / (Y) / (Z) del mismo nivel.
 *
 *   pergamino de Cabeza de Creeper (nivel maximo) + Quimica X = Cabeza de Creeper Mutante.
 *   pergamino de Invocar Blaze (nivel maximo) + Quimica X / Formula Y = Invocar Blaze Mutante (X) / (Y).
 *   pergamino de Invocar Cangrejo (nivel 5) + bloque de cobre = version de cobre; + bloque de hierro = version de hierro.
 *   (Subir de nivel con tinta, en el yunque arcano, lo hace Iron's solo: ver getRarity en los hechizos.)
 *
 *   pergamino de Enraizar (nivel maximo) + semilla de Foliaath (Mowzie's Mobs)
 *   = Enraizar Plantas (1); cada semilla mas lo sube un nivel, hasta 4.
 *
 * El yunque arcano no usa recetas normales: cuando un jugador lo tiene abierto
 * revisamos los dos espacios y, si coinciden, ponemos el resultado.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class AnvilHandler {
    private AnvilHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (!(menu instanceof ArcaneAnvilMenu)) {
            return;
        }

        // Buscamos los espacios por tipo de contenedor, sin depender de numeros de posicion.
        List<Slot> inputs = new ArrayList<>();
        Slot result = null;
        for (Slot slot : menu.slots) {
            if (slot.container instanceof ResultContainer) {
                result = slot;
            } else if (slot.container != player.getInventory()) {
                inputs.add(slot);
            }
        }
        if (result == null || inputs.size() < 2) {
            return;
        }

        ItemStack base = inputs.get(0).getItem();
        ItemStack modifier = inputs.get(1).getItem();
        if (base.isEmpty() || modifier.isEmpty() || !result.getItem().isEmpty()) {
            return;
        }

        if (!base.is(ItemRegistry.SCROLL.get()) || !ISpellContainer.isSpellContainer(base)) {
            return;
        }
        ISpellContainer container = ISpellContainer.get(base);
        if (container == null) {
            return;
        }
        SpellData data = container.getSpellAtIndex(0);
        if (data == null) {
            return;
        }

        // Quimicas: Revivir Muertos -> Revive Mutantes (X / Y / Z), Cabeza de Creeper -> mutante, Invocar Blaze -> mutante
        int tier = chemicalTier(modifier);
        if (tier > 0) {
            if (data.getSpell() == SpellRegistry.RAISE_DEAD_SPELL.get()) {
                if (data.getLevel() >= Config.REQUIRED_SPELL_LEVEL.get()) {
                    result.set(ModRegistry.createScroll(tier, data.getLevel()));
                }
            } else if (data.getSpell() == SpellRegistry.LOB_CREEPER_SPELL.get()) {
                // Cabeza de Creeper + Quimica X (solo la X) = Cabeza de Creeper Mutante, un solo nivel
                if (tier == 1 && data.getLevel() >= Config.REQUIRED_CREEPER_LEVEL.get()) {
                    result.set(ModRegistry.createCreeperScroll());
                }
            } else if (data.getSpell() == ModRegistry.BLAZE.get()) {
                // Invocar Blaze + Quimica X / Formula Y = Invocar Blaze Mutante (X) / (Y); el nivel se mantiene
                if (tier <= 2 && data.getLevel() >= Config.REQUIRED_BLAZE_LEVEL.get()) {
                    result.set(ModRegistry.createBlazeScroll(tier, data.getLevel()));
                }
            }
            return;
        }

        // Invocar Cangrejo: con el nivel pedido, un bloque de cobre lo pasa a la version de cobre (nivel 1)
        // y un bloque de hierro de la de cobre a la de hierro (nivel 1). La tinta sube los niveles (lo hace Iron's).
        GrottolVariant crab = ModRegistry.crabVariantOfSpell(data.getSpell());
        if (crab != null) {
            if (data.getLevel() >= Config.REQUIRED_CRAB_LEVEL.get()) {
                if (crab == GrottolVariant.COAL && modifier.is(Items.COPPER_BLOCK)) {
                    result.set(ModRegistry.createCrabScroll(GrottolVariant.COPPER, 1));
                } else if (crab == GrottolVariant.COPPER && modifier.is(Items.IRON_BLOCK)) {
                    result.set(ModRegistry.createCrabScroll(GrottolVariant.IRON, 1));
                }
            }
            return;
        }

        // Enraizar + semilla de Foliaath -> Enraizar Plantas (1); cada semilla mas sube un nivel (hasta 4)
        if (isFoliaathSeed(modifier)) {
            int current = ModRegistry.plantTierOfSpell(data.getSpell());
            if (current == 0) {
                if (data.getSpell() == SpellRegistry.ROOT_SPELL.get()
                        && data.getLevel() >= Config.REQUIRED_ROOT_LEVEL.get()) {
                    result.set(ModRegistry.createPlantScroll(1, data.getLevel()));
                }
            } else if (current < 4) {
                result.set(ModRegistry.createPlantScroll(current + 1, data.getLevel()));
            }
        }
    }

    static boolean isFoliaathSeed(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && key.toString().equals("mowziesmobs:foliaath_seed");
    }

    /** 1 = Quimica X (Mutant Monsters), 2 = Formula Y, 3 = Compuesto Z (Mutant More), 0 = nada de eso. */
    static int chemicalTier(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key != null) {
            String id = key.toString();
            if (id.equals("mutantmore:formula_y")) {
                return 2;
            }
            if (id.equals("mutantmore:compound_z")) {
                return 3;
            }
        }
        // La Quimica X es una pocion normal con el efecto "mutantmonsters:chemical_x"
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Potion", Tag.TAG_STRING)
                && tag.getString("Potion").equals("mutantmonsters:chemical_x")) {
            return 1;
        }
        return 0;
    }
}
