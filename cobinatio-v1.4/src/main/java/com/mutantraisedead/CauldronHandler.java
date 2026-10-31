package com.mutantraisedead;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.fluids.PotionFluid;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Reciclar el pergamino de Revive Mutantes en el caldero alquimico de Iron's:
 * en vez de tinta magica devuelve Quimica X (siempre la X, nunca la Y ni la Z).
 * Usa los mismos 250 de agua y la misma probabilidad de reciclado que Iron's.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class CauldronHandler {
    private static final ResourceLocation CHEMICAL_X = ResourceLocation.fromNamespaceAndPath("mutantmonsters", "chemical_x");

    private CauldronHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (ModRegistry.tierOfScroll(held) <= 0) {
            return;
        }
        Level level = event.getLevel();
        BlockEntity be = level.getBlockEntity(event.getPos());
        if (!(be instanceof AlchemistCauldronTile cauldron)) {
            return;
        }

        // Es nuestro pergamino sobre el caldero: nunca dejamos que Iron's lo convierta en tinta.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) {
            return;
        }

        Player player = event.getEntity();
        if (!cauldron.fluidInventory.contains(FluidTags.WATER, 250)) {
            player.displayClientMessage(Component.translatable("message.mutantraisedead.need_water"), true);
            return;
        }
        if (!ForgeRegistries.POTIONS.containsKey(CHEMICAL_X)) {
            return; // Mutant Monsters no esta: no hacemos nada
        }
        Potion chemicalX = ForgeRegistries.POTIONS.getValue(CHEMICAL_X);

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        if (Utils.random.nextFloat() < ServerConfigs.SCROLL_RECYCLE_CHANCE.get()) {
            cauldron.fluidInventory.drain(new FluidStack(Fluids.WATER, 250), IFluidHandler.FluidAction.EXECUTE);
            cauldron.fluidInventory.fill(PotionFluid.of(250, chemicalX, PotionFluid.BottleType.REGULAR), IFluidHandler.FluidAction.EXECUTE);
            level.playSound(null, event.getPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            level.playSound(null, event.getPos(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        cauldron.setChanged();
        level.sendBlockUpdated(event.getPos(), cauldron.getBlockState(), cauldron.getBlockState(), 3);
    }
}
