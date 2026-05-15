package com.knoxhack.echocore.api;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/**
 * Optional theme provider. The fallback is intentionally inert so UI mods can run without ThemeCore.
 */
public interface IThemeService {

    /**
     * Returns true if the ThemeCore module is loaded and functional.
     */
    default boolean available() {
        return false;
    }

    /**
     * Resolve a dotted theme token to an integer color.
     * Examples: {@code background.primary}, {@code text.muted}, {@code accent.primary}
     */
    default int resolveColor(String token, int fallback) {
        return fallback;
    }

    /**
     * Resolve a dotted theme token to a named color identifier.
     */
    default Optional<Identifier> resolveColorId(String token) {
        return Optional.empty();
    }

    /**
     * List known theme token prefixes.
     */
    default java.util.List<String> knownTokens() {
        return List.of();
    }

    /**
     * Returns a human-readable name for the active theme, for diagnostics.
     */
    default String currentThemeName() {
        return "Default Dark";
    }
}
