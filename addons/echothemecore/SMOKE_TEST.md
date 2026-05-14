# ECHO ThemeCore 0.2.0 Smoke Test

1. Run `.\gradlew :echothemecore:build`.
2. Attempt `.\gradlew buildEchoWorkspace -PechoAddonSet=all`.
3. Launch a dev client with ThemeCore enabled.
4. Confirm the log reports `ECHO ThemeCore 0.2.0 online`.
5. Run `/echo_theme list`.
6. Run `/echo_theme current`.
7. Confirm `echothemecore:cyberglass` is default and fallback.
8. Run `/echo_theme set echothemecore:nexus`.
9. Run `/echo_theme reset` and confirm CyberGlass is active again.
10. Run `/reload`, then `/echo_theme list` again.
11. Run `/echo_theme preview echothemecore:cyberglass` and `/echo_theme preview echothemecore:nexus`.
12. Run `/echo_theme visual current`.
13. Run `/echo_theme visual test terminal`; if RenderCore is absent, confirm the command reports unavailable.
14. Run `/echo_theme vanilla current`.
15. Open title, pause, options, inventory, chest/container, and creative inventory screens.
16. Confirm vanilla UI accents do not cover slot interiors or move any slot/widget.
17. Confirm dedicated server startup does not load `com.knoxhack.echothemecore.client`.
18. Run `python tools/echo-themeforge/themeforge.py prompts`.
19. Run `python tools/echo-themeforge/themeforge.py validate`.
20. Confirm `tools/echo-themeforge/generated/reports/missing_assets.md` is readable.
21. Confirm ThemeCore theme data and assets contain no forbidden legacy CRT line-overlay terms or files.
