package com.knoxhack.echoterminal.registry;

import com.knoxhack.echoterminal.EchoTerminal;
import com.knoxhack.echoterminal.mission.VanillaJourneyData;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EchoTerminal.MODID);

    public static final Supplier<AttachmentType<VanillaJourneyData>> VANILLA_JOURNEY_DATA =
            ATTACHMENT_TYPES.register(
                    "vanilla_journey_data",
                    () -> AttachmentType.<VanillaJourneyData>serializable(VanillaJourneyData::new)
                            .sync((holder, player) -> holder == player, VanillaJourneyData.STREAM_CODEC)
                            .copyOnDeath()
                            .build());

    private ModAttachments() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
