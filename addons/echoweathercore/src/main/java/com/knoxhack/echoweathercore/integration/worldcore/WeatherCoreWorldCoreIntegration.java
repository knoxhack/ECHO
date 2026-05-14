package com.knoxhack.echoweathercore.integration.worldcore;

import com.knoxhack.echoweathercore.EchoWeatherCore;

public final class WeatherCoreWorldCoreIntegration {
    private WeatherCoreWorldCoreIntegration() {}

    public static void register() {
        EchoWeatherCore.LOGGER.info("WeatherCore WorldCore integration scaffold registered.");
    }
}
