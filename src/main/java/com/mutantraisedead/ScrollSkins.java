package com.mutantraisedead;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.function.Function;

/**
 * Skins propias para los pergaminos (solo cliente).
 *
 * Iron's elige la imagen del pergamino segun la ESCUELA del hechizo, asi que los tres hechizos de
 * "Invocar Cangrejo" (carbon, cobre, hierro) se verian iguales. Aca envolvemos el modelo de la escuela
 * y, segun el hechizo que tenga el pergamino, mostramos la skin de cada uno. Lo mismo para Invocar Blaze,
 * que usa la escuela de fuego de Iron's.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ScrollSkins {
    private static final ResourceLocation SKIN_COAL = new ResourceLocation(MutantRaiseDead.MODID, "item/scroll_invocar_cangrejo_carbon");
    private static final ResourceLocation SKIN_COPPER = new ResourceLocation(MutantRaiseDead.MODID, "item/scroll_invocar_cangrejo_cobre");
    private static final ResourceLocation SKIN_IRON = new ResourceLocation(MutantRaiseDead.MODID, "item/scroll_invocar_cangrejo_hierro");
    private static final ResourceLocation SKIN_BLAZE = new ResourceLocation(MutantRaiseDead.MODID, "item/scroll_invocar_blaze");

    /** Modelos de pergamino por escuela que registra Iron's: el de nuestra escuela de cangrejos y el de fuego. */
    private static final ResourceLocation CRAB_SCHOOL_MODEL = new ResourceLocation(MutantRaiseDead.MODID, "item/scroll_invocar_cangrejo");
    private static final ResourceLocation FIRE_SCHOOL_MODEL = new ResourceLocation("irons_spellbooks", "item/scroll_fire");

    private ScrollSkins() {
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        event.register(SKIN_COAL);
        event.register(SKIN_COPPER);
        event.register(SKIN_IRON);
        event.register(SKIN_BLAZE);
    }

    @SubscribeEvent
    public static void onModifyBaking(ModelEvent.ModifyBakingResult event) {
        event.getModels().computeIfPresent(CRAB_SCHOOL_MODEL, (key, old) -> new SkinModel(old, ScrollSkins::crabSkin));
        event.getModels().computeIfPresent(FIRE_SCHOOL_MODEL, (key, old) -> new SkinModel(old, ScrollSkins::blazeSkin));
    }

    private static AbstractSpell firstSpell(ItemStack stack) {
        if (!ISpellContainer.isSpellContainer(stack)) {
            return null;
        }
        ISpellContainer container = ISpellContainer.get(stack);
        return container == null ? null : container.getSpellAtIndex(0).getSpell();
    }

    private static ResourceLocation crabSkin(ItemStack stack) {
        AbstractSpell spell = firstSpell(stack);
        if (spell == null) {
            return null;
        }
        if (spell == ModRegistry.CRAB_COAL.get()) {
            return SKIN_COAL;
        }
        if (spell == ModRegistry.CRAB_COPPER.get()) {
            return SKIN_COPPER;
        }
        if (spell == ModRegistry.CRAB_IRON.get()) {
            return SKIN_IRON;
        }
        return null;
    }

    private static ResourceLocation blazeSkin(ItemStack stack) {
        AbstractSpell spell = firstSpell(stack);
        return spell != null && spell == ModRegistry.BLAZE.get() ? SKIN_BLAZE : null;
    }

    /** Modelo que, al dibujar el item, cambia por la skin que corresponde al hechizo (o deja el original). */
    private static final class SkinModel extends BakedModelWrapper<BakedModel> {
        private final Function<ItemStack, ResourceLocation> chooser;

        private SkinModel(BakedModel original, Function<ItemStack, ResourceLocation> chooser) {
            super(original);
            this.chooser = chooser;
        }

        /** Hay que devolver este modelo (y no el original) para que el juego nos pregunte por la skin. */
        @Override
        public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean leftHand) {
            super.applyTransform(context, poseStack, leftHand);
            return this;
        }

        @Override
        public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
            ResourceLocation skin = chooser.apply(stack);
            if (skin != null) {
                ModelManager manager = Minecraft.getInstance().getModelManager();
                BakedModel model = manager.getModel(skin);
                if (model != manager.getModel(ModelBakery.MISSING_MODEL_LOCATION)) {
                    return List.of(model);
                }
            }
            return originalModel.getRenderPasses(stack, fabulous);
        }
    }
}
