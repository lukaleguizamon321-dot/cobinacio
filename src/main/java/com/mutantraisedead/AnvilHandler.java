package com.mutantraisedead;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Receta del yunque arcano de Iron's:
 *   libro con Revivir Muertos (nivel maximo) + Quimica X / Formula Y / Compuesto Z
 *   = el mismo libro, marcado con nivel mutante 1 / 2 / 3.
 *
 * El yunque arcano de Iron's no usa recetas normales, asi que cuando un jugador
 * lo tiene abierto revisamos los dos espacios y, si coinciden, ponemos el resultado.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class AnvilHandler {
    public static final String TAG_TIER = "mutantraisedead_tier";

    private static final String ARCANE_ANVIL_MENU = "io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu";

    private AnvilHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || !menu.getClass().getName().equals(ARCANE_ANVIL_MENU)) {
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

        int newTier = chemicalTier(modifier);
        if (newTier <= 0) {
            return;
        }
        if (isScroll(base) || !holdsRaiseDead(base, Config.REQUIRED_SPELL_LEVEL.get())) {
            return;
        }
        if (getTier(base) >= newTier) {
            return; // ya tiene ese nivel mutante o uno mejor
        }

        ItemStack output = base.copy();
        output.setCount(1);
        output.getOrCreateTag().putInt(TAG_TIER, newTier);
        result.set(output);
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        int tier = getTier(event.getItemStack());
        if (tier <= 0) {
            return;
        }
        int percent = (int) Math.round(Config.chanceForTier(tier) * 100.0D);
        event.getToolTip().add(Component.literal("Revivir Muertos Mutante - nivel " + tier + " (" + percent + "%)")
                .withStyle(ChatFormatting.DARK_GREEN));
    }

    /** Nivel mutante guardado en el item (0 si no tiene). */
    public static int getTier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_TIER);
    }

    /** 1 = Quimica X (Mutant Monsters), 2 = Formula Y, 3 = Compuesto Z (Mutant More). */
    private static int chemicalTier(ItemStack stack) {
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

    private static boolean isScroll(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && key.getPath().contains("scroll");
    }

    /** Revisa los hechizos guardados en el item (formato nuevo y viejo de Iron's). */
    private static boolean holdsRaiseDead(ItemStack stack, int minLevel) {
        CompoundTag root = stack.getTag();
        if (root == null) {
            return false;
        }
        for (String key : new String[]{"irons_spellbooks:spell_container", "ISB_Spells"}) {
            if (!root.contains(key, Tag.TAG_COMPOUND)) {
                continue;
            }
            ListTag spells = root.getCompound(key).getList("data", Tag.TAG_COMPOUND);
            for (int i = 0; i < spells.size(); i++) {
                CompoundTag spell = spells.getCompound(i);
                if (spell.getString("id").endsWith("raise_dead") && spell.getInt("level") >= minLevel) {
                    return true;
                }
            }
        }
        return false;
    }
}
