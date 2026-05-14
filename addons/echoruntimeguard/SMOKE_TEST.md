# ECHO RuntimeGuard Smoke Test

1. Run `./gradlew :echoruntimeguard:build`.
2. Launch a dev client.
3. Run `/echo_perf status`.
4. Run `/echo_perf mode potato`.
5. Run `/echo_perf mode balanced`.
6. Run `/echo_perf emergency on`, then `/echo_perf emergency off`.
7. Run `/echo_perf dump`.
8. Confirm a report appears under `run/echo-runtimeguard/reports/`.
9. Launch or compile the dedicated server run and confirm no client-only class crash occurs.
10. Run `/echo_perf particles` and confirm budget counters are present.
11. Run `/echo_perf multiblocks` and confirm validation queue data is present.
12. Confirm `SmartTickService` returns different rates for nearby, far, and emergency work in GameTests.
13. Confirm RuntimeGuard config files load.
14. Confirm server-impacting commands require gamemaster permissions.
15. If MultiblockCore is present, form a controller and confirm scheduled revalidation appears in `/echo_perf multiblocks`.
16. If HoloMap is present, run a manual sync and confirm `/echo_perf network` records HoloMap sync traffic.
17. If Lens is present, trigger server Deep Scan repeatedly and confirm RuntimeGuard throttles only when the Lens guard budget is exceeded.
