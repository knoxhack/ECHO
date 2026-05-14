package com.knoxhack.echotutorialcore.integration.powergrid;

import com.knoxhack.echotutorialcore.EchoTutorialCore;

public final class TutorialPowerGridIntegration {
    private TutorialPowerGridIntegration() {}

    public static void register() {
        EchoTutorialCore.LOGGER.info("ECHO: TutorialCore integrated with PowerGrid. Power event receiver scaffold registered.");
        // Future: subscribe to PowerGrid events for no-power, breaker trip, brownout, overload hints.
    }
}
