package com.knoxhack.echoterminal.client.screen;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.api.theme.BuiltinTerminalThemes;
import com.knoxhack.echoterminal.api.theme.TerminalTheme;
import com.knoxhack.echoterminal.api.theme.TerminalThemeRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * Client-only terminal presentation defaults. These values are intentionally
 * additive and do not affect server-side mission or action authority.
 */
public final class TerminalClientOptions {
    public static NavigationStyle navigationStyle = NavigationStyle.APP_HUB;
    public static MissionView missionView = MissionView.VISUAL_QUEST_HUB;
    public static InterfaceDensity interfaceDensity = InterfaceDensity.BALANCED;
    public static TerminalZoom terminalZoom = TerminalZoom.ZOOM_100;
    public static VisualLevel visualLevel = VisualLevel.BALANCED;
    public static boolean reducedMotion = false;
    public static boolean missionHudNotifications = true;
    private static final String CONFIG_FILE = "echoterminal-client.properties";
    private static final String THEME_KEY = "theme";
    private static final String NAVIGATION_STYLE_KEY = "navigationStyle";
    private static final String MISSION_VIEW_KEY = "missionView";
    private static final String INTERFACE_DENSITY_KEY = "interfaceDensity";
    private static final String TERMINAL_ZOOM_KEY = "terminalZoom";
    private static final String VISUAL_LEVEL_KEY = "visualLevel";
    private static final String REDUCED_MOTION_KEY = "reducedMotion";
    private static final String MISSION_HUD_NOTIFICATIONS_KEY = "missionHudNotifications";
    private static Identifier selectedTheme = BuiltinTerminalThemes.ECHO_CONSOLE;
    private static boolean loaded;

    private TerminalClientOptions() {
    }

    public static boolean useSidebarHub() {
        return navigationStyle == NavigationStyle.APP_HUB || navigationStyle == NavigationStyle.SIDEBAR_HUB;
    }

    public static boolean useAppHub() {
        return navigationStyle == NavigationStyle.APP_HUB;
    }

    public static boolean useVisualAssets() {
        return visualLevel != VisualLevel.MINIMAL;
    }

    public static boolean reduceMotion() {
        return reducedMotion || visualLevel == VisualLevel.REDUCED_MOTION;
    }

    public static TerminalTheme currentTheme() {
        ensureLoaded();
        return TerminalThemeRegistry.byId(selectedTheme);
    }

    public static InterfaceDensity interfaceDensity() {
        ensureLoaded();
        return interfaceDensity;
    }

    public static TerminalZoom terminalZoom() {
        ensureLoaded();
        return terminalZoom;
    }

    public static Identifier selectedThemeId() {
        ensureLoaded();
        return TerminalThemeRegistry.contains(selectedTheme)
                ? selectedTheme
                : TerminalThemeRegistry.defaultThemeId();
    }

    public static void selectTheme(Identifier themeId) {
        ensureLoaded();
        selectedTheme = TerminalThemeRegistry.contains(themeId)
                ? themeId
                : TerminalThemeRegistry.defaultThemeId();
        save();
    }

    public static void selectNavigationStyle(NavigationStyle style) {
        ensureLoaded();
        navigationStyle = style == null ? NavigationStyle.APP_HUB : style;
        save();
    }

    public static void selectMissionView(MissionView view) {
        ensureLoaded();
        missionView = view == null ? MissionView.VISUAL_QUEST_HUB : view;
        save();
    }

    public static void selectInterfaceDensity(InterfaceDensity density) {
        ensureLoaded();
        interfaceDensity = density == null ? InterfaceDensity.BALANCED : density;
        save();
    }

    public static void selectTerminalZoom(TerminalZoom zoom) {
        ensureLoaded();
        terminalZoom = zoom == null ? TerminalZoom.ZOOM_100 : zoom;
        save();
    }

    public static void selectVisualLevel(VisualLevel level) {
        ensureLoaded();
        visualLevel = level == null ? VisualLevel.BALANCED : level;
        reducedMotion = visualLevel == VisualLevel.REDUCED_MOTION;
        save();
    }

    public static void setReducedMotion(boolean value) {
        ensureLoaded();
        reducedMotion = value;
        if (value && visualLevel != VisualLevel.REDUCED_MOTION) {
            visualLevel = VisualLevel.REDUCED_MOTION;
        } else if (!value && visualLevel == VisualLevel.REDUCED_MOTION) {
            visualLevel = VisualLevel.BALANCED;
        }
        save();
    }

    public static void setMissionHudNotifications(boolean value) {
        ensureLoaded();
        missionHudNotifications = value;
        save();
    }

    public static void cycleTheme(int offset) {
        var ids = TerminalThemeRegistry.ids();
        if (ids.isEmpty()) {
            selectedTheme = TerminalThemeRegistry.defaultThemeId();
            return;
        }
        int index = ids.indexOf(selectedThemeId());
        selectedTheme = ids.get(Math.floorMod((index < 0 ? 0 : index) + offset, ids.size()));
        save();
    }

    public static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path path = configPath();
        if (path == null || !Files.isRegularFile(path)) {
            selectedTheme = TerminalThemeRegistry.defaultThemeId();
            return;
        }
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
            Identifier parsed = Identifier.tryParse(properties.getProperty(THEME_KEY, ""));
            selectedTheme = TerminalThemeRegistry.contains(parsed) ? parsed : TerminalThemeRegistry.defaultThemeId();
            navigationStyle = enumValue(
                    NavigationStyle.class,
                    properties.getProperty(NAVIGATION_STYLE_KEY),
                    NavigationStyle.APP_HUB);
            missionView = enumValue(
                    MissionView.class,
                    properties.getProperty(MISSION_VIEW_KEY),
                    MissionView.VISUAL_QUEST_HUB);
            interfaceDensity = enumValue(
                    InterfaceDensity.class,
                    properties.getProperty(INTERFACE_DENSITY_KEY),
                    InterfaceDensity.BALANCED);
            terminalZoom = enumValue(
                    TerminalZoom.class,
                    properties.getProperty(TERMINAL_ZOOM_KEY),
                    TerminalZoom.ZOOM_100);
            visualLevel = enumValue(
                    VisualLevel.class,
                    properties.getProperty(VISUAL_LEVEL_KEY),
                    VisualLevel.BALANCED);
            reducedMotion = Boolean.parseBoolean(properties.getProperty(REDUCED_MOTION_KEY, "false"))
                    || visualLevel == VisualLevel.REDUCED_MOTION;
            missionHudNotifications = Boolean.parseBoolean(
                    properties.getProperty(MISSION_HUD_NOTIFICATIONS_KEY, "true"));
        } catch (IOException | RuntimeException exception) {
            EchoTerminal.LOGGER.warn("Failed to load ECHO terminal client options; using defaults.", exception);
            selectedTheme = TerminalThemeRegistry.defaultThemeId();
        }
    }

    public static void resetThemeForTests(Identifier themeId) {
        loaded = true;
        selectedTheme = TerminalThemeRegistry.contains(themeId) ? themeId : TerminalThemeRegistry.defaultThemeId();
    }

    private static void ensureLoaded() {
        if (!loaded) {
            load();
        }
    }

    private static void save() {
        Path path = configPath();
        if (path == null) {
            return;
        }
        try {
            Files.createDirectories(path.getParent());
            Properties properties = new Properties();
            properties.setProperty(THEME_KEY, selectedThemeId().toString());
            properties.setProperty(NAVIGATION_STYLE_KEY, navigationStyle.name());
            properties.setProperty(MISSION_VIEW_KEY, missionView.name());
            properties.setProperty(INTERFACE_DENSITY_KEY, interfaceDensity.name());
            properties.setProperty(TERMINAL_ZOOM_KEY, terminalZoom.name());
            properties.setProperty(VISUAL_LEVEL_KEY, visualLevel.name());
            properties.setProperty(REDUCED_MOTION_KEY, Boolean.toString(reducedMotion));
            properties.setProperty(MISSION_HUD_NOTIFICATIONS_KEY, Boolean.toString(missionHudNotifications));
            try (OutputStream out = Files.newOutputStream(path)) {
                properties.store(out, "ECHO Terminal client options");
            }
        } catch (IOException | RuntimeException exception) {
            EchoTerminal.LOGGER.warn("Failed to save ECHO terminal client options.", exception);
        }
    }

    private static Path configPath() {
        try {
            return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(CONFIG_FILE);
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    public enum NavigationStyle {
        APP_HUB,
        SIDEBAR_HUB,
        COMPACT_TOP
    }

    public enum MissionView {
        VISUAL_QUEST_HUB,
        GUIDED,
        VISUAL_RPG,
        MINIMAL
    }

    public enum InterfaceDensity {
        COMFORTABLE,
        BALANCED,
        COMPACT;

        public int compactness() {
            return switch (this) {
                case COMFORTABLE -> 0;
                case BALANCED -> 1;
                case COMPACT -> 2;
            };
        }
    }

    public enum TerminalZoom {
        ZOOM_50(50),
        ZOOM_75(75),
        ZOOM_85(85),
        ZOOM_90(90),
        ZOOM_100(100),
        ZOOM_110(110),
        ZOOM_125(125),
        ZOOM_150(150);

        private final int percent;

        TerminalZoom(int percent) {
            this.percent = percent;
        }

        public int percent() {
            return percent;
        }

        public double scale() {
            return percent / 100.0D;
        }

        public String label() {
            return percent + "%";
        }
    }

    public enum VisualLevel {
        BALANCED,
        MINIMAL,
        REDUCED_MOTION
    }
}
