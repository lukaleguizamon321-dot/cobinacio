package com.mutantraisedead.goggles;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> clientes: "este jugador esta disparando el laser de las gafas (de este color, tantos ticks mas)". ticks = 0 lo apaga. */
public class GogglesBeamPacket {
    private final int entityId;
    private final int rgb;
    private final int ticks;

    public GogglesBeamPacket(int entityId, int rgb, int ticks) {
        this.entityId = entityId;
        this.rgb = rgb;
        this.ticks = ticks;
    }

    public GogglesBeamPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.rgb = buf.readInt();
        this.ticks = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeInt(rgb);
        buf.writeVarInt(ticks);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> GogglesClientEvents.receive(entityId, rgb, ticks)));
        return true;
    }
}
