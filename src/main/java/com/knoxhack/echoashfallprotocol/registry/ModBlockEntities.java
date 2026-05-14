package com.knoxhack.echoashfallprotocol.registry;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.block.entity.*;
import com.knoxhack.echoashfallprotocol.block.entity.DeepCoreMinerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EchoAshfallProtocol.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HandRecyclerBlockEntity>> HAND_RECYCLER =
            BLOCK_ENTITIES.register("hand_recycler",
                    () -> new BlockEntityType<>(HandRecyclerBlockEntity::new, Set.of(ModBlocks.HAND_RECYCLER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThermalBurnerBlockEntity>> THERMAL_BURNER =
            BLOCK_ENTITIES.register("thermal_burner",
                    () -> new BlockEntityType<>(ThermalBurnerBlockEntity::new, Set.of(ModBlocks.THERMAL_BURNER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterPurifierBlockEntity>> WATER_PURIFIER =
            BLOCK_ENTITIES.register("water_purifier",
                    () -> new BlockEntityType<>(WaterPurifierBlockEntity::new, Set.of(ModBlocks.WATER_PURIFIER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MicroGeneratorBlockEntity>> MICRO_GENERATOR =
            BLOCK_ENTITIES.register("micro_generator",
                    () -> new BlockEntityType<>(MicroGeneratorBlockEntity::new, Set.of(ModBlocks.MICRO_GENERATOR.get())));

    // === TIER 2.5 POWER GENERATION (Machinery Expansion) ===
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.knoxhack.echoashfallprotocol.block.entity.ThermalArrayBlockEntity>> THERMAL_ARRAY =
            BLOCK_ENTITIES.register("thermal_array",
                    () -> new BlockEntityType<>(com.knoxhack.echoashfallprotocol.block.entity.ThermalArrayBlockEntity::new, Set.of(ModBlocks.THERMAL_ARRAY.get())));

    // Orphan Machines - Phase 2 Implementation
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatteryBankBlockEntity>> BATTERY_BANK =
            BLOCK_ENTITIES.register("battery_bank",
                    () -> new BlockEntityType<>(BatteryBankBlockEntity::new, Set.of(ModBlocks.BATTERY_BANK.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ScrapDynamoBlockEntity>> SCRAP_DYNAMO =
            BLOCK_ENTITIES.register("scrap_dynamo",
                    () -> new BlockEntityType<>(ScrapDynamoBlockEntity::new, Set.of(ModBlocks.SCRAP_DYNAMO.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NexusCapacitorBlockEntity>> NEXUS_CAPACITOR =
            BLOCK_ENTITIES.register("nexus_capacitor",
                    () -> new BlockEntityType<>(NexusCapacitorBlockEntity::new, Set.of(ModBlocks.NEXUS_CAPACITOR.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LoadDistributorBlockEntity>> LOAD_DISTRIBUTOR =
            BLOCK_ENTITIES.register("load_distributor",
                    () -> new BlockEntityType<>(LoadDistributorBlockEntity::new, Set.of(ModBlocks.LOAD_DISTRIBUTOR.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ScrapPressBlockEntity>> SCRAP_PRESS =
            BLOCK_ENTITIES.register("scrap_press",
                    () -> new BlockEntityType<>(ScrapPressBlockEntity::new, Set.of(ModBlocks.SCRAP_PRESS.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SignalScannerBlockEntity>> SIGNAL_SCANNER =
            BLOCK_ENTITIES.register("signal_scanner",
                    () -> new BlockEntityType<>(SignalScannerBlockEntity::new, Set.of(ModBlocks.SIGNAL_SCANNER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FieldMedBayBlockEntity>> FIELD_MED_BAY =
            BLOCK_ENTITIES.register("field_med_bay",
                    () -> new BlockEntityType<>(FieldMedBayBlockEntity::new, Set.of(ModBlocks.FIELD_MED_BAY.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AtmosphericScrubberBlockEntity>> ATMOSPHERIC_SCRUBBER =
            BLOCK_ENTITIES.register("atmospheric_scrubber",
                    () -> new BlockEntityType<>(AtmosphericScrubberBlockEntity::new, Set.of(ModBlocks.ATMOSPHERIC_SCRUBBER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutofeedHopperBlockEntity>> AUTOFEED_HOPPER =
            BLOCK_ENTITIES.register("autofeed_hopper",
                    () -> new BlockEntityType<>(AutofeedHopperBlockEntity::new, Set.of(ModBlocks.AUTOFEED_HOPPER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ContaminantCondenserBlockEntity>> CONTAMINANT_CONDENSER =
            BLOCK_ENTITIES.register("contaminant_condenser",
                    () -> new BlockEntityType<>(ContaminantCondenserBlockEntity::new, Set.of(ModBlocks.CONTAMINANT_CONDENSER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FilterWorkbenchBlockEntity>> FILTER_WORKBENCH =
            BLOCK_ENTITIES.register("filter_workbench",
                    () -> new BlockEntityType<>(FilterWorkbenchBlockEntity::new, Set.of(ModBlocks.FILTER_WORKBENCH.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PowerNodeBlockEntity>> POWER_NODE =
            BLOCK_ENTITIES.register("power_node",
                    () -> new BlockEntityType<>(PowerNodeBlockEntity::new, Set.of(ModBlocks.POWER_NODE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NexusCoreBlockEntity>> NEXUS_CORE =
            BLOCK_ENTITIES.register("nexus_core",
                    () -> new BlockEntityType<>(NexusCoreBlockEntity::new, Set.of(ModBlocks.NEXUS_CORE.get())));

    // === GEO-EXTRACTOR MACHINES ===
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.knoxhack.echoashfallprotocol.block.entity.OreGrinderBlockEntity>> ORE_GRINDER =
            BLOCK_ENTITIES.register("ore_grinder",
                    () -> new BlockEntityType<>(com.knoxhack.echoashfallprotocol.block.entity.OreGrinderBlockEntity::new, Set.of(ModBlocks.ORE_GRINDER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.knoxhack.echoashfallprotocol.block.entity.IsotopeRefinerBlockEntity>> ISOTOPE_REFINER =
            BLOCK_ENTITIES.register("isotope_refiner",
                    () -> new BlockEntityType<>(com.knoxhack.echoashfallprotocol.block.entity.IsotopeRefinerBlockEntity::new, Set.of(ModBlocks.ISOTOPE_REFINER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.knoxhack.echoashfallprotocol.block.entity.CrystallineSynthesizerBlockEntity>> CRYSTALLINE_SYNTHESIZER =
            BLOCK_ENTITIES.register("crystalline_synthesizer",
                    () -> new BlockEntityType<>(com.knoxhack.echoashfallprotocol.block.entity.CrystallineSynthesizerBlockEntity::new, Set.of(ModBlocks.CRYSTALLINE_SYNTHESIZER.get())));

    // === ENDGAME MACHINES ===
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeepCoreMinerBlockEntity>> DEEP_CORE_MINER =
            BLOCK_ENTITIES.register("deep_core_miner",
                    () -> new BlockEntityType<>(DeepCoreMinerBlockEntity::new, Set.of(ModBlocks.DEEP_CORE_MINER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.knoxhack.echoashfallprotocol.block.entity.RadiationCleanserBlockEntity>> RADIATION_CLEANSER =
            BLOCK_ENTITIES.register("radiation_cleanser",
                    () -> new BlockEntityType<>(com.knoxhack.echoashfallprotocol.block.entity.RadiationCleanserBlockEntity::new, Set.of(ModBlocks.RADIATION_CLEANSER.get())));

    // === MACHINE INTEGRATION ===
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.knoxhack.echoashfallprotocol.block.entity.ItemPipeBlockEntity>> ITEM_PIPE =
            BLOCK_ENTITIES.register("item_pipe",
                    () -> new BlockEntityType<>(com.knoxhack.echoashfallprotocol.block.entity.ItemPipeBlockEntity::new, Set.of(ModBlocks.ITEM_PIPE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.knoxhack.echoashfallprotocol.block.entity.PowerCableBlockEntity>> POWER_CABLE =
            BLOCK_ENTITIES.register("power_cable",
                    () -> new BlockEntityType<>(com.knoxhack.echoashfallprotocol.block.entity.PowerCableBlockEntity::new, Set.of(
                            ModBlocks.POWER_CABLE.get(),
                            ModBlocks.REINFORCED_POWER_CABLE.get(),
                            ModBlocks.HIGH_VOLTAGE_POWER_CABLE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.knoxhack.echoashfallprotocol.block.entity.FactoryControllerBlockEntity>> FACTORY_CONTROLLER =
            BLOCK_ENTITIES.register("factory_controller",
                    () -> new BlockEntityType<>(com.knoxhack.echoashfallprotocol.block.entity.FactoryControllerBlockEntity::new, Set.of(ModBlocks.FACTORY_CONTROLLER.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StructureCacheBlockEntity>> STRUCTURE_CACHE =
            BLOCK_ENTITIES.register("structure_cache",
                    () -> new BlockEntityType<>(StructureCacheBlockEntity::new, Set.of(ModBlocks.STRUCTURE_CACHE.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EchoContainerBlockEntity>> ECHO_CONTAINER =
            BLOCK_ENTITIES.register("echo_container",
                    () -> new BlockEntityType<>(EchoContainerBlockEntity::new, Set.of(
                            ModBlocks.ECHO_CACHE.get(),
                            ModBlocks.ECHO_CRATE.get(),
                            ModBlocks.SUPPLY_CRATE.get())));
}
