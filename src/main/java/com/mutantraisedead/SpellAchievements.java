package com.mutantraisedead;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Logro "Esta vaina es seria bro": haber tenido todos los hechizos con mutantes
 * (Revive Mutantes X, Y y Z, Cabeza de Creeper Mutante e Invocar Blaze Mutante X e Y).
 * Cada 2 segundos se revisa el inventario y el libro de hechizos; lo que ya se vio queda anotado,
 * asi que no hace falta tenerlos todos al mismo tiempo.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class SpellAchievements {
    private static final String KEY_SEEN = "cobinatio_mutant_spells";
    private static final String KEY_DONE = "cobinatio_seria_bro_done";

    private SpellAchievements() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 40 != 0) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(KEY_DONE)) {
            return;
        }

        Set<String> wanted = new HashSet<>();
        for (RegistryObject<AbstractSpell> spell : ModRegistry.mutantSpells()) {
            wanted.add(spell.get().getSpellId());
        }

        ListTag seenTag = data.getList(KEY_SEEN, Tag.TAG_STRING);
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < seenTag.size(); i++) {
            seen.add(seenTag.getString(i));
        }

        List<ItemStack> stacks = new ArrayList<>();
        stacks.addAll(player.getInventory().items);
        stacks.addAll(player.getInventory().offhand);
        try {
            ItemStack book = Utils.getPlayerSpellbookStack(player);
            if (book != null) {
                stacks.add(book);
            }
        } catch (RuntimeException ignored) {
            // si Curios no responde, igual revisamos el inventario
        }

        boolean changed = false;
        for (ItemStack stack : stacks) {
            if (!ISpellContainer.isSpellContainer(stack)) {
                continue;
            }
            ISpellContainer container = ISpellContainer.get(stack);
            if (container == null) {
                continue;
            }
            for (SpellSlot slot : container.getActiveSpells()) {
                String id = slot.getSpell().getSpellId();
                if (wanted.contains(id) && seen.add(id)) {
                    changed = true;
                }
            }
        }

        if (changed) {
            ListTag list = new ListTag();
            for (String id : seen) {
                list.add(StringTag.valueOf(id));
            }
            data.put(KEY_SEEN, list);
        }
        if (seen.containsAll(wanted)) {
            data.putBoolean(KEY_DONE, true);
            Achievements.grant(player, "seria_bro");
        }
    }
}
