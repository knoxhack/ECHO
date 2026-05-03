"""
Enhanced Special Structure Generators v2 - Large Scale Landmarks

Upgraded versions of bio_lab, military_vault, data_center, and reactor_ruin
with multi-room layouts, verticality, and detailed interiors.
"""

import random
from typing import List, Tuple, Optional, Dict

from .poi_primitives import (
    add_block,
    add_supply_cluster,
    bridge_section,
    collapsed_section,
    doorway,
    elevated_platform,
    enforce_guardrails,
    fill,
    hollow_rect,
    line,
    multi_room_layout,
    pillared_hall,
    scatter,
    stairwell,
    underground_vault,
    watchtower,
    window_opening,
)

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]


def _r(seed: int) -> random.Random:
    return random.Random(seed)


def generate_bio_facility(seed: int) -> BlockList:
    """Big: 30x24 bio-research facility with containment, labs, and specimen vault."""
    rng = _r(seed)
    blocks: BlockList = []

    wall_block = "minecraft:stone_bricks"
    floor_block = "minecraft:smooth_stone"
    glass_block = "minecraft:glass"

    # Main building footprint
    main_x, main_z = 2, 2
    main_w, main_d = 26, 20

    # Foundation and floor
    fill(blocks, main_x, 0, main_z, main_x + main_w, 0, main_z + main_d, floor_block)

    # === Containment Wing (west) ===
    containment_x, containment_z = 4, 4
    containment_w, containment_d = 10, 8

    # Containment cells
    rooms = multi_room_layout(blocks, rng, containment_x, containment_z, containment_w, containment_d,
                               wall_block, floor_block, room_count=3)

    # Glass walls for cells
    for rx, rz, rx2, rz2 in rooms:
        for x in range(rx + 1, rx2):
            for y in range(1, 3):
                add_block(blocks, x, y, rz + (rz2 - rz) // 2, glass_block)

    # Containment equipment
    for room in rooms:
        rx, rz, rx2, rz2 = room
        cx, cz = (rx + rx2) // 2, (rz + rz2) // 2
        add_block(blocks, cx, 1, cz, "minecraft:cauldron")
        if rng.random() > 0.5:
            add_block(blocks, cx + 1, 1, cz, "echoashfallprotocol:bio_processing_station")

    # === Research Wing (east) ===
    research_x, research_z = 16, 4
    research_w, research_d = 10, 8

    # Open research hall
    for x in range(research_x, research_x + research_w + 1):
        for z in [research_z, research_z + research_d]:
            for y in range(1, 4):
                add_block(blocks, x, y, z, wall_block)
    for z in range(research_z, research_z + research_d + 1):
        for x in [research_x, research_x + research_w]:
            for y in range(1, 4):
                add_block(blocks, x, y, z, wall_block)

    # Lab benches (observers as equipment)
    for lx in range(research_x + 2, research_x + research_w - 1, 2):
        add_block(blocks, lx, 1, research_z + 2, "minecraft:observer")
        add_block(blocks, lx, 1, research_z + research_d - 2, "minecraft:observer")

    # === Central Corridor ===
    corridor_x = 14
    for z in range(main_z, main_z + main_d + 1):
        add_block(blocks, corridor_x, 0, z, "minecraft:gravel")
        for x in [corridor_x - 1, corridor_x + 1]:
            add_block(blocks, x, 1, z, wall_block)

    # Doorways connecting wings
    doorway(blocks, corridor_x - 2, 1, containment_z + containment_d // 2, "east", width=2, height=2)
    doorway(blocks, corridor_x + 2, 1, research_z + research_d // 2, "west", width=2, height=2)

    # === Specimen Vault (underground) ===
    vault_x, vault_z = main_x + main_w // 2, main_z + main_d // 2
    vault_cx, vault_cz = underground_vault(blocks, rng, vault_x, vault_z, 8, 8, 4, wall_block, floor_block)

    # Vault contents
    add_block(blocks, vault_cx - 2, -4, vault_cz - 2, "echoashfallprotocol:bio_processing_station")
    add_block(blocks, vault_cx + 2, -4, vault_cz + 2, "minecraft:spawner")  # Containment breach hint
    add_supply_cluster(blocks, rng, vault_cx - 1, vault_cz - 1, 2, 2, containers=2)

    # === Ventilation System ===
    # Pipes running along ceiling
    for z in range(main_z, main_z + main_d + 1):
        for x in [6, 20]:
            add_block(blocks, x, 4, z, "minecraft:iron_bars")

    # === Exterior Details ===
    # Warning signs/barrels
    add_block(blocks, main_x, 1, main_z + main_d + 1, "echoashfallprotocol:toxic_waste_barrel")
    add_block(blocks, main_x + main_w, 1, main_z + main_d + 1, "echoashfallprotocol:toxic_waste_barrel")

    # Overgrown/bio-hazard vegetation
    for _ in range(15):
        vx = rng.randint(main_x, main_x + main_w)
        vz = rng.randint(main_z + main_d + 1, main_z + main_d + 3)
        add_block(blocks, vx, 1, vz, "echoashfallprotocol:toxic_moss")

    return enforce_guardrails("bio_facility", blocks, 26, 22, 6, min_story_nodes=6, min_reward_nodes=3)


def generate_bunker_complex(seed: int) -> BlockList:
    """Big: 28x26 military bunker with armory, barracks, command, and vault."""
    rng = _r(seed)
    blocks: BlockList = []

    wall_block = "minecraft:stone_bricks"
    iron_block = "minecraft:iron_block"
    floor_block = "minecraft:smooth_stone"

    # Surface entrance structure
    entrance_x, entrance_z = 12, 2

    # Blast doors (iron blocks)
    fill(blocks, entrance_x, 1, entrance_z, entrance_x + 4, 3, entrance_z, iron_block)
    add_block(blocks, entrance_x + 2, 2, entrance_z, "minecraft:air")  # Open door

    # === Main Bunker Level (underground, represented at Y=-1 to -4) ===
    bunker_y = -2

    # Floor at bunker level
    fill(blocks, 2, bunker_y, 4, 26, bunker_y, 22, floor_block)

    # === Armory (east wing) ===
    armory_x, armory_z = 18, 8
    armory_w, armory_d = 7, 10

    for x in range(armory_x, armory_x + armory_w + 1):
        for z in [armory_z, armory_z + armory_d]:
            for y in range(bunker_y + 1, bunker_y + 4):
                add_block(blocks, x, y, z, iron_block)
    for z in range(armory_z, armory_z + armory_d + 1):
        for x in [armory_x, armory_x + armory_w]:
            for y in range(bunker_y + 1, bunker_y + 4):
                add_block(blocks, x, y, z, iron_block)

    # Weapon racks
    for wx in range(armory_x + 1, armory_x + armory_w):
        add_block(blocks, wx, bunker_y + 1, armory_z + 2, "echoashfallprotocol:weapon_rack")
        add_block(blocks, wx, bunker_y + 1, armory_z + armory_d - 2, "echoashfallprotocol:weapon_rack")

    # Storage crates
    add_supply_cluster(blocks, rng, armory_x + 2, armory_z + 4, 3, 3, containers=4)

    # === Barracks (west wing) ===
    barracks_x, barracks_z = 3, 8
    barracks_w, barracks_d = 7, 10

    for x in range(barracks_x, barracks_x + barracks_w + 1):
        for z in [barracks_z, barracks_z + barracks_d]:
            for y in range(bunker_y + 1, bunker_y + 4):
                add_block(blocks, x, y, z, wall_block)

    # Bunk beds
    for bx in range(barracks_x + 1, barracks_x + barracks_w, 2):
        for bz in [barracks_z + 2, barracks_z + 5]:
            add_block(blocks, bx, bunker_y + 1, bz, "minecraft:oak_planks")
            add_block(blocks, bx, bunker_y + 2, bz, "minecraft:white_wool")
            add_block(blocks, bx + 1, bunker_y + 1, bz, "minecraft:chest")

    # === Command Center (center) ===
    cmd_x, cmd_z = 10, 14
    cmd_w, cmd_d = 8, 6

    for x in range(cmd_x, cmd_x + cmd_w + 1):
        for z in [cmd_z, cmd_z + cmd_d]:
            for y in range(bunker_y + 1, bunker_y + 5):
                add_block(blocks, x, y, z, wall_block)

    # Map table and consoles
    add_block(blocks, cmd_x + cmd_w // 2, bunker_y + 1, cmd_z + cmd_d // 2, "echoashfallprotocol:map_table")
    add_block(blocks, cmd_x + 2, bunker_y + 1, cmd_z + 2, "minecraft:observer")
    add_block(blocks, cmd_x + cmd_w - 2, bunker_y + 1, cmd_z + 2, "minecraft:observer")

    # === Secure Vault (deepest area) ===
    vault_x, vault_z = 13, 6
    vault_cx, vault_cz = underground_vault(blocks, rng, vault_x, vault_z, 4, 4, 2, iron_block, floor_block)

    # Vault treasure
    add_block(blocks, vault_cx, -3, vault_cz, "minecraft:chest")
    add_block(blocks, vault_cx - 1, -3, vault_cz, "minecraft:barrel")
    add_block(blocks, vault_cx + 1, -3, vault_cz, "minecraft:barrel")

    # Ladder shaft from surface
    for y in range(1, bunker_y - 1, -1):
        add_block(blocks, entrance_x + 2, y, entrance_z + 2, "minecraft:ladder")

    # Additional anchor blocks to meet guardrails
    add_block(blocks, entrance_x + 1, 1, entrance_z + 3, "minecraft:campfire")
    add_block(blocks, vault_cx, -3, vault_cz + 2, "minecraft:cauldron")

    return enforce_guardrails("bunker_complex", blocks, 24, 20, 6, min_story_nodes=8, min_reward_nodes=4)


def generate_server_farm(seed: int) -> BlockList:
    """Big: 32x28 data center with server racks, cooling, and backup power."""
    rng = _r(seed)
    blocks: BlockList = []

    wall_block = "minecraft:stone_bricks"
    floor_block = "minecraft:smooth_stone"
    server_block = "minecraft:observer"

    # Main building
    main_x, main_z = 2, 2
    main_w, main_d = 28, 24

    fill(blocks, main_x, 0, main_z, main_x + main_w, 0, main_z + main_d, floor_block)

    # === Server Hall 1 (north) ===
    hall1_x, hall1_z = 4, 4
    hall1_w, hall1_d = 12, 8

    pillared_hall(blocks, hall1_x, hall1_z, hall1_w, hall1_d, 4, 3, wall_block, "minecraft:iron_bars", floor_block)

    # Server racks
    for x in range(hall1_x + 1, hall1_x + hall1_w, 2):
        for z in [hall1_z + 2, hall1_z + hall1_d - 2]:
            add_block(blocks, x, 1, z, server_block)
            add_block(blocks, x, 2, z, server_block)
            if rng.random() > 0.3:
                add_block(blocks, x, 3, z, "minecraft:glowstone")  # Status lights

    # === Server Hall 2 (south) ===
    hall2_x, hall2_z = 4, 16
    hall2_w, hall2_d = 12, 8

    pillared_hall(blocks, hall2_x, hall2_z, hall2_w, hall2_d, 4, 3, wall_block, "minecraft:iron_bars", floor_block)

    for x in range(hall2_x + 1, hall2_x + hall2_w, 2):
        for z in [hall2_z + 2, hall2_z + hall2_d - 2]:
            add_block(blocks, x, 1, z, server_block)
            add_block(blocks, x, 2, z, server_block)

    # === Network Operations Center (east) ===
    noc_x, noc_z = 18, 8
    noc_w, noc_d = 10, 12

    for x in range(noc_x, noc_x + noc_w + 1):
        for z in [noc_z, noc_z + noc_d]:
            for y in range(1, 4):
                add_block(blocks, x, y, z, wall_block)
    for z in range(noc_z, noc_z + noc_d + 1):
        for x in [noc_x, noc_x + noc_w]:
            for y in range(1, 4):
                add_block(blocks, x, y, z, wall_block)

    # Monitoring stations
    for mx in range(noc_x + 2, noc_x + noc_w - 1, 2):
        add_block(blocks, mx, 1, noc_z + 2, "minecraft:observer")
        add_block(blocks, mx, 1, noc_z + noc_d - 2, "minecraft:observer")

    # === Cooling System ===
    # External cooling units
    for cx in [main_x - 1, main_x + main_w + 1]:
        for cz in range(main_z + 5, main_z + main_d - 5, 6):
            add_block(blocks, cx, 1, cz, "minecraft:iron_block")
            add_block(blocks, cx, 2, cz, "minecraft:iron_bars")

    # === Backup Power (underground) ===
    power_x, power_z = main_x + 5, main_z + 12
    power_cx, power_cz = underground_vault(blocks, rng, power_x, power_z, 6, 6, 3, wall_block, floor_block)

    # Generators
    add_block(blocks, power_cx - 1, -3, power_cz - 1, "minecraft:iron_block")
    add_block(blocks, power_cx + 1, -3, power_cz - 1, "minecraft:iron_block")
    add_block(blocks, power_cx - 1, -3, power_cz + 1, "minecraft:iron_block")
    add_block(blocks, power_cx + 1, -3, power_cz + 1, "minecraft:iron_block")

    # Scattered clutter (cables, debris)
    for _ in range(15):
        cx = rng.randint(main_x + 2, main_x + main_w - 2)
        cz = rng.randint(main_z + 2, main_z + main_d - 2)
        add_block(blocks, cx, 0, cz, "echoashfallprotocol:item_pipe")

    # Reward containers (tech supplies)
    add_block(blocks, noc_x + 2, 1, noc_z + 4, "minecraft:chest")
    add_block(blocks, noc_x + 5, 1, noc_z + 4, "minecraft:barrel")
    add_block(blocks, noc_x + 3, 1, noc_z + 5, "minecraft:chest")

    return enforce_guardrails("server_farm", blocks, 28, 24, 6, min_story_nodes=6, min_reward_nodes=3)


def generate_power_plant_ruin(seed: int) -> BlockList:
    """Big: 40x36 power plant with reactor, control room, turbine hall, cooling towers."""
    rng = _r(seed)
    blocks: BlockList = []

    wall_block = "minecraft:stone_bricks"
    concrete_block = "minecraft:smooth_stone"
    iron_block = "minecraft:iron_block"

    # === Reactor Containment (center) ===
    reactor_x, reactor_z = 16, 14
    reactor_radius = 5

    # Circular containment
    for x in range(-reactor_radius, reactor_radius + 1):
        for z in range(-reactor_radius, reactor_radius + 1):
            dist = (x * x + z * z) ** 0.5
            px, pz = reactor_x + x, reactor_z + z

            if reactor_radius - 0.5 <= dist <= reactor_radius + 0.5:
                # Containment wall
                for y in range(1, 8):
                    if rng.random() > 0.2:  # Some damage
                        add_block(blocks, px, y, pz, concrete_block)
            elif dist < reactor_radius - 0.5:
                # Reactor floor
                add_block(blocks, px, 0, pz, concrete_block)

    # Glowing core (damaged)
    for dx in [-1, 0, 1]:
        for dz in [-1, 0, 1]:
            if abs(dx) + abs(dz) <= 1:
                add_block(blocks, reactor_x + dx, 1, reactor_z + dz, "minecraft:magma_block")
    add_block(blocks, reactor_x, 2, reactor_z, "minecraft:glowstone")

    # === Control Room (west) ===
    control_x, control_z = 4, 12
    control_w, control_d = 10, 8

    for x in range(control_x, control_x + control_w + 1):
        for z in [control_z, control_z + control_d]:
            for y in range(1, 4):
                add_block(blocks, x, y, z, wall_block)
    for z in range(control_z, control_z + control_d + 1):
        for x in [control_x, control_x + control_w]:
            for y in range(1, 4):
                add_block(blocks, x, y, z, wall_block)

    # Control consoles
    for cx in range(control_x + 2, control_x + control_w - 1, 2):
        add_block(blocks, cx, 1, control_z + 2, "minecraft:observer")
    add_block(blocks, control_x + control_w // 2, 1, control_z + control_d - 2, "echoashfallprotocol:map_table")

    # === Turbine Hall (east) ===
    turbine_x, turbine_z = 28, 8
    turbine_w, turbine_d = 10, 20

    # Large open hall with pillars
    pillared_hall(blocks, turbine_x, turbine_z, turbine_w, turbine_d, 6, 4, wall_block, "minecraft:iron_bars", concrete_block)

    # Turbine machinery
    for tx in range(turbine_x + 2, turbine_x + turbine_w - 1, 3):
        for tz in [turbine_z + 4, turbine_z + 12]:
            add_block(blocks, tx, 1, tz, iron_block)
            add_block(blocks, tx, 2, tz, iron_block)
            add_block(blocks, tx, 3, tz, "minecraft:iron_bars")  # Rotating element
            add_block(blocks, tx, 1, tz + 1, iron_block)
            add_block(blocks, tx, 1, tz - 1, iron_block)

    # === Cooling Towers (exterior, northeast and southeast) ===
    for tx, tz in [(35, 4), (35, 30)]:
        # Tower base
        for x in range(tx - 2, tx + 3):
            for z in range(tz - 2, tz + 3):
                add_block(blocks, x, 1, z, concrete_block)
        # Tower shaft
        for y in range(1, 8):
            for dx, dz in [(-1, -1), (-1, 1), (1, -1), (1, 1)]:
                add_block(blocks, tx + dx, y, tz + dz, concrete_block)

    # === Maintenance Tunnels (underground) ===
    tunnel_x, tunnel_z = reactor_x, reactor_z
    underground_vault(blocks, rng, tunnel_x - 3, tunnel_z, 6, 4, 3, wall_block, concrete_block)

    # Tunnel connecting to surface
    for z in range(tunnel_z, tunnel_z - 6, -1):
        add_block(blocks, tunnel_x - 3, 1, z, "minecraft:air")

    # Scattered rubble and debris
    for _ in range(20):
        rx = rng.randint(0, 38)
        rz = rng.randint(0, 34)
        add_block(blocks, rx, 0, rz, "echoashfallprotocol:concrete_rubble")
        if rng.random() > 0.5:
            add_block(blocks, rx, 1, rz, "echoashfallprotocol:concrete_rubble")

    # Reward containers in control room and vault
    add_block(blocks, control_x + 2, 1, control_z + 3, "minecraft:chest")
    add_block(blocks, control_x + 6, 1, control_z + 3, "minecraft:barrel")
    add_block(blocks, reactor_x - 2, 1, reactor_z + 2, "minecraft:chest")
    add_block(blocks, reactor_x + 2, 1, reactor_z - 2, "minecraft:chest")

    return enforce_guardrails("power_plant_ruin", blocks, 34, 30, 10, min_story_nodes=8, min_reward_nodes=4)
