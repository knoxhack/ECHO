"""
Cryogenic Ruins POI shape generators.
"""

import random
from typing import List, Tuple, Optional, Dict

from palettes import CRYOGENIC_BLOCKS, pick_from

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_frozen_vehicle(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    # Vehicle body
    for x in range(2, 6):
        for z in range(2, 5):
            blocks.append((x, 1, z, "minecraft:iron_block", None))
    for x in range(2, 4):
        for z in range(2, 4):
            blocks.append((x, 2, z, "minecraft:iron_block", None))
    # Wheels
    blocks.append((2, 0, 2, "minecraft:iron_bars", None))
    blocks.append((5, 0, 2, "minecraft:iron_bars", None))
    blocks.append((2, 0, 4, "minecraft:iron_bars", None))
    blocks.append((5, 0, 4, "minecraft:iron_bars", None))
    # Ice shell around
    for x in range(1, 7):
        for z in range(1, 6):
            for y in range(3):
                # Encase in ice if adjacent to vehicle
                if (x, y, z) not in [(b[0], b[1], b[2]) for b in blocks]:
                    dist = min(abs(x - 4), abs(z - 3))
                    if dist <= 2 and rng.random() > 0.3:
                        blocks.append((x, y, z, "minecraft:packed_ice", None))
    return blocks


def generate_ice_covered_ruin(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    w, d = 5, 5
    for x in range(w):
        for z in range(d):
            for y in range(4):
                if x == 0 or x == w - 1 or z == 0 or z == d - 1:
                    if rng.random() > 0.3:
                        b = pick_from(CRYOGENIC_BLOCKS, seed, x * 13 + z * 17 + y)
                        blocks.append((x, y, z, b, None))
                if y == 0:
                    blocks.append((x, y, z, "minecraft:stone_bricks", None))
    # Ice coating on walls
    for x in range(-1, w + 1):
        for z in range(-1, d + 1):
            for y in range(4):
                if x == -1 or x == w or z == -1 or z == d:
                    if rng.random() > 0.4:
                        blocks.append((x, y, z, "minecraft:packed_ice", None))
    return blocks


def generate_broken_tank(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    radius = 2
    height = rng.randint(4, 5)
    cx, cz = 2, 2
    for y in range(height):
        for x in range(-radius, radius + 1):
            for z in range(-radius, radius + 1):
                dist = (x * x + z * z) ** 0.5
                if radius - 0.5 <= dist <= radius + 0.5:
                    if rng.random() > 0.25:
                        blocks.append((cx + x, y, cz + z, "minecraft:stone_bricks", None))
                elif dist < radius - 0.5 and y == 0:
                    blocks.append((cx + x, y, cz + z, "minecraft:smooth_stone", None))
    # Broken section
    missing = [(cx + radius, y, cz) for y in range(1, height)]
    blocks = [b for b in blocks if (b[0], b[1], b[2]) not in missing]
    return blocks


def generate_frozen_cache(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    w, d = 3, 3
    for x in range(w):
        for z in range(d):
            for y in range(3):
                if x == 0 or x == w - 1 or z == 0 or z == d - 1:
                    blocks.append((x, y, z, "minecraft:packed_ice", None))
                elif y == 0:
                    blocks.append((x, y, z, "minecraft:smooth_stone", None))
    # Loot inside
    blocks.append((1, 0, 1, "minecraft:barrel", None))
    blocks.append((1, 1, 1, "minecraft:chest", None))
    return blocks
