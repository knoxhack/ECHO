package com.knoxhack.echomultiblockcore.registry;

import com.knoxhack.echomultiblockcore.EchoMultiblockCore;
import com.knoxhack.echomultiblockcore.api.RobotToolType;
import com.knoxhack.echomultiblockcore.item.BlueprintItem;
import com.knoxhack.echomultiblockcore.item.ToolHeadItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EchoMultiblockCore.MODID);
    private static final List<DeferredItem<? extends Item>> CREATIVE_ITEMS = new ArrayList<>();

    public static final DeferredItem<Item> GRIPPER_HEAD = toolHead("gripper_head", RobotToolType.GRIPPER);
    public static final DeferredItem<Item> WELDER_HEAD = toolHead("welder_head", RobotToolType.WELDER);
    public static final DeferredItem<Item> SCANNER_HEAD = toolHead("scanner_head", RobotToolType.SCANNER);
    public static final DeferredItem<Item> ASSEMBLER_HEAD = toolHead("assembler_head", RobotToolType.ASSEMBLER);
    public static final DeferredItem<Item> INJECTOR_HEAD = toolHead("injector_head", RobotToolType.INJECTOR);
    public static final DeferredItem<Item> CUTTER_HEAD = toolHead("cutter_head", RobotToolType.CUTTER);
    public static final DeferredItem<Item> CLAMP_HEAD = toolHead("clamp_head", RobotToolType.CLAMP);
    public static final DeferredItem<Item> DRILL_HEAD = toolHead("drill_head", RobotToolType.DRILL);
    public static final DeferredItem<Item> CALIBRATOR_HEAD = toolHead("calibrator_head", RobotToolType.CALIBRATOR);
    public static final DeferredItem<Item> STABILIZER_HEAD = toolHead("stabilizer_head", RobotToolType.STABILIZER);
    public static final DeferredItem<Item> SIGNAL_CIRCUIT = tracked(ITEMS.registerSimpleItem("signal_circuit"));
    public static final DeferredItem<Item> CALIBRATED_BUS_MODULE = tracked(ITEMS.registerSimpleItem("calibrated_bus_module"));
    public static final DeferredItem<Item> MACHINE_CASING = tracked(ITEMS.registerSimpleItem("machine_casing"));
    public static final DeferredItem<Item> SUPPLY_MANIFEST = tracked(ITEMS.registerSimpleItem("supply_manifest"));
    public static final DeferredItem<Item> SCANNER_MATRIX = tracked(ITEMS.registerSimpleItem("scanner_matrix"));
    public static final DeferredItem<Item> VEHICLE_FRAME_KIT = tracked(ITEMS.registerSimpleItem("vehicle_frame_kit"));
    public static final DeferredItem<Item> LAUNCH_GUIDANCE_CORE = tracked(ITEMS.registerSimpleItem("launch_guidance_core"));
    public static final DeferredItem<Item> ARCHIVE_MEMORY_CELL = tracked(ITEMS.registerSimpleItem("archive_memory_cell"));
    public static final DeferredItem<Item> RECLAMATION_GROWTH_MATRIX = tracked(ITEMS.registerSimpleItem("reclamation_growth_matrix"));
    public static final DeferredItem<Item> NEXUS_FIELD_COIL = tracked(ITEMS.registerSimpleItem("nexus_field_coil"));
    public static final DeferredItem<Item> ARMORY_PATTERN_CORE = tracked(ITEMS.registerSimpleItem("armory_pattern_core"));
    public static final DeferredItem<Item> CONSTRUCTION_PLANNER = tracked(ITEMS.registerSimpleItem("construction_planner"));
    public static final DeferredItem<Item> SPEED_UPGRADE = tracked(ITEMS.registerSimpleItem("speed_upgrade"));
    public static final DeferredItem<Item> REACH_UPGRADE = tracked(ITEMS.registerSimpleItem("reach_upgrade"));
    public static final DeferredItem<Item> COOLING_UPGRADE = tracked(ITEMS.registerSimpleItem("cooling_upgrade"));
    public static final DeferredItem<Item> INTEGRITY_UPGRADE = tracked(ITEMS.registerSimpleItem("integrity_upgrade"));
    public static final DeferredItem<Item> AUTO_BUILDER_CORE = tracked(ITEMS.registerSimpleItem("auto_builder_core"));
    public static final DeferredItem<Item> SIGNAL_TOWER_BLUEPRINT = tracked(ITEMS.registerItem(
            "signal_tower_blueprint",
            properties -> new BlueprintItem(EchoMultiblockCore.id("signal_tower_tier_1"), properties),
            p -> p.stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> INDUSTRIAL_ASSEMBLY_LINE_BLUEPRINT = tracked(ITEMS.registerItem(
            "industrial_assembly_line_blueprint",
            properties -> new BlueprintItem(EchoMultiblockCore.id("industrial_assembly_line"), properties),
            p -> p.stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> LOGISTICS_DEPOT_BLUEPRINT = blueprint("logistics_depot_blueprint", "logistics_depot");
    public static final DeferredItem<Item> SCANNER_ARRAY_BLUEPRINT = blueprint("scanner_array_blueprint", "scanner_array");
    public static final DeferredItem<Item> VEHICLE_REPAIR_GANTRY_BLUEPRINT = blueprint("vehicle_repair_gantry_blueprint", "vehicle_repair_gantry");
    public static final DeferredItem<Item> ORBITAL_LAUNCH_PLATFORM_BLUEPRINT = blueprint("orbital_launch_platform_blueprint", "orbital_launch_platform");
    public static final DeferredItem<Item> ARCHIVE_DATA_CHAMBER_BLUEPRINT = blueprint("archive_data_chamber_blueprint", "archive_data_chamber");
    public static final DeferredItem<Item> AGRICULTURE_DOME_BLUEPRINT = blueprint("agriculture_dome_blueprint", "agriculture_dome");
    public static final DeferredItem<Item> NEXUS_STABILIZER_BLUEPRINT = blueprint("nexus_stabilizer_blueprint", "nexus_stabilizer");
    public static final DeferredItem<Item> ARMORY_FABRICATOR_BLUEPRINT = blueprint("armory_fabricator_blueprint", "armory_fabricator");
    public static final DeferredItem<Item> AUTO_BUILDER_YARD_BLUEPRINT = blueprint("auto_builder_yard_blueprint", "auto_builder_yard");

    static {
        ModBlocks.ALL_BLOCKS.forEach(block -> tracked(ITEMS.registerSimpleBlockItem(block)));
    }

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static List<DeferredItem<? extends Item>> creativeItems() {
        return List.copyOf(CREATIVE_ITEMS);
    }

    private static DeferredItem<Item> toolHead(String name, RobotToolType toolType) {
        return tracked(ITEMS.registerItem(name, properties -> new ToolHeadItem(toolType, properties), p -> p.stacksTo(1)));
    }

    private static DeferredItem<Item> blueprint(String itemName, String definitionPath) {
        return tracked(ITEMS.registerItem(
                itemName,
                properties -> new BlueprintItem(EchoMultiblockCore.id(definitionPath), properties),
                p -> p.stacksTo(1).rarity(Rarity.UNCOMMON)));
    }

    private static <T extends Item> DeferredItem<T> tracked(DeferredItem<T> item) {
        CREATIVE_ITEMS.add(item);
        return item;
    }
}
