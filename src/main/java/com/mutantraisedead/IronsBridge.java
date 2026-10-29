package com.mutantraisedead;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Conecta con Iron's Spells 'n Spellbooks 1.20.1-3.x usando reflexion.
 * Asi el mod se compila solo contra Forge y no depende de bajar nada de otros mods.
 */
public final class IronsBridge {
    private static final String ROLLED_TAG = "mutantraisedead_rolled";

    private static boolean ready = false;
    private static Class<?> zombieClass;
    private static Class<?> skeletonClass;
    private static Method getOwnerMethod;
    private static Method getSpellbookMethod;
    private static Method eventGetOwner;
    private static Method eventGetSummon;

    private IronsBridge() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void init() {
        try {
            Class<?> eventClass = Class.forName("io.redspace.ironsspellbooks.api.events.SetSummonOwnerEvent");
            eventGetOwner = eventClass.getMethod("getOwner");
            eventGetSummon = eventClass.getMethod("getSummon");

            zombieClass = Class.forName("io.redspace.ironsspellbooks.entity.mobs.SummonedZombie");
            skeletonClass = Class.forName("io.redspace.ironsspellbooks.entity.mobs.SummonedSkeleton");

            getOwnerMethod = Class.forName("io.redspace.ironsspellbooks.capabilities.magic.SummonManager")
                    .getMethod("getOwner", Entity.class);
            getSpellbookMethod = Class.forName("io.redspace.ironsspellbooks.api.util.Utils")
                    .getMethod("getPlayerSpellbookStack", Player.class);

            Consumer<Event> listener = IronsBridge::onSetSummonOwner;
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, (Class) eventClass, (Consumer) listener);

            ready = true;
            MutantRaiseDead.LOGGER.info("[MutantRaiseDead] Conectado con Iron's Spells 'n Spellbooks correctamente.");
        } catch (Throwable t) {
            ready = false;
            MutantRaiseDead.LOGGER.error("[MutantRaiseDead] No pude conectarme con Iron's Spells 'n Spellbooks. "
                    + "Este mod esta hecho para la version 1.20.1-3.16.3. La mutacion NO va a funcionar.", t);
        }
    }

    /**
     * Se llama cada vez que Iron's le asigna un dueno a un invocado.
     * Para cada zombi/esqueleto de Revivir Muertos tira la probabilidad segun el nivel del libro.
     */
    private static void onSetSummonOwner(Event event) {
        try {
            Entity owner = (Entity) eventGetOwner.invoke(event);
            Entity summon = (Entity) eventGetSummon.invoke(event);
            if (!(owner instanceof ServerPlayer player) || summon == null) {
                return;
            }
            // Iron's dispara este evento dos veces: en el constructor (todavia no esta en el mundo)
            // y despues de agregarlo. Solo nos importa la segunda.
            if (!summon.isAddedToWorld() || summon.getTags().contains(ROLLED_TAG)) {
                return;
            }

            boolean isZombie = zombieClass.isInstance(summon);
            boolean isSkeleton = skeletonClass.isInstance(summon);
            if (!isZombie && !isSkeleton) {
                return;
            }
            summon.addTag(ROLLED_TAG);

            int tier = bestTier(player);
            if (tier <= 0) {
                return;
            }
            double chance = Config.chanceForTier(tier);
            if (player.getRandom().nextDouble() >= chance) {
                return;
            }
            GuardHandler.queueReplacement(player, summon, isZombie);
        } catch (Throwable t) {
            MutantRaiseDead.LOGGER.error("[MutantRaiseDead] Error al procesar un invocado", t);
        }
    }

    /** Mayor nivel mutante entre el libro equipado, la mano principal y la secundaria. */
    private static int bestTier(Player player) {
        int best = AnvilHandler.getTier(getSpellbook(player));
        best = Math.max(best, AnvilHandler.getTier(player.getMainHandItem()));
        best = Math.max(best, AnvilHandler.getTier(player.getOffhandItem()));
        return best;
    }

    private static ItemStack getSpellbook(Player player) {
        if (getSpellbookMethod == null) {
            return ItemStack.EMPTY;
        }
        try {
            Object result = getSpellbookMethod.invoke(null, player);
            return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        } catch (Throwable t) {
            return ItemStack.EMPTY;
        }
    }

    /** Dueno de un invocado de Iron's (o null si no es un invocado). */
    public static Entity getSummonOwner(Entity entity) {
        if (!ready || getOwnerMethod == null) {
            return null;
        }
        try {
            return (Entity) getOwnerMethod.invoke(null, entity);
        } catch (Throwable t) {
            return null;
        }
    }
}
