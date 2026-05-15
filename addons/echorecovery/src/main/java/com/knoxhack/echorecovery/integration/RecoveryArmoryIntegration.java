package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryArmoryIntegration {
    private RecoveryArmoryIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryArmoryIntegration integration registered.");
    }
}
