# ECHO ThemeCore 0.2.0

ECHO ThemeCore is the shared visual, audio, UI, and vanilla Minecraft skin service for the ECHO/Ashfall ecosystem. It is a first-party service/API addon with mod id `echothemecore` and package `com.knoxhack.echothemecore`.

ThemeCore hard-requires only `echocore`. Terminal, SignalOS, HoloMap, Lens, RenderCore, SoundCore, Blockworks, RuntimeGuard, and future addons consume it through optional-safe APIs and provider interfaces.

## Themes

- `echothemecore:cyberglass` is the default and fallback theme.
- `echothemecore:nexus` is the endgame anomaly theme.

Themes are loaded from datapacks at:

```text
data/<namespace>/themes/*.json
```

Bad theme JSON is logged and skipped. Missing selections fall back to CyberGlass.

## Config

ThemeCore publishes common and client config groups:

- `[theme]` default/fallback ids, player overrides, server sync, and module scope flags
- `[client]` overlays, transitions, and particle/glow controls
- `[accessibility]` high contrast, reduced glow, distortion/noise controls
- `[vanilla_ui]` vanilla screen surface toggles
- `[vanilla_ui_safety]` conservative vanilla UI safety behavior
- `[vanilla_ui_style]` vanilla UI visual styling toggles

The vanilla UI layer is client-only and uses conservative overlays. It does not replace screens, mutate slots, move widgets, change recipes, or alter gameplay behavior.

## Commands

```text
/echo_theme current
/echo_theme list
/echo_theme set <theme_id>
/echo_theme player set <player> <theme_id>
/echo_theme reset
/echo_theme reload
/echo_theme preview <theme_id>
/echo_theme visual current
/echo_theme visual test terminal
/echo_theme visual test holomap
/echo_theme visual test lens
/echo_theme visual test particles
/echo_theme visual intensity <0.0-2.0>
/echo_theme vanilla current
```

Low-permission players can inspect current/list/preview. Theme mutation, reload guidance, and visual intensity require game master permissions.

## API Example

```java
EchoTheme theme = EchoThemeApi.getTheme(player);
int panel = EchoThemeApi.color(player, EchoThemeColorKey.PANEL);
Optional<Identifier> panelTexture = EchoThemeApi.getTexture(player, EchoThemeTextureKey.PANEL);
ThemeVisualSettings visuals = EchoThemeApi.getEffectiveVisualSettings(player);
```

## Optional Integrations

ThemeCore exposes provider interfaces for ECHO modules:

- `EchoThemedUiProvider` for Terminal and SignalOS UI colors/textures
- `EchoHoloMapThemeProvider` for map lines, markers, opacity, and overlays
- `EchoLensThemeProvider` for scan rings and target highlights
- `EchoSoundThemeProvider` for SoundCore sound/theme music references
- `EchoBlockPaletteProvider` for Blockworks palettes
- `EchoRenderThemeProvider` for RenderCore visual profiles
- `EchoRuntimeGuardThemeProvider` for performance-aware visual reductions

RenderCore remains the engine for advanced VFX. ThemeCore supplies style data and a no-op-safe bridge when RenderCore is absent.

## Vanilla UI Skin Layer

The client layer classifies vanilla screens and applies safe accents to:

- title, pause, options, world select, multiplayer, and loading screens
- inventory, creative inventory, containers, crafting, furnace, anvil, enchanting, grindstone, and smithing screens
- advancements and recipe-book-adjacent screens where safe
- hotbar, selected slot, boss bar, chat accent, tooltips, and toasts where hooks are available

Container handling protects slot interiors by drawing only around panel bounds or at screen edges.

## Adding A Theme

1. Add `data/<namespace>/themes/<theme>.json`.
2. Include required `id`, `display_name`, `colors`, and optional `ui`, `render`, `sound`, `block_palette`, `vanilla_ui`, and `metadata`.
3. Add referenced PNGs under `assets/<namespace>/textures/gui/themes/<theme>/`.
4. Run `/reload`.
5. Use `/echo_theme list` and `/echo_theme preview <theme_id>`.

## ThemeForge

ThemeForge lives at `tools/echo-themeforge/`. It generates development-time prompt packs, validates PNG outputs, creates reports, and safely copies approved generated PNGs into ThemeCore resources. It has no runtime dependency and never belongs in the Minecraft game loop.

Visual rule: ThemeCore uses clean futuristic glass, hologram glow, thin neon borders, geometric circuitry, edge pulses, energy overlays, glints, and phase ripples. Legacy CRT line-overlay styling is forbidden for ThemeCore assets and theme data.
