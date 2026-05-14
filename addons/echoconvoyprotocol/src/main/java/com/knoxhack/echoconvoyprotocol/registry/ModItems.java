package com.knoxhack.echoconvoyprotocol.registry;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleKind;
import com.knoxhack.echoconvoyprotocol.item.VehicleKitItem;
import com.knoxhack.echoconvoyprotocol.item.VehicleUpgradeItem;
import com.knoxhack.echoconvoyprotocol.upgrade.ConvoyUpgradeSlot;
import com.knoxhack.echoconvoyprotocol.upgrade.VehicleUpgradeStats;
import com.knoxhack.echomultiblockcore.api.RobotToolType;
import com.knoxhack.echomultiblockcore.item.BlueprintItem;
import com.knoxhack.echomultiblockcore.item.ToolHeadItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

public final class ModItems {
   public static final Items ITEMS = DeferredRegister.createItems(EchoConvoyProtocol.MODID);
   private static final List<DeferredItem<? extends Item>> CREATIVE_ITEMS = new ArrayList<>();

   public static final DeferredItem<Item> SCRAP_TIRE = simple("scrap_tire");
   public static final DeferredItem<Item> ARMORED_TIRE = simple("armored_tire", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> VEHICLE_FRAME = simple("vehicle_frame", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> FUEL_CANISTER = simple("fuel_canister", p -> p.stacksTo(16));
   public static final DeferredItem<Item> BATTERY_CELL = simple("battery_cell", p -> p.stacksTo(16));
   public static final DeferredItem<Item> ENGINE_CORE = simple("engine_core", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> RADIATION_SHIELDING_PLATE = simple("radiation_shielding_plate", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> CONVOY_REPAIR_KIT = simple("convoy_repair_kit", p -> p.stacksTo(16));
   public static final DeferredItem<Item> CARGO_NET = simple("cargo_net", p -> p.stacksTo(16));
   public static final DeferredItem<Item> ROUTE_BEACON = simple("route_beacon", p -> p.stacksTo(16).rarity(Rarity.UNCOMMON));

   public static final DeferredItem<Item> SCRAP_BIKE_KIT = vehicleKit("scrap_bike_kit", ConvoyVehicleKind.SCRAP_BIKE, p -> p.stacksTo(1));
   public static final DeferredItem<Item> WASTELAND_ROVER_KIT = vehicleKit("wasteland_rover_kit", ConvoyVehicleKind.WASTELAND_ROVER, p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> CARGO_CRAWLER_KIT = vehicleKit("cargo_crawler_kit", ConvoyVehicleKind.CARGO_CRAWLER, p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> ARMORED_RELAY_TRUCK_KIT = vehicleKit("armored_relay_truck_kit", ConvoyVehicleKind.ARMORED_RELAY_TRUCK, p -> p.stacksTo(1).rarity(Rarity.RARE));

   public static final DeferredItem<VehicleUpgradeItem> SCRAP_BIKE_TUNED_CHAIN_KIT = vehicleUpgrade(
      "scrap_bike_tuned_chain_kit",
      ConvoyVehicleKind.SCRAP_BIKE,
      ConvoyUpgradeSlot.MOBILITY,
      new VehicleUpgradeStats(0.12D, 0.8F, 0.0D, 0, 0, 0, 0, 0.0D)
   );
   public static final DeferredItem<VehicleUpgradeItem> SCRAP_BIKE_SADDLEBAG_FRAME_KIT = vehicleUpgrade(
      "scrap_bike_saddlebag_frame_kit",
      ConvoyVehicleKind.SCRAP_BIKE,
      ConvoyUpgradeSlot.UTILITY,
      new VehicleUpgradeStats(0.0D, 0.0F, 0.0D, 4, 0, 0, 0, 0.0D)
   );
   public static final DeferredItem<VehicleUpgradeItem> SCRAP_BIKE_CRASH_CAGE_KIT = vehicleUpgrade(
      "scrap_bike_crash_cage_kit",
      ConvoyVehicleKind.SCRAP_BIKE,
      ConvoyUpgradeSlot.DEFENSE,
      new VehicleUpgradeStats(0.0D, 0.0F, 0.0D, 0, 0, 15, 0, 0.04D)
   );
   public static final DeferredItem<VehicleUpgradeItem> WASTELAND_ROVER_SUSPENSION_KIT = vehicleUpgrade(
      "wasteland_rover_suspension_kit",
      ConvoyVehicleKind.WASTELAND_ROVER,
      ConvoyUpgradeSlot.MOBILITY,
      new VehicleUpgradeStats(0.08D, 0.4F, 0.10D, 0, 0, 0, 0, 0.0D)
   );
   public static final DeferredItem<VehicleUpgradeItem> WASTELAND_ROVER_SCANNER_ARRAY_KIT = vehicleUpgrade(
      "wasteland_rover_scanner_array_kit",
      ConvoyVehicleKind.WASTELAND_ROVER,
      ConvoyUpgradeSlot.UTILITY,
      new VehicleUpgradeStats(0.0D, 0.0F, 0.0D, 0, 40, 0, 48, 0.0D)
   );
   public static final DeferredItem<VehicleUpgradeItem> WASTELAND_ROVER_REINFORCED_PANEL_KIT = vehicleUpgrade(
      "wasteland_rover_reinforced_panel_kit",
      ConvoyVehicleKind.WASTELAND_ROVER,
      ConvoyUpgradeSlot.DEFENSE,
      new VehicleUpgradeStats(0.0D, 0.0F, 0.0D, 0, 0, 25, 0, 0.07D)
   );
   public static final DeferredItem<VehicleUpgradeItem> CARGO_CRAWLER_LOW_GEAR_DRIVE_KIT = vehicleUpgrade(
      "cargo_crawler_low_gear_drive_kit",
      ConvoyVehicleKind.CARGO_CRAWLER,
      ConvoyUpgradeSlot.MOBILITY,
      new VehicleUpgradeStats(0.10D, 0.2F, 0.20D, 0, 0, 0, 0, 0.0D)
   );
   public static final DeferredItem<VehicleUpgradeItem> CARGO_CRAWLER_EXPANDED_BAY_KIT = vehicleUpgrade(
      "cargo_crawler_expanded_bay_kit",
      ConvoyVehicleKind.CARGO_CRAWLER,
      ConvoyUpgradeSlot.UTILITY,
      new VehicleUpgradeStats(0.0D, 0.0F, 0.0D, 12, 0, 0, 0, 0.0D)
   );
   public static final DeferredItem<VehicleUpgradeItem> CARGO_CRAWLER_TRACK_SKIRT_KIT = vehicleUpgrade(
      "cargo_crawler_track_skirt_kit",
      ConvoyVehicleKind.CARGO_CRAWLER,
      ConvoyUpgradeSlot.DEFENSE,
      new VehicleUpgradeStats(0.0D, 0.0F, 0.0D, 0, 0, 35, 0, 0.08D)
   );
   public static final DeferredItem<VehicleUpgradeItem> ARMORED_RELAY_TRUCK_TORQUE_AXLE_KIT = vehicleUpgrade(
      "armored_relay_truck_torque_axle_kit",
      ConvoyVehicleKind.ARMORED_RELAY_TRUCK,
      ConvoyUpgradeSlot.MOBILITY,
      new VehicleUpgradeStats(0.08D, 0.25F, 0.15D, 0, 0, 0, 0, 0.0D)
   );
   public static final DeferredItem<VehicleUpgradeItem> ARMORED_RELAY_TRUCK_RELAY_ARRAY_KIT = vehicleUpgrade(
      "armored_relay_truck_relay_array_kit",
      ConvoyVehicleKind.ARMORED_RELAY_TRUCK,
      ConvoyUpgradeSlot.UTILITY,
      new VehicleUpgradeStats(0.0D, 0.0F, 0.0D, 0, 60, 0, 64, 0.0D)
   );
   public static final DeferredItem<VehicleUpgradeItem> ARMORED_RELAY_TRUCK_REACTIVE_ARMOR_KIT = vehicleUpgrade(
      "armored_relay_truck_reactive_armor_kit",
      ConvoyVehicleKind.ARMORED_RELAY_TRUCK,
      ConvoyUpgradeSlot.DEFENSE,
      new VehicleUpgradeStats(0.0D, 0.0F, 0.0D, 0, 0, 40, 0, 0.10D)
   );

   public static final DeferredItem<Item> CONVOY_DEPOT_BLUEPRINT = blueprint("convoy_depot_blueprint", "convoy_depot");
   public static final DeferredItem<Item> VEHICLE_REPAIR_GANTRY_BLUEPRINT = blueprint("vehicle_repair_gantry_blueprint", "vehicle_repair_gantry");
   public static final DeferredItem<Item> CARGO_LOADING_BAY_BLUEPRINT = blueprint("cargo_loading_bay_blueprint", "cargo_loading_bay");
   public static final DeferredItem<Item> FUEL_REFINERY_PAD_BLUEPRINT = blueprint("fuel_refinery_pad_blueprint", "fuel_refinery_pad");
   public static final DeferredItem<Item> ROUTE_DISPATCH_TOWER_BLUEPRINT = blueprint("route_dispatch_tower_blueprint", "route_dispatch_tower");
   public static final DeferredItem<Item> MOBILE_COMMAND_GARAGE_BLUEPRINT = blueprint("mobile_command_garage_blueprint", "mobile_command_garage");
   public static final DeferredItem<Item> CONVOY_RECOVERY_BEACON_BLUEPRINT = blueprint("convoy_recovery_beacon_blueprint", "convoy_recovery_beacon");

   public static final DeferredItem<Item> ARMORED_TRUCK_FRAME = simple("armored_truck_frame", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> VEHICLE_ARMOR_PLATE = simple("vehicle_armor_plate");
   public static final DeferredItem<Item> REINFORCED_AXLE = simple("reinforced_axle", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> ENGINE_MODULE = simple("engine_module", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> FUEL_CELL = simple("fuel_cell", p -> p.stacksTo(16));
   public static final DeferredItem<Item> CARGO_MODULE = simple("cargo_module", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> REPAIR_KIT = simple("repair_kit", p -> p.stacksTo(16));
   public static final DeferredItem<Item> CONVOY_ROUTE_CHIP = simple("convoy_route_chip", p -> p.stacksTo(16));
   public static final DeferredItem<Item> DISPATCH_CONTRACT = simple("dispatch_contract", p -> p.stacksTo(16).rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> EMERGENCY_BEACON = simple("emergency_beacon", p -> p.stacksTo(16).rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> FIELD_SUPPLY_CRATE = simple("field_supply_crate", p -> p.stacksTo(16));
   public static final DeferredItem<Item> CONVOY_UPGRADE_CHIP = simple("convoy_upgrade_chip", p -> p.stacksTo(16).rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> CONVOY_DIAGNOSTIC_TOOL = simple("convoy_diagnostic_tool", p -> p.stacksTo(1));
   public static final DeferredItem<Item> CARGO_CAPACITY_UPGRADE = simple("cargo_capacity_upgrade", p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> FUEL_EFFICIENCY_UPGRADE = simple("fuel_efficiency_upgrade", p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> ARMOR_UPGRADE = simple("armor_upgrade", p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));

   public static final DeferredItem<Item> CARGO_CLAMP_HEAD = toolHead("cargo_clamp_head", RobotToolType.CLAMP);
   public static final DeferredItem<Item> VEHICLE_WELDER_HEAD = toolHead("vehicle_welder_head", RobotToolType.WELDER);
   public static final DeferredItem<Item> FUEL_INJECTOR_HEAD = toolHead("fuel_injector_head", RobotToolType.INJECTOR);
   public static final DeferredItem<Item> HEAVY_LOADER_HEAD = toolHead("heavy_loader_head", RobotToolType.GRIPPER);
   public static final DeferredItem<Item> ROUTE_SCANNER_HEAD = toolHead("route_scanner_head", RobotToolType.SCANNER);

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

   private static DeferredItem<Item> simple(String name, UnaryOperator<Properties> properties) {
      return tracked(ITEMS.registerSimpleItem(name, properties));
   }

   private static DeferredItem<Item> vehicleKit(String name, ConvoyVehicleKind kind, UnaryOperator<Properties> properties) {
      return tracked(ITEMS.registerItem(name, itemProperties -> new VehicleKitItem(kind, itemProperties), properties));
   }

   private static DeferredItem<Item> blueprint(String name, String definitionPath) {
      return tracked(ITEMS.registerItem(
         name,
         properties -> new BlueprintItem(id(definitionPath), properties),
         properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON)
      ));
   }

   private static DeferredItem<Item> toolHead(String name, RobotToolType toolType) {
      return tracked(ITEMS.registerItem(name, properties -> new ToolHeadItem(toolType, properties), properties -> properties.stacksTo(1)));
   }

   private static DeferredItem<VehicleUpgradeItem> vehicleUpgrade(
      String name,
      ConvoyVehicleKind kind,
      ConvoyUpgradeSlot slot,
      VehicleUpgradeStats stats
   ) {
      return tracked(ITEMS.registerItem(
         name,
         itemProperties -> new VehicleUpgradeItem(kind, slot, stats, itemProperties),
         properties -> properties.stacksTo(1).rarity(slot == ConvoyUpgradeSlot.DEFENSE ? Rarity.UNCOMMON : Rarity.COMMON)
      ));
   }

   private static <T extends Item> DeferredItem<T> tracked(DeferredItem<T> item) {
      CREATIVE_ITEMS.add(item);
      return item;
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, path);
   }

   static {
      ModBlocks.ALL_BLOCKS.forEach(block -> tracked(ITEMS.registerSimpleBlockItem(block)));
   }
}
