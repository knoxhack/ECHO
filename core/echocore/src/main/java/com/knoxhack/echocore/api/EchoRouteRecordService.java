package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface EchoRouteRecordService {
    List<EchoRouteRecord> routeRecords(Player player);
}
