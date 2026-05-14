package com.knoxhack.echoweathercore.server;

import com.knoxhack.echoweathercore.EchoWeatherCore;
import com.knoxhack.echoweathercore.api.weather.ActiveWeatherEvent;
import com.knoxhack.echoweathercore.api.weather.WeatherPhase;
import com.knoxhack.echoweathercore.api.weather.WeatherProfile;
import com.knoxhack.echoweathercore.api.weather.WeatherSeverity;
import com.knoxhack.echoweathercore.api.weather.WeatherType;
import com.knoxhack.echoweathercore.config.WeatherCoreConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class WeatherStateManager {
    private static final WeatherStateManager INSTANCE = new WeatherStateManager();
    private final Map<Identifier, List<ActiveWeatherEvent>> levelEvents = new ConcurrentHashMap<>();
    private MinecraftServer server;

    private WeatherStateManager() {}

    public static WeatherStateManager getInstance() {
        return INSTANCE;
    }

    public void onServerStarting(MinecraftServer server) {
        this.server = server;
        for (ServerLevel level : server.getAllLevels()) {
            loadLevel(level);
        }
        EchoWeatherCore.LOGGER.info("WeatherStateManager loaded.");
    }

    public void onServerStopping() {
        if (server != null) {
            for (ServerLevel level : server.getAllLevels()) {
                saveLevel(level);
            }
        }
        this.server = null;
        levelEvents.clear();
    }

    public void tickLevel(Level level) {
        if (level.isClientSide() || !(level instanceof ServerLevel sl)) return;
        Identifier dimension = level.dimension().identifier();
        List<ActiveWeatherEvent> events = levelEvents.getOrDefault(dimension, new ArrayList<>());
        long tick = level.getGameTime();
        List<ActiveWeatherEvent> updated = new ArrayList<>();
        for (ActiveWeatherEvent event : events) {
            WeatherPhase phase = computePhase(event, tick);
            if (phase == WeatherPhase.ENDED) {
                continue;
            }
            if (phase != event.phase()) {
                event = new ActiveWeatherEvent(event.eventId(), event.profileId(), event.type(), event.severity(),
                    event.scope(), phase, event.startTick(), event.endTick(), event.warningStartTick(),
                    event.centerPos(), event.radius(), event.regionId(), event.movementDirection(),
                    event.sourceReason(), event.generatedResources(), event.debugMetadata());
                WeatherWarningManager.notifyPhaseChange(sl, event);
            }
            updated.add(event);
        }
        levelEvents.put(dimension, updated);
    }

    private WeatherPhase computePhase(ActiveWeatherEvent event, long tick) {
        if (tick >= event.endTick()) return WeatherPhase.ENDED;
        if (tick >= event.startTick() + (event.endTick() - event.startTick()) * 0.85) return WeatherPhase.CLEARING;
        if (tick >= event.startTick() + (event.endTick() - event.startTick()) * 0.6) return WeatherPhase.CRITICAL;
        if (tick >= event.startTick()) return WeatherPhase.ACTIVE;
        if (tick >= event.warningStartTick() + (event.startTick() - event.warningStartTick()) * 0.5) return WeatherPhase.INCOMING;
        return WeatherPhase.FORECAST;
    }

    public List<ActiveWeatherEvent> getActiveEvents(Level level) {
        if (level.isClientSide()) return List.of();
        Identifier dimension = level.dimension().identifier();
        long tick = level.getGameTime();
        List<ActiveWeatherEvent> result = new ArrayList<>();
        for (ActiveWeatherEvent event : levelEvents.getOrDefault(dimension, List.of())) {
            if (event.isActive(tick)) result.add(event);
        }
        return Collections.unmodifiableList(result);
    }

    public List<ActiveWeatherEvent> getEventsAt(Level level, BlockPos pos) {
        if (level.isClientSide()) return List.of();
        long tick = level.getGameTime();
        List<ActiveWeatherEvent> result = new ArrayList<>();
        for (ActiveWeatherEvent event : getActiveEvents(level)) {
            if (event.affectsPosition(pos)) result.add(event);
        }
        return result;
    }

    public ActiveWeatherEvent startEvent(ServerLevel level, WeatherProfile profile, WeatherSeverity severity, BlockPos center, int radius, String source) {
        if (!WeatherCoreConfig.ENABLE_WEATHER_CORE.get()) return null;
        long tick = level.getGameTime();
        long warningTicks = Math.max(profile.warningTicks(), WeatherCoreConfig.MINIMUM_WARNING_TICKS.get());
        long duration = profile.durationTicks();
        long warningStart = tick;
        long start = tick + warningTicks;
        long end = start + duration;

        ActiveWeatherEvent event = new ActiveWeatherEvent(
            UUID.randomUUID(), profile.id(), profile.type(), severity, profile.scope(),
            WeatherPhase.FORECAST, start, end, warningStart, center, radius, null, null, source, null, null
        );

        Identifier dimension = level.dimension().identifier();
        levelEvents.computeIfAbsent(dimension, k -> new ArrayList<>()).add(event);
        WeatherWarningManager.broadcastForecast(level, event);
        saveLevel(level);
        EchoWeatherCore.LOGGER.info("Started weather event {} in dimension {}", profile.id(), dimension);
        return event;
    }

    public void clearEvents(ServerLevel level, WeatherType type) {
        Identifier dimension = level.dimension().identifier();
        List<ActiveWeatherEvent> events = levelEvents.getOrDefault(dimension, new ArrayList<>());
        events.removeIf(e -> e.type() == type);
        saveLevel(level);
    }

    public void clearAllEvents(ServerLevel level) {
        Identifier dimension = level.dimension().identifier();
        levelEvents.put(dimension, new ArrayList<>());
        saveLevel(level);
    }

    private void loadLevel(ServerLevel level) {
        WeatherSavedData data = WeatherSavedData.get(level);
        Identifier dimension = level.dimension().identifier();
        levelEvents.put(dimension, new ArrayList<>(data.getEvents()));
    }

    private void saveLevel(ServerLevel level) {
        Identifier dimension = level.dimension().identifier();
        List<ActiveWeatherEvent> events = levelEvents.getOrDefault(dimension, new ArrayList<>());
        WeatherSavedData data = WeatherSavedData.get(level);
        data.setEvents(events);
    }
}
