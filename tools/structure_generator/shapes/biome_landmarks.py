"""
Rare skyline landmarks for the Ashfall biome overhaul.

These generators stay within compact single-template footprints while giving
each biome a stronger distant silhouette.
"""

from __future__ import annotations

import random
from typing import Dict, List, Optional, Tuple

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def _add(blocks: BlockList, x: int, y: int, z: int, block: str, props: Optional[Dict[str, str]] = None) -> None:
    blocks.append((x, y, z, block, props))


def _fill(blocks: BlockList, x1: int, y1: int, z1: int, x2: int, y2: int, z2: int, block: str) -> None:
    for x in range(min(x1, x2), max(x1, x2) + 1):
        for y in range(min(y1, y2), max(y1, y2) + 1):
            for z in range(min(z1, z2), max(z1, z2) + 1):
                _add(blocks, x, y, z, block)


def _line(blocks: BlockList, x1: int, y1: int, z1: int, x2: int, y2: int, z2: int, block: str) -> None:
    steps = max(abs(x2 - x1), abs(y2 - y1), abs(z2 - z1), 1)
    for i in range(steps + 1):
        t = i / steps
        _add(blocks, round(x1 + (x2 - x1) * t), round(y1 + (y2 - y1) * t), round(z1 + (z2 - z1) * t), block)


def _scatter(blocks: BlockList, rng: random.Random, count: int, x1: int, z1: int, x2: int, z2: int, palette: list[str]) -> None:
    for _ in range(count):
        _add(blocks, rng.randint(x1, x2), 0, rng.randint(z1, z2), rng.choice(palette))


def _dedupe(blocks: BlockList) -> BlockList:
    keyed = {}
    for x, y, z, block, props in blocks:
        keyed[(x, y, z)] = (block, props)
    return [(x, y, z, block, props) for (x, y, z), (block, props) in sorted(keyed.items(), key=lambda e: (e[0][1], e[0][0], e[0][2]))]


def generate_nexus_pylon(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    cx, cz = 12, 12
    for radius, block in ((7, "echoashfallprotocol:riftstone"), (5, "echoashfallprotocol:nexus_cracked_soil"), (3, "minecraft:obsidian")):
        for x in range(cx - radius, cx + radius + 1):
            for z in range(cz - radius, cz + radius + 1):
                if abs(x - cx) + abs(z - cz) <= radius + rng.randint(-1, 1):
                    _add(blocks, x, 0, z, block)
    for y in range(1, 29):
        span = 2 if y < 8 else 1
        for dx in range(-span, span + 1):
            for dz in range(-span, span + 1):
                if abs(dx) + abs(dz) <= span + 1:
                    _add(blocks, cx + dx, y, cz + dz, "echoashfallprotocol:riftstone" if y % 4 else "minecraft:crying_obsidian")
        if y % 5 == 0:
            for dx, dz in ((4, 0), (-4, 0), (0, 4), (0, -4)):
                _line(blocks, cx, y, cz, cx + dx, y + 1, cz + dz, "echoashfallprotocol:energized_fissure")
    for y in (8, 16, 24, 29):
        _add(blocks, cx, y, cz, "echoashfallprotocol:echo_crystal")
    _scatter(blocks, rng, 36, 2, 2, 22, 22, ["echoashfallprotocol:echo_crystal", "echoashfallprotocol:energized_fissure", "echoashfallprotocol:riftstone"])
    return _dedupe(blocks)


def generate_floating_obelisk_cluster(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    _scatter(blocks, rng, 45, 0, 0, 30, 30, ["echoashfallprotocol:riftstone", "echoashfallprotocol:nexus_cracked_soil", "echoashfallprotocol:energized_fissure"])
    for cx, cz, base, height in ((8, 8, 12, 14), (19, 10, 18, 12), (14, 21, 15, 16), (24, 24, 10, 9)):
        for y in range(base, base + height):
            taper = 2 if y < base + 3 else 1 if y < base + height - 2 else 0
            for dx in range(-taper, taper + 1):
                for dz in range(-taper, taper + 1):
                    if abs(dx) + abs(dz) <= taper + 1:
                        _add(blocks, cx + dx, y, cz + dz, "minecraft:obsidian" if y % 3 else "echoashfallprotocol:riftstone")
        _add(blocks, cx, base + height, cz, "echoashfallprotocol:echo_crystal")
        for chain_y in range(1, base, 3):
            _add(blocks, cx, chain_y, cz, "minecraft:chain")
    return _dedupe(blocks)


def generate_drop_pod_wreck_large(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    _scatter(blocks, rng, 80, 0, 0, 36, 24, ["echoashfallprotocol:scorched_ash", "echoashfallprotocol:crash_slag", "echoashfallprotocol:twisted_metal"])
    for x in range(6, 31):
        for z in range(7, 17):
            if 0.45 <= abs((z - 12) / 6) + abs((x - 18) / 18) <= 1.35:
                y = 1 + (x - 6) // 7
                _add(blocks, x, y, z, "echoashfallprotocol:drop_pod_hull")
                if rng.random() < 0.18:
                    _add(blocks, x, y + 1, z, "echoashfallprotocol:drop_pod_glass")
    for i in range(8):
        _line(blocks, rng.randint(2, 34), 1, rng.randint(2, 22), rng.randint(2, 34), 1, rng.randint(2, 22), "echoashfallprotocol:cable_bundle")
    return _dedupe(blocks)


def generate_cargo_module_field(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    _scatter(blocks, rng, 65, 0, 0, 38, 28, ["echoashfallprotocol:scorched_ash", "echoashfallprotocol:rusted_metal_debris", "echoashfallprotocol:twisted_metal"])
    for ox, oz, w, d in ((4, 4, 9, 6), (18, 6, 11, 7), (10, 18, 13, 6), (28, 17, 7, 7)):
        _fill(blocks, ox, 0, oz, ox + w, 0, oz + d, "echoashfallprotocol:twisted_metal")
        for x in range(ox, ox + w + 1):
            for z in (oz, oz + d):
                _add(blocks, x, 1, z, "echoashfallprotocol:drop_pod_hull")
        for z in range(oz, oz + d + 1):
            for x in (ox, ox + w):
                _add(blocks, x, 1, z, "echoashfallprotocol:drop_pod_hull")
        for _ in range(4):
            _add(blocks, rng.randint(ox, ox + w), 1, rng.randint(oz, oz + d), "echoashfallprotocol:supply_crate")
    return _dedupe(blocks)


def generate_wasteland_bunker_ruin(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    _scatter(blocks, rng, 70, 0, 0, 34, 28, ["echoashfallprotocol:cracked_earth", "echoashfallprotocol:ash_stone", "echoashfallprotocol:thorn_scrub"])
    _fill(blocks, 6, 0, 6, 28, 0, 22, "minecraft:smooth_stone")
    for y in range(1, 7):
        for x in range(6, 29):
            if rng.random() > 0.22:
                _add(blocks, x, y, 6, "minecraft:cracked_stone_bricks")
            if rng.random() > 0.34:
                _add(blocks, x, y, 22, "minecraft:cracked_stone_bricks")
        for z in range(6, 23):
            if rng.random() > 0.30:
                _add(blocks, 6, y, z, "minecraft:cracked_stone_bricks")
            if rng.random() > 0.40:
                _add(blocks, 28, y, z, "minecraft:cracked_stone_bricks")
    _fill(blocks, 14, -4, 12, 20, -1, 17, "minecraft:stone_bricks")
    _add(blocks, 17, -3, 14, "minecraft:chest")
    return _dedupe(blocks)


def generate_collapsed_tower_large(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    _scatter(blocks, rng, 90, 0, 0, 32, 32, ["echoashfallprotocol:concrete_rubble", "echoashfallprotocol:rebar_block", "echoashfallprotocol:shattered_glass"])
    for y in range(1, 27):
        width = max(3, 13 - y // 4)
        for x in range(16 - width // 2, 17 + width // 2):
            for z in range(16 - width // 2, 17 + width // 2):
                edge = x in (16 - width // 2, 16 + width // 2) or z in (16 - width // 2, 16 + width // 2)
                if edge and rng.random() > y / 42:
                    _add(blocks, x + y // 7, y, z, "echoashfallprotocol:concrete_rubble" if rng.random() < 0.7 else "echoashfallprotocol:rebar_block")
        if y % 5 == 0:
            _line(blocks, 9 + y // 6, y, 16, 25 + y // 6, max(1, y - 2), 18, "echoashfallprotocol:rebar_block")
    return _dedupe(blocks)


def generate_corroded_pipe_network(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    _scatter(blocks, rng, 85, 0, 0, 38, 30, ["echoashfallprotocol:acid_mud", "echoashfallprotocol:toxic_moss", "echoashfallprotocol:ooze_crystal"])
    for z in (6, 14, 23):
        _line(blocks, 2, 2, z, 36, 3 + rng.randint(0, 2), z + rng.randint(-2, 2), "echoashfallprotocol:corroded_pipe")
        for x in range(4, 36, 7):
            _line(blocks, x, 1, z, x + rng.randint(-2, 3), 6, z + rng.randint(-3, 3), "minecraft:chain")
    _fill(blocks, 25, 0, 18, 35, 2, 27, "minecraft:mossy_cobblestone")
    for _ in range(14):
        _add(blocks, rng.randint(24, 36), rng.randint(1, 4), rng.randint(17, 28), "echoashfallprotocol:toxic_waste_barrel")
    return _dedupe(blocks)


def generate_reactor_containment_ruin(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    _scatter(blocks, rng, 90, 0, 0, 36, 34, ["echoashfallprotocol:fallout_dust", "echoashfallprotocol:radioactive_sludge", "echoashfallprotocol:uranium_crystal"])
    cx, cz = 18, 17
    for radius, block in ((14, "echoashfallprotocol:irradiated_shale"), (10, "minecraft:deepslate_tiles"), (5, "echoashfallprotocol:radioactive_sludge")):
        for x in range(cx - radius, cx + radius + 1):
            for z in range(cz - radius, cz + radius + 1):
                if abs(x - cx) + abs(z - cz) <= radius:
                    _add(blocks, x, 0, z, block)
    for x in range(cx - 12, cx + 13, 4):
        for z in (cz - 12, cz + 12):
            _fill(blocks, x, 1, z, x + 1, 8, z + 1, "minecraft:iron_block")
    for z in range(cz - 12, cz + 13, 4):
        for x in (cx - 12, cx + 12):
            _fill(blocks, x, 1, z, x + 1, 8, z + 1, "minecraft:iron_block")
    for _ in range(18):
        _add(blocks, rng.randint(8, 28), 1, rng.randint(7, 27), rng.choice(["echoashfallprotocol:radiation_block", "echoashfallprotocol:uranium_crystal", "echoashfallprotocol:toxic_waste_barrel"]))
    return _dedupe(blocks)


def generate_frozen_lab_large(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    _scatter(blocks, rng, 80, 0, 0, 36, 28, ["echoashfallprotocol:permafrost", "echoashfallprotocol:blue_ice_crystal", "minecraft:snow_block"])
    _fill(blocks, 5, 0, 5, 31, 0, 23, "echoashfallprotocol:permafrost")
    for y in range(1, 8):
        for x in range(5, 32):
            for z in (5, 23):
                if rng.random() > 0.15:
                    _add(blocks, x, y, z, "minecraft:packed_ice" if y % 3 else "echoashfallprotocol:frozen_conduit")
        for z in range(5, 24):
            for x in (5, 31):
                if rng.random() > 0.18:
                    _add(blocks, x, y, z, "minecraft:packed_ice" if y % 3 else "echoashfallprotocol:frozen_conduit")
    for x in range(9, 29, 5):
        _fill(blocks, x, 1, 10, x + 1, 3, 13, "minecraft:blue_ice")
        _add(blocks, x, 4, 11, "echoashfallprotocol:blue_ice_crystal")
    _add(blocks, 18, 1, 18, "echoashfallprotocol:research_lab")
    return _dedupe(blocks)


def generate_industrial_factory_shell(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []
    _scatter(blocks, rng, 90, 0, 0, 40, 30, ["echoashfallprotocol:oil_stained_concrete", "echoashfallprotocol:twisted_metal", "echoashfallprotocol:cable_bundle"])
    _fill(blocks, 4, 0, 4, 36, 0, 26, "echoashfallprotocol:industrial_aggregate")
    for x in range(4, 37, 8):
        _fill(blocks, x, 1, 4, x + 1, 13, 5, "echoashfallprotocol:rusted_metal_sheet")
        _fill(blocks, x, 1, 25, x + 1, 13, 26, "echoashfallprotocol:rusted_metal_sheet")
        _line(blocks, x, 12, 5, x + 1, 12, 25, "echoashfallprotocol:corroded_pipe")
    for z in range(6, 25, 6):
        _line(blocks, 5, 3, z, 35, 5, z + rng.randint(-1, 1), "echoashfallprotocol:corroded_pipe")
        _line(blocks, 5, 1, z + 1, 35, 1, z + 1, "minecraft:rail")
    for _ in range(18):
        _add(blocks, rng.randint(6, 34), 1, rng.randint(6, 24), rng.choice(["echoashfallprotocol:scrap_press", "echoashfallprotocol:factory_controller", "echoashfallprotocol:rusted_metal_debris"]))
    return _dedupe(blocks)
