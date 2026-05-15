package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryLogisticsIntegration {
    private RecoveryLogisticsIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryLogisticsIntegration integration registered.");
    }
}
