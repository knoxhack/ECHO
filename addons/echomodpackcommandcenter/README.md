# Noxhack Modpack Command Center

Local release-operations dashboard for ECHO and future Minecraft projects. The current pass is focused on real ECHO operations: hybrid QA scans, editable local settings, release command history, guarded release actions, and JSON/Markdown exports.

## Commands

This workstation currently exposes `node.exe` but may not expose `npm.cmd`. If `npm.cmd` is not on PATH, use the bundled npm CLI at `.local\tooling\npm\package\bin\npm-cli.js`.

```powershell
cd "C:\Github\Echo\addons\echomodpackcommandcenter"
npm.cmd install
npm.cmd run dev
npm.cmd test
npm.cmd run build
npm.cmd run self-check
```

The Vite UI runs at `http://127.0.0.1:5177` and proxies API calls to the local API at `http://127.0.0.1:4177`.

## Operations

- Quick Scan inspects JSON validity, resource references, lang coverage signals, runtime log signatures, and the configured modpack jar set. Full-stack ECHO scans also check terminal page signals and handoff docs.
- Deep Scan adds optional Python validator adapters for resource, gameplay, structure, and runtime-log checks.
- Settings persist the ECHO root, optional modpack mods folder, Python executable, runtime log age, and default scan mode in the local SQLite database.
- Release Deck actions are allowlisted only. Medium and high risk actions require UI confirmation, active shell runs can be stopped best-effort, and run output/history is retained.
- Exports include the settings snapshot, latest scan, recent scans, release run history, readiness score, roadmap, prompt templates, and raw validator summaries.

## Safety Model

The backend binds to `127.0.0.1` and exposes no arbitrary shell execution endpoint. Release buttons can run only explicit allowlisted ECHO actions. Stale jar cleanup moves matching stale ECHO jars into `.local/noxhack-command-center/quarantine` under the ECHO repo root instead of deleting them.
