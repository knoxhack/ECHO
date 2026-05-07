package com.knoxhack.echoindustrialnexus.registry;

import com.knoxhack.echoindustrialnexus.EchoIndustrialNexus;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
   private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, EchoIndustrialNexus.MODID);
   public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_HUM = sound("machine_hum");
   public static final DeferredHolder<SoundEvent, SoundEvent> GRINDER_LOOP = sound("grinder_loop");
   public static final DeferredHolder<SoundEvent, SoundEvent> SHREDDER_LOOP = sound("shredder_loop");
   public static final DeferredHolder<SoundEvent, SoundEvent> PIPE_TRANSFER = sound("pipe_transfer");
   public static final DeferredHolder<SoundEvent, SoundEvent> OVERHEAT_ALARM = sound("overheat_alarm");
   public static final DeferredHolder<SoundEvent, SoundEvent> SCRUBBER_OPERATION = sound("scrubber_operation");
   public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_PHASE = sound("warden_phase");
   public static final DeferredHolder<SoundEvent, SoundEvent> POI_AMBIENCE = sound("poi_ambience");

   private ModSounds() {
   }

   public static void register(IEventBus eventBus) {
      SOUNDS.register(eventBus);
   }

   private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
      Identifier id = Identifier.fromNamespaceAndPath(EchoIndustrialNexus.MODID, name);
      return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
   }
}
