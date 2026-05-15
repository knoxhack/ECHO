# ECHO: RenderCore V18 Cyberglass Adoption

RenderCore V18 moves cyberglass menu chrome from a shared primitive into addon-facing polish. Existing screen frame APIs remain compatible, while new builder presets make Terminal, SignalOS, HoloMap, Index, and Lens surfaces easier to style consistently.

## Highlights

- Adds `RenderCoreScreenFrameOptions` builder presets for cyberglass, terminal, hologram, neon, and minimal chrome.
- Migrates existing RenderCore screen integrations to named presets with distinct surface identities.
- Tunes screen visual profiles with `bloom_tint`, pulse, and scanline values so each addon keeps its own accent.
- Adds a generic `v18_cyberglass_screen` example profile for addon authors.

## QA Notes

- Compile RenderCore and the affected screen consumers before release.
- Visually check Terminal, SignalOS terminal/rack, HoloMap minimap, Index overlay, and Lens overlay.
- Confirm Terminal reduced-motion mode uses the static reduced chrome preset.
