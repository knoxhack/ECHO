# ECHO Mod Stack Wiki

Welcome to the official documentation hub for the **ECHO mod stack ecosystem**.

ECHO is a modular NeoForge experience built around a shared runtime (`echocore`) and a shared UI layer (`echoterminal`), with campaign chapters delivered as interoperable addons.

## Quick Navigation

- [[Getting Started]]
- [[Architecture]]
- [[Modules and Versions]]
- [[Progression Guide]]
- [[Addon Development]]
- [[Build and Release]]
- [[Troubleshooting]]
- [[Lore and World Canon]]

## What is the “Ecosystem”?

The ECHO ecosystem includes:

1. **Core runtime services** (`echocore`)
2. **Terminal UX shell and shared surfaces** (`echoterminal`)
3. **Main campaign module** (`echoashfallprotocol`)
4. **Expansion chapters/addons** (Orbital, Stationfall, Nexus, Industrial, Blackbox, and more)
5. **Tooling and release workflow** used to validate and ship the full stack

## Supported Baseline

- Minecraft `26.1.2`
- NeoForge `26.1.2.29-beta`+
- Java `25+`

## Stack Principles

- **Shared state through contracts:** addons integrate through public `echocore` APIs.
- **Chapter ownership:** each addon owns its own progression, rewards, and persistence.
- **Terminal as shell:** `echoterminal` routes and presents data; it does not own chapter logic.
- **Composable world narrative:** each chapter extends the route without hard dependencies on private internals of other modules.
