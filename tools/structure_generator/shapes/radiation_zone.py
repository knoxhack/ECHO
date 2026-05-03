"""
Radiation Zone POI shape generators.
"""

import random
from typing import List, Tuple, Optional, Dict

from palettes import RADIATION_BLOCKS, pick_from

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_containment_breach(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    size = 6
    for x in range(size):
        for z in range(size):
            # Floor
            blocks.append((x, 0, z, "minecraft:smooth_stone", None))
            # Glass walls around perimeter
            if x == 0 or x == size - 1 or z == 0 or z == size - 1:
                for y in range(rng.randint(2, 3)):
                    if rng.random() > 0.4:
                        blocks.append((x, y, z, "minecraft:glass", None))
                    else:
                        b = pick_from(RADIATION_BLOCKS, seed, x * 5 + z * 3 + y)
                        blocks.append((x, y, z, b, None))
            # Central breach (glowing core)
            if 2 <= x <= 3 and 2 <= z <= 3:
                blocks.append((x, 1, z, "minecraft:glowstone", None))
                blocks.append((x, 2, z, "minecraft:magma_block", None))
    return blocks


def generate_waste_barrel_cluster(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    # Ground
    for x in range(5):
        for z in range(5):
            blocks.append((x, 0, z, "minecraft:coarse_dirt", None))
    # Barrel cluster (cauldrons as barrels)
    positions = [(1, 1), (1, 3), (3, 1), (3, 3), (2, 2)]
    for bx, bz in positions:
        if rng.random() > 0.2:
            blocks.append((bx, 1, bz, "minecraft:cauldron", None))
    # Spill (green concrete)
    for _ in range(rng.randint(4, 8)):
        sx = rng.randint(0, 4)
        sz = rng.randint(0, 4)
        blocks.append((sx, 0, sz, "minecraft:green_concrete", None))
    return blocks


def generate_irradiated_vehicle(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    # Blocky vehicle body
    for x in range(2, 6):
        for z in range(2, 5):
            blocks.append((x, 1, z, "minecraft:iron_block", None))
    # Cab
    for x in range(2, 4):
        for z in range(2, 4):
            blocks.append((x, 2, z, "minecraft:iron_block", None))
    # Glow accents
    blocks.append((5, 2, 3, "minecraft:glowstone", None))
    blocks.append((3, 1, 4, "minecraft:magma_block", None))
    # Wheels (iron bars)
    blocks.append((2, 0, 2, "minecraft:iron_bars", None))
    blocks.append((5, 0, 2, "minecraft:iron_bars", None))
    blocks.append((2, 0, 4, "minecraft:iron_bars", None))
    blocks.append((5, 0, 4, "minecraft:iron_bars", None))
    return blocks


def generate_radiation_crater(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    radius = rng.randint(3, 4)
    cx, cz = radius, radius
    for x in range(-radius, radius + 1):
        for z in range(-radius, radius + 1):
            dist = (x * x + z * z) ** 0.5
            if dist <= radius + 0.5:
                h = max(0, int(radius - dist) + rng.randint(-1, 0))
                # Crater bowl goes down, edges up
                for y in range(-h, 1):
                    if dist < radius * 0.5:
                        b = "minecraft:magma_block"
                    elif dist < radius * 0.8:
                        b = "minecraft:smooth_stone"
                    else:
                        b = "minecraft:coarse_dirt"
                    blocks.append((cx + x, y + 1, cz + z, b, None))
    return blocks
