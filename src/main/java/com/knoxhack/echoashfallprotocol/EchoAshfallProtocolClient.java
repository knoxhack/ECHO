package com.knoxhack.echoashfallprotocol;

import com.knoxhack.echoashfallprotocol.client.hud.HudState;
import com.knoxhack.echoashfallprotocol.client.EnvironmentalVisualController;
import com.knoxhack.echoashfallprotocol.client.hud.BossHudOverlay;
import com.knoxhack.echoashfallprotocol.client.hud.MutationOverlayEffect;
import com.knoxhack.echoashfallprotocol.client.hud.SurvivalHudOverlay;
import com.knoxhack.echoashfallprotocol.client.renderer.BiomeBossRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.AshWraithRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.CityStalkerRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.CrashSurvivorRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.BoardCrawlerModel;
import com.knoxhack.echoashfallprotocol.client.renderer.BoardHeavyBossModel;
import com.knoxhack.echoashfallprotocol.client.renderer.BoardHumanoidModel;
import com.knoxhack.echoashfallprotocol.client.renderer.BoardQuadrupedModel;
import com.knoxhack.echoashfallprotocol.client.renderer.BoardSlimeModel;
import com.knoxhack.echoashfallprotocol.client.renderer.BoardWraithModel;
import com.knoxhack.echoashfallprotocol.client.renderer.DroneModel;
import com.knoxhack.echoashfallprotocol.client.renderer.EchoCompanionDroneRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.EchoDroneRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.FeralHumanRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.FactionNpcRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.GlowingGhoulRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.GuardianBossModel;
import com.knoxhack.echoashfallprotocol.client.renderer.IrradiatedWolfRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.MutatedCrawlerRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.NexusFinalBossRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.NexusPressureMobRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.RadZombieRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.RustWalkerRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.ScavengerBanditRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.ScoutDroneRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.SteamWraithRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.ToxicSlimeRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.WardenBossModel;
import com.knoxhack.echoashfallprotocol.client.renderer.WardenBossRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.WastelandSentinelRenderer;
import com.knoxhack.echoashfallprotocol.client.renderer.WildDogRenderer;
import com.knoxhack.echoashfallprotocol.client.screen.CrystallineSynthesizerScreen;
import com.knoxhack.echoashfallprotocol.client.screen.DeepCoreMinerScreen;
import com.knoxhack.echoashfallprotocol.client.screen.EchoTerminalStyle;
import com.knoxhack.echoashfallprotocol.client.screen.FilterWorkbenchScreen;
import com.knoxhack.echoashfallprotocol.client.screen.HandRecyclerScreen;
import com.knoxhack.echoashfallprotocol.client.screen.IsotopeRefinerScreen;
import com.knoxhack.echoashfallprotocol.client.screen.MachineStatusScreen;
import com.knoxhack.echoashfallprotocol.client.screen.MicroGeneratorScreen;
import com.knoxhack.echoashfallprotocol.client.screen.OreGrinderScreen;
import com.knoxhack.echoashfallprotocol.client.screen.RadiationCleanserScreen;
import com.knoxhack.echoashfallprotocol.client.screen.ResearchLabScreen;
import com.knoxhack.echoashfallprotocol.client.screen.ScrapPressScreen;
import com.knoxhack.echoashfallprotocol.client.screen.ThermalArrayScreen;
import com.knoxhack.echoashfallprotocol.client.screen.ThermalBurnerScreen;
import com.knoxhack.echoashfallprotocol.client.screen.WaterPurifierScreen;
import com.knoxhack.echoashfallprotocol.client.screen.WelcomeScreen;
import com.knoxhack.echoashfallprotocol.echo.MissionUxSummary;
import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.entity.ModEntities;
import com.knoxhack.echoashfallprotocol.integration.AshfallTerminalIntegration;
import com.knoxhack.echoashfallprotocol.registry.ModMenuTypes;
import com.knoxhack.echoterminal.client.screen.EchoTerminalScreen;
import com.knoxhack.echoterminal.client.screen.EchoTerminalScreenProvider;
import com.knoxhack.echoterminal.client.screen.EchoTerminalScreens;
import com.knoxhack.echoterminal.client.screen.TerminalScreenTheme;
import com.knoxhack.echoterminal.menu.EchoTerminalMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side initialization for ECHO: ASHFALL PROTOCOL.
 */
@Mod(value = EchoAshfallProtocol.MODID, dist = Dist.CLIENT)
public class EchoAshfallProtocolClient {
    /** [N] opens the welcome screen. [V] cycles HUD mode. [M] is handled by ECHO Terminal. */
    private static final int KEY_N = GLFW.GLFW_KEY_N;
    private static final int KEY_V = GLFW.GLFW_KEY_V;

    public EchoAshfallProtocolClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.addListener(EchoAshfallProtocolClient::onRenderGui);
        NeoForge.EVENT_BUS.addListener(EchoAshfallProtocolClient::onBossBar);
        NeoForge.EVENT_BUS.addListener(EchoAshfallProtocolClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(EchoAshfallProtocolClient::onKeyInput);
        if (ModList.get().isLoaded("echoterminal")) {
            AshfallTerminalIntegration.registerClient();
            registerAshfallTerminalScreen();
        }
    }

    private static void registerAshfallTerminalScreen() {
        EchoTerminalScreens.registerPrimary(new EchoTerminalScreenProvider() {
            @Override
            public AbstractContainerScreen<EchoTerminalMenu> create(EchoTerminalMenu menu, Inventory playerInventory, Component title) {
                return new EchoTerminalScreen(menu, playerInventory, title, ashfallTerminalTheme());
            }

            @Override
            public boolean isTerminalScreen(Screen screen) {
                return screen instanceof EchoTerminalScreen;
            }
        });
    }

    private static TerminalScreenTheme ashfallTerminalTheme() {
        return new TerminalScreenTheme(
                "ECHO-7 ASHFALL TERMINAL",
                minecraft -> {
                    if (minecraft.player == null) {
                        return "LINK OFFLINE";
                    }
                    QuestData quest = QuestData.get(minecraft.player);
                    MissionUxSummary summary = MissionUxSummary.current(minecraft.player, quest);
                    return summary.missionId().isBlank() ? "PROTOCOL SYNC PENDING" : summary.shortTitle();
                },
                "M / ESC closes | arrows cycle tabs | up/down groups | wheel/page scrolls",
                0xEE050B10,
                0xE8050B10,
                0xD8061016,
                EchoTerminalStyle.CYAN,
                0xFF244352,
                EchoTerminalStyle.TEXT,
                EchoTerminalStyle.MUTED,
                1500,
                820);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        WelcomeScreen.openPendingIfReady();
        EnvironmentalVisualController.tick();
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        float dt = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        EnvironmentalVisualController.renderOverlay(event.getGuiGraphics());
        SurvivalHudOverlay.render(event.getGuiGraphics(), dt);
        MutationOverlayEffect.render(event.getGuiGraphics(), dt);
    }

    private static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        BossHudOverlay.onBossEvent(event);
    }

    private static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int key = event.getKey();
        if (key == KEY_N) {
            if (mc.screen == null) {
                WelcomeScreen.openNow();
            } else if (mc.screen instanceof WelcomeScreen) {
                mc.setScreen(null);
            }
        } else if (key == KEY_V && mc.screen == null) {
            HudState.cycleMode();
        }
    }

    @EventBusSubscriber(modid = EchoAshfallProtocol.MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
            EchoAshfallProtocol.LOGGER.info("ECHO: ASHFALL PROTOCOL - Client systems online");

            event.enqueueWork(() -> {
                // Biome grass tinting is handled automatically by the tinted_cross model parent.
            });
        }

        @SubscribeEvent
        static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(WardenBossModel.LAYER_LOCATION, WardenBossModel::createBodyLayer);
            event.registerLayerDefinition(GuardianBossModel.LAYER_LOCATION, GuardianBossModel::createBodyLayer);
            event.registerLayerDefinition(DroneModel.LAYER_LOCATION, DroneModel::createBodyLayer);
            event.registerLayerDefinition(BoardHumanoidModel.LAYER_LOCATION, BoardHumanoidModel::createBodyLayer);
            event.registerLayerDefinition(BoardQuadrupedModel.LAYER_LOCATION, BoardQuadrupedModel::createBodyLayer);
            event.registerLayerDefinition(BoardCrawlerModel.LAYER_LOCATION, BoardCrawlerModel::createBodyLayer);
            event.registerLayerDefinition(BoardWraithModel.LAYER_LOCATION, BoardWraithModel::createBodyLayer);
            event.registerLayerDefinition(BoardSlimeModel.LAYER_LOCATION, BoardSlimeModel::createBodyLayer);
            event.registerLayerDefinition(BoardHeavyBossModel.LAYER_LOCATION, BoardHeavyBossModel::createBodyLayer);
        }

        @SubscribeEvent
        static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.RAD_ZOMBIE.get(), RadZombieRenderer::new);
            event.registerEntityRenderer(ModEntities.SCAVENGER_BANDIT.get(), ScavengerBanditRenderer::new);
            event.registerEntityRenderer(ModEntities.IRRADIATED_WOLF.get(), IrradiatedWolfRenderer::new);
            event.registerEntityRenderer(ModEntities.ECHO_DRONE.get(), EchoDroneRenderer::new);
            event.registerEntityRenderer(ModEntities.SCOUT_DRONE.get(), ScoutDroneRenderer::new);
            event.registerEntityRenderer(ModEntities.GLOWING_GHOUL.get(), GlowingGhoulRenderer::new);
            event.registerEntityRenderer(ModEntities.ASH_WRAITH.get(), AshWraithRenderer::new);
            event.registerEntityRenderer(ModEntities.TOXIC_SLIME.get(), ToxicSlimeRenderer::new);
            event.registerEntityRenderer(ModEntities.GRIDBOUND_HUSK.get(), NexusPressureMobRenderer.humanoid("gridbound_husk"));
            event.registerEntityRenderer(ModEntities.RELAY_WARDEN.get(), NexusPressureMobRenderer.heavy("relay_warden"));
            event.registerEntityRenderer(ModEntities.SIGNAL_LEECH.get(), NexusPressureMobRenderer.crawler("signal_leech"));
            event.registerEntityRenderer(ModEntities.NEXUS_NULLIFIER.get(), NexusPressureMobRenderer.humanoid("nexus_nullifier"));
            event.registerEntityRenderer(ModEntities.CITY_STALKER.get(), CityStalkerRenderer::new);
            event.registerEntityRenderer(ModEntities.RUST_WALKER.get(), RustWalkerRenderer::new);
            event.registerEntityRenderer(ModEntities.STEAM_WRAITH.get(), SteamWraithRenderer::new);
            event.registerEntityRenderer(ModEntities.MUTATED_CRAWLER.get(), MutatedCrawlerRenderer::new);
            event.registerEntityRenderer(ModEntities.ECHO_COMPANION_DRONE.get(), EchoCompanionDroneRenderer::new);
            event.registerEntityRenderer(ModEntities.WILD_DOG.get(), WildDogRenderer::new);
            event.registerEntityRenderer(ModEntities.FERAL_HUMAN.get(), FeralHumanRenderer::new);
            event.registerEntityRenderer(ModEntities.CRASH_SURVIVOR.get(), CrashSurvivorRenderer::new);
            event.registerEntityRenderer(ModEntities.FACTION_NPC.get(), FactionNpcRenderer::new);
            event.registerEntityRenderer(ModEntities.WARDEN_BOSS.get(), WardenBossRenderer::new);
            event.registerEntityRenderer(ModEntities.WASTELAND_SENTINEL.get(), WastelandSentinelRenderer::new);
            event.registerEntityRenderer(ModEntities.CRASH_ZONE_COLOSSUS.get(), BiomeBossRenderer::new);
            event.registerEntityRenderer(ModEntities.CRYOGENIC_OVERSEER.get(), BiomeBossRenderer::new);
            event.registerEntityRenderer(ModEntities.INDUSTRIAL_JUGGERNAUT.get(), BiomeBossRenderer::new);
            event.registerEntityRenderer(ModEntities.NEXUS_SCAR_AVATAR.get(), BiomeBossRenderer::new);
            event.registerEntityRenderer(ModEntities.RADIATION_BEHEMOTH.get(), BiomeBossRenderer::new);
            event.registerEntityRenderer(ModEntities.CITY_RUIN_STALKER.get(), BiomeBossRenderer::new);
            event.registerEntityRenderer(ModEntities.PLAINS_WARLORD.get(), BiomeBossRenderer::new);
            event.registerEntityRenderer(ModEntities.TOXIC_HIVE_MATRIARCH.get(), BiomeBossRenderer::new);
            event.registerEntityRenderer(ModEntities.CORRUPTION_BLOOM.get(), NexusFinalBossRenderer::new);
            event.registerEntityRenderer(ModEntities.SEVERANCE_ENGINE.get(), NexusFinalBossRenderer::new);
            event.registerEntityRenderer(ModEntities.MIRROR_COMMAND.get(), NexusFinalBossRenderer::new);
        }

        @SubscribeEvent
        static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.RESEARCH_LAB.get(), ResearchLabScreen::new);
            event.register(ModMenuTypes.HAND_RECYCLER.get(), HandRecyclerScreen::new);
            event.register(ModMenuTypes.THERMAL_BURNER.get(), ThermalBurnerScreen::new);
            event.register(ModMenuTypes.WATER_PURIFIER.get(), WaterPurifierScreen::new);
            event.register(ModMenuTypes.MICRO_GENERATOR.get(), MicroGeneratorScreen::new);
            event.register(ModMenuTypes.FILTER_WORKBENCH.get(), FilterWorkbenchScreen::new);
            event.register(ModMenuTypes.SCRAP_PRESS.get(), ScrapPressScreen::new);
            event.register(ModMenuTypes.MACHINE_STATUS.get(), MachineStatusScreen::new);
            event.register(ModMenuTypes.THERMAL_ARRAY.get(), ThermalArrayScreen::new);
            event.register(ModMenuTypes.ORE_GRINDER.get(), OreGrinderScreen::new);
            event.register(ModMenuTypes.ISOTOPE_REFINER.get(), IsotopeRefinerScreen::new);
            event.register(ModMenuTypes.CRYSTALLINE_SYNTHESIZER.get(), CrystallineSynthesizerScreen::new);
            event.register(ModMenuTypes.DEEP_CORE_MINER.get(), DeepCoreMinerScreen::new);
            event.register(ModMenuTypes.RADIATION_CLEANSER.get(), RadiationCleanserScreen::new);
        }
    }
}
