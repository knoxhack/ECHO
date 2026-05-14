<!-- CURSEFORGE_README_START -->
# ECHO: Terminal

![ECHO: Terminal banner](docs/curseforge/echoterminal-banner.png)

**The shared in-world command surface for missions, records, recipes, diagnostics, rewards, and addon chapters.**

![ECHO: Terminal feature overview](docs/curseforge/echoterminal-features.png)

## CurseForge Summary

Shared ECHO terminal block, UI shell, mission browser, archive surface, recipe index, action routing, and addon integration hub.

## Overview

ECHO: Terminal is the player-facing command surface for the modular ECHO stack. It adds the ECHO Terminal block and screen, then gives installed chapters a common place to publish missions, records, diagnostics, route status, recipe references, rewards, actions, vitals, faction reports, and chapter pages.

The terminal is organized around Command, Progress, Intel, and System sections. It can show a Command Deck, What Now guidance, Survival Route, Mission Graph, Mission Browser, Recipe Index, Field Archive, Route Records, Faction Atlas, Vitals, Reward Inbox, settings, and addon hubs.

For players, Terminal answers the practical question: what should I stabilize, craft, scan, repair, or explore next? For addon authors, it is the shared presentation and action contract that keeps the ECHO stack from becoming separate disconnected screens.

## Main Features

- ECHO Terminal block, menu, client screen, and configurable key binding.
- Command, Progress, Intel, and System shell with navigation profiles.
- Mission browser with filters, search, detail panes, keyboard navigation, reward claims, and server-authoritative actions.
- Recipe Index with ECHO item search, recipes/uses modes, provider categories, item details, machine/input/catalyst/output slots, process time, and locked hints.
- Field Archive, Route Records, Faction Atlas, Vitals, Reward Inbox, diagnostics, addon chapters, and transient mission HUD notices.
- Theme engine with Echo Console and Nexus Modpack presentation styles.

## How It Plays

- Craft or place the ECHO Terminal, open it from the block or keybind, then use Command Deck and What Now for immediate direction. Progress and Intel pages handle route detail, records, recipes, factions, and chapter context.
- As more ECHO addons are installed, the terminal expands with their pages and providers without replacing the shared shell.

## Requirements

- Minecraft 26.1.2
- NeoForge 26.1.2.29-beta or newer
- Java 25+
- ECHO: Core 1.0.0 or newer
- ECHO: NetCore 1.0.0 or newer

## Recommended Pairings

- All gameplay ECHO chapters
- ECHO: Index and JEI for recipe-heavy packs

## Compatibility Notes

- If an addon is missing, Terminal only shows registered providers that exist.
- Mission and recipe authority stays with owning addons.

## CurseForge Asset Files

- Banner: `docs/curseforge/echoterminal-banner.png`
- Feature image: `docs/curseforge/echoterminal-features.png`

<!-- CURSEFORGE_README_END -->
---

## Existing Developer Notes

# ECHO: Terminal

ECHO: Terminal is the shared command surface for the Echo stack. It provides the terminal block, menu, client screen, mission provider API, action routing, navigation sections, archive records, rewards inbox, diagnostics, route records, faction atlas, vitals telemetry, and addon chapter hub.

## Release Feature Set

- Four-section command shell: Command, Progress, Intel, and System.
- Command Deck summary with mode, chapter, route, blocker, and reward counts.
- What Now diagnostics for the next actionable blocker or release condition.
- Survival Route tab that aggregates vanilla progression and installed ECHO chapter missions into one guided route.
- Mission browser with visual/guided/minimal views, filters, search, scrollable detail panes, keyboard navigation, and claim/action routing.
- Provider-backed transient mission HUD notices for newly available missions, ready objectives, reward caches, claimed caches, and newly online phases.
- Recipe Index with searchable ECHO items, Recipes/Uses modes, provider category filters, item detail panes, machine/input/catalyst/output slots, process time, notes, and locked schematic hints.
- Baseline vanilla journey rewards route support caches through the Reward Inbox on an owned terminal.
- Shared Reward Inbox for support caches exposed through Echo Core.
- Addon Chapters hub, Route Records, Faction Atlas, Vitals, Mission Graph, and Field Archive tabs.
- Configurable client key binding for opening the terminal.

## Controls

- Open terminal: `M` by default, configurable in Minecraft Controls under `ECHO: Terminal`.
- Close terminal: `Esc` or the configured terminal key.
- Navigate terminal sections/pages: arrow keys or WASD.
- Mission browser: type to search, `Ctrl+F` focuses search, left/right cycles filters, up/down changes selected mission, `Enter` or `Space` activates the first enabled mission command.
- Recipe Index: type to search, click an ECHO item, switch Recipes/Uses, choose category chips, right-click an item slot to inspect uses, and scroll item/result/detail panes independently.
- Mission HUD notices: enabled by default and toggleable from Interface Settings as `MISSION HUD`.
- Scroll long content with mouse wheel or Page Up/Page Down.

## Addon Integration

Addons should register `TerminalMissionProvider` implementations for mission records and use `TerminalMissionActions.registerForTab` when a tab needs server-authoritative mission commands. Addons can also expose archive entries, navigation profiles, chapter metadata, route records, faction profiles, diagnostics, hazard telemetry, and pending rewards through Echo Core services.

Registered mission providers also feed the client mission HUD notification controller. Providers do not need a separate packet or notification API: the controller polls synced provider snapshots, skips the aggregate Survival Route provider to avoid duplicates, and shows transient cards for status transitions while leaving persistent objective pinning to chapter HUDs.

Explicit `TerminalNavigationProfile` registration is the public routing contract. Use `TerminalNavigationProfiles.register` to place tabs in Command, Progress, Intel, or System and to group chapter-owned pages. `TerminalTabChrome` group fallbacks remain for compatibility only.

Recipe-aware addons should register `TerminalRecipeProvider` implementations with `TerminalRecipeRegistry`. Providers publish `TerminalRecipeCategory` and `TerminalRecipeEntry` data built from `TerminalRecipeSlot`, `TerminalRecipeNote`, and `TerminalRecipeSnapshot` semantics so the Recipe Index can search outputs, uses, catalysts, machines, info-only entries, and locked notes.

For release stability, mission and recipe providers should tolerate missing players, return empty lists instead of throwing when content is unavailable, keep action handlers authoritative on the server, and leave recipe authority with the owning addon.

## Terminal Themes

Echo Terminal ships a Java-only theme engine for the release stack. Themes are registered through `TerminalThemeRegistry` and are client presentation only: they do not change packets, world data, mission authority, or reward behavior.

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

## Release Notes And Limits

- Mission HUD notices are transient status cards, not a persistent objective pin; chapter HUDs may still render their own trackers.
- Most chapter-specific behavior is provided by the owning addon. If an addon is missing or disabled, the terminal shows only the providers that are registered.
- Visual assets can be reduced through `TerminalClientOptions`, but there is not yet an in-game options screen for every terminal presentation setting beyond theme selection.
- `TerminalBadgeProvider` and a generalized `TerminalNotificationProvider` remain deferred APIs; mission HUD notices are currently wired through `TerminalMissionProvider` snapshots.
- Baseline cache claims require a placed or recently opened owned terminal so the Reward Inbox has a storage target.
- Save compatibility is expected for normal release use, but fresh-world smoke testing is recommended before public release candidates.

## Release Checklist

- Run `.\gradlew.bat :echoterminal:runGameTestServer --warning-mode all`.
- Run the workspace release verification after packaging paths are aligned.
- Smoke test block opening, keybind opening, every tab, narrow UI widths, mission search/filter, Recipe Index search/category/Recipes/Uses flows, vanilla reward claim, no-addon install, and full-addon install.
