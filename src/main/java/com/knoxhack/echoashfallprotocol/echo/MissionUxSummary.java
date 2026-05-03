package com.knoxhack.echoashfallprotocol.echo;

import com.knoxhack.echoashfallprotocol.endgame.PostNexusData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public record MissionUxSummary(
        String missionId,
        String shortTitle,
        String objectiveSummary,
        String nextStep,
        String routeHint,
        String statusLabel,
        String statusTone,
        List<String> tags,
        String relatedIntelKey) {
    public MissionUxSummary {
        missionId = clean(missionId, "");
        shortTitle = clean(shortTitle, "Mission Record");
        objectiveSummary = clean(objectiveSummary, "");
        nextStep = clean(nextStep, "Continue the active route from the MISSIONS tab.");
        routeHint = clean(routeHint, "");
        statusLabel = clean(statusLabel, "LOCKED");
        statusTone = clean(statusTone, "muted");
        tags = List.copyOf(tags == null ? List.of() : tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .toList());
        relatedIntelKey = clean(relatedIntelKey, "");
    }

    public static MissionUxSummary current(Player player, QuestData quest) {
        Mission mission = MissionRegistry.getMission(quest.getCurrentPhase(), quest.getCurrentMissionIndex());
        if (mission == null) {
            return new MissionUxSummary(
                    "",
                    "Protocol Sync Pending",
                    "Mission registry data is still syncing from the server.",
                    "Close and reopen the terminal after the next sync tick.",
                    "",
                    "SYNC",
                    "muted",
                    List.of("Sync"),
                    "ashfall_progression_manual");
        }
        return of(player, quest, mission);
    }

    public static MissionUxSummary of(Player player, QuestData quest, Mission mission) {
        QuestData.MissionStatus status = quest.getMissionStatus(mission.id());
        boolean current = isCurrentMission(quest, mission);
        boolean preview = mission.isPathPreview(player);
        boolean pendingRewards = quest.hasPendingRewards(mission.id());
        boolean completeNow = safeComplete(mission, player);
        DisplayState display = displayState(status, preview, pendingRewards);
        String nextStep = nextStep(player, quest, mission, status, current, completeNow, pendingRewards, preview);
        return new MissionUxSummary(
                mission.id(),
                mission.objectiveText(),
                mission.echoMessage(),
                nextStep,
                routeHint(quest, mission),
                display.label(),
                display.tone(),
                tags(quest, mission),
                relatedIntelKey(mission));
    }

    public static boolean isCurrentMission(QuestData quest, Mission mission) {
        Mission current = MissionRegistry.getMission(quest.getCurrentPhase(), quest.getCurrentMissionIndex());
        return current != null && current.id().equals(mission.id());
    }

    public static String unlockReason(Player player, QuestData quest, Mission mission) {
        for (String prerequisite : mission.getPrerequisites()) {
            if (!quest.isMissionCompleted(prerequisite)) {
                Mission prereq = MissionRegistry.getMissionById(prerequisite);
                return "Complete " + (prereq == null ? prerequisite : prereq.objectiveText()) + ".";
            }
        }
        if (mission.isPathRestricted() && PostNexusData.get(player).getSelectedPath() != mission.requiredPath()) {
            return "Select the " + mission.requiredPath().name() + " Nexus path.";
        }
        if (mission.isPathPreview(player)) {
            return "Resolve the required Nexus path before this protocol can run.";
        }
        return "Complete the prior protocol to unlock this route.";
    }

    public static String turnInReason(Player player, QuestData quest, Mission mission,
            QuestData.MissionStatus status, boolean current, boolean completeNow, boolean preview) {
        if (status == QuestData.MissionStatus.LOCKED || preview) {
            return unlockReason(player, quest, mission);
        }
        if (!current) {
            return "Only the current active protocol can be turned in.";
        }
        if (!mission.isTurnInMission()) {
            return "This protocol completes automatically.";
        }
        if (!completeNow) {
            return "Finish the visible requirements before turn-in.";
        }
        return "Server validation required.";
    }

    private static String nextStep(Player player, QuestData quest, Mission mission,
            QuestData.MissionStatus status, boolean current, boolean completeNow,
            boolean pendingRewards, boolean preview) {
        if (pendingRewards) {
            return "Claim the completed protocol reward cache from MISSIONS.";
        }
        if (status == QuestData.MissionStatus.LOCKED || preview) {
            return unlockReason(player, quest, mission);
        }
        if (!current) {
            return "Use this record for planning; follow the current active protocol first.";
        }
        if (mission.isTurnInMission() && completeNow) {
            return terminalInstalled()
                    ? "Objective complete. Turn in this protocol from MISSIONS."
                    : "Objective complete. ECHO will complete this protocol server-side.";
        }
        String requirement = firstOpenRequirement(player, quest, mission);
        if (!requirement.isBlank()) {
            return requirement;
        }
        if (!mission.isTurnInMission()) {
            return "Keep progressing; this protocol completes automatically when the server check passes.";
        }
        return terminalInstalled()
                ? "Finish the visible requirements, then turn in this protocol."
                : "Finish the visible requirements; ECHO will complete it server-side.";
    }

    private static boolean terminalInstalled() {
        return ModList.get().isLoaded("echoterminal");
    }

    private static String firstOpenRequirement(Player player, QuestData quest, Mission mission) {
        String endgameStep = EndgameMissionProgress.forMission(player, quest, mission)
                .map(EndgameMissionProgress.Snapshot::firstOpenStep)
                .orElse("");
        if (!endgameStep.isBlank()) {
            return endgameStep;
        }
        for (Mission.ItemProgress progress : mission.getItemProgress(player)) {
            if (!progress.satisfied()) {
                return "Collect " + Math.max(0, progress.need() - progress.have()) + " more "
                        + progress.item().getHoverName().getString() + ".";
            }
        }
        for (Mission.BlockRequirement requirement : mission.requiredBlocks()) {
            int have = quest.getBlockPlaceCount(requirement.blockId());
            if (have < requirement.count()) {
                return "Place " + Math.max(1, requirement.count() - have) + " "
                        + requirement.displayName() + ".";
            }
        }
        for (Mission.EntityKillRequirement requirement : mission.requiredEntityKills()) {
            int have = quest.getEntityKills(requirement.entityType());
            if (have < requirement.count()) {
                return "Neutralize " + Math.max(1, requirement.count() - have) + " "
                        + requirement.displayName() + ".";
            }
        }
        for (Mission.LocationRequirement requirement : mission.requiredLocations()) {
            if (!quest.hasVisitedLocation(requirement.locationType(), requirement.locationId())) {
                return "Reach " + requirement.displayName() + ".";
            }
        }
        for (Mission.EquipmentRequirement requirement : mission.requiredEquipment()) {
            if (player.getItemBySlot(requirement.slot()).isEmpty()
                    || player.getItemBySlot(requirement.slot()).getItem() != requirement.item().getItem()) {
                return "Equip " + requirement.displayName() + ".";
            }
        }
        return "";
    }

    private static DisplayState displayState(QuestData.MissionStatus status, boolean preview, boolean pendingRewards) {
        if (preview) {
            return new DisplayState("VIEW", "muted");
        }
        return switch (status) {
            case COMPLETED -> pendingRewards ? new DisplayState("READY", "success") : new DisplayState("DONE", "success");
            case UNLOCKED -> new DisplayState("ACTIVE", "active");
            case LOCKED -> new DisplayState("LOCKED", "muted");
        };
    }

    private static String routeHint(QuestData quest, Mission mission) {
        for (int phase = 0; phase < MissionRegistry.getPhaseCount(); phase++) {
            List<Mission> missions = MissionRegistry.getMissionsForPhase(phase);
            for (int index = 0; index < missions.size(); index++) {
                if (missions.get(index).id().equals(mission.id())) {
                    return "P" + (phase + 1) + " / Mission " + (index + 1) + " of " + missions.size()
                            + (isCurrentMission(quest, mission) ? " / current route" : " / roadmap");
                }
            }
        }
        return "Roadmap reference";
    }

    private static List<String> tags(QuestData quest, Mission mission) {
        List<String> tags = new ArrayList<>();
        tags.add(mission.category() == null ? "Mission" : mission.category().getDisplayName());
        tags.add(mission.difficulty() == null ? "STANDARD" : mission.difficulty().name());
        if (mission.isPathRestricted()) {
            tags.add(mission.requiredPath().name());
        }
        if (isCurrentMission(quest, mission)) {
            tags.add("Current");
        }
        return tags;
    }

    private static String relatedIntelKey(Mission mission) {
        if (mission.id().contains("nexus") || mission.isPathRestricted()) {
            return "ashfall_nexus_manual";
        }
        if (mission.category() == null) {
            return "ashfall_progression_manual";
        }
        return switch (mission.category()) {
            case SURVIVAL -> "ashfall_survival_manual";
            case CRAFTING, TECH -> "ashfall_systems_manual";
            case EXPLORATION -> "ashfall_progression_manual";
            case COMBAT -> "ashfall_threat_manual";
            case STORY -> "ashfall_progression_manual";
        };
    }

    private static boolean safeComplete(Mission mission, Player player) {
        try {
            return mission.isComplete(player);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static String clean(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private record DisplayState(String label, String tone) {
    }
}
