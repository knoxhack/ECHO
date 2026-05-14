package com.knoxhack.echotutorialcore.integration.mission;

import com.knoxhack.echotutorialcore.EchoTutorialCore;

public final class TutorialMissionCoreIntegration {
    private TutorialMissionCoreIntegration() {}

    public static void register() {
        EchoTutorialCore.LOGGER.info("ECHO: TutorialCore integrated with MissionCore. Tutorial flow bridge scaffold registered.");
        // Future: register tutorial missions/flows with MissionCore registry.
    }
}
