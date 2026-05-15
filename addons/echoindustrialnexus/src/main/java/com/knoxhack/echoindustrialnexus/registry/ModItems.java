package com.knoxhack.echoindustrialnexus.registry;

import com.knoxhack.echomultiblockcore.api.RobotToolType;
import com.knoxhack.echomultiblockcore.item.BlueprintItem;
import com.knoxhack.echomultiblockcore.item.ToolHeadItem;
import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import com.knoxhack.echoindustrialnexus.item.EmergencyCoolantPackItem;
import com.knoxhack.echoindustrialnexus.item.FluxMultimeterItem;
import com.knoxhack.echoindustrialnexus.item.FactoryDiagnosticToolItem;
import com.knoxhack.echoindustrialnexus.item.FurnaceWardenSummonerItem;
import com.knoxhack.echoindustrialnexus.item.SalvageMagnetItem;
import com.knoxhack.echoindustrialnexus.item.ThermalWrenchItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

public final class ModItems {
   public static final Items ITEMS = DeferredRegister.createItems("echoindustrialnexus");
   private static final List<DeferredItem<? extends Item>> CREATIVE_ITEMS = new ArrayList<>();
   public static final DeferredItem<Item> SCRAP_METAL = simple("scrap_metal");
   public static final DeferredItem<Item> SCRAP_FUEL = simple("scrap_fuel");
   public static final DeferredItem<Item> COMPACTED_ASH_FUEL = simple("compacted_ash_fuel");
   public static final DeferredItem<Item> THERMAL_DUST = simple("thermal_dust");
   public static final DeferredItem<Item> RUST_DUST = simple("rust_dust");
   public static final DeferredItem<Item> CIRCUIT_DUST = simple("circuit_dust");
   public static final DeferredItem<Item> IRON_DUST = simple("iron_dust");
   public static final DeferredItem<Item> COPPER_DUST = simple("copper_dust");
   public static final DeferredItem<Item> GOLD_DUST = simple("gold_dust");
   public static final DeferredItem<Item> URANIUM_DUST = simple("uranium_dust");
   public static final DeferredItem<Item> NEXUS_DUST = simple("nexus_dust", p -> p.rarity(Rarity.RARE));
   public static final DeferredItem<Item> RAD_SLAG = simple("rad_slag", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> BROKEN_CIRCUIT = simple("broken_circuit");
   public static final DeferredItem<Item> OLD_CIRCUIT = simple("old_circuit");
   public static final DeferredItem<Item> ECHO_CIRCUIT = simple("echo_circuit", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> COPPER_WIRE = simple("copper_wire");
   public static final DeferredItem<Item> SIGNAL_WIRE = simple("signal_wire", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> SERVO_MOTOR = simple("servo_motor");
   public static final DeferredItem<Item> PRESSURE_VALVE = simple("pressure_valve");
   public static final DeferredItem<Item> FIELD_RELAY = simple("field_relay", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> THERMAL_REGULATOR = simple("thermal_regulator", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> SCRAP_PLATE = simple("scrap_plate");
   public static final DeferredItem<Item> REFINED_PLATE = simple("refined_plate");
   public static final DeferredItem<Item> REINFORCED_PLATE = simple("reinforced_plate", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> HEAT_COIL = simple("heat_coil");
   public static final DeferredItem<Item> FLUX_CRYSTAL = simple("flux_crystal", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> STABILIZED_SLAG = simple("stabilized_slag");
   public static final DeferredItem<Item> INDUSTRIAL_MEMBRANE = simple("industrial_membrane");
   public static final DeferredItem<Item> FIELD_MEMBRANE = simple("field_membrane", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> INDUSTRIAL_FILTER_CORE = simple("industrial_filter_core", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> GAS_MASK_FILTER = simple("gas_mask_filter");
   public static final DeferredItem<Item> COOLANT_CELL = simple("coolant_cell");
   public static final DeferredItem<Item> DENSE_ALLOY_FRAGMENT = simple("dense_alloy_fragment");
   public static final DeferredItem<Item> DENSE_ALLOY_INGOT = simple("dense_alloy_ingot");
   public static final DeferredItem<Item> DENSE_ALLOY_PLATE = simple("dense_alloy_plate");
   public static final DeferredItem<Item> STABILIZED_ALLOY_INGOT = simple("stabilized_alloy_ingot", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> STABILIZED_ALLOY_PLATE = simple("stabilized_alloy_plate", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> STABLE_NEXUS_CORE = simple("stable_nexus_core", p -> p.rarity(Rarity.RARE).fireResistant());
   public static final DeferredItem<Item> HYBRID_THERMAL_CORE = simple("hybrid_thermal_core", p -> p.rarity(Rarity.RARE).fireResistant());
   public static final DeferredItem<Item> MACHINE_FRAME = simple("machine_frame");
   public static final DeferredItem<Item> REINFORCED_MACHINE_FRAME = simple("reinforced_machine_frame", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> PRECISION_CIRCUIT = simple("precision_circuit", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> INDUSTRIAL_CIRCUIT = simple("industrial_circuit", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> ROBOTIC_JOINT = simple("robotic_joint");
   public static final DeferredItem<Item> HYDRAULIC_ACTUATOR = simple("hydraulic_actuator");
   public static final DeferredItem<Item> CONVEYOR_GEAR = simple("conveyor_gear");
   public static final DeferredItem<Item> COOLING_COIL = simple("cooling_coil");
   public static final DeferredItem<Item> DATA_PROCESSOR = simple("data_processor", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> RECIPE_MATRIX_SHARD = simple("recipe_matrix_shard", p -> p.rarity(Rarity.RARE));
   public static final DeferredItem<Item> INDUSTRIAL_UPGRADE_CHIP = simple("industrial_upgrade_chip", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> SPEED_UPGRADE_CHIP = simple("speed_upgrade_chip", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> COOLING_UPGRADE_CHIP = simple("cooling_upgrade_chip", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> PRECISION_UPGRADE_CHIP = simple("precision_upgrade_chip", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> STABILIZED_MACHINE_FRAME = simple("stabilized_machine_frame", p -> p.rarity(Rarity.RARE));
   public static final DeferredItem<Item> HYBRID_NEXUS_FRAME = simple("hybrid_nexus_frame", p -> p.rarity(Rarity.EPIC).fireResistant());
   public static final DeferredItem<Item> SPEED_SERVO = simple("speed_servo", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> EFFICIENCY_COIL = simple("efficiency_coil", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> HEAT_SINK_UPGRADE = simple("heat_sink_upgrade");
   public static final DeferredItem<Item> FILTER_MODULE = simple("filter_module");
   public static final DeferredItem<Item> SECONDARY_OUTPUT_MODULE = simple("secondary_output_module", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> RADIATION_SHIELDING_UPGRADE = simple("radiation_shielding_upgrade", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> NEXUS_STABILIZER_UPGRADE = simple("nexus_stabilizer_upgrade", p -> p.rarity(Rarity.RARE).fireResistant());
   public static final DeferredItem<Item> FACTORY_LINK_CHIP = simple("factory_link_chip", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> OVERCLOCK_CORE = simple("overclock_core", p -> p.rarity(Rarity.RARE));
   public static final DeferredItem<Item> EMERGENCY_SHUTDOWN_MODULE = simple("emergency_shutdown_module", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> DIRTY_WATER_CELL = simple("dirty_water_cell");
   public static final DeferredItem<Item> CLEAN_WATER_CELL = simple("clean_water_cell");
   public static final DeferredItem<Item> TOXIC_SLUDGE_CELL = simple("toxic_sludge_cell");
   public static final DeferredItem<Item> CHEMICAL_SOLVENT = simple("chemical_solvent");
   public static final DeferredItem<Item> WASTE_CANISTER = simple("waste_canister");
   public static final DeferredItem<Item> STATIC_FLUID_CELL = simple("static_fluid_cell", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> NEXUS_GEL = simple("nexus_gel", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> CRYO_GEL = simple("cryo_gel");
   public static final DeferredItem<Item> CRYO_DUST = simple("cryo_dust");
   public static final DeferredItem<Item> FROZEN_CORE = simple("frozen_core", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> FUEL_CELL = simple("fuel_cell");
   public static final DeferredItem<Item> TAR = simple("tar");
   public static final DeferredItem<Item> PRESSURE_COMPONENT = simple("pressure_component", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> OXYGEN_COMPRESSOR_PART = simple("oxygen_compressor_part", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> SOLAR_GLASS = simple("solar_glass", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> LAUNCH_PLATFORM_FRAME = simple("launch_platform_frame", p -> p.rarity(Rarity.RARE));
   public static final DeferredItem<Item> STATION_BATTERY = simple("station_battery", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> PRESSURE_SEAL_KIT = simple("pressure_seal_kit");
   public static final DeferredItem<Item> EMERGENCY_OXYGEN_FILTER = simple("emergency_oxygen_filter");
   public static final DeferredItem<Item> HULL_REPAIR_FOAM = simple("hull_repair_foam");
   public static final DeferredItem<Item> AI_OVERRIDE_CHIP_CASING = simple("ai_override_chip_casing", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> SIGNAL_PANIC_DAMPENER = simple("signal_panic_dampener", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> CORE_KEY_ASSEMBLY = simple("core_key_assembly", p -> p.rarity(Rarity.EPIC).fireResistant());
   public static final DeferredItem<Item> TRUTH_ENGINE_PART = simple("truth_engine_part", p -> p.rarity(Rarity.RARE).fireResistant());
   public static final DeferredItem<Item> MEMORY_STABILIZER_CASING = simple("memory_stabilizer_casing", p -> p.rarity(Rarity.RARE));
   public static final DeferredItem<Item> BLACKBOX_DECODER_COOLING_SYSTEM = simple("blackbox_decoder_cooling_system", p -> p.rarity(Rarity.RARE));
   public static final DeferredItem<Item> PROTOCOL_EXTRACTOR_COIL = simple("protocol_extractor_coil", p -> p.rarity(Rarity.RARE));
   public static final DeferredItem<Item> WARDEN_THERMAL_CORE = simple("warden_thermal_core", p -> p.rarity(Rarity.EPIC).fireResistant());
   public static final DeferredItem<Item> FURNACE_WARDEN_TROPHY = simple("furnace_warden_trophy", p -> p.rarity(Rarity.EPIC).fireResistant());
   public static final DeferredItem<Item> FURNACE_WARDEN_WAKE_CORE = tracked(
      ITEMS.registerItem("furnace_warden_wake_core", FurnaceWardenSummonerItem::new, p -> p.stacksTo(1).rarity(Rarity.EPIC).fireResistant())
   );
   public static final DeferredItem<Item> THERMAL_WRENCH = tracked(
      ITEMS.registerItem("thermal_wrench", ThermalWrenchItem::new, p -> p.stacksTo(1).durability(256))
   );
   public static final DeferredItem<Item> FLUX_MULTIMETER = tracked(
      ITEMS.registerItem("flux_multimeter", FluxMultimeterItem::new, p -> p.stacksTo(1).durability(256))
   );
   public static final DeferredItem<Item> EMERGENCY_COOLANT_PACK = tracked(
      ITEMS.registerItem("emergency_coolant_pack", EmergencyCoolantPackItem::new, p -> p.stacksTo(16))
   );
   public static final DeferredItem<Item> SALVAGE_MAGNET = tracked(
      ITEMS.registerItem("salvage_magnet", SalvageMagnetItem::new, p -> p.stacksTo(1).durability(192).rarity(Rarity.UNCOMMON))
   );
   public static final DeferredItem<Item> FACTORY_DIAGNOSTIC_TOOL = tracked(
      ITEMS.registerItem("factory_diagnostic_tool", FactoryDiagnosticToolItem::new, p -> p.stacksTo(1).durability(256))
   );
   public static final DeferredItem<Item> ASSEMBLY_LINE_BLUEPRINT = blueprint("assembly_line_blueprint", "industrial_assembly_line");
   public static final DeferredItem<Item> SCRAP_PROCESSOR_BLUEPRINT = blueprint("scrap_processor_blueprint", "scrap_processor");
   public static final DeferredItem<Item> PLATE_PRESS_BLUEPRINT = blueprint("plate_press_blueprint", "plate_press");
   public static final DeferredItem<Item> CIRCUIT_FABRICATOR_BLUEPRINT = blueprint("circuit_fabricator_blueprint", "circuit_fabricator");
   public static final DeferredItem<Item> RECIPE_MATRIX_BLUEPRINT = blueprint("recipe_matrix_blueprint", "recipe_matrix_core");
   public static final DeferredItem<Item> NEXUS_FURNACE_ARRAY_BLUEPRINT = blueprint("nexus_furnace_array_blueprint", "nexus_furnace_array");
   public static final DeferredItem<Item> INDUSTRIAL_WELDER_HEAD = toolHead("industrial_welder_head", RobotToolType.WELDER);
   public static final DeferredItem<Item> PRECISION_ASSEMBLER_HEAD = toolHead("precision_assembler_head", RobotToolType.ASSEMBLER);
   public static final DeferredItem<Item> PLASMA_CUTTER_HEAD = toolHead("plasma_cutter_head", RobotToolType.CUTTER);
   public static final DeferredItem<Item> INSPECTION_SCANNER_HEAD = toolHead("inspection_scanner_head", RobotToolType.SCANNER);
   public static final DeferredItem<Item> HEAVY_GRIPPER_HEAD = toolHead("heavy_gripper_head", RobotToolType.GRIPPER);
   public static final DeferredItem<Item> COOLANT_INJECTOR_HEAD = toolHead("coolant_injector_head", RobotToolType.INJECTOR);
   public static final DeferredItem<Item> INDUSTRIAL_EXO_HELMET = armor("industrial_exo_helmet", ArmorType.HELMET);
   public static final DeferredItem<Item> INDUSTRIAL_EXO_CHESTPLATE = armor("industrial_exo_chestplate", ArmorType.CHESTPLATE);
   public static final DeferredItem<Item> INDUSTRIAL_EXO_LEGGINGS = armor("industrial_exo_leggings", ArmorType.LEGGINGS);
   public static final DeferredItem<Item> INDUSTRIAL_EXO_BOOTS = armor("industrial_exo_boots", ArmorType.BOOTS);

   private ModItems() {
   }

   public static void register(IEventBus eventBus) {
      ITEMS.register(eventBus);
   }

   public static List<DeferredItem<? extends Item>> creativeItems() {
      return List.copyOf(CREATIVE_ITEMS);
   }

   private static DeferredItem<Item> simple(String name) {
      return simple(name, p -> p);
   }

   private static DeferredItem<Item> armor(String name, ArmorType type) {
      return simple(name, p -> p.humanoidArmor(ArmorMaterials.IRON, type).rarity(Rarity.UNCOMMON));
   }

   private static DeferredItem<Item> simple(String name, UnaryOperator<Properties> properties) {
      return tracked(ITEMS.registerSimpleItem(name, properties));
   }

   private static DeferredItem<Item> toolHead(String name, RobotToolType toolType) {
      return tracked(ITEMS.registerItem(name, properties -> new ToolHeadItem(toolType, properties), p -> p.stacksTo(1).rarity(Rarity.UNCOMMON)));
   }

   private static DeferredItem<Item> blueprint(String name, String definitionPath) {
      return tracked(ITEMS.registerItem(
         name,
         properties -> new BlueprintItem(EchoIndustrialNexus.id(definitionPath), properties),
         p -> p.stacksTo(1).rarity(Rarity.UNCOMMON)
      ));
   }

   private static <T extends Item> DeferredItem<T> tracked(DeferredItem<T> item) {
      CREATIVE_ITEMS.add(item);
      return item;
   }

   static {
      ModBlocks.ALL_BLOCKS.forEach(block -> tracked(ITEMS.registerSimpleBlockItem(block)));
   }
}
