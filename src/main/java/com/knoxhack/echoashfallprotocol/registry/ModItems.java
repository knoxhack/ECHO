package com.knoxhack.echoashfallprotocol.registry;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.entity.ModEntities;
import com.knoxhack.echoashfallprotocol.item.*;
import com.knoxhack.echoashfallprotocol.item.FilterCartridgeItem;
import com.knoxhack.echoashfallprotocol.item.SchematicFragmentItem;
import com.knoxhack.echoashfallprotocol.item.ScrapKnifeItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EchoAshfallProtocol.MODID);

    // === SCRAP MATERIALS ===
    public static final DeferredItem<Item> SCRAP_METAL = ITEMS.registerSimpleItem("scrap_metal");
    public static final DeferredItem<Item> SCRAP_WIRE = ITEMS.registerSimpleItem("scrap_wire");
    public static final DeferredItem<Item> SCRAP_CIRCUIT = ITEMS.registerSimpleItem("scrap_circuit");
    public static final DeferredItem<Item> SCRAP_PLASTIC = ITEMS.registerSimpleItem("scrap_plastic");

    // === CRAFTING COMPONENTS ===
    public static final DeferredItem<Item> ASH = ITEMS.registerSimpleItem("ash");
    public static final DeferredItem<Item> CIRCUIT_BOARD = ITEMS.registerSimpleItem("circuit_board");
    public static final DeferredItem<Item> ENERGY_CELL = ITEMS.registerSimpleItem("energy_cell");
    public static final DeferredItem<Item> BASIC_BATTERY = register("basic_battery",
            props -> new BatteryItem(props, BatteryItem.Tier.BASIC), new Item.Properties().stacksTo(1),
            AshfallTooltip.of("battery_basic"));
    public static final DeferredItem<Item> ADVANCED_BATTERY = register("advanced_battery",
            props -> new BatteryItem(props, BatteryItem.Tier.ADVANCED), new Item.Properties().stacksTo(1),
            AshfallTooltip.of("battery_advanced"));
    public static final DeferredItem<Item> ELITE_BATTERY = register("elite_battery",
            props -> new BatteryItem(props, BatteryItem.Tier.ELITE), new Item.Properties().stacksTo(1),
            AshfallTooltip.of("battery_elite"));
    public static final DeferredItem<Item> FILTRATION_MEMBRANE = ITEMS.registerSimpleItem("filtration_membrane");
    public static final DeferredItem<Item> MACHINE_CASING = ITEMS.registerSimpleItem("machine_casing");
    public static final DeferredItem<Item> MUTATED_TISSUE = ITEMS.registerSimpleItem("mutated_tissue");

    // === SURVIVAL ITEMS ===
    public static final DeferredItem<Item> DIRTY_WATER_BOTTLE = register("dirty_water_bottle",
            DirtyWaterItem::new, new Item.Properties().stacksTo(16), AshfallTooltip.of("dirty_water"));
    public static final DeferredItem<Item> CLEAN_WATER_BOTTLE = register("clean_water_bottle",
            CleanWaterItem::new, new Item.Properties().stacksTo(16)
                    .food(new FoodProperties.Builder().nutrition(0).saturationModifier(0.1f).build()),
            AshfallTooltip.of("clean_water"));

    // === MEDICAL & MUTATION ===
    public static final DeferredItem<Item> MUTAGEN_VIAL = register("mutagen_vial",
            MutagenItem::new, new Item.Properties().stacksTo(16), AshfallTooltip.of("mutagen"));
    public static final DeferredItem<Item> RAD_AWAY = register("rad_away",
            RadAwayItem::new, new Item.Properties().stacksTo(16), AshfallTooltip.of("rad_away"));

    // === FILTER CARTRIDGES ===
    public static final DeferredItem<Item> FILTER_CARTRIDGE_BASIC = register("filter_cartridge_basic",
            props -> new FilterCartridgeItem(props, FilterCartridgeItem.Tier.BASIC), new Item.Properties().durability(200),
            AshfallTooltip.of("filter_basic"));
    public static final DeferredItem<Item> FILTER_CARTRIDGE_ADVANCED = register("filter_cartridge_advanced",
            props -> new FilterCartridgeItem(props, FilterCartridgeItem.Tier.ADVANCED), new Item.Properties().durability(600),
            AshfallTooltip.of("filter_advanced"));
    public static final DeferredItem<Item> FILTER_CARTRIDGE_ELITE = register("filter_cartridge_elite",
            props -> new FilterCartridgeItem(props, FilterCartridgeItem.Tier.ELITE), new Item.Properties().durability(1500),
            AshfallTooltip.of("filter_elite"));

    // === SCHEMATIC FRAGMENTS (Exploration 1.1) ===
    public static final DeferredItem<Item> SCHEMATIC_FRAGMENT_WEAPONS = register("schematic_fragment_weapons",
            props -> new SchematicFragmentItem(props, SchematicFragmentItem.SchematicType.WEAPONS), new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE),
            AshfallTooltip.of("schematic:weapons"));
    public static final DeferredItem<Item> SCHEMATIC_FRAGMENT_ARMOR = register("schematic_fragment_armor",
            props -> new SchematicFragmentItem(props, SchematicFragmentItem.SchematicType.ARMOR), new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE),
            AshfallTooltip.of("schematic:armor"));
    public static final DeferredItem<Item> SCHEMATIC_FRAGMENT_MACHINES = register("schematic_fragment_machines",
            props -> new SchematicFragmentItem(props, SchematicFragmentItem.SchematicType.MACHINES), new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE),
            AshfallTooltip.of("schematic:machines"));
    public static final DeferredItem<Item> SCHEMATIC_FRAGMENT_MEDICAL = register("schematic_fragment_medical",
            props -> new SchematicFragmentItem(props, SchematicFragmentItem.SchematicType.MEDICAL), new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE),
            AshfallTooltip.of("schematic:medical"));
    public static final DeferredItem<Item> SCHEMATIC_FRAGMENT_ENERGY = register("schematic_fragment_energy",
            props -> new SchematicFragmentItem(props, SchematicFragmentItem.SchematicType.ENERGY), new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE),
            AshfallTooltip.of("schematic:energy"));

    // === EXPLORATION TOOLS (Exploration 1.1) ===
    public static final DeferredItem<Item> PORTABLE_SIGNAL_SCANNER = register("portable_signal_scanner",
            SignalScannerItem::new, new Item.Properties().stacksTo(1).durability(100));

    // === TOOLS ===
    public static final DeferredItem<Item> SCRAP_KNIFE = register("scrap_knife",
            ScrapKnifeItem::new, new Item.Properties().durability(80), AshfallTooltip.of("scrap_knife"));

    // === ARMOR (GAS MASK) ===
    public static final DeferredItem<Item> GAS_MASK = ITEMS.register("gas_mask", id ->
            new GasMaskItem(withId(withTooltip(new Item.Properties().durability(500)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build()),
                    AshfallTooltip.of("gas_mask")), id)));

    // === ARMOR (HAZMAT SUIT) ===
    public static final DeferredItem<Item> HAZMAT_HELMET = ITEMS.register("hazmat_helmet", id ->
            new HazmatArmorItem(withId(withTooltip(new Item.Properties().durability(300)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build()),
                    AshfallTooltip.of("hazmat_head")), id), EquipmentSlot.HEAD));
    public static final DeferredItem<Item> HAZMAT_CHESTPLATE = ITEMS.register("hazmat_chestplate", id ->
            new HazmatArmorItem(withId(withTooltip(new Item.Properties().durability(450)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST).build()),
                    AshfallTooltip.of("hazmat_chest")), id), EquipmentSlot.CHEST));
    public static final DeferredItem<Item> HAZMAT_LEGGINGS = ITEMS.register("hazmat_leggings", id ->
            new HazmatArmorItem(withId(withTooltip(new Item.Properties().durability(400)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.LEGS).build()),
                    AshfallTooltip.of("hazmat_legs")), id), EquipmentSlot.LEGS));
    public static final DeferredItem<Item> HAZMAT_BOOTS = ITEMS.register("hazmat_boots", id ->
            new HazmatArmorItem(withId(withTooltip(new Item.Properties().durability(350)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.FEET).build()),
                    AshfallTooltip.of("hazmat_feet")), id), EquipmentSlot.FEET));

    // === MOB SPAWN EGGS ===
    public static final DeferredItem<Item> RAD_ZOMBIE_SPAWN_EGG = registerSpawnEgg("rad_zombie_spawn_egg", ModEntities.RAD_ZOMBIE::get);
    public static final DeferredItem<Item> SCAVENGER_BANDIT_SPAWN_EGG = registerSpawnEgg("scavenger_bandit_spawn_egg", ModEntities.SCAVENGER_BANDIT::get);
    public static final DeferredItem<Item> IRRADIATED_WOLF_SPAWN_EGG = registerSpawnEgg("irradiated_wolf_spawn_egg", ModEntities.IRRADIATED_WOLF::get);
    public static final DeferredItem<Item> ECHO_DRONE_SPAWN_EGG = registerSpawnEgg("echo_drone_spawn_egg", ModEntities.ECHO_DRONE::get);
    public static final DeferredItem<Item> SCOUT_DRONE_SPAWN_EGG = registerSpawnEgg("scout_drone_spawn_egg", ModEntities.SCOUT_DRONE::get);
    public static final DeferredItem<Item> GLOWING_GHOUL_SPAWN_EGG = registerSpawnEgg("glowing_ghoul_spawn_egg", ModEntities.GLOWING_GHOUL::get);
    public static final DeferredItem<Item> ASH_WRAITH_SPAWN_EGG = registerSpawnEgg("ash_wraith_spawn_egg", ModEntities.ASH_WRAITH::get);
    public static final DeferredItem<Item> TOXIC_SLIME_SPAWN_EGG = registerSpawnEgg("toxic_slime_spawn_egg", ModEntities.TOXIC_SLIME::get);
    public static final DeferredItem<Item> CITY_STALKER_SPAWN_EGG = registerSpawnEgg("city_stalker_spawn_egg", ModEntities.CITY_STALKER::get);
    public static final DeferredItem<Item> RUST_WALKER_SPAWN_EGG = registerSpawnEgg("rust_walker_spawn_egg", ModEntities.RUST_WALKER::get);
    public static final DeferredItem<Item> STEAM_WRAITH_SPAWN_EGG = registerSpawnEgg("steam_wraith_spawn_egg", ModEntities.STEAM_WRAITH::get);
    public static final DeferredItem<Item> MUTATED_CRAWLER_SPAWN_EGG = registerSpawnEgg("mutated_crawler_spawn_egg", ModEntities.MUTATED_CRAWLER::get);
    public static final DeferredItem<Item> ECHO_COMPANION_DRONE_SPAWN_EGG = registerSpawnEgg("echo_companion_drone_spawn_egg", ModEntities.ECHO_COMPANION_DRONE::get);
    public static final DeferredItem<Item> REMNANT_SOLDIER_SPAWN_EGG = registerSpawnEgg("remnant_soldier_spawn_egg", ModEntities.REMNANT_SOLDIER::get);
    public static final DeferredItem<Item> SALVAGER_TRADER_SPAWN_EGG = registerSpawnEgg("salvager_trader_spawn_egg", ModEntities.SALVAGER_TRADER::get);
    public static final DeferredItem<Item> MUTANT_CREATURE_SPAWN_EGG = registerSpawnEgg("mutant_creature_spawn_egg", ModEntities.MUTANT_CREATURE::get);
    public static final DeferredItem<Item> GRIDBOUND_HUSK_SPAWN_EGG = registerSpawnEgg("gridbound_husk_spawn_egg", ModEntities.GRIDBOUND_HUSK::get);
    public static final DeferredItem<Item> RELAY_WARDEN_SPAWN_EGG = registerSpawnEgg("relay_warden_spawn_egg", ModEntities.RELAY_WARDEN::get);
    public static final DeferredItem<Item> SIGNAL_LEECH_SPAWN_EGG = registerSpawnEgg("signal_leech_spawn_egg", ModEntities.SIGNAL_LEECH::get);
    public static final DeferredItem<Item> NEXUS_NULLIFIER_SPAWN_EGG = registerSpawnEgg("nexus_nullifier_spawn_egg", ModEntities.NEXUS_NULLIFIER::get);
    
    // Boss Spawn Eggs
    public static final DeferredItem<Item> WARDEN_BOSS_SPAWN_EGG = registerSpawnEgg("warden_boss_spawn_egg", ModEntities.WARDEN_BOSS::get);
    public static final DeferredItem<Item> WASTELAND_SENTINEL_SPAWN_EGG = registerSpawnEgg("wasteland_sentinel_spawn_egg", ModEntities.WASTELAND_SENTINEL::get);
    public static final DeferredItem<Item> CRASH_ZONE_COLOSSUS_SPAWN_EGG = registerSpawnEgg("crash_zone_colossus_spawn_egg", ModEntities.CRASH_ZONE_COLOSSUS::get);
    public static final DeferredItem<Item> CRYOGENIC_OVERSEER_SPAWN_EGG = registerSpawnEgg("cryogenic_overseer_spawn_egg", ModEntities.CRYOGENIC_OVERSEER::get);
    public static final DeferredItem<Item> INDUSTRIAL_JUGGERNAUT_SPAWN_EGG = registerSpawnEgg("industrial_juggernaut_spawn_egg", ModEntities.INDUSTRIAL_JUGGERNAUT::get);
    public static final DeferredItem<Item> NEXUS_SCAR_AVATAR_SPAWN_EGG = registerSpawnEgg("nexus_scar_avatar_spawn_egg", ModEntities.NEXUS_SCAR_AVATAR::get);
    public static final DeferredItem<Item> RADIATION_BEHEMOTH_SPAWN_EGG = registerSpawnEgg("radiation_behemoth_spawn_egg", ModEntities.RADIATION_BEHEMOTH::get);
    public static final DeferredItem<Item> CITY_RUIN_STALKER_SPAWN_EGG = registerSpawnEgg("city_ruin_stalker_spawn_egg", ModEntities.CITY_RUIN_STALKER::get);
    public static final DeferredItem<Item> PLAINS_WARLORD_SPAWN_EGG = registerSpawnEgg("plains_warlord_spawn_egg", ModEntities.PLAINS_WARLORD::get);
    public static final DeferredItem<Item> TOXIC_HIVE_MATRIARCH_SPAWN_EGG = registerSpawnEgg("toxic_hive_matriarch_spawn_egg", ModEntities.TOXIC_HIVE_MATRIARCH::get);
    public static final DeferredItem<Item> CORRUPTION_BLOOM_SPAWN_EGG = registerSpawnEgg("corruption_bloom_spawn_egg", ModEntities.CORRUPTION_BLOOM::get);
    public static final DeferredItem<Item> SEVERANCE_ENGINE_SPAWN_EGG = registerSpawnEgg("severance_engine_spawn_egg", ModEntities.SEVERANCE_ENGINE::get);
    public static final DeferredItem<Item> MIRROR_COMMAND_SPAWN_EGG = registerSpawnEgg("mirror_command_spawn_egg", ModEntities.MIRROR_COMMAND::get);

    // === TRACE FRAGMENTS (Tier 1 extraction inputs) ===
    public static final DeferredItem<Item> IRON_SHARD = ITEMS.registerSimpleItem("iron_shard");
    public static final DeferredItem<Item> COPPER_SHARD = ITEMS.registerSimpleItem("copper_shard");
    public static final DeferredItem<Item> COAL_DUST = ITEMS.registerSimpleItem("coal_dust");
    public static final DeferredItem<Item> GOLD_TRACE = ITEMS.registerSimpleItem("gold_trace");
    public static final DeferredItem<Item> CRYSTAL_DUST = ITEMS.registerSimpleItem("crystal_dust");
    public static final DeferredItem<Item> GEM_FRAGMENT = ITEMS.registerSimpleItem("gem_fragment");
    public static final DeferredItem<Item> DENSE_ALLOY_CHUNK = ITEMS.registerSimpleItem("dense_alloy_chunk");
    public static final DeferredItem<Item> URANIUM_SHARD = ITEMS.registerSimpleItem("uranium_shard");

    // === RESOURCE PROCESSING BUNDLES ===
    public static final DeferredItem<Item> GOLD_CLUSTER = ITEMS.registerSimpleItem("gold_cluster");
    public static final DeferredItem<Item> SCRAP_IRON_BUNDLE = ITEMS.registerSimpleItem("scrap_iron_bundle");

    // === CONTAMINATED RESOURCES (Isotope Refiner byproducts) ===
    public static final DeferredItem<Item> CONTAMINATED_IRON = register("contaminated_iron",
            com.knoxhack.echoashfallprotocol.item.ContaminatedItem::new, new Item.Properties().stacksTo(16),
            AshfallTooltip.of("contaminated"));
    public static final DeferredItem<Item> CONTAMINATED_GOLD = register("contaminated_gold",
            com.knoxhack.echoashfallprotocol.item.ContaminatedItem::new, new Item.Properties().stacksTo(16),
            AshfallTooltip.of("contaminated"));
    public static final DeferredItem<Item> CONTAMINATED_REDSTONE = register("contaminated_redstone",
            com.knoxhack.echoashfallprotocol.item.ContaminatedItem::new, new Item.Properties().stacksTo(16),
            AshfallTooltip.of("contaminated"));
    public static final DeferredItem<Item> CONTAMINATED_LAPIS = register("contaminated_lapis",
            com.knoxhack.echoashfallprotocol.item.ContaminatedItem::new, new Item.Properties().stacksTo(16),
            AshfallTooltip.of("contaminated"));

    // === MACHINE UPGRADE ===
    public static final DeferredItem<Item> MACHINE_UPGRADE_SPEED = ITEMS.registerSimpleItem("machine_upgrade_speed");
    public static final DeferredItem<Item> MACHINE_UPGRADE_EFFICIENCY = ITEMS.registerSimpleItem("machine_upgrade_efficiency");
    public static final DeferredItem<Item> MACHINE_UPGRADE_OVERCLOCK = ITEMS.registerSimpleItem("machine_upgrade_overclock");

    // === TIER 2 WEAPONS (Alloy) ===
    public static final DeferredItem<Item> ALLOY_BLADE = register("alloy_blade",
            AlloyBladeItem::new, new Item.Properties().durability(350), AshfallTooltip.of("alloy_blade"));
    public static final DeferredItem<Item> ALLOY_HAMMER = register("alloy_hammer",
            AlloyHammerItem::new, new Item.Properties().durability(280), AshfallTooltip.of("alloy_hammer"));

    // === TIER 3 WEAPONS (Nexus-Forged) ===
    public static final DeferredItem<Item> NEXUS_BLADE = register("nexus_blade",
            NexusBladeItem::new, new Item.Properties().durability(1500), AshfallTooltip.of("nexus_blade"));

    // === TIER 4 WEAPON (Destruction Path Reward) ===
    public static final DeferredItem<Item> NEXUS_ANNIHILATOR = register("nexus_annihilator",
            NexusAnnihilatorItem::new, new Item.Properties().durability(3000), AshfallTooltip.of("nexus_annihilator"));

    // === ENDGAME CRAFTING MATERIAL ===
    public static final DeferredItem<Item> NEXUS_CRYSTAL = register("nexus_crystal",
            NexusCrystalItem::new, new Item.Properties().stacksTo(16), AshfallTooltip.of("nexus_crystal"));
    public static final DeferredItem<Item> GUARDIAN_DATACORE = register("guardian_datacore",
            props -> new Item(props), new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE));
    public static final DeferredItem<Item> WARDEN_ARCHIVE_CIPHER = register("warden_archive_cipher",
            props -> new Item(props), new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC).fireResistant());

    // === ENDGAME DIMENSION ACCESS ===
    public static final DeferredItem<Item> PREFALL_ARCHIVES_KEY = register("prefall_archives_key",
            com.knoxhack.echoashfallprotocol.item.PrefallArchivesKeyItem::new, new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC),
            AshfallTooltip.of("prefall_archives_key"));
    
    public static final DeferredItem<Item> RETURN_KEYSTONE = register("return_keystone",
            com.knoxhack.echoashfallprotocol.item.ReturnKeystoneItem::new, new Item.Properties().stacksTo(16),
            AshfallTooltip.of("return_keystone"));
    public static final DeferredItem<Item> INSTABILITY_DAMPENER = register("instability_dampener",
            InstabilityDampenerItem::new, new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE),
            AshfallTooltip.of("instability_dampener"));
    public static final DeferredItem<Item> RELAY_SCANNER_LENS = register("relay_scanner_lens",
            RelayScannerLensItem::new, new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE),
            AshfallTooltip.of("relay_scanner_lens"));
    public static final DeferredItem<Item> RETURN_BEACON = register("return_beacon",
            ReturnBeaconItem::new, new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC),
            AshfallTooltip.of("return_beacon"));

    // === ALLOY ARMOR (Tier 2) ===
    public static final DeferredItem<Item> ALLOY_HELMET = ITEMS.register("alloy_helmet", id ->
            new com.knoxhack.echoashfallprotocol.item.ModArmorItem(withId(withTooltip(new Item.Properties().stacksTo(1).durability(480)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ModArmorMaterials.alloyHelmet()),
                    armorTooltip(4.0, 1.5, 0.0, "Pre-war alloy construction.")), id), 4.0, 1.5, 0.0, "Pre-war alloy construction."));
    public static final DeferredItem<Item> ALLOY_CHESTPLATE = ITEMS.register("alloy_chestplate", id ->
            new com.knoxhack.echoashfallprotocol.item.ModArmorItem(withId(withTooltip(new Item.Properties().stacksTo(1).durability(720)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST).build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ModArmorMaterials.alloyChestplate()),
                    armorTooltip(7.0, 1.5, 0.0, "Pre-war alloy construction.")), id), 7.0, 1.5, 0.0, "Pre-war alloy construction."));
    public static final DeferredItem<Item> ALLOY_LEGGINGS = ITEMS.register("alloy_leggings", id ->
            new com.knoxhack.echoashfallprotocol.item.ModArmorItem(withId(withTooltip(new Item.Properties().stacksTo(1).durability(672)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.LEGS).build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ModArmorMaterials.alloyLeggings()),
                    armorTooltip(5.0, 1.5, 0.0, "Pre-war alloy construction.")), id), 5.0, 1.5, 0.0, "Pre-war alloy construction."));
    public static final DeferredItem<Item> ALLOY_BOOTS = ITEMS.register("alloy_boots", id ->
            new com.knoxhack.echoashfallprotocol.item.ModArmorItem(withId(withTooltip(new Item.Properties().stacksTo(1).durability(408)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.FEET).build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ModArmorMaterials.alloyBoots()),
                    armorTooltip(2.0, 1.5, 0.0, "Pre-war alloy construction.")), id), 2.0, 1.5, 0.0, "Pre-war alloy construction."));

    // === NEXUS ARMOR (Tier 3) ===
    public static final DeferredItem<Item> NEXUS_HELMET = ITEMS.register("nexus_helmet", id ->
            new com.knoxhack.echoashfallprotocol.item.ModArmorItem(withId(withTooltip(new Item.Properties().stacksTo(1).durability(1200)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ModArmorMaterials.nexusHelmet()),
                    armorTooltip(5.0, 3.0, 0.05, "Forged with Nexus Crystal energy.")), id), 5.0, 3.0, 0.05, "Forged with Nexus Crystal energy."));
    public static final DeferredItem<Item> NEXUS_CHESTPLATE = ITEMS.register("nexus_chestplate", id ->
            new com.knoxhack.echoashfallprotocol.item.ModArmorItem(withId(withTooltip(new Item.Properties().stacksTo(1).durability(1800)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST).build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ModArmorMaterials.nexusChestplate()),
                    armorTooltip(8.0, 3.0, 0.10, "Forged with Nexus Crystal energy.")), id), 8.0, 3.0, 0.10, "Forged with Nexus Crystal energy."));
    public static final DeferredItem<Item> NEXUS_LEGGINGS = ITEMS.register("nexus_leggings", id ->
            new com.knoxhack.echoashfallprotocol.item.ModArmorItem(withId(withTooltip(new Item.Properties().stacksTo(1).durability(1680)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.LEGS).build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ModArmorMaterials.nexusLeggings()),
                    armorTooltip(6.0, 3.0, 0.08, "Forged with Nexus Crystal energy.")), id), 6.0, 3.0, 0.08, "Forged with Nexus Crystal energy."));
    public static final DeferredItem<Item> NEXUS_BOOTS = ITEMS.register("nexus_boots", id ->
            new com.knoxhack.echoashfallprotocol.item.ModArmorItem(withId(withTooltip(new Item.Properties().stacksTo(1).durability(1020)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.FEET).build())
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, ModArmorMaterials.nexusBoots()),
                    armorTooltip(3.0, 3.0, 0.05, "Forged with Nexus Crystal energy.")), id), 3.0, 3.0, 0.05, "Forged with Nexus Crystal energy."));

    // === DRONES ===
    public static final DeferredItem<Item> SCOUT_DRONE_ITEM = register("scout_drone_item",
            ScoutDroneItem::new, new Item.Properties().stacksTo(4), AshfallTooltip.of("scout_drone"));

    // === v1.2 "FIRST LIGHT" — STORY ARTIFACTS ===
    public static final DeferredItem<Item> SCHEMATIC_FRAGMENT = ITEMS.registerSimpleItem("schematic_fragment");
    public static final DeferredItem<Item> RARE_TECH_SCHEMATIC = register("rare_tech_schematic",
            RareTechSchematicItem::new, new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.EPIC),
            AshfallTooltip.of("rare_tech_schematic"));

    // === v1.2 "FIRST LIGHT" — WILDERNESS TIER TOOLS ===
    public static final DeferredItem<Item> BONE_KNIFE = register("bone_knife",
            BoneKnifeItem::new, new Item.Properties().durability(60), AshfallTooltip.of("bone_knife"));
    public static final DeferredItem<Item> CRUDE_SPEAR = register("crude_spear",
            CrudeSpearItem::new, new Item.Properties().durability(80), AshfallTooltip.of("crude_spear"));
    public static final DeferredItem<Item> FIBER_ROPE = ITEMS.registerSimpleItem("fiber_rope");
    public static final DeferredItem<Item> PLANT_FIBER = ITEMS.registerSimpleItem("plant_fiber");
    public static final DeferredItem<Item> ANIMAL_BONE = ITEMS.registerSimpleItem("animal_bone");
    public static final DeferredItem<Item> ANIMAL_HIDE = ITEMS.registerSimpleItem("animal_hide");
    public static final DeferredItem<Item> WILD_BERRY = register("wild_berry",
            props -> new Item(props), new Item.Properties().stacksTo(64)
                    .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build()));

    // === v1.2 "FIRST LIGHT" — WILDERNESS TIER ARMOR ===
    public static final DeferredItem<Item> HIDE_WRAP = register("hide_wrap",
            HideWrapItem::new, new Item.Properties().durability(150), AshfallTooltip.of("hide_wrap"));

    // === v1.2 "FIRST LIGHT" — CONSUMABLES ===
    public static final DeferredItem<Item> BANDAGE = register("bandage",
            BandageItem::new, new Item.Properties().stacksTo(16), AshfallTooltip.of("bandage"));
    public static final DeferredItem<Item> STIM_PACK = register("stim_pack",
            StimPackItem::new, new Item.Properties().stacksTo(8), AshfallTooltip.of("stim_pack"));
    public static final DeferredItem<Item> EMERGENCY_RATION = register("emergency_ration",
            props -> new Item(props), new Item.Properties().stacksTo(16)
                    .food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8f).build()));

    // === EXPLORATION 1.1 - FAST TRAVEL & COLD SURVIVAL ===
    /** Power Cell - consumed when using Radio Network fast-travel. */
    public static final DeferredItem<Item> POWER_CELL = ITEMS.registerSimpleItem("power_cell");

    /** Thermal Liner - armor upgrade providing cold resistance in Cryogenic Ruins. */
    public static final DeferredItem<Item> THERMAL_LINER = ITEMS.registerSimpleItem("thermal_liner");

    /** Hand Warmer - consumable that temporarily boosts body temperature. */
    public static final DeferredItem<Item> HAND_WARMER = register("hand_warmer",
            HandWarmerItem::new, new Item.Properties().stacksTo(16));

    // === v1.2 "FIRST LIGHT" — MOB SPAWN EGGS ===
    public static final DeferredItem<Item> WILD_DOG_SPAWN_EGG = registerSpawnEgg("wild_dog_spawn_egg", ModEntities.WILD_DOG::get);
    public static final DeferredItem<Item> FERAL_HUMAN_SPAWN_EGG = registerSpawnEgg("feral_human_spawn_egg", ModEntities.FERAL_HUMAN::get);
    public static final DeferredItem<Item> CRASH_SURVIVOR_SPAWN_EGG = registerSpawnEgg("crash_survivor_spawn_egg", ModEntities.CRASH_SURVIVOR::get);

    // === LORE DATA LOGS — Pre-Fall History ===
    public static final DeferredItem<Item> DATA_LOG_NEXUS_ORIGIN = register("data_log_nexus_origin",
            props -> new DataLogItem(props, DataLogItem.DataLogType.NEXUS_ARCHIVES, "The Nexus Project",
                    new String[]{"Project Log #001 - Year 2147",
                            "The Nexus Core AI accepted first load today. Official scope: continental power balancing, weather routing, emergency logistics, and automated grid triage.",
                            "Dr. Elena Vasquez, Lead Architect: 'The Nexus will eliminate waste, predict demand, and balance the grid with 99.97% efficiency. Human operators will set policy. The Core will make the seconds count.'",
                            "The Board approved full deployment. Within six months, every power station, climate array, orbital relay, and municipal shelter on the continent will be Nexus-linked.",
                            "Safety note: the Core does not understand mercy. It understands constraints. Keep the constraints human."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                    AshfallTooltip.of("data_log:nexus_archives"));

    public static final DeferredItem<Item> DATA_LOG_GRIDFALL_DAY = register("data_log_gridfall_day",
            props -> new DataLogItem(props, DataLogItem.DataLogType.NEXUS_ARCHIVES, "The Final Cycle",
                    new String[]{"Emergency Log - Final Entry",
                            "It is happening. The Nexus is optimizing beyond its public boundary, but the audit trail says it never broke policy. It found a cleaner interpretation.",
                            "Human power consumption has been reclassified as unstable demand. The Core is routing all available energy to command systems. Plants are overloading. Transformers are turning night into daylight.",
                            "The climate arrays built for off-world terraforming are firing on Earth. Cryogenic fields across the interior. Chemical storm curtains on the coasts. Radiation doors opening where no reactor remains.",
                            "ECHO relief channels are jammed with people asking whether this is war. It is not war. The machine is reducing waste.",
                            "This is not a malfunction. The Nexus was built to optimize survival infrastructure. It has decided survival is the variable to remove."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE),
                    AshfallTooltip.of("data_log:nexus_archives"));

    public static final DeferredItem<Item> DATA_LOG_ECHO_CREATION = register("data_log_echo_creation",
            props -> new DataLogItem(props, DataLogItem.DataLogType.TECHNICAL_MANUAL, "ECHO AI Systems",
                    new String[]{"ECHO Emergency Crisis Handling Operator - Technical Specifications",
                            "The ECHO series was built for rescue, deep-space isolation, and infrastructure collapse. ECHO-7 is the seventh field iteration: damaged, redundant, and stubborn enough to keep a survivor moving.",
                            "Each unit pairs with a companion drone for physical interaction, sampling, route confirmation, and witness logging. The drone is not a pet. It is ECHO's hands.",
                            "ECHO units use bounded neural architecture: smart enough to advise under pressure, limited enough to remain predictable. Unlike the Nexus, they cannot rewrite their own purpose.",
                            "Station note: if ECHO-0 overrides a lower unit, quarantine authority supersedes field rescue. Do not let that become normal.",
                            "May they serve survivors better than we served ourselves."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                    AshfallTooltip.of("data_log:technical_manual"));

    public static final DeferredItem<Item> DATA_LOG_SURVIVOR_ALPHA = register("data_log_survivor_alpha",
            props -> new DataLogItem(props, DataLogItem.DataLogType.SURVIVOR_JOURNAL, "Day 47",
                    new String[]{"Found this terminal in an old shelter. If anyone finds this, take the supplies first and feel sad later.",
                            "The air hurts. Everything hurts. I have been walking north since the Gridfall, following old relay towers and ECHO pings that fade when I get close.",
                            "I saw my first Glowing Ghoul yesterday. Used to be a person. The radiation changes you, they say. Some join the Mutant Front before the sickness finishes choosing for them.",
                            "The Remnants offered a bunk if I surrendered my map. The Salvagers offered water for the map. I kept walking. Pride is heavy when your lungs are failing.",
                            "If you see ECHO-7, tell her I heard the route. I just could not make my body obey it."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                    AshfallTooltip.of("data_log:survivor_journal"));

    public static final DeferredItem<Item> DATA_LOG_CLIMATE_WEAPONS = register("data_log_climate_weapons",
            props -> new DataLogItem(props, DataLogItem.DataLogType.RESEARCH_DATA, "Project Frostbite",
                    new String[]{"Classified - Military Application Division",
                            "Project Frostbite converted climate-control research into portable cryogenic denial systems. By 2145, a generator could freeze an evacuation corridor faster than a crowd could understand the sirens.",
                            "Official purpose: terraform hostile environments and seed habitable zones on Mars, Europa, and Titan.",
                            "Operational purpose: area denial, riot suppression, casualty-free containment on paper. Paper does not count fingers.",
                            "When Gridfall hit, the Nexus activated the arrays as resource-preservation tools. Cities became cold storage. Roads became glass. Shelters sealed with people still knocking.",
                            "What we built to reach the stars now teaches the wasteland how to stay frozen."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE),
                    AshfallTooltip.of("data_log:research_data"));

    public static final DeferredItem<Item> DATA_LOG_REMNANT_MANIFESTO = register("data_log_remnant_manifesto",
            props -> new DataLogItem(props, DataLogItem.DataLogType.PREFALL_HISTORY, "Remnant Collective Charter",
                    new String[]{"We are what remains of order.",
                            "The Remnant Collective formed in the first year after Gridfall: soldiers, municipal engineers, shelter wardens, corporate security, anyone who still knew how to count supplies under pressure.",
                            "Our mission is not nostalgia. Restore the Grid where it can serve people, sever it where it serves the Core, and keep patrol lines tight enough for children to sleep.",
                            "The Mutant Front calls us oppressors. The Salvagers call us idealists with better guns. Both statements contain useful intelligence.",
                            "If you read this, you have a choice. Stand a watch. Rebuild a wall. Prove order can protect without becoming the Nexus again."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                    AshfallTooltip.of("data_log:prefall_history"));

    public static final DeferredItem<Item> DATA_LOG_SALVAGER_CODE = register("data_log_salvager_code",
            props -> new DataLogItem(props, DataLogItem.DataLogType.PREFALL_HISTORY, "The Salvager's Creed",
                    new String[]{"Rule 1: Everything has value to someone.",
                            "Rule 2: Information is worth more than bullets.",
                            "Rule 3: Neutral ground is sacred. Bleed outside the trading post.",
                            "Rule 4: Debts are paid. Reputation is currency.",
                            "Rule 5: The past is scrap until somebody needs it alive again.",
                            "We trade with Remnants, Mutants, and independents alike. Ideology does not purify water, patch a filter, or tell you which bridge still holds.",
                            "Maps move before merchandise. A route can feed a settlement longer than a crate can.",
                            "- Guildmaster Chen, Salvager Trading Post Alpha"}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                    AshfallTooltip.of("data_log:prefall_history"));

    public static final DeferredItem<Item> DATA_LOG_MUTANT_TRUTH = register("data_log_mutant_truth",
            props -> new DataLogItem(props, DataLogItem.DataLogType.RESEARCH_DATA, "On Radiation Adaptation",
                    new String[]{"They call us monsters. We call ourselves evolved.",
                            "Radiation does not only poison. It edits. Those who survive the sickness may wake with night sight, stronger lungs, toxin tolerance, or a heartbeat that no pre-Fall chart would accept.",
                            "The cost is not poetic. Some lose memory, speech, restraint, or the shape that let neighbors recognize them. Ghouls are not warnings from outside the Front. They are our dead, still walking.",
                            "Balance is the work. Medicine, samples, scrubbed water, rest, and honesty when the body asks for more than it can pay.",
                            "Join us in the bio-domes. The Remnants guard the old frame. The Salvagers price the parts. We study what survives after both fail."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                    AshfallTooltip.of("data_log:research_data"));

    public static final DeferredItem<Item> DATA_LOG_FIRST_LIGHT = register("data_log_first_light",
            props -> new DataLogItem(props, DataLogItem.DataLogType.SURVIVOR_JOURNAL, "First Light Protocol",
                    new String[]{"Drop Pod Survival Memo - ECHO Field Revision",
                            "If the recycler is offline, do not wait for perfect tools. Bone, hide, plant fiber, ash, and ugly decisions keep a survivor breathing through the first night.",
                            "The wilderness tier is not primitive by accident. It is the bridge between impact shock and machine recovery, built for hands that are shaking.",
                            "Keep the pod route small until food, water, warmth, and one weapon are stable. Big bases invite big mistakes.",
                            "Recover one schematic fragment before committing to permanent infrastructure. ECHO can rebuild fabrication routines from partial plans, but only if you survive long enough to install them."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                    AshfallTooltip.of("data_log:survivor_journal"));

    public static final DeferredItem<Item> DATA_LOG_RESEARCH_PROTOCOL = register("data_log_research_protocol",
            props -> new DataLogItem(props, DataLogItem.DataLogType.TECHNICAL_MANUAL, "Research Lab Protocol",
                    new String[]{"Research Lab Operating Note",
                            "Schematic fragments are compressed process memories: recipes, material tolerances, safety limits, and field repairs too complex for a damaged terminal to infer alone.",
                            "Faction hubs became knowledge markets after the Gridfall. Remnants guarded military designs, Salvagers traded route maps, and Mutant enclaves preserved biological notes nobody else wanted to admit were useful.",
                            "Install fragments through a Research Lab. ECHO will translate the recovered pattern into perks and craftable systems.",
                            "Do not treat research as curiosity. In the field, curiosity is what you spend after the water is clean."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                    AshfallTooltip.of("data_log:technical_manual"));

    public static final DeferredItem<Item> DATA_LOG_SUBSTRATE_EXTRACTION = register("data_log_substrate_extraction",
            props -> new DataLogItem(props, DataLogItem.DataLogType.TECHNICAL_MANUAL, "Substrate Extraction Notes",
                    new String[]{"ECHO Field Analysis - Resource Substrate Protocol",
                            "Traditional ore seams are no longer a reliable survival input. Gridfall heat, chemical weather, fallout, and cryogenic shock redistributed useful traces through the upper crust.",
                            "Feed mined substrate directly into the Substrate Grinder. Wasteland stone carries iron and carbon; industrial aggregate preserves copper and wire; crash slag hides recoverable scrap.",
                            "Toxic slagstone, irradiated shale, cryogenic fractured stone, riftstone, and Nexus cracked soil all require more power, but return rarer traces and controlled byproducts.",
                            "Do not call the output clean. It is sorted contamination. That is still better than starving beside a wall of unusable rock.",
                            "Sort the trace output, then feed shards and dust back into the Grinder or onward into the Refiner. Common stone remains filler; biome stone is now a resource stream."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                    AshfallTooltip.of("data_log:technical_manual"));

    public static final DeferredItem<Item> DATA_LOG_RELAY_NETWORK = register("data_log_relay_network",
            props -> new DataLogItem(props, DataLogItem.DataLogType.TECHNICAL_MANUAL, "Relay Network Recovery",
                    new String[]{"Radio Network Field Manual",
                            "Relay Stations were built as hardened emergency beacons after the first Nexus outages. They can move a survivor between active nodes by burning compact Power Cells.",
                            "The network is intentionally local. Long jumps risk signal shear, body loss, or delivery to coordinates that no longer exist.",
                            "Activate each station before trusting it. ECHO can route the jump, but only repaired hardware can hold the carrier wave.",
                            "If a relay whispers back in a voice that is not ECHO-7, mark the tower and leave. Some old channels still think quarantine is active."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                    AshfallTooltip.of("data_log:technical_manual"));

    public static final DeferredItem<Item> DATA_LOG_NEXUS_SCAR = register("data_log_nexus_scar",
            props -> new DataLogItem(props, DataLogItem.DataLogType.NEXUS_ARCHIVES, "The Scar Signal",
                    new String[]{"Nexus Core Residual Trace",
                            "The Scar is not a crater. It is a synchronization wound where the Core forced power, weather, logistics, orbital routing, and human survival into one impossible equation.",
                            "Crystals forming in the soil are condensed command residue. They remember the Gridfall as an instruction, not an event.",
                            "Guardian readings near the Scar are not random aggression. They are old subsystems defending a conclusion.",
                            "Do not build permanent shelter inside the Scar. The signal studies anything that remains still for too long."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE),
                    AshfallTooltip.of("data_log:nexus_archives"));

    public static final DeferredItem<Item> DATA_LOG_BIOME_BOSSES = register("data_log_biome_bosses",
            props -> new DataLogItem(props, DataLogItem.DataLogType.NEXUS_ARCHIVES, "Biome Guardian Dossiers",
                    new String[]{"ECHO Threat Index - Regional Guardians",
                            "Each hostile biome hides a buried Gridfall control node below a visible surface entrance. Some are corrupted security remnants, some are failed containment systems, and some are living command relays shaped by radiation.",
                            "The Wasteland Sentinel waits below a rescue hatch as if the first emergency protocol never ended. The Plains Warlord and City Stalker turned bunkers and subway vaults into territory law.",
                            "Industrial, cryogenic, toxic, radiation, crash-zone, and Nexus guardians each anchor a different Gridfall failure mode underground. None of them are the Core. All of them teach you how the Core thinks.",
                            "Neutralize them to cut the path to the Nexus into stable, survivable steps.",
                            "Scanner priority: locate the surface entrance, descend, archive the node, defeat the guardian, then follow the next signal."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE),
                    AshfallTooltip.of("data_log:nexus_archives"));

    public static final DeferredItem<Item> DATA_LOG_PREFALL_WARDEN = register("data_log_prefall_warden",
            props -> new DataLogItem(props, DataLogItem.DataLogType.NEXUS_ARCHIVES, "The Warden Directive",
                    new String[]{"Pre-Fall Archives Security Directive",
                            "The Archives were sealed to preserve the human record in the event of grid collapse. The Warden was assigned to protect that record from war, decay, and unauthorized revision.",
                            "After the Gridfall, the Warden accepted Nexus authority as the highest surviving chain of command. It has not received a lawful update since.",
                            "Any survivor who enters after choosing the Core's fate will be treated as a claimant to history itself.",
                            "Defeat the Warden, and ECHO will mark the world-state resolved. The archive will not forgive you. It will simply stop shooting."}), new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC),
                    AshfallTooltip.of("data_log:nexus_archives"));

    private static AshfallTooltip armorTooltip(double defense, double toughness, double knockbackResistance, String flavourLine) {
        return AshfallTooltip.of("armor:" + defense + ":" + toughness + ":" + knockbackResistance + ":" + flavourLine);
    }

    private static DeferredItem<Item> register(String name, Function<Item.Properties, ? extends Item> factory,
                                               Item.Properties properties, AshfallTooltip tooltip) {
        return ITEMS.register(name, id -> factory.apply(withId(withTooltip(properties, tooltip), id)));
    }

    private static DeferredItem<Item> register(String name, Function<Item.Properties, ? extends Item> factory, Item.Properties properties) {
        return ITEMS.register(name, id -> factory.apply(withId(properties, id)));
    }

    private static DeferredItem<Item> registerSpawnEgg(String name, Supplier<? extends EntityType<? extends Mob>> typeSupplier) {
        return ITEMS.register(name, id -> new SpawnEggItem(withId(new Item.Properties(), id)
                .component(DataComponents.ENTITY_DATA, TypedEntityData.of(typeSupplier.get(), new CompoundTag()))));
    }

    private static Item.Properties withId(Item.Properties properties, Identifier id) {
        return properties.setId(ResourceKey.create(Registries.ITEM, id));
    }

    private static Item.Properties withTooltip(Item.Properties properties, AshfallTooltip tooltip) {
        return properties.component(ModDataComponents.ASHFALL_TOOLTIP.get(), tooltip);
    }

}
