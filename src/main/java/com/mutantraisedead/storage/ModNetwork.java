package com.mutantraisedead.storage;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Canal de red del mod (por ahora un solo mensaje: "abrir el Doble Almacen"). */
public final class ModNetwork {
    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MutantRaiseDead.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, OpenStoragePacket.class,
                OpenStoragePacket::encode, OpenStoragePacket::decode, OpenStoragePacket::handle);
    }
}
