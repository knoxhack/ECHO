# Ashfall 1.0.0 Release Smoke Test

This note records the Ashfall-specific release gates for the `1.0.0` baseline finish pass. Automation proves the module and stack load, but the release cannot be called fully player-validated until the manual fresh-world pass below is run in Minecraft.

## Automated Gates

Last checked: 2026-05-10.

```powershell
python tools\validate_resources.py --addon-set all
python tools\validate_gameplay_data.py
$env:JAVA_HOME='C:\Users\knox\.jdks\temurin-25'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat --no-configuration-cache -PechoAddonSet=all build validateReleaseArtifacts
.\gradlew.bat --no-configuration-cache -PechoAddonSet=all :runGameTestServer
.\gradlew.bat --no-configuration-cache -PechoAddonSet=all -PechoModpackModsDir="C:\CurseForge\Instances\Ashfall Protocol\mods" verifyEchoRelease
```

Expected result: all commands finish with `BUILD SUCCESSFUL` or the validator pass message. The root Ashfall GameTest run should report all 155 required tests passing.

## Manual Fresh-World Pass

Use the active CurseForge instance at `C:\CurseForge\Instances\Ashfall Protocol` with the full Echo stack installed.

1. Create a fresh world with default Ashfall worldgen.
2. Confirm the player spawns inside the compact drop pod, can move immediately, and can exit through the ramp.
3. Open visible pod lockers and confirm first-night supplies: clean water, emergency dirty water path, filters, rations, meds, torches, basic weapon support, campfire/chest support, scanner, and salvage.
4. Craft or obtain the first basic weapon/tool without using creative mode or vanilla forest luck.
5. Build or place the first progression blocks in order: Hand Recycler, Micro Generator, Filter Workbench, Water Purifier, and Battery Bank.
6. Run the main mechanic: recycle or purify an input, produce the expected output, and confirm no silent item loss or duplication.
7. Claim one mission reward once, then confirm it cannot be duplicated by repeating the same claim.
8. Save and reload the world; confirm pod location, mission state, pending rewards, and machine state persist.
9. Open ECHO Terminal and confirm What Now, Mission Graph, Reward Inbox, Vitals, Route Records, Faction Atlas, and Field Archive agree with the world state.
10. Scan a POI and confirm the scanner names a real site, hazard profile, prep kit, reward track, distance, direction, and field-log status.
11. Check `latest.log` after the session for severe errors, missing Ashfall assets, uncontrolled spawn loops, repeated long catch-up spam, or failed optional integrations.

## Pass Criteria

- Player can complete the first 10-minute survival loop without commands.
- At least one main machine interaction works and persists after reload.
- Rewards work once and safely.
- Terminal status and next steps are understandable from in-world state.
- Optional sibling integrations fail closed when absent and surface cleanly when present.
