package com.knoxhack.echoashfallprotocol;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import com.knoxhack.echoashfallprotocol.entity.ModEntities;
import com.knoxhack.echoashfallprotocol.integration.AshfallCoreServices;
import com.knoxhack.echoashfallprotocol.recipe.ScrapPressRecipe;
import com.knoxhack.echoashfallprotocol.registry.*;
import com.knoxhack.echoashfallprotocol.test.ModGameTests;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.core.registries.Registries;

/**
 * ECHO: Ashfall Protocol
 * A post-apocalyptic survival overhaul for Minecraft.
 * 
 * Core systems:
 * - ECHO-7 AI Guide
 * - Friction-based progression
 * - Mutation system
 * - Smart reactive events
 * - Deep machine crafting
 */
@Mod(EchoAshfallProtocol.MODID)
public class EchoAshfallProtocol {
    public static final String MODID = "echoashfallprotocol";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoAshfallProtocol(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // === REGISTER ALL DEFERRED REGISTERS ===
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModBiomes.BIOMES.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModPoiTypes.POI_TYPES.register(modEventBus);
        ModGameTests.register(modEventBus);
        modEventBus.addListener(ModEntities::registerAttributes);
        modEventBus.addListener(ModEntities::registerSpawnPlacements);
        modEventBus.addListener(ModEnergyCapabilities::register);
        modEventBus.addListener(ModGameTests::registerTests);

        // === REGISTER EVENT HANDLERS ===
        // Save migration handler for exploration progress.
        NeoForge.EVENT_BUS.register(com.knoxhack.echoashfallprotocol.data.SaveMigrationHandler.class);

        // Mod initialization complete
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("=== ECHO: Ashfall Protocol ===");
        LOGGER.info("Initializing survival systems...");
        LOGGER.info("Loading Ashfall + Orbital public beta route v1.3.0...");

        event.enqueueWork(() -> {
            AshfallCoreServices.register();
            // Register Scrap Press recipes
            registerScrapPressRecipes();
        });

        LOGGER.info("ECHO-7 AI Guide: ONLINE");
        LOGGER.info("Mutation System: ACTIVE");
        LOGGER.info("Smart Event Framework: ENABLED");
        LOGGER.info("Faction System: ACTIVE (10 Echo Core Ashfall factions)");
        LOGGER.info("Research System: ACTIVE (15 Perks, 5 Schematics)");
        LOGGER.info("Cold Survival: ACTIVE (Cryogenic Ruins Biome)");
        LOGGER.info("Fast Travel: ACTIVE (Radio Network)");
        LOGGER.info("POI System: ACTIVE (route-specific exploration profiles)");
        LOGGER.info("All systems initialized. Welcome to the wasteland.");
    }

    private void registerScrapPressRecipes() {
        // Use lazy suppliers to avoid creating ItemStacks during mod setup
        // (ItemStack requires components to be bound, which happens later)

        // 9 Scrap Metal -> 1 Machine Casing (compressed crafting component)
        ScrapPressRecipe.register(
            ModItems.SCRAP_METAL, 9,
            ModItems.MACHINE_CASING, 1,
            40
        );

        // 4 Scrap Circuits -> 1 Circuit Board (salvage usable components)
        ScrapPressRecipe.register(
            ModItems.SCRAP_CIRCUIT, 4,
            ModItems.CIRCUIT_BOARD, 1,
            60
        );

        // 4 Scrap Plastic -> 1 Filtration Membrane (pressure-forming)
        ScrapPressRecipe.register(
            ModItems.SCRAP_PLASTIC, 4,
            ModItems.FILTRATION_MEMBRANE, 1,
            50
        );

        LOGGER.info("Registered {} Scrap Press recipes", ScrapPressRecipe.getAllRecipes().size());
    }
}
