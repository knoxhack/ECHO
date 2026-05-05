"""
Shape registry mapping each POI template name to its generator function.
"""

from typing import Callable, Dict, List, Tuple, Optional

from .crash_zone import (
    generate_scrap_pile_small,
    generate_scrap_pile_medium,
    generate_wreckage_cluster,
    generate_ash_covered_ruin,
)
from .ruined_cityscape import (
    generate_collapsed_building_small,
    generate_collapsed_building_tall,
    generate_street_barricade,
    generate_parking_ruin,
)
from .radiation_zone import (
    generate_containment_breach,
    generate_waste_barrel_cluster,
    generate_irradiated_vehicle,
    generate_radiation_crater,
)
from .toxic_swamp import (
    generate_chemical_spill,
    generate_broken_pipeline,
    generate_abandoned_shed,
    generate_toxic_pool_small,
)
from .industrial_ruins import (
    generate_conveyor_ruin,
    generate_storage_yard,
    generate_crane_wreck,
    generate_pipe_cluster,
)
from .cryogenic_ruins import (
    generate_frozen_vehicle,
    generate_ice_covered_ruin,
    generate_broken_tank,
    generate_frozen_cache,
)
from .ruined_plains import (
    generate_nomad_camp,
    generate_windmill_ruin,
    generate_impact_crater,
    generate_supply_drop,
)
from .global_pois import (
    generate_debris_field_small,
    generate_debris_field_large,
    generate_survivor_cache,
    generate_radio_relay_small,
    generate_abandoned_camp,
    generate_road_wreck,
)
from .special_structures import (
    generate_bio_lab,
    generate_military_vault,
    generate_data_center_ruin,
    generate_reactor_ruin,
)
from .drop_pods import generate_drop_pod
from .industrial_complex import (
    generate_industrial_factory,
    generate_subway_station,
)
from .ruined_plains import generate_scavenger_camp
from .faction_villages import FACTION_GENERATORS

# Medium and Big structure imports
from .ruined_plains_medium import (
    generate_walled_encampment,
    generate_abandoned_homestead,
    generate_trader_post,
)
from .ruined_plains_big import (
    generate_ruined_outpost,
    generate_settlement_ruins,
)
from .crash_zone_medium import (
    generate_crashbreak_worksite,
    generate_crash_site_large,
    generate_radiation_field,
)
from .crash_zone_big import (
    generate_ship_breaking_yard,
    generate_containment_facility_ruin,
)
from .special_structures_v2 import (
    generate_bio_facility,
    generate_bunker_complex,
    generate_server_farm,
    generate_power_plant_ruin,
)
from .biome_landmarks import (
    generate_cargo_module_field,
    generate_collapsed_tower_large,
    generate_corroded_pipe_network,
    generate_drop_pod_wreck_large,
    generate_floating_obelisk_cluster,
    generate_frozen_lab_large,
    generate_industrial_factory_shell,
    generate_nexus_pylon,
    generate_reactor_containment_ruin,
    generate_wasteland_bunker_ruin,
)
from .poi_expansion import (
    generate_burned_convoy,
    generate_cargo_lift_wreck,
    generate_cryo_tank_field,
    generate_factory_pipe_gate,
    generate_frozen_comms_tower,
    generate_pipe_pump_house,
    generate_radiation_beacon_line,
    generate_rail_signal_yard,
    generate_reactor_gatehouse,
    generate_road_checkpoint,
    generate_sludge_drain,
    generate_subway_stairwell,
)

BlockList = List[Tuple[int, int, int, str, Optional[Dict[str, str]]]]
Generator = Callable[[int], BlockList]

SHAPE_REGISTRY: Dict[str, Generator] = {
    # crash_zone_wasteland
    "scrap_pile_small": generate_scrap_pile_small,
    "scrap_pile_medium": generate_scrap_pile_medium,
    "wreckage_cluster": generate_wreckage_cluster,
    "ash_covered_ruin": generate_ash_covered_ruin,
    # ruined_cityscape
    "collapsed_building_small": generate_collapsed_building_small,
    "collapsed_building_tall": generate_collapsed_building_tall,
    "street_barricade": generate_street_barricade,
    "parking_ruin": generate_parking_ruin,
    # radiation_zone
    "containment_breach": generate_containment_breach,
    "waste_barrel_cluster": generate_waste_barrel_cluster,
    "irradiated_vehicle": generate_irradiated_vehicle,
    "radiation_crater": generate_radiation_crater,
    # toxic_swamp
    "chemical_spill": generate_chemical_spill,
    "broken_pipeline": generate_broken_pipeline,
    "abandoned_shed": generate_abandoned_shed,
    "toxic_pool_small": generate_toxic_pool_small,
    # industrial_ruins
    "conveyor_ruin": generate_conveyor_ruin,
    "storage_yard": generate_storage_yard,
    "crane_wreck": generate_crane_wreck,
    "pipe_cluster": generate_pipe_cluster,
    # cryogenic_ruins
    "frozen_vehicle": generate_frozen_vehicle,
    "ice_covered_ruin": generate_ice_covered_ruin,
    "broken_tank": generate_broken_tank,
    "frozen_cache": generate_frozen_cache,
    # ruined_plains
    "nomad_camp": generate_nomad_camp,
    "windmill_ruin": generate_windmill_ruin,
    "impact_crater": generate_impact_crater,
    "supply_drop": generate_supply_drop,
    "scavenger_camp": generate_scavenger_camp,
    # global
    "debris_field_small": generate_debris_field_small,
    "debris_field_large": generate_debris_field_large,
    "survivor_cache": generate_survivor_cache,
    "radio_relay_small": generate_radio_relay_small,
    "abandoned_camp": generate_abandoned_camp,
    "road_wreck": generate_road_wreck,
    # special structures (from template pool audit)
    "bio_lab": generate_bio_lab,
    "military_vault": generate_military_vault,
    "data_center_ruin": generate_data_center_ruin,
    "reactor_ruin": generate_reactor_ruin,
    "drop_pod": generate_drop_pod,
    "industrial_factory": generate_industrial_factory,
    "subway_station": generate_subway_station,

    # Enhanced special structures v2 (big landmarks)
    "bio_facility": generate_bio_facility,
    "bunker_complex": generate_bunker_complex,
    "server_farm": generate_server_farm,
    "power_plant_ruin": generate_power_plant_ruin,

    # Ruined Plains - Medium structures
    "walled_encampment": generate_walled_encampment,
    "abandoned_homestead": generate_abandoned_homestead,
    "trader_post": generate_trader_post,

    # Ruined Plains - Big structures
    "ruined_outpost": generate_ruined_outpost,
    "settlement_ruins": generate_settlement_ruins,

    # Crash Zone - Medium structures
    "crashbreak_worksite": generate_crashbreak_worksite,
    "crash_site_large": generate_crash_site_large,
    "radiation_field": generate_radiation_field,

    # Crash Zone - Big structures
    "ship_breaking_yard": generate_ship_breaking_yard,
    "containment_facility_ruin": generate_containment_facility_ruin,

    # Rare biome-overhaul skyline landmarks
    "nexus_pylon": generate_nexus_pylon,
    "floating_obelisk_cluster": generate_floating_obelisk_cluster,
    "drop_pod_wreck_large": generate_drop_pod_wreck_large,
    "cargo_module_field": generate_cargo_module_field,
    "wasteland_bunker_ruin": generate_wasteland_bunker_ruin,
    "collapsed_tower_large": generate_collapsed_tower_large,
    "corroded_pipe_network": generate_corroded_pipe_network,
    "reactor_containment_ruin": generate_reactor_containment_ruin,
    "frozen_lab_large": generate_frozen_lab_large,
    "industrial_factory_shell": generate_industrial_factory_shell,

    # Full POI polish expansion
    "burned_convoy": generate_burned_convoy,
    "cargo_lift_wreck": generate_cargo_lift_wreck,
    "road_checkpoint": generate_road_checkpoint,
    "subway_stairwell": generate_subway_stairwell,
    "sludge_drain": generate_sludge_drain,
    "pipe_pump_house": generate_pipe_pump_house,
    "radiation_beacon_line": generate_radiation_beacon_line,
    "reactor_gatehouse": generate_reactor_gatehouse,
    "frozen_comms_tower": generate_frozen_comms_tower,
    "cryo_tank_field": generate_cryo_tank_field,
    "rail_signal_yard": generate_rail_signal_yard,
    "factory_pipe_gate": generate_factory_pipe_gate,

    # faction village structures
    **FACTION_GENERATORS,
}
