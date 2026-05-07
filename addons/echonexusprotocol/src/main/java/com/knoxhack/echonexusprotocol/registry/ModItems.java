package com.knoxhack.echonexusprotocol.registry;

import com.knoxhack.echonexusprotocol.item.CoreAccessKeyItem;
import com.knoxhack.echonexusprotocol.item.NexusArmorItem;
import com.knoxhack.echonexusprotocol.item.NexusChargeItem;
import com.knoxhack.echonexusprotocol.item.NexusFieldChargeItem;
import com.knoxhack.echonexusprotocol.item.NexusScannerVisorItem;
import com.knoxhack.echonexusprotocol.item.NexusUtilityItem;
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
   public static final Items ITEMS = DeferredRegister.createItems("echonexusprotocol");
   private static final List<DeferredItem<? extends Item>> CREATIVE_ITEMS = new ArrayList<>();
   public static final DeferredItem<Item> NEXUS_SHARD = tracked(
      ITEMS.registerItem("nexus_shard", p -> new NexusChargeItem(600, 2, p), p -> p.rarity(Rarity.UNCOMMON))
   );
   public static final DeferredItem<Item> STABLE_NEXUS_CORE = simple("stable_nexus_core", p -> p.rarity(Rarity.RARE).fireResistant());
   public static final DeferredItem<Item> BLACKBOX_FRAGMENT = simple("blackbox_fragment", p -> p.rarity(Rarity.RARE).fireResistant());
   public static final DeferredItem<Item> CORRUPTED_FERRITE = simple("corrupted_ferrite", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> STATIC_FLUID = tracked(
      ITEMS.registerItem("static_fluid", p -> new NexusChargeItem(150, 12, p), p -> p.stacksTo(16).rarity(Rarity.UNCOMMON))
   );
   public static final DeferredItem<Item> WHITE_SIGNAL_BARK = simple("white_signal_bark");
   public static final DeferredItem<Item> NEXUS_GEL = simple("nexus_gel", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> REALITY_DUST = simple("reality_dust", p -> p.rarity(Rarity.RARE));
   public static final DeferredItem<Item> FIELD_MEMBRANE = simple("field_membrane");
   public static final DeferredItem<Item> CORE_GLASS = simple("core_glass");
   public static final DeferredItem<Item> SIGNAL_WIRE = simple("signal_wire");
   public static final DeferredItem<Item> FILTER_MEMBRANE = simple("filter_membrane");
   public static final DeferredItem<Item> STABILIZED_ALLOY = simple("stabilized_alloy");
   public static final DeferredItem<Item> ECHO_CRYSTAL_DUST = simple("echo_crystal_dust");
   public static final DeferredItem<Item> CLEAN_RESONANCE_BATTERY = simple("clean_resonance_battery", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> MEMORY_SHARD = simple("memory_shard", p -> p.rarity(Rarity.UNCOMMON));
   public static final DeferredItem<Item> DATA_FRAGMENT = simple("data_fragment");
   public static final DeferredItem<Item> REACTOR_CORE = simple("reactor_core", p -> p.rarity(Rarity.EPIC).fireResistant());
   public static final DeferredItem<Item> CORE_ACCESS_KEY = tracked(ITEMS.registerItem("core_access_key", CoreAccessKeyItem::new, p -> p.rarity(Rarity.RARE).fireResistant().stacksTo(1)));
   public static final DeferredItem<Item> CORE_KEY_ASSEMBLY = tracked(ITEMS.registerItem("core_key_assembly", CoreAccessKeyItem::new, p -> p.rarity(Rarity.EPIC).fireResistant().stacksTo(1)));
   public static final DeferredItem<Item> NEXUS_SCANNER_VISOR = tracked(
      ITEMS.registerItem("nexus_scanner_visor", NexusScannerVisorItem::new, p -> p.stacksTo(1).rarity(Rarity.UNCOMMON))
   );
   public static final DeferredItem<Item> NEXUS_PICKAXE = tracked(
      ITEMS.registerItem("nexus_pickaxe", p -> new NexusUtilityItem(NexusUtilityItem.Mode.PICKAXE, p), p -> p.stacksTo(1).durability(640).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> SIGNAL_BLADE = tracked(
      ITEMS.registerItem(
         "signal_blade", p -> new NexusUtilityItem(NexusUtilityItem.Mode.SIGNAL_BLADE, p), p -> p.stacksTo(1).durability(520).rarity(Rarity.RARE)
      )
   );
   public static final DeferredItem<Item> REALITY_ANCHOR = tracked(
      ITEMS.registerItem("reality_anchor", p -> new NexusUtilityItem(NexusUtilityItem.Mode.REALITY_ANCHOR, p), p -> p.stacksTo(1).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> FIELD_ANCHOR = tracked(
      ITEMS.registerItem("field_anchor", p -> new NexusUtilityItem(NexusUtilityItem.Mode.FIELD_ANCHOR, p), p -> p.stacksTo(1).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> PURITY_CHARGE = tracked(
      ITEMS.registerItem("purity_charge", p -> new NexusFieldChargeItem(NexusFieldChargeItem.Mode.PURITY, p), p -> p.stacksTo(16).rarity(Rarity.UNCOMMON))
   );
   public static final DeferredItem<Item> STABILIZED_PURITY_CHARGE = tracked(
      ITEMS.registerItem("stabilized_purity_charge", p -> new NexusFieldChargeItem(NexusFieldChargeItem.Mode.STABILIZED_PURITY, p), p -> p.stacksTo(8).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> COLLAPSE_CHARGE = tracked(
      ITEMS.registerItem("collapse_charge", p -> new NexusFieldChargeItem(NexusFieldChargeItem.Mode.COLLAPSE, p), p -> p.stacksTo(16).rarity(Rarity.RARE))
   );
   public static final DeferredItem<Item> NEXUS_HELMET = armor("nexus_helmet", ArmorType.HELMET);
   public static final DeferredItem<Item> NEXUS_CHESTPLATE = armor("nexus_chestplate", ArmorType.CHESTPLATE);
   public static final DeferredItem<Item> NEXUS_LEGGINGS = armor("nexus_leggings", ArmorType.LEGGINGS);
   public static final DeferredItem<Item> NEXUS_BOOTS = armor("nexus_boots", ArmorType.BOOTS);

   private ModItems() {
   }

   public static void register(IEventBus eventBus) {
      ITEMS.register(eventBus);
   }

   public static List<DeferredItem<? extends Item>> creativeItems() {
      return List.copyOf(CREATIVE_ITEMS);
   }

   private static DeferredItem<Item> armor(String name, ArmorType type) {
      return tracked(ITEMS.registerItem(name, NexusArmorItem::new, p -> p.humanoidArmor(ArmorMaterials.DIAMOND, type).rarity(Rarity.RARE).stacksTo(1)));
   }

   private static DeferredItem<Item> simple(String name) {
      return simple(name, p -> p);
   }

   private static DeferredItem<Item> simple(String name, UnaryOperator<Properties> properties) {
      return tracked(ITEMS.registerSimpleItem(name, properties));
   }

   private static <T extends Item> DeferredItem<T> tracked(DeferredItem<T> item) {
      CREATIVE_ITEMS.add(item);
      return item;
   }

   static {
      ModBlocks.ALL_BLOCKS.forEach(block -> tracked(ITEMS.registerSimpleBlockItem(block)));
   }
}
