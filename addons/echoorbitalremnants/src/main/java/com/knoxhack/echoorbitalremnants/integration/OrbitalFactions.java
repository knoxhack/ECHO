package com.knoxhack.echoorbitalremnants.integration;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoDialogueTree;
import com.knoxhack.echocore.api.EchoFactionAction;
import com.knoxhack.echocore.api.EchoFactionContract;
import com.knoxhack.echocore.api.EchoFactionDefinition;
import com.knoxhack.echocore.api.EchoFactionPoiAffinity;
import com.knoxhack.echocore.api.EchoNpcRole;
import com.knoxhack.echoorbitalremnants.EchoOrbitalRemnants;
import com.knoxhack.echoorbitalremnants.progression.EchoTerminalProgress;
import com.knoxhack.echoorbitalremnants.progression.FactionStanding;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * Orbital Remnants faction definitions and standing mirror for Echo Core.
 */
public final class OrbitalFactions {
    public static final Identifier ORBITAL_REMNANTS = id("orbital_remnants");
    public static final Identifier VOID_SALVAGERS = id("void_salvagers");
    public static final Identifier NEXUS_CHOIR = id("nexus_choir");

    private OrbitalFactions() {
    }

    public static void register() {
        registerFaction("orbital_remnants", "Orbital Remnants", "Remnants", "Station ECHO",
                "Station crews keeping the old relay chain alive because dead orbit still has working locks.",
                "Vacuum exposure, unstable pressure loops, debris-field navigation",
                "Sealed suit, oxygen reserves, relay parts",
                "Station relay contracts, pressure telemetry, disciplined repair support",
                0x72A7FF,
                "Relay Officer", "Pressure Tech", "Archivist",
                "Orbital Remnant Relay Survey",
                "Restore relay confidence by logging Station ECHO infrastructure before quarantine logic reclaims it.",
                "Scan a Low Orbit Signal Relay or carry Orbit Survey Data.",
                "Station relay crew standing and pressure-support priority.",
                List.of("low_orbit_relay", "station_echo_debris", "orbital_signal"));

        registerFaction("void_salvagers", "Void Salvagers", "Salvagers", "Mars Transfer",
                "Independent wreck crews trading in alloys, vacuum circuits, and salvage no manifest wants remembered.",
                "Zero-pressure salvage, sharp wreckage, disputed manifests",
                "Vacuum tool kit, alloy, navigation chip",
                "Salvage manifests, barter terms, route-world scrap calls",
                0xE5B85C,
                "Manifest Broker", "Hull Cutter", "Tow Lead",
                "Void Salvager Manifest",
                "Prove the manifest is worth a berth by delivering orbital salvage data before somebody else edits the wreck.",
                "Scan orbital salvage or turn in Orbital Alloy and Vacuum Circuit.",
                "Manifest trust and salvage-market access.",
                List.of("lunar_extractor", "mars_habitat", "orbital_wreck"));

        registerFaction("nexus_choir", "Nexus Choir", "Choir", "Anomaly Belt",
                "Signal interpreters listening to the broken quarantine and stabilizing anchors that answer in borrowed voices.",
                "Nexus shear, anchor instability, hostile echoes",
                "Nexus stabilizer shards, scanner charge, emergency return point",
                "Anchor readings, anomaly warnings, late-route interpretation",
                0xB889F5,
                "Anchor Cantor", "Signal Witness", "Quarantine Scribe",
                "Nexus Choir Anchor Reading",
                "Read a post-ECHO-0 anchor and carry back a stable interpretation without becoming part of the chorus.",
                "After ECHO-0, scan a Nexus Anchor/Growth or spend a Nexus Stabilizer Shard.",
                "Choir interpretation and anchor-stability support.",
                List.of("nexus_anchor", "anomaly_growth", "echo_zero_quarantine"));
    }

    public static void sync(Player player, EchoTerminalProgress progress) {
        if (player == null || progress == null) {
            return;
        }
        register();
        mirror(player, ORBITAL_REMNANTS, progress.orbitalRemnantStanding());
        mirror(player, VOID_SALVAGERS, progress.voidSalvagerStanding());
        mirror(player, NEXUS_CHOIR, progress.nexusChoirStanding());
    }

    private static void registerFaction(String path, String displayName, String shortName, String route,
            String summary, String hazard, String prepHint, String services, int accentColor,
            String roleA, String roleB, String roleC, String contractTitle, String contractSummary,
            String objective, String reward, List<String> poiProfiles) {
        EchoCoreServices.registerFaction(new EchoFactionDefinition(
                id(path),
                displayName,
                shortName,
                route,
                summary,
                hazard,
                prepHint,
                services,
                accentColor,
                true,
                roles(roleA, roleB, roleC),
                List.of(
                        new EchoFactionAction(id(path + "_relay"), "Open Relay", "Ask for route context and current pressure.", 0, false),
                        new EchoFactionAction(id(path + "_service"), "Request Service", services, 10, true),
                        new EchoFactionAction(id(path + "_contract"), "Review Contract", "Check the proof this faction will accept.", 0, false)),
                List.of(new EchoFactionContract(
                        id(switch (path) {
                            case "orbital_remnants" -> "orbital_remnant_relay";
                            case "void_salvagers" -> "void_salvager_manifest";
                            case "nexus_choir" -> "nexus_choir_anchor";
                            default -> path + "_contract";
                        }),
                        contractTitle,
                        contractSummary,
                        0,
                        25,
                        objective,
                        reward,
                        route)),
                poiProfiles.stream()
                        .map(profile -> new EchoFactionPoiAffinity(profile, route, 3, true))
                        .toList(),
                new EchoDialogueTree(
                        "Relay open. " + displayName + " is listening through the static.",
                        List.of("Standing", "Contracts", "Route Hazards", "Services"),
                        "Keep the signal clean and the suit sealed.")));
    }

    private static List<EchoNpcRole> roles(String first, String second, String third) {
        return List.of(
                new EchoNpcRole(key(first), first, first + " route context and relay standing."),
                new EchoNpcRole(key(second), second, second + " service access and hazard advice."),
                new EchoNpcRole(key(third), third, third + " contract proof and POI leads."));
    }

    private static void mirror(Player player, Identifier factionId, FactionStanding standing) {
        switch (standing == null ? FactionStanding.UNKNOWN : standing) {
            case HOSTILE -> EchoCoreServices.setFactionReputation(player, factionId, -75);
            case CONTACTED -> {
                EchoCoreServices.markFactionContacted(player, factionId);
                EchoCoreServices.setFactionReputation(player, factionId, 10);
            }
            case TRUSTED -> EchoCoreServices.setFactionReputation(player, factionId, 55);
            case ALIGNED -> EchoCoreServices.setFactionReputation(player, factionId, 100);
            case UNKNOWN -> {
                if (EchoCoreServices.factionProfile(player, factionId).isEmpty()) {
                    EchoCoreServices.setFactionReputation(player, factionId, 0);
                }
            }
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoOrbitalRemnants.MODID, path);
    }

    private static String key(String label) {
        return label.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("_+$", "");
    }
}
