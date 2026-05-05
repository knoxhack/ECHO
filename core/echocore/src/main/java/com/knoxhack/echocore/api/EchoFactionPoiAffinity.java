package com.knoxhack.echocore.api;

import java.util.Objects;

/**
 * Lightweight hint connecting a faction to scanner/POI profile IDs.
 */
public record EchoFactionPoiAffinity(String profileId, String role, int residentWeight, boolean hub) {
    public EchoFactionPoiAffinity {
        profileId = cleanRequired(profileId, "profileId");
        role = clean(role);
        residentWeight = Math.max(0, residentWeight);
    }

    private static String cleanRequired(String value, String field) {
        String cleaned = clean(value);
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("Faction POI affinity " + field + " cannot be blank");
        }
        return cleaned;
    }

    private static String clean(String value) {
        return Objects.requireNonNullElse(value, "").trim();
    }
}
