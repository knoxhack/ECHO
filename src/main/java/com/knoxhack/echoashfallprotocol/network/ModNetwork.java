package com.knoxhack.echoashfallprotocol.network;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echonetcore.api.EchoNetPayloads;
import com.knoxhack.echonetcore.api.EchoRateLimitPolicy;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Network packet registration and handling for ECHO: ASHFALL PROTOCOL.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class ModNetwork {
    
    private static final String VERSION = "1";
    
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = EchoNetPayloads.optional(event, VERSION);
        
        // Register Nexus state sync packet
        EchoNetPayloads.clientboundSync(registrar,
            NexusStatePacket.TYPE,
            NexusStatePacket.CODEC,
            (packet, player, ctx) -> handleNexusState(packet)
        );
        
        // Register Mission Turn-In packet (Client to Server)
        EchoNetPayloads.serverboundAction(registrar,
            MissionTurnInPacket.TYPE,
            MissionTurnInPacket.CODEC,
            EchoRateLimitPolicy.of(10, "mission_turn_in"),
            (packet, player, ctx) -> handleMissionTurnIn(packet, player)
        );

        // Register Nexus Choice packet (Client to Server)
        EchoNetPayloads.serverboundAction(registrar,
            NexusChoicePacket.TYPE,
            NexusChoicePacket.CODEC,
            EchoRateLimitPolicy.of(20, "nexus_choice"),
            (packet, player, ctx) -> handleNexusChoice(packet, player)
        );

        // Register Research Purchase packet (Client to Server)
        EchoNetPayloads.serverboundAction(registrar,
            ResearchPurchasePacket.TYPE,
            ResearchPurchasePacket.CODEC,
            EchoRateLimitPolicy.of(10, "research_purchase"),
            (packet, player, ctx) -> handleResearchPurchase(packet, player)
        );

        // Register Research Fragment Analysis packet (Client to Server)
        EchoNetPayloads.serverboundAction(registrar,
            ResearchAnalyzeFragmentPacket.TYPE,
            ResearchAnalyzeFragmentPacket.CODEC,
            EchoRateLimitPolicy.of(10, "research_analyze"),
            (packet, player, ctx) -> handleResearchAnalyzeFragment(packet, player)
        );

        // Register Companion Drone command packet (Client to Server)
        EchoNetPayloads.serverboundAction(registrar,
            DroneCommandPacket.TYPE,
            DroneCommandPacket.CODEC,
            EchoRateLimitPolicy.of(10, "drone_command"),
            (packet, player, ctx) -> handleDroneCommand(packet, player)
        );

        // Register Archive intel read-state packet (Client to Server)
        EchoNetPayloads.serverboundAction(registrar,
            ArchiveIntelReadPacket.TYPE,
            ArchiveIntelReadPacket.CODEC,
            EchoRateLimitPolicy.of(4, "archive_intel_read"),
            (packet, player, ctx) -> handleArchiveIntelRead(packet, player)
        );

        EchoNetPayloads.clientboundSync(registrar,
            FactionDialogueOpenPacket.TYPE,
            FactionDialogueOpenPacket.CODEC,
            (packet, player, ctx) -> handleFactionDialogueOpen(packet)
        );

        EchoNetPayloads.serverboundAction(registrar,
            FactionNpcActionPacket.TYPE,
            FactionNpcActionPacket.CODEC,
            EchoRateLimitPolicy.of(10, "faction_npc_action"),
            (packet, player, ctx) -> handleFactionNpcAction(packet, player)
        );

        // Register Environmental Sync packet (Server to Client)
        EchoNetPayloads.clientboundSync(registrar,
            EnvironmentalSyncPacket.TYPE,
            EnvironmentalSyncPacket.CODEC,
            (packet, player, ctx) -> handleEnvironmentalSync(packet)
        );

        // Register Grace Countdown packet (Server to Client)
        EchoNetPayloads.clientboundSync(registrar,
            GraceCountdownPacket.TYPE,
            GraceCountdownPacket.CODEC,
            (packet, player, ctx) -> handleGraceCountdown(packet)
        );

        // Register ECHO boss HUD/navigation target packet (Server to Client)
        EchoNetPayloads.clientboundSync(registrar,
            BossNavigationPacket.TYPE,
            BossNavigationPacket.CODEC,
            (packet, player, ctx) -> handleBossNavigation(packet)
        );

        // Register first-join welcome screen packet (Server to Client)
        EchoNetPayloads.clientboundSync(registrar,
            WelcomeScreenPacket.TYPE,
            WelcomeScreenPacket.CODEC,
            (packet, player, ctx) -> handleWelcomeScreen(packet)
        );
    }

    private static void handleNexusChoice(NexusChoicePacket packet, net.minecraft.server.level.ServerPlayer player) {
        com.knoxhack.echoashfallprotocol.endgame.NexusChoiceService.applyChoice(player, packet.choice());
    }

    private static void handleMissionTurnIn(MissionTurnInPacket packet, net.minecraft.server.level.ServerPlayer player) {
        com.knoxhack.echoashfallprotocol.echo.QuestData quest = player.getData(com.knoxhack.echoashfallprotocol.registry.ModAttachments.QUEST_DATA.get());
        if (quest.repairMissionState(player)) {
            com.knoxhack.echoashfallprotocol.echo.QuestData.saveAndSync(player, quest);
        }

        com.knoxhack.echoashfallprotocol.echo.Mission requestedMission =
                com.knoxhack.echoashfallprotocol.echo.AshfallMissionActions.resolveTarget(quest, packet.missionId());
        String rejection = com.knoxhack.echoashfallprotocol.echo.AshfallMissionActions
                .turnInRejection(player, quest, requestedMission);
        if (!rejection.isBlank()) {
            com.knoxhack.echoashfallprotocol.echo.AshfallMissionActions.sendTurnInRejection(player, rejection);
            com.knoxhack.echoashfallprotocol.echo.QuestData.syncToClient(player);
            return;
        }

        com.knoxhack.echoashfallprotocol.echo.EchoGuideManager.turnInMission(player, quest, requestedMission);
        com.knoxhack.echoashfallprotocol.echo.QuestData.syncToClient(player);
    }

    private static void handleArchiveIntelRead(ArchiveIntelReadPacket packet, net.minecraft.server.level.ServerPlayer player) {
        String intelId = packet.intelId() == null ? "" : packet.intelId();
        if (intelId.isBlank() || intelId.length() > 160) {
            return;
        }

        com.knoxhack.echoashfallprotocol.echo.EchoIntel intel =
            com.knoxhack.echoashfallprotocol.echo.EchoIntel.get(player);
        intel.markAsRead(intelId);
        com.knoxhack.echoashfallprotocol.echo.EchoIntel.saveAndSync(player, intel);
    }

    private static void handleFactionNpcAction(FactionNpcActionPacket packet, net.minecraft.server.level.ServerPlayer player) {
        com.knoxhack.echoashfallprotocol.faction.FactionNpcDialogueService.handleAction(player, packet);
    }

    private static void handleResearchPurchase(ResearchPurchasePacket packet, net.minecraft.server.level.ServerPlayer player) {
        com.knoxhack.echoashfallprotocol.research.Perk perk = com.knoxhack.echoashfallprotocol.research.PerkRegistry.get(packet.perkId());
        if (perk == null) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("[ECHO-7] Unknown research node.")
                    .withStyle(net.minecraft.ChatFormatting.RED),
                true
            );
            return;
        }

        com.knoxhack.echoashfallprotocol.research.ResearchData data =
            com.knoxhack.echoashfallprotocol.research.ResearchData.get(player);

        if (data.hasPerk(perk.getId())) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("[ECHO-7] Perk already unlocked: ").append(perk.getName())
                    .withStyle(net.minecraft.ChatFormatting.YELLOW),
                true
            );
            com.knoxhack.echoashfallprotocol.research.ResearchData.syncToClient(player);
            return;
        }

        if (!perk.hasPrerequisites(player)) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("[ECHO-7] Previous research node required.")
                    .withStyle(net.minecraft.ChatFormatting.RED),
                true
            );
            com.knoxhack.echoashfallprotocol.research.ResearchData.syncToClient(player);
            return;
        }

        if (!data.hasPoints(perk.getCost())) {
            int missing = perk.getCost() - data.getPoints();
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("[ECHO-7] Need " + missing + " more RP.")
                    .withStyle(net.minecraft.ChatFormatting.RED),
                true
            );
            com.knoxhack.echoashfallprotocol.research.ResearchData.syncToClient(player);
            return;
        }

        boolean success = perk.unlock(player);
        if (success) {
            com.knoxhack.echoashfallprotocol.research.ResearchData.saveAndSync(player, data);
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("[ECHO-7] Perk unlocked: ").append(perk.getName())
                    .withStyle(net.minecraft.ChatFormatting.GREEN),
                false
            );
        } else {
            com.knoxhack.echoashfallprotocol.research.ResearchData.syncToClient(player);
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("[ECHO-7] Research purchase rejected.")
                    .withStyle(net.minecraft.ChatFormatting.RED),
                true
            );
        }
    }

    private static void handleResearchAnalyzeFragment(ResearchAnalyzeFragmentPacket packet, net.minecraft.server.level.ServerPlayer player) {
        if (!(player.containerMenu instanceof com.knoxhack.echoashfallprotocol.block.menu.ResearchLabMenu)) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("[ECHO-7] Open a Research Lab to analyze fragments.")
                    .withStyle(net.minecraft.ChatFormatting.RED),
                true
            );
            return;
        }

        String requested = packet.schematicType().toLowerCase(java.util.Locale.ROOT);
        com.knoxhack.echoashfallprotocol.item.SchematicFragmentItem fragmentItem = null;
        net.minecraft.world.item.ItemStack fragmentStack = net.minecraft.world.item.ItemStack.EMPTY;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof com.knoxhack.echoashfallprotocol.item.SchematicFragmentItem schematic
                    && schematic.getType().getDisplayName().toLowerCase(java.util.Locale.ROOT).equals(requested)) {
                fragmentItem = schematic;
                fragmentStack = stack;
                break;
            }
        }

        if (fragmentItem == null || fragmentStack.isEmpty()) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("[ECHO-7] No matching schematic fragment found.")
                    .withStyle(net.minecraft.ChatFormatting.RED),
                true
            );
            com.knoxhack.echoashfallprotocol.research.ResearchData.syncToClient(player);
            return;
        }

        com.knoxhack.echoashfallprotocol.research.ResearchData research =
            com.knoxhack.echoashfallprotocol.research.ResearchData.get(player);
        String category = fragmentItem.getType().getDisplayName().toLowerCase(java.util.Locale.ROOT);
        boolean newlyUnlocked = research.unlockSchematic(category);
        int bonus = newlyUnlocked ? 25 : 5;
        int added = research.addPoints(bonus);
        fragmentStack.shrink(1);
        com.knoxhack.echoashfallprotocol.research.ResearchData.saveAndSync(player, research);

        String status = newlyUnlocked
            ? "Schematic decoded: " + fragmentItem.getType().getDisplayName()
            : "Duplicate schematic archived";
        player.sendSystemMessage(
            net.minecraft.network.chat.Component.literal("[ECHO-7] " + status + ". +" + added + " RP")
                .withStyle(newlyUnlocked ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.YELLOW),
            false
        );
    }

    public static void handleDroneCommand(DroneCommandPacket packet, net.minecraft.world.entity.player.Player player) {
        String command = packet.command() == null ? "" : packet.command().toUpperCase(java.util.Locale.ROOT);
        java.util.List<com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone> drones =
                findOwnedCompanionDrones(player);
        if (drones.isEmpty()) {
            handleScoutDroneFallback(packet, player);
            return;
        }

        if ("RECALL".equals(command)) {
            int recalledCount = 0;
            for (com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone ownedDrone : drones) {
                if (ownedDrone.recallTo(player)) {
                    recalledCount++;
                }
            }
            boolean recalled = recalledCount > 0;
            playEchoCue(player, recalled ? com.knoxhack.echoashfallprotocol.registry.ModSounds.ECHO_COMPLETE.get()
                    : com.knoxhack.echoashfallprotocol.registry.ModSounds.ECHO_MESSAGE.get(), recalled ? 1.15f : 0.7f);
            sendDroneStatus(player,
                net.minecraft.network.chat.Component.literal(recalled
                    ? "[ECHO-7 // DRONE] Recall acknowledged."
                    : "[ECHO-7 // DRONE] Recall failed. Owner link unavailable.")
                    .withStyle(recalled ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED)
            );
            return;
        }

        com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone drone = nearestCompanionDrone(drones, player);
        if (drone == null) {
            handleScoutDroneFallback(packet, player);
            return;
        }

        if ("TOGGLE_LIGHT".equals(command)) {
            drone.toggleLight();
            drone.speak(drone.isLightEnabled() ? "Light enabled." : "Light disabled.",
                com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone.MOOD_PROFESSIONAL, 30, 0);
            playEchoCue(player, com.knoxhack.echoashfallprotocol.registry.ModSounds.ECHO_MESSAGE.get(), 1.35f);
            sendDroneStatus(player,
                net.minecraft.network.chat.Component.literal("[ECHO-7 // DRONE] Light " + (drone.isLightEnabled() ? "enabled." : "disabled."))
                    .withStyle(net.minecraft.ChatFormatting.AQUA)
            );
            return;
        }

        com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone.DroneMode mode;
        try {
            mode = com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone.DroneMode.valueOf(command);
        } catch (IllegalArgumentException ex) {
            playEchoCue(player, com.knoxhack.echoashfallprotocol.registry.ModSounds.ECHO_MESSAGE.get(), 0.7f);
            sendDroneStatus(player,
                net.minecraft.network.chat.Component.literal("[ECHO-7 // DRONE] Unknown command.")
                    .withStyle(net.minecraft.ChatFormatting.RED)
            );
            return;
        }

        if (!drone.canSwitchToMode(mode)) {
            drone.speak(mode.getDisplayName() + " locked. Repair required.",
                com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone.MOOD_CONCERNED, 45, 6);
            playEchoCue(player, com.knoxhack.echoashfallprotocol.registry.ModSounds.ECHO_MESSAGE.get(), 0.8f);
            sendDroneStatus(player,
                net.minecraft.network.chat.Component.literal("[ECHO-7 // DRONE] " + mode.getDisplayName()
                    + " requires higher repair integrity.")
                    .withStyle(net.minecraft.ChatFormatting.YELLOW)
            );
            return;
        }

        drone.setCurrentMode(mode);
        if (mode == com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone.DroneMode.SCOUT
                && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            recordSpecialProgress(serverPlayer, "drone:scout_mode");
            recordSpecialProgress(serverPlayer, "drone:intel_recovered");
        }
        drone.speak("Mode set: " + mode.getDisplayName() + ".",
            com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone.MOOD_PROFESSIONAL, 35, 0);
        playEchoCue(player, com.knoxhack.echoashfallprotocol.registry.ModSounds.ECHO_COMPLETE.get(), 1.05f);
        sendDroneStatus(player,
            net.minecraft.network.chat.Component.literal("[ECHO-7 // DRONE] Mode: " + mode.getDisplayName())
                .withStyle(net.minecraft.ChatFormatting.GREEN)
            );
    }

    private static java.util.List<com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone> findOwnedCompanionDrones(
            net.minecraft.world.entity.player.Player player) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return java.util.List.of();
        }
        net.minecraft.world.phys.AABB searchArea = new net.minecraft.world.phys.AABB(
            player.getX() - 128.0D, player.getY() - 64.0D, player.getZ() - 128.0D,
            player.getX() + 128.0D, player.getY() + 64.0D, player.getZ() + 128.0D);
        return serverLevel.getEntitiesOfClass(
            com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone.class,
            searchArea,
            drone -> !drone.isRemoved() && drone.isAlive() && player.getUUID().equals(drone.getOwnerUUID()));
    }

    private static com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone nearestCompanionDrone(
            java.util.List<com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone> drones,
            net.minecraft.world.entity.player.Player player) {
        com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (com.knoxhack.echoashfallprotocol.entity.EchoCompanionDrone drone : drones) {
            double distance = drone.distanceToSqr(player);
            if (distance < nearestDistance) {
                nearest = drone;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static void handleScoutDroneFallback(DroneCommandPacket packet, net.minecraft.world.entity.player.Player player) {
        com.knoxhack.echoashfallprotocol.entity.ScoutDrone scout = findOwnedScoutDrone(player);
        if (scout == null) {
            playEchoCue(player, com.knoxhack.echoashfallprotocol.registry.ModSounds.ECHO_MESSAGE.get(), 0.7f);
            sendDroneStatus(player,
                net.minecraft.network.chat.Component.literal("[ECHO-7 // DRONE] No linked companion or Scout Drone found.")
                    .withStyle(net.minecraft.ChatFormatting.RED)
            );
            return;
        }

        String command = packet.command() == null ? "" : packet.command().toUpperCase(java.util.Locale.ROOT);
        if ("RECALL".equals(command)) {
            scout.setMode(com.knoxhack.echoashfallprotocol.entity.ScoutDrone.DroneMode.FOLLOW);
            net.minecraft.world.phys.Vec3 target = player.position().add(0.0D, 1.5D, 0.0D);
            scout.teleportTo(target.x, target.y, target.z);
            scout.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            playEchoCue(player, com.knoxhack.echoashfallprotocol.registry.ModSounds.ECHO_COMPLETE.get(), 1.1f);
            sendDroneStatus(player,
                net.minecraft.network.chat.Component.literal("[ECHO-7 // DRONE] Scout Drone recalled.")
                    .withStyle(net.minecraft.ChatFormatting.GREEN)
            );
            return;
        }

        if ("TOGGLE_LIGHT".equals(command)) {
            playEchoCue(player, com.knoxhack.echoashfallprotocol.registry.ModSounds.ECHO_MESSAGE.get(), 0.85f);
            sendDroneStatus(player,
                net.minecraft.network.chat.Component.literal("[ECHO-7 // DRONE] Scout Drone has no light module.")
                    .withStyle(net.minecraft.ChatFormatting.YELLOW)
            );
            return;
        }

        com.knoxhack.echoashfallprotocol.entity.ScoutDrone.DroneMode mode = switch (command) {
            case "FOLLOW" -> com.knoxhack.echoashfallprotocol.entity.ScoutDrone.DroneMode.FOLLOW;
            case "SCOUT", "SCAVENGE" -> com.knoxhack.echoashfallprotocol.entity.ScoutDrone.DroneMode.SCAVENGE;
            case "COMBAT", "PATROL" -> com.knoxhack.echoashfallprotocol.entity.ScoutDrone.DroneMode.DEFENSE;
            default -> null;
        };

        if (mode == null) {
            playEchoCue(player, com.knoxhack.echoashfallprotocol.registry.ModSounds.ECHO_MESSAGE.get(), 0.7f);
            sendDroneStatus(player,
                net.minecraft.network.chat.Component.literal("[ECHO-7 // DRONE] Unknown Scout Drone command.")
                    .withStyle(net.minecraft.ChatFormatting.RED)
            );
            return;
        }

        scout.setMode(mode);
        if (mode == com.knoxhack.echoashfallprotocol.entity.ScoutDrone.DroneMode.SCAVENGE
                && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            recordSpecialProgress(serverPlayer, "drone:scout_mode");
            recordSpecialProgress(serverPlayer, "drone:intel_recovered");
        }
        playEchoCue(player, com.knoxhack.echoashfallprotocol.registry.ModSounds.ECHO_COMPLETE.get(), 1.0f);
        sendDroneStatus(player,
            net.minecraft.network.chat.Component.literal("[ECHO-7 // DRONE] Scout mode: " + mode.getDisplayName())
                .withStyle(net.minecraft.ChatFormatting.GREEN)
        );
    }

    private static com.knoxhack.echoashfallprotocol.entity.ScoutDrone findOwnedScoutDrone(
            net.minecraft.world.entity.player.Player player) {
        java.util.List<com.knoxhack.echoashfallprotocol.entity.ScoutDrone> drones =
            ((net.minecraft.server.level.ServerLevel) player.level()).getEntitiesOfClass(
                com.knoxhack.echoashfallprotocol.entity.ScoutDrone.class,
                player.getBoundingBox().inflate(128.0),
                drone -> !drone.isRemoved() && drone.isAlive() && player.getUUID().equals(drone.getOwnerUUID()));

        com.knoxhack.echoashfallprotocol.entity.ScoutDrone nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (com.knoxhack.echoashfallprotocol.entity.ScoutDrone drone : drones) {
            double distance = drone.distanceToSqr(player);
            if (distance < nearestDistance) {
                nearest = drone;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private static void sendDroneStatus(
            net.minecraft.world.entity.player.Player player,
            net.minecraft.network.chat.Component message) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message, true);
        } else {
            player.sendSystemMessage(message);
        }
    }

    private static void recordSpecialProgress(net.minecraft.server.level.ServerPlayer player, String marker) {
        com.knoxhack.echoashfallprotocol.echo.QuestData quest = com.knoxhack.echoashfallprotocol.echo.QuestData.get(player);
        quest.visitLocation("special", marker);
        com.knoxhack.echoashfallprotocol.echo.QuestData.saveAndSync(player, quest);
    }

    private static void playEchoCue(net.minecraft.world.entity.player.Player player,
            net.minecraft.sounds.SoundEvent sound, float pitch) {
        player.level().playSound(null, player.blockPosition(), sound,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.55f, pitch);
    }

    private static void handleEnvironmentalSync(EnvironmentalSyncPacket packet) {
        dispatchToClientHandler("handleEnvironmentalSync", EnvironmentalSyncPacket.class, packet);
    }

    private static void handleGraceCountdown(GraceCountdownPacket packet) {
        dispatchToClientHandler("handleGraceCountdown", GraceCountdownPacket.class, packet);
    }

    private static void handleBossNavigation(BossNavigationPacket packet) {
        dispatchToClientHandler("handleBossNavigation", BossNavigationPacket.class, packet);
    }

    private static void handleWelcomeScreen(WelcomeScreenPacket packet) {
        dispatchToClientHandler("handleWelcomeScreen", WelcomeScreenPacket.class, packet);
    }

    private static void handleFactionDialogueOpen(FactionDialogueOpenPacket packet) {
        dispatchToClientHandler("handleFactionDialogueOpen", FactionDialogueOpenPacket.class, packet);
    }

    private static void handleNexusState(NexusStatePacket packet) {
        dispatchToClientHandler("handleNexusState", NexusStatePacket.class, packet);
    }

    private static <T> void dispatchToClientHandler(String methodName, Class<T> packetType, T packet) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) {
            return;
        }

        try {
            Class<?> handlerClass = Class.forName("com.knoxhack.echoashfallprotocol.client.ClientNetworkHandlers");
            handlerClass.getMethod(methodName, packetType).invoke(null, packet);
        } catch (ReflectiveOperationException e) {
            EchoAshfallProtocol.LOGGER.error("Failed to dispatch client network packet {}", packetType.getSimpleName(), e);
        }
    }
}
