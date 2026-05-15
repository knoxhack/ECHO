package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryBlackboxIntegration {
    private RecoveryBlackboxIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("RecoveryBlackboxIntegration integration registered.");
    }
}
