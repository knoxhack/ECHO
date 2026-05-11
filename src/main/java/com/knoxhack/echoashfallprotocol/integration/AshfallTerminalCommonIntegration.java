package com.knoxhack.echoashfallprotocol.integration;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.echo.EchoGuideManager;
import com.knoxhack.echoashfallprotocol.echo.Mission;
import com.knoxhack.echoashfallprotocol.echo.MissionRegistry;
import com.knoxhack.echoashfallprotocol.echo.MissionUxSummary;
import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.endgame.NexusCampaignActions;
import com.knoxhack.echoashfallprotocol.endgame.NexusChoiceService;
import com.knoxhack.echoashfallprotocol.endgame.PostNexusData;
import com.knoxhack.echoashfallprotocol.network.DroneCommandPacket;
import com.knoxhack.echoashfallprotocol.network.ModNetwork;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.survival.SurvivalData;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import com.knoxhack.echoterminal.api.TerminalAddonGuide;
import com.knoxhack.echoterminal.api.TerminalAddonInfo;
import com.knoxhack.echoterminal.api.TerminalAddonInfoProvider;
import com.knoxhack.echoterminal.api.TerminalAddonInfoRegistry;
import com.knoxhack.echoterminal.api.TerminalAddonLink;
import com.knoxhack.echoterminal.api.TerminalAddonMetric;
import com.knoxhack.echoterminal.api.TerminalAddonSection;
import com.knoxhack.echoterminal.api.TerminalArchiveEntry;
import com.knoxhack.echoterminal.api.TerminalArchiveRegistry;
import com.knoxhack.echoterminal.api.TerminalUi;
import com.knoxhack.echoterminal.api.mission.TerminalMissionAction;
import com.knoxhack.echoterminal.api.mission.TerminalMissionActions;
import com.knoxhack.echoterminal.api.mission.TerminalMissionChapter;
import com.knoxhack.echoterminal.api.mission.TerminalMissionDefinition;
import com.knoxhack.echoterminal.api.mission.TerminalMissionProvider;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRequirement;
import com.knoxhack.echoterminal.api.mission.TerminalMissionReward;
import com.knoxhack.echoterminal.api.mission.TerminalMissionRole;
import com.knoxhack.echoterminal.api.mission.TerminalMissionSnapshot;
import com.knoxhack.echoterminal.api.mission.TerminalMissionStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

/**
 * Common/server terminal registrations for Ashfall.
 *
 * <p>Keep this class free of client imports. Client tabs and screen rendering live
 * in {@link AshfallTerminalIntegration}; server-executed mission/action handlers
 * live here so dedicated servers can handle TerminalActionPacket safely.</p>
 */
public final class AshfallTerminalCommonIntegration {
    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final boolean LEGACY_NEXUS_TERMINAL_ENABLED = false;
    private static final String ASHFALL_CHAPTER_ID = "ashfall_protocol";

    private static final Identifier OVERVIEW = id("overview");
    private static final Identifier MISSIONS = id("missions");
    private static final Identifier SIDE_OPS = id("side_ops");
    private static final Identifier DRONE = id("drone");
    private static final Identifier CODEX = id("codex");
    private static final Identifier WORLD = id("world");
    private static final Identifier NEXUS = id("nexus");
    private static final Identifier TURN_IN = id("turn_in_mission");
    private static final Identifier CLAIM_REWARDS = id("claim_terminal_rewards");
    private static final Identifier DRONE_COMMAND = id("drone_command");
    private static final Identifier NEXUS_CHOICE = id("nexus_choice");
    private static final Identifier NEXUS_WARFRONT = id("nexus_warfront");

    private AshfallTerminalCommonIntegration() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }

        TerminalMissionRegistryFacade.registerProviders();
        TerminalAddonInfoRegistry.register(new AshfallAddonInfoProvider());
        TerminalMissionActions.registerForTab(MISSIONS);
        TerminalMissionActions.registerForTab(SIDE_OPS);
        TerminalActionRegistry.register(MISSIONS, TURN_IN, AshfallTerminalCommonIntegration::turnInCurrentMission);
        TerminalActionRegistry.register(MISSIONS, CLAIM_REWARDS, (player, payload) -> {
            EchoGuideManager.claimRewards(player);
            QuestData.syncToClient(player);
        });
        TerminalActionRegistry.register(DRONE, DRONE_COMMAND,
                (player, payload) -> ModNetwork.handleDroneCommand(new DroneCommandPacket(payload), player));
        if (LEGACY_NEXUS_TERMINAL_ENABLED && !nexusProtocolLoaded()) {
            TerminalActionRegistry.register(NEXUS, NEXUS_CHOICE, AshfallTerminalCommonIntegration::chooseNexusPath);
            TerminalActionRegistry.register(NEXUS, NEXUS_WARFRONT, AshfallTerminalCommonIntegration::handleNexusWarfront);
        }

        registerFieldManualEntries();
    }

    public static boolean registeredForTests() {
        return REGISTERED.get();
    }

    private static void turnInCurrentMission(ServerPlayer player, String payload) {
        QuestData quest = QuestData.get(player);
        if (quest.repairMissionState(player)) {
            QuestData.saveAndSync(player, quest);
        }

        Mission current = MissionRegistry.getMission(quest.getCurrentPhase(), quest.getCurrentMissionIndex());
        if (current == null || quest.isMissionCompleted(current.id()) || !quest.isMissionUnlocked(current.id())
                || !current.isTurnInMission()) {
            player.sendSystemMessage(Component.literal("[ECHO-7] This protocol is not ready for turn-in.")
                    .withStyle(ChatFormatting.YELLOW), true);
            QuestData.syncToClient(player);
            return;
        }

        EchoGuideManager.turnInMission(player, quest, current);
        QuestData.syncToClient(player);
    }

    private static void chooseNexusPath(ServerPlayer player, String payload) {
        NexusChoiceService.applyChoice(player, payload);
    }

    private static void handleNexusWarfront(ServerPlayer player, String payload) {
        NexusCampaignActions.handleTerminalAction(player, payload);
    }

    private static boolean nexusProtocolLoaded() {
        try {
            return ModList.get().isLoaded("echonexusprotocol");
        } catch (RuntimeException exception) {
            EchoAshfallProtocol.LOGGER.warn("Ashfall terminal Nexus ownership check failed; keeping legacy Nexus terminal available.", exception);
            return false;
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, path);
    }

    private static final class TerminalMissionRegistryFacade {
        private static final TerminalMissionProvider ASHFALL_MISSIONS = new AshfallMissionProvider();
        private static final TerminalMissionProvider ASHFALL_SIDE_OPS = new AshfallSideOpsProvider();

        private TerminalMissionRegistryFacade() {
        }

        private static void registerProviders() {
            if (ModList.get().isLoaded("echomissioncore")) {
                EchoAshfallProtocol.LOGGER.info("Ashfall Terminal mission providers skipped; MissionCore owns mission display.");
                return;
            }
            com.knoxhack.echoterminal.api.mission.TerminalMissionRegistry.register(ASHFALL_MISSIONS);
            com.knoxhack.echoterminal.api.mission.TerminalMissionRegistry.register(ASHFALL_SIDE_OPS);
        }
    }

    private static final class AshfallAddonInfoProvider implements TerminalAddonInfoProvider {
        @Override
        public String chapterId() {
            return ASHFALL_CHAPTER_ID;
        }

        @Override
        public TerminalAddonInfo info(Player player) {
            if (player == null) {
                return new TerminalAddonInfo(
                        "Chapter 1 survival route, terminal repair, field safety, drone support, and early route intel.",
                        List.of(new TerminalAddonMetric("Signal", "OFFLINE", "waiting for player telemetry", TerminalUi.CYAN)),
                        List.of(new TerminalAddonSection("Start Feed",
                                List.of("Open Ashfall Command after player telemetry is available."))),
                        links(),
                        guide());
            }
            QuestData quest = QuestData.get(player);
            SurvivalData survival = player.getData(ModAttachments.SURVIVAL_DATA.get());
            MissionUxSummary current = MissionUxSummary.current(player, quest);
            return new TerminalAddonInfo(
                    "Chapter 1 survival route, terminal repair, field safety, drone support, and early route intel.",
                    List.of(
                            new TerminalAddonMetric("Phase", String.valueOf(quest.getCurrentPhase() + 1),
                                    "active Ashfall route phase", TerminalUi.GREEN),
                            new TerminalAddonMetric("Mission", String.valueOf(quest.getCurrentMissionIndex() + 1),
                                    current.shortTitle(), TerminalUi.CYAN),
                            new TerminalAddonMetric("Hydration", survival.getHydration() + "%",
                                    "field survival reserve", survival.getHydration() <= 30 ? TerminalUi.AMBER : TerminalUi.GREEN),
                            new TerminalAddonMetric("Filter", Math.round(survival.getFilterPercent() * 100.0F) + "%",
                                    survival.hasMask() ? "mask equipped" : "mask not confirmed",
                                    survival.getFilterPercent() <= 0.25F ? TerminalUi.AMBER : TerminalUi.CYAN)),
                    List.of(new TerminalAddonSection("Start Feed", List.of(
                            current.objectiveSummary(),
                            current.nextStep(),
                            survival.isSafeZone() ? "Safe zone detected." : "No safe zone detected yet."))),
                    links(),
                    guide());
        }

        private static TerminalAddonGuide guide() {
            return TerminalAddonGuide.mainline(1, 10, "Start here",
                    "Begin with Ashfall to learn survival basics, repair the terminal, stabilize camp, and open the first route signals.",
                    List.of(
                            "Secure water, shelter, food, and filters before long trips.",
                            "Open Ashfall Command or Protocol Roadmap for the next mission.",
                            "Use Survival Route when you want the complete roadmap."));
        }

        private static List<TerminalAddonLink> links() {
            return List.of(
                    new TerminalAddonLink(OVERVIEW, "Ashfall Command", "Chapter 1 field dashboard", 0xFF66D9FF),
                    new TerminalAddonLink(MISSIONS, "Protocol Roadmap", "Active Ashfall mission chain", 0xFF92F7A6),
                    new TerminalAddonLink(WORLD, "Route Map", "POIs, routes, and field map", 0xFFC09BFF),
                    new TerminalAddonLink(CODEX, "Survival Index", "Intel and recipe planning", 0xFFFFD166));
        }
    }

    private static final class AshfallMissionProvider implements TerminalMissionProvider {
        @Override
        public TerminalMissionChapter chapter() {
            return new TerminalMissionChapter(
                    id("ashfall_protocol"),
                    "ECHO-7 PROTOCOL CHAIN",
                    "Required ECHO-7 field protocols for crash survival, route recovery, drone support, buried nodes, and the Nexus decision.",
                    10,
                    0xFF66D9FF,
                    true);
        }

        @Override
        public List<TerminalMissionDefinition> missions(Player player) {
            QuestData quest = QuestData.get(player);
            List<TerminalMissionDefinition> definitions = new ArrayList<>();
            for (int phase = 0; phase < MissionRegistry.getPhaseCount(); phase++) {
                List<Mission> missions = MissionRegistry.getMissionsForPhase(phase);
                for (int i = 0; i < missions.size(); i++) {
                    Mission mission = missions.get(i);
                    definitions.add(definition(player, quest, mission, phase, i + 1));
                }
            }
            return definitions;
        }

        @Override
        public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
            Mission mission = missionId == null ? null : MissionRegistry.getMissionById(missionId.getPath());
            if (mission == null) {
                return new TerminalMissionSnapshot(missionId, TerminalMissionStatus.LOCKED, 0.0F,
                        "LOCKED", "Ashfall protocol signal missing.",
                        "ECHO-7 has no clean field record for this identifier.", List.of());
            }
            QuestData quest = QuestData.get(player);
            QuestData.MissionStatus status = quest.getMissionStatus(mission.id());
            boolean preview = mission.isPathPreview(player);
            boolean pendingRewards = quest.hasPendingRewards(mission.id());
            boolean completeNow = cheapMissionSatisfied(player, quest, mission);
            TerminalMissionStatus terminalStatus = preview
                    ? TerminalMissionStatus.VIEW_ONLY
                    : switch (status) {
                        case COMPLETED -> pendingRewards ? TerminalMissionStatus.CLAIMABLE : TerminalMissionStatus.COMPLETED;
                        case UNLOCKED -> TerminalMissionStatus.UNLOCKED;
                        case LOCKED -> TerminalMissionStatus.LOCKED;
                    };
            boolean current = isCurrentMission(quest, mission);
            boolean turnInReady = current
                    && status == QuestData.MissionStatus.UNLOCKED
                    && !quest.isMissionCompleted(mission.id())
                    && mission.isTurnInMission()
                    && completeNow
                    && !preview;
            MissionUxSummary summary = MissionUxSummary.forHud(player, quest, mission);
            return new TerminalMissionSnapshot(
                    id(mission.id()),
                    terminalStatus,
                    cheapProgress(player, quest, mission, status, completeNow),
                    summary.statusLabel(),
                    terminalStatus == TerminalMissionStatus.LOCKED || terminalStatus == TerminalMissionStatus.VIEW_ONLY
                            ? MissionUxSummary.unlockReason(player, quest, mission)
                            : "",
                    summary.nextStep(),
                    List.of(
                            turnInReady
                                    ? TerminalMissionAction.enabled(TURN_IN.getPath(), "TURN IN")
                                    : TerminalMissionAction.disabled(TURN_IN.getPath(), "TURN IN",
                                            MissionUxSummary.turnInReason(player, quest, mission, status, current, completeNow, preview)),
                            pendingRewards
                                    ? TerminalMissionAction.enabled(CLAIM_REWARDS.getPath(), "CLAIM REWARDS")
                                    : TerminalMissionAction.disabled(CLAIM_REWARDS.getPath(), "CLAIM REWARDS",
                                            "No sealed support cache is waiting for this protocol.")));
        }

        @Override
        public TerminalMissionRole role(Player player, TerminalMissionDefinition definition,
                TerminalMissionSnapshot snapshot) {
            Mission mission = definition == null ? null : MissionRegistry.getMissionById(definition.id().getPath());
            return mission != null && mission.isPathPreview(player)
                    ? TerminalMissionRole.REFERENCE
                    : TerminalMissionRole.fallback(definition, snapshot);
        }

        @Override
        public boolean handleAction(ServerPlayer player, Identifier missionId, String actionId) {
            if (TURN_IN.getPath().equals(actionId)) {
                turnInCurrentMission(player, "");
                return true;
            }
            if (CLAIM_REWARDS.getPath().equals(actionId)) {
                EchoGuideManager.claimRewards(player);
                QuestData.syncToClient(player);
                return true;
            }
            return false;
        }

        private static TerminalMissionDefinition definition(Player player, QuestData quest,
                Mission mission, int phase, int missionOrder) {
            return new TerminalMissionDefinition(
                    id(mission.id()),
                    id("ashfall_protocol"),
                    "phase_" + (phase + 1),
                    "P" + (phase + 1),
                    phase,
                    missionOrder,
                    mission.objectiveText(),
                    mission.echoMessage(),
                    mission.completionMessage(),
                    mission.category().getDisplayName(),
                    mission.difficulty().name(),
                    missionIcon(mission),
                    prerequisiteLabels(mission),
                    requirements(player, quest, mission),
                    mission.rewards().stream().map(TerminalMissionReward::of).toList());
        }

        private static List<TerminalMissionRequirement> requirements(Player player, QuestData quest, Mission mission) {
            List<TerminalMissionRequirement> requirements = new ArrayList<>();
            for (Mission.ItemProgress progress : mission.getItemProgress(player)) {
                requirements.add(TerminalMissionRequirement.item(
                        progress.item(), progress.have(), progress.need(), progress.satisfied()));
            }
            for (Mission.BlockRequirement requirement : mission.requiredBlocks()) {
                int have = quest.getBlockPlaceCount(requirement.blockId());
                int need = Math.max(1, requirement.count());
                requirements.add(TerminalMissionRequirement.custom(
                        requirement.displayName(),
                        Math.min(have, need) + "/" + need + " placed",
                        missionIcon(mission),
                        Math.min(have, need),
                        need,
                        have >= need));
            }
            for (Mission.EntityKillRequirement requirement : mission.requiredEntityKills()) {
                int have = quest.getEntityKills(requirement.entityType());
                int need = Math.max(1, requirement.count());
                requirements.add(TerminalMissionRequirement.custom(
                        requirement.displayName(),
                        Math.min(have, need) + "/" + need + " neutralized",
                        missionIcon(mission),
                        Math.min(have, need),
                        need,
                        have >= need));
            }
            for (Mission.LocationRequirement requirement : mission.requiredLocations()) {
                boolean visited = quest.hasVisitedLocation(requirement.locationType(), requirement.locationId());
                requirements.add(TerminalMissionRequirement.custom(
                        requirement.displayName(),
                        visited ? "Archived" : "Not archived",
                        missionIcon(mission),
                        visited ? 1 : 0,
                        1,
                        visited));
            }
            for (Mission.EquipmentRequirement requirement : mission.requiredEquipment()) {
                ItemStack equipped = player == null ? ItemStack.EMPTY : player.getItemBySlot(requirement.slot());
                boolean wearing = !equipped.isEmpty() && equipped.getItem() == requirement.item().getItem();
                requirements.add(TerminalMissionRequirement.custom(
                        requirement.displayName(),
                        wearing ? "Equipped" : "Not equipped",
                        requirement.item(),
                        wearing ? 1 : 0,
                        1,
                        wearing));
            }
            if (requirements.isEmpty()) {
                boolean complete = cheapMissionSatisfied(player, quest, mission);
                requirements.add(TerminalMissionRequirement.custom(
                        mission.objectiveText(),
                        complete ? "Objective complete" : "Progress tracked by synced route state",
                        missionIcon(mission),
                        complete ? 1 : 0,
                        1,
                        complete));
            }
            return requirements;
        }

        private static List<String> prerequisiteLabels(Mission mission) {
            List<String> labels = new ArrayList<>();
            for (String prerequisite : mission.getPrerequisites()) {
                Mission prereq = MissionRegistry.getMissionById(prerequisite);
                labels.add(prereq == null ? prerequisite : prereq.objectiveText());
            }
            if (mission.isPathRestricted()) {
                labels.add("Nexus path: " + mission.requiredPath().name());
            }
            return labels;
        }

        private static boolean isCurrentMission(QuestData quest, Mission mission) {
            Mission current = MissionRegistry.getMission(quest.getCurrentPhase(), quest.getCurrentMissionIndex());
            return current != null && current.id().equals(mission.id());
        }

        private static boolean cheapMissionSatisfied(Player player, QuestData quest, Mission mission) {
            if (player == null || quest == null || mission == null) {
                return false;
            }
            if (quest.isMissionCompleted(mission.id())) {
                return true;
            }
            if (mission.validatesRequiredItems() && !mission.hasRequiredItems(player)) {
                return false;
            }
            if (mission.hasBlockRequirements() && !mission.hasRequiredBlocks(player)) {
                return false;
            }
            for (Mission.EntityKillRequirement requirement : mission.requiredEntityKills()) {
                if (quest.getEntityKills(requirement.entityType()) < requirement.count()) {
                    return false;
                }
            }
            for (Mission.LocationRequirement requirement : mission.requiredLocations()) {
                if (!quest.hasVisitedLocation(requirement.locationType(), requirement.locationId())) {
                    return false;
                }
            }
            return mission.hasRequiredEquipment(player) && mission.hasAnyRequirements();
        }

        private static float cheapProgress(
                Player player,
                QuestData quest,
                Mission mission,
                QuestData.MissionStatus status,
                boolean completeNow) {
            if (status == QuestData.MissionStatus.COMPLETED || completeNow) {
                return 1.0F;
            }
            if (player == null || quest == null || mission == null) {
                return 0.0F;
            }

            float total = 0.0F;
            int entries = 0;
            if (mission.validatesRequiredItems()) {
                for (Mission.ItemProgress progress : mission.getItemProgress(player)) {
                    int need = Math.max(1, progress.need());
                    total += Math.min(1.0F, progress.have() / (float) need);
                    entries++;
                }
            }
            for (Mission.BlockRequirement requirement : mission.requiredBlocks()) {
                int need = Math.max(1, requirement.count());
                total += Math.min(1.0F, quest.getBlockPlaceCount(requirement.blockId()) / (float) need);
                entries++;
            }
            for (Mission.EntityKillRequirement requirement : mission.requiredEntityKills()) {
                int need = Math.max(1, requirement.count());
                total += Math.min(1.0F, quest.getEntityKills(requirement.entityType()) / (float) need);
                entries++;
            }
            for (Mission.LocationRequirement requirement : mission.requiredLocations()) {
                total += quest.hasVisitedLocation(requirement.locationType(), requirement.locationId()) ? 1.0F : 0.0F;
                entries++;
            }
            for (Mission.EquipmentRequirement requirement : mission.requiredEquipment()) {
                ItemStack equipped = player.getItemBySlot(requirement.slot());
                total += !equipped.isEmpty() && equipped.getItem() == requirement.item().getItem() ? 1.0F : 0.0F;
                entries++;
            }
            return entries == 0 ? 0.0F : total / entries;
        }
    }

    private static final class AshfallSideOpsProvider implements TerminalMissionProvider {
        @Override
        public TerminalMissionChapter chapter() {
            return new TerminalMissionChapter(
                    id("ashfall_side_ops"),
                    "ECHO-7 SIGNAL LEADS",
                    "Optional lore, recon, and world-context objectives.",
                    20,
                    0xFFFFD166,
                    true);
        }

        @Override
        public List<TerminalMissionDefinition> missions(Player player) {
            QuestData quest = QuestData.get(player);
            boolean nexusChoice = player != null && PostNexusData.get(player).hasMadeChoice();
            return List.of(
                    sideOp("crash_blackbox_signal", "Crash Blackbox Signal", "PERIMETER SIGNALS", 0, 1,
                            "Recover the first telemetry thread from the drop-pod perimeter.",
                            quest.isMissionCompleted("secure_crash_outpost"), Items.RECOVERY_COMPASS),
                    sideOp("poi_field_atlas", "POI Field Atlas", "Route Records", 1, 1,
                            "Catalogue at least three POI profiles through the Route Map atlas.",
                            quest.getDiscoveredPOICount() >= 3, Items.MAP),
                    sideOp("faction_crossband", "Faction Crossband", "Human Signals", 2, 1,
                            "Identify the first living social signal in the wasteland.",
                            quest.hasVisitedLocation("special", "faction_contact:any")
                                    || quest.isMissionCompleted("first_faction_contact"), Items.EMERALD),
                    sideOp("nexus_choice_record", "Nexus Choice Record", "Nexus", 5, 1,
                            "Archive the final path commitment once RESTORE, DESTROY, or CONTROL is chosen.",
                            nexusChoice, Items.END_CRYSTAL));
        }

        @Override
        public TerminalMissionSnapshot snapshot(Player player, Identifier missionId) {
            return new TerminalMissionSnapshot(missionId, TerminalMissionStatus.VIEW_ONLY, 0.0F,
                    "OPTIONAL", "", "Signal lead progress is rendered by the Ashfall client tab.", List.of());
        }

        private static TerminalMissionDefinition sideOp(String path, String title, String phase,
                int phaseOrder, int order, String briefing, boolean complete, Item icon) {
            return new TerminalMissionDefinition(
                    id(path),
                    id("ashfall_side_ops"),
                    phase.toLowerCase(Locale.ROOT).replace(' ', '_'),
                    phase,
                    phaseOrder,
                    order,
                    title,
                    briefing,
                    briefing,
                    "Field Recon",
                    "Recon",
                    new ItemStack(icon),
                    List.of(),
                    List.of(TerminalMissionRequirement.custom(title, complete ? "Archived" : "Pending",
                            new ItemStack(icon), complete ? 1 : 0, 1, complete)),
                    List.of(TerminalMissionReward.text("Archive Context",
                            "Adds tactical field context only; required route progress and caches stay unchanged.")));
        }
    }

    private static boolean safeComplete(Mission mission, Player player) {
        try {
            return mission != null && player != null && mission.isComplete(player);
        } catch (RuntimeException exception) {
            EchoAshfallProtocol.LOGGER.warn("Ashfall terminal mission completion check failed for {}.",
                    mission == null ? "<null>" : mission.id(), exception);
            return false;
        }
    }

    private static ItemStack missionIcon(Mission mission) {
        ItemStack objective = mission.getObjectiveItem();
        if (!objective.isEmpty()) {
            return objective.copy();
        }
        if (mission.objectiveIcon() != null) {
            Item item = BuiltInRegistries.ITEM.getOptional(mission.objectiveIcon()).orElse(Items.AIR);
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
        }
        return switch (mission.category()) {
            case SURVIVAL -> new ItemStack(Items.CAMPFIRE);
            case CRAFTING -> new ItemStack(Items.CRAFTING_TABLE);
            case EXPLORATION -> new ItemStack(Items.COMPASS);
            case COMBAT -> new ItemStack(Items.IRON_SWORD);
            case TECH -> new ItemStack(Items.REDSTONE);
            case STORY -> new ItemStack(Items.WRITABLE_BOOK);
        };
    }

    private static void registerFieldManualEntries() {
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                id("ashfall_survival_manual"),
                "Outpost Survival",
                "Ashfall Survival Manual",
                "OPEN",
                List.of(
                        "Gridfall did not leave a normal wilderness behind. The safe route is water, shelter, food, filters, and a powered outpost before distance.",
                        "Mission records are tactical briefings, not shortcuts. ECHO-7 can show the road ahead, but field validation still confirms every turn-in and reward.",
                        "Hazards stack quickly: toxic air, radiation, cold, acid contact, storms, and Nexus anomalies each need a different countermeasure."),
                false));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                id("ashfall_progression_manual"),
                "Protocol Flow",
                "Protocol Roadmap Rules",
                "OPEN",
                List.of(
                        "Locked missions are visible for planning, but completion, rewards, and phase advancement remain gated by QuestData.",
                        "Public beta route guidance ends at Orbital handoff. Legacy Nexus save state remains readable, but Ashfall no longer owns the finale terminal."),
                false));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                id("ashfall_drone_manual"),
                "Machine Systems",
                "Companion Drone Protocols",
                "OPEN",
                List.of(
                        "Drone commands are tactical orders routed through the terminal and confirmed against live field state.",
                        "Follow unlocks immediately. Scout and light require partial repairs, combat and scavenge require operational integrity, and patrol requires enhanced integrity.",
                        "The drone is not only gear. It is ECHO-7's moving witness, and the Scout Drone fallback keeps that witness active when the companion shell is gone."),
                false));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                id("ashfall_nexus_manual"),
                "Recovered Lore",
                "Nexus Path Interface",
                "OPEN",
                List.of(
                        "The Nexus Core can commit RESTORE, DESTROY, or CONTROL once the guardian chain, Warfront relays, countermeasure siege, and five Power Nodes are resolved.",
                        "The chosen path is mirrored through Echo Core services so addon chapters can react without owning Ashfall's route state.",
                        "When Nexus Protocol is installed, its chapter owns late-game terminal handoff and completion milestones."),
                false));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                id("ashfall_poi_atlas"),
                "Signal Logs",
                "POI Field Atlas",
                "OPEN",
                List.of(
                        "The Route Map POI Atlas groups every surface template signal under the scanner profile that owns its gameplay identity.",
                        "Individual ruins are not separate save objectives. ECHO tracks the route profile, then lists template variants for field recognition.",
                        "Muted template rows mean the parent route has not been scanned yet."),
                false));
        TerminalArchiveRegistry.register(new TerminalArchiveEntry(
                id("ashfall_faction_threads"),
                "Recovered Lore",
                "Faction Signal Threads",
                "OPEN",
                List.of(
                        "Three Ashfall factions report through Echo Core: Radwarden Compact containment crews, Crashbreak Salvage route builders, and Sporebound Sanctum anomaly interpreters.",
                        "Faction work is not separate from the main route: contacts, contracts, services, and standing all feed the same synced Echo Core record.",
                        "Orbital lanes mirror those same three pressures after the Nexus choice reaches orbit: Radwarden containment, Crashbreak salvage, and Sporebound anomaly reading."),
                false));
    }
}
