# ECHO Release Terminal

Local release-operations dashboard for the ECHO mod stack.

## Commands

Use `npm.cmd` on Windows because this workstation blocks `npm.ps1`.

```powershell
cd tools\echo-release-terminal
npm.cmd install
npm.cmd run dev
npm.cmd run build
npm.cmd run preview
```

The dev UI runs through Vite on `http://127.0.0.1:5177` and proxies API calls to the local Node server on `http://127.0.0.1:4177`.

## Safety Model

The backend binds to `127.0.0.1` and only runs explicit allowlisted commands. It does not accept arbitrary shell input. Runtime QA state and generated release drafts live in the repo-level `.local/echo-release-terminal` folder.

