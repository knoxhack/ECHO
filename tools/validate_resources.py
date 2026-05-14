#!/usr/bin/env python3
"""Validate ECHO asset, localization, and packet ID references.

This intentionally checks the failure modes that showed up in packaged-client
logs: missing item/block model parents, missing texture references, raw
advancement/entity translation keys, and invalid custom packet namespaces.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import sys
import zipfile
from pathlib import Path, PurePosixPath
from typing import Any, Iterable

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
MODS = (
    ("echocore", ROOT / "core/echocore/src/main/resources", ROOT / "core/echocore/src/main/java/com/knoxhack/echocore"),
    (
        "echonetcore",
        ROOT / "addons/echonetcore/src/main/resources",
        ROOT / "addons/echonetcore/src/main/java/com/knoxhack/echonetcore",
    ),
    (
        "echoruntimeguard",
        ROOT / "addons/echoruntimeguard/src/main/resources",
        ROOT / "addons/echoruntimeguard/src/main/java/com/knoxhack/echoruntimeguard",
    ),
    (
        "echothemecore",
        ROOT / "addons/echothemecore/src/main/resources",
        ROOT / "addons/echothemecore/src/main/java/com/knoxhack/echothemecore",
    ),
    (
        "echoplayercore",
        ROOT / "addons/echoplayercore/src/main/resources",
        ROOT / "addons/echoplayercore/src/main/java/com/knoxhack/echoplayercore",
    ),
    (
        "echomissioncore",
        ROOT / "addons/echomissioncore/src/main/resources",
        ROOT / "addons/echomissioncore/src/main/java/com/knoxhack/echomissioncore",
    ),
    (
        "echodatacore",
        ROOT / "addons/echodatacore/src/main/resources",
        ROOT / "addons/echodatacore/src/main/java/com/knoxhack/echodatacore",
    ),
    (
        "echoworldcore",
        ROOT / "addons/echoworldcore/src/main/resources",
        ROOT / "addons/echoworldcore/src/main/java/com/knoxhack/echoworldcore",
    ),
    (
        "echoterminal",
        ROOT / "addons/echoterminal/src/main/resources",
        ROOT / "addons/echoterminal/src/main/java/com/knoxhack/echoterminal",
    ),
    (
        "signalos",
        ROOT / "addons/echosignalos/src/main/resources",
        ROOT / "addons/echosignalos/src/main/java/com/knoxhack/signalos",
    ),
    (
        "signalosexample",
        ROOT / "addons/signalosexample/src/main/resources",
        ROOT / "addons/signalosexample/src/main/java/com/knoxhack/signalosexample",
    ),
    (
        "echorendercore",
        ROOT / "addons/echorendercore/src/main/resources",
        ROOT / "addons/echorendercore/src/main/java/com/knoxhack/echorendercore",
    ),
    ("echoashfallprotocol", ROOT / "src/main/resources", ROOT / "src/main/java/com/knoxhack/echoashfallprotocol"),
    (
        "echoorbitalremnants",
        ROOT / "addons/echoorbitalremnants/src/main/resources",
        ROOT / "addons/echoorbitalremnants/src/main/java/com/knoxhack/echoorbitalremnants",
    ),
    (
        "echonexusprotocol",
        ROOT / "addons/echonexusprotocol/src/main/resources",
        ROOT / "addons/echonexusprotocol/src/main/java/com/knoxhack/echonexusprotocol",
    ),
    (
        "echoagriculturereclamation",
        ROOT / "addons/echoagriculturereclamation/src/main/resources",
        ROOT / "addons/echoagriculturereclamation/src/main/java/com/knoxhack/echoagriculturereclamation",
    ),
    (
        "echoblackboxprotocol",
        ROOT / "addons/echoblackboxprotocol/src/main/resources",
        ROOT / "addons/echoblackboxprotocol/src/main/java/com/knoxhack/echoblackboxprotocol",
    ),
    (
        "echoindustrialnexus",
        ROOT / "addons/echoindustrialnexus/src/main/resources",
        ROOT / "addons/echoindustrialnexus/src/main/java/com/knoxhack/echoindustrialnexus",
    ),
    (
        "echologisticsnetwork",
        ROOT / "addons/echologisticsnetwork/src/main/resources",
        ROOT / "addons/echologisticsnetwork/src/main/java/com/knoxhack/echologisticsnetwork",
    ),
    (
        "echoconvoyprotocol",
        ROOT / "addons/echoconvoyprotocol/src/main/resources",
        ROOT / "addons/echoconvoyprotocol/src/main/java/com/knoxhack/echoconvoyprotocol",
    ),
    (
        "echoholomap",
        ROOT / "addons/echoholomap/src/main/resources",
        ROOT / "addons/echoholomap/src/main/java/com/knoxhack/echoholomap",
    ),
    (
        "echoindex",
        ROOT / "addons/echoindex/src/main/resources",
        ROOT / "addons/echoindex/src/main/java/com/knoxhack/echoindex",
    ),
    (
        "echostationfall",
        ROOT / "addons/echostationfall/src/main/resources",
        ROOT / "addons/echostationfall/src/main/java/com/knoxhack/echostationfall",
    ),
    (
        "echoarmory",
        ROOT / "addons/echoarmory/src/main/resources",
        ROOT / "addons/echoarmory/src/main/java/com/knoxhack/echoarmory",
    ),
    (
        "echolens",
        ROOT / "addons/echolens/src/main/resources",
        ROOT / "addons/echolens/src/main/java/com/knoxhack/echolens",
    ),
    (
        "echomultiblockcore",
        ROOT / "addons/echomultiblockcore/src/main/resources",
        ROOT / "addons/echomultiblockcore/src/main/java/com/knoxhack/echomultiblockcore",
    ),
    (
        "echoblockworks",
        ROOT / "addons/echoblockworks/src/main/resources",
        ROOT / "addons/echoblockworks/src/main/java/com/knoxhack/echoblockworks",
    ),
    (
        "echopowergrid",
        ROOT / "addons/echopowergrid/src/main/resources",
        ROOT / "addons/echopowergrid/src/main/java/com/knoxhack/echopowergrid",
    ),
    (
        "echosoundcore",
        ROOT / "addons/echosoundcore/src/main/resources",
        ROOT / "addons/echosoundcore/src/main/java/com/knoxhack/echosoundcore",
    ),
    (
        "echotutorialcore",
        ROOT / "addons/echotutorialcore/src/main/resources",
        ROOT / "addons/echotutorialcore/src/main/java/com/knoxhack/echotutorialcore",
    ),
    (
        "echorelictech",
        ROOT / "addons/echorelictech/src/main/resources",
        ROOT / "addons/echorelictech/src/main/java/com/knoxhack/echorelictech",
    ),
    (
        "echoweathercore",
        ROOT / "addons/echoweathercore/src/main/resources",
        ROOT / "addons/echoweathercore/src/main/java/com/knoxhack/echoweathercore",
    ),
)
BETA_MOD_IDS = {
    "echocore",
    "echonetcore",
    "echoruntimeguard",
    "echothemecore",
    "echoplayercore",
    "echoterminal",
    "echomissioncore",
    "echodatacore",
    "signalos",
    "signalosexample",
    "echorendercore",
    "echoashfallprotocol",
    "echoorbitalremnants",
    "echonexusprotocol",
    "echoagriculturereclamation",
    "echoworldcore",
    "echomultiblockcore",
    "echoblockworks",
}
ACTIVE_MOD_IDS = {modid for modid, _, _ in MODS}
ADDON_DIR_TO_MODID = {
    "echosignalos": "signalos",
}
BUILD_TRUTH_DOCS = (
    ROOT / "README.md",
    ROOT / "MODPACK_OVERVIEW.md",
    ROOT / "wiki/Modules-and-Versions.md",
    ROOT / "docs/FULL_GRADLE_STACK.md",
)
MODULE_ID_DOCS = BUILD_TRUTH_DOCS + (
    ROOT / "docs/chapter_handoff_ids.md",
)
REQUIRED_NAMING_PHRASES = (
    "Ashfall is the modpack",
    "ECHO is the ecosystem",
    "ECHO: Ashfall Protocol",
    "echoashfallprotocol",
)
REQUIRED_LOCAL_TOOLING_PHRASES = (
    "addons/echomodpackcommandcenter",
    "tools/echo-release-terminal",
    "not an active mod artifact",
)
STALE_DOC_TOKENS = (
    "echoagriculturereclamation` | `0.1.0",
    "echoagriculturereclamation-0.1.0.jar",
    "Axes of Tomorrow",
)
SKIPPED_SCAN_DIRS = {".git", ".gradle", "build", "run", "__pycache__"}
TEXT_SCAN_SUFFIXES = {".gradle", ".java", ".json", ".md", ".properties", ".py", ".toml", ".yaml", ".yml"}
RELEASE_POLISH_SCAN_ROOTS = (
    ROOT / "src/main/java",
    ROOT / "src/main/resources",
    ROOT / "core/echocore/src/main",
    ROOT / "addons/echonetcore/src/main",
    ROOT / "addons/echothemecore/src/main",
    ROOT / "addons/echoplayercore/src/main",
    ROOT / "addons/echomissioncore/src/main",
    ROOT / "addons/echodatacore/src/main",
    ROOT / "addons/echoworldcore/src/main",
    ROOT / "addons/echoterminal/src/main",
    ROOT / "addons/echosignalos/src/main",
    ROOT / "addons/signalosexample/src/main",
    ROOT / "addons/echorendercore/src/main",
    ROOT / "addons/echoorbitalremnants/src/main",
    ROOT / "addons/echonexusprotocol/src/main",
    ROOT / "addons/echoagriculturereclamation/src/main",
    ROOT / "addons/echostationfall/src/main",
    ROOT / "addons/echoblackboxprotocol/src/main",
    ROOT / "addons/echoindustrialnexus/src/main",
    ROOT / "addons/echologisticsnetwork/src/main",
    ROOT / "addons/echoconvoyprotocol/src/main",
    ROOT / "addons/echoholomap/src/main",
    ROOT / "addons/echoindex/src/main",
    ROOT / "addons/echoarmory/src/main",
    ROOT / "addons/echolens/src/main",
    ROOT / "addons/echomultiblockcore/src/main",
    ROOT / "addons/echoblockworks/src/main",
    ROOT / "addons/echopowergrid/src/main",
    ROOT / "addons/echosoundcore/src/main",
    ROOT / "addons/echotutorialcore/src/main",
    ROOT / "addons/echorelictech/src/main",
    ROOT / "addons/echoweathercore/src/main",
    ROOT / ".github",
    ROOT / "README.md",
    ROOT / "GETTING_STARTED.md",
    ROOT / "MODPACK_OVERVIEW.md",
    ROOT / "PROCEDURAL_STRUCTURES.md",
    ROOT / "build.gradle",
    ROOT / "settings.gradle",
    ROOT / "gradle.properties",
)
MOJIBAKE_CHARS = {
    "\u00c2": "U+00C2",
    "\u00c3": "U+00C3",
    "\u00e2": "U+00E2",
    "\ufffd": "U+FFFD",
}
PLACEHOLDER_MARKERS = (
    "stub implementation",
    "legacy placeholder",
    "structure placeholder",
    "placeholder generator",
    "placeholder approach",
    "manually replaced",
    "not implemented yet",
)
STALE_RELEASE_TOKENS = (
    "data/echoashfallprotocol/structures",
    "data\\echoashfallprotocol\\structures",
    "echoashfallprotocol:echo_terminal",
    "DroneMenu",
    "DRONE_MENU",
)
MODID_CONSTANTS = {
    "EchoCore.MODID": "echocore",
    "EchoNetCore.MODID": "echonetcore",
    "EchoThemeCore.MODID": "echothemecore",
    "EchoPlayerCore.MODID": "echoplayercore",
    "EchoMissionCore.MODID": "echomissioncore",
    "EchoDataCore.MODID": "echodatacore",
    "EchoWorldCore.MODID": "echoworldcore",
    "EchoTerminal.MODID": "echoterminal",
    "SignalOS.MODID": "signalos",
    "SignalOsExample.MODID": "signalosexample",
    "EchoAshfallProtocol.MODID": "echoashfallprotocol",
    "EchoOrbitalRemnants.MODID": "echoorbitalremnants",
    "EchoNexusProtocol.MODID": "echonexusprotocol",
    "EchoAgricultureReclamation.MODID": "echoagriculturereclamation",
    "EchoBlackboxProtocol.MODID": "echoblackboxprotocol",
    "EchoIndustrialNexus.MODID": "echoindustrialnexus",
    "EchoLogisticsNetwork.MODID": "echologisticsnetwork",
    "EchoConvoyProtocol.MODID": "echoconvoyprotocol",
    "EchoHoloMap.MODID": "echoholomap",
    "EchoIndex.MODID": "echoindex",
    "EchoStationfall.MODID": "echostationfall",
    "EchoArmory.MODID": "echoarmory",
    "EchoLens.MODID": "echolens",
    "EchoMultiblockCore.MODID": "echomultiblockcore",
    "EchoBlockworks.MODID": "echoblockworks",
    "EchoPowerGrid.MODID": "echopowergrid",
    "EchoSoundCore.MODID": "echosoundcore",
    "EchoTutorialCore.MODID": "echotutorialcore",
    "EchoRelicTech.MODID": "echorelictech",
    "EchoWeatherCore.MODID": "echoweathercore",
}
ALLOWED_PACKET_NAMESPACE_CONSTANTS = {
    ("echonetcore", "EchoCore.MODID"),
}
TEXTURE_QUALITY_MOD_IDS = {
    "echoashfallprotocol",
    "echoorbitalremnants",
    "echoagriculturereclamation",
    "echoblackboxprotocol",
    "echoindustrialnexus",
    "echologisticsnetwork",
    "echoconvoyprotocol",
    "echonexusprotocol",
    "signalos",
    "signalosexample",
    "echostationfall",
    "echoterminal",
    "echoarmory",
    "echoblockworks",
}
LOW_DETAIL_BLOCK_EXEMPTIONS = {
    "ash_layer",
    "drop_pod_glass",
    "toxic_puddle",
}
ALLOWED_PIXEL_TEXTURE_SIZES = {
    (16, 16),
    (16, 32),
    (16, 48),
    (16, 64),
    (16, 80),
    (16, 96),
    (144, 144),
}
REQUIRED_16X16_ITEM_TEXTURE_MOD_IDS = {
    "echoblackboxprotocol",
    "echonexusprotocol",
    "echostationfall",
}
REQUIRED_16X16_BLOCK_TEXTURES = {
    "acid_mud",
    "acidic_sludge",
    "ash_bush",
    "ash_layer",
    "ash_stone",
    "ashen_wasteland_dirt",
    "burnt_fern",
    "burnt_wasteland_soil",
    "concrete_chunk",
    "concrete_rubble",
    "contaminated_soil",
    "cracked_asphalt",
    "cracked_earth",
    "crash_slag",
    "cryogenic_fractured_stone",
    "debris_block",
    "deep_ash",
    "fallout_dust",
    "industrial_aggregate",
    "irradiated_cactus",
    "irradiated_crust",
    "mutated_leaves_gray",
    "mutated_leaves_purple",
    "mutated_wasteland_grass_block",
    "nexus_cracked_soil",
    "oil_stained_concrete",
    "permafrost",
    "radioactive_sludge",
    "riftstone",
    "rubble",
    "rusted_metal_debris",
    "rusted_metal_sheet",
    "rusty_wheat",
    "scorched_ash",
    "toxic_moss",
    "toxic_puddle",
    "toxic_slagstone",
    "toxic_wasteland_grass_block",
    "twisted_metal",
    "wasteland_dirt",
    "wasteland_grass",
    "wasteland_grass_block",
    "wasteland_reed",
    "wasteland_stone",
    "wasteland_tall_grass",
    "wasteland_trace_rubble",
}
TERMINAL_GUI_TEXTURE_SIZES = {
    (1920, 1080),
    (2048, 1024),
    (1024, 512),
    (512, 256),
}
TERMINAL_REQUIRED_GUI_TEXTURES = {
    "terminal_frame_backdrop.png",
    "overview_protocol_dashboard.png",
    "missions_visual_hero.png",
    "mission_survival.png",
    "mission_crafting.png",
    "mission_tech.png",
    "mission_exploration.png",
    "mission_combat.png",
    "mission_story.png",
    "mission_side_ops.png",
    "status_hazard_scan.png",
    "drone_command_link.png",
    "archives_dossier_wall.png",
    "codex_field_manual.png",
    "world_route_map.png",
    "nexus_core_interface.png",
    "orbital_route_telemetry.png",
    "addons_module_grid.png",
}
ASHFALL_TERMINAL_BACKGROUNDS = {
    "nexus_main_menu.png": (1920, 1080),
    "world_archive.png": (1920, 1080),
    "create_simulation.png": (1920, 1080),
    "multiplayer_uplink.png": (1920, 1080),
    "loading_boot.png": (1920, 1080),
    "terrain_loading.png": (1920, 1080),
}
ASHFALL_FULL_SURFACE_GRASS_BLOCKS = {
    "echoashfallprotocol:wasteland_grass_block",
    "echoashfallprotocol:toxic_wasteland_grass_block",
    "echoashfallprotocol:mutated_wasteland_grass_block",
}
STATIONFALL_REQUIRED_ITEMS = {
    "station_access_card",
    "pressure_seal_kit",
    "emergency_oxygen_pack",
    "station_battery",
    "hull_cutter",
    "crew_log_tablet",
    "ai_override_chip",
    "signal_panic_dampener",
    "stationfall_blackbox",
    "ai_override_core",
    "orbital_memory_fragment",
    "hollow_crewman_spawn",
    "eva_stalker_spawn",
    "medical_husk_spawn",
    "hydroponic_growth_spawn",
    "maintenance_drone_spawn",
    "screaming_signal_spawn",
    "station_mimic_spawn",
    "suit_without_body_spawn",
    "station_mother_spawn",
}
STATIONFALL_REQUIRED_BLOCKS = {
    "stationfall_plating",
    "stationfall_wall_panel",
    "station_power_node",
    "pressure_door",
    "damaged_airlock",
    "hull_breach",
    "crew_log_terminal",
    "data_core_terminal",
    "command_console",
    "cracked_observation_glass",
    "corrupted_hydroponic_growth",
    "containment_pod",
}
STATIONFALL_REQUIRED_CHEST_LOOT = {
    "station_salvage_power",
    "station_salvage_common",
    "station_salvage_command",
}
RENAMED_VANILLA_RESOURCE_HINTS = {
    "minecraft:scute": "minecraft:turtle_scute",
    "minecraft:item/scute": "minecraft:item/turtle_scute",
}
VANILLA_RECIPE_ITEM_FALLBACKS = {
    "blast_furnace",
    "chiseled_bookshelf",
    "compass",
    "copper_block",
    "crafting_table",
    "crying_obsidian",
    "oak_planks",
    "observer",
    "piston",
    "smithing_table",
}
VANILLA_ITEM_TEXTURE_FALLBACKS = {
    "observer",
    "piston",
    "shield",
}


def expected_terminal_gui_size(file_name: str) -> tuple[int, int] | None:
    if file_name == "terminal_frame_backdrop.png":
        return (1920, 1080)
    if file_name.startswith("cards/"):
        return None
    if file_name.startswith("mission_"):
        return (512, 256)
    if "/" not in file_name:
        return (2048, 1024)
    return None


def rel(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--addon-set",
        choices=("beta", "all"),
        default="all",
        help="Addon validation set. all matches the workspace default.",
    )
    return parser.parse_args()


def configure_addon_set(addon_set: str) -> None:
    global ACTIVE_MOD_IDS
    global MODS
    global RELEASE_POLISH_SCAN_ROOTS

    if addon_set == "all":
        ACTIVE_MOD_IDS = {modid for modid, _, _ in MODS}
        return

    ACTIVE_MOD_IDS = set(BETA_MOD_IDS)
    MODS = tuple(mod for mod in MODS if mod[0] in ACTIVE_MOD_IDS)
    RELEASE_POLISH_SCAN_ROOTS = tuple(
        root for root in RELEASE_POLISH_SCAN_ROOTS
        if release_polish_root_active(root)
    )


def release_polish_root_active(root: Path) -> bool:
    parts = root.relative_to(ROOT).parts if root.is_relative_to(ROOT) else root.parts
    if len(parts) < 2 or parts[0] != "addons":
        return True
    addon_modid = ADDON_DIR_TO_MODID.get(parts[1], parts[1])
    return addon_modid in ACTIVE_MOD_IDS


def check_build_truth_docs(errors: list[str], addon_set: str) -> None:
    truth = build_truth_modules(addon_set)
    active_truth_ids = {module["mod_id"] for module in truth}
    missing_validator_mods = sorted(active_truth_ids - ACTIVE_MOD_IDS)
    if missing_validator_mods:
        errors.append("VALIDATOR_STACK_DRIFT missing active module(s): " + ", ".join(missing_validator_mods))

    for doc_path in BUILD_TRUTH_DOCS:
        if not doc_path.exists():
            errors.append(f"MISSING_BUILD_TRUTH_DOC {rel(doc_path)}")
            continue
        text = doc_path.read_text(encoding="utf-8", errors="ignore")
        for module in truth:
            mod_id = module["mod_id"]
            version = module["version"]
            if mod_id not in text:
                errors.append(f"DOC_MISSING_MODULE {rel(doc_path)}: {mod_id}")
            if version and f"`{version}`" not in text:
                errors.append(f"DOC_MISSING_VERSION {rel(doc_path)}: {mod_id} {version}")
        for token in STALE_DOC_TOKENS:
            if token in text:
                errors.append(f"STALE_BUILD_TRUTH_DOC {rel(doc_path)}: {token}")

    for doc_path in MODULE_ID_DOCS:
        if not doc_path.exists():
            continue
        text = doc_path.read_text(encoding="utf-8", errors="ignore")
        for module in truth:
            if module["mod_id"] not in text:
                errors.append(f"DOC_MISSING_MODULE_ID {rel(doc_path)}: {module['mod_id']}")

    full_stack_doc = ROOT / "docs/FULL_GRADLE_STACK.md"
    full_stack_text = full_stack_doc.read_text(encoding="utf-8", errors="ignore") if full_stack_doc.exists() else ""
    for phrase in REQUIRED_NAMING_PHRASES:
        if phrase not in full_stack_text:
            errors.append(f"DOC_MISSING_NAMING_RULE {rel(full_stack_doc)}: {phrase}")
    for phrase in REQUIRED_LOCAL_TOOLING_PHRASES:
        if phrase not in full_stack_text:
            errors.append(f"DOC_MISSING_TOOLING_RULE {rel(full_stack_doc)}: {phrase}")


def build_truth_modules(addon_set: str) -> list[dict[str, str]]:
    settings = (ROOT / "settings.gradle").read_text(encoding="utf-8")
    beta_addons = parse_gradle_string_list(settings, "echoBetaAddons")
    release_addons = parse_gradle_string_list(settings, "echoReleaseAddons")
    addon_names = beta_addons + (release_addons if addon_set == "all" else [])

    modules = [
        module_from_properties("root", ROOT / "gradle.properties"),
        module_from_properties("core/echocore", ROOT / "core/echocore/gradle.properties"),
    ]
    for addon_name in addon_names:
        props_path = ROOT / f"addons/{addon_name}/gradle.properties"
        if props_path.exists():
            modules.append(module_from_properties(f"addons/{addon_name}", props_path))
    return modules


def parse_gradle_string_list(settings_text: str, variable_name: str) -> list[str]:
    match = re.search(rf"def\s+{re.escape(variable_name)}\s*=\s*\[(.*?)\]", settings_text, re.S)
    if not match:
        return []
    return re.findall(r"'([^']+)'", match.group(1))


def module_from_properties(project_path: str, props_path: Path) -> dict[str, str]:
    props = read_gradle_properties(props_path)
    return {
        "project_path": project_path,
        "mod_id": props.get("mod_id", ""),
        "mod_name": props.get("mod_name", ""),
        "version": props.get("mod_version", ""),
    }


def read_gradle_properties(path: Path) -> dict[str, str]:
    props: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8", errors="ignore").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        props[key.strip()] = value.strip()
    return props


def load_json(path: Path, errors: list[str]) -> Any | None:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:  # noqa: BLE001 - report any malformed JSON.
        errors.append(f"JSON_PARSE {rel(path)}: {exc}")
        return None


def check_pack_metadata(pack_path: Path, errors: list[str]) -> None:
    metadata = load_json(pack_path, errors)
    if not isinstance(metadata, dict):
        return
    pack = metadata.get("pack")
    if not isinstance(pack, dict):
        errors.append(f"BAD_PACK_METADATA {rel(pack_path)}: missing pack object")
        return

    if "pack_format" in pack:
        errors.append(
            f"BAD_PACK_METADATA {rel(pack_path)}: declares legacy pack_format; "
            "use min_format and max_format"
        )


def check_source_pack_metadata(errors: list[str]) -> None:
    for pack_path in sorted(ROOT.rglob("pack.mcmeta")):
        if any(part in SKIPPED_SCAN_DIRS for part in pack_path.relative_to(ROOT).parts):
            continue
        check_pack_metadata(pack_path, errors)


def walk(value: Any) -> Iterable[Any]:
    yield value
    if isinstance(value, dict):
        for item in value.values():
            yield from walk(item)
    elif isinstance(value, list):
        for item in value:
            yield from walk(item)


def is_visible_chroma_key_pixel(r: int, g: int, b: int, a: int) -> bool:
    return (
        a > 0
        and r >= 220
        and g <= 70
        and b >= 220
        and abs(r - b) <= 60
        and r - g >= 150
        and b - g >= 150
    )


def parse_ref(ref: str, default_namespace: str) -> tuple[str, str] | None:
    if not ref or ref.startswith("#") or ref.startswith("builtin/"):
        return None
    if ":" in ref:
        namespace, path = ref.split(":", 1)
    else:
        namespace, path = default_namespace, ref
    return namespace, path


def collect_resource_set(root: Path, suffix: str) -> set[str]:
    if not root.exists():
        return set()
    return {path.relative_to(root).as_posix().removesuffix(suffix) for path in root.rglob(f"*{suffix}")}


def collect_minecraft_artifact_entries(prefix: str, suffix: str) -> set[str] | None:
    artifact_roots = [
        ROOT / "build",
        ROOT / ".gradle",
    ]
    local_appdata = os.environ.get("LOCALAPPDATA")
    if local_appdata:
        artifact_roots.append(Path(local_appdata) / "EchoGradleBuild" / "Echo")

    jars: list[Path] = []
    for artifact_root in artifact_roots:
        if artifact_root.exists():
            jars.extend(artifact_root.rglob("minecraft-patched-*.jar"))
    if not jars:
        return None

    jar_path = max(jars, key=lambda path: path.stat().st_mtime)
    try:
        with zipfile.ZipFile(jar_path) as jar:
            return {
                name.removeprefix(prefix).removesuffix(suffix)
                for name in jar.namelist()
                if name.startswith(prefix) and name.endswith(suffix)
            }
    except (OSError, zipfile.BadZipFile):
        return None


def recipe_item_refs(recipe: Any) -> Iterable[str]:
    def refs_from_ingredient(value: Any) -> Iterable[str]:
        if isinstance(value, str) and ":" in value:
            yield value
        elif isinstance(value, dict):
            item = value.get("item")
            if isinstance(item, str):
                yield item
            elif isinstance(item, list):
                for item_value in item:
                    if isinstance(item_value, str):
                        yield item_value
            if "items" in value:
                yield from refs_from_ingredient(value["items"])
        elif isinstance(value, list):
            for entry in value:
                yield from refs_from_ingredient(entry)

    if not isinstance(recipe, dict):
        return

    for key_value in recipe.get("key", {}).values() if isinstance(recipe.get("key"), dict) else ():
        yield from refs_from_ingredient(key_value)
    yield from refs_from_ingredient(recipe.get("ingredients", []))

    result = recipe.get("result")
    if isinstance(result, str):
        yield result
    elif isinstance(result, dict):
        for key in ("id", "item"):
            value = result.get(key)
            if isinstance(value, str):
                yield value


def check_armory_recipe_and_model_references(errors: list[str]) -> None:
    if "echoarmory" not in ACTIVE_MOD_IDS:
        return

    resource_root = ROOT / "addons/echoarmory/src/main/resources"
    vanilla_items = collect_minecraft_artifact_entries("assets/minecraft/models/item/", ".json")
    vanilla_item_textures = collect_minecraft_artifact_entries("assets/minecraft/textures/item/", ".png")
    armory_items = collect_resource_set(resource_root / "assets/echoarmory/models/item", ".json")

    recipe_root = resource_root / "data/echoarmory/recipe"
    if recipe_root.exists():
        for path in sorted(recipe_root.rglob("*.json")):
            recipe = load_json(path, errors)
            for ref in recipe_item_refs(recipe):
                parsed = parse_ref(ref, "minecraft")
                if parsed is None:
                    continue
                namespace, item_path = parsed
                full_ref = f"{namespace}:{item_path}"
                if full_ref in RENAMED_VANILLA_RESOURCE_HINTS:
                    errors.append(f"RENAMED_VANILLA_ITEM_REF {rel(path)}: {full_ref} -> {RENAMED_VANILLA_RESOURCE_HINTS[full_ref]}")
                    continue
                if namespace == "echoarmory" and item_path not in armory_items:
                    errors.append(f"UNKNOWN_ARMORY_RECIPE_ITEM {rel(path)}: {full_ref}")
                if (namespace == "minecraft"
                        and vanilla_items is not None
                        and item_path not in vanilla_items
                        and item_path not in VANILLA_RECIPE_ITEM_FALLBACKS):
                    errors.append(f"UNKNOWN_VANILLA_RECIPE_ITEM {rel(path)}: {full_ref}")

    model_root = resource_root / "assets/echoarmory/models/item"
    if model_root.exists():
        for path in sorted(model_root.rglob("*.json")):
            model = load_json(path, errors)
            if not isinstance(model, dict) or not isinstance(model.get("textures"), dict):
                continue
            for texture in model["textures"].values():
                if not isinstance(texture, str) or not texture.startswith("minecraft:item/"):
                    continue
                if texture in RENAMED_VANILLA_RESOURCE_HINTS:
                    errors.append(f"RENAMED_VANILLA_TEXTURE_REF {rel(path)}: {texture} -> {RENAMED_VANILLA_RESOURCE_HINTS[texture]}")
                    continue
                texture_path = texture.removeprefix("minecraft:item/")
                if (vanilla_item_textures is not None
                        and texture_path not in vanilla_item_textures
                        and texture_path not in VANILLA_ITEM_TEXTURE_FALLBACKS):
                    errors.append(f"UNKNOWN_VANILLA_ITEM_TEXTURE {rel(path)}: {texture}")


def check_assets(modid: str, resource_root: Path, errors: list[str]) -> None:
    asset_root = resource_root / f"assets/{modid}"
    data_root = resource_root / f"data/{modid}"
    if not asset_root.exists():
        return

    models = collect_resource_set(asset_root / "models", ".json")
    textures = collect_resource_set(asset_root / "textures", ".png")
    lang_path = asset_root / "lang/en_us.json"
    lang = load_json(lang_path, errors) if lang_path.exists() else {}
    if not isinstance(lang, dict):
        lang = {}

    def model_exists(ref: str) -> bool:
        parsed = parse_ref(ref, modid)
        if parsed is None:
            return True
        namespace, path = parsed
        return namespace != modid or path in models

    def texture_exists(ref: str) -> bool:
        parsed = parse_ref(ref, modid)
        if parsed is None:
            return True
        namespace, path = parsed
        return namespace != modid or path in textures

    for path in asset_root.rglob("*.json"):
        data = load_json(path, errors)
        if data is None:
            continue
        path_text = path.as_posix()

        if "/blockstates/" in path_text or "/items/" in path_text or "/models/item/" in path_text:
            for node in walk(data):
                if isinstance(node, dict) and isinstance(node.get("model"), str) and not model_exists(node["model"]):
                    errors.append(f"MISSING_MODEL_REF {rel(path)}: {node['model']}")

        if "/models/" in path_text:
            parent = data.get("parent") if isinstance(data, dict) else None
            if isinstance(parent, str) and not parent.startswith("minecraft:") and not model_exists(parent):
                errors.append(f"MISSING_PARENT_MODEL {rel(path)}: {parent}")
            for node in walk(data):
                textures_obj = node.get("textures") if isinstance(node, dict) else None
                if not isinstance(textures_obj, dict):
                    continue
                for key, texture in textures_obj.items():
                    if isinstance(texture, str) and not texture.startswith("#") and not texture_exists(texture):
                        errors.append(f"MISSING_TEXTURE_REF {rel(path)}: {key} -> {texture}")

    if lang:
        for key, value in list(lang.items()):
            for prefix in ("item", "block", "entity", "effect", "biome"):
                marker = f"{prefix}.EchoAshfallProtocol."
                if key.startswith(marker):
                    alias = f"{prefix}.{modid}." + key[len(marker) :]
                    if alias not in lang:
                        errors.append(f"MISSING_LANG_ALIAS {rel(lang_path)}: {alias} for {key}")
                    elif lang[alias] != value:
                        errors.append(f"MISMATCHED_LANG_ALIAS {rel(lang_path)}: {alias} for {key}")
            if key == "itemGroup.EchoAshfallProtocol" and f"itemGroup.{modid}" not in lang:
                errors.append(f"MISSING_LANG_ALIAS {rel(lang_path)}: itemGroup.{modid}")

    if data_root.exists() and lang:
        for path in data_root.rglob("*.json"):
            data = load_json(path, errors)
            if data is None:
                continue
            for node in walk(data):
                if isinstance(node, dict) and isinstance(node.get("translate"), str):
                    key = node["translate"]
                    if key not in lang:
                        errors.append(f"MISSING_LANG_TRANSLATE {rel(path)}: {key}")


def check_global_loot_modifier_paths(modid: str, resource_root: Path, errors: list[str]) -> None:
    data_root = resource_root / f"data/{modid}"
    if not data_root.exists():
        return

    singular = data_root / "loot_modifier"
    if singular.exists():
        files = sorted(path for path in singular.rglob("*.json") if path.is_file())
        if files:
            errors.append(
                f"SINGULAR_LOOT_MODIFIER_DIR {rel(singular)}: "
                "NeoForge loads global loot modifiers from loot_modifiers"
            )

    plural = data_root / "loot_modifiers"
    if not plural.exists():
        return
    for path in sorted(plural.rglob("*.json")):
        data = load_json(path, errors)
        if not isinstance(data, dict):
            continue
        modifier_type = data.get("type")
        if not isinstance(modifier_type, str) or not modifier_type:
            errors.append(f"BAD_LOOT_MODIFIER_TYPE {rel(path)}")


def check_packet_namespaces(modid: str, java_root: Path, errors: list[str]) -> None:
    if not java_root.exists():
        return
    pattern = re.compile(
        r"Identifier\.fromNamespaceAndPath\(\s*(?:\"([^\"]+)\"|([A-Za-z_][A-Za-z0-9_.]*\.MODID))"
    )
    search_root = java_root / "network"
    if not search_root.exists():
        return
    files = search_root.rglob("*.java")
    for path in files:
        if "test" in path.relative_to(java_root).parts:
            continue
        text = path.read_text(encoding="utf-8")
        for match in pattern.finditer(text):
            namespace, modid_constant = match.groups()
            if namespace is not None:
                if namespace != namespace.lower():
                    errors.append(f"UPPERCASE_PACKET_NAMESPACE {rel(path)}: {namespace}")
                if namespace != modid:
                    errors.append(f"WRONG_PACKET_NAMESPACE {rel(path)}: {namespace}, expected {modid}")
                else:
                    errors.append(f"LITERAL_PACKET_NAMESPACE {rel(path)}: use MODID constant instead of {namespace}")
                continue

            expected_namespace = MODID_CONSTANTS.get(modid_constant)
            if (modid, modid_constant) in ALLOWED_PACKET_NAMESPACE_CONSTANTS:
                continue
            if expected_namespace != modid:
                errors.append(f"WRONG_PACKET_NAMESPACE_CONSTANT {rel(path)}: {modid_constant}, expected {modid}")


def iter_repo_text_files() -> Iterable[Path]:
    validator_path = Path(__file__).resolve()
    for path in iter_files_under(ROOT):
        if not path.is_file():
            continue
        if path.resolve() == validator_path:
            continue
        relative_parts = path.relative_to(ROOT).parts
        if any(part in SKIPPED_SCAN_DIRS for part in relative_parts):
            continue
        if len(relative_parts) >= 2 and relative_parts[0] == "addons":
            addon_modid = ADDON_DIR_TO_MODID.get(relative_parts[1], relative_parts[1])
            if addon_modid not in ACTIVE_MOD_IDS:
                continue
        if path.suffix.lower() not in TEXT_SCAN_SUFFIXES:
            continue
        yield path


def iter_files_under(root: Path) -> Iterable[Path]:
    if root.is_file():
        yield root
        return
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [name for name in dirnames if name not in SKIPPED_SCAN_DIRS]
        for filename in filenames:
            yield Path(dirpath) / filename


def iter_release_polish_files() -> Iterable[Path]:
    seen: set[Path] = set()
    for root in RELEASE_POLISH_SCAN_ROOTS:
        if root.is_file():
            files = (root,)
        elif root.is_dir():
            files = iter_files_under(root)
        else:
            continue

        for path in files:
            if not path.is_file():
                continue
            try:
                resolved = path.resolve()
            except OSError:
                continue
            if resolved in seen:
                continue
            seen.add(resolved)

            relative_parts = path.relative_to(ROOT).parts
            if any(part in SKIPPED_SCAN_DIRS for part in relative_parts):
                continue
            if len(relative_parts) >= 2 and relative_parts[0] == "addons":
                addon_modid = ADDON_DIR_TO_MODID.get(relative_parts[1], relative_parts[1])
                if addon_modid not in ACTIVE_MOD_IDS:
                    continue
            if path.suffix.lower() not in TEXT_SCAN_SUFFIXES:
                continue
            yield path


def check_uppercase_resource_namespaces(errors: list[str]) -> None:
    path_patterns = {
        "data/EchoAshfallProtocol": "data/echoashfallprotocol",
        "data\\EchoAshfallProtocol": "data\\echoashfallprotocol",
        "assets/EchoAshfallProtocol": "assets/echoashfallprotocol",
        "assets\\EchoAshfallProtocol": "assets\\echoashfallprotocol",
        "data/EchoOrbitalRemnants": "data/echoorbitalremnants",
        "data\\EchoOrbitalRemnants": "data\\echoorbitalremnants",
        "assets/EchoOrbitalRemnants": "assets/echoorbitalremnants",
        "assets\\EchoOrbitalRemnants": "assets\\echoorbitalremnants",
        "data/EchoStationfall": "data/echostationfall",
        "data\\EchoStationfall": "data\\echostationfall",
        "assets/EchoStationfall": "assets/echostationfall",
        "assets\\EchoStationfall": "assets\\echostationfall",
        "data/EchoCore": "data/echocore",
        "data\\EchoCore": "data\\echocore",
        "assets/EchoCore": "assets/echocore",
        "assets\\EchoCore": "assets\\echocore",
        "data/EchoTerminal": "data/echoterminal",
        "data\\EchoTerminal": "data\\echoterminal",
        "assets/EchoTerminal": "assets/echoterminal",
        "assets\\EchoTerminal": "assets\\echoterminal",
    }
    api_pattern = re.compile(
        r"(?:Identifier|ResourceLocation)\.fromNamespaceAndPath\(\s*\"(EchoAshfallProtocol|EchoOrbitalRemnants|EchoStationfall|EchoCore|EchoTerminal)\""
    )

    for path in iter_repo_text_files():
        text = path.read_text(encoding="utf-8", errors="ignore")
        for bad, expected in path_patterns.items():
            if bad in text:
                errors.append(f"UPPERCASE_RESOURCE_NAMESPACE {rel(path)}: {bad}, use {expected}")
        for match in api_pattern.finditer(text):
            namespace = match.group(1)
            errors.append(f"UPPERCASE_RESOURCE_NAMESPACE_API {rel(path)}: {namespace}")


def first_line_number(text: str, needle: str) -> int:
    index = text.find(needle)
    if index < 0:
        return 0
    return text.count("\n", 0, index) + 1


def check_release_polish_text(errors: list[str]) -> None:
    for path in iter_release_polish_files():
        text = path.read_text(encoding="utf-8", errors="ignore")
        lower_text = text.lower()
        for char, label in MOJIBAKE_CHARS.items():
            if char in text:
                errors.append(f"MOJIBAKE_TEXT {rel(path)}:{first_line_number(text, char)}: {label}")
        for marker in PLACEHOLDER_MARKERS:
            if marker in lower_text:
                errors.append(f"PLACEHOLDER_TEXT {rel(path)}:{first_line_number(lower_text, marker)}: {marker}")
        for token in STALE_RELEASE_TOKENS:
            if token in text:
                errors.append(f"STALE_RELEASE_REFERENCE {rel(path)}:{first_line_number(text, token)}: {token}")


def check_worldgen_resource_polish(errors: list[str]) -> None:
    data_root = ROOT / "src/main/resources/data/echoashfallprotocol"
    noise_settings = load_json(data_root / "worldgen/noise_settings/wasteland_overworld.json", errors)
    if isinstance(noise_settings, dict):
        surface_refs = {node for node in walk(noise_settings.get("surface_rule", {})) if isinstance(node, str)}
        for ref in sorted(ASHFALL_FULL_SURFACE_GRASS_BLOCKS & surface_refs):
            errors.append(f"FULL_SURFACE_GRASS_BLOCK data/echoashfallprotocol/worldgen/noise_settings/wasteland_overworld.json: {ref}")

    placed_root = data_root / "worldgen/placed_feature"
    if not placed_root.exists():
        return
    for path in placed_root.rglob("*.json"):
        data = load_json(path, errors)
        if not isinstance(data, dict):
            continue
        for node in walk(data):
            if isinstance(node, dict) and node.get("type") == "minecraft:matching_blocks" and "blocks" in node and "offset" not in node:
                errors.append(f"MISSING_GROUND_PREDICATE_OFFSET {rel(path)}")


def check_required_texture_sizes(errors: list[str]) -> None:
    required = {
        ROOT / "src/main/resources/assets/echoashfallprotocol/textures/entity/warden_boss.png": (128, 64),
        ROOT / "src/main/resources/assets/echoashfallprotocol/textures/entity/wasteland_sentinel.png": (128, 64),
    }
    terminal_root = ROOT / "src/main/resources/assets/echoashfallprotocol/textures/gui/terminal"
    for file_name, expected in ASHFALL_TERMINAL_BACKGROUNDS.items():
        required[terminal_root / file_name] = expected

    for path, expected in required.items():
        if not path.exists():
            errors.append(f"MISSING_TEXTURE {rel(path)}")
            continue
        data = path.read_bytes()
        if not data.startswith(b"\x89PNG\r\n\x1a\n") or len(data) < 24:
            errors.append(f"BAD_PNG {rel(path)}")
            continue
        width = int.from_bytes(data[16:20], "big")
        height = int.from_bytes(data[20:24], "big")
        if (width, height) != expected:
            errors.append(f"BAD_TEXTURE_SIZE {rel(path)}: {width}x{height}, expected {expected[0]}x{expected[1]}")


def check_terminal_visual_assets(errors: list[str]) -> None:
    asset_root = ROOT / "addons/echoterminal/src/main/resources/assets/echoterminal/textures/gui/terminal"
    manifest_path = ROOT / "tools/terminal_art_manifest.json"
    manifest = load_json(manifest_path, errors)
    manifest_assets: dict[str, tuple[int, int]] = {}
    manifest_ids: set[str] = set()
    if isinstance(manifest, dict) and isinstance(manifest.get("assets"), list):
        for entry in manifest["assets"]:
            if not isinstance(entry, dict):
                continue
            asset_id = entry.get("id")
            file_name = entry.get("file")
            size = entry.get("size")
            if not isinstance(asset_id, str) or not asset_id:
                errors.append(f"BAD_TERMINAL_ART_MANIFEST_ID {rel(manifest_path)}: {entry!r}")
                continue
            if asset_id in manifest_ids:
                errors.append(f"DUPLICATE_TERMINAL_ART_MANIFEST_ID {asset_id}")
            manifest_ids.add(asset_id)
            if not isinstance(file_name, str) or not file_name:
                errors.append(f"BAD_TERMINAL_ART_MANIFEST_FILE {asset_id}: {file_name!r}")
                continue
            rel_path = PurePosixPath(file_name)
            if file_name != rel_path.as_posix() or rel_path.is_absolute() or ".." in rel_path.parts:
                errors.append(f"BAD_TERMINAL_ART_MANIFEST_PATH {asset_id}: {file_name}")
                continue
            if not isinstance(size, list) or len(size) != 2:
                errors.append(f"BAD_TERMINAL_ART_MANIFEST_SIZE {file_name}: {size!r}")
                continue
            try:
                expected = (int(size[0]), int(size[1]))
            except (TypeError, ValueError):
                errors.append(f"BAD_TERMINAL_ART_MANIFEST_SIZE {file_name}: {size!r}")
                continue
            if expected not in TERMINAL_GUI_TEXTURE_SIZES:
                allowed = " or ".join(f"{w}x{h}" for w, h in sorted(TERMINAL_GUI_TEXTURE_SIZES))
                errors.append(
                    f"UNSUPPORTED_TERMINAL_ART_MANIFEST_SIZE {file_name}: "
                    f"{expected[0]}x{expected[1]}, expected {allowed}"
                )
            bucket_expected = expected_terminal_gui_size(file_name)
            if bucket_expected is not None and expected != bucket_expected:
                errors.append(
                    f"BAD_TERMINAL_ART_MANIFEST_SIZE {file_name}: "
                    f"{expected[0]}x{expected[1]}, expected {bucket_expected[0]}x{bucket_expected[1]}"
                )
            if file_name in manifest_assets:
                errors.append(f"DUPLICATE_TERMINAL_ART_MANIFEST_FILE {file_name}")
                continue
            manifest_assets[file_name] = expected
    else:
        errors.append(f"MISSING_TERMINAL_ART_MANIFEST {rel(manifest_path)}")

    for file_name in sorted(TERMINAL_REQUIRED_GUI_TEXTURES):
        if file_name not in manifest_assets:
            errors.append(f"MISSING_TERMINAL_ART_MANIFEST_ENTRY {file_name}")

    for file_name, expected in manifest_assets.items():
        path = asset_root / PurePosixPath(file_name)
        if not path.exists():
            errors.append(f"MISSING_TERMINAL_GUI_TEXTURE {rel(path)}")
            continue
        try:
            with Image.open(path) as image:
                size = image.size
        except Exception as exc:  # noqa: BLE001 - report corrupt or unreadable assets.
            errors.append(f"BAD_TERMINAL_GUI_TEXTURE {rel(path)}: {exc}")
            continue
        if size != expected:
            errors.append(f"BAD_TERMINAL_GUI_TEXTURE_SIZE {rel(path)}: {size[0]}x{size[1]}, expected {expected[0]}x{expected[1]}")

    if asset_root.exists():
        for path in asset_root.rglob("*.png"):
            file_name = path.relative_to(asset_root).as_posix()
            if file_name not in manifest_assets:
                errors.append(f"UNMANIFESTED_TERMINAL_GUI_TEXTURE {rel(path)}")
                continue
            try:
                with Image.open(path) as image:
                    size = image.size
            except Exception as exc:  # noqa: BLE001
                errors.append(f"BAD_TERMINAL_GUI_TEXTURE {rel(path)}: {exc}")
                continue
            expected = manifest_assets[file_name]
            if size != expected:
                errors.append(f"BAD_TERMINAL_GUI_TEXTURE_SIZE {rel(path)}: {size[0]}x{size[1]}, expected {expected[0]}x{expected[1]}")


def expected_terminal_mission_ids() -> set[tuple[str, str]]:
    expected: list[tuple[str, str]] = []

    ashfall = ROOT / "src/main/java/com/knoxhack/echoashfallprotocol/echo/MissionRegistry.java"
    if ashfall.exists():
        text = ashfall.read_text(encoding="utf-8", errors="ignore")
        expected.extend(("echoashfallprotocol", path) for path in re.findall(r'new Mission\(\s*"([^"]+)"', text))

    ashfall_terminal = ROOT / "src/main/java/com/knoxhack/echoashfallprotocol/integration/AshfallTerminalIntegration.java"
    if ashfall_terminal.exists():
        text = ashfall_terminal.read_text(encoding="utf-8", errors="ignore")
        expected.extend(("echoashfallprotocol", path) for path in re.findall(r'new SideOp\(\s*"([^"]+)"', text))

    vanilla = ROOT / "addons/echoterminal/src/main/java/com/knoxhack/echoterminal/mission/VanillaJourneyProvider.java"
    if vanilla.exists():
        text = vanilla.read_text(encoding="utf-8", errors="ignore")
        expected.extend(("minecraft", path) for path in re.findall(r'(?:root|task|goal|challenge)\(\s*"([^"]+)"', text))

    industrial = ROOT / "addons/echoindustrialnexus/src/main/java/com/knoxhack/echoindustrialnexus/integration/IndustrialMissionProvider.java"
    if industrial.exists():
        text = industrial.read_text(encoding="utf-8", errors="ignore")
        expected.extend(
            ("echoindustrialnexus", "mission/" + path)
            for path in re.findall(r'mission\(\s*"([^"]+)"', text)
        )

    agriculture = ROOT / "addons/echoagriculturereclamation/src/main/java/com/knoxhack/echoagriculturereclamation/integration/ReclamationMissionProvider.java"
    if agriculture.exists():
        text = agriculture.read_text(encoding="utf-8", errors="ignore")
        expected.extend(
            ("echoagriculturereclamation", "mission/" + path)
            for path in re.findall(r'mission\(\s*"([^"]+)"', text)
        )

    enum_providers = (
        (
            "echoblackboxprotocol",
            ROOT / "addons/echoblackboxprotocol/src/main/java/com/knoxhack/echoblackboxprotocol/integration/BlackboxMissionProvider.java",
        ),
        (
            "echonexusprotocol",
            ROOT / "addons/echonexusprotocol/src/main/java/com/knoxhack/echonexusprotocol/integration/NexusTerminalMissionProvider.java",
        ),
    )
    for namespace, path in enum_providers:
        if path.exists():
            text = path.read_text(encoding="utf-8", errors="ignore")
            expected.extend(
                (namespace, mission_path)
                for mission_path in re.findall(r'\b[A-Z_]+\(\s*"([a-z0-9_/]+)"\s*,\s*"', text)
            )

    folder_providers = (
        ("echostationfall", ROOT / "addons/echostationfall/src/main/java"),
        ("echoorbitalremnants", ROOT / "addons/echoorbitalremnants/src/main/java"),
    )
    for namespace, java_root in folder_providers:
        if not java_root.exists():
            continue
        for path in java_root.rglob("*.java"):
            text = path.read_text(encoding="utf-8", errors="ignore")
            if "TerminalMissionDefinition" not in text:
                continue
            expected.extend(
                (namespace, mission_path)
                for mission_path in re.findall(r'\b[A-Z_]+\(\s*"([a-z0-9_/]+)"\s*,\s*"', text)
            )
            expected.extend(
                (namespace, mission_path)
                for mission_path in re.findall(r'mission\(\s*"([a-z0-9_/]+)"', text)
            )

    return set(expected)


def check_terminal_mission_visual_assets(errors: list[str]) -> None:
    gui_root = ROOT / "addons/echoterminal/src/main/resources/assets/echoterminal/textures/gui"
    expected = expected_terminal_mission_ids()
    if not expected:
        errors.append("MISSING_TERMINAL_MISSION_VISUAL_MANIFEST")
        return

    for namespace, mission_path in sorted(expected):
        icon = gui_root / "mission_icons" / namespace / f"{mission_path}.png"
        hero = gui_root / "mission_heroes" / namespace / f"{mission_path}.png"
        for path, expected_size, label in ((icon, (128, 128), "ICON"), (hero, (1024, 512), "HERO")):
            if not path.exists():
                errors.append(f"MISSING_TERMINAL_MISSION_{label} {rel(path)}")
                continue
            try:
                with Image.open(path) as image:
                    size = image.size
                    mode = image.mode
            except Exception as exc:  # noqa: BLE001 - report corrupt or unreadable assets.
                errors.append(f"BAD_TERMINAL_MISSION_{label} {rel(path)}: {exc}")
                continue
            if size != expected_size:
                errors.append(
                    f"BAD_TERMINAL_MISSION_{label}_SIZE {rel(path)}: "
                    f"{size[0]}x{size[1]}, expected {expected_size[0]}x{expected_size[1]}"
                )
            if label == "ICON" and mode not in {"RGB", "RGBA"}:
                errors.append(f"BAD_TERMINAL_MISSION_ICON_MODE {rel(path)}: {mode}, expected RGB or RGBA")


def check_pixel_texture_quality(modid: str, resource_root: Path, errors: list[str]) -> None:
    if modid not in TEXTURE_QUALITY_MOD_IDS:
        return

    asset_root = resource_root / f"assets/{modid}"
    texture_root = asset_root / "textures"
    if not texture_root.exists():
        return

    for folder in ("block", "item"):
        root = texture_root / folder
        if not root.exists():
            continue
        for path in root.rglob("*.png"):
            try:
                with Image.open(path) as image:
                    rgba = image.convert("RGBA")
                    width, height = rgba.size
                    pixel_data = getattr(rgba, "get_flattened_data", rgba.getdata)
                    pixels = list(pixel_data())
            except Exception as exc:  # noqa: BLE001 - report corrupt or unreadable assets.
                errors.append(f"BAD_PNG {rel(path)}: {exc}")
                continue

            if (width, height) not in ALLOWED_PIXEL_TEXTURE_SIZES:
                expected = " or ".join(f"{w}x{h}" for w, h in sorted(ALLOWED_PIXEL_TEXTURE_SIZES))
                errors.append(f"BAD_PIXEL_TEXTURE_SIZE {rel(path)}: {width}x{height}, expected {expected}")

            if (
                modid in REQUIRED_16X16_ITEM_TEXTURE_MOD_IDS
                and folder == "item"
                and (width, height) != (16, 16)
            ):
                errors.append(f"BAD_ADDON_ITEM_TEXTURE_SIZE {rel(path)}: {width}x{height}, expected 16x16")

            if (
                modid == "echoashfallprotocol"
                and folder == "block"
                and path.stem in REQUIRED_16X16_BLOCK_TEXTURES
                and (width, height) != (16, 16)
            ):
                errors.append(f"BAD_TERRAIN_TEXTURE_SIZE {rel(path)}: {width}x{height}, expected 16x16")

            alpha_values = [pixel[3] for pixel in pixels]
            transparent = sum(1 for alpha in alpha_values if alpha == 0)
            opaque = sum(1 for alpha in alpha_values if alpha > 0)
            if folder == "item":
                if opaque == 0:
                    errors.append(f"EMPTY_ITEM_TEXTURE {rel(path)}")
                if transparent == 0:
                    errors.append(f"OPAQUE_ITEM_TEXTURE {rel(path)}")
            if folder == "block":
                if opaque == 0:
                    errors.append(f"EMPTY_BLOCK_TEXTURE {rel(path)}")
                chroma_pixels = sum(
                    1
                    for r, g, b, a in pixels
                    if is_visible_chroma_key_pixel(r, g, b, a)
                )
                if chroma_pixels > 128:
                    errors.append(f"VISIBLE_CHROMA_KEY_BLOCK_TEXTURE {rel(path)}")

            if modid != "echoashfallprotocol" or folder != "block":
                continue

            opaque_pixels = [pixel for pixel in pixels if pixel[3] >= 250]
            has_transparency = len(opaque_pixels) != len(pixels)
            if has_transparency or path.stem in LOW_DETAIL_BLOCK_EXEMPTIONS:
                continue

            opaque_colors = {pixel[:3] for pixel in opaque_pixels}
            if len(opaque_colors) < 5:
                errors.append(f"LOW_DETAIL_BLOCK_TEXTURE {rel(path)}: {len(opaque_colors)} opaque colors, expected at least 5")


def check_ashfall_block_model_texture_namespaces(errors: list[str]) -> None:
    model_root = ROOT / "src/main/resources/assets/echoashfallprotocol/models/block"
    if not model_root.exists():
        return

    for path in sorted(model_root.rglob("*.json")):
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as exc:
            errors.append(f"BAD_JSON {rel(path)}: {exc}")
            continue

        for node in walk(data):
            if not isinstance(node, dict):
                continue
            textures_obj = node.get("textures")
            if not isinstance(textures_obj, dict):
                continue
            for key, texture in textures_obj.items():
                if isinstance(texture, str) and texture.startswith("minecraft:"):
                    errors.append(f"VANILLA_BLOCK_MODEL_TEXTURE {rel(path)}: {key} -> {texture}")


def check_stationfall_resource_completeness(errors: list[str]) -> None:
    root = ROOT / "addons/echostationfall/src/main/resources"
    asset_root = root / "assets/echostationfall"
    data_root = root / "data/echostationfall"

    required_paths: list[Path] = []
    for item in sorted(STATIONFALL_REQUIRED_ITEMS | STATIONFALL_REQUIRED_BLOCKS):
        required_paths.extend(
            [
                asset_root / f"items/{item}.json",
                asset_root / f"models/item/{item}.json" if item in STATIONFALL_REQUIRED_ITEMS else asset_root / f"models/block/{item}.json",
                asset_root / f"textures/item/{item}.png" if item in STATIONFALL_REQUIRED_ITEMS else asset_root / f"textures/block/{item}.png",
            ]
        )

    for block in sorted(STATIONFALL_REQUIRED_BLOCKS):
        required_paths.extend(
            [
                asset_root / f"blockstates/{block}.json",
                asset_root / f"models/block/{block}.json",
                asset_root / f"textures/block/{block}.png",
                data_root / f"loot_table/blocks/{block}.json",
            ]
        )

    for loot_table in sorted(STATIONFALL_REQUIRED_CHEST_LOOT):
        required_paths.append(data_root / f"loot_table/chests/{loot_table}.json")

    for path in required_paths:
        if not path.exists():
            errors.append(f"MISSING_STATIONFALL_RESOURCE {rel(path)}")
            continue
        if path.suffix == ".png":
            try:
                with Image.open(path) as image:
                    size = image.size
            except Exception as exc:  # noqa: BLE001 - report corrupt or unreadable assets.
                errors.append(f"BAD_STATIONFALL_TEXTURE {rel(path)}: {exc}")
                continue
            if size not in ALLOWED_PIXEL_TEXTURE_SIZES:
                expected = " or ".join(f"{w}x{h}" for w, h in sorted(ALLOWED_PIXEL_TEXTURE_SIZES))
                errors.append(f"BAD_STATIONFALL_TEXTURE_SIZE {rel(path)}: {size[0]}x{size[1]}, expected {expected}")


def collect_nexus_registry_ids(java_root: Path, class_name: str, pattern: str) -> set[str]:
    path = java_root / f"registry/{class_name}.java"
    if not path.exists():
        return set()
    text = path.read_text(encoding="utf-8", errors="ignore")
    return {match.group(1) for match in re.finditer(pattern, text, re.DOTALL)}


def collect_nexus_explicit_items(java_root: Path) -> set[str]:
    path = java_root / "registry/ModItems.java"
    if not path.exists():
        return set()
    text = path.read_text(encoding="utf-8", errors="ignore")
    items: set[str] = set()
    for statement in text.split(";"):
        if "public static final DeferredItem" not in statement:
            continue
        match = re.search(r'"([a-z0-9_]+)"', statement)
        if match:
            items.add(match.group(1))
    return items


def check_nexus_lang(lang: Any | None, key: str, errors: list[str]) -> None:
    if not isinstance(lang, dict) or key not in lang:
        errors.append(f"MISSING_NEXUS_LANG {key}")


def check_nexus_paths(paths: Iterable[Path], errors: list[str]) -> None:
    for path in paths:
        if not path.exists():
            errors.append(f"MISSING_NEXUS_RESOURCE {rel(path)}")


def check_nexus_resource_completeness(errors: list[str]) -> None:
    resources = ROOT / "addons/echonexusprotocol/src/main/resources"
    java_root = ROOT / "addons/echonexusprotocol/src/main/java/com/knoxhack/echonexusprotocol"
    asset_root = resources / "assets/echonexusprotocol"
    data_root = resources / "data/echonexusprotocol"
    lang = load_json(asset_root / "lang/en_us.json", errors)

    blocks = collect_nexus_registry_ids(
        java_root,
        "ModBlocks",
        r"public\s+static\s+final\s+DeferredBlock<[^>]+>\s+\w+\s*=\s*(?:BLOCKS\.register(?:SimpleBlock|Block)|dust|stone|glass|wood|leaves|metal|machine)\(\s*\"([a-z0-9_]+)\"",
    )
    items = collect_nexus_explicit_items(java_root)
    entities = collect_nexus_registry_ids(java_root, "ModEntities", r'ENTITIES\.registerEntityType\(\s*"([a-z0-9_]+)"')

    for block in sorted(blocks):
        check_nexus_paths(
            (
                asset_root / f"blockstates/{block}.json",
                asset_root / f"items/{block}.json",
                asset_root / f"models/block/{block}.json",
                asset_root / f"textures/block/{block}.png",
                data_root / f"loot_table/blocks/{block}.json",
            ),
            errors,
        )
        check_nexus_lang(lang, f"block.echonexusprotocol.{block}", errors)

    for item in sorted(items):
        check_nexus_paths(
            (
                asset_root / f"items/{item}.json",
                asset_root / f"models/item/{item}.json",
                asset_root / f"textures/item/{item}.png",
            ),
            errors,
        )
        check_nexus_lang(lang, f"item.echonexusprotocol.{item}", errors)

    for entity in sorted(entities):
        check_nexus_paths(
            (
                asset_root / f"textures/entity/{entity}.png",
                data_root / f"loot_table/entities/{entity}.json",
            ),
            errors,
        )
        check_nexus_lang(lang, f"entity.echonexusprotocol.{entity}", errors)

    jei_catalog = java_root / "compat/jei/NexusJeiRecipeCatalog.java"
    if not jei_catalog.exists():
        errors.append(f"MISSING_NEXUS_JEI_CATALOG {rel(jei_catalog)}")
        return
    text = jei_catalog.read_text(encoding="utf-8", errors="ignore")
    for recipe_id in sorted(set(re.findall(r'\brecipe\(\s*"([a-z0-9_]+)"', text))):
        path = data_root / f"recipe/{recipe_id}.json"
        if not path.exists():
            errors.append(f"MISSING_NEXUS_JEI_RECIPE_JSON {rel(path)}")


def main() -> int:
    args = parse_args()
    configure_addon_set(args.addon_set)
    errors: list[str] = []
    check_source_pack_metadata(errors)
    for modid, resources, java_root in MODS:
        check_assets(modid, resources, errors)
        check_global_loot_modifier_paths(modid, resources, errors)
        check_packet_namespaces(modid, java_root, errors)
        check_pixel_texture_quality(modid, resources, errors)
    check_uppercase_resource_namespaces(errors)
    check_release_polish_text(errors)
    check_worldgen_resource_polish(errors)
    check_required_texture_sizes(errors)
    check_terminal_visual_assets(errors)
    check_terminal_mission_visual_assets(errors)
    check_ashfall_block_model_texture_namespaces(errors)
    check_armory_recipe_and_model_references(errors)
    if "echonexusprotocol" in ACTIVE_MOD_IDS:
        check_nexus_resource_completeness(errors)
    if "echostationfall" in ACTIVE_MOD_IDS:
        check_stationfall_resource_completeness(errors)
    check_build_truth_docs(errors, args.addon_set)

    if errors:
        for error in errors:
            print(error)
        print(f"Resource validation failed with {len(errors)} issue(s).")
        return 1

    print("Resource validation passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
