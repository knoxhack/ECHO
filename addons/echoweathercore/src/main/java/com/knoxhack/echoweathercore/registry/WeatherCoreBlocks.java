package com.knoxhack.echoweathercore.registry;

import com.knoxhack.echoweathercore.EchoWeatherCore;
import com.knoxhack.echoweathercore.block.ClimateSensorBlock;
import com.knoxhack.echoweathercore.block.EmergencySirenBlock;
import com.knoxhack.echoweathercore.block.RouteWarningPostBlock;
import com.knoxhack.echoweathercore.block.WeatherStationBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WeatherCoreBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EchoWeatherCore.MODID);
    private static final List<DeferredBlock<? extends Block>> BLOCK_ITEMS = new ArrayList<>();

    public static final DeferredBlock<Block> WEATHER_STATION = machine("weather_station", MapColor.COLOR_LIGHT_BLUE);
    public static final DeferredBlock<Block> STORM_BEACON = machine("storm_beacon", MapColor.COLOR_ORANGE);
    public static final DeferredBlock<Block> FARADAY_SHELTER_CORE = machine("faraday_shelter_core", MapColor.COLOR_GRAY);
    public static final DeferredBlock<Block> ATMOSPHERIC_SHIELD_EMITTER = machine("atmospheric_shield_emitter", MapColor.COLOR_CYAN);
    public static final DeferredBlock<Block> ROUTE_WARNING_POST = tracked(BLOCKS.registerBlock("route_warning_post", RouteWarningPostBlock::new,
        p -> p.mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> DEBRIS_RADAR_DISH = machine("debris_radar_dish", MapColor.METAL);
    public static final DeferredBlock<Block> SIGNAL_STABILIZER = machine("signal_stabilizer", MapColor.COLOR_PURPLE);
    public static final DeferredBlock<Block> EMERGENCY_SIREN = tracked(BLOCKS.registerBlock("emergency_siren", EmergencySirenBlock::new,
        p -> p.mapColor(MapColor.COLOR_RED).strength(2.0F, 4.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> CLIMATE_SENSOR = tracked(BLOCKS.registerBlock("climate_sensor", ClimateSensorBlock::new,
        p -> p.mapColor(MapColor.COLOR_GREEN).strength(1.5F, 3.0F).sound(SoundType.METAL)));

    private WeatherCoreBlocks() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    @SuppressWarnings("unchecked")
    public static List<DeferredBlock<Block>> blockItems() {
        return (List<DeferredBlock<Block>>) (List<?>) List.copyOf(BLOCK_ITEMS);
    }

    private static DeferredBlock<Block> machine(String name, MapColor color) {
        return tracked(BLOCKS.registerBlock(name, WeatherStationBlock::new,
            p -> p.mapColor(color).strength(3.0F, 6.0F).sound(SoundType.METAL)));
    }

    static <T extends Block> DeferredBlock<T> tracked(DeferredBlock<T> block) {
        BLOCK_ITEMS.add(block);
        return block;
    }
}
