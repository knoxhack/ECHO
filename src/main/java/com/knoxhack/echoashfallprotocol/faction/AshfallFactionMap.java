package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoFactionDefinition;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import java.util.List;
import java.util.Locale;
import net.minecraft.resources.Identifier;

/**
 * Identifier-only faction lookup for Ashfall runtime systems.
 */
public final class AshfallFactionMap {
    private static final List<Identifier> ALL = List.of(
            AshfallBiomeFactions.SURVIVOR_NETWORK,
            AshfallBiomeFactions.ASHLAND_RANGERS,
            AshfallBiomeFactions.DUSTLINE_FREEHOLDS,
            AshfallBiomeFactions.METRO_ARCHIVISTS,
            AshfallBiomeFactions.RUSTWORKS_UNION,
            AshfallBiomeFactions.SPOREBOUND_SANCTUM,
            AshfallBiomeFactions.CRASHBREAK_SALVAGE,
            AshfallBiomeFactions.RADWARDEN_COMPACT,
            AshfallBiomeFactions.THAWBOUND_COLLECTIVE,
            AshfallBiomeFactions.SCARBOUND_CONCLAVE);

    private AshfallFactionMap() {
    }

    public static List<Identifier> all() {
        return ALL;
    }

    public static boolean isAshfall(Identifier factionId) {
        return factionId != null && EchoAshfallProtocol.MODID.equals(factionId.getNamespace())
                && ALL.contains(factionId);
    }

    public static Identifier resolveFactionId(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return AshfallBiomeFactions.SURVIVOR_NETWORK;
        }

        if (normalized.contains(":")) {
            try {
                Identifier parsed = Identifier.parse(normalized);
                return isAshfall(parsed) ? parsed : AshfallBiomeFactions.SURVIVOR_NETWORK;
            } catch (RuntimeException ignored) {
                return AshfallBiomeFactions.SURVIVOR_NETWORK;
            }
        }

        for (Identifier factionId : ALL) {
            if (factionId.getPath().equals(normalized)) {
                return factionId;
            }
        }
        return AshfallBiomeFactions.SURVIVOR_NETWORK;
    }

    public static Identifier forPoi(String poiId) {
        String value = normalize(poiId);
        if (value.contains("nexus") || value.contains("scar") || value.contains("anomaly")) {
            return AshfallBiomeFactions.SCARBOUND_CONCLAVE;
        }
        if (value.contains("cryo") || value.contains("frozen") || value.contains("thermal")) {
            return AshfallBiomeFactions.THAWBOUND_COLLECTIVE;
        }
        if (value.contains("radiation") || value.contains("reactor") || value.contains("military")
                || value.contains("containment") || value.contains("warning")) {
            return AshfallBiomeFactions.RADWARDEN_COMPACT;
        }
        if (value.contains("crash") || value.contains("drop_pod") || value.contains("wreck")
                || value.contains("airframe")) {
            return AshfallBiomeFactions.CRASHBREAK_SALVAGE;
        }
        if (value.contains("toxic") || value.contains("spore") || value.contains("mutant")
                || value.contains("bio") || value.contains("grove")) {
            return AshfallBiomeFactions.SPOREBOUND_SANCTUM;
        }
        if (value.contains("industrial") || value.contains("factory") || value.contains("train")
                || value.contains("refinery") || value.contains("pipe") || value.contains("gantry")) {
            return AshfallBiomeFactions.RUSTWORKS_UNION;
        }
        if (value.contains("city") || value.contains("metro") || value.contains("subway")
                || value.contains("archive") || value.contains("data_center")) {
            return AshfallBiomeFactions.METRO_ARCHIVISTS;
        }
        if (value.contains("plains") || value.contains("freehold") || value.contains("watchtower")
                || value.contains("roadside") || value.contains("camp")) {
            return AshfallBiomeFactions.DUSTLINE_FREEHOLDS;
        }
        if (value.contains("ash") || value.contains("wasteland") || value.contains("checkpoint")) {
            return AshfallBiomeFactions.ASHLAND_RANGERS;
        }
        return AshfallBiomeFactions.SURVIVOR_NETWORK;
    }

    public static Identifier forEntity(String entityId) {
        String value = normalize(entityId);
        if (value.contains("nexus") || value.contains("scar")) {
            return AshfallBiomeFactions.SCARBOUND_CONCLAVE;
        }
        if (value.contains("cryo") || value.contains("frozen")) {
            return AshfallBiomeFactions.THAWBOUND_COLLECTIVE;
        }
        if (value.contains("radiation") || value.contains("rad_zombie") || value.contains("behemoth")) {
            return AshfallBiomeFactions.RADWARDEN_COMPACT;
        }
        if (value.contains("crash") || value.contains("scavenger") || value.contains("bandit")) {
            return AshfallBiomeFactions.CRASHBREAK_SALVAGE;
        }
        if (value.contains("toxic") || value.contains("spore") || value.contains("feral")
                || value.contains("crawler") || value.contains("ghoul")) {
            return AshfallBiomeFactions.SPOREBOUND_SANCTUM;
        }
        if (value.contains("rust") || value.contains("steam") || value.contains("industrial")) {
            return AshfallBiomeFactions.RUSTWORKS_UNION;
        }
        if (value.contains("city") || value.contains("metro")) {
            return AshfallBiomeFactions.METRO_ARCHIVISTS;
        }
        if (value.contains("plains") || value.contains("warlord") || value.contains("wild_dog")) {
            return AshfallBiomeFactions.DUSTLINE_FREEHOLDS;
        }
        if (value.contains("ash") || value.contains("wasteland")) {
            return AshfallBiomeFactions.ASHLAND_RANGERS;
        }
        return AshfallBiomeFactions.SURVIVOR_NETWORK;
    }

    public static String displayName(Identifier factionId) {
        return EchoCoreServices.factionDefinition(factionId)
                .map(EchoFactionDefinition::displayName)
                .orElseGet(() -> readable(factionId));
    }

    public static String shortName(Identifier factionId) {
        return EchoCoreServices.factionDefinition(factionId)
                .map(EchoFactionDefinition::shortName)
                .orElseGet(() -> readable(factionId));
    }

    public static String readable(Identifier factionId) {
        if (factionId == null) {
            return "Ashfall";
        }
        String path = factionId.getPath();
        String[] words = path.split("_+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.isEmpty() ? factionId.toString() : builder.toString();
    }

    public static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
