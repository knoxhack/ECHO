package com.knoxhack.echonexusprotocol.integration;

import com.knoxhack.echonexusprotocol.EchoNexusProtocol;
import com.knoxhack.echonexusprotocol.client.NexusRenderCoreZombieRenderer;
import com.knoxhack.echonexusprotocol.registry.ModEntities;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class NexusRenderCoreClientIntegration {
   private NexusRenderCoreClientIntegration() {
   }

   public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
      register(event, ModEntities.NEXUS_HUSK.get(), "nexus_husk", 0xFFD9A4FF, 1.0F, 0.55F);
      register(event, ModEntities.DATA_WRAITH.get(), "data_wraith", 0xFF9FE8FF, 0.9F, 0.25F);
      register(event, ModEntities.STATIC_CRAWLER.get(), "static_crawler", 0xFFB85CFF, 0.72F, 0.25F);
      register(event, ModEntities.CORE_SOLDIER.get(), "core_soldier", 0xFF7C8EAA, 1.08F, 0.62F);
      register(event, ModEntities.ARCHIVE_SEEKER.get(), "archive_seeker", 0xFFE8F8FF, 1.18F, 0.45F);
      register(event, ModEntities.CORRUPTION_WARDEN.get(), "corruption_warden", 0xFFFF62D6, 1.35F, 0.95F);
      register(event, ModEntities.NEXUS_GUARDIAN.get(), "nexus_guardian", 0xFF66E8FF, 1.65F, 1.1F);
   }

   private static void register(EntityRenderersEvent.RegisterRenderers event,
         net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.monster.zombie.Zombie> type,
         String name, int tint, float scale, float shadow) {
      event.registerEntityRenderer(type, context -> new NexusRenderCoreZombieRenderer(
         context,
         entityTexture(name),
         Identifier.fromNamespaceAndPath(EchoNexusProtocol.MODID, "echo_mobs/" + name),
         tint,
         scale,
         shadow
      ));
   }

   private static Identifier entityTexture(String name) {
      return Identifier.fromNamespaceAndPath(EchoNexusProtocol.MODID, "textures/entity/" + name + ".png");
   }
}
