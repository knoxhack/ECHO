# ECHO: WorldCore 0.2.0 Release Hardening

WorldCore 0.2.0 is a framework release. It keeps WorldCore free of craftable
items, blocks, entities, menus, recipes, loot, and models, and focuses on the
shared world API loop used by Ashfall, HoloMap, DataCore, MissionCore, Terminal,
RenderCore, AudioCore, Convoy, and Orbital systems.

## Certification Commands

Run from `C:\Github\Echo`. The local workspace currently provides a Java 21
launcher while Gradle resolves the Java 25 toolchain required by NeoForge 26.1.2.

```powershell
$env:JAVA_HOME='C:\Users\knox\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2'
$env:Path="$env:JAVA_HOME\bin;$env:Path"

.\gradlew.bat :echoworldcore:build --warning-mode all --no-daemon --no-configuration-cache --console=plain
.\gradlew.bat :echoworldcore:runGameTestServer --warning-mode all --no-daemon --no-configuration-cache --console=plain
.\gradlew.bat :echodatacore:runGameTestServer :echomissioncore:runGameTestServer --warning-mode all --no-daemon --no-configuration-cache --console=plain
.\gradlew.bat :echoholomap:build :echorendercore:build --warning-mode all --no-daemon --no-configuration-cache --console=plain
.\gradlew.bat runGameTestServer --warning-mode all --no-daemon --no-configuration-cache --no-parallel --max-workers=1 --console=plain
.\gradlew.bat buildEchoWorkspace -PechoAddonSet=all --warning-mode all --no-daemon --no-configuration-cache --no-parallel --max-workers=1 --console=plain
New-Item -ItemType Directory -Force .\build\tmp\echo-release-mods | Out-Null
.\gradlew.bat copyEchoJarsToModpack checkEchoModJarSet verifyEchoRelease -PechoAddonSet=all -PechoModpackModsDir=.\build\tmp\echo-release-mods --warning-mode all --no-daemon --no-configuration-cache --no-parallel --max-workers=1 --console=plain
```

If release verification is run without `-PechoModpackModsDir` and fails because
`C:\Users\Ivan\curseforge\minecraft\Instances\Axes of Tomorrow\mods` is missing,
that is an external environment blocker. Use the workspace temp directory above
for local certification, or pass a real modpack `mods` folder.

## Expected Results

- `:echoworldcore:build` compiles the framework and emits `echoworldcore-0.2.0.jar`.
- `:echoworldcore:runGameTestServer` passes WorldCore registry, JSON parser,
  SavedData, runtime bus, no-op fallback, and v0.2 release guard tests.
- DataCore and MissionCore GameTests prove WorldCore runtime bus consumers update
  telemetry keys and objective progress.
- HoloMap and RenderCore builds prove marker styling and profile resources remain
  compile-safe without common client imports.
- Root GameTests and `buildEchoWorkspace` prove WorldCore inside the full addon
  stack. Any unrelated blocker should be fixed only when it is a narrow release
  gate and does not rewrite unrelated gameplay.

## Manual Smoke Path

1. Start with `echocore`, `echoworldcore`, `echoterminal`, `echoholomap`,
   `echodatacore`, and `echomissioncore`.
2. Run `/echoworld validate`.
3. Run `/echoworld list region` and confirm
   `echoashfallprotocol:crash_zone_wasteland`.
4. Reveal or enter a region and confirm Terminal, DataCore, and MissionCore
   state updates.
5. Open HoloMap and inspect crash, hazard, route, orbital, outpost,
   Nexus/anomaly, and structure marker styling.
6. Reload the world and confirm markers and discoveries persist.
