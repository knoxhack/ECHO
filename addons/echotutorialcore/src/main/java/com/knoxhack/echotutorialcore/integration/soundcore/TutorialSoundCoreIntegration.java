package com.knoxhack.echotutorialcore.integration.soundcore;

import com.knoxhack.echotutorialcore.EchoTutorialCore;

public final class TutorialSoundCoreIntegration {
    private TutorialSoundCoreIntegration() {}

    public static void register() {
        EchoTutorialCore.LOGGER.info("ECHO: TutorialCore integrated with SoundCore. Tutorial sound event bridge scaffold registered.");
        // Future: bridge tutorial notification sounds to SoundCore events.
    }
}
