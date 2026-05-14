package com.knoxhack.echoweathercore.api;

import com.knoxhack.echoweathercore.api.forecast.WeatherForecast;
import com.knoxhack.echoweathercore.api.weather.ActiveWeatherEvent;
import com.knoxhack.echoweathercore.api.weather.WeatherEffectModifiers;
import com.knoxhack.echoweathercore.api.weather.WeatherPhase;
import com.knoxhack.echoweathercore.api.weather.WeatherProfile;
import com.knoxhack.echoweathercore.api.weather.WeatherRouteRisk;
import com.knoxhack.echoweathercore.api.weather.WeatherSeverity;
import com.knoxhack.echoweathercore.api.weather.WeatherType;
import com.knoxhack.echoweathercore.config.WeatherCoreConfig;
import com.knoxhack.echoweathercore.data.WeatherDataReloadListener;
import com.knoxhack.echoweathercore.server.WeatherCountermeasureManager;
import com.knoxhack.echoweathercore.server.WeatherForecastManager;
import com.knoxhack.echoweathercore.server.WeatherStateManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class WeatherCoreApi {
    private static final List<Consumer<ActiveWeatherEvent>> listeners = new ArrayList<>();

    private WeatherCoreApi() {}

    public static List<ActiveWeatherEvent> getCurrentWeather(Level level, BlockPos pos) {
        if (level.isClientSide()) return List.of();
        return WeatherStateManager.getInstance().getEventsAt(level, pos);
    }

    public static List<ActiveWeatherEvent> getActiveWeather(Level level) {
        if (level.isClientSide()) return List.of();
        return WeatherStateManager.getInstance().getActiveEvents(level);
    }

    public static List<WeatherForecast> getForecast(ServerPlayer player) {
        if (player.level().isClientSide()) return List.of();
        return WeatherForecastManager.getForecastForPlayer(player);
    }

    public static boolean isWeatherActive(Level level, WeatherType type) {
        if (level.isClientSide()) return false;
        for (ActiveWeatherEvent event : getActiveWeather(level)) {
            if (event.type() == type) return true;
        }
        return false;
    }

    public static WeatherSeverity getWeatherSeverity(Level level, BlockPos pos) {
        WeatherSeverity max = null;
        for (ActiveWeatherEvent event : getCurrentWeather(level, pos)) {
            if (max == null || event.severity().ordinal() > max.ordinal()) max = event.severity();
        }
        return max != null ? max : WeatherSeverity.LOW;
    }

    public static WeatherPhase getWeatherPhase(Level level, BlockPos pos) {
        WeatherPhase dominant = WeatherPhase.ENDED;
        for (ActiveWeatherEvent event : getCurrentWeather(level, pos)) {
            if (event.phase().ordinal() > dominant.ordinal()) dominant = event.phase();
        }
        return dominant;
    }

    public static WeatherEffectModifiers getWeatherModifiers(Level level, BlockPos pos) {
        WeatherEffectModifiers combined = WeatherEffectModifiers.DEFAULT;
        for (ActiveWeatherEvent event : getCurrentWeather(level, pos)) {
            WeatherProfile profile = WeatherDataReloadListener.INSTANCE.getProfile(event.profileId());
            if (profile != null) {
                combined = combined.merge(profile.effects());
            }
        }
        return combined;
    }

    public static double getScannerReliability(ServerPlayer player) {
        WeatherEffectModifiers mods = getWeatherModifiers(player.level(), player.blockPosition());
        return mods.scannerReliabilityMultiplier();
    }

    public static double getScannerRangeMultiplier(ServerPlayer player) {
        WeatherEffectModifiers mods = getWeatherModifiers(player.level(), player.blockPosition());
        return mods.scannerRangeMultiplier();
    }

    public static double getFilterDrainMultiplier(ServerPlayer player) {
        WeatherEffectModifiers mods = getWeatherModifiers(player.level(), player.blockPosition());
        return mods.filterDrainMultiplier();
    }

    public static double getPowerGridInstability(Level level, BlockPos pos) {
        if (!WeatherCoreConfig.ALLOW_POWER_GRID_DISRUPTION.get()) return 1.0;
        WeatherEffectModifiers mods = getWeatherModifiers(level, pos);
        return mods.powerGridInstabilityMultiplier();
    }

    public static WeatherRouteRisk getRouteWeatherRisk(ServerPlayer player, Identifier routeId) {
        return getRouteWeatherRisk(player.level(), player.blockPosition(), null);
    }

    public static WeatherRouteRisk getRouteWeatherRisk(Level level, BlockPos start, BlockPos end) {
        WeatherSeverity severity = getWeatherSeverity(level, start);
        return switch (severity) {
            case LOW -> WeatherRouteRisk.SAFE;
            case MODERATE -> WeatherRouteRisk.WATCH;
            case SEVERE -> WeatherRouteRisk.HAZARDOUS;
            case EXTREME -> WeatherRouteRisk.DELAY_RECOMMENDED;
        };
    }

    public static List<Identifier> getRecommendedGear(ServerPlayer player) {
        List<Identifier> gear = new ArrayList<>();
        for (ActiveWeatherEvent event : getCurrentWeather(player.level(), player.blockPosition())) {
            WeatherProfile profile = WeatherDataReloadListener.INSTANCE.getProfile(event.profileId());
            if (profile != null) gear.addAll(profile.recommendedGear());
        }
        return Collections.unmodifiableList(gear);
    }

    public static boolean isSheltered(Entity entity) {
        return isSheltered(entity.level(), entity.blockPosition());
    }

    public static boolean isSheltered(Level level, BlockPos pos) {
        return !level.canSeeSky(pos.above());
    }

    public static void registerWeatherProfile(WeatherProfile profile) {
        // Data-driven profiles are loaded via JSON; runtime registration is stored in the reload listener map if needed.
    }

    public static ActiveWeatherEvent triggerWeather(ServerLevel level, Identifier profileId, WeatherSeverity severity, BlockPos center, int radius) {
        WeatherProfile profile = WeatherDataReloadListener.INSTANCE.getProfile(profileId);
        if (profile == null) return null;
        return WeatherStateManager.getInstance().startEvent(level, profile, severity, center, radius, "api");
    }

    public static void clearWeather(ServerLevel level, WeatherType type) {
        WeatherStateManager.getInstance().clearEvents(level, type);
    }

    public static void clearAllWeather(ServerLevel level) {
        WeatherStateManager.getInstance().clearAllEvents(level);
    }

    public static void addWeatherListener(Consumer<ActiveWeatherEvent> listener) {
        listeners.add(listener);
    }

    public static void registerWeatherCountermeasure(WeatherType type, WeatherEffectModifiers modifiers) {
        // Stored in WeatherCountermeasureManager if runtime registration needed
    }

    public static void reportShelterEntered(ServerPlayer player, BlockPos shelterPos) {
        // Hook for tracking shelter usage statistics
    }

    public static String getDroneWeatherRisk(ServerPlayer player) {
        if (!WeatherCoreConfig.ALLOW_DRONE_WEATHER_EFFECTS.get()) return "Clear";
        WeatherEffectModifiers mods = getWeatherModifiers(player.level(), player.blockPosition());
        if (mods.droneScoutReliability() < 0.5) return "High Risk";
        if (mods.droneScoutReliability() < 0.8) return "Moderate Risk";
        return "Low Risk";
    }

    public static double getFactionWeatherActivity(String factionId, WeatherType weather) {
        if (!WeatherCoreConfig.ALLOW_FACTION_PATROL_RETREAT.get()) return 1.0;
        WeatherEffectModifiers mods = WeatherCountermeasureManager.getCountermeasureModifiers(weather);
        return mods.factionPatrolActivityMultiplier();
    }

    public static List<String> getLensRows(Level level, BlockPos pos) {
        List<String> rows = new ArrayList<>();
        for (ActiveWeatherEvent event : getCurrentWeather(level, pos)) {
            WeatherProfile profile = WeatherDataReloadListener.INSTANCE.getProfile(event.profileId());
            if (profile != null) {
                rows.add("Event: " + profile.displayName());
                rows.add("Intensity: " + event.severity());
                rows.add("Filter Drain: " + (int) ((profile.effects().filterDrainMultiplier() - 1.0) * 100) + "%");
                rows.add("Scanner Reliability: " + (int) (profile.effects().scannerReliabilityMultiplier() * 100) + "%");
            }
        }
        return rows;
    }
}
