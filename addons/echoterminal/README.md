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

## Beta Limitations

- The terminal is primarily a client command interface; it does not pin objectives to the HUD yet.
- Most chapter-specific behavior is provided by the owning addon. If an addon is missing or disabled, the terminal shows only the providers that are registered.
- Visual assets can be reduced through `TerminalClientOptions`, but there is not yet an in-game options screen for every terminal presentation setting.
- `TerminalBadgeProvider` and `TerminalNotificationProvider` are deferred beta APIs until badges and notification chrome are fully wired.
- Baseline cache claims require a placed or recently opened owned terminal so the Reward Inbox has a storage target.
- Save compatibility is expected for normal beta use, but fresh-world smoke testing is recommended before public release candidates.

## Release Checklist

- Run `.\gradlew.bat :echoterminal:runGameTestServer --warning-mode all`.
- Run the workspace release verification after packaging paths are aligned.
- Smoke test block opening, keybind opening, every tab, narrow UI widths, mission search/filter, vanilla reward claim, no-addon install, and full-addon install.
