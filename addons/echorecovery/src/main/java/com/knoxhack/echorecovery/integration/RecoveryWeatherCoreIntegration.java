package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryWeatherCoreIntegration {
    private RecoveryWeatherCoreIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryWeatherCoreIntegration integration registered.");
    }
}
