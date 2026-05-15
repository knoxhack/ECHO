package com.knoxhack.echoblackboxprotocol.integration;

import com.knoxhack.echoblackboxprotocol.EchoBlackboxProtocol;
import com.knoxhack.echoblackboxprotocol.registry.ModEntities;
import com.knoxhack.echocore.client.model.EchoMobFamily;
import com.knoxhack.echorendercore.client.EchoRenderCoreMobFamilyRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class BlackboxRenderCoreClientIntegration {
   private BlackboxRenderCoreClientIntegration() {
   }

   public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      event.registerEntityRenderer(ModEntities.ARCHIVE_HUSK.get(), renderer("archive_husk", EchoMobFamily.HUMANOID, 1.0F, 0.5F));
      event.registerEntityRenderer(ModEntities.SECURITY_ECHO.get(), renderer("security_echo", EchoMobFamily.HUMANOID, 1.03F, 0.52F));
      event.registerEntityRenderer(ModEntities.MEMORY_PARASITE.get(), renderer("memory_parasite", EchoMobFamily.CRAWLER, 0.72F, 0.28F));
      event.registerEntityRenderer(ModEntities.FALSE_ECHO_MINION.get(), renderer("false_echo_minion", EchoMobFamily.HUMANOID, 1.0F, 0.5F));
      event.registerEntityRenderer(ModEntities.COMMAND_REMNANT_MINION.get(), renderer("command_remnant_minion", EchoMobFamily.HUMANOID, 1.05F, 0.55F));
      event.registerEntityRenderer(ModEntities.BLACKBOX_SENTINEL.get(), renderer("blackbox_sentinel", EchoMobFamily.HEAVY_BOSS, 1.25F, 0.72F));
      event.registerEntityRenderer(ModEntities.FALSE_ECHO.get(), renderer("false_echo", EchoMobFamily.HEAVY_BOSS, 1.16F, 0.65F));
      event.registerEntityRenderer(ModEntities.COMMAND_REMNANT.get(), renderer("command_remnant", EchoMobFamily.HEAVY_BOSS, 1.25F, 0.75F));
      event.registerEntityRenderer(ModEntities.NEXUS_GUARDIAN.get(), renderer("nexus_guardian", EchoMobFamily.HEAVY_BOSS, 1.38F, 0.88F));
   }

   private static <T extends Mob> EntityRendererProvider<T> renderer(String entityName, EchoMobFamily family,
         float scale, float shadow) {
      return context -> new EchoRenderCoreMobFamilyRenderer<>(context, EchoBlackboxProtocol.MODID, entityName, family, scale, shadow);
   }
}
