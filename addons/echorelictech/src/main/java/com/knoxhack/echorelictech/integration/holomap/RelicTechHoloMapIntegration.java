package com.knoxhack.echorelictech.integration.holomap;

import com.knoxhack.echorelictech.EchoRelicTech;
import net.minecraft.resources.Identifier;

public class RelicTechHoloMapIntegration {
    public static void register() {
        EchoRelicTech.LOGGER.info("ECHO HoloMap integration loaded for RelicTech.");
        try {
            Class<?> echoCoreServices = Class.forName("com.knoxhack.echocore.api.EchoCoreServices");
            Class<?> mapProvider = Class.forName("com.knoxhack.echocore.api.IMapDataProvider");
            Class<?> mapLayer = Class.forName("com.knoxhack.echocore.api.EchoMapLayer");
            Class<?> mapMarker = Class.forName("com.knoxhack.echocore.api.EchoMapMarker");
            Class<?> markerKind = Class.forName("com.knoxhack.echocore.api.IMapMarker$MarkerKind");
            Class<?> markerState = Class.forName("com.knoxhack.echocore.api.IMapMarker$MarkerState");

            java.lang.reflect.Method register = echoCoreServices.getMethod("registerMapDataProvider", mapProvider);

            Object provider = java.lang.reflect.Proxy.newProxyInstance(
                RelicTechHoloMapIntegration.class.getClassLoader(),
                new Class[]{mapProvider},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("providerId".equals(name)) return Identifier.fromNamespaceAndPath("echorelictech", "vault_markers");
                    if ("layers".equals(name)) {
                        Object layer = mapLayer.getConstructor(Identifier.class, String.class, int.class, int.class, boolean.class)
                            .newInstance(Identifier.fromNamespaceAndPath("echoholomap", "layer/missions"), "Relic Vaults", 50, 0xFFAA44, true);
                        return java.util.List.of(layer);
                    }
                    if ("markers".equals(name)) return java.util.List.of();
                    return null;
                }
            );
            register.invoke(null, provider);
        } catch (Exception | LinkageError e) {
            EchoRelicTech.LOGGER.warn("HoloMap integration could not fully register.", e);
        }
    }
}
