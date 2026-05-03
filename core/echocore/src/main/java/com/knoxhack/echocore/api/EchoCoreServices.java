package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Convenience accessors around optional cross-mod ECHO services.
 */
public final class EchoCoreServices {
    private EchoCoreServices() {
    }

    public static void registerNexusPathService(NexusPathService service) {
        EchoServiceRegistry.register(NexusPathService.class, service);
    }

    public static boolean hasPostNexusChoice(Player player) {
        return EchoServiceRegistry.find(NexusPathService.class)
                .map(service -> service.hasPostNexusChoice(player))
                .orElse(false);
    }

    public static void registerIntelMirrorService(IntelMirrorService service) {
        EchoServiceRegistry.register(IntelMirrorService.class, service);
    }

    public static void mirrorIntel(ServerPlayer player, String sourceModId, String id, String title, String content) {
        EchoServiceRegistry.find(IntelMirrorService.class)
                .ifPresent(service -> service.mirrorIntel(player, sourceModId, id, title, content));
    }

    public static void registerTerminalPlacementService(TerminalPlacementService service) {
        EchoServiceRegistry.register(TerminalPlacementService.class, service);
    }

    public static boolean placeTerminal(Level level, BlockPos pos, Player owner) {
        return terminalPlacementService().placeTerminal(level, pos, owner);
    }

    public static BlockState terminalStructureBlockState() {
        return terminalPlacementService().structureBlockState();
    }

    public static boolean isTerminalBlock(BlockState state) {
        return terminalPlacementService().isTerminalBlock(state);
    }

    public static void registerTerminalRewardService(TerminalRewardService service) {
        EchoServiceRegistry.register(TerminalRewardService.class, service);
    }

    public static boolean storeTerminalRewards(ServerPlayer player, String missionId, List<ItemStack> rewards) {
        return terminalRewardService().storeRewards(player, missionId, rewards);
    }

    public static boolean claimTerminalRewards(ServerPlayer player) {
        return terminalRewardService().claimRewards(player);
    }

    private static TerminalPlacementService terminalPlacementService() {
        return EchoServiceRegistry.getOrDefault(TerminalPlacementService.class, TerminalPlacementService.NOOP);
    }

    private static TerminalRewardService terminalRewardService() {
        return EchoServiceRegistry.getOrDefault(TerminalRewardService.class, TerminalRewardService.NOOP);
    }
}
