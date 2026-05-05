package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.world.POIScannerService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Data Log items containing pre-fall lore, survivor stories, and world history.
 * Reading these adds the lore to ECHO-7's intel database.
 */
public class DataLogItem extends Item {
    
    private final DataLogType logType;
    private final String loreTitle;
    private final String[] lorePages;
    
    public DataLogItem(Properties properties, DataLogType logType, String loreTitle, String[] lorePages) {
        super(properties.stacksTo(1));
        this.logType = logType;
        this.loreTitle = loreTitle;
        this.lorePages = lorePages;
    }
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide()) {
            // Add to ECHO intel
            String content = String.join("\n\n", lorePages);
            var echoIntel = player.getData(ModAttachments.ECHO_INTEL.get());
            echoIntel.discoverLore(
                "datalog_" + logType.name().toLowerCase() + "_" + loreTitle.toLowerCase().replace(" ", "_"),
                "[DATA LOG] " + loreTitle,
                content
            );
            
            if (player instanceof ServerPlayer serverPlayer) {
                recordDataRecovered(serverPlayer);
            }

            // Send message to player
            player.sendSystemMessage(Component.literal("\u00A7b[ECHO-7]\u00A7r Data log archived. " + loreTitle + " added to the Field Archive."));
            
            // Consume the item
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        
        return InteractionResult.SUCCESS;
    }
        
    public DataLogType getLogType() {
        return logType;
    }
    
    public String getLoreTitle() {
        return loreTitle;
    }

    private void recordDataRecovered(ServerPlayer player) {
        QuestData quest = QuestData.get(player);
        quest.visitLocation("special", "data_log:archived");
        quest.visitLocation("special", "data_log:" + logType.name().toLowerCase());

        POIScannerService.ScanHit hit = POIScannerService.scan(player);
        if (hit != null && hit.distance() <= POIScannerService.DISCOVERY_RADIUS * 1.5D) {
            quest.recordPOIState(hit.id(), QuestData.POIObjectiveState.SCANNED);
            quest.recordPOIState(hit.id(), QuestData.POIObjectiveState.DATA_RECOVERED);
            quest.visitLocation("poi", hit.id());
        }

        QuestData.saveAndSync(player, quest);
    }
    
    public enum DataLogType {
        PREFALL_HISTORY("Pre-Fall History", "Documents from before the Gridfall"),
        NEXUS_ARCHIVES("Nexus Archives", "AI system logs and diagnostics"),
        SURVIVOR_JOURNAL("Survivor Journal", "Personal accounts from other survivors"),
        TECHNICAL_MANUAL("Technical Manual", "Old World technical documentation"),
        RESEARCH_DATA("Research Data", "Scientific findings and experiments");
        
        private final String displayName;
        private final String description;
        
        DataLogType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}
