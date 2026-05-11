package com.knoxhack.echocore.api;

/**
 * Runtime view of a known ECHO module and the version metadata NeoForge loaded.
 */
public record EchoModuleInfo(
        String modId,
        String displayName,
        String version,
        String projectPath,
        String ownership,
        boolean loaded,
        boolean expectedInAll) {
    public EchoModuleInfo {
        modId = clean(modId);
        displayName = displayName == null || displayName.isBlank() ? modId : displayName.strip();
        version = version == null ? "" : version.strip();
        projectPath = projectPath == null ? "" : projectPath.strip();
        ownership = ownership == null ? "" : ownership.strip();
    }

    public String statusLine() {
        return loaded
                ? displayName + " " + (version.isBlank() ? "unknown" : version)
                : displayName + " missing";
    }

    private static String clean(String value) {
        return value == null ? "" : value.strip().toLowerCase(java.util.Locale.ROOT);
    }
}
