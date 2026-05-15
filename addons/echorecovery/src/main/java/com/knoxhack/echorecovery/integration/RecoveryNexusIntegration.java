package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryNexusIntegration {
    private RecoveryNexusIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryNexusIntegration integration registered.");
    }
}
