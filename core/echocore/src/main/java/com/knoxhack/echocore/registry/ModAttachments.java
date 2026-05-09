package com.knoxhack.echocore.registry;

import com.knoxhack.echocore.EchoCore;
import com.knoxhack.echocore.discovery.EchoDiscoveryData;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EchoCore.MODID);

    public static final Supplier<AttachmentType<EchoDiscoveryData>> DISCOVERY_DATA = ATTACHMENTS.register(
            "discovery_data",
            () -> AttachmentType.<EchoDiscoveryData>serializable(EchoDiscoveryData::new)
                    .sync((holder, player) -> holder == player, EchoDiscoveryData.STREAM_CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {
    }

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}
