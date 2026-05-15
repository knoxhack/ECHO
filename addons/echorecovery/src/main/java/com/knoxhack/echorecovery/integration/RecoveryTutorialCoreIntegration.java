package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryTutorialCoreIntegration {
    private RecoveryTutorialCoreIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("TutorialCore integration registered.");
    }
}
