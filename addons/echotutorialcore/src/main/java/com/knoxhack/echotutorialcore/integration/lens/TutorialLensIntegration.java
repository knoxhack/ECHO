package com.knoxhack.echotutorialcore.integration.lens;

import com.knoxhack.echotutorialcore.EchoTutorialCore;

public final class TutorialLensIntegration {
    private TutorialLensIntegration() {}

    public static void register() {
        EchoTutorialCore.LOGGER.info("ECHO: TutorialCore integrated with Lens. Lens assist row provider scaffold registered.");
        // Future: register Lens providers for machine troubleshooting hints.
    }
}
