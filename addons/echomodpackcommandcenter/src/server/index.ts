import express from "express";
import cors from "cors";
import { CommandCenterStore } from "./db.js";
import { runHybridScan } from "./scanner.js";
import { exportJson, exportMarkdown } from "./exporter.js";
import { startReleaseAction, stopReleaseAction } from "./runner.js";
import { DB_PATH, ECHO_ROOT, LOCAL_DATA_DIR } from "./paths.js";
import type { AppSettings, ExportFormat, ScanMode } from "../shared/types.js";

const HOST = "127.0.0.1";
const PORT = Number(process.env.NOXHACK_COMMAND_CENTER_PORT ?? 4177);
const store = new CommandCenterStore();
const app = express();

app.use(cors({ origin: [/^http:\/\/127\.0\.0\.1:\d+$/, /^http:\/\/localhost:\d+$/] }));
app.use(express.json({ limit: "1mb" }));

app.get("/api/health", (_req, res) => {
  res.json({
    ok: true,
    service: "noxhack-command-center",
    echoRoot: ECHO_ROOT,
    localDataDir: LOCAL_DATA_DIR,
    database: DB_PATH
  });
});

app.get("/api/settings", (_req, res) => {
  res.json(store.getSettings());
});

app.put("/api/settings", (req, res) => {
  res.json(store.updateSettings(req.body as Partial<AppSettings>));
});

app.get("/api/projects", (_req, res) => {
  res.json({ projects: store.listProjects() });
});

app.get("/api/projects/:slug", (req, res) => {
  const detail = store.getProjectDetail(req.params.slug);
  if (!detail) {
    res.status(404).json({ error: "Project not found" });
    return;
  }
  res.json(detail);
});

app.post("/api/projects/:slug/scan", (req, res) => {
  const project = store.getProject(req.params.slug);
  if (!project) {
    res.status(404).json({ error: "Project not found" });
    return;
  }
  const mode = scanMode(req.body?.mode, store.getSettings().defaultScanMode);
  const scan = runHybridScan(project, store.getQaTracks(project.slug), store.getSettings(), mode);
  const report = store.createScanReport(scan);
  res.json(report);
});

app.get("/api/projects/:slug/scans", (req, res) => {
  const project = store.getProject(req.params.slug);
  if (!project) {
    res.status(404).json({ error: "Project not found" });
    return;
  }
  res.json({ reports: store.listScanReports(project.slug, 25) });
});

app.get("/api/projects/:slug/scans/:reportId", (req, res) => {
  const report = store.getScanReportById(req.params.slug, Number(req.params.reportId));
  if (!report) {
    res.status(404).json({ error: "Scan report not found" });
    return;
  }
  res.json(report);
});

app.get("/api/projects/:slug/qa/latest", (req, res) => {
  const project = store.getProject(req.params.slug);
  if (!project) {
    res.status(404).json({ error: "Project not found" });
    return;
  }
  res.json({ report: store.getLatestScanReport(project.slug) });
});

app.get("/api/projects/:slug/roadmap", (req, res) => {
  const project = store.getProject(req.params.slug);
  if (!project) {
    res.status(404).json({ error: "Project not found" });
    return;
  }
  res.json({ roadmap: store.getRoadmap(project.slug) });
});

app.get("/api/projects/:slug/prompts", (req, res) => {
  const project = store.getProject(req.params.slug);
  if (!project) {
    res.status(404).json({ error: "Project not found" });
    return;
  }
  res.json({ prompts: store.getPrompts(project.slug) });
});

app.post("/api/projects/:slug/prompts/render", (req, res) => {
  const promptId = typeof req.body?.promptId === "string" ? req.body.promptId : "";
  const prompt = store.getPrompt(req.params.slug, promptId);
  if (!prompt) {
    res.status(404).json({ error: "Prompt not found" });
    return;
  }
  res.json({ prompt });
});

app.get("/api/projects/:slug/release", (req, res) => {
  const project = store.getProject(req.params.slug);
  if (!project) {
    res.status(404).json({ error: "Project not found" });
    return;
  }
  res.json({
    actions: store.getReleaseActions(project.slug),
    modpackModsDir: store.getSettings().modpackModsDir,
    runs: store.listCommandRuns(project.slug, 25)
  });
});

app.get("/api/projects/:slug/runs", (req, res) => {
  const project = store.getProject(req.params.slug);
  if (!project) {
    res.status(404).json({ error: "Project not found" });
    return;
  }
  res.json({ runs: store.listCommandRuns(project.slug, 50) });
});

app.post("/api/projects/:slug/release/:commandId/run", (req, res) => {
  const action = store.getReleaseAction(req.params.slug, req.params.commandId);
  if (action && action.risk !== "low" && req.body?.confirmed !== true) {
    res.status(409).json({
      error: "Confirmation required",
      commandId: action.commandId,
      risk: action.risk
    });
    return;
  }
  const run = startReleaseAction(store, req.params.slug, req.params.commandId);
  res.status(run.status === "rejected" ? 400 : 202).json(run);
});

app.get("/api/runs/:runId", (req, res) => {
  const run = store.getCommandRun(req.params.runId);
  if (!run) {
    res.status(404).json({ error: "Run not found" });
    return;
  }
  res.json(run);
});

app.post("/api/runs/:runId/stop", (req, res) => {
  const run = stopReleaseAction(store, req.params.runId);
  if (!run) {
    res.status(404).json({ error: "Run not found" });
    return;
  }
  res.json(run);
});

app.get("/api/projects/:slug/export", (req, res) => {
  const detail = store.getProjectDetail(req.params.slug);
  if (!detail) {
    res.status(404).json({ error: "Project not found" });
    return;
  }
  const context = {
    detail,
    settings: store.getSettings(),
    scans: store.listScanReports(req.params.slug, 10),
    runs: store.listCommandRuns(req.params.slug, 25)
  };
  const format = (req.query.format === "markdown" ? "markdown" : "json") satisfies ExportFormat;
  if (format === "markdown") {
    res.type("text/markdown").send(exportMarkdown(context));
    return;
  }
  res.type("application/json").send(exportJson(context));
});

app.use((req, res) => {
  res.status(404).json({ error: `No route for ${req.method} ${req.path}` });
});

const server = app.listen(PORT, HOST, () => {
  console.log(`Noxhack Modpack Command Center API listening on http://${HOST}:${PORT}`);
});

process.on("SIGINT", () => shutdown());
process.on("SIGTERM", () => shutdown());

function scanMode(value: unknown, fallback: ScanMode): ScanMode {
  return value === "deep" || value === "quick" ? value : fallback;
}

function shutdown(): void {
  server.close(() => {
    store.close();
    process.exit(0);
  });
}
