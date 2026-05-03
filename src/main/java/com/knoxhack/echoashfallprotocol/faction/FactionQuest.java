package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echoashfallprotocol.research.ResearchData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Faction quest definition with objectives and rewards.
 * Quests are faction-specific and provide reputation + item rewards.
 */
public class FactionQuest {
    public enum ObjectiveType {
        KILL,
        ITEM_DELIVERY,
        POI_DISCOVERY,
        REPAIR,
        RECON,
        RAID_DEFENSE,
        GENERIC
    }

    public record Objective(ObjectiveType type, String targetId, int requiredCount, String displayText,
                            ReputationData.Faction faction) {
        public Objective {
            targetId = targetId == null ? "" : targetId;
            displayText = displayText == null ? "" : displayText;
            requiredCount = Math.max(1, requiredCount);
        }
    }
    
    public enum QuestType {
        DELIVERY("Delivery", "Transport items to a location"),
        ELIMINATION("Elimination", "Defeat specific targets"),
        RETRIEVAL("Retrieval", "Recover lost items/tech"),
        RECON("Recon", "Explore and report on an area"),
        REPAIR("Repair", "Fix damaged infrastructure"),
        DEFENSE("Defense", "Protect a location from attack");
        
        private final String name;
        private final String description;
        
        QuestType(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
    
    public enum Difficulty {
        EASY(25, 10),
        MEDIUM(25, 25),
        HARD(25, 50),
        EXTREME(25, 100);
        
        private final int reputationReward;
        private final int researchPoints;
        
        Difficulty(int reputationReward, int researchPoints) {
            this.reputationReward = reputationReward;
            this.researchPoints = researchPoints;
        }
        
        public int getReputationReward() { return reputationReward; }
        public int getResearchPoints() { return researchPoints; }
    }
    
    private final String id;
    private final String title;
    private final String description;
    private final ReputationData.Faction faction;
    private final QuestType type;
    private final Difficulty difficulty;
    private final String[] objectives;
    private final List<Objective> objectiveModels;
    private final String[] rewardItems; // Item registry IDs
    private final int rewardReputation;
    private final int rewardResearch;
    private final int requiredReputation; // Minimum rep to accept
    
    public FactionQuest(String id, String title, String description, 
                        ReputationData.Faction faction, QuestType type, Difficulty difficulty,
                        String[] objectives, String[] rewardItems,
                        int requiredReputation) {
        this(id, title, description, faction, type, difficulty, objectives, toGenericObjectives(faction, objectives), rewardItems, requiredReputation);
    }

    public FactionQuest(String id, String title, String description,
                        ReputationData.Faction faction, QuestType type, Difficulty difficulty,
                        String[] objectives, List<Objective> objectiveModels, String[] rewardItems,
                        int requiredReputation) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.faction = faction;
        this.type = type;
        this.difficulty = difficulty;
        this.objectives = objectives;
        this.objectiveModels = Collections.unmodifiableList(new ArrayList<>(objectiveModels));
        this.rewardItems = rewardItems;
        this.rewardReputation = difficulty.getReputationReward();
        this.rewardResearch = difficulty.getResearchPoints();
        this.requiredReputation = requiredReputation;
    }

    private static List<Objective> toGenericObjectives(ReputationData.Faction faction, String[] objectives) {
        List<Objective> models = new ArrayList<>();
        for (String objective : objectives) {
            models.add(new Objective(ObjectiveType.GENERIC, "", 1, objective, faction));
        }
        return models;
    }
    
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ReputationData.Faction getFaction() { return faction; }
    public QuestType getType() { return type; }
    public Difficulty getDifficulty() { return difficulty; }
    public String[] getObjectives() { return objectives; }
    public List<Objective> getObjectiveModels() { return objectiveModels; }
    public String[] getRewardItems() { return rewardItems; }
    public int getRewardReputation() { return rewardReputation; }
    public int getRewardResearch() { return rewardResearch; }
    public int getRequiredReputation() { return requiredReputation; }
    
    public boolean canAccept(Player player) {
        ReputationData rep = ReputationData.get(player);
        FactionQuestData questData = FactionQuestData.get(player);
        return rep.getReputation(faction) >= requiredReputation
            && !questData.isCompleted(id)
            && !questData.hasActiveQuest(faction);
    }
    
    public void assignTo(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            FactionQuestProgression.acceptQuest(serverPlayer, this);
        }
    }
    
    public void updateProgress(int amount) {
        // Progress is tracked per player in FactionQuestData.
    }
    
    public void complete(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            FactionQuestProgression.completeQuest(serverPlayer, this);
            return;
        }

        // Award reputation
        ReputationData rep = ReputationData.get(player);
        rep.addReputation(faction, rewardReputation);
        
        // Award research points
        ResearchData research = ResearchData.get(player);
        research.addPoints(rewardResearch);
        
        // Notify player
        player.sendSystemMessage(Component.literal("Quest Complete: " + title)
            .withStyle(net.minecraft.ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("+" + rewardReputation + " " + faction.getDisplayName() + " reputation")
            .withStyle(net.minecraft.ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("+" + rewardResearch + " Research Points")
            .withStyle(net.minecraft.ChatFormatting.YELLOW));
    }
    
    public boolean isCompleted() { return false; }
    public int getProgress() { return 0; }
    public int getTotalObjectives() { return objectives.length; }
}
