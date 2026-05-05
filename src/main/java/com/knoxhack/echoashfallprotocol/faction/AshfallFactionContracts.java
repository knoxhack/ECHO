package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echocore.api.EchoFactionContract;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/**
 * Contract catalogue for the 10 Ashfall Echo Core factions.
 */
public final class AshfallFactionContracts {
    private static final Map<String, Loadout> LOADOUTS = new LinkedHashMap<>();

    static {
        loadout("survivor_network", "Survivor Network", "Shelter", ServiceKind.SUPPLY,
                List.of("survivor_cache", "abandoned_camp", "relay_station"),
                item("echoashfallprotocol:clean_water_bottle", 2, "Deliver clean water to the shelter channel"),
                objective(ObjectiveType.REPAIR, List.of("relay", "power_node"), 1, "Restore one route utility node"),
                List.of("echoashfallprotocol:bandage", "echoashfallprotocol:emergency_ration"));
        loadout("ashland_rangers", "Ashland Rangers", "Stormline", ServiceKind.HAZARD,
                List.of("crash_zone_wasteland", "wasteland_outpost", "ash_checkpoint"),
                item("echoashfallprotocol:filter_cartridge_basic", 2, "Deliver spare respirator filters"),
                objective(ObjectiveType.KILL, List.of("ash_wraith", "feral_human", "scavenger"), 3, "Clear threats from an exposed route"),
                List.of("echoashfallprotocol:filter_cartridge_advanced", "echoashfallprotocol:clean_water_bottle"));
        loadout("dustline_freeholds", "Dustline Freeholds", "Freehold", ServiceKind.SALVAGE,
                List.of("ruined_plains", "scavenger_camp", "survivor_cache"),
                item("echoashfallprotocol:scrap_metal", 10, "Deliver trade scrap for camp repairs"),
                objective(ObjectiveType.KILL, List.of("scavenger_bandit", "bandit", "raider"), 4, "Break a bandit pressure line"),
                List.of("echoashfallprotocol:scrap_wire", "echoashfallprotocol:clean_water_bottle"));
        loadout("metro_archivists", "Metro Archivists", "Archive", ServiceKind.ARCHIVE,
                List.of("ruined_cityscape", "data_center_ruin", "subway_station"),
                item("echoashfallprotocol:circuit_board", 2, "Deliver recovered civic electronics"),
                objective(ObjectiveType.POI_DISCOVERY, List.of("data_center_ruin", "subway_station", "ruined_cityscape"), 2,
                        "Index two urban route records"),
                List.of("echoashfallprotocol:schematic_fragment_energy", "echoashfallprotocol:circuit_board"));
        loadout("rustworks_union", "Rustworks Union", "Work Order", ServiceKind.REPAIR,
                List.of("industrial_ruins", "industrial_factory", "train_yard"),
                item("echoashfallprotocol:dense_alloy_chunk", 2, "Deliver dense alloy for machine lanes"),
                objective(ObjectiveType.REPAIR, List.of("relay", "factory", "power_node"), 2, "Repair two industrial route systems"),
                List.of("echoashfallprotocol:machine_casing", "echoashfallprotocol:energy_cell"));
        loadout("sporebound_sanctum", "Sporebound Sanctum", "Sample", ServiceKind.MEDICAL,
                List.of("toxic_swamp", "bio_lab", "sporebound_sanctum"),
                item("echoashfallprotocol:mutated_tissue", 2, "Deliver controlled biological samples"),
                objective(ObjectiveType.KILL, List.of("toxic_slime", "mutated_crawler", "mutant", "zombie"), 4,
                        "Cull unstable bioforms near a route"),
                List.of("echoashfallprotocol:rad_away", "echoashfallprotocol:mutagen_vial"));
        loadout("crashbreak_salvage", "Crashbreak Salvage", "Blackbox", ServiceKind.SALVAGE,
                List.of("crash_zone_wasteland", "drop_pod", "train_yard"),
                item("echoashfallprotocol:scrap_circuit", 3, "Deliver intact wreck circuits"),
                objective(ObjectiveType.POI_DISCOVERY, List.of("crash_zone_wasteland", "drop_pod", "train_yard"), 2,
                        "Survey two crash or wreck routes"),
                List.of("echoashfallprotocol:power_cell", "echoashfallprotocol:scrap_metal"));
        loadout("radwarden_compact", "Radwarden Compact", "Containment", ServiceKind.RADIATION,
                List.of("radiation_zone", "reactor_ruin", "military_vault"),
                item("echoashfallprotocol:rad_away", 2, "Deliver RadAway for containment crews"),
                objective(ObjectiveType.REPAIR, List.of("relay", "beacon", "power_node"), 2, "Restore two warning or power nodes"),
                List.of("echoashfallprotocol:filter_cartridge_advanced", "echoashfallprotocol:uranium_shard"));
        loadout("thawbound_collective", "Thawbound Collective", "Thaw", ServiceKind.COLD,
                List.of("cryogenic_ruins", "cryo_lab", "frozen_outpost"),
                item("echoashfallprotocol:hand_warmer", 2, "Deliver heat supplies for thaw crews"),
                objective(ObjectiveType.POI_DISCOVERY, List.of("cryogenic_ruins", "frozen_cache", "ice_covered_ruin"), 2,
                        "Log two cryogenic route readings"),
                List.of("echoashfallprotocol:thermal_liner", "echoashfallprotocol:energy_cell"));
        loadout("scarbound_conclave", "Scarbound Conclave", "Scar", ServiceKind.NEXUS,
                List.of("nexus_scar", "scar_anchor", "nexus_anomaly"),
                item("echoashfallprotocol:nexus_crystal", 1, "Offer a stable Nexus crystal reading"),
                objective(ObjectiveType.REPAIR, List.of("anchor", "nexus", "relay"), 1, "Stabilize one anomaly route anchor"),
                List.of("echoashfallprotocol:nexus_crystal", "echoashfallprotocol:filter_cartridge_elite"));
    }

    private AshfallFactionContracts() {
    }

    public static List<EchoFactionContract> echoContracts(String path, String fieldTitle, String fieldSummary,
            String fieldObjective, String fieldReward, String route) {
        Loadout loadout = loadout(path);
        return specs(path, fieldTitle, fieldSummary, fieldObjective, fieldReward, route).stream()
                .map(spec -> new EchoFactionContract(
                        spec.contractId(),
                        spec.title(),
                        spec.summary(),
                        spec.requiredReputation(),
                        spec.reputationReward(),
                        spec.objectives().stream().map(Objective::displayText).reduce((a, b) -> a + " | " + b).orElse("Field work"),
                        spec.rewardLine(),
                        route.isBlank() ? loadout.routeName() : route))
                .toList();
    }

    public static Optional<Spec> spec(Identifier contractId) {
        if (contractId == null || !EchoAshfallProtocol.MODID.equals(contractId.getNamespace())) {
            return Optional.empty();
        }
        String path = contractId.getPath();
        for (String factionPath : LOADOUTS.keySet()) {
            if (path.equals(factionPath + "_field_contract")
                    || path.equals(factionPath + "_trusted_contract")
                    || path.equals(factionPath + "_aligned_contract")) {
                return specs(factionPath, "", "", "", "", "").stream()
                        .filter(spec -> spec.contractId().equals(contractId))
                        .findFirst();
            }
        }
        return Optional.empty();
    }

    public static List<String> poiTargets(String path) {
        return loadout(path).poiTargets();
    }

    public static ServiceKind serviceKind(Identifier factionId) {
        return loadout(factionId == null ? "" : factionId.getPath()).serviceKind();
    }

    private static List<Spec> specs(String path, String fieldTitle, String fieldSummary, String fieldObjective,
            String fieldReward, String route) {
        Loadout loadout = loadout(path);
        List<Spec> specs = new ArrayList<>();
        specs.add(new Spec(
                id(path),
                id(path + "_field_contract"),
                fieldTitle.isBlank() ? loadout.routeName() + " Field Proof" : fieldTitle,
                fieldSummary.isBlank() ? "Log one field route for " + loadout.displayName() + "." : fieldSummary,
                0,
                15,
                List.of(objective(ObjectiveType.POI_DISCOVERY, loadout.poiTargets(), 1,
                        fieldObjective.isBlank() ? "Log one matching route POI" : fieldObjective)),
                loadout.rewardItems(),
                8,
                fieldReward.isBlank() ? loadout.routeName() + " field supplies." : fieldReward));
        specs.add(new Spec(
                id(path),
                id(path + "_trusted_contract"),
                loadout.routeName() + " Trusted Supply",
                "Prove dependable logistics before " + loadout.displayName() + " opens higher-risk work.",
                35,
                20,
                List.of(loadout.deliveryObjective()),
                loadout.rewardItems(),
                12,
                loadout.routeName() + " trusted support cache."));
        specs.add(new Spec(
                id(path),
                id(path + "_aligned_contract"),
                loadout.routeName() + " Aligned Operation",
                "Complete a route-changing operation for " + loadout.displayName() + ".",
                75,
                25,
                List.of(loadout.alignedObjective()),
                loadout.rewardItems(),
                18,
                loadout.routeName() + " aligned support cache."));
        return specs;
    }

    private static Loadout loadout(String path) {
        return LOADOUTS.getOrDefault(path, LOADOUTS.get("survivor_network"));
    }

    private static void loadout(String path, String displayName, String routeName, ServiceKind serviceKind,
            List<String> poiTargets, Objective deliveryObjective, Objective alignedObjective, List<String> rewardItems) {
        LOADOUTS.put(path, new Loadout(displayName, routeName, serviceKind, List.copyOf(poiTargets),
                deliveryObjective, alignedObjective, List.copyOf(rewardItems)));
    }

    private static Objective item(String itemId, int count, String text) {
        return objective(ObjectiveType.ITEM_DELIVERY, List.of(itemId), count, text);
    }

    private static Objective objective(ObjectiveType type, List<String> targetIds, int count, String text) {
        return new Objective(type, targetIds.stream().map(AshfallFactionContracts::normalize).toList(),
                Math.max(1, count), text);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, path);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    public enum ObjectiveType {
        POI_DISCOVERY,
        ITEM_DELIVERY,
        KILL,
        REPAIR,
        RAID_DEFENSE
    }

    public enum ServiceKind {
        SUPPLY,
        HAZARD,
        REPAIR,
        MEDICAL,
        COLD,
        RADIATION,
        SALVAGE,
        ARCHIVE,
        NEXUS
    }

    public record Objective(ObjectiveType type, List<String> targetIds, int requiredCount, String displayText) {
        public Objective {
            targetIds = targetIds == null ? List.of() : List.copyOf(targetIds);
            requiredCount = Math.max(1, requiredCount);
            displayText = displayText == null || displayText.isBlank()
                    ? Arrays.toString(targetIds.toArray())
                    : displayText.trim();
        }
    }

    public record Spec(Identifier factionId, Identifier contractId, String title, String summary,
            int requiredReputation, int reputationReward, List<Objective> objectives, List<String> rewardItems,
            int researchReward, String rewardLine) {
        public Spec {
            objectives = objectives == null ? List.of() : List.copyOf(objectives);
            rewardItems = rewardItems == null ? List.of() : List.copyOf(rewardItems);
            rewardLine = rewardLine == null ? "" : rewardLine.trim();
        }
    }

    private record Loadout(String displayName, String routeName, ServiceKind serviceKind, List<String> poiTargets,
            Objective deliveryObjective, Objective alignedObjective, List<String> rewardItems) {
    }
}
