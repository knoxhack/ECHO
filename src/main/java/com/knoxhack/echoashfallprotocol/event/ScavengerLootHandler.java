package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import com.knoxhack.echoashfallprotocol.research.PerkEffectHandler;
import com.knoxhack.echoashfallprotocol.world.POIScannerService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wires scavenger loot perks into container looting and combat drops.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public final class ScavengerLootHandler {

    private static final Set<String> AWARDED_CONTAINER_KEYS = ConcurrentHashMap.newKeySet();

    private ScavengerLootHandler() {
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (!(blockEntity instanceof RandomizableContainerBlockEntity)) {
            return;
        }

        recordCacheLooted(player);

        float chance = PerkEffectHandler.getLootBonusChance(player);
        if (chance <= 0.0F) {
            return;
        }

        String key = player.getUUID() + "|" + player.level().dimension() + "|" + pos.asLong();
        if (!AWARDED_CONTAINER_KEYS.add(key)) {
            return;
        }

        int bonusRolls = PerkEffectHandler.getLootBonusRolls(player);
        List<ItemStack> rewards = new ArrayList<>();
        for (int i = 0; i < bonusRolls; i++) {
            if (player.getRandom().nextFloat() <= chance) {
                rewards.add(generateScavengerBonus(player));
            }
        }

        if (rewards.isEmpty()) {
            return;
        }

        for (ItemStack reward : rewards) {
            ItemStack pending = reward.copy();
            if (!player.getInventory().add(pending)) {
                player.drop(pending, false);
            }
        }

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§6[ECHO-7]§r Scavenger perk recovered bonus salvage from this cache."));
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        float chance = PerkEffectHandler.getLootBonusChance(player);
        if (chance <= 0.0F || event.getDrops().isEmpty() || player.getRandom().nextFloat() > chance) {
            return;
        }

        ItemStack bonusDrop = ItemStack.EMPTY;
        ItemEntity sourceDrop = null;
        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if (!stack.isEmpty()) {
                bonusDrop = stack.copyWithCount(Math.min(1, stack.getMaxStackSize()));
                sourceDrop = drop;
                break;
            }
        }

        if (!bonusDrop.isEmpty() && sourceDrop != null) {
            event.getDrops().add(new ItemEntity(player.level(), sourceDrop.getX(), sourceDrop.getY(), sourceDrop.getZ(), bonusDrop));
        }
    }

    private static ItemStack generateScavengerBonus(ServerPlayer player) {
        return switch (player.getRandom().nextInt(6)) {
            case 0 -> new ItemStack(ModItems.SCRAP_METAL.get(), 2);
            case 1 -> new ItemStack(ModItems.SCRAP_CIRCUIT.get(), 1);
            case 2 -> new ItemStack(ModItems.SCRAP_WIRE.get(), 2);
            case 3 -> new ItemStack(ModItems.ENERGY_CELL.get(), 1);
            case 4 -> new ItemStack(ModItems.FILTER_CARTRIDGE_BASIC.get(), 1);
            default -> new ItemStack(ModItems.RAD_AWAY.get(), 1);
        };
    }

    private static void recordCacheLooted(ServerPlayer player) {
        POIScannerService.ScanHit hit = POIScannerService.scan(player);
        if (hit == null || hit.distance() > POIScannerService.DISCOVERY_RADIUS * 1.5D) {
            return;
        }

        QuestData quest = QuestData.get(player);
        if (quest.hasPOIState(hit.id(), QuestData.POIObjectiveState.CACHE_LOOTED)) {
            return;
        }

        quest.recordPOIState(hit.id(), QuestData.POIObjectiveState.SCANNED);
        if (hit.distance() <= POIScannerService.DISCOVERY_RADIUS) {
            quest.recordPOIState(hit.id(), QuestData.POIObjectiveState.ENTERED);
            quest.visitLocation("poi", hit.id());
        }
        quest.recordPOIState(hit.id(), QuestData.POIObjectiveState.CACHE_LOOTED);
        QuestData.saveAndSync(player, quest);
        player.sendSystemMessage(Component.literal("\u00A76[ECHO-7]\u00A7r Cache state updated: " + hit.displayName()), true);
    }
}
