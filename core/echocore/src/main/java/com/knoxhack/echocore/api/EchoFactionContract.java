package com.knoxhack.echocore.api;

import java.util.Objects;

import net.minecraft.resources.Identifier;

/**
 * Data-only faction contract description. Addons own concrete objectives and rewards.
 */
public record EchoFactionContract(
        Identifier id,
        String title,
        String summary,
        int requiredReputation,
        int reputationReward,
        String objective,
        String reward,
        String route) {

    public EchoFactionContract {
        Objects.requireNonNull(id, "id");
        title = cleanRequired(title, "title");
        summary = clean(summary);
        requiredReputation = clampReputation(requiredReputation);
        reputationReward = clampReputation(reputationReward);
        objective = clean(objective);
        reward = clean(reward);
        route = clean(route);
    }

    private static String cleanRequired(String value, String field) {
        String cleaned = clean(value);
        if (cleaned.isBlank()) {
            throw new IllegalArgumentException("Faction contract " + field + " cannot be blank");
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
