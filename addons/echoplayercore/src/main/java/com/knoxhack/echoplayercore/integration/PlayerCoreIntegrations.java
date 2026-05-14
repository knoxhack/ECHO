package com.knoxhack.echoplayercore.integration;

import com.knoxhack.echoplayercore.EchoPlayerCore;
import net.neoforged.fml.ModList;

public final class PlayerCoreIntegrations {
    private PlayerCoreIntegrations() {
    }

    public static boolean dataCoreLoaded() {
        return ModList.get().isLoaded("echodatacore");
    }

    public static boolean worldCoreLoaded() {
        return ModList.get().isLoaded("echoworldcore");
    }

    public static boolean terminalLoaded() {
        return ModList.get().isLoaded("echoterminal");
    }

    public static boolean holoMapLoaded() {
        return ModList.get().isLoaded("echoholomap");
    }

    public static boolean runtimeGuardLoaded() {
        return ModList.get().isLoaded("echoruntimeguard");
    }

    public static void logIntegrationStatus() {
        EchoPlayerCore.LOGGER.info(
                "ECHO PlayerCore integrations - DataCore:{}, WorldCore:{}, Terminal:{}, HoloMap:{}, RuntimeGuard:{}",
                dataCoreLoaded(), worldCoreLoaded(), terminalLoaded(), holoMapLoaded(), runtimeGuardLoaded()
        );
    }
}
