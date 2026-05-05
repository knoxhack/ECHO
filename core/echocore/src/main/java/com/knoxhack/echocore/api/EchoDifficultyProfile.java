package com.knoxhack.echocore.api;

import java.util.Locale;

public enum EchoDifficultyProfile {
    GUIDED("Guided Normal"),
    NORMAL("Normal"),
    HARDCORE("Hardcore"),
    STORY("Story");

    private final String displayName;

    EchoDifficultyProfile(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static EchoDifficultyProfile byId(String id) {
        if (id == null || id.isBlank()) {
            return GUIDED;
        }
        String normalized = id.trim().toUpperCase(Locale.ROOT);
        for (EchoDifficultyProfile profile : values()) {
            if (profile.name().equals(normalized)) {
                return profile;
            }
        }
        return GUIDED;
    }
}
