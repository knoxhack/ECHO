#!/usr/bin/env python3
"""Batch bridge for Codex internal GPT image texture sheets.

This tool does not call OpenAI APIs and does not generate fallback art. It
builds ordered block/item manifests and prompts for the built-in image
generator, then crops a generated source sheet into Minecraft PNG assets. By
default it can crop to true 16x16 assets; use --native-resolution to keep the
generated cell artwork at source-sheet resolution.
"""

from __future__ import annotations

import argparse
import json
import math
import shutil
import warnings
from dataclasses import asdict, dataclass
from pathlib import Path
from typing import Iterable

from PIL import Image, ImageDraw


warnings.filterwarnings("ignore", category=DeprecationWarning)


REPO_ROOT = Path(__file__).resolve().parents[1]
BUILD_OUT = REPO_ROOT / "build" / "texture_previews" / "internal_gpt_textures"
GRID_SIZE = 8
CELL_SIZE = 128
SHEET_SIZE = GRID_SIZE * CELL_SIZE
FINAL_SIZE = 16
NATIVE_CROP_SIZE = 144
DEFAULT_BATCH_SIZE = GRID_SIZE * GRID_SIZE
BLOCK_TILE_REPLACEMENTS = {
    "wasteland_grass_block": "wasteland_dirt",
    "toxic_wasteland_grass_block": "contaminated_soil",
    "mutated_wasteland_grass_block": "spore_garden_top",
}


@dataclass(frozen=True)
class TextureRoot:
    target: str
    modid: str
    textures: Path


@dataclass(frozen=True)
class TextureAsset:
    index: int
    target: str
    modid: str
    category: str
    group: str
    name: str
    path: str
    prompt_hint: str


ROOTS = (
    TextureRoot(
        "ashfall",
        "echoashfallprotocol",
        REPO_ROOT
        / "src"
        / "main"
        / "resources"
        / "assets"
        / "echoashfallprotocol"
        / "textures",
    ),
    TextureRoot(
        "orbital",
        "echoorbitalremnants",
        REPO_ROOT
        / "addons"
        / "echoorbitalremnants"
        / "src"
        / "main"
        / "resources"
        / "assets"
        / "echoorbitalremnants"
        / "textures",
    ),
)

MACHINE_MARKERS = (
    "machine",
    "scrubber",
    "hopper",
    "battery_bank",
    "condenser",
    "synthesizer",
    "miner",
    "controller",
    "med_bay",
    "workbench",
    "recycler",
    "refiner",
    "generator",
    "capacitor",
    "core",
    "grinder",
    "relay",
    "array",
    "burner",
    "purifier",
    "compressor",
    "fabricator",
    "extractor",
    "assembler",
    "life_support",
)

PLANT_MARKERS = (
    "grass",
    "fern",
    "bush",
    "sapling",
    "leaves",
    "reed",
    "cactus",
    "fungus",
    "moss",
    "wheat",
)

ORE_MARKERS = (
    "ore",
    "crystal",
    "ingot",
    "alloy",
    "titanium",
    "uranium",
    "nexus",
    "lunar",
    "martian",
    "europa",
    "helium",
    "silica",
)

UTILITY_BLOCK_MARKERS = (
    "pipe",
    "cable",
    "conduit",
    "crate",
    "barrel",
    "terminal",
    "meter",
    "table",
    "pod",
    "glass",
    "door",
    "ladder",
)

ITEM_GROUP_MARKERS = (
    ("tools_weapons", ("blade", "sword", "axe", "pickaxe", "weapon", "rifle", "launcher", "tool")),
    ("batteries_modules", ("battery", "cell", "module", "capacitor", "core", "tank", "cartridge", "filter")),
    ("containers_medical", ("bottle", "vial", "syringe", "med", "bandage", "kit", "canister")),
    ("logs_maps_data", ("data", "log", "schematic", "map", "fragment", "tablet", "guide")),
    ("armor_equipment", ("helmet", "chestplate", "leggings", "boots", "mask", "visor", "liner")),
    ("spawn_eggs", ("spawn_egg",)),
)

BLOCK_GROUP_ORDER = {
    "terrain": 0,
    "plant_cutout": 1,
    "ore_crystal": 2,
    "machine_utility": 3,
    "misc_block": 4,
}

ITEM_GROUP_ORDER = {
    "resources": 0,
    "tools_weapons": 1,
    "batteries_modules": 2,
    "containers_medical": 3,
    "logs_maps_data": 4,
    "armor_equipment": 5,
    "spawn_eggs": 6,
    "misc_item": 7,
}


def has_any(name: str, markers: Iterable[str]) -> bool:
    return any(marker in name for marker in markers)


def classify_block(name: str) -> str:
    if has_any(name, MACHINE_MARKERS) or has_any(name, UTILITY_BLOCK_MARKERS):
        return "machine_utility"
    if has_any(name, PLANT_MARKERS):
        return "plant_cutout"
    if has_any(name, ORE_MARKERS):
        return "ore_crystal"
    if has_any(
        name,
        (
            "dirt",
            "soil",
            "stone",
            "ash",
            "dust",
            "sand",
            "slate",
            "rubble",
            "debris",
            "metal",
            "sheet",
            "concrete",
            "wood",
            "log",
            "plank",
            "ice",
            "slag",
        ),
    ):
        return "terrain"
    return "misc_block"


def classify_item(name: str) -> str:
    for group, markers in ITEM_GROUP_MARKERS:
        if has_any(name, markers):
            return group
    if has_any(name, ORE_MARKERS) or has_any(
        name,
        (
            "scrap",
            "dust",
            "shard",
            "chunk",
            "hide",
            "bone",
            "tissue",
            "wire",
            "plastic",
            "coal",
            "water",
        ),
    ):
        return "resources"
    return "misc_item"


def prompt_hint_for(asset: TextureAsset | dict[str, str]) -> str:
    name = asset.name if isinstance(asset, TextureAsset) else asset["name"]
    category = asset.category if isinstance(asset, TextureAsset) else asset["category"]
    group = asset.group if isinstance(asset, TextureAsset) else asset["group"]
    words = name.replace("_", " ")
    if category == "block":
        if group == "plant_cutout":
            return f"transparent/cutout plant-like block tile for {words}, sparse leafy pixels"
        if group == "ore_crystal":
            return f"tileable mineral block for {words}, embedded readable chunks, no center emblem"
        if group == "machine_utility":
            return f"industrial Minecraft block face for {words}, dark casing, vents, bolts, subtle accent lights"
        return f"seamless tileable material block for {words}, chunky connected clusters"
    if group == "tools_weapons":
        return f"clear angled item silhouette for {words}, dark outline, readable inventory shape"
    if group == "batteries_modules":
        return f"compact sci-fi module item for {words}, cylindrical or device silhouette, glowing accent"
    if group == "containers_medical":
        return f"bottle, kit, or medical container item for {words}, transparent background, strong silhouette"
    if group == "logs_maps_data":
        return f"readable document/device item for {words}, visible screen or parchment shape"
    if group == "armor_equipment":
        return f"wearable equipment icon for {words}, clean outline, material highlights"
    if group == "spawn_eggs":
        return f"Minecraft spawn egg item for {words}, distinct speckled colors"
    return f"readable 16x16 item sprite for {words}, transparent background"


def discover_assets(target: str, category: str) -> list[TextureAsset]:
    selected: list[TextureAsset] = []
    for root in ROOTS:
        if target != "all" and root.target != target:
            continue
        for cat in ("block", "item"):
            if category != "all" and category != cat:
                continue
            folder = root.textures / cat
            if not folder.exists():
                continue
            for path in sorted(folder.glob("*.png")):
                with Image.open(path) as image:
                    if image.size != (FINAL_SIZE, FINAL_SIZE):
                        continue
                name = path.stem
                group = classify_block(name) if cat == "block" else classify_item(name)
                selected.append(
                    TextureAsset(
                        index=-1,
                        target=root.target,
                        modid=root.modid,
                        category=cat,
                        group=group,
                        name=name,
                        path=str(path.relative_to(REPO_ROOT)),
                        prompt_hint="",
                    )
                )
    selected.sort(key=asset_sort_key)
    assets: list[TextureAsset] = []
    for index, asset in enumerate(selected):
        patched = TextureAsset(
            index=index,
            target=asset.target,
            modid=asset.modid,
            category=asset.category,
            group=asset.group,
            name=asset.name,
            path=asset.path,
            prompt_hint="",
        )
        assets.append(
            TextureAsset(
                index=patched.index,
                target=patched.target,
                modid=patched.modid,
                category=patched.category,
                group=patched.group,
                name=patched.name,
                path=patched.path,
                prompt_hint=prompt_hint_for(patched),
            )
        )
    return assets


def asset_sort_key(asset: TextureAsset) -> tuple[int, int, int, str, str]:
    cat_order = 0 if asset.category == "block" else 1
    if asset.category == "block":
        group_order = BLOCK_GROUP_ORDER.get(asset.group, 99)
    else:
        group_order = ITEM_GROUP_ORDER.get(asset.group, 99)
    target_order = 0 if asset.target == "ashfall" else 1
    return (cat_order, group_order, target_order, asset.name, asset.path)


def batch_dir(batch_id: str) -> Path:
    return BUILD_OUT / "batches" / batch_id


def batch_assets(assets: list[TextureAsset], offset: int, limit: int | None) -> list[TextureAsset]:
    if limit is None:
        limit = DEFAULT_BATCH_SIZE
    return assets[offset : offset + limit]


def make_batch_id(target: str, category: str, assets: list[TextureAsset], offset: int, limit: int | None) -> str:
    if not assets:
        return f"{target}_{category}_{offset:04d}_empty"
    end = offset + len(assets) - 1
    label = assets[0].category if category == "all" else category
    return f"{target}_{label}_{offset:04d}_{end:04d}"


def sheet_prompt(assets: list[TextureAsset]) -> str:
    categories = {asset.category for asset in assets}
    if categories == {"block"}:
        subject = "BLOCK TEXTURE SHEET"
        rules = (
            "Each cell is one enlarged seamless 16x16 Minecraft block texture. "
            "Fill the entire tile for solid/material blocks. For plant/cutout block cells, use pure magenta #ff00ff as the transparent background. "
            "Make edges connect naturally. Avoid giant center symbols except machine faces. "
            "Use chunky pixel clusters, top-left light, bottom-right shadow, crisp hard pixels, and rich but controlled palettes."
        )
        background = "No labels inside cells. Do not use magenta except as plant/cutout background. No screenshots or 3D perspective."
    elif categories == {"item"}:
        subject = "ITEM TEXTURE SHEET"
        rules = (
            "Each cell is one enlarged 16x16 Minecraft item sprite. "
            "Use a solid pure magenta #ff00ff chroma-key background in every cell, with the item centered and not touching cell edges. "
            "Use strong silhouettes, dark outlines, top-left highlights, bottom-right shadows, and readable inventory shapes."
        )
        background = "No labels inside cells. Do not use magenta in the item art. No screenshots or 3D perspective."
    else:
        subject = "MIXED BLOCK AND ITEM TEXTURE SHEET"
        rules = (
            "Blocks fill their cell as seamless square tiles. Items are centered sprites on pure magenta #ff00ff chroma-key backgrounds. "
            "Use crisp Minecraft 16x16 pixel art with strong readability."
        )
        background = "No labels inside cells. No screenshots or 3D perspective."

    cell_lines = []
    for i in range(DEFAULT_BATCH_SIZE):
        row = i // GRID_SIZE + 1
        col = i % GRID_SIZE + 1
        if i < len(assets):
            asset = assets[i]
            cell_lines.append(
                f"R{row}C{col}: {asset.category.upper()} {asset.modid}:{asset.name} - {asset.prompt_hint}."
            )
        else:
            cell_lines.append(f"R{row}C{col}: leave as a simple neutral unused texture tile.")

    return (
        f"{subject}\n"
        "Create a single square 1024x1024 PNG source sheet with an exact 8x8 grid of 128x128 cells. "
        "Use thin black separator lines between cells. The art inside each cell should look like an enlarged 16x16 pixel texture, "
        "with visible square pixel blocks and no blur.\n\n"
        "Style guide: follow the provided Minecraft 16x16 texture design guides. Clarity first. "
        "Rich reference look like the supplied machine sheets: dark industrial outlines where appropriate, chunky highlights, "
        "limited intentional accent colors, and clean silhouettes.\n\n"
        f"Rules: {rules}\n{background}\n\n"
        "Cell assignment, left to right, top to bottom:\n"
        + "\n".join(cell_lines)
    )


def write_manifest(batch_id: str, assets: list[TextureAsset], source_sheet: str | None = None) -> Path:
    out_dir = batch_dir(batch_id)
    out_dir.mkdir(parents=True, exist_ok=True)
    manifest = {
        "batch_id": batch_id,
        "grid_size": GRID_SIZE,
        "cell_size": CELL_SIZE,
        "final_size": FINAL_SIZE,
        "source_sheet": source_sheet,
        "assets": [asdict(asset) for asset in assets],
    }
    path = out_dir / "manifest.json"
    path.write_text(json.dumps(manifest, indent=2), encoding="utf-8")
    (out_dir / "prompt.txt").write_text(sheet_prompt(assets), encoding="utf-8")
    return path


def load_manifest(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def normalize_sheet(sheet_path: Path) -> Image.Image:
    image = Image.open(sheet_path).convert("RGBA")
    width, height = image.size
    side = min(width, height)
    left = (width - side) // 2
    top = (height - side) // 2
    image = image.crop((left, top, left + side, top + side))
    if image.size != (SHEET_SIZE, SHEET_SIZE):
        image = image.resize((SHEET_SIZE, SHEET_SIZE), Image.Resampling.NEAREST)
    return image


def dark_grid_runs(image: Image.Image, axis: str) -> list[tuple[int, int]]:
    """Find full-sheet black separator lines from generated grid sheets."""

    rgb = image.convert("RGB")
    width, height = rgb.size
    limit = width if axis == "row" else height
    span = height if axis == "row" else width
    threshold = span * 0.65
    hits: list[int] = []
    for i in range(limit):
        dark = 0
        if axis == "row":
            for x in range(width):
                r, g, b = rgb.getpixel((x, i))
                if r < 24 and g < 24 and b < 24:
                    dark += 1
        else:
            for y in range(height):
                r, g, b = rgb.getpixel((i, y))
                if r < 24 and g < 24 and b < 24:
                    dark += 1
        if dark >= threshold:
            hits.append(i)

    runs: list[tuple[int, int]] = []
    if not hits:
        return runs
    start = end = hits[0]
    for hit in hits[1:]:
        if hit == end + 1:
            end = hit
        else:
            runs.append((start, end))
            start = end = hit
    runs.append((start, end))
    return runs


def grid_intervals_from_runs(runs: list[tuple[int, int]], limit: int) -> list[tuple[int, int]]:
    if len(runs) < 2:
        return []
    intervals: list[tuple[int, int]] = []
    for previous, current in zip(runs, runs[1:]):
        start = max(0, previous[1] + 1)
        end = min(limit, current[0])
        if end - start > 12:
            intervals.append((start, end))
    return intervals


def line_dark_scores(image: Image.Image, axis: str) -> list[int]:
    """Count near-black pixels along each row or column."""

    rgb = image.convert("RGB")
    width, height = rgb.size
    pixels = list(rgb.getdata())
    scores: list[int] = []
    if axis == "row":
        for y in range(height):
            offset = y * width
            scores.append(
                sum(1 for r, g, b in pixels[offset : offset + width] if r < 24 and g < 24 and b < 24)
            )
    else:
        for x in range(width):
            dark = 0
            for y in range(height):
                r, g, b = pixels[y * width + x]
                if r < 24 and g < 24 and b < 24:
                    dark += 1
            scores.append(dark)
    return scores


def merge_close_runs(runs: list[tuple[int, int]], max_gap: int = 4) -> list[tuple[int, int]]:
    if not runs:
        return runs
    merged: list[tuple[int, int]] = [runs[0]]
    for start, end in runs[1:]:
        previous_start, previous_end = merged[-1]
        if start - previous_end <= max_gap:
            merged[-1] = (previous_start, end)
        else:
            merged.append((start, end))
    return merged


def best_local_separator_run(
    scores: list[int],
    expected: int,
    window: int,
    minimum_peak: int = 0,
) -> tuple[int, int]:
    """Pick the darkest separator run near an expected 8x8 boundary."""

    limit = len(scores)
    left = max(0, expected - window)
    right = min(limit - 1, expected + window)
    local = scores[left : right + 1]
    if not local:
        return expected, expected

    peak = max(local)
    if peak <= 0 or peak < minimum_peak:
        return expected, expected

    threshold = max(peak * 0.72, peak - max(10, peak // 7))
    hits = [left + i for i, score in enumerate(local) if score >= threshold]
    runs: list[tuple[int, int]] = []
    if hits:
        start = end = hits[0]
        for hit in hits[1:]:
            if hit == end + 1:
                end = hit
            else:
                runs.append((start, end))
                start = end = hit
        runs.append((start, end))
    runs = merge_close_runs(runs)
    if not runs:
        return expected, expected

    def run_key(run: tuple[int, int]) -> tuple[int, int, int]:
        start, end = run
        score = sum(scores[start : end + 1])
        center = (start + end) // 2
        return score, -abs(center - expected), end - start

    return max(runs, key=run_key)


def detect_expected_grid_intervals(
    sheet: Image.Image,
) -> tuple[list[tuple[int, int]], list[tuple[int, int]], dict[str, object]]:
    """Detect block-sheet cells by searching near expected 8x8 grid lines."""

    width, height = sheet.size
    row_scores = line_dark_scores(sheet, "row")
    col_scores = line_dark_scores(sheet, "col")
    row_window = max(10, round(height / (GRID_SIZE * 14)))
    col_window = max(10, round(width / (GRID_SIZE * 14)))
    row_minimum_peak = round(width * 0.34)
    col_minimum_peak = round(height * 0.34)

    row_boundaries: list[tuple[int, int]] = []
    col_boundaries: list[tuple[int, int]] = []
    for i in range(GRID_SIZE + 1):
        if i == 0:
            row_boundaries.append((0, 0))
            col_boundaries.append((0, 0))
            continue
        if i == GRID_SIZE:
            row_boundaries.append((height, height))
            col_boundaries.append((width, width))
            continue
        expected_y = round(i * height / GRID_SIZE)
        expected_x = round(i * width / GRID_SIZE)
        row_boundaries.append(
            best_local_separator_run(row_scores, expected_y, row_window, row_minimum_peak)
        )
        col_boundaries.append(
            best_local_separator_run(col_scores, expected_x, col_window, col_minimum_peak)
        )

    def expected_axis_intervals(limit: int) -> list[tuple[int, int]]:
        return [
            (round(i * limit / GRID_SIZE), round((i + 1) * limit / GRID_SIZE))
            for i in range(GRID_SIZE)
        ]

    row_intervals = grid_intervals_from_runs(row_boundaries, height)
    col_intervals = grid_intervals_from_runs(col_boundaries, width)
    ok = len(row_intervals) == GRID_SIZE and len(col_intervals) == GRID_SIZE
    row_mode = "local_expected"
    col_mode = "local_expected"
    minimum_interval = NATIVE_CROP_SIZE + 4
    if ok and any(bottom - top < minimum_interval for top, bottom in row_intervals):
        row_intervals = expected_axis_intervals(height)
        row_mode = "equal_slice_width_guard"
    if ok and any(right - left < minimum_interval for left, right in col_intervals):
        col_intervals = expected_axis_intervals(width)
        col_mode = "equal_slice_width_guard"
    metadata: dict[str, object] = {
        "mode": "local_expected_8x8" if ok else "equal_slice_fallback",
        "row_mode": row_mode,
        "col_mode": col_mode,
        "row_boundaries": row_boundaries,
        "col_boundaries": col_boundaries,
        "row_widths": [end - start for start, end in row_intervals],
        "col_widths": [end - start for start, end in col_intervals],
    }
    if not ok:
        fallback_rows = expected_axis_intervals(height)
        fallback_cols = expected_axis_intervals(width)
        return fallback_cols, fallback_rows, metadata
    return col_intervals, row_intervals, metadata


def detect_grid_intervals(sheet: Image.Image, asset_count: int) -> tuple[list[tuple[int, int]], list[tuple[int, int]]] | None:
    """Detect generated grid interiors, including sheets that drift from 8 columns."""

    width, height = sheet.size
    col_intervals = grid_intervals_from_runs(dark_grid_runs(sheet, "col"), width)
    row_intervals = grid_intervals_from_runs(dark_grid_runs(sheet, "row"), height)
    if not col_intervals or not row_intervals:
        return None

    # GPT sometimes adds a trailing blank ninth column while still laying out
    # the requested manifest cells in the first eight columns of each row. Use
    # detected bounds for those first eight columns instead of equal slicing.
    if len(col_intervals) >= GRID_SIZE:
        col_intervals = col_intervals[:GRID_SIZE]
    if len(row_intervals) >= GRID_SIZE:
        row_intervals = row_intervals[:GRID_SIZE]

    if len(col_intervals) != GRID_SIZE or len(row_intervals) != GRID_SIZE:
        return None
    if len(col_intervals) * len(row_intervals) < asset_count:
        return None
    return col_intervals, row_intervals


def center_crop_or_pad(image: Image.Image, output_size: int) -> Image.Image:
    """Normalize native generated cells without scaling their pixel art."""

    image = image.convert("RGBA")
    if image.width > output_size:
        left = (image.width - output_size) // 2
        image = image.crop((left, 0, left + output_size, image.height))
    if image.height > output_size:
        top = (image.height - output_size) // 2
        image = image.crop((0, top, image.width, top + output_size))
    if image.size == (output_size, output_size):
        return image

    final = Image.new("RGBA", (output_size, output_size), (0, 0, 0, 0))
    final.alpha_composite(image, ((output_size - image.width) // 2, (output_size - image.height) // 2))
    return final


def native_cell_from_grid(
    sheet: Image.Image,
    row: int,
    col: int,
    grid: tuple[list[tuple[int, int]], list[tuple[int, int]]],
) -> Image.Image:
    col_intervals, row_intervals = grid
    left, right = col_intervals[col]
    top, bottom = row_intervals[row]
    margin = max(1, round(min(right - left, bottom - top) / 72))
    left = min(left + margin, right)
    right = max(left, right - margin)
    top = min(top + margin, bottom)
    bottom = max(top, bottom - margin)
    return sheet.crop((left, top, right, bottom))


def native_cell_from_sheet(sheet: Image.Image, row: int, col: int, crop_size: int) -> Image.Image:
    """Crop the generated cell interior without resizing the sheet or cell art."""

    width, height = sheet.size
    left = round(col * width / GRID_SIZE)
    right = round((col + 1) * width / GRID_SIZE)
    top = round(row * height / GRID_SIZE)
    bottom = round((row + 1) * height / GRID_SIZE)

    # Built-in generation often returns ~1254px sheets with black grid
    # dividers. Crop just inside those dividers, then take a consistent square.
    margin = max(2, round(min(width, height) / 210))
    inner_left = min(left + margin, right)
    inner_right = max(inner_left, right - margin)
    inner_top = min(top + margin, bottom)
    inner_bottom = max(inner_top, bottom - margin)
    inner_width = inner_right - inner_left
    inner_height = inner_bottom - inner_top
    size = min(crop_size, inner_width, inner_height)
    if size <= 0:
        raise ValueError(f"Invalid native crop for row {row + 1}, col {col + 1}")

    crop_left = inner_left + (inner_width - size) // 2
    crop_top = inner_top + (inner_height - size) // 2
    return sheet.crop((crop_left, crop_top, crop_left + size, crop_top + size))


def corner_average(cell: Image.Image) -> tuple[int, int, int]:
    rgb = cell.convert("RGB")
    samples = []
    width, height = rgb.size
    for x0, y0 in ((0, 0), (width - 8, 0), (0, height - 8), (width - 8, height - 8)):
        crop = rgb.crop((x0, y0, x0 + 8, y0 + 8))
        pixels = list(crop.getdata())
        samples.extend(pixels)
    return tuple(sum(pixel[i] for pixel in samples) // len(samples) for i in range(3))


def color_distance(a: tuple[int, int, int], b: tuple[int, int, int]) -> int:
    return abs(a[0] - b[0]) + abs(a[1] - b[1]) + abs(a[2] - b[2])


def is_magenta_key_pixel(r: int, g: int, b: int, *, loose: bool = False) -> bool:
    if loose:
        return r >= 120 and b >= 120 and g <= 120 and r - g >= 55 and b - g >= 55 and abs(r - b) <= 115
    return r >= 180 and b >= 180 and g <= 90 and r - g >= 110 and b - g >= 110 and abs(r - b) <= 80


def is_purple_key_fringe_pixel(r: int, g: int, b: int) -> bool:
    """Catch darker anti-aliased chroma-key fringe without eating item art."""

    return (
        r >= 45
        and b >= 45
        and g <= 90
        and r - g >= 24
        and b - g >= 24
        and abs(r - b) <= 130
    )


def remove_key_background(rgba: Image.Image) -> Image.Image:
    """Remove generated chroma backgrounds while preserving purple item art."""

    rgba = rgba.convert("RGBA")
    width, height = rgba.size
    pixels = list(rgba.getdata())
    magenta = (255, 0, 255)

    border_indices: list[int] = []
    for x in range(width):
        border_indices.append(x)
        border_indices.append((height - 1) * width + x)
    for y in range(1, height - 1):
        border_indices.append(y * width)
        border_indices.append(y * width + width - 1)

    border_magenta = sum(
        1
        for i in border_indices
        if is_magenta_key_pixel(pixels[i][0], pixels[i][1], pixels[i][2], loose=True)
    )
    global_magenta = sum(
        1
        for r, g, b, a in pixels
        if a and is_magenta_key_pixel(r, g, b, loose=False)
    )
    use_magenta = border_magenta >= max(6, len(border_indices) // 32) or global_magenta >= len(pixels) // 80

    if use_magenta:
        def is_candidate(pixel: tuple[int, int, int, int]) -> bool:
            r, g, b, a = pixel
            return bool(a) and (
                is_magenta_key_pixel(r, g, b, loose=True)
                or is_purple_key_fringe_pixel(r, g, b)
            )
    else:
        bg = corner_average(rgba)
        threshold = 70

        def is_candidate(pixel: tuple[int, int, int, int]) -> bool:
            r, g, b, a = pixel
            if not a:
                return True
            return color_distance((r, g, b), bg) <= threshold or (
                r > 235 and g > 235 and b > 235 and color_distance(bg, magenta) > 120
            )

    candidates = [is_candidate(pixel) for pixel in pixels]
    reached = [False] * len(pixels)
    stack = [i for i in border_indices if candidates[i]]
    while stack:
        i = stack.pop()
        if reached[i]:
            continue
        reached[i] = True
        x = i % width
        y = i // width
        for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
            if nx < 0 or nx >= width or ny < 0 or ny >= height:
                continue
            ni = ny * width + nx
            if not reached[ni] and candidates[ni]:
                stack.append(ni)

    cleaned: list[tuple[int, int, int, int]] = []
    for i, (r, g, b, a) in enumerate(pixels):
        strong_magenta = use_magenta and is_magenta_key_pixel(r, g, b, loose=False)
        if reached[i] or strong_magenta:
            cleaned.append((0, 0, 0, 0))
        else:
            cleaned.append((r, g, b, a))
    rgba.putdata(cleaned)
    return rgba


def remove_edge_artifacts(rgba: Image.Image) -> Image.Image:
    width, height = rgba.size
    pixels = list(rgba.getdata())
    mask = [pixel[3] > 0 for pixel in pixels]
    seen = [False] * len(mask)
    cleaned = pixels[:]
    total_opaque = sum(mask)
    if total_opaque == 0:
        return rgba
    protected_box = (width * 0.16, height * 0.16, width * 0.84, height * 0.84)
    components: list[dict[str, object]] = []

    for start, keep in enumerate(mask):
        if not keep or seen[start]:
            continue
        stack = [start]
        component: list[int] = []
        touches_edge = False
        seen[start] = True
        while stack:
            i = stack.pop()
            component.append(i)
            x = i % width
            y = i // width
            if x <= 2 or y <= 2 or x >= width - 3 or y >= height - 3:
                touches_edge = True
            for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                if nx < 0 or nx >= width or ny < 0 or ny >= height:
                    continue
                ni = ny * width + nx
                if mask[ni] and not seen[ni]:
                    seen[ni] = True
                    stack.append(ni)
        xs = [i % width for i in component]
        ys = [i // width for i in component]
        bbox = (min(xs), min(ys), max(xs) + 1, max(ys) + 1)
        cx = sum(xs) / len(xs)
        cy = sum(ys) / len(ys)
        overlaps_protected = not (
            bbox[2] < protected_box[0]
            or bbox[0] > protected_box[2]
            or bbox[3] < protected_box[1]
            or bbox[1] > protected_box[3]
        )
        components.append(
            {
                "pixels": component,
                "area": len(component),
                "bbox": bbox,
                "touches_edge": touches_edge,
                "overlaps_protected": overlaps_protected,
                "centroid": (cx, cy),
            }
        )

    def should_remove(component: dict[str, object]) -> bool:
        area = int(component["area"])
        left, top, right, bottom = component["bbox"]  # type: ignore[misc]
        bbox_w = int(right) - int(left)
        bbox_h = int(bottom) - int(top)
        touches_edge = bool(component["touches_edge"])
        overlaps_protected = bool(component["overlaps_protected"])
        cx, cy = component["centroid"]  # type: ignore[misc]

        long_horizontal_line = bbox_h <= max(3, height // 48) and bbox_w >= width * 0.34
        long_vertical_line = bbox_w <= max(3, width // 48) and bbox_h >= height * 0.34
        if long_horizontal_line or long_vertical_line:
            return True

        # Keep multi-part sprites intact. Only edge-connected scraps are
        # removed; generated items are prompted and framed away from edges.
        if touches_edge:
            thin_edge_strip = bbox_w <= width * 0.18 or bbox_h <= height * 0.18
            outside_core = cx < width * 0.18 or cx > width * 0.82 or cy < height * 0.18 or cy > height * 0.82
            if thin_edge_strip and (outside_core or not overlaps_protected):
                return True
            if area <= max(10, total_opaque // 120) and not overlaps_protected:
                return True

        return False

    for component in components:
        if not should_remove(component):
            continue
        for i in component["pixels"]:  # type: ignore[assignment]
            cleaned[i] = (0, 0, 0, 0)

    rgba.putdata(cleaned)
    return rgba


def reframe_transparent_sprite(rgba: Image.Image, output_size: int) -> Image.Image:
    rgba = remove_edge_artifacts(rgba.convert("RGBA"))
    bbox = rgba.getbbox()
    if bbox is None:
        return Image.new("RGBA", (output_size, output_size), (0, 0, 0, 0))

    sprite = rgba.crop(bbox)
    padding = max(6, output_size // 16)
    max_side = output_size - padding * 2
    if sprite.width > max_side or sprite.height > max_side:
        sprite.thumbnail((max_side, max_side), Image.Resampling.NEAREST)

    final = Image.new("RGBA", (output_size, output_size), (0, 0, 0, 0))
    final.alpha_composite(sprite, ((output_size - sprite.width) // 2, (output_size - sprite.height) // 2))
    return final


def item_from_cell(cell: Image.Image, output_size: int | None) -> Image.Image:
    rgba = remove_key_background(cell.convert("RGBA"))
    return reframe_transparent_sprite(rgba, output_size or rgba.width)


def block_from_cell(cell: Image.Image, output_size: int | None) -> Image.Image:
    texture = cell.convert("RGBA")
    if output_size is not None:
        if output_size >= 64:
            texture = center_crop_or_pad(texture, output_size)
        else:
            texture = texture.resize((output_size, output_size), Image.Resampling.NEAREST)
    opaque = Image.new("RGBA", texture.size, (0, 0, 0, 255))
    opaque.alpha_composite(texture)
    return opaque


def visible_chroma_key_pixel_count(image: Image.Image) -> int:
    return sum(
        1
        for r, g, b, a in image.convert("RGBA").getdata()
        if a and is_magenta_key_pixel(r, g, b, loose=False)
    )


def repair_block_chroma_key(asset: dict, texture: Image.Image, expected_size: int) -> Image.Image:
    if asset["category"] != "block" or visible_chroma_key_pixel_count(texture) <= 128:
        return texture

    replacement = BLOCK_TILE_REPLACEMENTS.get(asset["name"])
    if replacement is None:
        return texture

    replacement_path = REPO_ROOT / Path(asset["path"]).with_name(f"{replacement}.png")
    if not replacement_path.exists():
        return texture

    with Image.open(replacement_path).convert("RGBA") as replacement_image:
        fixed = center_crop_or_pad(replacement_image, expected_size)
    opaque = Image.new("RGBA", fixed.size, (0, 0, 0, 255))
    opaque.alpha_composite(fixed)
    return opaque


def is_true_cutout_block(asset: dict) -> bool:
    if asset["category"] != "block":
        return False
    name = asset["name"]
    if name.endswith("_grass_block") or "wasteland_grass_block" in name:
        return False
    return asset.get("group") == "plant_cutout"


def validate_texture(asset: dict, image: Image.Image, expected_size: int) -> list[str]:
    errors: list[str] = []
    if image.size != (expected_size, expected_size):
        errors.append(
            f"{asset['path']} is {image.width}x{image.height}, expected {expected_size}x{expected_size}"
        )
    alpha = [pixel[3] for pixel in image.convert("RGBA").getdata()]
    transparent = sum(1 for a in alpha if a == 0)
    opaque = sum(1 for a in alpha if a > 0)
    if asset["category"] == "item":
        if transparent == 0:
            errors.append(f"{asset['path']} item has no transparent pixels")
        if opaque == 0:
            errors.append(f"{asset['path']} item is fully transparent")
    elif is_true_cutout_block(asset):
        if opaque == 0:
            errors.append(f"{asset['path']} plant/cutout block is fully transparent")
    else:
        if transparent:
            errors.append(f"{asset['path']} block has transparent pixels")
    return errors


def crop_sheet(
    manifest_path: Path,
    sheet_path: Path,
    apply: bool,
    native_resolution: bool,
    native_size: int,
) -> tuple[list[str], list[str]]:
    manifest_path = manifest_path.resolve()
    sheet_path = sheet_path.resolve()
    manifest = load_manifest(manifest_path)
    assets = manifest["assets"]
    out_dir = manifest_path.parent
    source_copy = out_dir / "source_sheet.png"
    if sheet_path != source_copy.resolve():
        shutil.copy2(sheet_path, source_copy)
    sheet = Image.open(sheet_path).convert("RGBA") if native_resolution else normalize_sheet(sheet_path)
    block_native_grid = native_resolution and assets and all(asset["category"] == "block" for asset in assets)
    detected_grid: tuple[list[tuple[int, int]], list[tuple[int, int]]] | None = None
    grid_metadata: dict[str, object] | None = None
    if native_resolution:
        if block_native_grid:
            col_intervals, row_intervals, grid_metadata = detect_expected_grid_intervals(sheet)
            detected_grid = (col_intervals, row_intervals)
        else:
            detected_grid = detect_grid_intervals(sheet, len(assets))
            if detected_grid is not None:
                grid_metadata = {
                    "mode": "detected_runs",
                    "col_widths": [right - left for left, right in detected_grid[0]],
                    "row_widths": [bottom - top for top, bottom in detected_grid[1]],
                }
            else:
                grid_metadata = {"mode": "equal_slice_fallback"}
    expected_size = native_size if native_resolution else FINAL_SIZE
    crop_records: list[tuple[dict, Path, Image.Image]] = []
    outputs: list[str] = []
    errors: list[str] = []

    crops_dir = out_dir / "crops"
    crops_dir.mkdir(parents=True, exist_ok=True)
    for cell_index, asset in enumerate(assets):
        row = cell_index // GRID_SIZE
        col = cell_index % GRID_SIZE
        if native_resolution:
            if detected_grid is not None:
                col_count = len(detected_grid[0])
                row = cell_index // col_count
                col = cell_index % col_count
                cell = native_cell_from_grid(sheet, row, col, detected_grid)
            else:
                cell = native_cell_from_sheet(sheet, row, col, native_size)
        else:
            cell = sheet.crop(
                (
                    col * CELL_SIZE,
                    row * CELL_SIZE,
                    (col + 1) * CELL_SIZE,
                    (row + 1) * CELL_SIZE,
                )
            )
        if asset["category"] == "item" or is_true_cutout_block(asset):
            texture = item_from_cell(cell, native_size if native_resolution else FINAL_SIZE)
        else:
            texture = block_from_cell(cell, native_size if native_resolution else FINAL_SIZE)
            texture = repair_block_chroma_key(asset, texture, expected_size)
        errors.extend(validate_texture(asset, texture, expected_size))
        crop_path = crops_dir / Path(asset["path"]).name
        texture.save(crop_path)
        crop_records.append((asset, crop_path, texture))
        outputs.append(str(crop_path))

    if apply and not errors:
        for asset, _, texture in crop_records:
            final_path = REPO_ROOT / asset["path"]
            final_path.parent.mkdir(parents=True, exist_ok=True)
            texture.save(final_path)

    updated = dict(manifest)
    updated["source_sheet"] = str(source_copy.relative_to(REPO_ROOT))
    updated["native_resolution"] = native_resolution
    updated["output_size"] = expected_size
    updated["applied"] = apply and not errors
    if grid_metadata is not None:
        updated["grid_detection"] = grid_metadata
    updated["crop_outputs"] = [str(Path(path).relative_to(REPO_ROOT)) for path in outputs]
    manifest_path.write_text(json.dumps(updated, indent=2), encoding="utf-8")
    write_contact_sheet(updated, out_dir / "contact_sheet.png")
    if any(asset["category"] == "block" for asset in assets):
        write_block_tiling_sheet(updated, out_dir / "block_3x3_tiling_sheet.png")
    return outputs, errors


def write_contact_sheet(manifest: dict, out_path: Path) -> None:
    assets = manifest["assets"]
    cols = 8
    label_h = 18
    cell = max(FINAL_SIZE * 4, min(int(manifest.get("output_size", FINAL_SIZE)), 144))
    rows = math.ceil(len(assets) / cols)
    sheet = Image.new("RGBA", (cols * cell, rows * (cell + label_h)), (28, 28, 28, 255))
    draw = ImageDraw.Draw(sheet)
    crops_dir = out_path.parent / "crops"
    for idx, asset in enumerate(assets):
        x = (idx % cols) * cell
        y = (idx // cols) * (cell + label_h)
        crop_path = crops_dir / Path(asset["path"]).name
        if crop_path.exists():
            with Image.open(crop_path).convert("RGBA") as image:
                preview = image.resize((cell, cell), Image.Resampling.NEAREST)
            if asset["category"] == "item":
                checker = checkerboard((cell, cell), 8)
                sheet.alpha_composite(checker, (x, y + label_h))
            sheet.alpha_composite(preview, (x, y + label_h))
        draw.text((x + 2, y + 2), asset["name"][:11], fill=(235, 235, 235, 255))
    sheet.save(out_path)


def write_block_tiling_sheet(manifest: dict, out_path: Path) -> None:
    assets = [asset for asset in manifest["assets"] if asset["category"] == "block"]
    if not assets:
        return
    cols = 4
    output_size = int(manifest.get("output_size", FINAL_SIZE))
    tile_size = max(FINAL_SIZE * 3 * 3, min(output_size * 3, 432))
    label_h = 18
    rows = math.ceil(len(assets) / cols)
    sheet = Image.new("RGBA", (cols * tile_size, rows * (tile_size + label_h)), (28, 28, 28, 255))
    draw = ImageDraw.Draw(sheet)
    crops_dir = out_path.parent / "crops"
    for idx, asset in enumerate(assets):
        x = (idx % cols) * tile_size
        y = (idx // cols) * (tile_size + label_h)
        crop_path = crops_dir / Path(asset["path"]).name
        if crop_path.exists():
            with Image.open(crop_path).convert("RGBA") as image:
                source_size = image.width
                tile = Image.new("RGBA", (source_size * 3, source_size * 3))
                for ty in range(3):
                    for tx in range(3):
                        tile.alpha_composite(image, (tx * source_size, ty * source_size))
                preview = tile.resize((tile_size, tile_size), Image.Resampling.NEAREST)
            sheet.alpha_composite(preview, (x, y + label_h))
        draw.text((x + 2, y + 2), asset["name"][:16], fill=(235, 235, 235, 255))
    sheet.save(out_path)


def checkerboard(size: tuple[int, int], block: int) -> Image.Image:
    image = Image.new("RGBA", size, (255, 255, 255, 255))
    draw = ImageDraw.Draw(image)
    for y in range(0, size[1], block):
        for x in range(0, size[0], block):
            if (x // block + y // block) % 2:
                draw.rectangle((x, y, x + block - 1, y + block - 1), fill=(205, 205, 205, 255))
    return image


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--target", choices=("ashfall", "orbital", "all"), default="all")
    parser.add_argument("--category", choices=("block", "item", "all"), default="all")
    parser.add_argument("--offset", type=int, default=0)
    parser.add_argument("--limit", type=int, default=DEFAULT_BATCH_SIZE)
    parser.add_argument("--prepare", action="store_true", help="Write a manifest and prompt for one batch.")
    parser.add_argument("--sheet", type=Path, help="Generated source sheet to crop/apply.")
    parser.add_argument("--manifest", type=Path, help="Existing batch manifest for --sheet.")
    parser.add_argument("--apply", action="store_true", help="Write validated crops to game asset paths.")
    parser.add_argument(
        "--native-resolution",
        action="store_true",
        help="Crop native generated cell interiors without downscaling to 16x16.",
    )
    parser.add_argument(
        "--native-size",
        type=int,
        default=NATIVE_CROP_SIZE,
        help="Square crop size for --native-resolution outputs.",
    )
    parser.add_argument("--list", action="store_true", help="Print manifest counts only.")
    args = parser.parse_args()

    assets = discover_assets(args.target, args.category)
    if args.list:
        counts: dict[tuple[str, str, str], int] = {}
        for asset in assets:
            key = (asset.target, asset.category, asset.group)
            counts[key] = counts.get(key, 0) + 1
        print(f"Texture assets: {len(assets)}")
        for key, count in sorted(counts.items()):
            print(f"  {key[0]} {key[1]} {key[2]}: {count}")
        return 0

    if args.sheet:
        manifest_path = args.manifest
        if not manifest_path:
            batch = batch_assets(assets, args.offset, args.limit)
            batch_id = make_batch_id(args.target, args.category, batch, args.offset, args.limit)
            manifest_path = write_manifest(batch_id, batch, str(args.sheet))
        outputs, errors = crop_sheet(
            manifest_path,
            args.sheet,
            args.apply,
            args.native_resolution,
            args.native_size,
        )
        print(f"Cropped textures: {len(outputs)}")
        print(f"Batch dir: {manifest_path.parent}")
        if errors:
            print("Validation errors:")
            for error in errors:
                print(f"  - {error}")
            return 1
        if args.apply:
            print("Applied crops to asset paths.")
        return 0

    batch = batch_assets(assets, args.offset, args.limit)
    if not batch:
        raise SystemExit("No assets selected for batch.")
    batch_id = make_batch_id(args.target, args.category, batch, args.offset, args.limit)
    manifest_path = write_manifest(batch_id, batch)
    print(f"Prepared batch: {batch_id}")
    print(f"Assets: {len(batch)}")
    print(f"Manifest: {manifest_path}")
    print(f"Prompt: {manifest_path.parent / 'prompt.txt'}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
