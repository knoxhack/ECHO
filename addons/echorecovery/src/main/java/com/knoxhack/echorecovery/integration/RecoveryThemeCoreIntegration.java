package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryThemeCoreIntegration {
    private RecoveryThemeCoreIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("ThemeCore integration registered.");
    }
}
