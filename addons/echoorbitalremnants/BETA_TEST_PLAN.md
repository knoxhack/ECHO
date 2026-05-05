# ECHO-7 Release Test Plan

Use this checklist before publishing a release jar.

## Automated Checks

1. Run `.\gradlew.bat :echoorbitalremnants:build` from the ECHO workspace root.
2. Run `.\gradlew.bat :echoorbitalremnants:runGameTestServer` from the ECHO workspace root.
3. Confirm the release jar is `addons/echoorbitalremnants/build/libs/echoorbitalremnants-1.5.0.jar`.
4. Confirm the `terminal_mission_cache_state` and `terminal_mission_integration` GameTests pass when the shared ECHO Terminal is present in the local runtime.
5. Confirm Core/Terminal/Ashfall stack versions are aligned: ECHO Core `1.1.0`, ECHO Terminal `1.1.0`, Ashfall Protocol `1.3.0`, and Orbital Remnants `1.5.0`.

## Clean Survival Smoke Test

1. Create a new survival world with only ECHO-7 enabled.
2. Craft the ECHO-7 Terminal.
3. Sneak-use the terminal on Earth and confirm starter recovery sites generate.
4. Loot the launch pad, crashed satellite, and comms array.
5. Craft or recover the Launch Platform, Rocket Assembly Frame, Fuel Refinery, Oxygen Compressor, pressure suit, Oxygen Tank, and rocket parts.
6. Open the Rocket Assembly Frame and confirm the Emergency Rocket appears only when the checklist is complete.
7. Launch to Low Earth Orbit and confirm an Earth return vector is saved.
8. Confirm the terminal names the next missing hook whenever progression is blocked.

## Shared Terminal Smoke Test

1. Launch with ECHO: Terminal and ECHO: Orbital Remnants installed.
2. Open `echoterminal:echo_terminal` and confirm Orbital Command, Survey, and ECHO tabs appear under Addons.
3. Press SCAN from Orbital Command and Orbital Survey; both should execute the same server-side ECHO-7 scan path.
4. Complete Earth calibration and confirm the Earth Calibration mission becomes claimable in Orbital ECHO.
5. Claim the support cache once, verify utility items are delivered or stored through the Terminal reward service, and confirm a second claim does not duplicate rewards.
6. Repeat before and after an Ashfall Nexus choice in the full stack to confirm the lock reason and unlock handoff stay clear.
7. Confirm What Now shows Orbital blockers, Route Records lists Earth Recontact / Launch Chain / Route Worlds / ECHO-0 Quarantine, Vitals includes Orbital hazard telemetry while in route dimensions, Faction Atlas shows Orbital Remnant / Void Salvagers / Nexus Choir standings, and Reward Inbox agrees with standalone cache state.

## Creative Route Smoke Test

1. Open every machine menu and confirm progress, charge, blocked-output, and bad-input text still update.
2. Travel through Low Earth Orbit, Lunar Scar Zone, Mars Ash Basin, Europa Cryo Ocean, and Nexus Anomaly Belt.
3. Confirm each arrival site has a readable landmark and at least one useful cache.
4. Repair three Station Relay Nodes, three Helium Extractor Nodes, three Mars Pressure Consoles, and three Europa Thermal Arrays.
5. Spawn or encounter Corrupted Docking AI, Lunar Nexus Husk, Abandoned Captain, Europa Cryo Warden, and ECHO-0.
6. Defeat ECHO-0 and confirm the terminal reports quarantine resolution and grants the final-protocol reward once.
7. Confirm Nexus stabilization is locked before ECHO-0, then complete three unique survey logs for Orbit, Moon, Mars, Europa, and Nexus.
8. Confirm Nexus 0/3 through 2/3 guidance names Anchor/Growth sites and Nexus Stabilizer Shard recovery, and that stabilization grants the survey reward once.
9. Complete one faction contract, press SCAN to seal the final survey network, and repeat SCAN to confirm only completion guidance refreshes.

## Faction Contract Smoke Test

1. Use each pledge item: Orbital Remnant Badge, Void Salvager Marker, and Nexus Choir Sigil.
2. Confirm the ECHO tab shows the current faction contract.
3. Complete an Orbital Remnant contract by scanning a Low Orbit relay or spending Orbit Survey Data.
4. Complete a Void Salvager contract by scanning salvage or turning in Orbital Alloy plus Vacuum Circuit.
5. Complete a Nexus Choir contract after ECHO-0 by scanning Nexus growth/anchors or spending a Nexus Stabilizer Shard.
6. Confirm each completion grants rewards once and the cooldown prevents immediate double-grants.

## Return Vector Checks

1. Try using route vessels from the wrong state and confirm the failure message is clear.
2. Try returning without a saved vector and confirm the failure message is clear.
3. Save a route vector, return, and confirm the player lands safely.

## Release Sanity

1. Check `README.md`, `guide.md`, `CHANGELOG.md`, and `KNOWN_ISSUES.md` for the shipped version.
2. Upload only the latest `1.5.0` jar.
3. Include the known issues and feedback link in the release notes.
4. Record the manual smoke-test result in the release verification notes.
