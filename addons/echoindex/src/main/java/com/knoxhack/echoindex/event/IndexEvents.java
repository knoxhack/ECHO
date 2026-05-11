package com.knoxhack.echoindex.event;

import com.knoxhack.echoindex.EchoIndex;
import com.knoxhack.echoindex.IndexIds;
import com.knoxhack.echoindex.network.IndexSync;
import com.knoxhack.echoindex.service.IndexDiscoveryStore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = EchoIndex.MODID)
public final class IndexEvents {
    private static final String ROOT = "echoindex_state";

    private IndexEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            IndexDiscoveryStore.INSTANCE.discover(player, IndexIds.ENTRY_OVERVIEW);
            IndexSync.send(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag original = event.getOriginal().getPersistentData().getCompoundOrEmpty(ROOT);
        if (!original.isEmpty()) {
            event.getEntity().getPersistentData().put(ROOT, original.copy());
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            IndexDiscoveryStore.INSTANCE.discover(player, IndexIds.ENTRY_OVERVIEW);
        }
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        if (event.getPlayer() instanceof ServerPlayer player && !event.getOriginalStack().isEmpty()) {
            IndexDiscoveryStore.INSTANCE.discover(player, IndexIds.ENTRY_OVERVIEW);
        }
    }
}
