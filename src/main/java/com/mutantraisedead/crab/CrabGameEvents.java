package com.mutantraisedead.crab;

import com.mutantraisedead.Achievements;
import com.mutantraisedead.Config;
import com.mutantraisedead.GuardHandler;
import com.mutantraisedead.MutantRaiseDead;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Eventos del juego (bus de Forge) que usan los cangrejos:
 *  - efecto Detectado (redstone): los monstruos cercanos te apuntan aunque haya paredes
 *  - descuento del cangrejo de esmeralda a los aldeanos
 *  - logros: tan duro como el acero, mago electrico, que arde
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class CrabGameEvents {
    /** Marca que se le pone al rayo del cangrejo de cobre. */
    public static final String BOLT_TAG = "cobinatio_crab_bolt";
    private static final String KEY_IRON_HIT = "cobinatio_iron_crab_hit";
    private static final String KEY_BOLT_HIT = "cobinatio_crab_bolt_hit";

    private record PendingReset(Villager villager, long deadline) {
    }

    private static final List<PendingReset> PENDING = new ArrayList<>();

    private CrabGameEvents() {
    }

    // ------------------------------------------------------------------
    // Efecto Detectado
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 20 != 0 || !player.hasEffect(ModEffects.DETECTED.get())
                || player.isSpectator() || player.isCreative()) {
            return;
        }
        double range = Config.DETECTED_RANGE.get();
        List<Mob> mobs = player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(range),
                m -> m instanceof Enemy
                        && !(m instanceof NeutralMob)
                        && !(m instanceof Warden)
                        && m.isAlive()
                        && m.getTarget() == null
                        && m.canAttack(player)
                        && !GuardHandler.isAllied(m)
                        && SummonManager.getOwner(m) == null
                        && m.distanceToSqr(player) <= range * range);
        for (Mob mob : mobs) {
            mob.setTarget(player);
        }
    }

    // ------------------------------------------------------------------
    // Cangrejo de esmeralda: descuento a los aldeanos
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!(event.getTarget() instanceof Villager villager) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.isSecondaryUseActive() || villager.isBaby() || villager.isSleeping()
                || villager.isTrading() || !villager.isAlive()) {
            return;
        }
        VillagerProfession profession = villager.getVillagerData().getProfession();
        if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
            return;
        }
        int percent = Config.EMERALD_DISCOUNT.get();
        if (percent <= 0) {
            return;
        }
        double range = Config.EMERALD_RANGE.get();
        List<GrottolVariantEntity> crabs = villager.level().getEntitiesOfClass(GrottolVariantEntity.class,
                villager.getBoundingBox().inflate(range),
                c -> c.getVariant() == GrottolVariant.EMERALD && c.isAlive() && c.distanceToSqr(villager) <= range * range);
        if (crabs.isEmpty()) {
            return;
        }
        GrottolVariantEntity crab = crabs.stream()
                .min(Comparator.comparingDouble(c -> c.distanceToSqr(villager))).get();

        // El aldeano ya sabe restar solo; nosotros le sumamos el descuento antes de que abra el comercio.
        // Villager.setTradingPlayer(null) borra estos ajustes cuando se cierra la ventana.
        for (MerchantOffer offer : villager.getOffers()) {
            int cut = Mth.floor(offer.getBaseCostA().getCount() * percent / 100.0F);
            if (cut > 0) {
                offer.addToSpecialPriceDiff(-cut);
            }
        }
        PENDING.add(new PendingReset(villager, villager.level().getGameTime() + 10L));
        crab.recordDiscount(villager, player, percent);
    }

    /** Si el comercio no llego a abrirse (aldeano molesto, etc.), se saca el descuento para que no se acumule. */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) {
            return;
        }
        Iterator<PendingReset> it = PENDING.iterator();
        while (it.hasNext()) {
            PendingReset pending = it.next();
            Villager villager = pending.villager();
            if (villager.isRemoved()) {
                it.remove();
                continue;
            }
            if (villager.level().getGameTime() < pending.deadline()) {
                continue;
            }
            it.remove();
            if (villager.getTradingPlayer() == null) {
                for (MerchantOffer offer : villager.getOffers()) {
                    offer.resetSpecialPriceDiff();
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Logros
    // ------------------------------------------------------------------

    /** Lo llaman los cangrejos de hierro cuando un jugador con pico los golpea. */
    public static void markIronHit(Player player) {
        player.getPersistentData().putLong(KEY_IRON_HIT, player.level().getGameTime());
    }

    /** "Tan duro como el acero": se te rompe el pico mientras le pegabas a un cangrejo de hierro. */
    @SubscribeEvent
    public static void onItemBroken(PlayerDestroyItemEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        ItemStack original = event.getOriginal();
        if (!(original.getItem() instanceof PickaxeItem) && !original.canPerformAction(ToolActions.PICKAXE_DIG)) {
            return;
        }
        if (player.getPersistentData().contains(KEY_IRON_HIT)
                && player.level().getGameTime() - player.getPersistentData().getLong(KEY_IRON_HIT) <= 20L) {
            Achievements.grant(player, "tan_duro");
        }
    }

    /** Recuerda que el rayo de un cangrejo de cobre te pego. */
    @SubscribeEvent
    public static void onStruckByLightning(EntityStruckByLightningEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getLightning().getPersistentData().getBoolean(BOLT_TAG)) {
            player.getPersistentData().putLong(KEY_BOLT_HIT, player.level().getGameTime());
        }
    }

    /** "Mago electrico nivel 15": moris poco despues de un rayo de cangrejo de cobre. */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getPersistentData().contains(KEY_BOLT_HIT)
                && player.level().getGameTime() - player.getPersistentData().getLong(KEY_BOLT_HIT) <= 200L) {
            Achievements.grant(player, "mago_electrico");
        }
    }

    /** Para "Que arde": anota a los jugadores que recibieron un disparo del cangrejo de carbon. */
    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getSource().getEntity() instanceof GrottolVariantEntity crab
                && crab.getVariant() == GrottolVariant.COAL) {
            crab.onShotHit(player);
        }
    }
}
