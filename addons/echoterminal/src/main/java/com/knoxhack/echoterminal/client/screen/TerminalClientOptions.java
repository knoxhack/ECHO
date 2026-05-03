package com.knoxhack.echoterminal.client.screen;

/**
 * Client-only terminal presentation defaults. These values are intentionally
 * additive and do not affect server-side mission or action authority.
 */
public final class TerminalClientOptions {
    public static NavigationStyle navigationStyle = NavigationStyle.APP_HUB;
    public static MissionView missionView = MissionView.VISUAL_QUEST_HUB;
    public static VisualLevel visualLevel = VisualLevel.BALANCED;
    public static boolean reducedMotion = false;

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
