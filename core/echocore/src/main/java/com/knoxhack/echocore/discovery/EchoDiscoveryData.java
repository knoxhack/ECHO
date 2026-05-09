package com.knoxhack.echocore.discovery;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class EchoDiscoveryData implements ValueIOSerializable {
    public static final StreamCodec<RegistryFriendlyByteBuf, EchoDiscoveryData> STREAM_CODEC = StreamCodec.of(
            EchoDiscoveryData::writeSync,
            EchoDiscoveryData::readSync);

    private final Set<String> discoveredIds = new LinkedHashSet<>();

    public boolean discover(Identifier id) {
        return id != null && discoveredIds.add(id.toString());
    }

    public boolean contains(Identifier id) {
        return id != null && discoveredIds.contains(id.toString());
    }

    public Set<String> discoveredIds() {
        return Set.copyOf(discoveredIds);
    }

    public static EchoDiscoveryData get(Player player) {
        return player == null
                ? new EchoDiscoveryData()
                : player.getData(com.knoxhack.echocore.registry.ModAttachments.DISCOVERY_DATA.get());
    }

    public static void saveAndSync(ServerPlayer player, EchoDiscoveryData data) {
        if (player == null || data == null) {
            return;
        }
        player.setData(com.knoxhack.echocore.registry.ModAttachments.DISCOVERY_DATA.get(), data);
        player.syncData(com.knoxhack.echocore.registry.ModAttachments.DISCOVERY_DATA.get());
    }

    private static void writeSync(RegistryFriendlyByteBuf buf, EchoDiscoveryData data) {
        buf.writeVarInt(data.discoveredIds.size());
        for (String id : data.discoveredIds) {
            buf.writeUtf(id);
        }
    }

    private static EchoDiscoveryData readSync(RegistryFriendlyByteBuf buf) {
        EchoDiscoveryData data = new EchoDiscoveryData();
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            String value = buf.readUtf();
            if (Identifier.tryParse(value) != null) {
                data.discoveredIds.add(value);
            }
        }
        return data;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("discoveredCount", discoveredIds.size());
        int index = 0;
        for (String id : discoveredIds) {
            output.putString("discovered_" + index++, id);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        discoveredIds.clear();
        int count = input.getIntOr("discoveredCount", 0);
        for (int i = 0; i < count; i++) {
            String value = input.getStringOr("discovered_" + i, "");
            if (Identifier.tryParse(value) != null) {
                discoveredIds.add(value);
            }
        }
    }
}
