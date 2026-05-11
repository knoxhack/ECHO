package com.knoxhack.echonetcore.api;

import com.knoxhack.echonetcore.network.EchoSyncPayload;
import com.knoxhack.echonetcore.network.EchoSyncType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;

public final class EchoClientSyncRegistry {
    private static final Map<Key, List<Consumer<EchoSyncPayload>>> CONSUMERS = new ConcurrentHashMap<>();

    private EchoClientSyncRegistry() {
    }

    public static void register(EchoSyncType type, Identifier channelId, Consumer<EchoSyncPayload> consumer) {
        if (type == null || channelId == null || consumer == null) {
            return;
        }
        CONSUMERS.computeIfAbsent(new Key(type, channelId), ignored -> new CopyOnWriteArrayList<>()).add(consumer);
    }

    public static void dispatch(EchoSyncPayload payload) {
        if (payload == null) {
            return;
        }
        for (Consumer<EchoSyncPayload> consumer : CONSUMERS.getOrDefault(new Key(payload.syncType(), payload.channelId()), List.of())) {
            consumer.accept(payload);
        }
    }

    public static void clearForTests() {
        CONSUMERS.clear();
    }

    private record Key(EchoSyncType type, Identifier channelId) {
    }
}
