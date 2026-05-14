"""
Compact futuristic starting drop pod generator.

The starter pod is a small Blockworks-heavy crash capsule in a 16x16 landing
scar. It keeps a clear spawn bay, emergency bunk, ECHO cache/crate storage,
terminal wall, landing struts, warning lights, and a south hatch/ramp.
"""

import random
from typing import Any, Dict, List, Optional, Tuple

BlockEntry = Tuple[int, int, int, str, Optional[Dict[str, str]], Optional[Dict[str, Any]]]
BlockList = List[BlockEntry]

_SITE_SIZE = 16
_POD_X0, _POD_X1 = 3, 12
_POD_Z0, _POD_Z1 = 3, 12
_POD_CX, _POD_CZ = 8, 8
_DOOR_X = _POD_CX

# Generator-space coordinates. The NBT writer shifts y by +2 because the
# template has two buried foundation layers at y=-2 and y=-1.
_SPAWN_X, _SPAWN_Y, _SPAWN_Z = 8, 1, 10
_BUNK_FOOT = (4, 1, 7)
_BUNK_HEAD = (4, 1, 8)

_BW = "echoblockworks:"
_HULL = _BW + "orbital_hull_hull_panel"
_HULL_DAMAGED = _BW + "orbital_hull_damaged_hull"
_HULL_BLACK = _BW + "orbital_hull_black_hull"
_HULL_WHITE = _BW + "orbital_hull_white_hull"
_THERMAL = _BW + "orbital_hull_thermal_tile"
_AIRLOCK = _BW + "orbital_hull_airlock_frame"
_DOCKING = _BW + "orbital_hull_docking_trim"
_ORBITAL_LIT = _BW + "orbital_hull_lit_strip"
_METAL = _BW + "reinforced_metal_panel"
_RIVETED = _BW + "reinforced_metal_riveted"
_GRATE = _BW + "reinforced_metal_grate"
_FRAME = _BW + "reinforced_metal_frame"
_HAZARD = _BW + "reinforced_metal_hazard_stripe"
_LIT_PANEL = _BW + "reinforced_metal_lit_panel"
_PILLAR = _BW + "reinforced_metal_pillar"
_TERMINAL = _BW + "terminal_panel_wall_panel"
_SCREEN = _BW + "terminal_panel_screen"
_DATA = _BW + "terminal_panel_data_panel"
_WARN_PANEL = _BW + "terminal_panel_warning_panel"
_SERVER = _BW + "terminal_panel_server_rack"
_CIRCUIT = _BW + "echo_circuit_circuit_panel"
_MATRIX = _BW + "echo_circuit_matrix"
_GLOWING_CIRCUIT = _BW + "echo_circuit_glowing_circuit"
_STRIP_LIGHT = _BW + "echo_strip_light"
_BEACON = _BW + "warning_beacon"
_FLICKER = _BW + "flickering_warning_light"
_DATA_WALL = _BW + "data_wall"
_MONITOR = _BW + "broken_monitor"
_WALL_PIPE = _BW + "wall_pipe"
_CEILING_PIPE = _BW + "ceiling_pipe"
_STEAM_VENT = _BW + "steam_vent"
_SPARK = _BW + "sparking_cable_panel"
_RUBBLE = _BW + "rubble_pile"
_SCATTER = _BW + "scattered_debris"
_HANGING_WIRE = _BW + "hanging_wire"
_PROJECTOR = _BW + "hologram_floor_projector"
_DISH = _BW + "signal_dish_decorative"

_BUNK = "echoashfallprotocol:emergency_bunk"
_CACHE = "echoashfallprotocol:echo_cache"
_CRATE = "echoashfallprotocol:echo_crate"
_GLASS = "echoashfallprotocol:drop_pod_glass"
_CRASH_SLAG = "echoashfallprotocol:crash_slag"
_SCORCHED = "echoashfallprotocol:scorched_ash"
_BURNT_SOIL = "echoashfallprotocol:burnt_wasteland_soil"
_CONCRETE_RUBBLE = "echoashfallprotocol:concrete_rubble"
_RUSTED_DEBRIS = "echoashfallprotocol:rusted_metal_debris"
_TWISTED = "echoashfallprotocol:twisted_metal"
_CABLE = "echoashfallprotocol:cable_bundle"


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
    blocks[:] = [b for b in blocks if b[0] != x or b[1] != y or b[2] != z]
    blocks.append((x, y, z, block_id, props, nbt))


def _in_bounds(x: int, z: int) -> bool:
    return 0 <= x < _SITE_SIZE and 0 <= z < _SITE_SIZE


def _is_clipped_corner(x: int, z: int) -> bool:
    return (
        (x == _POD_X0 and z == _POD_Z0)
        or (x == _POD_X0 and z == _POD_Z1)
        or (x == _POD_X1 and z == _POD_Z0)
        or (x == _POD_X1 and z == _POD_Z1)
    )


def _in_pod_footprint(x: int, z: int) -> bool:
    return _POD_X0 <= x <= _POD_X1 and _POD_Z0 <= z <= _POD_Z1 and not _is_clipped_corner(x, z)


def _shell_cell(x: int, z: int) -> bool:
    if not _in_pod_footprint(x, z):
        return False
    return any(not _in_pod_footprint(x + dx, z + dz) for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))


def _near_exit_path(x: int, z: int) -> bool:
    return abs(x - _DOOR_X) <= 2 and _POD_Z1 <= z < _SITE_SIZE


def _protected_path_cell(x: int, z: int) -> bool:
    if abs(x - _SPAWN_X) <= 1 and _POD_CZ <= z <= _SPAWN_Z:
        return True
    if _POD_X0 + 1 <= x <= _SPAWN_X and _BUNK_FOOT[2] <= z <= _SPAWN_Z:
        return True
    if x in (5, 6, 10, 11) and _POD_Z0 + 1 <= z <= _POD_Z0 + 2:
        return True
    return _near_exit_path(x, z)


def _path_blocking_clutter(block_id: str) -> bool:
    return block_id in {
        _RUSTED_DEBRIS,
        _TWISTED,
        _CABLE,
        _RUBBLE,
        _SCATTER,
        _STEAM_VENT,
    }


def _container_nbt(loot_table: str) -> Dict[str, Any]:
    return {"id": "echoashfallprotocol:echo_container", "LootTable": loot_table}


def _build_site(blocks: BlockList, rng: random.Random) -> None:
    for x in range(_SITE_SIZE):
        for z in range(_SITE_SIZE):
            if _in_pod_footprint(x, z):
                continue
            dx = x - (_POD_CX - 0.5)
            dz = z - (_POD_CZ - 0.5)
            dist = (dx * dx + dz * dz) ** 0.5
            if dist <= 6.2:
                mat = rng.choices([_CRASH_SLAG, _SCORCHED, _BURNT_SOIL, _CONCRETE_RUBBLE], [0.42, 0.28, 0.18, 0.12])[0]
                _add(blocks, x, 0, z, mat)
            elif dist <= 8.0 and rng.random() < 0.34:
                _add(blocks, x, 0, z, rng.choice([_BURNT_SOIL, _SCORCHED, _CONCRETE_RUBBLE]))

    # Anchor the intended footprint and make the landing scar readable.
    for x, z in [(0, _POD_CZ), (_SITE_SIZE - 1, _POD_CZ), (_POD_CX, 0), (_POD_CX, _SITE_SIZE - 1)]:
        _add(blocks, x, 0, z, _CRASH_SLAG)

    debris_cells = [
        (x, z)
        for x in range(_SITE_SIZE)
        for z in range(_SITE_SIZE)
        if not _in_pod_footprint(x, z) and not _protected_path_cell(x, z)
    ]
    rng.shuffle(debris_cells)
    for x, z in debris_cells[:8]:
        _add(blocks, x, 1, z, rng.choice([_RUSTED_DEBRIS, _TWISTED, _CABLE, _RUBBLE, _SCATTER]))
    for x, z in debris_cells[8:12]:
        if rng.random() < 0.5:
            _add(blocks, x, 1, z, _STEAM_VENT)
        else:
            _add(blocks, x, 1, z, _SPARK, {"facing": rng.choice(["north", "south", "east", "west"])})

    for z in range(_POD_Z1 + 1, _SITE_SIZE):
        for x in range(_DOOR_X - 2, _DOOR_X + 3):
            if _in_bounds(x, z):
                _add(blocks, x, 0, z, _HAZARD if abs(x - _DOOR_X) == 2 else _GRATE)


def _build_floor(blocks: BlockList) -> None:
    for x in range(_POD_X0, _POD_X1 + 1):
        for z in range(_POD_Z0, _POD_Z1 + 1):
            if not _in_pod_footprint(x, z):
                continue
            _add(blocks, x, -2, z, _CRASH_SLAG)
            _add(blocks, x, -1, z, _RIVETED)
            if _shell_cell(x, z):
                floor = _FRAME
            elif (x == _SPAWN_X and z in (_SPAWN_Z, _SPAWN_Z - 1)) or (x == _DOOR_X and z >= _POD_CZ):
                floor = _GRATE
            elif x in (_POD_X0 + 2, _POD_X1 - 2) and _POD_Z0 + 3 <= z <= _POD_Z1 - 2:
                floor = _HAZARD
            else:
                floor = _METAL
            _add(blocks, x, 0, z, floor)


def _wall_block(x: int, y: int, z: int) -> str:
    if y == 1:
        return _AIRLOCK if (x + z) % 3 == 0 else _FRAME
    if y == 5:
        return _HULL_BLACK if (x + z) % 2 else _HULL
    rib = x in (_POD_X0, _POD_X1) or z in (_POD_Z0, _POD_Z1)
    if rib and y == 3:
        return _ORBITAL_LIT if (x + z) % 5 == 0 else _HULL
    if y == 4 and (x + z) % 4 == 0:
        return _HULL_WHITE
    if (x + z + y) % 7 == 0:
        return _HULL_DAMAGED
    return _THERMAL if y == 2 else _HULL


def _build_walls(blocks: BlockList) -> None:
    for y in range(1, 6):
        for x in range(_POD_X0, _POD_X1 + 1):
            for z in range(_POD_Z0, _POD_Z1 + 1):
                if not _shell_cell(x, z):
                    continue
                if z == _POD_Z1 and x == _DOOR_X and y in (1, 2):
                    _add(blocks, x, y, z, "minecraft:air")
                    continue
                window = y in (2, 3) and (
                    (x in (_POD_X0, _POD_X1) and _POD_CZ - 1 <= z <= _POD_CZ + 1)
                    or (z == _POD_Z0 and _POD_CX - 1 <= x <= _POD_CX)
                )
                _add(blocks, x, y, z, _GLASS if window else _wall_block(x, y, z))

    for x in (_DOOR_X - 1, _DOOR_X + 1):
        _add(blocks, x, 1, _POD_Z1, _AIRLOCK)
        _add(blocks, x, 2, _POD_Z1, _ORBITAL_LIT)
    _add(blocks, _DOOR_X, 3, _POD_Z1, _WARN_PANEL)
    _add(blocks, _DOOR_X, 4, _POD_Z1, _AIRLOCK)

    for x, z, facing in [
        (_POD_X0 + 1, _POD_Z0 + 2, "east"),
        (_POD_X1 - 1, _POD_Z0 + 2, "west"),
        (_POD_X0 + 1, _POD_Z1 - 2, "east"),
        (_POD_X1 - 1, _POD_Z1 - 2, "west"),
    ]:
        _add(blocks, x, 3, z, _SPARK, {"facing": facing})


def _build_roof(blocks: BlockList) -> None:
    for x in range(_POD_X0 + 1, _POD_X1):
        for z in range(_POD_Z0 + 1, _POD_Z1):
            if _is_clipped_corner(x, z):
                continue
            edge = x in (_POD_X0 + 1, _POD_X1 - 1) or z in (_POD_Z0 + 1, _POD_Z1 - 1)
            block = _HULL_BLACK if edge else _HULL
            if x in (_POD_CX - 1, _POD_CX) and _POD_CZ - 1 <= z <= _POD_CZ + 1:
                block = _ORBITAL_LIT
            elif (x + z) % 6 == 0:
                block = _HULL_DAMAGED
            _add(blocks, x, 6, z, block)

    for x, z in [(_POD_X0 + 2, _POD_Z0 + 2), (_POD_X1 - 2, _POD_Z0 + 2), (_POD_X0 + 2, _POD_Z1 - 2), (_POD_X1 - 2, _POD_Z1 - 2)]:
        _add(blocks, x, 6, z, _BEACON)

    _add(blocks, _POD_CX, 6, _POD_CZ, _DISH, {"facing": "south"})
    _add(blocks, _POD_CX - 1, 5, _POD_CZ, _STRIP_LIGHT)
    _add(blocks, _POD_CX + 1, 5, _POD_CZ, _STRIP_LIGHT)
    _add(blocks, _POD_CX, 5, _POD_CZ - 2, _CEILING_PIPE)
    _add(blocks, _POD_CX, 5, _POD_CZ + 2, _HANGING_WIRE)


def _build_struts_and_ramp(blocks: BlockList) -> None:
    struts = [
        ((_POD_X0 + 1, _POD_Z0 + 1), (1, 2)),
        ((_POD_X1 - 1, _POD_Z0 + 1), (14, 2)),
        ((_POD_X0 + 1, _POD_Z1 - 1), (1, 13)),
        ((_POD_X1 - 1, _POD_Z1 - 1), (14, 13)),
    ]
    for (sx, sz), (fx, fz) in struts:
        _add(blocks, fx, 0, fz, _PILLAR)
        _add(blocks, fx, 1, fz, _FRAME)
        mx, mz = (sx + fx) // 2, (sz + fz) // 2
        _add(blocks, mx, 1, mz, _DOCKING)
        _add(blocks, sx, 1, sz, _ORBITAL_LIT)

    for z in range(_POD_Z1 + 1, _SITE_SIZE):
        _add(blocks, _DOOR_X, 1, z, _GRATE)
        _add(blocks, _DOOR_X - 1, 1, z, _METAL)
        _add(blocks, _DOOR_X + 1, 1, z, _METAL)
        if z % 2 == 1:
            _add(blocks, _DOOR_X - 2, 1, z, _HAZARD)
            _add(blocks, _DOOR_X + 2, 1, z, _HAZARD)


def _build_lockers(blocks: BlockList) -> None:
    lockers = [
        (5, 4, "echoashfallprotocol:chests/drop_pod_survival"),
        (6, 4, "echoashfallprotocol:chests/drop_pod_tools"),
        (10, 4, "echoashfallprotocol:chests/drop_pod_scrap"),
        (11, 4, "echoashfallprotocol:chests/drop_pod_logs"),
    ]
    for x, z, table in lockers:
        _add(blocks, x, 1, z, _CRATE, {"facing": "south"}, _container_nbt(table))
        _add(blocks, x, 2, z, _TERMINAL)
        _add(blocks, x, 3, z, _DATA)


def _build_echo_wall(blocks: BlockList) -> None:
    for x, block in [(_POD_CX - 1, _SCREEN), (_POD_CX, _DATA_WALL), (_POD_CX + 1, _SERVER)]:
        _add(blocks, x, 1, _POD_Z0 + 1, block, {"facing": "south"} if block in (_DATA_WALL, _SERVER) else None)
        _add(blocks, x, 2, _POD_Z0 + 1, _CIRCUIT)
    _add(blocks, _POD_CX, 3, _POD_Z0 + 1, _GLOWING_CIRCUIT)
    _add(blocks, _POD_CX - 1, 0, _POD_Z0 + 2, _MATRIX)
    _add(blocks, _POD_CX, 0, _POD_Z0 + 2, _PROJECTOR)
    _add(blocks, _POD_CX + 1, 0, _POD_Z0 + 2, _MATRIX)
    _add(blocks, _POD_CX - 2, 2, _POD_Z0 + 1, _MONITOR, {"facing": "south"})
    _add(blocks, _POD_CX + 2, 2, _POD_Z0 + 1, _WALL_PIPE, {"facing": "south"})


def _build_interior(blocks: BlockList) -> None:
    _build_lockers(blocks)
    _build_echo_wall(blocks)

    _add(blocks, *_BUNK_FOOT, _BUNK, {"facing": "south", "part": "foot", "occupied": "false"})
    _add(blocks, *_BUNK_HEAD, _BUNK, {"facing": "south", "part": "head", "occupied": "false"})
    _add(blocks, _BUNK_FOOT[0], 2, _BUNK_FOOT[2] - 1, _STRIP_LIGHT)
    _add(blocks, _BUNK_FOOT[0] + 1, 1, _BUNK_FOOT[2] - 1, _WALL_PIPE, {"facing": "east"})

    _add(blocks, _POD_X1 - 1, 1, _POD_CZ + 1, _CACHE, {"facing": "west"}, _container_nbt("echoashfallprotocol:chests/crashed_satellite_cache"))
    _add(blocks, _POD_X1 - 1, 2, _POD_CZ + 1, _WARN_PANEL)
    _add(blocks, _POD_X1 - 1, 1, _POD_CZ - 1, _STEAM_VENT)
    _add(blocks, _POD_X1 - 1, 2, _POD_CZ - 1, _FLICKER, {"facing": "west"})

    for x, z in [(_POD_CX - 2, _POD_CZ), (_POD_CX + 2, _POD_CZ), (_POD_CX, _POD_CZ + 1)]:
        _add(blocks, x, 1, z, _CIRCUIT)
        _add(blocks, x, 2, z, _MATRIX)


def _clear_spawn_bay(blocks: BlockList) -> None:
    for x in range(_SPAWN_X - 1, _SPAWN_X + 2):
        for y in (_SPAWN_Y, _SPAWN_Y + 1):
            for z in (_SPAWN_Z - 1, _SPAWN_Z + 1):
                _add(blocks, x, y, z, "minecraft:air")
    _add(blocks, _DOOR_X, 1, _POD_Z1, "minecraft:air")
    _add(blocks, _DOOR_X, 2, _POD_Z1, "minecraft:air")


def _clear_protected_path_clutter(blocks: BlockList) -> None:
    blocks[:] = [
        block
        for block in blocks
        if not (
            block[1] == _SPAWN_Y
            and _protected_path_cell(block[0], block[2])
            and _path_blocking_clutter(block[3])
        )
    ]


def generate_drop_pod(seed: int) -> BlockList:
    rng = _r(seed)
    blocks: BlockList = []

    _build_site(blocks, rng)
    _build_floor(blocks)
    _build_walls(blocks)
    _build_roof(blocks)
    _build_struts_and_ramp(blocks)
    _build_interior(blocks)
    _clear_spawn_bay(blocks)
    _clear_protected_path_clutter(blocks)

    return blocks
