package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.event.FieldOpsContractHandler;
import com.knoxhack.echoashfallprotocol.faction.FactionEvents;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.world.POIScannerService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Signal Scanner - ECHO exploration tool for POI route discovery.
 */
public class SignalScannerItem extends Item {
    
    public SignalScannerItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        
        if (!player.isShiftKeyDown()) {
            POIScannerService.ScanHit hit = POIScannerService.scan(serverPlayer);

            if (hit != null) {
                sendScanReadout(player, hit);

                if (POIScannerService.shouldAutoDiscover(hit) && POIScannerService.discover(serverPlayer, hit)) {
                    FactionEvents.onPOIDiscovered(serverPlayer, hit.id());
                    FieldOpsContractHandler.onPoiDiscovered(serverPlayer, hit);
                    player.sendSystemMessage(Component.literal("[ECHO-7] Site archived in your field log.")
                            .withStyle(ChatFormatting.GREEN));
                }
            } else {
                player.sendSystemMessage(Component.literal("No reliable signals detected in scanner range.")
                    .withStyle(ChatFormatting.YELLOW));
            }
            
            player.getItemInHand(hand).hurtAndBreak(1, player, hand);
            return InteractionResult.SUCCESS;
        } else {
            POIScannerService.ScanHit hit = POIScannerService.scan(serverPlayer);
            FieldOpsContractHandler.requestContract(serverPlayer, hit);

            if (hit != null) {
                player.sendSystemMessage(Component.literal("Map area scanned. Long-range POI vector recorded.")
                        .withStyle(ChatFormatting.AQUA));
                sendScanReadout(player, hit);

                QuestData quest = serverPlayer.getData(ModAttachments.QUEST_DATA.get());
                boolean firstScan = !quest.hasPOIState(hit.id(), QuestData.POIObjectiveState.SCANNED);
                if (firstScan) {
                    if (POIScannerService.discover(serverPlayer, hit)) {
                        FactionEvents.onPOIDiscovered(serverPlayer, hit.id());
                        FieldOpsContractHandler.onPoiDiscovered(serverPlayer, hit);
                    }
                    com.knoxhack.echoashfallprotocol.research.ResearchData research =
                        com.knoxhack.echoashfallprotocol.research.ResearchData.get(player);
                    research.addPoints(2);
                    com.knoxhack.echoashfallprotocol.research.ResearchData.saveAndSync(serverPlayer, research);
                } else {
                    player.sendSystemMessage(Component.literal("[ECHO-7] Signal already exists in your field log.")
                            .withStyle(ChatFormatting.DARK_GRAY));
                    QuestData.saveAndSync(serverPlayer, quest);
                }
            }

            player.getItemInHand(hand).hurtAndBreak(3, player, hand);
            return InteractionResult.SUCCESS;
        }
    }
    
    /**
     * Signal Scanner cannot be enchanted.
     */
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
    
    /**
     * Signal Scanner can be repaired with scrap circuits.
     */
    public boolean isRepairable(ItemStack stack) {
        return true;
    }

    private static void sendScanReadout(Player player, POIScannerService.ScanHit hit) {
        for (Component line : POIScannerService.createReadout(hit)) {
            player.sendSystemMessage(line);
        }
    }
}
