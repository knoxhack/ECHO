package com.knoxhack.echoashfallprotocol.item.upgrade;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * Handles Tier 5 gear upgrades via anvil.
 * Nexus weapons can be upgraded with Nexus Crystals: +1 damage per crystal, max +5.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class GearUpgradeHandler {

    private static final String NBT_UPGRADE_KEY = "nexus_upgrades";
    private static final int MAX_UPGRADES = 5;

    /**
     * Check if an item is a Nexus upgradable weapon
     */
    public static boolean isNexusWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ModItems.NEXUS_BLADE.get()) ||
               stack.is(ModItems.NEXUS_ANNIHILATOR.get()) ||
               stack.is(ModItems.NEXUS_BLADE.get()); // Using Nexus Blade as proxy for CONTROL path
    }

    /**
     * Get current upgrade level from item NBT
     */
    public static int getUpgradeLevel(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            // Use getIntOr with default value
            return tag.getIntOr(NBT_UPGRADE_KEY, 0);
        }
        return 0;
    }

    /**
     * Set upgrade level on item NBT
     */
    public static void setUpgradeLevel(ItemStack stack, int level) {
        int cappedLevel = Math.min(MAX_UPGRADES, Math.max(0, level));
        
        CompoundTag tag = new CompoundTag();
        CustomData existingData = stack.get(DataComponents.CUSTOM_DATA);
        if (existingData != null) {
            tag = existingData.copyTag();
        }
        
        tag.putInt(NBT_UPGRADE_KEY, cappedLevel);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        
        // Add lore indicating upgrade level
        updateUpgradeLore(stack, cappedLevel);
    }

    /**
     * Update item lore to show upgrade level
     */
    private static void updateUpgradeLore(ItemStack stack, int level) {
        if (level <= 0) return;
        
        // The lore is handled via tooltip events in the item classes
        // This is just a marker that the upgrade exists
    }

    /**
     * Calculate bonus damage from upgrades
     */
    public static float getBonusDamage(ItemStack stack) {
        return getUpgradeLevel(stack) * 1.0f; // +1 damage per level
    }

    /**
     * Handle anvil updates for gear upgrades
     */
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        // Check if this is a Nexus weapon upgrade
        if (isNexusWeapon(left) && right.is(ModItems.NEXUS_CRYSTAL.get())) {
            int currentLevel = getUpgradeLevel(left);
            
            if (currentLevel >= MAX_UPGRADES) {
                // Maxed out - no upgrade possible
                event.setCanceled(true);
                return;
            }

            // Create upgraded output
            ItemStack output = left.copy();
            setUpgradeLevel(output, currentLevel + 1);

            event.setOutput(output);
            // Cost is handled by setOutput, material cost by consuming right item
        }
    }
}
