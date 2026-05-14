package com.knoxhack.echoagriculturereclamation.integration;

import com.knoxhack.echocore.api.index.IIndexRecipeProvider;
import com.knoxhack.echocore.api.index.IndexRecipeCategory;
import com.knoxhack.echocore.api.index.IndexRecipeSlot;
import com.knoxhack.echocore.api.index.IndexRecipeView;
import com.knoxhack.echocore.api.index.IndexSlotRole;
import com.knoxhack.echoagriculturereclamation.EchoAgricultureReclamation;
import com.knoxhack.echoagriculturereclamation.content.CropCategory;
import com.knoxhack.echoagriculturereclamation.content.CropSpec;
import com.knoxhack.echoagriculturereclamation.content.ReclamationContent;
import com.knoxhack.echoagriculturereclamation.content.ReclamationCropRule;
import com.knoxhack.echoagriculturereclamation.content.ReclamationMachineRules;
import com.knoxhack.echoagriculturereclamation.content.ReclamationSoilRule;
import com.knoxhack.echoagriculturereclamation.content.SoilState;
import com.knoxhack.echoagriculturereclamation.registry.ModBlocks;
import com.knoxhack.echoagriculturereclamation.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public enum ReclamationIndexProvider implements IIndexRecipeProvider {
   INSTANCE;

   private static final Identifier CATEGORY_CROPS = id("recipe/agriculture_reclamation");
   private static final Identifier CATEGORY_SOIL = id("recipe/soil_restoration");
   private static final Identifier CATEGORY_MACHINES = id("recipe/reclamation_machines");

   @Override
   public Identifier id() {
      return id("provider/index_recipes");
   }

   @Override
   public List<IndexRecipeCategory> recipeCategories(Player player) {
      return List.of(
         new IndexRecipeCategory(CATEGORY_CROPS, "Agriculture Reclamation",
            new ItemStack(ModItems.RECOVERED_SEED_CAPSULE.get()), 0xFF92F7A6, 540),
         new IndexRecipeCategory(CATEGORY_SOIL, "Soil Restoration",
            new ItemStack(ModBlocks.SOIL_PURIFIER.asItem()), 0xFF8AF6B6, 545),
         new IndexRecipeCategory(CATEGORY_MACHINES, "Reclamation Machines",
            new ItemStack(ModBlocks.GREENHOUSE_CONTROLLER.asItem()), 0xFF66E8FF, 550)
      );
   }

   @Override
   public List<IndexRecipeView> recipes(Player player) {
      List<IndexRecipeView> views = new ArrayList<>();
      for (CropSpec spec : CropSpec.sorted()) {
         views.add(cropView(spec));
      }
      for (SoilState state : SoilState.values()) {
         views.add(soilView(state));
      }
      views.addAll(machineViews());
      return List.copyOf(views);
   }

   private static IndexRecipeView cropView(CropSpec spec) {
      ReclamationCropRule rule = ReclamationContent.crop(spec);
      ItemStack machine = new ItemStack(ModBlocks.HYDROPONIC_TRAY.asItem());
      List<IndexRecipeSlot> slots = new ArrayList<>();
      slots.add(new IndexRecipeSlot(IndexSlotRole.INPUT, List.of(new ItemStack(ModItems.STABILIZED_SEED.get())),
         "Seed profile: " + spec.displayName()));
      slots.add(new IndexRecipeSlot(IndexSlotRole.CATALYST, List.of(), "Crop category: " + spec.category().displayName()));
      slots.add(IndexRecipeSlot.machine(machine));
      slots.add(IndexRecipeSlot.output(new ItemStack(ModItems.produceFor(spec).get(), Math.max(1, rule.baseYield()))));
      slots.add(new IndexRecipeSlot(IndexSlotRole.OUTPUT, List.of(), "Restoration weight: " + rule.restorationWeight()));
      return new IndexRecipeView(
         id("recipe/crop/" + spec.path()),
         CATEGORY_CROPS,
         spec.displayName() + " Cultivation",
         machine,
         slots,
         List.of(
            "Base growth chance: " + rule.baseGrowthChance() + "%",
            "Base yield: " + rule.baseYield(),
            "Supported by soils matching " + spec.category().displayName() + "; greenhouse bypass at "
               + rule.greenhouseBypassThreshold() + "% safety.",
            "Hydroponic yield bonus: +" + rule.hydroponicYieldBonus(),
            "Stable crop bonus: growth +" + rule.stableGrowthBonus() + ", yield +" + rule.stableYieldBonus()),
         ReclamationContent.machines().hydroponicGrowthTicks(),
         false,
         EchoAgricultureReclamation.MODID);
   }

   private static IndexRecipeView soilView(SoilState state) {
      ReclamationSoilRule rule = ReclamationContent.soil(state);
      SoilState next = state.purifiedStep();
      ItemStack machine = new ItemStack(ModBlocks.SOIL_PURIFIER.asItem());
      List<IndexRecipeSlot> slots = new ArrayList<>();
      slots.add(IndexRecipeSlot.input(new ItemStack(ModBlocks.blockFor(state))));
      slots.add(IndexRecipeSlot.catalyst(new ItemStack(ModItems.PURIFICATION_ENZYME.get()), "Purification enzyme"));
      slots.add(IndexRecipeSlot.catalyst(new ItemStack(ModItems.SOIL_NUTRIENT_MIX.get()), "Nutrient mix"));
      slots.add(IndexRecipeSlot.machine(machine));
      slots.add(IndexRecipeSlot.output(new ItemStack(ModBlocks.blockFor(next))));
      return new IndexRecipeView(
         id("recipe/soil/" + state.name().toLowerCase(Locale.ROOT)),
         CATEGORY_SOIL,
         state.displayName() + " Purification",
         machine,
         slots,
         List.of(
            "Safe soil: " + yesNo(rule.safe()),
            "Growth chance: " + rule.growthChance() + "%",
            "Restoration gain: " + rule.restorationGain(),
            "Native support: " + categoryList(rule.supportedCategories()),
            "Stable support: " + categoryList(rule.stabilizedSupportedCategories())
               + " at " + rule.stabilizedSupportMinStability() + "% stability"),
         80,
         false,
         EchoAgricultureReclamation.MODID);
   }

   private static List<IndexRecipeView> machineViews() {
      ReclamationMachineRules rules = ReclamationContent.machines();
      return List.of(
         machineView("hydroponic_tray", "Hydroponic Growth",
            new ItemStack(ModBlocks.HYDROPONIC_TRAY.asItem()),
            List.of(
               IndexRecipeSlot.input(new ItemStack(ModItems.STABILIZED_SEED.get())),
               IndexRecipeSlot.catalyst(new ItemStack(ModItems.SOIL_NUTRIENT_MIX.get()), "Nutrient charge"),
               new IndexRecipeSlot(IndexSlotRole.OUTPUT, List.of(), "Accelerated crop growth")),
            List.of("Growth cycle: " + rules.hydroponicGrowthTicks() + " ticks",
               "Nutrient capacity: " + rules.hydroponicNutrientCap(),
               "Nutrients per mix: " + rules.hydroponicNutrientPerMix())),
         machineView("gene_stabilizer", "Seed Stabilization",
            new ItemStack(ModBlocks.GENE_STABILIZER.asItem()),
            List.of(
               IndexRecipeSlot.input(new ItemStack(ModItems.CONTAMINATED_SEED.get())),
               IndexRecipeSlot.catalyst(new ItemStack(ModItems.BIO_GEL.get()), "Bio gel"),
               IndexRecipeSlot.catalyst(new ItemStack(ModItems.GENE_SAMPLE.get()), "Gene sample"),
               IndexRecipeSlot.output(new ItemStack(ModItems.STABILIZED_SEED.get()))),
            List.of("Converts contaminated crop routes into stable seed profiles.")),
         machineView("bio_reactor", "Bioreactor Synthesis",
            new ItemStack(ModBlocks.BIO_REACTOR.asItem()),
            List.of(
               new IndexRecipeSlot(IndexSlotRole.INPUT, List.of(), "Crop matter or recovered seed biomass"),
               IndexRecipeSlot.output(new ItemStack(ModItems.BIO_GEL.get(), rules.bioReactorOrganicOutput())),
               IndexRecipeSlot.output(new ItemStack(ModItems.GENE_SAMPLE.get(), rules.bioReactorGeneSampleOutput()))),
            List.of("Processes agriculture biomass into gel and gene samples.")),
         machineView("compost_recycler", "Compost Recycling",
            new ItemStack(ModBlocks.COMPOST_RECYCLER.asItem()),
            List.of(
               new IndexRecipeSlot(IndexSlotRole.INPUT, List.of(), "Crop matter or failed growth waste"),
               IndexRecipeSlot.output(new ItemStack(ModItems.SOIL_NUTRIENT_MIX.get(), rules.compostRecyclerOutput()))),
            List.of("Reclaims plant mass into nutrient mix.")),
         machineView("greenhouse_support", "Greenhouse Support",
            new ItemStack(ModBlocks.GREENHOUSE_CONTROLLER.asItem()),
            List.of(
               IndexRecipeSlot.catalyst(new ItemStack(ModBlocks.GREENHOUSE_GLASS.asItem()), "Greenhouse glass"),
               IndexRecipeSlot.catalyst(new ItemStack(ModBlocks.SPORE_FILTER.asItem()), "Spore filter"),
               IndexRecipeSlot.catalyst(new ItemStack(ModBlocks.POLLINATOR_DRONE_DOCK.asItem()), "Pollinator dock"),
               new IndexRecipeSlot(IndexSlotRole.OUTPUT, List.of(), "Safe greenhouse envelope")),
            List.of("Safe threshold: " + ReclamationContent.progression().greenhouseSafeThreshold() + "%",
               "Scan range: " + rules.greenhouseHorizontalRange() + " horizontal, "
                  + rules.greenhouseDownRange() + " down, " + rules.greenhouseUpRange() + " up",
               "Pollinator service radius: " + rules.pollinatorDroneServiceRadius()
                  + ", growth bonus +" + rules.pollinatorDroneGrowthBonus()))
      );
   }

   private static IndexRecipeView machineView(String path, String title, ItemStack machine,
         List<IndexRecipeSlot> slots, List<String> notes) {
      List<IndexRecipeSlot> allSlots = new ArrayList<>(slots);
      allSlots.add(IndexRecipeSlot.machine(machine));
      return new IndexRecipeView(
         id("recipe/machine/" + path),
         CATEGORY_MACHINES,
         title,
         machine,
         allSlots,
         notes,
         0,
         false,
         EchoAgricultureReclamation.MODID);
   }

   private static String categoryList(Set<CropCategory> categories) {
      if (categories == null || categories.isEmpty()) {
         return "none";
      }
      return categories.stream()
         .map(CropCategory::displayName)
         .sorted(String::compareTo)
         .reduce((left, right) -> left + ", " + right)
         .orElse("none");
   }

   private static String yesNo(boolean value) {
      return value ? "yes" : "no";
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(EchoAgricultureReclamation.MODID, sanitize(path));
   }

   private static String sanitize(String path) {
      String clean = path == null ? "unknown" : path.trim().toLowerCase(Locale.ROOT);
      clean = clean.replace('\\', '/').replace(':', '/').replaceAll("[^a-z0-9_./-]", "_");
      while (clean.contains("//")) {
         clean = clean.replace("//", "/");
      }
      return clean.isBlank() ? "unknown" : clean;
   }
}
