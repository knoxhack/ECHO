package com.knoxhack.echoweathercore.registry;

import com.knoxhack.echoweathercore.EchoWeatherCore;
import com.knoxhack.echoweathercore.item.StormScannerItem;
import com.knoxhack.echoweathercore.item.WeatherRadioItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WeatherCoreItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EchoWeatherCore.MODID);
    private static final List<DeferredItem<? extends Item>> CREATIVE_ITEMS = new ArrayList<>();

    public static final DeferredItem<Item> STORM_SCANNER = tracked(ITEMS.registerItem("storm_scanner", StormScannerItem::new, p -> p.stacksTo(1)));
    public static final DeferredItem<Item> WEATHER_RADIO = tracked(ITEMS.registerItem("weather_radio", WeatherRadioItem::new, p -> p.stacksTo(1)));
    public static final DeferredItem<Item> PORTABLE_SHELTER_BEACON = simple("portable_shelter_beacon", p -> p.stacksTo(1));
    public static final DeferredItem<Item> ASH_FILTER_WRAP = simple("ash_filter_wrap", p -> p.stacksTo(16));
    public static final DeferredItem<Item> FARADAY_COIL = simple("faraday_coil", p -> p.stacksTo(16));
    public static final DeferredItem<Item> SIGNAL_ANCHOR = simple("signal_anchor", p -> p.stacksTo(16));
    public static final DeferredItem<Item> CRYO_HEAT_CELL = simple("cryo_heat_cell", p -> p.stacksTo(16));
    public static final DeferredItem<Item> TOXIC_RAIN_COLLECTOR = simple("toxic_rain_collector", p -> p.stacksTo(1));
    public static final DeferredItem<Item> DEBRIS_TRACKER = simple("debris_tracker", p -> p.stacksTo(1));
    public static final DeferredItem<Item> ROUTE_FLARE = simple("route_flare", p -> p.stacksTo(16));

    // Weather resources
    public static final DeferredItem<Item> FINE_ASH = simple("fine_ash");
    public static final DeferredItem<Item> ASH_GLASS_DUST = simple("ash_glass_dust");
    public static final DeferredItem<Item> STORM_SIFTED_SCRAP = simple("storm_sifted_scrap");
    public static final DeferredItem<Item> CONDENSED_TOXIN = simple("condensed_toxin");
    public static final DeferredItem<Item> ACIDIC_SLUDGE = simple("acidic_sludge");
    public static final DeferredItem<Item> TOXIC_RAINWATER = simple("toxic_rainwater");
    public static final DeferredItem<Item> CHARGED_URANIUM_DUST = simple("charged_uranium_dust");
    public static final DeferredItem<Item> IRRADIATED_CRYSTAL_DUST = simple("irradiated_crystal_dust");
    public static final DeferredItem<Item> REACTOR_TRACE_PARTICLES = simple("reactor_trace_particles");
    public static final DeferredItem<Item> CRYO_FROST = simple("cryo_frost");
    public static final DeferredItem<Item> FROZEN_CONDUIT_SHARD = simple("frozen_conduit_shard");
    public static final DeferredItem<Item> CONDENSED_ICE_FILM = simple("condensed_ice_film");
    public static final DeferredItem<Item> THERMAL_RESIDUE = simple("thermal_residue");
    public static final DeferredItem<Item> BAKED_ASH_GLASS = simple("baked_ash_glass");
    public static final DeferredItem<Item> DRY_REACTOR_SALT = simple("dry_reactor_salt");
    public static final DeferredItem<Item> STATIC_FILAMENT = simple("static_filament");
    public static final DeferredItem<Item> MEMORY_RESIDUE = simple("memory_residue");
    public static final DeferredItem<Item> NEXUS_TRACE = simple("nexus_trace");
    public static final DeferredItem<Item> ECHO_CRYSTAL_CHARGE = simple("echo_crystal_charge");
    public static final DeferredItem<Item> ORBITAL_ALLOY_SCRAP = simple("orbital_alloy_scrap");
    public static final DeferredItem<Item> BURNED_CIRCUITRY = simple("burned_circuitry");
    public static final DeferredItem<Item> SATELLITE_LENS = simple("satellite_lens");
    public static final DeferredItem<Item> ECHO0_SIGNAL_SHARD = simple("echo0_signal_shard");
    public static final DeferredItem<Item> MAGNETIZED_SCRAP = simple("magnetized_scrap");
    public static final DeferredItem<Item> BURNED_RELAY_COIL = simple("burned_relay_coil");
    public static final DeferredItem<Item> STATIC_GLASS = simple("static_glass");
    public static final DeferredItem<Item> OVERLOADED_CAPACITOR = simple("overloaded_capacitor");

    static {
        WeatherCoreBlocks.blockItems().forEach(block -> tracked(ITEMS.registerSimpleBlockItem(block)));
    }

    private WeatherCoreItems() {}

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static List<DeferredItem<? extends Item>> creativeItems() {
        return List.copyOf(CREATIVE_ITEMS);
    }

    private static DeferredItem<Item> simple(String name) {
        return tracked(ITEMS.registerItem(name, Item::new, p -> p));
    }

    private static DeferredItem<Item> simple(String name, java.util.function.UnaryOperator<Item.Properties> props) {
        return tracked(ITEMS.registerItem(name, Item::new, props));
    }

    private static <T extends Item> DeferredItem<T> tracked(DeferredItem<T> item) {
        CREATIVE_ITEMS.add(item);
        return item;
    }
}
