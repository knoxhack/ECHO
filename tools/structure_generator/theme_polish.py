"""
Post-process generated POIs with ECHO: Ashfall Protocol thematic detailing.

The base generators define silhouettes. This pass keeps those silhouettes and
adds deterministic custom-block accents so every template has modpack identity
without becoming visually noisy.
"""

from __future__ import annotations

import random
from typing import Any, Dict, List, Optional, Tuple, Union

BlockEntry = Union[
    Tuple[int, int, int, str, Optional[Dict[str, str]]],
    Tuple[int, int, int, str, Optional[Dict[str, str]], Optional[Dict[str, Any]]],
]
BlockList = List[BlockEntry]


THEMES = {
    "crash_zone_wasteland": {
        "ground": ["echoashfallprotocol:scorched_ash", "echoashfallprotocol:burnt_wasteland_soil", "echoashfallprotocol:rubble"],
        "debris": ["echoashfallprotocol:debris_block", "echoashfallprotocol:rusted_metal_debris", "echoashfallprotocol:drop_pod_hull"],
        "story": ["echoashfallprotocol:drop_pod_glass", "echoashfallprotocol:power_cable", "minecraft:campfire"],
    },
    "global": {
        "ground": ["echoashfallprotocol:ashen_wasteland_dirt", "echoashfallprotocol:rubble", "echoashfallprotocol:wasteland_trace_rubble"],
        "debris": ["echoashfallprotocol:debris_block", "echoashfallprotocol:rusted_metal_debris", "echoashfallprotocol:concrete_chunk"],
        "story": ["echoashfallprotocol:rain_collector", "echoashfallprotocol:supply_crate", "echoashfallprotocol:scattered_bones"],
    },
    "radiation_zone": {
        "ground": ["echoashfallprotocol:fallout_dust", "echoashfallprotocol:contaminated_soil", "echoashfallprotocol:nuclear_grass"],
        "debris": ["echoashfallprotocol:toxic_waste_barrel", "echoashfallprotocol:rusted_metal_debris", "echoashfallprotocol:concrete_rubble"],
        "story": ["echoashfallprotocol:radiation_block", "echoashfallprotocol:contaminant_condenser"],
    },
    "toxic_swamp": {
        "ground": ["echoashfallprotocol:toxic_puddle", "echoashfallprotocol:acidic_sludge", "echoashfallprotocol:contaminated_soil"],
        "debris": ["echoashfallprotocol:toxic_waste_barrel", "echoashfallprotocol:toxic_moss", "echoashfallprotocol:mutated_bush"],
        "story": ["echoashfallprotocol:bio_processing_station", "echoashfallprotocol:contaminant_condenser"],
    },
    "industrial_ruins": {
        "ground": ["echoashfallprotocol:oil_stained_concrete", "echoashfallprotocol:concrete_rubble", "echoashfallprotocol:rusted_metal_sheet"],
        "debris": ["echoashfallprotocol:item_pipe", "echoashfallprotocol:power_cable", "echoashfallprotocol:rusted_metal_debris"],
        "story": ["echoashfallprotocol:scrap_press", "echoashfallprotocol:battery_bank", "echoashfallprotocol:factory_controller"],
    },
    "ruined_cityscape": {
        "ground": ["echoashfallprotocol:oil_stained_concrete", "echoashfallprotocol:concrete_rubble", "echoashfallprotocol:rubble"],
        "debris": ["echoashfallprotocol:rusted_metal_sheet", "echoashfallprotocol:concrete_chunk", "echoashfallprotocol:power_cable"],
        "story": ["echoashfallprotocol:signal_scanner", "echoashfallprotocol:research_lab", "echoashfallprotocol:field_med_bay"],
    },
    "cryogenic_ruins": {
        "ground": ["echoashfallprotocol:deep_ash", "echoashfallprotocol:concrete_chunk", "minecraft:blue_ice"],
        "debris": ["echoashfallprotocol:rusted_metal_debris", "echoashfallprotocol:concrete_rubble", "echoashfallprotocol:power_cable"],
        "story": ["echoashfallprotocol:thermal_array", "echoashfallprotocol:research_lab", "echoashfallprotocol:contaminant_condenser"],
    },
    "ruined_plains": {
        "ground": ["echoashfallprotocol:wasteland_dirt", "echoashfallprotocol:ashen_wasteland_dirt", "echoashfallprotocol:rubble"],
        "debris": ["echoashfallprotocol:dead_wood_log", "echoashfallprotocol:charred_wood_log", "echoashfallprotocol:rusted_metal_debris"],
        "story": ["echoashfallprotocol:rain_collector", "echoashfallprotocol:wild_berry_bush", "echoashfallprotocol:supply_crate"],
    },
    "nexus_scar": {
        "ground": ["echoashfallprotocol:riftstone", "echoashfallprotocol:nexus_cracked_soil", "echoashfallprotocol:energized_fissure"],
        "debris": ["echoashfallprotocol:echo_crystal", "minecraft:crying_obsidian", "minecraft:obsidian"],
        "story": ["echoashfallprotocol:nexus_core", "echoashfallprotocol:nexus_capacitor", "echoashfallprotocol:crystalline_synthesizer"],
    },
}

FACTION_THEMES = {
    "remnant_outpost": {
        "ground": ["echoashfallprotocol:concrete_rubble", "echoashfallprotocol:rusted_metal_sheet", "echoashfallprotocol:power_cable"],
        "debris": ["echoashfallprotocol:weapon_rack", "echoashfallprotocol:supply_crate", "echoashfallprotocol:rusted_metal_debris"],
        "story": ["echoashfallprotocol:relay_station", "echoashfallprotocol:battery_bank", "echoashfallprotocol:power_node"],
    },
    "salvager_post": {
        "ground": ["echoashfallprotocol:rubble", "echoashfallprotocol:concrete_chunk", "echoashfallprotocol:ash_layer"],
        "debris": ["echoashfallprotocol:supply_crate", "echoashfallprotocol:rusted_metal_debris", "echoashfallprotocol:rain_collector"],
        "story": ["echoashfallprotocol:trade_counter", "echoashfallprotocol:map_table", "echoashfallprotocol:rain_collector"],
    },
    "mutant_sanctuary": {
        "ground": ["echoashfallprotocol:toxic_moss", "echoashfallprotocol:toxic_puddle", "echoashfallprotocol:acidic_sludge"],
        "debris": ["echoashfallprotocol:mutated_bush", "echoashfallprotocol:nuclear_fungus", "echoashfallprotocol:mutated_sapling"],
        "story": ["echoashfallprotocol:bio_processing_station", "echoashfallprotocol:spore_garden", "echoashfallprotocol:toxic_waste_barrel"],
    },
}

GROUND_REPLACE = {
    "minecraft:coarse_dirt",
    "minecraft:gravel",
    "minecraft:dirt",
    "minecraft:mossy_cobblestone",
    "minecraft:green_terracotta",
}

DEBRIS_REPLACE = {
    "minecraft:iron_block",
    "minecraft:smooth_stone",
    "minecraft:stone_bricks",
    "minecraft:cobblestone",
    "minecraft:oak_log",
    "minecraft:scattered_bones",
}

BRIGHT_REPLACE = {
    "minecraft:white_wool",
    "minecraft:bone_block",
    "minecraft:quartz_block",
    "minecraft:quartz_pillar",
}


def apply_theme_polish(name: str, category: str, blocks: BlockList, seed: int) -> BlockList:
    theme = _theme_for(name, category)
    if not theme or not blocks:
        return blocks

    rng = random.Random(seed ^ 0xA07A5EED)
    polished: BlockList = []

    for entry in blocks:
        x, y, z, block_id, props, be_nbt = _unpack(entry)
        replacement = _replacement_for(block_id, y, theme, rng)
        polished.append(_pack(x, y, z, replacement, props, be_nbt))

    _add_signature_details(polished, name, category, theme, rng)
    return _dedupe_entries(polished)


def _theme_for(name: str, category: str):
    if category == "faction":
        faction = name.split("/", 1)[0]
        return FACTION_THEMES.get(faction)
    return THEMES.get(category)


def _replacement_for(block_id: str, y: int, theme, rng: random.Random) -> str:
    if block_id == "minecraft:air":
        return block_id
    if block_id == "minecraft:scattered_bones":
        return "echoashfallprotocol:scattered_bones"
    if block_id in BRIGHT_REPLACE and rng.random() < 0.85:
        return rng.choice(["minecraft:light_gray_wool", "minecraft:gray_wool", "echoashfallprotocol:concrete_chunk"])
    if y <= 0 and block_id in GROUND_REPLACE and rng.random() < 0.28:
        return rng.choice(theme["ground"])
    if block_id in DEBRIS_REPLACE and rng.random() < 0.14:
        return rng.choice(theme["debris"])
    if block_id == "minecraft:barrel" and rng.random() < 0.20:
        return "echoashfallprotocol:supply_crate"
    if block_id == "minecraft:cauldron" and rng.random() < 0.25:
        return rng.choice(theme["story"])
    if block_id == "minecraft:campfire":
        return block_id
    if block_id == "minecraft:iron_bars" and rng.random() < 0.12:
        return "echoashfallprotocol:item_pipe"
    return block_id


def _add_signature_details(blocks: BlockList, name: str, category: str, theme, rng: random.Random) -> None:
    solid = [_unpack(entry) for entry in blocks if _unpack(entry)[3] != "minecraft:air"]
    if not solid:
        return

    xs = [entry[0] for entry in solid]
    ys = [entry[1] for entry in solid]
    zs = [entry[2] for entry in solid]
    min_x, max_x = min(xs), max(xs)
    min_y, max_y = min(ys), max(ys)
    min_z, max_z = min(zs), max(zs)
    width = max(1, max_x - min_x + 1)
    depth = max(1, max_z - min_z + 1)
    area = width * depth

    occupied = {(entry[0], entry[2]) for entry in solid if entry[1] == min_y}
    edge_positions = []
    for x, z in occupied:
        for dx, dz in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            neighbor = (x + dx, z + dz)
            if neighbor not in occupied:
                edge_positions.append(neighbor)

    scatter_count = max(3, min(18, area // 12))
    if edge_positions:
        rng.shuffle(edge_positions)
        for x, z in edge_positions[:scatter_count]:
            blocks.append((x, min_y, z, rng.choice(theme["ground"] + theme["debris"]), None))
            if rng.random() < 0.18:
                blocks.append((x, min_y + 1, z, rng.choice(theme["debris"]), None))

    anchors = [
        (min_x + 1, min_z + 1),
        (max_x - 1, min_z + 1),
        (min_x + 1, max_z - 1),
        (max_x - 1, max_z - 1),
        ((min_x + max_x) // 2, (min_z + max_z) // 2),
    ]
    rng.shuffle(anchors)
    for x, z in anchors[: max(1, min(3, area // 30 + 1))]:
        blocks.append((x, min_y + 1, z, rng.choice(theme["story"]), None))

    if category in {"industrial_ruins", "ruined_cityscape", "radiation_zone", "cryogenic_ruins"}:
        y = min(max_y, min_y + 2)
        z = rng.randint(min_z, max_z)
        for x in range(min_x, max_x + 1, 2):
            blocks.append((x, y, z, "echoashfallprotocol:power_cable", None))

    if category == "toxic_swamp":
        cx, cz = (min_x + max_x) // 2, (min_z + max_z) // 2
        for dx, dz in ((0, 0), (1, 0), (0, 1), (-1, 0), (0, -1)):
            blocks.append((cx + dx, min_y, cz + dz, rng.choice(["echoashfallprotocol:toxic_puddle", "echoashfallprotocol:acidic_sludge"]), None))

    if category == "crash_zone_wasteland":
        z = rng.randint(min_z, max_z)
        for x in range(min_x, max_x + 1, 3):
            blocks.append((x, min_y + 1, z, rng.choice(["echoashfallprotocol:power_cable", "echoashfallprotocol:cable_bundle"]), None))
    elif category == "ruined_plains":
        cx, cz = (min_x + max_x) // 2, (min_z + max_z) // 2
        blocks.append((cx, min_y + 1, cz, "echoashfallprotocol:rain_collector", None))
        blocks.append((cx + 1, min_y + 1, cz, "echoashfallprotocol:wild_berry_bush", None))
    elif category == "industrial_ruins":
        x = rng.randint(min_x, max_x)
        for z in range(min_z, max_z + 1, 2):
            blocks.append((x, min_y + 2, z, "echoashfallprotocol:item_pipe", None))
    elif category == "radiation_zone":
        cx, cz = (min_x + max_x) // 2, (min_z + max_z) // 2
        blocks.append((cx, min_y + 1, cz, "echoashfallprotocol:radiation_block", None))
        blocks.append((cx + 1, min_y + 1, cz, "echoashfallprotocol:toxic_waste_barrel", None))
    elif category == "cryogenic_ruins":
        cx, cz = (min_x + max_x) // 2, (min_z + max_z) // 2
        blocks.append((cx, min_y + 1, cz, "echoashfallprotocol:frozen_conduit", None))
        blocks.append((cx, min_y + 2, cz, "echoashfallprotocol:blue_ice_crystal", None))
    elif category == "nexus_scar":
        cx, cz = (min_x + max_x) // 2, (min_z + max_z) // 2
        blocks.append((cx, min_y, cz, "echoashfallprotocol:riftstone", None))
        blocks.append((cx, min_y + 1, cz, "echoashfallprotocol:echo_crystal", None))

    if category == "faction":
        if name.startswith("remnant_outpost/"):
            cx = (min_x + max_x) // 2
            blocks.append((cx, min_y + 1, min_z, "echoashfallprotocol:weapon_rack", None))
            blocks.append((cx - 1, min_y + 1, min_z, "echoashfallprotocol:supply_crate", None))
            blocks.append((cx + 1, min_y + 1, min_z, "echoashfallprotocol:power_node", None))
            for x in range(min_x, max_x + 1, 3):
                blocks.append((x, min_y + 1, max_z, "minecraft:iron_bars", None))
        elif name.startswith("salvager_post/"):
            cx = (min_x + max_x) // 2
            blocks.append((cx, min_y + 1, min_z, "echoashfallprotocol:trade_counter", None))
            blocks.append((cx - 1, min_y + 1, min_z + 1, "echoashfallprotocol:map_table", None))
            blocks.append((cx + 1, min_y + 1, min_z + 1, "echoashfallprotocol:rain_collector", None))
            for x in range(min_x + 1, max_x, 4):
                blocks.append((x, min_y + 1, max_z - 1, "echoashfallprotocol:supply_crate", None))
        elif name.startswith("mutant_sanctuary/"):
            cx, cz = (min_x + max_x) // 2, (min_z + max_z) // 2
            blocks.append((cx, min_y + 1, min_z, "echoashfallprotocol:spore_garden", None))
            blocks.append((cx, min_y, cz, "echoashfallprotocol:toxic_moss", None))
            for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                blocks.append((cx + dx, min_y + 1, cz + dz, "echoashfallprotocol:mutated_bush", None))
            blocks.append((cx, min_y + 1, cz, "echoashfallprotocol:bio_processing_station", None))


def _unpack(entry: BlockEntry):
    if len(entry) == 6:
        return entry  # type: ignore[return-value]
    x, y, z, block_id, props = entry  # type: ignore[misc]
    return x, y, z, block_id, props, None


def _pack(x: int, y: int, z: int, block_id: str, props, be_nbt):
    if be_nbt:
        return (x, y, z, block_id, props, be_nbt)
    return (x, y, z, block_id, props)


def _dedupe_entries(blocks: BlockList) -> BlockList:
    deduped = {}
    for entry in blocks:
        x, y, z, block_id, props, be_nbt = _unpack(entry)
        normalized_props = dict(sorted(props.items())) if props else None
        deduped[(x, y, z)] = (block_id, normalized_props, be_nbt)

    result: BlockList = []
    for (x, y, z), (block_id, props, be_nbt) in sorted(deduped.items(), key=lambda item: (item[0][1], item[0][0], item[0][2])):
        result.append(_pack(x, y, z, block_id, props, be_nbt))
    return result
