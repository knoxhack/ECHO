package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.echo.QuestData;
import com.knoxhack.echoashfallprotocol.gameplay.AshfallInteractionRules;
import com.knoxhack.echoashfallprotocol.network.WelcomeScreenPacket;
import com.knoxhack.echoashfallprotocol.registry.ModAttachments;
import com.knoxhack.echoashfallprotocol.world.StartingDropPodData;
import com.knoxhack.echoashfallprotocol.worldgen.ProceduralStructureGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import com.mojang.datafixers.util.Pair;

/**
 * Handles giving the player their starting kit on first join.
 * Design: the first crash window is forgiving enough for new players to learn the survival systems.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class PlayerStartingKitHandler {
    private static final ResourceKey<Biome> THE_WASTELAND = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(EchoAshfallProtocol.MODID, "the_wasteland"));
    private static final int STARTING_BIOME_SEARCH_RADIUS = 8192;
    private static final int STARTING_BIOME_SEARCH_STEP = 64;
    private static final int MIN_STARTING_POD_RADIUS_CHUNKS = 10;
    private static final int MAX_STARTING_POD_RADIUS_CHUNKS = 500;
    private static final int MIN_STARTING_POD_SPACING_CHUNKS = 8;
    private static final int CHUNK_SIZE = 16;
    private static final int STARTING_POD_CANDIDATE_ATTEMPTS = 128;
    private static final int STARTING_SURFACE_SEARCH_RADIUS = 64;
    private static final int STARTING_SURFACE_SEARCH_STEP = 4;
    private static final int MIN_SAFE_SURFACE_ABOVE_BOTTOM = 16;
    private static final int MIN_STARTING_SURFACE_Y = 48;
    private static final int MIN_STARTING_POD_RADIUS_BLOCKS = MIN_STARTING_POD_RADIUS_CHUNKS * CHUNK_SIZE;
    private static final int MAX_STARTING_POD_RADIUS_BLOCKS = MAX_STARTING_POD_RADIUS_CHUNKS * CHUNK_SIZE;
    private static final int MIN_STARTING_POD_SPACING_BLOCKS = MIN_STARTING_POD_SPACING_CHUNKS * CHUNK_SIZE;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Check if player has received starting kit (using persistent data)
        CompoundTag playerData = player.getPersistentData();
        if (playerData.getBoolean("ashes_of_tomorrow.received_kit").orElse(false)) {
            rescueUndergroundStartingPod(player);
            return; // Already received kit
        }

        // Starter supplies live in the pod lockers; the note points players to them.
        ItemStack welcomeNote = new ItemStack(Items.PAPER);
        welcomeNote.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                Component.translatable("item.EchoAshfallProtocol.echo_starter_note.name"));
        CompoundTag noteTag = new CompoundTag();
        String terminalLine = ModList.get().isLoaded("echoterminal")
                ? "\u00A7eTerminal:\u00A7r press [M] and follow ECHO objectives.\n"
                : "\u00A7eGuide:\u00A7r follow HUD/chat goals; press [N] to reopen this briefing.\n";
        noteTag.putString("message",
            "\n\u00A7b[ECHO-7] FIRST 10 MINUTES\n" +
            "\n\u00A7eLockers:\u00A7r open OXYGEN, TOOLS, SCRAP, and LOGS before leaving.\n" +
            "\u00A7eWater:\u00A7r drink Clean Water from the OXYGEN locker before scouting.\n" +
            "\u00A7eShelter:\u00A7r deploy the Ash Campfire, chest, and torches near the ramp.\n" +
            "\u00A7eTool:\u00A7r take the sword, then craft a Scrap Knife from pod salvage.\n" +
            "\u00A7eScanner:\u00A7r store the Signal Scanner until base power is online.\n" +
            terminalLine +
            "\n\u00A77Emergency Buffer: 10 minutes. Use it to equip, drink, shelter, and craft.\n"
        );
        welcomeNote.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(noteTag));
        player.getInventory().add(welcomeNote);

        // Place a personal starting drop pod away from shared server spawn.
        if (!(player.level() instanceof ServerLevel level)) return;
        StartingDropPodData podData = StartingDropPodData.get(level);
        java.util.Optional<StartingDropPodData.Entry> existingPod = podData.findForPlayer(player.getUUID())
                .filter(PlayerStartingKitHandler::isReusableStartingPod);
        BlockPos origin = existingPod.map(StartingDropPodData.Entry::origin)
                .orElseGet(() -> findStartingPodOrigin(level, podData));
        BlockPos interior = existingPod.map(StartingDropPodData.Entry::interior)
                .orElseGet(() -> ProceduralStructureGenerator.placeStartingDropPod(level, origin, level.getRandom()));
        if (interior != null) {
            podData.addOrReplace(player.getUUID(), origin, interior);
            player.teleportTo(
                    interior.getX() + 0.5,
                    interior.getY(),
                    interior.getZ() + 0.5);
            player.setYRot(0f);
            player.setXRot(0f);
            // NOTE: setRespawnPosition signature changed in the current target (now takes RespawnConfig).
            // Skipping respawn anchor for now \u2014 player will respawn at world spawn.
            QuestData quest = player.getData(ModAttachments.QUEST_DATA.get());
            quest.setDropPodInitialized(true);
            player.setData(ModAttachments.QUEST_DATA.get(), quest);
            EchoAshfallProtocol.LOGGER.info(
                    "Spawned {} inside personal starting drop pod at {} from origin {}.",
                    player.getName().getString(),
                    interior,
                    origin);
        }

        // Mark kit as received
        playerData.putBoolean("ashes_of_tomorrow.received_kit", true);
        PacketDistributor.sendToPlayer(player, new WelcomeScreenPacket());

        // Welcome message
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.starting.line"));
        player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.starting.kit"));
        player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.starting.buffer"));
        player.sendSystemMessage(Component.translatable("message.EchoAshfallProtocol.starting.line"));
        player.sendSystemMessage(Component.literal(""));
    }

    private static boolean isReusableStartingPod(StartingDropPodData.Entry entry) {
        return entry.origin().getY() >= MIN_STARTING_SURFACE_Y
                && entry.interior().getY() >= MIN_STARTING_SURFACE_Y;
    }

    private static void rescueUndergroundStartingPod(ServerPlayer player) {
        if (player.blockPosition().getY() >= MIN_STARTING_SURFACE_Y || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        StartingDropPodData podData = StartingDropPodData.get(level);
        if (podData.findForPlayer(player.getUUID()).filter(entry -> !isReusableStartingPod(entry)).isEmpty()) {
            return;
        }

        BlockPos origin = findStartingPodOrigin(level, podData);
        BlockPos interior = ProceduralStructureGenerator.placeStartingDropPod(level, origin, level.getRandom());
        if (interior == null) {
            return;
        }

        podData.addOrReplace(player.getUUID(), origin, interior);
        player.teleportTo(interior.getX() + 0.5, interior.getY(), interior.getZ() + 0.5);
        player.setYRot(0f);
        player.setXRot(0f);
        player.sendSystemMessage(Component.literal("\u00A7b[ECHO-7]\u00A7r Unsafe underground crash site detected. Relocating to surface pod."));
        EchoAshfallProtocol.LOGGER.warn(
                "Relocated {} from invalid underground starting drop pod to {} from origin {}.",
                player.getName().getString(),
                interior,
                origin);
    }

    private static BlockPos findStartingPodOrigin(ServerLevel level, StartingDropPodData podData) {
        BlockPos serverSpawn = getServerSpawn(level);
        RandomSource random = level.getRandom();

        for (int attempt = 0; attempt < STARTING_POD_CANDIDATE_ATTEMPTS; attempt++) {
            BlockPos candidate = randomSurfaceAround(level, serverSpawn, random, false);
            BlockPos preferred = findStartingWastelandSurface(level, candidate);
            if (isWithinStartingRadius(serverSpawn, preferred)
                    && podData.isFarEnoughFromExistingPods(preferred, MIN_STARTING_POD_SPACING_BLOCKS)) {
                return preferred;
            }

            if (podData.isFarEnoughFromExistingPods(candidate, MIN_STARTING_POD_SPACING_BLOCKS)) {
                return candidate;
            }
        }

        for (int attempt = 0; attempt < STARTING_POD_CANDIDATE_ATTEMPTS; attempt++) {
            BlockPos candidate = randomSurfaceAround(level, serverSpawn, random, true);
            if (podData.isFarEnoughFromExistingPods(candidate, MIN_STARTING_POD_SPACING_BLOCKS)) {
                EchoAshfallProtocol.LOGGER.warn(
                        "Used outer-radius fallback for starting pod after {} occupied random candidates near {}.",
                        STARTING_POD_CANDIDATE_ATTEMPTS,
                        serverSpawn);
                return candidate;
            }
        }

        EchoAshfallProtocol.LOGGER.warn(
                "Could not find an unoccupied starting pod location after {} attempts. Using nearest safe capped position near {}.",
                STARTING_POD_CANDIDATE_ATTEMPTS * 2,
                serverSpawn);
        return randomSurfaceAround(level, serverSpawn, random, true);
    }

    private static BlockPos getServerSpawn(ServerLevel level) {
        BlockPos sharedSpawn = level.getRespawnData().pos();
        int x = sharedSpawn.getX();
        int z = sharedSpawn.getZ();
        return resolveSafeStartingSurface(level, x, z);
    }

    private static BlockPos randomSurfaceAround(ServerLevel level, BlockPos center, RandomSource random, boolean nearOuterRadius) {
        int radiusBlocks;
        if (nearOuterRadius) {
            int fallbackBand = Math.max(CHUNK_SIZE, MIN_STARTING_POD_SPACING_BLOCKS);
            radiusBlocks = MAX_STARTING_POD_RADIUS_BLOCKS - random.nextInt(fallbackBand);
        } else {
            radiusBlocks = MIN_STARTING_POD_RADIUS_BLOCKS
                    + random.nextInt(MAX_STARTING_POD_RADIUS_BLOCKS - MIN_STARTING_POD_RADIUS_BLOCKS + 1);
        }

        double angle = random.nextDouble() * Math.PI * 2.0;
        int x = center.getX() + (int) Math.round(Math.cos(angle) * radiusBlocks);
        int z = center.getZ() + (int) Math.round(Math.sin(angle) * radiusBlocks);
        return resolveSafeStartingSurface(level, x, z);
    }

    private static boolean isWithinStartingRadius(BlockPos serverSpawn, BlockPos candidate) {
        int dx = candidate.getX() - serverSpawn.getX();
        int dz = candidate.getZ() - serverSpawn.getZ();
        int minSqr = MIN_STARTING_POD_RADIUS_BLOCKS * MIN_STARTING_POD_RADIUS_BLOCKS;
        int maxSqr = MAX_STARTING_POD_RADIUS_BLOCKS * MAX_STARTING_POD_RADIUS_BLOCKS;
        int distanceSqr = dx * dx + dz * dz;
        return distanceSqr >= minSqr && distanceSqr <= maxSqr;
    }

    private static BlockPos findStartingWastelandSurface(ServerLevel level, BlockPos around) {
        if (level.getBiome(around).is(THE_WASTELAND)) {
            return around;
        }

        Pair<BlockPos, net.minecraft.core.Holder<Biome>> located = level.findClosestBiome3d(
                holder -> holder.is(THE_WASTELAND),
                around,
                STARTING_BIOME_SEARCH_RADIUS,
                STARTING_BIOME_SEARCH_STEP,
                32);
        if (located != null) {
            BlockPos found = located.getFirst();
            BlockPos surface = resolveSafeStartingSurface(level, found.getX(), found.getZ());
            if (level.getBiome(surface).is(THE_WASTELAND)) {
                EchoAshfallProtocol.LOGGER.info("Located echoashfallprotocol:the_wasteland for first-start drop pod at {}.", surface);
                return surface;
            }
            EchoAshfallProtocol.LOGGER.warn(
                    "Nearest echoashfallprotocol:the_wasteland sample at {} did not match the surface biome at {}; continuing surface scan.",
                    found,
                    surface);
        }

        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int radius = STARTING_BIOME_SEARCH_STEP; radius <= STARTING_BIOME_SEARCH_RADIUS; radius += STARTING_BIOME_SEARCH_STEP) {
            for (int dx = -radius; dx <= radius; dx += STARTING_BIOME_SEARCH_STEP) {
                for (int dz = -radius; dz <= radius; dz += STARTING_BIOME_SEARCH_STEP) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }
                    int x = around.getX() + dx;
                    int z = around.getZ() + dz;
                    BlockPos candidate = resolveSafeStartingSurface(level, x, z);
                    if (!level.getBiome(candidate).is(THE_WASTELAND)) {
                        continue;
                    }
                    double distance = around.distSqr(candidate);
                    if (distance < bestDistance) {
                        best = candidate;
                        bestDistance = distance;
                    }
                }
            }
            if (best != null) {
                EchoAshfallProtocol.LOGGER.info("Moved first-start drop pod target to {} in echoashfallprotocol:the_wasteland.", best);
                return best;
            }
        }

        EchoAshfallProtocol.LOGGER.warn(
                "Could not find echoashfallprotocol:the_wasteland within {} blocks of {}. Using original world spawn for starting pod.",
                STARTING_BIOME_SEARCH_RADIUS,
                around);
        return around;
    }

    private static BlockPos resolveSafeStartingSurface(ServerLevel level, int x, int z) {
        BlockPos exact = getSafeSurfaceColumn(level, x, z);
        if (exact != null) {
            return exact;
        }

        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int radius = STARTING_SURFACE_SEARCH_STEP; radius <= STARTING_SURFACE_SEARCH_RADIUS; radius += STARTING_SURFACE_SEARCH_STEP) {
            for (int dx = -radius; dx <= radius; dx += STARTING_SURFACE_SEARCH_STEP) {
                for (int dz = -radius; dz <= radius; dz += STARTING_SURFACE_SEARCH_STEP) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }

                    BlockPos candidate = getSafeSurfaceColumn(level, x + dx, z + dz);
                    if (candidate == null) {
                        continue;
                    }

                    double distance = candidate.distSqr(new BlockPos(x, candidate.getY(), z));
                    if (distance < bestDistance) {
                        best = candidate;
                        bestDistance = distance;
                    }
                }
            }

            if (best != null) {
                return best;
            }
        }

        int fallbackY = Math.min(
                level.getMaxY() - 2,
                Math.max(
                        Math.max(level.getMinY() + MIN_SAFE_SURFACE_ABOVE_BOTTOM, MIN_STARTING_SURFACE_Y),
                        level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)));
        EchoAshfallProtocol.LOGGER.warn(
                "No fully safe starting pod surface found near [{}, {}]. Using capped fallback Y {}.",
                x,
                z,
                fallbackY);
        return new BlockPos(x, fallbackY, z);
    }

    private static BlockPos getSafeSurfaceColumn(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        int motionBlockingY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int worldSurfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

        BlockPos motionBlocking = new BlockPos(x, motionBlockingY, z);
        if (isSafeStartingSurface(level, motionBlocking)) {
            return motionBlocking;
        }

        BlockPos worldSurface = new BlockPos(x, worldSurfaceY, z);
        if (worldSurfaceY != motionBlockingY && isSafeStartingSurface(level, worldSurface)) {
            return worldSurface;
        }

        return null;
    }

    private static boolean isSafeStartingSurface(ServerLevel level, BlockPos pos) {
        if (pos.getY() <= level.getMinY() + MIN_SAFE_SURFACE_ABOVE_BOTTOM
                || pos.getY() < MIN_STARTING_SURFACE_Y
                || pos.getY() >= level.getMaxY() - 2) {
            return false;
        }

        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && AshfallInteractionRules.supportsPlacement(level, pos.below())
                && level.canSeeSky(pos.above());
    }
}

