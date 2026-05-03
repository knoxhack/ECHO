package com.knoxhack.echoashfallprotocol.registry;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Biome registrations for ECHO: ASHFALL PROTOCOL.
 * Re-compiled to fix stale cache issues.
 */
public class ModBiomes {
    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(Registries.BIOME, EchoAshfallProtocol.MODID);
    
    // Biome references - actual registration via JSON in data/echoashfallprotocol/worldgen/biome
    public static final DeferredHolder<Biome, Biome> THE_WASTELAND = BIOMES.register(
            "the_wasteland", () -> null);
    public static final DeferredHolder<Biome, Biome> CRASH_ZONE_WASTELAND = BIOMES.register(
            "crash_zone_wasteland", () -> null); // JSON overrides
    public static final DeferredHolder<Biome, Biome> INDUSTRIAL_RUINS = BIOMES.register(
            "industrial_ruins", () -> null);
    public static final DeferredHolder<Biome, Biome> TOXIC_SWAMP = BIOMES.register(
            "toxic_swamp", () -> null);
    public static final DeferredHolder<Biome, Biome> RUINED_CITYSCAPE = BIOMES.register(
            "ruined_cityscape", () -> null);
    public static final DeferredHolder<Biome, Biome> RUINED_PLAINS = BIOMES.register(
            "ruined_plains", () -> null);
    public static final DeferredHolder<Biome, Biome> RADIATION_ZONE = BIOMES.register(
            "radiation_zone", () -> null);
    
    // Exploration 1.1 - Cryogenic Ruins Biome
    public static final DeferredHolder<Biome, Biome> CRYOGENIC_RUINS = BIOMES.register(
            "cryogenic_ruins", 
            com.knoxhack.echoashfallprotocol.world.CryogenicRuinsBiome::create);

    public static final DeferredHolder<Biome, Biome> NEXUS_SCAR = BIOMES.register(
            "nexus_scar", () -> null);
}
