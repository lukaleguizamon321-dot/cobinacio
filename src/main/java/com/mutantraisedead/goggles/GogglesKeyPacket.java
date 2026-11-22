package com.mutantraisedead.goggles;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Cliente -> servidor: "apreté la tecla de las gafas". El servidor decide si se puede disparar. */
public class GogglesKeyPacket {
    public GogglesKeyPacket() {
    }

    public GogglesKeyPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                GogglesLaser.toggle(player);
            }
        });
        return true;
    }
}
