#!/usr/bin/env python3
"""Generate ECHO RenderCore mob visual assets from a canonical manifest.

The source image sheet comes from Codex image generation, while this script
turns the roster into deterministic Minecraft-ready textures and RenderCore
profiles so every registered Echo entity has coverage.
"""

from __future__ import annotations

import argparse
import json
import shutil
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE_ROOT = ROOT / "art_sources/echo_mob_rendercore"
DOCS_BOARD_ROOT = ROOT / "docs/echo_mob_rendercore/entity_sheets"
BOARD_PROMPT_ROOT = SOURCE_ROOT / "prompts/entity_boards"
MANIFEST_PATH = ROOT / "tools/echo_mob_rendercore_manifest.json"


EXPORT_PANEL_BOXES = {
    "base": [0.030, 0.790, 0.360, 0.930],
    "glow": [0.385, 0.790, 0.655, 0.930],
    "damage": [0.675, 0.790, 0.965, 0.930],
}


@dataclass(frozen=True)
class EntitySpec:
    modid: str
    entity: str
    family: str
    theme: str
    role: str
    feature: str
    fallback: str


MODULE_RESOURCE_ROOTS = {
    "echoashfallprotocol": "src/main/resources",
    "echoorbitalremnants": "addons/echoorbitalremnants/src/main/resources",
    "echonexusprotocol": "addons/echonexusprotocol/src/main/resources",
    "echostationfall": "addons/echostationfall/src/main/resources",
    "echoblackboxprotocol": "addons/echoblackboxprotocol/src/main/resources",
    "echoindustrialnexus": "addons/echoindustrialnexus/src/main/resources",
    "echologisticsnetwork": "addons/echologisticsnetwork/src/main/resources",
    "echoagriculturereclamation": "addons/echoagriculturereclamation/src/main/resources",
    "echoconvoyprotocol": "addons/echoconvoyprotocol/src/main/resources",
}


PALETTES = {
    "radiation": {"base": "#48613A", "dark": "#1F271D", "mid": "#6F8F38", "accent": "#B7FF37", "overlay": "#6EFF2F"},
    "toxic": {"base": "#3C6E2F", "dark": "#162416", "mid": "#75A83E", "accent": "#B8FF38", "overlay": "#54FF22"},
    "ash": {"base": "#6B6961", "dark": "#262421", "mid": "#9A9688", "accent": "#FF9C3D", "overlay": "#BFC5C4"},
    "rust": {"base": "#6D5141", "dark": "#251F1C", "mid": "#A46A3B", "accent": "#FF7A28", "overlay": "#D95B25"},
    "survivor": {"base": "#5C5145", "dark": "#211D1B", "mid": "#8D7560", "accent": "#80E6FF", "overlay": "#D7C098"},
    "faction": {"base": "#3C4650", "dark": "#15191E", "mid": "#64798A", "accent": "#7AFFD2", "overlay": "#36485F"},
    "cyan": {"base": "#384751", "dark": "#11191E", "mid": "#607A88", "accent": "#5EEBFF", "overlay": "#47BFFF"},
    "nexus": {"base": "#3A3150", "dark": "#120D1B", "mid": "#6C4EA0", "accent": "#B45CFF", "overlay": "#4DEBFF"},
    "blackbox": {"base": "#2D3035", "dark": "#111215", "mid": "#525963", "accent": "#FF8A2B", "overlay": "#75ECFF"},
    "station": {"base": "#6A7074", "dark": "#202428", "mid": "#B7C2C7", "accent": "#FF4F4F", "overlay": "#8EE7FF"},
    "orbital": {"base": "#66747E", "dark": "#151B21", "mid": "#C5D5DF", "accent": "#76E8FF", "overlay": "#DDEBFF"},
    "cryo": {"base": "#536C77", "dark": "#17232A", "mid": "#9FD6E8", "accent": "#7EECFF", "overlay": "#E2FBFF"},
    "industrial": {"base": "#5B5145", "dark": "#211E1A", "mid": "#8B7D69", "accent": "#FF8128", "overlay": "#FFC05A"},
    "bio": {"base": "#4C6B3B", "dark": "#162617", "mid": "#789953", "accent": "#79FF55", "overlay": "#D951C6"},
    "void": {"base": "#23222F", "dark": "#07070B", "mid": "#40365F", "accent": "#8D30FF", "overlay": "#D987FF"},
    "convoy": {"base": "#5C5044", "dark": "#1E1B17", "mid": "#897B64", "accent": "#FF9A2F", "overlay": "#67DFFF"},
}


FAMILY_PARTS = {
    "humanoid": ["head", "torso", "left_arm", "right_arm", "left_leg", "right_leg", "pack", "core", "eyes"],
    "survivor_npc": ["head", "torso", "left_arm", "right_arm", "left_leg", "right_leg", "satchel", "radio", "eyes"],
    "wraith": ["head", "veil", "left_claw", "right_claw", "smoke_trail", "core", "eyes"],
    "drone": ["chassis", "lens", "left_rotor", "right_rotor", "rear_rotor", "tool_arm", "core", "scanner"],
    "quadruped": ["head", "body", "tail", "left_front_leg", "right_front_leg", "left_back_leg", "right_back_leg", "spines", "eyes"],
    "crawler": ["head", "body", "front_claws", "rear_legs", "acid_sacs", "mandibles", "eyes"],
    "slime": ["body", "inner_core", "puddle", "bubbles", "eyes"],
    "heavy_boss": ["head", "torso", "left_arm", "right_arm", "left_leg", "right_leg", "reactor", "shoulder_rig", "back_spines", "eyes"],
    "industrial_construct": ["head", "furnace_core", "torso", "left_arm", "right_arm", "legs", "exhaust", "eyes"],
    "station_suit": ["helmet", "torso", "left_arm", "right_arm", "left_leg", "right_leg", "life_support", "warning_lights"],
    "vehicle": ["body", "cabin", "left_wheels", "right_wheels", "engine", "exhaust", "scanner", "cargo"],
    "rocket": ["hull", "nose", "fins", "engine", "window", "warning_lights"],
}


ENTITY_SPECS: list[EntitySpec] = [
    # Ashfall common mobs, drones, NPCs, pressure mobs, and bosses.
    EntitySpec("echoashfallprotocol", "rad_zombie", "humanoid", "radiation", "hostile irradiated zombie", "green poison glow, infected ribs, dripping radiation", "RadZombieRenderer"),
    EntitySpec("echoashfallprotocol", "scavenger_bandit", "humanoid", "survivor", "hostile raider", "scrap armor, mask, scavenged weapon straps", "ScavengerBanditRenderer"),
    EntitySpec("echoashfallprotocol", "irradiated_wolf", "quadruped", "radiation", "fast toxic wolf", "glowing ribs, acid bite, ragged fur plates", "IrradiatedWolfRenderer"),
    EntitySpec("echoashfallprotocol", "wild_dog", "quadruped", "survivor", "lean pack hunter", "alert ears, lean body, dust-worn coat", "WildDogRenderer"),
    EntitySpec("echoashfallprotocol", "echo_drone", "drone", "cyan", "hostile scanner drone", "blue scan lens, four rotors, hard-light beam core", "EchoDroneRenderer"),
    EntitySpec("echoashfallprotocol", "scout_drone", "drone", "cyan", "utility scavenger drone", "cargo claw, compact scanner, blue utility glow", "ScoutDroneRenderer"),
    EntitySpec("echoashfallprotocol", "echo_companion_drone", "drone", "cyan", "owner-bound support drone", "medical cross display, repair tool arm, friendly pulse", "EchoCompanionDroneRenderer"),
    EntitySpec("echoashfallprotocol", "glowing_ghoul", "humanoid", "radiation", "undead radiation ghoul", "transparent green glow, torn silhouette, aura leaks", "GlowingGhoulRenderer"),
    EntitySpec("echoashfallprotocol", "ash_wraith", "wraith", "ash", "stealth ash spirit", "smoke veil, ember eyes, drifting ash chunks", "AshWraithRenderer"),
    EntitySpec("echoashfallprotocol", "toxic_slime", "slime", "toxic", "toxic ooze slime", "bright gel cube, puddles, suspended bubbles", "ToxicSlimeRenderer"),
    EntitySpec("echoashfallprotocol", "city_stalker", "humanoid", "cyan", "urban ambusher", "dark stealth armor, cyan eyes, shimmer panels", "CityStalkerRenderer"),
    EntitySpec("echoashfallprotocol", "rust_walker", "heavy_boss", "rust", "slow scrap construct", "plated rust armor, heavy fists, orange furnace scars", "RustWalkerRenderer"),
    EntitySpec("echoashfallprotocol", "steam_wraith", "wraith", "ash", "hot vapor wraith", "white steam body, copper vents, pressure haze", "SteamWraithRenderer"),
    EntitySpec("echoashfallprotocol", "mutated_crawler", "crawler", "toxic", "acid leaping crawler", "wide mandibles, acid sacs, low insect body", "MutatedCrawlerRenderer"),
    EntitySpec("echoashfallprotocol", "feral_human", "humanoid", "survivor", "hostile survivor", "ragged clothes, cracked mask, scavenged tool", "FeralHumanRenderer"),
    EntitySpec("echoashfallprotocol", "crash_survivor", "survivor_npc", "survivor", "neutral trader survivor", "ration pack, bandages, tech scraps", "CrashSurvivorRenderer"),
    EntitySpec("echoashfallprotocol", "faction_npc", "survivor_npc", "faction", "neutral faction contact", "radio rig, insignia plates, faction beacon", "FactionNpcRenderer"),
    EntitySpec("echoashfallprotocol", "gridbound_husk", "humanoid", "nexus", "nexus pressure husk", "grid cracks, cyan corruption seams, stiff pose", "NexusPressureMobRenderer"),
    EntitySpec("echoashfallprotocol", "relay_warden", "heavy_boss", "nexus", "nexus pressure warden", "relay antennae, shield plates, purple field", "NexusPressureMobRenderer"),
    EntitySpec("echoashfallprotocol", "signal_leech", "crawler", "nexus", "nexus signal leech", "cable legs, data-drain proboscis, cyan sacs", "NexusPressureMobRenderer"),
    EntitySpec("echoashfallprotocol", "nexus_nullifier", "humanoid", "nexus", "anti-tech nullifier", "black armor, violet core, signal-dampening rings", "NexusPressureMobRenderer"),
    EntitySpec("echoashfallprotocol", "warden_boss", "heavy_boss", "blackbox", "ashfall boss", "archive plates, orange eyes, heavy command weapon", "WardenBossRenderer"),
    EntitySpec("echoashfallprotocol", "wasteland_sentinel", "heavy_boss", "ash", "inactive sentinel boss", "ash stone armor, cyan cracks, sentinel crest", "BiomeBossRenderer"),
    EntitySpec("echoashfallprotocol", "crash_zone_colossus", "heavy_boss", "rust", "crash biome boss", "wreckage armor, exposed reactor, salvage plates", "BiomeBossRenderer"),
    EntitySpec("echoashfallprotocol", "cryogenic_overseer", "heavy_boss", "cryo", "cryogenic biome boss", "ice tanks, frost vents, cold blue core", "BiomeBossRenderer"),
    EntitySpec("echoashfallprotocol", "industrial_juggernaut", "heavy_boss", "industrial", "industrial biome boss", "factory armor, furnace heart, hydraulic shoulders", "BiomeBossRenderer"),
    EntitySpec("echoashfallprotocol", "nexus_scar_avatar", "heavy_boss", "nexus", "nexus scar biome boss", "purple spines, glitch halo, cyan fractures", "BiomeBossRenderer"),
    EntitySpec("echoashfallprotocol", "radiation_behemoth", "heavy_boss", "radiation", "radiation biome boss", "huge glowing ribs, toxin boils, green vents", "BiomeBossRenderer"),
    EntitySpec("echoashfallprotocol", "city_ruin_stalker", "heavy_boss", "cyan", "city ruin biome boss", "stealth panels, antenna mask, cyan optics", "BiomeBossRenderer"),
    EntitySpec("echoashfallprotocol", "plains_warlord", "heavy_boss", "survivor", "plains warlord boss", "tribal scrap armor, banner plates, heavy axe silhouette", "BiomeBossRenderer"),
    EntitySpec("echoashfallprotocol", "toxic_hive_matriarch", "heavy_boss", "toxic", "toxic hive boss", "acid sacs, brood vents, green haze", "BiomeBossRenderer"),
    EntitySpec("echoashfallprotocol", "corruption_bloom", "heavy_boss", "bio", "nexus final boss", "organic petals, corrupted roots, magenta spores", "NexusFinalBossRenderer"),
    EntitySpec("echoashfallprotocol", "severance_engine", "heavy_boss", "industrial", "nexus final engine", "industrial frame, cut-off signal core, orange machinery", "NexusFinalBossRenderer"),
    EntitySpec("echoashfallprotocol", "mirror_command", "heavy_boss", "blackbox", "nexus final commander", "black command armor, mirrored plates, orange/cyan eyes", "NexusFinalBossRenderer"),

    # Orbital Remnants.
    EntitySpec("echoorbitalremnants", "emergency_rocket_vehicle", "rocket", "orbital", "rescue rocket vehicle", "burned capsule hull, warning lights, engine plume", "EmergencyRocketRenderer"),
    EntitySpec("echoorbitalremnants", "echo_defense_drone", "drone", "cyan", "orbital defense drone", "armored cube, blue hardpoint lens, micro thrusters", "TintedVexRenderer"),
    EntitySpec("echoorbitalremnants", "vacuum_wraith", "wraith", "orbital", "space vacuum wraith", "thin frost veil, star specks, pale glow", "TintedVexRenderer"),
    EntitySpec("echoorbitalremnants", "broken_astronaut", "station_suit", "orbital", "broken astronaut husk", "cracked helmet, torn suit, oxygen leak", "TintedZombieRenderer"),
    EntitySpec("echoorbitalremnants", "nexus_husk", "humanoid", "nexus", "orbital nexus husk", "purple data cracks, ruined suit armor", "TintedZombieRenderer"),
    EntitySpec("echoorbitalremnants", "corrupted_docking_ai", "drone", "blackbox", "rogue docking AI", "red warning lens, fragmented shell, docking clamps", "TintedVexRenderer"),
    EntitySpec("echoorbitalremnants", "lunar_nexus_husk", "station_suit", "nexus", "lunar nexus husk", "moon dust suit, purple core, cracked visor", "TintedZombieRenderer"),
    EntitySpec("echoorbitalremnants", "abandoned_captain", "station_suit", "orbital", "abandoned captain", "officer suit, radio pack, faded mission stripe", "TintedZombieRenderer"),
    EntitySpec("echoorbitalremnants", "echo_zero", "heavy_boss", "void", "orbital anomaly boss", "void-black armor, magenta fracture halo, zero-g trail", "TintedZombieRenderer"),
    EntitySpec("echoorbitalremnants", "europa_cryo_warden", "drone", "cryo", "cryo warden elite", "icy plates, blue vents, floating cold shards", "TintedVexRenderer"),
    EntitySpec("echoorbitalremnants", "saturn_relay_sentinel", "drone", "cyan", "saturn relay sentinel", "ring antennae, gold panels, blue relay core", "TintedVexRenderer"),
    EntitySpec("echoorbitalremnants", "titan_methane_stalker", "humanoid", "industrial", "methane stalker", "orange methane suit, pressure hoses, amber eyes", "TintedZombieRenderer"),
    EntitySpec("echoorbitalremnants", "orbital_faction_npc", "survivor_npc", "faction", "orbital faction contact", "radio suit, patched mission marks, cyan comms", "OrbitalFactionNpcRenderer"),

    # Nexus Protocol.
    EntitySpec("echonexusprotocol", "nexus_husk", "humanoid", "nexus", "basic nexus husk", "purple cracks, cyan telemetry eyes", "TintedNexusZombieRenderer"),
    EntitySpec("echonexusprotocol", "data_wraith", "wraith", "cyan", "data ghost", "blue transparent body, scanline fragments", "TintedNexusZombieRenderer"),
    EntitySpec("echonexusprotocol", "static_crawler", "crawler", "nexus", "static crawler", "jittering body, purple sparks, low profile", "TintedNexusZombieRenderer"),
    EntitySpec("echonexusprotocol", "core_soldier", "humanoid", "blackbox", "core soldier", "black armor, orange core, military stance", "TintedNexusZombieRenderer"),
    EntitySpec("echonexusprotocol", "archive_seeker", "humanoid", "cyan", "archive seeker", "tall archive mask, pale blue glow, data ribbons", "TintedNexusZombieRenderer"),
    EntitySpec("echonexusprotocol", "corruption_warden", "heavy_boss", "void", "corruption warden", "magenta corruption armor, heavy shoulders", "TintedNexusZombieRenderer"),
    EntitySpec("echonexusprotocol", "nexus_guardian", "heavy_boss", "nexus", "nexus guardian boss", "cyan halo, fortress plating, energy shield", "TintedNexusZombieRenderer"),

    # Stationfall.
    EntitySpec("echostationfall", "hollow_crewman", "station_suit", "station", "hollow crewman", "white suit, exposed red hollow core", "TintedZombieRenderer"),
    EntitySpec("echostationfall", "eva_stalker", "station_suit", "orbital", "EVA stalker", "long EVA limbs, cracked visor, cable tether", "TintedZombieRenderer"),
    EntitySpec("echostationfall", "medical_husk", "station_suit", "station", "medical husk", "white/red medical panels, emergency lens", "TintedZombieRenderer"),
    EntitySpec("echostationfall", "hydroponic_growth", "humanoid", "bio", "mutated hydroponic growth", "plant overgrowth, moss plates, spores", "TintedZombieRenderer"),
    EntitySpec("echostationfall", "maintenance_drone", "drone", "cyan", "station maintenance drone", "tool arms, blue maintenance lens, compact shell", "TintedVexRenderer"),
    EntitySpec("echostationfall", "screaming_signal", "wraith", "station", "signal ghost", "red soundwave halo, flickering body", "TintedVexRenderer"),
    EntitySpec("echostationfall", "station_mimic", "humanoid", "blackbox", "station mimic", "panel armor, false lights, hinge mouth", "TintedZombieRenderer"),
    EntitySpec("echostationfall", "suit_without_body", "station_suit", "orbital", "empty space suit", "floating suit, dark visor, leaking air", "TintedZombieRenderer"),
    EntitySpec("echostationfall", "station_mother", "heavy_boss", "station", "station boss", "large suit mass, red warning core, cable crown", "TintedZombieRenderer"),

    # Blackbox Protocol.
    EntitySpec("echoblackboxprotocol", "archive_husk", "humanoid", "cyan", "archive husk", "blue archive glow, broken memory plates", "TintedZombieRenderer"),
    EntitySpec("echoblackboxprotocol", "security_echo", "humanoid", "station", "security echo", "red security lens, baton silhouette, white armor", "TintedZombieRenderer"),
    EntitySpec("echoblackboxprotocol", "memory_parasite", "crawler", "nexus", "memory parasite", "small purple leech, data tendrils", "TintedZombieRenderer"),
    EntitySpec("echoblackboxprotocol", "false_echo_minion", "humanoid", "void", "false echo minion", "glitch double, purple overlay, faceless mask", "TintedZombieRenderer"),
    EntitySpec("echoblackboxprotocol", "command_remnant_minion", "humanoid", "blackbox", "command remnant minion", "red command armor, orange eye slit", "TintedZombieRenderer"),
    EntitySpec("echoblackboxprotocol", "blackbox_sentinel", "heavy_boss", "blackbox", "blackbox sentinel", "blocky archive armor, orange vents, heavy arms", "TintedZombieRenderer"),
    EntitySpec("echoblackboxprotocol", "false_echo", "heavy_boss", "void", "false echo boss", "mirrored purple shell, hologram static", "TintedZombieRenderer"),
    EntitySpec("echoblackboxprotocol", "command_remnant", "heavy_boss", "blackbox", "command remnant boss", "black command armor, red/orange core", "TintedZombieRenderer"),
    EntitySpec("echoblackboxprotocol", "nexus_guardian", "heavy_boss", "nexus", "blackbox nexus guardian", "cyan guardian plates, archive halo", "TintedZombieRenderer"),

    # Industrial, service drones, agriculture, and convoy.
    EntitySpec("echoindustrialnexus", "furnace_warden", "industrial_construct", "industrial", "industrial furnace warden", "burning furnace chest, heavy plating, exhaust stacks", "IndustrialFurnaceRenderer"),
    EntitySpec("echoindustrialnexus", "furnace_drone", "industrial_construct", "industrial", "industrial furnace drone", "small furnace core, smoke vents, claw arms", "IndustrialFurnaceRenderer"),
    EntitySpec("echologisticsnetwork", "courier_drone", "drone", "cyan", "logistics courier drone", "parcel clamp, cyan route light, boxy courier body", "CourierDroneRenderer"),
    EntitySpec("echoagriculturereclamation", "pollinator_drone", "drone", "bio", "agriculture pollinator drone", "green pollen pods, soft blue sensor, sprayer arms", "PollinatorDroneRenderer"),
    EntitySpec("echoconvoyprotocol", "scrap_bike", "vehicle", "convoy", "scrap bike vehicle", "exposed engine, side cargo, orange dust lights", "ConvoyVehicleRenderer"),
    EntitySpec("echoconvoyprotocol", "wasteland_rover", "vehicle", "convoy", "wasteland rover vehicle", "scanner mast, four wheels, cyan headlights", "ConvoyVehicleRenderer/ConvoyRenderCoreVehicleRenderer"),
    EntitySpec("echoconvoyprotocol", "cargo_crawler", "vehicle", "convoy", "cargo crawler vehicle", "tracked cargo bed, hydraulic legs, crate load", "ConvoyVehicleRenderer"),
    EntitySpec("echoconvoyprotocol", "armored_relay_truck", "vehicle", "convoy", "armored relay truck vehicle", "plow front, relay antennae, heavy armor", "ConvoyVehicleRenderer"),
]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--imagegen-source", type=Path, help="Optional generated chroma-key roster sheet to preserve.")
    parser.add_argument("--imagegen-transparent", type=Path, help="Optional transparent version of the imagegen source sheet.")
    parser.add_argument("--prompts-only", action="store_true", help="Write production-board prompts and manifest without cutting textures.")
    parser.add_argument("--check", action="store_true", help="Validate generated outputs without writing.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    manifest = build_manifest(args)
    if args.check:
        return check_outputs(manifest)
    if args.prompts_only:
        write_prompt_outputs(manifest)
        print(f"Wrote Echo mob production-board prompts for {len(manifest['entities'])} entities.")
        return 0
    write_outputs(manifest, args)
    print(f"Generated {len(manifest['entities'])} Echo mob RenderCore entries.")
    print(f"Wrote manifest: {relative(MANIFEST_PATH)}")
    return 0


def write_prompt_outputs(manifest: dict[str, Any]) -> None:
    SOURCE_ROOT.joinpath("prompts").mkdir(parents=True, exist_ok=True)
    SOURCE_ROOT.joinpath("model_blueprints").mkdir(parents=True, exist_ok=True)
    DOCS_BOARD_ROOT.mkdir(parents=True, exist_ok=True)
    BOARD_PROMPT_ROOT.mkdir(parents=True, exist_ok=True)
    write_text(SOURCE_ROOT / "prompts/codex_gpt_image2_entity_board_prompt_template.txt", imagegen_prompt())
    write_entity_board_prompts(manifest["entities"])
    write_model_blueprints()
    write_json(MANIFEST_PATH, manifest)


def build_manifest(args: argparse.Namespace) -> dict[str, Any]:
    source_root = "art_sources/echo_mob_rendercore"
    entities: list[dict[str, Any]] = []
    for spec in ENTITY_SPECS:
        profile_path = f"echo_mobs/{spec.entity}"
        texture_base = f"textures/entity/rendercore_echo_mobs/{spec.entity}"
        size = texture_size(spec)
        family_parts = FAMILY_PARTS[spec.family]
        entities.append(
            {
                "modid": spec.modid,
                "entity": spec.entity,
                "role": spec.role,
                "feature": spec.feature,
                "theme": spec.theme,
                "family": spec.family,
                "fallback_renderer": spec.fallback,
                "model": {
                    "family": spec.family,
                    "texture_size": [size, size],
                    "named_parts": family_parts,
                    "anchor_parts": anchor_names(spec.family),
                },
                "rendercore": {
                    "profile_id": f"{spec.modid}:{profile_path}",
                    "visual_profile": f"{MODULE_RESOURCE_ROOTS[spec.modid]}/assets/{spec.modid}/rendercore/visual_profiles/{profile_path}.json",
                    "animation_profile": f"{MODULE_RESOURCE_ROOTS[spec.modid]}/assets/{spec.modid}/rendercore/animations/{profile_path}.json",
                    "particle_profile": f"{MODULE_RESOURCE_ROOTS[spec.modid]}/assets/{spec.modid}/rendercore/particles/{profile_path}.json",
                },
                "production_board": f"docs/echo_mob_rendercore/entity_sheets/{spec.modid}/{spec.entity}.png",
                "board_prompt": f"{source_root}/prompts/entity_boards/{spec.modid}/{spec.entity}.txt",
                "texture_source": {
                    "mode": "codex_gpt_imagegen_production_board_crop",
                    "board": f"docs/echo_mob_rendercore/entity_sheets/{spec.modid}/{spec.entity}.png",
                    "export_panels": EXPORT_PANEL_BOXES,
                    "postprocess": "nearest_neighbor_crop_pad_resize_only",
                },
                "textures": {
                    "base": f"{MODULE_RESOURCE_ROOTS[spec.modid]}/assets/{spec.modid}/{texture_base}.png",
                    "glow": f"{MODULE_RESOURCE_ROOTS[spec.modid]}/assets/{spec.modid}/{texture_base}_glow.png",
                    "active_overlay": f"{MODULE_RESOURCE_ROOTS[spec.modid]}/assets/{spec.modid}/{texture_base}_active_overlay.png",
                    "damage_overlay": f"{MODULE_RESOURCE_ROOTS[spec.modid]}/assets/{spec.modid}/{texture_base}_damage_overlay.png",
                    "corrupted_overlay": f"{MODULE_RESOURCE_ROOTS[spec.modid]}/assets/{spec.modid}/{texture_base}_corrupted_overlay.png",
                },
            }
        )
    return {
        "schema_version": 1,
        "generated_by": "tools/generate_echo_mob_rendercore_assets.py",
        "source_art": {
            "mode": "Codex built-in imagegen/gpt-image-2 per-entity Minecraft production boards; Python crops only fixed export panels",
            "prompt_template": f"{source_root}/prompts/codex_gpt_image2_entity_board_prompt_template.txt",
            "entity_prompt_root": f"{source_root}/prompts/entity_boards",
            "board_root": "docs/echo_mob_rendercore/entity_sheets",
            "export_panels": EXPORT_PANEL_BOXES,
        },
        "model_blueprints": {
            family: f"{source_root}/model_blueprints/{family}.json" for family in FAMILY_PARTS
        },
        "families": {family: {"named_parts": parts, "anchors": anchor_names(family)} for family, parts in FAMILY_PARTS.items()},
        "resource_roots": MODULE_RESOURCE_ROOTS,
        "entities": entities,
    }


def write_outputs(manifest: dict[str, Any], args: argparse.Namespace) -> None:
    SOURCE_ROOT.joinpath("prompts").mkdir(parents=True, exist_ok=True)
    SOURCE_ROOT.joinpath("source_sheets").mkdir(parents=True, exist_ok=True)
    SOURCE_ROOT.joinpath("model_blueprints").mkdir(parents=True, exist_ok=True)
    DOCS_BOARD_ROOT.mkdir(parents=True, exist_ok=True)
    BOARD_PROMPT_ROOT.mkdir(parents=True, exist_ok=True)
    write_text(SOURCE_ROOT / "prompts/codex_gpt_image2_entity_board_prompt_template.txt", imagegen_prompt())
    write_entity_board_prompts(manifest["entities"])
    preserve_imagegen_sheet(args)
    write_model_blueprints()
    for entry in manifest["entities"]:
        write_entity_assets(entry)
    write_json(MANIFEST_PATH, manifest)


def preserve_imagegen_sheet(args: argparse.Namespace) -> None:
    chroma_dest = SOURCE_ROOT / "source_sheets/codex_gpt_image2_roster_sheet_chroma.png"
    transparent_dest = SOURCE_ROOT / "source_sheets/codex_gpt_image2_roster_sheet_transparent.png"
    if args.imagegen_source and args.imagegen_source.exists():
        shutil.copy2(args.imagegen_source, chroma_dest)
    if args.imagegen_transparent and args.imagegen_transparent.exists():
        shutil.copy2(args.imagegen_transparent, transparent_dest)
    elif chroma_dest.exists() and not transparent_dest.exists():
        remove_chroma_key(chroma_dest, transparent_dest, (255, 0, 255))


def write_entity_board_prompts(entities: list[dict[str, Any]]) -> None:
    for entry in entities:
        spec = next(item for item in ENTITY_SPECS if item.modid == entry["modid"] and item.entity == entry["entity"])
        write_text(ROOT / entry["board_prompt"], entity_board_prompt(spec, entry))


def entity_board_prompt(spec: EntitySpec, entry: dict[str, Any]) -> str:
    parts = ", ".join(entry["model"]["named_parts"])
    anchors_text = ", ".join(entry["model"]["anchor_parts"])
    texture_size_text = "x".join(str(value) for value in entry["model"]["texture_size"])
    return (
        "Use case: stylized-concept\n"
        "Asset type: Minecraft mod entity production board and texture source sheet for Echo Ecosystem RenderCore.\n"
        f"Primary request: Create ONE complete production board for the entity `{spec.entity}` from `{spec.modid}`. "
        "Model it after an advanced modded Minecraft mob guide: dark technical board, multiple labeled panels, clean readable pixel-art/voxel examples, and a dedicated texture export area.\n"
        f"Subject: {spec.role}; {spec.feature}. Family: {spec.family}. Theme: {spec.theme}.\n"
        "Required board sections: advanced modded mob model overview, model breakdown and parts, multi-angle model reference front/back/left/right/3-4 view, UV layout and template, texture maps explained, texture example, animation setup, implementation checklist, pro tip summary.\n"
        f"Named parts for the model and UV atlas: {parts}. RenderCore anchors to show in notes: {anchors_text}.\n"
        f"Critical export panel: the bottom row must contain three clean export panels. Left panel title outside the art: BASE TEXTURE EXPORT. Middle: EMISSIVE EXPORT. Right: DAMAGE EXPORT. The art inside each export panel must be an aligned Minecraft UV atlas sized for final {texture_size_text} use. Do not put any UI text, labels, arrows, numbers, shadows, perspective, or full-body sprite art inside the export panel artwork itself.\n"
        "Texture technique: true Minecraft mob UV atlas, organized rectangular islands, hard square pixels, no blur, no painterly mush, limited palette, clear shapes, front/top faces brighter, back/bottom faces darker, consistent texel density, no overlapping islands. Use the same UV layout in base, emissive, and damage panels.\n"
        "Style constraints: modded Minecraft, blocky voxel model, gritty Echo ash-wasteland sci-fi style, cyan/orange/green/purple emissive accents as appropriate, no watermark, no real-world logo. Dark cyan/orange sci-fi UI frame is allowed outside the export panels only.\n"
    )


def crop_board_textures(entry: dict[str, Any]) -> None:
    board_path = ROOT / entry["production_board"]
    if not board_path.exists():
        raise FileNotFoundError(f"Missing production board for {entry['modid']}:{entry['entity']}: {relative(board_path)}")
    with Image.open(board_path) as image:
        board = image.convert("RGBA")
    size = tuple(entry["model"]["texture_size"])
    base = crop_export_panel(board, "base", size)
    glow = crop_export_panel(board, "glow", size)
    damage = crop_export_panel(board, "damage", size)
    active = make_active_overlay(glow)
    corrupted = make_corrupted_overlay(damage)
    save_png(base, ROOT / entry["textures"]["base"])
    save_png(glow, ROOT / entry["textures"]["glow"])
    save_png(active, ROOT / entry["textures"]["active_overlay"])
    save_png(damage, ROOT / entry["textures"]["damage_overlay"])
    save_png(corrupted, ROOT / entry["textures"]["corrupted_overlay"])


def crop_export_panel(board: Image.Image, panel: str, size: tuple[int, int]) -> Image.Image:
    width, height = board.size
    left_n, top_n, right_n, bottom_n = EXPORT_PANEL_BOXES[panel]
    crop = board.crop(
        (
            round(width * left_n),
            round(height * top_n),
            round(width * right_n),
            round(height * bottom_n),
        )
    )
    side = min(crop.size)
    left = (crop.width - side) // 2
    top = (crop.height - side) // 2
    square = crop.crop((left, top, left + side, top + side))
    return square.resize(size, Image.Resampling.NEAREST)


def make_active_overlay(glow: Image.Image) -> Image.Image:
    return glow.copy()


def make_corrupted_overlay(damage: Image.Image) -> Image.Image:
    corrupted = Image.new("RGBA", damage.size, (0, 0, 0, 0))
    source = damage.convert("RGBA")
    src_pixels = source.load()
    out_pixels = corrupted.load()
    for y in range(source.height):
        for x in range(source.width):
            r, g, b, a = src_pixels[x, y]
            if a == 0:
                continue
            intensity = max(r, g, b)
            if intensity < 36:
                continue
            if (x * 11 + y * 7) % 19 < 4:
                out_pixels[x, y] = (180, 92, 255, min(210, max(72, intensity)))
            elif (x + y) % 23 == 0:
                out_pixels[x, y] = (78, 235, 255, min(190, max(64, intensity)))
    return corrupted


def write_entity_assets(entry: dict[str, Any]) -> None:
    spec = next(item for item in ENTITY_SPECS if item.modid == entry["modid"] and item.entity == entry["entity"])
    palette = PALETTES[spec.theme]
    crop_board_textures(entry)
    profile_id = entry["rendercore"]["profile_id"]
    profile_path = profile_id.split(":", 1)[1]
    visual = visual_profile(spec, palette, profile_id, profile_path)
    particles = particle_profile(spec, palette)
    animations = animation_profile(spec)
    write_json(ROOT / entry["rendercore"]["visual_profile"], visual)
    write_json(ROOT / entry["rendercore"]["particle_profile"], particles)
    write_json(ROOT / entry["rendercore"]["animation_profile"], animations)


def visual_profile(spec: EntitySpec, palette: dict[str, str], profile_id: str, profile_path: str) -> dict[str, Any]:
    modid = spec.modid
    texture_base = f"{modid}:textures/entity/rendercore_echo_mobs/{spec.entity}"
    default_state = "ONLINE" if spec.family in {"drone", "vehicle", "rocket"} else "IDLE"
    effect = family_effect(spec.family, spec.theme)
    return {
        "schema_version": 11,
        "base_texture": f"{texture_base}.png",
        "glow_texture": f"{texture_base}_glow.png",
        "damaged_overlay_texture": f"{texture_base}_damage_overlay.png",
        "corrupted_overlay_texture": f"{texture_base}_corrupted_overlay.png",
        "active_overlay_texture": f"{texture_base}_active_overlay.png",
        "animation_profile": f"{modid}:{profile_path}",
        "particle_profile": f"{modid}:{profile_path}",
        "default_state": default_state,
        "transition_seconds": 0.18,
        "effect": effect,
        "materials": {
            "echo_emissive": {
                "color": palette["accent"],
                "alpha": 0.92,
                "emissive": True,
                "blend_mode": "additive",
                "light_mode": "emissive",
                "render_pass": "emissive",
                "effect": effect,
            },
            "damage_heat": {
                "color": "#FFFF7A28",
                "alpha": 0.75,
                "emissive": True,
                "blend_mode": "additive",
                "light_mode": "emissive",
                "render_pass": "emissive",
                "effect": {"preset": "neon", "glow_intensity": 0.8, "pulse_speed": 3.0, "pulse_min_alpha": 0.45, "pulse_max_alpha": 0.9, "target_scope": "entity"},
            },
            "corruption": {
                "color": "#FFB45CFF",
                "alpha": 0.72,
                "emissive": True,
                "blend_mode": "additive",
                "light_mode": "emissive",
                "render_pass": "emissive",
                "effect": {"preset": "hologram", "glow_intensity": 0.75, "scanline_strength": 0.35, "pulse_speed": 2.2, "target_scope": "entity"},
            },
        },
        "layers": [
            {"id": "core_glow", "kind": "glow", "texture": f"{texture_base}_glow.png", "material": "echo_emissive", "states": ["ONLINE", "IDLE", "ACTIVE", "WORKING", "SCANNING", "CHARGING", "COMPLETE"]},
            {"id": "active_motion_overlay", "kind": "overlay", "texture": f"{texture_base}_active_overlay.png", "material": "echo_emissive", "states": ["ACTIVE", "WORKING", "SCANNING", "CHARGING"]},
            {"id": "damage_heat_overlay", "kind": "overlay", "texture": f"{texture_base}_damage_overlay.png", "material": "damage_heat", "states": ["DAMAGED", "OVERHEATED", "FAILED"]},
            {"id": "corruption_overlay", "kind": "overlay", "texture": f"{texture_base}_corrupted_overlay.png", "material": "corruption", "states": ["CORRUPTED"]},
        ],
        "state_animations": {
            "IDLE": "idle",
            "ONLINE": "idle",
            "ACTIVE": "active",
            "WORKING": "active",
            "SCANNING": "scan",
            "CHARGING": "charge",
            "DAMAGED": "damaged",
            "CORRUPTED": "corrupt",
            "OVERHEATED": "damaged",
            "FAILED": "damaged",
        },
        "anchors": anchors(spec.family),
        "preview": {
            "title": f"{spec.entity} RenderCore mob visual",
            "notes": f"{spec.role}; {spec.feature}",
            "artifact": f"docs/echo_mob_rendercore/entity_sheets/{spec.modid}/{spec.entity}.png",
            "screenshot": {"enabled": False, "reason": "Per-entity production board is preserved; in-game screenshot pass is manual."},
        },
    }


def family_effect(family: str, theme: str) -> dict[str, Any]:
    preset = "hologram" if theme in {"nexus", "blackbox", "void", "cyan"} or family == "wraith" else "neon"
    if family in {"slime", "crawler", "quadruped"} and theme in {"toxic", "radiation", "bio"}:
        preset = "energy_field"
    return {
        "preset": preset,
        "glow_intensity": 1.1 if family in {"heavy_boss", "industrial_construct"} else 0.75,
        "pulse_speed": 1.8 if family in {"drone", "vehicle", "rocket"} else 1.15,
        "pulse_min_alpha": 0.55,
        "pulse_max_alpha": 1.0,
        "flicker_intensity": 0.16 if theme in {"blackbox", "station", "void"} else 0.05,
        "scanline_strength": 0.22 if theme in {"nexus", "blackbox", "cyan", "void"} else 0.0,
        "target_scope": "entity",
    }


def particle_profile(spec: EntitySpec, palette: dict[str, str]) -> dict[str, Any]:
    accent = hex_to_unit_rgb(palette["accent"])
    overlay = hex_to_unit_rgb(palette["overlay"])
    emitters: dict[str, Any] = {
        "ambient_core_dust": {
            "anchor": "core",
            "particle": "minecraft:dust",
            "states": ["IDLE", "ONLINE", "ACTIVE", "SCANNING"],
            "rate": family_rate(spec.family, 0.055),
            "burst_count": 1,
            "offset": [0.0, 0.0, 0.0],
            "velocity": [0.0, 0.015, 0.0],
            "spread": [0.12, 0.18, 0.12],
            "options": {"type": "minecraft:dust", "color": accent, "scale": 0.62},
        },
        "active_trail": {
            "anchor": "trail",
            "particle": "minecraft:dust",
            "states": ["ACTIVE", "WORKING", "SCANNING"],
            "requires_moving": True,
            "rate": family_rate(spec.family, 0.07),
            "burst_count": 1,
            "offset": [0.0, 0.08, 0.0],
            "velocity": [0.0, 0.01, 0.0],
            "spread": [0.22, 0.08, 0.22],
            "options": {"type": "minecraft:dust", "color": overlay, "scale": 0.45},
        },
        "damaged_smoke": {
            "anchor": "core",
            "particle": "minecraft:smoke",
            "states": ["DAMAGED", "OVERHEATED", "FAILED"],
            "requires_damaged": True,
            "rate": family_rate(spec.family, 0.05),
            "burst_count": 1,
            "offset": [0.0, 0.08, 0.0],
            "velocity": [0.0, 0.03, 0.0],
            "spread": [0.14, 0.06, 0.14],
        },
    }
    if spec.theme in {"toxic", "radiation", "bio"}:
        emitters["toxic_drip"] = {
            "anchor": "ground",
            "particle": "minecraft:dust",
            "states": ["ACTIVE", "DAMAGED", "CORRUPTED"],
            "rate": 0.065,
            "burst_count": 1,
            "offset": [0.0, 0.02, 0.0],
            "velocity": [0.0, 0.0, 0.0],
            "spread": [0.2, 0.02, 0.2],
            "options": {"type": "minecraft:dust", "color": hex_to_unit_rgb(PALETTES["toxic"]["accent"]), "scale": 0.7},
        }
    if spec.family in {"drone", "vehicle", "rocket", "industrial_construct"}:
        emitters["exhaust_spark"] = {
            "anchor": "exhaust",
            "particle": "minecraft:dust",
            "states": ["ACTIVE", "WORKING", "OVERHEATED"],
            "requires_moving": spec.family in {"vehicle", "rocket"},
            "rate": family_rate(spec.family, 0.08),
            "burst_count": 1,
            "offset": [0.0, 0.0, 0.0],
            "velocity": [0.0, 0.02, 0.0],
            "spread": [0.16, 0.12, 0.16],
            "options": {"type": "minecraft:dust", "color": hex_to_unit_rgb("#FF9A2F"), "scale": 0.55},
        }
    if spec.theme in {"nexus", "blackbox", "void", "cyan"}:
        emitters["scanline_sparks"] = {
            "anchor": "eyes",
            "particle": "minecraft:dust",
            "states": ["SCANNING", "CHARGING", "CORRUPTED"],
            "rate": family_rate(spec.family, 0.06),
            "burst_count": 1,
            "offset": [0.0, 0.0, 0.0],
            "velocity": [0.0, 0.015, 0.0],
            "spread": [0.18, 0.16, 0.18],
            "options": {"type": "minecraft:dust", "color": hex_to_unit_rgb("#78F5FF"), "scale": 0.5},
        }
    return {"emitters": emitters}


def family_rate(family: str, base: float) -> float:
    if family == "heavy_boss":
        return round(base * 1.75, 3)
    if family in {"vehicle", "rocket"}:
        return round(base * 1.35, 3)
    if family == "drone":
        return round(base * 1.15, 3)
    return round(base, 3)


def animation_profile(spec: EntitySpec) -> dict[str, Any]:
    root = primary_part(spec.family)
    optic = "scanner" if spec.family in {"drone", "vehicle"} else "eyes"
    exhaust = "exhaust" if spec.family in {"drone", "vehicle", "rocket", "industrial_construct"} else root
    return {
        "animations": {
            "idle": {"loop": True, "length": 2.0, "transition_seconds": 0.16, "tracks": pulse_track(root, "position_y", 0.0, idle_bob(spec.family), 2.0)},
            "active": {"loop": True, "length": 1.0, "transition_seconds": 0.10, "tracks": pulse_track(root, "position_y", 0.0, active_bob(spec.family), 1.0) + rotate_track(optic, "rotation_y", 0.0, 360.0, 1.0)},
            "scan": {"loop": True, "length": 1.6, "transition_seconds": 0.10, "tracks": rotate_track(optic, "rotation_y", -18.0, 18.0, 1.6) + pulse_track(optic, "scale_y", 1.0, 1.18, 1.6)},
            "charge": {"loop": True, "length": 0.8, "transition_seconds": 0.08, "tracks": pulse_track("core", "scale_x", 1.0, 1.22, 0.8) + pulse_track("core", "scale_y", 1.0, 1.22, 0.8) + pulse_track("core", "scale_z", 1.0, 1.22, 0.8)},
            "damaged": {"loop": True, "length": 0.7, "transition_seconds": 0.05, "tracks": pulse_track(root, "rotation_z", -1.8, 1.8, 0.7) + pulse_track(exhaust, "scale_y", 1.0, 1.18, 0.7)},
            "corrupt": {"loop": True, "length": 1.2, "transition_seconds": 0.08, "tracks": pulse_track(root, "position_x", -0.02, 0.02, 1.2) + pulse_track("core", "scale_y", 0.95, 1.18, 1.2)},
        }
    }


def pulse_track(part: str, channel: str, low: float, high: float, length: float) -> list[dict[str, Any]]:
    return [
        {
            "part": part,
            "channel": channel,
            "keyframes": [
                {"time": 0.0, "value": low},
                {"time": round(length / 2.0, 3), "value": high, "easing": "ease_in_out"},
                {"time": length, "value": low, "easing": "ease_in_out"},
            ],
        }
    ]


def rotate_track(part: str, channel: str, start: float, end: float, length: float) -> list[dict[str, Any]]:
    return [{"part": part, "channel": channel, "keyframes": [{"time": 0.0, "value": start}, {"time": length, "value": end, "easing": "linear"}]}]


def primary_part(family: str) -> str:
    return {
        "drone": "chassis",
        "vehicle": "body",
        "rocket": "hull",
        "slime": "body",
        "quadruped": "body",
        "crawler": "body",
        "wraith": "veil",
        "industrial_construct": "torso",
        "station_suit": "torso",
    }.get(family, "torso")


def idle_bob(family: str) -> float:
    return 0.08 if family in {"drone", "wraith", "rocket"} else 0.035


def active_bob(family: str) -> float:
    return 0.14 if family in {"drone", "vehicle", "rocket", "crawler"} else 0.07


def anchors(family: str) -> dict[str, Any]:
    return {
        "core": [0.0, anchor_height(family), 0.0],
        "eyes": [0.0, anchor_height(family) + 0.45, -0.35],
        "trail": [0.0, 0.18, 0.55],
        "ground": [0.0, 0.04, 0.0],
        "exhaust": [0.0, exhaust_height(family), 0.65],
        "scanner": [0.0, anchor_height(family) + 0.25, -0.45],
    }


def anchor_names(family: str) -> list[str]:
    return list(anchors(family).keys())


def anchor_height(family: str) -> float:
    return {
        "slime": 0.45,
        "crawler": 0.45,
        "quadruped": 0.75,
        "drone": 0.9,
        "vehicle": 0.8,
        "rocket": 1.15,
        "heavy_boss": 1.7,
        "industrial_construct": 1.25,
        "wraith": 1.25,
    }.get(family, 1.05)


def exhaust_height(family: str) -> float:
    return {
        "drone": 0.8,
        "vehicle": 0.55,
        "rocket": 0.2,
        "industrial_construct": 1.4,
    }.get(family, 0.6)


def texture_size(spec: EntitySpec) -> int:
    if spec.modid == "echoconvoyprotocol" and spec.family == "vehicle":
        return 256
    return 128 if spec.family in {"heavy_boss", "vehicle", "rocket", "industrial_construct"} else 64


def write_model_blueprints() -> None:
    for family, parts in FAMILY_PARTS.items():
        size = 256 if family == "vehicle" else 128 if family in {"heavy_boss", "rocket", "industrial_construct"} else 64
        blueprint = {
            "schema_version": 1,
            "family": family,
            "intended_runtime": "NeoForge EntityModel with RenderCorePartProvider",
            "texture_size": [size, size],
            "anchors": anchors(family),
            "parts": [
                {
                    "name": part,
                    "role": part_role(part),
                    "cube": blueprint_cube(part, family),
                    "pivot": blueprint_pivot(part, family),
                }
                for part in parts
            ],
            "animation_contract": {
                "idle": [primary_part(family)],
                "active": [primary_part(family), "core"],
                "scan": ["eyes", "scanner"],
                "charge": ["core"],
                "damaged": [primary_part(family), "exhaust"],
                "corrupt": [primary_part(family), "core"],
            },
        }
        write_json(SOURCE_ROOT / f"model_blueprints/{family}.json", blueprint)


def part_role(part: str) -> str:
    if part in {"eyes", "lens", "scanner", "warning_lights"}:
        return "emissive focal point"
    if part in {"core", "reactor", "furnace_core", "inner_core", "engine"}:
        return "RenderCore glow and charge anchor"
    if "arm" in part or "claw" in part or "leg" in part or "wheel" in part:
        return "animated locomotion limb"
    if part in {"exhaust", "smoke_trail", "trail"}:
        return "particle emission anchor"
    return "structural model part"


def blueprint_cube(part: str, family: str) -> list[float]:
    base = 8.0 if family not in {"heavy_boss", "vehicle", "rocket", "industrial_construct"} else 16.0
    if "arm" in part or "leg" in part or "wheel" in part or "rotor" in part:
        return [base * 0.5, base * 1.4, base * 0.5]
    if part in {"head", "helmet", "lens", "scanner", "window"}:
        return [base, base, base * 0.8]
    if part in {"core", "reactor", "furnace_core", "inner_core", "engine"}:
        return [base * 0.9, base * 0.9, base * 0.55]
    if family in {"vehicle", "rocket"} and part in {"body", "hull"}:
        return [base * 4.5, base * 1.6, base * 2.2]
    return [base * 1.4, base * 1.8, base * 0.9]


def blueprint_pivot(part: str, family: str) -> list[float]:
    height = anchor_height(family)
    if part in {"head", "helmet", "eyes", "lens", "scanner"}:
        return [0.0, round(height + 0.45, 3), -0.25]
    if part in {"core", "reactor", "furnace_core", "inner_core", "engine"}:
        return [0.0, round(height, 3), 0.0]
    if part in {"exhaust", "smoke_trail"}:
        return [0.0, round(exhaust_height(family), 3), 0.65]
    if "left" in part:
        return [-0.35, round(height * 0.55, 3), 0.0]
    if "right" in part:
        return [0.35, round(height * 0.55, 3), 0.0]
    return [0.0, round(height * 0.65, 3), 0.0]


def remove_chroma_key(source: Path, dest: Path, key: tuple[int, int, int]) -> None:
    with Image.open(source) as image:
        rgba_img = image.convert("RGBA")
    pixels = rgba_img.load()
    width, height = rgba_img.size
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            distance = abs(r - key[0]) + abs(g - key[1]) + abs(b - key[2])
            if distance < 42:
                pixels[x, y] = (r, g, b, 0)
    save_png(rgba_img, dest)


def check_outputs(manifest: dict[str, Any]) -> int:
    errors: list[str] = []
    expected = [MANIFEST_PATH, SOURCE_ROOT / "prompts/codex_gpt_image2_entity_board_prompt_template.txt"]
    expected.extend(SOURCE_ROOT / f"model_blueprints/{family}.json" for family in FAMILY_PARTS)
    for entry in manifest["entities"]:
        expected.append(ROOT / entry["production_board"])
        expected.append(ROOT / entry["board_prompt"])
        expected.extend(ROOT / path for path in entry["textures"].values())
        expected.extend(ROOT / entry["rendercore"][key] for key in ("visual_profile", "animation_profile", "particle_profile"))
    for path in expected:
        if not path.exists():
            errors.append(f"MISSING {relative(path)}")
    if errors:
        print("\n".join(errors))
        return 1
    print(f"Echo mob RenderCore generated assets present for {len(manifest['entities'])} entities.")
    return 0


def imagegen_prompt() -> str:
    return (
        "Use case: stylized-concept\n"
        "Asset type: Minecraft mod entity production board and texture source sheet for Echo Ecosystem RenderCore.\n"
        "Primary request: Create ONE complete production board for a single Echo entity. Model it after an advanced modded Minecraft mob guide: dark technical board, multiple labeled panels, clean readable pixel-art/voxel examples, and a dedicated texture export area.\n"
        "Required board sections: advanced modded mob model overview, model breakdown and parts, multi-angle model reference front/back/left/right/3-4 view, UV layout and template, texture maps explained, texture example, animation setup, implementation checklist, pro tip summary.\n"
        "Critical export panel: the bottom row must contain three clean export panels. Left: BASE TEXTURE EXPORT. Middle: EMISSIVE EXPORT. Right: DAMAGE EXPORT. The art inside each export panel must be an aligned Minecraft UV atlas with no UI text, labels, arrows, numbers, shadows, perspective, or full-body sprite art inside the export panel artwork itself.\n"
        "Texture technique: true Minecraft mob UV atlas, organized rectangular islands, hard square pixels, no blur, no painterly mush, limited palette, clear shapes, front/top faces brighter, back/bottom faces darker, consistent texel density, no overlapping islands. Use the same UV layout in base, emissive, and damage panels.\n"
        "Style constraints: modded Minecraft, blocky voxel model, gritty Echo ash-wasteland sci-fi style, no watermark, no real-world logo. Dark cyan/orange sci-fi UI frame is allowed outside the export panels only.\n"
    )


def hex_to_unit_rgb(value: str) -> list[float]:
    value = value.lstrip("#")
    return [round(int(value[i : i + 2], 16) / 255.0, 3) for i in (0, 2, 4)]


def rgba(value: str, alpha: int) -> tuple[int, int, int, int]:
    value = value.lstrip("#")
    return (int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), alpha)


def save_png(image: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, "PNG", optimize=True)


def write_json(path: Path, value: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, sort_keys=False) + "\n", encoding="utf-8")


def write_text(path: Path, value: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(value, encoding="utf-8")


def relative(path: Path) -> str:
    return path.resolve().relative_to(ROOT).as_posix()


if __name__ == "__main__":
    raise SystemExit(main())
