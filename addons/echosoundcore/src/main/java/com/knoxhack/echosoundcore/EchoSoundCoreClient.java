package com.knoxhack.echosoundcore;

import com.knoxhack.echosoundcore.client.ambience.SoundCoreAmbienceManager;
import com.knoxhack.echosoundcore.client.music.SoundCoreMusicManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = EchoSoundCore.MODID, dist = Dist.CLIENT)
public class EchoSoundCoreClient {
    public EchoSoundCoreClient(IEventBus modEventBus) {
        modEventBus.addListener(this::clientSetup);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onSelectMusic);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        EchoSoundCore.LOGGER.info("ECHO: SoundCore client audio managers initialized.");
    }

    private void onClientTick(ClientTickEvent.Post event) {
        SoundCoreMusicManager.tick();
        SoundCoreAmbienceManager.tick();
    }

    private void onSelectMusic(SelectMusicEvent event) {
        if (SoundCoreMusicManager.shouldSuppressVanillaMusic()) {
            event.overrideMusic(null);
        }
    }
}
