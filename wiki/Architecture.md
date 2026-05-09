# Architecture

## Layer Model

- **`echocore` (service layer):** shared profile state, faction data, route records, diagnostics, rewards, and cross-module contracts.
- **`echoterminal` (presentation layer):** shared terminal surfaces and navigation profile routing.
- **Campaign addons (domain layer):** chapter-specific gameplay, progression, worldgen, entities, and rewards.

## Integration Contracts

### Echo Core

Addons should publish and consume shared data through `echocore` service interfaces rather than direct save-data coupling.

### Echo Terminal

Navigation is registered via terminal profile types (for owned chapter surfaces), and recipe-aware modules can register recipe providers for shared indexing/search.

## Why This Design

- Reduces brittle cross-addon dependencies
- Keeps chapters independently shippable
- Preserves a consistent terminal UX across the full stack
- Enables progressive chapter expansion without rewriting the base campaign
