#!/usr/bin/env python3
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageEnhance, ImageFilter


ROOT = Path(__file__).resolve().parent
SHEETS = ROOT / "generated" / "source_sheets" / "cyberglass"
OUT = ROOT / "generated" / "assets" / "cyberglass"
FALLBACK = ROOT / "generated" / "fallback" / "cyberglass_procedural"


CORE = [
    ("background.png", (512, 512), False),
    ("glass_panel.png", (256, 256), False),
    ("glass_panel_alt.png", (256, 256), False),
    ("glass_button.png", (256, 96), False),
    ("glass_button_hover.png", (256, 96), False),
    ("tab.png", (192, 80), False),
    ("tab_active.png", (192, 80), False),
    ("mission_card.png", (320, 180), False),
    ("mission_card_selected.png", (320, 180), False),
    ("status_chip.png", (192, 64), False),
    ("progress_bar.png", (256, 48), False),
    ("scrollbar.png", (48, 256), False),
    ("hologram_overlay.png", (256, 256), True),
    ("energy_overlay.png", (256, 256), True),
    ("edge_glow.png", (256, 256), True),
    ("particle_glints.png", (256, 256), True),
    ("locked_overlay.png", (256, 256), True),
    ("vanilla_container_frame.png", (512, 384), False),
    ("vanilla_inventory_frame.png", (512, 384), False),
    ("vanilla_tooltip_panel.png", (256, 128), False),
]

HOLO_LENS = [
    ("holomap_grid.png", (512, 512), True),
    ("holomap_panel.png", (384, 256), False),
    ("marker_signal.png", (128, 128), True),
    ("marker_hazard.png", (128, 128), True),
    ("marker_mission.png", (128, 128), True),
    ("marker_nexus.png", (128, 128), True),
    ("marker_reclamation.png", (128, 128), True),
    ("route_line.png", (256, 64), True),
    ("selected_marker_ring.png", (256, 256), True),
    ("region_scan_sweep.png", (256, 256), True),
    ("lens_scan_ring.png", (256, 256), True),
    ("lens_target_box.png", (256, 256), True),
    ("lens_warning_overlay.png", (256, 256), True),
    ("lens_anomaly_overlay.png", (256, 256), True),
    ("lens_weakpoint_marker.png", (128, 128), True),
    ("lens_progress_arc.png", (256, 256), True),
    ("lens_noise_overlay.png", (256, 256), True),
    ("edge_glow.png", (256, 256), True),
    ("energy_overlay.png", (256, 256), True),
    ("hologram_overlay.png", (256, 256), True),
]

ICONS = [
    "icon_core.png", "icon_missions.png", "icon_holomap.png", "icon_lens.png",
    "icon_index.png", "icon_terminal.png", "icon_nexus.png", "icon_orbital.png",
    "icon_convoy.png", "icon_industrial.png", "icon_reclamation.png", "icon_blackbox.png",
    "icon_armory.png", "icon_runtime.png", "icon_sound.png", "icon_theme.png",
]

VANILLA_RENDER_BLOCK = [
    ("vanilla_title_backplate.png", (512, 256), False),
    ("vanilla_pause_panel.png", (384, 384), False),
    ("vanilla_inventory_frame.png", (512, 384), False),
    ("vanilla_container_frame.png", (512, 384), False),
    ("vanilla_tooltip_panel.png", (256, 128), False),
    ("vanilla_toast_accent.png", (256, 64), False),
    ("vanilla_hotbar_accent.png", (512, 64), False),
    ("vanilla_boss_bar_accent.png", (512, 48), False),
    ("vanilla_widget_outline.png", (256, 256), True),
    ("rendercore/hologram_style_reference.png", (512, 512), False),
    ("rendercore/particle_style_reference.png", (512, 512), True),
    ("rendercore/glow_overlay_reference.png", (512, 512), True),
    ("rendercore/distortion_overlay.png", (512, 512), True),
    ("rendercore/entity_highlight_reference.png", (512, 512), True),
    ("rendercore/multiblock_energy_lines.png", (512, 512), True),
    ("rendercore/terminal_boot_effect_reference.png", (512, 512), False),
    ("rendercore/lens_scan_effect_reference.png", (512, 512), False),
    ("rendercore/holomap_route_effect_reference.png", (512, 512), False),
    ("block_palette_sheet.png", (512, 512), False),
    ("light_strip_variants_sheet.png", (512, 512), False),
]

PUBLISHING = [
    ("theme_banner.png", (1600, 600), False),
    ("theme_desktop_wallpaper.png", (1920, 1080), False),
    ("theme_mobile_wallpaper.png", (1080, 1920), False),
    ("theme_overview_card.png", (1024, 1024), False),
    ("theme_feature_sheet.png", (1600, 1200), False),
]


def ensure(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)


def crop_grid(sheet: Image.Image, cols: int, rows: int, index: int, margin_ratio: float = 0.035) -> Image.Image:
    w, h = sheet.size
    cell_w = w / cols
    cell_h = h / rows
    col = index % cols
    row = index // cols
    margin_x = int(cell_w * margin_ratio)
    margin_y = int(cell_h * margin_ratio)
    box = (
        int(col * cell_w) + margin_x,
        int(row * cell_h) + margin_y,
        int((col + 1) * cell_w) - margin_x,
        int((row + 1) * cell_h) - margin_y,
    )
    return sheet.crop(box)


def dark_to_alpha(img: Image.Image) -> Image.Image:
    img = img.convert("RGBA")
    pix = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = pix[x, y]
            brightness = max(r, g, b)
            saturation = max(r, g, b) - min(r, g, b)
            if brightness < 20:
                alpha = 0
            elif brightness < 72 and saturation < 36:
                alpha = int((brightness - 20) * 2.2)
            else:
                alpha = min(255, int((brightness - 18) * 1.25))
            pix[x, y] = (r, g, b, min(a, max(0, alpha)))
    return img.filter(ImageFilter.GaussianBlur(0.15))


def polish(img: Image.Image, transparent: bool) -> Image.Image:
    img = img.convert("RGBA")
    img = ImageEnhance.Color(img).enhance(1.08)
    img = ImageEnhance.Contrast(img).enhance(1.05)
    if transparent:
        img = dark_to_alpha(img)
    return img


def save(img: Image.Image, relative: str, size: tuple[int, int], transparent: bool) -> None:
    target = OUT / relative
    ensure(target)
    img = polish(img, transparent)
    img = img.resize(size, Image.Resampling.LANCZOS)
    img.save(target)


def slice_sheet(sheet_name: str, cols: int, rows: int, mapping, margin_ratio: float = 0.035) -> None:
    sheet = Image.open(SHEETS / sheet_name).convert("RGBA")
    for index, item in enumerate(mapping):
        relative, size, transparent = item
        save(crop_grid(sheet, cols, rows, index, margin_ratio), relative, size, transparent)


def derive_variant(source: str, target: str, hue: str) -> None:
    img = Image.open(OUT / source).convert("RGBA")
    overlay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    if hue == "panel":
        color = (0, 229, 255, 46)
    else:
        color = (180, 76, 255, 52)
    overlay.paste(color, (0, 0, *img.size))
    img.alpha_composite(overlay)
    out = OUT / target
    ensure(out)
    img.save(out)


def copy_fallback_if_missing(relative: str) -> None:
    target = OUT / relative
    if target.exists():
        return
    source = FALLBACK / relative
    if source.exists():
        ensure(target)
        Image.open(source).save(target)


def main() -> None:
    slice_sheet("core_ui_sheet.png", 4, 5, CORE, 0.035)
    slice_sheet("holomap_lens_sheet.png", 4, 5, HOLO_LENS, 0.035)
    icon_sheet = Image.open(SHEETS / "icons_sheet.png").convert("RGBA")
    for index, name in enumerate(ICONS):
        save(crop_grid(icon_sheet, 4, 4, index, 0.045), f"icons/{name}", (128, 128), True)
    slice_sheet("vanilla_render_block_sheet.png", 4, 5, VANILLA_RENDER_BLOCK, 0.03)
    publishing = Image.open(SHEETS / "publishing_sheet.png").convert("RGBA")
    for index, item in enumerate(PUBLISHING):
        relative, size, transparent = item
        save(crop_grid(publishing, 2, 3, index, 0.018), relative, size, transparent)
    derive_variant("block_palette_sheet.png", "panel_variants_sheet.png", "panel")
    derive_variant("block_palette_sheet.png", "glass_variants_sheet.png", "glass")
    required = [
        *[item[0] for item in CORE],
        *[item[0] for item in HOLO_LENS],
        *[f"icons/{name}" for name in ICONS],
        *[item[0] for item in VANILLA_RENDER_BLOCK],
        *[item[0] for item in PUBLISHING],
        "panel_variants_sheet.png",
        "glass_variants_sheet.png",
    ]
    for relative in required:
        copy_fallback_if_missing(relative)
    print(f"Sliced CyberGlass image-generation sheets into {OUT}")


if __name__ == "__main__":
    main()
