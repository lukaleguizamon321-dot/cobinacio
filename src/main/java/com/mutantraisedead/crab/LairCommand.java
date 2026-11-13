package com.mutantraisedead.crab;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Comando para probar la guarida sin tener que buscarla: /cobinatio guarida
 * (hace falta ser operador). La construye alrededor tuyo y te deja adentro.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class LairCommand {
    private LairCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cobinatio")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("guarida").executes(context -> build(context.getSource()))));
    }

    private static int build(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();
        BlockPos pos = player.blockPosition();

        // Que entre en la altura del mundo (el piso queda 2 bloques debajo, el techo 15 arriba).
        int minY = level.getMinBuildHeight() + 4;
        int maxY = level.getMaxBuildHeight() - LairFeature.HEIGHT - 4;
        BlockPos center = new BlockPos(pos.getX(), Mth.clamp(pos.getY(), minY, maxY), pos.getZ());

        LairFeature.build(level, center, level.getRandom(), true);

        // El centro es el pedestal con el huevo del jefe. Te dejamos a un costado, sobre el piso y fuera del alcance
        // del huevo (8.5 bloques), para que el jefe no despierte hasta que te acerques vos.
        player.teleportTo(level, center.getX() + 8.5D, center.getY(), center.getZ() + 0.5D, 90.0F, 0.0F);
        source.sendSuccess(() -> Component.literal("Guarida creada. En el centro esta el huevo del jefe: cuando te acerques, despierta. Los nidos estan en la pared."), true);
        return 1;
    }
}
