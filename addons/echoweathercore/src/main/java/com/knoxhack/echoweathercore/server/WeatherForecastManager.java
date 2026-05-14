package com.knoxhack.echoweathercore.server;

import com.knoxhack.echoweathercore.api.forecast.WeatherForecast;
import com.knoxhack.echoweathercore.api.weather.ActiveWeatherEvent;
import com.knoxhack.echoweathercore.api.weather.WeatherEffectModifiers;
import com.knoxhack.echoweathercore.api.weather.WeatherPhase;
import com.knoxhack.echoweathercore.api.weather.WeatherProfile;
import com.knoxhack.echoweathercore.api.weather.WeatherRouteRisk;
import com.knoxhack.echoweathercore.api.weather.WeatherSeverity;
import com.knoxhack.echoweathercore.data.WeatherDataReloadListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class WeatherForecastManager {
    private WeatherForecastManager() {}

    public static List<WeatherForecast> getForecastForPlayer(ServerPlayer player) {
        Level level = player.level();
        if (level.isClientSide()) return List.of();
        List<WeatherForecast> forecasts = new ArrayList<>();
        long tick = level.getGameTime();

        for (ActiveWeatherEvent event : WeatherStateManager.getInstance().getEventsAt(level, player.blockPosition())) {
            WeatherProfile profile = WeatherDataReloadListener.INSTANCE.getProfile(event.profileId());
            if (profile == null) continue;

            WeatherRouteRisk risk = WeatherRouteRisk.SAFE;
            if (event.severity() == WeatherSeverity.MODERATE) risk = WeatherRouteRisk.WATCH;
            else if (event.severity() == WeatherSeverity.SEVERE) risk = WeatherRouteRisk.HAZARDOUS;
            else if (event.severity() == WeatherSeverity.EXTREME) risk = WeatherRouteRisk.DELAY_RECOMMENDED;

            long eta = event.startTick() > tick ? event.startTick() - tick : 0;
            String shelter = event.type().name().toLowerCase().contains("radiation") ? "Shielded shelter recommended." : "Seek shelter if available.";

            forecasts.add(new WeatherForecast(
                event.profileId(), event.type(), profile.displayName(), event.phase(), event.severity(), eta,
                event.regionId() != null ? event.regionId().toString() : "Unknown",
                (int) (event.endTick() - event.startTick()), profile.effects(), profile.recommendedGear(),
                shelter, risk, (int) (profile.effects().scannerReliabilityMultiplier() * 100) + "%",
                profile.echoLines()
            ));
        }
        return forecasts;
    }

    public static WeatherForecast getCurrentWeatherForPlayer(ServerPlayer player) {
        List<WeatherForecast> forecasts = getForecastForPlayer(player);
        for (WeatherForecast f : forecasts) {
            if (f.phase() == WeatherPhase.ACTIVE || f.phase() == WeatherPhase.CRITICAL) return f;
        }
        return forecasts.isEmpty() ? null : forecasts.get(0);
    }

    public static String formatForecast(WeatherForecast forecast) {
        StringBuilder sb = new StringBuilder();
        sb.append("Weather: ").append(forecast.displayName()).append("\n");
        sb.append("Phase: ").append(forecast.phase()).append("\n");
        sb.append("Severity: ").append(forecast.severity()).append("\n");
        if (forecast.etaTicks() > 0) sb.append("ETA: ").append(forecast.etaTicks() / 20).append("s\n");
        sb.append("Route Risk: ").append(forecast.routeRisk()).append("\n");
        if (!forecast.recommendedGear().isEmpty()) {
            sb.append("Recommended Gear:\n");
            for (Identifier gear : forecast.recommendedGear()) sb.append(" - ").append(gear).append("\n");
        }
        return sb.toString();
    }
}
