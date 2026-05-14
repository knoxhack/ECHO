package com.knoxhack.echosoundcore.integration.mission;

import com.knoxhack.echosoundcore.EchoSoundCore;

public final class SoundCoreMissionIntegration {
    private SoundCoreMissionIntegration() {}

    public static void register() {
        EchoSoundCore.LOGGER.info("SoundCore MissionCore integration registered.");
        // Future: hook into mission events to trigger stingers.
    }
}
