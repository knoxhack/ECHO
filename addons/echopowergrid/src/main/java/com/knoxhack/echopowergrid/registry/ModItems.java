package com.knoxhack.echopowergrid.registry;

import com.knoxhack.echopowergrid.EchoPowerGrid;
import com.knoxhack.echopowergrid.item.GridDiagnosticToolItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EchoPowerGrid.MODID);
    private static final List<DeferredItem<? extends Item>> CREATIVE_ITEMS = new ArrayList<>();

    public static final DeferredItem<Item> COPPER_COIL = tracked(ITEMS.registerItem("copper_coil", p -> new Item(p)));
    public static final DeferredItem<Item> SCRAP_WIRE = tracked(ITEMS.registerItem("scrap_wire", p -> new Item(p)));
    public static final DeferredItem<Item> INSULATED_WIRE = tracked(ITEMS.registerItem("insulated_wire", p -> new Item(p)));
    public static final DeferredItem<Item> POWER_CELL = tracked(ITEMS.registerItem("power_cell", p -> new Item(p)));
    public static final DeferredItem<Item> BATTERY_CORE = tracked(ITEMS.registerItem("battery_core", p -> new Item(p)));
    public static final DeferredItem<Item> FUSE = tracked(ITEMS.registerItem("fuse", p -> new Item(p)));
    public static final DeferredItem<Item> BREAKER_SWITCH = tracked(ITEMS.registerItem("breaker_switch", p -> new Item(p)));
    public static final DeferredItem<Item> GRID_DIAGNOSTIC_TOOL = tracked(ITEMS.registerItem("grid_diagnostic_tool", p -> new GridDiagnosticToolItem(p)));

    static {
        ModBlocks.blockItems().forEach(block -> tracked(ITEMS.registerSimpleBlockItem(block)));
    }

    private ModItems() {}

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static List<DeferredItem<? extends Item>> creativeItems() {
        return List.copyOf(CREATIVE_ITEMS);
    }

    private static <T extends Item> DeferredItem<T> tracked(DeferredItem<T> item) {
        CREATIVE_ITEMS.add(item);
        return item;
    }
}
