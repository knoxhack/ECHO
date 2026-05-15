# ECHO ThemeForge

ThemeForge is a development-time helper for ECHO ThemeCore 1.2.0. It reads ThemeCore theme JSON, writes prompt packs for approved image generation, validates generated PNGs, and copies approved assets into the ThemeCore resources folder.

The Minecraft mod never calls image-generation APIs at runtime. Keep API keys and online generation tools outside the mod.

## Commands

```powershell
python tools/echo-themeforge/themeforge.py prompts
python tools/echo-themeforge/themeforge.py validate
python tools/echo-themeforge/themeforge.py validate --theme cyberglass --strict
python tools/echo-themeforge/themeforge.py report
python tools/echo-themeforge/themeforge.py report --theme cyberglass
python tools/echo-themeforge/themeforge.py apply
```

Use `apply --force` only when you intentionally want to replace existing resource PNGs.
Use `validate --theme cyberglass --strict` for CyberGlass release gates; global validation may still report future Nexus generated assets as backlog.

## Output

- Prompts: `tools/echo-themeforge/generated/prompts/`
- Generated assets: `tools/echo-themeforge/generated/assets/<theme>/`
- Previews: `tools/echo-themeforge/generated/previews/`
- Reports: `tools/echo-themeforge/generated/reports/`

Prompts should target clean sci-fi game UI assets, transparent backgrounds for overlays/icons, lowercase PNG filenames, and no legacy CRT line-overlay styling.
