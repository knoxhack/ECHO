package com.knoxhack.echorecovery.registry;

import com.knoxhack.echorecovery.EchoRecovery;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(Registries.SOUND_EVENT, EchoRecovery.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVE_OPEN =
        sound("grave_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVE_CLOSE =
        sound("grave_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVE_RECOVER =
        sound("grave_recover");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRAVE_CREATE =
        sound("grave_create");

    private ModSounds() {}

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(EchoRecovery.MODID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
