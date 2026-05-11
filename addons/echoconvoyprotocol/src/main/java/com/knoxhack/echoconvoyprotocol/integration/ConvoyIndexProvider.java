package com.knoxhack.echoconvoyprotocol.integration;

import com.knoxhack.echoconvoyprotocol.EchoConvoyProtocol;
import com.knoxhack.echoconvoyprotocol.block.ConvoyBlock.ConvoyBlockKind;
import com.knoxhack.echoconvoyprotocol.recipe.ConvoyStationRecipe;
import com.knoxhack.echoconvoyprotocol.registry.ModBlocks;
import com.knoxhack.echoconvoyprotocol.registry.ModRecipes;
import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.index.IIndexEntryProvider;
import com.knoxhack.echocore.api.index.IIndexRecipeProvider;
import com.knoxhack.echocore.api.index.IIndexRegistry;
import com.knoxhack.echocore.api.index.IndexCategory;
import com.knoxhack.echocore.api.index.IndexEntry;
import com.knoxhack.echocore.api.index.IndexEntryState;
import com.knoxhack.echocore.api.index.IndexRecipeCategory;
import com.knoxhack.echocore.api.index.IndexRecipeSlot;
import com.knoxhack.echocore.api.index.IndexRecipeView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

public enum ConvoyIndexProvider implements IIndexEntryProvider, IIndexRecipeProvider {
   INSTANCE;

   private static final Identifier CATEGORY_MACHINES = id("machines");

   public static void register() {
      EchoCoreServices.registerIndexProvider(INSTANCE);
      EchoCoreServices.registerIndexRecipeProvider(INSTANCE);
   }

   @Override
   public Identifier id() {
      return id("provider/index");
   }

   @Override
   public void register(IIndexRegistry registry) {
      registry.registerCategory(new IndexCategory(
         CATEGORY_MACHINES,
         "Convoy Stations",
         "Vehicle fabrication, fuel, battery, and field support stations.",
         machineStack(ConvoyBlockKind.VEHICLE_WORKBENCH),
         140,
         EchoConvoyProtocol.MODID));
      for (ConvoyBlockKind kind : ConvoyBlockKind.values()) {
         registry.registerEntry(new IndexEntry(
            id("station/" + kind.getSerializedName()),
            CATEGORY_MACHINES,
            kind.displayName(),
            "",
            kind.recipeDriven() ? "Processes route and vehicle supplies." : "Supports route, vehicle, or field logistics.",
            kind.displayName() + " is part of the Convoy Protocol field station network.",
            machineStack(kind),
            EchoConvoyProtocol.MODID,
            List.of("machine", "convoy", "station", kind.getSerializedName()),
            IndexEntryState.VISIBLE,
            List.of(),
            List.of(itemId(machineStack(kind))),
            List.of(),
            10 + kind.ordinal()));
      }
   }

   @Override
   public List<IndexRecipeCategory> recipeCategories(Player player) {
      List<IndexRecipeCategory> categories = new ArrayList<>();
      for (ConvoyBlockKind kind : ConvoyBlockKind.values()) {
         if (kind.recipeDriven()) {
            categories.add(new IndexRecipeCategory(categoryId(kind), kind.displayName(),
               machineStack(kind), 0xFFFFD166, 360 + kind.ordinal()));
         }
      }
      return categories;
   }

   @Override
   public List<IndexRecipeView> recipes(Player player) {
      if (player == null || player.level() == null) {
         return List.of();
      }
      List<IndexRecipeView> views = new ArrayList<>();
      for (RecipeHolder<?> holder : recipeHolders(player)) {
         if (!(holder.value() instanceof ConvoyStationRecipe recipe)) {
            continue;
         }
         ItemStack machine = machineStack(recipe.station());
         List<IndexRecipeSlot> slots = new ArrayList<>();
         for (ConvoyStationRecipe.StationIngredient ingredient : recipe.ingredients()) {
            slots.add(IndexRecipeSlot.inputs(stacks(ingredient.ingredient(), ingredient.count())));
         }
         slots.add(IndexRecipeSlot.machine(machine));
         slots.add(IndexRecipeSlot.output(recipe.result()));
         views.add(new IndexRecipeView(
            holder.id().identifier(),
            categoryId(recipe.station()),
            recipe.result().getHoverName().getString(),
            machine,
            slots,
            List.of("Station: " + recipe.station().displayName(),
               "Energy: " + recipe.energyCost(),
               "Duration: " + recipe.duration() + " ticks"),
            recipe.duration(),
            false,
            EchoConvoyProtocol.MODID));
      }
      return views;
   }

   private static List<RecipeHolder<?>> recipeHolders(Player player) {
      MinecraftServer server = player.level().getServer();
      if (server == null) {
         return List.of();
      }
      try {
         List<RecipeHolder<?>> holders = new ArrayList<>();
         for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            if (holder.value().getType() == ModRecipes.CONVOY_STATION_PROCESSING_TYPE.get()) {
               holders.add(holder);
            }
         }
         return holders;
      } catch (RuntimeException ignored) {
         EchoConvoyProtocol.LOGGER.debug("ECHO: Index could not enumerate Convoy station recipes.");
      }
      return List.of();
   }

   private static List<ItemStack> stacks(Ingredient ingredient, int count) {
      if (ingredient == null || ingredient.isEmpty()) {
         return List.of();
      }
      return ingredient.items()
         .map(Holder::value)
         .map(item -> new ItemStack(item, Math.max(1, count)))
         .filter(stack -> !stack.isEmpty())
         .limit(24)
         .toList();
   }

   private static Identifier categoryId(ConvoyBlockKind kind) {
      return id("recipe/" + kind.getSerializedName());
   }

   private static ItemStack machineStack(ConvoyBlockKind kind) {
      return new ItemStack(machineBlock(kind).get());
   }

   private static DeferredBlock<Block> machineBlock(ConvoyBlockKind kind) {
      return switch (kind) {
         case VEHICLE_WORKBENCH -> ModBlocks.VEHICLE_WORKBENCH;
         case FUEL_STILL -> ModBlocks.FUEL_STILL;
         case BATTERY_CHARGING_PAD -> ModBlocks.BATTERY_CHARGING_PAD;
         case VEHICLE_DOCK -> ModBlocks.VEHICLE_DOCK;
         case CONVOY_BEACON -> ModBlocks.CONVOY_BEACON;
         case ROADSIDE_SIGNAL_MARKER -> ModBlocks.ROADSIDE_SIGNAL_MARKER;
         case CARGO_ANCHOR -> ModBlocks.CARGO_ANCHOR;
         case FIELD_REPAIR_STATION -> ModBlocks.FIELD_REPAIR_STATION;
      };
   }

   private static Identifier itemId(ItemStack stack) {
      Identifier id = stack.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(stack.getItem());
      return id == null ? Identifier.withDefaultNamespace("air") : id;
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(EchoConvoyProtocol.MODID, sanitize(path));
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
