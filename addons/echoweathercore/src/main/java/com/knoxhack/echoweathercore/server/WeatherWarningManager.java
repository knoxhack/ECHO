package com.knoxhack.echoweathercore.server;

import com.knoxhack.echoweathercore.EchoWeatherCore;
import com.knoxhack.echoweathercore.api.weather.ActiveWeatherEvent;
import com.knoxhack.echoweathercore.api.weather.WeatherPhase;
import com.knoxhack.echoweathercore.api.weather.WeatherProfile;
import com.knoxhack.echoweathercore.data.WeatherDataReloadListener;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class WeatherWarningManager {
    private static final Map<UUID, WeatherPhase> lastReportedPhase = new HashMap<>();

    private WeatherWarningManager() {}

    public static void broadcastForecast(ServerLevel level, ActiveWeatherEvent event) {
        WeatherProfile profile = WeatherDataReloadListener.INSTANCE.getProfile(event.profileId());
        if (profile == null) return;

        String msg = profile.terminalWarning();
        if (msg == null || msg.isEmpty()) {
            msg = "ECHO WEATHER ALERT: " + profile.displayName() + " likely. Prepare accordingly.";
        }
        for (ServerPlayer player : level.players()) {
            if (event.affectsPosition(player.blockPosition())) {
                player.sendSystemMessage(Component.literal(msg));
            }
        }
    }

    public static void notifyPhaseChange(ServerLevel level, ActiveWeatherEvent event) {
        WeatherProfile profile = WeatherDataReloadListener.INSTANCE.getProfile(event.profileId());
        if (profile == null) return;
        WeatherPhase last = lastReportedPhase.get(event.eventId());
        if (last == event.phase()) return;
        lastReportedPhase.put(event.eventId(), event.phase());

        String msg = switch (event.phase()) {
            case INCOMING -> "ECHO-7: " + profile.displayName() + " incoming. Shelter or reduce travel speed.";
            case ACTIVE -> "WEATHER EVENT ACTIVE: " + profile.displayName().toUpperCase() + ". Scanner range degraded.";
            case CRITICAL -> "ECHO-7: External conditions worsening. Expedition continuation not advised.";
            case CLEARING -> "Weather clearing. Scanner reliability restored.";
            default -> null;
        };

        if (msg == null) return;

        for (ServerPlayer player : level.players()) {
            if (event.affectsPosition(player.blockPosition())) {
                player.sendSystemMessage(Component.literal(msg));
            }
        }

        if (event.phase() == WeatherPhase.ENDED) {
            lastReportedPhase.remove(event.eventId());
        }
    }

    public static void sendPersonalWarning(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
