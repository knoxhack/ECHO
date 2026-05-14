package com.knoxhack.echothemecore.api;

import java.util.Locale;

public enum DistortionStyle {
    NONE,
    LIGHT_STATIC,
    GLITCH,
    PHASE_WARP,
    NEXUS_RIPPLE;

    public static DistortionStyle byName(String value, DistortionStyle fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
