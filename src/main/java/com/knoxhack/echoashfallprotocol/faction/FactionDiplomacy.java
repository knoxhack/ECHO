package com.knoxhack.echoashfallprotocol.faction;

import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Manages inter-faction diplomatic relations and war/peace states.
 * Tracks relationships between Remnants, Salvagers, and Mutants.
 */
public class FactionDiplomacy implements ValueIOSerializable {
    public static final StreamCodec<RegistryFriendlyByteBuf, FactionDiplomacy> STREAM_CODEC = StreamCodec.of(
        FactionDiplomacy::writeSync,
        FactionDiplomacy::readSync
    );
    
    public enum DiplomaticState {
        ALLIANCE("Alliance", 0xFF4DBAF4, 75, 100),
        TRUCE("Truce", 0xFF8A9BB0, 25, 74),
        COLD_WAR("Cold War", 0xFFAAAAAA, 0, 24),
        TENSION("Tension", 0xFFFFA94D, -25, -1),
        SKIRMISH("Skirmish", 0xFFFF8C42, -50, -26),
        OPEN_WAR("Open War", 0xFFFF3333, -100, -51);
        
        private final String displayName;
        private final int color;
        private final int minRelation;
        private final int maxRelation;
        
        DiplomaticState(String displayName, int color, int minRelation, int maxRelation) {
            this.displayName = displayName;
            this.color = color;
            this.minRelation = minRelation;
            this.maxRelation = maxRelation;
        }
        
        public String getDisplayName() { return displayName; }
        public int getColor() { return color; }
        
        public static DiplomaticState fromRelation(int relation) {
            for (DiplomaticState state : values()) {
                if (relation >= state.minRelation && relation <= state.maxRelation) {
                    return state;
                }
            }
            return COLD_WAR;
        }
        
        public boolean isConflict() {
            return this == TENSION || this == SKIRMISH || this == OPEN_WAR;
        }
        
        public boolean isCooperative() {
            return this == ALLIANCE || this == TRUCE;
        }
    }
    
    // Faction pairs
    public enum FactionPair {
        REMNANT_SALVAGER(ReputationData.Faction.REMNANTS, ReputationData.Faction.SALVAGERS),
        REMNANT_MUTANT(ReputationData.Faction.REMNANTS, ReputationData.Faction.MUTANTS),
        SALVAGER_MUTANT(ReputationData.Faction.SALVAGERS, ReputationData.Faction.MUTANTS);
        
        private final ReputationData.Faction factionA;
        private final ReputationData.Faction factionB;
        
        FactionPair(ReputationData.Faction a, ReputationData.Faction b) {
            this.factionA = a;
            this.factionB = b;
        }
        
        public ReputationData.Faction getFactionA() { return factionA; }
        public ReputationData.Faction getFactionB() { return factionB; }
        
        public static FactionPair fromFactions(ReputationData.Faction a, ReputationData.Faction b) {
            for (FactionPair pair : values()) {
                if ((pair.factionA == a && pair.factionB == b) || 
                    (pair.factionA == b && pair.factionB == a)) {
                    return pair;
                }
            }
            return null;
        }
        
        public boolean involves(ReputationData.Faction faction) {
            return factionA == faction || factionB == faction;
        }
        
        public ReputationData.Faction getOther(ReputationData.Faction faction) {
            return factionA == faction ? factionB : factionA;
        }
    }
    
    // Relation values (-100 to +100) for each pair
    private int remnantSalvagerRelation = -10;  // Natural competition
    private int remnantMutantRelation = -40;    // Strong hostility
    private int salvagerMutantRelation = -20;   // Distrust
    
    private final Map<FactionPair, Long> lastStateChangeTick = new HashMap<>();
    private final Random random = new Random();
    
    public FactionDiplomacy() {}

    private static void writeSync(RegistryFriendlyByteBuf buf, FactionDiplomacy data) {
        buf.writeVarInt(data.remnantSalvagerRelation);
        buf.writeVarInt(data.remnantMutantRelation);
        buf.writeVarInt(data.salvagerMutantRelation);
        buf.writeVarInt(data.lastStateChangeTick.size());
        for (Map.Entry<FactionPair, Long> entry : data.lastStateChangeTick.entrySet()) {
            buf.writeUtf(entry.getKey().name());
            buf.writeLong(entry.getValue());
        }
    }

    private static FactionDiplomacy readSync(RegistryFriendlyByteBuf buf) {
        FactionDiplomacy data = new FactionDiplomacy();
        data.remnantSalvagerRelation = buf.readVarInt();
        data.remnantMutantRelation = buf.readVarInt();
        data.salvagerMutantRelation = buf.readVarInt();
        data.lastStateChangeTick.clear();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            String pairName = buf.readUtf();
            long tick = buf.readLong();
            try {
                data.lastStateChangeTick.put(FactionPair.valueOf(pairName), tick);
            } catch (IllegalArgumentException ignored) {}
        }
        return data;
    }
    
    public int getRelation(FactionPair pair) {
        return switch (pair) {
            case REMNANT_SALVAGER -> remnantSalvagerRelation;
            case REMNANT_MUTANT -> remnantMutantRelation;
            case SALVAGER_MUTANT -> salvagerMutantRelation;
        };
    }
    
    public int getRelation(ReputationData.Faction a, ReputationData.Faction b) {
        FactionPair pair = FactionPair.fromFactions(a, b);
        return pair != null ? getRelation(pair) : 0;
    }
    
    public void setRelation(FactionPair pair, int value) {
        int clamped = Math.max(-100, Math.min(100, value));
        switch (pair) {
            case REMNANT_SALVAGER -> remnantSalvagerRelation = clamped;
            case REMNANT_MUTANT -> remnantMutantRelation = clamped;
            case SALVAGER_MUTANT -> salvagerMutantRelation = clamped;
        }
    }
    
    public void modifyRelation(FactionPair pair, int amount) {
        setRelation(pair, getRelation(pair) + amount);
    }
    
    public DiplomaticState getState(FactionPair pair) {
        return DiplomaticState.fromRelation(getRelation(pair));
    }
    
    public DiplomaticState getState(ReputationData.Faction a, ReputationData.Faction b) {
        FactionPair pair = FactionPair.fromFactions(a, b);
        return pair != null ? getState(pair) : DiplomaticState.COLD_WAR;
    }
    
    /**
     * Check if two factions are currently at war
     */
    public boolean isAtWar(ReputationData.Faction a, ReputationData.Faction b) {
        DiplomaticState state = getState(a, b);
        return state == DiplomaticState.OPEN_WAR || state == DiplomaticState.SKIRMISH;
    }
    
    /**
     * Check if two factions are allied
     */
    public boolean isAllied(ReputationData.Faction a, ReputationData.Faction b) {
        return getState(a, b) == DiplomaticState.ALLIANCE;
    }
    
    /**
     * Get the worst diplomatic state involving a given faction
     */
    public DiplomaticState getWorstStateForFaction(ReputationData.Faction faction) {
        DiplomaticState worst = DiplomaticState.ALLIANCE;
        for (FactionPair pair : FactionPair.values()) {
            if (pair.involves(faction)) {
                DiplomaticState state = getState(pair);
                if (state.ordinal() > worst.ordinal()) {
                    worst = state;
                }
            }
        }
        return worst;
    }
    
    /**
     * Get the faction that is most hostile to the given faction
     */
    public ReputationData.Faction getMostHostile(ReputationData.Faction faction) {
        ReputationData.Faction mostHostile = null;
        int lowestRelation = 100;
        
        for (ReputationData.Faction other : ReputationData.Faction.values()) {
            if (other == faction) continue;
            int relation = getRelation(faction, other);
            if (relation < lowestRelation) {
                lowestRelation = relation;
                mostHostile = other;
            }
        }
        return mostHostile;
    }
    
    /**
     * Simulate natural drift in faction relations over time
     * Called periodically by world tick events
     */
    public void tickRelations(long worldTick) {
        // Small random drift every 20 minutes (24000 ticks)
        if (worldTick % 24000 != 0) return;
        
        for (FactionPair pair : FactionPair.values()) {
            int current = getRelation(pair);
            DiplomaticState state = getState(pair);
            
            // Natural tendency toward COLD_WAR
            int drift = switch (state) {
                case ALLIANCE -> -2;      // Alliances require maintenance
                case TRUCE -> -1;         // Truces slowly decay
                case COLD_WAR -> 1;       // Cold war warms slightly
                case TENSION -> -2;       // Tension escalates
                case SKIRMISH -> -3;      // Skirmishes push toward war
                case OPEN_WAR -> -1;      // Wars continue until resolved
            };
            
            // Random variation
            drift += random.nextInt(3) - 1;
            
            setRelation(pair, current + drift);
        }
    }
    
    /**
     * Force a state change with a minimum cooldown
     */
    public boolean tryStateChange(FactionPair pair, DiplomaticState targetState, long worldTick, int cooldownTicks) {
        Long lastChange = lastStateChangeTick.get(pair);
        if (lastChange != null && worldTick - lastChange < cooldownTicks) {
            return false; // Cooldown active
        }
        
        int targetRelation = switch (targetState) {
            case ALLIANCE -> 75;
            case TRUCE -> 25;
            case COLD_WAR -> 0;
            case TENSION -> -25;
            case SKIRMISH -> -50;
            case OPEN_WAR -> -75;
        };
        
        setRelation(pair, targetRelation + random.nextInt(10) - 5);
        lastStateChangeTick.put(pair, worldTick);
        return true;
    }
    
    @Override
    public void serialize(ValueOutput output) {
        output.putInt("RemnantSalvagerRelation", remnantSalvagerRelation);
        output.putInt("RemnantMutantRelation", remnantMutantRelation);
        output.putInt("SalvagerMutantRelation", salvagerMutantRelation);
        
        // Save state change timestamps
        output.putInt("StateChangeCount", lastStateChangeTick.size());
        int idx = 0;
        for (Map.Entry<FactionPair, Long> entry : lastStateChangeTick.entrySet()) {
            output.putString("StateChange_" + idx + "_Pair", entry.getKey().name());
            output.putLong("StateChange_" + idx + "_Tick", entry.getValue());
            idx++;
        }
    }
    
    @Override
    public void deserialize(ValueInput input) {
        remnantSalvagerRelation = input.getIntOr("RemnantSalvagerRelation", -10);
        remnantMutantRelation = input.getIntOr("RemnantMutantRelation", -40);
        salvagerMutantRelation = input.getIntOr("SalvagerMutantRelation", -20);
        
        // Clamp values
        remnantSalvagerRelation = Math.max(-100, Math.min(100, remnantSalvagerRelation));
        remnantMutantRelation = Math.max(-100, Math.min(100, remnantMutantRelation));
        salvagerMutantRelation = Math.max(-100, Math.min(100, salvagerMutantRelation));
        
        // Load state change timestamps
        lastStateChangeTick.clear();
        int count = input.getIntOr("StateChangeCount", 0);
        for (int i = 0; i < count; i++) {
            String pairName = input.getStringOr("StateChange_" + i + "_Pair", "");
            long tick = input.getLongOr("StateChange_" + i + "_Tick", 0L);
            try {
                FactionPair pair = FactionPair.valueOf(pairName);
                lastStateChangeTick.put(pair, tick);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    public static void saveAndSync(ServerPlayer player, FactionDiplomacy diplomacy) {
        player.setData(ModAttachments.FACTION_DIPLOMACY.get(), diplomacy);
        player.syncData(ModAttachments.FACTION_DIPLOMACY.get());
    }

    public static void syncToClient(ServerPlayer player) {
        player.syncData(ModAttachments.FACTION_DIPLOMACY.get());
    }
}
