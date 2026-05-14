package com.knoxhack.echorelictech.registry;

import com.knoxhack.echorelictech.EchoRelicTech;
import com.knoxhack.echorelictech.api.relic.RelicInstanceData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, EchoRelicTech.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RelicInstanceData>> RELIC_DATA =
            DATA_COMPONENT_TYPES.register("relic_data", () -> DataComponentType.<RelicInstanceData>builder()
                    .persistent(RelicInstanceData.CODEC)
                    .networkSynchronized(RelicInstanceData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> NULL_CHARGE =
            DATA_COMPONENT_TYPES.register("null_charge", () -> DataComponentType.<Integer>builder()
                    .persistent(net.minecraft.util.ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<com.knoxhack.echorelictech.api.relic.UnidentifiedRelicData>> UNIDENTIFIED_RELIC_DATA =
            DATA_COMPONENT_TYPES.register("unidentified_relic_data", () -> DataComponentType.<com.knoxhack.echorelictech.api.relic.UnidentifiedRelicData>builder()
                    .persistent(com.knoxhack.echorelictech.api.relic.UnidentifiedRelicData.CODEC)
                    .networkSynchronized(com.knoxhack.echorelictech.api.relic.UnidentifiedRelicData.STREAM_CODEC)
                    .build());

    private ModDataComponents() {}

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
