package com.knoxhack.echocore.api;

import net.minecraft.resources.Identifier;

public record EchoRouteRecord(
        Identifier id,
        String chapterId,
        String title,
        String category,
        String dimensionHint,
        String status,
        String summary,
        boolean complete) {
    public EchoRouteRecord {
        if (id == null) {
            throw new IllegalArgumentException("ECHO route record id is required.");
        }
        chapterId = clean(chapterId, id.getNamespace());
        title = clean(title, id.getPath());
        category = clean(category, "Route");
        dimensionHint = clean(dimensionHint, "Any dimension");
        status = clean(status, complete ? "COMPLETE" : "ACTIVE");
        summary = clean(summary, "");
    }

    private static String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
