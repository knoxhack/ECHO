package com.knoxhack.echorendercore.client;

import com.knoxhack.echorendercore.EchoRenderCore;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = EchoRenderCore.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EchoRenderCore.MODID, value = Dist.CLIENT)
public final class EchoRenderCoreClient {
   public EchoRenderCoreClient() {
      NeoForge.EVENT_BUS.addListener(RenderCoreClientCommands::register);
      NeoForge.EVENT_BUS.addListener(RenderCoreDebugHud::render);
      NeoForge.EVENT_BUS.addListener(RenderCoreAnchorDebugRenderer::render);
   }

   @SubscribeEvent
   static void addClientReloadListeners(AddClientReloadListenersEvent event) {
      event.addListener(
         Identifier.fromNamespaceAndPath(EchoRenderCore.MODID, "profiles"),
         new RenderCoreClientReloadListener()
      );
   }
}
