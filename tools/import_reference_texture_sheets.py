#!/usr/bin/env python3
"""Import user-provided reference texture sheets into Echo block textures.

The reference images in Downloads are presentation sheets: each contains a
machine block icon plus several large face renders. This tool crops the large
face renders, downsamples them to true 16x16 Minecraft textures, and writes
them to the existing per-face machine texture paths.
"""

from __future__ import annotations

import argparse
import json
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

from PIL import Image, ImageDraw


REPO_ROOT = Path(__file__).resolve().parents[1]
ASHFALL_BLOCK_TEXTURES = (
    REPO_ROOT
    / "src"
    / "main"
    / "resources"
    / "assets"
    / "echoashfallprotocol"
    / "textures"
    / "block"
)
BUILD_OUT = REPO_ROOT / "build" / "texture_previews" / "reference_texture_imports"


@dataclass(frozen=True)
class SheetMapping:
    block: str
    filename: str


@dataclass(frozen=True)
class Box:
    x0: int
    y0: int
    x1: int
    y1: int

    @property
    def width(self) -> int:
        return self.x1 - self.x0

    @property
    def height(self) -> int:
        return self.y1 - self.y0

    @property
    def area(self) -> int:
        return self.width * self.height

    @property
    def cx(self) -> float:
        return (self.x0 + self.x1) / 2

    @property
    def cy(self) -> float:
        return (self.y0 + self.y1) / 2


MACHINE_SHEETS: tuple[SheetMapping, ...] = (
    SheetMapping("hand_recycler", "ChatGPT Image May 1, 2026, 03_23_50 PM (1).png"),
    SheetMapping("thermal_array", "ChatGPT Image May 1, 2026, 03_23_50 PM (2).png"),
    SheetMapping("water_purifier", "ChatGPT Image May 1, 2026, 03_23_50 PM (3).png"),
    SheetMapping("filter_workbench", "ChatGPT Image May 1, 2026, 03_23_50 PM (4).png"),
    SheetMapping("thermal_burner", "ChatGPT Image May 1, 2026, 03_23_50 PM (5).png"),
    SheetMapping("ore_grinder", "ChatGPT Image May 1, 2026, 03_23_50 PM (6).png"),
    SheetMapping("nexus_capacitor", "ChatGPT Image May 1, 2026, 03_23_50 PM (7).png"),
    SheetMapping("contaminant_condenser", "ChatGPT Image May 1, 2026, 03_23_50 PM (8).png"),
    SheetMapping("micro_generator", "ChatGPT Image May 1, 2026, 03_24_03 PM (1).png"),
    SheetMapping("battery_bank", "ChatGPT Image May 1, 2026, 03_24_03 PM (2).png"),
    SheetMapping("factory_controller", "ChatGPT Image May 1, 2026, 03_24_03 PM (3).png"),
    SheetMapping("atmospheric_scrubber", "ChatGPT Image May 1, 2026, 03_24_03 PM (4).png"),
    SheetMapping("bio_processing_station", "ChatGPT Image May 1, 2026, 03_24_03 PM (5).png"),
    SheetMapping("crystalline_synthesizer", "ChatGPT Image May 1, 2026, 03_24_03 PM (6).png"),
    SheetMapping("deep_core_miner", "ChatGPT Image May 1, 2026, 03_24_03 PM (7).png"),
    SheetMapping("isotope_refiner", "ChatGPT Image May 1, 2026, 03_24_03 PM (8).png"),
)


def existing_texture(name: str) -> bool:
    return (ASHFALL_BLOCK_TEXTURES / f"{name}.png").exists()


def target_block_name(mapping: SheetMapping) -> str:
    return mapping.block


def sprite_mask(image: Image.Image) -> list[list[bool]]:
    pixels = image.convert("RGB").load()
    width, height = image.size
    mask: list[list[bool]] = [[False] * width for _ in range(height)]
    for y in range(height):
        row = mask[y]
        for x in range(width):
            r, g, b = pixels[x, y]
            high = max(r, g, b)
            low = min(r, g, b)
            saturation = high - low
            row[x] = high < 220 or (high < 245 and saturation > 18)
    return mask


def connected_boxes(mask: list[list[bool]]) -> list[Box]:
    height = len(mask)
    width = len(mask[0])
    seen = [[False] * width for _ in range(height)]
    boxes: list[tuple[int, Box]] = []
    for y in range(height):
        for x in range(width):
            if seen[y][x] or not mask[y][x]:
                continue
            stack = [(x, y)]
            seen[y][x] = True
            x0 = x1 = x
            y0 = y1 = y
            count = 0
            while stack:
                sx, sy = stack.pop()
                count += 1
                x0 = min(x0, sx)
                x1 = max(x1, sx)
                y0 = min(y0, sy)
                y1 = max(y1, sy)
                for ny in range(sy - 1, sy + 2):
                    if ny < 0 or ny >= height:
                        continue
                    for nx in range(sx - 1, sx + 2):
                        if nx < 0 or nx >= width or seen[ny][nx] or not mask[ny][nx]:
                            continue
                        seen[ny][nx] = True
                        stack.append((nx, ny))
            if count >= 500:
                boxes.append((count, Box(x0, y0, x1 + 1, y1 + 1)))
    return [box for _, box in sorted(boxes, key=lambda item: item[0], reverse=True)]


def large_face_boxes(image: Image.Image) -> list[Box]:
    boxes = connected_boxes(sprite_mask(image))
    large = [
        box
        for box in boxes
        if box.width >= 230
        and box.height >= 230
        and 0.72 <= box.width / max(1, box.height) <= 1.35
    ]
    return sorted(large, key=lambda box: (box.cy, box.cx))


def cluster_rows(boxes: Iterable[Box]) -> list[list[Box]]:
    rows: list[list[Box]] = []
    for box in sorted(boxes, key=lambda item: item.cy):
        for row in rows:
            if abs(row[0].cy - box.cy) < 130:
                row.append(box)
                break
        else:
            rows.append([box])
    return [sorted(row, key=lambda item: item.cx) for row in rows]


def choose_faces(boxes: list[Box]) -> dict[str, Box]:
    rows = cluster_rows(boxes)
    face_rows = [row for row in rows if len(row) >= 2]
    if not face_rows:
        raise ValueError("No large face rows found")
    upper = face_rows[0]
    lower = face_rows[1] if len(face_rows) > 1 else face_rows[0]
    faces = {
        "front": upper[0],
        "active_front": upper[1] if len(upper) > 1 else upper[0],
        "side": upper[2] if len(upper) > 2 else upper[-1],
        "top": lower[1] if len(lower) > 1 else lower[0],
        "bottom": lower[-1],
    }
    return faces


def square_crop(image: Image.Image, box: Box, padding: int = 2) -> Image.Image:
    cx = box.cx
    cy = box.cy
    side = max(box.width, box.height) + padding * 2
    x0 = round(cx - side / 2)
    y0 = round(cy - side / 2)
    x1 = x0 + side
    y1 = y0 + side
    if x0 < 0 or y0 < 0 or x1 > image.width or y1 > image.height:
        padded = Image.new("RGB", (image.width + side * 2, image.height + side * 2), (255, 255, 255))
        padded.paste(image.convert("RGB"), (side, side))
        return padded.crop((x0 + side, y0 + side, x1 + side, y1 + side))
    return image.convert("RGB").crop((x0, y0, x1, y1))


def to_texture(image: Image.Image, box: Box) -> Image.Image:
    crop = square_crop(image, box)
    texture = crop.resize((16, 16), Image.Resampling.LANCZOS).convert("RGBA")
    opaque = Image.new("RGBA", texture.size, (0, 0, 0, 255))
    opaque.alpha_composite(texture)
    return opaque


def write_machine_textures(source_dir: Path, mapping: SheetMapping, dry_run: bool) -> list[dict[str, str]]:
    path = source_dir / mapping.filename
    if not path.exists():
        raise FileNotFoundError(path)
    block = target_block_name(mapping)
    if not existing_texture(f"{block}_front"):
        raise FileNotFoundError(ASHFALL_BLOCK_TEXTURES / f"{block}_front.png")
    image = Image.open(path)
    faces = choose_faces(large_face_boxes(image))
    outputs: list[tuple[str, Image.Image]] = [
        (f"{block}_front", to_texture(image, faces["front"])),
        (f"{block}_side", to_texture(image, faces["side"])),
        (f"{block}_top", to_texture(image, faces["top"])),
        (f"{block}_bottom", to_texture(image, faces["bottom"])),
        (block, to_texture(image, faces["front"])),
    ]
    active_front = to_texture(image, faces["active_front"])
    if existing_texture(f"{block}_active_front"):
        outputs.extend(
            [
                (f"{block}_active_front", active_front),
                (f"{block}_active_side", to_texture(image, faces["side"])),
                (f"{block}_active_top", to_texture(image, faces["top"])),
                (f"{block}_active_bottom", to_texture(image, faces["bottom"])),
                (f"{block}_active", active_front),
            ]
        )
    records: list[dict[str, str]] = []
    for name, texture in outputs:
        out_path = ASHFALL_BLOCK_TEXTURES / f"{name}.png"
        records.append({"block": block, "texture": name, "path": str(out_path.relative_to(REPO_ROOT))})
        if not dry_run:
            texture.save(out_path)
    return records


def write_contact_sheet(records: list[dict[str, str]], out_path: Path) -> None:
    grouped: dict[str, list[dict[str, str]]] = {}
    for record in records:
        grouped.setdefault(record["block"], []).append(record)
    cell = 48
    label_h = 12
    cols = 5
    rows = len(grouped)
    sheet = Image.new("RGBA", (cols * cell, rows * (cell + label_h)), (24, 24, 24, 255))
    draw = ImageDraw.Draw(sheet)
    order = ("front", "active_front", "side", "top", "bottom")
    for row_idx, (block, block_records) in enumerate(sorted(grouped.items())):
        by_suffix: dict[str, Path] = {}
        for record in block_records:
            texture = Path(record["path"]).stem
            suffix = texture.removeprefix(block).removeprefix("_")
            by_suffix[suffix or "front"] = REPO_ROOT / record["path"]
        draw.text((2, row_idx * (cell + label_h)), block[:24], fill=(230, 230, 230, 255))
        for col_idx, suffix in enumerate(order):
            source = by_suffix.get(suffix)
            if not source or not source.exists():
                continue
            with Image.open(source).convert("RGBA") as img:
                preview = img.resize((cell, cell), Image.Resampling.NEAREST)
            sheet.alpha_composite(preview, (col_idx * cell, row_idx * (cell + label_h) + label_h))
    out_path.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(out_path)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-dir", type=Path, default=Path.home() / "Downloads")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    all_records: list[dict[str, str]] = []
    for mapping in MACHINE_SHEETS:
        records = write_machine_textures(args.source_dir, mapping, args.dry_run)
        all_records.extend(records)

    BUILD_OUT.mkdir(parents=True, exist_ok=True)
    manifest_path = BUILD_OUT / "machine_reference_import_manifest.json"
    manifest_path.write_text(json.dumps(all_records, indent=2), encoding="utf-8")
    if not args.dry_run:
        write_contact_sheet(all_records, BUILD_OUT / "machine_reference_import_contact_sheet.png")

    print(f"Imported machine reference sheets: {len(MACHINE_SHEETS)}")
    print(f"Texture writes: {len(all_records)}")
    print(f"Manifest: {manifest_path}")
    if not args.dry_run:
        print(f"Contact sheet: {BUILD_OUT / 'machine_reference_import_contact_sheet.png'}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
