package com.knoxhack.echopowergrid.integration.holomap;

import com.knoxhack.echocore.api.EchoMapLayer;
import com.knoxhack.echocore.api.EchoMapMarker;
import com.knoxhack.echocore.api.IMapDataProvider;
import com.knoxhack.echocore.api.IMapLayer;
import com.knoxhack.echocore.api.IMapMarker;
import com.knoxhack.echopowergrid.EchoPowerGrid;
import com.knoxhack.echopowergrid.api.EchoGridState;
import com.knoxhack.echopowergrid.api.EchoPowerGridApi;
import com.knoxhack.echopowergrid.api.PowerGridNetworkSummary;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PowerGridMapDataProvider implements IMapDataProvider {
    public static final PowerGridMapDataProvider INSTANCE = new PowerGridMapDataProvider();
    private static final Identifier PROVIDER_ID = Identifier.fromNamespaceAndPath(EchoPowerGrid.MODID, "holomap/power_grid");
    private static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(EchoPowerGrid.MODID, "power_networks");
    private static final Identifier ICON = Identifier.fromNamespaceAndPath(EchoPowerGrid.MODID, "textures/gui/holomap/power_network.png");

    private PowerGridMapDataProvider() {
    }

    @Override
    public Identifier providerId() {
        return PROVIDER_ID;
    }

    @Override
    public List<IMapLayer> layers(Player player) {
        return List.of(new EchoMapLayer(LAYER_ID, "Power Networks", 82, 0xFF55DDEF, true));
    }

    @Override
    public List<IMapMarker> markers(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(serverPlayer.level() instanceof ServerLevel level)) {
            return List.of();
        }
        return EchoPowerGridApi.loadedNetworkSummaries(level).stream()
                .map(PowerGridMapDataProvider::marker)
                .map(IMapMarker.class::cast)
                .toList();
    }

    @Override
    public boolean refresh(ServerPlayer player, String reason) {
        return player != null;
    }

    private static EchoMapMarker marker(PowerGridNetworkSummary summary) {
        Identifier markerId = Identifier.fromNamespaceAndPath(EchoPowerGrid.MODID,
                "power_network/" + summary.networkId().toString().replace('-', '_'));
        String title = "Power Network " + summary.networkId().toString().substring(0, 8);
        String status = summary.state().name() + " / " + summary.quality().name();
        String summaryText = status
                + " / gen " + summary.totalGeneration() + " EP/t"
                + " / demand " + summary.totalDemand() + " EP/t"
                + " / stored " + summary.totalStored() + "/" + summary.totalCapacity() + " EP"
                + (summary.state() == EchoGridState.BROWNOUT || summary.state() == EchoGridState.OVERLOADED
                        ? " / attention required"
                        : "");
        return new EchoMapMarker(
                markerId,
                LAYER_ID,
                PROVIDER_ID,
                IMapMarker.MarkerKind.BASE_OUTPOST,
                IMapMarker.MarkerState.DISCOVERED,
                title,
                summaryText,
                summary.dimension(),
                summary.anchorPos().getX() + 0.5D,
                summary.anchorPos().getY(),
                summary.anchorPos().getZ() + 0.5D,
                28.0F,
                ICON,
                null,
                -1,
                true);
    }
}
