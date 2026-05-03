package com.knoxhack.echoorbitalremnants.registry;

import com.knoxhack.echoorbitalremnants.EchoOrbitalRemnants;
import com.knoxhack.echoorbitalremnants.recipe.OrbitalProcessingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, EchoOrbitalRemnants.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, EchoOrbitalRemnants.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<OrbitalProcessingRecipe>> ORBITAL_PROCESSING_TYPE =
            RECIPE_TYPES.register("orbital_processing",
                    () -> RecipeType.simple(Identifier.fromNamespaceAndPath(EchoOrbitalRemnants.MODID, "orbital_processing")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<OrbitalProcessingRecipe>> ORBITAL_PROCESSING_SERIALIZER =
            RECIPE_SERIALIZERS.register("orbital_processing",
                    () -> new RecipeSerializer<>(OrbitalProcessingRecipe.CODEC, OrbitalProcessingRecipe.STREAM_CODEC));

    private ModRecipes() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
