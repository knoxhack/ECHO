package com.knoxhack.echotutorialcore.integration;

import com.knoxhack.echotutorialcore.EchoTutorialCore;
import net.neoforged.fml.ModList;

public final class TutorialIntegrations {
    private TutorialIntegrations() {}

    public static void registerOptionalIntegrations() {
        if (ModList.get().isLoaded("echoterminal")) {
            tryInvoke("com.knoxhack.echotutorialcore.integration.terminal.TutorialTerminalIntegration");
        }
        if (ModList.get().isLoaded("echomissioncore")) {
            tryInvoke("com.knoxhack.echotutorialcore.integration.mission.TutorialMissionCoreIntegration");
        }
        if (ModList.get().isLoaded("echopowergrid")) {
            tryInvoke("com.knoxhack.echotutorialcore.integration.powergrid.TutorialPowerGridIntegration");
        }
        if (ModList.get().isLoaded("echolens")) {
            tryInvoke("com.knoxhack.echotutorialcore.integration.lens.TutorialLensIntegration");
        }
        if (ModList.get().isLoaded("echoholomap")) {
            tryInvoke("com.knoxhack.echotutorialcore.integration.holomap.TutorialHoloMapIntegration");
        }
        if (ModList.get().isLoaded("echosoundcore")) {
            tryInvoke("com.knoxhack.echotutorialcore.integration.soundcore.TutorialSoundCoreIntegration");
        }
        if (ModList.get().isLoaded("echoworldcore")) {
            tryInvoke("com.knoxhack.echotutorialcore.integration.worldcore.TutorialWorldCoreIntegration");
        }
        if (ModList.get().isLoaded("echodatacore")) {
            tryInvoke("com.knoxhack.echotutorialcore.integration.datacore.TutorialDataCoreIntegration");
        }
    }

    private static void tryInvoke(String className) {
        try {
            Class.forName(className).getMethod("register").invoke(null);
        } catch (ClassNotFoundException e) {
            EchoTutorialCore.LOGGER.debug("Optional integration {} not present.", className);
        } catch (ReflectiveOperationException | LinkageError e) {
            EchoTutorialCore.LOGGER.warn("Optional integration {} could not be registered.", className, e);
        }
    }
}
