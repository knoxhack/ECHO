package com.knoxhack.echoworldcore.registry;

import com.knoxhack.echocore.api.WorldHazardDefinition;
import com.knoxhack.echocore.api.WorldRegionDefinition;
import com.knoxhack.echocore.api.WorldRegionType;
import com.knoxhack.echoworldcore.EchoWorldCore;
import com.knoxhack.echoworldcore.service.WorldRegionService;
import java.util.List;
import net.minecraft.resources.Identifier;

public final class WorldCoreBuiltins {
    public static final Identifier CRASH_ZONE = ashfall("crash_zone_wasteland");

    private WorldCoreBuiltins() {
    }

    public static void register(WorldRegionService service) {
        registerHazards(service);
        registerRegions(service);
    }

    private static void registerHazards(WorldRegionService service) {
        service.registerHazardDefinition(new WorldHazardDefinition(id("hazard/salvage_debris"),
                "Salvage Debris", "Sharp wreckage, unstable scraps, and damaged hull fragments.", 25, false));
        service.registerHazardDefinition(new WorldHazardDefinition(id("hazard/toxic_air"),
                "Toxic Air", "Airborne chemical and spore contamination.", 55, false));
        service.registerHazardDefinition(new WorldHazardDefinition(id("hazard/radiation"),
                "Radiation", "Irradiated terrain and unstable fallout pockets.", 70, false));
        service.registerHazardDefinition(new WorldHazardDefinition(id("hazard/cryo_cold"),
                "Cryogenic Cold", "Extreme cold around ruptured cryogenic infrastructure.", 60, false));
        service.registerHazardDefinition(new WorldHazardDefinition(id("hazard/nexus_anomaly"),
                "Nexus Anomaly", "Reality instability and corrupted field pressure.", 85, false));
        service.registerHazardDefinition(new WorldHazardDefinition(id("hazard/orbital_exposure"),
                "Orbital Exposure", "Vacuum, debris, oxygen, and pressure instability.", 75, false));
        service.registerHazardDefinition(new WorldHazardDefinition(id("hazard/convoy_threat"),
                "Convoy Threat", "Route ambush and vehicle attrition pressure.", 45, false));
        service.registerHazardDefinition(new WorldHazardDefinition(id("hazard/secure_zone"),
                "Secure Zone", "Stabilized or faction-held field position.", 0, false));
    }

    private static void registerRegions(WorldRegionService service) {
        service.registerRegionDefinition(region(ashfall("crash_zone_wasteland"), WorldRegionType.CRASH_ZONE,
                "Crash Zone Wasteland",
                "Impact-scattered wreckage fields and Ashfall crash debris.",
                List.of(ashfall("crash_zone_wasteland")),
                List.of(ashfall("common_wasteland_biomes"), ashfall("has_structure/crash_zone_wasteland")),
                List.of(ashfall("crash_zone_wasteland"), ashfall("crash_zone_landmarks"), ashfall("drop_pod")),
                List.of(id("hazard/salvage_debris")), 10));
        service.registerRegionDefinition(region(ashfall("ruined_cityscape"), WorldRegionType.RUINED_CITY,
                "Ruined Cityscape",
                "Collapsed urban grid, data ruins, and buried transit signals.",
                List.of(ashfall("ruined_cityscape")),
                List.of(ashfall("rare_wasteland_biomes"), ashfall("has_structure/ruined_cityscape")),
                List.of(ashfall("ruined_cityscape"), ashfall("ruined_city_landmarks"), ashfall("data_center_ruin"),
                        ashfall("subway_station"), ashfall("military_vault")),
                List.of(id("hazard/salvage_debris")), 20));
        service.registerRegionDefinition(region(ashfall("toxic_swamp"), WorldRegionType.TOXIC_SWAMP,
                "Toxic Swamp",
                "Corroded wetlands, chemical runoff, bio-lab remains, and spore exposure.",
                List.of(ashfall("toxic_swamp")),
                List.of(ashfall("toxic_air_biomes"), ashfall("hazardous_wasteland_biomes"),
                        ashfall("has_structure/toxic_swamp")),
                List.of(ashfall("toxic_swamp"), ashfall("toxic_swamp_landmarks"), ashfall("bio_lab"),
                        ashfall("sporebound_sanctum")),
                List.of(id("hazard/toxic_air")), 30));
        service.registerRegionDefinition(region(ashfall("radiation_zone"), WorldRegionType.RADIATION_ZONE,
                "Radiation Zone",
                "Fallout-heavy terrain around reactor ruins and exposed radiation pockets.",
                List.of(ashfall("radiation_zone")),
                List.of(ashfall("radiation_biomes"), ashfall("hazardous_wasteland_biomes"),
                        ashfall("has_structure/radiation_zone")),
                List.of(ashfall("radiation_zone"), ashfall("radiation_zone_landmarks"), ashfall("reactor_ruin")),
                List.of(id("hazard/radiation")), 40));
        service.registerRegionDefinition(region(ashfall("cryogenic_ruins"), WorldRegionType.CRYOGENIC_RUINS,
                "Cryogenic Ruins",
                "Frozen laboratories, shattered cryo tanks, and cold-storage wreckage.",
                List.of(ashfall("cryogenic_ruins")),
                List.of(ashfall("cryogenic_biomes"), ashfall("has_structure/cryogenic_ruins")),
                List.of(ashfall("cryogenic_ruins"), ashfall("cryogenic_ruins_landmarks")),
                List.of(id("hazard/cryo_cold")), 50));
        service.registerRegionDefinition(region(ashfall("nexus_scar"), WorldRegionType.NEXUS_SCAR,
                "Nexus Scar",
                "Corrupted rift terrain where Ashfall and Nexus field logic overlap.",
                List.of(ashfall("nexus_scar")),
                List.of(ashfall("nexus_anomaly_biomes"), ashfall("has_structure/nexus_scar")),
                List.of(ashfall("nexus_scar"), ashfall("nexus_scar_landmarks")),
                List.of(id("hazard/nexus_anomaly"), id("hazard/radiation")), 60));
        service.registerRegionDefinition(region(Identifier.fromNamespaceAndPath("echoorbitalremnants", "orbital_debris_field"),
                WorldRegionType.ORBITAL_DEBRIS_FIELD,
                "Orbital Debris Field",
                "Station ECHO wreckage, docking debris, and broken orbital relay fields.",
                List.of(), List.of(),
                List.of(Identifier.fromNamespaceAndPath("echoorbitalremnants", "low_earth_orbit"),
                        Identifier.fromNamespaceAndPath("echoorbitalremnants", "ground_recovery_site")),
                List.of(id("hazard/orbital_exposure"), id("hazard/salvage_debris")), 70));
        service.registerRegionDefinition(region(Identifier.fromNamespaceAndPath("echoconvoyprotocol", "convoy_route"),
                WorldRegionType.CONVOY_ROUTE,
                "Convoy Route",
                "Active vehicle route, roadside signal, checkpoint, and destination corridor.",
                List.of(), List.of(),
                List.of(Identifier.fromNamespaceAndPath("echoconvoyprotocol", "convoy_route"),
                        Identifier.fromNamespaceAndPath("echoconvoyprotocol", "roadside_signal")),
                List.of(id("hazard/convoy_threat")), 80));
        service.registerRegionDefinition(region(ashfall("radwarden_outpost"), WorldRegionType.SECURE_OUTPOST,
                "Secure Outpost",
                "Faction or recovery foothold with stabilized field infrastructure.",
                List.of(), List.of(),
                List.of(ashfall("radwarden_outpost"), ashfall("crashbreak_salvage_yard"), ashfall("scavenger_camp")),
                List.of(id("hazard/secure_zone")), 90));
        service.registerRegionDefinition(region(Identifier.fromNamespaceAndPath("echonexusprotocol", "anomaly_zone"),
                WorldRegionType.ANOMALY_ZONE,
                "Anomaly Zone",
                "Nexus Protocol anomaly fields, scar relays, and unstable memory-space positions.",
                List.of(), List.of(),
                List.of(Identifier.fromNamespaceAndPath("echonexusprotocol", "nexus_core_chamber"),
                        Identifier.fromNamespaceAndPath("echonexusprotocol", "abandoned_nexus_field_station")),
                List.of(id("hazard/nexus_anomaly")), 100));
    }

    private static WorldRegionDefinition region(Identifier id, WorldRegionType type, String name, String summary,
            List<Identifier> biomes, List<Identifier> biomeTags, List<Identifier> structures,
            List<Identifier> hazards, int sortOrder) {
        return new WorldRegionDefinition(id, type, name, summary, biomes, biomeTags, structures, hazards,
                id, 96, Identifier.fromNamespaceAndPath(EchoWorldCore.MODID, "region/" + id.getPath()),
                Identifier.fromNamespaceAndPath(EchoWorldCore.MODID, "ambience/" + id.getPath()), sortOrder);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoWorldCore.MODID, path);
    }

    private static Identifier ashfall(String path) {
        return Identifier.fromNamespaceAndPath("echoashfallprotocol", path);
    }
}
