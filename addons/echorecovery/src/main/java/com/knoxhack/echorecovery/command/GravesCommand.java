package com.knoxhack.echorecovery.command;

import com.knoxhack.echorecovery.EchoRecovery;
import com.knoxhack.echorecovery.config.RecoveryConfig;
import com.knoxhack.echorecovery.data.RecoveryWorldData;
import com.knoxhack.echorecovery.grave.GraveManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class GravesCommand {
    private GravesCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("graves")
            .then(Commands.literal("list")
                .executes(ctx -> listGraves(ctx.getSource().getPlayerOrException())))
            .then(Commands.literal("locate")
                .then(Commands.argument("id", StringArgumentType.word())
                    .executes(ctx -> locateGrave(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id")))))
            .then(Commands.literal("recover")
                .then(Commands.argument("id", StringArgumentType.word())
                    .executes(ctx -> recoverGrave(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id")))))
            .then(Commands.literal("delete")
                .then(Commands.argument("id", StringArgumentType.word())
                    .executes(ctx -> deleteGrave(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "id")))))
            .then(Commands.literal("history")
                .executes(ctx -> showHistory(ctx.getSource().getPlayerOrException())))
            .then(Commands.literal("share")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(ctx -> shareGrave(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))))
            .then(Commands.literal("team")
                .executes(ctx -> teamGraves(ctx.getSource().getPlayerOrException())))
            .then(Commands.literal("debug")
                .executes(ctx -> debugInfo(ctx.getSource()))
                .requires(src -> src.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)))
            .then(Commands.literal("reload")
                .executes(ctx -> reloadConfig(ctx.getSource()))
                .requires(src -> src.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)))
            .then(Commands.literal("admin")
                .requires(src -> src.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("list")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> adminList(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("restore")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> adminRestore(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "id"))))))
                .then(Commands.literal("delete")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ctx -> adminDelete(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "id")))))))
            .executes(ctx -> listGraves(ctx.getSource().getPlayerOrException())));
    }

    private static int listGraves(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        RecoveryWorldData data = RecoveryWorldData.getOrCreate(level);
        List<RecoveryWorldData.GraveEntry> graves = data.getActiveGraves(player.getUUID());
        if (graves.isEmpty()) {
            player.sendSystemMessage(Component.literal("No active graves."));
            return 0;
        }
        player.sendSystemMessage(Component.literal("Active graves:"));
        for (RecoveryWorldData.GraveEntry grave : graves) {
            String dim = grave.dimension();
            long ageMinutes = (System.currentTimeMillis() - grave.createdAt()) / 60000L;
            String line = String.format("  %s at [%d, %d, %d] in %s | Age: %dm",
                grave.graveId().toString().substring(0, 8),
                grave.pos().getX(), grave.pos().getY(), grave.pos().getZ(),
                dim, ageMinutes);
            player.sendSystemMessage(Component.literal(line));
        }
        return graves.size();
    }

    private static int locateGrave(ServerPlayer player, String id) {
        RecoveryWorldData data = RecoveryWorldData.getOrCreate((ServerLevel) player.level());
        for (RecoveryWorldData.GraveEntry grave : data.getActiveGraves(player.getUUID())) {
            if (grave.graveId().toString().startsWith(id)) {
                String msg = String.format("Grave at [%d, %d, %d] in %s",
                    grave.pos().getX(), grave.pos().getY(), grave.pos().getZ(), grave.dimension());
                player.sendSystemMessage(Component.literal(msg));
                return 1;
            }
        }
        player.sendSystemMessage(Component.literal("Grave not found."));
        return 0;
    }

    private static int recoverGrave(ServerPlayer player, String id) {
        if (!RecoveryConfig.REMOTE_RECOVERY_ENABLED.get()) {
            player.sendSystemMessage(Component.literal("Remote recovery is disabled."));
            return 0;
        }
        RecoveryWorldData data = RecoveryWorldData.getOrCreate((ServerLevel) player.level());
        for (RecoveryWorldData.GraveEntry entry : data.getActiveGraves(player.getUUID())) {
            if (entry.graveId().toString().startsWith(id)) {
                if (player.level().getBlockEntity(entry.pos()) instanceof com.knoxhack.echorecovery.block.entity.GraveBlockEntity grave) {
                    GraveManager.recoverGrave(grave, player);
                    player.sendSystemMessage(Component.literal("Grave recovered."));
                    return 1;
                }
            }
        }
        player.sendSystemMessage(Component.literal("Grave not found."));
        return 0;
    }

    private static int deleteGrave(ServerPlayer player, String id) {
        RecoveryWorldData data = RecoveryWorldData.getOrCreate((ServerLevel) player.level());
        for (RecoveryWorldData.GraveEntry entry : data.getActiveGraves(player.getUUID())) {
            if (entry.graveId().toString().startsWith(id)) {
                ((ServerLevel) player.level()).removeBlock(entry.pos(), false);
                data.removeGrave(player.getUUID(), entry.pos());
                player.sendSystemMessage(Component.literal("Grave deleted."));
                return 1;
            }
        }
        player.sendSystemMessage(Component.literal("Grave not found."));
        return 0;
    }

    private static int showHistory(ServerPlayer player) {
        RecoveryWorldData data = RecoveryWorldData.getOrCreate((ServerLevel) player.level());
        List<RecoveryWorldData.DeathRecord> history = data.getDeathHistory(player.getUUID());
        if (history.isEmpty()) {
            player.sendSystemMessage(Component.literal("No death history."));
            return 0;
        }
        player.sendSystemMessage(Component.literal("Death history:"));
        for (RecoveryWorldData.DeathRecord record : history) {
            String line = String.format("  %s at [%d, %d, %d] in %s",
                record.cause(), record.pos().getX(), record.pos().getY(), record.pos().getZ(), record.dimension());
            player.sendSystemMessage(Component.literal(line));
        }
        return history.size();
    }

    private static int shareGrave(ServerPlayer player, ServerPlayer target) {
        player.sendSystemMessage(Component.literal("Grave sharing is not yet implemented."));
        return 0;
    }

    private static int teamGraves(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("Team graves are not yet implemented."));
        return 0;
    }

    private static int debugInfo(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("ECHO Recovery debug: graves=" + RecoveryConfig.ENABLE_GRAVES.get() +
            " | safe_placement=" + RecoveryConfig.SAFE_PLACEMENT.get() +
            " | owner_only=" + RecoveryConfig.OWNER_ONLY.get()), false);
        return 1;
    }

    private static int reloadConfig(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Config reload not yet implemented (requires server restart)."), false);
        return 1;
    }

    private static int adminList(CommandSourceStack source, ServerPlayer target) {
        RecoveryWorldData data = RecoveryWorldData.getOrCreate((ServerLevel) target.level());
        List<RecoveryWorldData.GraveEntry> graves = data.getActiveGraves(target.getUUID());
        source.sendSuccess(() -> Component.literal("Graves for " + target.getScoreboardName() + ": " + graves.size()), false);
        return graves.size();
    }

    private static int adminRestore(CommandSourceStack source, ServerPlayer target, String id) {
        source.sendSuccess(() -> Component.literal("Admin restore not yet implemented."), false);
        return 0;
    }

    private static int adminDelete(CommandSourceStack source, ServerPlayer target, String id) {
        RecoveryWorldData data = RecoveryWorldData.getOrCreate((ServerLevel) target.level());
        for (RecoveryWorldData.GraveEntry entry : data.getActiveGraves(target.getUUID())) {
            if (entry.graveId().toString().startsWith(id)) {
                ((ServerLevel) target.level()).removeBlock(entry.pos(), false);
                data.removeGrave(target.getUUID(), entry.pos());
                source.sendSuccess(() -> Component.literal("Deleted grave for " + target.getScoreboardName()), false);
                return 1;
            }
        }
        source.sendFailure(Component.literal("Grave not found."));
        return 0;
    }
}
