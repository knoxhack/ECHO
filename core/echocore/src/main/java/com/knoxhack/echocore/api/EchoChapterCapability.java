package com.knoxhack.echocore.api;

public record EchoChapterCapability(
        String id,
        String displayName,
        boolean installed,
        boolean available,
        String statusLine) {
    public EchoChapterCapability {
        id = id == null || id.isBlank() ? "unknown" : id.trim().toLowerCase(java.util.Locale.ROOT);
        displayName = displayName == null || displayName.isBlank() ? id : displayName.trim();
        statusLine = statusLine == null ? "" : statusLine.trim();
    }
}
