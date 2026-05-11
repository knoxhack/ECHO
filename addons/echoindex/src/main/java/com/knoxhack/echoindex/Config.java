package com.knoxhack.echoindex;

import com.knoxhack.echocore.api.config.EchoConfigCategory;
import com.knoxhack.echocore.api.config.EchoConfigEntry;
import com.knoxhack.echocore.api.config.EchoConfigModule;
import com.knoxhack.echocore.api.config.EchoConfigProvider;
import com.knoxhack.echocore.api.config.EchoConfigRegistry;
import com.knoxhack.echocore.api.config.EchoConfigSide;
import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue OVERLAY_ENABLED;
    public static final ModConfigSpec.EnumValue<OverlaySide> OVERLAY_SIDE;
    public static final ModConfigSpec.EnumValue<OverlayLayout> OVERLAY_LAYOUT;
    public static final ModConfigSpec.IntValue OVERLAY_WIDTH;
    public static final ModConfigSpec.IntValue OVERLAY_MAX_COLUMNS;
    public static final ModConfigSpec.BooleanValue OVERLAY_SHOW_BOOKMARKS;
    public static final ModConfigSpec.BooleanValue SEARCH_TOOLTIP_SEARCH;
    public static final ModConfigSpec.BooleanValue SEARCH_TAG_SEARCH;
    public static final ModConfigSpec.BooleanValue SEARCH_REGISTRY_SEARCH;
    public static final ModConfigSpec.BooleanValue UI_CINEMATIC_STYLE;
    public static final ModConfigSpec.BooleanValue DISCOVERY_ENABLED;
    public static final ModConfigSpec.BooleanValue DISCOVERY_HIDE_LOCKED;
    public static final ModConfigSpec.BooleanValue DISCOVERY_SHOW_LOCKED_HINTS;
    public static final ModConfigSpec.BooleanValue RECIPES_SHOW_ALL;
    public static final ModConfigSpec.BooleanValue RECIPES_REQUIRE_DISCOVERY;
    public static final ModConfigSpec.BooleanValue DEBUG_SHOW_RECIPE_IDS;
    public static final ModConfigSpec.BooleanValue SEARCH_CACHE_ENABLED;
    public static final ModConfigSpec.IntValue SEARCH_MAX_RESULTS;
    public static final ModConfigSpec.IntValue UI_MAX_RENDERED_ITEMS;
    public static final ModConfigSpec.BooleanValue DEBUG_COMMANDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("overlay");
        OVERLAY_ENABLED = builder.define("enabled", true);
        OVERLAY_SIDE = builder.defineEnum("side", OverlaySide.RIGHT);
        OVERLAY_LAYOUT = builder.defineEnum("layout", OverlayLayout.COMPACT);
        OVERLAY_WIDTH = builder.defineInRange("width", 238, 160, 420);
        OVERLAY_MAX_COLUMNS = builder.defineInRange("max_columns", 9, 3, 14);
        OVERLAY_SHOW_BOOKMARKS = builder.define("show_bookmarks", true);
        builder.pop();
        builder.push("search");
        SEARCH_TOOLTIP_SEARCH = builder.define("tooltip_search", false);
        SEARCH_TAG_SEARCH = builder.define("tag_search", true);
        SEARCH_REGISTRY_SEARCH = builder.define("registry_search", true);
        SEARCH_CACHE_ENABLED = builder.define("cache_enabled", true);
        SEARCH_MAX_RESULTS = builder.defineInRange("max_results", 512, 32, 4096);
        builder.pop();
        builder.push("ui");
        UI_CINEMATIC_STYLE = builder.define("cinematic_style", true);
        UI_MAX_RENDERED_ITEMS = builder.defineInRange("max_rendered_items", 240, 36, 800);
        builder.pop();
        builder.push("discovery");
        DISCOVERY_ENABLED = builder.define("enabled", false);
        DISCOVERY_HIDE_LOCKED = builder.define("hide_locked", false);
        DISCOVERY_SHOW_LOCKED_HINTS = builder.define("show_locked_hints", true);
        builder.pop();
        builder.push("recipes");
        RECIPES_SHOW_ALL = builder.define("show_all", true);
        RECIPES_REQUIRE_DISCOVERY = builder.define("require_discovery", false);
        builder.pop();
        builder.push("debug");
        DEBUG_SHOW_RECIPE_IDS = builder.define("show_recipe_ids", false);
        DEBUG_COMMANDS = builder.define("commands", true);
        builder.pop();
        SPEC = builder.build();
    }

    private Config() {
    }

    public static void registerEchoConfig() {
        EchoConfigRegistry.register(EchoConfigProvider.of(EchoIndex.MODID, () -> new EchoConfigModule(
                EchoIndex.MODID,
                "Index",
                List.of(
                        new EchoConfigCategory("overlay", "Overlay", List.of(
                                EchoConfigEntry.booleanSpec("overlay_enabled", "Overlay Enabled", "",
                                        EchoConfigSide.COMMON, OVERLAY_ENABLED, true, false, false),
                                EchoConfigEntry.enumSpec("overlay_side", "Overlay Side", "",
                                        EchoConfigSide.COMMON, OVERLAY_SIDE, OverlaySide.class,
                                        true, false, false),
                                EchoConfigEntry.enumSpec("overlay_layout", "Overlay Layout", "",
                                        EchoConfigSide.COMMON, OVERLAY_LAYOUT, OverlayLayout.class,
                                        true, false, false),
                                EchoConfigEntry.intSpec("overlay_width", "Overlay Width", "",
                                        EchoConfigSide.COMMON, OVERLAY_WIDTH, 160, 420,
                                        true, false, false),
                                EchoConfigEntry.intSpec("overlay_columns", "Overlay Columns", "",
                                        EchoConfigSide.COMMON, OVERLAY_MAX_COLUMNS, 3, 14,
                                        true, false, false),
                                EchoConfigEntry.booleanSpec("show_bookmarks", "Show Bookmarks", "",
                                        EchoConfigSide.COMMON, OVERLAY_SHOW_BOOKMARKS, true, false, false))),
                        new EchoConfigCategory("search", "Search", List.of(
                                EchoConfigEntry.booleanSpec("tooltip_search", "Tooltip Search", "",
                                        EchoConfigSide.COMMON, SEARCH_TOOLTIP_SEARCH, true, false, false),
                                EchoConfigEntry.booleanSpec("tag_search", "Tag Search", "",
                                        EchoConfigSide.COMMON, SEARCH_TAG_SEARCH, true, false, false),
                                EchoConfigEntry.booleanSpec("registry_search", "Registry Search", "",
                                        EchoConfigSide.COMMON, SEARCH_REGISTRY_SEARCH, true, false, false),
                                EchoConfigEntry.booleanSpec("cache_enabled", "Search Cache", "",
                                        EchoConfigSide.COMMON, SEARCH_CACHE_ENABLED, true, false, false),
                                EchoConfigEntry.intSpec("max_results", "Max Results", "",
                                        EchoConfigSide.COMMON, SEARCH_MAX_RESULTS, 32, 4096,
                                        true, false, false))),
                        new EchoConfigCategory("ui", "UI", List.of(
                                EchoConfigEntry.booleanSpec("cinematic_style", "Cinematic Style", "",
                                        EchoConfigSide.COMMON, UI_CINEMATIC_STYLE, true, false, false),
                                EchoConfigEntry.intSpec("max_rendered_items", "Max Rendered Items", "",
                                        EchoConfigSide.COMMON, UI_MAX_RENDERED_ITEMS, 36, 800,
                                        true, false, false))),
                        new EchoConfigCategory("discovery", "Discovery", List.of(
                                EchoConfigEntry.booleanSpec("discovery_enabled", "Discovery Enabled", "",
                                        EchoConfigSide.COMMON, DISCOVERY_ENABLED, true, false, false),
                                EchoConfigEntry.booleanSpec("hide_locked", "Hide Locked", "",
                                        EchoConfigSide.COMMON, DISCOVERY_HIDE_LOCKED, true, false, false),
                                EchoConfigEntry.booleanSpec("show_locked_hints", "Show Locked Hints", "",
                                        EchoConfigSide.COMMON, DISCOVERY_SHOW_LOCKED_HINTS, true, false, false))),
                        new EchoConfigCategory("recipes", "Recipes", List.of(
                                EchoConfigEntry.booleanSpec("show_all_recipes", "Show All Recipes", "",
                                        EchoConfigSide.COMMON, RECIPES_SHOW_ALL, true, false, false),
                                EchoConfigEntry.booleanSpec("require_discovery", "Require Discovery", "",
                                        EchoConfigSide.COMMON, RECIPES_REQUIRE_DISCOVERY, true, false, false)))))));
    }

    public enum OverlaySide {
        LEFT,
        RIGHT
    }

    public enum OverlayLayout {
        COMPACT,
        TALL
    }
}
