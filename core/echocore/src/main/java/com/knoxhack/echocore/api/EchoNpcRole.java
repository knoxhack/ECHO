package com.knoxhack.echocore.api;

import java.util.Objects;

/**
 * A reusable NPC role exposed by a faction. Entity implementations remain addon-owned.
 */
public record EchoNpcRole(String id, String displayName, String serviceSummary) {
    public EchoNpcRole {
        id = cleanRequired(id, "id");
        displayName = cleanRequired(displayName, "displayName");
        serviceSummary = clean(serviceSummary);
    }

    private static String cleanRequired(String value, String field) {
        String cleaned = clean(value);
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("NPC role " + field + " cannot be blank");
        }
        return cleaned;
    }

    private static String clean(String value) {
        return Objects.requireNonNullElse(value, "").trim();
    }
}
