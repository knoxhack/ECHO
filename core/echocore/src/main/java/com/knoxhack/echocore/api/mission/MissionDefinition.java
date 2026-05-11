package com.knoxhack.echocore.api.mission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record MissionDefinition(
        Identifier id,
        Identifier chapterId,
        String phaseId,
        String phaseTitle,
        int phaseOrder,
        int missionOrder,
        String title,
        String briefing,
        String fieldGuide,
        String category,
        String difficulty,
        ItemStack icon,
        List<Identifier> prerequisites,
        List<ObjectiveDefinition> objectives,
        List<RewardDefinition> rewards,
        MissionKind kind,
        MissionRepeatPolicy repeatPolicy,
        boolean hidden,
        Map<String, String> metadata,
        MissionStatusRule statusRule,
        MissionCompletionRule completionRule,
        MissionCompletionHandler completionHandler) {
    public MissionDefinition {
        if (id == null) {
            throw new IllegalArgumentException("Mission id cannot be null.");
        }
        if (chapterId == null) {
            throw new IllegalArgumentException("Mission chapter id cannot be null.");
        }
        phaseId = phaseId == null || phaseId.isBlank() ? "phase_" + Math.max(0, phaseOrder) : phaseId;
        phaseTitle = phaseTitle == null || phaseTitle.isBlank() ? phaseId : phaseTitle;
        title = title == null || title.isBlank() ? id.getPath() : title;
        briefing = briefing == null ? "" : briefing;
        fieldGuide = fieldGuide == null ? "" : fieldGuide;
        category = category == null ? "" : category;
        difficulty = difficulty == null ? "" : difficulty;
        icon = icon == null ? ItemStack.EMPTY : icon.copy();
        prerequisites = List.copyOf(prerequisites == null ? List.of() : prerequisites);
        objectives = List.copyOf(objectives == null ? List.of() : objectives);
        rewards = List.copyOf(rewards == null ? List.of() : rewards);
        kind = kind == null ? MissionKind.MAIN : kind;
        repeatPolicy = repeatPolicy == null ? MissionRepeatPolicy.ONCE : repeatPolicy;
        metadata = Map.copyOf(metadata == null ? Map.of() : new LinkedHashMap<>(metadata));
        statusRule = statusRule == null ? MissionStatusRule.NONE : statusRule;
        completionRule = completionRule == null ? MissionCompletionRule.NONE : completionRule;
        completionHandler = completionHandler == null ? MissionCompletionHandler.NONE : completionHandler;
    }

    public static Builder builder(Identifier id, Identifier chapterId) {
        return new Builder(id, chapterId);
    }

    public static final class Builder {
        private final Identifier id;
        private final Identifier chapterId;
        private String phaseId = "";
        private String phaseTitle = "";
        private int phaseOrder;
        private int missionOrder;
        private String title = "";
        private String briefing = "";
        private String fieldGuide = "";
        private String category = "";
        private String difficulty = "";
        private ItemStack icon = ItemStack.EMPTY;
        private final List<Identifier> prerequisites = new ArrayList<>();
        private final List<ObjectiveDefinition> objectives = new ArrayList<>();
        private final List<RewardDefinition> rewards = new ArrayList<>();
        private MissionKind kind = MissionKind.MAIN;
        private MissionRepeatPolicy repeatPolicy = MissionRepeatPolicy.ONCE;
        private boolean hidden;
        private final Map<String, String> metadata = new LinkedHashMap<>();
        private MissionStatusRule statusRule = MissionStatusRule.NONE;
        private MissionCompletionRule completionRule = MissionCompletionRule.NONE;
        private MissionCompletionHandler completionHandler = MissionCompletionHandler.NONE;

        private Builder(Identifier id, Identifier chapterId) {
            this.id = id;
            this.chapterId = chapterId;
        }

        public Builder phase(String phaseId, String phaseTitle, int phaseOrder, int missionOrder) {
            this.phaseId = phaseId;
            this.phaseTitle = phaseTitle;
            this.phaseOrder = phaseOrder;
            this.missionOrder = missionOrder;
            return this;
        }

        public Builder text(String title, String briefing, String fieldGuide) {
            this.title = title;
            this.briefing = briefing;
            this.fieldGuide = fieldGuide;
            return this;
        }

        public Builder category(String category, String difficulty) {
            this.category = category;
            this.difficulty = difficulty;
            return this;
        }

        public Builder icon(ItemStack icon) {
            this.icon = icon == null ? ItemStack.EMPTY : icon.copy();
            return this;
        }

        public Builder prerequisite(Identifier prerequisite) {
            if (prerequisite != null) {
                this.prerequisites.add(prerequisite);
            }
            return this;
        }

        public Builder prerequisites(List<Identifier> prerequisites) {
            if (prerequisites != null) {
                prerequisites.forEach(this::prerequisite);
            }
            return this;
        }

        public Builder objective(ObjectiveDefinition objective) {
            if (objective != null) {
                this.objectives.add(objective);
            }
            return this;
        }

        public Builder reward(RewardDefinition reward) {
            if (reward != null) {
                this.rewards.add(reward);
            }
            return this;
        }

        public Builder kind(MissionKind kind) {
            this.kind = kind == null ? MissionKind.MAIN : kind;
            return this;
        }

        public Builder repeatPolicy(MissionRepeatPolicy repeatPolicy) {
            this.repeatPolicy = repeatPolicy == null ? MissionRepeatPolicy.ONCE : repeatPolicy;
            return this;
        }

        public Builder hidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public Builder metadata(String key, String value) {
            if (key != null && !key.isBlank()) {
                this.metadata.put(key, value == null ? "" : value);
            }
            return this;
        }

        public Builder completionRule(MissionCompletionRule completionRule) {
            this.completionRule = completionRule == null ? MissionCompletionRule.NONE : completionRule;
            return this;
        }

        public Builder statusRule(MissionStatusRule statusRule) {
            this.statusRule = statusRule == null ? MissionStatusRule.NONE : statusRule;
            return this;
        }

        public Builder completionHandler(MissionCompletionHandler completionHandler) {
            this.completionHandler = completionHandler == null ? MissionCompletionHandler.NONE : completionHandler;
            return this;
        }

        public MissionDefinition build() {
            return new MissionDefinition(id, chapterId, phaseId, phaseTitle, phaseOrder, missionOrder, title,
                    briefing, fieldGuide, category, difficulty, icon, prerequisites, objectives, rewards, kind,
                    repeatPolicy, hidden, metadata, statusRule, completionRule, completionHandler);
        }
    }
}
