package com.knoxhack.echoindustrialnexus.integration;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.index.IIndexRecipeProvider;
import com.knoxhack.echocore.api.index.IndexRecipeCategory;
import com.knoxhack.echocore.api.index.IndexRecipeSlot;
import com.knoxhack.echocore.api.index.IndexRecipeView;
import com.knoxhack.echocore.api.index.IndexSlotRole;
import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import com.knoxhack.echoindustrialnexus.block.IndustrialMachineBlock;
import com.knoxhack.echoindustrialnexus.recipe.IndustrialProcessingRecipe;
import com.knoxhack.echoindustrialnexus.registry.ModBlocks;
import com.knoxhack.echoindustrialnexus.registry.ModRecipes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;

public enum IndustrialIndexProvider implements IIndexRecipeProvider {
   INSTANCE;

   private static final int ACCENT = 0xFFFF9F3D;

   public static void register() {
      EchoCoreServices.registerIndexRecipeProvider(INSTANCE);
   }

   @Override
   public Identifier id() {
      return EchoIndustrialNexus.id("provider/index_recipes");
   }

   @Override
   public List<IndexRecipeCategory> recipeCategories(Player player) {
      List<IndexRecipeCategory> categories = new ArrayList<>();
      for (IndustrialMachineBlock.MachineKind kind : IndustrialMachineBlock.MachineKind.values()) {
         if (kind.recipeDriven()) {
            categories.add(new IndexRecipeCategory(categoryId(kind), kind.displayName(), machineStack(kind), ACCENT, 400 + kind.ordinal()));
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
         if (holder.value() instanceof IndustrialProcessingRecipe recipe) {
            views.add(view(holder, recipe));
         }
      }
      return List.copyOf(views);
   }

   private static IndexRecipeView view(RecipeHolder<?> holder, IndustrialProcessingRecipe recipe) {
      IndustrialMachineBlock.MachineKind kind = recipe.machine();
      ItemStack machine = machineStack(kind);
      List<IndexRecipeSlot> slots = new ArrayList<>();
      List<ItemStack> inputs = stacks(recipe.ingredient(), 1);
      if (!inputs.isEmpty()) {
         slots.add(IndexRecipeSlot.inputs(inputs));
      }
      if (recipe.inputFluidAmount() > 0) {
         slots.add(new IndexRecipeSlot(IndexSlotRole.INPUT, List.of(),
            "Input fluid #" + recipe.inputFluidId() + " x" + recipe.inputFluidAmount()));
      }
      ItemStack catalyst = recipe.catalyst();
      if (!catalyst.isEmpty()) {
         slots.add(IndexRecipeSlot.catalyst(catalyst, "Catalyst"));
      }
      if (!machine.isEmpty()) {
         slots.add(IndexRecipeSlot.machine(machine));
      }
      ItemStack output = recipe.result();
      if (!output.isEmpty()) {
         slots.add(IndexRecipeSlot.output(output));
      }
      ItemStack byproduct = recipe.byproduct();
      if (!byproduct.isEmpty()) {
         slots.add(new IndexRecipeSlot(IndexSlotRole.OUTPUT, List.of(byproduct), "Byproduct"));
      }
      if (recipe.outputFluidAmount() > 0) {
         slots.add(new IndexRecipeSlot(IndexSlotRole.OUTPUT, List.of(),
            "Output fluid #" + recipe.outputFluidId() + " x" + recipe.outputFluidAmount()));
      }

      List<String> notes = new ArrayList<>();
      if (!byproduct.isEmpty()) {
         notes.add(recipe.byproductChance() + "% chance for byproduct output.");
      }
      if (recipe.fluxCost() > 0) {
         notes.add("Consumes " + recipe.fluxCost() + " Thermal Flux.");
      }
      if (recipe.fluxGeneration() > 0) {
         notes.add("Generates " + recipe.fluxGeneration() + " Thermal Flux.");
      }
      if (recipe.heat() > 0) {
         notes.add("Adds " + recipe.heat() + " heat.");
      }

      String title = kind.displayName() + ": " + (output.isEmpty() ? "Fluid Process" : output.getHoverName().getString());
      return new IndexRecipeView(
         holder.id().identifier(),
         categoryId(kind),
         title,
         machine,
         slots,
         notes,
         recipe.duration(),
         false,
         EchoIndustrialNexus.MODID);
   }

   private static List<RecipeHolder<?>> recipeHolders(Player player) {
      MinecraftServer server = player.level().getServer();
      if (server == null) {
         return List.of();
      }
      try {
         List<RecipeHolder<?>> holders = new ArrayList<>();
         for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            if (holder.value().getType() == ModRecipes.INDUSTRIAL_PROCESSING_TYPE.get()) {
               holders.add(holder);
            }
         }
         return holders;
      } catch (RuntimeException exception) {
         EchoIndustrialNexus.LOGGER.debug("ECHO: Index could not enumerate Industrial recipes.", exception);
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

   private static Identifier categoryId(IndustrialMachineBlock.MachineKind kind) {
      return EchoIndustrialNexus.id("recipe/" + kind.getSerializedName());
   }

   private static ItemStack machineStack(IndustrialMachineBlock.MachineKind kind) {
      Optional<Block> block = ModBlocks.ALL_BLOCKS.stream()
         .map(holder -> holder.get())
         .filter(candidate -> candidate instanceof IndustrialMachineBlock machine && machine.kind() == kind)
         .findFirst();
      return block.map(value -> new ItemStack(value.asItem())).orElse(ItemStack.EMPTY);
   }

   @SuppressWarnings("unused")
   private static Identifier itemId(ItemStack stack) {
      Identifier id = stack.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(stack.getItem());
      return id == null ? Identifier.withDefaultNamespace("air") : id;
   }
}
