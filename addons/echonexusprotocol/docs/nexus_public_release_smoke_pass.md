# ECHO: Nexus Protocol Public Release Smoke Pass

Status: automated release-risk pass complete; manual interactive client run still required on a graphical modpack instance

Automated coverage added in this polish pass:

- Nexus beta addon set wiring: Core, Terminal, Orbital, Nexus, plus root Ashfall.
- Nexus Field Map telemetry for all five field states, corruption pressure, storms, and reality tears.
- Collapsed-chunk recovery tools: Stabilized Purity Charge and Field Anchor.
- Ending-specific world feedback for restore, control, destroy, and merge.
- Config defaults for balance preset, field-map radius, recovery strength, and recovery quarantine time.

Manual pass checklist:

- New world start.
- Nexus unlock through Stationfall handoff or Nexus dev unlock.
- Terminal mission tab readability.
- Nexus Field tab readability.
- Nexus Field Map tab readability.
- Nexus machine UI: slots, progress, charge, contamination, status.
- Nexus dimension entry and saved return point.
- Blackbox Monolith activation and anomaly storm feedback.
- Corruption Warden fight feel and drops.
- Nexus Guardian phase readability and defeat side effects.
- One final ending choice and permanence message.
- Quit/reload persistence for Nexus player data, world field data, ending state, and return position.

Manual notes:

- 2026-05-06 automated hardening pass:
  - Fixed Core Access Key missing-dimension fallback so progression no longer advances if `echonexusprotocol:nexus` is unavailable.
  - Added safe Nexus entry pad creation at the fixed Core route arrival point.
  - Deduplicated multiplayer field ticking and pruned stale storm telemetry.
  - Made ending world feedback one-shot and protected final path permanence through Terminal and command routes.
  - Reduced Protocol Seal sound spam and made sounds/mission credit happen only when a seal action succeeds.
  - Removed natural Warden biome spawning and blocked Warden/Guardian natural spawn placement.
  - Added recipe-aware machine input filtering, clearer machine status text, Archive Seeker recovery feedback, Data Wraith pulse behavior, Warden summon cap, Guardian phase effects, and Merge-path reality tear traversal.
  - Confirmed beta jar folder contains only Ashfall, Core, Terminal, Orbital, and Nexus jars.
- 2026-05-06 release-polish fix pass:
  - Fixed Protocol Seal relay transfer so charge is consumed only after a target tank accepts it.
  - Added Nexus regular mob loot tables and POI chest loot tables.
  - Added Nexus biome dressing features for White Signal Trees, crystal clusters, Static Fluid pools, debris, ferrite pockets, lab pipes, and reality tear hotspots.
  - Added machine hover/status help for valid inputs, missing charge, output conflicts, contamination risk, and field risk.
  - Added role-specific Nexus mob presentation cues with particles, float/pulse scaling, and phase tinting.
  - Kept the Field Map display radius intentionally fixed at 5x5 for release stability.
  - Fixed the beta release-lane blockers in Orbital/Ashfall verification without adding future addon jars to the beta set.
- Progression clarity: still needs real client playthrough.
- UI readability: machine status text improved; still needs real client visual check.
- Loot feel: not yet manually verified.
- Boss feel: server behavior improved; still needs real combat pass.
- Field recovery feel: automated coverage exists; still needs real client pacing pass.
- Reload persistence: not yet manually verified in a client quit/reload session.

Verification notes:

- `:echonexusprotocol:build -PechoAddonSet=beta --warning-mode all --no-configuration-cache` passed.
- `python tools\validate_resources.py --addon-set beta` passed.
- `python tools\validate_gameplay_data.py` passed.
- `python tools\check_echo_runtime_logs.py --max-age-minutes 180` passed.
- `copyEchoJarsToModpack -PechoAddonSet=beta` passed into `build\tmp\echo-release-mods`.
- `checkEchoModJarSet -PechoAddonSet=beta` passed with exactly five beta jars.
- `:echonexusprotocol:runGameTestServer -PechoAddonSet=beta --warning-mode all --no-configuration-cache` passed.
- `verifyEchoRelease -PechoAddonSet=beta -PechoModpackModsDir="C:\Users\Ivan\Documents\GitHub\Echo\build\tmp\echo-release-mods" --warning-mode all --max-workers=1 --no-configuration-cache` passed.
- `checkEchoModJarSet -PechoAddonSet=beta` passed with exactly five beta jars: Ashfall, Core, Terminal, Orbital, and Nexus.
