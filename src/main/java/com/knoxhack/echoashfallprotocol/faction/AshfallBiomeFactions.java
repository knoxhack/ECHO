package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoDialogueTree;
import com.knoxhack.echocore.api.EchoFactionAction;
import com.knoxhack.echocore.api.EchoFactionContract;
import com.knoxhack.echocore.api.EchoFactionDefinition;
import com.knoxhack.echocore.api.EchoFactionPoiAffinity;
import com.knoxhack.echocore.api.EchoNpcRole;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import java.util.List;
import net.minecraft.resources.Identifier;

/**
 * Ashfall-owned faction definitions exposed through Echo Core.
 */
public final class AshfallBiomeFactions {
    public static final Identifier SURVIVOR_NETWORK = id("survivor_network");
    public static final Identifier ASHLAND_RANGERS = id("ashland_rangers");
    public static final Identifier DUSTLINE_FREEHOLDS = id("dustline_freeholds");
    public static final Identifier METRO_ARCHIVISTS = id("metro_archivists");
    public static final Identifier RUSTWORKS_UNION = id("rustworks_union");
    public static final Identifier SPOREBOUND_SANCTUM = id("sporebound_sanctum");
    public static final Identifier CRASHBREAK_SALVAGE = id("crashbreak_salvage");
    public static final Identifier RADWARDEN_COMPACT = id("radwarden_compact");
    public static final Identifier THAWBOUND_COLLECTIVE = id("thawbound_collective");
    public static final Identifier SCARBOUND_CONCLAVE = id("scarbound_conclave");

    private AshfallBiomeFactions() {
    }

    public static void register() {
        registerFaction("survivor_network", "Survivor Network", "Network", "Shelter Route",
                "Shelter operators keeping starter pods, cache relays, and distress channels alive one night at a time.",
                "Supply pressure and roaming threats", "Water, bandages, and a charged scanner",
                "Bedroll caches, starter repairs, and quiet route warnings", 0x6ED6A5, true,
                roles("Quartermaster", "Medic", "Route Caller"),
                "Shelter Handshake", "Stabilize a shelter route before the next distress channel goes quiet.",
                "Deliver supplies or scan a nearby cache beacon.", "Shelter service calls and cache priority.",
                List.of("starter_shelter", "survivor_cache", "abandoned_camp"));

        registerFaction("ashland_rangers", "Ashland Rangers", "Rangers", "Ash Wastes",
                "Mask-wearing patrols guiding caravans through ashfall basins where the wind erases bad decisions slowly.",
                "Toxic air, ash storms, exposed approach paths", "Respirator filters and spare water",
                "Patrol escorts, storm warnings, and ridge-line field maps", 0xBFC469, false,
                roles("Scout", "Storm Reader", "Trail Guard"),
                "Stormline Survey", "Map an ash route before the next front closes visibility and leaves only guesswork.",
                "Scan an ashland POI or clear a route marker.", "Updated storm approach markers.",
                List.of("crash_zone_wasteland", "wasteland_outpost", "ash_checkpoint"));

        registerFaction("dustline_freeholds", "Dustline Freeholds", "Freeholds", "Ruined Plains",
                "Camp federations holding old farms, watchfires, and open-road trade against the horizon.",
                "Bandit pressure and low-cover plains", "Ranged weapon, food, and bedroll",
                "Camp barter, work boards, and road rumors paid for in water", 0xD0A057, false,
                roles("Camp Boss", "Fence", "Lookout"),
                "Freehold Marker", "Secure a ruined-plains trail and return with camp intel before the fires move.",
                "Visit a camp or watchtower POI and confirm the route.", "Freehold barter credit.",
                List.of("ruined_plains_camp", "watchtower", "roadside_cache"));

        registerFaction("metro_archivists", "Metro Archivists", "Archivists", "Collapsed City",
                "Archivists catalog vertical ruins, buried terminals, and civic records that survived their cities.",
                "Falling debris, close interiors, signal blind spots", "Blocks, lights, and a backup weapon",
                "Archive leads, locked-door hints, and data recovery with names still attached", 0x74B7D8, true,
                roles("Indexer", "Elevator Tech", "Field Scribe"),
                "Vertical Index", "Recover a civic trace from a collapsed city landmark without letting the tower choose the exit.",
                "Scan or search an urban ruin and return the index key.", "Archive context and terminal hints.",
                List.of("collapsed_city_highrise", "metro_station", "data_archive"));

        registerFaction("rustworks_union", "Rustworks Union", "Union", "Industrial Belt",
                "Pipefitters, machinists, and gantry crews keeping refinery yards useful after the quotas outlived the workers.",
                "Crushing machinery, sparks, and enclosed catwalks", "Repair kit, fire protection, and blocks",
                "Workbench access, scrap orders, and machine repairs that still smell hot", 0xC47D4C, false,
                roles("Foreman", "Pipe Tech", "Scrap Broker"),
                "Gantry Work Order", "Reopen a machine lane and tag hardware before the yard grinds it into scrap.",
                "Clear or scan an industrial POI.", "Union scrap ledger credit.",
                List.of("industrial_ruins", "pipe_gantry", "refinery_yard"));

        registerFaction("sporebound_sanctum", "Sporebound Sanctum", "Sanctum", "Toxic Groves",
                "Adaptation circles studying living contamination instead of pretending fire solves everything.",
                "Spores, poison clouds, and infected floors", "Antitoxin, mask, and ranged control",
                "Biomass analysis, antidote rumors, and living-route warnings", 0x68B86A, false,
                roles("Spore Tender", "Antitoxin Brewer", "Myco Scout"),
                "Spore Sample", "Gather a clean read from a toxic growth without letting the route learn your lungs.",
                "Scan a toxic POI or recover a sample marker.", "Antitoxin service priority.",
                List.of("toxic_grove", "spore_lab", "mutant_den"));

        registerFaction("crashbreak_salvage", "Crashbreak Salvage", "Crashbreak", "Crash Fields",
                "Wreck crews carving aircraft hulls and pod debris into usable route infrastructure.",
                "Sharp wreckage, fuel pockets, unstable interiors", "Fire resistance, axe, and spare tools",
                "Wreck maps, hull salvage, and emergency beacon repair", 0xE5B85C, true,
                roles("Hull Cutter", "Beacon Ratchet", "Loadmaster"),
                "Blackbox Pull", "Recover a signal core from crash wreckage before raiders strip out the last witness.",
                "Scan or search a crash-zone POI.", "Priority salvage callouts.",
                List.of("crash_wreckage", "drop_pod_field", "airframe_shelter"));

        registerFaction("radwarden_compact", "Radwarden Compact", "Radwardens", "Hot Zone",
                "Containment crews marking hot soil, sealing breached labs, and keeping warning beacons lit for people who ignore them.",
                "Radiation, sealed rooms, and contaminated water", "Rad meds, filters, and lead-lined gear",
                "Containment codes, warning markers, and decon routing", 0xD8D65F, false,
                roles("Dosimeter", "Containment Guard", "Decon Tech"),
                "Warning Beacon", "Restore a containment marker before the hot zone forgets where it ends.",
                "Scan a radiation POI or service a warning station.", "Decon and warning route support.",
                List.of("radiation_bunker", "containment_site", "warning_tower"));

        registerFaction("thawbound_collective", "Thawbound Collective", "Thawbound", "Cryo Flats",
                "Cold-tech survivors keeping cryogenic chambers and frost-buried machinery operational because thawing wrong is still dying.",
                "Cold exposure, brittle floors, and frozen machinery", "Warm gear, fuel, and heat sources",
                "Cryo battery service, thaw routes, thermal shelter leads", 0x8CC7E8, false,
                roles("Cryo Keeper", "Thermal Tech", "Frost Runner"),
                "Thaw Relay", "Restart a frozen relay and log the safe heat radius before the cold edits the map.",
                "Scan a cryogenic POI or relight a heat station.", "Thermal service priority.",
                List.of("cryo_lab", "frozen_outpost", "thermal_station"));

        registerFaction("scarbound_conclave", "Scarbound Conclave", "Conclave", "Nexus Scar",
                "Anomaly witnesses reading scar tissue in terrain, memory loops, and routes that should not fit inside the world.",
                "Reality shear, hostile echoes, and unstable navigation", "Scanner charge, anchors, and escape blocks",
                "Anomaly warnings, scar-route readings, and late-game guidance with the names sanded off", 0xB889F5, true,
                roles("Scar Reader", "Anchor Tender", "Oath Speaker"),
                "Scar Witness", "Confirm a Nexus scar without letting the route back become theoretical.",
                "Scan an anomaly POI or stabilize a scar anchor.", "Anomaly-route interpretation.",
                List.of("nexus_anomaly", "scar_anchor", "anomaly_shrine"));
    }

    private static void registerFaction(String path, String displayName, String shortName, String route,
            String summary, String hazard, String prepHint, String services, int accentColor, boolean landmark,
            List<EchoNpcRole> roles, String contractTitle, String contractSummary, String objective,
            String reward, List<String> poiProfiles) {
        Identifier factionId = id(path);
        EchoCoreServices.registerFaction(new EchoFactionDefinition(
                factionId,
                displayName,
                shortName,
                route,
                summary,
                hazard,
                prepHint,
                services,
                accentColor,
                landmark,
                roles,
                List.of(
                        new EchoFactionAction(id(path + "_talk"), "Open Dialogue", "Ask for local route context.", 0, false),
                        new EchoFactionAction(id(path + "_service"), "Request Service", services, 10, true),
                        new EchoFactionAction(id(path + "_contract"), "Review Contract", "Check the current field proof and route reward.", 0, false)),
                // Contract ids still follow path + "_field_contract", plus trusted/aligned tiers.
                AshfallFactionContracts.echoContracts(path, contractTitle, contractSummary, objective, reward, route),
                AshfallFactionContracts.poiTargets(path).stream()
                        .map(profile -> new EchoFactionPoiAffinity(profile, route, landmark ? 3 : 2, landmark))
                        .toList(),
                new EchoDialogueTree(
                        "Signal recognized. " + displayName + " keeps this route marked.",
                        List.of("Standing", "Services", "Route Risks", "Contracts"),
                        "Keep the route marked and the exit closer than pride.")));
    }

    private static List<EchoNpcRole> roles(String first, String second, String third) {
        return List.of(
                new EchoNpcRole(key(first), first, first + " route context and contact memory."),
                new EchoNpcRole(key(second), second, second + " service access and local hazard advice."),
                new EchoNpcRole(key(third), third, third + " contract proof and POI leads."));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, path);
    }

    private static String key(String label) {
        return label.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("_+$", "");
    }
}
