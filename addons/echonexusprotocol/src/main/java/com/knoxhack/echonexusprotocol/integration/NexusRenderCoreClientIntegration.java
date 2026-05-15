package com.knoxhack.echonexusprotocol.integration;

import com.knoxhack.echocore.client.model.EchoMobFamily;
import com.knoxhack.echorendercore.client.EchoRenderCoreMobFamilyRenderer;
import com.knoxhack.echonexusprotocol.EchoNexusProtocol;
import com.knoxhack.echonexusprotocol.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class NexusRenderCoreClientIntegration {
   private NexusRenderCoreClientIntegration() {
   }

   public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(ModEntities.NEXUS_HUSK.get(), renderer("nexus_husk", EchoMobFamily.HUMANOID, 1.0F, 0.55F));
      event.registerEntityRenderer(ModEntities.DATA_WRAITH.get(), renderer("data_wraith", EchoMobFamily.WRAITH, 0.9F, 0.25F));
      event.registerEntityRenderer(ModEntities.STATIC_CRAWLER.get(), renderer("static_crawler", EchoMobFamily.CRAWLER, 0.72F, 0.25F));
      event.registerEntityRenderer(ModEntities.CORE_SOLDIER.get(), renderer("core_soldier", EchoMobFamily.HUMANOID, 1.08F, 0.62F));
      event.registerEntityRenderer(ModEntities.ARCHIVE_SEEKER.get(), renderer("archive_seeker", EchoMobFamily.HUMANOID, 1.18F, 0.45F));
      event.registerEntityRenderer(ModEntities.CORRUPTION_WARDEN.get(), renderer("corruption_warden", EchoMobFamily.HEAVY_BOSS, 1.35F, 0.95F));
      event.registerEntityRenderer(ModEntities.NEXUS_GUARDIAN.get(), renderer("nexus_guardian", EchoMobFamily.HEAVY_BOSS, 1.65F, 1.1F));
   }

   private static <T extends Mob> EntityRendererProvider<T> renderer(String entityName, EchoMobFamily family,
         float scale, float shadow) {
      return context -> new EchoRenderCoreMobFamilyRenderer<>(context, EchoNexusProtocol.MODID, entityName, family, scale, shadow);
   }
}
