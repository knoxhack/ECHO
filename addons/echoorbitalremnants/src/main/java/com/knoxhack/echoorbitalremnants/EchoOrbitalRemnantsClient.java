package com.knoxhack.echoorbitalremnants;

import com.knoxhack.echoorbitalremnants.suit.SuitEvents;
import com.knoxhack.echoorbitalremnants.suit.SuitState;
import com.knoxhack.echoorbitalremnants.client.EchoTerminalScreen;
import com.knoxhack.echoorbitalremnants.client.EmergencyRocketModel;
import com.knoxhack.echoorbitalremnants.client.EmergencyRocketRenderer;
import com.knoxhack.echoorbitalremnants.client.OrbitalMachineScreen;
import com.knoxhack.echoorbitalremnants.integration.OrbitalTerminalIntegration;
import com.knoxhack.echoorbitalremnants.network.OpenEchoTerminalPayload;
import com.knoxhack.echoorbitalremnants.network.OrbitalEventVisualPayload;
import com.knoxhack.echoorbitalremnants.registry.ModEntities;
import com.knoxhack.echoorbitalremnants.registry.ModMenus;
import com.knoxhack.echoorbitalremnants.client.TintedVexRenderer;
import com.knoxhack.echoorbitalremnants.client.TintedZombieRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@Mod(value = EchoOrbitalRemnants.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EchoOrbitalRemnants.MODID, value = Dist.CLIENT)
public class EchoOrbitalRemnantsClient {
    private static final Identifier ORBITAL_HUD = Identifier.fromNamespaceAndPath(EchoOrbitalRemnants.MODID, "orbital_hud");
    private static final String BASE_VISUAL_CONTROLLER = "com.knoxhack.echoashfallprotocol.client.EnvironmentalVisualController";
    private static final String BASE_VISUAL_PULSE_METHOD = "triggerOrbitalPulse";
    private static final String BASE_CONFIG_CLASS = "com.knoxhack.echoashfallprotocol.Config";
    private static final String BASE_ORBITAL_VISUALS_FLAG = "ORBITAL_EVENT_VISUALS";
    private static int eventVisualTicks = 0;
    private static String eventVisualName = "";

    public EchoOrbitalRemnantsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        if (ModList.get().isLoaded("echoterminal")) {
            OrbitalTerminalIntegration.register();
        }
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL, ORBITAL_HUD, EchoOrbitalRemnantsClient::renderOrbitalHud);
    }

    @SubscribeEvent
    static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ORBITAL_MACHINE.get(), OrbitalMachineScreen::new);
    }

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EmergencyRocketModel.LAYER_LOCATION, EmergencyRocketModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(OpenEchoTerminalPayload.TYPE, (payload, context) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof EchoTerminalScreen screen) {
                screen.updateSnapshot(payload.snapshot());
            } else {
                minecraft.setScreen(new EchoTerminalScreen(payload.snapshot()));
            }
        });
        event.register(OrbitalEventVisualPayload.TYPE, (payload, context) -> {
            eventVisualTicks = 120;
            eventVisualName = payload.eventName().replace('_', ' ');
            triggerBaseOrbitalPulse(payload.overlayColor(), payload.particleColor(), payload.intensity(), payload.seed());
        });
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (eventVisualTicks <= 0) {
            return;
        }
        eventVisualTicks--;
    }

    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.EMERGENCY_ROCKET_VEHICLE.get(), EmergencyRocketRenderer::new);
        event.registerEntityRenderer(ModEntities.ECHO_DEFENSE_DRONE.get(),
                context -> new TintedVexRenderer(context, entityTexture("echo_defense_drone"), 0xFF82E9FF, 1.0F, 0.34F));
        event.registerEntityRenderer(ModEntities.VACUUM_WRAITH.get(),
                context -> new TintedVexRenderer(context, entityTexture("vacuum_wraith"), 0xFFD8E2FF, 1.15F, 0.25F));
        event.registerEntityRenderer(ModEntities.CORRUPTED_DOCKING_AI.get(),
                context -> new TintedVexRenderer(context, entityTexture("corrupted_docking_ai"), 0xFFFF6868, 1.35F, 0.44F));
        event.registerEntityRenderer(ModEntities.BROKEN_ASTRONAUT.get(),
                context -> new TintedZombieRenderer(context, entityTexture("broken_astronaut"), 0xFFBFD0D6, 1.0F, 0.52F));
        event.registerEntityRenderer(ModEntities.NEXUS_HUSK.get(),
                context -> new TintedZombieRenderer(context, entityTexture("nexus_husk"), 0xFFD48BFF, 1.05F, 0.56F));
        event.registerEntityRenderer(ModEntities.LUNAR_NEXUS_HUSK.get(),
                context -> new TintedZombieRenderer(context, entityTexture("lunar_nexus_husk"), 0xFFE09CFF, 1.22F, 0.68F));
        event.registerEntityRenderer(ModEntities.ABANDONED_CAPTAIN.get(),
                context -> new TintedZombieRenderer(context, entityTexture("abandoned_captain"), 0xFF6D7B88, 1.18F, 0.72F));
        event.registerEntityRenderer(ModEntities.ECHO_ZERO.get(),
                context -> new TintedZombieRenderer(context, entityTexture("echo_zero"), 0xFFFF5AF7, 1.35F, 0.9F));
        event.registerEntityRenderer(ModEntities.EUROPA_CRYO_WARDEN.get(),
                context -> new TintedVexRenderer(context, entityTexture("europa_cryo_warden"), 0xFF7FE8FF, 1.45F, 0.58F));
        event.registerEntityRenderer(ModEntities.SATURN_RELAY_SENTINEL.get(),
                context -> new TintedVexRenderer(context, entityTexture("saturn_relay_sentinel"), 0xFFFFE2B8, 1.55F, 0.6F));
        event.registerEntityRenderer(ModEntities.TITAN_METHANE_STALKER.get(),
                context -> new TintedZombieRenderer(context, entityTexture("titan_methane_stalker"), 0xFFE58A45, 1.18F, 0.66F));
    }

    private static Identifier entityTexture(String name) {
        return Identifier.fromNamespaceAndPath(EchoOrbitalRemnants.MODID, "textures/entity/" + name + ".png");
    }

    private static void triggerBaseOrbitalPulse(int overlayColor, int particleColor, float intensity, long seed) {
        if (!baseOrbitalVisualsEnabled()) {
            return;
        }
        try {
            Class<?> controller = Class.forName(BASE_VISUAL_CONTROLLER);
            controller.getMethod(BASE_VISUAL_PULSE_METHOD, int.class, int.class, float.class, long.class)
                    .invoke(null, overlayColor, particleColor, intensity, seed);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // The base mod owns the cohesive overlay. If it is unavailable, keep the Orbital HUD label only.
        }
    }

    private static boolean baseOrbitalVisualsEnabled() {
        try {
            Object configValue = Class.forName(BASE_CONFIG_CLASS).getField(BASE_ORBITAL_VISUALS_FLAG).get(null);
            Object enabled = configValue.getClass().getMethod("get").invoke(configValue);
            return !(enabled instanceof Boolean value) || value;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return true;
        }
    }

    private static void renderOrbitalHud(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || !SuitEvents.isOrbitalExposure(minecraft.player)) {
            return;
        }

        SuitState suit = SuitState.get(minecraft.player);
        Font font = minecraft.font;
        int x = 8;
        int y = 8;
        int width = 154;
        int height = 74;
        graphics.fill(x - 4, y - 4, x + width, y + height, 0xAA061014);
        graphics.fill(x - 4, y - 4, x + width, y - 2, 0xCC56D6FF);
        graphics.text(font, Component.literal("ECHO-7 ORBITAL STATUS"), x, y, 0x66E8FF, true);
        graphics.text(font, Component.literal("OXYGEN: " + suit.oxygen() + "%"), x, y + 11, colorFor(suit.oxygen()), true);
        graphics.text(font, Component.literal("PRESSURE: " + pressureLabel(suit)), x, y + 22, colorFor(suit.pressure()), true);
        graphics.text(font, Component.literal("RADIATION: " + radiationLabel(suit.radiation())), x, y + 33, radiationColor(suit.radiation()), true);
        graphics.text(font, Component.literal("GRAVITY: " + String.format(java.util.Locale.ROOT, "%.2fG", suit.gravity())), x, y + 44, 0xD9F7FF, true);
        graphics.text(font, Component.literal("STATION POWER: " + suit.stationPower() + "%"), x, y + 55, 0xD9F7FF, true);
        if (eventVisualTicks > 0) {
            graphics.text(font, Component.literal(eventVisualName), x, y + 66, 0xFFE09CFF, true);
        }
    }

    private static String pressureLabel(SuitState suit) {
        if (!suit.helmetSealSecure()) {
            return "COMPROMISED";
        }
        if (suit.suitLeak()) {
            return "LEAK";
        }
        return "STABLE";
    }

    private static String radiationLabel(int radiation) {
        if (radiation >= 75) {
            return "EXTREME";
        }
        if (radiation >= 45) {
            return "HIGH";
        }
        return "ELEVATED";
    }

    private static int colorFor(int value) {
        if (value <= 20) {
            return 0xFF6B6B;
        }
        if (value <= 45) {
            return 0xFFD166;
        }
        return 0xA8F7C5;
    }

    private static int radiationColor(int radiation) {
        if (radiation >= 75) {
            return 0xE099FF;
        }
        if (radiation >= 45) {
            return 0xFFD166;
        }
        return 0xD9F7FF;
    }
}
