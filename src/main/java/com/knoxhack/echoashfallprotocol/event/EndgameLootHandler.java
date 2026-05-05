package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.endgame.PostNexusData;
import com.knoxhack.echoashfallprotocol.registry.ModItems;
import com.knoxhack.echoashfallprotocol.world.NexusWorldData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Random;

/**
 * Loot handler for post-Nexus endgame content.
 * Adds Nexus Crystal drops from mobs after Nexus choice is made.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class EndgameLootHandler {

    private static final Random RANDOM = new Random();
    private static final float CRYSTAL_DROP_CHANCE = 0.05f; // 5% base chance

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        // Check if Nexus choice has been made
        NexusWorldData worldData = NexusWorldData.get(level);
        if (!worldData.hasChoiceBeenMade()) return;

        LivingEntity killed = event.getEntity();
        String entityType = killed.getType().getDescriptionId();

        // Only hostile mobs drop crystals
        if (!isHostileMob(entityType)) return;

        // Calculate drop chance based on world state
        float chance = CRYSTAL_DROP_CHANCE;
        
        // Increase chance in DESTROYED world (more chaos = more crystals)
        if (worldData.isDestroyed()) {
            chance *= 1.5f; // 7.5% in destroyed world
        }

        // Roll for drop
        if (RANDOM.nextFloat() < chance) {
            int amount = 1 + RANDOM.nextInt(2); // 1-2 crystals
            ItemStack crystals = new ItemStack(ModItems.NEXUS_CRYSTAL.get(), amount);
            
            // Spawn at entity location
            ItemEntity itemEntity = new ItemEntity(
                level,
                killed.getX(),
                killed.getY(),
                killed.getZ(),
                crystals
            );
            
            // Set owner to prevent pickup delay issues
            itemEntity.setTarget(player.getUUID());
            itemEntity.setThrower(player);
            
            level.addFreshEntity(itemEntity);
            
            // Also give player tracking notification occasionally
            if (RANDOM.nextFloat() < 0.1f) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§5[NEXUS RESONANCE]§r Crystal fragments detected..."
                ));
            }
        }
    }

    /**
     * Check if entity is a hostile mob type
     */
    private static boolean isHostileMob(String entityType) {
        return entityType.contains("rad_zombie") ||
               entityType.contains("glowing_ghoul") ||
               entityType.contains("irradiated_wolf") ||
               entityType.contains("toxic_slime") ||
               entityType.contains("ash_wraith") ||
               entityType.contains("rust_walker") ||
               entityType.contains("city_stalker") ||
               entityType.contains("steam_wraith") ||
               entityType.contains("mutated_crawler") ||
               entityType.contains("scavenger_bandit") ||
               entityType.contains("wild_dog") ||
               entityType.contains("feral_human");
    }
}
