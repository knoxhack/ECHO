# ECHO PowerGrid Feature Matrix

## Legend
- **Impl** = Code exists and is wired
- **Test** = GameTest or manual test covers it
- **Docs** = README/SMOKE_TEST/docs are accurate
- **Gap** = Known deficiency

## Core Energy

| Feature | Impl | Test | Docs | Gap |
|---------|------|------|------|-----|
| EP unit and internal math | Yes | Partial | Yes | — |
| Creative Power Source | Yes | Yes | Yes | Admin-only; OK |
| Scrap Burner Generator (fuel slot, burn time, buffer) | Yes | Yes | Yes | — |
| Hand Crank Generator | Partial | No | Yes | No manual crank interaction; uses generic daytime logic |
| Solar Panel | Partial | No | Yes | Only generic daytime logic; no sky/weather/dimension checks |
| Small Battery Bank (charge/discharge, capacity) | Yes | Yes | Yes | — |
| Low Voltage Cable (connection shapes, placement) | Yes | Yes | Yes | — |
| Industrial Cable (higher transfer limit) | Yes | Partial | Yes | — |
| Test Power Consumer | Yes | Yes | Yes | Dev-only; OK |
| Creative Power Sink | Yes | Partial | Yes | Dev-only; OK |
| Generator type explicit enum | No | No | Yes | Inferred from usesFuel/bufferSize/rate |
| Solar weather modifiers (WorldCore) | No | No | Yes | Placeholder integration |
| Hand crank cooldown/burst rules | No | No | Yes | Not implemented |
| Power loss over cable distance | No | No | Yes | Config exists, code missing |

## Network Topology

| Feature | Impl | Test | Docs | Gap |
|---------|------|------|------|-----|
| Network discovery (BFS) | Yes | Partial | Yes | — |
| Network dirty rebuild batching | Yes | Partial | Yes | — |
| Network split on cable removal | Partial | No | Partial | Leaves orphaned nodes in old network |
| Network merge on cable placement | Yes | Partial | Yes | — |
| Transfer limit (network-wide min) | Yes | Partial | Yes | Should be per-path, not global min |
| Load priority tiers | No | No | Yes | Not implemented |
| Idle network sleep | No | No | Yes | Config exists, code missing |
| Large network stress limits | Partial | No | Yes | Max size enforced, no performance tests |

## Overload / Brownout / Safety

| Feature | Impl | Test | Docs | Gap |
|---------|------|------|------|-----|
| Brownout detection and state | Yes | Partial | Yes | Partial power delivery exists |
| Overload detection and state | Yes | Partial | Yes | State set; no downstream effects wired |
| Breaker trip on overload | Partial | No | Yes | `BreakerBlock.tryTrip()` exists but never called |
| Breaker reset (screen + shift-click) | Yes | Yes | Yes | — |
| Breaker isolates power flow when tripped | No | No | Yes | Tripped breaker still traversed in BFS |
| Overload grace ticks | No | No | Yes | Config exists, code missing |
| Cable damage on overload | No | No | Yes | Config exists, code missing |
| Extreme overload explosions | No | No | Yes | Config exists, code missing; server safety config exists |
| Player feedback (trip message, meter alert) | Partial | No | Yes | Breaker message only |

## Control / UX

| Feature | Impl | Test | Docs | Gap |
|---------|------|------|------|-----|
| Power Node screen (all node types) | Yes | Partial | Yes | Fuel slot + bars + readouts + refresh button |
| Substation screen | Yes | Partial | Yes | Passive monitor only |
| Power Meter screen | Partial | No | Partial | Opens generic PowerNode screen; no dedicated GUI |
| Emergency Breaker screen | Yes | Yes | Yes | — |
| Grid Diagnostic Tool | Yes | Partial | Yes | Right-click info |
| Substation as grid coordinator | No | No | Yes | Future feature; currently passive |

## FE Compatibility

| Feature | Impl | Test | Docs | Gap |
|---------|------|------|------|-----|
| FE bridge config toggle | Yes | Partial | Yes | — |
| FE -> EP insert with transaction rollback | Yes | Yes | Yes | — |
| EP -> FE extract with transaction rollback | Yes | Partial | Yes | — |
| Sided capability registration | Partial | No | Yes | Registered for generator + battery only |

## ECHO Ecosystem Integrations

| Feature | Impl | Test | Docs | Gap |
|---------|------|------|------|-----|
| Terminal tab + packet sync | Yes | Partial | Yes | — |
| Terminal archive entries | Yes | Partial | Yes | — |
| HoloMap Power Networks layer | Yes | Partial | Yes | — |
| Lens scan data | Yes | Partial | Yes | — |
| MultiblockCore power provider | Yes | Partial | Yes | — |
| Industrial Nexus status bridge | Partial | No | Yes | Only status helpers; no real EP draw for tasks |
| RuntimeGuard tick budgets | No | No | Yes | Explicit placeholder comment |
| WorldCore weather/region hooks | No | No | Yes | Declared in mods.toml; no code |

## Commands / Config

| Feature | Impl | Test | Docs | Gap |
|---------|------|------|------|-----|
| `/echo_power status` | Yes | Partial | Yes | — |
| `/echo_power inspect` | Yes | Partial | Yes | — |
| `/echo_power networks` | Yes | Partial | Yes | — |
| `/echo_power debug_chunk` | Yes | Partial | Yes | — |
| `/echo_power give_test_kit` | Yes | Partial | Yes | — |
| `/echo_power set_energy` | Yes | Partial | Yes | — |
| `/echo_power reset_network` | Yes | Partial | Yes | — |
| All config options functional | Partial | Partial | Yes | Loss, idle sleep, overload effects missing |

## Persistence / MP / Performance

| Feature | Impl | Test | Docs | Gap |
|---------|------|------|------|-----|
| Block entity save/load (energy, fuel, burn) | Yes | Yes | Yes | — |
| Network rebuild on dimension load | Yes | Partial | Yes | — |
| Cross-chunk cable networks | Yes | Partial | Yes | — |
| Dedicated server safety | Partial | No | Yes | No client-only class crashes known |
| Update budget / batching | Partial | Partial | Yes | Rebuild batch exists; no RuntimeGuard hook |

## Acceptance Definition

“Complete” means:
1. Every row above marked **Docs = Yes** must also be **Impl = Yes**.
2. Every advertised survival feature must have a **Test = Yes**.
3. No placeholder integration comments or “future” claims in user-facing docs.
4. Creative/test blocks are not required for normal gameplay validation.
5. All build commands pass (`compileJava`, `runGameTestServer`, `buildEchoWorkspace`).
