package com.knoxhack.echoterminal;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import com.knoxhack.echoterminal.discovery.TerminalDiscoveryProvider;
import com.knoxhack.echoterminal.player.TerminalPlayerData;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.resources.Identifier;

/**
 * Common/server registrations for built-in terminal actions.
 */
public final class BuiltinTerminalCommonIntegration {
    public static final Identifier REWARD_INBOX =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "reward_inbox");
    public static final Identifier CLAIM_REWARDS =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "claim_rewards");
    public static final Identifier ARCHIVES =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "archives");
    public static final Identifier MARK_ARCHIVE_READ =
            Identifier.fromNamespaceAndPath(EchoTerminal.MODID, "mark_archive_read");

    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private BuiltinTerminalCommonIntegration() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        EchoCoreServices.registerDiscoveryProvider(new TerminalDiscoveryProvider());
        registerActions();
    }

    public static void registerActionsForTests() {
        registerActions();
    }

    private static void registerActions() {
        TerminalActionRegistry.register(REWARD_INBOX, CLAIM_REWARDS,
                (player, payload) -> EchoCoreServices.claimTerminalRewards(player));
        TerminalActionRegistry.register(ARCHIVES, MARK_ARCHIVE_READ, (player, payload) -> {
            if (player == null) {
                return;
            }
            Identifier archiveId = Identifier.tryParse(payload == null ? "" : payload);
            if (archiveId == null) {
                return;
            }
            TerminalPlayerData data = TerminalPlayerData.get(player);
            data.markArchiveRead(archiveId);
            TerminalPlayerData.saveAndSync(player, data);
        });
    }
}
