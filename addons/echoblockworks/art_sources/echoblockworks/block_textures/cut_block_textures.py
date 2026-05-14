from __future__ import annotations

import argparse
import json
from pathlib import Path

from PIL import Image, ImageFilter


ROOT = Path(__file__).resolve().parents[3]
MANIFEST = Path(__file__).with_name("manifest.json")
SOURCE_DIR = Path(__file__).with_name("source_sheets")
TEXTURE_DIR = ROOT / "src" / "main" / "resources" / "assets" / "echoblockworks" / "textures" / "block"


def texture_ids(sheet: dict) -> list[str]:
    if "textures" in sheet:
        return sheet["textures"]
    return [f"{sheet['prefix']}_{variant}" for variant in sheet["variants"]]


def crop_cell(sheet_image: Image.Image, cols: int, rows: int, index: int, inset_ratio: float) -> Image.Image:
    col = index % cols
    row = index // cols
    width, height = sheet_image.size
    left = round(col * width / cols)
    top = round(row * height / rows)
    right = round((col + 1) * width / cols)
    bottom = round((row + 1) * height / rows)
    inset_x = round((right - left) * inset_ratio)
    inset_y = round((bottom - top) * inset_ratio)
    return sheet_image.crop((left + inset_x, top + inset_y, right - inset_x, bottom - inset_y))


def resized_alpha(source_path: Path, size: int) -> Image.Image:
    with Image.open(source_path) as existing:
        return existing.convert("RGBA").getchannel("A").resize((size, size), Image.Resampling.NEAREST)


def cut_sheet(sheet: dict, tile_size: int, default_inset: float, output_dir: Path, alpha_dir: Path) -> list[Path]:
    source = SOURCE_DIR / f"{sheet['id']}.png"
    if not source.exists():
        raise FileNotFoundError(f"Missing source sheet: {source}")

    cols, rows = sheet["grid"]
    ids = texture_ids(sheet)
    if len(ids) > cols * rows:
        raise ValueError(f"Sheet {sheet['id']} has {len(ids)} textures but only {cols * rows} cells")

    output_paths: list[Path] = []
    with Image.open(source) as image:
        sheet_image = image.convert("RGBA")
        inset = float(sheet.get("crop_inset", default_inset))
        for index, texture_id in enumerate(ids):
            target = output_dir / f"{texture_id}.png"
            alpha_source = alpha_dir / f"{texture_id}.png"
            if not alpha_source.exists():
                raise FileNotFoundError(f"Cannot preserve alpha; existing texture is missing: {alpha_source}")

            cell = crop_cell(sheet_image, cols, rows, index, inset)
            tile = cell.resize((tile_size, tile_size), Image.Resampling.LANCZOS)
            tile = tile.filter(ImageFilter.UnsharpMask(radius=0.5, percent=130, threshold=2))
            tile.putalpha(resized_alpha(alpha_source, tile_size))
            output_dir.mkdir(parents=True, exist_ok=True)
            tile.save(target)
            output_paths.append(target)
    return output_paths


def main() -> None:
    parser = argparse.ArgumentParser(description="Cut ECHO Blockworks source sheets into live 32x32 block textures.")
    parser.add_argument("--manifest", type=Path, default=MANIFEST)
    parser.add_argument("--output-dir", type=Path, default=TEXTURE_DIR)
    parser.add_argument("--alpha-dir", type=Path, default=TEXTURE_DIR)
    args = parser.parse_args()

    with args.manifest.open("r", encoding="utf-8") as manifest_file:
        manifest = json.load(manifest_file)

    tile_size = int(manifest["tile_size"])
    default_inset = float(manifest.get("crop_inset", 0.0))
    output_dir = args.output_dir.resolve()
    alpha_dir = args.alpha_dir.resolve()

    written: list[Path] = []
    for sheet in manifest["sheets"]:
        written.extend(cut_sheet(sheet, tile_size, default_inset, output_dir, alpha_dir))

    print(f"Wrote {len(written)} block textures to {output_dir}")


if __name__ == "__main__":
    main()
