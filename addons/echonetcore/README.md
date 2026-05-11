# ECHO: NetCore

ECHO: NetCore is the shared packet, sync, server action, rate limiting, and debug network layer for ECHO addons.

## Packet Categories

- Clientbound sync packets mirror server-owned state to the logical client.
- Serverbound action packets represent player intent only; handlers must validate permissions, distance, ownership, inventory, menu state, and world state on the server.
- Debug/dev packets are disabled by default and require operator permissions when enabled.
- Optional addon packets should use optional registration and safe send helpers so missing consumers do not crash a server or client.

## Registering Packets

Use `EchoNetPayloads.optional(event)` to create the shared optional registrar, then register packets by category:

```java
PayloadRegistrar registrar = EchoNetPayloads.optional(event);
EchoNetPayloads.clientboundSync(registrar, MySyncPacket.TYPE, MySyncPacket.CODEC, MyNetwork::handleSync);
EchoNetPayloads.serverboundAction(registrar, MyActionPacket.TYPE, MyActionPacket.CODEC,
        EchoRateLimitPolicy.of(10, "my_action"), MyNetwork::handleAction);
```

Serverbound handlers receive a `ServerPlayer`; packets from non-server contexts are dropped before the handler runs. Rate-limited packets are dropped without mutating gameplay state.

## Sync Helpers

`EchoCoreServices.networkBridge()` exposes no-op-safe helpers for player data, world data, mission progress, visual state, machine/block-entity state, debug data, faction sync, and discovery toasts. NetCore supplies the real bridge when loaded; ECHO Core falls back to `NoOpNetworkService`.

Client code can subscribe to generic NetCore sync packets with `EchoClientSyncRegistry.register(type, channelId, consumer)`.

## Safe Sends

Use `EchoNetSend.toPlayer(player, payload, kind)` for optional clientbound sends. It catches missing-channel failures and emits packet debug events. Use `EchoNetClientActions.sendServerboundAction(payload)` from client-only classes for terminal buttons and other UI actions.

## Debug Logging

Packet logging is controlled by NetCore common config:

- `debugPacketLogging=false`
- `logDroppedPackets=false`
- `enableDebugPackets=false`

Debug logs and debug packet handlers stay silent unless explicitly enabled.
