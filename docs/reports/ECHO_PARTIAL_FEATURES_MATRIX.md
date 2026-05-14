# ECHO Partial Features Matrix

| Module | Feature | Status | Evidence | Why incomplete | Fix plan | Priority | Test needed |
|---|---|---|---|---|---|---|---|
| echopowergrid | EP network distribution | IMPLEMENTED | addons/echopowergrid/src/main/java/com/knoxhack/echopowergrid/grid/PowerNetworkManager.java | fixed duplicated generated EP | run GameTests when Java exists | P1 | generation_not_duplicated_under_deficit |
| echoashfallprotocol | Water/filter item assets | IMPLEMENTED | src/main/resources/assets/echoashfallprotocol/items/boiled_water_bottle.json | missing resources fixed | client icon check | P1 | resource validation |
| echosoundcore | Complete soundtrack/stingers | PARTIAL | addons/echosoundcore/src/main/resources/assets/echosoundcore/sounds.json | 123 missing real OGG targets | produce real OGG or explicit fallback map | P1 | sound missing-file validator |
| echoterminal | Mission filter/visual chips | PARTIAL | tools/validate_gameplay_data.py | MissionFilter/drawFilterChips/EXPAND/COMPACT tokens missing | implement real mission filters and view chips | P2 | Terminal UI tests |
| echoplayercore | TPA/warps | PLANNED | addons/echoplayercore/README.md | roadmap only | implement persistence/cooldowns/permissions | P2 | command flow tests |
| echotutorialcore | Provider integrations | PARTIAL | addons/echotutorialcore/README.md | scaffold/future wiring | wire Terminal/Lens/HoloMap providers | P2 | seen-state tests |
| echoweathercore | Weather loop integrations | PARTIAL | addons/echoweathercore/README.md | needs runtime validation | validate events/shelter/protection/alerts | P2 | exposed vs sheltered tests |
| echorelictech | Advanced relic integrations | PARTIAL | addons/echorelictech/README.md | known limitations listed | finish custom tabs/decoys/faction gates | P3 | relic flow tests |
