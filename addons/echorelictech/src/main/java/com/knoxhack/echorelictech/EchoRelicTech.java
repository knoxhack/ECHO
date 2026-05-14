package com.knoxhack.echorelictech;

import com.knoxhack.echorelictech.command.RelicTechCommands;
import com.knoxhack.echorelictech.config.RelicTechConfig;
import com.knoxhack.echorelictech.data.RelicDefinitionLoader;
import com.knoxhack.echorelictech.data.RelicFailureLoader;
import com.knoxhack.echorelictech.data.RelicVaultLoader;
import com.knoxhack.echorelictech.integration.RelicTechIntegrations;
import com.knoxhack.echorelictech.registry.*;
import com.knoxhack.echorelictech.server.RelicInstabilityManager;
import com.knoxhack.echorelictech.test.RelicTechGameTests;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod("echorelictech")
public class EchoRelicTech {
    public static final String MODID = "echorelictech";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EchoRelicTech(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        modContainer.registerConfig(ModConfig.Type.COMMON, RelicTechConfig.SPEC);
        RelicTechGameTests.register(modEventBus);
        modEventBus.addListener(RelicTechGameTests::registerTests);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ECHO: RelicTech online. Powerful enough to be exciting. Dangerous enough to respect.");
        event.enqueueWork(RelicTechIntegrations::registerOptional);
        registerInternalEventHooks();
    }

    private void registerInternalEventHooks() {
        com.knoxhack.echorelictech.api.event.RelicTechEvents.onAnalyze((e) -> {
            com.knoxhack.echorelictech.api.RelicTechApi.recordAnalyzedRelic(e.player());
        });
        com.knoxhack.echorelictech.api.event.RelicTechEvents.onUse((e) -> {
            com.knoxhack.echorelictech.api.RelicTechApi.recordRelicUse(e.player(), e.relicId());
        });
        com.knoxhack.echorelictech.api.event.RelicTechEvents.onVaultDiscover((e) -> {
            com.knoxhack.echorelictech.api.RelicTechApi.recordVaultDiscovery(e.player(), e.vaultId(), e.pos());
        });
    }

    private void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(MODID, "relic_definitions"), new RelicDefinitionLoader());
        event.addListener(Identifier.fromNamespaceAndPath(MODID, "relic_failures"), new RelicFailureLoader());
        event.addListener(Identifier.fromNamespaceAndPath(MODID, "relic_vaults"), new RelicVaultLoader());
    }

    private void onServerTick(ServerTickEvent.Post event) {
        RelicInstabilityManager.tickDecay(event.getServer().overworld());
        tickRelicCooldowns(event);
        com.knoxhack.echorelictech.item.EchoMirrorDecoyTracker.tick(event.getServer().overworld());
    }

    private void tickRelicCooldowns(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.has(ModDataComponents.RELIC_DATA.get())) {
                    var data = stack.get(ModDataComponents.RELIC_DATA.get());
                    if (data != null && data.cooldownRemaining() > 0) {
                        stack.set(ModDataComponents.RELIC_DATA.get(), data.withCooldown(data.cooldownRemaining() - 1));
                    }
                }
            }
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        RelicTechCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }
}
