package com.knoxhack.echothemecore.client.vanilla;

import com.knoxhack.echothemecore.config.ThemeCoreConfig;
import net.minecraft.client.gui.screens.Screen;

public final class VanillaUiScreenClassifier {
    private VanillaUiScreenClassifier() {
    }

    public static VanillaUiSurface classify(Screen screen) {
        if (screen == null) {
            return VanillaUiSurface.UNKNOWN;
        }
        String name = screen.getClass().getName();
        if (name.endsWith(".TitleScreen")) {
            return VanillaUiSurface.MAIN_MENU;
        }
        if (name.endsWith(".PauseScreen")) {
            return VanillaUiSurface.PAUSE_MENU;
        }
        if (containsAny(name, "OptionsScreen", "VideoSettingsScreen", "ControlsScreen", "AccessibilityOptionsScreen", "OnlineOptionsScreen", "LanguageSelectScreen", "SoundOptionsScreen")) {
            return VanillaUiSurface.OPTIONS_MENU;
        }
        if (containsAny(name, "SelectWorldScreen", "CreateWorldScreen", "EditWorldScreen")) {
            return VanillaUiSurface.WORLD_SELECT;
        }
        if (containsAny(name, "JoinMultiplayerScreen", "ServerSelectionList", "DirectJoinServerScreen", "EditServerScreen")) {
            return VanillaUiSurface.MULTIPLAYER;
        }
        if (containsAny(name, "LevelLoadingScreen", "ReceivingLevelScreen", "ProgressScreen")) {
            return VanillaUiSurface.LOADING;
        }
        if (name.endsWith(".InventoryScreen")) {
            return VanillaUiSurface.INVENTORY;
        }
        if (name.endsWith(".CreativeModeInventoryScreen")) {
            return VanillaUiSurface.CREATIVE_INVENTORY;
        }
        if (containsAny(name, "FurnaceScreen", "BlastFurnaceScreen", "SmokerScreen")) {
            return VanillaUiSurface.FURNACE;
        }
        if (containsAny(name, "CraftingScreen")) {
            return VanillaUiSurface.CRAFTING;
        }
        if (containsAny(name, "AnvilScreen")) {
            return VanillaUiSurface.ANVIL;
        }
        if (containsAny(name, "EnchantmentScreen")) {
            return VanillaUiSurface.ENCHANTING;
        }
        if (containsAny(name, "GrindstoneScreen")) {
            return VanillaUiSurface.GRINDSTONE;
        }
        if (containsAny(name, "SmithingScreen")) {
            return VanillaUiSurface.SMITHING;
        }
        if (containsAny(name, "AdvancementsScreen")) {
            return VanillaUiSurface.ADVANCEMENTS;
        }
        if (containsAny(name, "RecipeBook")) {
            return VanillaUiSurface.RECIPE_BOOK;
        }
        if (containsAny(name, "ContainerScreen", "ChestScreen", "ShulkerBoxScreen", "DispenserScreen", "HopperScreen", "BrewingStandScreen", "BeaconScreen", "AbstractContainerScreen")) {
            return VanillaUiSurface.CONTAINER;
        }
        return VanillaUiSurface.UNKNOWN;
    }

    public static boolean enabled(VanillaUiSurface surface) {
        if (!ThemeCoreConfig.vanillaUiEnabled()) {
            return false;
        }
        return switch (surface) {
            case MAIN_MENU -> ThemeCoreConfig.bool(ThemeCoreConfig.THEME_MAIN_MENU) && ThemeCoreConfig.bool(ThemeCoreConfig.THEME_AFFECTS_MAIN_MENU);
            case PAUSE_MENU -> ThemeCoreConfig.bool(ThemeCoreConfig.THEME_PAUSE_MENU);
            case OPTIONS_MENU -> ThemeCoreConfig.bool(ThemeCoreConfig.THEME_OPTIONS_MENU);
            case WORLD_SELECT, MULTIPLAYER -> ThemeCoreConfig.bool(ThemeCoreConfig.THEME_WORLD_SELECT);
            case LOADING -> ThemeCoreConfig.bool(ThemeCoreConfig.THEME_LOADING_SCREEN);
            case INVENTORY -> ThemeCoreConfig.bool(ThemeCoreConfig.THEME_INVENTORY);
            case CREATIVE_INVENTORY -> ThemeCoreConfig.bool(ThemeCoreConfig.THEME_CREATIVE_INVENTORY);
            case CONTAINER, FURNACE, CRAFTING, ANVIL, ENCHANTING, GRINDSTONE, SMITHING -> ThemeCoreConfig.bool(ThemeCoreConfig.THEME_CONTAINERS);
            case ADVANCEMENTS -> ThemeCoreConfig.bool(ThemeCoreConfig.THEME_ADVANCEMENTS);
            case RECIPE_BOOK -> ThemeCoreConfig.bool(ThemeCoreConfig.THEME_RECIPE_BOOK);
            case UNKNOWN -> !ThemeCoreConfig.disableUnknownScreens();
        };
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
