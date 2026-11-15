package com.mutantraisedead.crab;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mutantraisedead.MutantRaiseDead;

/**
 * Piedra de afilar: el huevo con imperfecciones (de carbon o de cobre; solo, en cualquiera de las dos ranuras)
 * sale como huevo pulido del mismo cangrejo.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class EggEvents {
    private EggEvents() {
    }

    @SubscribeEvent
    public static void onGrindstonePlace(GrindstoneEvent.OnPlaceItem event) {
        ItemStack top = event.getTopItem();
        ItemStack bottom = event.getBottomItem();
        // exactamente un huevo con imperfecciones y la otra ranura vacia
        ItemStack rough = bottom.isEmpty() ? top : (top.isEmpty() ? bottom : ItemStack.EMPTY);
        if (rough.is(ModBlocks.COAL_EGG_ROUGH.get())) {
            event.setOutput(new ItemStack(ModBlocks.COAL_EGG_ITEM.get()));
            event.setXp(3);
        } else if (rough.is(ModBlocks.COPPER_EGG_ROUGH.get())) {
            event.setOutput(new ItemStack(ModBlocks.COPPER_EGG_ITEM.get()));
            event.setXp(3);
        }
    }
}
