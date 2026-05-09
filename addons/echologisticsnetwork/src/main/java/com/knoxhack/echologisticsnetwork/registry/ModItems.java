package com.knoxhack.echologisticsnetwork.registry;

import com.knoxhack.echologisticsnetwork.EchoLogisticsNetwork;
import com.knoxhack.echologisticsnetwork.item.LogisticsToolItem;
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
   public static final Items ITEMS = DeferredRegister.createItems(EchoLogisticsNetwork.MODID);
   private static final List<DeferredItem<? extends Item>> CREATIVE_ITEMS = new ArrayList<>();

   public static final DeferredItem<Item> SUPPLY_TAG = tool("supply_tag", LogisticsToolItem.Mode.SUPPLY_TAG, p -> p);
   public static final DeferredItem<Item> LOGISTICS_CHIP = simple("logistics_chip", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> COURIER_DRONE_MODULE = simple("courier_drone_module", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> ROUTE_MANIFEST = tool("route_manifest", LogisticsToolItem.Mode.ROUTE_MANIFEST, p -> p.stacksTo(16));
   public static final DeferredItem<Item> LOADOUT_CARD = tool("loadout_card", LogisticsToolItem.Mode.LOADOUT_CARD, p -> p.stacksTo(16));
   public static final DeferredItem<Item> REMOTE_REQUEST_TABLET = tool("remote_request_tablet", LogisticsToolItem.Mode.REMOTE_REQUEST_TABLET, p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));

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

   private static DeferredItem<Item> tool(String name, LogisticsToolItem.Mode mode, UnaryOperator<Properties> properties) {
      return tracked(ITEMS.registerItem(name, itemProperties -> new LogisticsToolItem(mode, itemProperties), properties));
   }

   private static <T extends Item> DeferredItem<T> tracked(DeferredItem<T> item) {
      CREATIVE_ITEMS.add(item);
      return item;
   }

   static {
      ModBlocks.ALL_BLOCKS.forEach(block -> tracked(ITEMS.registerSimpleBlockItem(block)));
   }
}
