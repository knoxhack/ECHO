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
- **Size:** 7-14 blocks
- **Rooms:** 1-3
- **Biomes:** Crash Zone
- **Features:** Emergency shelter, starter supplies

### Guardian Sites
- **Surface:** distinct landmarks for each guardian, including rescue hatch, bunker door, subway stair, freight lift, toxic sinkhole, reactor hatch, frozen shaft, impact breach, or Nexus breach
- **Descent:** themed ladder shafts, stair-styled shafts, lift shafts, sinkholes, or breach shafts with a reliable fallback route
- **Underground:** BSP arena with corridors, side rooms, loot, hazards, defenders, one boss chamber, and biome-specific arena motifs
- **Progression:** all nine guardians must be neutralized before the Nexus Core route opens
- **Scanner:** active guardian missions point to the saved surface entrance, not the buried boss room

---

## Spawn Rates

| Structure | Spacing | Separation | Rarity |
|-----------|---------|------------|--------|
| Drop Pod | 24 chunks | 6 | Common |
| Bio Lab | 32 chunks | 8 | Uncommon |
| Data Center | 40 chunks | 10 | Rare |
| Military Vault | 48 chunks | 12 | Very Rare |
| Reactor Ruin | 64 chunks | 16 | Endgame |

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

```
# Teleport to nearest bio lab
/locate structure echoashfallprotocol:bio_lab

# Generate a biome guardian entrance and underground arena
/genpoi biome_main toxic_swamp
/genpoi biome_main the_wasteland

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
- Every biome-main guardian can be smoke-tested with `/genpoi biome_main <biome>` for `the_wasteland`, `ruined_plains`, `ruined_cityscape`, `industrial_ruins`, `toxic_swamp`, `crash_zone_wasteland`, `radiation_zone`, `cryogenic_ruins`, and `nexus_scar`
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
