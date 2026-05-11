package com.knoxhack.echodatacore.command;

import com.knoxhack.echocore.api.DataScope;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.IDataKey;
import com.knoxhack.echocore.api.IDataService;
import com.knoxhack.echodatacore.Config;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Comparator;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class DataCoreCommands {
    private static final int MAX_LINES = 24;

    private DataCoreCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("echodata")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("keys")
                                .executes(context -> dumpKeys(context.getSource())))
                        .then(Commands.literal("inspect")
                                .then(Commands.literal("player")
                                        .executes(context -> inspectPlayer(context.getSource(),
                                                context.getSource().getPlayerOrException()))
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(context -> inspectPlayer(context.getSource(),
                                                        EntityArgument.getPlayer(context, "target")))))
                                .then(Commands.literal("world")
                                        .executes(context -> inspectWorld(context.getSource()))))
                        .then(Commands.literal("flag")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("key", StringArgumentType.word())
                                                .then(Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(context -> setFlag(context.getSource(),
                                                                StringArgumentType.getString(context, "key"),
                                                                BoolArgumentType.getBool(context, "value"))))))
                                .then(Commands.literal("unset")
                                        .then(Commands.argument("key", StringArgumentType.word())
                                                .executes(context -> unsetFlag(context.getSource(),
                                                        StringArgumentType.getString(context, "key")))))));
    }

    private static int dumpKeys(CommandSourceStack source) {
        IDataService service = EchoCoreServices.dataService();
        tell(source, "Data service: " + service.getClass().getName(), ChatFormatting.AQUA);
        tell(source, "Registered keys: " + service.registeredKeys().size(), ChatFormatting.GRAY);
        service.registeredKeys().stream()
                .sorted(Comparator.comparing(key -> key.id().toString()))
                .limit(MAX_LINES)
                .forEach(key -> tell(source, key.scope() + " " + key.kind() + " " + key.id()
                        + (key.synced() ? " synced" : " server"), ChatFormatting.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static int inspectPlayer(CommandSourceStack source, ServerPlayer player) {
        Map<Identifier, String> values = EchoCoreServices.playerData(player).debugSnapshot();
        tell(source, "Player data for " + player.getScoreboardName() + ": " + values.size() + " value(s)",
                ChatFormatting.AQUA);
        dumpValues(source, values);
        return Command.SINGLE_SUCCESS;
    }

    private static int inspectWorld(CommandSourceStack source) {
        Map<Identifier, String> values = EchoCoreServices.worldData(source.getLevel()).debugSnapshot();
        tell(source, "World data for " + source.getLevel().dimension().identifier() + ": "
                + values.size() + " value(s)", ChatFormatting.AQUA);
        dumpValues(source, values);
        return Command.SINGLE_SUCCESS;
    }

    private static int setFlag(CommandSourceStack source, String rawKey, boolean value) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!debugMutationsAllowed(source)) {
            tell(source, "Flag mutation is disabled. Enable echodatacore debug commands for this world.",
                    ChatFormatting.RED);
            return 0;
        }
        Identifier key = parseKey(rawKey);
        ServerPlayer player = source.getPlayerOrException();
        boolean changed = EchoCoreServices.playerData(player).set(IDataKey.flag(key, DataScope.PLAYER, false, true), value);
        tell(source, "Set " + key + "=" + value + (changed ? "" : " (unchanged)"), ChatFormatting.YELLOW);
        return Command.SINGLE_SUCCESS;
    }

    private static int unsetFlag(CommandSourceStack source, String rawKey) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!debugMutationsAllowed(source)) {
            tell(source, "Flag mutation is disabled. Enable echodatacore debug commands for this world.",
                    ChatFormatting.RED);
            return 0;
        }
        Identifier key = parseKey(rawKey);
        ServerPlayer player = source.getPlayerOrException();
        boolean changed = EchoCoreServices.playerData(player).clear(IDataKey.flag(key, DataScope.PLAYER, false, true));
        tell(source, "Unset " + key + (changed ? "" : " (not present)"), ChatFormatting.YELLOW);
        return Command.SINGLE_SUCCESS;
    }

    private static void dumpValues(CommandSourceStack source, Map<Identifier, String> values) {
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Identifier::toString)))
                .limit(MAX_LINES)
                .forEach(entry -> tell(source, entry.getKey() + " = " + entry.getValue(), ChatFormatting.GRAY));
        if (values.size() > MAX_LINES) {
            tell(source, "... " + (values.size() - MAX_LINES) + " more value(s)", ChatFormatting.DARK_GRAY);
        }
    }

    private static Identifier parseKey(String rawKey) {
        Identifier key = Identifier.tryParse(rawKey == null ? "" : rawKey);
        if (key == null) {
            key = Identifier.fromNamespaceAndPath("echodatacore", rawKey == null || rawKey.isBlank() ? "debug/flag" : rawKey);
        }
        return key;
    }

    private static boolean debugMutationsAllowed(CommandSourceStack source) {
        return Config.DEBUG_COMMANDS.get() || !source.getServer().isDedicatedServer();
    }

    private static void tell(CommandSourceStack source, String message, ChatFormatting color) {
        source.sendSuccess(() -> Component.literal("[ECHO DATA] " + message).withStyle(color), false);
    }
}
