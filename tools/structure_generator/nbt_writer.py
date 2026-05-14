"""
NBT serialization helpers using nbtlib.
Produces gzip-wrapped structure template files valid for the target structure-template format.
"""

from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple, Union

from nbtlib import File
from nbtlib.tag import (
    Byte,
    Compound,
    Float,
    Int,
    List as NBList,
    String,
)

from echo_blockwork import (
    ECHO_LOOT_CONTAINER_BLOCKS,
    loot_container_block_entity_id,
    sanitize_blocks,
    vanilla_structure_blocks,
)


DATA_VERSION = 4189  # the target structure-template format
DEFAULT_STRUCTURE_CACHE_LOOT_TABLE = "echoashfallprotocol:chests/survivor_cache"

# Block tuple may be 5-element (no block-entity data) or 6-element (with BE NBT dict).
BlockEntry = Union[
    Tuple[int, int, int, str, Optional[Dict[str, str]]],
    Tuple[int, int, int, str, Optional[Dict[str, str]], Optional[Dict[str, Any]]],
]


def _to_nbt_tag(value: Any):
    """Convert a plain Python value to an nbtlib tag.

    Accepts already-wrapped nbtlib tags and returns them unchanged so callers
    can force a specific type (e.g. Long, Short) when they need to.
    """
    # bool must be checked before int because bool is a subclass of int in Python.
    if isinstance(value, bool):
        return Byte(1 if value else 0)
    if isinstance(value, int):
        return Int(value)
    if isinstance(value, float):
        return Float(value)
    if isinstance(value, str):
        return String(value)
    if isinstance(value, dict):
        compound = Compound()
        for k, v in value.items():
            compound[k] = _to_nbt_tag(v)
        return compound
    if isinstance(value, list):
        if not value:
            return NBList[Compound]()
        tags = [_to_nbt_tag(item) for item in value]
        element_type = type(tags[0])
        typed_list = NBList[element_type]()
        for tag in tags:
            typed_list.append(tag)
        return typed_list
    # Already an nbtlib tag, pass through.
    return value


def _with_default_cache_loot(
    blocks: List[Tuple[int, int, int, str, Optional[Dict[str, str]], Optional[Dict[str, Any]]]],
    default_loot_table: Optional[str],
) -> List[Tuple[int, int, int, str, Optional[Dict[str, str]], Optional[Dict[str, Any]]]]:
    loot_table = default_loot_table or DEFAULT_STRUCTURE_CACHE_LOOT_TABLE
    normalized = []
    for x, y, z, block_id, props, be_nbt in blocks:
        if block_id not in ECHO_LOOT_CONTAINER_BLOCKS:
            normalized.append((x, y, z, block_id, props, be_nbt))
            continue

        block_entity_id = loot_container_block_entity_id(block_id)
        if not block_entity_id:
            normalized.append((x, y, z, block_id, props, be_nbt))
            continue

        next_nbt = dict(be_nbt or {})
        next_nbt["id"] = block_entity_id
        if not str(next_nbt.get("LootTable", "")):
            next_nbt["LootTable"] = loot_table
        normalized.append((x, y, z, block_id, props, next_nbt))
    return normalized


def write_structure_nbt(
    blocks: List[BlockEntry],
    output_path: Path,
    default_loot_table: Optional[str] = None,
) -> None:
    """
    Write a structure NBT file.

    Args:
        blocks: List of (x, y, z, block_id, properties_dict[, block_entity_nbt_dict]).
                block_id is a full namespaced ID like "minecraft:stone".
                properties_dict may be None.
                block_entity_nbt_dict, if present, is attached as the block's
                ``nbt`` compound (used for chests/barrels with a LootTable,
                signs, lecterns carrying a written book, etc.).
        output_path: Destination .nbt file path.
        default_loot_table: Loot table added to any Echo cache/crate block
                missing explicit loot NBT.
    """
    # Deduplicate palette by (block_id, frozenset(properties)).
    palette_entries: List[Tuple[str, Optional[Dict[str, str]]]] = []
    palette_index: Dict[Tuple[str, frozenset], int] = {}

    def get_palette_index(block_id: str, props: Optional[Dict[str, str]]) -> int:
        key = (block_id, frozenset(props.items()) if props else frozenset())
        if key not in palette_index:
            palette_index[key] = len(palette_entries)
            palette_entries.append((block_id, props))
        return palette_index[key]

    def unpack(entry: BlockEntry):
        if len(entry) == 6:
            return entry  # type: ignore[return-value]
        x, y, z, block_id, props = entry  # type: ignore[misc]
        return x, y, z, block_id, props, None

    blocks = sanitize_blocks(blocks)
    normalized = [unpack(entry) for entry in blocks]
    normalized = _with_default_cache_loot(normalized, default_loot_table)
    vanilla_blocks = vanilla_structure_blocks(block_id for _, _, _, block_id, _, _ in normalized)
    if vanilla_blocks:
        raise ValueError(f"{output_path} still contains non-air vanilla blocks: {', '.join(vanilla_blocks)}")

    # Compute bounds
    xs = [b[0] for b in normalized]
    ys = [b[1] for b in normalized]
    zs = [b[2] for b in normalized]
    width = max(xs) - min(xs) + 1 if normalized else 1
    height = max(ys) - min(ys) + 1 if normalized else 1
    depth = max(zs) - min(zs) + 1 if normalized else 1
    ox = min(xs) if normalized else 0
    oy = min(ys) if normalized else 0
    oz = min(zs) if normalized else 0

    # Populate palette first so indices are valid.
    for _, _, _, block_id, props, _ in normalized:
        get_palette_index(block_id, props)

    palette_tags = NBList[Compound]()
    for block_id, props in palette_entries:
        entry = Compound({"Name": String(block_id)})
        if props:
            prop_compound = Compound()
            for k, v in props.items():
                prop_compound[k] = String(v)
            entry["Properties"] = prop_compound
        palette_tags.append(entry)

    block_tags = NBList[Compound]()
    for x, y, z, block_id, props, be_nbt in normalized:
        idx = get_palette_index(block_id, props)
        block_compound = Compound({
            "pos": NBList[Int]([Int(x - ox), Int(y - oy), Int(z - oz)]),
            "state": Int(idx),
        })
        if be_nbt:
            block_compound["nbt"] = _to_nbt_tag(be_nbt)
        block_tags.append(block_compound)

    root = Compound({
        "DataVersion": Int(DATA_VERSION),
        "size": NBList[Int]([Int(width), Int(height), Int(depth)]),
        "palette": palette_tags,
        "blocks": block_tags,
        "entities": NBList[Compound](),
    })

    nbt_file = File(root, gzipped=True)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    nbt_file.save(str(output_path))
