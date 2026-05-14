# ECHO Ecosystem Update - Audit/Hardening Pass

We ran a broad ECHO stack audit and landed real fixes instead of calling unfinished systems complete.

**Changed**
- PowerGrid now budgets EP correctly: generated power is not duplicated between consumers and batteries.
- Power consumers stay powered across network update intervals.
- Boiled Water, Filtered Water, and Crude Filter now have real item definitions/models/textures.
- Docs and validation now recognize PowerGrid, SoundCore, TutorialCore, RelicTech, and WeatherCore as active service addons.

**Still partial**
- SoundCore needs real OGG exports for many registered tracks/stingers.
- Terminal mission filter/visual polish still needs implementation.
- Local Java is missing, so Gradle build validation is blocked.

This is a real partial update and a foundation for the next release-candidate pass.
