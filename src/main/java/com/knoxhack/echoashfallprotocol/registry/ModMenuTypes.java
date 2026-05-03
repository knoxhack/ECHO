package com.knoxhack.echoashfallprotocol.registry;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.block.menu.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, EchoAshfallProtocol.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ResearchLabMenu>> RESEARCH_LAB =
            MENU_TYPES.register("research_lab",
                    () -> IMenuTypeExtension.create(ResearchLabMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<HandRecyclerMenu>> HAND_RECYCLER =
            MENU_TYPES.register("hand_recycler",
                    () -> IMenuTypeExtension.create(HandRecyclerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ThermalBurnerMenu>> THERMAL_BURNER =
            MENU_TYPES.register("thermal_burner",
                    () -> IMenuTypeExtension.create(ThermalBurnerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<WaterPurifierMenu>> WATER_PURIFIER =
            MENU_TYPES.register("water_purifier",
                    () -> IMenuTypeExtension.create(WaterPurifierMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MicroGeneratorMenu>> MICRO_GENERATOR =
            MENU_TYPES.register("micro_generator",
                    () -> IMenuTypeExtension.create(MicroGeneratorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FilterWorkbenchMenu>> FILTER_WORKBENCH =
            MENU_TYPES.register("filter_workbench",
                    () -> IMenuTypeExtension.create(FilterWorkbenchMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ScrapPressMenu>> SCRAP_PRESS =
            MENU_TYPES.register("scrap_press",
                    () -> IMenuTypeExtension.create(ScrapPressMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MachineStatusMenu>> MACHINE_STATUS =
            MENU_TYPES.register("machine_status",
                    () -> IMenuTypeExtension.create(MachineStatusMenu::new));

    // === TIER 2.5 POWER GENERATION ===
    public static final DeferredHolder<MenuType<?>, MenuType<ThermalArrayMenu>> THERMAL_ARRAY =
            MENU_TYPES.register("thermal_array",
                    () -> IMenuTypeExtension.create(ThermalArrayMenu::new));

    // === GEO-EXTRACTOR MACHINES ===
    public static final DeferredHolder<MenuType<?>, MenuType<com.knoxhack.echoashfallprotocol.block.menu.OreGrinderMenu>> ORE_GRINDER =
            MENU_TYPES.register("ore_grinder",
                    () -> IMenuTypeExtension.create(com.knoxhack.echoashfallprotocol.block.menu.OreGrinderMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<com.knoxhack.echoashfallprotocol.block.menu.IsotopeRefinerMenu>> ISOTOPE_REFINER =
            MENU_TYPES.register("isotope_refiner",
                    () -> IMenuTypeExtension.create(com.knoxhack.echoashfallprotocol.block.menu.IsotopeRefinerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<com.knoxhack.echoashfallprotocol.block.menu.CrystallineSynthesizerMenu>> CRYSTALLINE_SYNTHESIZER =
            MENU_TYPES.register("crystalline_synthesizer",
                    () -> IMenuTypeExtension.create(com.knoxhack.echoashfallprotocol.block.menu.CrystallineSynthesizerMenu::new));

    // === ENDGAME MACHINES ===
    public static final DeferredHolder<MenuType<?>, MenuType<com.knoxhack.echoashfallprotocol.block.menu.DeepCoreMinerMenu>> DEEP_CORE_MINER =
            MENU_TYPES.register("deep_core_miner",
                    () -> IMenuTypeExtension.create(com.knoxhack.echoashfallprotocol.block.menu.DeepCoreMinerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<com.knoxhack.echoashfallprotocol.block.menu.RadiationCleanserMenu>> RADIATION_CLEANSER =
            MENU_TYPES.register("radiation_cleanser",
                    () -> IMenuTypeExtension.create(com.knoxhack.echoashfallprotocol.block.menu.RadiationCleanserMenu::new));

    // Companion and Scout Drone controls are intentionally routed through the ECHO terminal.
}
