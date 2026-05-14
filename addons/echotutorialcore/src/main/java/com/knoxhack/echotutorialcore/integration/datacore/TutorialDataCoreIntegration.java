package com.knoxhack.echotutorialcore.integration.datacore;

import com.knoxhack.echotutorialcore.EchoTutorialCore;

public final class TutorialDataCoreIntegration {
    private TutorialDataCoreIntegration() {}

    public static void register() {
        EchoTutorialCore.LOGGER.info("ECHO: TutorialCore integrated with DataCore. Persistent data bridge scaffold registered.");
        // Future: mirror tutorial progress to DataCore for cross-addon persistence.
    }
}
