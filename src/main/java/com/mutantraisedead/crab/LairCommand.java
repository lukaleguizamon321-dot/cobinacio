package com.mutantraisedead.crab;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mutantraisedead.MutantRaiseDead;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Comando para probar las guaridas sin tener que buscarlas (hace falta ser operador):
 *   /cobinatio guarida          -> la de carbon
 *   /cobinatio guarida carbon   -> la de carbon
 *   /cobinatio guarida cobre    -> la del Cangrejo Titan de Cobre
 * La construye alrededor tuyo y te deja adentro.
 */
@Mod.EventBusSubscriber(modid = MutantRaiseDead.MODID)
public final class LairCommand {
    private LairCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cobinatio")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("guarida")
                        .executes(context -> build(context.getSource(), LairFeature.Theme.COAL))
                        .then(Commands.argument("tipo", StringArgumentType.word())
                                .suggests((context, suggestions) -> SharedSuggestionProvider.suggest(new String[]{"carbon", "cobre"}, suggestions))
                                .executes(context -> {
                                    String type = StringArgumentType.getString(context, "tipo").toLowerCase(java.util.Locale.ROOT);
                                    if (type.equals("cobre") || type.equals("copper")) {
                                        return build(context.getSource(), LairFeature.Theme.COPPER);
                                    }
                                    if (type.equals("carbon") || type.equals("coal")) {
                                        return build(context.getSource(), LairFeature.Theme.COAL);
                                    }
                                    context.getSource().sendFailure(Component.literal("Tipos de guarida: carbon o cobre"));
                                    return 0;
                                }))));
    }

    private static int build(CommandSourceStack source, LairFeature.Theme theme) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();
        BlockPos pos = player.blockPosition();

        // Que entre en la altura del mundo (el piso queda 2 bloques debajo, el techo 15 arriba).
        int minY = level.getMinBuildHeight() + 4;
        int maxY = level.getMaxBuildHeight() - LairFeature.HEIGHT - 4;
        BlockPos center = new BlockPos(pos.getX(), Mth.clamp(pos.getY(), minY, maxY), pos.getZ());

        LairFeature.build(level, center, level.getRandom(), true, theme);

        // El centro es el pedestal con el huevo del jefe. Te dejamos a un costado, sobre el piso y fuera del alcance
        // del huevo (8.5 bloques), para que el jefe no despierte hasta que te acerques vos.
        player.teleportTo(level, center.getX() + 8.5D, center.getY(), center.getZ() + 0.5D, 90.0F, 0.0F);
        String name = theme == LairFeature.Theme.COPPER ? "de cobre" : "de carbon";
        source.sendSuccess(() -> Component.literal("Guarida " + name + " creada. En el centro esta el huevo del jefe: cuando te acerques, despierta. Los nidos estan en la pared."), true);
        return 1;
    }
}
