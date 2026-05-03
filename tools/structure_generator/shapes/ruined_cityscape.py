"""
Ruined Cityscape POI shape generators.
"""

import random
from typing import List, Tuple, Optional, Dict

from palettes import CITYSCAPE_BLOCKS, pick_from

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_collapsed_building_small(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    w, d = 5, 5
    height = rng.randint(4, 6)
    for x in range(w):
        for z in range(d):
            for y in range(height):
                # Walls only, some collapsed
                if x == 0 or x == w - 1 or z == 0 or z == d - 1:
                    if rng.random() > 0.25:
                        b = pick_from(CITYSCAPE_BLOCKS, seed, x * 13 + z * 17 + y)
                        blocks.append((x, y, z, b, None))
                # Floor
                if y == 0:
                    b = pick_from(CITYSCAPE_BLOCKS, seed + 1, x + z)
                    blocks.append((x, y, z, b, None))
    return blocks


def generate_collapsed_building_tall(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    w, d = 5, 5
    height = rng.randint(7, 10)
    for x in range(w):
        for z in range(d):
            for y in range(height):
                if x == 0 or x == w - 1 or z == 0 or z == d - 1:
                    if rng.random() > 0.35:
                        b = pick_from(CITYSCAPE_BLOCKS, seed, x * 13 + z * 17 + y)
                        blocks.append((x, y, z, b, None))
                if y == 0 or y == height // 2:
                    if rng.random() > 0.3:
                        b = pick_from(CITYSCAPE_BLOCKS, seed + 2, x + z + y)
                        blocks.append((x, y, z, b, None))
    return blocks


def generate_street_barricade(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    length = rng.randint(7, 9)
    for x in range(length):
        for z in range(2):
            h = rng.randint(1, 3)
            if rng.random() > 0.15:
                for y in range(h):
                    b = pick_from(CITYSCAPE_BLOCKS, seed, x * 3 + z + y)
                    blocks.append((x, y, z, b, None))
    # Sandbags (oak planks) in front
    for _ in range(rng.randint(2, 4)):
        sx = rng.randint(0, length - 1)
        sz = rng.randint(2, 3)
        blocks.append((sx, 0, sz, "minecraft:oak_planks", None))
    return blocks


def generate_parking_ruin(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    w, d = 7, 5
    for x in range(w):
        for z in range(d):
            # Pavement
            blocks.append((x, 0, z, "minecraft:smooth_stone", None))
            # Divider walls
            if x % 3 == 0 and z > 0 and z < d - 1:
                if rng.random() > 0.3:
                    for y in range(rng.randint(1, 2)):
                        b = pick_from(CITYSCAPE_BLOCKS, seed, x + z + y)
                        blocks.append((x, y, z, b, None))
    return blocks
