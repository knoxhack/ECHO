package com.knoxhack.echorelictech.integration;

import com.knoxhack.echorelictech.EchoRelicTech;
import net.neoforged.fml.ModList;

public final class RelicTechIntegrations {
    private RelicTechIntegrations() {}

    public static void registerOptional() {
        if (ModList.get().isLoaded("echoterminal")) {
            tryInvoke("com.knoxhack.echorelictech.integration.terminal.RelicTechTerminalIntegration");
        }
        if (ModList.get().isLoaded("echolens")) {
            tryInvoke("com.knoxhack.echorelictech.integration.lens.RelicTechLensIntegration");
        }
        if (ModList.get().isLoaded("echoholomap")) {
            tryInvoke("com.knoxhack.echorelictech.integration.holomap.RelicTechHoloMapIntegration");
        }
        if (ModList.get().isLoaded("echopowergrid")) {
            tryInvoke("com.knoxhack.echorelictech.integration.powergrid.RelicTechPowerGridIntegration");
        }
        if (ModList.get().isLoaded("echoworldcore")) {
            tryInvoke("com.knoxhack.echorelictech.integration.worldcore.RelicTechWorldCoreIntegration");
        }
        if (ModList.get().isLoaded("echosoundcore")) {
            tryInvoke("com.knoxhack.echorelictech.integration.soundcore.RelicTechSoundCoreIntegration");
        }
        if (ModList.get().isLoaded("echonexusprotocol")) {
            tryInvoke("com.knoxhack.echorelictech.integration.nexus.RelicTechNexusIntegration");
        }
        if (ModList.get().isLoaded("echomissioncore")) {
            tryInvoke("com.knoxhack.echorelictech.integration.missioncore.RelicTechMissionCoreIntegration");
        }
    }

    private static void tryInvoke(String className) {
        try {
            Class.forName(className).getMethod("register").invoke(null);
        } catch (ClassNotFoundException e) {
            EchoRelicTech.LOGGER.debug("Optional integration {} not present.", className);
        } catch (ReflectiveOperationException | LinkageError e) {
            EchoRelicTech.LOGGER.warn("Optional integration {} could not be registered.", className, e);
        }
    }
}
