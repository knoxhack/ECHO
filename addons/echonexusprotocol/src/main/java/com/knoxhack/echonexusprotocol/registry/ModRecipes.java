package com.knoxhack.echonexusprotocol.registry;

import com.knoxhack.echonexusprotocol.recipe.NexusProcessingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
   private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "echonexusprotocol");
   private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "echonexusprotocol");
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<NexusProcessingRecipe>> NEXUS_PROCESSING_SERIALIZER = SERIALIZERS.register(
      "nexus_processing", () -> new RecipeSerializer(NexusProcessingRecipe.CODEC, NexusProcessingRecipe.STREAM_CODEC)
   );
   public static final DeferredHolder<RecipeType<?>, RecipeType<NexusProcessingRecipe>> NEXUS_PROCESSING_TYPE = TYPES.register(
      "nexus_processing", () -> RecipeType.simple(Identifier.fromNamespaceAndPath("echonexusprotocol", "nexus_processing"))
   );

   private ModRecipes() {
   }

   public static void register(IEventBus eventBus) {
      SERIALIZERS.register(eventBus);
      TYPES.register(eventBus);
   }
}
