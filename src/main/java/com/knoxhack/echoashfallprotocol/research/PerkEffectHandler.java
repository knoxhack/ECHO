package com.knoxhack.echoashfallprotocol.research;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies active research perk effects to players.
 * Handles weapon damage, machine efficiency, loot bonuses, health regen, and radiation resistance.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class PerkEffectHandler {

    // Track last combat time for out-of-combat detection
    private static final Map<UUID, Long> lastCombatTime = new HashMap<>();
    private static final long COMBAT_COOLDOWN_TICKS = 200; // 10 seconds

    /**
     * WEAPON DAMAGE PERKS: +10/20/30% damage
     * Applied when player deals damage
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        ResearchData researchData = ResearchData.get(player);
        float damageMultiplier = 1.0f;

        // Check for weapon damage perks (stacking)
        if (hasPerk(researchData, "radwarden.weapon_damage.3")) {
            damageMultiplier = 1.30f; // +30%
        } else if (hasPerk(researchData, "radwarden.weapon_damage.2")) {
            damageMultiplier = 1.20f; // +20%
        } else if (hasPerk(researchData, "radwarden.weapon_damage.1")) {
            damageMultiplier = 1.10f; // +10%
        }

        if (damageMultiplier > 1.0f) {
            event.setNewDamage(event.getNewDamage() * damageMultiplier);
        }

        // Track combat for health regen perk
        lastCombatTime.put(player.getUUID(), player.level().getGameTime());
    }

    /**
     * ARMOR DURABILITY PERKS: -15/30% durability loss
     * Applied when player takes damage - reduces armor durability loss
     */
    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getNewDamage() <= 0) return;

        ResearchData researchData = ResearchData.get(player);
        float durabilityMultiplier = 1.0f;

        // Check for armor durability perks
        if (hasPerk(researchData, "radwarden.armor_durability.2")) {
            durabilityMultiplier = 0.70f; // 30% reduction
        } else if (hasPerk(researchData, "radwarden.armor_durability.1")) {
            durabilityMultiplier = 0.85f; // 15% reduction
        }

        if (durabilityMultiplier < 1.0f) {
            // Apply reduced durability damage to all armor pieces
            for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                if (slot.getType() == net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR) {
                    net.minecraft.world.item.ItemStack armor = player.getItemBySlot(slot);
                    if (!armor.isEmpty() && armor.isDamageableItem()) {
                        // Chance to skip durability damage entirely based on reduction
                        if (player.getRandom().nextFloat() >= durabilityMultiplier) {
                            // Restore 1 durability point that was just consumed
                            int currentDamage = armor.getDamageValue();
                            if (currentDamage > 0) {
                                armor.setDamageValue(currentDamage - 1);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * HEALTH REGEN PERKS: Regeneration when not in combat
     * Rapid Recovery I: Slow regen out of combat
     * Rapid Recovery II: Faster regen, reduced regen in combat
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // Only apply every 20 ticks (1 second)
        if (player.level().getGameTime() % 20 != 0) return;

        ResearchData researchData = ResearchData.get(serverPlayer);
        long currentTime = player.level().getGameTime();
        long lastCombat = lastCombatTime.getOrDefault(player.getUUID(), 0L);
        boolean inCombat = (currentTime - lastCombat) < COMBAT_COOLDOWN_TICKS;

        // HEALTH REGEN PERKS
        if (hasPerk(researchData, "sporebound.regen.2")) {
            // Tier 2: Faster regen out of combat, reduced in combat
            if (!inCombat && player.getHealth() < player.getMaxHealth()) {
                player.heal(0.5f); // 0.5 HP per second out of combat
            } else if (inCombat && player.getHealth() < player.getMaxHealth()) {
                player.heal(0.2f); // 0.2 HP per second in combat
            }
        } else if (hasPerk(researchData, "sporebound.regen.1")) {
            // Tier 1: Slow regen only out of combat
            if (!inCombat && player.getHealth() < player.getMaxHealth()) {
                player.heal(0.3f); // 0.3 HP per second out of combat
            }
        }
    }

    /**
     * BETTER LOOT PERKS: +15/30% extra loot from containers
     * Applied by ScavengerLootHandler plus mission and faction reward paths.
     */
    public static void applyLootBonus(ServerPlayer player, net.minecraft.world.item.ItemStack item) {
        if (item.isEmpty()) {
            return;
        }

        float chance = getLootBonusChance(player);
        if (chance <= 0.0f) {
            return;
        }

        for (int i = 0; i < getLootBonusRolls(player); i++) {
            if (player.getRandom().nextFloat() <= chance && item.getCount() < item.getMaxStackSize()) {
                item.grow(1);
            }
        }
    }

    public static float getLootBonusChance(ServerPlayer player) {
        ResearchData researchData = ResearchData.get(player);

        if (hasPerk(researchData, "crashbreak.loot.2")) {
            return 0.30f;
        } else if (hasPerk(researchData, "crashbreak.loot.1")) {
            return 0.15f;
        }
        return 0.0f;
    }

    public static int getLootBonusRolls(ServerPlayer player) {
        ResearchData researchData = ResearchData.get(player);
        return hasPerk(researchData, "crashbreak.loot.2") ? 2 : hasPerk(researchData, "crashbreak.loot.1") ? 1 : 0;
    }

    /**
     * MACHINE EFFICIENCY PERKS: 10/20% faster processing
     * These are checked by machine block entities when calculating processing time
     * See: Machine block entity classes should call getMachineSpeedMultiplier()
     */
    public static float getMachineSpeedMultiplier(ServerPlayer player) {
        return getMachineSpeedMultiplier((Player) player);
    }

    public static float getMachineSpeedMultiplier(Player player) {
        ResearchData researchData = ResearchData.get(player);
        
        if (hasPerk(researchData, "radwarden.machine_eff.2")) {
            return 1.20f; // 20% faster
        } else if (hasPerk(researchData, "radwarden.machine_eff.1")) {
            return 1.10f; // 10% faster
        }
        return 1.0f; // No bonus
    }

    /**
     * RADIATION RESISTANCE PERKS: +20/40/60% resistance
     * Checked by radiation system when applying radiation damage
     */
    public static float getRadiationResistanceMultiplier(ServerPlayer player) {
        ResearchData researchData = ResearchData.get(player);
        
        if (hasPerk(researchData, "sporebound.rad_resist.3")) {
            return 0.40f; // 60% resistance = 40% damage taken
        } else if (hasPerk(researchData, "sporebound.rad_resist.2")) {
            return 0.60f; // 40% resistance = 60% damage taken
        } else if (hasPerk(researchData, "sporebound.rad_resist.1")) {
            return 0.80f; // 20% resistance = 80% damage taken
        }
        return 1.0f; // No resistance
    }

    /**
     * CHEAPER TRADES PERKS: 10/20% better prices
     * Checked by trading system when calculating costs
     */
    public static float getTradePriceMultiplier(ServerPlayer player) {
        ResearchData researchData = ResearchData.get(player);
        
        if (hasPerk(researchData, "crashbreak.trade.2")) {
            return 0.80f; // 20% cheaper
        } else if (hasPerk(researchData, "crashbreak.trade.1")) {
            return 0.90f; // 10% cheaper
        }
        return 1.0f; // Normal price
    }

    /**
     * FASTER SCAVENGING PERKS: 20/40% faster looting
     * Checked by container opening logic
     */
    public static float getLootingSpeedMultiplier(ServerPlayer player) {
        return getLootingSpeedMultiplier((Player) player);
    }

    public static float getLootingSpeedMultiplier(Player player) {
        ResearchData researchData = ResearchData.get(player);
        
        if (hasPerk(researchData, "crashbreak.speed.2")) {
            return 1.40f; // 40% faster
        } else if (hasPerk(researchData, "crashbreak.speed.1")) {
            return 1.20f; // 20% faster
        }
        return 1.0f; // Normal speed
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        float multiplier = getLootingSpeedMultiplier(player);
        if (multiplier <= 1.0F || !isScavengeBlock(event.getState())) {
            return;
        }

        event.setNewSpeed(event.getNewSpeed() * multiplier);
    }

    public static boolean hasResearchPerk(Player player, String perkId) {
        return ResearchData.get(player).hasPerk(perkId);
    }

    public static float getMutationSideEffectMultiplier(ServerPlayer player) {
        ResearchData researchData = ResearchData.get(player);
        if (hasPerk(researchData, "sporebound.synergy.2")) {
            return 0.0F;
        }
        if (hasPerk(researchData, "sporebound.synergy.1")) {
            return 0.45F;
        }
        return 1.0F;
    }

    private static boolean isScavengeBlock(net.minecraft.world.level.block.state.BlockState state) {
        String id = state.getBlock().getDescriptionId().toLowerCase(java.util.Locale.ROOT);
        return id.contains("debris")
            || id.contains("rubble")
            || id.contains("scrap")
            || id.contains("ash")
            || id.contains("waste")
            || id.contains("barrel");
    }

    /**
     * Helper method to check if player has a specific perk unlocked
     */
    private static boolean hasPerk(ResearchData data, String perkId) {
        return data.hasPerk(perkId);
    }

    /**
     * Initialize the handler
     */
    public static void init() {
        // Event bus registration happens via @EventBusSubscriber
    }
}
