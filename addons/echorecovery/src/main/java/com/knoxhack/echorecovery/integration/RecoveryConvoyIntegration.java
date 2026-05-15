package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryConvoyIntegration {
    private RecoveryConvoyIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryConvoyIntegration integration registered.");
    }
}
