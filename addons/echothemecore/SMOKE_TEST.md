# ECHO Stack 1.2.0 CyberGlass Default Smoke Test

1. Run `.\gradlew :echothemecore:build`.
2. Attempt `.\gradlew buildEchoWorkspace -PechoAddonSet=all`.
3. Launch a dev client with ThemeCore enabled.
4. Confirm the log reports `ECHO ThemeCore 1.2.0 online`.
5. Run `/echo_theme list`.
6. Run `/echo_theme current`.
7. Confirm `echothemecore:cyberglass` is default and fallback.
8. Open Terminal with no saved Terminal theme and confirm CyberGlass is selected.
9. Set a valid non-CyberGlass Terminal theme, restart the client, and confirm the saved selection is preserved.
10. Run `/echo_theme set echothemecore:nexus`.
11. Run `/echo_theme reset` and confirm CyberGlass is active again.
12. Run `/reload`, then `/echo_theme list` again.
13. Run `/echo_theme preview echothemecore:cyberglass` and `/echo_theme preview echothemecore:nexus`.
14. Run `/echo_theme visual current`.
15. Run `/echo_theme visual test terminal`; if RenderCore is absent, confirm the command reports unavailable.
16. Run `/echo_theme vanilla current`.
17. Open title, pause, options, inventory, chest/container, creative inventory, tooltips, toasts, hotbar, and boss bar.
18. Confirm CyberGlass vanilla UI accents do not cover slot interiors, move slots/widgets, or reduce text contrast.
19. Confirm HoloMap, Lens, RenderCore, SignalOS, SoundCore, and Blockworks use CyberGlass/ThemeCore when present and keep their existing fallbacks when ThemeCore is absent.
20. Confirm dedicated server startup does not load `com.knoxhack.echothemecore.client`.
21. Run `python tools/echo-themeforge/themeforge.py prompts`.
22. Run `python tools/echo-themeforge/themeforge.py validate --theme cyberglass --strict`.
23. Run `python tools/echo-themeforge/themeforge.py report --theme cyberglass`.
24. Confirm `tools/echo-themeforge/generated/reports/missing_assets.md` is readable.
25. Confirm ThemeCore theme data and assets contain no forbidden legacy CRT line-overlay terms or files.
