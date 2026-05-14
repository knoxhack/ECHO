package com.knoxhack.echothemecore.api;

import com.knoxhack.echothemecore.content.ThemeRegistry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class EchoThemeApi {
    private EchoThemeApi() {
    }

    public static EchoTheme getTheme() {
        return ThemeRegistry.getCurrentTheme();
    }

    public static EchoTheme getTheme(Player player) {
        return ThemeRegistry.getThemeFor(player);
    }

    public static EchoTheme getTheme(Identifier id) {
        return ThemeRegistry.get(id);
    }

    public static Identifier getThemeId(Player player) {
        return getTheme(player).id();
    }

    public static void setGlobalTheme(Identifier id) {
        ThemeRegistry.setGlobalTheme(id);
    }

    public static void setPlayerTheme(UUID playerId, Identifier id) {
        ThemeRegistry.setPlayerTheme(playerId, id);
    }

    public static List<EchoTheme> getThemes() {
        return ThemeRegistry.listThemes();
    }

    public static EchoThemeColors getColors(Player player) {
        return getTheme(player).colors();
    }

    public static EchoThemeRenderProfile getRenderProfile(Player player) {
        return getTheme(player).renderProfile();
    }

    public static EchoThemeVanillaUiProfile getVanillaUiProfile(Player player) {
        return getTheme(player).vanillaUiProfile();
    }

    public static ThemeVisualSettings getEffectiveVisualSettings(Player player) {
        return ThemeVisualSettings.resolve(getTheme(player));
    }

    public static int color(Player player, EchoThemeColorKey key) {
        return getTheme(player).colors().color(key);
    }

    public static Optional<Identifier> getTexture(Player player, EchoThemeTextureKey key) {
        EchoTheme theme = getTheme(player);
        Optional<Identifier> vanilla = theme.vanillaUiProfile().texture(key);
        if (vanilla.isPresent()) {
            return vanilla;
        }
        Optional<Identifier> ui = theme.uiAssets().texture(key);
        if (ui.isPresent()) {
            return ui;
        }
        return theme.moduleTexture(key);
    }

    public static Optional<Identifier> getModuleTexture(Player player, EchoThemeTextureKey key) {
        EchoTheme theme = getTheme(player);
        Optional<Identifier> direct = theme.moduleTexture(key);
        if (direct.isPresent()) {
            return direct;
        }
        return switch (key) {
            case TERMINAL_PANEL, TERMINAL_TAB, TERMINAL_TAB_ACTIVE, TERMINAL_MISSION_CARD,
                 TERMINAL_STATUS_CHIP, TERMINAL_BUTTON -> theme.uiAssets().texture(EchoThemeTextureKey.PANEL);
            case TERMINAL_ICON -> theme.uiAssets().texture(EchoThemeTextureKey.ICON_PACK);
            case HOLOMAP_GRID, HOLOMAP_PANEL, HOLOMAP_ROUTE, HOLOMAP_MARKER_SIGNAL,
                 HOLOMAP_MARKER_HAZARD, HOLOMAP_MARKER_MISSION, HOLOMAP_MARKER_NEXUS,
                 HOLOMAP_MARKER_RECLAIMED, HOLOMAP_SELECTED_RING, HOLOMAP_DANGER,
                 HOLOMAP_ANOMALY, HOLOMAP_RECLAIMED ->
                theme.uiAssets().texture(EchoThemeTextureKey.PANEL);
            case LENS_SCAN_RING, LENS_TARGET_BOX, LENS_WEAK_POINT, LENS_WARNING, LENS_ANOMALY_REVEAL,
                 LENS_COMPLETION_PULSE, LENS_PROGRESS_ARC, LENS_NOISE_OVERLAY ->
                theme.uiAssets().texture(EchoThemeTextureKey.EDGE_GLOW);
            case VANILLA_CONTAINER_FRAME, VANILLA_INVENTORY_FRAME, VANILLA_TITLE_BACKPLATE,
                 VANILLA_PAUSE_PANEL, VANILLA_TOOLTIP_PANEL, VANILLA_WIDGET_OUTLINE ->
                theme.vanillaUiProfile().texture(EchoThemeTextureKey.VANILLA_PANEL);
            case VANILLA_SELECTED_SLOT -> theme.vanillaUiProfile().texture(EchoThemeTextureKey.VANILLA_HOTBAR);
            case VANILLA_TOAST_ACCENT -> theme.vanillaUiProfile().texture(EchoThemeTextureKey.VANILLA_TOAST);
            case VANILLA_BOSS_BAR_ACCENT -> theme.vanillaUiProfile().texture(EchoThemeTextureKey.VANILLA_BOSS_BAR);
            case RENDERCORE_GLOW_OVERLAY -> theme.uiAssets().texture(EchoThemeTextureKey.ENERGY_OVERLAY);
            case RENDERCORE_DISTORTION_OVERLAY -> theme.uiAssets().texture(EchoThemeTextureKey.HOLOGRAM_OVERLAY);
            case RENDERCORE_ENTITY_HIGHLIGHT -> theme.uiAssets().texture(EchoThemeTextureKey.HOLOGRAM_OVERLAY);
            case RENDERCORE_MULTIBLOCK_ENERGY -> theme.uiAssets().texture(EchoThemeTextureKey.ENERGY_OVERLAY);
            default -> Optional.empty();
        };
    }

    public static Optional<Identifier> getSound(Player player, EchoThemeSoundKey key) {
        return getTheme(player).soundProfile().sound(key);
    }

    public static ThemeTransition getTransition(Identifier fromTheme, Identifier toTheme) {
        EchoTheme target = ThemeRegistry.get(toTheme);
        EchoThemeRenderProfile render = target.renderProfile();
        return new ThemeTransition(
            fromTheme,
            target.id(),
            render.transitionStyle(),
            ThemeRegistry.transitionTicks(),
            render.hologramColor(),
            render.hologramSecondary(),
            render.edgeGlowStrength(),
            render.particleIntensity()
        );
    }

    public static void playThemeTransition(Player player, Identifier newTheme) {
        ThemeRegistry.setPlayerTheme(player.getUUID(), newTheme);
    }
}
