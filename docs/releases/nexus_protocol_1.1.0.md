# ECHO: Nexus Protocol 1.1.0

Status: planned minor release implementation for the Nexus Field Map upgrade.

## Highlights

- Keeps the stable 5x5 Nexus Field Map telemetry shape from `1.0.0`.
- Adds deterministic field-map risk planning for local field value, corruption pressure, active storms, and reality tears.
- Adds Terminal guidance for the safest adjacent work chunk, the highest-risk recovery target, and local hazard counts.
- Updates Nexus Protocol release metadata and public artifact expectations to `1.1.0`.

## Verification

- Automated: run Nexus resource/gameplay validators and the beta GameTest server.
- Build: produce `addons/echonexusprotocol/build/libs/echonexusprotocol-1.1.0.jar`.
- Manual: verify Terminal Field Map readability in a graphical client because the main player-facing change is visual guidance.
