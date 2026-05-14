# Procedural Structure Generation System

## Overview

The procedural structure system generates unique, randomized POI layouts, scanner-readable surface routes, and the underground biome guardian arenas used by Phase 6 progression. Normal POIs can still appear as surface or ruin structures; guardian sites use a visible surface entrance with the actual arena buried below.

---

## How It Works

### BSP Tree Algorithm

Structures are generated using a **Binary Space Partitioning (BSP)** algorithm:

1. **Start with a large rectangular area** (16-48 blocks)
2. **Recursively split** the area into smaller regions (3-5 levels deep)
3. **Create rooms** in the leaf nodes with random padding
4. **Connect rooms** with L-shaped or Z-shaped corridors
5. **Populate rooms** with themed blocks, loot chests, hazards, defenders, and decorations

Guardian mode adds a biome-specific surface landmark, themed descent route, and larger boss chamber. The arena is placed 18-36 blocks below the surface when possible and stores both entrance and arena positions for scanner routing.

### Room Types

| Type | Description | Contents |
|------|-------------|----------|
| `ENTRANCE` | Main entry point | Door, basic loot |
| `LABORATORY` | Research area | Cauldrons, equipment |
| `SERVER_ROOM` | Tech facility | Bookshelves, redstone |
| `ARMORY` | Weapon storage | Barrels, anvil |
| `REACTOR_CORE` | Radiation source | Glowstone core, iron bars |
| `STORAGE` | Supply cache | Chests, barrels |
| `CONTAINMENT` | Hazard area | Glass walls |

---

## Structure Types

### 1. Bio Lab
- **Size:** 16-32 blocks
- **Rooms:** 3-8
- **Biomes:** Crash Zone, Toxic Swamp, Wasteland
- **Features:** Containment cells, chemical spills, medical chests

### 2. Data Center
- **Size:** 20-40 blocks
- **Rooms:** 4-10
- **Biomes:** Industrial Ruins, Ruined City
- **Features:** Server racks, control rooms, tech loot

### 3. Military Vault
- **Size:** 24-48 blocks
- **Rooms:** 5-12
- **Biomes:** All wasteland areas
- **Features:** Armory, barracks, secure storage

### 4. Reactor Ruin
- **Size:** 20-36 blocks
- **Rooms:** 4-10
- **Biomes:** Radiation Zone
- **Features:** Glowing core, radiation hazards

### 5. Drop Pod
- **Template size:** `16x9x16`
- **Main body:** roughly `12x12` with chamfered/clipped corners inside the 20-block site
- **Rooms:** 1 compact starter bay with wall zones
- **Biomes:** Crash Zone
- **Features:** Blockworks orbital hull shell, compact landing struts, south hatch/ramp, side windows, roof beacons, pipe/cable runs, Echo crates/cache, emergency bunk, ECHO control core, embedded lights, and reduced landing debris
- **Contracts:** player spawn at template `(8, 3, 10)`; emergency bunk foot/head at `(4, 3, 7)` and `(4, 3, 8)`; four `echoashfallprotocol:echo_crate` lockers and one `echoashfallprotocol:echo_cache`; no visible vanilla blocks; mirrored NBTs must stay byte-equivalent by generated palette signature under both `structure/drop_pod.nbt` and `structure/global/drop_pod.nbt`

## POI Field Atlas Catalog

The Route Map POI Atlas is the player-facing catalog for the 99 concrete template signals referenced by Ashfall template pools. Each row below is grouped by scanner profile: discovery, mission checks, loot routing, aliases, and save data remain profile-level, while the atlas helps players recognize the concrete ruin, camp, hub, vault, lab, or landmark variant in the field.

| Scanner profile | Route | Template signals |
|---|---|---|
| `bio_lab` | Bio Lab Route | Bio Facility; Bio Lab |
| `drop_pod` | Crash Recovery Route | Drop Pod |
| `crash_zone_wasteland` | Crash Wreck Route | Ash Covered Ruin; Burned Convoy; Cargo Lift Wreck; Cargo Module Field; Containment Facility Ruin; Crash Site Large; Drop Pod Wreck Large; Radiation Field; Salvager Hut; Salvager Worksite; Scrap Pile Medium; Scrap Pile Small; Ship Breaking Yard; Wreckage Cluster; Wreckage Command Post |
| `cryogenic_ruins` | Cryogenic Route | Broken Tank; Cryo Tank Field; Frozen Cache; Frozen Comms Tower; Frozen Lab Large; Frozen Vehicle; Ice Covered Ruin |
| `data_center_ruin` | Data Center Route | Data Center Ruin; Server Farm |
| `industrial_factory` | Factory Route | Industrial Factory; Industrial Factory Shell |
| `survivor_cache` | Global Recovery Route | Abandoned Camp; Debris Field Large; Debris Field Small; Radio Relay Small; Road Checkpoint; Road Wreck; Survivor Cache |
| `industrial_ruins` | Industrial Worksite Route | Conveyor Ruin; Crane Wreck; Factory Pipe Gate; Pipe Cluster; Rail Signal Yard; Storage Yard |
| `mutant_sanctuary` | Mutant Bio Route | Biodome Hub; Processing Hut |
| `nexus_scar` | Nexus Route | Floating Obelisk Cluster; Nexus Pylon |
| `ruined_plains` | Open Wasteland Route | Abandoned Homestead; Impact Crater; Nomad Camp; Relay Tower; Settlement Ruins; Supply Drop; Trader Post; Walled Encampment; Wasteland Bunker Ruin; Windmill Ruin |
| `radiation_zone` | Radiation Hotspot Route | Containment Breach; Contaminated Lab; Fallout Shelter; Irradiated Vehicle; Radiation Beacon Line; Radiation Crater; Waste Barrel Cluster |
| `reactor_ruin` | Reactor Route | Power Plant Ruin; Reactor Containment Ruin; Reactor Gatehouse; Reactor Ruin; Reactor Ruin Alias |
| `remnant_outpost` | Remnant Military Route | Armory; Barracks; Command Bunker; Guard Post; Street Corner; Street Cross; Street Straight; Supply Depot; Wall Corner; Wall Section |
| `salvager_trading_post` | Salvager Trade Route | Market Plaza; Warehouse |
| `scavenger_camp` | Scavenger Camp Route | Scavenger Camp |
| `subway_station` | Subway Route | Subway Stairwell; Subway Station |
| `toxic_swamp` | Toxic Swamp Route | Abandoned Shed; Broken Pipeline; Chemical Spill; Corroded Pipe Network; Pipe Pump House; Sludge Drain; Spore Research Hut; Stilted Outpost; Toxic Pool Small |
| `ruined_cityscape` | Urban Block Route | Collapsed Building Small; Collapsed Building Tall; Collapsed Tower Large; Parking Ruin; Street Barricade |
| `military_vault` | Vault Route | Bunker Complex; Military Vault |

Profile-only scanner records currently have no template-pool entry but remain visible in the Route Map atlas with a zero template count: `relay_station`, `satellite_array`, `sewer_junction`, `train_yard`, and `abandoned_mine`.

### Guardian Sites
- **Surface:** distinct landmarks for each active guardian, including bunker door, subway stair, freight lift, toxic sinkhole, reactor hatch, frozen shaft, impact breach, or Nexus breach
- **Descent:** themed ladder shafts, stair-styled shafts, lift shafts, sinkholes, or breach shafts with a reliable fallback route
- **Underground:** BSP arena with corridors, side rooms, loot, hazards, defenders, one boss chamber, and biome-specific arena motifs
- **Progression:** all eight active guardians must be neutralized before the Nexus Core route opens
- **Scanner:** active guardian missions point to the saved surface entrance, not the buried boss room

---

## Spawn Rates

| Structure | Spacing | Separation | Rarity |
|-----------|---------|------------|--------|
| Drop Pod | 24 chunks | 6 | Common |
| Bio Lab | 60 chunks | 24 | Major |
| Data Center | 60 chunks | 24 | Major |
| Military Vault | 60 chunks | 24 | Major |
| Reactor Ruin | 60 chunks | 24 | Major |

---

## Code Architecture

```
com.knoxhack.echoashfallprotocol.worldgen/
|-- BSPNode.java              - BSP tree node for room subdivision
|-- Room.java                 - Room data structure
|-- Corridor.java             - Corridor connection logic
|-- StructureType.java        - Enum with structure definitions
`-- ProceduralStructureGenerator.java  - Main generation algorithm

com.knoxhack.echoashfallprotocol.event/
`-- ProceduralStructureHandler.java    - Chunk generation event handler
```

---

## Testing

### Find Generated Structures

Regenerate and validate the committed surface POI template assets from the canonical Python pipeline:

```powershell
./gradlew.bat generateEchoPoiStructures
./gradlew.bat checkEchoPoiStructures
```

The offline generator writes the runtime `structure` resources and keeps the legacy plural mirror in sync. In-game structure commands remain useful for exporting hand-built templates and testing procedural placements, but `/modstructures generate` now points back to the offline generator instead of writing POI assets itself.

For the starter pod only, use the focused generator target:

```powershell
python tools\structure_generator\generator.py --target drop_pod
python tools\structure_generator\generator.py --check --target drop_pod
python tools\validate_gameplay_data.py
```

The drop pod check expects a compact `16x9x16` template, the spawn/bunk/cache contract above, visible Blockworks hull coverage, mirrored `structure` and `structures` outputs, and no visible vanilla blocks.

```
# Teleport to nearest bio lab
/locate structure echoashfallprotocol:bio_lab

# Generate a biome guardian entrance and underground arena
/genpoi biome_main toxic_swamp

# List active saved guardian entrances after generation
/genpoi guardian_sites
/genpoi guardian_sites toxic_hive_matriarch

# Generate an individual procedural structure type
/genpoi procedural bio_lab
/genpoi procedural data_center
/genpoi procedural military_vault
/genpoi procedural reactor_ruin

# Export a selected area into the release resource tree
/modstructures create cache_room 16 8 16 global
# Writes under src/main/resources/data/echoashfallprotocol/structure
```

### Verify Randomization

Each structure instance should have:
- Different room layouts
- Different corridor paths
- Player-accessible chests and barrels with themed loot tables
- Different decorative elements
- Guardian sites have a visually distinct entrance, accessible descent route, one reachable boss chamber, one reserved boss spawn zone, and saved site data
- Every biome-main guardian can be smoke-tested with `/genpoi biome_main <biome>` for `ruined_plains`, `ruined_cityscape`, `industrial_ruins`, `toxic_swamp`, `crash_zone_wasteland`, `radiation_zone`, `cryogenic_ruins`, and `nexus_scar`
- `/genpoi guardian_sites [guardian]` should report the saved surface entrance and buried arena used by scanner routing

---

## Customization

### Modifying Spawn Rates

Edit `ProceduralStructureHandler.java`:

```java
static {
    SPAWN_CONFIGS.put(StructureType.BIO_LAB, new SpawnConfig(32, 8, 210415002));
    // Increase frequency: spacing=24, separation=6
}
```

### Adding New Structure Types

1. Add entry to `StructureType.java` enum
2. Define block palette and room count
3. Add spawn config in `ProceduralStructureHandler.java`
4. Add biome check in `isValidBiome()` method

### Custom Room Populators

Edit `ProceduralStructureGenerator.java`:

```java
private static void populateMyCustomRoom(ServerLevel level, BlockPos origin, 
                                          Room room, RandomSource random) {
    // Add custom blocks
    level.setBlock(worldPos, Blocks.MY_BLOCK.defaultBlockState(), 2);
}
```

---

## Performance Notes

- Structures generate during chunk loading or through debug commands
- Average generation time: <50ms per structure
- BSP algorithm is efficient for 3-5 levels of subdivision
- Block placement uses `setBlock()` with flag 2 (no updates during gen)

---

## Future Enhancements

- [x] Biome-specific guardian entrance, descent, and arena visual themes
- [ ] Additional loot room variants
- [ ] More defender patrol patterns
- [ ] Structure-specific advancements
- [x] Portable and stationary signal scanners report route-specific hazards, prep, rewards, distance, direction, and field-log state for tagged POI structures.
