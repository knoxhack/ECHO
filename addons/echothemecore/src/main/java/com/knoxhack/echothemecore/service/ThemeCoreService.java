package com.knoxhack.echothemecore.service;

import com.knoxhack.echocore.api.EchoThemeToken;
import com.knoxhack.echocore.api.IThemeService;
import com.knoxhack.echothemecore.api.EchoThemeColorKey;
import com.knoxhack.echothemecore.content.ThemeRegistry;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * ThemeCore implementation of the Core {@link IThemeService}.
 * Bridges dotted token resolution to the ThemeRegistry color system.
 */
public final class ThemeCoreService implements IThemeService {
    public static final ThemeCoreService INSTANCE = new ThemeCoreService();

    private ThemeCoreService() {
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public int resolveColor(String token, int fallback) {
        if (token == null || token.isBlank()) {
            return fallback;
        }
        EchoThemeColorKey key = tokenToColorKey(token);
        if (key == null) {
            return EchoThemeToken.resolveDefault(token, fallback);
        }
        return ThemeRegistry.getCurrentTheme().colors().color(key);
    }

    @Override
    public Optional<Identifier> resolveColorId(String token) {
        EchoThemeColorKey key = tokenToColorKey(token);
        if (key == null) {
            return Optional.empty();
        }
        return Optional.of(Identifier.fromNamespaceAndPath("echothemecore", "color/" + key.name().toLowerCase()));
    }

    @Override
    public List<String> knownTokens() {
        return List.of(
                EchoThemeToken.BACKGROUND_PRIMARY,
                EchoThemeToken.BACKGROUND_SECONDARY,
                EchoThemeToken.PANEL_PRIMARY,
                EchoThemeToken.PANEL_SECONDARY,
                EchoThemeToken.PANEL_WARNING,
                EchoThemeToken.TEXT_PRIMARY,
                EchoThemeToken.TEXT_MUTED,
                EchoThemeToken.TEXT_WARNING,
                EchoThemeToken.ACCENT_PRIMARY,
                EchoThemeToken.ACCENT_SECONDARY,
                EchoThemeToken.STATE_LOCKED,
                EchoThemeToken.STATE_READY,
                EchoThemeToken.STATE_ACTIVE,
                EchoThemeToken.STATE_COMPLETED,
                EchoThemeToken.BORDER_PRIMARY,
                EchoThemeToken.BORDER_SELECTED
        );
    }

    @Override
    public String currentThemeName() {
        return ThemeRegistry.getCurrentTheme().displayName();
    }

    private static EchoThemeColorKey tokenToColorKey(String token) {
        if (token == null) {
            return null;
        }
        return switch (token) {
            case "background.primary" -> EchoThemeColorKey.BACKGROUND;
            case "background.secondary" -> EchoThemeColorKey.GLASS;
            case "panel.primary" -> EchoThemeColorKey.PANEL;
            case "panel.secondary" -> EchoThemeColorKey.PANEL_ALT;
            case "panel.warning" -> EchoThemeColorKey.WARNING;
            case "text.primary" -> EchoThemeColorKey.TEXT;
            case "text.muted" -> EchoThemeColorKey.MUTED_TEXT;
            case "text.warning" -> EchoThemeColorKey.WARNING;
            case "accent.primary" -> EchoThemeColorKey.PRIMARY;
            case "accent.secondary" -> EchoThemeColorKey.ACCENT;
            case "state.locked" -> EchoThemeColorKey.LOCKED;
            case "state.ready" -> EchoThemeColorKey.SUCCESS;
            case "state.active" -> EchoThemeColorKey.PRIMARY;
            case "state.completed" -> EchoThemeColorKey.SUCCESS;
            case "border.primary" -> EchoThemeColorKey.BORDER;
            case "border.selected" -> EchoThemeColorKey.SELECTION;
            default -> null;
        };
    }
}
