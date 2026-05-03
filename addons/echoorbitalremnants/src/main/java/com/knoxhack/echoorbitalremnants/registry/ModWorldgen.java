package com.knoxhack.echoorbitalremnants.registry;

import com.knoxhack.echoorbitalremnants.EchoOrbitalRemnants;
import com.knoxhack.echoorbitalremnants.world.RouteTerrainGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModWorldgen {
    private static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, EchoOrbitalRemnants.MODID);

    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<RouteTerrainGenerator>> ROUTE_TERRAIN =
            CHUNK_GENERATORS.register("route_terrain", () -> RouteTerrainGenerator.CODEC);

    private ModWorldgen() {
    }

    public static void register(IEventBus eventBus) {
        CHUNK_GENERATORS.register(eventBus);
    }
}
