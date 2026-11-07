package com.mutantraisedead.storage;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** El cliente avisa al servidor que apretaste la tecla del Doble Almacen. */
public record OpenStoragePacket() {
    public void encode(FriendlyByteBuf buffer) {
    }

    public static OpenStoragePacket decode(FriendlyByteBuf buffer) {
        return new OpenStoragePacket();
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                StorageHelper.open(player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
