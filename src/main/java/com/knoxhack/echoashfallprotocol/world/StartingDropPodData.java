package com.knoxhack.echoashfallprotocol.world;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * World-saved registry for first-login drop pods.
 * Keeps multiplayer starting pods from stacking on the same server spawn.
 */
public class StartingDropPodData extends SavedData {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

    private static final Codec<BlockPos> BLOCK_POS_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("x").forGetter(BlockPos::getX),
                    Codec.INT.fieldOf("y").forGetter(BlockPos::getY),
                    Codec.INT.fieldOf("z").forGetter(BlockPos::getZ)
            ).apply(instance, BlockPos::new)
    );

    public static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUID_CODEC.fieldOf("playerId").forGetter(Entry::playerId),
                    BLOCK_POS_CODEC.fieldOf("origin").forGetter(Entry::origin),
                    BLOCK_POS_CODEC.fieldOf("interior").forGetter(Entry::interior)
            ).apply(instance, Entry::new)
    );

    public static final Codec<StartingDropPodData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ENTRY_CODEC.listOf().fieldOf("pods").forGetter(data -> data.pods)
            ).apply(instance, StartingDropPodData::new)
    );

    public static final SavedDataType<StartingDropPodData> TYPE = new SavedDataType<StartingDropPodData>(
            Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "starting_drop_pods"),
            StartingDropPodData::new,
            CODEC
    );

    private final List<Entry> pods = new ArrayList<>();

    public StartingDropPodData() {
    }

    private StartingDropPodData(List<Entry> pods) {
        this.pods.addAll(pods);
    }

    public static StartingDropPodData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public Optional<Entry> findForPlayer(UUID playerId) {
        return pods.stream()
                .filter(entry -> entry.playerId().equals(playerId))
                .findFirst();
    }

    public boolean isFarEnoughFromExistingPods(BlockPos candidate, int minimumDistanceBlocks) {
        int minDistanceSqr = minimumDistanceBlocks * minimumDistanceBlocks;
        for (Entry pod : pods) {
            int dx = candidate.getX() - pod.origin().getX();
            int dz = candidate.getZ() - pod.origin().getZ();
            if (dx * dx + dz * dz < minDistanceSqr) {
                return false;
            }
        }
        return true;
    }

    public void addOrReplace(UUID playerId, BlockPos origin, BlockPos interior) {
        pods.removeIf(entry -> entry.playerId().equals(playerId));
        pods.add(new Entry(playerId, origin, interior));
        setDirty();
    }

    public record Entry(UUID playerId, BlockPos origin, BlockPos interior) {
    }
}
