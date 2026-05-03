package com.knoxhack.echoashfallprotocol.block.entity;

import com.knoxhack.echoashfallprotocol.event.FieldOpsContractHandler;
import com.knoxhack.echoashfallprotocol.faction.FactionEvents;
import com.knoxhack.echoashfallprotocol.machine.MachineWearData;
import com.knoxhack.echoashfallprotocol.power.PowerNetwork;
import com.knoxhack.echoashfallprotocol.registry.ModBlockEntities;
import com.knoxhack.echoashfallprotocol.world.POIScannerService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Signal Scanner - locates real nearby POIs and archives route-specific intel.
 */
public class SignalScannerBlockEntity extends BlockEntity {

    private static final int SCAN_COOLDOWN_TICKS = 100;
    private static final int SCAN_POWER_COST = 50;

    private int scanCooldown = 0;
    private MachineWearData wearData;

    public SignalScannerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SIGNAL_SCANNER.get(), pos, state);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level != null) {
            wearData = new MachineWearData(level);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SignalScannerBlockEntity entity) {
        if (entity.scanCooldown > 0) {
            entity.scanCooldown--;
        }
    }

    public void triggerScan(ServerPlayer player) {
        if (scanCooldown > 0) {
            player.sendSystemMessage(Component.literal("Scanner cooling down..."));
            return;
        }

        if (!PowerNetwork.hasPowerAccess(level, worldPosition)) {
            player.sendSystemMessage(Component.literal("No power available!"));
            return;
        }

        if (!PowerNetwork.tryConsumePower(level, worldPosition, SCAN_POWER_COST)) {
            player.sendSystemMessage(Component.literal("Insufficient power!"));
            return;
        }

        scanCooldown = SCAN_COOLDOWN_TICKS;
        if (wearData != null) {
            wearData.addWear(worldPosition, 2, level.getRandom());
        }

        POIScannerService.ScanHit hit = POIScannerService.scan(player);
        if (hit == null) {
            player.sendSystemMessage(Component.literal("[ECHO-7] No POI signals detected within current scanner range.")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        player.sendSystemMessage(POIScannerService.createSummary(hit));
        player.sendSystemMessage(Component.literal("Risk: " + hit.riskProfile()
                + " | Hazard: " + hit.hazardProfile()
                + " | Route: " + hit.route()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Supplies: " + hit.resourceProfile()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("Distance: " + (int) hit.distance()
                + " blocks | Direction: " + hit.direction()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal(hit.intelLine()).withStyle(ChatFormatting.DARK_AQUA));
        player.sendSystemMessage(Component.literal("Prep: " + hit.prepHint()).withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(Component.literal("Recommended Track: " + hit.rewardTrack()).withStyle(ChatFormatting.GRAY));

        if (POIScannerService.shouldAutoDiscover(hit) && POIScannerService.discover(player, hit)) {
            FactionEvents.onPOIDiscovered(player, hit.id());
            FieldOpsContractHandler.onPoiDiscovered(player, hit);
            player.sendSystemMessage(Component.literal("[ECHO-7] Close-range verification complete. Location archived.")
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    public boolean isScanCooldownActive() {
        return scanCooldown > 0;
    }

    public MachineWearData getWearData() {
        return wearData;
    }
}
