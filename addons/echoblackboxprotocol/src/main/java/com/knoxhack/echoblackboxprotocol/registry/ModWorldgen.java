package com.knoxhack.echoblackboxprotocol.registry;

import com.knoxhack.echoblackboxprotocol.world.BlackboxDungeonGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModWorldgen {
   private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(
      Registries.CHUNK_GENERATOR, "echoblackboxprotocol"
   );
   public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<BlackboxDungeonGenerator>> BLACKBOX_DUNGEON = CHUNK_GENERATORS.register(
      "blackbox_dungeon", () -> BlackboxDungeonGenerator.CODEC
   );

   private ModWorldgen() {
   }

   public static void register(IEventBus eventBus) {
      CHUNK_GENERATORS.register(eventBus);
   }
}
