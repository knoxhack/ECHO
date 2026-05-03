package com.knoxhack.echoorbitalremnants.integration;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoorbitalremnants.item.EchoTerminalItem;
import com.knoxhack.echoorbitalremnants.progression.EchoTerminalProgress;
import com.knoxhack.echoorbitalremnants.progression.LaunchReadiness;
import com.knoxhack.echoorbitalremnants.registry.ModBlocks;
import com.knoxhack.echoorbitalremnants.registry.ModItems;
import com.knoxhack.echoterminal.api.mission.TerminalMissionAction;
import com.knoxhack.echoterminal.api.mission.TerminalMissionChapter;
import com.knoxhack.echoterminal.api.mission.TerminalMissionDefinition;
import com.knoxhack.echoterminal.api.mission.TerminalMissionPresentation;
import com.knoxhack.echoterminal.api.mission.TerminalMissionProvider;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRequirement;
import com.knoxhack.echoterminal.api.mission.TerminalMissionReward;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRole;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class OrbitalMissionProvider implements TerminalMissionProvider {
    public static final OrbitalMissionProvider INSTANCE = new OrbitalMissionProvider();
    private static final String ACTION_SCAN = "scan";
    private static final String ACTION_CLAIM_CACHE = "claim_cache";
    private static final int ACCENT = 0xFF82E9FF;

    private OrbitalMissionProvider() {
    }

    @Override
    public TerminalMissionChapter chapter() {
        return new TerminalMissionChapter(
                OrbitalTerminalIds.CHAPTER_ID,
                "ECHO-0 ROUTE CHAIN",
                "Post-Nexus route chapter for Earth recovery, Station ECHO, route worlds, ECHO-0, surveys, and faction relay sealing.",
                300,
                ACCENT,
                true);
    }

    @Override
    public List<TerminalMissionDefinition> missions(Player player) {
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        LaunchReadiness launch = LaunchReadiness.evaluateForLaunch(player);
        LaunchReadiness assembly = LaunchReadiness.evaluateForAssembly(player);
        return List.of(OrbitalMission.values()).stream()
                .map(mission -> definition(player, progress, launch, assembly, mission))
                .toList();
    }

    @Override
    public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
        OrbitalMission mission = mission(missionId);
        if (mission == null) {
            return new TerminalMissionSnapshot(missionId, TerminalMissionStatus.LOCKED, 0.0F,
                    "UNKNOWN", "Unknown Orbital Remnants mission record.", "Unknown mission record.", List.of());
        }
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        boolean available = isAvailable(player, progress, mission);
        boolean complete = isComplete(progress, mission);
        boolean claimed = progress.hasTerminalMissionCacheClaimed(mission.path());
        TerminalMissionStatus status = !available
                ? TerminalMissionStatus.LOCKED
                : complete
                        ? claimed ? TerminalMissionStatus.CLAIMED : TerminalMissionStatus.CLAIMABLE
                        : TerminalMissionStatus.UNLOCKED;
        List<TerminalMissionAction> actions = actions(player, progress, mission, available, complete, claimed);
        return new TerminalMissionSnapshot(
                mission.id(),
                status,
                progress(player, progress, mission),
                statusLabel(status),
                available ? "" : lockedReason(player, progress, mission),
                actionHint(player, progress, mission, available, complete, claimed),
                actions);
    }

    @Override
    public TerminalMissionPresentation presentation(
            Player player,
            TerminalMissionDefinition definition,
            TerminalMissionSnapshot snapshot) {
        return new TerminalMissionPresentation(
                definition.title(),
                definition.briefing(),
                snapshot.actionHint(),
                definition.phaseTitle(),
                switch (snapshot.status()) {
                    case CLAIMABLE, CLAIMED, COMPLETED -> "success";
                    case UNLOCKED -> "active";
                    case LOCKED, VIEW_ONLY -> "muted";
                },
                List.of(definition.category(), definition.difficulty(), snapshot.statusLabel()),
                "echoorbitalremnants:" + definition.id().getPath());
    }

    @Override
    public TerminalMissionRole role(Player player, TerminalMissionDefinition definition, TerminalMissionSnapshot snapshot) {
        return TerminalMissionRole.MAIN;
    }

    @Override
    public boolean handleAction(ServerPlayer player, Identifier missionId, String actionId) {
        OrbitalMission mission = mission(missionId);
        if (mission == null) {
            return false;
        }
        if (ACTION_SCAN.equals(actionId)) {
            EchoTerminalItem.performScan(player);
            return true;
        }
        if (!ACTION_CLAIM_CACHE.equals(actionId)) {
            return false;
        }
        EchoTerminalProgress progress = EchoTerminalProgress.get(player);
        if (!isAvailable(player, progress, mission) || !isComplete(progress, mission)) {
            player.sendSystemMessage(Component.literal("[ECHO-7] Mission cache locked. Complete the route record first."), true);
            return true;
        }
        if (!progress.markTerminalMissionCacheClaimed(player, mission.path())) {
            player.sendSystemMessage(Component.literal("[ECHO-7] Mission cache already claimed."), true);
            return true;
        }
        List<ItemStack> rewards = rewards(mission);
        if (!EchoCoreServices.storeTerminalRewards(player, mission.id().toString(), rewards)) {
            awardDirectly(player, rewards);
            player.sendSystemMessage(Component.literal("[ECHO-7] Mission cache delivered."), true);
        }
        return true;
    }

    private static TerminalMissionDefinition definition(Player player, EchoTerminalProgress progress,
            LaunchReadiness launch, LaunchReadiness assembly, OrbitalMission mission) {
        return new TerminalMissionDefinition(
                mission.id(),
                OrbitalTerminalIds.CHAPTER_ID,
                mission.phaseId(),
                mission.phaseTitle(),
                mission.phaseOrder(),
                mission.order(),
                mission.title(),
                mission.briefing(),
                mission.guide(),
                mission.category(),
                mission.difficulty(),
                icon(mission),
                prerequisites(mission),
                requirements(player, progress, launch, assembly, mission),
                rewards(mission).stream().map(TerminalMissionReward::of).toList());
    }

    private static List<TerminalMissionAction> actions(Player player, EchoTerminalProgress progress,
            OrbitalMission mission, boolean available, boolean complete, boolean claimed) {
        if (!available) {
            return List.of(TerminalMissionAction.disabled(ACTION_SCAN, "SCAN", lockedReason(player, progress, mission)));
        }
        if (complete) {
            return List.of(claimed
                    ? TerminalMissionAction.disabled(ACTION_CLAIM_CACHE, "CLAIM CACHE", "Support cache already claimed.")
                    : TerminalMissionAction.enabled(ACTION_CLAIM_CACHE, "CLAIM CACHE"));
        }
        return List.of(TerminalMissionAction.enabled(ACTION_SCAN, "SCAN ROUTE"));
    }

    private static List<TerminalMissionRequirement> requirements(Player player, EchoTerminalProgress progress,
            LaunchReadiness launch, LaunchReadiness assembly, OrbitalMission mission) {
        return switch (mission) {
            case EARTH_CALIBRATION -> List.of(requirement(
                    "Earth calibration",
                    progress.launchSiteTracked() ? "Orbital contact calibrated." : "Sneak-use ECHO-7 on Earth.",
                    ModItems.ECHO_TERMINAL.get(), progress.launchSiteTracked() ? 1 : 0, 1));
            case LAUNCH_CHAIN -> List.of(
                    requirement("Launch systems", missingOrReady(launch, "Launch systems ready."),
                            ModBlocks.LAUNCH_PLATFORM.get(), launch.ready() ? 1 : 0, 1),
                    requirement("Rocket assembly", missingOrReady(assembly, "Rocket assembly ready."),
                            ModItems.EMERGENCY_ROCKET.get(), assembly.ready() || progress.launchPrepared() ? 1 : 0, 1));
            case LOW_ORBIT -> List.of(requirement(
                    "Low Earth Orbit",
                    progress.lowOrbitReached() ? "Emergency Rocket route confirmed." : "Use the Emergency Rocket from Earth.",
                    ModItems.EMERGENCY_ROCKET.get(), progress.lowOrbitReached() ? 1 : 0, 1));
            case STATION_NETWORK -> List.of(requirement(
                    "Station relay network",
                    progress.stationNetworkGateOpen() ? "Station network route gate open." : "Repair unique Station Relay Nodes.",
                    ModItems.STATION_RELAY_FUSE.get(), progress.stationNetworkGateOpen() ? 3 : progress.stationRelayRepairs(), 3));
            case LUNAR_SIGNAL -> List.of(requirement(
                    "Lunar signal",
                    progress.lunarSignalInvestigated() ? "Lunar route investigated." : "Use the Orbital Shuttle from staging.",
                    ModItems.ORBITAL_SHUTTLE.get(), progress.lunarSignalInvestigated() ? 1 : 0, 1));
            case MARS_ROUTE -> List.of(requirement(
                    "Mars transfer",
                    progress.marsRouteUnlocked() ? "Mars transfer route unlocked." : "Resolve Helium-3 telemetry from the Moon.",
                    ModItems.MARS_TRANSFER_WINDOW.get(), progress.marsRouteUnlocked() ? 1 : 0, 1));
            case EUROPA_ROUTE -> List.of(requirement(
                    "Europa transfer",
                    progress.europaRouteUnlocked() ? "Europa cryo route unlocked." : "Resolve Martian Silica telemetry.",
                    ModItems.EUROPA_TRANSFER_WINDOW.get(), progress.europaRouteUnlocked() ? 1 : 0, 1));
            case DEEP_SPACE_PROTOCOL -> List.of(requirement(
                    "Deep Space Protocol",
                    progress.deepSpaceProtocolUnlocked() ? "Deep Space Protocol unlocked." : "Resolve Europa cryo or Nexus drive telemetry.",
                    ModItems.NEXUS_DRIVE_CORE.get(), progress.deepSpaceProtocolUnlocked() ? 1 : 0, 1));
            case ECHO_ZERO -> List.of(requirement(
                    "ECHO-0",
                    progress.echoZeroEncountered() ? "ECHO-0 resolved." : "Confront ECHO-0 in the Nexus Anomaly Belt.",
                    ModItems.NEXUS_DRIVE_VESSEL.get(), progress.echoZeroEncountered() ? 1 : 0, 1));
            case SURVEY_NETWORK -> List.of(requirement(
                    "Route survey network",
                    progress.allSurveysComplete() ? "Every route survey is complete." : progress.surveyStatus(),
                    ModItems.ORBIT_SURVEY_DATA.get(), progress.totalSurveyCount(), 15));
            case FACTION_CONTRACT -> List.of(requirement(
                    "Faction contract",
                    progress.completedFactionContractCount() > 0 ? "One faction relay sealed." : progress.factionContractRequirement(),
                    ModItems.ORBITAL_REMNANT_BADGE.get(), progress.completedFactionContractCount(), 1));
            case FINAL_SEAL -> List.of(requirement(
                    "Final network seal",
                    progress.finalNetworkSealed() ? "Orbital Remnants arc complete." : progress.scanRequirement(),
                    ModItems.STABILIZED_ECHO_CORE.get(), progress.finalNetworkSealed() ? 1 : 0, 1));
        };
    }

    private static TerminalMissionRequirement requirement(String label, String detail, ItemLike icon, int have, int need) {
        int safeNeed = Math.max(1, need);
        int safeHave = Math.max(0, Math.min(safeNeed, have));
        return TerminalMissionRequirement.custom(label, detail, new ItemStack(icon), safeHave, safeNeed, safeHave >= safeNeed);
    }

    private static boolean isAvailable(Player player, EchoTerminalProgress progress, OrbitalMission mission) {
        if (mission == OrbitalMission.EARTH_CALIBRATION) {
            return !AshfallCompat.isOrbitalCalibrationLocked(player);
        }
        OrbitalMission previous = mission.previous();
        return previous != null && isAvailable(player, progress, previous) && isComplete(progress, previous);
    }

    private static boolean isComplete(EchoTerminalProgress progress, OrbitalMission mission) {
        return switch (mission) {
            case EARTH_CALIBRATION -> progress.launchSiteTracked();
            case LAUNCH_CHAIN -> progress.launchPrepared() || progress.lowOrbitReached();
            case LOW_ORBIT -> progress.lowOrbitReached();
            case STATION_NETWORK -> progress.stationNetworkGateOpen();
            case LUNAR_SIGNAL -> progress.lunarSignalInvestigated();
            case MARS_ROUTE -> progress.marsRouteUnlocked();
            case EUROPA_ROUTE -> progress.europaRouteUnlocked();
            case DEEP_SPACE_PROTOCOL -> progress.deepSpaceProtocolUnlocked();
            case ECHO_ZERO -> progress.echoZeroEncountered();
            case SURVEY_NETWORK -> progress.allSurveysComplete();
            case FACTION_CONTRACT -> progress.completedFactionContractCount() > 0;
            case FINAL_SEAL -> progress.finalNetworkSealed();
        };
    }

    private static float progress(Player player, EchoTerminalProgress progress, OrbitalMission mission) {
        if (!isAvailable(player, progress, mission)) {
            return 0.0F;
        }
        if (isComplete(progress, mission)) {
            return 1.0F;
        }
        return switch (mission) {
            case EARTH_CALIBRATION, LOW_ORBIT, LUNAR_SIGNAL, MARS_ROUTE, EUROPA_ROUTE, DEEP_SPACE_PROTOCOL,
                    ECHO_ZERO, FACTION_CONTRACT, FINAL_SEAL -> 0.0F;
            case LAUNCH_CHAIN -> {
                LaunchReadiness launch = LaunchReadiness.evaluateForLaunch(player);
                LaunchReadiness assembly = LaunchReadiness.evaluateForAssembly(player);
                yield ((launch.ready() ? 1.0F : 0.0F) + (assembly.ready() ? 1.0F : 0.0F)) / 2.0F;
            }
            case STATION_NETWORK -> progress.stationRelayRepairs() / 3.0F;
            case SURVEY_NETWORK -> progress.totalSurveyCount() / 15.0F;
        };
    }

    private static String lockedReason(Player player, EchoTerminalProgress progress, OrbitalMission mission) {
        if (mission == OrbitalMission.EARTH_CALIBRATION && AshfallCompat.isOrbitalCalibrationLocked(player)) {
            return "Resolve an ECHO: Ashfall Protocol Nexus path before orbital calibration can begin.";
        }
        OrbitalMission previous = mission.previous();
        if (previous == null) {
            return "Orbital route record locked.";
        }
        return "Complete " + previous.title() + " first.";
    }

    private static String actionHint(Player player, EchoTerminalProgress progress, OrbitalMission mission,
            boolean available, boolean complete, boolean claimed) {
        if (!available) {
            return lockedReason(player, progress, mission);
        }
        if (complete) {
            return claimed
                    ? "Support cache claimed. Continue the Orbital route from the next active record."
                    : "Route record complete. Claim the optional support cache from the shared terminal.";
        }
        if (mission == OrbitalMission.SURVEY_NETWORK) {
            return "Use the Survey tab for route counts. " + progress.missionHelpReport();
        }
        if (mission == OrbitalMission.FACTION_CONTRACT) {
            return progress.factionContractRequirement();
        }
        return progress.scanRequirement();
    }

    private static List<String> prerequisites(OrbitalMission mission) {
        OrbitalMission previous = mission.previous();
        return previous == null ? List.of() : List.of(previous.title());
    }

    private static String missingOrReady(LaunchReadiness readiness, String readyText) {
        if (readiness.ready()) {
            return readyText;
        }
        List<String> missing = readiness.missing().stream()
                .limit(3)
                .map(component -> component.getString().replaceFirst("^- ", ""))
                .toList();
        if (missing.isEmpty()) {
            return "Checklist incomplete.";
        }
        String detail = "Missing: " + String.join(", ", missing);
        if (readiness.missing().size() > missing.size()) {
            detail += ", +" + (readiness.missing().size() - missing.size()) + " more";
        }
        return detail;
    }

    private static String statusLabel(TerminalMissionStatus status) {
        return switch (status) {
            case LOCKED -> "LOCKED";
            case UNLOCKED -> "ACTIVE";
            case COMPLETED -> "COMPLETE";
            case CLAIMABLE -> "CACHE READY";
            case CLAIMED -> "CLAIMED";
            case VIEW_ONLY -> "VIEW";
        };
    }

    private static List<ItemStack> rewards(OrbitalMission mission) {
        return switch (mission) {
            case EARTH_CALIBRATION -> stacks(
                    stack(ModItems.EMERGENCY_OXYGEN_CELL.get(), 2),
                    stack(ModItems.SUIT_SEALANT_PATCH.get(), 1),
                    stack(ModItems.VACUUM_CIRCUIT.get(), 1));
            case LAUNCH_CHAIN -> stacks(
                    stack(ModItems.EMERGENCY_OXYGEN_CELL.get(), 3),
                    stack(ModItems.SUIT_SEALANT_PATCH.get(), 2),
                    stack(ModItems.HEAT_SHIELD_PLATE.get(), 1));
            case LOW_ORBIT -> stacks(
                    stack(ModItems.OXYGEN_CANISTER.get(), 1),
                    stack(ModItems.VACUUM_CIRCUIT.get(), 2));
            case STATION_NETWORK -> stacks(
                    stack(ModItems.EMERGENCY_OXYGEN_CELL.get(), 4),
                    stack(ModItems.ORBITAL_ALLOY.get(), 1));
            case LUNAR_SIGNAL -> stacks(
                    stack(ModItems.SUIT_SEALANT_PATCH.get(), 3),
                    stack(ModItems.HEAT_SHIELD_PLATE.get(), 2));
            case MARS_ROUTE -> stacks(
                    stack(ModItems.OXYGEN_CANISTER.get(), 2),
                    stack(ModItems.SUIT_SEALANT_PATCH.get(), 2));
            case EUROPA_ROUTE -> stacks(
                    stack(ModItems.EMERGENCY_OXYGEN_CELL.get(), 4),
                    stack(ModItems.FROZEN_WIRING.get(), 2));
            case DEEP_SPACE_PROTOCOL -> stacks(
                    stack(ModItems.OXYGEN_CANISTER.get(), 2),
                    stack(ModItems.ORBITAL_ALLOY.get(), 2));
            case ECHO_ZERO -> stacks(
                    stack(ModItems.EMERGENCY_OXYGEN_CELL.get(), 6),
                    stack(ModItems.SUIT_SEALANT_PATCH.get(), 4),
                    stack(ModItems.HEAT_SHIELD_PLATE.get(), 2));
            case SURVEY_NETWORK -> stacks(
                    stack(ModItems.OXYGEN_CANISTER.get(), 3),
                    stack(ModItems.VACUUM_CIRCUIT.get(), 3));
            case FACTION_CONTRACT -> stacks(
                    stack(ModItems.ORBITAL_ALLOY.get(), 2),
                    stack(ModItems.HEAT_SHIELD_PLATE.get(), 2));
            case FINAL_SEAL -> stacks(
                    stack(ModItems.EMERGENCY_OXYGEN_CELL.get(), 8),
                    stack(ModItems.SUIT_SEALANT_PATCH.get(), 6),
                    stack(ModItems.ORBITAL_ALLOY.get(), 2));
        };
    }

    private static List<ItemStack> stacks(ItemStack... stacks) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return List.copyOf(result);
    }

    private static ItemStack stack(ItemLike item, int count) {
        return new ItemStack(item, count);
    }

    private static void awardDirectly(ServerPlayer player, List<ItemStack> rewards) {
        for (ItemStack reward : rewards) {
            ItemStack copy = reward.copy();
            if (!player.getInventory().add(copy)) {
                player.drop(copy, false);
            }
        }
    }

    private static ItemStack icon(OrbitalMission mission) {
        return switch (mission) {
            case EARTH_CALIBRATION -> stack(ModItems.ECHO_TERMINAL.get(), 1);
            case LAUNCH_CHAIN -> stack(ModItems.EMERGENCY_ROCKET.get(), 1);
            case LOW_ORBIT -> stack(ModBlocks.STATION_LIFE_SUPPORT_CORE.get(), 1);
            case STATION_NETWORK -> stack(ModItems.STATION_RELAY_FUSE.get(), 1);
            case LUNAR_SIGNAL -> stack(ModItems.ORBITAL_SHUTTLE.get(), 1);
            case MARS_ROUTE -> stack(ModItems.MARS_TRANSFER_WINDOW.get(), 1);
            case EUROPA_ROUTE -> stack(ModItems.EUROPA_TRANSFER_WINDOW.get(), 1);
            case DEEP_SPACE_PROTOCOL -> stack(ModItems.NEXUS_DRIVE_CORE.get(), 1);
            case ECHO_ZERO -> stack(ModItems.NEXUS_DRIVE_VESSEL.get(), 1);
            case SURVEY_NETWORK -> stack(ModItems.ORBIT_SURVEY_DATA.get(), 1);
            case FACTION_CONTRACT -> stack(ModItems.ORBITAL_REMNANT_BADGE.get(), 1);
            case FINAL_SEAL -> stack(ModItems.STABILIZED_ECHO_CORE.get(), 1);
        };
    }

    private static OrbitalMission mission(Identifier id) {
        if (id == null) {
            return null;
        }
        for (OrbitalMission mission : OrbitalMission.values()) {
            if (mission.id().equals(id)) {
                return mission;
            }
        }
        return null;
    }

    private enum OrbitalMission {
        EARTH_CALIBRATION("earth_calibration", "EARTH RECONTACT", 0, 0,
                "Earth Calibration",
                "Reopen ECHO-7 orbital contact from ruined Earth.",
                "Sneak-use the ECHO-7 Terminal on Earth. When Ashfall is installed, complete any Nexus path first.",
                "Calibration", "Guide"),
        LAUNCH_CHAIN("launch_chain", "EARTH RECONTACT", 0, 1,
                "Launch Chain",
                "Build the launch pad, assembly frame, fuel, oxygen, suit, and rocket parts.",
                "Use Earth recovery sites and machine recipes to complete launch readiness.",
                "Crafting", "Route"),
        LOW_ORBIT("low_orbit", "ORBITAL CALIBRATION", 1, 0,
                "Low Earth Orbit",
                "Use the Emergency Rocket and establish the first orbital vector.",
                "Launch from Earth, recover the return vector, and scan for station systems.",
                "Route", "Hazard"),
        STATION_NETWORK("station_network", "ORBITAL CALIBRATION", 1, 1,
                "Station Network",
                "Restore station life support and repair the Low Orbit relay network.",
                "Scan Station Relay Nodes with Station Relay Fuses. Old saves may show this as bypassed.",
                "Repair", "Route"),
        LUNAR_SIGNAL("lunar_signal", "Moon", 2, 0,
                "Lunar Signal",
                "Follow the Station ECHO route to the Lunar Scar Zone.",
                "Use the Orbital Shuttle from orbital staging, then stabilize lunar telemetry.",
                "Route", "Hazard"),
        MARS_ROUTE("mars_route", "Mars", 3, 0,
                "Mars Route",
                "Resolve Helium-3 telemetry into a Mars transfer route.",
                "Repair lunar extractors when required, then scan with Helium-3 support.",
                "Route", "Hazard"),
        EUROPA_ROUTE("europa_route", "Europa", 4, 0,
                "Europa Route",
                "Resolve Martian pressure telemetry into the Europa cryo route.",
                "Repair Mars pressure consoles when required, then scan with Martian Silica support.",
                "Route", "Hazard"),
        DEEP_SPACE_PROTOCOL("deep_space_protocol", "Nexus Anomaly", 5, 0,
                "Deep Space Protocol",
                "Use Europa or Nexus-drive telemetry to reveal the anomaly belt.",
                "Calibrate Europa thermal arrays when required, then scan with Cryo Crystal or Nexus Drive support.",
                "Route", "Endgame"),
        ECHO_ZERO("echo_zero", "Nexus Anomaly", 5, 1,
                "ECHO-0",
                "Confront the quarantine intelligence at the edge of the route network.",
                "Enter the Nexus Anomaly Belt, survive the route pressure, and resolve ECHO-0.",
                "Story", "Endgame"),
        SURVEY_NETWORK("survey_network", "ROUTE SURVEY", 6, 0,
                "Survey Network",
                "Map the route worlds and stabilize the post-ECHO Nexus anchors.",
                "Use the Survey tab to finish each route's three unique logs.",
                "Survey", "Endgame"),
        FACTION_CONTRACT("faction_contract", "ROUTE SURVEY", 6, 1,
                "Faction Contract",
                "Seal one faction relay after the survey network is stable.",
                "Pledge to a faction, follow the ECHO-tab proof requirement, and press SCAN when ready.",
                "Faction", "Endgame"),
        FINAL_SEAL("final_seal", "FINAL SEAL", 7, 0,
                "Final Network Seal",
                "Close the Orbital Remnants arc after ECHO-0, surveys, and one faction relay.",
                "Press SCAN once all final prerequisites are complete.",
                "Story", "Complete");

        private final String path;
        private final String phaseTitle;
        private final int phaseOrder;
        private final int order;
        private final String title;
        private final String briefing;
        private final String guide;
        private final String category;
        private final String difficulty;

        OrbitalMission(String path, String phaseTitle, int phaseOrder, int order, String title,
                String briefing, String guide, String category, String difficulty) {
            this.path = path;
            this.phaseTitle = phaseTitle;
            this.phaseOrder = phaseOrder;
            this.order = order;
            this.title = title;
            this.briefing = briefing;
            this.guide = guide;
            this.category = category;
            this.difficulty = difficulty;
        }

        Identifier id() {
            return OrbitalTerminalIds.id(path);
        }

        String path() {
            return path;
        }

        String phaseId() {
            return phaseTitle.toLowerCase(java.util.Locale.ROOT).replace(' ', '_');
        }

        String phaseTitle() {
            return phaseTitle;
        }

        int phaseOrder() {
            return phaseOrder;
        }

        int order() {
            return order;
        }

        String title() {
            return title;
        }

        String briefing() {
            return briefing;
        }

        String guide() {
            return guide;
        }

        String category() {
            return category;
        }

        String difficulty() {
            return difficulty;
        }

        OrbitalMission previous() {
            int index = ordinal() - 1;
            return index < 0 ? null : values()[index];
        }
    }
}
