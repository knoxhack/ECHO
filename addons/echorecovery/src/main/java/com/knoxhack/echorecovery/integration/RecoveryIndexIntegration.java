package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryIndexIntegration {
    private RecoveryIndexIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryIndexIntegration integration registered.");
    }
}
