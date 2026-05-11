package com.knoxhack.echomissioncore.registry;

import com.knoxhack.echomissioncore.EchoMissionCore;
import com.knoxhack.echomissioncore.storage.MissionPlayerData;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EchoMissionCore.MODID);

    public static final Supplier<AttachmentType<MissionPlayerData>> MISSION_PLAYER_DATA =
            ATTACHMENT_TYPES.register("mission_player_data",
                    () -> AttachmentType.<MissionPlayerData>serializable(MissionPlayerData::new)
                            .sync((holder, player) -> holder == player, MissionPlayerData.STREAM_CODEC)
                            .copyOnDeath()
                            .build());

    private ModAttachments() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
