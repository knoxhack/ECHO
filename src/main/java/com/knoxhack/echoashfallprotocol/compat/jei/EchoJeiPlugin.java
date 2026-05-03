package com.knoxhack.echoashfallprotocol.compat.jei;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.block.menu.CrystallineSynthesizerMenu;
import com.knoxhack.echoashfallprotocol.block.menu.FilterWorkbenchMenu;
import com.knoxhack.echoashfallprotocol.block.menu.HandRecyclerMenu;
import com.knoxhack.echoashfallprotocol.block.menu.IsotopeRefinerMenu;
import com.knoxhack.echoashfallprotocol.block.menu.OreGrinderMenu;
import com.knoxhack.echoashfallprotocol.block.menu.RadiationCleanserMenu;
import com.knoxhack.echoashfallprotocol.block.menu.ScrapPressMenu;
import com.knoxhack.echoashfallprotocol.block.menu.WaterPurifierMenu;
import com.knoxhack.echoashfallprotocol.client.screen.CrystallineSynthesizerScreen;
import com.knoxhack.echoashfallprotocol.client.screen.DeepCoreMinerScreen;
import com.knoxhack.echoashfallprotocol.client.screen.FilterWorkbenchScreen;
import com.knoxhack.echoashfallprotocol.client.screen.HandRecyclerScreen;
import com.knoxhack.echoashfallprotocol.client.screen.IsotopeRefinerScreen;
import com.knoxhack.echoashfallprotocol.client.screen.MicroGeneratorScreen;
import com.knoxhack.echoashfallprotocol.client.screen.OreGrinderScreen;
import com.knoxhack.echoashfallprotocol.client.screen.RadiationCleanserScreen;
import com.knoxhack.echoashfallprotocol.client.screen.ScrapPressScreen;
import com.knoxhack.echoashfallprotocol.client.screen.ThermalBurnerScreen;
import com.knoxhack.echoashfallprotocol.client.screen.WaterPurifierScreen;
import com.knoxhack.echoashfallprotocol.registry.ModBlocks;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import com.knoxhack.echoashfallprotocol.registry.ModMenuTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class EchoJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                category(gui, EchoJeiRecipeTypes.HAND_RECYCLER, "Hand Recycler", ModBlocks.HAND_RECYCLER_ITEM.get()),
                category(gui, EchoJeiRecipeTypes.WATER_PURIFIER, "Water Purifier", ModBlocks.WATER_PURIFIER_ITEM.get()),
                category(gui, EchoJeiRecipeTypes.WATER_COLLECTION, "Water Collection", ModItems.DIRTY_WATER_BOTTLE.get()),
                category(gui, EchoJeiRecipeTypes.THERMAL_BURNER, "Thermal Burner", ModBlocks.THERMAL_BURNER_ITEM.get()),
                category(gui, EchoJeiRecipeTypes.MICRO_GENERATOR, "Micro Generator", ModBlocks.MICRO_GENERATOR_ITEM.get()),
                category(gui, EchoJeiRecipeTypes.FILTER_WORKBENCH, "Filter Workbench", ModBlocks.FILTER_WORKBENCH_ITEM.get()),
                category(gui, EchoJeiRecipeTypes.SCRAP_PRESS, "Scrap Press", ModBlocks.SCRAP_PRESS_ITEM.get()),
                category(gui, EchoJeiRecipeTypes.ORE_GRINDER, "Substrate Grinder", ModBlocks.ORE_GRINDER_ITEM.get()),
                category(gui, EchoJeiRecipeTypes.ISOTOPE_REFINER, "Isotope Refiner", ModBlocks.ISOTOPE_REFINER_ITEM.get()),
                category(gui, EchoJeiRecipeTypes.RADIATION_CLEANSER, "Radiation Cleanser", ModBlocks.RADIATION_CLEANSER_ITEM.get()),
                category(gui, EchoJeiRecipeTypes.CRYSTALLINE_SYNTHESIZER, "Crystalline Synthesizer", ModBlocks.CRYSTALLINE_SYNTHESIZER_ITEM.get()),
                category(gui, EchoJeiRecipeTypes.DEEP_CORE_MINER, "Deep Core Miner", ModBlocks.DEEP_CORE_MINER_ITEM.get())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        EchoJeiRecipeCatalog.allRecipes().forEach(registration::addRecipes);

        registration.addItemStackInfo(new ItemStack(ModBlocks.RAIN_COLLECTOR_ITEM.get()),
                Component.literal("Collects dirty water during rain. Use the block while rain reaches the collector."));
        registration.addItemStackInfo(new ItemStack(ModBlocks.HAND_RECYCLER_ITEM.get()),
                Component.literal("First-hour machine bridge: craft with 1 Machine Casing, 4 Scrap Metal, and 4 Scrap Wire. Use a starter battery or nearby generator for power."));
        registration.addItemStackInfo(new ItemStack(ModBlocks.MICRO_GENERATOR_ITEM.get()),
                Component.literal("Starter power source. The first-hour recipe uses 1 Machine Casing, 3 Scrap Wire, 1 Energy Cell, 1 Circuit Board, and 1 Scrap Metal."));
        registration.addItemStackInfo(new ItemStack(ModBlocks.WATER_PURIFIER_ITEM.get()),
                Component.literal("Stable-base water loop. Build with 3 Machine Casings, 2 Scrap Plastic, 1 Filtration Membrane, and 1 Circuit Board, then feed dirty water and filters."));
        registration.addItemStackInfo(new ItemStack(ModItems.FILTER_CARTRIDGE_BASIC.get()),
                Component.literal("Basic filters have three early recipes: Scrap Plastic + Ash + Scrap Wire, String, or Plant Fiber + extra Ash. Rarely recovered from toxic salvage and scavengers."));
        registration.addItemStackInfo(new ItemStack(ModItems.RAD_AWAY.get()),
                Component.translatable("jei.EchoAshfallProtocol.rad_away.info"));
        registration.addItemStackInfo(new ItemStack(ModItems.BASIC_BATTERY.get()),
                Component.translatable("jei.EchoAshfallProtocol.battery.info"));
        registration.addItemStackInfo(new ItemStack(ModItems.ADVANCED_BATTERY.get()),
                Component.translatable("jei.EchoAshfallProtocol.battery.info"));
        registration.addItemStackInfo(new ItemStack(ModItems.ELITE_BATTERY.get()),
                Component.translatable("jei.EchoAshfallProtocol.battery.info"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.POWER_CABLE_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.energy.cables"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.REINFORCED_POWER_CABLE_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.energy.cables"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.HIGH_VOLTAGE_POWER_CABLE_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.energy.cables"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.ENERGY_METER_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.energy.meter"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.LOAD_DISTRIBUTOR_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.energy.distributor"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.SCRAP_DYNAMO_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.energy.scrap_dynamo"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.NEXUS_CAPACITOR_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.energy.nexus_capacitor"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.THERMAL_ARRAY_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.thermal_array"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.ORE_GRINDER_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.ore_grinder"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.ISOTOPE_REFINER_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.isotope_refiner"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.FIELD_MED_BAY_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.field_med_bay"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.ATMOSPHERIC_SCRUBBER_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.scrubber"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.RADIATION_CLEANSER_ITEM.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.cleanser"));
        registration.addItemStackInfo(new ItemStack(ModItems.ALLOY_BLADE.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.alloy_weapon"));
        registration.addItemStackInfo(new ItemStack(ModItems.ALLOY_HAMMER.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.alloy_weapon"));
        registration.addItemStackInfo(new ItemStack(ModItems.ALLOY_HELMET.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.alloy_armor"));
        registration.addItemStackInfo(new ItemStack(ModItems.ALLOY_CHESTPLATE.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.alloy_armor"));
        registration.addItemStackInfo(new ItemStack(ModItems.ALLOY_LEGGINGS.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.alloy_armor"));
        registration.addItemStackInfo(new ItemStack(ModItems.ALLOY_BOOTS.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.alloy_armor"));
        registration.addItemStackInfo(new ItemStack(ModItems.HAZMAT_HELMET.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.hazmat"));
        registration.addItemStackInfo(new ItemStack(ModItems.HAZMAT_CHESTPLATE.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.hazmat"));
        registration.addItemStackInfo(new ItemStack(ModItems.HAZMAT_LEGGINGS.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.hazmat"));
        registration.addItemStackInfo(new ItemStack(ModItems.HAZMAT_BOOTS.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.hazmat"));
        registration.addItemStackInfo(new ItemStack(ModItems.FILTER_CARTRIDGE_ADVANCED.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.advanced_filter"));
        registration.addItemStackInfo(new ItemStack(ModItems.PORTABLE_SIGNAL_SCANNER.get()),
                Component.translatable("jei.EchoAshfallProtocol.midgame.route_supplies"));
        registration.addItemStackInfo(new ItemStack(ModItems.MACHINE_UPGRADE_EFFICIENCY.get()),
                Component.translatable("jei.EchoAshfallProtocol.energy.upgrades"));
        registration.addItemStackInfo(new ItemStack(ModItems.MACHINE_UPGRADE_OVERCLOCK.get()),
                Component.translatable("jei.EchoAshfallProtocol.energy.upgrades"));
        registration.addItemStackInfo(new ItemStack(ModBlocks.DEEP_CORE_MINER_ITEM.get()),
                Component.literal("Generates random core samples only when powered and placed at Y <= -32."));

        EchoJeiRecipeCatalog.gatedItemInfo().forEach((item, info) ->
                registration.addItemStackInfo(new ItemStack(item), info));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(EchoJeiRecipeTypes.HAND_RECYCLER, ModBlocks.HAND_RECYCLER_ITEM.get());
        registration.addCraftingStation(EchoJeiRecipeTypes.WATER_PURIFIER, ModBlocks.WATER_PURIFIER_ITEM.get());
        registration.addCraftingStation(EchoJeiRecipeTypes.THERMAL_BURNER, ModBlocks.THERMAL_BURNER_ITEM.get());
        registration.addCraftingStation(EchoJeiRecipeTypes.MICRO_GENERATOR, ModBlocks.MICRO_GENERATOR_ITEM.get());
        registration.addCraftingStation(EchoJeiRecipeTypes.FILTER_WORKBENCH, ModBlocks.FILTER_WORKBENCH_ITEM.get());
        registration.addCraftingStation(EchoJeiRecipeTypes.SCRAP_PRESS, ModBlocks.SCRAP_PRESS_ITEM.get());
        registration.addCraftingStation(EchoJeiRecipeTypes.ORE_GRINDER, ModBlocks.ORE_GRINDER_ITEM.get());
        registration.addCraftingStation(EchoJeiRecipeTypes.ISOTOPE_REFINER, ModBlocks.ISOTOPE_REFINER_ITEM.get());
        registration.addCraftingStation(EchoJeiRecipeTypes.RADIATION_CLEANSER, ModBlocks.RADIATION_CLEANSER_ITEM.get());
        registration.addCraftingStation(EchoJeiRecipeTypes.CRYSTALLINE_SYNTHESIZER, ModBlocks.CRYSTALLINE_SYNTHESIZER_ITEM.get());
        registration.addCraftingStation(EchoJeiRecipeTypes.DEEP_CORE_MINER, ModBlocks.DEEP_CORE_MINER_ITEM.get());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(HandRecyclerScreen.class, 78, 39, 30, 8, EchoJeiRecipeTypes.HAND_RECYCLER);
        registration.addRecipeClickArea(WaterPurifierScreen.class, 78, 39, 30, 8, EchoJeiRecipeTypes.WATER_PURIFIER);
        registration.addRecipeClickArea(ThermalBurnerScreen.class, 83, 32, 12, 34, EchoJeiRecipeTypes.THERMAL_BURNER);
        registration.addRecipeClickArea(MicroGeneratorScreen.class, 98, 31, 26, 34, EchoJeiRecipeTypes.MICRO_GENERATOR);
        registration.addRecipeClickArea(FilterWorkbenchScreen.class, 82, 39, 24, 8, EchoJeiRecipeTypes.FILTER_WORKBENCH);
        registration.addRecipeClickArea(ScrapPressScreen.class, 78, 39, 30, 8, EchoJeiRecipeTypes.SCRAP_PRESS);
        registration.addRecipeClickArea(OreGrinderScreen.class, 78, 39, 30, 8, EchoJeiRecipeTypes.ORE_GRINDER);
        registration.addRecipeClickArea(IsotopeRefinerScreen.class, 78, 39, 30, 8, EchoJeiRecipeTypes.ISOTOPE_REFINER);
        registration.addRecipeClickArea(RadiationCleanserScreen.class, 72, 50, 28, 8, EchoJeiRecipeTypes.RADIATION_CLEANSER);
        registration.addRecipeClickArea(CrystallineSynthesizerScreen.class, 78, 42, 32, 8, EchoJeiRecipeTypes.CRYSTALLINE_SYNTHESIZER);
        registration.addRecipeClickArea(DeepCoreMinerScreen.class, 42, 46, 58, 8, EchoJeiRecipeTypes.DEEP_CORE_MINER);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(HandRecyclerMenu.class, ModMenuTypes.HAND_RECYCLER.get(),
                EchoJeiRecipeTypes.HAND_RECYCLER, 0, 1, 4, 36);
        registration.addRecipeTransferHandler(WaterPurifierMenu.class, ModMenuTypes.WATER_PURIFIER.get(),
                EchoJeiRecipeTypes.WATER_PURIFIER, 0, 2, 4, 36);
        registration.addRecipeTransferHandler(FilterWorkbenchMenu.class, ModMenuTypes.FILTER_WORKBENCH.get(),
                EchoJeiRecipeTypes.FILTER_WORKBENCH, 0, 3, 5, 36);
        registration.addRecipeTransferHandler(ScrapPressMenu.class, ModMenuTypes.SCRAP_PRESS.get(),
                EchoJeiRecipeTypes.SCRAP_PRESS, 0, 1, 3, 36);
        registration.addRecipeTransferHandler(OreGrinderMenu.class, ModMenuTypes.ORE_GRINDER.get(),
                EchoJeiRecipeTypes.ORE_GRINDER, 0, 2, 5, 36);
        registration.addRecipeTransferHandler(IsotopeRefinerMenu.class, ModMenuTypes.ISOTOPE_REFINER.get(),
                EchoJeiRecipeTypes.ISOTOPE_REFINER, 0, 2, 5, 36);
        registration.addRecipeTransferHandler(RadiationCleanserMenu.class, ModMenuTypes.RADIATION_CLEANSER.get(),
                EchoJeiRecipeTypes.RADIATION_CLEANSER, 0, 2, 4, 36);
        registration.addRecipeTransferHandler(CrystallineSynthesizerMenu.class, ModMenuTypes.CRYSTALLINE_SYNTHESIZER.get(),
                EchoJeiRecipeTypes.CRYSTALLINE_SYNTHESIZER, 0, 3, 5, 36);
    }

    private static EchoJeiCategory category(IGuiHelper gui, IRecipeType<EchoJeiRecipe> type,
                                            String title, net.minecraft.world.level.ItemLike icon) {
        return new EchoJeiCategory(type, Component.literal(title), gui.createDrawableItemLike(icon));
    }
}
