package com.knoxhack.echoagriculturereclamation.registry;

import com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamation;
import com.knoxhack.echoagriculturereclamation.content.CropSpec;
import com.knoxhack.echoagriculturereclamation.content.SeedProfile;
import com.knoxhack.echoagriculturereclamation.item.ReclamationSeedItem;
import com.knoxhack.echoagriculturereclamation.item.ReclamationUtilityItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

public final class ModItems {
   public static final Items ITEMS = DeferredRegister.createItems(EchoAgricultureReclamation.MODID);
   private static final List<DeferredItem<? extends Item>> CREATIVE_ITEMS = new ArrayList<>();
   private static final Map<String, DeferredItem<Item>> PRODUCE = new LinkedHashMap<>();

   public static final DeferredItem<Item> RECOVERED_SEED_CAPSULE = tracked(ITEMS.registerItem(
      "recovered_seed_capsule", p -> new ReclamationSeedItem(ReclamationSeedItem.Mode.CAPSULE, p.stacksTo(16))));
   public static final DeferredItem<Item> CONTAMINATED_SEED = tracked(ITEMS.registerItem(
      "contaminated_seed", p -> new ReclamationSeedItem(ReclamationSeedItem.Mode.CONTAMINATED, p.stacksTo(64))));
   public static final DeferredItem<Item> STABILIZED_SEED = tracked(ITEMS.registerItem(
      "stabilized_seed", p -> new ReclamationSeedItem(ReclamationSeedItem.Mode.STABILIZED, p.stacksTo(64))));
   public static final DeferredItem<Item> GENE_SAMPLE = tracked(ITEMS.registerItem(
      "gene_sample", p -> new ReclamationUtilityItem("tooltip.echoagriculturereclamation.gene_sample", p)));
   public static final DeferredItem<Item> SOIL_NUTRIENT_MIX = tracked(ITEMS.registerItem(
      "soil_nutrient_mix", p -> new ReclamationUtilityItem("tooltip.echoagriculturereclamation.soil_nutrient_mix", p)));
   public static final DeferredItem<Item> PURIFICATION_ENZYME = tracked(ITEMS.registerItem(
      "purification_enzyme", p -> new ReclamationUtilityItem("tooltip.echoagriculturereclamation.purification_enzyme", p)));
   public static final DeferredItem<Item> BIO_GEL = tracked(ITEMS.registerItem(
      "bio_gel", p -> new ReclamationUtilityItem("tooltip.echoagriculturereclamation.bio_gel", p)));

   public static final DeferredItem<Item> ASH_WHEAT = produce(CropSpec.byPath("ash_wheat"));
   public static final DeferredItem<Item> HARDROOT = produce(CropSpec.byPath("hardroot"));
   public static final DeferredItem<Item> GLOW_BEANS = produce(CropSpec.byPath("glow_beans"));
   public static final DeferredItem<Item> RADLEAF = produce(CropSpec.byPath("radleaf"));
   public static final DeferredItem<Item> MUTANT_BERRIES = produce(CropSpec.byPath("mutant_berries"));
   public static final DeferredItem<Item> CRYO_MOSS = produce(CropSpec.byPath("cryo_moss"));
   public static final DeferredItem<Item> CLEAN_CORN = produce(CropSpec.byPath("clean_corn"));
   public static final DeferredItem<Item> MEDICINAL_ALOE = produce(CropSpec.byPath("medicinal_aloe"));
   public static final DeferredItem<Item> FILTER_REED = produce(CropSpec.byPath("filter_reed"));
   public static final DeferredItem<Item> NEXUS_ORCHID = produce(CropSpec.byPath("nexus_orchid"));
   public static final DeferredItem<Item> SIGNAL_FUNGUS = produce(CropSpec.byPath("signal_fungus"));

   static {
      ModBlocks.blockItems().forEach(block -> tracked(ITEMS.registerSimpleBlockItem(block)));
   }

   private ModItems() {
   }

   public static void register(IEventBus eventBus) {
      ITEMS.register(eventBus);
   }

   public static List<DeferredItem<? extends Item>> creativeItems() {
      return List.copyOf(CREATIVE_ITEMS);
   }

   public static DeferredItem<Item> produceFor(CropSpec spec) {
      return PRODUCE.get(spec.path());
   }

   public static DataComponentType<SeedProfile> seedProfileComponent() {
      return ModDataComponents.SEED_PROFILE.get();
   }

   private static DeferredItem<Item> produce(CropSpec spec) {
      FoodProperties food = foodFor(spec);
      DeferredItem<Item> item = food == null
         ? tracked(ITEMS.registerItem(spec.path(), properties -> new ReclamationUtilityItem("tooltip.echoagriculturereclamation.crop." + spec.path(), properties)))
         : tracked(ITEMS.registerItem(spec.path(), properties -> new Item(properties.food(food))));
      PRODUCE.put(spec.path(), item);
      return item;
   }

   private static FoodProperties foodFor(CropSpec spec) {
      return switch (spec.path()) {
         case "ash_wheat" -> new FoodProperties.Builder().nutrition(1).saturationModifier(0.2F).build();
         case "hardroot" -> new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build();
         case "glow_beans" -> new FoodProperties.Builder().nutrition(2).saturationModifier(0.25F).build();
         case "mutant_berries" -> new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).build();
         case "clean_corn" -> new FoodProperties.Builder().nutrition(3).saturationModifier(0.35F).build();
         default -> null;
      };
   }

   private static <T extends Item> DeferredItem<T> tracked(DeferredItem<T> item) {
      CREATIVE_ITEMS.add(item);
      return item;
   }
}
