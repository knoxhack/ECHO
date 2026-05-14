package com.knoxhack.echothemecore.network;

import com.knoxhack.echothemecore.EchoThemeCore;
import com.knoxhack.echothemecore.config.ThemeCoreConfig;
import com.knoxhack.echothemecore.content.ThemeRegistry;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ThemeCoreServerSync {
    private static MinecraftServer currentServer;

    private ThemeCoreServerSync() {
    }

    public static void onServerStarted(ServerStartedEvent event) {
        currentServer = event.getServer();
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        if (currentServer == event.getServer()) {
            currentServer = null;
        }
    }

    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!ThemeCoreConfig.syncServerTheme()) {
            return;
        }
        Identifier themeId = ThemeRegistry.getThemeFor(player).id();
        sendToPlayer(player, themeId);
        EchoThemeCore.LOGGER.debug("Sent theme sync {} to player {}", themeId, player.getScoreboardName());
    }

    public static void broadcastGlobalTheme(Identifier themeId) {
        if (!ThemeCoreConfig.syncServerTheme()) {
            return;
        }
        PacketDistributor.sendToAllPlayers(new ThemeSyncPacket(themeId));
        EchoThemeCore.LOGGER.debug("Broadcast global theme sync {}", themeId);
    }

    public static void sendPlayerTheme(ServerPlayer player, Identifier themeId) {
        if (!ThemeCoreConfig.syncServerTheme()) {
            return;
        }
        sendToPlayer(player, themeId);
    }

    public static void sendPlayerTheme(UUID playerId, Identifier themeId) {
        if (playerId == null) {
            return;
        }
        MinecraftServer server = currentServer;
        if (server == null) {
            return;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player != null) {
            sendPlayerTheme(player, themeId);
        }
    }

    private static void sendToPlayer(ServerPlayer player, Identifier themeId) {
        try {
            PacketDistributor.sendToPlayer(player, new PlayerThemeSyncPacket(themeId));
        } catch (UnsupportedOperationException | IllegalStateException exception) {
            EchoThemeCore.LOGGER.debug("Failed to send theme sync to player: {}", exception.getMessage());
        }
    }
}
