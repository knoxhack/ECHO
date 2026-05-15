package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryRelicTechIntegration {
    private RecoveryRelicTechIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryRelicTechIntegration integration registered.");
    }
}
