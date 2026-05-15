package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryWorldCoreIntegration {
    private RecoveryWorldCoreIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryWorldCoreIntegration integration registered.");
    }
}
