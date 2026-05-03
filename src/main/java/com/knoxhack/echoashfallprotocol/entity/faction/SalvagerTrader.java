package com.knoxhack.echoashfallprotocol.entity.faction;

import com.knoxhack.echoashfallprotocol.faction.ReputationData;
import com.knoxhack.echoashfallprotocol.research.PerkEffectHandler;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Salvager Trader - Faction mob for the Rust Market.
 * Neutral mob that trades with players.
 * Offers better trades to players with positive Salvager reputation.
 * Hostile only if attacked or reputation is very negative.
 */
public class SalvagerTrader extends Monster {
    
    public SalvagerTrader(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false) {
            @Override
            public boolean canUse() {
                // Only attack if reputation is very negative
                return super.canUse() && shouldAttackPlayer();
            }
        });
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D, 0.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        // Attack players (reputation check done in attack goal)
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    
    private boolean shouldAttackPlayer() {
        // Check nearest player
        Player nearestPlayer = this.level().getNearestPlayer(this, 16.0);
        if (nearestPlayer != null) {
            return shouldAttackPlayer(nearestPlayer);
        }
        return false;
    }
    
    private boolean shouldAttackPlayer(Player player) {
        // Get player's reputation with Rust Market (Salvagers)
        ReputationData factionData = player.getData(ModAttachments.REPUTATION_DATA.get());
        int reputation = factionData.getReputation(ReputationData.Faction.SALVAGERS);
        
        // Attack only if reputation is very negative (hated)
        return reputation < -50;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            // Get reputation for trade checks
            ReputationData factionData = player.getData(ModAttachments.REPUTATION_DATA.get());
            int reputation = factionData.getReputation(ReputationData.Faction.SALVAGERS);
            
            ItemStack held = player.getItemInHand(hand);
            float tradeMultiplier = player instanceof ServerPlayer serverPlayer
                ? PerkEffectHandler.getTradePriceMultiplier(serverPlayer)
                : 1.0F;
            
            // Trade 1: 12 Scrap Metal -> 1 Circuit Board
            int scrapForCircuit = adjustedCost(12, tradeMultiplier);
            if (held.is(ModItems.SCRAP_METAL.get()) && held.getCount() >= scrapForCircuit) {
                held.shrink(scrapForCircuit);
                player.addItem(new ItemStack(ModItems.CIRCUIT_BOARD.get()));
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Trade]§r 12 Scrap Metal -> 1 Circuit Board"));
                return InteractionResult.SUCCESS;
            }
            
            // Trade 2: 8 Dense Alloy Chunks -> 1 Energy Cell
            int alloyForCell = adjustedCost(8, tradeMultiplier);
            if (held.is(ModItems.DENSE_ALLOY_CHUNK.get()) && held.getCount() >= alloyForCell) {
                held.shrink(alloyForCell);
                player.addItem(new ItemStack(ModItems.ENERGY_CELL.get()));
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Trade]§r 8 Dense Alloy Chunks -> 1 Energy Cell"));
                return InteractionResult.SUCCESS;
            }
            
            // Trade 3 (VIP): 6 Scrap Circuits -> 1 Schematic Fragment (requires rep > 20)
            int circuitForFragment = adjustedCost(6, tradeMultiplier);
            if (reputation > 20 && held.is(ModItems.SCRAP_CIRCUIT.get()) && held.getCount() >= circuitForFragment) {
                held.shrink(circuitForFragment);
                player.addItem(new ItemStack(ModItems.SCHEMATIC_FRAGMENT.get()));
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Trade]§r 6 Scrap Circuits -> 1 Schematic Fragment"));
                return InteractionResult.SUCCESS;
            }
            
            // VIP Trade: 20 Scrap Metal -> 2 Circuit Boards (requires rep > 20)
            int scrapForVip = adjustedCost(20, tradeMultiplier);
            if (reputation > 20 && held.is(ModItems.SCRAP_METAL.get()) && held.getCount() >= scrapForVip) {
                held.shrink(scrapForVip);
                player.addItem(new ItemStack(ModItems.CIRCUIT_BOARD.get(), 2));
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Trade]§r 20 Scrap Metal -> 2 Circuit Boards (VIP)"));
                return InteractionResult.SUCCESS;
            }
            
            // VIP Import Trade: 12 Emeralds -> 1 recovered biome good (requires rep > 20)
            int emeraldsForBiomeGood = adjustedCost(12, tradeMultiplier);
            if (reputation > 20 && held.is(Items.EMERALD) && held.getCount() >= emeraldsForBiomeGood) {
                held.shrink(emeraldsForBiomeGood);
                player.addItem(randomImportedBiomeGood());
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[Trade] 12 Emeralds -> 1 Imported Biome Good (VIP)"));
                return InteractionResult.SUCCESS;
            }

            // VIP Restoration Trade: 8 Emeralds -> 1 recovered vanilla sapling (requires rep > 20)
            int emeraldsForSapling = adjustedCost(8, tradeMultiplier);
            if (reputation > 20 && held.is(Items.EMERALD) && held.getCount() >= emeraldsForSapling) {
                held.shrink(emeraldsForSapling);
                player.addItem(randomRecoveredSapling());
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Trade]§r 8 Emeralds -> 1 Recovered Sapling (VIP)"));
                return InteractionResult.SUCCESS;
            }
            
            if (tryEliteRareSchematicTrade(player, held, reputation, tradeMultiplier)) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Trade]§r 4 Schematic Fragments -> Rare Tech Schematic"));
                return InteractionResult.SUCCESS;
            }
            
            // If no trade executed, show menu
            if (hand == InteractionHand.MAIN_HAND) {
                openTradeMenu(player);
                return InteractionResult.SUCCESS;
            }
        }
        
        return InteractionResult.PASS;
    }
    
    private void openTradeMenu(Player player) {
        // Get reputation for trade modifiers
        ReputationData factionData = player.getData(ModAttachments.REPUTATION_DATA.get());
        int reputation = factionData.getReputation(ReputationData.Faction.SALVAGERS);
        
        // Send trade info to player
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("=== Rust Market Trader ==="));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Reputation: " + reputation));
        
        // Display available trades
        displayTrades(player, reputation);
    }
    
    private void displayTrades(Player player, int reputation) {
        float tradeMultiplier = player instanceof ServerPlayer serverPlayer
            ? PerkEffectHandler.getTradePriceMultiplier(serverPlayer)
            : 1.0F;

        // Trade offers based on reputation
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Available Trades:"));
        
        // Basic trades always available
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[1] " + adjustedCost(12, tradeMultiplier) + " Scrap Metal -> 1 Circuit Board"));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[2] " + adjustedCost(8, tradeMultiplier) + " Dense Alloy Chunks -> 1 Energy Cell"));
        
        // Better trades for positive reputation
        if (reputation > 20) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[3] " + adjustedCost(6, tradeMultiplier) + " Scrap Circuits -> 1 Schematic Fragment"));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[VIP] " + adjustedCost(20, tradeMultiplier) + " Scrap Metal -> 2 Circuit Boards (Reputation Bonus)"));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[VIP] " + adjustedCost(12, tradeMultiplier) + " Emeralds -> 1 Imported Biome Good"));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[VIP] " + adjustedCost(8, tradeMultiplier) + " Emeralds -> 1 Recovered Sapling"));
        }
        
        // Premium trades for high reputation
        if (reputation > 50) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("[ELITE] " + adjustedCost(4, tradeMultiplier) + " Schematic Fragments -> 1 Rare Tech Schematic"));
        }

        if (tradeMultiplier < 1.0F) {
            int discount = Math.round((1.0F - tradeMultiplier) * 100.0F);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Research Discount: " + discount + "%"));
        }
        
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Use /trade <number> to accept a trade"));
    }

    private static int adjustedCost(int baseCost, float multiplier) {
        return Math.max(1, (int) Math.ceil(baseCost * multiplier));
    }

    public static boolean tryEliteRareSchematicTrade(Player player, ItemStack held, int reputation, float tradeMultiplier) {
        int fragmentsForRare = adjustedCost(4, tradeMultiplier);
        if (reputation <= 50 || !held.is(ModItems.SCHEMATIC_FRAGMENT.get()) || held.getCount() < fragmentsForRare) {
            return false;
        }
        held.shrink(fragmentsForRare);
        player.addItem(new ItemStack(ModItems.RARE_TECH_SCHEMATIC.get()));
        return true;
    }

    private ItemStack randomRecoveredSapling() {
        return switch (this.random.nextInt(3)) {
            case 0 -> new ItemStack(Items.BIRCH_SAPLING);
            case 1 -> new ItemStack(Items.SPRUCE_SAPLING);
            default -> new ItemStack(Items.OAK_SAPLING);
        };
    }

    private ItemStack randomImportedBiomeGood() {
        return switch (this.random.nextInt(12)) {
            case 0 -> new ItemStack(Items.SUGAR_CANE, 3);
            case 1 -> new ItemStack(Items.CACTUS, 2);
            case 2 -> new ItemStack(Items.BAMBOO, 3);
            case 3 -> new ItemStack(Items.COCOA_BEANS, 3);
            case 4 -> new ItemStack(Items.KELP, 4);
            case 5 -> new ItemStack(Items.SEAGRASS, 2);
            case 6 -> new ItemStack(Items.INK_SAC, 3);
            case 7 -> new ItemStack(Items.CLAY_BALL, 6);
            case 8 -> new ItemStack(Items.PUMPKIN_SEEDS, 2);
            case 9 -> new ItemStack(Items.MELON_SEEDS, 2);
            case 10 -> new ItemStack(Items.LILY_PAD, 2);
            default -> new ItemStack(Items.BROWN_MUSHROOM, 2);
        };
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, wasRecentlyHit);
        
        // Drop salvager goods
        this.spawnAtLocation(level, ModItems.SCRAP_METAL.get());
        
        if (this.random.nextFloat() < 0.5f) {
            this.spawnAtLocation(level, ModItems.SCRAP_CIRCUIT.get());
        }
        
        if (this.random.nextFloat() < 0.3f) {
            this.spawnAtLocation(level, ModItems.CIRCUIT_BOARD.get());
        }
        
        // Chance to drop trader's pouch
        if (this.random.nextFloat() < 0.1f) {
            this.spawnAtLocation(level, Items.EMERALD);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ARMOR, 1.0);
    }
}
