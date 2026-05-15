package com.knoxhack.echostationfall;
import com.knoxhack.echocore.client.model.EchoMobFamily;
import com.knoxhack.echocore.client.model.EchoMobFamilyRenderer;
import com.knoxhack.echostationfall.integration.StationfallTerminalIntegration;
import com.knoxhack.echostationfall.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@Mod(value = EchoStationfall.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EchoStationfall.MODID, value = Dist.CLIENT)
public final class EchoStationfallClient {
    public EchoStationfallClient() {
        if (ModList.get().isLoaded("echoterminal")) {
            StationfallTerminalIntegration.register();
        }
    }

    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        if (ModList.get().isLoaded("echorendercore") && registerRenderCoreEntityRenderers(event)) {
            return;
        }
        registerFallbackEntityRenderers(event);
    }

    private static void registerFallbackEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HOLLOW_CREWMAN.get(),
                renderer("hollow_crewman", EchoMobFamily.STATION_SUIT, 1.0F, 0.5F));
        event.registerEntityRenderer(ModEntities.EVA_STALKER.get(),
                renderer("eva_stalker", EchoMobFamily.STATION_SUIT, 1.08F, 0.56F));
        event.registerEntityRenderer(ModEntities.MEDICAL_HUSK.get(),
                renderer("medical_husk", EchoMobFamily.STATION_SUIT, 1.0F, 0.48F));
        event.registerEntityRenderer(ModEntities.HYDROPONIC_GROWTH.get(),
                renderer("hydroponic_growth", EchoMobFamily.HUMANOID, 0.86F, 0.42F));
        event.registerEntityRenderer(ModEntities.MAINTENANCE_DRONE.get(),
                renderer("maintenance_drone", EchoMobFamily.DRONE, 0.86F, 0.32F));
        event.registerEntityRenderer(ModEntities.SCREAMING_SIGNAL.get(),
                renderer("screaming_signal", EchoMobFamily.WRAITH, 0.9F, 0.25F));
        event.registerEntityRenderer(ModEntities.STATION_MIMIC.get(),
                renderer("station_mimic", EchoMobFamily.HUMANOID, 0.92F, 0.44F));
        event.registerEntityRenderer(ModEntities.SUIT_WITHOUT_BODY.get(),
                renderer("suit_without_body", EchoMobFamily.STATION_SUIT, 1.12F, 0.58F));
        event.registerEntityRenderer(ModEntities.STATION_MOTHER.get(),
                renderer("station_mother", EchoMobFamily.HEAVY_BOSS, 1.35F, 0.8F));
    }

    private static boolean registerRenderCoreEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        try {
            Class.forName("com.knoxhack.echostationfall.integration.StationfallRenderCoreClientIntegration")
                    .getMethod("registerEntityRenderers", EntityRenderersEvent.RegisterRenderers.class)
                    .invoke(null, event);
            return true;
        } catch (ReflectiveOperationException | LinkageError exception) {
            EchoStationfall.LOGGER.warn("ECHO Stationfall RenderCore entity renderer integration unavailable; using generated fallback renderers.", exception);
            return false;
        }
    }

    private static <T extends Mob> EntityRendererProvider<T> renderer(String entityName, EchoMobFamily family,
            float scale, float shadow) {
        return context -> new EchoMobFamilyRenderer<>(context, EchoStationfall.MODID, entityName, family, scale, shadow);
    }
}
