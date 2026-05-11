# Changelog

## 0.1.0 - Release Candidate

- Added survival discovery advancements for the Logistics progression path from Supply Tag through dispatch-ready infrastructure.
- Added persistence GameTests for Logistics block state and courier payload state.
- Hardened Logistics container removal so stocked crates, docks, lockers, depots, relays, terminals, requesters, and restock stations drop stored items once on block break instead of silently voiding or duplicating contents.
- Added GameTest coverage for breaking a stocked Supply Crate and verifying exact one-time item drops.
- Aligned the bundled salvage-water depot offer with the Ashfall Crashbreak Salvage faction id used by current Core faction data.
- Updated smoke-test documentation with the Java 25 precondition, playable survival loop, break-drop safety check, verification commands, and 25/25 GameTest expectation.
- Kept optional ECHO Terminal, ECHO Core, Industrial Nexus, and sibling integrations guarded while validating the full included workspace build.
