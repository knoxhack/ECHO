package com.knoxhack.echocore.api;

public enum WorldRegionType {
    CRASH_ZONE,
    RUINED_CITY,
    TOXIC_SWAMP,
    RADIATION_ZONE,
    CRYOGENIC_RUINS,
    NEXUS_SCAR,
    ORBITAL_DEBRIS_FIELD,
    CONVOY_ROUTE,
    SECURE_OUTPOST,
    ANOMALY_ZONE;

    public String displayName() {
        String[] parts = name().toLowerCase(java.util.Locale.ROOT).split("_+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }
}
