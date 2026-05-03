package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.Config;
import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.echo.EchoMessages;
import com.knoxhack.echoashfallprotocol.entity.ModEntities;
import com.knoxhack.echoashfallprotocol.entity.ScoutDrone;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Scout Drone Item — deploys a player-owned ScoutDrone entity into the world.
 * Right-click air: deploy drone.
 * Shift+Right-click: cycle drone mode (if already deployed).
 */
public class ScoutDroneItem extends Item {

    public ScoutDroneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // If sneaking, try to find and toggle owner's existing drone
            if (player.isShiftKeyDown()) {
                ScoutDrone ownedDrone = findOwnedDrone(serverLevel, player);
                if (ownedDrone != null) {
                    ownedDrone.cycleMode();
                    recordScoutSupport(player, ownedDrone);
                    return InteractionResult.SUCCESS;
                } else {
                    player.sendSystemMessage(Component.literal(
                            "§c[ECHO-7 // DRONE]§r No deployed Scout Drone found. Deploy one first."));
                    return InteractionResult.FAIL;
                }
            }

            // Normal deploy
            Vec3 lookVec = player.getLookAngle();
            double spawnX = player.getX() + lookVec.x * 2.0;
            double spawnY = player.getY() + 1.5;
            double spawnZ = player.getZ() + lookVec.z * 2.0;

            ScoutDrone drone = new ScoutDrone(ModEntities.SCOUT_DRONE.get(), serverLevel);
            drone.setPos(spawnX, spawnY, spawnZ);
            drone.setOwner(player);
            serverLevel.addFreshEntity(drone);

            player.sendSystemMessage(Component.literal(
                    EchoMessages.getMessage(EchoMessages.Context.SCOUT_DRONE_DEPLOYED)));

            if (!player.isCreative()) {
                stack.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
            net.minecraft.world.entity.LivingEntity target, InteractionHand hand) {
        // Right-click on the drone itself to cycle its mode
        if (target instanceof ScoutDrone drone) {
            if (drone.getOwnerUUID() != null && drone.getOwnerUUID().equals(player.getUUID())) {
                drone.cycleMode();
                recordScoutSupport(player, drone);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private ScoutDrone findOwnedDrone(ServerLevel level, Player player) {
        for (ScoutDrone drone : level.getEntitiesOfClass(ScoutDrone.class,
                player.getBoundingBox().inflate(64.0))) {
            if (drone.getOwnerUUID() != null && drone.getOwnerUUID().equals(player.getUUID())) {
                return drone;
            }
        }
        return null;
    }

    private void recordScoutSupport(Player player, ScoutDrone drone) {
        if (!(player instanceof ServerPlayer serverPlayer) || drone.getMode() != ScoutDrone.DroneMode.SCAVENGE) {
            return;
        }
        QuestData quest = QuestData.get(serverPlayer);
        boolean changed = false;
        if (!quest.hasVisitedLocation("special", "drone:scout_mode")) {
            quest.visitLocation("special", "drone:scout_mode");
            changed = true;
        }
        if (!quest.hasVisitedLocation("special", "drone:intel_recovered")) {
            quest.visitLocation("special", "drone:intel_recovered");
            changed = true;
        }
        if (changed) {
            QuestData.saveAndSync(serverPlayer, quest);
            serverPlayer.sendSystemMessage(Component.literal("\u00A7b[ECHO-7 // DRONE]\u00A7r Scout route intel recorded."), true);
        }
    }
}
