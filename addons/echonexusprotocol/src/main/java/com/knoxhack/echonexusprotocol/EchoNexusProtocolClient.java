package com.knoxhack.echonexusprotocol;

import com.knoxhack.echonexusprotocol.client.NexusMachineScreen;
import com.knoxhack.echonexusprotocol.client.TintedNexusZombieRenderer;
import com.knoxhack.echonexusprotocol.integration.NexusTerminalIntegration;
import com.knoxhack.echonexusprotocol.registry.ModEntities;
import com.knoxhack.echonexusprotocol.registry.ModMenus;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = EchoNexusProtocol.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EchoNexusProtocol.MODID, value = Dist.CLIENT)
public class EchoNexusProtocolClient {
   public EchoNexusProtocolClient() {
      registerTerminalClientIntegration();
   }

   private static void registerTerminalClientIntegration() {
      if (!ModList.get().isLoaded("echoterminal")) {
         return;
      }

      try {
         NexusTerminalIntegration.register();
      } catch (LinkageError error) {
         EchoNexusProtocol.LOGGER.warn("ECHO-7 Nexus Terminal client integration skipped because echoterminal APIs were unavailable.", error);
      }
   }

   @SubscribeEvent static void registerMenuScreens(RegisterMenuScreensEvent event) { event.register(ModMenus.NEXUS_MACHINE.get(), NexusMachineScreen::new); }
   @SubscribeEvent static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) { event.registerEntityRenderer(ModEntities.NEXUS_HUSK.get(), context -> new TintedNexusZombieRenderer(context, entityTexture("nexus_husk"), 0xFFD9A4FF, 1.0F, 0.55F)); event.registerEntityRenderer(ModEntities.DATA_WRAITH.get(), context -> new TintedNexusZombieRenderer(context, entityTexture("data_wraith"), 0xFF9FE8FF, 0.9F, 0.25F)); event.registerEntityRenderer(ModEntities.STATIC_CRAWLER.get(), context -> new TintedNexusZombieRenderer(context, entityTexture("static_crawler"), 0xFFB85CFF, 0.72F, 0.25F)); event.registerEntityRenderer(ModEntities.CORE_SOLDIER.get(), context -> new TintedNexusZombieRenderer(context, entityTexture("core_soldier"), 0xFF7C8EAA, 1.08F, 0.62F)); event.registerEntityRenderer(ModEntities.ARCHIVE_SEEKER.get(), context -> new TintedNexusZombieRenderer(context, entityTexture("archive_seeker"), 0xFFE8F8FF, 1.18F, 0.45F)); event.registerEntityRenderer(ModEntities.CORRUPTION_WARDEN.get(), context -> new TintedNexusZombieRenderer(context, entityTexture("corruption_warden"), 0xFFFF62D6, 1.35F, 0.95F)); event.registerEntityRenderer(ModEntities.NEXUS_GUARDIAN.get(), context -> new TintedNexusZombieRenderer(context, entityTexture("nexus_guardian"), 0xFF66E8FF, 1.65F, 1.1F)); }
   private static Identifier entityTexture(String name) { return Identifier.fromNamespaceAndPath(EchoNexusProtocol.MODID, "textures/entity/" + name + ".png"); }
}
