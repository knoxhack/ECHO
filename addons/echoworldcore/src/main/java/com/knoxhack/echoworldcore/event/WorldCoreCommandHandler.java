package com.knoxhack.echoworldcore.event;

import com.knoxhack.echocore.api.WorldHazardSnapshot;
import com.knoxhack.echocore.api.WorldMarker;
import com.knoxhack.echocore.api.WorldRegionDefinition;
import com.knoxhack.echocore.api.WorldRegionInstance;
import com.knoxhack.echocore.api.WorldDiscoverySource;
import com.knoxhack.echoworldcore.Config;
import com.knoxhack.echoworldcore.EchoWorldCore;
import com.knoxhack.echoworldcore.service.WorldRegionService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = EchoWorldCore.MODID)
public final class WorldCoreCommandHandler {
    private static final int DEFAULT_RADIUS = 256;

    private WorldCoreCommandHandler() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // Player-facing subcommands
        event.getDispatcher().register(
                Commands.literal("echoworld")
                        .then(Commands.literal("current")
                                .executes(ctx -> current(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("hazard")
                                .executes(ctx -> playerHazard(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("nearby")
                                .executes(ctx -> playerNearby(ctx.getSource().getPlayerOrException(), DEFAULT_RADIUS))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(16, 4096))
                                        .executes(ctx -> playerNearby(ctx.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(ctx, "radius"))))));

        // Gamemaster debug subcommands
        event.getDispatcher().register(
                Commands.literal("echoworld")
                        .requires(source -> Config.debugCommandsEnabled()
                                && source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("list")
                                .executes(ctx -> list(ctx.getSource().getPlayerOrException(), "all"))
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .executes(ctx -> list(ctx.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(ctx, "type")))))
                        .then(Commands.literal("nearby")
                                .executes(ctx -> nearby(ctx.getSource().getPlayerOrException(), DEFAULT_RADIUS))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(16, 4096))
                                        .executes(ctx -> nearby(ctx.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(ctx, "radius")))))
                        .then(Commands.literal("reveal")
                                .then(Commands.argument("region", StringArgumentType.word())
                                        .executes(ctx -> reveal(ctx.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(ctx, "region")))))
                        .then(Commands.literal("hazard")
                                .executes(ctx -> hazard(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("markers")
                                .executes(ctx -> markers(ctx.getSource().getPlayerOrException(), DEFAULT_RADIUS))
                                .then(Commands.argument("radius", IntegerArgumentType.integer(16, 4096))
                                        .executes(ctx -> markers(ctx.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(ctx, "radius")))))
                        .then(Commands.literal("validate")
                                .executes(ctx -> validate(ctx.getSource().getPlayerOrException()))));
    }

    private static int current(ServerPlayer player) {
        var current = WorldRegionService.INSTANCE.currentRegion(player);
        if (current.isPresent()) {
            var region = current.get();
            tell(player, "Current region: " + region.displayName()
                    + " [" + region.definitionId() + "]", ChatFormatting.AQUA);
            if (!region.hazardIds().isEmpty()) {
                tell(player, "  Hazards: " + region.hazardIds().size(), ChatFormatting.GRAY);
            }
        } else {
            tell(player, "No shared region currently active.", ChatFormatting.YELLOW);
        }
        return current.isPresent() ? Command.SINGLE_SUCCESS : 0;
    }

    private static int playerHazard(ServerPlayer player) {
        WorldHazardSnapshot snapshot = WorldRegionService.INSTANCE.hazardSnapshot(player);
        tell(player, "Hazard " + (snapshot.safeZone() ? "NOMINAL" : "SEVERITY " + snapshot.severity()),
                snapshot.safeZone() ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
        tell(player, "  Summary: " + snapshot.summary(), ChatFormatting.GRAY);
        if (!snapshot.regionIds().isEmpty()) {
            tell(player, "  Affected regions: " + snapshot.regionIds().size(), ChatFormatting.GRAY);
        }
        if (!snapshot.hazardIds().isEmpty()) {
            tell(player, "  Active hazards: " + snapshot.hazardIds(), ChatFormatting.GRAY);
        }
        return snapshot.safeZone() ? 0 : Command.SINGLE_SUCCESS;
    }

    private static int playerNearby(ServerPlayer player, int radius) {
        List<WorldMarker> markers = WorldRegionService.INSTANCE.nearbyMarkers(player.level(), player.blockPosition(), radius);
        if (markers.isEmpty()) {
            tell(player, "No shared world markers nearby.", ChatFormatting.YELLOW);
        } else {
            tell(player, "Nearby shared markers (" + markers.size() + "):", ChatFormatting.AQUA);
            for (WorldMarker marker : markers) {
                tell(player, "  - " + marker.displayName()
                        + " [" + marker.type() + "] at " + marker.pos().toShortString(), ChatFormatting.GRAY);
            }
        }
        List<WorldRegionInstance> regions = WorldRegionService.INSTANCE.nearbyRegions(player.level(), player.blockPosition(), radius);
        if (!regions.isEmpty()) {
            tell(player, "Nearby regions (" + regions.size() + "):", ChatFormatting.AQUA);
            for (WorldRegionInstance region : regions) {
                tell(player, "  - " + region.displayName() + " [" + region.type().displayName() + "]", ChatFormatting.GRAY);
            }
        }
        return markers.size() + regions.size();
    }

    private static int nearby(ServerPlayer player, int radius) {
        List<WorldRegionInstance> regions = WorldRegionService.INSTANCE.nearbyRegions(player.level(), player.blockPosition(), radius);
        if (regions.isEmpty()) {
            tell(player, "No shared world regions nearby.", ChatFormatting.YELLOW);
            return 0;
        }
        tell(player, "Nearby shared regions (" + regions.size() + "):", ChatFormatting.AQUA);
        for (WorldRegionInstance region : regions) {
            tell(player, " - " + region.definitionId() + " | " + region.type().displayName()
                    + " | " + region.displayName()
                    + " | hazards=" + region.hazardIds().size(), ChatFormatting.GRAY);
        }
        return regions.size();
    }

    private static int list(ServerPlayer player, String type) {
        String cleaned = type == null ? "all" : type.strip().toLowerCase(java.util.Locale.ROOT);
        if ("all".equals(cleaned) || "regions".equals(cleaned) || "region".equals(cleaned)) {
            List<WorldRegionDefinition> regions = WorldRegionService.INSTANCE.regionDefinitions();
            tell(player, "Region definitions (" + regions.size() + ", data="
                    + WorldRegionService.INSTANCE.dataRegionDefinitionCount() + "):", ChatFormatting.AQUA);
            for (WorldRegionDefinition region : regions) {
                tell(player, " - " + region.id() + " | " + region.type().displayName()
                        + " | hazards=" + region.hazardIds().size()
                        + " | radius=" + region.radius(), ChatFormatting.GRAY);
            }
            if (!"all".equals(cleaned)) {
                return regions.size();
            }
        }
        if ("all".equals(cleaned) || "hazards".equals(cleaned) || "hazard".equals(cleaned)) {
            var hazards = WorldRegionService.INSTANCE.hazardDefinitions();
            tell(player, "Hazard definitions (" + hazards.size() + ", data="
                    + WorldRegionService.INSTANCE.dataHazardDefinitionCount() + "):", ChatFormatting.AQUA);
            for (var hazard : hazards) {
                tell(player, " - " + hazard.id() + " | severity=" + hazard.defaultSeverity()
                        + " | ticking=" + hazard.ticking(), ChatFormatting.GRAY);
            }
            return hazards.size();
        }
        tell(player, "Unknown list type '" + type + "'. Use region, hazard, or all.", ChatFormatting.RED);
        return 0;
    }

    private static int reveal(ServerPlayer player, String rawRegion) {
        Identifier id = parseRegionId(rawRegion);
        if (id == null) {
            tell(player, "Invalid region id: " + rawRegion, ChatFormatting.RED);
            return 0;
        }
        if (WorldRegionService.INSTANCE.discoverRegion(player, id, WorldDiscoverySource.DEBUG)) {
            tell(player, "Revealed region " + id + ".", ChatFormatting.GREEN);
            return Command.SINGLE_SUCCESS;
        }
        if (WorldRegionService.INSTANCE.hasDiscoveredRegion(player, id)) {
            tell(player, "Region already discovered: " + id + ".", ChatFormatting.YELLOW);
            return Command.SINGLE_SUCCESS;
        }
        tell(player, "Unknown or unavailable region: " + id + ".", ChatFormatting.RED);
        return 0;
    }

    private static int hazard(ServerPlayer player) {
        WorldHazardSnapshot snapshot = WorldRegionService.INSTANCE.hazardSnapshot(player);
        tell(player, "Hazard " + (snapshot.safeZone() ? "NOMINAL" : "SEVERITY " + snapshot.severity())
                + " | " + snapshot.summary(), snapshot.safeZone() ? ChatFormatting.GREEN : ChatFormatting.YELLOW);
        if (!snapshot.regionIds().isEmpty()) {
            tell(player, "Regions: " + snapshot.regionIds(), ChatFormatting.GRAY);
        }
        if (!snapshot.hazardIds().isEmpty()) {
            tell(player, "Hazards: " + snapshot.hazardIds(), ChatFormatting.GRAY);
        }
        return snapshot.safeZone() ? 0 : Command.SINGLE_SUCCESS;
    }

    private static int markers(ServerPlayer player, int radius) {
        List<WorldMarker> markers = WorldRegionService.INSTANCE.nearbyMarkers(player.level(), player.blockPosition(), radius);
        if (markers.isEmpty()) {
            tell(player, "No shared world markers nearby.", ChatFormatting.YELLOW);
            return 0;
        }
        tell(player, "Nearby shared markers (" + markers.size() + "):", ChatFormatting.AQUA);
        for (WorldMarker marker : markers) {
            tell(player, " - " + marker.id() + " | " + marker.type()
                    + " | " + marker.displayName()
                    + " | " + marker.pos().toShortString(), ChatFormatting.GRAY);
        }
        return markers.size();
    }

    private static int validate(ServerPlayer player) {
        List<String> warnings = WorldRegionService.INSTANCE.validateMarkers(player.level());
        if (warnings.isEmpty()) {
            tell(player, "WorldCore validation passed. Regions="
                    + WorldRegionService.INSTANCE.regionDefinitions().size()
                    + ", hazards=" + WorldRegionService.INSTANCE.hazardDefinitions().size()
                    + ", markers=" + WorldRegionService.INSTANCE.markers(player).size() + ".", ChatFormatting.GREEN);
            return Command.SINGLE_SUCCESS;
        }
        tell(player, "WorldCore validation warnings:", ChatFormatting.YELLOW);
        for (String warning : warnings) {
            tell(player, " - " + warning, ChatFormatting.GRAY);
        }
        return 0;
    }

    private static Identifier parseRegionId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String cleaned = raw.strip();
        Identifier parsed = Identifier.tryParse(cleaned);
        if (parsed != null && !WorldRegionService.INSTANCE.regionDefinition(parsed).isEmpty()) {
            return parsed;
        }
        if (!cleaned.contains(":")) {
            Identifier ashfall = Identifier.fromNamespaceAndPath("echoashfallprotocol", cleaned);
            if (!WorldRegionService.INSTANCE.regionDefinition(ashfall).isEmpty()) {
                return ashfall;
            }
            Identifier worldcore = Identifier.fromNamespaceAndPath(EchoWorldCore.MODID, cleaned);
            if (!WorldRegionService.INSTANCE.regionDefinition(worldcore).isEmpty()) {
                return worldcore;
            }
        }
        return parsed;
    }

    private static void tell(ServerPlayer player, String message, ChatFormatting color) {
        player.sendSystemMessage(Component.literal("[ECHO WORLD] " + message).withStyle(color));
    }
}
