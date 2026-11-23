package com.mutantraisedead;

import com.mutantraisedead.crab.ModSounds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Objetos sueltos del mod: los corazones que sueltan los jefes, la Bobina y los discos con la musica de los jefes.
 */
public final class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MutantRaiseDead.MODID);

    /** Lo suelta el Cangrejo Titan de Cobre. Solo sirve para craftear la Bobina. */
    public static final RegistryObject<Item> COPPER_HEART = ITEMS.register("copper_heart", () ->
            new TipItem(new Item.Properties().rarity(Rarity.RARE), "item.mutantraisedead.copper_heart.tip"));

    /** Lo suelta el Cangrejo Titan de Amatista. Solo sirve para craftear las Gafas de amatista. */
    public static final RegistryObject<Item> AMETHYST_HEART = ITEMS.register("amethyst_heart", () ->
            new TipItem(new Item.Properties().rarity(Rarity.RARE), "item.mutantraisedead.amethyst_heart.tip"));

    /** Artefacto (ranura de amuletos): sube el poder de los hechizos de rayo. */
    public static final RegistryObject<Item> BOBINA = ITEMS.register("bobina", BobinaItem::new);

    /** Disco con "Colossus Beneath the Crags" (3:00 = 3597 ticks). Lo suelta el jefe de cobre con 25 % de probabilidad. */
    public static final RegistryObject<Item> DISC_COPPER_BOSS = ITEMS.register("disc_copper_boss", () ->
            new RecordItem(13, ModSounds.COPPER_BOSS_MUSIC, new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 3597));

    /** Disco con "Sovereign of the Deep" (2:49 = 3373 ticks). Lo suelta el jefe de amatista con 25 % de probabilidad. */
    public static final RegistryObject<Item> DISC_AMETHYST_BOSS = ITEMS.register("disc_amethyst_boss", () ->
            new RecordItem(14, ModSounds.AMETHYST_BOSS_MUSIC, new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 3373));

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
