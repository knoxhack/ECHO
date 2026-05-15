package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryRenderCoreIntegration {
    private RecoveryRenderCoreIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryRenderCoreIntegration integration registered.");
    }
}
