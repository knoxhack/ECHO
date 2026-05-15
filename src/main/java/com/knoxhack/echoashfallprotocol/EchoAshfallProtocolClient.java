package com.knoxhack.echoashfallprotocol;

import com.knoxhack.echocore.client.model.EchoMobFamily;
import com.knoxhack.echocore.client.model.EchoMobFamilyRenderer;
import com.knoxhack.echoashfallprotocol.client.hud.HudState;
import com.knoxhack.echoashfallprotocol.client.EnvironmentalVisualController;
import com.knoxhack.echoashfallprotocol.client.hud.BossHudOverlay;
import com.knoxhack.echoashfallprotocol.client.hud.MutationOverlayEffect;
import com.knoxhack.echoashfallprotocol.client.hud.SurvivalHudOverlay;
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
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
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
            // Shared ECHO mob model layers are registered by echocore.
        }

        @SubscribeEvent
        static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            if (ModList.get().isLoaded("echorendercore") && registerRenderCoreEntityRenderers(event)) {
                return;
            }
            registerFallbackEntityRenderers(event);
        }

        private static void registerFallbackEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.RAD_ZOMBIE.get(), renderer("rad_zombie", EchoMobFamily.HUMANOID, 1.0F, 0.5F));
            event.registerEntityRenderer(ModEntities.SCAVENGER_BANDIT.get(), renderer("scavenger_bandit", EchoMobFamily.HUMANOID, 1.0F, 0.5F));
            event.registerEntityRenderer(ModEntities.IRRADIATED_WOLF.get(), renderer("irradiated_wolf", EchoMobFamily.QUADRUPED, 1.0F, 0.5F));
            event.registerEntityRenderer(ModEntities.ECHO_DRONE.get(), renderer("echo_drone", EchoMobFamily.DRONE, 1.0F, 0.4F));
            event.registerEntityRenderer(ModEntities.SCOUT_DRONE.get(), renderer("scout_drone", EchoMobFamily.DRONE, 1.0F, 0.4F));
            event.registerEntityRenderer(ModEntities.GLOWING_GHOUL.get(), renderer("glowing_ghoul", EchoMobFamily.HUMANOID, 1.0F, 0.5F));
            event.registerEntityRenderer(ModEntities.ASH_WRAITH.get(), renderer("ash_wraith", EchoMobFamily.WRAITH, 1.0F, 0.5F));
            event.registerEntityRenderer(ModEntities.TOXIC_SLIME.get(), renderer("toxic_slime", EchoMobFamily.SLIME, 1.0F, 0.35F));
            event.registerEntityRenderer(ModEntities.GRIDBOUND_HUSK.get(), renderer("gridbound_husk", EchoMobFamily.HUMANOID, 1.0F, 0.55F));
            event.registerEntityRenderer(ModEntities.RELAY_WARDEN.get(), renderer("relay_warden", EchoMobFamily.HEAVY_BOSS, 1.0F, 0.85F));
            event.registerEntityRenderer(ModEntities.SIGNAL_LEECH.get(), renderer("signal_leech", EchoMobFamily.CRAWLER, 1.0F, 0.35F));
            event.registerEntityRenderer(ModEntities.NEXUS_NULLIFIER.get(), renderer("nexus_nullifier", EchoMobFamily.HUMANOID, 1.0F, 0.55F));
            event.registerEntityRenderer(ModEntities.CITY_STALKER.get(), renderer("city_stalker", EchoMobFamily.HUMANOID, 1.0F, 0.5F));
            event.registerEntityRenderer(ModEntities.RUST_WALKER.get(), renderer("rust_walker", EchoMobFamily.HEAVY_BOSS, 1.0F, 0.7F));
            event.registerEntityRenderer(ModEntities.STEAM_WRAITH.get(), renderer("steam_wraith", EchoMobFamily.WRAITH, 1.0F, 0.4F));
            event.registerEntityRenderer(ModEntities.MUTATED_CRAWLER.get(), renderer("mutated_crawler", EchoMobFamily.CRAWLER, 1.0F, 0.3F));
            event.registerEntityRenderer(ModEntities.ECHO_COMPANION_DRONE.get(), renderer("echo_companion_drone", EchoMobFamily.DRONE, 1.0F, 0.4F));
            event.registerEntityRenderer(ModEntities.WILD_DOG.get(), renderer("wild_dog", EchoMobFamily.QUADRUPED, 1.0F, 0.45F));
            event.registerEntityRenderer(ModEntities.FERAL_HUMAN.get(), renderer("feral_human", EchoMobFamily.HUMANOID, 1.0F, 0.5F));
            event.registerEntityRenderer(ModEntities.CRASH_SURVIVOR.get(), renderer("crash_survivor", EchoMobFamily.SURVIVOR_NPC, 1.0F, 0.5F));
            event.registerEntityRenderer(ModEntities.FACTION_NPC.get(), renderer("faction_npc", EchoMobFamily.SURVIVOR_NPC, 1.0F, 0.5F));
            event.registerEntityRenderer(ModEntities.WARDEN_BOSS.get(), renderer("warden_boss", EchoMobFamily.HEAVY_BOSS, 1.0F, 1.0F));
            event.registerEntityRenderer(ModEntities.WASTELAND_SENTINEL.get(), renderer("wasteland_sentinel", EchoMobFamily.HEAVY_BOSS, 1.0F, 0.9F));
            event.registerEntityRenderer(ModEntities.CRASH_ZONE_COLOSSUS.get(), renderer("crash_zone_colossus", EchoMobFamily.HEAVY_BOSS, 1.24F, 1.12F));
            event.registerEntityRenderer(ModEntities.CRYOGENIC_OVERSEER.get(), renderer("cryogenic_overseer", EchoMobFamily.HEAVY_BOSS, 1.04F, 0.9F));
            event.registerEntityRenderer(ModEntities.INDUSTRIAL_JUGGERNAUT.get(), renderer("industrial_juggernaut", EchoMobFamily.HEAVY_BOSS, 1.16F, 1.04F));
            event.registerEntityRenderer(ModEntities.NEXUS_SCAR_AVATAR.get(), renderer("nexus_scar_avatar", EchoMobFamily.HEAVY_BOSS, 1.18F, 1.08F));
            event.registerEntityRenderer(ModEntities.RADIATION_BEHEMOTH.get(), renderer("radiation_behemoth", EchoMobFamily.HEAVY_BOSS, 1.12F, 1.0F));
            event.registerEntityRenderer(ModEntities.CITY_RUIN_STALKER.get(), renderer("city_ruin_stalker", EchoMobFamily.HEAVY_BOSS, 0.92F, 0.68F));
            event.registerEntityRenderer(ModEntities.PLAINS_WARLORD.get(), renderer("plains_warlord", EchoMobFamily.HEAVY_BOSS, 1.02F, 0.88F));
            event.registerEntityRenderer(ModEntities.TOXIC_HIVE_MATRIARCH.get(), renderer("toxic_hive_matriarch", EchoMobFamily.HEAVY_BOSS, 1.05F, 0.92F));
            event.registerEntityRenderer(ModEntities.CORRUPTION_BLOOM.get(), renderer("corruption_bloom", EchoMobFamily.HEAVY_BOSS, 1.04F, 0.86F));
            event.registerEntityRenderer(ModEntities.SEVERANCE_ENGINE.get(), renderer("severance_engine", EchoMobFamily.HEAVY_BOSS, 1.14F, 0.86F));
            event.registerEntityRenderer(ModEntities.MIRROR_COMMAND.get(), renderer("mirror_command", EchoMobFamily.HEAVY_BOSS, 1.08F, 0.86F));
        }

        private static boolean registerRenderCoreEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            try {
                Class.forName("com.knoxhack.echoashfallprotocol.integration.AshfallRenderCoreClientIntegration")
                        .getMethod("registerEntityRenderers", EntityRenderersEvent.RegisterRenderers.class)
                        .invoke(null, event);
                return true;
            } catch (ReflectiveOperationException | LinkageError exception) {
                EchoAshfallProtocol.LOGGER.warn("ECHO Ashfall RenderCore entity renderer integration unavailable; using generated fallback renderers.", exception);
                return false;
            }
        }

        private static <T extends Mob> EntityRendererProvider<T> renderer(String entityName, EchoMobFamily family,
                float scale, float shadow) {
            return context -> new EchoMobFamilyRenderer<>(context, EchoAshfallProtocol.MODID, entityName, family, scale, shadow);
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
