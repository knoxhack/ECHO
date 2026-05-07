package com.knoxhack.echonexusprotocol.registry;

import com.knoxhack.echonexusprotocol.data.NexusPlayerData;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
   private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "echonexusprotocol");
   public static final Supplier<AttachmentType<NexusPlayerData>> NEXUS_PLAYER_DATA = ATTACHMENTS.register(
      "nexus_player_data",
      () -> AttachmentType.serializable(NexusPlayerData::new).sync((holder, player) -> holder == player, NexusPlayerData.STREAM_CODEC).copyOnDeath().build()
   );

   private ModAttachments() {
   }

   public static void register(IEventBus eventBus) {
      ATTACHMENTS.register(eventBus);
   }
}
