package com.knoxhack.echorecovery.integration;

import com.knoxhack.echorecovery.EchoRecovery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

public final class RecoveryIntegrationDispatcher {
    private RecoveryIntegrationDispatcher() {}

    public static void registerCommon() {
        try {
            if (ModList.get().isLoaded("echoterminal")) {
                load("RecoveryTerminalIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echothemecore")) {
                load("RecoveryThemeCoreIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echomissioncore")) {
                load("RecoveryMissionCoreIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echotutorialcore")) {
                load("RecoveryTutorialCoreIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echosoundcore")) {
                load("RecoverySoundCoreIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echoholomap")) {
                load("RecoveryHoloMapIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echoindex")) {
                load("RecoveryIndexIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echoworldcore")) {
                load("RecoveryWorldCoreIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echoruntimeguard")) {
                load("RecoveryRuntimeGuardIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echoweathercore")) {
                load("RecoveryWeatherCoreIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echolens")) {
                load("RecoveryLensIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echoarmory")) {
                load("RecoveryArmoryIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echologisticsnetwork")) {
                load("RecoveryLogisticsIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echoconvoyprotocol")) {
                load("RecoveryConvoyIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echopowergrid")) {
                load("RecoveryPowerGridIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echorelictech")) {
                load("RecoveryRelicTechIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echonexusprotocol")) {
                load("RecoveryNexusIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echoblackboxprotocol")) {
                load("RecoveryBlackboxIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echoashfallprotocol")) {
                load("RecoveryAshfallIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echorendercore")) {
                load("RecoveryRenderCoreIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echodatacore")) {
                load("RecoveryDataCoreIntegration", "registerCommon");
            }
            if (ModList.get().isLoaded("echoplayercore")) {
                load("RecoveryPlayerCoreIntegration", "registerCommon");
            }
        } catch (Exception e) {
            EchoRecovery.LOGGER.error("Error during integration dispatch", e);
        }
    }

    public static void onGraveCreated(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null) {
            return;
        }
        try {
            if (ModList.get().isLoaded("echoholomap")) {
                load("RecoveryHoloMapIntegration", "onGraveCreated", player, pos);
            }
        } catch (Exception e) {
            EchoRecovery.LOGGER.debug("HoloMap grave creation hook failed: {}", e.getMessage());
        }
    }

    private static void load(String className, String methodName, Object... args) {
        try {
            Class<?> clazz = Class.forName("com.knoxhack.echorecovery.integration." + className);
            if (args.length == 0) {
                clazz.getMethod(methodName).invoke(null);
            } else {
                Class<?>[] paramTypes = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) {
                    paramTypes[i] = args[i].getClass();
                }
                clazz.getMethod(methodName, paramTypes).invoke(null, args);
            }
        } catch (ReflectiveOperationException e) {
            EchoRecovery.LOGGER.warn("Integration {} not found or failed to register.", className);
        }
    }
}
