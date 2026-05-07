package com.knoxhack.echoblackboxprotocol.registry;

import com.knoxhack.echoblackboxprotocol.EchoBlackboxProtocol;
import com.knoxhack.echoblackboxprotocol.recipe.BlackboxProcessingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
   private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, EchoBlackboxProtocol.MODID);
   private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, EchoBlackboxProtocol.MODID);
   public static final DeferredHolder<RecipeType<?>, RecipeType<BlackboxProcessingRecipe>> BLACKBOX_PROCESSING_TYPE = RECIPE_TYPES.register(
      "blackbox_processing", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(EchoBlackboxProtocol.MODID, "blackbox_processing"))
   );
   public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BlackboxProcessingRecipe>> BLACKBOX_PROCESSING_SERIALIZER = RECIPE_SERIALIZERS.register(
      "blackbox_processing", () -> new RecipeSerializer(BlackboxProcessingRecipe.CODEC, BlackboxProcessingRecipe.STREAM_CODEC)
   );

   private ModRecipes() {
   }

   public static void register(IEventBus eventBus) {
      RECIPE_TYPES.register(eventBus);
      RECIPE_SERIALIZERS.register(eventBus);
   }
}
