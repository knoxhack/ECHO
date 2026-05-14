package com.knoxhack.echotutorialcore.data;

import com.knoxhack.echotutorialcore.EchoTutorialCore;
import com.knoxhack.echotutorialcore.api.TutorialGuideMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class TutorialPlayerData implements ValueIOSerializable {
    public static final StreamCodec<RegistryFriendlyByteBuf, TutorialPlayerData> STREAM_CODEC = StreamCodec.of(
            TutorialPlayerData::writeSync,
            TutorialPlayerData::readSync);

    private TutorialGuideMode guideMode = TutorialGuideMode.NORMAL;
    private final Set<String> progressFlags = new HashSet<>();
    private final Set<String> dismissedHintIds = new HashSet<>();
    private final Set<String> dismissedCardIds = new HashSet<>();
    private final Set<String> unlockedCardIds = new HashSet<>();
    private final Set<String> completedFlowIds = new HashSet<>();
    private final Map<String, Integer> mistakeCounters = new HashMap<>();
    private final Map<String, Long> lastHintTimestamps = new HashMap<>();
    private int popupCountThisSession = 0;
    private long lastDeathTime = 0;
    private String lastDeathCause = "";
    private int repeatedDeathCount = 0;

    public static TutorialPlayerData get(Player player) {
        if (player == null) {
            return new TutorialPlayerData();
        }
        try {
            return player.getData(ModAttachments.TUTORIAL_PLAYER_DATA.get());
        } catch (Exception e) {
            EchoTutorialCore.LOGGER.debug("Failed to get tutorial player data, returning blank.", e);
            return new TutorialPlayerData();
        }
    }

    public static void save(Player player, TutorialPlayerData data) {
        if (player == null || data == null) {
            return;
        }
        try {
            player.setData(ModAttachments.TUTORIAL_PLAYER_DATA.get(), data);
        } catch (Exception e) {
            EchoTutorialCore.LOGGER.debug("Failed to save tutorial player data.", e);
        }
    }

    public static void saveAndSync(ServerPlayer player, TutorialPlayerData data) {
        save(player, data);
        if (player != null) {
            try {
                player.syncData(ModAttachments.TUTORIAL_PLAYER_DATA.get());
            } catch (Exception e) {
                EchoTutorialCore.LOGGER.debug("Failed to sync tutorial player data.", e);
            }
        }
    }

    public TutorialGuideMode guideMode() {
        return guideMode;
    }

    public void setGuideMode(TutorialGuideMode mode) {
        this.guideMode = mode == null ? TutorialGuideMode.NORMAL : mode;
    }

    public boolean hasProgress(Identifier id) {
        return id != null && progressFlags.contains(id.toString());
    }

    public void markProgress(Identifier id) {
        if (id != null) {
            progressFlags.add(id.toString());
        }
    }

    public boolean isHintDismissed(Identifier id) {
        return id != null && dismissedHintIds.contains(id.toString());
    }

    public void dismissHint(Identifier id) {
        if (id != null) {
            dismissedHintIds.add(id.toString());
        }
    }

    public boolean isCardDismissed(Identifier id) {
        return id != null && dismissedCardIds.contains(id.toString());
    }

    public void dismissCard(Identifier id) {
        if (id != null) {
            dismissedCardIds.add(id.toString());
        }
    }

    public boolean isCardUnlocked(Identifier id) {
        return id != null && unlockedCardIds.contains(id.toString());
    }

    public void unlockCard(Identifier id) {
        if (id != null) {
            unlockedCardIds.add(id.toString());
        }
    }

    public boolean isFlowCompleted(Identifier id) {
        return id != null && completedFlowIds.contains(id.toString());
    }

    public void completeFlow(Identifier id) {
        if (id != null) {
            completedFlowIds.add(id.toString());
        }
    }

    public int getMistakeCount(String key) {
        return mistakeCounters.getOrDefault(key, 0);
    }

    public void incrementMistake(String key) {
        mistakeCounters.merge(key, 1, Integer::sum);
    }

    public void resetMistake(String key) {
        mistakeCounters.remove(key);
    }

    public long getLastHintTime(Identifier id) {
        return lastHintTimestamps.getOrDefault(id == null ? "" : id.toString(), 0L);
    }

    public void recordHintTime(Identifier id, long time) {
        if (id != null) {
            lastHintTimestamps.put(id.toString(), time);
        }
    }

    public int popupCountThisSession() {
        return popupCountThisSession;
    }

    public void incrementPopupCount() {
        popupCountThisSession++;
    }

    public void resetPopupCount() {
        popupCountThisSession = 0;
    }

    public long lastDeathTime() {
        return lastDeathTime;
    }

    public String lastDeathCause() {
        return lastDeathCause;
    }

    public int repeatedDeathCount() {
        return repeatedDeathCount;
    }

    public void recordDeath(String cause, long time) {
        if (cause != null && cause.equals(lastDeathCause)) {
            repeatedDeathCount++;
        } else {
            repeatedDeathCount = 1;
        }
        lastDeathCause = cause == null ? "" : cause;
        lastDeathTime = time;
    }

    public Set<String> progressFlags() {
        return Set.copyOf(progressFlags);
    }

    public Set<String> unlockedCardIds() {
        return Set.copyOf(unlockedCardIds);
    }

    public Set<String> completedFlowIds() {
        return Set.copyOf(completedFlowIds);
    }

    public void resetAll() {
        guideMode = TutorialGuideMode.NORMAL;
        progressFlags.clear();
        dismissedHintIds.clear();
        dismissedCardIds.clear();
        unlockedCardIds.clear();
        completedFlowIds.clear();
        mistakeCounters.clear();
        lastHintTimestamps.clear();
        popupCountThisSession = 0;
        lastDeathTime = 0;
        lastDeathCause = "";
        repeatedDeathCount = 0;
    }

    private static void writeSync(RegistryFriendlyByteBuf buf, TutorialPlayerData data) {
        buf.writeUtf(data.guideMode.name());
        writeStringSet(buf, data.progressFlags);
        writeStringSet(buf, data.dismissedHintIds);
        writeStringSet(buf, data.dismissedCardIds);
        writeStringSet(buf, data.unlockedCardIds);
        writeStringSet(buf, data.completedFlowIds);
        buf.writeVarInt(data.popupCountThisSession);
        buf.writeVarLong(data.lastDeathTime);
        buf.writeUtf(data.lastDeathCause);
        buf.writeVarInt(data.repeatedDeathCount);
    }

    private static TutorialPlayerData readSync(RegistryFriendlyByteBuf buf) {
        TutorialPlayerData data = new TutorialPlayerData();
        data.guideMode = TutorialGuideMode.byName(buf.readUtf());
        readStringSet(buf, data.progressFlags);
        readStringSet(buf, data.dismissedHintIds);
        readStringSet(buf, data.dismissedCardIds);
        readStringSet(buf, data.unlockedCardIds);
        readStringSet(buf, data.completedFlowIds);
        data.popupCountThisSession = buf.readVarInt();
        data.lastDeathTime = buf.readVarLong();
        data.lastDeathCause = buf.readUtf();
        data.repeatedDeathCount = buf.readVarInt();
        return data;
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
            if (value != null && !value.isBlank()) {
                values.add(value);
            }
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putString("guideMode", guideMode.name());
        output.putInt("progressCount", progressFlags.size());
        int idx = 0;
        for (String id : progressFlags) {
            output.putString("progress_" + (idx++), id);
        }
        output.putInt("dismissedHintCount", dismissedHintIds.size());
        idx = 0;
        for (String id : dismissedHintIds) {
            output.putString("dismissedHint_" + (idx++), id);
        }
        output.putInt("dismissedCardCount", dismissedCardIds.size());
        idx = 0;
        for (String id : dismissedCardIds) {
            output.putString("dismissedCard_" + (idx++), id);
        }
        output.putInt("unlockedCardCount", unlockedCardIds.size());
        idx = 0;
        for (String id : unlockedCardIds) {
            output.putString("unlockedCard_" + (idx++), id);
        }
        output.putInt("completedFlowCount", completedFlowIds.size());
        idx = 0;
        for (String id : completedFlowIds) {
            output.putString("completedFlow_" + (idx++), id);
        }
        output.putInt("popupCount", popupCountThisSession);
        output.putLong("lastDeathTime", lastDeathTime);
        output.putString("lastDeathCause", lastDeathCause);
        output.putInt("repeatedDeathCount", repeatedDeathCount);
    }

    @Override
    public void deserialize(ValueInput input) {
        guideMode = TutorialGuideMode.byName(input.getStringOr("guideMode", "NORMAL"));
        progressFlags.clear();
        int pCount = input.getIntOr("progressCount", 0);
        for (int i = 0; i < pCount; i++) {
            String id = input.getStringOr("progress_" + i, "");
            if (!id.isBlank()) progressFlags.add(id);
        }
        dismissedHintIds.clear();
        int dhCount = input.getIntOr("dismissedHintCount", 0);
        for (int i = 0; i < dhCount; i++) {
            String id = input.getStringOr("dismissedHint_" + i, "");
            if (!id.isBlank()) dismissedHintIds.add(id);
        }
        dismissedCardIds.clear();
        int dcCount = input.getIntOr("dismissedCardCount", 0);
        for (int i = 0; i < dcCount; i++) {
            String id = input.getStringOr("dismissedCard_" + i, "");
            if (!id.isBlank()) dismissedCardIds.add(id);
        }
        unlockedCardIds.clear();
        int ucCount = input.getIntOr("unlockedCardCount", 0);
        for (int i = 0; i < ucCount; i++) {
            String id = input.getStringOr("unlockedCard_" + i, "");
            if (!id.isBlank()) unlockedCardIds.add(id);
        }
        completedFlowIds.clear();
        int cfCount = input.getIntOr("completedFlowCount", 0);
        for (int i = 0; i < cfCount; i++) {
            String id = input.getStringOr("completedFlow_" + i, "");
            if (!id.isBlank()) completedFlowIds.add(id);
        }
        popupCountThisSession = input.getIntOr("popupCount", 0);
        lastDeathTime = input.getLongOr("lastDeathTime", 0);
        lastDeathCause = input.getStringOr("lastDeathCause", "");
        repeatedDeathCount = input.getIntOr("repeatedDeathCount", 0);
    }
}
