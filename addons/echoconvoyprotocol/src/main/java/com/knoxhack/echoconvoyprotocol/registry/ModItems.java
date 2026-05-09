package com.knoxhack.echoconvoyprotocol.registry;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.entity.ConvoyVehicleKind;
import com.knoxhack.echoconvoyprotocol.item.VehicleKitItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
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

   private static <T extends Item> DeferredItem<T> tracked(DeferredItem<T> item) {
      CREATIVE_ITEMS.add(item);
      return item;
   }

   static {
      ModBlocks.ALL_BLOCKS.forEach(block -> tracked(ITEMS.registerSimpleBlockItem(block)));
   }
}
