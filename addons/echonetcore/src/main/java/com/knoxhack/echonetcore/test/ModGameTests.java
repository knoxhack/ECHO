package com.knoxhack.echonetcore.test;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoServiceRegistry;
import com.knoxhack.echocore.api.network.EchoPacketDirection;
import com.knoxhack.echocore.api.network.EchoPacketKind;
import com.knoxhack.echocore.api.network.NoOpNetworkService;
import com.knoxhack.echocore.api.network.PacketDebugHook;
import com.knoxhack.echonetcore.EchoNetCore;
import com.knoxhack.echonetcore.api.EchoRateLimitPolicy;
import com.knoxhack.echonetcore.api.EchoRateLimiter;
import com.knoxhack.echonetcore.config.EchoNetCoreConfig;
import com.knoxhack.echonetcore.network.DiscoveryToastPacket;
import com.knoxhack.echonetcore.network.EchoFactionSyncPacket;
import com.knoxhack.echonetcore.network.EchoNetDebug;
import com.knoxhack.echonetcore.service.NetCoreNetworkService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, EchoNetCore.MODID);

    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SERVICE_FALLBACKS =
            TEST_FUNCTIONS.register("service_fallbacks", () -> ModGameTests::serviceFallbacks);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PACKET_IDS =
            TEST_FUNCTIONS.register("packet_ids", () -> ModGameTests::packetIds);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RATE_LIMITER =
            TEST_FUNCTIONS.register("rate_limiter", () -> ModGameTests::rateLimiter);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> DEBUG_HOOKS =
            TEST_FUNCTIONS.register("debug_hooks", () -> ModGameTests::debugHooks);

    private ModGameTests() {
    }

    public static void register(IEventBus eventBus) {
        TEST_FUNCTIONS.register(eventBus);
    }

    public static void registerTests(RegisterGameTestsEvent event) {
        if (!shouldRegisterTests()) {
            return;
        }
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("netcore"));
        register(event, environment, "service_fallbacks", SERVICE_FALLBACKS.getId());
        register(event, environment, "packet_ids", PACKET_IDS.getId());
        register(event, environment, "rate_limiter", RATE_LIMITER.getId());
        register(event, environment, "debug_hooks", DEBUG_HOOKS.getId());
    }

    private static void serviceFallbacks(GameTestHelper helper) {
        EchoServiceRegistry.withClearedForTests(() -> {
            helper.assertTrue(EchoCoreServices.networkService() == NoOpNetworkService.INSTANCE,
                    "Core should expose NoOpNetworkService without NetCore registration.");
            EchoCoreServices.registerNetworkService(NetCoreNetworkService.INSTANCE);
            helper.assertTrue(EchoCoreServices.networkService() == NetCoreNetworkService.INSTANCE,
                    "NetCore service should register through Echo Core.");
        });
        helper.succeed();
    }

    private static void packetIds(GameTestHelper helper) {
        helper.assertTrue(EchoFactionSyncPacket.ID.equals(Identifier.fromNamespaceAndPath("echocore", "faction_sync")),
                "Faction sync packet id must preserve the old Core wire id.");
        helper.assertTrue(DiscoveryToastPacket.ID.equals(Identifier.fromNamespaceAndPath("echocore", "discovery_toast")),
                "Discovery toast packet id must preserve the old Core wire id.");
        helper.succeed();
    }

    private static void rateLimiter(GameTestHelper helper) {
        EchoRateLimiter.clearForTests();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Identifier packetId = Identifier.fromNamespaceAndPath(EchoNetCore.MODID, "test_action");
        EchoRateLimitPolicy policy = EchoRateLimitPolicy.of(10, "test");
        helper.assertTrue(EchoRateLimiter.tryAcquire(player, packetId, policy),
                "First packet should pass the rate limiter.");
        helper.assertFalse(EchoRateLimiter.tryAcquire(player, packetId, policy),
                "Second same-tick packet should be rate-limited.");
        helper.assertTrue(EchoRateLimiter.tryAcquire(player, packetId, EchoRateLimitPolicy.NONE),
                "Disabled policies should always pass.");
        EchoRateLimiter.clearForTests();
        helper.succeed();
    }

    private static void debugHooks(GameTestHelper helper) {
        AtomicInteger count = new AtomicInteger();
        PacketDebugHook hook = event -> {
            if (event.payloadId().equals(Identifier.fromNamespaceAndPath(EchoNetCore.MODID, "debug_probe"))) {
                count.incrementAndGet();
            }
        };
        boolean previousDebugLogging = EchoNetCoreConfig.DEBUG_PACKET_LOGGING.get();
        boolean previousDroppedLogging = EchoNetCoreConfig.LOG_DROPPED_PACKETS.get();
        boolean previousDebugPackets = EchoNetCoreConfig.ENABLE_DEBUG_PACKETS.get();
        try {
            EchoNetCoreConfig.DEBUG_PACKET_LOGGING.set(false);
            EchoNetCoreConfig.LOG_DROPPED_PACKETS.set(false);
            EchoNetCoreConfig.ENABLE_DEBUG_PACKETS.set(false);
            EchoNetDebug.HOOKS.add(hook);
            EchoNetDebug.emit(Identifier.fromNamespaceAndPath(EchoNetCore.MODID, "debug_probe"),
                    EchoPacketDirection.SERVERBOUND, EchoPacketKind.DEBUG_DEV, "tester", true, "probe");
            helper.assertTrue(count.get() == 0, "Packet debug hooks should stay silent when debug config is disabled.");

            EchoNetCoreConfig.DEBUG_PACKET_LOGGING.set(true);
            EchoNetDebug.emit(Identifier.fromNamespaceAndPath(EchoNetCore.MODID, "debug_probe"),
                    EchoPacketDirection.SERVERBOUND, EchoPacketKind.DEBUG_DEV, "tester", true, "probe");
            helper.assertTrue(count.get() == 1, "Packet debug hooks should receive events when debug logging is enabled.");
        } finally {
            EchoNetDebug.HOOKS.remove(hook);
            EchoNetCoreConfig.DEBUG_PACKET_LOGGING.set(previousDebugLogging);
            EchoNetCoreConfig.LOG_DROPPED_PACKETS.set(previousDroppedLogging);
            EchoNetCoreConfig.ENABLE_DEBUG_PACKETS.set(previousDebugPackets);
        }
        helper.succeed();
    }

    private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
            String testName, Identifier functionId) {
        TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                environment,
                Identifier.withDefaultNamespace("empty"),
                100,
                0,
                true,
                net.minecraft.world.level.block.Rotation.NONE,
                false,
                1,
                1,
                false,
                2
        );
        event.registerTest(id(testName), new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, functionId), data));
    }

    private static boolean shouldRegisterTests() {
        String namespaces = System.getProperty("neoforge.enabledGameTestNamespaces", "");
        if (namespaces == null || namespaces.isBlank()) {
            return true;
        }
        for (String namespace : namespaces.split(",")) {
            String normalized = namespace.trim();
            if (normalized.equals(EchoNetCore.MODID) || normalized.equals("*") || normalized.equalsIgnoreCase("all")) {
                return true;
            }
        }
        return false;
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoNetCore.MODID, path);
    }
}
