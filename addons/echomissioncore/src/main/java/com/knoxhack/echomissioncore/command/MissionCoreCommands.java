package com.knoxhack.echomissioncore.command;

import com.knoxhack.echocore.api.mission.MissionObjectiveType;
import com.knoxhack.echomissioncore.service.MissionCoreService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class MissionCoreCommands {
    private MissionCoreCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("echomission")
                .requires(MissionCoreCommands::isGamemaster)
                .then(Commands.literal("list").executes(context -> list(context.getSource())))
                .then(Commands.literal("inspect")
                        .then(Commands.argument("mission", StringArgumentType.string())
                                .executes(context -> inspect(
                                        context.getSource(),
                                        parse(StringArgumentType.getString(context, "mission"))))))
                .then(Commands.literal("start")
                        .then(Commands.argument("mission", StringArgumentType.string())
                                .executes(context -> start(
                                        context.getSource().getPlayerOrException(),
                                        parse(StringArgumentType.getString(context, "mission"))))))
                .then(Commands.literal("complete")
                        .then(Commands.argument("mission", StringArgumentType.string())
                                .executes(context -> complete(
                                        context.getSource().getPlayerOrException(),
                                        parse(StringArgumentType.getString(context, "mission"))))))
                .then(Commands.literal("claim")
                        .then(Commands.argument("mission", StringArgumentType.string())
                                .executes(context -> claim(
                                        context.getSource().getPlayerOrException(),
                                        parse(StringArgumentType.getString(context, "mission"))))))
                .then(Commands.literal("progress")
                        .then(Commands.argument("mission", StringArgumentType.string())
                                .then(Commands.argument("objective", StringArgumentType.string())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> progress(
                                                        context.getSource().getPlayerOrException(),
                                                        parse(StringArgumentType.getString(context, "mission")),
                                                        parse(StringArgumentType.getString(context, "objective")),
                                                        IntegerArgumentType.getInteger(context, "amount")))))))
                .then(Commands.literal("record")
                        .then(Commands.argument("type", StringArgumentType.string())
                                .then(Commands.argument("target", StringArgumentType.string())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> record(
                                                        context.getSource().getPlayerOrException(),
                                                        MissionObjectiveType.byId(StringArgumentType.getString(context, "type")),
                                                        parse(StringArgumentType.getString(context, "target")),
                                                        IntegerArgumentType.getInteger(context, "amount")))))))
                .then(Commands.literal("validate").executes(context -> validate(context.getSource())))
                .then(Commands.literal("reload").executes(context -> reload(context.getSource()))));
    }

    private static boolean isGamemaster(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    private static int list(CommandSourceStack source) {
        String missions = MissionCoreService.INSTANCE.missionDefinitions().stream()
                .map(mission -> mission.id().toString())
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("none");
        source.sendSuccess(() -> Component.literal("MissionCore missions: " + missions), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int inspect(CommandSourceStack source, Identifier missionId) {
        source.sendSuccess(() -> Component.literal(MissionCoreService.INSTANCE.debugState(source.getPlayer(), missionId)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int start(ServerPlayer player, Identifier missionId) {
        boolean ok = MissionCoreService.INSTANCE.startMission(player, missionId);
        tell(player, ok ? "Started mission " + missionId + "." : "Could not start mission " + missionId + ".");
        return ok ? Command.SINGLE_SUCCESS : 0;
    }

    private static int complete(ServerPlayer player, Identifier missionId) {
        boolean ok = MissionCoreService.INSTANCE.completeMission(player, missionId);
        tell(player, ok ? "Completed mission " + missionId + "." : "Could not complete mission " + missionId + ".");
        return ok ? Command.SINGLE_SUCCESS : 0;
    }

    private static int claim(ServerPlayer player, Identifier missionId) {
        boolean ok = MissionCoreService.INSTANCE.claimReward(player, missionId);
        tell(player, ok ? "Claimed reward for " + missionId + "." : "No claimable reward for " + missionId + ".");
        return ok ? Command.SINGLE_SUCCESS : 0;
    }

    private static int progress(ServerPlayer player, Identifier missionId, Identifier objectiveId, int amount) {
        boolean ok = MissionCoreService.INSTANCE.forceProgress(player, missionId, objectiveId, amount);
        tell(player, ok ? "Progressed " + objectiveId + " by " + amount + "." : "Could not progress objective.");
        return ok ? Command.SINGLE_SUCCESS : 0;
    }

    private static int record(ServerPlayer player, MissionObjectiveType type, Identifier target, int amount) {
        boolean ok = MissionCoreService.INSTANCE.recordObjective(player, type, target, amount, java.util.Map.of("debug", "true"));
        tell(player, ok ? "Recorded " + type.id() + " -> " + target + " by " + amount + "." : "No matching objective recorded.");
        return ok ? Command.SINGLE_SUCCESS : 0;
    }

    private static int reload(CommandSourceStack source) {
        source.getServer().reloadResources(source.getServer().getPackRepository().getSelectedIds());
        source.sendSuccess(() -> Component.literal("MissionCore content reload requested."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int validate(CommandSourceStack source) {
        java.util.List<String> warnings = MissionCoreService.INSTANCE.validateContent();
        if (warnings.isEmpty()) {
            source.sendSuccess(() -> Component.literal("MissionCore validation passed."), false);
            return Command.SINGLE_SUCCESS;
        }
        source.sendSuccess(() -> Component.literal("MissionCore validation warnings: " + warnings.size()), false);
        warnings.stream().limit(8).forEach(warning ->
                source.sendSuccess(() -> Component.literal("- " + warning), false));
        return warnings.size();
    }

    private static Identifier parse(String value) {
        Identifier id = Identifier.tryParse(value);
        return id == null ? Identifier.fromNamespaceAndPath("echomissioncore", value.toLowerCase(java.util.Locale.ROOT)) : id;
    }

    private static void tell(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal("[MissionCore] " + message), true);
    }
}
