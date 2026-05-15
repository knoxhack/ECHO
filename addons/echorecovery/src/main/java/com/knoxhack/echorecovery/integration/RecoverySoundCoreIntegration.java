package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;

public final class RecoverySoundCoreIntegration {
    private RecoverySoundCoreIntegration() {}
    public static void registerCommon() {
        EchoRecovery.LOGGER.info("SoundCore integration registered.");
    }
}
