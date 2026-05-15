package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoveryMissionCoreIntegration {
    private RecoveryMissionCoreIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("MissionCore integration registered.");
    }
}
