package com.knoxhack.echoworldcore.registry;

import com.knoxhack.echocore.api.WorldHazardDefinition;
import com.knoxhack.echocore.api.WorldRegionDefinition;
import com.knoxhack.echocore.api.WorldRegionType;
import com.knoxhack.echoworldcore.EchoWorldCore;
import com.knoxhack.echoworldcore.service.WorldRegionService;
import java.util.List;
import net.minecraft.resources.Identifier;

public final class WorldCoreBuiltins {
    public static final Identifier ORBITAL_DEBRIS_FIELD =
            Identifier.fromNamespaceAndPath("echoorbitalremnants", "orbital_debris_field");

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
        service.registerRegionDefinition(region(ORBITAL_DEBRIS_FIELD,
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
}
