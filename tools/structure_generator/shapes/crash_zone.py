"""
Crash Zone Wasteland POI shape generators.
"""

import random
from typing import List, Tuple, Optional, Dict

from palettes import CRASH_ZONE_BLOCKS, pick_from

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def _place_mound(rng: random.Random, cx: int, cz: int, base_r: int, height: int, blocks: BlockList) -> None:
    """Place an irregular mound centered at (cx, 0, cz)."""
    for dx in range(-base_r, base_r + 1):
        for dz in range(-base_r, base_r + 1):
            dist = max(abs(dx), abs(dz))
            if dist > base_r:
                continue
            h = max(0, height - dist + rng.randint(-1, 1))
            for y in range(0, h + 1):
                if rng.random() > 0.25:
                    b = pick_from(CRASH_ZONE_BLOCKS, rng.randint(0, 999), y * 7 + dx * 3 + dz)
                    blocks.append((cx + dx, y, cz + dz, b, None))


def generate_scrap_pile_small(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    _place_mound(rng, 2, 2, 2, 2, blocks)
    return blocks


def generate_scrap_pile_medium(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    _place_mound(rng, 3, 3, 3, 3, blocks)
    # Add some vertical beams
    for _ in range(rng.randint(1, 3)):
        bx = rng.randint(1, 5)
        bz = rng.randint(1, 5)
        for by in range(rng.randint(2, 4)):
            blocks.append((bx, by, bz, "minecraft:iron_bars", None))
    return blocks


def generate_wreckage_cluster(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    # Two or three small mounds
    for _ in range(rng.randint(2, 3)):
        mx = rng.randint(2, 7)
        mz = rng.randint(2, 7)
        _place_mound(rng, mx, mz, rng.randint(1, 2), rng.randint(1, 2), blocks)
    # Scattered upright beams
    for _ in range(rng.randint(3, 5)):
        bx = rng.randint(0, 9)
        bz = rng.randint(0, 9)
        h = rng.randint(2, 4)
        for by in range(h):
            blocks.append((bx, by, bz, "minecraft:iron_bars", None))
    return blocks


def generate_ash_covered_ruin(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    # Partial L-shaped wall
    for x in range(5):
        for z in range(5):
            if x == 0 or z == 0 or (x == 4 and z < 3) or (z == 4 and x < 3):
                h = rng.randint(2, 4)
                if rng.random() > 0.2:
                    for y in range(h):
                        b = pick_from(CRASH_ZONE_BLOCKS, seed, x * 11 + z * 7 + y)
                        blocks.append((x, y, z, b, None))
    # Ash cover (coarse dirt on ground around)
    for _ in range(rng.randint(8, 15)):
        ax = rng.randint(0, 6)
        az = rng.randint(0, 6)
        blocks.append((ax, 0, az, "minecraft:coarse_dirt", None))
    return blocks
