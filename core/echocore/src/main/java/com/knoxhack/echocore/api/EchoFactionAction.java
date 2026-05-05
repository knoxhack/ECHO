package com.knoxhack.echocore.api;

import java.util.Objects;

import net.minecraft.resources.Identifier;

/**
 * A faction-provided action or service that a terminal/NPC implementation can expose.
 */
public record EchoFactionAction(
        Identifier id,
        String label,
        String description,
        int requiredReputation,
        boolean service) {

    public EchoFactionAction {
        Objects.requireNonNull(id, "id");
        label = cleanRequired(label, "label");
        description = clean(description);
        requiredReputation = clampReputation(requiredReputation);
    }

    private static String cleanRequired(String value, String field) {
        String cleaned = clean(value);
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("Faction action " + field + " cannot be blank");
        }
        return cleaned;
    }

    private static String clean(String value) {
        return Objects.requireNonNullElse(value, "").trim();
    }

    private static int clampReputation(int reputation) {
        return Math.max(-100, Math.min(100, reputation));
    }
}
