package com.knoxhack.echoashfallprotocol.item;

import com.knoxhack.echoashfallprotocol.Config;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.survival.SurvivalData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Filter Cartridge — consumed by the Gas Mask air system.
 * Tiered cartridges refill the shared gas mask hazard-zone capacity.
 */
public class FilterCartridgeItem extends Item {

    public enum Tier {
        BASIC(1, "Basic", 0xAAAA00),
        ADVANCED(2, "Advanced", 0x5555FF),
        ELITE(3, "Elite", 0xFF55FF);

        private final int level;
        private final String displayName;
        private final int color;

        Tier(int level, String displayName, int color) {
            this.level = level;
            this.displayName = displayName;
            this.color = color;
        }

        public int getLevel() { return level; }
        public String getDisplayName() { return displayName; }
        public int getColor() { return color; }
    }

    private final Tier tier;

    public FilterCartridgeItem(Properties properties, Tier tier) {
        super(properties);
        this.tier = tier;
    }

    public Tier getFilterTier() { return tier; }

    /**
     * Legacy tooltip value retained for compatibility with old item text.
     * Runtime filter drain is now configured globally and only active in toxic zones.
     */
    public float getDegradationRate() {
        return switch (tier) {
            case BASIC -> 1.0f;
            case ADVANCED -> 0.5f;
            case ELITE -> 0.2f;
        };
    }

    /**
     * Filter life amount provided by this cartridge.
     * BASIC: +300, ADVANCED: +600, ELITE: +1000
     */
    public int getFilterAmount() {
        return switch (tier) {
            case BASIC -> 300;
            case ADVANCED -> 600;
            case ELITE -> 1000;
        };
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Check if player has gas mask equipped
            ItemStack headSlot = player.getItemBySlot(EquipmentSlot.HEAD);
            if (!(headSlot.getItem() instanceof GasMaskItem)) {
                player.sendSystemMessage(Component.literal(
                        "§c[ECHO-7]§r Gas Mask required. Equip a Gas Mask before installing filter cartridges."));
                return InteractionResult.FAIL;
            }

            // Get survival data and refill air filter
            SurvivalData survivalData = player.getData(ModAttachments.SURVIVAL_DATA.get());
            int currentFilter = survivalData.getAirFilterLife();
            int maxFilter = SurvivalData.MAX_AIR_FILTER;

            if (currentFilter >= maxFilter) {
                player.sendSystemMessage(Component.literal(
                        "§a[ECHO-7]§r Air filter already at maximum capacity."));
                return InteractionResult.FAIL;
            }

            // Calculate refill amount (capped at max)
            int refillAmount = getFilterAmount();
            int newFilterLife = Math.min(currentFilter + refillAmount, maxFilter);
            survivalData.setAirFilterLife(newFilterLife);
            survivalData.setFilterTier(tier.getLevel());
            player.setData(ModAttachments.SURVIVAL_DATA.get(), survivalData);

            // Calculate actual amount added (for message)
            int actualAdded = newFilterLife - currentFilter;

            // Send ECHO-7 confirmation
            player.sendSystemMessage(Component.literal(
                    String.format("§b[ECHO-7]§r %s filter installed. Air capacity restored: +%d%%",
                            tier.getDisplayName(), (actualAdded * 100) / maxFilter)));

            // Consume the cartridge
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}
