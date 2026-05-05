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

SUPPORT_SENSITIVE_EXACT_BLOCKS = {
    "minecraft:barrel",
    "minecraft:campfire",
    "minecraft:chest",
    "minecraft:dead_bush",
    "minecraft:grass",
    "minecraft:gravel",
    "minecraft:hay_block",
    "minecraft:lantern",
    "minecraft:lily_pad",
    "minecraft:magma_block",
    "minecraft:redstone_torch",
    "minecraft:snow",
    "minecraft:torch",
    "minecraft:trapped_chest",
    "echoashfallprotocol:acidic_sludge",
    "echoashfallprotocol:ash_bush",
    "echoashfallprotocol:ash_layer",
    "echoashfallprotocol:burnt_fern",
    "echoashfallprotocol:cable_bundle",
    "echoashfallprotocol:concrete_chunk",
    "echoashfallprotocol:concrete_rubble",
    "echoashfallprotocol:deep_ash",
    "echoashfallprotocol:dry_grass",
    "echoashfallprotocol:fallout_dust",
    "echoashfallprotocol:item_pipe",
    "echoashfallprotocol:power_cable",
    "echoashfallprotocol:rubble",
    "echoashfallprotocol:rusted_metal_debris",
    "echoashfallprotocol:rusted_metal_sheet",
    "echoashfallprotocol:scattered_bones",
    "echoashfallprotocol:toxic_moss",
    "echoashfallprotocol:toxic_puddle",
    "echoashfallprotocol:wasteland_trace_rubble",
}

SUPPORT_SENSITIVE_TOKENS = (
    "barrel",
    "bush",
    "cactus",
    "campfire",
    "cache",
    "crate",
    "debris",
    "dirt",
    "dust",
    "fern",
    "fungus",
    "grass",
    "lantern",
    "moss",
    "mud",
    "puddle",
    "reed",
    "rubble",
    "sapling",
    "sludge",
    "soil",
    "wheat",
)

TERRAIN_BLEND_TOKENS = (
    "aggregate",
    "ash",
    "crust",
    "dirt",
    "dust",
    "gravel",
    "moss",
    "mud",
    "permafrost",
    "puddle",
    "rubble",
    "slag",
    "sludge",
    "soil",
    "stone",
)

PIPE_AND_CABLE_TOKENS = (
    "cable",
    "conduit",
    "pipe",
)

INTENTIONAL_FLOATING_TEMPLATE_NAMES = {
    "floating_obelisk_cluster",
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


def is_support_sensitive_block(block_id: str) -> bool:
    """Return true for loose/grounded blocks that should never float."""
    if block_id == "minecraft:air":
        return False
    if block_id in SUPPORT_SENSITIVE_EXACT_BLOCKS:
        return True
    if any(token in block_id for token in SUPPORT_SENSITIVE_TOKENS):
        return True
    return block_id.startswith("echoashfallprotocol:") and any(token in block_id for token in PIPE_AND_CABLE_TOKENS)


def is_terrain_blend_block(block_id: str) -> bool:
    """Return true for blocks used as terrain apron or low blending material."""
    if block_id == "minecraft:air":
        return False
    if block_id in {"minecraft:coarse_dirt", "minecraft:gravel", "minecraft:snow", "minecraft:snow_block"}:
        return True
    return any(token in block_id for token in TERRAIN_BLEND_TOKENS)


def _is_pipe_or_cable(block_id: str) -> bool:
    return any(token in block_id for token in PIPE_AND_CABLE_TOKENS)


def _is_vegetation(block_id: str) -> bool:
    return any(token in block_id for token in ("bush", "fern", "grass", "sapling", "fungus", "cactus", "reed", "wheat"))


def _support_block_for(category: str, block_id: str, rng: random.Random) -> str:
    foundation = FOUNDATION_PALETTES.get(category, FOUNDATION_PALETTES["global"])
    retaining = RETAINING_PALETTES.get(category, RETAINING_PALETTES["global"])
    if _is_pipe_or_cable(block_id):
        return rng.choice(retaining)
    if _is_vegetation(block_id) or is_terrain_blend_block(block_id):
        return rng.choice(foundation)
    if "barrel" in block_id or "crate" in block_id or "chest" in block_id or block_id.endswith("station"):
        return rng.choice(foundation)
    return rng.choice(retaining if rng.random() < 0.35 else foundation)


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


def soften_bottom_outline(blocks: BlockList, rng: random.Random, category: str) -> BlockList:
    """Nibble perfectly flat apron edges without removing load-bearing cells."""
    deduped = _dedupe_blocks(blocks)
    bounds = _bounds(deduped)
    if not bounds:
        return deduped

    min_x, max_x, min_y, _, min_z, max_z = bounds
    width = max_x - min_x + 1
    depth = max_z - min_z + 1
    if width < 10 or depth < 10:
        return deduped

    by_pos = {(x, y, z): (block_id, props) for x, y, z, block_id, props in deduped}
    bottom = {
        (x, z)
        for x, y, z, block_id, _ in deduped
        if y == min_y and block_id != "minecraft:air"
    }
    if len(bottom) < 32:
        return deduped

    removable: set[Tuple[int, int]] = set()
    for x, z in bottom:
        block_id, _ = by_pos[(x, min_y, z)]
        if not is_terrain_blend_block(block_id):
            continue
        if (x, min_y + 1, z) in by_pos and by_pos[(x, min_y + 1, z)][0] != "minecraft:air":
            continue
        exposed_neighbors = sum(
            1
            for dx, dz in [(-1, 0), (1, 0), (0, -1), (0, 1)]
            if (x + dx, z + dz) not in bottom
        )
        on_outer_line = x in (min_x, max_x) or z in (min_z, max_z)
        if exposed_neighbors or on_outer_line:
            chance = 0.12 + (0.12 if on_outer_line else 0.0) + min(0.12, exposed_neighbors * 0.03)
            if rng.random() < chance:
                removable.add((x, z))

    if not removable:
        return deduped

    result = [
        (x, y, z, block_id, props)
        for x, y, z, block_id, props in deduped
        if not (y == min_y and (x, z) in removable)
    ]

    foundation = FOUNDATION_PALETTES.get(category, FOUNDATION_PALETTES["global"])
    for x, z in sorted(removable):
        if rng.random() < 0.45:
            for dx, dz in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                if (x + dx, z + dz) in bottom and (x + dx, z + dz) not in removable:
                    result.append((x, min_y, z, rng.choice(foundation), None))
                    break

    return _dedupe_blocks(result)


def apply_grounding_pass(
    blocks: BlockList,
    category: str,
    seed: int,
    name: str = "",
    soften_outline: bool = True,
) -> BlockList:
    """Add final supports and irregular terrain grounding to generated POIs."""
    rng = random.Random(seed + 4242)
    result = soften_bottom_outline(blocks, rng, category) if soften_outline else _dedupe_blocks(blocks)
    bounds = _bounds(result)
    if not bounds:
        return result

    _, _, min_y, _, _, _ = bounds
    intentional_float = name in INTENTIONAL_FLOATING_TEMPLATE_NAMES
    air_positions = {
        (x, y, z)
        for x, y, z, block_id, _ in result
        if block_id == "minecraft:air"
    }
    occupied: Dict[Tuple[int, int, int], Tuple[str, Optional[Dict[str, str]]]] = {
        (x, y, z): (block_id, props)
        for x, y, z, block_id, props in result
        if block_id != "minecraft:air"
    }

    def add_support(
        x: int,
        y: int,
        z: int,
        support_for: str,
        props: Optional[Dict[str, str]] = None,
        allow_air_override: bool = False,
    ) -> None:
        if (x, y, z) in occupied or ((x, y, z) in air_positions and not allow_air_override):
            return
        block_id = _support_block_for(category, support_for, rng)
        result.append((x, y, z, block_id, props))
        occupied[(x, y, z)] = (block_id, props)

    def support_column(x: int, y: int, z: int, support_for: str, allow_air_override: bool = False) -> None:
        for sy in range(min_y, y):
            add_support(x, sy, z, support_for, allow_air_override=allow_air_override)

    # Ground any low visible mass and every loose/detail block. This catches the
    # obvious "floating rubble/plant/cache" cases while preserving high ruined silhouettes.
    for (x, y, z), (block_id, _) in list(occupied.items()):
        if y <= min_y:
            continue
        has_below = (x, y - 1, z) in occupied
        if has_below:
            continue
        low_mass = y <= min_y + (3 if not intentional_float else 2)
        sensitive = is_support_sensitive_block(block_id)
        if sensitive or low_mass:
            support_column(x, y, z, block_id)

    # Add sparse posts under elevated pipes/cables and broad platforms so they read
    # as built objects, not loose blocks suspended over the terrain.
    for (x, y, z), (block_id, _) in list(occupied.items()):
        if y <= min_y + 1:
            continue
        if not _is_pipe_or_cable(block_id):
            continue
        if (x + z + seed) % 3 == 0 or (x, y - 1, z) not in occupied:
            support_column(x, y, z, block_id)

    # Rebuild occupancy after support posts, then prune decorative singletons that
    # still cannot be grounded because an intentional air carve-out is below them.
    result = _dedupe_blocks(result)
    occupied = {
        (x, y, z): (block_id, props)
        for x, y, z, block_id, props in result
        if block_id != "minecraft:air"
    }
    cleaned: BlockList = []
    for x, y, z, block_id, props in result:
        if block_id != "minecraft:air" and y > min_y and (x, y - 1, z) not in occupied:
            has_supported_detail_above = (x, y + 1, z) in occupied
            if (
                is_support_sensitive_block(block_id)
                and (_is_vegetation(block_id) or is_terrain_blend_block(block_id))
                and not has_supported_detail_above
            ):
                continue
        cleaned.append((x, y, z, block_id, props))

    result = _dedupe_blocks(cleaned)
    occupied = {
        (x, y, z): (block_id, props)
        for x, y, z, block_id, props in result
        if block_id != "minecraft:air"
    }
    # Final strict pass: if a loose detail still floats above an intentional air
    # carve-out, prefer a small rubble/support post over leaving it suspended.
    for (x, y, z), (block_id, _) in list(occupied.items()):
        if y > min_y and (x, y - 1, z) not in occupied and is_support_sensitive_block(block_id):
            support_column(x, y, z, block_id, allow_air_override=True)

    return _dedupe_blocks(result)


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


def add_access_path(
    blocks: BlockList,
    rng: random.Random,
    category: str,
    route_count: int = 1,
    route_length: int = 7,
) -> BlockList:
    """Add readable approach paths without ruler-straight footprints."""
    bounds = _bounds(blocks)
    if not bounds:
        return blocks
    min_x, max_x, min_y, _, min_z, max_z = bounds
    cx = (min_x + max_x) // 2
    cz = (min_z + max_z) // 2
    path_block = PATH_BLOCKS.get(category, PATH_BLOCKS["global"])
    result = blocks[:]

    directions = ["south", "west", "north", "east"]
    rng.shuffle(directions)
    foundation = FOUNDATION_PALETTES.get(category, FOUNDATION_PALETTES["global"])
    for direction in directions[:max(1, route_count)]:
        length = max(5, route_length + rng.randint(-1, 2))
        for step in range(length):
            drift = rng.choice([-1, 0, 0, 1])
            if direction == "south":
                x, z = cx + drift, max_z + 1 + step
                cross_axis = "x"
            elif direction == "north":
                x, z = cx + drift, min_z - 1 - step
                cross_axis = "x"
            elif direction == "east":
                x, z = max_x + 1 + step, cz + drift
                cross_axis = "z"
            else:
                x, z = min_x - 1 - step, cz + drift
                cross_axis = "z"

            for offset in (-1, 0, 1):
                if abs(offset) == 1 and rng.random() < 0.35:
                    continue
                px, pz = (x + offset, z) if cross_axis == "x" else (x, z + offset)
                result.append((px, min_y, pz, path_block, None))

            if step % 2 == 0:
                edge_offset = rng.choice([-2, 2])
                px, pz = (x + edge_offset, z) if cross_axis == "x" else (x, z + edge_offset)
                result.append((px, min_y, pz, rng.choice(foundation), None))

        # Put a small marker at the point where the route meets the structure.
        if direction == "south":
            marker_x, marker_z = cx, max_z
        elif direction == "north":
            marker_x, marker_z = cx, min_z
        elif direction == "east":
            marker_x, marker_z = max_x, cz
        else:
            marker_x, marker_z = min_x, cz
        result.append((marker_x, min_y + 1, marker_z, rng.choice(LIGHTING_NODES.get(category, LIGHTING_NODES["global"])), None))
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


def _profile_value(profile: Optional[Dict[str, object]], key: str, fallback: str) -> str:
    if not profile:
        return fallback
    value = profile.get(key)
    return str(value) if value is not None else fallback


def _profile_bool(profile: Optional[Dict[str, object]], key: str) -> bool:
    if not profile:
        return False
    return bool(profile.get(key))


def _interior_floor_positions(blocks: BlockList) -> List[Tuple[int, int]]:
    occupied = {(x, y, z) for x, y, z, bid, _ in blocks if bid != "minecraft:air"}
    return [
        (x, z)
        for x, y, z, bid, _ in blocks
        if y == 0 and bid != "minecraft:air" and (x, 1, z) not in occupied
    ]


def add_route_role_set_piece(
    blocks: BlockList,
    rng: random.Random,
    category: str,
    name: str,
    profile: Optional[Dict[str, object]] = None,
) -> BlockList:
    """Add a readable set piece tied to category and route role."""
    bounds = _bounds(blocks)
    if not bounds:
        return blocks
    min_x, max_x, min_y, max_y, min_z, max_z = bounds
    cx = (min_x + max_x) // 2
    cz = (min_z + max_z) // 2
    base_y = min_y + 1
    role = _profile_value(profile, "route_role", "route")
    result = blocks[:]

    def add(x: int, y: int, z: int, block: str, props: Optional[Dict[str, str]] = None) -> None:
        result.append((x, y, z, block, props))

    def line(start_x: int, start_z: int, dx: int, dz: int, length: int, block: str, y: int = base_y) -> None:
        for step in range(length):
            add(start_x + dx * step, y, start_z + dz * step, block)

    if category == "crash_zone_wasteland":
        rib_x = min(max_x - 2, cx + 2)
        for y in range(base_y, base_y + 4):
            add(rib_x, y, cz - 2, "echoashfallprotocol:drop_pod_hull")
            add(rib_x, y, cz + 2, "echoashfallprotocol:drop_pod_hull")
        for z in range(cz - 2, cz + 3):
            add(rib_x, base_y + 4, z, "echoashfallprotocol:rusted_metal_sheet")
        line(min_x + 1, cz, 1, 0, max(4, min(10, max_x - min_x - 1)), "echoashfallprotocol:power_cable", base_y)
        add(cx - 2, base_y, cz + 2, "echoashfallprotocol:supply_crate")
    elif category == "ruined_plains":
        line(cx - 3, max_z - 1, 1, 0, 7, "minecraft:gravel", min_y)
        for x in range(cx - 2, cx + 3, 2):
            add(x, base_y, max_z - 2, "minecraft:campfire", {"lit": "false", "facing": "north", "signal_fire": "false", "waterlogged": "false"})
        add(cx + 2, base_y, cz, "echoashfallprotocol:rain_collector")
        add(cx - 2, base_y, cz, "echoashfallprotocol:map_table")
    elif category == "ruined_cityscape":
        stair_x = min_x + 2
        for step in range(5):
            add(stair_x + step, base_y + step, min_z + 2, "minecraft:stone_brick_stairs", {"facing": "east", "half": "bottom", "shape": "straight", "waterlogged": "false"})
            add(stair_x + step, base_y + step - 1, min_z + 2, "echoashfallprotocol:concrete_rubble")
        line(cx - 4, cz, 1, 0, 9, "echoashfallprotocol:power_cable", min(max_y, base_y + 3))
        add(max_x - 2, base_y, cz + 1, "echoashfallprotocol:signal_scanner")
    elif category == "industrial_ruins":
        gantry_y = min(max_y + 1, base_y + 5)
        line(min_x + 1, cz, 1, 0, max(6, max_x - min_x - 1), "minecraft:iron_bars", gantry_y)
        for x in range(min_x + 1, max_x, 4):
            for y in range(base_y, gantry_y):
                add(x, y, cz, "echoashfallprotocol:rusted_metal_sheet")
        add(cx, base_y, cz - 2, "echoashfallprotocol:factory_controller")
        add(cx + 2, base_y, cz - 1, "echoashfallprotocol:scrap_press")
    elif category == "toxic_swamp":
        line(min_x + 1, cz, 1, 0, max(6, max_x - min_x - 1), "minecraft:oak_planks", min_y)
        for x in range(min_x + 2, max_x, 4):
            add(x, base_y, cz - 1, "echoashfallprotocol:corroded_pipe")
            add(x, base_y, cz + 1, "echoashfallprotocol:corroded_pipe")
        add(cx, min_y, cz + 3, "echoashfallprotocol:acidic_sludge")
        add(cx + 1, min_y, cz + 3, "echoashfallprotocol:toxic_puddle")
    elif category == "radiation_zone":
        for dx, dz in [(-3, -3), (3, -3), (-3, 3), (3, 3)]:
            add(cx + dx, base_y, cz + dz, "echoashfallprotocol:radiation_block")
            add(cx + dx, base_y + 1, cz + dz, "minecraft:redstone_torch", {"lit": "true"})
        for x in range(cx - 4, cx + 5):
            if abs(x - cx) in (0, 4) or rng.random() < 0.45:
                add(x, min_y, cz, "echoashfallprotocol:fallout_dust")
        add(cx, base_y, cz, "echoashfallprotocol:toxic_waste_barrel")
    elif category == "cryogenic_ruins":
        for y in range(base_y, base_y + 6):
            add(cx - 2, y, cz, "echoashfallprotocol:frozen_conduit")
            add(cx + 2, y, cz, "echoashfallprotocol:frozen_conduit")
        for x in range(cx - 3, cx + 4):
            add(x, min_y, cz + 2, rng.choice(["minecraft:blue_ice", "minecraft:packed_ice"]))
        add(cx, base_y, cz, "echoashfallprotocol:thermal_array")
    elif category == "nexus_scar":
        radius = 4
        for dx, dz in [(-radius, 0), (radius, 0), (0, -radius), (0, radius), (-3, -3), (3, 3)]:
            add(cx + dx, min_y, cz + dz, "echoashfallprotocol:energized_fissure")
        for y in range(base_y, base_y + 7):
            add(cx, y, cz, "minecraft:crying_obsidian")
        add(cx, base_y + 7, cz, "echoashfallprotocol:echo_crystal")
    elif category == "faction":
        pad_block = "minecraft:smooth_stone" if name.startswith("remnant_outpost/") else "minecraft:gravel"
        if name.startswith("mutant_sanctuary/"):
            pad_block = "echoashfallprotocol:toxic_moss"
        for dx in range(-2, 3):
            for dz in range(-1, 2):
                if rng.random() < 0.85:
                    add(cx + dx, min_y, cz + dz, pad_block)
        add(cx - 2, base_y, cz, "echoashfallprotocol:map_table")
        add(cx, base_y, cz, "echoashfallprotocol:trade_counter")
        add(cx + 2, base_y, cz, "echoashfallprotocol:weapon_rack")
        if _profile_bool(profile, "faction"):
            add(cx, base_y, cz + 2, "echoashfallprotocol:power_node")
    else:
        add(cx, base_y, cz, "echoashfallprotocol:supply_crate")
        add(cx + 2, base_y, cz, "echoashfallprotocol:rain_collector")

    if role in {"hub", "faction_hub", "camp"}:
        add(cx, base_y, max_z - 2, "echoashfallprotocol:map_table")
    elif role in {"cache", "salvage"}:
        add(cx, base_y, max(min_z + 1, cz - 2), "echoashfallprotocol:supply_crate")
    elif role in {"hazard", "anomaly"}:
        add(cx + 1, base_y, cz + 1, "minecraft:redstone_torch", {"lit": "true"})

    return result


def add_hazard_pockets(
    blocks: BlockList,
    rng: random.Random,
    category: str,
    hazard_tier: str = "low",
) -> BlockList:
    """Place hazards as readable risk/reward pockets instead of random noise."""
    if hazard_tier in {"none", "safe"}:
        return blocks
    bounds = _bounds(blocks)
    if not bounds:
        return blocks
    min_x, max_x, min_y, _, min_z, max_z = bounds
    cx = (min_x + max_x) // 2
    cz = (min_z + max_z) // 2
    result = blocks[:]

    palettes = {
        "crash_zone_wasteland": ["minecraft:magma_block", "echoashfallprotocol:scorched_ash"],
        "industrial_ruins": ["echoashfallprotocol:oil_stained_concrete", "echoashfallprotocol:rusted_metal_debris"],
        "ruined_cityscape": ["echoashfallprotocol:oil_stained_concrete", "echoashfallprotocol:concrete_rubble"],
        "radiation_zone": ["echoashfallprotocol:fallout_dust", "echoashfallprotocol:radiation_block"],
        "toxic_swamp": ["echoashfallprotocol:acidic_sludge", "echoashfallprotocol:toxic_puddle"],
        "cryogenic_ruins": ["minecraft:blue_ice", "minecraft:powder_snow"],
        "nexus_scar": ["echoashfallprotocol:riftstone", "echoashfallprotocol:energized_fissure"],
    }
    palette = palettes.get(category)
    if not palette:
        return blocks

    cluster_counts = {"low": 1, "medium": 2, "high": 3, "extreme": 4, "anomaly": 4}
    clusters = cluster_counts.get(hazard_tier, 1)
    anchors = [
        (min_x + 2, min_z + 2),
        (max_x - 2, min_z + 2),
        (min_x + 2, max_z - 2),
        (max_x - 2, max_z - 2),
        (cx + 3, cz),
        (cx - 3, cz),
    ]
    rng.shuffle(anchors)
    for ax, az in anchors[:clusters]:
        radius = 1 if hazard_tier in {"low", "medium"} else 2
        for dx in range(-radius, radius + 1):
            for dz in range(-radius, radius + 1):
                if abs(dx) + abs(dz) > radius + 1 or rng.random() < 0.18:
                    continue
                result.append((ax + dx, min_y, az + dz, rng.choice(palette), None))
        if category in {"radiation_zone", "toxic_swamp"}:
            result.append((ax, min_y + 1, az, "echoashfallprotocol:toxic_waste_barrel", None))
        elif category == "nexus_scar":
            result.append((ax, min_y + 1, az, "echoashfallprotocol:echo_crystal", None))
        elif category == "cryogenic_ruins":
            result.append((ax, min_y + 1, az, "echoashfallprotocol:blue_ice_crystal", None))

    return result


def add_sheltered_cache_pads(
    blocks: BlockList,
    rng: random.Random,
    category: str,
    loot_tier: str = "low",
    route_role: str = "route",
) -> BlockList:
    """Place caches on visible supported pads with a small bit of shelter."""
    bounds = _bounds(blocks)
    if not bounds:
        return blocks
    min_x, max_x, min_y, _, min_z, max_z = bounds
    floor_positions = _interior_floor_positions(blocks)
    if not floor_positions:
        floor_positions = [
            (x, z)
            for x, y, z, bid, _ in blocks
            if y == min_y and bid != "minecraft:air"
        ]
    if not floor_positions:
        return blocks

    tier_counts = {"low": 1, "medium": 2, "high": 3, "landmark": 3, "faction": 2}
    count = tier_counts.get(loot_tier, 1)
    if route_role in {"landmark", "anomaly"}:
        count = max(count, 2)
    rng.shuffle(floor_positions)
    result = blocks[:]
    foundation = FOUNDATION_PALETTES.get(category, FOUNDATION_PALETTES["global"])
    shelter_block = rng.choice(RETAINING_PALETTES.get(category, RETAINING_PALETTES["global"]))
    containers = ["minecraft:chest", "minecraft:barrel"]
    if category == "faction":
        containers = ["echoashfallprotocol:supply_crate", "minecraft:barrel"]

    placed = 0
    for x, z in floor_positions:
        near_edge = x <= min_x + 2 or x >= max_x - 2 or z <= min_z + 2 or z >= max_z - 2
        if not near_edge and placed == 0 and rng.random() < 0.65:
            continue
        result.append((x, min_y, z, rng.choice(foundation), None))
        result.append((x, min_y + 1, z, rng.choice(containers), None))
        for dx, dz in [(-1, 0), (1, 0), (0, -1)]:
            if rng.random() < 0.7:
                result.append((x + dx, min_y + 1, z + dz, shelter_block, None))
        placed += 1
        if placed >= count:
            break

    return result


def apply_detail_pass(
    blocks: BlockList,
    category: str,
    biome: str,
    seed: int,
    structure_size: str = "small",
    name: str = "",
    profile: Optional[Dict[str, object]] = None,
) -> BlockList:
    """Apply complete detail pass to structure.

    Args:
        blocks: Base structure blocks
        category: Structure category (e.g., "ruined_plains", "crash_zone")
        biome: Biome type for vegetation
        seed: Random seed
        structure_size: "small", "medium", "big", or "landmark" - affects detail intensity
    """
    rng = random.Random(seed + 999)  # Offset seed for detail pass
    if profile:
        structure_size = _profile_value(profile, "size", structure_size)
    route_role = _profile_value(profile, "route_role", "route")
    loot_tier = _profile_value(profile, "loot_tier", "low")
    hazard_tier = _profile_value(profile, "hazard_tier", "low")

    # Adjust intensity by size
    size_multipliers = {
        "small": 0.7,
        "medium": 1.0,
        "big": 1.3,
        "landmark": 1.55,
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

    # Add readable approach paths, skyline anchors, and role-specific set pieces.
    route_count = 1
    if structure_size in {"medium", "big", "landmark"}:
        route_count += 1
    if route_role in {"hub", "faction_hub", "landmark", "anomaly"}:
        route_count += 1
    result = add_access_path(result, rng, category, route_count=min(route_count, 3), route_length=max(6, int(7 * multiplier)))
    result = add_silhouette_polish(result, rng, category)
    result = add_category_signature(result, rng, category, name)
    result = add_route_role_set_piece(result, rng, category, name, profile)
    result = add_hazard_pockets(result, rng, category, hazard_tier=hazard_tier)
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

    # Add loot containers (more for larger structures) and make at least one cache visibly sheltered.
    loot_count = int(2 * multiplier)
    result = add_loot_containers(result, rng, container_count=loot_count)
    result = add_sheltered_cache_pads(result, rng, category, loot_tier=loot_tier, route_role=route_role)

    result = apply_grounding_pass(result, category, seed, name)

    return _dedupe_blocks(result)
