from __future__ import annotations

import json
import re
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable


ROOT = Path(__file__).resolve().parents[1]
REPORT = ROOT / "reports" / "items_and_blocks_reference.md"


@dataclass(frozen=True)
class ModSpec:
    key: str
    modid: str
    title: str
    items_file: Path | None
    blocks_file: Path | None
    lang_file: Path | None
    recipe_roots: tuple[Path, ...]
    note: str = ""


@dataclass
class Entry:
    mod: ModSpec
    kind: str
    id: str
    name: str
    category: str
    role: str
    description: str
    recipe_note: str

    @property
    def full_id(self) -> str:
        return f"{self.mod.modid}:{self.id}"


MODS = [
    ModSpec(
        key="ashfall",
        modid="echoashfallprotocol",
        title="ECHO: Ashfall Protocol",
        items_file=ROOT / "src/main/java/com/knoxhack/echoashfallprotocol/registry/ModItems.java",
        blocks_file=ROOT / "src/main/java/com/knoxhack/echoashfallprotocol/registry/ModBlocks.java",
        lang_file=ROOT / "src/main/resources/assets/echoashfallprotocol/lang/en_us.json",
        recipe_roots=(ROOT / "src/main/resources/data/echoashfallprotocol/recipe",),
    ),
    ModSpec(
        key="orbital",
        modid="echoorbitalremnants",
        title="ECHO: Orbital Remnants",
        items_file=ROOT / "addons/echoorbitalremnants/src/main/java/com/knoxhack/echoorbitalremnants/registry/ModItems.java",
        blocks_file=ROOT / "addons/echoorbitalremnants/src/main/java/com/knoxhack/echoorbitalremnants/registry/ModBlocks.java",
        lang_file=ROOT / "addons/echoorbitalremnants/src/main/resources/assets/echoorbitalremnants/lang/en_us.json",
        recipe_roots=(ROOT / "addons/echoorbitalremnants/src/main/resources/data/echoorbitalremnants/recipe",),
    ),
    ModSpec(
        key="terminal",
        modid="echoterminal",
        title="ECHO Terminal",
        items_file=None,
        blocks_file=ROOT / "addons/echoterminal/src/main/java/com/knoxhack/echoterminal/registry/ModBlocks.java",
        lang_file=ROOT / "addons/echoterminal/src/main/resources/assets/echoterminal/lang/en_us.json",
        recipe_roots=(ROOT / "addons/echoterminal/src/main/resources/data/echoterminal/recipe",),
    ),
]


ITEM_PATTERNS = (
    re.compile(r'ITEMS\.registerSimpleItem\("([^"]+)"'),
    re.compile(r'ITEMS\.registerItem\("([^"]+)"'),
    re.compile(r'ITEMS\.register\("([^"]+)"'),
    re.compile(r'registerSpawnEgg\("([^"]+)"'),
    re.compile(r'\bregister\("([^"]+)"'),
    re.compile(r'\bsimple\("([^"]+)"'),
    re.compile(r'\barmor\("([^"]+)"'),
    re.compile(r'\bweapon\("([^"]+)"'),
)

BLOCK_PATTERNS = (
    re.compile(r'registerCustomBlock\("([^"]+)"'),
    re.compile(r'BLOCKS\.registerSimpleBlock\("([^"]+)"'),
    re.compile(r'BLOCKS\.registerBlock\("([^"]+)"'),
    re.compile(r'registerSimpleProfessionBlock\("([^"]+)"'),
    re.compile(r'\bmetal\("([^"]+)"'),
    re.compile(r'\bglass\("([^"]+)"'),
    re.compile(r'\bmachine\("([^"]+)"'),
    re.compile(r'\bstone\("([^"]+)"'),
    re.compile(r'\bdust\("([^"]+)"'),
)


ASHFALL_ITEM_DESCRIPTIONS = {
    "dirty_water_bottle": ("Consumable", "Restores hydration in a pinch, but applies nausea because the water is unsafe."),
    "clean_water_bottle": ("Consumable", "Safe water source that restores hydration and a tiny amount of food value."),
    "mutagen_vial": ("Consumable", "Experimental mutation dose; adds radiation and applies weakness/nausea risk."),
    "rad_away": ("Consumable", "Medical anti-rad treatment; reduces current radiation and grants regeneration."),
    "filter_cartridge_basic": ("Gas mask filter", "Basic gas-mask cartridge with 200 durability."),
    "filter_cartridge_advanced": ("Gas mask filter", "Advanced gas-mask cartridge with 600 durability."),
    "filter_cartridge_elite": ("Gas mask filter", "Elite gas-mask cartridge with 1500 durability."),
    "portable_signal_scanner": ("Tool", "Handheld scanner for finding/reading exploration signals."),
    "scrap_knife": ("Tool", "Early scrap tool/weapon with short durability."),
    "gas_mask": ("Armor", "Head-slot respirator; works with filter cartridges for contaminated air protection."),
    "hazmat_helmet": ("Armor", "Hazmat armor piece; each suit piece contributes radiation protection."),
    "hazmat_chestplate": ("Armor", "Hazmat armor piece; contributes radiation protection."),
    "hazmat_leggings": ("Armor", "Hazmat armor piece; contributes radiation protection."),
    "hazmat_boots": ("Armor", "Hazmat armor piece; contributes radiation protection."),
    "basic_battery": ("Power storage", "Portable/basic battery tier used by the power and machine loop."),
    "advanced_battery": ("Power storage", "Upgraded battery tier for larger energy buffers."),
    "elite_battery": ("Power storage", "Top battery tier for late-game energy buffers."),
    "contaminated_iron": ("Contaminated resource", "Radioactive refiner byproduct; process or cleanse before relying on it."),
    "contaminated_gold": ("Contaminated resource", "Radioactive refiner byproduct; process or cleanse before relying on it."),
    "contaminated_redstone": ("Contaminated resource", "Radioactive refiner byproduct; process or cleanse before relying on it."),
    "contaminated_lapis": ("Contaminated resource", "Radioactive refiner byproduct; process or cleanse before relying on it."),
    "alloy_blade": ("Weapon", "Tier 2 alloy melee weapon with armor-piercing behavior."),
    "alloy_hammer": ("Weapon", "Slow heavy alloy weapon with area knockback."),
    "nexus_blade": ("Weapon", "Tier 3 Nexus melee weapon with high damage, piercing, and radiation pressure."),
    "nexus_annihilator": ("Weapon", "Destruction-path endgame weapon with heavy damage, area pressure, and radiation."),
    "nexus_crystal": ("Endgame material", "Rare Nexus material for high-tier equipment and progression crafts."),
    "prefall_archives_key": ("Dimension key", "Key item for entering the Pre-Fall Archives after the Nexus choice."),
    "return_keystone": ("Dimension return", "Return item for leaving the Pre-Fall Archives safely."),
    "scout_drone_item": ("Deployable", "Deploys or manages a scout drone companion/tool."),
    "rare_tech_schematic": ("Research", "Rare research schematic decoded for advanced unlock progress."),
    "bone_knife": ("Tool", "Wilderness-tier knife used before machine progression is secure."),
    "crude_spear": ("Weapon", "Wilderness-tier reach weapon for early survival."),
    "hide_wrap": ("Armor", "Early hide armor wrap for the first wilderness tier."),
    "bandage": ("Consumable", "Heals a small amount and clears poison/bleeding-style pressure."),
    "stim_pack": ("Consumable", "Emergency combat medicine; grants regeneration and speed."),
    "emergency_ration": ("Food", "Compact first-session food source."),
    "power_cell": ("Utility fuel", "Consumed by radio-network fast travel and related utility systems."),
    "thermal_liner": ("Armor upgrade", "Cold-survival upgrade material for cryogenic environments."),
    "hand_warmer": ("Consumable", "Raises body temperature temporarily in cold areas."),
}


ASHFALL_BLOCK_DESCRIPTIONS = {
    "toxic_puddle": ("Hazard", "Shallow toxic hazard; poisons entities that stand in it."),
    "acidic_sludge": ("Hazard", "Corrosive sludge; slows and harms with poison/weakness pressure."),
    "fallout_dust": ("Hazard", "Thin fallout layer that contributes environmental radiation."),
    "radioactive_sludge": ("Hazard", "Radiation-zone sludge; combines poison pressure with radiation exposure."),
    "energized_fissure": ("Hazard", "Glowing Nexus fissure; emits light and deals minor contact danger."),
    "radiation_block": ("Hazard", "Glowing radioactive block for reactor/radiation set dressing."),
    "irradiated_cactus": ("Hazard plant", "Glowing cactus-like plant; hurts and poisons on contact."),
    "oil_stained_concrete": ("Terrain hazard", "Industrial floor block with a slippery/slow movement feel."),
    "wild_berry_bush": ("Survival plant", "Growable berry bush that supplies wild berries."),
    "rain_collector": ("Survival utility", "Early water-collection block for getting water before full purifier setup."),
    "ash_campfire": ("Survival utility", "Ashfall-themed campfire/light and camp dressing block."),
    "mutated_sapling": ("Plant", "Sapling for mutated tree/growth features in the wilderness path."),
    "drop_pod_hull": ("Starter structure", "Structural hull block used by the crash-start drop pod."),
    "drop_pod_glass": ("Starter structure", "Window/glass block used by the crash-start drop pod."),
    "hand_recycler": ("Machine", "Starter machine that recycles early salvage into usable components."),
    "thermal_burner": ("Machine", "Early heat/fuel machine used in survival processing."),
    "water_purifier": ("Machine", "Core early-game machine that converts unsafe water into safe water."),
    "micro_generator": ("Power", "Starter generator for the first powered machine loop."),
    "thermal_array": ("Power", "Mid-tier thermal generator producing stronger power output."),
    "filter_workbench": ("Machine", "Workbench for filter and gas-mask cartridge support."),
    "battery_bank": ("Power storage", "Stores generated energy for machine networks."),
    "scrap_dynamo": ("Power", "Scrap-fueled generator for machinery expansion."),
    "scrap_press": ("Machine", "Presses scrap/materials into denser machine components."),
    "signal_scanner": ("Machine", "Placed scanner for exploration signals and diagnostics."),
    "field_med_bay": ("Machine", "Medical utility station for field recovery workflows."),
    "atmospheric_scrubber": ("Machine", "Air-cleaning machine for contaminated bases or progression systems."),
    "autofeed_hopper": ("Machine integration", "Automation helper for feeding nearby machines."),
    "contaminant_condenser": ("Machine", "Processes/condenses contaminated byproducts."),
    "ore_grinder": ("Geo-extractor", "Grinds biome substrate and ores into trace outputs."),
    "isotope_refiner": ("Geo-extractor", "Refines irradiated/trace materials and produces contaminated byproducts."),
    "crystalline_synthesizer": ("Geo-extractor", "Late machine for Nexus/crystal synthesis stages."),
    "power_node": ("Power grid", "Endgame power-grid node with high durability and active lighting."),
    "nexus_core": ("Endgame", "Central Nexus/endgame block with extreme durability and constant light."),
    "deep_core_miner": ("Endgame machine", "Deep mining machine intended for very low Y-level operation."),
    "radiation_cleanser": ("Endgame machine", "Cleanses contamination using power and filtering resources."),
    "research_lab": ("Research", "Research station for schematic fragments and recipe/perk unlocks."),
    "relay_station": ("Travel", "Radio-network relay used for activated-node travel."),
    "workshop_block": ("Crafting", "Workshop station for advanced construction and repairs."),
    "item_pipe": ("Logistics", "Item transfer pipe for machine automation."),
    "power_cable": ("Power cable", "Basic power cable for local machine networks."),
    "reinforced_power_cable": ("Power cable", "Higher-capacity cable variant rated around 2000 storage / 256 transfer."),
    "high_voltage_power_cable": ("Power cable", "High-capacity cable variant rated around 4000 storage / 1024 transfer."),
    "energy_meter": ("Power utility", "Power-grid diagnostic block for measuring energy networks."),
    "load_distributor": ("Power utility", "Power-grid utility for distributing machine load."),
    "nexus_capacitor": ("Power storage", "Late-game Nexus-styled energy capacitor."),
    "factory_controller": ("Automation", "Factory controller for coordinated machine operation."),
    "weapon_rack": ("Faction workstation", "Radwarden faction profession/workstation block."),
    "supply_crate": ("Faction workstation", "Radwarden faction supply profession/workstation block."),
    "trade_counter": ("Faction workstation", "Crashbreak faction trade profession/workstation block."),
    "map_table": ("Faction workstation", "Crashbreak faction map profession/workstation block."),
    "bio_processing_station": ("Faction workstation", "Sporebound faction bio-processing profession/workstation block."),
    "spore_garden": ("Faction workstation", "Sporebound faction spore/garden profession/workstation block."),
}


ORBITAL_ITEM_DESCRIPTIONS = {
    "echo_terminal": ("Terminal", "Handheld terminal; scans route objectives, shows suit/route/faction diagnostics, and advances Orbital finale checks."),
    "orbital_transponder": ("Progression component", "Signal component for orbital route setup and launch progression."),
    "sealed_suit_fragment": ("Suit component", "Suit-seal salvage used in pressure-suit crafting and readiness."),
    "emergency_rocket": ("Route vehicle", "Launches to low Earth orbit when readiness is complete; also returns from space contexts."),
    "orbital_shuttle": ("Route vehicle", "Moon route vehicle after station/orbital prerequisites are met."),
    "mars_transfer_window": ("Route key", "Unlocks/uses the Mars transfer route after orbital staging gates."),
    "europa_transfer_window": ("Route key", "Unlocks/uses the Europa transfer route after prior route gates."),
    "nexus_drive_vessel": ("Route key", "Deep-space/Nexus route vessel for the endgame path."),
    "oxygen_tank": ("Suit support", "Required launch-readiness suit oxygen item; provides oxygen support."),
    "oxygen_booster": ("Suit module", "Suit module that improves oxygen endurance and can flush reserve oxygen."),
    "emergency_oxygen_cell": ("Consumable", "Manual or automatic emergency oxygen refill at critical oxygen."),
    "suit_sealant_patch": ("Consumable", "Manual or automatic pressure-repair patch when pressure integrity is dangerous."),
    "radiation_visor": ("Suit module", "Suit module for radiation protection/diagnostics."),
    "thermal_space_liner": ("Suit module", "Suit module for temperature regulation in hostile route environments."),
    "jet_burst_module": ("Suit module", "Short movement burst module that spends oxygen."),
    "scanner_visor": ("Suit module", "Route, dimension, and suit diagnostic visor module."),
    "orbital_remnant_badge": ("Faction pledge", "Pledges/activates the Orbital Remnant faction contract path."),
    "void_salvager_marker": ("Faction pledge", "Pledges/activates the Void Salvager faction contract path."),
    "nexus_choir_sigil": ("Faction pledge", "Pledges/activates the Nexus Choir faction contract path."),
    "plasma_cutter": ("Weapon", "Orbital weapon profile for cutting/energy attacks."),
    "rail_spike_launcher": ("Weapon", "Precision ranged launcher; right-click fires rail spike shots."),
    "gravity_hammer": ("Weapon", "Heavy orbital weapon profile with gravity/impact flavor."),
    "solar_lance": ("Weapon", "High-tier solar-energy weapon profile."),
    "nexus_pulse_blade": ("Weapon", "Endgame Nexus weapon profile with fire-resistant rarity."),
}


ORBITAL_BLOCK_DESCRIPTIONS = {
    "launch_platform": ("Launch infrastructure", "Foundation/platform block for rocket launch setups."),
    "rocket_assembly_frame": ("Machine", "Assembles emergency rocket infrastructure when parts are ready."),
    "fuel_refinery": ("Machine", "Fuel-processing machine for orbital vehicle readiness."),
    "oxygen_compressor": ("Machine", "Converts bottles/resources into emergency oxygen cells."),
    "heat_shield_fabricator": ("Machine", "Fabricates heat-shield components for launch readiness."),
    "orbital_fabricator": ("Machine", "General orbital crafting/fabrication station."),
    "vacuum_smelter": ("Machine", "Space-route smelting/refining station."),
    "solar_reclaimer": ("Machine", "Reclaims solar/station salvage into usable route materials."),
    "suit_charging_station": ("Machine", "Converts oxygen canisters into oxygen tanks and supports suit upkeep."),
    "signal_analyzer": ("Machine", "Processes route survey data and Nexus stabilizer shards."),
    "navigation_console": ("Machine", "Navigation station for route diagnostics/progression."),
    "docking_beacon": ("Route infrastructure", "Beacon/docking marker used in station and route builds."),
    "station_life_support_core": ("Route infrastructure", "Station life-support objective/core block."),
    "survey_marker": ("Route objective", "Generated route survey block; scan with the ECHO terminal."),
    "signal_relay": ("Route objective", "Generated signal relay; scan/repair for route progress."),
    "thermal_vent": ("Route objective", "Generated thermal objective, especially for Europa-style route checks."),
    "nexus_anchor": ("Route objective", "Generated Nexus objective used for stabilization/finale progress."),
    "station_relay_node": ("Repair objective", "Station repair node that ties into relay-fuse objectives."),
    "helium_extractor_node": ("Repair objective", "Moon/helium route objective tied to extractor-core repair."),
    "mars_pressure_console": ("Repair objective", "Mars route objective tied to pressure regulation repair."),
    "europa_thermal_array": ("Repair objective", "Europa route objective tied to probe/thermal repair."),
}


TERMINAL_BLOCK_DESCRIPTIONS = {
    "echo_terminal": ("Terminal", "Standalone ECHO Terminal block; lighted metal terminal for accessing ECHO terminal interactions."),
}


def read_json(path: Path | None) -> dict:
    if not path or not path.exists():
        return {}
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def clean_category(raw: str) -> str:
    cleaned = raw.strip().strip("-").strip()
    cleaned = cleaned.replace("&", "and")
    cleaned = re.sub(r"\s+", " ", cleaned)
    return ascii_sanitize(cleaned.title())


def extract_ids(path: Path | None, kind: str) -> list[tuple[str, str]]:
    if not path or not path.exists():
        return []
    patterns = ITEM_PATTERNS if kind == "item" else BLOCK_PATTERNS
    category = "General"
    entries: list[tuple[str, str]] = []
    seen: set[str] = set()

    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        category_match = re.search(r"//\s*===\s*(.*?)\s*===", line)
        if category_match:
            category = clean_category(category_match.group(1))

        for pattern in patterns:
            match = pattern.search(line)
            if not match:
                continue
            item_id = match.group(1)
            if item_id in seen:
                break
            seen.add(item_id)
            entries.append((item_id, category))
            break

    return entries


def title_from_id(item_id: str) -> str:
    special = {
        "echo": "ECHO",
        "nexus": "Nexus",
        "rad": "Rad",
        "ai": "AI",
        "leo": "LEO",
        "europa": "Europa",
        "mars": "Mars",
    }
    return " ".join(special.get(part, part.capitalize()) for part in item_id.split("_"))


def ascii_sanitize(text: str) -> str:
    return (
        text.replace("\u2014", "-")
        .replace("\u2013", "-")
        .replace("\u2018", "'")
        .replace("\u2019", "'")
        .replace("\u201c", '"')
        .replace("\u201d", '"')
    )


def display_name(mod: ModSpec, kind: str, item_id: str, lang: dict) -> str:
    key = f"{kind}.{mod.modid}.{item_id}"
    return ascii_sanitize(lang.get(key, title_from_id(item_id)))


def role_for_item(mod: ModSpec, item_id: str, category: str) -> str:
    exact = {}
    if mod.key == "ashfall":
        exact.update({k: v[0] for k, v in ASHFALL_ITEM_DESCRIPTIONS.items()})
    elif mod.key == "orbital":
        exact.update({k: v[0] for k, v in ORBITAL_ITEM_DESCRIPTIONS.items()})
    if item_id in exact:
        return exact[item_id]
    if item_id.endswith("_spawn_egg"):
        return "Spawn egg"
    if item_id.startswith("data_log_"):
        return "Lore data log"
    if item_id.startswith("schematic_fragment"):
        return "Research"
    if "battery" in item_id or item_id in {"energy_cell", "power_cell"}:
        return "Power component"
    if item_id.startswith("machine_upgrade_"):
        return "Machine upgrade"
    if any(token in item_id for token in ("helmet", "chestplate", "leggings", "boots")):
        return "Armor"
    if any(token in item_id for token in ("blade", "hammer", "spear", "knife", "lance", "launcher", "cutter")):
        return "Weapon"
    if any(token in item_id for token in ("ration", "berry", "water", "bandage", "stim", "warmer")):
        return "Consumable"
    if item_id in {
        "rocket_nose_cone",
        "salvaged_engine",
        "fuel_tank",
        "heat_shield_plate",
        "landing_gear",
        "cargo_bay_module",
        "life_support_module",
        "echo_flight_core",
        "navigation_computer",
    }:
        return "Rocket component"
    if item_id in {
        "orbit_survey_data",
        "lunar_core_sample",
        "martian_pressure_valve",
        "europa_thermal_probe",
        "nexus_stabilizer_shard",
        "stabilized_echo_core",
        "station_relay_fuse",
        "station_power_matrix",
        "helium_extractor_core",
        "lunar_pressure_map",
        "martian_habitat_key",
        "pressure_regulator",
        "europa_probe_array",
        "thermal_stabilizer",
    }:
        return "Route objective"
    if category and category != "General":
        return category
    return "Material"


def role_for_block(mod: ModSpec, item_id: str, category: str) -> str:
    exact = {}
    if mod.key == "ashfall":
        exact.update({k: v[0] for k, v in ASHFALL_BLOCK_DESCRIPTIONS.items()})
    elif mod.key == "orbital":
        exact.update({k: v[0] for k, v in ORBITAL_BLOCK_DESCRIPTIONS.items()})
    elif mod.key == "terminal":
        exact.update({k: v[0] for k, v in TERMINAL_BLOCK_DESCRIPTIONS.items()})
    if item_id in exact:
        return exact[item_id]
    if any(token in item_id for token in ("sludge", "toxic", "fallout", "radiation", "radioactive", "fissure", "uranium", "irradiated")):
        return "Hazard"
    if any(token in item_id for token in ("grass", "bush", "reed", "sapling", "leaves", "fungus", "fern", "thorn", "cactus", "wheat")):
        return "Vegetation"
    if any(token in item_id for token in ("ore", "aggregate", "slagstone", "shale", "riftstone", "crystal", "dust", "rock", "basalt", "regolith")):
        return "Resource terrain"
    if any(token in item_id for token in ("dirt", "soil", "stone", "ash", "earth", "permafrost", "concrete", "rubble")):
        return "Terrain"
    if category and category != "General":
        return category
    return "Decorative block"


def item_description(mod: ModSpec, item_id: str, role: str) -> str:
    if mod.key == "ashfall" and item_id in ASHFALL_ITEM_DESCRIPTIONS:
        return ASHFALL_ITEM_DESCRIPTIONS[item_id][1]
    if mod.key == "orbital" and item_id in ORBITAL_ITEM_DESCRIPTIONS:
        return ORBITAL_ITEM_DESCRIPTIONS[item_id][1]
    if item_id.endswith("_spawn_egg"):
        mob = title_from_id(item_id.removesuffix("_spawn_egg"))
        return f"Creative/admin spawn egg for the {mob} entity."
    if item_id.startswith("data_log_"):
        return "Readable lore artifact; right-click archives the entry and consumes the log."
    if item_id.startswith("schematic_fragment_"):
        track = title_from_id(item_id.removeprefix("schematic_fragment_"))
        return f"Research fragment for the {track} unlock track."
    if item_id == "schematic_fragment":
        return "Generic early schematic fragment used to start guided machine progression."
    if item_id.startswith("machine_upgrade_"):
        upgrade = title_from_id(item_id.removeprefix("machine_upgrade_"))
        return f"Machine upgrade part for {upgrade.lower()} tuning."
    if item_id in {"scrap_metal", "scrap_wire", "scrap_circuit", "scrap_plastic"}:
        return "Common salvage material used across starter machines, components, and repairs."
    if item_id in {"ash", "circuit_board", "energy_cell", "filtration_membrane", "machine_casing", "mutated_tissue"}:
        return "Crafting component for survival machines, filters, and mid-progression equipment."
    if item_id in {"plant_fiber", "fiber_rope", "animal_bone", "animal_hide"}:
        return "Wilderness-tier material for early tools, shelter, and hide gear."
    if item_id in {"iron_shard", "copper_shard", "coal_dust", "gold_trace", "crystal_dust", "gem_fragment", "dense_alloy_chunk", "uranium_shard"}:
        return "Trace resource from biome substrate processing; feed into refining/crafting loops."
    if item_id in {"gold_cluster", "scrap_iron_bundle"}:
        return "Processed resource bundle from the scrap/geo-extraction economy."
    if item_id.startswith("alloy_"):
        return "Tier 2 alloy equipment piece with stronger protection/durability than early survival gear."
    if item_id.startswith("nexus_") and any(slot in item_id for slot in ("helmet", "chestplate", "leggings", "boots")):
        return "Tier 3 Nexus armor piece with high protection and Nexus-energy bonuses."
    if mod.key == "orbital" and item_id.startswith("pressurized_"):
        return "Pressurized suit armor piece for orbital exposure readiness."
    if mod.key == "orbital" and item_id == "magnetic_boots":
        return "Pressurized suit boots for orbital exposure readiness."
    if mod.key == "orbital" and item_id in {
        "rocket_nose_cone",
        "salvaged_engine",
        "fuel_tank",
        "heat_shield_plate",
        "landing_gear",
        "cargo_bay_module",
        "life_support_module",
        "echo_flight_core",
        "navigation_computer",
    }:
        return "Rocket assembly component for launch readiness and vehicle construction."
    if mod.key == "orbital" and item_id in {
        "orbital_alloy",
        "vacuum_circuit",
        "frozen_wiring",
        "navigation_chip",
        "oxygen_canister",
        "cryo_battery",
        "lunar_titanium",
        "helium_3_cell",
        "martian_silica",
        "cryo_crystal",
        "nexus_dust",
        "lunar_core_fragment",
        "nexus_drive_core",
    }:
        return "Orbital route material used for machines, route keys, suit support, or late-game fabrication."
    if mod.key == "orbital" and role == "Route objective":
        return "Route-specific key/sample/repair item used by scans, machines, or route unlock checks."
    return "Crafting/progression item used by the mod's recipes, rewards, or machine systems."


def block_description(mod: ModSpec, item_id: str, role: str) -> str:
    if mod.key == "ashfall" and item_id in ASHFALL_BLOCK_DESCRIPTIONS:
        return ASHFALL_BLOCK_DESCRIPTIONS[item_id][1]
    if mod.key == "orbital" and item_id in ORBITAL_BLOCK_DESCRIPTIONS:
        return ORBITAL_BLOCK_DESCRIPTIONS[item_id][1]
    if mod.key == "terminal" and item_id in TERMINAL_BLOCK_DESCRIPTIONS:
        return TERMINAL_BLOCK_DESCRIPTIONS[item_id][1]
    if role == "Hazard":
        return "Biome hazard/set-dressing block that visually marks danger and may apply environmental pressure."
    if role == "Vegetation":
        return "Worldgen vegetation or clutter block used to define biome silhouette and resource flavor."
    if role == "Resource terrain":
        return "Biome-specific terrain/resource block used by worldgen and substrate processing visuals."
    if role == "Terrain":
        return "Biome surface, rubble, or foundation block for terrain and structure dressing."
    if mod.key == "orbital":
        if item_id.endswith("_block"):
            return "Compact storage/decorative block for the matching orbital material."
        if any(token in item_id for token in ("glass", "ice", "growth", "panel")):
            return "Decorative route block for station, lunar, Europa, or Nexus site dressing."
        return "Orbital route building/decorative block used in structures and route terrain."
    return "Decorative/placeable block used by worldgen, POIs, structures, or player building."


def iter_json_values(value) -> Iterable[str]:
    if isinstance(value, dict):
        for key, child in value.items():
            if key in {"item", "id"} and isinstance(child, str):
                yield child
            else:
                yield from iter_json_values(child)
    elif isinstance(value, list):
        for child in value:
            yield from iter_json_values(child)


def recipe_usage(mods: list[ModSpec]) -> tuple[dict[str, list[str]], dict[str, list[str]]]:
    produced: dict[str, list[str]] = defaultdict(list)
    consumed: dict[str, list[str]] = defaultdict(list)
    modids = {mod.modid for mod in mods}

    for mod in mods:
        for root in mod.recipe_roots:
            if not root.exists():
                continue
            for path in sorted(root.rglob("*.json")):
                rel = path.relative_to(ROOT).as_posix()
                try:
                    data = json.loads(path.read_text(encoding="utf-8"))
                except json.JSONDecodeError:
                    continue

                result = data.get("result")
                result_ids: set[str] = set()
                if isinstance(result, str):
                    result_ids.add(result)
                elif isinstance(result, dict):
                    for key in ("item", "id"):
                        if isinstance(result.get(key), str):
                            result_ids.add(result[key])

                for result_id in result_ids:
                    if result_id.split(":", 1)[0] in modids:
                        produced[result_id].append(rel)

                for found in iter_json_values({k: v for k, v in data.items() if k != "result"}):
                    if found.split(":", 1)[0] in modids:
                        consumed[found].append(rel)

    return produced, consumed


def recipe_note(full_id: str, produced: dict[str, list[str]], consumed: dict[str, list[str]]) -> str:
    parts: list[str] = []
    if full_id in produced:
        parts.append(f"crafted/output by {len(produced[full_id])} recipe(s)")
    if full_id in consumed:
        parts.append(f"used in {len(consumed[full_id])} recipe(s)")
    return "; ".join(parts) if parts else "no direct JSON recipe use found"


def build_entries() -> dict[str, dict[str, list[Entry]]]:
    produced, consumed = recipe_usage(MODS)
    by_mod: dict[str, dict[str, list[Entry]]] = {}

    for mod in MODS:
        lang = read_json(mod.lang_file)
        item_entries: list[Entry] = []
        block_entries: list[Entry] = []

        for item_id, category in extract_ids(mod.items_file, "item"):
            role = role_for_item(mod, item_id, category)
            item_entries.append(
                Entry(
                    mod=mod,
                    kind="item",
                    id=item_id,
                    name=display_name(mod, "item", item_id, lang),
                    category=category,
                    role=role,
                    description=item_description(mod, item_id, role),
                    recipe_note=recipe_note(f"{mod.modid}:{item_id}", produced, consumed),
                )
            )

        for block_id, category in extract_ids(mod.blocks_file, "block"):
            role = role_for_block(mod, block_id, category)
            block_entries.append(
                Entry(
                    mod=mod,
                    kind="block",
                    id=block_id,
                    name=display_name(mod, "block", block_id, lang),
                    category=category,
                    role=role,
                    description=block_description(mod, block_id, role),
                    recipe_note=recipe_note(f"{mod.modid}:{block_id}", produced, consumed),
                )
            )

        by_mod[mod.key] = {"items": item_entries, "blocks": block_entries}

    return by_mod


def markdown_table(entries: list[Entry]) -> list[str]:
    lines = [
        "| ID | Name | Role | What it does | Recipe links |",
        "| --- | --- | --- | --- | --- |",
    ]
    for entry in entries:
        lines.append(
            f"| `{entry.full_id}` | {entry.name} | {entry.role} | {entry.description} | {entry.recipe_note} |"
        )
    return lines


def write_report() -> None:
    by_mod = build_entries()
    standalone_items = sum(len(groups["items"]) for groups in by_mod.values())
    blocks = sum(len(groups["blocks"]) for groups in by_mod.values())
    placeable_item_forms = blocks
    total_usable = standalone_items + blocks

    lines: list[str] = [
        "# ECHO Item and Block Reference",
        "",
        "Generated from the Java registries, language files, and recipe JSON in this workspace.",
        "Block item forms are covered by the block tables, so the item tables list standalone non-block items only.",
        "",
        "## Totals",
        "",
        "| Scope | Count |",
        "| --- | ---: |",
        f"| Standalone item IDs | {standalone_items} |",
        f"| Block IDs | {blocks} |",
        f"| Placeable block-item forms | {placeable_item_forms} |",
        f"| Total usable item/block entries | {total_usable} |",
        "",
    ]

    for mod in MODS:
        groups = by_mod[mod.key]
        lines.extend(
            [
                f"## {mod.title} (`{mod.modid}`)",
                "",
            ]
        )
        if mod.key == "terminal":
            lines.append("This addon exposes its terminal as one placeable block with a matching block-item form.")
            lines.append("")

        if groups["items"]:
            lines.extend([f"### Standalone Items ({len(groups['items'])})", ""])
            lines.extend(markdown_table(groups["items"]))
            lines.append("")
        else:
            lines.extend(["### Standalone Items (0)", "", "No standalone non-block items are registered by this module.", ""])

        lines.extend([f"### Blocks ({len(groups['blocks'])})", ""])
        if groups["blocks"]:
            lines.extend(markdown_table(groups["blocks"]))
        else:
            lines.append("No blocks are registered by this module.")
        lines.append("")

    lines.extend(
        [
            "## ECHO Core (`echocore`)",
            "",
            "No item or block registries were found for ECHO Core in this workspace. It functions as shared code/services for the ECHO mods rather than adding player-visible item or block IDs.",
            "",
            "## Sources",
            "",
            "- `src/main/java/com/knoxhack/echoashfallprotocol/registry/ModItems.java`",
            "- `src/main/java/com/knoxhack/echoashfallprotocol/registry/ModBlocks.java`",
            "- `addons/echoorbitalremnants/src/main/java/com/knoxhack/echoorbitalremnants/registry/ModItems.java`",
            "- `addons/echoorbitalremnants/src/main/java/com/knoxhack/echoorbitalremnants/registry/ModBlocks.java`",
            "- `addons/echoterminal/src/main/java/com/knoxhack/echoterminal/registry/ModBlocks.java`",
            "- `assets/*/lang/en_us.json` and `data/*/recipe/*.json` trees for names and recipe-link notes.",
            "",
        ]
    )

    REPORT.parent.mkdir(parents=True, exist_ok=True)
    REPORT.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {REPORT}")


if __name__ == "__main__":
    write_report()
