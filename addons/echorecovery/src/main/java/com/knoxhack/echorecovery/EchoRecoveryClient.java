package com.knoxhack.echorecovery;

import com.knoxhack.echorecovery.client.screen.GraveScreen;
import com.knoxhack.echorecovery.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = EchoRecovery.MODID, dist = Dist.CLIENT)
public class EchoRecoveryClient {

    @SubscribeEvent
    static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.GRAVE.get(), GraveScreen::new);
    }
}
