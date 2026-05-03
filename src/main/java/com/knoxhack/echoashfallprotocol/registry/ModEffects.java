package com.knoxhack.echoashfallprotocol.registry;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.effect.AllianceEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for custom mob effects.
 */
public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, EchoAshfallProtocol.MODID);

    // === NEXUS PATH EFFECTS ===
    public static final Supplier<MobEffect> ALLIANCE = EFFECTS.register("alliance", AllianceEffect::new);
}
