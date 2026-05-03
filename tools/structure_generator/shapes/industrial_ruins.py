"""
Industrial Ruins POI shape generators.
"""

import random
from typing import List, Tuple, Optional, Dict

from palettes import INDUSTRIAL_BLOCKS, pick_from

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_conveyor_ruin(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    length = rng.randint(8, 10)
    for x in range(length):
        # Belt (smooth stone slab-like, but we use full blocks)
        blocks.append((x, 1, 1, "minecraft:smooth_stone", None))
        # Side rails
        blocks.append((x, 2, 0, "minecraft:iron_bars", None))
        blocks.append((x, 2, 2, "minecraft:iron_bars", None))
        # Supports
        if x % 4 == 0:
            for y in range(2):
                blocks.append((x, y, 1, "minecraft:stone_bricks", None))
    return blocks


def generate_storage_yard(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    for x in range(6):
        for z in range(6):
            blocks.append((x, 0, z, "minecraft:smooth_stone", None))
    # Stacks of crates (barrels and planks)
    for _ in range(rng.randint(6, 10)):
        cx = rng.randint(1, 4)
        cz = rng.randint(1, 4)
        h = rng.randint(1, 3)
        for y in range(1, h + 1):
            b = pick_from(INDUSTRIAL_BLOCKS, seed, cx * 11 + cz * 7 + y)
            blocks.append((cx, y, cz, b, None))
    return blocks


def generate_crane_wreck(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    # Vertical tower
    tower_h = rng.randint(6, 8)
    for y in range(tower_h):
        blocks.append((2, y, 2, "minecraft:stone_bricks", None))
    # Horizontal arm (partially collapsed)
    arm_len = rng.randint(3, 5)
    for x in range(3, 3 + arm_len):
        if rng.random() > 0.3:
            blocks.append((x, tower_h - 1, 2, "minecraft:iron_bars", None))
    # Counterweight side
    for x in range(0, 2):
        blocks.append((x, tower_h - 2, 2, "minecraft:iron_block", None))
    return blocks


def generate_pipe_cluster(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    # Ground
    for x in range(5):
        for z in range(5):
            blocks.append((x, 0, z, "minecraft:smooth_stone", None))
    # Horizontal pipes
    for x in range(5):
        blocks.append((x, 1, 2, "minecraft:iron_bars", None))
    for z in range(5):
        blocks.append((2, 2, z, "minecraft:iron_bars", None))
    # Vertical connectors
    for y in range(1, 3):
        blocks.append((2, y, 2, "minecraft:iron_block", None))
    return blocks
