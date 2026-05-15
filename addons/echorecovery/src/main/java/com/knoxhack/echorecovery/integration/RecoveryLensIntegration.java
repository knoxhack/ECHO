package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryLensIntegration {
    private RecoveryLensIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryLensIntegration integration registered.");
    }
}
