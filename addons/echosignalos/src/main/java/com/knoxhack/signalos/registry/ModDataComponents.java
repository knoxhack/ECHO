package com.knoxhack.signalos.registry;

import com.knoxhack.signalos.SignalOS;
import com.knoxhack.signalos.api.SignalOsDriveData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SignalOS.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SignalOsDriveData>> DRIVE_DATA =
            DATA_COMPONENT_TYPES.register("drive_data", () -> DataComponentType.<SignalOsDriveData>builder()
                    .persistent(SignalOsDriveData.CODEC)
                    .networkSynchronized(SignalOsDriveData.STREAM_CODEC)
                    .build());

    private ModDataComponents() {
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
