package com.knoxhack.echothemecore;

import com.knoxhack.echothemecore.client.ClientThemeCache;
import com.knoxhack.echothemecore.client.vanilla.VanillaUiSkinLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = EchoThemeCore.MODID, dist = Dist.CLIENT)
public final class EchoThemeCoreClient {
    public EchoThemeCoreClient() {
        NeoForge.EVENT_BUS.addListener(ClientThemeCache::onClientTick);
        NeoForge.EVENT_BUS.addListener(VanillaUiSkinLayer::onScreenRender);
        NeoForge.EVENT_BUS.addListener(VanillaUiSkinLayer::onRenderGui);
    }
}
