"""
Industrial complex structures: industrial_factory, subway_station.
"""

import random
from typing import List, Tuple, Optional, Dict

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_industrial_factory(seed: int) -> BlockList:
    """Large factory floor with machinery."""
    rng = _r(seed)
    blocks: BlockList = []
    w, d = 12, 10
    
    # Concrete floor
    for x in range(w):
        for z in range(d):
            blocks.append((x, 0, z, "minecraft:smooth_stone", None))
    
    # Support pillars
    for x in [2, 5, 8]:
        for z in [2, 7]:
            for y in range(1, 5):
                blocks.append((x, y, z, "minecraft:stone_bricks", None))
    
    # Machinery rows (iron blocks with hoppers)
    for x in range(1, w - 1, 2):
        for z in range(3, d - 3):
            if rng.random() > 0.4:
                blocks.append((x, 1, z, "minecraft:iron_block", None))
                if rng.random() > 0.6:
                    blocks.append((x, 2, z, "minecraft:hopper", None))
    
    # Overhead pipes (iron bars)
    for x in range(w):
        blocks.append((x, 4, 5, "minecraft:iron_bars", None))
    
    # Control room
    for x in range(w - 3, w):
        for z in range(d - 3, d):
            blocks.append((x, 1, z, "minecraft:stone_bricks", None))
            for y in range(2, 4):
                blocks.append((x, y, z, "minecraft:glass", None))
    
    return blocks


def generate_subway_station(seed: int) -> BlockList:
    """Underground subway platform."""
    rng = _r(seed)
    blocks: BlockList = []
    w, d = 14, 8
    
    # Platform (raised on one side)
    for x in range(w):
        for z in range(d):
            if z < 4:
                # Track level
                blocks.append((x, 0, z, "minecraft:stone_bricks", None))
            else:
                # Platform level
                blocks.append((x, 1, z, "minecraft:smooth_stone", None))
    
    # Platform edge wall
    for x in range(w):
        blocks.append((x, 0, 3, "minecraft:stone_bricks", None))
    
    # Support columns
    for x in range(3, w, 4):
        for z in range(4, d):
            for y in range(2, 5):
                blocks.append((x, y, z, "minecraft:stone_bricks", None))
    
    # Rails (iron bars as tracks)
    for x in range(w):
        blocks.append((x, 0, 1, "minecraft:iron_bars", None))
    
    # Ceiling
    for x in range(w):
        for z in range(d):
            if rng.random() > 0.1:
                blocks.append((x, 5, z, "minecraft:smooth_stone", None))
    
    # Lights
    for x in range(2, w, 4):
        blocks.append((x, 4, 6, "minecraft:glowstone", None))
    
    return blocks
