# ECHO Ecosystem Update Changelog Draft

## Headline
ECHO ecosystem audit and hardening pass: PowerGrid EP distribution fixed, Ashfall water/filter item assets restored, active service addon docs/validation updated, and full report matrices generated.

## New/Active Addons Reflected
- `echopowergrid` 0.1.0
- `echosoundcore` 0.1.0
- `echotutorialcore` 0.1.0
- `echorelictech` 0.2.0-beta
- `echoweathercore` 0.1.0

## Major Fixes
- Fixed PowerGrid generated EP duplication between batteries and consumers.
- Added interval-aware consumer power coverage so consumers do not flicker between network updates.
- Added missing `boiled_water_bottle`, `filtered_water_bottle`, and `crude_filter` item definitions/models/textures.
- Updated resource validation module coverage for the active service addons listed in `settings.gradle`.

## Gameplay Improvements
- PowerGrid now prioritizes active demand before charging storage under deficit.
- First-hour water progression has concrete inventory assets for boiled/filtered water and crude filters.

## Tests/Validation
- Added PowerGrid GameTest coverage for generation not duplicating under deficit.
- Ran resource validation, gameplay data validation, RenderCore mob validation, existing-only mob texture cropping, and a Gradle compile attempt.

## Known Issues
- Gradle compile/build/GameTests are blocked because Java is not available in the shell.
- SoundCore still references many missing OGG files.
- Terminal mission filter/visual mode polish remains partial per gameplay validator.

## Upgrade Notes
Treat this as a real partial hardening pass, not a release candidate, until Java-backed Gradle validation passes.
