package com.knoxhack.echorecovery.grave;

import com.knoxhack.echorecovery.EchoRecovery;
import com.knoxhack.echorecovery.block.entity.GraveBlockEntity;
import com.knoxhack.echorecovery.config.RecoveryConfig;
import com.knoxhack.echorecovery.data.RecoveryWorldData;
import com.knoxhack.echorecovery.integration.RecoveryIntegrationDispatcher;
import com.knoxhack.echorecovery.item.GraveKeyItem;
import com.knoxhack.echorecovery.registry.ModBlocks;
import com.knoxhack.echorecovery.registry.ModItems;
import com.knoxhack.echorecovery.registry.ModSounds;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class DeathHandler {
    private DeathHandler() {}

    public static void register() {
        NeoForge.EVENT_BUS.register(DeathHandler.class);
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!RecoveryConfig.ENABLE_GRAVES.get()) {
            return;
        }
        createGrave(player, event.getSource().getLocalizedDeathMessage(player).getString());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }
        if (event.getOriginal() instanceof ServerPlayer oldPlayer && event.getEntity() instanceof ServerPlayer newPlayer) {
            if (RecoveryConfig.GRAVE_KEY_ENABLED.get()) {
                for (int i = 0; i < oldPlayer.getInventory().getContainerSize(); i++) {
                    ItemStack stack = oldPlayer.getInventory().getItem(i);
                    if (stack.is(ModItems.GRAVE_KEY.get())) {
                        newPlayer.getInventory().add(stack.copy());
                    }
                }
            }
        }
    }

    private static void createGrave(ServerPlayer player, String deathCause) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos origin = player.blockPosition();
        boolean voidDeath = player.getY() < level.getMinY();
        boolean lavaDeath = player.isInLava();

        if (voidDeath && !RecoveryConfig.CREATE_GRAVE_ON_VOID_DEATH.get()) {
            return;
        }
        if (lavaDeath && !RecoveryConfig.CREATE_GRAVE_ON_LAVA_DEATH.get()) {
            return;
        }
        if (voidDeath && RecoveryConfig.VOID_DEATH_MODE.get() == RecoveryConfig.VoidDeathMode.DISABLED) {
            return;
        }

        if (voidDeath) {
            origin = GraveManager.resolveVoidDeathPosition(player);
        }

        BlockPos gravePos = GraveManager.findSafePosition(level, origin);
        if (gravePos.getY() < level.getMinY() || gravePos.getY() > level.getMinY() + level.getHeight()) {
            gravePos = level.getRespawnData().pos();
        }

        BlockState state = ModBlocks.GRAVE.get().defaultBlockState();
        level.setBlock(gravePos, state, 3);

        if (!(level.getBlockEntity(gravePos) instanceof GraveBlockEntity grave)) {
            EchoRecovery.LOGGER.error("Failed to create grave block entity at {}", gravePos);
            return;
        }

        UUID graveId = UUID.randomUUID();
        grave.setOwner(player.getUUID(), player.getScoreboardName());
        grave.setCreatedAt(System.currentTimeMillis());
        grave.setDeathCause(deathCause);
        grave.setDimension(level.dimension().identifier().toString());
        grave.setDeathMessage(deathCause);

        int slot = 0;
        if (RecoveryConfig.STORE_ITEMS.get()) {
            for (int i = 0; i < player.getInventory().getContainerSize() && slot < grave.items().size(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    grave.items().set(slot++, stack.copy());
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
        if (RecoveryConfig.STORE_ARMOR.get()) {
            for (EquipmentSlot es : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                if (slot >= grave.items().size()) break;
                ItemStack stack = player.getItemBySlot(es);
                if (!stack.isEmpty()) {
                    grave.items().set(slot++, stack.copy());
                    player.setItemSlot(es, ItemStack.EMPTY);
                }
            }
        }
        if (RecoveryConfig.STORE_OFFHAND.get()) {
            if (slot < grave.items().size()) {
                ItemStack stack = player.getOffhandItem();
                if (!stack.isEmpty()) {
                    grave.items().set(slot++, stack.copy());
                    player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
            }
        }
        if (RecoveryConfig.STORE_XP.get()) {
            // XP storage deferred: newer NeoForge uses ExperienceHandler with private fields.
            // Grave stores 0 for now; vanilla drops XP orbs on death.
            grave.setXpStored(0);
        }

        long expiresAt = 0;
        int expirationMinutes = RecoveryConfig.GRAVE_EXPIRATION_MINUTES.get();
        if (expirationMinutes > 0) {
            expiresAt = grave.createdAt() + (expirationMinutes * 60000L);
        }

        RecoveryWorldData data = RecoveryWorldData.getOrCreate(level);
        data.addGrave(player.getUUID(), new RecoveryWorldData.GraveEntry(
            graveId, player.getUUID(), player.getScoreboardName(), gravePos,
            level.dimension().identifier().toString(), grave.createdAt(), expiresAt,
            deathCause, deathCause, 0, grave.xpStored(), false, false
        ));

        if (RecoveryConfig.ENABLE_DEATH_HISTORY.get()) {
            data.addDeathRecord(player.getUUID(), new RecoveryWorldData.DeathRecord(
                player.getUUID(), grave.createdAt(), deathCause,
                level.dimension().identifier().toString(), gravePos, false, false
            ));
        }

        if (RecoveryConfig.GRAVE_KEY_ENABLED.get()) {
            ItemStack key = new ItemStack(ModItems.GRAVE_KEY.get());
            GraveKeyItem.bindToGrave(key, graveId, gravePos, level.dimension().identifier());
            if (!player.getInventory().add(key)) {
                ItemEntity entity = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), key);
                level.addFreshEntity(entity);
            }
        }

        level.playSound(null, gravePos, ModSounds.GRAVE_CREATE.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
        RecoveryIntegrationDispatcher.onGraveCreated(player, gravePos);

        EchoRecovery.LOGGER.info("Created grave {} for {} at {}", graveId, player.getScoreboardName(), gravePos);
    }
}
