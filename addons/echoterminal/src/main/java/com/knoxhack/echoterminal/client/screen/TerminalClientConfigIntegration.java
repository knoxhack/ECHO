package com.knoxhack.echoterminal.client.screen;

import com.knoxhack.echocore.api.config.EchoConfigCategory;
import com.knoxhack.echocore.api.config.EchoConfigEntry;
import com.knoxhack.echocore.api.config.EchoConfigModule;
import com.knoxhack.echocore.api.config.EchoConfigProvider;
import com.knoxhack.echocore.api.config.EchoConfigRegistry;
import com.knoxhack.echocore.api.config.EchoConfigSide;
import com.knoxhack.echoterminal.EchoTerminal;
import java.util.List;

public final class TerminalClientConfigIntegration {
    private TerminalClientConfigIntegration() {
    }

    public static void register() {
        EchoConfigRegistry.register(EchoConfigProvider.of(EchoTerminal.MODID, () -> new EchoConfigModule(
                EchoTerminal.MODID,
                "ECHO Terminal",
                List.of(
                        new EchoConfigCategory("navigation", "Navigation", List.of(
                                EchoConfigEntry.enumEntry("navigation_style", "Navigation Style",
                                        "Terminal navigation layout.",
                                        EchoConfigSide.CLIENT,
                                        TerminalClientOptions.NavigationStyle.APP_HUB,
                                        TerminalClientOptions.NavigationStyle.class,
                                        () -> TerminalClientOptions.navigationStyle,
                                        TerminalClientOptions::selectNavigationStyle,
                                        null, true, false, false),
                                EchoConfigEntry.enumEntry("mission_view", "Mission View",
                                        "Mission presentation mode.",
                                        EchoConfigSide.CLIENT,
                                        TerminalClientOptions.MissionView.VISUAL_QUEST_HUB,
                                        TerminalClientOptions.MissionView.class,
                                        () -> TerminalClientOptions.missionView,
                                        TerminalClientOptions::selectMissionView,
                                        null, true, false, false))),
                        new EchoConfigCategory("presentation", "Presentation", List.of(
                                EchoConfigEntry.enumEntry("interface_density", "Interface Density",
                                        "Terminal information density.",
                                        EchoConfigSide.CLIENT,
                                        TerminalClientOptions.InterfaceDensity.BALANCED,
                                        TerminalClientOptions.InterfaceDensity.class,
                                        () -> TerminalClientOptions.interfaceDensity,
                                        TerminalClientOptions::selectInterfaceDensity,
                                        null, true, false, false),
                                EchoConfigEntry.enumEntry("terminal_zoom", "Terminal Zoom",
                                        "Terminal interface zoom.",
                                        EchoConfigSide.CLIENT,
                                        TerminalClientOptions.TerminalZoom.ZOOM_100,
                                        TerminalClientOptions.TerminalZoom.class,
                                        () -> TerminalClientOptions.terminalZoom,
                                        TerminalClientOptions::selectTerminalZoom,
                                        null, true, false, false),
                                EchoConfigEntry.enumEntry("visual_level", "Visual Level",
                                        "Terminal visual treatment level.",
                                        EchoConfigSide.CLIENT,
                                        TerminalClientOptions.VisualLevel.BALANCED,
                                        TerminalClientOptions.VisualLevel.class,
                                        () -> TerminalClientOptions.visualLevel,
                                        TerminalClientOptions::selectVisualLevel,
                                        null, true, false, false),
                                EchoConfigEntry.booleanEntry("reduced_motion", "Reduced Motion",
                                        "Reduce terminal motion and heavier animated treatments.",
                                        EchoConfigSide.CLIENT,
                                        false,
                                        TerminalClientOptions::reduceMotion,
                                        TerminalClientOptions::setReducedMotion,
                                        null, true, false, false),
                                EchoConfigEntry.booleanEntry("mission_hud_notifications", "Mission HUD Notifications",
                                        "Show mission HUD notifications.",
                                        EchoConfigSide.CLIENT,
                                        true,
                                        () -> TerminalClientOptions.missionHudNotifications,
                                        TerminalClientOptions::setMissionHudNotifications,
                                        null, true, false, false)))))));
    }
}
