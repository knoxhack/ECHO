package com.knoxhack.echocore.api.mission;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.Identifier;

/**
 * Shared naming helpers for server-side mission objective hooks.
 */
public final class MissionHookTargets {
    private static final String UNKNOWN_SOURCE = "echocore";

    private MissionHookTargets() {
    }

    public static Identifier objectiveTarget(String source, Identifier missionId, int objectiveIndex) {
        return objectiveTarget(source, missionId, "requirement_" + Math.max(0, objectiveIndex));
    }

    public static Identifier objectiveTarget(String source, Identifier missionId, String objectiveKey) {
        String namespace = sanitizeNamespace(source);
        String missionPath = missionId == null ? "unknown" : sanitizePath(missionId.getPath());
        String key = sanitizePath(objectiveKey == null || objectiveKey.isBlank() ? "objective" : objectiveKey);
        return Identifier.fromNamespaceAndPath(namespace, "mission/" + missionPath + "/" + key);
    }

    public static Map<String, String> context(String source, Identifier missionId, String detailKey, String detailValue) {
        LinkedHashMap<String, String> context = new LinkedHashMap<>();
        context.put("source", sanitizeNamespace(source));
        if (missionId != null) {
            context.put("legacy_mission", missionId.toString());
        }
        if (detailKey != null && !detailKey.isBlank() && detailValue != null && !detailValue.isBlank()) {
            context.put(sanitizeContextKey(detailKey), detailValue);
        }
        return Map.copyOf(context);
    }

    private static String sanitizeNamespace(String value) {
        String source = value == null || value.isBlank() ? UNKNOWN_SOURCE : value.toLowerCase(Locale.ROOT);
        return source.replaceAll("[^a-z0-9_.-]", "_");
    }

    private static String sanitizePath(String value) {
        String path = value == null || value.isBlank() ? "unknown" : value.toLowerCase(Locale.ROOT).replace('\\', '/');
        return path.replaceAll("[^a-z0-9/._-]", "_");
    }

    private static String sanitizeContextKey(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
    }
}
