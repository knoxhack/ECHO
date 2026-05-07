# ECHO: Terminal

ECHO: Terminal is the shared command surface for the Echo stack. It provides the terminal block, menu, client screen, mission provider API, action routing, navigation sections, archive records, rewards inbox, diagnostics, route records, faction atlas, vitals telemetry, and addon chapter hub.

## Beta Feature Set

- Command Deck summary with mode, chapter, route, blocker, and reward counts.
- What Now diagnostics for the next actionable blocker or release condition.
- Survival Route tab that aggregates vanilla progression and installed ECHO chapter missions into one guided route.
- Mission browser with visual/guided/minimal views, filters, search, scrollable detail panes, keyboard navigation, and claim/action routing.
- Baseline vanilla journey rewards route support caches through the Reward Inbox on an owned terminal.
- Shared Reward Inbox for support caches exposed through Echo Core.
- Addon Chapters hub, Route Records, Faction Atlas, Vitals, Mission Graph, and Field Archive tabs.
- Configurable client key binding for opening the terminal.

## Controls

- Open terminal: `M` by default, configurable in Minecraft Controls under `ECHO: Terminal`.
- Close terminal: `Esc` or the configured terminal key.
- Navigate terminal sections/pages: arrow keys or WASD.
- Mission browser: type to search, `Ctrl+F` focuses search, left/right cycles filters, up/down changes selected mission, `Enter` or `Space` activates the first enabled mission command.
- Scroll long content with mouse wheel or Page Up/Page Down.

## Addon Integration

Addons should register `TerminalMissionProvider` implementations for mission records and use `TerminalMissionActions.registerForTab` when a tab needs server-authoritative mission commands. Addons can also expose archive entries, navigation profiles, chapter metadata, route records, faction profiles, diagnostics, hazard telemetry, and pending rewards through Echo Core services.

For beta stability, mission providers should tolerate missing players, return empty lists instead of throwing when content is unavailable, and keep action handlers authoritative on the server.

## Terminal Themes

Echo Terminal ships a Java-only theme engine for beta. Themes are registered through `TerminalThemeRegistry` and are client presentation only: they do not change packets, world data, mission authority, or reward behavior.

Built-in themes:

- `echoterminal:echo_console` keeps the current cyan ECHO terminal style as a tokenized default.
- `echoterminal:nexus_modpack` adds an ECHO/Nexus/modded-Minecraft questbook style with chapter overlays, HD panels, banners, borders, and semantic icons.

Themes are selected from Command Deck through the compact `Theme` command or the `Appearance` selector. The selected theme persists in the local Minecraft config file `config/echoterminal-client.properties`.

Theme authors should provide:

- `TerminalThemeTokens` for colors, typography, panels, borders, prompts, output text, states, dividers, effects, and core assets.
- `TerminalIconSet` entries for semantic keys such as navigation groups, pages, actions, status states, rewards, blockers, mission categories, chapters, settings, locked, empty, and unknown.
- `TerminalChapterStyle` overrides for chapter namespaces when a theme needs biome, dimension, magic, tech, archive, or hazard flavor.

Register addon themes during client setup before any terminal screen opens. Theme ids must be lowercase resource identifiers; duplicate ids are rejected, missing selected themes fall back to `echoterminal:echo_console`, and missing icon keys fall back through the theme icon set before using ECHO's built-in vector glyphs.

Example:

```java
TerminalThemeRegistry.register(TerminalTheme.builder(id("my_theme"), "My Theme")
        .tokens(myTokens)
        .icons(TerminalIconSet.builder()
                .icon(TerminalIconKey.action("claim"), id("textures/gui/theme/icons/action_claim.png"))
                .icon(TerminalIconKey.chapter("myaddon"), id("textures/gui/theme/icons/chapter_myaddon.png"))
                .build())
        .chapterStyle(TerminalChapterStyle.builder("myaddon", "My Chapter")
                .colors(0xFFB6F06C, 0xFF77DDEE)
                .banner(id("textures/gui/theme/banners/my_chapter.png"))
                .build())
        .build());
```

Generated or hand-authored assets should avoid baked-in text. Labels are rendered by code for localization, readability, scaling, and accessibility.

## Beta Limitations

- The terminal is primarily a client command interface; it does not pin objectives to the HUD yet.
- Most chapter-specific behavior is provided by the owning addon. If an addon is missing or disabled, the terminal shows only the providers that are registered.
- Visual assets can be reduced through `TerminalClientOptions`, but there is not yet an in-game options screen for every terminal presentation setting beyond theme selection.
- `TerminalBadgeProvider` and `TerminalNotificationProvider` are deferred beta APIs until badges and notification chrome are fully wired.
- Baseline cache claims require a placed or recently opened owned terminal so the Reward Inbox has a storage target.
- Save compatibility is expected for normal beta use, but fresh-world smoke testing is recommended before public release candidates.

## Release Checklist

- Run `.\gradlew.bat :echoterminal:runGameTestServer --warning-mode all`.
- Run the workspace release verification after packaging paths are aligned.
- Smoke test block opening, keybind opening, every tab, narrow UI widths, mission search/filter, vanilla reward claim, no-addon install, and full-addon install.
