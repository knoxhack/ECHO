package com.knoxhack.echotutorialcore.data;

import com.knoxhack.echotutorialcore.EchoTutorialCore;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EchoTutorialCore.MODID);

    public static final Supplier<AttachmentType<TutorialPlayerData>> TUTORIAL_PLAYER_DATA =
            ATTACHMENT_TYPES.register(
                    "tutorial_player_data",
                    () -> AttachmentType.<TutorialPlayerData>serializable(TutorialPlayerData::new)
                            .sync((holder, player) -> holder == player, TutorialPlayerData.STREAM_CODEC)
                            .copyOnDeath()
                            .build());

    private ModAttachments() {}

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
