package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Per-player runtime state for faction quests.
 */
public class FactionQuestData implements ValueIOSerializable {
    private final Map<ReputationData.Faction, String> activeQuestIds = new HashMap<>();
    private final Set<String> completedQuestIds = new HashSet<>();
    private final Map<String, Integer> objectiveProgress = new HashMap<>();
    private final Map<String, Long> lastAcceptedTicks = new HashMap<>();
    private final Map<String, Long> lastCompletedTicks = new HashMap<>();
    private String trackedQuestId = "";

    public static final StreamCodec<RegistryFriendlyByteBuf, FactionQuestData> STREAM_CODEC = StreamCodec.of(
        FactionQuestData::writeSync,
        FactionQuestData::readSync
    );

    public boolean hasActiveQuest(ReputationData.Faction faction) {
        return activeQuestIds.containsKey(faction);
    }

    public String getActiveQuestId(ReputationData.Faction faction) {
        return activeQuestIds.getOrDefault(faction, "");
    }

    public FactionQuest getActiveQuest(ReputationData.Faction faction) {
        String id = getActiveQuestId(faction);
        return id.isEmpty() ? null : FactionQuestRegistry.get(id);
    }

    public Map<ReputationData.Faction, String> getActiveQuestIds() {
        return Collections.unmodifiableMap(activeQuestIds);
    }

    public boolean isCompleted(String questId) {
        return completedQuestIds.contains(questId);
    }

    public Set<String> getCompletedQuestIds() {
        return Collections.unmodifiableSet(completedQuestIds);
    }

    public boolean acceptQuest(FactionQuest quest, long gameTime) {
        if (quest == null || isCompleted(quest.getId()) || hasActiveQuest(quest.getFaction())) {
            return false;
        }
        activeQuestIds.put(quest.getFaction(), quest.getId());
        trackedQuestId = quest.getId();
        lastAcceptedTicks.put(quest.getId(), gameTime);
        for (int i = 0; i < quest.getObjectiveModels().size(); i++) {
            objectiveProgress.put(progressKey(quest.getId(), i), 0);
        }
        return true;
    }

    public int getObjectiveProgress(String questId, int objectiveIndex) {
        return objectiveProgress.getOrDefault(progressKey(questId, objectiveIndex), 0);
    }

    public int setObjectiveProgress(String questId, int objectiveIndex, int value, int max) {
        int clamped = Math.max(0, Math.min(max, value));
        objectiveProgress.put(progressKey(questId, objectiveIndex), clamped);
        return clamped;
    }

    public int addObjectiveProgress(String questId, int objectiveIndex, int amount, int max) {
        return setObjectiveProgress(questId, objectiveIndex, getObjectiveProgress(questId, objectiveIndex) + amount, max);
    }

    public boolean isQuestComplete(FactionQuest quest) {
        if (quest == null) return false;
        for (int i = 0; i < quest.getObjectiveModels().size(); i++) {
            FactionQuest.Objective objective = quest.getObjectiveModels().get(i);
            if (getObjectiveProgress(quest.getId(), i) < objective.requiredCount()) {
                return false;
            }
        }
        return !quest.getObjectiveModels().isEmpty();
    }

    public void completeQuest(FactionQuest quest, long gameTime) {
        if (quest == null) return;
        activeQuestIds.remove(quest.getFaction());
        completedQuestIds.add(quest.getId());
        lastCompletedTicks.put(quest.getId(), gameTime);
        if (trackedQuestId.equals(quest.getId())) {
            trackedQuestId = "";
        }
    }

    public long getLastAcceptedTick(String questId) {
        return lastAcceptedTicks.getOrDefault(questId, 0L);
    }

    public long getLastCompletedTick(String questId) {
        return lastCompletedTicks.getOrDefault(questId, 0L);
    }

    public String getTrackedQuestId() {
        return trackedQuestId;
    }

    public void setTrackedQuestId(String trackedQuestId) {
        this.trackedQuestId = trackedQuestId == null ? "" : trackedQuestId;
    }

    public void clearLegacyFactionProgress() {
        activeQuestIds.clear();
        completedQuestIds.clear();
        objectiveProgress.clear();
        lastAcceptedTicks.clear();
        lastCompletedTicks.clear();
        trackedQuestId = "";
    }

    private static String progressKey(String questId, int objectiveIndex) {
        return questId + "#" + objectiveIndex;
    }

    public static FactionQuestData get(Player player) {
        return player.getData(ModAttachments.FACTION_QUEST_DATA.get());
    }

    public static void saveAndSync(ServerPlayer player, FactionQuestData data) {
        player.setData(ModAttachments.FACTION_QUEST_DATA.get(), data);
        player.syncData(ModAttachments.FACTION_QUEST_DATA.get());
    }

    public static void syncToClient(ServerPlayer player) {
        player.syncData(ModAttachments.FACTION_QUEST_DATA.get());
    }

    private static void writeSync(RegistryFriendlyByteBuf buf, FactionQuestData data) {
        writeFactionStringMap(buf, data.activeQuestIds);
        writeStringSet(buf, data.completedQuestIds);
        writeStringIntMap(buf, data.objectiveProgress);
        writeStringLongMap(buf, data.lastAcceptedTicks);
        writeStringLongMap(buf, data.lastCompletedTicks);
        buf.writeUtf(data.trackedQuestId);
    }

    private static FactionQuestData readSync(RegistryFriendlyByteBuf buf) {
        FactionQuestData data = new FactionQuestData();
        readFactionStringMap(buf, data.activeQuestIds);
        readStringSet(buf, data.completedQuestIds);
        readStringIntMap(buf, data.objectiveProgress);
        readStringLongMap(buf, data.lastAcceptedTicks);
        readStringLongMap(buf, data.lastCompletedTicks);
        data.trackedQuestId = buf.readUtf();
        return data;
    }

    private static void writeFactionStringMap(RegistryFriendlyByteBuf buf, Map<ReputationData.Faction, String> values) {
        buf.writeVarInt(values.size());
        for (Map.Entry<ReputationData.Faction, String> entry : values.entrySet()) {
            buf.writeUtf(entry.getKey().name());
            buf.writeUtf(entry.getValue());
        }
    }

    private static void readFactionStringMap(RegistryFriendlyByteBuf buf, Map<ReputationData.Faction, String> values) {
        values.clear();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            String factionName = buf.readUtf();
            String questId = buf.readUtf();
            try {
                if (!questId.isEmpty()) {
                    values.put(ReputationData.Faction.valueOf(factionName), questId);
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private static void writeStringSet(RegistryFriendlyByteBuf buf, Set<String> values) {
        buf.writeVarInt(values.size());
        for (String value : values) {
            buf.writeUtf(value);
        }
    }

    private static void readStringSet(RegistryFriendlyByteBuf buf, Set<String> values) {
        values.clear();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            String value = buf.readUtf();
            if (!value.isEmpty()) values.add(value);
        }
    }

    private static void writeStringIntMap(RegistryFriendlyByteBuf buf, Map<String, Integer> values) {
        buf.writeVarInt(values.size());
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
    }

    private static void readStringIntMap(RegistryFriendlyByteBuf buf, Map<String, Integer> values) {
        values.clear();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            String key = buf.readUtf();
            int value = buf.readVarInt();
            if (!key.isEmpty()) values.put(key, value);
        }
    }

    private static void writeStringLongMap(RegistryFriendlyByteBuf buf, Map<String, Long> values) {
        buf.writeVarInt(values.size());
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeLong(entry.getValue());
        }
    }

    private static void readStringLongMap(RegistryFriendlyByteBuf buf, Map<String, Long> values) {
        values.clear();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            String key = buf.readUtf();
            long value = buf.readLong();
            if (!key.isEmpty()) values.put(key, value);
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("activeCount", activeQuestIds.size());
        int idx = 0;
        for (Map.Entry<ReputationData.Faction, String> entry : activeQuestIds.entrySet()) {
            output.putString("active_" + idx + "_faction", entry.getKey().name());
            output.putString("active_" + idx + "_quest", entry.getValue());
            idx++;
        }

        output.putInt("completedCount", completedQuestIds.size());
        idx = 0;
        for (String questId : completedQuestIds) {
            output.putString("completed_" + idx++, questId);
        }

        output.putInt("progressCount", objectiveProgress.size());
        idx = 0;
        for (Map.Entry<String, Integer> entry : objectiveProgress.entrySet()) {
            output.putString("progress_" + idx + "_key", entry.getKey());
            output.putInt("progress_" + idx + "_value", entry.getValue());
            idx++;
        }

        output.putInt("acceptedCount", lastAcceptedTicks.size());
        idx = 0;
        for (Map.Entry<String, Long> entry : lastAcceptedTicks.entrySet()) {
            output.putString("accepted_" + idx + "_quest", entry.getKey());
            output.putLong("accepted_" + idx + "_tick", entry.getValue());
            idx++;
        }

        output.putInt("completedTickCount", lastCompletedTicks.size());
        idx = 0;
        for (Map.Entry<String, Long> entry : lastCompletedTicks.entrySet()) {
            output.putString("completedTick_" + idx + "_quest", entry.getKey());
            output.putLong("completedTick_" + idx + "_tick", entry.getValue());
            idx++;
        }
        output.putString("trackedQuestId", trackedQuestId);
    }

    @Override
    public void deserialize(ValueInput input) {
        activeQuestIds.clear();
        int activeCount = input.getIntOr("activeCount", 0);
        for (int i = 0; i < activeCount; i++) {
            String factionName = input.getStringOr("active_" + i + "_faction", "");
            String questId = input.getStringOr("active_" + i + "_quest", "");
            try {
                if (!questId.isEmpty()) {
                    activeQuestIds.put(ReputationData.Faction.valueOf(factionName), questId);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        completedQuestIds.clear();
        int completedCount = input.getIntOr("completedCount", 0);
        for (int i = 0; i < completedCount; i++) {
            String questId = input.getStringOr("completed_" + i, "");
            if (!questId.isEmpty()) completedQuestIds.add(questId);
        }

        objectiveProgress.clear();
        int progressCount = input.getIntOr("progressCount", 0);
        for (int i = 0; i < progressCount; i++) {
            String key = input.getStringOr("progress_" + i + "_key", "");
            int value = input.getIntOr("progress_" + i + "_value", 0);
            if (!key.isEmpty()) objectiveProgress.put(key, value);
        }

        lastAcceptedTicks.clear();
        int acceptedCount = input.getIntOr("acceptedCount", 0);
        for (int i = 0; i < acceptedCount; i++) {
            String questId = input.getStringOr("accepted_" + i + "_quest", "");
            long tick = input.getLongOr("accepted_" + i + "_tick", 0L);
            if (!questId.isEmpty()) lastAcceptedTicks.put(questId, tick);
        }

        lastCompletedTicks.clear();
        int completedTickCount = input.getIntOr("completedTickCount", 0);
        for (int i = 0; i < completedTickCount; i++) {
            String questId = input.getStringOr("completedTick_" + i + "_quest", "");
            long tick = input.getLongOr("completedTick_" + i + "_tick", 0L);
            if (!questId.isEmpty()) lastCompletedTicks.put(questId, tick);
        }
        trackedQuestId = input.getStringOr("trackedQuestId", "");
    }
}
