package com.knoxhack.echorelictech.registry;

import com.knoxhack.echorelictech.EchoRelicTech;
import com.knoxhack.echorelictech.item.*;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EchoRelicTech.MODID);
    private static final List<DeferredItem<? extends Item>> CREATIVE_ITEMS = new ArrayList<>();

    // MVP Relics
    public static final DeferredItem<Item> PHASE_ANCHOR = tracked(ITEMS.registerItem("phase_anchor", p -> new PhaseAnchorItem(p.stacksTo(1).durability(256))));
    public static final DeferredItem<Item> NULL_BATTERY = tracked(ITEMS.registerItem("null_battery", p -> new NullBatteryItem(p.stacksTo(1))));
    public static final DeferredItem<Item> GUARDIAN_LENS = tracked(ITEMS.registerItem("guardian_lens", p -> new GuardianLensItem(p.stacksTo(1).durability(128))));
    public static final DeferredItem<Item> ECHO_MIRROR = tracked(ITEMS.registerItem("echo_mirror", p -> new EchoMirrorItem(p.stacksTo(1).durability(128))));
    public static final DeferredItem<Item> MATTER_STITCHER = tracked(ITEMS.registerItem("matter_stitcher", p -> new MatterStitcherItem(p.stacksTo(1).durability(256))));

    // Materials
    public static final DeferredItem<Item> UNIDENTIFIED_RELIC = tracked(ITEMS.registerItem("unidentified_relic", p -> new Item(p)));
    public static final DeferredItem<Item> RELIC_SHARD = tracked(ITEMS.registerItem("relic_shard", p -> new Item(p)));
    public static final DeferredItem<Item> DAMAGED_AI_CORE = tracked(ITEMS.registerItem("damaged_ai_core", p -> new Item(p)));
    public static final DeferredItem<Item> PRE_GRIDFALL_CIRCUIT = tracked(ITEMS.registerItem("pre_gridfall_circuit", p -> new Item(p)));
    public static final DeferredItem<Item> QUANTUM_LATTICE = tracked(ITEMS.registerItem("quantum_lattice", p -> new Item(p)));
    public static final DeferredItem<Item> NULL_CELL = tracked(ITEMS.registerItem("null_cell", p -> new Item(p)));
    public static final DeferredItem<Item> STABILIZED_RIFTSTONE = tracked(ITEMS.registerItem("stabilized_riftstone", p -> new Item(p)));
    public static final DeferredItem<Item> GUARDIAN_ALLOY = tracked(ITEMS.registerItem("guardian_alloy", p -> new Item(p)));
    public static final DeferredItem<Item> MEMORY_FILAMENT = tracked(ITEMS.registerItem("memory_filament", p -> new Item(p)));
    public static final DeferredItem<Item> CONTAINMENT_GLASS = tracked(ITEMS.registerItem("containment_glass", p -> new Item(p)));
    public static final DeferredItem<Item> OLD_WORLD_ACTUATOR = tracked(ITEMS.registerItem("old_world_actuator", p -> new Item(p)));
    public static final DeferredItem<Item> BROKEN_CLIMATE_KEY_FRAGMENT = tracked(ITEMS.registerItem("broken_climate_key_fragment", p -> new Item(p)));
    public static final DeferredItem<Item> NEXUS_STAINED_CAPACITOR = tracked(ITEMS.registerItem("nexus_stained_capacitor", p -> new Item(p)));
    public static final DeferredItem<Item> RELIC_DIAGNOSTIC_REPORT = tracked(ITEMS.registerItem("relic_diagnostic_report", p -> new Item(p)));
    public static final DeferredItem<Item> FORBIDDEN_PROTOTYPE_FILE = tracked(ITEMS.registerItem("forbidden_prototype_file", p -> new Item(p)));
    public static final DeferredItem<Item> OLD_WORLD_PATENT_FRAGMENT = tracked(ITEMS.registerItem("old_world_patent_fragment", p -> new Item(p)));
    public static final DeferredItem<Item> ECHO_RECOVERY_LOG = tracked(ITEMS.registerItem("echo_recovery_log", p -> new Item(p)));
    public static final DeferredItem<Item> NEXUS_WARNING_PACKET = tracked(ITEMS.registerItem("nexus_warning_packet", p -> new Item(p)));
    public static final DeferredItem<Item> BLACKBOX_RELIC_RECORD = tracked(ITEMS.registerItem("blackbox_relic_record", p -> new Item(p)));

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
