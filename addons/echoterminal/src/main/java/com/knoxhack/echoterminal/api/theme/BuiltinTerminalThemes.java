package com.knoxhack.echoterminal.api.theme;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.api.TerminalTabChrome;
import com.knoxhack.echoterminal.api.TerminalVisualAssets;
import net.minecraft.resources.Identifier;

public final class BuiltinTerminalThemes {
    public static final Identifier ECHO_CONSOLE =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "echo_console");
    public static final Identifier NEXUS_MODPACK =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "nexus_modpack");

    private BuiltinTerminalThemes() {
    }

    static void register() {
        TerminalThemeRegistry.registerBuiltin(echoConsole());
        TerminalThemeRegistry.registerBuiltin(nexusModpack());
    }

    public static TerminalTheme echoConsole() {
        TerminalThemeTokens tokens = new TerminalThemeTokens(
                new TerminalThemeTokens.Colors(
                        0xFF02070C, 0xEE050B10, 0xDD071017, 0x6610242F, 0xB6050D14,
                        0xFF0D171F, 0xFF123241, 0xFFE9FBFF, 0xFF8CA7B5,
                        0xFF66E8FF, 0xFF2E8E9D, 0xFF92F7A6, 0xFFFFD166,
                        0xFFFF8FA3, 0xFF9FD1FF),
                TerminalThemeTokens.Typography.defaults(),
                TerminalThemeTokens.Panels.defaults(),
                TerminalThemeTokens.Borders.defaults(),
                TerminalThemeTokens.Prompt.defaults(),
                TerminalThemeTokens.Output.defaults(),
                TerminalThemeTokens.States.defaults(),
                TerminalThemeTokens.Dividers.defaults(),
                TerminalThemeTokens.Effects.defaults(),
                new TerminalThemeTokens.Assets(
                        TerminalVisualAssets.TERMINAL_FRAME_BACKDROP,
                        TerminalVisualAssets.CARD_PANEL_DETAIL_STANDARD,
                        TerminalVisualAssets.CARD_METRIC_TILE_PLATE,
                        TerminalVisualAssets.CARD_PANEL_LIST_COMPACT,
                        null,
                        null,
                        TerminalVisualAssets.MISSIONS_VISUAL_HERO,
                        TerminalVisualAssets.OVERVIEW_PROTOCOL_DASHBOARD,
                        null));
        return TerminalTheme.builder(ECHO_CONSOLE, "ECHO Console")
                .tokens(tokens)
                .icons(defaultIcons())
                .fallbackChapterStyle(TerminalChapterStyle.builder("echo", "ECHO")
                        .colors(tokens.colors().accent(), tokens.colors().accentDim())
                        .banner(TerminalVisualAssets.OVERVIEW_PROTOCOL_DASHBOARD)
                        .panel(TerminalVisualAssets.CARD_PANEL_DETAIL_STANDARD)
                        .icons(defaultIcons())
                        .build())
                .build();
    }

    public static TerminalTheme nexusModpack() {
        TerminalThemeTokens tokens = new TerminalThemeTokens(
                new TerminalThemeTokens.Colors(
                        0xFF060706, 0xF20B0E0D, 0xE30B1210, 0x76301F18, 0xC90A0B09,
                        0xFF171B15, 0xFF25351F, 0xFFFFF4D8, 0xFFC9BFA6,
                        0xFFFFC85A, 0xFF7F6E42, 0xFFA7F06C, 0xFFFFC85A,
                        0xFFFF6E6E, 0xFF74D9FF),
                new TerminalThemeTokens.Typography(false, 11, 9, 14),
                new TerminalThemeTokens.Panels(0x7A2A1D16, 0xD7080A08, 0xD0161812,
                        0xFF2B3F25, 0xFF27311E, 0xAA17120E, 0x662E2418, 0.58F),
                new TerminalThemeTokens.Borders(0x446D5A37, 0x669B7B3D, 0xC4FFC85A,
                        0xD0FFE08A, 0x555F5A4E, 0x88A66CFF),
                new TerminalThemeTokens.Prompt(0xFFFFC85A, 0xFFFFF4D8, 0xFF74D9FF, 0xFFFF6E6E),
                new TerminalThemeTokens.Output(0xFFFFF4D8, 0xFFC9BFA6, 0xFFA7F06C, 0xFFFFC85A, 0xFFFF6E6E),
                new TerminalThemeTokens.States(0xFFA7F06C, 0xFFFFC85A, 0xFF74D9FF,
                        0xFF8D8475, 0xFFFFC85A, 0xFFA7F06C, 0xFFFF6E6E),
                new TerminalThemeTokens.Dividers(0x665F4A2E, 0xFFFFC85A, 0x66A66CFF, 0x184F8A5C),
                new TerminalThemeTokens.Effects(true, true, true, 0xD0060706, 0x66A66CFF),
                new TerminalThemeTokens.Assets(
                        nexus("backgrounds/terminal_backdrop"),
                        nexus("panels/panel_plate"),
                        nexus("panels/panel_selected"),
                        nexus("panels/panel_hover"),
                        nexus("borders/divider_rune"),
                        nexus("borders/prompt_ornament"),
                        nexus("backgrounds/loading_portal"),
                        nexus("banners/nexus_modpack"),
                        nexus("borders/panel_border")));
        return TerminalTheme.builder(NEXUS_MODPACK, "Nexus Modpack")
                .tokens(tokens)
                .icons(nexusIcons())
                .fallbackChapterStyle(TerminalChapterStyle.builder("nexus_modpack", "Nexus Modpack")
                        .colors(tokens.colors().accent(), tokens.colors().info())
                        .banner(nexus("banners/nexus_modpack"))
                        .panel(nexus("panels/panel_plate"))
                        .border(nexus("borders/panel_border"))
                        .icons(nexusIcons())
                        .build())
                .chapterStyle(style("minecraft", "Baseline", 0xFF8FE36B, 0xFFB8874A,
                        "banners/baseline", "icons/chapter_baseline"))
                .chapterStyle(style("echoashfallprotocol", "Ashfall", 0xFFFF9A5A, 0xFFFF6E6E,
                        "banners/ashfall", "icons/chapter_ashfall"))
                .chapterStyle(style("echoindustrialnexus", "Industrial Nexus", 0xFFFFC85A, 0xFF74D9FF,
                        "banners/industrial", "icons/chapter_industrial"))
                .chapterStyle(style("echonexusprotocol", "Nexus Protocol", 0xFFA66CFF, 0xFF74D9FF,
                        "banners/nexus_protocol", "icons/chapter_nexus_protocol"))
                .chapterStyle(style("echoorbitalremnants", "Orbital Remnants", 0xFF74D9FF, 0xFFA66CFF,
                        "banners/orbital", "icons/chapter_orbital"))
                .chapterStyle(style("echostationfall", "Stationfall", 0xFFFF6E6E, 0xFFFFC85A,
                        "banners/stationfall", "icons/chapter_stationfall"))
                .chapterStyle(style("echoblackboxprotocol", "Blackbox Protocol", 0xFFC6A8FF, 0xFF8D8475,
                        "banners/blackbox", "icons/chapter_blackbox"))
                .build();
    }

    public static TerminalIconSet defaultIcons() {
        return TerminalIconSet.builder()
                .fallback(TerminalVisualAssets.ICON_BRAND_ECHO)
                .icon(TerminalIconKey.group(TerminalTabChrome.GROUP_PROTOCOL), TerminalVisualAssets.ICON_GROUP_PROTOCOL)
                .icon(TerminalIconKey.group(TerminalTabChrome.GROUP_CORE), TerminalVisualAssets.ICON_GROUP_FIELD)
                .icon(TerminalIconKey.group(TerminalTabChrome.GROUP_FIELD), TerminalVisualAssets.ICON_GROUP_FIELD)
                .icon(TerminalIconKey.group(TerminalTabChrome.GROUP_SYSTEMS), TerminalVisualAssets.ICON_GROUP_SYSTEMS)
                .icon(TerminalIconKey.group(TerminalTabChrome.GROUP_NEXUS), TerminalVisualAssets.ICON_GROUP_NEXUS)
                .icon(TerminalIconKey.group(TerminalTabChrome.GROUP_ENDGAME), TerminalVisualAssets.ICON_GROUP_NEXUS)
                .icon(TerminalIconKey.group(TerminalTabChrome.GROUP_ORBITAL), TerminalVisualAssets.ICON_GROUP_ORBITAL)
                .icon(TerminalIconKey.group(TerminalTabChrome.GROUP_ADDONS), TerminalVisualAssets.ICON_GROUP_CHAPTERS)
                .icon(TerminalIconKey.page("command_deck"), TerminalVisualAssets.ICON_PAGE_COMMAND_DECK)
                .icon(TerminalIconKey.page("survival_route"), TerminalVisualAssets.ICON_PAGE_SURVIVAL_INDEX)
                .icon(TerminalIconKey.page("mission_graph"), TerminalVisualAssets.ICON_PAGE_PROTOCOL_ROADMAP)
                .icon(TerminalIconKey.page("what_now"), TerminalVisualAssets.ICON_PAGE_SIGNAL_LEADS)
                .icon(TerminalIconKey.page("vitals"), TerminalVisualAssets.ICON_PAGE_VITALS_SCAN)
                .icon(TerminalIconKey.page("route_records"), TerminalVisualAssets.ICON_PAGE_ROUTE_MAP)
                .icon(TerminalIconKey.page("factions"), TerminalVisualAssets.ICON_PAGE_ROUTE_MAP)
                .icon(TerminalIconKey.page("reward_inbox"), TerminalVisualAssets.ICON_ACTION_CLAIM)
                .icon(TerminalIconKey.page("field_archive"), TerminalVisualAssets.ICON_PAGE_FIELD_ARCHIVE)
                .icon(TerminalIconKey.page("chapter_guide"), TerminalVisualAssets.ICON_PAGE_CHAPTERS)
                .icon(TerminalIconKey.action("claim"), TerminalVisualAssets.ICON_ACTION_CLAIM)
                .icon(TerminalIconKey.action("turn_in"), TerminalVisualAssets.ICON_ACTION_TURN_IN)
                .icon(TerminalIconKey.action("view"), TerminalVisualAssets.ICON_ACTION_VIEW)
                .icon(TerminalIconKey.action("scan"), TerminalVisualAssets.ICON_ACTION_SCAN)
                .icon(TerminalIconKey.action("open"), TerminalVisualAssets.ICON_ACTION_OPEN_ROADMAP)
                .icon(TerminalIconKey.action("continue"), TerminalVisualAssets.ICON_ACTION_OPEN_ROADMAP)
                .icon(TerminalIconKey.action("review"), TerminalVisualAssets.ICON_ACTION_VIEW)
                .icon(TerminalIconKey.action("resolve"), TerminalVisualAssets.ICON_STATE_NEEDED)
                .icon(TerminalIconKey.action("settings"), TerminalVisualAssets.ICON_GROUP_SYSTEMS)
                .icon(TerminalIconKey.action("theme_cycle"), TerminalVisualAssets.ICON_GROUP_SYSTEMS)
                .icon(TerminalIconKey.state("locked"), TerminalVisualAssets.ICON_STATE_LOCKED)
                .icon(TerminalIconKey.state("active"), TerminalVisualAssets.ICON_STATE_ACTIVE)
                .icon(TerminalIconKey.state("needed"), TerminalVisualAssets.ICON_STATE_NEEDED)
                .icon(TerminalIconKey.state("open"), TerminalVisualAssets.ICON_STATE_OPEN)
                .icon(TerminalIconKey.state("available"), TerminalVisualAssets.ICON_STATE_AVAILABLE)
                .icon(TerminalIconKey.state("online"), TerminalVisualAssets.ICON_STATE_ONLINE)
                .icon(TerminalIconKey.state("claimable"), TerminalVisualAssets.ICON_ACTION_CLAIM)
                .icon(TerminalIconKey.state("complete"), TerminalVisualAssets.ICON_STATE_ONLINE)
                .icon(TerminalIconKey.state("completed"), TerminalVisualAssets.ICON_STATE_ONLINE)
                .icon(TerminalIconKey.state("warning"), TerminalVisualAssets.ICON_STATE_NEEDED)
                .icon(TerminalIconKey.state("blocker"), TerminalVisualAssets.ICON_STATE_LOCKED)
                .icon(TerminalIconKey.state("empty"), TerminalVisualAssets.ICON_BRAND_ECHO)
                .icon(TerminalIconKey.state("unknown"), TerminalVisualAssets.ICON_BRAND_ECHO)
                .icon(TerminalIconKey.missionCategory("survival"), TerminalVisualAssets.MISSION_ICON_SURVIVAL)
                .icon(TerminalIconKey.missionCategory("crafting"), TerminalVisualAssets.MISSION_ICON_CRAFTING)
                .icon(TerminalIconKey.missionCategory("tech"), TerminalVisualAssets.MISSION_ICON_TECH)
                .icon(TerminalIconKey.missionCategory("exploration"), TerminalVisualAssets.MISSION_ICON_EXPLORATION)
                .icon(TerminalIconKey.missionCategory("combat"), TerminalVisualAssets.MISSION_ICON_COMBAT)
                .icon(TerminalIconKey.missionCategory("story"), TerminalVisualAssets.MISSION_ICON_STORY)
                .icon(TerminalIconKey.missionCategory("side_ops"), TerminalVisualAssets.MISSION_ICON_SIDE_OPS)
                .icon(TerminalIconKey.missionCategory("hazard"), TerminalVisualAssets.MISSION_ICON_HAZARD)
                .icon(TerminalIconKey.reward("cache"), TerminalVisualAssets.ICON_ACTION_CLAIM)
                .icon(TerminalIconKey.reward("inbox"), TerminalVisualAssets.ICON_ACTION_CLAIM)
                .icon(TerminalIconKey.theme("brand"), TerminalVisualAssets.ICON_BRAND_ECHO)
                .icon(TerminalIconKey.theme("settings"), TerminalVisualAssets.ICON_GROUP_SYSTEMS)
                .icon(TerminalIconKey.theme("cycle"), TerminalVisualAssets.ICON_GROUP_SYSTEMS)
                .icon(TerminalIconKey.fallback("unknown"), TerminalVisualAssets.ICON_BRAND_ECHO)
                .icon(TerminalIconKey.fallback("state"), TerminalVisualAssets.ICON_BRAND_ECHO)
                .icon(TerminalIconKey.fallback("action"), TerminalVisualAssets.ICON_ACTION_VIEW)
                .icon(TerminalIconKey.fallback("page"), TerminalVisualAssets.ICON_PAGE_COMMAND_DECK)
                .build();
    }

    private static TerminalIconSet nexusIcons() {
        TerminalIconSet.Builder builder = TerminalIconSet.builder()
                .fallback(nexus("icons/fallback_unknown"));
        for (TerminalIconKey key : defaultIcons().icons().keySet()) {
            builder.icon(key, nexus("icons/" + key.category() + "_" + key.name().replace(':', '_').replace('/', '_')));
        }
        builder.icon(TerminalIconKey.chapter("minecraft"), nexus("icons/chapter_baseline"))
                .icon(TerminalIconKey.chapter("echoashfallprotocol"), nexus("icons/chapter_ashfall"))
                .icon(TerminalIconKey.chapter("echoindustrialnexus"), nexus("icons/chapter_industrial"))
                .icon(TerminalIconKey.chapter("echonexusprotocol"), nexus("icons/chapter_nexus_protocol"))
                .icon(TerminalIconKey.chapter("echoorbitalremnants"), nexus("icons/chapter_orbital"))
                .icon(TerminalIconKey.chapter("echostationfall"), nexus("icons/chapter_stationfall"))
                .icon(TerminalIconKey.chapter("echoblackboxprotocol"), nexus("icons/chapter_blackbox"));
        return builder.build();
    }

    private static TerminalChapterStyle style(String key, String displayName, int accent, int secondary,
            String banner, String chapterIcon) {
        TerminalIconSet icons = TerminalIconSet.builder()
                .icon(TerminalIconKey.chapter(key), nexus(chapterIcon))
                .build();
        return TerminalChapterStyle.builder(key, displayName)
                .colors(accent, secondary)
                .banner(nexus(banner))
                .panel(nexus("panels/panel_" + key))
                .border(nexus("borders/panel_border"))
                .icons(icons)
                .build();
    }

    private static Identifier nexus(String path) {
        return Identifier.fromNamespaceAndPath(EchoTerminal.MODID,
                "textures/gui/themes/nexus_modpack/" + path + ".png");
    }
}
