package com.knoxhack.echoruntimeguard;

import com.knoxhack.echocore.api.EchoAddonChapter;
import com.knoxhack.echocore.api.EchoAddonRegistry;
import com.knoxhack.echocore.api.EchoServiceRegistry;
import com.knoxhack.echoruntimeguard.command.RuntimeGuardCommands;
import com.knoxhack.echoruntimeguard.registry.ModGameTests;
import com.knoxhack.echoruntimeguard.runtime.BlockEntitySleepService;
import com.knoxhack.echoruntimeguard.runtime.EntityAiGuardService;
import com.knoxhack.echoruntimeguard.runtime.IntegrationThrottleService;
import com.knoxhack.echoruntimeguard.runtime.MultiblockValidationScheduler;
import com.knoxhack.echoruntimeguard.runtime.NetworkBudgetService;
import com.knoxhack.echoruntimeguard.runtime.ParticleBudgetService;
import com.knoxhack.echoruntimeguard.runtime.PerformanceBudgetService;
import com.knoxhack.echoruntimeguard.runtime.RuntimeModeService;
import com.knoxhack.echoruntimeguard.runtime.RuntimeProfilerService;
import com.knoxhack.echoruntimeguard.runtime.SmartTickService;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod(EchoRuntimeGuard.MODID)
public final class EchoRuntimeGuard {
    public static final String MODID = "echoruntimeguard";
    public static final String CHAPTER_ID = "runtimeguard";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoRuntimeGuard(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, RuntimeGuardConfig.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, RuntimeGuardConfig.CLIENT_SPEC);
        RuntimeGuardConfig.registerEchoConfig();

        ModGameTests.register(modEventBus);
        modEventBus.addListener(ModGameTests::registerTests);
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.addListener(RuntimeProfilerService.INSTANCE::onServerTickPre);
        NeoForge.EVENT_BUS.addListener(RuntimeProfilerService.INSTANCE::onServerTickPost);
        NeoForge.EVENT_BUS.addListener(MultiblockValidationScheduler.INSTANCE::onServerTick);
        NeoForge.EVENT_BUS.addListener(NetworkBudgetService.INSTANCE::onServerTick);
        NeoForge.EVENT_BUS.addListener(RuntimeGuardCommands::register);
        NeoForge.EVENT_BUS.addListener(EchoRuntimeGuard::resetTickBudgets);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private static void resetTickBudgets(ServerTickEvent.Post event) {
        ParticleBudgetService.INSTANCE.beginTick();
        PerformanceBudgetService.INSTANCE.resetTick();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerServices();
            registerAddonChapter();
        });
        LOGGER.info("ECHO RuntimeGuard online. Find the lag. Protect the signal. Restore performance.");
    }

    private static void registerServices() {
        EchoServiceRegistry.register(RuntimeModeService.class, RuntimeModeService.INSTANCE);
        EchoServiceRegistry.register(RuntimeProfilerService.class, RuntimeProfilerService.INSTANCE);
        EchoServiceRegistry.register(PerformanceBudgetService.class, PerformanceBudgetService.INSTANCE);
        EchoServiceRegistry.register(SmartTickService.class, SmartTickService.INSTANCE);
        EchoServiceRegistry.register(BlockEntitySleepService.class, BlockEntitySleepService.INSTANCE);
        EchoServiceRegistry.register(ParticleBudgetService.class, ParticleBudgetService.INSTANCE);
        EchoServiceRegistry.register(MultiblockValidationScheduler.class, MultiblockValidationScheduler.INSTANCE);
        EchoServiceRegistry.register(NetworkBudgetService.class, NetworkBudgetService.INSTANCE);
        EchoServiceRegistry.register(IntegrationThrottleService.class, IntegrationThrottleService.INSTANCE);
        EchoServiceRegistry.register(EntityAiGuardService.class, EntityAiGuardService.INSTANCE);
    }

    private static void registerAddonChapter() {
        if (EchoAddonRegistry.isRegistered(CHAPTER_ID)) {
            return;
        }
        EchoAddonRegistry.register(new EchoAddonChapter() {
            @Override
            public String id() {
                return CHAPTER_ID;
            }

            @Override
            public String modId() {
                return MODID;
            }

            @Override
            public String displayName() {
                return "ECHO RuntimeGuard";
            }

            @Override
            public String summary() {
                return "Shared performance optimization, diagnostics, smart ticking, and runtime protection.";
            }

            @Override
            public String statusLine(Player player) {
                return "RuntimeGuard: " + RuntimeModeService.INSTANCE.summary()
                        + ", TPS " + String.format(java.util.Locale.ROOT, "%.1f",
                        RuntimeProfilerService.INSTANCE.lastSnapshot().averageTps())
                        + ", MSPT " + Math.round(RuntimeProfilerService.INSTANCE.lastSnapshot().averageMspt()) + "ms.";
            }
        });
    }
}
