package com.knoxhack.echostationfall;
import com.knoxhack.echostationfall.client.TintedVexRenderer;
import com.knoxhack.echostationfall.client.TintedZombieRenderer;
import com.knoxhack.echostationfall.integration.StationfallTerminalIntegration;
import com.knoxhack.echostationfall.registry.ModEntities;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
@Mod(value = EchoStationfall.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EchoStationfall.MODID, value = Dist.CLIENT)
public final class EchoStationfallClient {
    private static final Identifier ZOMBIE_TEXTURE = Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png");
    private static final Identifier VEX_TEXTURE = Identifier.withDefaultNamespace("textures/entity/illager/vex.png");

    public EchoStationfallClient() {
        if (ModList.get().isLoaded("echoterminal")) {
            StationfallTerminalIntegration.register();
        }
    }

    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HOLLOW_CREWMAN.get(),
                context -> new TintedZombieRenderer(context, ZOMBIE_TEXTURE, 0xFFB8C4D2, 1.0F, 0.5F));
        event.registerEntityRenderer(ModEntities.EVA_STALKER.get(),
                context -> new TintedZombieRenderer(context, ZOMBIE_TEXTURE, 0xFF8DB5C8, 1.08F, 0.56F));
        event.registerEntityRenderer(ModEntities.MEDICAL_HUSK.get(),
                context -> new TintedZombieRenderer(context, ZOMBIE_TEXTURE, 0xFFE4D8C8, 1.0F, 0.48F));
        event.registerEntityRenderer(ModEntities.HYDROPONIC_GROWTH.get(),
                context -> new TintedZombieRenderer(context, ZOMBIE_TEXTURE, 0xFF7EBA75, 0.86F, 0.42F));
        event.registerEntityRenderer(ModEntities.MAINTENANCE_DRONE.get(),
                context -> new TintedVexRenderer(context, VEX_TEXTURE, 0xFF8EDCFF, 0.86F, 0.32F));
        event.registerEntityRenderer(ModEntities.SCREAMING_SIGNAL.get(),
                context -> new TintedVexRenderer(context, VEX_TEXTURE, 0xFFFF5D87, 0.9F, 0.25F));
        event.registerEntityRenderer(ModEntities.STATION_MIMIC.get(),
                context -> new TintedZombieRenderer(context, ZOMBIE_TEXTURE, 0xFF5E6670, 0.92F, 0.44F));
        event.registerEntityRenderer(ModEntities.SUIT_WITHOUT_BODY.get(),
                context -> new TintedZombieRenderer(context, ZOMBIE_TEXTURE, 0xFFDCEBFF, 1.12F, 0.58F));
        event.registerEntityRenderer(ModEntities.STATION_MOTHER.get(),
                context -> new TintedZombieRenderer(context, ZOMBIE_TEXTURE, 0xFFFF6A7B, 1.35F, 0.8F));
    }
}
