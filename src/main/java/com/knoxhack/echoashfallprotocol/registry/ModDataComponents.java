package com.knoxhack.echoashfallprotocol.registry;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.item.AshfallTooltip;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, EchoAshfallProtocol.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STORED_ENERGY =
            DATA_COMPONENT_TYPES.register("stored_energy", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AshfallTooltip>> ASHFALL_TOOLTIP =
            DATA_COMPONENT_TYPES.register("ashfall_tooltip", () -> DataComponentType.<AshfallTooltip>builder()
                    .persistent(AshfallTooltip.CODEC)
                    .networkSynchronized(AshfallTooltip.STREAM_CODEC)
                    .build());

    private ModDataComponents() {
    }
}
