package com.knoxhack.echocore.api;

import java.util.Map;

/**
 * Dotted theme token vocabulary for the ECHO theme system.
 * <p>
 * These tokens are the public pack-facing identifier for theme colors.
 * UI modules should request colors through {@link IThemeService#resolveColor(String, int)}
 * using these tokens, with a local fallback integer.
 */
public final class EchoThemeToken {
    private EchoThemeToken() {
    }

    public static final String BACKGROUND_PRIMARY = "background.primary";
    public static final String BACKGROUND_SECONDARY = "background.secondary";
    public static final String PANEL_PRIMARY = "panel.primary";
    public static final String PANEL_SECONDARY = "panel.secondary";
    public static final String PANEL_WARNING = "panel.warning";
    public static final String TEXT_PRIMARY = "text.primary";
    public static final String TEXT_MUTED = "text.muted";
    public static final String TEXT_WARNING = "text.warning";
    public static final String ACCENT_PRIMARY = "accent.primary";
    public static final String ACCENT_SECONDARY = "accent.secondary";
    public static final String STATE_LOCKED = "state.locked";
    public static final String STATE_READY = "state.ready";
    public static final String STATE_ACTIVE = "state.active";
    public static final String STATE_COMPLETED = "state.completed";
    public static final String BORDER_PRIMARY = "border.primary";
    public static final String BORDER_SELECTED = "border.selected";

    private static final Map<String, Integer> DEFAULT_DARK = Map.ofEntries(
            Map.entry(BACKGROUND_PRIMARY, 0xFF1A1A2E),
            Map.entry(BACKGROUND_SECONDARY, 0xFF16213E),
            Map.entry(PANEL_PRIMARY, 0xCC0F3460),
            Map.entry(PANEL_SECONDARY, 0x881A1A2E),
            Map.entry(PANEL_WARNING, 0xCC8B4500),
            Map.entry(TEXT_PRIMARY, 0xFFE0E0E0),
            Map.entry(TEXT_MUTED, 0xFF8A8A8A),
            Map.entry(TEXT_WARNING, 0xFFFFD166),
            Map.entry(ACCENT_PRIMARY, 0xFF00E5FF),
            Map.entry(ACCENT_SECONDARY, 0xFFB44CFF),
            Map.entry(STATE_LOCKED, 0xFF3B4652),
            Map.entry(STATE_READY, 0xFF45FFB0),
            Map.entry(STATE_ACTIVE, 0xFF00E5FF),
            Map.entry(STATE_COMPLETED, 0xFF45FFB0),
            Map.entry(BORDER_PRIMARY, 0xFF2BEAFF),
            Map.entry(BORDER_SELECTED, 0xFFB44CFF)
    );

    /**
     * Resolves a dotted token to a color using the Default Dark fallback palette.
     */
    public static int resolveDefault(String token, int fallback) {
        if (token == null || token.isBlank()) {
            return fallback;
        }
        return DEFAULT_DARK.getOrDefault(token, fallback);
    }

    /**
     * Returns the Default Dark fallback palette as an unmodifiable map.
     */
    public static Map<String, Integer> defaultDarkPalette() {
        return DEFAULT_DARK;
    }
}
