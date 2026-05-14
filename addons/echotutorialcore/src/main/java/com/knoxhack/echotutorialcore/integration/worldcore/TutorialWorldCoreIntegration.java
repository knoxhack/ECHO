package com.knoxhack.echotutorialcore.integration.worldcore;

import com.knoxhack.echotutorialcore.EchoTutorialCore;

public final class TutorialWorldCoreIntegration {
    private TutorialWorldCoreIntegration() {}

    public static void register() {
        EchoTutorialCore.LOGGER.info("ECHO: TutorialCore integrated with WorldCore. Hazard context receiver scaffold registered.");
        // Future: read hazard/region data from WorldCore for contextual hints.
    }
}
