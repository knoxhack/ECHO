package com.knoxhack.echocore.api;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Canonical cross-addon milestone ids and legacy aliases.
 */
public final class EchoHandoffs {
    public static final String STATIONFALL_BLACKBOX_RECOVERED =
            "echostationfall:stationfall_blackbox_recovered";
    public static final String NEXUS_PROTOCOL_COMPLETE =
            "echonexusprotocol:nexus_protocol_complete";

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("stationfall.blackbox_retrieved", STATIONFALL_BLACKBOX_RECOVERED),
            Map.entry("stationfall:blackbox_recovered", STATIONFALL_BLACKBOX_RECOVERED),
            Map.entry("nexus:path:restore", NEXUS_PROTOCOL_COMPLETE),
            Map.entry("nexus:path:control", NEXUS_PROTOCOL_COMPLETE),
            Map.entry("nexus:path:destroy", NEXUS_PROTOCOL_COMPLETE),
            Map.entry("nexus:path:merge", NEXUS_PROTOCOL_COMPLETE));

    private EchoHandoffs() {
    }

    public static String canonicalMilestone(String milestoneId) {
        String clean = clean(milestoneId);
        return ALIASES.getOrDefault(clean, clean);
    }

    public static Set<String> aliasesFor(String milestoneId) {
        String canonical = canonicalMilestone(milestoneId);
        if (canonical.isBlank()) {
            return Set.of();
        }
        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        aliases.add(canonical);
        for (Map.Entry<String, String> entry : ALIASES.entrySet()) {
            if (entry.getValue().equals(canonical)) {
                aliases.add(entry.getKey());
            }
        }
        return Set.copyOf(aliases);
    }

    public static boolean matches(String storedMilestoneId, String requestedMilestoneId) {
        String stored = canonicalMilestone(storedMilestoneId);
        String requested = canonicalMilestone(requestedMilestoneId);
        return !stored.isBlank() && stored.equals(requested);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
