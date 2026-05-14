# ECHO Stack 1.1.0 - Index Recipe Coverage

ECHO Stack `1.1.0` is the Index recipe coverage release. It keeps gameplay, save data, packets, mission schemas, route schemas, and menu contracts stable while hardening the shared recipe browser around all Echo-owned machine and recipe-like catalogs.

## Highlights

- ECHO: Index now treats direct addon providers as authoritative and uses Terminal recipe import as a broad fallback.
- Direct recipe cards cover vanilla/data-source cards, Ashfall, Armory, Convoy, Orbital, Industrial Nexus, Nexus Protocol, Blackbox Protocol, Logistics Network, Agriculture Reclamation, Multiblock Automation, Mission rewards, and World sources/hazards.
- Terminal recipe registry revisions invalidate Index recipe snapshots so newly registered Terminal providers surface without stale menus.
- Semantic duplicate filtering keeps direct Index cards over imported Terminal duplicates for the same machine/input/output recipe.
- Text-only recipe slots are supported for fluids, status, readiness, unlocks, reputation, hazards, route progress, source notes, and other non-item outputs.

## Compatibility

- Public stack version: `1.1.0`.
- EchoIndex now requires ECHO Core `1.1.0+`; optional Terminal import requires ECHO Terminal `1.1.0+`.
- Addons that register direct Index recipe providers require ECHO Core `1.1.0+`.
- No packet, save-data, recipe JSON, mission, route, loadout, or menu schema migration is included.
- Index transfer remains limited to existing safe paths; recipe-like cards are display/search/pin/reference surfaces.

## Focused Verification

Run the Index-centered release set:

```powershell
.\gradlew.bat :compileJava :echocore:compileJava :echoterminal:compileJava :echoindex:compileJava :echoindustrialnexus:compileJava :echonexusprotocol:compileJava :echoblackboxprotocol:compileJava :echoconvoyprotocol:compileJava :echologisticsnetwork:compileJava :echomultiblockcore:compileJava :echoagriculturereclamation:compileJava :echomissioncore:compileJava :echoworldcore:compileJava
```

Run the focused Index GameTests:

```powershell
.\gradlew.bat :echoindex:runGameTestServer
```

Smoke-test in-game by opening Index and checking search, detail, pins, recipes, uses, and sources for machine recipes, logistics offers/loadouts, convoy routes, agriculture rules, multiblock automation, mission rewards, world sources/hazards, and text-only non-item outputs.
