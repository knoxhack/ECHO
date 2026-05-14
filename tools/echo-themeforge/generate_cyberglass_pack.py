#!/usr/bin/env python3
from __future__ import annotations

import math
import random
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont


ROOT = Path(__file__).resolve().parent
OUT = ROOT / "generated" / "assets" / "cyberglass"

BG = (3, 7, 17, 255)
PANEL = (8, 17, 31, 210)
PANEL_ALT = (13, 26, 46, 212)
GLASS = (16, 36, 58, 128)
CYAN = (0, 229, 255, 255)
CYAN_SOFT = (43, 234, 255, 190)
VIOLET = (180, 76, 255, 255)
MAGENTA = (255, 43, 214, 255)
TEXT = (234, 251, 255, 255)
MUTED = (138, 175, 194, 210)
SUCCESS = (69, 255, 176, 255)
WARNING = (255, 209, 102, 255)
ERROR = (255, 77, 109, 255)


def ensure(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)


def rgba(size, color=(0, 0, 0, 0)):
    return Image.new("RGBA", size, color)


def add(base: Image.Image, layer: Image.Image) -> Image.Image:
    base.alpha_composite(layer)
    return base


def gradient(size, top, bottom, horizontal=False):
    w, h = size
    img = rgba(size)
    pix = img.load()
    span = max(1, (w if horizontal else h) - 1)
    for y in range(h):
        for x in range(w):
            t = (x if horizontal else y) / span
            pix[x, y] = tuple(int(top[i] * (1 - t) + bottom[i] * t) for i in range(4))
    return img


def glow_line(size, points, color, width=2, blur=6):
    layer = rgba(size)
    draw = ImageDraw.Draw(layer)
    draw.line(points, fill=color, width=width, joint="curve")
    glow = layer.filter(ImageFilter.GaussianBlur(blur))
    out = rgba(size)
    out.alpha_composite(glow)
    out.alpha_composite(layer)
    return out


def draw_panel(size=(256, 256), alt=False, selected=False, corners=True):
    w, h = size
    img = gradient(size, PANEL_ALT if alt else PANEL, BG)
    glass = rgba(size)
    gd = ImageDraw.Draw(glass)
    gd.rounded_rectangle((5, 5, w - 6, h - 6), radius=10 if corners else 2, fill=GLASS)
    gd.rounded_rectangle((7, 7, w - 8, h - 8), radius=8 if corners else 1, outline=(255, 255, 255, 22), width=1)
    add(img, glass)
    d = ImageDraw.Draw(img)
    edge = VIOLET if selected else CYAN_SOFT
    d.rounded_rectangle((3, 3, w - 4, h - 4), radius=12 if corners else 2, outline=edge, width=2)
    d.rounded_rectangle((9, 9, w - 10, h - 10), radius=7 if corners else 1, outline=(43, 234, 255, 54), width=1)
    for i in range(0, w, 54):
        y = (i * 37) % h
        d.line((i, y, min(w - 1, i + 24), y), fill=(43, 234, 255, 50), width=1)
        d.line((min(w - 1, i + 24), y, min(w - 1, i + 36), min(h - 1, y + 12)), fill=(180, 76, 255, 40), width=1)
    d.rectangle((0, 0, w - 1, 1), fill=(43, 234, 255, 70))
    d.rectangle((w - 2, h // 4, w - 1, h - h // 5), fill=(255, 43, 214, 85))
    return img


def save(img, relative):
    path = OUT / relative
    ensure(path)
    img.save(path)


def button(size=(256, 96), hover=False):
    img = draw_panel(size, alt=hover, selected=hover)
    d = ImageDraw.Draw(img)
    w, h = size
    fill = (0, 229, 255, 70 if hover else 36)
    d.rounded_rectangle((18, 18, w - 19, h - 19), radius=8, fill=fill, outline=(255, 43, 214, 125 if hover else 70), width=1)
    d.line((26, h - 22, w - 27, h - 22), fill=(0, 229, 255, 145 if hover else 80), width=2)
    return img


def tab(active=False):
    img = draw_panel((192, 80), alt=active, selected=active)
    d = ImageDraw.Draw(img)
    color = MAGENTA if active else CYAN_SOFT
    d.polygon([(20, 58), (172, 58), (154, 72), (38, 72)], fill=(color[0], color[1], color[2], 100))
    return img


def background():
    img = gradient((512, 512), BG, (5, 13, 29, 255))
    d = ImageDraw.Draw(img)
    for radius, alpha in [(220, 35), (150, 48), (80, 65)]:
        d.ellipse((256 - radius, 256 - radius, 256 + radius, 256 + radius), outline=(0, 229, 255, alpha), width=1)
    rng = random.Random(44)
    for _ in range(38):
        x = rng.randrange(20, 492)
        y = rng.randrange(20, 492)
        length = rng.randrange(28, 90)
        if rng.random() < 0.5:
            pts = [(x, y), (min(511, x + length), y), (min(511, x + length + 18), min(511, y + 18))]
        else:
            pts = [(x, y), (x, min(511, y + length)), (min(511, x + 18), min(511, y + length + 18))]
        d.line(pts, fill=(0, 229, 255, rng.randrange(22, 60)), width=1)
    add(img, glow_line((512, 512), [(42, 420), (186, 340), (330, 356), (470, 236)], (180, 76, 255, 115), 2, 10))
    add(img, glow_line((512, 512), [(20, 110), (168, 88), (330, 122), (496, 70)], (0, 229, 255, 120), 2, 8))
    return img


def overlay(kind):
    img = rgba((256, 256))
    d = ImageDraw.Draw(img)
    if kind == "energy":
        for i in range(7):
            y = 24 + i * 33
            d.line((18, y, 238, y + ((i % 3) - 1) * 18), fill=(0, 229, 255, 42), width=2)
            d.ellipse((230, y - 4, 238, y + 4), fill=(255, 43, 214, 90))
    elif kind == "hologram":
        for r in range(30, 128, 24):
            d.ellipse((128 - r, 128 - r, 128 + r, 128 + r), outline=(0, 229, 255, 42), width=1)
        d.line((36, 188, 98, 92, 162, 118, 220, 54), fill=(180, 76, 255, 80), width=2)
    elif kind == "edge":
        d.rounded_rectangle((14, 14, 241, 241), radius=22, outline=(0, 229, 255, 190), width=4)
        d.rounded_rectangle((30, 30, 225, 225), radius=16, outline=(255, 43, 214, 80), width=2)
        img = img.filter(ImageFilter.GaussianBlur(1))
    elif kind == "glints":
        rng = random.Random(11)
        for _ in range(54):
            x = rng.randrange(16, 240)
            y = rng.randrange(16, 240)
            c = CYAN if rng.random() < 0.7 else MAGENTA
            d.line((x - 3, y, x + 3, y), fill=(c[0], c[1], c[2], 150), width=1)
            d.line((x, y - 3, x, y + 3), fill=(c[0], c[1], c[2], 150), width=1)
    elif kind == "locked":
        d.rounded_rectangle((40, 74, 216, 218), radius=20, fill=(6, 10, 18, 155), outline=(138, 175, 194, 120), width=3)
        d.arc((78, 36, 178, 142), 180, 360, fill=(138, 175, 194, 160), width=9)
    return img


def marker(kind):
    img = rgba((128, 128))
    d = ImageDraw.Draw(img)
    colors = {
        "signal": CYAN,
        "hazard": ERROR,
        "mission": MAGENTA,
        "nexus": VIOLET,
        "reclamation": SUCCESS,
    }
    c = colors[kind]
    d.ellipse((28, 28, 100, 100), fill=(c[0], c[1], c[2], 40), outline=(c[0], c[1], c[2], 220), width=3)
    if kind == "hazard":
        d.polygon([(64, 30), (100, 96), (28, 96)], outline=c, fill=(c[0], c[1], c[2], 52))
    elif kind == "mission":
        d.polygon([(64, 24), (102, 64), (64, 104), (26, 64)], outline=c, fill=(c[0], c[1], c[2], 55))
    else:
        d.rounded_rectangle((48, 48, 80, 80), radius=5, outline=c, fill=(0, 229, 255, 48), width=2)
    d.ellipse((56, 56, 72, 72), fill=(234, 251, 255, 220))
    return img.filter(ImageFilter.GaussianBlur(0.2))


def ring(size=(256, 256), variant="scan"):
    img = rgba(size)
    w, h = size
    d = ImageDraw.Draw(img)
    cx, cy = w // 2, h // 2
    r = min(w, h) // 2 - 18
    for offset, color, width in [(0, CYAN, 4), (-18, VIOLET, 2), (-36, MAGENTA, 1)]:
        d.arc((cx - r - offset, cy - r - offset, cx + r + offset, cy + r + offset), 18, 330, fill=color, width=width)
    for a in range(0, 360, 45):
        rad = math.radians(a)
        x1 = cx + math.cos(rad) * (r - 18)
        y1 = cy + math.sin(rad) * (r - 18)
        x2 = cx + math.cos(rad) * (r + 4)
        y2 = cy + math.sin(rad) * (r + 4)
        d.line((x1, y1, x2, y2), fill=(0, 229, 255, 140), width=2)
    if variant == "progress":
        d.arc((24, 24, w - 24, h - 24), 210, 342, fill=SUCCESS, width=7)
    return img


def target_box():
    img = rgba((256, 256))
    d = ImageDraw.Draw(img)
    c = CYAN
    for x, y, sx, sy in [(34, 34, 62, 34), (34, 34, 34, 62), (222, 34, 194, 34), (222, 34, 222, 62),
                         (34, 222, 62, 222), (34, 222, 34, 194), (222, 222, 194, 222), (222, 222, 222, 194)]:
        d.line((x, y, sx, sy), fill=c, width=4)
    d.rounded_rectangle((64, 76, 192, 180), radius=8, outline=(180, 76, 255, 130), width=2)
    return img


def route_line():
    img = rgba((256, 64))
    add(img, glow_line((256, 64), [(4, 42), (72, 18), (142, 30), (252, 12)], (0, 229, 255, 185), 4, 8))
    d = ImageDraw.Draw(img)
    for x, y in [(72, 18), (142, 30), (220, 18)]:
        d.ellipse((x - 4, y - 4, x + 4, y + 4), fill=MAGENTA)
    return img


def holomap_grid():
    img = rgba((512, 512))
    d = ImageDraw.Draw(img)
    for i in range(0, 512, 64):
        d.line((i, 0, i, 512), fill=(0, 229, 255, 48), width=1)
        d.line((0, i, 512, i), fill=(0, 229, 255, 48), width=1)
    for i in range(32, 512, 64):
        d.line((i, 0, i, 512), fill=(180, 76, 255, 24), width=1)
        d.line((0, i, 512, i), fill=(180, 76, 255, 24), width=1)
    add(img, glow_line((512, 512), [(48, 410), (180, 330), (300, 350), (470, 180)], (255, 43, 214, 95), 2, 7))
    return img


def sheet(kind):
    img = draw_panel((512, 512), alt=True)
    d = ImageDraw.Draw(img)
    colors = [CYAN, VIOLET, MAGENTA, SUCCESS]
    for row in range(4):
        for col in range(4):
            x = 38 + col * 112
            y = 38 + row * 112
            c = colors[(row + col) % len(colors)]
            d.rounded_rectangle((x, y, x + 76, y + 76), radius=8, fill=(c[0] // 8, c[1] // 8, c[2] // 8, 190), outline=c, width=2)
            d.line((x + 12, y + 56, x + 64, y + 20), fill=(234, 251, 255, 70), width=1)
    return img


def icon(name):
    img = rgba((128, 128))
    d = ImageDraw.Draw(img)
    d.ellipse((18, 18, 110, 110), fill=(0, 229, 255, 32), outline=(0, 229, 255, 200), width=3)
    d.ellipse((34, 34, 94, 94), outline=(180, 76, 255, 150), width=2)
    c = MAGENTA if name in {"nexus", "blackbox", "armory"} else CYAN
    if name in {"terminal", "core", "theme"}:
        d.rounded_rectangle((38, 42, 90, 82), radius=5, outline=c, width=4)
        d.line((48, 58, 62, 66, 48, 74), fill=TEXT, width=2)
        d.line((66, 74, 82, 74), fill=TEXT, width=2)
    elif name in {"holomap", "orbital", "convoy"}:
        d.arc((32, 32, 96, 96), 15, 315, fill=c, width=4)
        d.line((40, 82, 88, 44), fill=TEXT, width=2)
    elif name in {"lens", "runtime"}:
        d.ellipse((42, 42, 86, 86), outline=c, width=4)
        d.line((80, 80, 100, 100), fill=TEXT, width=3)
    elif name in {"missions", "index"}:
        d.polygon([(64, 30), (94, 64), (64, 98), (34, 64)], outline=c, fill=(c[0], c[1], c[2], 45))
    else:
        d.polygon([(64, 28), (96, 48), (96, 82), (64, 100), (32, 82), (32, 48)], outline=c, fill=(c[0], c[1], c[2], 45))
    return img


def render_reference(kind):
    img = draw_panel((512, 512), alt=True)
    d = ImageDraw.Draw(img)
    rng = random.Random(hash(kind) & 0xFFFF)
    if "particle" in kind:
        for _ in range(80):
            x, y = rng.randrange(44, 468), rng.randrange(44, 468)
            c = CYAN if rng.random() < 0.65 else MAGENTA
            d.rectangle((x, y, x + rng.randrange(2, 7), y + rng.randrange(2, 7)), fill=(c[0], c[1], c[2], rng.randrange(90, 230)))
    elif "multiblock" in kind or "route" in kind:
        for i in range(7):
            y = 72 + i * 56
            add(img, glow_line((512, 512), [(42, y), (150, y + 24), (280, y - 8), (470, y + 18)], (0, 229, 255, 130), 2, 8))
    elif "lens" in kind:
        add(img, ring((512, 512), "scan").resize((512, 512)))
    elif "terminal" in kind:
        d.rounded_rectangle((78, 96, 434, 380), radius=20, outline=CYAN, width=4, fill=(5, 12, 24, 130))
        for i in range(5):
            d.rounded_rectangle((112, 134 + i * 42, 400, 156 + i * 42), radius=4, fill=(0, 229, 255, 38), outline=(180, 76, 255, 90), width=1)
    elif "distortion" in kind:
        for i in range(11):
            pts = []
            for x in range(36, 476, 24):
                y = 80 + i * 32 + math.sin((x + i * 17) / 34) * 10
                pts.append((x, y))
            d.line(pts, fill=(180, 76, 255, 92), width=2)
    else:
        for r in range(48, 224, 34):
            d.ellipse((256 - r, 256 - r, 256 + r, 256 + r), outline=(0, 229, 255, 80), width=2)
        d.line((86, 332, 202, 188, 320, 236, 430, 118), fill=(255, 43, 214, 120), width=3)
    return img


def publishing(kind):
    size = (1600, 600) if "banner" in kind else (1920, 1080)
    if "mobile" in kind:
        size = (1080, 1920)
    if "card" in kind:
        size = (1024, 1024)
    img = background().resize(size)
    overlay_img = draw_panel((int(size[0] * 0.66), int(size[1] * 0.42)), alt=True, selected=True)
    img.alpha_composite(overlay_img, (int(size[0] * 0.08), int(size[1] * 0.16)))
    d = ImageDraw.Draw(img)
    try:
        font_big = ImageFont.truetype("arial.ttf", max(42, size[0] // 18))
        font_small = ImageFont.truetype("arial.ttf", max(24, size[0] // 34))
    except OSError:
        font_big = ImageFont.load_default()
        font_small = ImageFont.load_default()
    x = int(size[0] * 0.12)
    y = int(size[1] * 0.25)
    d.text((x, y), "ECHO THEMECORE", fill=TEXT, font=font_big)
    d.text((x, y + int(size[1] * 0.10)), "CYBERGLASS", fill=CYAN, font=font_small)
    return img


def generate():
    random.seed(7)
    save(background(), "background.png")
    save(draw_panel(), "glass_panel.png")
    save(draw_panel(alt=True), "glass_panel_alt.png")
    save(button(), "glass_button.png")
    save(button(hover=True), "glass_button_hover.png")
    save(tab(), "tab.png")
    save(tab(active=True), "tab_active.png")
    save(draw_panel((320, 180), alt=True), "mission_card.png")
    save(draw_panel((320, 180), alt=True, selected=True), "mission_card_selected.png")
    save(draw_panel((192, 64), alt=True), "status_chip.png")
    save(draw_panel((256, 48), alt=True, selected=True), "progress_bar.png")
    save(draw_panel((48, 256), alt=True), "scrollbar.png")
    save(overlay("hologram"), "hologram_overlay.png")
    save(overlay("energy"), "energy_overlay.png")
    save(overlay("edge"), "edge_glow.png")
    save(overlay("glints"), "particle_glints.png")
    save(overlay("locked"), "locked_overlay.png")
    save(holomap_grid(), "holomap_grid.png")
    save(draw_panel((384, 256), alt=True), "holomap_panel.png")
    for item in ["signal", "hazard", "mission", "nexus", "reclamation"]:
        save(marker(item), f"marker_{item}.png")
    save(route_line(), "route_line.png")
    save(ring((256, 256)), "selected_marker_ring.png")
    save(overlay("hologram"), "region_scan_sweep.png")
    save(ring((256, 256)), "lens_scan_ring.png")
    save(target_box(), "lens_target_box.png")
    save(overlay("locked"), "lens_warning_overlay.png")
    save(overlay("energy"), "lens_anomaly_overlay.png")
    save(marker("hazard"), "lens_weakpoint_marker.png")
    save(ring((256, 256), "progress"), "lens_progress_arc.png")
    save(overlay("glints"), "lens_noise_overlay.png")
    save(draw_panel((512, 256), alt=True, selected=True), "vanilla_title_backplate.png")
    save(draw_panel((384, 384), alt=True), "vanilla_pause_panel.png")
    save(draw_panel((512, 384), alt=True), "vanilla_inventory_frame.png")
    save(draw_panel((512, 384), alt=True, selected=True), "vanilla_container_frame.png")
    save(draw_panel((256, 128), alt=True), "vanilla_tooltip_panel.png")
    save(draw_panel((256, 64), alt=True, selected=True), "vanilla_toast_accent.png")
    save(draw_panel((512, 64), alt=True), "vanilla_hotbar_accent.png")
    save(draw_panel((512, 48), alt=True, selected=True), "vanilla_boss_bar_accent.png")
    save(overlay("edge"), "vanilla_widget_outline.png")
    for item in [
        "core", "missions", "holomap", "lens", "index", "terminal", "nexus", "orbital",
        "convoy", "industrial", "reclamation", "blackbox", "armory", "runtime", "sound", "theme",
    ]:
        save(icon(item), f"icons/icon_{item}.png")
    for item in [
        "hologram_style_reference", "particle_style_reference", "glow_overlay_reference",
        "distortion_overlay", "entity_highlight_reference", "multiblock_energy_lines",
        "terminal_boot_effect_reference", "lens_scan_effect_reference", "holomap_route_effect_reference",
    ]:
        save(render_reference(item), f"rendercore/{item}.png")
    save(sheet("block"), "block_palette_sheet.png")
    save(sheet("panel"), "panel_variants_sheet.png")
    save(sheet("glass"), "glass_variants_sheet.png")
    save(sheet("light"), "light_strip_variants_sheet.png")
    for item in ["theme_banner", "theme_desktop_wallpaper", "theme_mobile_wallpaper", "theme_overview_card", "theme_feature_sheet"]:
        save(publishing(item), f"{item}.png")


if __name__ == "__main__":
    generate()
    print(f"Generated CyberGlass assets in {OUT}")
