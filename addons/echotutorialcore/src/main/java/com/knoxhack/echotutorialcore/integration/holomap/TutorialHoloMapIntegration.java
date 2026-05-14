package com.knoxhack.echotutorialcore.integration.holomap;

import com.knoxhack.echotutorialcore.EchoTutorialCore;

public final class TutorialHoloMapIntegration {
    private TutorialHoloMapIntegration() {}

    public static void register() {
        EchoTutorialCore.LOGGER.info("ECHO: TutorialCore integrated with HoloMap. Route prep warning provider scaffold registered.");
        // Future: register route prep warnings and signal lead hints with HoloMap.
    }
}
