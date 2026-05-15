package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryRuntimeGuardIntegration {
    private RecoveryRuntimeGuardIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryRuntimeGuardIntegration integration registered.");
    }
}
