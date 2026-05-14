package com.knoxhack.echothemecore.content;

import com.knoxhack.echothemecore.EchoThemeCore;
import com.knoxhack.echothemecore.api.DistortionStyle;
import com.knoxhack.echothemecore.api.EchoTheme;
import com.knoxhack.echothemecore.api.EchoThemeBlockPalette;
import com.knoxhack.echothemecore.api.EchoThemeColors;
import com.knoxhack.echothemecore.api.EchoThemeRenderProfile;
import com.knoxhack.echothemecore.api.EchoThemeSoundProfile;
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

    static {
        THEMES.put(BUILTIN_CYBERGLASS.id(), BUILTIN_CYBERGLASS);
    }

    private ThemeRegistry() {
    }

    public static synchronized void replaceLoaded(Map<Identifier, EchoTheme> loaded) {
        THEMES.clear();
        THEMES.put(BUILTIN_CYBERGLASS.id(), BUILTIN_CYBERGLASS);
        if (loaded != null) {
            loaded.values().stream()
                .filter(theme -> theme != null && theme.id() != null)
                .forEach(theme -> THEMES.put(theme.id(), theme));
        }
        Identifier configured = parseConfigured(ThemeCoreConfig.string(ThemeCoreConfig.DEFAULT_THEME), CYBERGLASS_ID);
        globalThemeId = THEMES.containsKey(configured) ? configured : CYBERGLASS_ID;
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
        Identifier fallback = parseConfigured(ThemeCoreConfig.string(ThemeCoreConfig.FALLBACK_THEME), CYBERGLASS_ID);
        EchoTheme theme = THEMES.get(fallback);
        return theme == null ? BUILTIN_CYBERGLASS : theme;
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
    }

    public static void clearPlayerTheme(UUID playerId) {
        if (playerId != null) {
            PLAYER_THEMES.remove(playerId);
        }
    }

    public static synchronized void reset() {
        globalThemeId = CYBERGLASS_ID;
        PLAYER_THEMES.clear();
    }

    public static float debugVisualIntensity() {
        return debugVisualIntensity;
    }

    public static void setDebugVisualIntensity(float value) {
        debugVisualIntensity = Math.max(0.0F, Math.min(2.0F, value));
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
        EchoThemeSoundProfile sound = new EchoThemeSoundProfile(null, null, null, null, null, null, null);
        EchoThemeBlockPalette blocks = new EchoThemeBlockPalette(List.of(), List.of(), List.of(), List.of(), List.of());
        return new EchoTheme(
            CYBERGLASS_ID,
            "CyberGlass",
            "A clean cyberpunk futuristic glass theme with dark translucent panels, cyan hologram glow, magenta accents, thin neon borders, and premium modern ECHO UI styling.",
            colors,
            ui,
            render,
            sound,
            blocks,
            EchoThemeVanillaUiProfile.fromParts(colors, ui, render),
            Map.of("tier", "default", "family", "cyberglass")
        );
    }
}
