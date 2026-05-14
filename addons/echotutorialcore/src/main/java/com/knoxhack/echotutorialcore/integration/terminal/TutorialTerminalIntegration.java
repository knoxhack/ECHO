package com.knoxhack.echotutorialcore.integration.terminal;

import com.knoxhack.echotutorialcore.EchoTutorialCore;

public final class TutorialTerminalIntegration {
    private static boolean registered;

    private TutorialTerminalIntegration() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        // Server-side actions and archives can be registered here.
        // Client-side tab registration is loaded reflectively to avoid classloading
        // Terminal client classes on dedicated servers.
        try {
            Class<?> clientIntegration = Class.forName(
                    "com.knoxhack.echotutorialcore.integration.terminal.TutorialTerminalClientIntegration");
            clientIntegration.getMethod("register").invoke(null);
        } catch (ClassNotFoundException e) {
            EchoTutorialCore.LOGGER.debug("TutorialTerminalClientIntegration not present (client-only).");
        } catch (ReflectiveOperationException | LinkageError e) {
            EchoTutorialCore.LOGGER.warn("TutorialTerminalClientIntegration could not be registered.", e);
        }

        EchoTutorialCore.LOGGER.info("ECHO: TutorialCore integrated with Terminal.");
    }
}
