"""
Compact futuristic starting drop pod generator.

The starting pod is a dense armored survival module in a 20x20 landing scar.
It keeps a clear spawn bay, bed, ECHO control wall, visible supply lockers,
crafting surfaces, lights, slim landing struts, roof beacons, and a south ramp.
"""

import json
import random
from typing import Any, Dict, List, Optional, Tuple

from nbtlib.tag import Byte

BlockEntry = Tuple[int, int, int, str, Optional[Dict[str, str]], Optional[Dict[str, Any]]]
BlockList = List[BlockEntry]

_SITE_SIZE = 20
_POD_X0, _POD_X1 = 4, 15
_POD_Z0, _POD_Z1 = 4, 15
_POD_CX, _POD_CZ = 9, 9
_DOOR_X = _POD_CX

# Template-space feet block for the player. Java mirrors this coordinate after
# the two buried foundation layers shift into NBT-space.
_SPAWN_X, _SPAWN_Y, _SPAWN_Z = _POD_CX, 1, _POD_CZ + 3
_BED_FOOT = (_POD_X0 + 1, 1, _POD_CZ + 1)
_BED_HEAD = (_POD_X0 + 1, 1, _POD_CZ + 2)

_HULL = "echoashfallprotocol:drop_pod_hull"
_GLASS = "echoashfallprotocol:drop_pod_glass"
_DEBRIS = "echoashfallprotocol:rusted_metal_debris"
_CABLE = "echoashfallprotocol:cable_bundle"
_TWISTED = "echoashfallprotocol:twisted_metal"
_CRASH_SLAG = "echoashfallprotocol:crash_slag"
_YELLOW = "minecraft:yellow_concrete"
_DARK = "minecraft:black_concrete"
_MID = "minecraft:gray_concrete"
_LIGHT = "minecraft:light_gray_concrete"
_COPPER = "minecraft:cut_copper"
_POLISHED = "minecraft:polished_andesite"
_SLAB = "minecraft:polished_andesite_slab"
_STONE_SLAB = "minecraft:stone_slab"
_BLACKSTONE = "minecraft:polished_blackstone"
_DEEPSLATE = "minecraft:polished_deepslate"
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


def _in_bounds(x: int, z: int) -> bool:
    return 0 <= x < _SITE_SIZE and 0 <= z < _SITE_SIZE


def _is_clipped_corner(x: int, z: int) -> bool:
    return (
        (x <= _POD_X0 + 1 and z <= _POD_Z0 + 1)
        or (x <= _POD_X0 + 1 and z >= _POD_Z1 - 1)
        or (x >= _POD_X1 - 1 and z <= _POD_Z0 + 1)
        or (x >= _POD_X1 - 1 and z >= _POD_Z1 - 1)
    )


def _in_pod_footprint(x: int, z: int) -> bool:
    return _POD_X0 <= x <= _POD_X1 and _POD_Z0 <= z <= _POD_Z1 and not _is_clipped_corner(x, z)


def _shell_cell(x: int, z: int) -> bool:
    if not _in_pod_footprint(x, z):
        return False
    return any(not _in_pod_footprint(x + dx, z + dz) for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))


def _near_exit_path(x: int, z: int) -> bool:
    return abs(x - _DOOR_X) <= 2 and _POD_Z1 <= z < _SITE_SIZE


def _barrel_nbt(loot_table: str) -> Dict[str, Any]:
    return {"id": "minecraft:barrel", "LootTable": loot_table}


def _chest_nbt(loot_table: str) -> Dict[str, Any]:
    return {"id": "minecraft:chest", "LootTable": loot_table}


def _book_nbt(title: str, body: str) -> Dict[str, Any]:
    return {
        "id": "minecraft:written_book",
        "count": Byte(1),
        "components": {
            "minecraft:written_book_content": {
                "title": {"raw": title},
                "author": "ECHO-7",
                "pages": [{"raw": json.dumps({"text": body})}],
                "resolved": Byte(1),
            }
        },
    }


def _sign_nbt(*lines: str) -> Dict[str, Any]:
    blank = '{"text":""}'
    messages = []
    for line in list(lines[:4]) + [""] * max(0, 4 - len(lines)):
        messages.append(json.dumps({"text": line, "color": "yellow" if line.startswith("CAUTION") else "green", "bold": line.startswith(("ECHO", "DP"))}))
    return {
        "id": "minecraft:sign",
        "is_waxed": Byte(0),
        "front_text": {
            "messages": messages,
            "color": "green",
            "has_glowing_text": Byte(1),
        },
        "back_text": {
            "messages": [blank, blank, blank, blank],
            "color": "black",
            "has_glowing_text": Byte(0),
        },
    }


def _build_site(blocks: BlockList, rng: random.Random) -> None:
    for x in range(_SITE_SIZE):
        for z in range(_SITE_SIZE):
            if _in_pod_footprint(x, z):
                continue

            dx = x - (_POD_CX + 0.5)
            dz = z - (_POD_CZ + 0.5)
            dist = (dx * dx + dz * dz) ** 0.5
            if dist <= 7:
                mat = rng.choices(
                    [_CRASH_SLAG, "minecraft:blackstone", _BLACKSTONE, "minecraft:gravel"],
                    [0.22, 0.34, 0.20, 0.24],
                )[0]
            elif dist <= 9 and rng.random() < 0.42:
                mat = rng.choices(["minecraft:coarse_dirt", "minecraft:gravel", "minecraft:blackstone"], [0.58, 0.28, 0.14])[0]
            else:
                continue
            _add(blocks, x, 0, z, mat)

    # Edge anchors keep the generated NBT at the intended 20x20 footprint.
    for x, z in [(0, _POD_CZ), (_SITE_SIZE - 1, _POD_CZ), (_POD_CX, 0), (_POD_CX, _SITE_SIZE - 1)]:
        _add(blocks, x, 0, z, "minecraft:blackstone")

    for x, z in [(2, 2), (17, 2), (2, 17), (17, 17)]:
        _add(blocks, x, 0, z, _BLACKSTONE)
        _add(blocks, x, 1, z, "minecraft:stone_button", {"face": "floor", "facing": "north", "powered": "false"})

    for x, z in [(3, 16), (16, 3)]:
        _add(blocks, x, 0, z, "minecraft:campfire", {"lit": "true", "facing": "north", "signal_fire": "true", "waterlogged": "false"})
        _add(blocks, x, 1, z, _STONE_SLAB, {"type": "top", "waterlogged": "false"})

    debris_cells = [
        (x, z)
        for x in range(_SITE_SIZE)
        for z in range(_SITE_SIZE)
        if not _in_pod_footprint(x, z) and not _near_exit_path(x, z)
    ]
    rng.shuffle(debris_cells)

    for x, z in debris_cells[:10]:
        _add(blocks, x, 1, z, rng.choice([_DEBRIS, _CABLE, _TWISTED]))
    for x, z in debris_cells[10:16]:
        _add(blocks, x, 1, z, "minecraft:chain", {"axis": rng.choice(["x", "z", "y"]), "waterlogged": "false"})
    for x, z in debris_cells[16:22]:
        _add(blocks, x, 1, z, "minecraft:stone_button", {"face": "floor", "facing": "north", "powered": "false"})

    # Short, clean approach path and ramp apron.
    for z in range(_POD_Z1 + 1, _SITE_SIZE):
        for x in range(_DOOR_X - 2, _DOOR_X + 3):
            if _in_bounds(x, z):
                mat = _BLACKSTONE if abs(x - _DOOR_X) == 2 else _POLISHED
                _add(blocks, x, 0, z, mat)


def _build_foundation_and_floor(blocks: BlockList) -> None:
    for x in range(_POD_X0, _POD_X1 + 1):
        for z in range(_POD_Z0, _POD_Z1 + 1):
            if not _in_pod_footprint(x, z):
                continue
            _add(blocks, x, -2, z, "minecraft:deepslate")
            _add(blocks, x, -1, z, "minecraft:deepslate")
            edge = _shell_cell(x, z)
            _add(blocks, x, 0, z, _DARK if edge else _POLISHED)

    # A narrow caution path frames the work zones without turning the room into
    # a checkerboard hangar.
    for x in range(_POD_X0 + 3, _POD_X1 - 2):
        _add(blocks, x, 0, _POD_Z0 + 3, _YELLOW if x % 2 == 0 else _DARK)
    for z in range(_POD_Z0 + 4, _POD_Z1 - 2):
        for x in (_POD_X0 + 3, _POD_X1 - 3):
            _add(blocks, x, 0, z, _YELLOW if z % 2 == 0 else _DARK)

    for x, z in [(_SPAWN_X, _SPAWN_Z), (_SPAWN_X, _SPAWN_Z - 1), (_DOOR_X, _POD_Z1 - 1)]:
        _add(blocks, x, 0, z, _POLISHED)


def _wall_panel_block(x: int, y: int, z: int) -> str:
    rib = (
        x in (_POD_X0, _POD_X1, _POD_X0 + 1, _POD_X1 - 1)
        or z in (_POD_Z0, _POD_Z1, _POD_Z0 + 1, _POD_Z1 - 1)
        or (x in (_POD_CX - 3, _POD_CX + 3) and z in (_POD_Z0, _POD_Z1))
        or (z in (_POD_CZ - 3, _POD_CZ + 3) and x in (_POD_X0, _POD_X1))
    )
    if y in (1, 5):
        return _HULL
    if rib:
        return _LIGHT if y == 3 else _HULL
    if y == 4 and (x + z) % 4 == 0:
        return _MID
    return _DEEPSLATE


def _build_walls(blocks: BlockList) -> None:
    for y in range(1, 6):
        for x in range(_POD_X0, _POD_X1 + 1):
            for z in range(_POD_Z0, _POD_Z1 + 1):
                if not _shell_cell(x, z):
                    continue
                side_window = y in (2, 3) and (
                    (x in (_POD_X0, _POD_X1) and _POD_CZ - 1 <= z <= _POD_CZ + 1)
                    or (z == _POD_Z0 and _POD_CX <= x <= _POD_CX + 1)
                )
                is_door = z == _POD_Z1 and x == _DOOR_X and y in (1, 2)
                if is_door:
                    continue
                if side_window:
                    block = _GLASS
                else:
                    block = _wall_panel_block(x, y, z)
                _add(blocks, x, y, z, block)

    # Deliberate small warning plates, not full wall stripes.
    for x in (_POD_CX - 2, _POD_CX + 2):
        _add(blocks, x, 4, _POD_Z1, _YELLOW)
    for z in (_POD_CZ - 2, _POD_CZ + 2):
        _add(blocks, _POD_X0, 4, z, _YELLOW)
        _add(blocks, _POD_X1, 4, z, _YELLOW)

    for x, z, facing in [
        (_POD_X0 + 1, _POD_Z0 + 2, "south"),
        (_POD_X1 - 1, _POD_Z0 + 2, "south"),
        (_POD_X0 + 1, _POD_Z1 - 2, "north"),
        (_POD_X1 - 1, _POD_Z1 - 2, "north"),
    ]:
        _add(blocks, x, 2, z, "minecraft:stone_button", {"face": "wall", "facing": facing, "powered": "false"})


def _build_roof(blocks: BlockList) -> None:
    for x in range(_POD_X0 + 1, _POD_X1):
        for z in range(_POD_Z0 + 1, _POD_Z1):
            if _is_clipped_corner(x, z):
                continue
            edge_band = x in (_POD_X0 + 1, _POD_X1 - 1) or z in (_POD_Z0 + 1, _POD_Z1 - 1)
            block = _HULL if edge_band else _DEEPSLATE
            if edge_band and (x + z) % 5 == 0:
                block = _LIGHT
            _add(blocks, x, 6, z, block)

    # A smaller, cleaner roof caution frame and service hatch.
    for x in range(_POD_CX - 3, _POD_CX + 5):
        for z in (_POD_CZ - 3, _POD_CZ + 4):
            if _in_bounds(x, z) and _in_pod_footprint(x, z):
                _add(blocks, x, 6, z, _YELLOW if (x + z) % 2 == 0 else _DARK)
    for z in range(_POD_CZ - 2, _POD_CZ + 4):
        for x in (_POD_CX - 3, _POD_CX + 4):
            if _in_bounds(x, z) and _in_pod_footprint(x, z):
                _add(blocks, x, 6, z, _YELLOW if (x + z) % 2 == 0 else _DARK)

    for x in range(_POD_CX - 1, _POD_CX + 3):
        for z in range(_POD_CZ - 1, _POD_CZ + 3):
            _add(blocks, x, 7, z, _HULL)
    _add(blocks, _POD_CX, 7, _POD_CZ, _GLASS)
    _add(blocks, _POD_CX + 1, 7, _POD_CZ + 1, "minecraft:sea_lantern")

    for x, z in [(_POD_X0 + 2, _POD_Z0 + 2), (_POD_X1 - 2, _POD_Z0 + 2), (_POD_X0 + 2, _POD_Z1 - 2), (_POD_X1 - 2, _POD_Z1 - 2)]:
        _add(blocks, x, 6, z, "minecraft:blackstone")
        _add(blocks, x, 7, z, "minecraft:redstone_torch", {"lit": "true"})


def _build_struts_and_pipes(blocks: BlockList) -> None:
    struts = [
        ((_POD_X0 + 2, _POD_Z0 + 1), (2, 2), (1, 0, 0, 1)),
        ((_POD_X1 - 2, _POD_Z0 + 1), (17, 2), (-1, 0, 0, 1)),
        ((_POD_X0 + 2, _POD_Z1 - 1), (2, 17), (1, 0, 0, -1)),
        ((_POD_X1 - 2, _POD_Z1 - 1), (17, 17), (-1, 0, 0, -1)),
    ]
    for (sx, sz), (fx, fz), (foot_dx, foot_dz, brace_dx, brace_dz) in struts:
        _add(blocks, fx, 0, fz, _BLACKSTONE)
        _add(blocks, fx + foot_dx, 0, fz, _BLACKSTONE)
        _add(blocks, fx, 0, fz + foot_dz, _BLACKSTONE)
        _add(blocks, fx, 1, fz, _HULL)
        mx, mz = (sx + fx) // 2, (sz + fz) // 2
        _add(blocks, mx, 1, mz, "minecraft:iron_bars", dict(_IRON_BARS))
        _add(blocks, mx, 2, mz, _COPPER)
        _add(blocks, mx + brace_dx, 1, mz + brace_dz, "minecraft:chain", {"axis": "y", "waterlogged": "false"})
        _add(blocks, sx, 1, sz, _LIGHT)
        _add(blocks, sx, 2, sz, _HULL)

    for x in range(_POD_X0 + 3, _POD_X1 - 2, 3):
        _add(blocks, x, 3, _POD_Z0 - 1, _COPPER)
        _add(blocks, x + 1, 3, _POD_Z0 - 1, _CABLE)
        _add(blocks, x, 3, _POD_Z1 + 1, _COPPER)
    for z in range(_POD_Z0 + 3, _POD_Z1 - 2, 3):
        _add(blocks, _POD_X0 - 1, 3, z, _CABLE)
        _add(blocks, _POD_X1 + 1, 3, z, _COPPER)


def _clear_spawn_bay(blocks: BlockList) -> None:
    for pos in [
        (_SPAWN_X, _SPAWN_Y, _SPAWN_Z),
        (_SPAWN_X, _SPAWN_Y + 1, _SPAWN_Z),
        (_SPAWN_X, _SPAWN_Y, _SPAWN_Z - 1),
        (_SPAWN_X, _SPAWN_Y + 1, _SPAWN_Z - 1),
        (_SPAWN_X - 1, _SPAWN_Y, _SPAWN_Z),
        (_SPAWN_X + 1, _SPAWN_Y, _SPAWN_Z),
        (_SPAWN_X - 1, _SPAWN_Y + 1, _SPAWN_Z),
        (_SPAWN_X + 1, _SPAWN_Y + 1, _SPAWN_Z),
    ]:
        _add(blocks, *pos, "minecraft:air")


def _build_lockers(blocks: BlockList) -> None:
    lockers = [
        (_POD_X0 + 2, _POD_Z0 + 1, "echoashfallprotocol:chests/drop_pod_survival", "OXYGEN"),
        (_POD_X0 + 3, _POD_Z0 + 1, "echoashfallprotocol:chests/drop_pod_tools", "TOOLS"),
        (_POD_X1 - 3, _POD_Z0 + 1, "echoashfallprotocol:chests/drop_pod_scrap", "SCRAP"),
        (_POD_X1 - 2, _POD_Z0 + 1, "echoashfallprotocol:chests/drop_pod_logs", "LOGS"),
    ]
    for x, z, table, label in lockers:
        _add(blocks, x, 1, z, "minecraft:barrel", {"facing": "south", "open": "false"}, _barrel_nbt(table))
        _add(blocks, x, 2, z + 1, "minecraft:oak_wall_sign", {"facing": "south", "waterlogged": "false"}, _sign_nbt(label))
        _add(blocks, x, 2, z, "minecraft:iron_trapdoor", {"facing": "north", "half": "bottom", "open": "false", "powered": "false", "waterlogged": "false"})


def _build_echo_wall(blocks: BlockList) -> None:
    _add(blocks, _POD_CX, 1, _POD_Z0 + 2, "minecraft:lectern", {"facing": "south", "has_book": "true", "powered": "false"}, {
        "id": "minecraft:lectern",
        "Book": _book_nbt(
            "ECHO-7 WAKE LOG",
            "ECHO-7 ONLINE.\nPOD DP-07 IMPACT STABLE.\nLOCKERS: OXYGEN, TOOLS, SCRAP, LOGS.\nOBJECTIVE: SURVIVE.",
        ),
        "Page": 0,
    })
    _add(blocks, _POD_CX, 2, _POD_Z0 + 1, "minecraft:oak_wall_sign", {"facing": "south", "waterlogged": "false"}, _sign_nbt("ECHO-7", "ONLINE", "DP-07"))
    _add(blocks, _POD_CX - 1, 1, _POD_Z0 + 2, "minecraft:cartography_table")
    _add(blocks, _POD_CX + 1, 1, _POD_Z0 + 2, "minecraft:crafting_table")
    _add(blocks, _POD_CX, 3, _POD_Z0 + 1, "minecraft:sea_lantern")
    _add(blocks, _POD_CX - 1, 0, _POD_Z0 + 2, _DARK)
    _add(blocks, _POD_CX, 0, _POD_Z0 + 2, _DARK)
    _add(blocks, _POD_CX + 1, 0, _POD_Z0 + 2, _DARK)


def _build_interior(blocks: BlockList) -> None:
    _build_lockers(blocks)
    _build_echo_wall(blocks)

    _add(blocks, *_BED_FOOT, "minecraft:white_bed", {"facing": "south", "occupied": "false", "part": "foot"})
    _add(blocks, *_BED_HEAD, "minecraft:white_bed", {"facing": "south", "occupied": "false", "part": "head"})
    _add(blocks, _BED_FOOT[0], 2, _BED_FOOT[2] - 1, "minecraft:sea_lantern")
    _add(blocks, _POD_X0 + 2, 1, _POD_CZ + 3, "minecraft:heavy_weighted_pressure_plate", {"power": "0"})
    _add(blocks, _POD_X0 + 2, 2, _POD_CZ + 3, "minecraft:iron_trapdoor", {"facing": "west", "half": "bottom", "open": "false", "powered": "false", "waterlogged": "false"})

    _add(blocks, _POD_X1 - 1, 1, _POD_CZ - 1, "minecraft:grindstone", {"face": "floor", "facing": "west"})
    _add(blocks, _POD_X1 - 1, 1, _POD_CZ + 1, "minecraft:chest", {"facing": "west", "type": "single", "waterlogged": "false"}, _chest_nbt("echoashfallprotocol:chests/crashed_satellite_cache"))
    _add(blocks, _POD_X1 - 1, 2, _POD_CZ + 1, "minecraft:iron_trapdoor", {"facing": "west", "half": "bottom", "open": "false", "powered": "false", "waterlogged": "false"})

    # Compact floor consoles make the room feel occupied while preserving the
    # spawn bay and runtime starter-cache placement options.
    for x, z in [(_POD_CX - 2, _POD_CZ + 1), (_POD_CX + 2, _POD_CZ + 1), (_POD_CX + 2, _POD_CZ + 3)]:
        _add(blocks, x, 1, z, "minecraft:heavy_weighted_pressure_plate", {"power": "0"})
        _add(blocks, x, 2, z, "minecraft:iron_trapdoor", {"facing": "north", "half": "bottom", "open": "false", "powered": "false", "waterlogged": "false"})

    for x, z in [(_POD_CX - 2, _POD_CZ), (_POD_CX + 2, _POD_CZ), (_POD_CX, _POD_CZ + 1)]:
        _add(blocks, x, 5, z, "minecraft:sea_lantern")

    _add(blocks, _DOOR_X, 1, _POD_Z1, "minecraft:iron_door", {"facing": "north", "half": "lower", "hinge": "left", "open": "false", "powered": "false"})
    _add(blocks, _DOOR_X, 2, _POD_Z1, "minecraft:iron_door", {"facing": "north", "half": "upper", "hinge": "left", "open": "false", "powered": "false"})
    _add(blocks, _DOOR_X - 1, 1, _POD_Z1, "minecraft:iron_bars", dict(_IRON_BARS))
    _add(blocks, _DOOR_X + 1, 1, _POD_Z1, "minecraft:iron_bars", dict(_IRON_BARS))
    _add(blocks, _DOOR_X + 1, 2, _POD_Z1, "minecraft:stone_button", {"face": "wall", "facing": "south", "powered": "false"})
    _add(blocks, _DOOR_X, 3, _POD_Z1, "minecraft:oak_wall_sign", {"facing": "south", "waterlogged": "false"}, _sign_nbt("CAUTION", "DROP RAMP"))

    for z in range(_POD_Z1 + 1, _SITE_SIZE):
        _add(blocks, _DOOR_X, 0, z, _SLAB, {"type": "bottom", "waterlogged": "false"})
        _add(blocks, _DOOR_X - 1, 0, z, _STONE_SLAB, {"type": "bottom", "waterlogged": "false"})
        _add(blocks, _DOOR_X + 1, 0, z, _STONE_SLAB, {"type": "bottom", "waterlogged": "false"})
        if z % 2 == 0:
            _add(blocks, _DOOR_X - 2, 0, z, _YELLOW)
            _add(blocks, _DOOR_X + 2, 0, z, _DARK)

    _clear_spawn_bay(blocks)


def generate_drop_pod(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []

    _build_site(blocks, rng)
    _build_foundation_and_floor(blocks)
    _build_walls(blocks)
    _build_roof(blocks)
    _build_struts_and_pipes(blocks)
    _build_interior(blocks)

    return blocks
