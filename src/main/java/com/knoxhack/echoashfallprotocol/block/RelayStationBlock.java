package com.knoxhack.echoashfallprotocol.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import com.knoxhack.echoashfallprotocol.faction.AshfallFactionContractProgression;
import com.knoxhack.echoashfallprotocol.fasttravel.RadioNetwork;
import com.knoxhack.echoashfallprotocol.fasttravel.StationRegistry;

/**
 * Relay Station Block - Core component of the Radio Network fast-travel system.
 * Players must repair and activate stations to enable fast travel between them.
 */
public class RelayStationBlock extends Block {
    
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty REPAIRED = BooleanProperty.create("repaired");
    
    public RelayStationBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(ACTIVE, false)
            .setValue(REPAIRED, false));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, REPAIRED);
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        
        boolean isRepaired = state.getValue(REPAIRED);
        boolean isActive = state.getValue(ACTIVE);
        
        if (!isRepaired) {
            // Check if player has repair materials
            if (hasRepairMaterials(player)) {
                repairStation(level, pos, state, player);
                return InteractionResult.SUCCESS;
            } else {
                discoverStation(player, pos);
                player.sendSystemMessage(Component.literal("[ECHO-7] Relay Station unsealed but unrepaired.")
                    .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.literal("Required: Power Cell, Circuit Board, 2 Scrap Circuits.")
                    .withStyle(ChatFormatting.GRAY));
                return InteractionResult.SUCCESS;
            }
        }
        
        if (!isActive) {
            // Check if player has power cell to activate
            if (hasActivationMaterials(player)) {
                activateStation(level, pos, state, player);
                return InteractionResult.SUCCESS;
            } else {
                player.sendSystemMessage(Component.literal("[ECHO-7] Relay hardware repaired. Activation cell missing.")
                    .withStyle(ChatFormatting.YELLOW));
                player.sendSystemMessage(Component.literal("Required: Power Cell.")
                    .withStyle(ChatFormatting.GRAY));
                return InteractionResult.SUCCESS;
            }
        }
        
        // Station is active - open fast travel UI
        openFastTravelUI(player, pos);
        return InteractionResult.SUCCESS;
    }
    
    private boolean hasRepairMaterials(Player player) {
        boolean hasPowerCell = hasItem(player, ModItems.POWER_CELL.get());
        boolean hasCircuitBoard = hasItem(player, ModItems.CIRCUIT_BOARD.get());
        boolean hasAntennaParts = hasItem(player, ModItems.SCRAP_CIRCUIT.get());
        return hasPowerCell && hasCircuitBoard && hasAntennaParts;
    }
    
    private boolean hasItem(Player player, net.minecraft.world.item.Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasActivationMaterials(Player player) {
        return hasItem(player, ModItems.POWER_CELL.get());
    }
    
    private void repairStation(Level level, BlockPos pos, BlockState state, Player player) {
        // Consume materials: 1 Power Cell, 1 Circuit Board, 2 Scrap Circuits
        if (!consumeItem(player, ModItems.POWER_CELL.get(), 1) ||
            !consumeItem(player, ModItems.CIRCUIT_BOARD.get(), 1) ||
            !consumeItem(player, ModItems.SCRAP_CIRCUIT.get(), 2)) {
            player.sendSystemMessage(Component.literal("[ECHO-7] Repair failed. Required materials desynced during handoff.")
                .withStyle(ChatFormatting.RED));
            return;
        }
        
        level.setBlock(pos, state.setValue(REPAIRED, true), 3);
        discoverStation(player, pos);
        
        player.sendSystemMessage(Component.literal("[ECHO-7] Relay Station repaired. Radio spine is listening.")
            .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("Activate with a Power Cell to open the route network.")
            .withStyle(ChatFormatting.YELLOW));
        
        // Award research points
        if (player instanceof ServerPlayer serverPlayer) {
            com.knoxhack.echoashfallprotocol.research.ResearchData research =
                com.knoxhack.echoashfallprotocol.research.ResearchData.get(player);
            research.addPoints(15);
            com.knoxhack.echoashfallprotocol.research.ResearchData.saveAndSync(serverPlayer, research);
            AshfallFactionContractProgression.progressRepair(serverPlayer, "relay");
        }
    }
    
    private boolean consumeItem(Player player, net.minecraft.world.item.Item item, int count) {
        int remaining = count;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int toRemove = Math.min(stack.getCount(), remaining);
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
        return remaining == 0;
    }
    
    private void activateStation(Level level, BlockPos pos, BlockState state, Player player) {
        if (!consumeItem(player, ModItems.POWER_CELL.get(), 1)) {
            player.sendSystemMessage(Component.literal("[ECHO-7] Activation failed. Power Cell missing during handoff.")
                .withStyle(ChatFormatting.RED));
            return;
        }
        
        level.setBlock(pos, state.setValue(ACTIVE, true), 3);
        activateStationForPlayer(player, pos);
        
        player.sendSystemMessage(Component.literal("[ECHO-7] Relay Station active. Radio route added to your network.")
            .withStyle(ChatFormatting.GREEN));
        
        // Reveal map area
        revealMapArea(level, pos, player);
        
        // Award research points
        if (player instanceof ServerPlayer serverPlayer) {
            com.knoxhack.echoashfallprotocol.research.ResearchData research =
                com.knoxhack.echoashfallprotocol.research.ResearchData.get(player);
            research.addPoints(10);
            com.knoxhack.echoashfallprotocol.research.ResearchData.saveAndSync(serverPlayer, research);
            AshfallFactionContractProgression.progressRepair(serverPlayer, "relay");
        }
    }
    
    private void openFastTravelUI(Player player, BlockPos currentPos) {
        activateStationForPlayer(player, currentPos);

        player.sendSystemMessage(Component.literal("[ECHO-7] Radio network online.")
            .withStyle(ChatFormatting.AQUA));
        
        // List available destinations
        var network = RadioNetwork.get(player);
        var destinations = network.getAvailableDestinations(currentPos);
        
        if (destinations.isEmpty()) {
            player.sendSystemMessage(Component.literal("[ECHO-7] No other active relay stations found.")
                .withStyle(ChatFormatting.RED));
            player.sendSystemMessage(Component.literal("Activate another Relay Station to create a return route.")
                .withStyle(ChatFormatting.GRAY));
        } else {
            if (player.isShiftKeyDown()) {
                RadioNetwork.StationInfo nearest = destinations.stream()
                    .min(java.util.Comparator.comparingDouble(dest -> dest.getPosition().distSqr(currentPos)))
                    .orElse(null);
                if (nearest != null) {
                    network.fastTravelTo(player, nearest.getId());
                    return;
                }
            }

            player.sendSystemMessage(Component.literal("Available relay destinations:")
                .withStyle(ChatFormatting.GREEN));
            for (var dest : destinations) {
                int distance = (int) Math.sqrt(dest.getPosition().distSqr(currentPos));
                player.sendSystemMessage(Component.literal("  - " + dest.getName() + " [" + distance + "m]")
                    .withStyle(ChatFormatting.YELLOW));
            }
            player.sendSystemMessage(Component.literal("Sneak-use this relay to travel to the nearest listed station. Cost: 1 Power Cell or Energy Cell.")
                .withStyle(ChatFormatting.GRAY));
        }
    }
    
    private void revealMapArea(Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            var quest = com.knoxhack.echoashfallprotocol.echo.QuestData.get(serverPlayer);
            quest.visitLocation("special", "relay:map_revealed");
            quest.addToArchive("[RELAY] Radio map sweep centered at " + pos.getX() + ", " + pos.getZ() + ".");
            com.knoxhack.echoashfallprotocol.echo.QuestData.saveAndSync(serverPlayer, quest);
        }
    }

    private void discoverStation(Player player, BlockPos pos) {
        RadioNetwork.StationInfo station = StationRegistry.getOrCreateStation(pos);
        RadioNetwork.get(player).discoverStation(station.getId());
    }

    private void activateStationForPlayer(Player player, BlockPos pos) {
        RadioNetwork.StationInfo station = StationRegistry.getOrCreateStation(pos);
        RadioNetwork.get(player).activateStation(station.getId());
    }
    
    /**
     * Check if station is active for fast travel
     */
    public static boolean isStationActive(BlockState state) {
        return state.getValue(ACTIVE);
    }
}
