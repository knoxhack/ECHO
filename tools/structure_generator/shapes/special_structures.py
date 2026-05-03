"""
Special structure generators for bio_lab, military_vault, data_center_ruin, reactor_ruin.
"""

import random
from typing import List, Tuple, Optional, Dict

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_bio_lab(seed: int) -> BlockList:
    """Bio laboratory with containment cells and equipment."""
    rng = _r(seed)
    blocks: BlockList = []
    w, d = 9, 7
    
    # Floor and walls
    for x in range(w):
        for z in range(d):
            blocks.append((x, 0, z, "minecraft:smooth_stone", None))
            if x == 0 or x == w - 1 or z == 0 or z == d - 1:
                for y in range(1, 4):
                    blocks.append((x, y, z, "minecraft:stone_bricks", None))
    
    # Containment cells (glass walls)
    for x in range(2, 5):
        for y in range(1, 3):
            blocks.append((x, y, 3, "minecraft:glass", None))
    for x in range(5, 8):
        for y in range(1, 3):
            blocks.append((x, y, 3, "minecraft:glass", None))
    
    # Cauldrons as equipment
    blocks.append((2, 1, 2, "minecraft:cauldron", None))
    blocks.append((3, 1, 2, "minecraft:cauldron", None))
    blocks.append((6, 1, 2, "minecraft:cauldron", None))
    
    # Iron bars for cage effect
    blocks.append((4, 2, 2, "minecraft:iron_bars", None))
    blocks.append((4, 2, 4, "minecraft:iron_bars", None))
    
    return blocks


def generate_military_vault(seed: int) -> BlockList:
    """Reinforced military bunker with armory."""
    rng = _r(seed)
    blocks: BlockList = []
    w, d = 8, 8
    
    # Reinforced floor and walls
    for x in range(w):
        for z in range(d):
            blocks.append((x, 0, z, "minecraft:stone_bricks", None))
            if x == 0 or x == w - 1 or z == 0 or z == d - 1:
                for y in range(1, 4):
                    if rng.random() > 0.3:
                        blocks.append((x, y, z, "minecraft:stone_bricks", None))
                    else:
                        blocks.append((x, y, z, "minecraft:iron_block", None))
    
    # Armory room dividers
    for z in range(1, d - 1):
        blocks.append((3, 1, z, "minecraft:iron_bars", None))
        blocks.append((5, 1, z, "minecraft:iron_bars", None))
    
    # Storage
    blocks.append((1, 1, 1, "minecraft:barrel", None))
    blocks.append((1, 1, 2, "minecraft:chest", None))
    blocks.append((6, 1, 1, "minecraft:barrel", None))
    blocks.append((6, 1, 2, "minecraft:barrel", None))
    
    # Anvil in workshop area
    blocks.append((4, 1, 4, "minecraft:anvil", None))
    
    return blocks


def generate_data_center_ruin(seed: int) -> BlockList:
    """Server room with rows of equipment."""
    rng = _r(seed)
    blocks: BlockList = []
    w, d = 10, 8
    
    # Floor
    for x in range(w):
        for z in range(d):
            blocks.append((x, 0, z, "minecraft:smooth_stone", None))
    
    # Server racks (rows of observers/iron blocks)
    for row in range(2, 7, 2):
        for x in range(1, w - 1):
            blocks.append((x, 1, row, "minecraft:observer", None))
            if rng.random() > 0.5:
                blocks.append((x, 2, row, "minecraft:glowstone", None))
    
    # Walls with gaps
    for x in range(w):
        for z in [0, d - 1]:
            for y in range(3):
                if rng.random() > 0.4:
                    blocks.append((x, y, z, "minecraft:stone_bricks", None))
    
    # Ceiling lights
    for x in range(2, w - 2, 3):
        for z in range(2, d - 2, 3):
            blocks.append((x, 3, z, "minecraft:glowstone", None))
    
    return blocks


def generate_reactor_ruin(seed: int) -> BlockList:
    """Broken reactor with glowing core."""
    rng = _r(seed)
    blocks: BlockList = []
    
    # Circular containment
    cx, cz = 4, 4
    radius = 3
    
    for x in range(-radius, radius + 1):
        for z in range(-radius, radius + 1):
            dist = (x * x + z * z) ** 0.5
            px, pz = cx + x, cz + z
            
            if radius - 0.5 <= dist <= radius + 0.5:
                for y in range(4):
                    if rng.random() > 0.25:
                        blocks.append((px, y, pz, "minecraft:iron_block", None))
                    else:
                        blocks.append((px, y, pz, "minecraft:iron_bars", None))
            elif dist < radius - 0.5:
                # Floor
                blocks.append((px, 0, pz, "minecraft:smooth_stone", None))
    
    # Glowing core
    blocks.append((cx, 1, cz, "minecraft:glowstone", None))
    blocks.append((cx, 2, cz, "minecraft:magma_block", None))
    blocks.append((cx - 1, 1, cz, "minecraft:magma_block", None))
    blocks.append((cx + 1, 1, cz, "minecraft:magma_block", None))
    blocks.append((cx, 1, cz - 1, "minecraft:magma_block", None))
    blocks.append((cx, 1, cz + 1, "minecraft:magma_block", None))
    
    return blocks
