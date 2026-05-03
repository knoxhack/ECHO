import gzip
import hashlib
import struct
from pathlib import Path


SIZE = (24, 9, 24)
DATA_VERSION = 4189
ROOT = Path(__file__).resolve().parents[1]
OUTPUTS = [
    ROOT / "src/main/resources/data/echoashfallprotocol/structure/drop_pod.nbt",
    ROOT / "src/main/resources/data/echoashfallprotocol/structure/global/drop_pod.nbt",
    ROOT / "src/main/resources/data/echoashfallprotocol/structures/drop_pod.nbt",
    ROOT / "src/main/resources/data/echoashfallprotocol/structures/global/drop_pod.nbt",
]


def h(x, z, salt=0):
    digest = hashlib.sha1(f"{x},{z},{salt}".encode("ascii")).digest()
    return digest[0] / 255.0


def block(name, **properties):
    return (name, tuple(sorted((str(k), str(v)) for k, v in properties.items())))


AIR = block("minecraft:air")
HULL = block("echoashfallprotocol:drop_pod_hull")
GLASS = block("echoashfallprotocol:drop_pod_glass")
RUST = block("echoashfallprotocol:rusted_metal_sheet")
RUST_DEBRIS = block("echoashfallprotocol:rusted_metal_debris")
CONCRETE_RUBBLE = block("echoashfallprotocol:concrete_rubble")
TRACE_RUBBLE = block("echoashfallprotocol:wasteland_trace_rubble")
CRASH_SLAG = block("echoashfallprotocol:crash_slag")
BURNT_SOIL = block("echoashfallprotocol:burnt_wasteland_soil")
CONTAMINATED_SOIL = block("echoashfallprotocol:contaminated_soil")
ASH_DIRT = block("echoashfallprotocol:ashen_wasteland_dirt")
OIL_CONCRETE = block("echoashfallprotocol:oil_stained_concrete")
ASH_LAYER = block("echoashfallprotocol:ash_layer", layers="1")
TERMINAL = block("echoashfallprotocol:echo_terminal")
HAND_RECYCLER = block("echoashfallprotocol:hand_recycler")
SCANNER = block("echoashfallprotocol:signal_scanner")
MED_BAY = block("echoashfallprotocol:field_med_bay")


blocks = {}


def setb(x, y, z, state):
    if 0 <= x < SIZE[0] and 0 <= y < SIZE[1] and 0 <= z < SIZE[2]:
        if state == AIR:
            blocks.pop((x, y, z), None)
        else:
            blocks[(x, y, z)] = state


def fill(x1, y1, z1, x2, y2, z2, state):
    for x in range(min(x1, x2), max(x1, x2) + 1):
        for y in range(min(y1, y2), max(y1, y2) + 1):
            for z in range(min(z1, z2), max(z1, z2) + 1):
                setb(x, y, z, state)


def oval_value(x, z, cx=11.5, cz=12.5, rx=10.4, rz=8.4):
    return ((x - cx) / rx) ** 2 + ((z - cz) / rz) ** 2


def build_crash_scar():
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            v = oval_value(x, z)
            jitter = (h(x, z, 1) - 0.5) * 0.22
            if v + jitter <= 1.0:
                r = h(x, z, 2)
                if v < 0.34:
                    state = CRASH_SLAG if r < 0.58 else OIL_CONCRETE
                elif v < 0.62:
                    state = BURNT_SOIL if r < 0.35 else (CRASH_SLAG if r < 0.68 else CONTAMINATED_SOIL)
                else:
                    state = ASH_DIRT if r < 0.38 else (CONTAMINATED_SOIL if r < 0.68 else TRACE_RUBBLE)
                setb(x, 2, z, state)
                if v > 0.72 and h(x, z, 3) < 0.2:
                    setb(x, 3, z, ASH_LAYER)

    # Rear impact trail and scattered wreckage.
    for i in range(8):
        cx = 11 + (i // 3) - 1
        z = 6 - i
        width = max(1, 4 - i // 2)
        for x in range(cx - width, cx + width + 1):
            if h(x, z, 4) < 0.82:
                setb(x, 2, z, CRASH_SLAG if h(x, z, 5) < 0.45 else BURNT_SOIL)
            if h(x, z, 6) < 0.18:
                setb(x, 3, z, RUST_DEBRIS)

    for x, z in [(4, 11), (5, 15), (7, 5), (18, 8), (20, 14), (16, 20), (10, 3), (14, 4)]:
        setb(x, 3, z, RUST_DEBRIS if h(x, z, 7) < 0.55 else CONCRETE_RUBBLE)

    for x, z in [(7, 7), (8, 19), (16, 6), (18, 18), (5, 13)]:
        setb(x, 3, z, block("minecraft:campfire", lit="true", signal_fire="false", waterlogged="false", facing="north"))

    for x, z in [(8, 6), (15, 6), (6, 17), (18, 16)]:
        setb(x, 3, z, block("minecraft:magma_block"))


def in_capsule(x, z, inset=0):
    return 6 + inset <= x <= 17 - inset and 7 + inset <= z <= 17 - inset and not (
        (x <= 6 + inset or x >= 17 - inset) and (z <= 7 + inset or z >= 17 - inset)
    )


def build_shell():
    # Floor and maintenance strips.
    for x in range(6, 18):
        for z in range(7, 18):
            if in_capsule(x, z):
                if x in (9, 14) or z in (10, 15):
                    setb(x, 2, z, block("minecraft:polished_deepslate"))
                elif (x + z) % 5 == 0:
                    setb(x, 2, z, RUST)
                else:
                    setb(x, 2, z, OIL_CONCRETE)

    # Wall ring with rounded corners.
    for y in range(3, 6):
        for x in range(6, 18):
            for z in range(7, 18):
                perimeter = in_capsule(x, z) and not in_capsule(x, z, 1)
                if perimeter:
                    state = HULL
                    if y == 4 and (x in (6, 17)) and z in (10, 11, 14, 15):
                        state = GLASS
                    if y == 4 and (z in (7, 17)) and x in (9, 10, 13, 14):
                        state = GLASS
                    if (x, y, z) in [(6, 3, 9), (17, 5, 16), (8, 5, 7), (15, 3, 17)]:
                        state = RUST
                    setb(x, y, z, state)

    # Open front hatch.
    for x in (11, 12):
        for y in (3, 4):
            setb(x, y, 17, AIR)

    # Ceiling and inset roof hatch.
    for x in range(6, 18):
        for z in range(7, 18):
            if in_capsule(x, z):
                edge = not in_capsule(x, z, 1)
                setb(x, 6, z, HULL if edge else block("minecraft:light_gray_concrete"))
    for x, z in [(9, 10), (14, 10), (9, 15), (14, 15), (11, 12), (12, 12)]:
        setb(x, 6, z, GLASS)
    fill(9, 7, 10, 14, 7, 15, HULL)
    fill(10, 8, 11, 13, 8, 14, HULL)
    setb(11, 8, 12, GLASS)
    setb(12, 8, 12, GLASS)

    # Exterior stepped armor plates and damaged fins.
    fill(5, 3, 10, 5, 4, 15, HULL)
    fill(18, 3, 10, 18, 4, 15, HULL)
    fill(8, 3, 6, 15, 4, 6, HULL)
    setb(5, 5, 12, RUST)
    setb(18, 5, 13, RUST)
    fill(7, 3, 5, 8, 4, 5, RUST)
    fill(15, 3, 5, 16, 4, 5, RUST)
    for x in (8, 15):
        setb(x, 3, 4, block("minecraft:blast_furnace"))
        setb(x, 4, 4, block("minecraft:iron_bars", north="false", south="false", east="true", west="true", waterlogged="false"))
        setb(x, 3, 3, block("minecraft:magma_block"))

    # Ramp out of the hatch.
    fill(10, 2, 18, 13, 2, 20, HULL)
    fill(10, 3, 18, 13, 3, 18, RUST)
    setb(11, 3, 19, block("minecraft:heavy_weighted_pressure_plate", power="0"))
    setb(12, 3, 19, block("minecraft:heavy_weighted_pressure_plate", power="0"))


def build_interior():
    # Clear walkable air after shell placement.
    for x in range(7, 17):
        for y in range(3, 6):
            for z in range(8, 17):
                if in_capsule(x, z, 1):
                    setb(x, y, z, AIR)

    # Restore walls near hatch after clearing.
    for x in (7, 16):
        for z in range(9, 16):
            if z in (9, 12, 15):
                setb(x, 4, z, GLASS)

    # Onboard computer bay / terminal.
    setb(11, 3, 8, TERMINAL)
    setb(10, 3, 8, SCANNER)
    setb(12, 3, 8, block("minecraft:lectern", facing="south", has_book="false", powered="false"))
    setb(11, 4, 8, GLASS)

    # Field fabrication corner.
    setb(16, 3, 13, HAND_RECYCLER)
    setb(16, 3, 14, block("minecraft:crafting_table"))
    setb(15, 3, 15, block("minecraft:barrel", facing="up", open="false"))
    setb(16, 3, 15, block("minecraft:chest", facing="west", type="single", waterlogged="false"))

    # Med/sleep alcove.
    setb(7, 3, 13, MED_BAY)
    setb(7, 3, 14, block("minecraft:white_bed", facing="south", occupied="false", part="foot"))
    setb(7, 3, 15, block("minecraft:white_bed", facing="south", occupied="false", part="head"))

    # Supply locker and cable damage.
    setb(8, 3, 16, block("minecraft:barrel", facing="up", open="false"))
    setb(9, 3, 16, block("minecraft:chain", axis="y"))
    setb(10, 3, 16, RUST_DEBRIS)
    setb(8, 4, 9, block("minecraft:iron_bars", north="false", south="false", east="true", west="true", waterlogged="false"))
    setb(15, 4, 9, block("minecraft:chain", axis="y"))
    setb(15, 5, 9, block("minecraft:redstone_torch", lit="true"))

    # Cyan emergency light line.
    for x in (9, 11, 13, 15):
        setb(x, 5, 10, GLASS)
        setb(x, 5, 15, GLASS)

    # Make certain gameplay spaces explicit.
    for pos in [(11, 3, 13), (11, 4, 13), (11, 3, 14), (11, 4, 14), (12, 3, 13), (12, 4, 13)]:
        setb(*pos, AIR)


def write_byte(v):
    return struct.pack(">b", v)


def write_short(v):
    return struct.pack(">h", v)


def write_int(v):
    return struct.pack(">i", v)


def write_string(s):
    data = s.encode("utf-8")
    return write_short(len(data)) + data


def tag_header(tag_type, name):
    return write_byte(tag_type) + write_string(name)


def tag_string(name, value):
    return tag_header(8, name) + write_string(value)


def tag_int(name, value):
    return tag_header(3, name) + write_int(value)


def int_list(name, values):
    return tag_header(9, name) + write_byte(3) + write_int(len(values)) + b"".join(write_int(v) for v in values)


def compound_payload(items):
    return b"".join(items) + write_byte(0)


def block_state_payload(state):
    name, props = state
    items = [tag_string("Name", name)]
    if props:
        prop_items = [tag_string(k, v) for k, v in props]
        items.append(tag_header(10, "Properties") + compound_payload(prop_items))
    return compound_payload(items)


def block_entry_payload(pos, state_id):
    return compound_payload([
        int_list("pos", list(pos)),
        tag_int("state", state_id),
    ])


def list_tag(name, element_type, payloads):
    return tag_header(9, name) + write_byte(element_type) + write_int(len(payloads)) + b"".join(payloads)


def build_nbt():
    palette = [AIR]
    for state in blocks.values():
        if state not in palette:
            palette.append(state)
    state_ids = {state: index for index, state in enumerate(palette)}
    ordered_blocks = sorted(blocks.items(), key=lambda item: (item[0][1], item[0][2], item[0][0]))

    root = tag_header(10, "")
    root += compound_payload([
        tag_int("DataVersion", DATA_VERSION),
        int_list("size", list(SIZE)),
        list_tag("palette", 10, [block_state_payload(state) for state in palette]),
        list_tag("blocks", 10, [block_entry_payload(pos, state_ids[state]) for pos, state in ordered_blocks]),
        list_tag("entities", 10, []),
    ])
    return root


def main():
    build_crash_scar()
    build_shell()
    build_interior()
    data = gzip.compress(build_nbt())
    for output in OUTPUTS:
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_bytes(data)
        print(f"Wrote {output.relative_to(ROOT)} ({len(blocks)} blocks, {len(data)} bytes)")


if __name__ == "__main__":
    main()
