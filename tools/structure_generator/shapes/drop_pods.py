"""
Drop pod structure generator.

The starting drop pod is a safe mini-base: a large curated capsule wreck in a
32x32 scorched impact site. The interior has a guaranteed-clear spawn bay, a
visible ECHO-7 terminal wall, starter storage, bed, side workstations, lighting,
and a south-facing exit ramp.
"""

import json
import random
from typing import Any, Dict, List, Optional, Tuple

from nbtlib.tag import Byte

BlockEntry = Tuple[int, int, int, str, Optional[Dict[str, str]], Optional[Dict[str, Any]]]
BlockList = List[BlockEntry]

_CRATER_SIZE = 32
_POD_X0, _POD_X1 = 8, 23
_POD_Z0, _POD_Z1 = 7, 24
_POD_CX, _POD_CZ = 16, 15

# Template-space feet block for the player. Java mirrors this coordinate and
# validates feet/head after placement.
_SPAWN_X, _SPAWN_Y, _SPAWN_Z = _POD_CX, 1, _POD_CZ + 4
_BED_FOOT = (_POD_X0 + 2, 1, _POD_CZ + 3)
_BED_HEAD = (_POD_X0 + 2, 1, _POD_CZ + 4)
_HULL = "echoashfallprotocol:drop_pod_hull"
_IRON_BARS = {
    "east": "false",
    "north": "false",
    "south": "false",
    "waterlogged": "false",
    "west": "false",
}


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def _add(
    blocks: BlockList,
    x: int,
    y: int,
    z: int,
    block_id: str,
    props: Optional[Dict[str, str]] = None,
    nbt: Optional[Dict[str, Any]] = None,
) -> None:
    # Keep one final state per coordinate. Structure templates with duplicate
    # positions can produce stale blocks depending on loader order.
    blocks[:] = [b for b in blocks if b[0] != x or b[1] != y or b[2] != z]
    blocks.append((x, y, z, block_id, props, nbt))


def _in_pod_footprint(x: int, z: int) -> bool:
    return _POD_X0 <= x <= _POD_X1 and _POD_Z0 <= z <= _POD_Z1


def _near_exit_path(x: int, z: int) -> bool:
    return abs(x - _POD_CX) <= 1 and _POD_Z1 <= z <= _POD_Z1 + 4


def _shell_cell(x: int, z: int) -> bool:
    return (x in (_POD_X0, _POD_X1) or z in (_POD_Z0, _POD_Z1)) and _in_pod_footprint(x, z)


def _build_crater(blocks: BlockList, rng: random.Random) -> None:
    for x in range(_CRATER_SIZE):
        for z in range(_CRATER_SIZE):
            if _in_pod_footprint(x, z):
                continue

            dx = x - _POD_CX
            dz = z - _POD_CZ
            dist = (dx * dx + dz * dz) ** 0.5
            if dist <= 7:
                mat = rng.choices(
                    ["minecraft:magma_block", "minecraft:blackstone", "minecraft:netherrack", "minecraft:polished_blackstone"],
                    [0.25, 0.35, 0.2, 0.2],
                )[0]
            elif dist <= 12:
                mat = rng.choices(
                    ["minecraft:blackstone", "minecraft:polished_blackstone", "minecraft:netherrack", "minecraft:magma_block"],
                    [0.45, 0.3, 0.15, 0.1],
                )[0]
            elif dist <= 16:
                mat = rng.choices(
                    ["minecraft:coarse_dirt", "minecraft:gravel", "minecraft:blackstone"],
                    [0.5, 0.3, 0.2],
                )[0]
            else:
                continue

            _add(blocks, x, 0, z, mat)
            if mat == "minecraft:netherrack" and rng.random() < 0.35 and not _near_exit_path(x, z):
                _add(blocks, x, 1, z, "minecraft:fire")

    # Edge scorch marks pin the template to the intended 32x32 footprint.
    for x, z in [(0, _POD_CZ), (_CRATER_SIZE - 1, _POD_CZ), (_POD_CX, 0), (_POD_CX, _CRATER_SIZE - 1)]:
        _add(blocks, x, 0, z, "minecraft:blackstone")

    for lx, lz in [(4, 15), (26, 8), (23, 27), (6, 5), (5, 24)]:
        _add(blocks, lx, 0, lz, "minecraft:lava")

    smoke_spots = [(4, 4), (4, 26), (26, 4), (26, 26), (16, 3), (3, 15), (28, 15)]
    for sx, sz in smoke_spots:
        if _in_pod_footprint(sx, sz):
            continue
        _add(
            blocks,
            sx,
            0,
            sz,
            "minecraft:campfire",
            {"lit": "true", "facing": "north", "signal_fire": "true", "waterlogged": "false"},
        )
        _add(blocks, sx, 1, sz, "minecraft:stone_slab", {"type": "top", "waterlogged": "false"})

    debris_cells = [
        (x, z)
        for x in range(_CRATER_SIZE)
        for z in range(_CRATER_SIZE)
        if not _in_pod_footprint(x, z) and not _near_exit_path(x, z)
    ]
    rng.shuffle(debris_cells)

    for x, z in debris_cells[:24]:
        _add(blocks, x, 1, z, "minecraft:iron_bars", dict(_IRON_BARS))
    for x, z in debris_cells[24:44]:
        _add(blocks, x, 1, z, "echoashfallprotocol:charred_wood_log", {"axis": rng.choice(["x", "z"])})
    for x, z in debris_cells[44:62]:
        _add(blocks, x, 1, z, "minecraft:chain", {"axis": "y", "waterlogged": "false"})
        if rng.random() < 0.5:
            _add(blocks, x, 2, z, "minecraft:chain", {"axis": "y", "waterlogged": "false"})
    for x, z in debris_cells[62:78]:
        _add(blocks, x, 1, z, "minecraft:stone_button", {"face": "floor", "facing": "north", "powered": "false"})

    # Skidded hull ribs and cables tell the impact direction.
    for offset in range(7):
        left_z = _POD_Z1 + 2 + offset
        right_z = _POD_Z1 + 3 + offset
        if left_z < _CRATER_SIZE:
            _add(blocks, _POD_CX - 7 + offset, 1, left_z, _HULL)
        if right_z < _CRATER_SIZE:
            _add(blocks, _POD_CX + 6 - offset, 1, right_z, "echoashfallprotocol:rusted_metal_debris")
    for z in range(_POD_Z1 + 1, min(_CRATER_SIZE, _POD_Z1 + 7)):
        _add(blocks, _POD_CX - 2, 1, z, "echoashfallprotocol:cable_bundle")
        if z % 2 == 0:
            _add(blocks, _POD_CX + 2, 1, z, "echoashfallprotocol:cable_bundle")


def _build_pod_shell(blocks: BlockList, rng: random.Random) -> None:
    for x in range(_POD_X0, _POD_X1 + 1):
        for z in range(_POD_Z0, _POD_Z1 + 1):
            _add(blocks, x, -2, z, "minecraft:deepslate")
            _add(blocks, x, -1, z, "minecraft:deepslate")

            if _shell_cell(x, z):
                _add(blocks, x, 0, z, "minecraft:deepslate")
            else:
                _add(blocks, x, 0, z, "minecraft:smooth_stone")

    for z in range(_POD_Z0 + 2, _POD_Z1):
        _add(blocks, _POD_CX, 0, z, "minecraft:polished_andesite")
    _add(blocks, _SPAWN_X, 0, _SPAWN_Z, "minecraft:smooth_stone")

    for y in range(1, 5):
        for x in range(_POD_X0, _POD_X1 + 1):
            for z in range(_POD_Z0, _POD_Z1 + 1):
                if not _shell_cell(x, z):
                    continue
                is_corner = (x in (_POD_X0, _POD_X1)) and (z in (_POD_Z0, _POD_Z1))
                is_side_window = y in (2, 3) and z in (_POD_CZ - 4, _POD_CZ, _POD_CZ + 4) and x in (_POD_X0, _POD_X1)
                if is_side_window:
                    block = "echoashfallprotocol:drop_pod_glass"
                elif is_corner and y in (1, 2):
                    block = "minecraft:crying_obsidian"
                elif y in (2, 3):
                    block = "minecraft:polished_andesite" if ((x + z) % 2 == 0) else "minecraft:smooth_stone"
                else:
                    block = _HULL
                _add(blocks, x, y, z, block)

    for x in range(_POD_X0, _POD_X1 + 1):
        for z in range(_POD_Z0, _POD_Z1 + 1):
            is_corner = (x in (_POD_X0, _POD_X1)) and (z in (_POD_Z0, _POD_Z1))
            if is_corner:
                continue
            inner = (_POD_X0 + 1 <= x <= _POD_X1 - 1) and (_POD_Z0 + 1 <= z <= _POD_Z1 - 1)
            center_lights = inner and x == _POD_CX and z in (_POD_CZ - 5, _POD_CZ, _POD_CZ + 5)
            if center_lights:
                _add(blocks, x, 5, z, "minecraft:sea_lantern")
            elif inner:
                _add(blocks, x, 5, z, "minecraft:quartz_block")
            else:
                _add(blocks, x, 5, z, _HULL)

    for x in range(_POD_CX - 5, _POD_CX + 6):
        for z in range(_POD_CZ - 6, _POD_CZ + 7):
            if abs(x - _POD_CX) + abs(z - _POD_CZ) <= 8:
                _add(blocks, x, 6, z, _HULL)
    for x in range(_POD_CX - 2, _POD_CX + 3):
        for z in range(_POD_CZ - 2, _POD_CZ + 3):
            _add(blocks, x, 7, z, _HULL)
    _add(blocks, _POD_CX, 7, _POD_CZ, "echoashfallprotocol:drop_pod_glass")
    _add(blocks, _POD_CX, 8, _POD_CZ, "minecraft:lightning_rod")

    for pos in [(_POD_X0, 3, _POD_Z0 + 2), (_POD_X1, 1, _POD_Z1 - 3), (_POD_X0 + 2, 5, _POD_Z0), (_POD_X1 - 2, 4, _POD_Z0 + 1)]:
        _add(blocks, *pos, "echoashfallprotocol:rusted_metal_debris")


_BOOK_PAGE_TEXT = (
    "AIR TOXIC. FILTER REQUIRED.\n"
    "GRID STATUS: OFFLINE.\n"
    "ECHO-7 ONLINE.\n"
    "OBJECTIVE: SURVIVE.\n"
    "SCAVENGE. ADAPT. RESTORE.\n"
    "ECHO: ASHFALL PROTOCOL."
)


def _echo_book_nbt() -> Dict[str, Any]:
    return {
        "id": "minecraft:written_book",
        "count": Byte(1),
        "components": {
            "minecraft:written_book_content": {
                "title": {"raw": "ECHO-7 TERMINAL LOG"},
                "author": "ECHO-7",
                "pages": [{"raw": json.dumps({"text": _BOOK_PAGE_TEXT})}],
                "resolved": Byte(1),
            }
        },
    }


def _sign_nbt() -> Dict[str, Any]:
    blank = '{"text":""}'
    return {
        "id": "minecraft:sign",
        "is_waxed": Byte(0),
        "front_text": {
            "messages": [
                blank,
                '{"text":"ECHO-7","color":"green","bold":true}',
                '{"text":"ONLINE","color":"green"}',
                blank,
            ],
            "color": "green",
            "has_glowing_text": Byte(1),
        },
        "back_text": {
            "messages": [blank, blank, blank, blank],
            "color": "black",
            "has_glowing_text": Byte(0),
        },
    }


def _barrel_nbt(loot_table: str) -> Dict[str, Any]:
    return {"id": "minecraft:barrel", "LootTable": loot_table}


def _chest_nbt(loot_table: str) -> Dict[str, Any]:
    return {"id": "minecraft:chest", "LootTable": loot_table}


def _clear_spawn_bay(blocks: BlockList) -> None:
    for pos in [
        (_SPAWN_X, _SPAWN_Y, _SPAWN_Z),
        (_SPAWN_X, _SPAWN_Y + 1, _SPAWN_Z),
        (_SPAWN_X - 1, _SPAWN_Y, _SPAWN_Z),
        (_SPAWN_X + 1, _SPAWN_Y, _SPAWN_Z),
    ]:
        _add(blocks, *pos, "minecraft:air")


def _build_interior(blocks: BlockList) -> None:
    barrel_tables = [
        "echoashfallprotocol:chests/drop_pod_survival",
        "echoashfallprotocol:chests/drop_pod_scrap",
        "echoashfallprotocol:chests/drop_pod_logs",
    ]
    for i, table in enumerate(barrel_tables):
        _add(
            blocks,
            _POD_X0 + 1 + i,
            1,
            _POD_Z0 + 1,
            "minecraft:barrel",
            {"facing": "south", "open": "false"},
            _barrel_nbt(table),
        )

    _add(blocks, _POD_X1 - 1, 1, _POD_CZ - 2, "minecraft:grindstone", {"face": "floor", "facing": "west"})
    _add(blocks, _POD_X1 - 1, 1, _POD_CZ, "minecraft:cartography_table")
    _add(blocks, _POD_X1 - 1, 1, _POD_CZ + 2, "minecraft:crafting_table")
    _add(
        blocks,
        _POD_X1 - 1,
        2,
        _POD_CZ,
        "minecraft:iron_trapdoor",
        {"facing": "west", "half": "bottom", "open": "false", "powered": "false", "waterlogged": "false"},
    )

    _add(blocks, _POD_CX, 1, _POD_Z0 + 1, "minecraft:quartz_pillar", {"axis": "y"})
    _add(blocks, _POD_CX, 2, _POD_Z0 + 1, "minecraft:quartz_pillar", {"axis": "y"})
    _add(
        blocks,
        _POD_CX,
        2,
        _POD_Z0 + 2,
        "minecraft:oak_wall_sign",
        {"facing": "south", "waterlogged": "false"},
        _sign_nbt(),
    )
    _add(blocks, _POD_CX, 2, _POD_Z0, "minecraft:sea_lantern")
    _add(
        blocks,
        _POD_CX - 1,
        1,
        _POD_Z0 + 2,
        "minecraft:lectern",
        {"facing": "south", "has_book": "true", "powered": "false"},
        {"id": "minecraft:lectern", "Book": _echo_book_nbt(), "Page": 0},
    )
    _add(
        blocks,
        _POD_CX + 2,
        1,
        _POD_Z0 + 2,
        "minecraft:chest",
        {"facing": "south", "type": "single", "waterlogged": "false"},
        _chest_nbt("echoashfallprotocol:chests/crashed_satellite_cache"),
    )

    _add(blocks, _POD_X0 + 1, 1, _POD_CZ - 1, "minecraft:oak_stairs", {"facing": "east", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    _add(blocks, _POD_X0 + 1, 1, _POD_CZ + 6, "minecraft:oak_stairs", {"facing": "east", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    _add(
        blocks,
        *_BED_FOOT,
        "minecraft:white_bed",
        {"facing": "south", "occupied": "false", "part": "foot"},
    )
    _add(
        blocks,
        *_BED_HEAD,
        "minecraft:white_bed",
        {"facing": "south", "occupied": "false", "part": "head"},
    )
    _add(blocks, _BED_FOOT[0] - 1, 1, _BED_FOOT[2], "minecraft:barrel", {"facing": "east", "open": "false"}, _barrel_nbt("echoashfallprotocol:chests/drop_pod_survival"))
    _add(blocks, _BED_FOOT[0] + 1, 1, _BED_HEAD[2], "minecraft:sea_lantern")

    for z in range(_POD_CZ + 1, _POD_Z1 - 2, 2):
        _add(blocks, _POD_X1 - 2, 1, z, "minecraft:heavy_weighted_pressure_plate", {"power": "0"})
        _add(blocks, _POD_X1 - 2, 2, z, "minecraft:iron_trapdoor", {"facing": "west", "half": "bottom", "open": "false", "powered": "false", "waterlogged": "false"})

    _add(
        blocks,
        _POD_CX,
        1,
        _POD_Z1,
        "minecraft:iron_door",
        {"facing": "north", "half": "lower", "hinge": "left", "open": "false", "powered": "false"},
    )
    _add(
        blocks,
        _POD_CX,
        2,
        _POD_Z1,
        "minecraft:iron_door",
        {"facing": "north", "half": "upper", "hinge": "left", "open": "false", "powered": "false"},
    )
    _add(blocks, _POD_CX - 1, 1, _POD_Z1, "minecraft:iron_bars", dict(_IRON_BARS))
    _add(blocks, _POD_CX + 1, 1, _POD_Z1, "minecraft:iron_bars", dict(_IRON_BARS))
    _add(
        blocks,
        _POD_CX + 1,
        2,
        _POD_Z1,
        "minecraft:stone_button",
        {"face": "wall", "facing": "south", "powered": "false"},
    )

    _add(blocks, _POD_CX, 0, _POD_Z1 + 1, "minecraft:polished_andesite")
    _add(blocks, _POD_CX - 1, 0, _POD_Z1 + 1, "minecraft:smooth_stone")
    _add(blocks, _POD_CX + 1, 0, _POD_Z1 + 1, "minecraft:smooth_stone")
    _add(blocks, _POD_CX, 0, _POD_Z1 + 2, "minecraft:polished_andesite_slab", {"type": "bottom", "waterlogged": "false"})
    _add(blocks, _POD_CX - 1, 0, _POD_Z1 + 2, "minecraft:polished_andesite_slab", {"type": "bottom", "waterlogged": "false"})
    _add(blocks, _POD_CX + 1, 0, _POD_Z1 + 2, "minecraft:polished_andesite_slab", {"type": "bottom", "waterlogged": "false"})
    for z in range(_POD_Z1 + 3, min(_CRATER_SIZE, _POD_Z1 + 7)):
        _add(blocks, _POD_CX, 0, z, "minecraft:stone_slab", {"type": "bottom", "waterlogged": "false"})

    _clear_spawn_bay(blocks)


def _build_hidden_compartment(blocks: BlockList) -> None:
    hx, hz = _POD_CX, _POD_Z0 - 1
    _add(blocks, hx - 1, 1, hz, _HULL)
    _add(blocks, hx + 1, 1, hz, _HULL)
    _add(blocks, hx, 0, hz, "minecraft:deepslate")
    _add(blocks, hx, 2, hz, _HULL)
    _add(blocks, hx, 1, hz - 1, _HULL)
    _add(
        blocks,
        hx,
        1,
        hz,
        "minecraft:chest",
        {"facing": "south", "type": "single", "waterlogged": "false"},
        _chest_nbt("echoashfallprotocol:chests/crashed_satellite_cache"),
    )


def generate_drop_pod(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []

    _build_crater(blocks, rng)
    _build_pod_shell(blocks, rng)
    _build_interior(blocks)
    _build_hidden_compartment(blocks)

    return blocks
