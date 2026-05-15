package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryDataCoreIntegration {
    private RecoveryDataCoreIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryDataCoreIntegration integration registered.");
    }
}
