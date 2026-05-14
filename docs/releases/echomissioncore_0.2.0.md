# ECHO: MissionCore 0.2.0

MissionCore 0.2.0 is the addon migration release. It keeps MissionCore as framework
infrastructure and moves existing addon-owned Terminal mission provider content into
the MissionCore registration path without adding blocks, items, entities, or textures.

## Migration Coverage

- Reclamation, Industrial, Convoy, Orbital, Nexus, Blackbox, and Stationfall now have
  common-side `*MissionCoreIntegration` adapters.
- Adapter registration preserves existing `TerminalMissionDefinition.id()` values as
  MissionCore mission ids.
- Legacy mission provider registration is skipped when `echomissioncore` is loaded,
  while non-mission Terminal dashboards, archives, actions, and screens continue to
  register normally.
- Java-only mission action provider/handler hooks preserve custom scan, decode, route,
  and path-choice actions.
- `/echomission validate` reports mission counts by source and warns if known legacy
  mission providers remain registered alongside migrated MissionCore content.

## Compatibility Notes

- MissionCore storage remains canonical for new mission state.
- Existing addon progress stores remain readable and are used by adapters for import,
  status rules, completion checks, and action delegation.
- JSON mission files cannot define executable custom handlers. Use Java registration
  for complex actions.
- Terminal is optional. Mission progress, rewards, persistence, and debug commands do
  not require Terminal at runtime.
- RenderCore, NetCore, DataCore, and WorldCore are not required by MissionCore 0.2.0.

## Test Commands

```powershell
.\gradlew.bat :echocore:compileJava --no-configuration-cache
.\gradlew.bat :echomissioncore:compileJava --no-configuration-cache
.\gradlew.bat :echoagriculturereclamation:compileJava :echoindustrialnexus:compileJava :echoconvoyprotocol:compileJava :echoorbitalremnants:compileJava :echonexusprotocol:compileJava :echoblackboxprotocol:compileJava :echostationfall:compileJava --no-configuration-cache
.\gradlew.bat :echomissioncore:runGameTestServer --no-configuration-cache
.\gradlew.bat compileJava buildEchoWorkspace --no-configuration-cache
```

## Release Manifest Expectations

- `addons/echomissioncore/gradle.properties` reports `mod_version=0.2.0`.
- The release jar remains a framework module requiring only `echocore`.
- The release includes MissionCore lang, sample data, README, and addon registration
  documentation.
- No MissionCore gameplay assets are expected for this release.
