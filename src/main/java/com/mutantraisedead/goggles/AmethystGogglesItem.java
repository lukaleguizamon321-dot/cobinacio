package com.mutantraisedead.goggles;

import com.mutantraisedead.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

/**
 * Gafas de amatista: artefacto de la ranura de amuletos (charm). Con una tecla disparan el laser del Cangrejo Titan de
 * Amatista (ver GogglesLaser). Lo que hacen y el color del laser se explican en el texto del objeto.
 */
public class AmethystGogglesItem extends Item implements ICurioItem {
    public AmethystGogglesItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.mutantraisedead.amethyst_goggles.use",
                Component.keybind("key.mutantraisedead.goggles")).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.mutantraisedead.amethyst_goggles.cost",
                Config.GOGGLES_SECONDS.get(), Config.GOGGLES_BLIND_SECONDS.get(), Config.GOGGLES_COOLDOWN_SECONDS.get())
                .withStyle(ChatFormatting.GRAY));
        DyeColor dye = GogglesColors.dyeOf(stack);
        if (dye == null) {
            tooltip.add(Component.translatable("tooltip.mutantraisedead.amethyst_goggles.dye_hint")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        } else {
            tooltip.add(Component.translatable("tooltip.mutantraisedead.amethyst_goggles.color",
                    Component.translatable("color.minecraft." + dye.getName())).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
}
