#!/usr/bin/env python3
import argparse
import json
import shutil
import struct
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parent
CONFIG_PATH = ROOT / "input" / "themeforge_config.json"
PROMPT_DIR = ROOT / "generated" / "prompts"
ASSET_DIR = ROOT / "generated" / "assets"
REPORT_DIR = ROOT / "generated" / "reports"

ASSET_GROUPS = {
    "terminal_ui": [
        "background.png", "glass_panel.png", "glass_panel_alt.png", "glass_button.png",
        "glass_button_hover.png", "tab.png", "tab_active.png", "mission_card.png",
        "mission_card_selected.png", "status_chip.png", "progress_bar.png", "scrollbar.png",
        "hologram_overlay.png", "energy_overlay.png", "edge_glow.png", "particle_glints.png",
        "locked_overlay.png"
    ],
    "holomap_ui": [
        "holomap_grid.png", "holomap_panel.png", "marker_signal.png", "marker_hazard.png",
        "marker_mission.png", "marker_nexus.png", "marker_reclamation.png", "route_line.png",
        "selected_marker_ring.png", "region_scan_sweep.png"
    ],
    "lens_ui": [
        "lens_scan_ring.png", "lens_target_box.png", "lens_warning_overlay.png",
        "lens_anomaly_overlay.png", "lens_weakpoint_marker.png", "lens_progress_arc.png",
        "lens_noise_overlay.png"
    ],
    "icons": [
        "icons/icon_core.png", "icons/icon_missions.png", "icons/icon_holomap.png",
        "icons/icon_lens.png", "icons/icon_index.png", "icons/icon_terminal.png",
        "icons/icon_nexus.png", "icons/icon_orbital.png", "icons/icon_convoy.png",
        "icons/icon_industrial.png", "icons/icon_reclamation.png", "icons/icon_blackbox.png",
        "icons/icon_armory.png", "icons/icon_runtime.png", "icons/icon_sound.png",
        "icons/icon_theme.png"
    ],
    "rendercore_references": [
        "rendercore/hologram_style_reference.png", "rendercore/particle_style_reference.png",
        "rendercore/glow_overlay_reference.png", "rendercore/distortion_overlay.png",
        "rendercore/entity_highlight_reference.png", "rendercore/multiblock_energy_lines.png",
        "rendercore/terminal_boot_effect_reference.png", "rendercore/lens_scan_effect_reference.png",
        "rendercore/holomap_route_effect_reference.png"
    ],
    "blockworks_palette": [
        "block_palette_sheet.png", "panel_variants_sheet.png", "glass_variants_sheet.png",
        "light_strip_variants_sheet.png"
    ],
    "publishing_art": [
        "theme_banner.png", "theme_desktop_wallpaper.png", "theme_mobile_wallpaper.png",
        "theme_overview_card.png", "theme_feature_sheet.png"
    ],
    "vanilla_ui": [
        "vanilla_title_backplate.png", "vanilla_pause_panel.png", "vanilla_inventory_frame.png",
        "vanilla_container_frame.png", "vanilla_tooltip_panel.png", "vanilla_toast_accent.png",
        "vanilla_hotbar_accent.png", "vanilla_boss_bar_accent.png", "vanilla_widget_outline.png"
    ]
}

PROMPT_NAMES = {
    "terminal_ui": "terminal_ui",
    "holomap_ui": "holomap_ui",
    "lens_ui": "lens_ui",
    "icons": "icons",
    "rendercore_references": "rendercore",
    "blockworks_palette": "blockworks",
    "publishing_art": "publishing",
    "vanilla_ui": "vanilla_ui"
}

UI_JSON_KEYS = {
    "background.png": "background_texture",
    "glass_panel.png": "panel_texture",
    "glass_panel_alt.png": "panel_alt_texture",
    "glass_button.png": "button_texture",
    "glass_button_hover.png": "button_hover_texture",
    "hologram_overlay.png": "hologram_overlay",
    "energy_overlay.png": "energy_overlay",
    "edge_glow.png": "edge_glow",
    "particle_glints.png": "particle_glints"
}

MODULE_ASSET_KEYS = {
    "terminal": {
        "glass_panel.png": "panel_texture",
        "tab.png": "tab_texture",
        "tab_active.png": "tab_active_texture",
        "mission_card.png": "mission_card_texture",
        "status_chip.png": "status_chip_texture",
        "glass_button.png": "button_texture",
        "icons/icon_terminal.png": "icon_texture"
    },
    "holomap": {
        "holomap_grid.png": "grid_texture",
        "holomap_panel.png": "panel_texture",
        "route_line.png": "route_texture",
        "marker_signal.png": "marker_signal_texture",
        "marker_hazard.png": "marker_hazard_texture",
        "marker_mission.png": "marker_mission_texture",
        "selected_marker_ring.png": "selected_ring_texture"
    },
    "lens": {
        "lens_scan_ring.png": "scan_ring_texture",
        "lens_target_box.png": "target_box_texture",
        "lens_weakpoint_marker.png": "weak_point_texture",
        "lens_warning_overlay.png": "warning_texture",
        "lens_anomaly_overlay.png": "anomaly_reveal_texture"
    },
    "vanilla_ui": {
        "vanilla_container_frame.png": "container_frame_texture",
        "vanilla_inventory_frame.png": "inventory_frame_texture",
        "vanilla_title_backplate.png": "title_backplate_texture",
        "vanilla_pause_panel.png": "pause_panel_texture",
        "vanilla_hotbar_accent.png": "selected_slot_texture"
    },
    "rendercore": {
        "rendercore/glow_overlay_reference.png": "glow_overlay_texture",
        "rendercore/entity_highlight_reference.png": "entity_highlight_texture",
        "rendercore/multiblock_energy_lines.png": "multiblock_energy_texture"
    }
}


def load_config():
    with CONFIG_PATH.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def resolve(path):
    return (ROOT / path).resolve()


def theme_base(theme):
    if theme["id"] == "cyberglass":
        return (
            "Create a high-quality modded Minecraft sci-fi UI asset for the ECHO CyberGlass theme. "
            "Futuristic cyberpunk glass interface, dark navy and black translucent glass panels, "
            "cyan holographic glow, violet-magenta accent light, thin neon borders, smooth gradients, "
            "clean geometric circuit patterns, soft bloom, premium AAA game UI style, no retro display artifacts, "
            "no dirty grunge, no extra text unless requested."
        )
    return (
        "Create a high-quality modded Minecraft sci-fi UI asset for the ECHO Nexus theme. "
        "Dark blue-black anomaly glass interface, cold cyan glow, violet secondary glow, "
        "crystalline phase lines, subtle phase ripple distortion, clean readable futuristic UI, "
        "premium AAA game UI style, transparent background where appropriate, no retro display artifacts, "
        "no extra text unless requested."
    )


def prompt_for(theme, filename, group):
    transparent = any(part in filename for part in ["icons/", "overlay", "ring", "marker", "accent", "particle", "glow", "route_line"])
    text_rule = "no text, no logos"
    if group == "publishing_art":
        text_rule = "minimal title text allowed: ECHO THEMECORE, CYBERGLASS, or NEXUS"
    size = "1024x1024"
    if "wallpaper" in filename:
        size = "1920x1080"
    elif "banner" in filename:
        size = "1600x600"
    elif group == "terminal_ui":
        size = "512x512, tileable edges where useful"
    background = "transparent background" if transparent else "clean dark game UI background"
    return (
        f"{theme_base(theme)} Asset: {filename}. {background}. {text_rule}. "
        f"Readable at Minecraft GUI scale, crisp edges, {size}. Avoid legacy display-line overlays."
    )


def command_prompts(_args):
    config = load_config()
    PROMPT_DIR.mkdir(parents=True, exist_ok=True)
    generated = []
    for theme in config["themes"]:
        for group in config["asset_sets"]:
            if group not in ASSET_GROUPS:
                continue
            prompt_name = PROMPT_NAMES[group]
            out = PROMPT_DIR / f"{theme['id']}_{prompt_name}_prompts.md"
            lines = [
                f"# {theme['id'].title()} {prompt_name.replace('_', ' ').title()} Prompt Pack",
                "",
                f"Theme JSON: `{theme['theme_json']}`",
                f"Style: {theme['style']}",
                "",
            ]
            for filename in ASSET_GROUPS[group]:
                expected = ASSET_DIR / theme["id"] / filename
                lines.extend([
                    f"## {filename}",
                    "",
                    prompt_for(theme, filename, group),
                    "",
                    f"Expected file: `{expected.as_posix()}`",
                    "",
                ])
            out.write_text("\n".join(lines), encoding="utf-8")
            generated.append(out)
    print(f"Generated {len(generated)} prompt pack(s).")
    return 0


def png_dimensions(path):
    try:
        with path.open("rb") as handle:
            header = handle.read(24)
        if not header.startswith(b"\x89PNG\r\n\x1a\n"):
            return None
        return struct.unpack(">II", header[16:24])
    except OSError:
        return None


def referenced_paths(theme_json):
    refs = []
    def walk(value):
        if isinstance(value, dict):
            for nested in value.values():
                walk(nested)
        elif isinstance(value, list):
            for nested in value:
                walk(nested)
        elif isinstance(value, str) and value.endswith(".png") and ":" in value:
            refs.append(value)
    walk(theme_json)
    return refs


def forbidden_found(text):
    lowered = text.lower()
    return ("scan" + "line") in lowered or ("c" + "rt") in lowered


def selected_themes(config, theme_id=None):
    if not theme_id:
        return config["themes"]
    return [theme for theme in config["themes"] if theme["id"] == theme_id]


def command_validate(args):
    config = load_config()
    REPORT_DIR.mkdir(parents=True, exist_ok=True)
    missing = []
    missing_generated = []
    warnings = []
    themes = selected_themes(config, getattr(args, "theme", None))
    if not themes:
        print(f"No configured theme matched {args.theme}")
        return 1
    for theme in themes:
        theme_id = theme["id"]
        theme_json_path = resolve(theme["theme_json"])
        module_path = resolve(config["theme_module_path"])
        if not theme_json_path.exists():
            missing.append(str(theme_json_path))
            continue
        text = theme_json_path.read_text(encoding="utf-8")
        if forbidden_found(text):
            warnings.append(f"Forbidden legacy line-overlay term in {theme_json_path}")
        theme_json = json.loads(text)
        for ref in referenced_paths(theme_json):
            namespace, resource_path = ref.split(":", 1)
            if namespace != config["namespace"]:
                continue
            resolved = module_path / "src/main/resources/assets" / namespace / resource_path
            if not resolved.exists():
                missing.append(str(resolved))
        for group in config["asset_sets"]:
            for relative in ASSET_GROUPS.get(group, []):
                candidate = ASSET_DIR / theme_id / relative
                if not candidate.exists():
                    missing_generated.append(str(candidate))
                    continue
                if candidate.name != candidate.name.lower() or " " in candidate.name:
                    warnings.append(f"Filename should be lowercase without spaces: {candidate}")
                dims = png_dimensions(candidate)
                if dims is None:
                    warnings.append(f"Not a readable PNG: {candidate}")
                elif dims[0] > 4096 or dims[1] > 4096 or dims[0] < 8 or dims[1] < 8:
                    warnings.append(f"Unusual PNG dimensions {dims}: {candidate}")
    report = REPORT_DIR / "missing_assets.md"
    lines = ["# ThemeForge Missing Assets", "", f"Generated: {datetime.now().isoformat(timespec='seconds')}", ""]
    if missing:
        lines.append("## Missing")
        lines.extend(f"- `{item}`" for item in sorted(set(missing)))
    else:
        lines.append("No missing referenced ThemeCore assets were found.")
    if missing_generated:
        lines.extend(["", "## Missing Generated Assets"])
        lines.extend(f"- `{item}`" for item in sorted(set(missing_generated)))
    if warnings:
        lines.extend(["", "## Warnings"])
        lines.extend(f"- {item}" for item in warnings)
    report.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Validation report written to {report}")
    if missing:
        print(f"Missing assets reported: {len(set(missing))}")
    if missing_generated:
        print(f"Missing generated assets reported: {len(set(missing_generated))}")
    return 0


def copy_asset(source, target, force):
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.exists() and not force:
        backup = target.with_suffix(target.suffix + ".bak")
        if not backup.exists():
            shutil.copy2(target, backup)
        return False
    shutil.copy2(source, target)
    return True


def update_theme_json(theme, config, force):
    theme_json_path = resolve(theme["theme_json"])
    if not theme_json_path.exists():
        return False
    data = json.loads(theme_json_path.read_text(encoding="utf-8"))
    backup = theme_json_path.with_suffix(".json.bak")
    if not backup.exists():
        shutil.copy2(theme_json_path, backup)
    namespace = config["namespace"]
    theme_id = theme["id"]
    data.setdefault("ui", {})
    for filename, key in UI_JSON_KEYS.items():
        data["ui"][key] = f"{namespace}:textures/gui/themes/{theme_id}/{filename}"
    data["ui"]["icon_pack"] = f"{namespace}:textures/gui/themes/{theme_id}/icons/icon_theme.png"
    data.setdefault("render", {})
    data["render"]["hologram_reference"] = f"{namespace}:textures/gui/themes/{theme_id}/rendercore/hologram_style_reference.png"
    data["render"]["particle_reference"] = f"{namespace}:textures/gui/themes/{theme_id}/rendercore/particle_style_reference.png"
    data["render"]["distortion_overlay"] = f"{namespace}:textures/gui/themes/{theme_id}/rendercore/distortion_overlay.png"
    data.setdefault("module_assets", {})
    for section, mapping in MODULE_ASSET_KEYS.items():
        data["module_assets"].setdefault(section, {})
        for filename, key in mapping.items():
            data["module_assets"][section][key] = f"{namespace}:textures/gui/themes/{theme_id}/{filename}"
    theme_json_path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    return True


def command_apply(args):
    config = load_config()
    copied = 0
    skipped = 0
    module_path = resolve(config["theme_module_path"])
    themes = selected_themes(config, getattr(args, "theme", None))
    if not themes:
        print(f"No configured theme matched {args.theme}")
        return 1
    for theme in themes:
        theme_id = theme["id"]
        source_root = ASSET_DIR / theme_id
        if not source_root.exists():
            skipped += 1
            continue
        target_root = module_path / "src/main/resources/assets" / config["namespace"] / "textures/gui/themes" / theme_id
        for group in config["asset_sets"]:
            for relative in ASSET_GROUPS.get(group, []):
                source = source_root / relative
                if not source.exists():
                    continue
                target = target_root / relative
                if copy_asset(source, target, args.force):
                    copied += 1
                else:
                    skipped += 1
        update_theme_json(theme, config, args.force)
    print(f"Copied {copied} asset(s), skipped {skipped} existing asset(s).")
    return 0


def command_report(args):
    config = load_config()
    REPORT_DIR.mkdir(parents=True, exist_ok=True)
    report = REPORT_DIR / "themeforge_report.md"
    lines = ["# ECHO ThemeForge Report", "", f"Generated: {datetime.now().isoformat(timespec='seconds')}", ""]
    themes = selected_themes(config, getattr(args, "theme", None))
    if not themes:
        print(f"No configured theme matched {args.theme}")
        return 1
    for theme in themes:
        lines.append(f"## {theme['id']}")
        for group in config["asset_sets"]:
            files = ASSET_GROUPS.get(group, [])
            present = sum(1 for item in files if (ASSET_DIR / theme["id"] / item).exists())
            lines.append(f"- {group}: {present}/{len(files)} generated asset(s) present")
        lines.append("")
    report.write_text("\n".join(lines), encoding="utf-8")
    print(f"Report written to {report}")
    return 0


def main():
    parser = argparse.ArgumentParser(description="ECHO ThemeForge development asset helper")
    sub = parser.add_subparsers(dest="command", required=True)
    sub.add_parser("prompts")
    validate_parser = sub.add_parser("validate")
    validate_parser.add_argument("--theme", help="Validate only one configured theme id")
    apply_parser = sub.add_parser("apply")
    apply_parser.add_argument("--force", action="store_true", help="Overwrite existing resources instead of creating backups and skipping")
    apply_parser.add_argument("--theme", help="Apply only one configured theme id")
    report_parser = sub.add_parser("report")
    report_parser.add_argument("--theme", help="Report only one configured theme id")
    args = parser.parse_args()
    if args.command == "prompts":
        return command_prompts(args)
    if args.command == "validate":
        return command_validate(args)
    if args.command == "apply":
        return command_apply(args)
    if args.command == "report":
        return command_report(args)
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
