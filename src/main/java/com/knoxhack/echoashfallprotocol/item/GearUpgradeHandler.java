package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Tier 5 Gear Upgrade System
 * Allows players to upgrade path-specific weapons using Nexus Crystals
 * 
 * Upgrade tiers:
 * - +1 damage per crystal (max +5)
 * - Cosmetic effects at +3 and +5 (glowing runes, particles)
 */
@EventBusSubscriber(modid = com.knoxhack.echoashfallprotocol.EchoAshfallProtocol.MODID)
public class GearUpgradeHandler {

    private static final int MAX_UPGRADE = 5;
    private static final float DAMAGE_BONUS_PER_LEVEL = 1.0f;

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();

        // Check if player is holding a Nexus Crystal in main hand and upgradeable item in offhand
        ItemStack offhandItem = player.getOffhandItem();
        
        if (heldItem.is(ModItems.NEXUS_CRYSTAL.get()) && isUpgradeable(offhandItem)) {
            // Attempt upgrade
            if (player instanceof ServerPlayer serverPlayer) {
                attemptUpgrade(serverPlayer, offhandItem);
                // Cancel the crystal use
                event.setCanceled(true);
            }
        } else if (heldItem.is(ModItems.NEXUS_CRYSTAL.get()) && isUpgradeable(heldItem)) {
            // Attempt upgrade (crystal in main, item itself)
            if (player instanceof ServerPlayer serverPlayer) {
                attemptUpgrade(serverPlayer, heldItem);
                event.setCanceled(true);
            }
        }
    }

    /**
     * Check if an item can be upgraded
     */
    public static boolean isUpgradeable(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // Check upgradeable items
        return stack.is(ModItems.NEXUS_BLADE.get()) ||
               stack.is(ModItems.NEXUS_ANNIHILATOR.get()) ||
               stack.is(ModItems.ALLOY_BLADE.get());
    }

    /**
     * Get current upgrade level of an item
     */
    public static int getUpgradeLevel(ItemStack stack) {
        return com.knoxhack.echoashfallprotocol.item.upgrade.GearUpgradeHandler.getUpgradeLevel(stack);
    }

    /**
     * Attempt to upgrade an item
     */
    private static void attemptUpgrade(ServerPlayer player, ItemStack item) {
        int currentLevel = getUpgradeLevel(item);

        if (currentLevel >= MAX_UPGRADE) {
            player.sendSystemMessage(Component.literal(
                "§cThis item is already at maximum upgrade level (+" + MAX_UPGRADE + ")"
            ));
            return;
        }

        // Consume one Nexus Crystal
        ItemStack crystalStack = findCrystalInInventory(player);
        if (crystalStack.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                "§cYou need a Nexus Crystal to upgrade this item"
            ));
            return;
        }

        // Consume crystal
        crystalStack.shrink(1);

        // Apply upgrade
        int newLevel = currentLevel + 1;
        com.knoxhack.echoashfallprotocol.item.upgrade.GearUpgradeHandler.setUpgradeLevel(item, newLevel);

        // Notify player
        player.sendSystemMessage(Component.literal(
            "§aItem upgraded to +" + newLevel + "! Max +" + MAX_UPGRADE
        ));

        // Check for cosmetic thresholds
        if (newLevel == 3) {
            player.sendSystemMessage(Component.literal(
                "§dYour weapon begins to glow with ethereal runes..."
            ));
        } else if (newLevel == 5) {
            player.sendSystemMessage(Component.literal(
                "§6Your weapon reaches maximum power! A particle trail follows your strikes."
            ));
        }
    }

    /**
     * Find a Nexus Crystal in player inventory
     */
    private static ItemStack findCrystalInInventory(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.NEXUS_CRYSTAL.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Calculate damage bonus from upgrades
     */
    public static float getDamageBonus(ItemStack weapon) {
        int level = getUpgradeLevel(weapon);
        return level * DAMAGE_BONUS_PER_LEVEL;
    }

    /**
     * Check if item has cosmetic glow effect (level 3+)
     */
    public static boolean hasGlowEffect(ItemStack stack) {
        return getUpgradeLevel(stack) >= 3;
    }

    /**
     * Check if item has particle trail effect (level 5)
     */
    public static boolean hasParticleTrail(ItemStack stack) {
        return getUpgradeLevel(stack) >= 5;
    }
}
