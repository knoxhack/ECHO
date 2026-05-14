#!/usr/bin/env python3
"""Inject clean Convoy vehicle UV export panels into generated production boards."""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageEnhance

from generate_echo_mob_rendercore_assets import EXPORT_PANEL_BOXES, ROOT


VEHICLES = ("scrap_bike", "wasteland_rover", "cargo_crawler", "armored_relay_truck")
MODID = "echoconvoyprotocol"
BOARD_ROOT = ROOT / "docs/echo_mob_rendercore/entity_sheets" / MODID
SOURCE_TEXTURE_ROOT = ROOT / "addons/echoconvoyprotocol/src/main/resources/assets/echoconvoyprotocol/textures/entity"


def main() -> int:
    count = 0
    for vehicle in VEHICLES:
        board_path = BOARD_ROOT / f"{vehicle}.png"
        base_path = SOURCE_TEXTURE_ROOT / f"{vehicle}.png"
        if not board_path.exists():
            raise FileNotFoundError(board_path)
        if not base_path.exists():
            raise FileNotFoundError(base_path)

        with Image.open(board_path) as board_image:
            board = board_image.convert("RGBA")
        with Image.open(base_path) as base_image:
            base = base_image.convert("RGBA").resize((256, 256), Image.Resampling.NEAREST)

        glow = make_glow_overlay(base)
        damage = make_damage_overlay(base, vehicle)
        paste_export_panel(board, "base", base)
        paste_export_panel(board, "glow", glow)
        paste_export_panel(board, "damage", damage)
        board.save(board_path)
        count += 1

    print(f"Normalized {count} Convoy vehicle production boards.")
    return 0


def paste_export_panel(board: Image.Image, panel: str, atlas: Image.Image) -> None:
    width, height = board.size
    left_n, top_n, right_n, bottom_n = EXPORT_PANEL_BOXES[panel]
    left = round(width * left_n)
    top = round(height * top_n)
    right = round(width * right_n)
    bottom = round(height * bottom_n)
    panel_width = right - left
    panel_height = bottom - top
    side = min(panel_width, panel_height)
    x = left + (panel_width - side) // 2
    y = top + (panel_height - side) // 2
    board.alpha_composite(Image.new("RGBA", (panel_width, panel_height), (10, 13, 14, 255)), (left, top))
    board.alpha_composite(atlas.resize((side, side), Image.Resampling.NEAREST), (x, y))


def make_glow_overlay(base: Image.Image) -> Image.Image:
    overlay = Image.new("RGBA", base.size, (0, 0, 0, 0))
    source = ImageEnhance.Color(base.convert("RGBA")).enhance(1.3)
    src = source.load()
    out = overlay.load()
    for y in range(base.height):
        for x in range(base.width):
            r, g, b, a = src[x, y]
            if a == 0:
                continue
            cyan = b > 130 and g > 105 and r < 110
            amber = r > 165 and 55 < g < 180 and b < 95
            headlight = r > 175 and g > 145 and b < 120
            if cyan or amber or headlight:
                out[x, y] = (min(255, r + 30), min(255, g + 30), min(255, b + 42), 220)
    return overlay


def make_damage_overlay(base: Image.Image, vehicle: str) -> Image.Image:
    overlay = Image.new("RGBA", base.size, (0, 0, 0, 0))
    src = base.convert("RGBA").load()
    out = overlay.load()
    salt = sum(ord(ch) for ch in vehicle)
    for y in range(base.height):
        for x in range(base.width):
            r, g, b, a = src[x, y]
            if a == 0:
                continue
            metal = max(r, g, b) > 42 and not (b > 130 and g > 105 and r < 110)
            crack = ((x * 17 + y * 31 + salt) % 97 == 0) or ((x - y + salt) % 113 == 0)
            scorch = ((x // 8 + y // 8 + salt) % 11 == 0) and metal
            if crack:
                out[x, y] = (255, 122, 40, 210)
            elif scorch:
                gray = max(80, min(170, (r + g + b) // 3))
                out[x, y] = (gray, gray, gray, 120)
    return overlay


if __name__ == "__main__":
    raise SystemExit(main())
