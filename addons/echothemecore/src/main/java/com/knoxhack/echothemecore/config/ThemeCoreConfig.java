package com.knoxhack.echothemecore.config;

import com.knoxhack.echocore.api.config.EchoConfigCategory;
import com.knoxhack.echocore.api.config.EchoConfigEntry;
import com.knoxhack.echocore.api.config.EchoConfigModule;
import com.knoxhack.echocore.api.config.EchoConfigProvider;
import com.knoxhack.echocore.api.config.EchoConfigRegistry;
import com.knoxhack.echocore.api.config.EchoConfigSide;
import com.knoxhack.echothemecore.EchoThemeCore;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ThemeCoreConfig {
    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> DEFAULT_THEME;
    public static final ModConfigSpec.ConfigValue<String> FALLBACK_THEME;
    public static final ModConfigSpec.BooleanValue ALLOW_PLAYER_THEME_OVERRIDE;
    public static final ModConfigSpec.BooleanValue SYNC_SERVER_THEME;
    public static final ModConfigSpec.BooleanValue THEME_AFFECTS_MAIN_MENU;
    public static final ModConfigSpec.BooleanValue THEME_AFFECTS_TERMINAL;
    public static final ModConfigSpec.BooleanValue THEME_AFFECTS_HOLOMAP;
    public static final ModConfigSpec.BooleanValue THEME_AFFECTS_LENS;
    public static final ModConfigSpec.BooleanValue THEME_AFFECTS_RENDERCORE;
    public static final ModConfigSpec.BooleanValue THEME_AFFECTS_SOUNDCORE;
    public static final ModConfigSpec.BooleanValue DEBUG_THEME_LOGGING;

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_HOLOGRAM_OVERLAY;
    public static final ModConfigSpec.BooleanValue ENABLE_ENERGY_OVERLAY;
    public static final ModConfigSpec.BooleanValue ENABLE_EDGE_GLOW;
    public static final ModConfigSpec.BooleanValue ENABLE_PARTICLE_GLINTS;
    public static final ModConfigSpec.BooleanValue ENABLE_THEME_TRANSITIONS;
    public static final ModConfigSpec.IntValue THEME_TRANSITION_TICKS;
    public static final ModConfigSpec.BooleanValue ENABLE_TRANSITION_PARTICLES;

    public static final ModConfigSpec.BooleanValue FORCE_HIGH_CONTRAST;
    public static final ModConfigSpec.BooleanValue REDUCE_GLOW;
    public static final ModConfigSpec.BooleanValue DISABLE_DISTORTION;
    public static final ModConfigSpec.BooleanValue DISABLE_NOISE;

    public static final ModConfigSpec.BooleanValue ENABLE_VANILLA_UI_THEMING;
    public static final ModConfigSpec.BooleanValue THEME_MAIN_MENU;
    public static final ModConfigSpec.BooleanValue THEME_PAUSE_MENU;
    public static final ModConfigSpec.BooleanValue THEME_OPTIONS_MENU;
    public static final ModConfigSpec.BooleanValue THEME_WORLD_SELECT;
    public static final ModConfigSpec.BooleanValue THEME_LOADING_SCREEN;
    public static final ModConfigSpec.BooleanValue THEME_INVENTORY;
    public static final ModConfigSpec.BooleanValue THEME_CONTAINERS;
    public static final ModConfigSpec.BooleanValue THEME_CREATIVE_INVENTORY;
    public static final ModConfigSpec.BooleanValue THEME_ADVANCEMENTS;
    public static final ModConfigSpec.BooleanValue THEME_RECIPE_BOOK;
    public static final ModConfigSpec.BooleanValue THEME_TOOLTIPS;
    public static final ModConfigSpec.BooleanValue THEME_TOASTS;
    public static final ModConfigSpec.BooleanValue THEME_BOSS_BAR;
    public static final ModConfigSpec.BooleanValue THEME_HOTBAR;
    public static final ModConfigSpec.BooleanValue THEME_CHAT;

    public static final ModConfigSpec.BooleanValue VANILLA_SAFE_MODE;
    public static final ModConfigSpec.BooleanValue DISABLE_IF_SCREEN_UNKNOWN;
    public static final ModConfigSpec.BooleanValue DO_NOT_MODIFY_SLOT_POSITIONS;
    public static final ModConfigSpec.BooleanValue PRESERVE_VANILLA_TEXT_CONTRAST;
    public static final ModConfigSpec.BooleanValue SHOW_DEBUG_SCREEN_NAMES;

    public static final ModConfigSpec.BooleanValue GLASS_INVENTORY_PANELS;
    public static final ModConfigSpec.BooleanValue BUTTON_RESKIN;
    public static final ModConfigSpec.BooleanValue TRANSPARENT_PANELS;
    public static final ModConfigSpec.BooleanValue VANILLA_EDGE_GLOW;
    public static final ModConfigSpec.BooleanValue VANILLA_HOLOGRAM_OVERLAY;
    public static final ModConfigSpec.BooleanValue ENERGY_BACKGROUND;
    public static final ModConfigSpec.BooleanValue REDUCE_VANILLA_BROWN;

    public static final ModConfigSpec CLIENT_SPEC;

    static {
        COMMON_BUILDER.push("theme");
        DEFAULT_THEME = COMMON_BUILDER
            .comment("Default ECHO theme id.")
            .define("default_theme", "echothemecore:cyberglass");
        FALLBACK_THEME = COMMON_BUILDER
            .comment("Theme id used whenever a selected theme is missing or invalid.")
            .define("fallback_theme", "echothemecore:cyberglass");
        ALLOW_PLAYER_THEME_OVERRIDE = COMMON_BUILDER.define("allow_player_theme_override", true);
        SYNC_SERVER_THEME = COMMON_BUILDER.define("sync_server_theme", true);
        THEME_AFFECTS_MAIN_MENU = COMMON_BUILDER.define("theme_affects_main_menu", true);
        THEME_AFFECTS_TERMINAL = COMMON_BUILDER.define("theme_affects_terminal", true);
        THEME_AFFECTS_HOLOMAP = COMMON_BUILDER.define("theme_affects_holomap", true);
        THEME_AFFECTS_LENS = COMMON_BUILDER.define("theme_affects_lens", true);
        THEME_AFFECTS_RENDERCORE = COMMON_BUILDER.define("theme_affects_rendercore", true);
        THEME_AFFECTS_SOUNDCORE = COMMON_BUILDER.define("theme_affects_soundcore", true);
        DEBUG_THEME_LOGGING = COMMON_BUILDER.define("debug_theme_logging", false);
        COMMON_BUILDER.pop();
        SPEC = COMMON_BUILDER.build();

        CLIENT_BUILDER.push("client");
        ENABLE_HOLOGRAM_OVERLAY = CLIENT_BUILDER.define("enable_hologram_overlay", true);
        ENABLE_ENERGY_OVERLAY = CLIENT_BUILDER.define("enable_energy_overlay", true);
        ENABLE_EDGE_GLOW = CLIENT_BUILDER.define("enable_edge_glow", true);
        ENABLE_PARTICLE_GLINTS = CLIENT_BUILDER.define("enable_particle_glints", true);
        ENABLE_THEME_TRANSITIONS = CLIENT_BUILDER.define("enable_theme_transitions", true);
        THEME_TRANSITION_TICKS = CLIENT_BUILDER.defineInRange("theme_transition_ticks", 20, 0, 80);
        ENABLE_TRANSITION_PARTICLES = CLIENT_BUILDER.define("enable_transition_particles", true);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.push("accessibility");
        FORCE_HIGH_CONTRAST = CLIENT_BUILDER.define("force_high_contrast", false);
        REDUCE_GLOW = CLIENT_BUILDER.define("reduce_glow", false);
        DISABLE_DISTORTION = CLIENT_BUILDER.define("disable_distortion", false);
        DISABLE_NOISE = CLIENT_BUILDER.define("disable_noise", false);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.push("vanilla_ui");
        ENABLE_VANILLA_UI_THEMING = CLIENT_BUILDER.define("enable_vanilla_ui_theming", true);
        THEME_MAIN_MENU = CLIENT_BUILDER.define("theme_main_menu", true);
        THEME_PAUSE_MENU = CLIENT_BUILDER.define("theme_pause_menu", true);
        THEME_OPTIONS_MENU = CLIENT_BUILDER.define("theme_options_menu", true);
        THEME_WORLD_SELECT = CLIENT_BUILDER.define("theme_world_select", true);
        THEME_LOADING_SCREEN = CLIENT_BUILDER.define("theme_loading_screen", true);
        THEME_INVENTORY = CLIENT_BUILDER.define("theme_inventory", true);
        THEME_CONTAINERS = CLIENT_BUILDER.define("theme_containers", true);
        THEME_CREATIVE_INVENTORY = CLIENT_BUILDER.define("theme_creative_inventory", true);
        THEME_ADVANCEMENTS = CLIENT_BUILDER.define("theme_advancements", true);
        THEME_RECIPE_BOOK = CLIENT_BUILDER.define("theme_recipe_book", true);
        THEME_TOOLTIPS = CLIENT_BUILDER.define("theme_tooltips", true);
        THEME_TOASTS = CLIENT_BUILDER.define("theme_toasts", true);
        THEME_BOSS_BAR = CLIENT_BUILDER.define("theme_boss_bar", true);
        THEME_HOTBAR = CLIENT_BUILDER.define("theme_hotbar", true);
        THEME_CHAT = CLIENT_BUILDER.define("theme_chat", false);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.push("vanilla_ui_safety");
        VANILLA_SAFE_MODE = CLIENT_BUILDER.define("safe_mode", false);
        DISABLE_IF_SCREEN_UNKNOWN = CLIENT_BUILDER.define("disable_if_screen_unknown", true);
        DO_NOT_MODIFY_SLOT_POSITIONS = CLIENT_BUILDER.define("do_not_modify_slot_positions", true);
        PRESERVE_VANILLA_TEXT_CONTRAST = CLIENT_BUILDER.define("preserve_vanilla_text_contrast", true);
        SHOW_DEBUG_SCREEN_NAMES = CLIENT_BUILDER.define("show_debug_screen_names", false);
        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.push("vanilla_ui_style");
        GLASS_INVENTORY_PANELS = CLIENT_BUILDER.define("glass_inventory_panels", true);
        BUTTON_RESKIN = CLIENT_BUILDER.define("button_reskin", true);
        TRANSPARENT_PANELS = CLIENT_BUILDER.define("transparent_panels", true);
        VANILLA_EDGE_GLOW = CLIENT_BUILDER.define("edge_glow", true);
        VANILLA_HOLOGRAM_OVERLAY = CLIENT_BUILDER.define("hologram_overlay", true);
        ENERGY_BACKGROUND = CLIENT_BUILDER.define("energy_background", true);
        REDUCE_VANILLA_BROWN = CLIENT_BUILDER.define("reduce_vanilla_brown", true);
        CLIENT_BUILDER.pop();

        CLIENT_SPEC = CLIENT_BUILDER.build();
    }

    private ThemeCoreConfig() {
    }

    public static boolean allowPlayerThemeOverride() {
        return bool(ALLOW_PLAYER_THEME_OVERRIDE);
    }

    public static boolean syncServerTheme() {
        return bool(SYNC_SERVER_THEME);
    }

    public static boolean debugThemeLogging() {
        return bool(DEBUG_THEME_LOGGING);
    }

    public static boolean enableEdgeGlow() {
        return bool(ENABLE_EDGE_GLOW);
    }

    public static boolean enableParticleGlints() {
        return bool(ENABLE_PARTICLE_GLINTS);
    }

    public static boolean enableThemeTransitions() {
        return bool(ENABLE_THEME_TRANSITIONS);
    }

    public static int themeTransitionTicks() {
        return integer(THEME_TRANSITION_TICKS);
    }

    public static boolean forceHighContrast() {
        return bool(FORCE_HIGH_CONTRAST);
    }

    public static boolean reduceGlow() {
        return bool(REDUCE_GLOW);
    }

    public static boolean disableDistortion() {
        return bool(DISABLE_DISTORTION);
    }

    public static boolean disableNoise() {
        return bool(DISABLE_NOISE);
    }

    public static boolean vanillaUiEnabled() {
        return bool(ENABLE_VANILLA_UI_THEMING);
    }

    public static boolean vanillaSafeMode() {
        return bool(VANILLA_SAFE_MODE);
    }

    public static boolean disableUnknownScreens() {
        return bool(DISABLE_IF_SCREEN_UNKNOWN);
    }

    public static boolean preserveTextContrast() {
        return bool(PRESERVE_VANILLA_TEXT_CONTRAST);
    }

    public static boolean showDebugScreenNames() {
        return bool(SHOW_DEBUG_SCREEN_NAMES);
    }

    public static boolean bool(ModConfigSpec.BooleanValue value) {
        try {
            return value.get();
        } catch (RuntimeException ignored) {
            return Boolean.TRUE.equals(value.getDefault());
        }
    }

    public static int integer(ModConfigSpec.IntValue value) {
        try {
            return value.get();
        } catch (RuntimeException ignored) {
            return value.getDefault();
        }
    }

    public static String string(ModConfigSpec.ConfigValue<String> value) {
        try {
            return value.get();
        } catch (RuntimeException ignored) {
            return value.getDefault();
        }
    }

    public static void registerEchoConfig() {
        EchoConfigRegistry.register(EchoConfigProvider.of(EchoThemeCore.MODID, () -> new EchoConfigModule(
            EchoThemeCore.MODID,
            "ThemeCore",
            List.of(
                new EchoConfigCategory("theme", "Theme", List.of(
                    EchoConfigEntry.stringSpec("default_theme", "Default Theme", "Default ECHO theme id.",
                        EchoConfigSide.COMMON, DEFAULT_THEME, true, false, false),
                    EchoConfigEntry.stringSpec("fallback_theme", "Fallback Theme", "Theme used when a selection is missing.",
                        EchoConfigSide.COMMON, FALLBACK_THEME, true, false, false),
                    EchoConfigEntry.booleanSpec("player_overrides", "Player Overrides", "Allows player-specific theme selection.",
                        EchoConfigSide.COMMON, ALLOW_PLAYER_THEME_OVERRIDE, true, false, false),
                    EchoConfigEntry.booleanSpec("server_sync", "Server Sync", "Makes the server-selected theme authoritative.",
                        EchoConfigSide.COMMON, SYNC_SERVER_THEME, true, false, false))),
                new EchoConfigCategory("visuals", "Visuals", List.of(
                    EchoConfigEntry.booleanSpec("transitions", "Theme Transitions", "Enables lightweight theme transitions.",
                        EchoConfigSide.CLIENT, ENABLE_THEME_TRANSITIONS, true, false, false),
                    EchoConfigEntry.intSpec("transition_ticks", "Transition Ticks", "Theme transition duration in client ticks.",
                        EchoConfigSide.CLIENT, THEME_TRANSITION_TICKS, 0, 80, true, false, false),
                    EchoConfigEntry.booleanSpec("edge_glow", "Edge Glow", "Enables themed edge glow accents.",
                        EchoConfigSide.CLIENT, ENABLE_EDGE_GLOW, true, false, false),
                    EchoConfigEntry.booleanSpec("particle_glints", "Particle Glints", "Enables themed particle accents.",
                        EchoConfigSide.CLIENT, ENABLE_PARTICLE_GLINTS, true, false, false))),
                new EchoConfigCategory("accessibility", "Accessibility", List.of(
                    EchoConfigEntry.booleanSpec("high_contrast", "High Contrast", "Uses stronger text and backdrop contrast.",
                        EchoConfigSide.CLIENT, FORCE_HIGH_CONTRAST, true, false, false),
                    EchoConfigEntry.booleanSpec("reduce_glow", "Reduce Glow", "Reduces glow and emissive intensity.",
                        EchoConfigSide.CLIENT, REDUCE_GLOW, true, false, false),
                    EchoConfigEntry.booleanSpec("disable_distortion", "Disable Distortion", "Disables distortion-heavy visuals.",
                        EchoConfigSide.CLIENT, DISABLE_DISTORTION, true, false, false),
                    EchoConfigEntry.booleanSpec("disable_noise", "Disable Noise", "Disables fine-grain visual noise.",
                        EchoConfigSide.CLIENT, DISABLE_NOISE, true, false, false))),
                new EchoConfigCategory("vanilla_ui", "Vanilla UI", List.of(
                    EchoConfigEntry.booleanSpec("enabled", "Enabled", "Enables the client-side vanilla UI skin layer.",
                        EchoConfigSide.CLIENT, ENABLE_VANILLA_UI_THEMING, true, false, false),
                    EchoConfigEntry.booleanSpec("main_menu", "Main Menu", "Themes the title screen when the main-menu flag is also enabled.",
                        EchoConfigSide.CLIENT, THEME_MAIN_MENU, true, false, false),
                    EchoConfigEntry.booleanSpec("inventory", "Inventory", "Adds safe accents to inventory screens.",
                        EchoConfigSide.CLIENT, THEME_INVENTORY, true, false, false),
                    EchoConfigEntry.booleanSpec("containers", "Containers", "Adds safe accents around container panels.",
                        EchoConfigSide.CLIENT, THEME_CONTAINERS, true, false, false),
                    EchoConfigEntry.booleanSpec("hotbar", "Hotbar", "Adds themed hotbar accents.",
                        EchoConfigSide.CLIENT, THEME_HOTBAR, true, false, false),
                    EchoConfigEntry.booleanSpec("chat", "Chat", "Adds a chat accent when enabled.",
                        EchoConfigSide.CLIENT, THEME_CHAT, true, false, false)))))
        ));
    }
}
