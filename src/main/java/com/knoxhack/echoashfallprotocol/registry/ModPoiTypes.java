package com.knoxhack.echoashfallprotocol.registry;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Registers Point of Interest types for faction villager professions.
 * Each POI type links a profession block to a villager profession.
 */
public class ModPoiTypes {

    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, EchoAshfallProtocol.MODID);

    // === REMNANT POIs ===
    public static final DeferredHolder<PoiType, PoiType> WEAPON_RACK_POI = registerPoi(
            "weapon_rack_poi", ModBlocks.WEAPON_RACK, 1, 1
    );
    public static final DeferredHolder<PoiType, PoiType> SUPPLY_CRATE_POI = registerPoi(
            "supply_crate_poi", ModBlocks.SUPPLY_CRATE, 1, 1
    );

    // === SALVAGER POIs ===
    public static final DeferredHolder<PoiType, PoiType> TRADE_COUNTER_POI = registerPoi(
            "trade_counter_poi", ModBlocks.TRADE_COUNTER, 1, 1
    );
    public static final DeferredHolder<PoiType, PoiType> MAP_TABLE_POI = registerPoi(
            "map_table_poi", ModBlocks.MAP_TABLE, 1, 1
    );

    // === MUTANT POIs ===
    public static final DeferredHolder<PoiType, PoiType> BIO_PROCESSING_STATION_POI = registerPoi(
            "bio_processing_station_poi", ModBlocks.BIO_PROCESSING_STATION, 1, 1
    );
    public static final DeferredHolder<PoiType, PoiType> SPORE_GARDEN_POI = registerPoi(
            "spore_garden_poi", ModBlocks.SPORE_GARDEN, 1, 1
    );

    /**
     * Register a POI type for a given block.
     *
     * @param name       registry name
     * @param block      the block supplier that acts as the job site
     * @param maxTickets max villagers that can use this POI simultaneously
     * @param validRange how close a villager must be to claim it
     */
    private static DeferredHolder<PoiType, PoiType> registerPoi(String name, Supplier<? extends Block> block, int maxTickets, int validRange) {
        return POI_TYPES.register(name, id -> {
            Set<BlockState> matchingStates = new HashSet<>(block.get().getStateDefinition().getPossibleStates());
            return new PoiType(
                    matchingStates,
                    maxTickets,
                    validRange
            );
        });
    }

    /**
     * Helper to get all registered POI keys for validation.
     */
    public static Set<ResourceKey<PoiType>> getAllPoiKeys() {
        return POI_TYPES.getEntries().stream()
                .map(DeferredHolder::getKey)
                .collect(Collectors.toSet());
    }
}
