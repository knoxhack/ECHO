package com.knoxhack.echorelictech.integration.missioncore;

import com.knoxhack.echorelictech.EchoRelicTech;
import com.knoxhack.echorelictech.api.event.RelicTechEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class RelicTechMissionCoreIntegration {
    public static void register() {
        EchoRelicTech.LOGGER.info("ECHO MissionCore integration loaded for RelicTech.");
        try {
            registerMissionContent();
            registerEventHooks();
        } catch (Exception | LinkageError e) {
            EchoRelicTech.LOGGER.warn("MissionCore integration could not fully register.", e);
        }
    }

    private static void registerMissionContent() throws Exception {
        Class<?> echoServices = Class.forName("com.knoxhack.echocore.api.EchoCoreServices");
        Class<?> registrarClass = Class.forName("com.knoxhack.echocore.api.MissionContentRegistrar");
        java.lang.reflect.Method registerContent = echoServices.getMethod("registerMissionContent", String.class, registrarClass);

        Object registrar = java.lang.reflect.Proxy.newProxyInstance(
            RelicTechMissionCoreIntegration.class.getClassLoader(),
            new Class[]{registrarClass},
            (proxy, method, args) -> {
                if (!"register".equals(method.getName())) return null;
                Object registry = args[0];
                registerChapter(registry);
                registerMissions(registry);
                return null;
            }
        );
        registerContent.invoke(null, "echorelictech", registrar);
    }

    private static void registerChapter(Object registry) throws Exception {
        Class<?> chapterClass = Class.forName("com.knoxhack.echomissioncore.mission.MissionChapterDefinition");
        Object chapter = chapterClass.getConstructor(Identifier.class, String.class, String.class, int.class, int.class)
            .newInstance(
                Identifier.fromNamespaceAndPath("echorelictech", "relic_ops"),
                "Relic Operations",
                "Recover, analyze, and stabilize pre-Gridfall relics.",
                60,
                0xFFAA44
            );
        registry.getClass().getMethod("registerChapter", String.class, chapterClass).invoke(registry, "echorelictech", chapter);
    }

    private static void registerMissions(Object registry) throws Exception {
        Class<?> missionDefClass = Class.forName("com.knoxhack.echomissioncore.mission.MissionDefinition");
        Class<?> builderClass = Class.forName("com.knoxhack.echomissioncore.mission.MissionDefinition$Builder");
        Class<?> objectiveClass = Class.forName("com.knoxhack.echomissioncore.mission.ObjectiveDefinition");
        Class<?> rewardClass = Class.forName("com.knoxhack.echomissioncore.mission.RewardDefinition");
        Class<?> missionTypeClass = Class.forName("com.knoxhack.echomissioncore.mission.MissionObjectiveType");

        // Recover relic mission
        registerMission(registry, builderClass, missionDefClass, objectiveClass, rewardClass, missionTypeClass,
            "recover_relic", "relic_ops", "Recover a Relic", "Find an Unidentified Relic in a vault.", "Relic recovered.",
            missionTypeClass.getEnumConstants()[0], // OBTAIN_ITEM
            Identifier.fromNamespaceAndPath("echorelictech", "unidentified_relic"),
            1);

        // Analyze relic mission
        registerMission(registry, builderClass, missionDefClass, objectiveClass, rewardClass, missionTypeClass,
            "analyze_relic", "relic_ops", "Analyze a Relic", "Use a Relic Analyzer to identify a relic.", "Relic analyzed.",
            missionTypeClass.getEnumConstants()[11], // REPAIR_MACHINE (closest to analyze)
            Identifier.fromNamespaceAndPath("echorelictech", "relic_analyzer"),
            1);

        // Stabilize relic mission
        registerMission(registry, builderClass, missionDefClass, objectiveClass, rewardClass, missionTypeClass,
            "stabilize_relic", "relic_ops", "Stabilize a Relic", "Use a Prototype Workbench to stabilize a relic.", "Relic stabilized.",
            missionTypeClass.getEnumConstants()[11], // REPAIR_MACHINE
            Identifier.fromNamespaceAndPath("echorelictech", "prototype_workbench"),
            1);
    }

    private static void registerMission(Object registry, Class<?> builderClass, Class<?> missionDefClass, Class<?> objectiveClass, Class<?> rewardClass, Class<?> missionTypeClass,
                                        String id, String chapterId, String title, String briefing, String fieldGuide,
                                        Object objectiveType, Identifier target, int required) throws Exception {
        java.lang.reflect.Method builderMethod = missionDefClass.getMethod("builder", Identifier.class, Identifier.class);
        Object builder = builderMethod.invoke(null, Identifier.fromNamespaceAndPath("echorelictech", id), Identifier.fromNamespaceAndPath("echorelictech", chapterId));

        builder.getClass().getMethod("phase", String.class, String.class, int.class, int.class).invoke(builder, chapterId, title, 0, 1);
        builder.getClass().getMethod("text", String.class, String.class, String.class).invoke(builder, title, briefing, fieldGuide);

        Object objective = objectiveClass.getMethod("simple", Identifier.class, missionTypeClass, String.class, String.class, net.minecraft.world.item.ItemStack.class, int.class)
            .invoke(null, Identifier.fromNamespaceAndPath("echorelictech", id + "/obj"), objectiveType, title, "", net.minecraft.world.item.ItemStack.EMPTY, required);
        builder.getClass().getMethod("objective", objectiveClass).invoke(builder, objective);

        Object reward = rewardClass.getMethod("item", Identifier.class, Class.forName("com.knoxhack.echomissioncore.mission.MissionRewardClaimMode"), net.minecraft.world.item.ItemStack.class)
            .invoke(null, Identifier.fromNamespaceAndPath("echorelictech", id + "/reward"), Class.forName("com.knoxhack.echomissioncore.mission.MissionRewardClaimMode").getEnumConstants()[1], new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD, 2));
        builder.getClass().getMethod("reward", rewardClass).invoke(builder, reward);

        Object mission = builder.getClass().getMethod("build").invoke(builder);
        registry.getClass().getMethod("registerMission", String.class, missionDefClass).invoke(registry, "echorelictech", mission);
    }

    private static void registerEventHooks() throws Exception {
        Class<?> serviceClass = Class.forName("com.knoxhack.echomissioncore.service.MissionCoreService");
        Object instance = serviceClass.getField("INSTANCE").get(null);
        java.lang.reflect.Method recordObjective = serviceClass.getMethod("recordObjective", ServerPlayer.class, Class.forName("com.knoxhack.echomissioncore.mission.MissionObjectiveType"), Identifier.class, int.class, java.util.Map.class);

        RelicTechEvents.onAnalyze(e -> {
            try {
                recordObjective.invoke(instance, e.player(), Class.forName("com.knoxhack.echomissioncore.mission.MissionObjectiveType").getEnumConstants()[11], Identifier.fromNamespaceAndPath("echorelictech", "relic_analyzer"), 1, java.util.Map.of("source", "echorelictech"));
            } catch (Exception ignored) {}
        });

        RelicTechEvents.onVaultDiscover(e -> {
            try {
                recordObjective.invoke(instance, e.player(), Class.forName("com.knoxhack.echomissioncore.mission.MissionObjectiveType").getEnumConstants()[6], e.vaultId(), 1, java.util.Map.of("source", "echorelictech"));
            } catch (Exception ignored) {}
        });
    }
}
