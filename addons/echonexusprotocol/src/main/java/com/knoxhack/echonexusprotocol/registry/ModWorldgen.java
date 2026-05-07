package com.knoxhack.echonexusprotocol.registry;

import com.knoxhack.echonexusprotocol.world.NexusTerrainGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModWorldgen {
   private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(
      Registries.CHUNK_GENERATOR, "echonexusprotocol"
   );
   public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<NexusTerrainGenerator>> NEXUS_TERRAIN = CHUNK_GENERATORS.register(
      "nexus_terrain", () -> NexusTerrainGenerator.CODEC
   );

   private ModWorldgen() {
   }

   public static void register(IEventBus eventBus) {
      CHUNK_GENERATORS.register(eventBus);
   }
}
