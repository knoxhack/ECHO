"""
Shared POI composition helpers.

These utilities keep the lightweight procedural POIs readable and varied
without promoting them into full dungeon-scale structures.
"""

from __future__ import annotations

import random
from collections import Counter
from typing import Dict, Iterable, List, Optional, Tuple

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]

REWARD_BLOCKS = {
    "minecraft:barrel",
    "minecraft:chest",
    "minecraft:trapped_chest",
    "echoashfallprotocol:supply_crate",
    "echoashfallprotocol:trade_counter",
    "echoashfallprotocol:map_table",
    "echoashfallprotocol:weapon_rack",
    "echoashfallprotocol:bio_processing_station",
    "echoashfallprotocol:spore_garden",
    "echoashfallprotocol:field_med_bay",
    "echoashfallprotocol:research_lab",
}

ANCHOR_BLOCKS = {
    "minecraft:campfire",
    "minecraft:lightning_rod",
    "minecraft:chain",
    "minecraft:iron_bars",
    "minecraft:magma_block",
    "minecraft:observer",
    "minecraft:cauldron",
    "echoashfallprotocol:ash_campfire",
    "echoashfallprotocol:rain_collector",
    "echoashfallprotocol:relay_station",
    "echoashfallprotocol:radiation_block",
    "echoashfallprotocol:toxic_waste_barrel",
    "echoashfallprotocol:thermal_array",
    "echoashfallprotocol:bio_processing_station",
    "echoashfallprotocol:spore_garden",
}

CLUTTER_BLOCKS = {
    "minecraft:barrel",
    "minecraft:chest",
    "minecraft:campfire",
    "minecraft:hay_block",
    "minecraft:gravel",
    "minecraft:coarse_dirt",
    "minecraft:slime_block",
    "minecraft:oak_fence",
    "minecraft:oak_log",
    "minecraft:crate",  # ignored in vanilla, kept for future compatibility
    "minecraft:anvil",
    "minecraft:chain",
    "minecraft:moss_block",
    "minecraft:ice",
    "minecraft:snow",
    "minecraft:vine",
    "minecraft:grass",
    "echoashfallprotocol:debris_block",
    "echoashfallprotocol:rubble",
    "echoashfallprotocol:concrete_chunk",
    "echoashfallprotocol:rusted_metal_debris",
    "echoashfallprotocol:concrete_rubble",
    "echoashfallprotocol:rusted_metal_sheet",
    "echoashfallprotocol:ash_layer",
    "echoashfallprotocol:deep_ash",
    "echoashfallprotocol:scattered_bones",
    "echoashfallprotocol:toxic_waste_barrel",
    "echoashfallprotocol:supply_crate",
    "echoashfallprotocol:item_pipe",
    "echoashfallprotocol:power_cable",
    "echoashfallprotocol:dead_wood_log",
    "echoashfallprotocol:charred_wood_log",
    "echoashfallprotocol:toxic_moss",
    "echoashfallprotocol:mutated_bush",
    "echoashfallprotocol:nuclear_fungus",
    "echoashfallprotocol:mutated_sapling",
    "echoashfallprotocol:wild_berry_bush",
    "echoashfallprotocol:contaminated_soil",
    "echoashfallprotocol:fallout_dust",
    "echoashfallprotocol:toxic_puddle",
    "echoashfallprotocol:acidic_sludge",
    "echoashfallprotocol:oil_stained_concrete",
    "echoashfallprotocol:frozen_conduit",
    "echoashfallprotocol:blue_ice_crystal",
}

STORY_BLOCKS = {
    "minecraft:barrel",
    "minecraft:chest",
    "minecraft:campfire",
    "minecraft:cauldron",
    "minecraft:anvil",
    "minecraft:observer",
    "minecraft:lightning_rod",
    "minecraft:magma_block",
    "echoashfallprotocol:weapon_rack",
    "echoashfallprotocol:supply_crate",
    "echoashfallprotocol:trade_counter",
    "echoashfallprotocol:map_table",
    "echoashfallprotocol:bio_processing_station",
    "echoashfallprotocol:spore_garden",
    "echoashfallprotocol:relay_station",
    "echoashfallprotocol:rain_collector",
    "echoashfallprotocol:ash_campfire",
    "echoashfallprotocol:thermal_array",
    "echoashfallprotocol:research_lab",
    "echoashfallprotocol:contaminant_condenser",
    "echoashfallprotocol:scrap_press",
    "echoashfallprotocol:battery_bank",
    "echoashfallprotocol:factory_controller",
    "echoashfallprotocol:toxic_puddle",
    "echoashfallprotocol:acidic_sludge",
    "echoashfallprotocol:radiation_block",
    "echoashfallprotocol:toxic_waste_barrel",
}

# Size tier definitions for structure generation
SIZE_SMALL = {"min": 10, "max": 15, "height": 4, "target_blocks": 150}
SIZE_MEDIUM = {"min": 20, "max": 30, "height": 8, "target_blocks": 600}
SIZE_BIG = {"min": 35, "max": 50, "height": 12, "target_blocks": 2000}


def _sort_key(item: Tuple[Tuple[int, int, int], Tuple[str, Optional[Tuple[Tuple[str, str], ...]]]]):
    (x, y, z), _ = item
    return (y, x, z)


def dedupe_blocks(blocks: Iterable[Tuple[int, int, int, str, Optional[Dict[str, str]]]]) -> BlockList:
    deduped: Dict[Tuple[int, int, int], Tuple[str, Optional[Tuple[Tuple[str, str], ...]]]] = {}
    for x, y, z, block_id, props in blocks:
        normalized = tuple(sorted(props.items())) if props else None
        deduped[(x, y, z)] = (block_id, normalized)

    result: BlockList = []
    for (x, y, z), (block_id, normalized) in sorted(deduped.items(), key=_sort_key):
        props = dict(normalized) if normalized else None
        result.append((x, y, z, block_id, props))
    return result


def add_block(
    blocks: BlockList,
    x: int,
    y: int,
    z: int,
    block_id: str,
    props: Optional[Dict[str, str]] = None,
) -> None:
    blocks.append((x, y, z, block_id, props))


def fill(
    blocks: BlockList,
    x1: int,
    y1: int,
    z1: int,
    x2: int,
    y2: int,
    z2: int,
    block_id: str,
    props: Optional[Dict[str, str]] = None,
) -> None:
    for x in range(min(x1, x2), max(x1, x2) + 1):
        for y in range(min(y1, y2), max(y1, y2) + 1):
            for z in range(min(z1, z2), max(z1, z2) + 1):
                blocks.append((x, y, z, block_id, props))


def hollow_rect(
    blocks: BlockList,
    x1: int,
    z1: int,
    x2: int,
    z2: int,
    y1: int,
    y2: int,
    block_id: str,
    props: Optional[Dict[str, str]] = None,
) -> None:
    for x in range(min(x1, x2), max(x1, x2) + 1):
        for z in range(min(z1, z2), max(z1, z2) + 1):
            for y in range(min(y1, y2), max(y1, y2) + 1):
                if x in (x1, x2) or z in (z1, z2):
                    blocks.append((x, y, z, block_id, props))


def line(
    blocks: BlockList,
    start: Tuple[int, int, int],
    end: Tuple[int, int, int],
    block_id: str,
    props: Optional[Dict[str, str]] = None,
) -> None:
    x1, y1, z1 = start
    x2, y2, z2 = end
    steps = max(abs(x2 - x1), abs(y2 - y1), abs(z2 - z1))
    if steps == 0:
        blocks.append((x1, y1, z1, block_id, props))
        return
    for step in range(steps + 1):
        t = step / steps
        x = round(x1 + (x2 - x1) * t)
        y = round(y1 + (y2 - y1) * t)
        z = round(z1 + (z2 - z1) * t)
        blocks.append((x, y, z, block_id, props))


def scatter(
    blocks: BlockList,
    rng: random.Random,
    x1: int,
    z1: int,
    x2: int,
    z2: int,
    count: int,
    palette: List[str],
    y: int = 0,
) -> None:
    for _ in range(count):
        add_block(blocks, rng.randint(x1, x2), y, rng.randint(z1, z2), rng.choice(palette))


def blob_patch(
    blocks: BlockList,
    center_x: int,
    center_z: int,
    radius_x: int,
    radius_z: int,
    block_id: str,
    y: int = 0,
    jitter: int = 1,
) -> None:
    for x in range(center_x - radius_x, center_x + radius_x + 1):
        for z in range(center_z - radius_z, center_z + radius_z + 1):
            nx = abs(x - center_x) / max(radius_x, 1)
            nz = abs(z - center_z) / max(radius_z, 1)
            if (nx * nx + nz * nz) <= 1.15:
                blocks.append((x, y, z, block_id, None))
                if jitter and (abs(x - center_x) == radius_x or abs(z - center_z) == radius_z):
                    if (x + z) % (jitter + 1) == 0:
                        blocks.append((x, y, z, block_id, None))


def raised_patch(
    blocks: BlockList,
    center_x: int,
    center_z: int,
    radius: int,
    base_block: str,
    cap_block: Optional[str] = None,
) -> None:
    for x in range(center_x - radius, center_x + radius + 1):
        for z in range(center_z - radius, center_z + radius + 1):
            dist = abs(x - center_x) + abs(z - center_z)
            if dist <= radius + 1:
                height = max(0, radius - dist // 2)
                for y in range(height):
                    blocks.append((x, y, z, base_block, None))
                if cap_block:
                    blocks.append((x, height, z, cap_block, None))


def ruined_wall(
    blocks: BlockList,
    rng: random.Random,
    x1: int,
    z1: int,
    x2: int,
    z2: int,
    block_id: str,
    min_height: int = 2,
    max_height: int = 4,
    gap_chance: float = 0.25,
) -> None:
    for x in range(min(x1, x2), max(x1, x2) + 1):
        for z in range(min(z1, z2), max(z1, z2) + 1):
            if x not in (x1, x2) and z not in (z1, z2):
                continue
            if rng.random() < gap_chance:
                continue
            height = rng.randint(min_height, max_height)
            for y in range(1, height + 1):
                blocks.append((x, y, z, block_id, None))


def tent_frame(
    blocks: BlockList,
    x: int,
    z: int,
    width: int,
    depth: int,
    height: int,
    shell_block: str,
    support_block: str = "minecraft:oak_fence",
) -> None:
    x2 = x + width - 1
    z2 = z + depth - 1
    for px in (x, x2):
        for pz in (z, z2):
            for y in range(1, max(2, height)):
                blocks.append((px, y, pz, support_block, None))

    ridge_x = x + width // 2
    for dz in range(depth):
        blocks.append((ridge_x, height, z + dz, shell_block, None))
    for dx in range(width):
        roof_y = max(2, height - abs(dx - width // 2))
        blocks.append((x + dx, roof_y, z, shell_block, None))
        blocks.append((x + dx, roof_y, z2, shell_block, None))
    for dz in range(1, depth - 1):
        blocks.append((x, 2, z + dz, shell_block, None))
        blocks.append((x2, 2, z + dz, shell_block, None))


def lean_to(
    blocks: BlockList,
    x: int,
    z: int,
    width: int,
    depth: int,
    roof_block: str,
    support_block: str = "minecraft:oak_log",
) -> None:
    x2 = x + width - 1
    z2 = z + depth - 1
    for px in (x, x2):
        blocks.append((px, 1, z, support_block, None))
        blocks.append((px, 2, z2, support_block, None))
    for dx in range(width):
        for dz in range(depth):
            roof_y = 2 if dz == 0 else 3
            blocks.append((x + dx, roof_y, z + dz, roof_block, None))


def scrap_barricade(
    blocks: BlockList,
    rng: random.Random,
    x1: int,
    z1: int,
    x2: int,
    z2: int,
    palette: List[str],
) -> None:
    line(blocks, (x1, 1, z1), (x2, 1, z2), rng.choice(palette))
    for _ in range(max(2, abs(x2 - x1) + abs(z2 - z1) // 3)):
        px = rng.randint(min(x1, x2), max(x1, x2))
        pz = rng.randint(min(z1, z2), max(z1, z2))
        blocks.append((px, 2, pz, rng.choice(palette), None))


def pipe_run(
    blocks: BlockList,
    start: Tuple[int, int, int],
    end: Tuple[int, int, int],
    pipe_block: str = "minecraft:iron_bars",
    support_block: str = "minecraft:smooth_stone",
    support_spacing: int = 3,
) -> None:
    line(blocks, start, end, pipe_block)
    x1, y1, z1 = start
    x2, y2, z2 = end
    steps = max(abs(x2 - x1), abs(z2 - z1))
    if steps == 0:
        return
    for step in range(0, steps + 1, max(1, support_spacing)):
        t = step / steps
        x = round(x1 + (x2 - x1) * t)
        y = round(y1 + (y2 - y1) * t)
        z = round(z1 + (z2 - z1) * t)
        for sy in range(0, max(1, y)):
            blocks.append((x, sy, z, support_block, None))


def add_supply_cluster(
    blocks: BlockList,
    rng: random.Random,
    x: int,
    z: int,
    width: int,
    depth: int,
    containers: int = 2,
    clutter_palette: Optional[List[str]] = None,
) -> None:
    clutter_palette = clutter_palette or ["minecraft:barrel", "minecraft:oak_log", "minecraft:gravel"]
    slots = [(px, pz) for px in range(x, x + width) for pz in range(z, z + depth)]
    rng.shuffle(slots)
    for idx, (px, pz) in enumerate(slots[: max(containers + 2, 4)]):
        if idx < containers:
            blocks.append((px, 0, pz, rng.choice(clutter_palette), None))
            blocks.append((px, 1, pz, "minecraft:barrel" if idx % 2 == 0 else "minecraft:chest", None))
        else:
            blocks.append((px, 0, pz, rng.choice(clutter_palette), None))


def add_signal_marker(blocks: BlockList, x: int, z: int, height: int = 5) -> None:
    for y in range(1, height):
        blocks.append((x, y, z, "minecraft:oak_fence", None))
    blocks.append((x, height, z, "minecraft:lightning_rod", None))
    blocks.append((x + 1, max(2, height - 1), z, "minecraft:white_wool", None))


def add_debris_scatter(
    blocks: BlockList,
    rng: random.Random,
    x1: int,
    z1: int,
    x2: int,
    z2: int,
    count: int,
    palette: List[str],
    y: int = 0,
) -> None:
    """Scatter low debris/hazard props without replacing the readable structure."""
    if y > 0:
        for _ in range(max(1, count // 2)):
            sx = rng.randint(x1, x2)
            sz = rng.randint(z1, z2)
            blocks.append((sx, y - 1, sz, rng.choice(palette), None))
    scatter(blocks, rng, x1, z1, x2, z2, count, palette, y=y)


def add_hazard_leak(
    blocks: BlockList,
    rng: random.Random,
    x: int,
    z: int,
    radius: int,
    liquid_block: str,
    stained_block: str,
    y: int = 0,
) -> None:
    for dx in range(-radius, radius + 1):
        for dz in range(-radius, radius + 1):
            if abs(dx) + abs(dz) <= radius + rng.randint(0, 1):
                block = liquid_block if rng.random() < 0.55 else stained_block
                blocks.append((x + dx, y, z + dz, block, None))


def add_machine_cluster(
    blocks: BlockList,
    rng: random.Random,
    x: int,
    z: int,
    machines: List[str],
    cable_block: str = "echoashfallprotocol:power_cable",
) -> None:
    chosen = machines[:]
    rng.shuffle(chosen)
    for idx, block_id in enumerate(chosen[:3]):
        px = x + idx
        blocks.append((px, 0, z, "echoashfallprotocol:oil_stained_concrete", None))
        blocks.append((px, 1, z, block_id, None))
        if idx > 0:
            blocks.append((px - 1, 0, z, cable_block, None))


def add_faction_station(blocks: BlockList, x: int, z: int, station_block: str, support_block: str) -> None:
    blocks.append((x, 0, z, support_block, None))
    blocks.append((x, 1, z, station_block, None))
    blocks.append((x + 1, 0, z, support_block, None))
    blocks.append((x + 1, 1, z, "minecraft:barrel", None))


def add_biome_vegetation(
    blocks: BlockList,
    rng: random.Random,
    x1: int,
    z1: int,
    x2: int,
    z2: int,
    palette: List[str],
    count: int,
) -> None:
    for _ in range(count):
        x = rng.randint(x1, x2)
        z = rng.randint(z1, z2)
        blocks.append((x, 0, z, "minecraft:coarse_dirt", None))
        blocks.append((x, 1, z, rng.choice(palette), None))


def footprint(blocks: BlockList) -> Tuple[int, int, int]:
    xs = [x for x, _, _, _, _ in blocks]
    ys = [y for _, y, _, _, _ in blocks]
    zs = [z for _, _, z, _, _ in blocks]
    return max(xs) - min(xs) + 1, max(ys) - min(ys) + 1, max(zs) - min(zs) + 1


def enforce_guardrails(
    name: str,
    blocks: BlockList,
    min_width: int,
    min_depth: int,
    min_height: int,
    min_anchor_blocks: int = 3,
    min_clutter_blocks: int = 10,
    min_story_nodes: int = 2,
    min_reward_nodes: int = 1,
) -> BlockList:
    cleaned = dedupe_blocks(blocks)
    width, height, depth = footprint(cleaned)
    if width < min_width or depth < min_depth:
        raise ValueError(f"{name} footprint too small: got {width}x{depth}, expected at least {min_width}x{min_depth}")
    if height < min_height:
        raise ValueError(f"{name} height too small: got {height}, expected at least {min_height}")

    counts = Counter(block_id for _, _, _, block_id, _ in cleaned)
    anchor_count = sum(counts[block_id] for block_id in ANCHOR_BLOCKS)
    clutter_count = sum(counts[block_id] for block_id in CLUTTER_BLOCKS)
    story_count = sum(counts[block_id] for block_id in STORY_BLOCKS)
    reward_count = sum(counts[block_id] for block_id in REWARD_BLOCKS)

    if anchor_count < min_anchor_blocks:
        raise ValueError(f"{name} anchor block count too low: {anchor_count} < {min_anchor_blocks}")
    if clutter_count < min_clutter_blocks:
        raise ValueError(f"{name} clutter block count too low: {clutter_count} < {min_clutter_blocks}")
    if story_count < min_story_nodes:
        raise ValueError(f"{name} story node count too low: {story_count} < {min_story_nodes}")
    if reward_count < min_reward_nodes:
        raise ValueError(f"{name} reward node count too low: {reward_count} < {min_reward_nodes}")

    return cleaned


# ============================================================================
# Enhanced Architectural Primitives for Multi-Scale Structures
# ============================================================================

def multi_room_layout(
    blocks: BlockList,
    rng: random.Random,
    start_x: int,
    start_z: int,
    width: int,
    depth: int,
    wall_block: str,
    floor_block: str,
    room_count: int = 3,
) -> List[Tuple[int, int, int, int]]:
    """Generate a connected room layout, returns list of room bounding boxes (x1, z1, x2, z2)."""
    rooms: List[Tuple[int, int, int, int]] = []
    end_x = start_x + width
    end_z = start_z + depth

    # Build outer walls
    for x in range(start_x, end_x + 1):
        add_block(blocks, x, 1, start_z, wall_block)
        add_block(blocks, x, 1, end_z, wall_block)
    for z in range(start_z, end_z + 1):
        add_block(blocks, start_x, 1, z, wall_block)
        add_block(blocks, end_x, 1, z, wall_block)

    # Floor
    fill(blocks, start_x, 0, start_z, end_x, 0, end_z, floor_block)

    # Divide into rooms
    if room_count <= 1:
        rooms.append((start_x, start_z, end_x, end_z))
        return rooms

    # Create room divisions
    if width >= depth:
        # Split along Z axis (vertical divisions)
        split_step = width // room_count
        for i in range(room_count):
            rx1 = start_x + i * split_step
            rx2 = start_x + (i + 1) * split_step if i < room_count - 1 else end_x
            rz1, rz2 = start_z, end_z
            rooms.append((rx1, rz1, rx2, rz2))
            # Interior wall with door gap
            if i < room_count - 1:
                door_z = start_z + (depth // 2)
                for z in range(start_z, end_z + 1):
                    if z != door_z:
                        add_block(blocks, rx2, 1, z, wall_block)
                        add_block(blocks, rx2, 2, z, wall_block)
    else:
        # Split along X axis (horizontal divisions)
        split_step = depth // room_count
        for i in range(room_count):
            rz1 = start_z + i * split_step
            rz2 = start_z + (i + 1) * split_step if i < room_count - 1 else end_z
            rx1, rx2 = start_x, end_x
            rooms.append((rx1, rz1, rx2, rz2))
            # Interior wall with door gap
            if i < room_count - 1:
                door_x = start_x + (width // 2)
                for x in range(start_x, end_x + 1):
                    if x != door_x:
                        add_block(blocks, x, 1, rz2, wall_block)
                        add_block(blocks, x, 2, rz2, wall_block)

    return rooms


def stairwell(
    blocks: BlockList,
    x: int,
    z: int,
    height: int,
    stair_block: str = "minecraft:stone_bricks",
    direction: str = "up",
) -> None:
    """Create a stairwell going up or down."""
    if direction == "up":
        for y in range(height):
            # Spiral staircase pattern
            offset = y % 4
            if offset == 0:
                add_block(blocks, x, y, z, stair_block)
                add_block(blocks, x, y, z + 1, "minecraft:air")  # Clear space
            elif offset == 1:
                add_block(blocks, x + 1, y, z, stair_block)
            elif offset == 2:
                add_block(blocks, x + 1, y, z + 1, stair_block)
            else:
                add_block(blocks, x, y, z + 1, stair_block)
            # Support column in center
            add_block(blocks, x, y, z, stair_block)
    else:
        # Going down (negative Y in structure space)
        for y in range(height):
            add_block(blocks, x, -y, z, stair_block)
            # Landing every 4 steps
            if y % 4 == 3:
                fill(blocks, x - 1, -y, z - 1, x + 1, -y, z + 1, stair_block)


def watchtower(
    blocks: BlockList,
    rng: random.Random,
    cx: int,
    cz: int,
    base_height: int,
    tower_height: int,
    wall_block: str,
    floor_block: str,
) -> None:
    """Create a watchtower with ladder access."""
    # Base platform
    fill(blocks, cx - 1, base_height, cz - 1, cx + 1, base_height, cz + 1, floor_block)

    # Tower walls
    for y in range(base_height, base_height + tower_height):
        for dx in (-2, 2):
            for dz in (-2, 2):
                if rng.random() > 0.15:  # Some gaps for ruined look
                    add_block(blocks, cx + dx, y, cz + dz, wall_block)
        # Corner pillars
        add_block(blocks, cx - 2, y, cz - 2, wall_block)
        add_block(blocks, cx + 2, y, cz - 2, wall_block)
        add_block(blocks, cx - 2, y, cz + 2, wall_block)
        add_block(blocks, cx + 2, y, cz + 2, wall_block)

    # Ladder shaft
    for y in range(base_height, base_height + tower_height):
        add_block(blocks, cx, y, cz, "minecraft:ladder", {"facing": "north"})

    # Observation deck
    deck_y = base_height + tower_height
    fill(blocks, cx - 2, deck_y, cz - 2, cx + 2, deck_y, cz + 2, floor_block)
    # Crenellations
    for dx in (-2, 2):
        for dz in (-2, 2):
            add_block(blocks, cx + dx, deck_y + 1, cz + dz, wall_block)


def elevated_platform(
    blocks: BlockList,
    x: int,
    z: int,
    width: int,
    depth: int,
    height: int,
    platform_block: str,
    support_block: str,
) -> None:
    """Create an elevated platform with support pillars."""
    # Platform surface
    fill(blocks, x, height, z, x + width, height, z + depth, platform_block)

    # Support pillars at corners and a sparse grid under larger spans.
    support_points = {
        (x, z), (x + width, z),
        (x, z + depth), (x + width, z + depth)
    }
    if width >= 6 or depth >= 6:
        for px in range(x + 3, x + width, 4):
            support_points.add((px, z))
            support_points.add((px, z + depth))
        for pz in range(z + 3, z + depth, 4):
            support_points.add((x, pz))
            support_points.add((x + width, pz))
        support_points.add((x + width // 2, z + depth // 2))

    for px, pz in sorted(support_points):
        for y in range(height):
            add_block(blocks, px, y, pz, support_block)


def collapsed_section(
    blocks: BlockList,
    rng: random.Random,
    x1: int,
    z1: int,
    x2: int,
    z2: int,
    y: int,
    wall_block: str,
    rubble_palette: List[str],
    collapse_chance: float = 0.6,
) -> None:
    """Create a partially collapsed wall/section with rubble."""
    for x in range(min(x1, x2), max(x1, x2) + 1):
        for z in range(min(z1, z2), max(z1, z2) + 1):
            if rng.random() < collapse_chance:
                # Collapsed - place rubble at base
                add_block(blocks, x, 0, z, rng.choice(rubble_palette))
                if rng.random() > 0.5:
                    add_block(blocks, x, 1, z, rng.choice(rubble_palette))
            else:
                # Standing wall section
                add_block(blocks, x, y, z, wall_block)
                if rng.random() > 0.3:
                    add_block(blocks, x, y + 1, z, wall_block)


def underground_vault(
    blocks: BlockList,
    rng: random.Random,
    entrance_x: int,
    entrance_z: int,
    vault_width: int,
    vault_depth: int,
    vault_height: int,
    wall_block: str,
    floor_block: str,
) -> Tuple[int, int]:
    """Create an underground vault/basement, returns vault center coordinates."""
    # Entrance ladder/stairs
    for y in range(1, vault_height + 1):
        add_block(blocks, entrance_x, -y, entrance_z, "minecraft:ladder", {"facing": "north"})
        # Clear shaft
        add_block(blocks, entrance_x + 1, -y, entrance_z, "minecraft:air")
        add_block(blocks, entrance_x - 1, -y, entrance_z, "minecraft:air")

    # Vault room (negative Y coordinates)
    vault_y = -vault_height
    cx = entrance_x
    cz = entrance_z

    # Floor
    fill(blocks, cx - vault_width // 2, vault_y, cz - vault_depth // 2,
         cx + vault_width // 2, vault_y, cz + vault_depth // 2, floor_block)

    # Walls
    for x in range(cx - vault_width // 2, cx + vault_width // 2 + 1):
        for z in [cz - vault_depth // 2, cz + vault_depth // 2]:
            for y in range(vault_y, 0):
                add_block(blocks, x, y, z, wall_block)
    for z in range(cz - vault_depth // 2, cz + vault_depth // 2 + 1):
        for x in [cx - vault_width // 2, cx + vault_width // 2]:
            for y in range(vault_y, 0):
                add_block(blocks, x, y, z, wall_block)

    return cx, cz


def bridge_section(
    blocks: BlockList,
    x1: int,
    z1: int,
    x2: int,
    z2: int,
    y: int,
    deck_block: str,
    railing_block: str = "minecraft:oak_fence",
) -> None:
    """Create a bridge or walkway between two points."""
    steps = max(abs(x2 - x1), abs(z2 - z1))
    if steps == 0:
        return

    for step in range(steps + 1):
        t = step / steps
        x = round(x1 + (x2 - x1) * t)
        z = round(z1 + (z2 - z1) * t)
        # Deck
        add_block(blocks, x, y, z, deck_block)
        # Railings (check if we're at an edge of the bridge)
        if step > 0:
            prev_t = (step - 1) / steps
            prev_x = round(x1 + (x2 - x1) * prev_t)
            prev_z = round(z1 + (z2 - z1) * prev_t)
            # Add railings perpendicular to bridge direction
            if abs(x - prev_x) > abs(z - prev_z):
                # Moving in X, railings in Z
                add_block(blocks, x, y + 1, z + 1, railing_block)
                add_block(blocks, x, y + 1, z - 1, railing_block)
            else:
                # Moving in Z, railings in X
                add_block(blocks, x + 1, y + 1, z, railing_block)
                add_block(blocks, x - 1, y + 1, z, railing_block)


def pillared_hall(
    blocks: BlockList,
    x: int,
    z: int,
    width: int,
    depth: int,
    height: int,
    pillar_spacing: int,
    wall_block: str,
    pillar_block: str,
    floor_block: str,
) -> None:
    """Create a large hall with regularly spaced pillars."""
    # Floor
    fill(blocks, x, 0, z, x + width, 0, z + depth, floor_block)

    # Pillars
    for px in range(x + pillar_spacing, x + width, pillar_spacing):
        for pz in range(z + pillar_spacing, z + depth, pillar_spacing):
            for y in range(1, height):
                add_block(blocks, px, y, pz, pillar_block)

    # Roof beams (optional - reduced chance for ruined look)
    for px in range(x, x + width + 1, 2):
        add_block(blocks, px, height, z + depth // 2, wall_block)


def courtyard_layout(
    blocks: BlockList,
    rng: random.Random,
    x: int,
    z: int,
    width: int,
    depth: int,
    building_count: int,
    wall_block: str,
    path_block: str = "minecraft:gravel",
) -> List[Tuple[int, int, int, int]]:
    """Create a courtyard with surrounding buildings, returns building positions."""
    buildings: List[Tuple[int, int, int, int]] = []
    cx, cz = x + width // 2, z + depth // 2

    # Central courtyard floor
    inner_margin = 4
    fill(blocks, x + inner_margin, 0, z + inner_margin,
         x + width - inner_margin, 0, z + depth - inner_margin, path_block)

    # Place buildings around perimeter
    positions = [
        (x + 2, z + 2, inner_margin - 2, inner_margin - 2),  # NW
        (x + width - inner_margin + 2, z + 2, inner_margin - 2, inner_margin - 2),  # NE
        (x + 2, z + depth - inner_margin + 2, inner_margin - 2, inner_margin - 2),  # SW
        (x + width - inner_margin + 2, z + depth - inner_margin + 2, inner_margin - 2, inner_margin - 2),  # SE
    ]

    for i in range(min(building_count, len(positions))):
        bx, bz, bw, bd = positions[i]
        # Simple building shell
        for bx_pos in range(bx, min(bx + bw, x + width)):
            for bz_pos in range(bz, min(bz + bd, z + depth)):
                add_block(blocks, bx_pos, 1, bz_pos, wall_block)
                if rng.random() > 0.3:
                    add_block(blocks, bx_pos, 2, bz_pos, wall_block)
        buildings.append((bx, bz, min(bx + bw, x + width), min(bz + bd, z + depth)))

    return buildings


def window_opening(
    blocks: BlockList,
    x: int,
    y: int,
    z: int,
    width: int,
    height: int,
    facing: str,
) -> None:
    """Cut a window opening in a wall, replacing blocks with air."""
    if facing in ("north", "south"):
        for wy in range(y, y + height):
            for wx in range(x, x + width):
                add_block(blocks, wx, wy, z, "minecraft:air")
    else:  # east, west
        for wy in range(y, y + height):
            for wz in range(z, z + width):
                add_block(blocks, x, wy, wz, "minecraft:air")


def doorway(
    blocks: BlockList,
    x: int,
    y: int,
    z: int,
    facing: str,
    width: int = 2,
    height: int = 3,
) -> None:
    """Cut a doorway through a wall."""
    if facing in ("north", "south"):
        for dy in range(y, y + height):
            for dx in range(x, x + width):
                add_block(blocks, dx, dy, z, "minecraft:air")
        # Door frame
        for dx in range(x, x + width):
            add_block(blocks, dx, y + height, z, "minecraft:oak_log")
    else:
        for dy in range(y, y + height):
            for dz in range(z, z + width):
                add_block(blocks, x, dy, dz, "minecraft:air")
        for dz in range(z, z + width):
            add_block(blocks, x, y + height, dz, "minecraft:oak_log")
