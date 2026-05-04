"""
Detail Pass System for Enhanced POI Structures

Applies post-processing layers to basic structure generation for richer,
more narratively interesting ruins and settlements.
"""

import random
from typing import Dict, List, Optional, Tuple

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]

# Block variants for weathering
WEATHERING_VARIANTS = {
    "minecraft:stone_bricks": [
        "minecraft:stone_bricks",
        "minecraft:cracked_stone_bricks",
        "minecraft:mossy_stone_bricks",
    ],
    "minecraft:cobblestone": [
        "minecraft:cobblestone",
        "minecraft:mossy_cobblestone",
    ],
    "minecraft:oak_planks": [
        "minecraft:oak_planks",
        "minecraft:spruce_planks",
        "minecraft:dark_oak_planks",
    ],
    "minecraft:white_wool": [
        "minecraft:white_wool",
        "minecraft:light_gray_wool",
        "minecraft:gray_wool",
    ],
    "minecraft:smooth_stone": [
        "minecraft:smooth_stone",
        "minecraft:stone",
        "minecraft:cobblestone",
    ],
    "minecraft:iron_block": [
        "minecraft:iron_block",
        "echoashfallprotocol:rusted_metal_debris",
        "echoashfallprotocol:rusted_metal_sheet",
    ],
}

# Vegetation by biome type
BIOME_VEGETATION = {
    "ruined_plains": [
        "echoashfallprotocol:ash_bush",
        "echoashfallprotocol:burnt_fern",
        "echoashfallprotocol:dry_grass",
        "minecraft:dead_bush",
    ],
    "crash_zone_wasteland": [
        "echoashfallprotocol:ash_bush",
        "echoashfallprotocol:burnt_fern",
        "echoashfallprotocol:dry_grass",
        "minecraft:dead_bush",
    ],
    "ruined_cityscape": [
        "minecraft:moss_block",
        "minecraft:vine",
        "echoashfallprotocol:deep_ash",
    ],
    "radiation_zone": [
        "echoashfallprotocol:nuclear_fungus",
        "echoashfallprotocol:mutated_sapling",
        "echoashfallprotocol:contaminated_soil",
    ],
    "toxic_swamp": [
        "echoashfallprotocol:toxic_moss",
        "echoashfallprotocol:toxic_puddle",
        "minecraft:lily_pad",
    ],
    "industrial_ruins": [
        "echoashfallprotocol:oil_stained_concrete",
        "echoashfallprotocol:rusted_metal_debris",
    ],
    "cryogenic_ruins": [
        "minecraft:snow",
        "minecraft:ice",
        "echoashfallprotocol:cryogenic_fractured_stone",
    ],
    "nexus_scar": [
        "echoashfallprotocol:echo_crystal",
        "echoashfallprotocol:energized_fissure",
        "echoashfallprotocol:riftstone",
    ],
}

# Rubble/debris by category
RUBBLE_PALETTES = {
    "ruined_plains": [
        "echoashfallprotocol:rubble",
        "echoashfallprotocol:concrete_chunk",
        "minecraft:gravel",
        "minecraft:cobblestone",
    ],
    "crash_zone_wasteland": [
        "echoashfallprotocol:rusted_metal_debris",
        "echoashfallprotocol:rusted_metal_sheet",
        "echoashfallprotocol:concrete_rubble",
        "minecraft:iron_bars",
    ],
    "ruined_cityscape": [
        "echoashfallprotocol:concrete_rubble",
        "echoashfallprotocol:rubble",
        "echoashfallprotocol:wasteland_trace_rubble",
        "minecraft:gravel",
    ],
    "radiation_zone": [
        "echoashfallprotocol:fallout_dust",
        "echoashfallprotocol:contaminated_soil",
        "echoashfallprotocol:toxic_waste_barrel",
    ],
    "toxic_swamp": [
        "echoashfallprotocol:acidic_sludge",
        "echoashfallprotocol:toxic_puddle",
        "minecraft:slime_block",
    ],
    "industrial_ruins": [
        "echoashfallprotocol:rusted_metal_debris",
        "echoashfallprotocol:oil_stained_concrete",
        "minecraft:iron_bars",
    ],
    "cryogenic_ruins": [
        "minecraft:snow",
        "minecraft:ice",
        "echoashfallprotocol:cryogenic_fractured_stone",
    ],
    "global": [
        "echoashfallprotocol:rubble",
        "minecraft:gravel",
        "echoashfallprotocol:wasteland_trace_rubble",
    ],
    "nexus_scar": [
        "echoashfallprotocol:riftstone",
        "echoashfallprotocol:energized_fissure",
        "minecraft:crying_obsidian",
    ],
}

# Story/narrative props
STORY_PROPS = {
    "survivor": [
        ("minecraft:campfire", 0.3),
        ("minecraft:cauldron", 0.2),
        ("minecraft:anvil", 0.15),
        ("echoashfallprotocol:weapon_rack", 0.1),
    ],
    "military": [
        ("echoashfallprotocol:weapon_rack", 0.4),
        ("minecraft:anvil", 0.2),
        ("minecraft:observer", 0.15),
        ("minecraft:iron_bars", 0.3),
    ],
    "scientific": [
        ("minecraft:cauldron", 0.4),
        ("minecraft:observer", 0.3),
        ("echoashfallprotocol:bio_processing_station", 0.2),
        ("minecraft:brewing_stand", 0.15),
    ],
    "industrial": [
        ("minecraft:anvil", 0.3),
        ("echoashfallprotocol:scrap_press", 0.2),
        ("echoashfallprotocol:factory_controller", 0.15),
        ("minecraft:hopper", 0.25),
    ],
}

LIGHTING_NODES = {
    "crash_zone_wasteland": ["minecraft:campfire", "minecraft:magma_block"],
    "ruined_plains": ["minecraft:campfire", "minecraft:lantern"],
    "ruined_cityscape": ["minecraft:redstone_torch", "minecraft:lantern"],
    "radiation_zone": ["minecraft:redstone_torch", "echoashfallprotocol:radiation_block"],
    "toxic_swamp": ["minecraft:glowstone", "echoashfallprotocol:ooze_crystal"],
    "industrial_ruins": ["minecraft:redstone_torch", "minecraft:lantern"],
    "cryogenic_ruins": ["minecraft:sea_lantern", "echoashfallprotocol:blue_ice_crystal"],
    "nexus_scar": ["echoashfallprotocol:echo_crystal", "echoashfallprotocol:energized_fissure"],
    "global": ["minecraft:campfire", "minecraft:lantern"],
    "faction": ["minecraft:lantern", "minecraft:redstone_torch"],
}

PATH_BLOCKS = {
    "crash_zone_wasteland": "echoashfallprotocol:scorched_ash",
    "ruined_plains": "minecraft:gravel",
    "ruined_cityscape": "echoashfallprotocol:oil_stained_concrete",
    "radiation_zone": "echoashfallprotocol:fallout_dust",
    "toxic_swamp": "echoashfallprotocol:acid_mud",
    "industrial_ruins": "echoashfallprotocol:oil_stained_concrete",
    "cryogenic_ruins": "echoashfallprotocol:permafrost",
    "nexus_scar": "echoashfallprotocol:riftstone",
    "global": "minecraft:gravel",
    "faction": "minecraft:gravel",
}

FOUNDATION_PALETTES = {
    "crash_zone_wasteland": [
        "echoashfallprotocol:burnt_wasteland_soil",
        "echoashfallprotocol:scorched_ash",
        "echoashfallprotocol:rusted_metal_debris",
        "echoashfallprotocol:rubble",
    ],
    "ruined_plains": [
        "echoashfallprotocol:wasteland_dirt",
        "echoashfallprotocol:ashen_wasteland_dirt",
        "echoashfallprotocol:rubble",
        "minecraft:gravel",
    ],
    "ruined_cityscape": [
        "echoashfallprotocol:concrete_rubble",
        "echoashfallprotocol:oil_stained_concrete",
        "echoashfallprotocol:wasteland_trace_rubble",
        "minecraft:gravel",
    ],
    "radiation_zone": [
        "echoashfallprotocol:irradiated_crust",
        "echoashfallprotocol:fallout_dust",
        "echoashfallprotocol:contaminated_soil",
        "echoashfallprotocol:rubble",
    ],
    "toxic_swamp": [
        "echoashfallprotocol:acid_mud",
        "echoashfallprotocol:toxic_moss",
        "echoashfallprotocol:contaminated_soil",
        "echoashfallprotocol:toxic_slagstone",
    ],
    "industrial_ruins": [
        "echoashfallprotocol:oil_stained_concrete",
        "echoashfallprotocol:industrial_aggregate",
        "echoashfallprotocol:rusted_metal_debris",
        "echoashfallprotocol:concrete_rubble",
    ],
    "cryogenic_ruins": [
        "echoashfallprotocol:permafrost",
        "echoashfallprotocol:cryogenic_fractured_stone",
        "minecraft:snow_block",
        "minecraft:ice",
    ],
    "nexus_scar": [
        "echoashfallprotocol:nexus_cracked_soil",
        "echoashfallprotocol:riftstone",
        "echoashfallprotocol:energized_fissure",
        "minecraft:obsidian",
    ],
    "global": [
        "echoashfallprotocol:wasteland_dirt",
        "echoashfallprotocol:ashen_wasteland_dirt",
        "echoashfallprotocol:rubble",
        "minecraft:gravel",
    ],
    "faction": [
        "minecraft:gravel",
        "echoashfallprotocol:rubble",
        "echoashfallprotocol:concrete_rubble",
    ],
}

RETAINING_PALETTES = {
    "crash_zone_wasteland": ["echoashfallprotocol:rusted_metal_sheet", "echoashfallprotocol:concrete_chunk"],
    "ruined_plains": ["echoashfallprotocol:dead_wood_log", "echoashfallprotocol:concrete_chunk"],
    "ruined_cityscape": ["echoashfallprotocol:concrete_chunk", "echoashfallprotocol:rusted_metal_sheet"],
    "radiation_zone": ["echoashfallprotocol:concrete_rubble", "echoashfallprotocol:rusted_metal_debris"],
    "toxic_swamp": ["echoashfallprotocol:corroded_pipe", "echoashfallprotocol:toxic_waste_barrel"],
    "industrial_ruins": ["echoashfallprotocol:rusted_metal_sheet", "echoashfallprotocol:item_pipe"],
    "cryogenic_ruins": ["echoashfallprotocol:frozen_conduit", "echoashfallprotocol:cryogenic_fractured_stone"],
    "nexus_scar": ["minecraft:obsidian", "echoashfallprotocol:riftstone"],
    "global": ["echoashfallprotocol:concrete_chunk", "echoashfallprotocol:rusted_metal_debris"],
    "faction": ["echoashfallprotocol:concrete_chunk", "echoashfallprotocol:rusted_metal_sheet"],
}

SOFT_WHITE_REPLACEMENTS = {
    "minecraft:white_wool": ["minecraft:light_gray_wool", "minecraft:gray_wool"],
    "minecraft:bone_block": ["echoashfallprotocol:concrete_chunk", "minecraft:light_gray_wool"],
    "minecraft:quartz_block": ["minecraft:smooth_stone", "minecraft:light_gray_concrete"],
    "minecraft:quartz_pillar": ["minecraft:smooth_stone", "minecraft:light_gray_concrete"],
    "minecraft:scattered_bones": ["echoashfallprotocol:scattered_bones"],
}


def _bounds(blocks: BlockList) -> Optional[tuple[int, int, int, int, int, int]]:
    solid = [(x, y, z) for x, y, z, block_id, _ in blocks if block_id != "minecraft:air"]
    if not solid:
        return None
    xs = [x for x, _, _ in solid]
    ys = [y for _, y, _ in solid]
    zs = [z for _, _, z in solid]
    return min(xs), max(xs), min(ys), max(ys), min(zs), max(zs)


def _base_footprint(blocks: BlockList, base_y: int) -> set[Tuple[int, int]]:
    return {
        (x, z)
        for x, y, z, block_id, _ in blocks
        if y == base_y and block_id != "minecraft:air"
    }


def _edge_positions(footprint: set[Tuple[int, int]]) -> set[Tuple[int, int]]:
    edges: set[Tuple[int, int]] = set()
    for x, z in footprint:
        for dx, dz in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            neighbor = (x + dx, z + dz)
            if neighbor not in footprint:
                edges.add(neighbor)
    return edges


def normalize_visual_blocks(blocks: BlockList, category: str, rng: random.Random) -> BlockList:
    """Tone down placeholder-bright blocks before extra visual passes."""
    result: BlockList = []
    for x, y, z, block_id, props in blocks:
        if block_id in SOFT_WHITE_REPLACEMENTS and category != "cryogenic_ruins":
            block_id = rng.choice(SOFT_WHITE_REPLACEMENTS[block_id])
            props = None
        result.append((x, y, z, block_id, props))
    return result


def _dedupe_blocks(blocks: BlockList) -> BlockList:
    deduped: Dict[Tuple[int, int, int], Tuple[str, Optional[Dict[str, str]]]] = {}
    for x, y, z, block_id, props in blocks:
        deduped[(x, y, z)] = (block_id, dict(props) if props else None)
    return [
        (x, y, z, block_id, props)
        for (x, y, z), (block_id, props) in sorted(deduped.items(), key=lambda item: (item[0][1], item[0][0], item[0][2]))
    ]


def apply_weathering(
    blocks: BlockList,
    rng: random.Random,
    intensity: float = 0.4,
) -> BlockList:
    """Replace pristine blocks with weathered/cracked variants."""
    result: BlockList = []
    for x, y, z, block_id, props in blocks:
        if block_id in WEATHERING_VARIANTS and rng.random() < intensity:
            variants = WEATHERING_VARIANTS[block_id]
            # Weight toward original for subtle effect
            weights = [0.6] + [0.4 / (len(variants) - 1)] * (len(variants) - 1)
            block_id = rng.choices(variants, weights=weights)[0]
        result.append((x, y, z, block_id, props))
    return result


def add_overgrowth(
    blocks: BlockList,
    rng: random.Random,
    biome: str,
    intensity: float = 0.25,
) -> BlockList:
    """Add biome-specific vegetation overgrowth to structure edges."""
    if biome not in BIOME_VEGETATION:
        return blocks

    vegetation = BIOME_VEGETATION[biome]
    result = blocks[:]

    # Find edges and add vegetation
    occupied = {(x, z) for x, y, z, _, _ in blocks if y == 0}
    edge_positions = set()

    for x, z in occupied:
        for dx, dz in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            if (x + dx, z + dz) not in occupied:
                edge_positions.add((x + dx, z + dz))

    # Add vegetation at edges
    for x, z in edge_positions:
        if rng.random() < intensity:
            plant = rng.choice(vegetation)
            result.append((x, 1, z, plant, None))
            # Sometimes stack higher
            if rng.random() < 0.3:
                result.append((x, 2, z, plant, None))

    return result


def scatter_rubble(
    blocks: BlockList,
    rng: random.Random,
    category: str,
    count: int = 10,
) -> BlockList:
    """Scatter clustered rubble/debris around structure edges."""
    palette = RUBBLE_PALETTES.get(category, RUBBLE_PALETTES["global"])
    result = blocks[:]

    bounds = _bounds(blocks)
    if not bounds:
        return result
    _, _, min_y, _, _, _ = bounds

    footprint = _base_footprint(blocks, min_y)
    edge_positions = list(_edge_positions(footprint))
    if not edge_positions:
        return result

    clusters = max(1, count // 4)
    for _ in range(clusters):
        x, z = rng.choice(edge_positions)
        for _ in range(max(2, count // clusters)):
            if (x, z) not in footprint or rng.random() < 0.35:
                rubble = rng.choice(palette)
                result.append((x, min_y, z, rubble, None))
                if rng.random() < 0.22:
                    result.append((x, min_y + 1, z, rubble, None))
            x += rng.choice([-1, 0, 1])
            z += rng.choice([-1, 0, 1])

    return result


def add_terrain_apron(blocks: BlockList, rng: random.Random, category: str, intensity: float = 1.0) -> BlockList:
    """Add an irregular biome-matched apron so POIs sit in the terrain."""
    bounds = _bounds(blocks)
    if not bounds:
        return blocks
    min_x, max_x, min_y, _, min_z, max_z = bounds
    width = max_x - min_x + 1
    depth = max_z - min_z + 1
    if width < 4 or depth < 4:
        return blocks

    foundation = FOUNDATION_PALETTES.get(category, FOUNDATION_PALETTES["global"])
    retaining = RETAINING_PALETTES.get(category, RETAINING_PALETTES["global"])
    footprint = _base_footprint(blocks, min_y)
    edge_positions = list(_edge_positions(footprint))
    rng.shuffle(edge_positions)

    result = blocks[:]
    edge_budget = min(len(edge_positions), max(10, int((width + depth) * intensity * 1.25)))
    for x, z in edge_positions[:edge_budget]:
        if rng.random() < 0.82:
            result.append((x, min_y, z, rng.choice(foundation), None))
        if rng.random() < 0.18:
            result.append((x, min_y + 1, z, rng.choice(retaining), None))

    cx = (min_x + max_x) // 2
    cz = (min_z + max_z) // 2
    fingers = max(3, min(8, (width + depth) // 8))
    directions = [(-1, 0), (1, 0), (0, -1), (0, 1), (-1, -1), (1, 1), (-1, 1), (1, -1)]
    rng.shuffle(directions)
    for dx, dz in directions[:fingers]:
        x = cx + dx * max(1, width // 3)
        z = cz + dz * max(1, depth // 3)
        length = rng.randint(3, 6)
        for step in range(length):
            px = x + dx * step + rng.choice([-1, 0, 0, 1])
            pz = z + dz * step + rng.choice([-1, 0, 0, 1])
            result.append((px, min_y, pz, rng.choice(foundation), None))
            if step <= 1 and rng.random() < 0.25:
                result.append((px, min_y + 1, pz, rng.choice(retaining), None))

    return result


def add_story_elements(
    blocks: BlockList,
    rng: random.Random,
    story_type: str = "survivor",
    count: int = 3,
) -> BlockList:
    """Add narrative props that suggest what happened here."""
    if story_type not in STORY_PROPS:
        return blocks

    props = STORY_PROPS[story_type]
    result = blocks[:]

    # Find floor positions inside structure
    floor_positions = [(x, z) for x, y, z, _, _ in blocks if y == 0]
    if not floor_positions:
        return result

    # Place props
    for _ in range(count):
        if not floor_positions:
            break
        x, z = rng.choice(floor_positions)
        # Pick prop based on probability
        candidates = [p for p, prob in props if rng.random() < prob * 2]
        if candidates:
            prop = rng.choice(candidates)
            # Clear any block at this position first
            result = [(bx, by, bz, bid, bprops) for bx, by, bz, bid, bprops in result
                      if not (bx == x and by == 1 and bz == z)]
            result.append((x, 1, z, prop, None))

    return result


def add_loot_containers(
    blocks: BlockList,
    rng: random.Random,
    container_count: int = 2,
    container_types: Optional[List[str]] = None,
) -> BlockList:
    """Intelligently place loot containers in sheltered positions."""
    if container_types is None:
        container_types = ["minecraft:chest", "minecraft:barrel"]

    result = blocks[:]

    occupied = {(x, y, z) for x, y, z, bid, _ in blocks if bid != "minecraft:air"}
    wall_positions = {
        (x, z)
        for x, y, z, bid, _ in blocks
        if y in (1, 2) and bid != "minecraft:air"
    }
    floor_positions = {
        (x, z)
        for x, y, z, bid, _ in blocks
        if y == 0 and bid != "minecraft:air" and (x, 1, z) not in occupied and (x, 2, z) not in occupied
    }

    sheltered = []
    for x, z in floor_positions:
        # Check if adjacent to a wall
        for dx, dz in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            if (x + dx, z + dz) in wall_positions:
                sheltered.append((x, z))
                break

    if not sheltered:
        sheltered = list(floor_positions)

    # Place containers
    rng.shuffle(sheltered)
    for _, (x, z) in enumerate(sheltered[:container_count]):
        container = rng.choice(container_types)
        # Remove any existing block at this position
        result = [(bx, by, bz, bid, bprops) for bx, by, bz, bid, bprops in result
                  if not (bx == x and by == 1 and bz == z)]
        result.append((x, 1, z, container, None))

    return result


def add_access_path(blocks: BlockList, rng: random.Random, category: str) -> BlockList:
    """Add a small readable approach path without a ruler-straight footprint."""
    bounds = _bounds(blocks)
    if not bounds:
        return blocks
    min_x, max_x, min_y, _, min_z, max_z = bounds
    cx = (min_x + max_x) // 2
    cz = (min_z + max_z) // 2
    path_block = PATH_BLOCKS.get(category, PATH_BLOCKS["global"])
    result = blocks[:]
    from_south = rng.random() < 0.5
    if from_south:
        for step, z in enumerate(range(max_z + 1, max_z + rng.randint(5, 8))):
            drift = rng.choice([-1, 0, 0, 1])
            for dx in (-1, 0, 1):
                if abs(dx) == 1 and rng.random() < 0.35:
                    continue
                result.append((cx + dx + drift, min_y, z, path_block, None))
            if step % 2 == 0:
                result.append((cx + drift + rng.choice([-2, 2]), min_y, z, rng.choice(FOUNDATION_PALETTES.get(category, FOUNDATION_PALETTES["global"])), None))
    else:
        for step, x in enumerate(range(min_x - rng.randint(5, 8), min_x)):
            drift = rng.choice([-1, 0, 0, 1])
            for dz in (-1, 0, 1):
                if abs(dz) == 1 and rng.random() < 0.35:
                    continue
                result.append((x, min_y, cz + dz + drift, path_block, None))
            if step % 2 == 0:
                result.append((x, min_y, cz + drift + rng.choice([-2, 2]), rng.choice(FOUNDATION_PALETTES.get(category, FOUNDATION_PALETTES["global"])), None))
    return result


def add_lighting_nodes(blocks: BlockList, rng: random.Random, category: str, count: int = 3) -> BlockList:
    """Place category-appropriate light/marker blocks at readable anchors."""
    bounds = _bounds(blocks)
    if not bounds:
        return blocks
    min_x, max_x, min_y, _, min_z, max_z = bounds
    palette = LIGHTING_NODES.get(category, LIGHTING_NODES["global"])
    result = blocks[:]
    candidates = [
        (min_x + 1, min_z + 1),
        (max_x - 1, min_z + 1),
        (min_x + 1, max_z - 1),
        (max_x - 1, max_z - 1),
        ((min_x + max_x) // 2, (min_z + max_z) // 2),
    ]
    rng.shuffle(candidates)
    for x, z in candidates[:count]:
        block = rng.choice(palette)
        props = {"lit": "true"} if block == "minecraft:redstone_torch" else None
        if block == "minecraft:campfire":
            props = {"lit": "true", "facing": "north", "signal_fire": "false", "waterlogged": "false"}
        result.append((x, min_y + 1, z, block, props))
    return result


def add_silhouette_polish(blocks: BlockList, rng: random.Random, category: str) -> BlockList:
    """Add a small vertical read to otherwise flat POIs."""
    bounds = _bounds(blocks)
    if not bounds:
        return blocks
    min_x, max_x, min_y, max_y, min_z, max_z = bounds
    width = max_x - min_x + 1
    depth = max_z - min_z + 1
    if width < 8 or depth < 8:
        return blocks

    result = blocks[:]
    x = rng.choice([min_x + 2, max_x - 2, (min_x + max_x) // 2])
    z = rng.choice([min_z + 2, max_z - 2, (min_z + max_z) // 2])
    base_y = min_y + 1
    height = max(3, min(8, 3 + (width + depth) // 18))

    if category == "nexus_scar":
        for y in range(base_y, base_y + height):
            result.append((x, y, z, "minecraft:crying_obsidian", None))
        result.append((x, base_y + height, z, "echoashfallprotocol:echo_crystal", None))
    elif category == "toxic_swamp":
        for y in range(base_y, base_y + height):
            result.append((x, y, z, "echoashfallprotocol:corroded_pipe", None))
        result.append((x + 1, base_y + height - 1, z, "echoashfallprotocol:toxic_waste_barrel", None))
    elif category == "radiation_zone":
        for y in range(base_y, base_y + height):
            result.append((x, y, z, "echoashfallprotocol:concrete_rubble", None))
        result.append((x, base_y + height, z, "minecraft:redstone_torch", {"lit": "true"}))
        result.append((x + 1, base_y, z, "echoashfallprotocol:radiation_block", None))
    elif category == "cryogenic_ruins":
        for y in range(base_y, base_y + height):
            result.append((x, y, z, "echoashfallprotocol:frozen_conduit", None))
        result.append((x, base_y + height, z, "echoashfallprotocol:blue_ice_crystal", None))
    elif category in {"industrial_ruins", "ruined_cityscape", "faction"}:
        for y in range(base_y, base_y + height):
            result.append((x, y, z, "minecraft:iron_bars", {"waterlogged": "false"}))
        result.append((x, base_y + height, z, "minecraft:lightning_rod", None))
    else:
        for y in range(base_y, base_y + height):
            result.append((x, y, z, "echoashfallprotocol:charred_wood_log", {"axis": "y"}))
        result.append((x, base_y + height, z, "minecraft:campfire", {"lit": "true", "facing": "north", "signal_fire": "true", "waterlogged": "false"}))

    return result


def add_category_signature(
    blocks: BlockList,
    rng: random.Random,
    category: str,
    name: str = "",
) -> BlockList:
    """Stamp a concise, readable visual motif onto each generated POI."""
    bounds = _bounds(blocks)
    if not bounds:
        return blocks

    min_x, max_x, min_y, max_y, min_z, max_z = bounds
    cx = (min_x + max_x) // 2
    cz = (min_z + max_z) // 2
    base_y = min_y + 1
    result = blocks[:]

    def add(x: int, y: int, z: int, block: str, props: Optional[Dict[str, str]] = None) -> None:
        result.append((x, y, z, block, props))

    def post(x: int, z: int, height: int, block: str) -> None:
        for y in range(base_y, base_y + height):
            add(x, y, z, block)

    if category == "crash_zone_wasteland":
        for offset in range(-2, 3):
            add(cx + offset, base_y, cz + rng.choice([-2, 2]), "echoashfallprotocol:drop_pod_hull")
            add(cx + offset, base_y + 1, cz, "echoashfallprotocol:power_cable")
        add(cx - 3, base_y, cz - 1, "echoashfallprotocol:rusted_metal_debris")
        add(cx + 3, base_y, cz + 1, "echoashfallprotocol:cable_bundle")
    elif category == "ruined_plains":
        add(cx, base_y, cz, "minecraft:campfire", {"lit": "true", "facing": "north", "signal_fire": "false", "waterlogged": "false"})
        post(cx - 2, cz - 2, 3, "echoashfallprotocol:charred_wood_log")
        add(cx + 2, base_y, cz - 1, "echoashfallprotocol:rain_collector")
        add(cx + 1, base_y, cz + 2, "echoashfallprotocol:wild_berry_bush")
    elif category == "ruined_cityscape":
        post(cx - 2, cz, 5, "echoashfallprotocol:rebar_block")
        post(cx + 2, cz + 1, 4, "echoashfallprotocol:concrete_rubble")
        for x in range(min_x + 1, max_x, 4):
            add(x, min(max_y, base_y + 3), cz, "echoashfallprotocol:power_cable")
    elif category == "industrial_ruins":
        gantry_y = min(max_y + 1, base_y + 5)
        for x in range(min_x + 1, max_x, 3):
            add(x, gantry_y, cz, "echoashfallprotocol:item_pipe")
        post(cx - 3, cz - 2, 4, "echoashfallprotocol:rusted_metal_sheet")
        add(cx, base_y, cz, "echoashfallprotocol:scrap_press")
        add(cx + 2, base_y, cz + 1, "echoashfallprotocol:battery_bank")
    elif category == "toxic_swamp":
        for dx, dz in ((0, 0), (1, 0), (0, 1), (-1, 0), (0, -1), (1, 1)):
            add(cx + dx, min_y, cz + dz, rng.choice(["echoashfallprotocol:toxic_puddle", "echoashfallprotocol:acidic_sludge"]))
        post(cx - 3, cz, 4, "echoashfallprotocol:corroded_pipe")
        add(cx + 2, base_y, cz + 2, "echoashfallprotocol:bio_processing_station")
    elif category == "radiation_zone":
        for x in range(cx - 4, cx + 5, 2):
            add(x, base_y, cz - 2, "echoashfallprotocol:radiation_block")
            add(x, base_y + 1, cz - 2, "minecraft:redstone_torch", {"lit": "true"})
        add(cx, base_y, cz + 2, "echoashfallprotocol:toxic_waste_barrel")
        add(cx + 2, base_y, cz + 2, "echoashfallprotocol:uranium_crystal")
    elif category == "cryogenic_ruins":
        post(cx, cz, 5, "echoashfallprotocol:frozen_conduit")
        add(cx, base_y + 5, cz, "echoashfallprotocol:blue_ice_crystal")
        for dx, dz in ((-2, 0), (2, 0), (0, -2), (0, 2)):
            add(cx + dx, min_y, cz + dz, "minecraft:blue_ice")
    elif category == "nexus_scar":
        post(cx, cz, 6, "minecraft:crying_obsidian")
        add(cx, base_y + 6, cz, "echoashfallprotocol:echo_crystal")
        for dx, dz in ((-2, -1), (2, 1), (-1, 2), (1, -2)):
            add(cx + dx, min_y, cz + dz, rng.choice(["echoashfallprotocol:riftstone", "echoashfallprotocol:energized_fissure"]))
    elif category == "faction":
        if name.startswith("remnant_outpost/"):
            add(cx, base_y, cz, "echoashfallprotocol:weapon_rack")
            add(cx + 1, base_y, cz, "echoashfallprotocol:supply_crate")
            add(cx - 1, base_y, cz, "echoashfallprotocol:power_node")
        elif name.startswith("salvager_post/"):
            add(cx, base_y, cz, "echoashfallprotocol:trade_counter")
            add(cx + 1, base_y, cz, "echoashfallprotocol:map_table")
            add(cx - 1, base_y, cz, "echoashfallprotocol:rain_collector")
        elif name.startswith("mutant_sanctuary/"):
            add(cx, base_y, cz, "echoashfallprotocol:spore_garden")
            add(cx + 1, base_y, cz, "echoashfallprotocol:bio_processing_station")
            add(cx - 1, min_y, cz, "echoashfallprotocol:toxic_moss")
    else:
        add(cx, base_y, cz, "minecraft:campfire", {"lit": "true", "facing": "north", "signal_fire": "false", "waterlogged": "false"})
        add(cx + 1, base_y, cz, "echoashfallprotocol:rain_collector")
        add(cx - 1, base_y, cz, "echoashfallprotocol:supply_crate")

    return result


def apply_detail_pass(
    blocks: BlockList,
    category: str,
    biome: str,
    seed: int,
    structure_size: str = "small",
    name: str = "",
) -> BlockList:
    """Apply complete detail pass to structure.

    Args:
        blocks: Base structure blocks
        category: Structure category (e.g., "ruined_plains", "crash_zone")
        biome: Biome type for vegetation
        seed: Random seed
        structure_size: "small", "medium", or "big" - affects detail intensity
    """
    rng = random.Random(seed + 999)  # Offset seed for detail pass

    # Adjust intensity by size
    size_multipliers = {
        "small": 0.7,
        "medium": 1.0,
        "big": 1.3,
    }
    multiplier = size_multipliers.get(structure_size, 1.0)

    result = blocks

    # Replace bright test-looking materials before adding more detail.
    result = normalize_visual_blocks(result, category, rng)

    # Apply weathering
    result = apply_weathering(result, rng, intensity=0.4 * multiplier)

    # Add overgrowth
    result = add_overgrowth(result, rng, biome, intensity=0.25 * multiplier)

    # Soften template footprints before other decorative scatter.
    result = add_terrain_apron(result, rng, category, intensity=multiplier)

    # Scatter rubble (more for larger structures)
    rubble_count = int(10 * multiplier)
    result = scatter_rubble(result, rng, category, count=rubble_count)

    # Add readable approach paths and skyline anchors.
    result = add_access_path(result, rng, category)
    result = add_silhouette_polish(result, rng, category)
    result = add_category_signature(result, rng, category, name)
    result = add_lighting_nodes(result, rng, category, count=max(2, int(3 * multiplier)))

    # Determine story type from category
    story_map = {
        "ruined_plains": "survivor",
        "crash_zone_wasteland": "survivor",
        "ruined_cityscape": "military",
        "radiation_zone": "scientific",
        "toxic_swamp": "scientific",
        "industrial_ruins": "industrial",
        "cryogenic_ruins": "scientific",
        "global": "survivor",
    }
    story_type = story_map.get(category, "survivor")
    story_count = int(3 * multiplier)
    result = add_story_elements(result, rng, story_type, count=story_count)

    # Add loot containers (more for larger structures)
    loot_count = int(2 * multiplier)
    result = add_loot_containers(result, rng, container_count=loot_count)

    return _dedupe_blocks(result)
