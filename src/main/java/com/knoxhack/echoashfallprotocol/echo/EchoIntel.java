package com.knoxhack.echoashfallprotocol.echo;

import com.knoxhack.echoashfallprotocol.faction.FactionDiplomacy;
import com.knoxhack.echoashfallprotocol.faction.ReputationData;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.*;

/**
 * Tracks intelligence gathered by ECHO-7 through drone reconnaissance.
 * Stores faction dossiers, intercepted communications, and tactical data.
 */
public class EchoIntel implements ValueIOSerializable {
    public static final StreamCodec<RegistryFriendlyByteBuf, EchoIntel> STREAM_CODEC = StreamCodec.of(
        EchoIntel::writeSync,
        EchoIntel::readSync
    );
    
    public enum IntelType {
        RECON("Field Reconnaissance", "Visual observations from drone patrols"),
        INTERCEPT("Intercepted Transmission", "Radio and communication monitoring"),
        DOSSIER("Faction Dossier", "Compiled intelligence reports"),
        TACTICAL("Tactical Data", "Troop movements and base locations"),
        DIPLOMATIC("Diplomatic Intel", "Faction relations and negotiations"),
        HISTORICAL("Historical Record", "Archived world events");
        
        private final String displayName;
        private final String description;
        
        IntelType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public enum IntelPriority {
        LOW(0xFF8A9BB0, "Low"),
        MEDIUM(0xFFFFA94D, "Medium"),
        HIGH(0xFFFF8C42, "High"),
        CRITICAL(0xFFFF3333, "Critical");
        
        private final int color;
        private final String label;
        
        IntelPriority(int color, String label) {
            this.color = color;
            this.label = label;
        }
        
        public int getColor() { return color; }
        public String getLabel() { return label; }
    }
    
    /**
     * Individual intel entry
     */
    public static class IntelEntry {
        public final String id;
        public final IntelType type;
        public final String title;
        public String content;
        public final long timestamp;
        public final IntelPriority priority;
        public final ReputationData.Faction relatedFaction;
        public boolean isRead = false;
        
        public IntelEntry(String id, IntelType type, String title, String content,
                         IntelPriority priority, ReputationData.Faction faction) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
            this.priority = priority;
            this.relatedFaction = faction;
        }

        private IntelEntry(String id, IntelType type, String title, String content,
                           long timestamp, IntelPriority priority, ReputationData.Faction faction, boolean isRead) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.content = content;
            this.timestamp = timestamp;
            this.priority = priority;
            this.relatedFaction = faction;
            this.isRead = isRead;
        }
    }
    
    // Stored intel entries by category
    private final List<IntelEntry> allIntel = new ArrayList<>();
    private final Map<String, IntelEntry> intelById = new HashMap<>();
    
    // Dossier completion tracking
    private final Map<ReputationData.Faction, Integer> dossierProgress = new HashMap<>();
    private final Set<String> discoveredLore = new HashSet<>();
    
    // Pattern detection - tracks suspicious activities
    private final List<String> activityLog = new ArrayList<>();
    
    // Maximum intel storage
    private static final int MAX_INTEL_ENTRIES = 100;
    
    public EchoIntel() {
        // Initialize dossier progress
        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            dossierProgress.put(faction, 0);
        }
    }

    private static void writeSync(RegistryFriendlyByteBuf buf, EchoIntel data) {
        buf.writeVarInt(data.allIntel.size());
        for (IntelEntry entry : data.allIntel) {
            buf.writeUtf(entry.id);
            buf.writeUtf(entry.type.name());
            buf.writeUtf(entry.title);
            buf.writeUtf(entry.content);
            buf.writeLong(entry.timestamp);
            buf.writeUtf(entry.priority.name());
            buf.writeBoolean(entry.relatedFaction != null);
            if (entry.relatedFaction != null) {
                buf.writeUtf(entry.relatedFaction.name());
            }
            buf.writeBoolean(entry.isRead);
        }

        buf.writeVarInt(data.dossierProgress.size());
        for (Map.Entry<ReputationData.Faction, Integer> entry : data.dossierProgress.entrySet()) {
            buf.writeUtf(entry.getKey().name());
            buf.writeVarInt(entry.getValue());
        }

        buf.writeVarInt(data.discoveredLore.size());
        for (String loreId : data.discoveredLore) {
            buf.writeUtf(loreId);
        }

        buf.writeVarInt(data.activityLog.size());
        for (String activity : data.activityLog) {
            buf.writeUtf(activity);
        }
    }

    private static EchoIntel readSync(RegistryFriendlyByteBuf buf) {
        EchoIntel data = new EchoIntel();
        data.allIntel.clear();
        data.intelById.clear();
        int intelCount = buf.readVarInt();
        for (int i = 0; i < intelCount; i++) {
            String id = buf.readUtf();
            IntelType type = safeEnum(IntelType.class, buf.readUtf(), IntelType.RECON);
            String title = buf.readUtf();
            String content = buf.readUtf();
            long timestamp = buf.readLong();
            IntelPriority priority = safeEnum(IntelPriority.class, buf.readUtf(), IntelPriority.LOW);
            ReputationData.Faction faction = null;
            if (buf.readBoolean()) {
                faction = safeEnum(ReputationData.Faction.class, buf.readUtf(), null);
            }
            boolean isRead = buf.readBoolean();
            IntelEntry entry = new IntelEntry(id, type, title, content, timestamp, priority, faction, isRead);
            data.allIntel.add(entry);
            data.intelById.put(id, entry);
        }

        data.dossierProgress.clear();
        int dossierCount = buf.readVarInt();
        for (int i = 0; i < dossierCount; i++) {
            ReputationData.Faction faction = safeEnum(ReputationData.Faction.class, buf.readUtf(), null);
            int progress = buf.readVarInt();
            if (faction != null) {
                data.dossierProgress.put(faction, progress);
            }
        }

        data.discoveredLore.clear();
        int loreCount = buf.readVarInt();
        for (int i = 0; i < loreCount; i++) {
            String loreId = buf.readUtf();
            if (!loreId.isEmpty()) data.discoveredLore.add(loreId);
        }

        data.activityLog.clear();
        int activityCount = buf.readVarInt();
        for (int i = 0; i < activityCount; i++) {
            String activity = buf.readUtf();
            if (!activity.isEmpty()) data.activityLog.add(activity);
        }
        return data;
    }

    private static <T extends Enum<T>> T safeEnum(Class<T> type, String name, T fallback) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
    
    /**
     * Add new intel from drone reconnaissance
     */
    public void addIntel(IntelEntry entry) {
        if (intelById.containsKey(entry.id)) return; // Duplicate
        
        // Remove oldest if at capacity
        if (allIntel.size() >= MAX_INTEL_ENTRIES) {
            IntelEntry oldest = allIntel.remove(0);
            intelById.remove(oldest.id);
        }
        
        allIntel.add(entry);
        intelById.put(entry.id, entry);
        
        // Update dossier progress for faction-related intel
        if (entry.relatedFaction != null && entry.type == IntelType.DOSSIER) {
            int current = dossierProgress.getOrDefault(entry.relatedFaction, 0);
            dossierProgress.put(entry.relatedFaction, current + 1);
        }
    }
    
    /**
     * Quick method to add recon intel
     */
    public void addReconIntel(String title, String content, ReputationData.Faction faction, IntelPriority priority) {
        String id = "recon_" + System.currentTimeMillis();
        addIntel(new IntelEntry(id, IntelType.RECON, title, content, priority, faction));
    }
    
    /**
     * Add tactical intel about enemy movements or threats
     */
    public void addTacticalIntel(String title, String content, ReputationData.Faction faction, IntelPriority priority) {
        String id = "tactical_" + System.currentTimeMillis();
        addIntel(new IntelEntry(id, IntelType.TACTICAL, title, content, priority, faction));
    }
    
    /**
     * Add intercepted communication intel
     */
    public void addInterceptedTransmission(String title, String transmission, ReputationData.Faction faction) {
        String id = "intercept_" + System.currentTimeMillis();
        IntelPriority priority = determinePriority(transmission);
        addIntel(new IntelEntry(id, IntelType.INTERCEPT, title, 
            "[INTERCEPTED] \"" + transmission + "\"", priority, faction));
        
        // Log for pattern detection
        activityLog.add(faction.name() + ": " + transmission);
        if (activityLog.size() > 50) {
            activityLog.remove(0);
        }
    }
    
    /**
     * Update faction dossier with new information
     */
    public void updateDossier(ReputationData.Faction faction, String category, String info) {
        String id = "dossier_" + faction.name() + "_" + category;
        if (!intelById.containsKey(id)) {
            IntelEntry entry = new IntelEntry(id, IntelType.DOSSIER,
                faction.getDisplayName() + " - " + category, info, IntelPriority.MEDIUM, faction);
            addIntel(entry);
        } else {
            // Append to existing
            IntelEntry existing = intelById.get(id);
            existing.content += "\n[UPDATE] " + info;
        }
    }
    
    /**
     * Get all intel entries
     */
    public List<IntelEntry> getAllIntel() {
        return new ArrayList<>(allIntel);
    }
    
    /**
     * Get intel filtered by type
     */
    public List<IntelEntry> getIntelByType(IntelType type) {
        return allIntel.stream()
            .filter(e -> e.type == type)
            .toList();
    }
    
    /**
     * Get intel related to a specific faction
     */
    public List<IntelEntry> getFactionIntel(ReputationData.Faction faction) {
        return allIntel.stream()
            .filter(e -> e.relatedFaction == faction)
            .toList();
    }
    
    /**
     * Get unread intel count
     */
    public int getUnreadCount() {
        return (int) allIntel.stream().filter(e -> !e.isRead).count();
    }
    
    /**
     * Mark intel as read
     */
    public void markAsRead(String intelId) {
        IntelEntry entry = intelById.get(intelId);
        if (entry != null) {
            entry.isRead = true;
        }
    }
    
    /**
     * Mark all intel as read
     */
    public void markAllAsRead() {
        for (IntelEntry entry : allIntel) {
            entry.isRead = true;
        }
    }
    
    /**
     * Get dossier completion percentage for a faction (0-100)
     */
    public int getDossierCompletion(ReputationData.Faction faction) {
        int collected = dossierProgress.getOrDefault(faction, 0);
        return Math.min(100, collected * 10); // 10 entries = 100%
    }
    
    /**
     * Check if a specific lore entry has been discovered
     */
    public boolean hasDiscoveredLore(String loreId) {
        return discoveredLore.contains(loreId);
    }
    
    /**
     * Mark lore as discovered
     */
    public void discoverLore(String loreId, String title, String content) {
        if (discoveredLore.add(loreId)) {
            String intelId = "lore_" + loreId;
            addIntel(new IntelEntry(intelId, IntelType.HISTORICAL, title, content, 
                IntelPriority.MEDIUM, null));
        }
    }
    
    /**
     * Analyze patterns in activity log to detect emerging threats
     */
    public List<String> detectPatterns() {
        List<String> patterns = new ArrayList<>();
        
        // Simple pattern detection - count faction mentions
        Map<ReputationData.Faction, Integer> mentionCount = new HashMap<>();
        for (String activity : activityLog) {
            for (ReputationData.Faction faction : ReputationData.Faction.values()) {
                if (activity.contains(faction.name())) {
                    mentionCount.merge(faction, 1, Integer::sum);
                }
            }
        }
        
        // If a faction is mentioned frequently, flag it
        for (Map.Entry<ReputationData.Faction, Integer> entry : mentionCount.entrySet()) {
            if (entry.getValue() >= 5) {
                patterns.add(entry.getKey().getDisplayName() + " activity detected - possible operation in planning");
            }
        }
        
        return patterns;
    }
    
    /**
     * Synthesize intel from multiple sources for ECHO-7 insights
     */
    public String synthesizeInsight() {
        List<String> patterns = detectPatterns();
        if (!patterns.isEmpty()) {
            return "Pattern analysis suggests: " + patterns.get(0);
        }
        
        // Check for diplomatic shifts
        long recentDiplomaticIntel = allIntel.stream()
            .filter(e -> e.type == IntelType.DIPLOMATIC)
            .filter(e -> System.currentTimeMillis() - e.timestamp < 86400000) // 24 hours
            .count();
        
        if (recentDiplomaticIntel > 3) {
            return "Unusual diplomatic traffic detected. Faction relations may be shifting.";
        }
        
        // Check for tactical buildup
        long recentTactical = allIntel.stream()
            .filter(e -> e.type == IntelType.TACTICAL)
            .filter(e -> System.currentTimeMillis() - e.timestamp < 3600000) // 1 hour
            .count();
        
        if (recentTactical > 5) {
            return "Elevated tactical activity in your region. Expect encounters.";
        }
        
        return null; // No significant insight
    }
    
    /**
     * Determine priority based on keywords
     */
    private IntelPriority determinePriority(String content) {
        String lower = content.toLowerCase();
        if (lower.contains("attack") || lower.contains("raid") || lower.contains("war")) {
            return IntelPriority.HIGH;
        }
        if (lower.contains("planned") || lower.contains("moving") || lower.contains("position")) {
            return IntelPriority.MEDIUM;
        }
        return IntelPriority.LOW;
    }
    
    @Override
    public void serialize(ValueOutput output) {
        // Save intel entries
        output.putInt("IntelCount", allIntel.size());
        for (int i = 0; i < allIntel.size(); i++) {
            IntelEntry e = allIntel.get(i);
            String prefix = "Intel_" + i + "_";
            output.putString(prefix + "id", e.id);
            output.putString(prefix + "type", e.type.name());
            output.putString(prefix + "title", e.title);
            output.putString(prefix + "content", e.content);
            output.putLong(prefix + "time", e.timestamp);
            output.putString(prefix + "priority", e.priority.name());
            output.putBoolean(prefix + "read", e.isRead);
            if (e.relatedFaction != null) {
                output.putString(prefix + "faction", e.relatedFaction.name());
            }
        }
        
        // Save dossier progress
        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            output.putInt("Dossier_" + faction.name(), 
                dossierProgress.getOrDefault(faction, 0));
        }
        
        // Save discovered lore
        output.putInt("LoreCount", discoveredLore.size());
        int loreIdx = 0;
        for (String loreId : discoveredLore) {
            output.putString("Lore_" + loreIdx++, loreId);
        }
    }
    
    @Override
    public void deserialize(ValueInput input) {
        allIntel.clear();
        intelById.clear();
        
        int count = input.getIntOr("IntelCount", 0);
        for (int i = 0; i < count; i++) {
            String prefix = "Intel_" + i + "_";
            String id = input.getStringOr(prefix + "id", "");
            if (id.isEmpty()) continue;
            
            try {
                IntelType type = IntelType.valueOf(input.getStringOr(prefix + "type", "RECON"));
                String title = input.getStringOr(prefix + "title", "Unknown");
                String content = input.getStringOr(prefix + "content", "");
                long time = input.getLongOr(prefix + "time", 0L);
                IntelPriority priority = IntelPriority.valueOf(input.getStringOr(prefix + "priority", "MEDIUM"));
                boolean read = input.getBooleanOr(prefix + "read", false);
                
                ReputationData.Faction faction = null;
                String factionStr = input.getStringOr(prefix + "faction", "");
                if (!factionStr.isEmpty()) {
                    try {
                        faction = ReputationData.Faction.valueOf(factionStr);
                    } catch (IllegalArgumentException ignored) {}
                }
                
                IntelEntry entry = new IntelEntry(id, type, title, content, time, priority, faction, read);
                
                allIntel.add(entry);
                intelById.put(id, entry);
            } catch (IllegalArgumentException ignored) {}
        }
        
        // Load dossier progress
        for (ReputationData.Faction faction : ReputationData.Faction.values()) {
            int progress = input.getIntOr("Dossier_" + faction.name(), 0);
            dossierProgress.put(faction, progress);
        }
        
        // Load discovered lore
        discoveredLore.clear();
        int loreCount = input.getIntOr("LoreCount", 0);
        for (int i = 0; i < loreCount; i++) {
            String loreId = input.getStringOr("Lore_" + i, "");
            if (!loreId.isEmpty()) {
                discoveredLore.add(loreId);
            }
        }
    }

    public static EchoIntel get(net.minecraft.world.entity.player.Player player) {
        return player.getData(ModAttachments.ECHO_INTEL.get());
    }

    public static void saveAndSync(ServerPlayer player, EchoIntel intel) {
        player.setData(ModAttachments.ECHO_INTEL.get(), intel);
        player.syncData(ModAttachments.ECHO_INTEL.get());
    }

    public static void syncToClient(ServerPlayer player) {
        player.syncData(ModAttachments.ECHO_INTEL.get());
    }
}
