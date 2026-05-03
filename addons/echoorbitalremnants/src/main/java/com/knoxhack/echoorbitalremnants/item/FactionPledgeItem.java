package com.knoxhack.echoorbitalremnants.item;

import com.knoxhack.echoorbitalremnants.progression.EchoTerminalProgress;
import com.knoxhack.echoorbitalremnants.progression.FactionStanding;
import com.knoxhack.echoorbitalremnants.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FactionPledgeItem extends Item {
    private final Faction faction;

    public FactionPledgeItem(Faction faction, Properties properties) {
        super(properties);
        this.faction = faction;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            EchoTerminalProgress progress = EchoTerminalProgress.get(player);
            boolean alreadyAligned = alreadyAligned(progress, faction);
            progress.alignFaction(player, faction);
            if (!alreadyAligned) {
                faction.grantReward(player);
            }
            if (!alreadyAligned && !player.hasInfiniteMaterials()) {
                player.getItemInHand(hand).shrink(1);
            }
            String message = alreadyAligned
                    ? "ECHO-7 // Faction alignment already active: " + faction.displayName + ". Contract remains on terminal."
                    : "ECHO-7 // Faction alignment accepted: " + faction.displayName + ". Contract assigned on terminal.";
            player.sendSystemMessage(Component.literal(message));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private static boolean alreadyAligned(EchoTerminalProgress progress, Faction faction) {
        return switch (faction) {
            case ORBITAL_REMNANT -> progress.orbitalRemnantStanding() == FactionStanding.ALIGNED;
            case VOID_SALVAGERS -> progress.voidSalvagerStanding() == FactionStanding.ALIGNED;
            case NEXUS_CHOIR -> progress.nexusChoirStanding() == FactionStanding.ALIGNED;
        };
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }

    public enum Faction {
        ORBITAL_REMNANT("Orbital Remnant") {
            @Override
            void grantReward(Player player) {
                give(player, new ItemStack(ModItems.OXYGEN_BOOSTER.get()));
                give(player, new ItemStack(ModItems.SUIT_SEALANT_PATCH.get(), 3));
            }
        },
        VOID_SALVAGERS("Void Salvagers") {
            @Override
            void grantReward(Player player) {
                give(player, new ItemStack(ModItems.ORBITAL_ALLOY.get(), 4));
                give(player, new ItemStack(ModItems.VACUUM_CIRCUIT.get(), 2));
                give(player, new ItemStack(ModItems.CARGO_BAY_MODULE.get()));
            }
        },
        NEXUS_CHOIR("Nexus Choir") {
            @Override
            void grantReward(Player player) {
                give(player, new ItemStack(ModItems.NEXUS_DUST.get(), 3));
                give(player, new ItemStack(ModItems.NEXUS_PULSE_BLADE.get()));
            }
        };

        private final String displayName;

        Faction(String displayName) {
            this.displayName = displayName;
        }

        abstract void grantReward(Player player);
    }
}
