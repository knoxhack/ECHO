package com.knoxhack.echosoundcore.integration.worldcore;

import com.knoxhack.echosoundcore.EchoSoundCore;

public final class SoundCoreWorldCoreIntegration {
    private SoundCoreWorldCoreIntegration() {}

    public static void register() {
        EchoSoundCore.LOGGER.info("SoundCore WorldCore integration registered.");
        // Future: read region/hazard context for ambience.
    }
}
