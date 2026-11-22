package com.mutantraisedead.goggles;

import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** El canal de mensajes de las gafas (tecla -> servidor, rayo -> clientes). */
public final class GogglesNetwork {
    private static SimpleChannel channel;

    private GogglesNetwork() {
    }

    public static void register() {
        if (channel != null) {
            return;
        }
        channel = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(MutantRaiseDead.MODID, "goggles"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        channel.messageBuilder(GogglesKeyPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .decoder(GogglesKeyPacket::new)
                .encoder(GogglesKeyPacket::toBytes)
                .consumerMainThread(GogglesKeyPacket::handle)
                .add();

        channel.messageBuilder(GogglesBeamPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(GogglesBeamPacket::new)
                .encoder(GogglesBeamPacket::toBytes)
                .consumerMainThread(GogglesBeamPacket::handle)
                .add();
    }

    /** Lo llama el cliente al apretar la tecla. */
    public static void sendKeyToServer() {
        channel.sendToServer(new GogglesKeyPacket());
    }

    /** Avisa a quien esta mirando (y al propio jugador) que el rayo esta encendido o se apago (ticks = 0). */
    public static void sendBeam(ServerPlayer player, int rgb, int ticks) {
        channel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new GogglesBeamPacket(player.getId(), rgb, ticks));
    }
}
