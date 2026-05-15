package com.knoxhack.echothemecore.content;

import com.knoxhack.echothemecore.EchoThemeCore;
import com.knoxhack.echothemecore.api.DistortionStyle;
import com.knoxhack.echothemecore.api.EchoTheme;
import com.knoxhack.echothemecore.api.EchoThemeBlockPalette;
import com.knoxhack.echothemecore.api.EchoThemeColors;
import com.knoxhack.echothemecore.api.EchoThemeRenderProfile;
import com.knoxhack.echothemecore.api.EchoThemeSoundProfile;
import com.knoxhack.echothemecore.api.EchoThemeTextureKey;
import com.knoxhack.echothemecore.api.EchoThemeUiAssets;
import com.knoxhack.echothemecore.api.EchoThemeVanillaUiProfile;
import com.knoxhack.echothemecore.api.HologramStyle;
import com.knoxhack.echothemecore.api.ParticleStyle;
import com.knoxhack.echothemecore.api.TransitionStyle;
import com.knoxhack.echothemecore.config.ThemeCoreConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public final class ThemeRegistry {
    public static final Identifier CYBERGLASS_ID = id("cyberglass");
    public static final Identifier NEXUS_ID = id("nexus");

    private static final EchoTheme BUILTIN_CYBERGLASS = createBuiltinCyberGlass();
    private static final Map<Identifier, EchoTheme> THEMES = new LinkedHashMap<>();
    private static final Map<UUID, Identifier> PLAYER_THEMES = new ConcurrentHashMap<>();
    private static Identifier globalThemeId = CYBERGLASS_ID;
    private static float debugVisualIntensity = 1.0F;
    private static Consumer<Identifier> globalThemeChangeListener;
    private static BiConsumer<UUID, Identifier> playerThemeChangeListener;

    static {
        registerBuiltin(BUILTIN_CYBERGLASS);
        for (EchoTheme theme : BuiltinThemes.all()) {
            registerBuiltin(theme);
        }
        globalThemeId = BuiltinThemes.defaultDark().id();
    }

    private static void registerBuiltin(EchoTheme theme) {
        if (theme != null && theme.id() != null) {
            THEMES.put(theme.id(), theme);
        }
    }

    private ThemeRegistry() {
    }

    public static synchronized void replaceLoaded(Map<Identifier, EchoTheme> loaded) {
        THEMES.clear();
        registerBuiltin(BUILTIN_CYBERGLASS);
        for (EchoTheme theme : BuiltinThemes.all()) {
            registerBuiltin(theme);
        }
        if (loaded != null) {
            loaded.values().stream()
                .filter(theme -> theme != null && theme.id() != null)
                .forEach(theme -> THEMES.put(theme.id(), theme));
        }
        Identifier defaultDarkId = BuiltinThemes.defaultDark().id();
        Identifier configured = parseConfigured(ThemeCoreConfig.string(ThemeCoreConfig.DEFAULT_THEME), defaultDarkId);
        globalThemeId = THEMES.containsKey(configured) ? configured : defaultDarkId;
    }

    public static synchronized EchoTheme get(Identifier id) {
        if (id == null) {
            return fallbackTheme();
        }
        EchoTheme theme = THEMES.get(id);
        return theme == null ? fallbackTheme() : theme;
    }

    public static synchronized Optional<EchoTheme> find(Identifier id) {
        return Optional.ofNullable(THEMES.get(id));
    }

    public static synchronized List<EchoTheme> listThemes() {
        return Collections.unmodifiableList(new ArrayList<>(THEMES.values()));
    }

    public static synchronized EchoTheme fallbackTheme() {
        Identifier defaultDarkId = BuiltinThemes.defaultDark().id();
        Identifier fallback = parseConfigured(ThemeCoreConfig.string(ThemeCoreConfig.FALLBACK_THEME), defaultDarkId);
        EchoTheme theme = THEMES.get(fallback);
        return theme == null ? BuiltinThemes.defaultDark() : theme;
    }

    public static synchronized EchoTheme getCurrentTheme() {
        return get(globalThemeId);
    }

    public static EchoTheme getThemeFor(Player player) {
        if (player != null) {
            Identifier playerTheme = PLAYER_THEMES.get(player.getUUID());
            if (playerTheme != null) {
                return get(playerTheme);
            }
        }
        return getCurrentTheme();
    }

    public static synchronized Identifier globalThemeId() {
        return getCurrentTheme().id();
    }

    public static synchronized boolean setGlobalTheme(Identifier id) {
        if (id == null || !THEMES.containsKey(id)) {
            globalThemeId = fallbackTheme().id();
            return false;
        }
        globalThemeId = id;
        if (globalThemeChangeListener != null) {
            globalThemeChangeListener.accept(globalThemeId);
        }
        return true;
    }

    public static void setPlayerTheme(UUID playerId, Identifier id) {
        if (playerId == null) {
            return;
        }
        if (id == null || !find(id).isPresent()) {
            PLAYER_THEMES.remove(playerId);
            return;
        }
        PLAYER_THEMES.put(playerId, id);
        if (playerThemeChangeListener != null) {
            playerThemeChangeListener.accept(playerId, id);
        }
    }

    public static void clearPlayerTheme(UUID playerId) {
        if (playerId != null) {
            PLAYER_THEMES.remove(playerId);
        }
    }

    public static synchronized void reset() {
        globalThemeId = CYBERGLASS_ID;
        PLAYER_THEMES.clear();
        if (globalThemeChangeListener != null) {
            globalThemeChangeListener.accept(globalThemeId);
        }
    }

    public static float debugVisualIntensity() {
        return debugVisualIntensity;
    }

    public static void setDebugVisualIntensity(float value) {
        debugVisualIntensity = Math.max(0.0F, Math.min(2.0F, value));
    }

    public static void setGlobalThemeChangeListener(Consumer<Identifier> listener) {
        globalThemeChangeListener = listener;
    }

    public static void setPlayerThemeChangeListener(BiConsumer<UUID, Identifier> listener) {
        playerThemeChangeListener = listener;
    }

    public static int transitionTicks() {
        if (!ThemeCoreConfig.enableThemeTransitions()) {
            return 0;
        }
        return ThemeCoreConfig.themeTransitionTicks();
    }

    public static Identifier parseThemeId(String raw) {
        Identifier parsed = Identifier.tryParse(raw == null ? "" : raw.trim());
        return parsed == null ? CYBERGLASS_ID : parsed;
    }

    private static Identifier parseConfigured(String raw, Identifier fallback) {
        Identifier parsed = Identifier.tryParse(raw == null ? "" : raw.trim());
        return parsed == null ? fallback : parsed;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoThemeCore.MODID, path);
    }

    private static Identifier texture(String theme, String name) {
        return Identifier.fromNamespaceAndPath(EchoThemeCore.MODID, "textures/gui/themes/" + theme + "/" + name + ".png");
    }

    private static Identifier minecraft(String path) {
        return Identifier.fromNamespaceAndPath("minecraft", path);
    }

    private static Identifier soundcore(String path) {
        return Identifier.fromNamespaceAndPath("echosoundcore", path);
    }

    public static EchoTheme getCyberGlassBuiltin() {
        return BUILTIN_CYBERGLASS;
    }

    private static EchoTheme createBuiltinCyberGlass() {
        EchoThemeColors colors = new EchoThemeColors(
            0xFF00E5FF,
            0xFFB44CFF,
            0xFFFF2BD6,
            0xFF030711,
            0xCC08111F,
            0xCC0D1A2E,
            0x8810243A,
            0xFF2BEAFF,
            0xFF1A6F8A,
            0xFFEAFBFF,
            0xFF8AAFC2,
            0xFF45FFB0,
            0xFFFFD166,
            0xFFFF4D6D,
            0xFF3B4652,
            0xFF00E5FF,
            0xFFB44CFF
        );
        EchoThemeUiAssets ui = new EchoThemeUiAssets(
            texture("cyberglass", "background"),
            texture("cyberglass", "glass_panel"),
            texture("cyberglass", "glass_panel_alt"),
            texture("cyberglass", "glass_button"),
            texture("cyberglass", "glass_button_hover"),
            texture("cyberglass", "tab"),
            texture("cyberglass", "tab_active"),
            texture("cyberglass", "mission_card"),
            texture("cyberglass", "mission_card_selected"),
            texture("cyberglass", "status_chip"),
            texture("cyberglass", "progress_bar"),
            texture("cyberglass", "scrollbar"),
            Identifier.fromNamespaceAndPath(EchoThemeCore.MODID, "textures/gui/themes/cyberglass/icons/icon_theme.png"),
            texture("cyberglass", "hologram_overlay"),
            texture("cyberglass", "energy_overlay"),
            texture("cyberglass", "edge_glow"),
            texture("cyberglass", "particle_glints"),
            texture("cyberglass", "locked_overlay")
        );
        EchoThemeRenderProfile render = new EchoThemeRenderProfile(
            colors.primary(),
            colors.secondary(),
            colors.primary(),
            colors.accent(),
            colors.primary(),
            colors.secondary(),
            colors.glow(),
            colors.warning(),
            colors.success(),
            colors.error(),
            colors.secondary(),
            0.85F,
            0.9F,
            0.68F,
            0.62F,
            0.05F,
            0.0F,
            0.75F,
            0.45F,
            0.35F,
            0.65F,
            0.75F,
            0.85F,
            HologramStyle.CYBER_GLASS,
            ParticleStyle.SOFT_GLINTS,
            DistortionStyle.NONE,
            "GLASS_GEOMETRIC",
            TransitionStyle.GLASS_FADE
        );
        EchoThemeSoundProfile sound = new EchoThemeSoundProfile(
            soundcore("ui.terminal.select"),
            soundcore("ui.terminal.error"),
            soundcore("ui.terminal.open"),
            soundcore("ui.terminal.close"),
            soundcore("music.terminal.command_bed"),
            soundcore("stinger.objective.complete"),
            soundcore("ui.terminal.warning")
        );
        EchoThemeBlockPalette blocks = new EchoThemeBlockPalette(
            List.of(minecraft("tinted_glass"), minecraft("cyan_stained_glass"), minecraft("magenta_stained_glass")),
            List.of(minecraft("blackstone"), minecraft("deepslate_tiles")),
            List.of(minecraft("tinted_glass"), minecraft("cyan_stained_glass")),
            List.of(minecraft("sea_lantern"), minecraft("end_rod")),
            List.of(minecraft("amethyst_block"), minecraft("magenta_glazed_terracotta"))
        );
        EchoThemeVanillaUiProfile vanilla = new EchoThemeVanillaUiProfile(
            texture("cyberglass", "background"),
            texture("cyberglass", "glass_panel"),
            texture("cyberglass", "glass_button"),
            texture("cyberglass", "status_chip"),
            texture("cyberglass", "vanilla_tooltip_panel"),
            texture("cyberglass", "vanilla_toast_accent"),
            texture("cyberglass", "vanilla_boss_bar_accent"),
            0x88030711,
            0xCC08111F,
            0xDD10243A,
            colors.border(),
            colors.selection(),
            colors.glow(),
            0.34F,
            0.58F,
            0.72F,
            true
        );
        Map<EchoThemeTextureKey, Identifier> moduleTextures = new LinkedHashMap<>();
        moduleTextures.put(EchoThemeTextureKey.TERMINAL_PANEL, texture("cyberglass", "glass_panel"));
        moduleTextures.put(EchoThemeTextureKey.TERMINAL_TAB, texture("cyberglass", "tab"));
        moduleTextures.put(EchoThemeTextureKey.TERMINAL_TAB_ACTIVE, texture("cyberglass", "tab_active"));
        moduleTextures.put(EchoThemeTextureKey.TERMINAL_MISSION_CARD, texture("cyberglass", "mission_card"));
        moduleTextures.put(EchoThemeTextureKey.TERMINAL_STATUS_CHIP, texture("cyberglass", "status_chip"));
        moduleTextures.put(EchoThemeTextureKey.TERMINAL_BUTTON, texture("cyberglass", "glass_button"));
        moduleTextures.put(EchoThemeTextureKey.TERMINAL_ICON, texture("cyberglass", "icons/icon_terminal"));
        moduleTextures.put(EchoThemeTextureKey.HOLOMAP_GRID, texture("cyberglass", "holomap_grid"));
        moduleTextures.put(EchoThemeTextureKey.HOLOMAP_PANEL, texture("cyberglass", "holomap_panel"));
        moduleTextures.put(EchoThemeTextureKey.HOLOMAP_ROUTE, texture("cyberglass", "route_line"));
        moduleTextures.put(EchoThemeTextureKey.HOLOMAP_MARKER_SIGNAL, texture("cyberglass", "marker_signal"));
        moduleTextures.put(EchoThemeTextureKey.HOLOMAP_MARKER_HAZARD, texture("cyberglass", "marker_hazard"));
        moduleTextures.put(EchoThemeTextureKey.HOLOMAP_MARKER_MISSION, texture("cyberglass", "marker_mission"));
        moduleTextures.put(EchoThemeTextureKey.HOLOMAP_MARKER_NEXUS, texture("cyberglass", "marker_nexus"));
        moduleTextures.put(EchoThemeTextureKey.HOLOMAP_MARKER_RECLAIMED, texture("cyberglass", "marker_reclamation"));
        moduleTextures.put(EchoThemeTextureKey.HOLOMAP_SELECTED_RING, texture("cyberglass", "selected_marker_ring"));
        moduleTextures.put(EchoThemeTextureKey.HOLOMAP_DANGER, texture("cyberglass", "marker_hazard"));
        moduleTextures.put(EchoThemeTextureKey.HOLOMAP_ANOMALY, texture("cyberglass", "marker_nexus"));
        moduleTextures.put(EchoThemeTextureKey.HOLOMAP_RECLAIMED, texture("cyberglass", "marker_reclamation"));
        moduleTextures.put(EchoThemeTextureKey.LENS_SCAN_RING, texture("cyberglass", "lens_scan_ring"));
        moduleTextures.put(EchoThemeTextureKey.LENS_TARGET_BOX, texture("cyberglass", "lens_target_box"));
        moduleTextures.put(EchoThemeTextureKey.LENS_WEAK_POINT, texture("cyberglass", "lens_weakpoint_marker"));
        moduleTextures.put(EchoThemeTextureKey.LENS_WARNING, texture("cyberglass", "lens_warning_overlay"));
        moduleTextures.put(EchoThemeTextureKey.LENS_ANOMALY_REVEAL, texture("cyberglass", "lens_anomaly_overlay"));
        moduleTextures.put(EchoThemeTextureKey.LENS_COMPLETION_PULSE, texture("cyberglass", "lens_progress_arc"));
        moduleTextures.put(EchoThemeTextureKey.LENS_PROGRESS_ARC, texture("cyberglass", "lens_progress_arc"));
        moduleTextures.put(EchoThemeTextureKey.LENS_NOISE_OVERLAY, texture("cyberglass", "lens_noise_overlay"));
        moduleTextures.put(EchoThemeTextureKey.VANILLA_CONTAINER_FRAME, texture("cyberglass", "vanilla_container_frame"));
        moduleTextures.put(EchoThemeTextureKey.VANILLA_INVENTORY_FRAME, texture("cyberglass", "vanilla_inventory_frame"));
        moduleTextures.put(EchoThemeTextureKey.VANILLA_TITLE_BACKPLATE, texture("cyberglass", "vanilla_title_backplate"));
        moduleTextures.put(EchoThemeTextureKey.VANILLA_PAUSE_PANEL, texture("cyberglass", "vanilla_pause_panel"));
        moduleTextures.put(EchoThemeTextureKey.VANILLA_SELECTED_SLOT, texture("cyberglass", "vanilla_hotbar_accent"));
        moduleTextures.put(EchoThemeTextureKey.VANILLA_TOOLTIP_PANEL, texture("cyberglass", "vanilla_tooltip_panel"));
        moduleTextures.put(EchoThemeTextureKey.VANILLA_TOAST_ACCENT, texture("cyberglass", "vanilla_toast_accent"));
        moduleTextures.put(EchoThemeTextureKey.VANILLA_BOSS_BAR_ACCENT, texture("cyberglass", "vanilla_boss_bar_accent"));
        moduleTextures.put(EchoThemeTextureKey.VANILLA_WIDGET_OUTLINE, texture("cyberglass", "vanilla_widget_outline"));
        moduleTextures.put(EchoThemeTextureKey.RENDERCORE_GLOW_OVERLAY, texture("cyberglass", "rendercore/glow_overlay_reference"));
        moduleTextures.put(EchoThemeTextureKey.RENDERCORE_DISTORTION_OVERLAY, texture("cyberglass", "rendercore/distortion_overlay"));
        moduleTextures.put(EchoThemeTextureKey.RENDERCORE_ENTITY_HIGHLIGHT, texture("cyberglass", "rendercore/entity_highlight_reference"));
        moduleTextures.put(EchoThemeTextureKey.RENDERCORE_MULTIBLOCK_ENERGY, texture("cyberglass", "rendercore/multiblock_energy_lines"));
        return new EchoTheme(
            CYBERGLASS_ID,
            "CyberGlass",
            "A clean cyberpunk futuristic glass theme with dark translucent panels, cyan hologram glow, magenta accents, thin neon borders, and premium modern ECHO UI styling.",
            colors,
            ui,
            render,
            sound,
            blocks,
            vanilla,
            moduleTextures,
            Map.of("module_tags", "terminal,signalos,holomap,lens,rendercore,soundcore,blockworks,vanilla_ui",
                "default", "true", "tier", "default", "family", "cyberglass", "version", "0.2.0")
        );
    }
}
