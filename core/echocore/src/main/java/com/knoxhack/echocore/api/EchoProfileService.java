package com.knoxhack.echocore.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface EchoProfileService {
    EchoProfile profile(Player player);

    void saveProfile(ServerPlayer player, EchoProfile profile);

    EchoProgressLedger progressLedger(Player player);

    void saveProgressLedger(ServerPlayer player, EchoProgressLedger ledger);
}
