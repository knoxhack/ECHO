package com.knoxhack.echoindustrialnexus.registry;

import com.knoxhack.echoindustrialnexus.recipe.IndustrialProcessingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
   private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "echoindustrialnexus");
   private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "echoindustrialnexus");
   public static final DeferredHolder<RecipeType<?>, RecipeType<IndustrialProcessingRecipe>> INDUSTRIAL_PROCESSING_TYPE = RECIPE_TYPES.register(
      "industrial_processing", () -> RecipeType.simple(Identifier.fromNamespaceAndPath("echoindustrialnexus", "industrial_processing"))
   );
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<IndustrialProcessingRecipe>> INDUSTRIAL_PROCESSING_SERIALIZER = RECIPE_SERIALIZERS.register(
      "industrial_processing", () -> new RecipeSerializer(IndustrialProcessingRecipe.CODEC, IndustrialProcessingRecipe.STREAM_CODEC)
   );

   private ModRecipes() {
   }

   public static void register(IEventBus eventBus) {
      RECIPE_TYPES.register(eventBus);
      RECIPE_SERIALIZERS.register(eventBus);
   }
}
