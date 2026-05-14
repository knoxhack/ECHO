package com.knoxhack.echopowergrid;

import com.knoxhack.echopowergrid.client.screen.SubstationScreen;
import com.knoxhack.echopowergrid.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = "echopowergrid", dist = Dist.CLIENT)
public class EchoPowerGridClient {
    public EchoPowerGridClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerScreens);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.SUBSTATION.get(), SubstationScreen::new);
    }
}
