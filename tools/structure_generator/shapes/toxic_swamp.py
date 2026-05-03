"""
Toxic Swamp POI shape generators.
"""

import random
from typing import List, Tuple, Optional, Dict

from palettes import TOXIC_BLOCKS, pick_from
from .poi_primitives import (
    add_block,
    add_supply_cluster,
    blob_patch,
    enforce_guardrails,
    fill,
    lean_to,
    pipe_run,
    ruined_wall,
    scatter,
)

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_chemical_spill(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    variant = seed % 3
    blob_patch(blocks, 7, 7, 7, 6, "minecraft:coarse_dirt")
    fill(blocks, 4, 0, 4, 9, 0, 9, "minecraft:green_terracotta")
    fill(blocks, 5, 0, 5, 8, 0, 8, "minecraft:green_stained_glass")

    if variant == 0:
        for y in range(1, 4):
            add_block(blocks, 4, y, 4, "minecraft:iron_block")
            add_block(blocks, 9, y, 9, "minecraft:iron_block")
        pipe_run(blocks, (4, 3, 4), (9, 2, 9), pipe_block="minecraft:iron_bars", support_block="minecraft:smooth_stone")
        add_block(blocks, 8, 1, 6, "minecraft:cauldron")
    elif variant == 1:
        pipe_run(blocks, (2, 2, 6), (11, 2, 6), pipe_block="minecraft:iron_bars", support_block="minecraft:mossy_cobblestone")
        add_block(blocks, 11, 1, 6, "minecraft:slime_block")
        add_block(blocks, 10, 0, 7, "minecraft:green_stained_glass")
        add_block(blocks, 9, 0, 7, "minecraft:green_stained_glass")
    else:
        for x in range(3, 6):
            for z in range(10, 13):
                add_block(blocks, x, 0, z, "minecraft:coarse_dirt")
        for y in range(1, 4):
            add_block(blocks, 4, y, 11, "minecraft:barrel")
        add_block(blocks, 5, 1, 10, "minecraft:barrel")
        add_block(blocks, 5, 1, 11, "minecraft:cauldron")

    for px, pz in ((4, 4), (9, 4), (4, 9), (9, 9), (2, 7), (11, 6)):
        if rng.random() > 0.15:
            add_block(blocks, px, 1, pz, "minecraft:barrel")
    add_block(blocks, 11, 1, 11, "minecraft:iron_bars")
    add_block(blocks, 11, 2, 11, "minecraft:iron_bars")
    add_block(blocks, 11, 3, 11, "minecraft:lightning_rod")
    scatter(blocks, rng, 1, 1, 13, 13, 18, ["minecraft:slime_block", "minecraft:mossy_cobblestone", "minecraft:green_stained_glass", "minecraft:coarse_dirt"])
    add_supply_cluster(blocks, rng, 10, 9, 2, 2, containers=1, clutter_palette=["minecraft:mossy_cobblestone", "minecraft:slime_block"])

    return enforce_guardrails("chemical_spill", blocks, 12, 12, 4, min_story_nodes=3)


def generate_broken_pipeline(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    blob_patch(blocks, 8, 5, 8, 4, "minecraft:coarse_dirt")
    pipe_run(blocks, (1, 2, 5), (14, 2, 5), pipe_block="minecraft:iron_bars", support_block="minecraft:mossy_cobblestone", support_spacing=2)
    pipe_run(blocks, (8, 2, 5), (11, 1, 9), pipe_block="minecraft:iron_bars", support_block="minecraft:mossy_cobblestone", support_spacing=2)
    add_block(blocks, 15, 1, 5, "minecraft:slime_block")
    add_block(blocks, 14, 0, 5, "minecraft:green_stained_glass")
    add_block(blocks, 13, 0, 5, "minecraft:green_stained_glass")
    add_block(blocks, 12, 1, 8, "minecraft:cauldron")
    add_block(blocks, 4, 1, 3, "minecraft:lightning_rod")
    ruined_wall(blocks, rng, 3, 2, 6, 8, "minecraft:mossy_cobblestone", min_height=1, max_height=2, gap_chance=0.55)
    add_supply_cluster(blocks, rng, 2, 7, 2, 2, containers=1, clutter_palette=["minecraft:barrel", "minecraft:mossy_cobblestone", "minecraft:slime_block"])
    scatter(blocks, rng, 1, 1, 15, 10, 16, ["minecraft:slime_block", "minecraft:green_stained_glass", "minecraft:mossy_cobblestone", "minecraft:coarse_dirt"])

    return enforce_guardrails("broken_pipeline", blocks, 14, 9, 3, min_story_nodes=3)


def generate_abandoned_shed(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    blob_patch(blocks, 7, 7, 7, 6, "minecraft:coarse_dirt")
    for x in range(4, 9):
        for z in range(4, 10):
            add_block(blocks, x, 0, z, "minecraft:oak_planks")
            if x in (4, 8) or z in (4, 9):
                if (x, z) != (6, 9):
                    add_block(blocks, x, 1, z, pick_from(TOXIC_BLOCKS, seed, x * 11 + z))
                    if rng.random() > 0.25:
                        add_block(blocks, x, 2, z, pick_from(TOXIC_BLOCKS, seed, x * 13 + z))
    for x in range(4, 9):
        for z in range(4, 10):
            if rng.random() > 0.25:
                add_block(blocks, x, 3, z, "minecraft:oak_planks")

    lean_to(blocks, 9, 6, 4, 3, "minecraft:green_terracotta")
    pipe_run(blocks, (5, 3, 4), (11, 3, 6), pipe_block="minecraft:iron_bars", support_block="minecraft:mossy_cobblestone")
    add_block(blocks, 6, 1, 6, "minecraft:barrel")
    add_block(blocks, 7, 1, 6, "minecraft:chest")
    add_block(blocks, 10, 1, 7, "minecraft:cauldron")
    scatter(blocks, rng, 2, 2, 13, 13, 16, ["minecraft:slime_block", "minecraft:mossy_cobblestone", "minecraft:green_stained_glass", "minecraft:coarse_dirt"])

    return enforce_guardrails("abandoned_shed", blocks, 12, 12, 4, min_story_nodes=3)


def generate_toxic_pool_small(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    variant = seed % 3
    blob_patch(blocks, 7, 7, 6, 6, "minecraft:coarse_dirt")
    for x in range(4, 10):
        for z in range(4, 10):
            if 5 <= x <= 8 and 5 <= z <= 8:
                add_block(blocks, x, -1, z, "minecraft:green_stained_glass")
                add_block(blocks, x, 0, z, "minecraft:slime_block")
            elif x in (4, 9) or z in (4, 9):
                add_block(blocks, x, 0, z, "minecraft:mossy_cobblestone")
    add_block(blocks, 4, 1, 4, "minecraft:iron_bars")
    add_block(blocks, 4, 2, 4, "minecraft:lightning_rod")
    add_block(blocks, 9, 1, 9, "minecraft:iron_bars")
    add_block(blocks, 9, 2, 9, "minecraft:cauldron")

    if variant == 0:
        pipe_run(blocks, (2, 2, 7), (5, 1, 7), pipe_block="minecraft:iron_bars", support_block="minecraft:mossy_cobblestone")
        add_block(blocks, 2, 1, 7, "minecraft:cauldron")
    elif variant == 1:
        add_block(blocks, 7, 1, 4, "minecraft:barrel")
        add_block(blocks, 8, 1, 4, "minecraft:barrel")
        add_block(blocks, 9, 1, 5, "minecraft:cauldron")
    else:
        ruined_wall(blocks, rng, 3, 3, 10, 10, "minecraft:mossy_cobblestone", min_height=1, max_height=2, gap_chance=0.7)
        add_block(blocks, 4, 1, 9, "minecraft:barrel")

    scatter(blocks, rng, 1, 1, 12, 12, 14, ["minecraft:slime_block", "minecraft:mossy_cobblestone", "minecraft:green_stained_glass", "minecraft:coarse_dirt"])
    add_supply_cluster(blocks, rng, 2, 8, 2, 2, containers=1, clutter_palette=["minecraft:mossy_cobblestone", "minecraft:slime_block"])

    return enforce_guardrails("toxic_pool_small", blocks, 11, 11, 3, min_story_nodes=3)
