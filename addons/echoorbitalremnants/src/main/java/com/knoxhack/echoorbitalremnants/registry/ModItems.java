package com.knoxhack.echoorbitalremnants.registry;

import com.knoxhack.echoorbitalremnants.EchoOrbitalRemnants;
import com.knoxhack.echoorbitalremnants.item.EchoTerminalItem;
import com.knoxhack.echoorbitalremnants.item.EmergencyRocketItem;
import com.knoxhack.echoorbitalremnants.item.EmergencyOxygenCellItem;
import com.knoxhack.echoorbitalremnants.item.FactionPledgeItem;
import com.knoxhack.echoorbitalremnants.item.NexusDriveVesselItem;
import com.knoxhack.echoorbitalremnants.item.OrbitalShuttleItem;
import com.knoxhack.echoorbitalremnants.item.OrbitalWeaponItem;
import com.knoxhack.echoorbitalremnants.item.PlanetaryRouteItem;
import com.knoxhack.echoorbitalremnants.item.SuitModuleItem;
import com.knoxhack.echoorbitalremnants.item.SuitSealantPatchItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EchoOrbitalRemnants.MODID);

    private static final List<DeferredItem<? extends Item>> CREATIVE_ITEMS = new ArrayList<>();

    public static final DeferredItem<Item> ECHO_TERMINAL = tracked(ITEMS.registerItem("echo_terminal", EchoTerminalItem::new,
            p -> p.stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> ORBITAL_TRANSPONDER = simple("orbital_transponder", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SEALED_SUIT_FRAGMENT = simple("sealed_suit_fragment", p -> p.rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> EMERGENCY_ROCKET = tracked(ITEMS.registerItem("emergency_rocket", EmergencyRocketItem::new,
            p -> p.stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> ORBITAL_SHUTTLE = tracked(ITEMS.registerItem("orbital_shuttle", OrbitalShuttleItem::new,
            p -> p.stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> MARS_TRANSFER_WINDOW = tracked(ITEMS.registerItem("mars_transfer_window",
            properties -> new PlanetaryRouteItem(PlanetaryRouteItem.Target.MARS, properties),
            p -> p.stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> EUROPA_TRANSFER_WINDOW = tracked(ITEMS.registerItem("europa_transfer_window",
            properties -> new PlanetaryRouteItem(PlanetaryRouteItem.Target.EUROPA, properties),
            p -> p.stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> SATURN_TRANSFER_WINDOW = tracked(ITEMS.registerItem("saturn_transfer_window",
            properties -> new PlanetaryRouteItem(PlanetaryRouteItem.Target.SATURN, properties),
            p -> p.stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> TITAN_TRANSFER_WINDOW = tracked(ITEMS.registerItem("titan_transfer_window",
            properties -> new PlanetaryRouteItem(PlanetaryRouteItem.Target.TITAN, properties),
            p -> p.stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> NEXUS_DRIVE_VESSEL = tracked(ITEMS.registerItem("nexus_drive_vessel", NexusDriveVesselItem::new,
            p -> p.stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    public static final DeferredItem<Item> PRESSURIZED_HELMET = armor("pressurized_helmet", ArmorType.HELMET);
    public static final DeferredItem<Item> PRESSURIZED_CHESTPLATE = armor("pressurized_chestplate", ArmorType.CHESTPLATE);
    public static final DeferredItem<Item> PRESSURIZED_LEGGINGS = armor("pressurized_leggings", ArmorType.LEGGINGS);
    public static final DeferredItem<Item> MAGNETIC_BOOTS = armor("magnetic_boots", ArmorType.BOOTS);
    public static final DeferredItem<Item> OXYGEN_TANK = simple("oxygen_tank", p -> p.stacksTo(1).durability(240));
    public static final DeferredItem<Item> OXYGEN_BOOSTER = tracked(ITEMS.registerItem("oxygen_booster",
            properties -> new SuitModuleItem(SuitModuleItem.Module.OXYGEN_BOOSTER, properties),
            p -> p.stacksTo(1).durability(160)));
    public static final DeferredItem<Item> EMERGENCY_OXYGEN_CELL = tracked(ITEMS.registerItem("emergency_oxygen_cell", EmergencyOxygenCellItem::new,
            p -> p.stacksTo(16)));
    public static final DeferredItem<Item> SUIT_SEALANT_PATCH = tracked(ITEMS.registerItem("suit_sealant_patch", SuitSealantPatchItem::new,
            p -> p.stacksTo(16)));
    public static final DeferredItem<Item> RADIATION_VISOR = tracked(ITEMS.registerItem("radiation_visor",
            properties -> new SuitModuleItem(SuitModuleItem.Module.RADIATION_VISOR, properties),
            p -> p.stacksTo(1)));
    public static final DeferredItem<Item> THERMAL_SPACE_LINER = tracked(ITEMS.registerItem("thermal_space_liner",
            properties -> new SuitModuleItem(SuitModuleItem.Module.THERMAL_REGULATOR, properties),
            p -> p.stacksTo(1)));
    public static final DeferredItem<Item> JET_BURST_MODULE = tracked(ITEMS.registerItem("jet_burst_module",
            properties -> new SuitModuleItem(SuitModuleItem.Module.JET_BURST, properties),
            p -> p.stacksTo(1).durability(180)));
    public static final DeferredItem<Item> SCANNER_VISOR = tracked(ITEMS.registerItem("scanner_visor",
            properties -> new SuitModuleItem(SuitModuleItem.Module.SCANNER, properties),
            p -> p.stacksTo(1)));

    public static final DeferredItem<Item> ROCKET_NOSE_CONE = simple("rocket_nose_cone");
    public static final DeferredItem<Item> SALVAGED_ENGINE = simple("salvaged_engine");
    public static final DeferredItem<Item> FUEL_TANK = simple("fuel_tank");
    public static final DeferredItem<Item> HEAT_SHIELD_PLATE = simple("heat_shield_plate");
    public static final DeferredItem<Item> LANDING_GEAR = simple("landing_gear");
    public static final DeferredItem<Item> CARGO_BAY_MODULE = simple("cargo_bay_module");
    public static final DeferredItem<Item> LIFE_SUPPORT_MODULE = simple("life_support_module");
    public static final DeferredItem<Item> ECHO_FLIGHT_CORE = simple("echo_flight_core", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> NAVIGATION_COMPUTER = simple("navigation_computer", p -> p.rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> ORBITAL_ALLOY = simple("orbital_alloy");
    public static final DeferredItem<Item> VACUUM_CIRCUIT = simple("vacuum_circuit");
    public static final DeferredItem<Item> FROZEN_WIRING = simple("frozen_wiring");
    public static final DeferredItem<Item> NAVIGATION_CHIP = simple("navigation_chip");
    public static final DeferredItem<Item> OXYGEN_CANISTER = simple("oxygen_canister");
    public static final DeferredItem<Item> CRYO_BATTERY = simple("cryo_battery");
    public static final DeferredItem<Item> LUNAR_TITANIUM = simple("lunar_titanium");
    public static final DeferredItem<Item> HELIUM_3_CELL = simple("helium_3_cell");
    public static final DeferredItem<Item> MARTIAN_SILICA = simple("martian_silica");
    public static final DeferredItem<Item> CRYO_CRYSTAL = simple("cryo_crystal");
    public static final DeferredItem<Item> NEXUS_DUST = simple("nexus_dust", p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> LUNAR_CORE_FRAGMENT = simple("lunar_core_fragment", p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> NEXUS_DRIVE_CORE = simple("nexus_drive_core", p -> p.rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<Item> ORBITAL_BLACK_BOX = simple("orbital_black_box", p -> p.rarity(Rarity.RARE).fireResistant());
    public static final DeferredItem<Item> ORBIT_SURVEY_DATA = simple("orbit_survey_data", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> LUNAR_CORE_SAMPLE = simple("lunar_core_sample", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> MARTIAN_PRESSURE_VALVE = simple("martian_pressure_valve", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> EUROPA_THERMAL_PROBE = simple("europa_thermal_probe", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SATURN_RING_FRAGMENT = simple("saturn_ring_fragment", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SATURN_RELAY_LENS = simple("saturn_relay_lens", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> TITAN_METHANE_CELL = simple("titan_methane_cell", p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> TITAN_SURVEY_CORE = simple("titan_survey_core", p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> NEXUS_STABILIZER_SHARD = simple("nexus_stabilizer_shard", p -> p.rarity(Rarity.RARE).fireResistant());
    public static final DeferredItem<Item> STABILIZED_ECHO_CORE = simple("stabilized_echo_core", p -> p.rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<Item> STATION_RELAY_FUSE = simple("station_relay_fuse", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> STATION_POWER_MATRIX = simple("station_power_matrix", p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> HELIUM_EXTRACTOR_CORE = simple("helium_extractor_core", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> LUNAR_PRESSURE_MAP = simple("lunar_pressure_map", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> MARTIAN_HABITAT_KEY = simple("martian_habitat_key", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PRESSURE_REGULATOR = simple("pressure_regulator", p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> EUROPA_PROBE_ARRAY = simple("europa_probe_array", p -> p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> THERMAL_STABILIZER = simple("thermal_stabilizer", p -> p.rarity(Rarity.RARE));

    public static final DeferredItem<Item> ORBITAL_REMNANT_BADGE = tracked(ITEMS.registerItem("orbital_remnant_badge",
            properties -> new FactionPledgeItem(FactionPledgeItem.Faction.ORBITAL_REMNANT, properties),
            p -> p.stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> VOID_SALVAGER_MARKER = tracked(ITEMS.registerItem("void_salvager_marker",
            properties -> new FactionPledgeItem(FactionPledgeItem.Faction.VOID_SALVAGERS, properties),
            p -> p.stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> NEXUS_CHOIR_SIGIL = tracked(ITEMS.registerItem("nexus_choir_sigil",
            properties -> new FactionPledgeItem(FactionPledgeItem.Faction.NEXUS_CHOIR, properties),
            p -> p.stacksTo(1).rarity(Rarity.RARE).fireResistant()));

    public static final DeferredItem<Item> PLASMA_CUTTER = weapon("plasma_cutter", OrbitalWeaponItem.WeaponProfile.PLASMA_CUTTER, p -> p.stacksTo(1).durability(512));
    public static final DeferredItem<Item> RAIL_SPIKE_LAUNCHER = weapon("rail_spike_launcher", OrbitalWeaponItem.WeaponProfile.RAIL_SPIKE_LAUNCHER, p -> p.stacksTo(1).durability(384));
    public static final DeferredItem<Item> GRAVITY_HAMMER = weapon("gravity_hammer", OrbitalWeaponItem.WeaponProfile.GRAVITY_HAMMER, p -> p.stacksTo(1).durability(640).rarity(Rarity.RARE));
    public static final DeferredItem<Item> SOLAR_LANCE = weapon("solar_lance", OrbitalWeaponItem.WeaponProfile.SOLAR_LANCE, p -> p.stacksTo(1).durability(768).rarity(Rarity.RARE));
    public static final DeferredItem<Item> NEXUS_PULSE_BLADE = weapon("nexus_pulse_blade", OrbitalWeaponItem.WeaponProfile.NEXUS_PULSE_BLADE, p -> p.stacksTo(1).durability(1024).rarity(Rarity.EPIC).fireResistant());

    static {
        ModBlocks.ALL_BLOCKS.forEach(block -> tracked(ITEMS.registerSimpleBlockItem(block)));
    }

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static List<DeferredItem<? extends Item>> creativeItems() {
        return List.copyOf(CREATIVE_ITEMS);
    }

    private static DeferredItem<Item> armor(String name, ArmorType type) {
        return simple(name, p -> p.humanoidArmor(ArmorMaterials.IRON, type));
    }

    private static DeferredItem<Item> simple(String name) {
        return simple(name, p -> p);
    }

    private static DeferredItem<Item> simple(String name, java.util.function.UnaryOperator<Item.Properties> properties) {
        return tracked(ITEMS.registerSimpleItem(name, properties));
    }

    private static DeferredItem<Item> weapon(String name, OrbitalWeaponItem.WeaponProfile profile, java.util.function.UnaryOperator<Item.Properties> properties) {
        return tracked(ITEMS.registerItem(name, itemProperties -> new OrbitalWeaponItem(profile, itemProperties), properties));
    }

    private static <T extends Item> DeferredItem<T> tracked(DeferredItem<T> item) {
        CREATIVE_ITEMS.add(item);
        return item;
    }
}
