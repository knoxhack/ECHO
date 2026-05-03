package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Player faction reputation tracking system.
 * Three factions with reputation range -100 (hostile) to +100 (allied).
 */
public class ReputationData implements ValueIOSerializable {
    public static final StreamCodec<RegistryFriendlyByteBuf, ReputationData> STREAM_CODEC = StreamCodec.of(
        ReputationData::writeSync,
        ReputationData::readSync
    );
    
    public enum Faction {
        REMNANTS("Remnants"),      // Blue theme - military tech
        SALVAGERS("Salvagers"),     // Yellow theme - trade economy
        MUTANTS("Mutants");         // Green theme - bio adaptation
        
        private final String displayName;
        
        Faction(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Reputation thresholds
    public static final int HOSTILE_THRESHOLD = -50;
    public static final int NEUTRAL_THRESHOLD = 0;
    public static final int FRIENDLY_THRESHOLD = 50;
    public static final int ALLIED_THRESHOLD = 100;
    
    // Reputation values (-100 to +100)
    private int remnantRep = 0;
    private int salvagerRep = 0;
    private int mutantRep = 0;
    
    // Perk points earned from faction reputation
    private int remnantPerks = 0;
    private int salvagerPerks = 0;
    private int mutantPerks = 0;
    
    public ReputationData() {
        // Default neutral reputation
    }

    private static void writeSync(RegistryFriendlyByteBuf buf, ReputationData data) {
        buf.writeVarInt(data.remnantRep);
        buf.writeVarInt(data.salvagerRep);
        buf.writeVarInt(data.mutantRep);
        buf.writeVarInt(data.remnantPerks);
        buf.writeVarInt(data.salvagerPerks);
        buf.writeVarInt(data.mutantPerks);
    }

    private static ReputationData readSync(RegistryFriendlyByteBuf buf) {
        ReputationData data = new ReputationData();
        data.remnantRep = buf.readVarInt();
        data.salvagerRep = buf.readVarInt();
        data.mutantRep = buf.readVarInt();
        data.remnantPerks = buf.readVarInt();
        data.salvagerPerks = buf.readVarInt();
        data.mutantPerks = buf.readVarInt();
        return data;
    }
    
    /**
     * Get reputation for a specific faction
     */
    public int getReputation(Faction faction) {
        return switch (faction) {
            case REMNANTS -> remnantRep;
            case SALVAGERS -> salvagerRep;
            case MUTANTS -> mutantRep;
        };
    }
    
    /**
     * Set reputation for a specific faction (clamped -100 to +100)
     */
    public void setReputation(Faction faction, int value) {
        int clamped = Math.max(-100, Math.min(100, value));
        switch (faction) {
            case REMNANTS -> remnantRep = clamped;
            case SALVAGERS -> salvagerRep = clamped;
            case MUTANTS -> mutantRep = clamped;
        }
        updatePerksFromReputation(faction);
    }
    
    /**
     * Add to reputation (can be negative for reputation loss)
     */
    public void addReputation(Faction faction, int amount) {
        setReputation(faction, getReputation(faction) + amount);
    }
    
    /**
     * Get the reputation tier for a faction
     */
    public ReputationTier getTier(Faction faction) {
        int rep = getReputation(faction);
        if (rep <= HOSTILE_THRESHOLD) return ReputationTier.HOSTILE;
        if (rep < FRIENDLY_THRESHOLD) return ReputationTier.NEUTRAL;
        if (rep < ALLIED_THRESHOLD) return ReputationTier.FRIENDLY;
        return ReputationTier.ALLIED;
    }
    
    /**
     * Get discount percentage based on reputation (0-20%)
     */
    public int getDiscountPercent(Faction faction) {
        int rep = getReputation(faction);
        if (rep >= ALLIED_THRESHOLD) return 20;
        if (rep >= 75) return 15;
        if (rep >= FRIENDLY_THRESHOLD) return 10;
        if (rep >= 25) return 5;
        return 0;
    }

    public boolean hasSafehouseAccess(Faction faction) {
        return getReputation(faction) >= 25;
    }
    
    /**
     * Get perk points available for a faction branch
     */
    public int getPerkPoints(Faction faction) {
        return switch (faction) {
            case REMNANTS -> remnantPerks;
            case SALVAGERS -> salvagerPerks;
            case MUTANTS -> mutantPerks;
        };
    }
    
    /**
     * Consume perk points for unlocking perks
     */
    public boolean consumePerkPoints(Faction faction, int amount) {
        int current = getPerkPoints(faction);
        if (current < amount) return false;
        
        switch (faction) {
            case REMNANTS -> remnantPerks -= amount;
            case SALVAGERS -> salvagerPerks -= amount;
            case MUTANTS -> mutantPerks -= amount;
        }
        return true;
    }
    
    /**
     * Update perk points based on reputation milestones
     */
    private void updatePerksFromReputation(Faction faction) {
        int rep = getReputation(faction);
        int earnedPerks = 0;
        
        // Earn perks at reputation milestones
        if (rep >= 25) earnedPerks++;
        if (rep >= 50) earnedPerks++;
        if (rep >= 75) earnedPerks++;
        if (rep >= 100) earnedPerks++;
        
        switch (faction) {
            case REMNANTS -> remnantPerks = earnedPerks;
            case SALVAGERS -> salvagerPerks = earnedPerks;
            case MUTANTS -> mutantPerks = earnedPerks;
        }
    }
    
    @Override
    public void serialize(ValueOutput output) {
        output.putInt("remnantRep", remnantRep);
        output.putInt("salvagerRep", salvagerRep);
        output.putInt("mutantRep", mutantRep);
        output.putInt("remnantPerks", remnantPerks);
        output.putInt("salvagerPerks", salvagerPerks);
        output.putInt("mutantPerks", mutantPerks);
    }
    
    @Override
    public void deserialize(ValueInput input) {
        remnantRep = input.getIntOr("remnantRep", 0);
        salvagerRep = input.getIntOr("salvagerRep", 0);
        mutantRep = input.getIntOr("mutantRep", 0);
        remnantPerks = input.getIntOr("remnantPerks", 0);
        salvagerPerks = input.getIntOr("salvagerPerks", 0);
        mutantPerks = input.getIntOr("mutantPerks", 0);
        
        // Ensure values are clamped
        remnantRep = Math.max(-100, Math.min(100, remnantRep));
        salvagerRep = Math.max(-100, Math.min(100, salvagerRep));
        mutantRep = Math.max(-100, Math.min(100, mutantRep));
    }
    
    /**
     * Get or create reputation data for a player
     */
    public static ReputationData get(Player player) {
        return player.getData(ModAttachments.REPUTATION_DATA.get());
    }

    public static void saveAndSync(ServerPlayer player, ReputationData reputation) {
        player.setData(ModAttachments.REPUTATION_DATA.get(), reputation);
        player.syncData(ModAttachments.REPUTATION_DATA.get());
    }

    public static void syncToClient(ServerPlayer player) {
        player.syncData(ModAttachments.REPUTATION_DATA.get());
    }
    
    public enum ReputationTier {
        HOSTILE("Hostile", 0xFFFF3333),
        NEUTRAL("Neutral", 0xFFAAAAAA),
        FRIENDLY("Friendly", 0xFF42D67E),
        ALLIED("Allied", 0xFF4DBAF4);
        
        private final String displayName;
        private final int color;
        
        ReputationTier(String displayName, int color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getColor() {
            return color;
        }
    }
}
