package com.knoxhack.echoholomap.command;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.network.EchoPacketKind;
import com.knoxhack.echonetcore.api.EchoNetSend;
import com.knoxhack.echoholomap.Config;
import com.knoxhack.echoholomap.HoloMapIds;
import com.knoxhack.echoholomap.map.HoloMapTerrainScanner;
import com.knoxhack.echoholomap.network.HoloMapTileBatchPacket;
import com.knoxhack.echoholomap.network.HoloMapTileRequestPacket;
import com.knoxhack.echoholomap.network.HoloMapSync;
import com.knoxhack.echoholomap.world.HoloMapSavedData;
import com.knoxhack.echoholomap.world.HoloMapTerrainSavedData;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class HoloMapCommands {
    private HoloMapCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("echoholomap")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("debug")
                        .then(Commands.literal("add_marker")
                                .then(Commands.argument("layer", StringArgumentType.word())
                                        .executes(context -> addMarker(context.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(context, "layer")))))
                        .then(Commands.literal("clear_markers")
                                .executes(context -> clearMarkers(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("scan_terrain")
                                .executes(context -> scanTerrain(context.getSource().getPlayerOrException(), configuredScanRadius()))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(0, 24))
                                        .executes(context -> scanTerrain(context.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(context, "radius")))))
                        .then(Commands.literal("resample_terrain")
                                .executes(context -> resampleTerrain(context.getSource().getPlayerOrException(), configuredScanRadius()))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(0, 24))
                                        .executes(context -> resampleTerrain(context.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(context, "radius")))))
                        .then(Commands.literal("clear_terrain")
                                .executes(context -> clearTerrain(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("dump_terrain")
                                .executes(context -> dumpTerrain(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("dump")
                                .executes(context -> dump(context.getSource().getPlayerOrException())))));
    }

    private static int addMarker(ServerPlayer player, String layerInput) {
        if (!debugEnabled() || !(player.level() instanceof ServerLevel serverLevel)) {
            player.sendSystemMessage(Component.literal("ECHO HoloMap // Debug markers are disabled."));
            return 0;
        }
        Identifier layer = HoloMapIds.layerFromInput(layerInput);
        HoloMapSavedData.get(serverLevel).addDebugMarker(player, layer);
        player.sendSystemMessage(Component.translatable("command.echoholomap.added"));
        HoloMapSync.send(player);
        return 1;
    }

    private static int clearMarkers(ServerPlayer player) {
        if (!debugEnabled() || !(player.level() instanceof ServerLevel serverLevel)) {
            player.sendSystemMessage(Component.literal("ECHO HoloMap // Debug markers are disabled."));
            return 0;
        }
        int cleared = HoloMapSavedData.get(serverLevel).clearDebugMarkers();
        player.sendSystemMessage(Component.translatable("command.echoholomap.cleared")
                .append(Component.literal(" (" + cleared + ")")));
        HoloMapSync.send(player);
        return cleared;
    }

    private static int dump(ServerPlayer player) {
        int layers = EchoCoreServices.mapLayers(player).size();
        int markers = EchoCoreServices.mapMarkers(player).size();
        int providers = EchoCoreServices.mapMarkerService().providerCount();
        player.sendSystemMessage(Component.translatable("command.echoholomap.dump", layers, markers, providers));
        HoloMapSync.send(player);
        return markers;
    }

    private static int scanTerrain(ServerPlayer player, int radius) {
        if (!debugEnabled()) {
            player.sendSystemMessage(Component.literal("ECHO HoloMap // Debug terrain commands are disabled."));
            return 0;
        }
        int safeRadius = Math.max(0, Math.min(24, radius));
        int maxChunks = Math.max(1, (safeRadius * 2 + 1) * (safeRadius * 2 + 1));
        int sampled = HoloMapTerrainScanner.scanAround(player, safeRadius, maxChunks);
        player.sendSystemMessage(Component.translatable("command.echoholomap.terrain_scanned", sampled, safeRadius));
        sendTerrainAround(player, safeRadius);
        return sampled;
    }

    private static int resampleTerrain(ServerPlayer player, int radius) {
        if (!debugEnabled()) {
            player.sendSystemMessage(Component.literal("ECHO HoloMap // Debug terrain commands are disabled."));
            return 0;
        }
        int safeRadius = Math.max(0, Math.min(24, radius));
        int maxChunks = Math.max(1, (safeRadius * 2 + 1) * (safeRadius * 2 + 1));
        int sampled = HoloMapTerrainScanner.scanAround(player, safeRadius, maxChunks, true);
        player.sendSystemMessage(Component.translatable("command.echoholomap.terrain_resampled", sampled, safeRadius));
        sendTerrainAround(player, safeRadius);
        return sampled;
    }

    private static int clearTerrain(ServerPlayer player) {
        if (!debugEnabled() || !(player.level() instanceof ServerLevel serverLevel)) {
            player.sendSystemMessage(Component.literal("ECHO HoloMap // Debug terrain commands are disabled."));
            return 0;
        }
        int cleared = HoloMapTerrainSavedData.get(serverLevel).clear(player.getUUID());
        player.sendSystemMessage(Component.translatable("command.echoholomap.terrain_cleared", cleared));
        sendTerrainAround(player, configuredScanRadius());
        return cleared;
    }

    private static int dumpTerrain(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return 0;
        }
        int count = HoloMapTerrainSavedData.get(serverLevel)
                .discoverableTileCount(player.getUUID(), serverLevel.dimension());
        HoloMapTerrainSavedData.TerrainStats stats = HoloMapTerrainSavedData.get(serverLevel)
                .stats(player.getUUID(), serverLevel.dimension());
        player.sendSystemMessage(Component.translatable("command.echoholomap.terrain_dump", count,
                serverLevel.dimension().identifier().toString(), stats.summary()));
        sendTerrainAround(player, configuredScanRadius());
        return count;
    }

    private static void sendTerrainAround(ServerPlayer player, int radius) {
        HoloMapTileRequestPacket request = new HoloMapTileRequestPacket(
                player.level().dimension().identifier().toString(),
                Math.floorDiv(player.blockPosition().getX(), 16),
                Math.floorDiv(player.blockPosition().getZ(), 16),
                radius);
        EchoNetSend.toPlayer(player, HoloMapTileBatchPacket.from(player, request), EchoPacketKind.CLIENTBOUND_SYNC);
    }

    private static boolean debugEnabled() {
        try {
            return Config.DEBUG_MARKERS.get();
        } catch (RuntimeException exception) {
            return true;
        }
    }

    private static int configuredScanRadius() {
        try {
            return Math.max(0, Math.min(24, Config.TERRAIN_SCAN_RADIUS.get()));
        } catch (RuntimeException exception) {
            return 5;
        }
    }
}
