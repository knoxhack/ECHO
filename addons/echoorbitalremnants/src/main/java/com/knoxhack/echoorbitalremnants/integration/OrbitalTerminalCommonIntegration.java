package com.knoxhack.echoorbitalremnants.integration;

import com.knoxhack.echoorbitalremnants.item.EchoTerminalItem;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import com.knoxhack.echoterminal.api.TerminalArchiveEntry;
import com.knoxhack.echoterminal.api.TerminalArchiveRegistry;
import com.knoxhack.echoterminal.api.mission.TerminalMissionActions;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRegistry;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Common-side optional ECHO Terminal bridge. Rendering stays client-only, but
 * actions and mission providers must be present on the server.
 */
public final class OrbitalTerminalCommonIntegration {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private OrbitalTerminalCommonIntegration() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        TerminalMissionRegistry.register(OrbitalMissionProvider.INSTANCE);
        TerminalMissionActions.registerForTab(OrbitalTerminalIds.ECHO_TAB);
        TerminalActionRegistry.register(OrbitalTerminalIds.COMMAND_TAB, OrbitalTerminalIds.SCAN_ACTION,
                (player, payload) -> EchoTerminalItem.performScan(player));
        TerminalActionRegistry.register(OrbitalTerminalIds.SURVEY_TAB, OrbitalTerminalIds.SCAN_ACTION,
                (player, payload) -> EchoTerminalItem.performScan(player));
        registerArchiveEntries();
    }

    private static void registerArchiveEntries() {
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                OrbitalTerminalIds.id("orbital_remnants_route_manual"),
                "ECHO-0",
                "Orbital Remnants Route Manual",
                "OPEN",
                List.of(
                        "Orbital Remnants remains playable from its standalone ECHO-7 terminal item.",
                        "When ECHO Terminal is installed, shared tabs mirror route status and expose the same server-side SCAN action."),
                false));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                OrbitalTerminalIds.id("orbital_ashfall_handoff"),
                "ECHO-0",
                "Post-Nexus Orbital Handoff",
                "OPEN",
                List.of(
                        "Orbital calibration waits for an ECHO: Ashfall Protocol Nexus choice when Ashfall is installed.",
                        "The handoff is read through ECHO Core services; Orbital does not own Ashfall progression or the shared terminal block.",
                        "After the choice, ECHO-7 can reopen the route from ruined Earth to Station ECHO debris."),
                false));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                OrbitalTerminalIds.id("echo_zero_quarantine_context"),
                "ECHO-0",
                "ECHO-0 Quarantine Context",
                "OPEN",
                List.of(
                        "ECHO-0 treated Earth as a quarantine field after the Gridfall signal crossed orbit.",
                        "Locked records can name the quarantine, Station ECHO, and ECHO-7 without exposing the final resolution.",
                        "The active mission record carries spoiler-sensitive instructions only when the player reaches that route state."),
                false));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                OrbitalTerminalIds.id("living_route_worlds_field_notes"),
                "ECHO-0",
                "Living Route Worlds Field Notes",
                "OPEN",
                List.of(
                        "Route worlds use terminal surveys instead of Ashfall mission turn-ins.",
                        "Each route has local landmarks, repair hooks, hazards, and recovery items that the Survey tab summarizes.",
                        "Terminal caches are optional support bundles; progression still comes from Orbital scans, repairs, bosses, and contracts."),
                false));
    }
}
