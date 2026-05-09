package com.knoxhack.echocore.api;

import java.util.Objects;
import net.minecraft.resources.Identifier;

public record EchoDiscoveryEntry(
        Identifier id,
        Identifier chapterId,
        EchoDiscoveryCategory category,
        String revealedTitle,
        String lockedHintTitle,
        String hintText,
        String revealedSummary,
        Identifier iconArt,
        Identifier heroArt,
        int accentColor,
        Identifier relatedMissionId,
        int sortOrder) {
    public EchoDiscoveryEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(chapterId, "chapterId");
        category = category == null ? EchoDiscoveryCategory.STRUCTURE : category;
        revealedTitle = clean(revealedTitle, readable(id.getPath()));
        lockedHintTitle = clean(lockedHintTitle, "Unknown Signal");
        hintText = clean(hintText, "Discover this signal through its owning chapter.");
        revealedSummary = clean(revealedSummary, hintText);
        accentColor = accentColor == 0 ? 0xFF66E8FF : accentColor;
    }

    private static String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String readable(String value) {
        String[] parts = clean(value, "signal").split("_+");
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
        return builder.isEmpty() ? "Signal" : builder.toString();
    }
}
