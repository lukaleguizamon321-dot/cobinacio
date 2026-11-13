package com.mutantraisedead.crab;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mutantraisedead.MutantRaiseDead;

/**
 * Piedra de afilar: el huevo con imperfecciones (solo, en cualquiera de las dos ranuras) sale como huevo pulido.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class EggEvents {
    private EggEvents() {
    }

    @SubscribeEvent
    public static void onGrindstonePlace(GrindstoneEvent.OnPlaceItem event) {
        ItemStack top = event.getTopItem();
        ItemStack bottom = event.getBottomItem();
        boolean topRough = top.is(ModBlocks.COAL_EGG_ROUGH.get());
        boolean bottomRough = bottom.is(ModBlocks.COAL_EGG_ROUGH.get());
        // exactamente un huevo con imperfecciones y la otra ranura vacia
        if ((topRough && bottom.isEmpty()) || (bottomRough && top.isEmpty())) {
            event.setOutput(new ItemStack(ModBlocks.COAL_EGG_ITEM.get()));
            event.setXp(3);
        }
    }
}
