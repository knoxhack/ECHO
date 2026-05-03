package com.knoxhack.echoorbitalremnants;

import com.knoxhack.echoorbitalremnants.registry.ModBlocks;
import com.knoxhack.echoorbitalremnants.registry.ModBlockEntities;
import com.knoxhack.echoorbitalremnants.registry.ModCreativeTabs;
import com.knoxhack.echoorbitalremnants.registry.ModEntities;
import com.knoxhack.echoorbitalremnants.registry.ModItems;
import com.knoxhack.echoorbitalremnants.registry.ModMenus;
import com.knoxhack.echoorbitalremnants.registry.ModRecipes;
import com.knoxhack.echoorbitalremnants.registry.ModWorldgen;
import com.knoxhack.echoorbitalremnants.integration.AshfallCompat;
import com.knoxhack.echoorbitalremnants.integration.OrbitalTerminalCommonIntegration;
import com.knoxhack.echoorbitalremnants.network.ModNetworking;
import com.knoxhack.echoorbitalremnants.test.ModGameTests;
import com.knoxhack.echoorbitalremnants.item.ModTooltipEvents;
import com.knoxhack.echoorbitalremnants.suit.SuitEvents;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(EchoOrbitalRemnants.MODID)
public class EchoOrbitalRemnants {
    public static final String MODID = "echoorbitalremnants";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoOrbitalRemnants(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModWorldgen.register(modEventBus);
        ModGameTests.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModEntities::registerAttributes);
        modEventBus.addListener(ModNetworking::registerPayloads);
        modEventBus.addListener(ModGameTests::registerTests);
        NeoForge.EVENT_BUS.register(new SuitEvents());
        NeoForge.EVENT_BUS.register(new ModTooltipEvents());

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ECHO-7 orbital systems initialized. Orbit is haunted above.");
        event.enqueueWork(() -> {
            AshfallCompat.registerAddonChapter();
            if (ModList.get().isLoaded("echoterminal")) {
                OrbitalTerminalCommonIntegration.register();
            }
        });
    }
}
