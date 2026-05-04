package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echoashfallprotocol.world.ExplorationSiteRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for all faction quests in the game.
 * Includes quests for Remnants, Salvagers, and Mutants.
 */
public class FactionQuestRegistry {
    
    private static final Map<String, FactionQuest> QUESTS = new HashMap<>();
    private static final List<FactionQuest> REMNANT_QUESTS = new ArrayList<>();
    private static final List<FactionQuest> SALVAGER_QUESTS = new ArrayList<>();
    private static final List<FactionQuest> MUTANT_QUESTS = new ArrayList<>();
    
    // === REMNANT QUESTS (Military/Tech focus) ===
    public static final FactionQuest REMNANT_PATROL = register(new FactionQuest(
        "remnant.patrol", "Perimeter Supply Audit",
        "Deliver basic salvage so the Remnants can patch weak points before they let an outsider stand inside the wire.",
        ReputationData.Faction.REMNANTS, FactionQuest.QuestType.DELIVERY, FactionQuest.Difficulty.EASY,
        new String[]{"Deliver 6 scrap metal", "Report to the perimeter sergeant"},
        List.of(obj(FactionQuest.ObjectiveType.ITEM_DELIVERY, "echoashfallprotocol:scrap_metal", 6, "Deliver perimeter scrap", ReputationData.Faction.REMNANTS)),
        new String[]{"echoashfallprotocol:power_cell", "echoashfallprotocol:bandage", "echoashfallprotocol:filter_cartridge_basic"},
        0 // Neutral can accept
    ));
    
    public static final FactionQuest REMNANT_RECOVERY = register(new FactionQuest(
        "remnant.recovery", "Tech Recovery",
        "Locate a hardened salvage site and recover electronics the Remnants can fold back into patrol logistics.",
        ReputationData.Faction.REMNANTS, FactionQuest.QuestType.RETRIEVAL, FactionQuest.Difficulty.MEDIUM,
        new String[]{"Locate military salvage", "Recover a circuit board", "Return to the quartermaster"},
        List.of(obj(FactionQuest.ObjectiveType.POI_DISCOVERY, "military_vault", 1, "Locate military salvage", ReputationData.Faction.REMNANTS),
            obj(FactionQuest.ObjectiveType.ITEM_DELIVERY, "echoashfallprotocol:circuit_board", 1, "Deliver a circuit board", ReputationData.Faction.REMNANTS)),
        new String[]{"echoashfallprotocol:circuit_board", "echoashfallprotocol:energy_cell", "echoashfallprotocol:filter_cartridge_advanced"},
        25 // Friendly required
    ));
    
    public static final FactionQuest REMNANT_DEFENSE = register(new FactionQuest(
        "remnant.defense", "Outpost Defense",
        "Hold a Remnant position long enough for their generator crew to keep the safe lights on.",
        ReputationData.Faction.REMNANTS, FactionQuest.QuestType.DEFENSE, FactionQuest.Difficulty.HARD,
        new String[]{"Hold through the raid", "Keep the generator line intact", "Report to the drill sergeant"},
        List.of(obj(FactionQuest.ObjectiveType.RAID_DEFENSE, "remnants", 1, "Defend a Remnant position", ReputationData.Faction.REMNANTS)),
        new String[]{"echoashfallprotocol:dense_alloy_chunk", "echoashfallprotocol:machine_casing", "echoashfallprotocol:bandage"},
        50 // Friendly required
    ));
    
    public static final FactionQuest REMNANT_SABOTAGE = register(new FactionQuest(
        "remnant.sabotage", "Relay Severance",
        "Enter a reactor relay route and cut damaged command traffic before it routes something worse back to the outpost.",
        ReputationData.Faction.REMNANTS, FactionQuest.QuestType.RECON, FactionQuest.Difficulty.EXTREME,
        new String[]{"Locate the reactor relay route", "Disable or recalibrate 3 relays", "Leave before the network notices"},
        List.of(obj(FactionQuest.ObjectiveType.POI_DISCOVERY, "reactor_ruin", 1, "Locate reactor relay route", ReputationData.Faction.REMNANTS),
            obj(FactionQuest.ObjectiveType.REPAIR, "relay", 3, "Disable or recalibrate relays", ReputationData.Faction.REMNANTS)),
        new String[]{"echoashfallprotocol:scout_drone_item", "echoashfallprotocol:energy_cell", "echoashfallprotocol:nexus_crystal"},
        75 // Allied required
    ));
    
    // === SALVAGER QUESTS (Trade/Scavenging focus) ===
    public static final FactionQuest SALVAGER_DELIVERY = register(new FactionQuest(
        "salvager.delivery", "Supply Run",
        "Deliver useful trade stock and prove you can move goods without turning every favor into a rescue bill.",
        ReputationData.Faction.SALVAGERS, FactionQuest.QuestType.DELIVERY, FactionQuest.Difficulty.EASY,
        new String[]{"Deliver 6 scrap metal", "Deliver 1 clean water", "Return to the route merchant"},
        List.of(obj(FactionQuest.ObjectiveType.ITEM_DELIVERY, "echoashfallprotocol:scrap_metal", 6, "Deliver trade scrap", ReputationData.Faction.SALVAGERS),
            obj(FactionQuest.ObjectiveType.ITEM_DELIVERY, "echoashfallprotocol:clean_water_bottle", 1, "Deliver clean water", ReputationData.Faction.SALVAGERS)),
        new String[]{"echoashfallprotocol:scrap_wire", "echoashfallprotocol:clean_water_bottle", "echoashfallprotocol:energy_cell"},
        0 // Neutral can accept
    ));
    
    public static final FactionQuest SALVAGER_SALVAGE = register(new FactionQuest(
        "salvager.salvage", "Wreck Salvage",
        "Survey a factory shell and pull enough scrap to prove the route is worth selling twice.",
        ReputationData.Faction.SALVAGERS, FactionQuest.QuestType.RETRIEVAL, FactionQuest.Difficulty.MEDIUM,
        new String[]{"Explore the factory route", "Recover machine scrap", "Watch the exits"},
        List.of(obj(FactionQuest.ObjectiveType.POI_DISCOVERY, "industrial_factory", 1, "Locate salvage site", ReputationData.Faction.SALVAGERS),
            obj(FactionQuest.ObjectiveType.ITEM_DELIVERY, "echoashfallprotocol:scrap_metal", 12, "Deliver route scrap", ReputationData.Faction.SALVAGERS)),
        new String[]{"echoashfallprotocol:scrap_metal", "echoashfallprotocol:dense_alloy_chunk", "echoashfallprotocol:schematic_fragment_machines"},
        25 // Friendly required
    ));
    
    public static final FactionQuest SALVAGER_EXPEDITION = register(new FactionQuest(
        "salvager.expedition", "Deep Expedition",
        "Map an industrial ruin and bring back dense alloy before greed keeps you underground too long.",
        ReputationData.Faction.SALVAGERS, FactionQuest.QuestType.RECON, FactionQuest.Difficulty.HARD,
        new String[]{"Map the industrial ruins", "Recover dense alloy", "Retreat before exposure stacks"},
        List.of(obj(FactionQuest.ObjectiveType.POI_DISCOVERY, "industrial_ruins", 1, "Map a resource site", ReputationData.Faction.SALVAGERS),
            obj(FactionQuest.ObjectiveType.ITEM_DELIVERY, "echoashfallprotocol:dense_alloy_chunk", 3, "Deliver dense alloy", ReputationData.Faction.SALVAGERS)),
        new String[]{"echoashfallprotocol:gem_fragment", "echoashfallprotocol:schematic_fragment_machines", "echoashfallprotocol:nexus_crystal"},
        50 // Friendly required
    ));
    
    public static final FactionQuest SALVAGER_NEGOTIATION = register(new FactionQuest(
        "salvager.negotiation", "High-Stakes Negotiation",
        "Carry a Nexus Crystal as leverage and turn rival faction tension into a route the Salvagers can keep open.",
        ReputationData.Faction.SALVAGERS, FactionQuest.QuestType.DELIVERY, FactionQuest.Difficulty.EXTREME,
        new String[]{"Visit two faction hubs", "Stake a Nexus Crystal", "Return with a live contract"},
        List.of(obj(FactionQuest.ObjectiveType.RECON, "faction_hub", 2, "Visit two faction hubs", null),
            obj(FactionQuest.ObjectiveType.ITEM_DELIVERY, "echoashfallprotocol:nexus_crystal", 1, "Stake a Nexus Crystal", ReputationData.Faction.SALVAGERS)),
        new String[]{"echoashfallprotocol:nexus_crystal", "echoashfallprotocol:dense_alloy_chunk", "echoashfallprotocol:clean_water_bottle"},
        75 // Allied required
    ));
    
    // === MUTANT QUESTS (Bio/Adaptation focus) ===
    public static final FactionQuest MUTANT_HUNT = register(new FactionQuest(
        "mutant.hunt", "Sample Exchange",
        "Provide a controlled biological sample so the Mutant Front can tune medicine without guessing on living bodies.",
        ReputationData.Faction.MUTANTS, FactionQuest.QuestType.RETRIEVAL, FactionQuest.Difficulty.EASY,
        new String[]{"Deliver 1 mutated tissue", "Confirm safe sample handling"},
        List.of(obj(FactionQuest.ObjectiveType.ITEM_DELIVERY, "echoashfallprotocol:mutated_tissue", 1, "Deliver controlled tissue", ReputationData.Faction.MUTANTS)),
        new String[]{"echoashfallprotocol:mutagen_vial", "echoashfallprotocol:rad_away", "echoashfallprotocol:bandage"},
        0 // Neutral can accept
    ));
    
    public static final FactionQuest MUTANT_RESCUE = register(new FactionQuest(
        "mutant.rescue", "Stranded Mutants",
        "Locate stranded adapted survivors and bring enough treatment for the ones still fighting the air.",
        ReputationData.Faction.MUTANTS, FactionQuest.QuestType.RETRIEVAL, FactionQuest.Difficulty.MEDIUM,
        new String[]{"Locate stranded survivors", "Deliver treatment supplies", "Mark a safer exit"},
        List.of(obj(FactionQuest.ObjectiveType.POI_DISCOVERY, "toxic_swamp", 1, "Locate contaminated zone", ReputationData.Faction.MUTANTS),
            obj(FactionQuest.ObjectiveType.ITEM_DELIVERY, "echoashfallprotocol:rad_away", 2, "Deliver RadAway", ReputationData.Faction.MUTANTS)),
        new String[]{"echoashfallprotocol:rad_away", "echoashfallprotocol:mutagen_vial", "echoashfallprotocol:filter_cartridge_advanced"},
        25 // Friendly required
    ));
    
    public static final FactionQuest MUTANT_PURIFICATION = register(new FactionQuest(
        "mutant.purification", "Zone Purification",
        "Repair purifier units and hold the safe zone while the swamp decides whether to give the air back.",
        ReputationData.Faction.MUTANTS, FactionQuest.QuestType.REPAIR, FactionQuest.Difficulty.HARD,
        new String[]{"Repair 3 purification units", "Defend during startup", "Confirm the air holds"},
        List.of(obj(FactionQuest.ObjectiveType.REPAIR, "purifier", 3, "Repair purifier units", ReputationData.Faction.MUTANTS),
            obj(FactionQuest.ObjectiveType.RAID_DEFENSE, "mutants", 1, "Defend the safe zone", ReputationData.Faction.MUTANTS)),
        new String[]{"echoashfallprotocol:filtration_membrane", "echoashfallprotocol:clean_water_bottle", "echoashfallprotocol:stim_pack"},
        50 // Friendly required
    ));
    
    public static final FactionQuest MUTANT_EVOLUTION = register(new FactionQuest(
        "mutant.evolution", "Evolution Trial",
        "Enter an extreme radiation route and prove you can survive mutation pressure without surrendering judgment.",
        ReputationData.Faction.MUTANTS, FactionQuest.QuestType.RECON, FactionQuest.Difficulty.EXTREME,
        new String[]{"Enter extreme radiation", "Defeat evolved mutants", "Return before the change gets expensive"},
        List.of(obj(FactionQuest.ObjectiveType.POI_DISCOVERY, "radiation_zone", 1, "Enter extreme radiation", null),
            obj(FactionQuest.ObjectiveType.KILL, "mutant", 5, "Defeat evolved threats", ReputationData.Faction.MUTANTS)),
        new String[]{"echoashfallprotocol:nexus_crystal", "echoashfallprotocol:mutagen_vial", "echoashfallprotocol:rad_away"},
        75 // Allied required
    ));

    private static FactionQuest.Objective obj(FactionQuest.ObjectiveType type, String targetId, int count, String text, ReputationData.Faction faction) {
        return new FactionQuest.Objective(type, targetId, count, text, faction);
    }
    
    private static FactionQuest register(FactionQuest quest) {
        QUESTS.put(quest.getId(), quest);
        
        // Add to faction-specific list
        switch (quest.getFaction()) {
            case REMNANTS -> REMNANT_QUESTS.add(quest);
            case SALVAGERS -> SALVAGER_QUESTS.add(quest);
            case MUTANTS -> MUTANT_QUESTS.add(quest);
        }
        
        return quest;
    }
    
    public static FactionQuest get(String id) {
        return QUESTS.get(id);
    }
    
    public static List<FactionQuest> getAll() {
        return new ArrayList<>(QUESTS.values());
    }
    
    public static List<FactionQuest> getForFaction(ReputationData.Faction faction) {
        return switch (faction) {
            case REMNANTS -> new ArrayList<>(REMNANT_QUESTS);
            case SALVAGERS -> new ArrayList<>(SALVAGER_QUESTS);
            case MUTANTS -> new ArrayList<>(MUTANT_QUESTS);
        };
    }
    
    public static List<FactionQuest> getAvailableForPlayer(net.minecraft.world.entity.player.Player player, ReputationData.Faction faction) {
        List<FactionQuest> available = new ArrayList<>();
        ReputationData rep = ReputationData.get(player);
        FactionQuestData questData = FactionQuestData.get(player);
        
        for (FactionQuest quest : getForFaction(faction)) {
            if (!questData.isCompleted(quest.getId())
                && !questData.hasActiveQuest(faction)
                && rep.getReputation(faction) >= quest.getRequiredReputation()) {
                available.add(quest);
            }
        }
        
        return available;
    }

    public static List<String> validationWarnings() {
        List<String> warnings = new ArrayList<>();

        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            List<FactionQuest> quests = getForFaction(faction);
            if (quests.size() != 4) {
                warnings.add(faction.name() + " should have exactly 4 quests, found " + quests.size());
            }

            List<Integer> requiredReputation = quests.stream()
                    .map(FactionQuest::getRequiredReputation)
                    .sorted()
                    .toList();
            if (!requiredReputation.equals(List.of(0, 25, 50, 75))) {
                warnings.add(faction.name() + " quest required reputation ladder should be 0/25/50/75, found " + requiredReputation);
            }
        }

        for (FactionQuest quest : getAll()) {
            if (quest.getId().isBlank()) {
                warnings.add("Quest has blank id");
            }
            if (quest.getTitle().isBlank()) {
                warnings.add(quest.getId() + " has blank title");
            }
            if (quest.getDescription().isBlank()) {
                warnings.add(quest.getId() + " has blank description");
            }
            if (quest.getObjectives().length == 0) {
                warnings.add(quest.getId() + " has no objective copy");
            }
            for (String objectiveCopy : quest.getObjectives()) {
                if (objectiveCopy == null || objectiveCopy.isBlank()) {
                    warnings.add(quest.getId() + " has blank objective copy");
                }
            }
            if (quest.getObjectiveModels().isEmpty()) {
                warnings.add(quest.getId() + " has no objective models");
            }
            for (FactionQuest.Objective objective : quest.getObjectiveModels()) {
                validateObjective(warnings, quest, objective);
            }
            if (quest.getRewardItems().length == 0) {
                warnings.add(quest.getId() + " has no reward items");
            }
            for (String rewardItem : quest.getRewardItems()) {
                validateItemId(warnings, quest.getId(), "reward", rewardItem);
            }
        }

        return warnings;
    }

    private static void validateObjective(List<String> warnings, FactionQuest quest, FactionQuest.Objective objective) {
        if (objective.displayText().isBlank()) {
            warnings.add(quest.getId() + " has blank objective display text");
        }
        if (objective.requiredCount() <= 0) {
            warnings.add(quest.getId() + " objective has non-positive count: " + objective.displayText());
        }
        if (objective.type() == FactionQuest.ObjectiveType.ITEM_DELIVERY) {
            validateItemId(warnings, quest.getId(), "delivery target", objective.targetId());
        }
        if (objective.type() == FactionQuest.ObjectiveType.POI_DISCOVERY
                || objective.type() == FactionQuest.ObjectiveType.RECON) {
            validatePoiTarget(warnings, quest.getId(), objective.targetId());
        }
    }

    private static void validateItemId(List<String> warnings, String questId, String field, String itemId) {
        if (itemId == null || itemId.isBlank()) {
            warnings.add(questId + " has blank " + field + " item id");
            return;
        }
        try {
            Identifier id = Identifier.parse(itemId);
            if (BuiltInRegistries.ITEM.getOptional(id).isEmpty()) {
                warnings.add(questId + " " + field + " item id is not registered: " + itemId);
            }
        } catch (Exception ex) {
            warnings.add(questId + " " + field + " item id is invalid: " + itemId);
        }
    }

    private static void validatePoiTarget(List<String> warnings, String questId, String targetId) {
        if (targetId == null || targetId.isBlank()) {
            warnings.add(questId + " has blank POI target");
            return;
        }
        if ("faction_hub".equals(targetId)) {
            return;
        }
        String normalized = ExplorationSiteRegistry.normalize(targetId);
        if (ExplorationSiteRegistry.get(normalized).isEmpty()) {
            warnings.add(questId + " POI target does not resolve to a scanner profile: " + targetId);
        }
    }
    
    public static void init() {
        // Static initialization happens above
    }
}
