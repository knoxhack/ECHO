package com.knoxhack.echoblockworks.registry;

import com.knoxhack.echoblockworks.EchoBlockworks;
import com.knoxhack.echoblockworks.item.BlockworksPatternCutterItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EchoBlockworks.MODID);

   private static final List<DeferredItem<? extends Item>> CREATIVE_ITEMS = new ArrayList<>();

   public static final DeferredItem<Item> ECHO_PATTERN_CUTTER;

   static {
      ModBlocks.creativeBlocks().forEach(block -> tracked(ITEMS.registerSimpleBlockItem(block)));
      ECHO_PATTERN_CUTTER = tracked(ITEMS.registerItem("echo_pattern_cutter", BlockworksPatternCutterItem::new,
         properties -> properties.stacksTo(1).durability(384).rarity(Rarity.UNCOMMON)));
   }

   private ModItems() {
   }

   public static void register(IEventBus eventBus) {
      ITEMS.register(eventBus);
   }

   public static List<DeferredItem<? extends Item>> creativeItems() {
      return List.copyOf(CREATIVE_ITEMS);
   }

   private static <T extends Item> DeferredItem<T> tracked(DeferredItem<T> item) {
      CREATIVE_ITEMS.add(item);
      return item;
   }
}
