package com.knoxhack.echocore.client.model;

import com.knoxhack.echocore.EchoCore;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public final class EchoMobModelLayers {
    public static final ModelLayerLocation HUMANOID = layer("echo_mob_humanoid");
    public static final ModelLayerLocation SURVIVOR_NPC = layer("echo_mob_survivor_npc");
    public static final ModelLayerLocation STATION_SUIT = layer("echo_mob_station_suit");
    public static final ModelLayerLocation WRAITH = layer("echo_mob_wraith");
    public static final ModelLayerLocation DRONE = layer("echo_mob_drone");
    public static final ModelLayerLocation QUADRUPED = layer("echo_mob_quadruped");
    public static final ModelLayerLocation CRAWLER = layer("echo_mob_crawler");
    public static final ModelLayerLocation SLIME = layer("echo_mob_slime");
    public static final ModelLayerLocation HEAVY_BOSS = layer("echo_mob_heavy_boss");
    public static final ModelLayerLocation INDUSTRIAL_CONSTRUCT = layer("echo_mob_industrial_construct");
    public static final ModelLayerLocation ROCKET = layer("echo_mob_rocket");

    private EchoMobModelLayers() {
    }

    public static ModelLayerLocation forFamily(EchoMobFamily family) {
        return switch (family) {
            case HUMANOID -> HUMANOID;
            case SURVIVOR_NPC -> SURVIVOR_NPC;
            case STATION_SUIT -> STATION_SUIT;
            case WRAITH -> WRAITH;
            case DRONE -> DRONE;
            case QUADRUPED -> QUADRUPED;
            case CRAWLER -> CRAWLER;
            case SLIME -> SLIME;
            case HEAVY_BOSS -> HEAVY_BOSS;
            case INDUSTRIAL_CONSTRUCT -> INDUSTRIAL_CONSTRUCT;
            case ROCKET -> ROCKET;
        };
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(EchoCore.MODID, name), "main");
    }
}
