package com.knoxhack.echopowergrid.integration.terminal;

import com.knoxhack.echopowergrid.EchoPowerGrid;
import com.knoxhack.echopowergrid.network.PowerGridNetworkSummaryPacket;
import com.knoxhack.echonetcore.api.EchoNetSend;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import com.knoxhack.echoterminal.api.TerminalArchiveEntry;
import com.knoxhack.echoterminal.api.TerminalArchiveRegistry;
import com.knoxhack.echoterminal.api.mission.TerminalMissionActions;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;

public final class PowerGridTerminalIntegration {
    private static boolean registered;

    private PowerGridTerminalIntegration() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        TerminalMissionActions.registerForTab(PowerGridTerminalIds.TAB);
        registerActions();
        registerArchive();
        EchoPowerGrid.LOGGER.info("ECHO PowerGrid terminal integration registered.");
    }

    private static void registerActions() {
        TerminalActionRegistry.register(PowerGridTerminalIds.TAB, PowerGridTerminalIds.STATUS_ACTION, (player, payload) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                EchoNetSend.toPlayer(serverPlayer, PowerGridNetworkSummaryPacket.current(serverPlayer));
            }
        });
    }

    private static void registerArchive() {
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
            PowerGridTerminalIds.id("archive/power_basics"),
            "Power Grid",
            "EP Basics",
            "ACTIVE",
            List.of(
                "EP = Echo Power. The standard unit across ECHO infrastructure.",
                "Generators produce EP. Batteries store EP. Cables transfer EP. Consumers use EP.",
                "Grid state: STABLE, CHARGING, DISCHARGING, BROWNOUT, OVERLOADED, TRIPPED, EMERGENCY."
            ),
            false
        ));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
            PowerGridTerminalIds.id("archive/generators"),
            "Power Grid",
            "Generators",
            "ONLINE",
            List.of(
                "Hand Crank: 5 EP/t, manual, scrap tier.",
                "Scrap Burner: 40 EP/t, burns fuel, outpost tier.",
                "Solar Panel: 10 EP/t during daylight, outpost tier."
            ),
            false
        ));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
            PowerGridTerminalIds.id("archive/brownout"),
            "Power Grid",
            "Brownout & Overload",
            "RECOVERED",
            List.of(
                "Brownout: demand exceeds supply. Consumers receive partial power.",
                "Overload: flow exceeds cable or transfer limits. Breakers may trip.",
                "Emergency Breakers can be reset by right-click."
            ),
            false
        ));
    }
}
