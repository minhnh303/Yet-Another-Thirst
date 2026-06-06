package dev.minhnh.yetanotherthirst.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.stream.Collectors;

public final class ThirstCommands {

    private ThirstCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("thirst")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("query")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> query(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("thirst", IntegerArgumentType.integer(0, 20))
                                        .then(Commands.argument("quenched", IntegerArgumentType.integer(0, 20))
                                                .executes(context -> set(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        IntegerArgumentType.getInteger(context, "thirst"),
                                                        IntegerArgumentType.getInteger(context, "quenched")))))))
                .then(Commands.literal("enable")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> enable(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "players"),
                                                BoolArgumentType.getBool(context, "enabled")))))));
    }

    private static int query(CommandSourceStack source, ServerPlayer player) {

        ThirstState state = ThirstStorage.get(player);
        source.sendSuccess(() -> Component.translatable("command.yet_another_thirst.query", state.getThirst(), state.getQuenched()), false);
        return state.getThirst();
    }

    private static int set(CommandSourceStack source, ServerPlayer player, int thirst, int quenched) {

        ThirstState state = ThirstStorage.get(player);
        state.setThirst(thirst);
        state.setQuenched(quenched);
        ThirstStorage.sync(player);
        source.sendSuccess(() -> Component.translatable("command.yet_another_thirst.set", thirst, quenched), false);
        return thirst;
    }

    private static int enable(CommandSourceStack source, Collection<ServerPlayer> players, boolean enabled) {

        for (ServerPlayer player : players) {
            ThirstStorage.get(player).setEnabled(enabled);
            ThirstStorage.sync(player);
        }

        String names = players.stream()
                .map(player -> player.getGameProfile().getName())
                .collect(Collectors.joining(", "));
        String key = enabled ? "command.yet_another_thirst.enable" : "command.yet_another_thirst.disable";
        source.sendSuccess(() -> Component.translatable(key, names), false);
        return players.size();
    }
}
