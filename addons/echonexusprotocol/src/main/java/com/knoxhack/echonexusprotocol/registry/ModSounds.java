package com.knoxhack.echonexusprotocol.registry;

import com.knoxhack.echonexusprotocol.EchoNexusProtocol;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
   private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, EchoNexusProtocol.MODID);
   public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_PROCESS = sound("machine_process");
   public static final DeferredHolder<SoundEvent, SoundEvent> SEAL_ACTIVATE = sound("seal_activate");
   public static final DeferredHolder<SoundEvent, SoundEvent> FIELD_STABILIZE = sound("field_stabilize");
   public static final DeferredHolder<SoundEvent, SoundEvent> CORRUPTION_LEAK = sound("corruption_leak");
   public static final DeferredHolder<SoundEvent, SoundEvent> MONOLITH_ACTIVATE = sound("monolith_activate");
   public static final DeferredHolder<SoundEvent, SoundEvent> REALITY_TEAR_PULSE = sound("reality_tear_pulse");
   public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_PULSE = sound("warden_pulse");
   public static final DeferredHolder<SoundEvent, SoundEvent> GUARDIAN_PHASE = sound("guardian_phase");
   public static final DeferredHolder<SoundEvent, SoundEvent> ENDING_CHOICE = sound("ending_choice");

   private ModSounds() {
   }

   public static void register(IEventBus eventBus) {
      SOUNDS.register(eventBus);
   }

   private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
      Identifier id = Identifier.fromNamespaceAndPath(EchoNexusProtocol.MODID, name);
      return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
   }
}
