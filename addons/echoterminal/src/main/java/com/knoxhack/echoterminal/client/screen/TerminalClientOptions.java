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
    public static VisualLevel visualLevel = VisualLevel.BALANCED;
    public static boolean reducedMotion = false;
    private static final String CONFIG_FILE = "echoterminal-client.properties";
    private static final String THEME_KEY = "theme";
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

    public enum VisualLevel {
        BALANCED,
        MINIMAL,
        REDUCED_MOTION
    }
}
