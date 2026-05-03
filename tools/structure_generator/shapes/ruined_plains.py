"""
Ruined Plains POI shape generators.
"""

import random
from typing import List, Tuple, Optional, Dict

from palettes import PLAINS_BLOCKS, pick_from
from .poi_primitives import (
    add_block,
    add_signal_marker,
    add_supply_cluster,
    blob_patch,
    enforce_guardrails,
    fill,
    lean_to,
    line,
    ruined_wall,
    scatter,
    scrap_barricade,
    tent_frame,
)

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_nomad_camp(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    variant = seed % 3
    blob_patch(blocks, 7, 7, 7, 6, "minecraft:coarse_dirt")
    scatter(blocks, rng, 1, 1, 13, 13, 18, ["minecraft:gravel", "minecraft:hay_block", "minecraft:oak_log", "minecraft:cobblestone"])
    scrap_barricade(blocks, rng, 2, 11, 12, 9, ["minecraft:oak_fence", "minecraft:oak_log", "minecraft:cobblestone"])
    add_signal_marker(blocks, 12, 4, 4)

    if variant == 0:
        tent_frame(blocks, 3, 4, 5, 4, 4, "minecraft:white_wool")
        tent_frame(blocks, 8, 3, 4, 4, 3, "minecraft:white_wool")
    elif variant == 1:
        tent_frame(blocks, 4, 3, 4, 5, 4, "minecraft:white_wool")
        lean_to(blocks, 9, 7, 3, 4, "minecraft:oak_planks")
        add_signal_marker(blocks, 2, 4, 4)
    else:
        tent_frame(blocks, 3, 3, 4, 4, 3, "minecraft:white_wool")
        tent_frame(blocks, 8, 8, 4, 3, 3, "minecraft:white_wool")
        lean_to(blocks, 8, 3, 4, 3, "minecraft:oak_planks")

    add_block(blocks, 7, 0, 7, "minecraft:campfire")
    add_block(blocks, 10, 1, 5, "minecraft:cauldron")
    add_supply_cluster(blocks, rng, 4, 8, 3, 2, containers=2, clutter_palette=["minecraft:hay_block", "minecraft:oak_log", "minecraft:gravel"])
    add_block(blocks, 10, 1, 10, "minecraft:barrel")
    add_block(blocks, 5, 1, 4, "minecraft:chest")
    add_block(blocks, 11, 0, 4, "minecraft:hay_block")

    return enforce_guardrails("nomad_camp", blocks, 12, 12, 4, min_story_nodes=3)


def generate_windmill_ruin(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    base_x, base_z = 5, 5
    blob_patch(blocks, 7, 7, 7, 6, "minecraft:coarse_dirt")
    fill(blocks, base_x, 0, base_z, base_x + 3, 0, base_z + 3, "minecraft:cobblestone")
    ruined_wall(blocks, rng, base_x, base_z, base_x + 3, base_z + 3, "minecraft:cobblestone", min_height=4, max_height=8, gap_chance=0.15)
    for y in range(3, 8):
        add_block(blocks, base_x + 1, y, base_z + 1, "minecraft:oak_log")

    blade_y = 7
    line(blocks, (base_x + 1, blade_y, base_z + 1), (base_x + 5, blade_y + 1, base_z + 1), "minecraft:oak_planks")
    line(blocks, (base_x + 1, blade_y, base_z + 1), (base_x - 3, blade_y - 1, base_z + 1), "minecraft:oak_planks")
    line(blocks, (base_x + 1, blade_y, base_z + 1), (base_x + 1, blade_y + 1, base_z + 5), "minecraft:oak_planks")
    line(blocks, (base_x + 1, blade_y, base_z + 1), (base_x + 1, blade_y - 1, base_z - 3), "minecraft:oak_planks")
    add_signal_marker(blocks, 11, 4, 4)
    add_block(blocks, 3, 1, 9, "minecraft:campfire")
    add_block(blocks, 4, 1, 9, "minecraft:cauldron")
    scrap_barricade(blocks, rng, 2, 10, 11, 11, ["minecraft:oak_log", "minecraft:cobblestone", "minecraft:oak_fence"])
    add_supply_cluster(blocks, rng, 2, 4, 3, 2, containers=1, clutter_palette=["minecraft:hay_block", "minecraft:gravel", "minecraft:oak_log"])
    scatter(blocks, rng, 1, 1, 13, 13, 16, ["minecraft:gravel", "minecraft:cobblestone", "minecraft:oak_log"])

    return enforce_guardrails("windmill_ruin", blocks, 12, 12, 7, min_reward_nodes=1)


def generate_impact_crater(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    radius = rng.randint(3, 4)
    cx, cz = radius, radius
    for x in range(-radius, radius + 1):
        for z in range(-radius, radius + 1):
            dist = (x * x + z * z) ** 0.5
            if dist <= radius + 0.5:
                h = max(0, int(radius - dist) + rng.randint(-1, 0))
                for y in range(-h, 1):
                    if dist < radius * 0.5:
                        b = "minecraft:magma_block"
                    elif dist < radius * 0.8:
                        b = "minecraft:stone"
                    else:
                        b = "minecraft:coarse_dirt"
                    blocks.append((cx + x, y + 1, cz + z, b, None))
    return blocks


def generate_supply_drop(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    variant = seed % 3
    blob_patch(blocks, 7, 7, 6, 6, "minecraft:coarse_dirt")
    fill(blocks, 5, 0, 5, 8, 1, 8, "minecraft:oak_planks")
    add_block(blocks, 6, 2, 6, "minecraft:barrel")
    add_block(blocks, 7, 2, 6, "minecraft:chest")
    add_block(blocks, 6, 2, 7, "minecraft:barrel")

    if variant == 0:
        for x in range(3, 11):
            for z in range(3, 11):
                if abs(x - 7) + abs(z - 7) <= 6:
                    add_block(blocks, x, 6, z, "minecraft:white_wool")
        for px, pz in ((4, 4), (10, 4), (4, 10), (10, 10)):
            line(blocks, (px, 5, pz), (6 if px < 7 else 7, 2, 6 if pz < 7 else 7), "minecraft:oak_fence")
    elif variant == 1:
        for x in range(4, 10):
            for z in range(4, 10):
                if x in (4, 9) or z in (4, 9):
                    add_block(blocks, x, 5, z, "minecraft:white_wool")
        add_block(blocks, 3, 0, 6, "minecraft:campfire")
        add_block(blocks, 10, 0, 8, "minecraft:oak_log")
    else:
        for x in range(4, 10):
            for z in range(4, 10):
                if (x + z) % 2 == 0:
                    add_block(blocks, x, 5, z, "minecraft:white_wool")
        line(blocks, (4, 5, 4), (5, 2, 5), "minecraft:oak_fence")
        line(blocks, (9, 5, 4), (8, 2, 5), "minecraft:oak_fence")
        line(blocks, (4, 5, 9), (5, 2, 8), "minecraft:oak_fence")
        line(blocks, (9, 5, 9), (8, 2, 8), "minecraft:oak_fence")

    scatter(blocks, rng, 2, 2, 12, 12, 12, ["minecraft:gravel", "minecraft:oak_log", "minecraft:coarse_dirt"])
    add_signal_marker(blocks, 10, 3, 4)
    add_block(blocks, 4, 1, 10, "minecraft:cauldron")
    add_block(blocks, 3, 1, 9, "minecraft:campfire")
    add_block(blocks, 7, 1, 7, "minecraft:chest")  # Reward chest

    return enforce_guardrails("supply_drop", blocks, 11, 11, 6, min_story_nodes=3)


def generate_scavenger_camp(seed: int) -> BlockList:
    """Scavenger outpost with shacks and supplies."""
    rng = _r(seed)
    blocks: BlockList = []
    variant = seed % 3

    blob_patch(blocks, 8, 8, 8, 7, "minecraft:coarse_dirt")
    scrap_barricade(blocks, rng, 2, 12, 13, 11, ["minecraft:oak_fence", "minecraft:oak_log", "minecraft:cobblestone"])
    ruined_wall(blocks, rng, 2, 3, 13, 13, "minecraft:oak_fence", min_height=1, max_height=2, gap_chance=0.5)

    for x in range(4, 9):
        for z in range(4, 9):
            add_block(blocks, x, 0, z, "minecraft:oak_planks")
            if x in (4, 8) or z in (4, 8):
                if (x, z) not in ((6, 8), (7, 8)):
                    add_block(blocks, x, 1, z, "minecraft:oak_planks")
                    if rng.random() > 0.25:
                        add_block(blocks, x, 2, z, "minecraft:oak_planks")
    for x in range(4, 9):
        for z in range(4, 9):
            if rng.random() > 0.18:
                add_block(blocks, x, 3, z, "minecraft:oak_planks")

    if variant == 0:
        lean_to(blocks, 10, 5, 4, 3, "minecraft:white_wool")
        add_signal_marker(blocks, 12, 4, 4)
    elif variant == 1:
        lean_to(blocks, 10, 8, 3, 4, "minecraft:oak_planks")
        add_block(blocks, 12, 2, 12, "minecraft:oak_log")
        add_block(blocks, 12, 3, 12, "minecraft:oak_log")
        add_block(blocks, 12, 4, 12, "minecraft:lightning_rod")
    else:
        tent_frame(blocks, 10, 4, 4, 4, 3, "minecraft:white_wool")
        ruined_wall(blocks, rng, 9, 9, 13, 13, "minecraft:cobblestone", min_height=1, max_height=2, gap_chance=0.45)
        add_signal_marker(blocks, 4, 4, 4)

    add_block(blocks, 7, 0, 10, "minecraft:campfire")
    add_block(blocks, 10, 1, 4, "minecraft:cauldron")
    add_signal_marker(blocks, 13, 5, 4)
    add_supply_cluster(blocks, rng, 5, 5, 3, 3, containers=2, clutter_palette=["minecraft:gravel", "minecraft:oak_log", "minecraft:hay_block"])
    add_block(blocks, 11, 1, 9, "minecraft:barrel")
    add_block(blocks, 6, 1, 6, "minecraft:chest")
    scatter(blocks, rng, 2, 2, 14, 14, 20, [pick_from(PLAINS_BLOCKS, seed, i) for i in range(10)])

    return enforce_guardrails("scavenger_camp", blocks, 13, 13, 4, min_story_nodes=3)
