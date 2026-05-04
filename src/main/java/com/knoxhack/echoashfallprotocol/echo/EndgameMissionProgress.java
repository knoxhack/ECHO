package com.knoxhack.echoashfallprotocol.echo;

import com.knoxhack.echoashfallprotocol.block.NexusCoreBlock;
import com.knoxhack.echoashfallprotocol.block.entity.NexusCoreBlockEntity;
import com.knoxhack.echoashfallprotocol.endgame.PostNexusData;
import com.knoxhack.echoashfallprotocol.guardian.BiomeGuardianProfile;
import com.knoxhack.echoashfallprotocol.guardian.BiomeGuardianProfiles;
import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Presentation-only progress for Phase 7/8 objectives whose authority lives in
 * QuestData/PostNexusData rather than normal item, block, or kill requirements.
 */
public final class EndgameMissionProgress {
    private static final int CORE_SCAN_HORIZONTAL = 24;
    private static final int CORE_SCAN_VERTICAL = 8;
    private static final String ARCHIVES_LOCATION = "echoashfallprotocol:prefall_archives";

    private EndgameMissionProgress() {
    }

    public static Optional<Snapshot> forMission(Player player, QuestData quest, Mission mission) {
        if (player == null || quest == null || mission == null) {
            return Optional.empty();
        }

        boolean completed = quest.isMissionCompleted(mission.id());
        PostNexusData post = PostNexusData.get(player);
        return switch (mission.id()) {
            case "find_nexus_core" -> Optional.of(snapshot(line(
                    "Nexus Core range",
                    completed || coreStatus(player).nearby(),
                    completed || coreStatus(player).nearby() ? 1 : 0,
                    1,
                    completed || coreStatus(player).nearby() ? "Core signal acquired"
                            : "Stand near the unresolved Nexus Core.",
                    new ItemStack(ModBlocks.NEXUS_CORE_ITEM.get()),
                    "Stand near the unresolved Nexus Core.")));
            case "stabilize_nexus_grid" -> Optional.of(nexusGridSnapshot(player, completed));
            case "reach_decision" -> Optional.of(reachDecisionSnapshot(player, quest, post, completed));
            case "restore_repair_nodes" -> Optional.of(snapshot(line(
                    "Power Nodes activated",
                    completed || post.getNodesRepaired() >= 3,
                    completed ? 3 : post.getNodesRepaired(),
                    3,
                    "Activate " + remaining(3, post.getNodesRepaired()) + " more Power Nodes.",
                    new ItemStack(ModBlocks.POWER_NODE_ITEM.get()),
                    "Activate or rebuild Power Nodes; crafted replacements count.")));
            case "restore_purge_corruption" -> Optional.of(snapshot(line(
                    "Corrupted hostiles neutralized",
                    completed || post.getCorruptedMobsKilled() >= 20,
                    completed ? 20 : post.getCorruptedMobsKilled(),
                    20,
                    "Neutralize " + remaining(20, post.getCorruptedMobsKilled()) + " more corrupted hostiles.",
                    new ItemStack(ModItems.RAD_AWAY.get()),
                    "Clear corrupted hostiles around damaged routes.")));
            case "restore_enter_archives", "destroy_enter_archives", "control_enter_archives" ->
                    Optional.of(archivesEntrySnapshot(quest, post, completed));
            case "restore_guardian", "destroy_guardian", "control_guardian" ->
                    Optional.of(wardenSnapshot(post, completed));
            case "restore_epilogue", "destroy_epilogue", "control_epilogue" ->
                    Optional.of(epilogueSnapshot(mission, post, completed));
            case "destroy_scorched_earth" -> Optional.of(snapshot(line(
                    "Power Nodes destroyed",
                    completed || post.getNodesDestroyed() >= 5,
                    completed ? 5 : post.getNodesDestroyed(),
                    5,
                    "Destroy " + remaining(5, post.getNodesDestroyed()) + " more Power Nodes.",
                    new ItemStack(ModItems.NEXUS_ANNIHILATOR.get()),
                    "Break Power Nodes; crafted or rebuilt nodes keep the route recoverable.")));
            case "destroy_survive_storms" -> Optional.of(snapshot(line(
                    "Severe storm survived",
                    completed || post.getStormsSurvived() >= 1,
                    completed ? 1 : post.getStormsSurvived(),
                    1,
                    post.getStormsSurvived() >= 1 ? "Storm survival logged"
                            : "Shelter through a radiation storm, ash storm, Nexus surge, or thunder event.",
                    new ItemStack(ModItems.MUTAGEN_VIAL.get()),
                    "Use shelter, RadAway, and a recovery route during a severe storm.")));
            case "control_signal_expansion" -> Optional.of(snapshot(line(
                    "Signal beacons placed",
                    completed || post.getSignalBoostersPlaced() >= 3,
                    completed ? 3 : post.getSignalBoostersPlaced(),
                    3,
                    "Place " + remaining(3, post.getSignalBoostersPlaced()) + " more Signal Scanners or Relay Stations.",
                    new ItemStack(ModBlocks.SIGNAL_SCANNER_ITEM.get()),
                    "Place Signal Scanner or Relay Station blocks in the Overworld.")));
            case "control_resource_dominance" -> Optional.of(controlResourcesSnapshot(player, post, completed));
            default -> Optional.empty();
        };
    }

    private static Snapshot nexusGridSnapshot(Player player, boolean completed) {
        CoreStatus core = coreStatus(player);
        int have = completed ? NexusCoreBlock.REQUIRED_NODES : core.activatedNodes();
        String detail = core.nearby()
                ? have + "/" + NexusCoreBlock.REQUIRED_NODES + " active near Core"
                : "No unresolved Core in range";
        return snapshot(line(
                "Nexus grid anchors",
                completed || (core.nearby() && have >= NexusCoreBlock.REQUIRED_NODES),
                have,
                NexusCoreBlock.REQUIRED_NODES,
                detail,
                new ItemStack(ModBlocks.POWER_NODE_ITEM.get()),
                core.nearby()
                        ? "Activate " + remaining(NexusCoreBlock.REQUIRED_NODES, have) + " more Power Nodes near the Core."
                        : "Stand near the unresolved Nexus Core and activate nearby Power Nodes."));
    }

    private static Snapshot reachDecisionSnapshot(Player player, QuestData quest, PostNexusData post, boolean completed) {
        CoreStatus core = coreStatus(player);
        GuardianStatus guardians = guardianStatus(quest);
        Entry guardianEntry = line(
                "Guardian signals resolved",
                completed || guardians.resolved() >= guardians.total(),
                completed ? guardians.total() : guardians.resolved(),
                guardians.total(),
                guardians.resolved() + "/" + guardians.total() + " guardians resolved",
                new ItemStack(ModItems.NEXUS_CRYSTAL.get()),
                guardians.missing() == 0 ? "Guardian chain complete."
                        : "Defeat the remaining guardian: " + guardians.firstMissing() + ".");
        Entry gridEntry = line(
                "Nexus grid anchors",
                completed || (core.nearby() && core.activatedNodes() >= NexusCoreBlock.REQUIRED_NODES),
                completed ? NexusCoreBlock.REQUIRED_NODES : core.activatedNodes(),
                NexusCoreBlock.REQUIRED_NODES,
                core.nearby()
                        ? core.activatedNodes() + "/" + NexusCoreBlock.REQUIRED_NODES + " active near Core"
                        : "Stand near the unresolved Core",
                new ItemStack(ModBlocks.POWER_NODE_ITEM.get()),
                core.nearby()
                        ? "Activate " + remaining(NexusCoreBlock.REQUIRED_NODES, core.activatedNodes()) + " more Power Nodes near the Core."
                        : "Stand near the unresolved Nexus Core.");
        Entry choiceEntry = line(
                "Permanent path confirmed",
                completed || post.hasMadeChoice(),
                completed || post.hasMadeChoice() ? 1 : 0,
                1,
                post.hasMadeChoice() ? "Path committed: " + post.getSelectedPath().name()
                        : "Open the NEXUS tab, select a path, then confirm.",
                new ItemStack(ModBlocks.NEXUS_CORE_ITEM.get()),
                "Open the NEXUS tab and confirm RESTORE, DESTROY, or CONTROL. This is permanent.");
        return snapshot(guardianEntry, gridEntry, choiceEntry);
    }

    private static Snapshot archivesEntrySnapshot(QuestData quest, PostNexusData post, boolean completed) {
        boolean entered = completed || post.hasEnteredArchives()
                || quest.hasVisitedLocation("dimension", ARCHIVES_LOCATION);
        return snapshot(line(
                "Pre-Fall Archives entry",
                entered,
                entered ? 1 : 0,
                1,
                entered ? "Archive entry confirmed" : "Use Archives Key in Overworld",
                new ItemStack(ModItems.PREFALL_ARCHIVES_KEY.get()),
                "Right-click the Archives Key on overworld ground. Lost keys are craftable."));
    }

    private static Snapshot wardenSnapshot(PostNexusData post, boolean completed) {
        boolean defeated = completed || post.isWardenDefeated();
        return snapshot(line(
                "The Warden defeated",
                defeated,
                defeated ? 1 : 0,
                1,
                defeated ? "Warden signal down" : "Archives defender still active",
                new ItemStack(ModItems.WARDEN_ARCHIVE_CIPHER.get()),
                "Enter the Archives with a Return Keystone; prepare for defender lockdowns and pulse phases."));
    }

    private static Snapshot epilogueSnapshot(Mission mission, PostNexusData post, boolean completed) {
        boolean ready = completed || post.isWardenDefeated();
        String path = mission.requiredPath() == null ? "final" : mission.requiredPath().name();
        return snapshot(line(
                path + " epilogue confirmation",
                ready,
                ready ? 1 : 0,
                1,
                ready ? "Ready for terminal turn-in" : "Defeat The Warden first",
                new ItemStack(ModBlocks.NEXUS_CORE_ITEM.get()),
                ready ? "Return the epilogue through the mission channel."
                        : "Defeat The Warden, then confirm the epilogue in the mission channel."));
    }

    private static Snapshot controlResourcesSnapshot(Player player, PostNexusData post, boolean completed) {
        int dense = completed ? PostNexusData.CONTROL_DENSE_ALLOY_REQUIRED : Math.max(
                post.getDenseAlloyCollected(),
                countItem(player, ModItems.DENSE_ALLOY_CHUNK.get()));
        int crystals = completed ? PostNexusData.CONTROL_NEXUS_CRYSTALS_REQUIRED : Math.max(
                post.getNexusCrystalsCollected(),
                countItem(player, ModItems.NEXUS_CRYSTAL.get()));
        int cells = completed ? PostNexusData.CONTROL_ENERGY_CELLS_REQUIRED : Math.max(
                post.getEnergyCellsCollected(),
                countItem(player, ModItems.ENERGY_CELL.get()));
        return snapshot(
                resourceLine("Dense Alloy Chunks", dense, PostNexusData.CONTROL_DENSE_ALLOY_REQUIRED,
                        new ItemStack(ModItems.DENSE_ALLOY_CHUNK.get())),
                resourceLine("Nexus Crystals", crystals, PostNexusData.CONTROL_NEXUS_CRYSTALS_REQUIRED,
                        new ItemStack(ModItems.NEXUS_CRYSTAL.get())),
                resourceLine("Energy Cells", cells, PostNexusData.CONTROL_ENERGY_CELLS_REQUIRED,
                        new ItemStack(ModItems.ENERGY_CELL.get())));
    }

    private static Entry resourceLine(String label, int have, int need, ItemStack icon) {
        return line(
                label,
                have >= need,
                have,
                need,
                Math.min(have, need) + "/" + need + " tracked or held",
                icon,
                "Collect or carry " + remaining(need, have) + " more " + label + ".");
    }

    private static Snapshot snapshot(Entry... entries) {
        return new Snapshot(List.of(entries));
    }

    private static Entry line(String label, boolean satisfied, int have, int need,
            String detail, ItemStack icon, String nextStep) {
        int safeNeed = Math.max(1, need);
        int safeHave = satisfied ? safeNeed : Math.max(0, Math.min(have, safeNeed));
        return new Entry(label, detail, icon, safeHave, safeNeed, satisfied, satisfied ? "" : nextStep);
    }

    private static CoreStatus coreStatus(Player player) {
        Level level = player.level();
        BlockPos center = player.blockPosition();
        boolean sawCore = false;
        int bestNodes = 0;
        for (BlockPos cursor : BlockPos.betweenClosed(
                center.offset(-CORE_SCAN_HORIZONTAL, -CORE_SCAN_VERTICAL, -CORE_SCAN_HORIZONTAL),
                center.offset(CORE_SCAN_HORIZONTAL, CORE_SCAN_VERTICAL, CORE_SCAN_HORIZONTAL))) {
            if (!level.getBlockState(cursor).is(ModBlocks.NEXUS_CORE.get())) {
                continue;
            }
            sawCore = true;
            BlockEntity blockEntity = level.getBlockEntity(cursor);
            if (blockEntity instanceof NexusCoreBlockEntity core && !core.hasChoiceBeenMade()) {
                bestNodes = Math.max(bestNodes, core.getActivatedNodeCount(level, core.getBlockPos()));
            }
        }
        return new CoreStatus(sawCore, bestNodes);
    }

    private static GuardianStatus guardianStatus(QuestData quest) {
        int total = BiomeGuardianProfiles.all().size();
        int resolved = 0;
        String firstMissing = "unknown route";
        for (BiomeGuardianProfile profile : BiomeGuardianProfiles.all()) {
            boolean done = quest.isMissionCompleted(profile.missionId())
                    || quest.getEntityKills(profile.entityId()) >= 1;
            if (done) {
                resolved++;
            } else if ("unknown route".equals(firstMissing)) {
                firstMissing = profile.title();
            }
        }
        return new GuardianStatus(total, resolved, Math.max(0, total - resolved), firstMissing);
    }

    private static int countItem(Player player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int remaining(int need, int have) {
        return Math.max(0, need - Math.max(0, have));
    }

    public record Entry(String label, String detail, ItemStack icon, int have, int need,
                        boolean satisfied, String nextStep) {
        public Entry {
            label = label == null ? "" : label.trim();
            detail = detail == null ? "" : detail.trim();
            icon = icon == null ? ItemStack.EMPTY : icon.copy();
            need = Math.max(1, need);
            have = Math.max(0, Math.min(have, need));
            nextStep = nextStep == null ? "" : nextStep.trim();
        }
    }

    public record Snapshot(List<Entry> entries) {
        public Snapshot {
            entries = List.copyOf(entries == null ? List.of() : entries);
        }

        public float progress() {
            if (entries.isEmpty()) {
                return 0.0F;
            }
            float total = 0.0F;
            for (Entry entry : entries) {
                total += entry.satisfied() ? 1.0F : Math.min(1.0F, (float) entry.have() / (float) entry.need());
            }
            return total / entries.size();
        }

        public String firstOpenStep() {
            for (Entry entry : entries) {
                if (!entry.satisfied() && !entry.nextStep().isBlank()) {
                    return entry.nextStep();
                }
            }
            return "";
        }
    }

    private record CoreStatus(boolean nearby, int activatedNodes) {
    }

    private record GuardianStatus(int total, int resolved, int missing, String firstMissing) {
    }
}
