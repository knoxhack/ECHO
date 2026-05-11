package com.knoxhack.echoashfallprotocol.echo;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.mission.MissionObjectiveType;
import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/**
 * Feeds placed-block counts into {@link QuestData} so block-placement mission
 * requirements ({@link Mission.BlockRequirement}) can actually be evaluated.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public final class MissionBlockPlaceTracker {
    private MissionBlockPlaceTracker() {}

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var key = BuiltInRegistries.BLOCK.getKey(event.getPlacedBlock().getBlock());
        if (key == null) return;

        QuestData data = player.getData(ModAttachments.QUEST_DATA.get());
        data.recordBlockPlacement(key.toString());
        EchoCoreServices.recordMissionObjective(
                player,
                MissionObjectiveType.PLACE_BLOCK,
                key,
                1,
                Map.of("source", EchoAshfallProtocol.MODID));
        player.setData(ModAttachments.QUEST_DATA.get(), data);
        MissionRegistry.invalidateBlockProbeCache(player);
    }

    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            MissionRegistry.invalidateBlockProbeCache(player);
        }
    }
}
