import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { CommandCenterStore } from "../src/server/db.ts";
import { exportJson, exportMarkdown } from "../src/server/exporter.ts";
import { startReleaseAction, stopReleaseAction } from "../src/server/runner.ts";

function tempStore() {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "noxhack-db-"));
  return {
    store: new CommandCenterStore(path.join(dir, "command-center.sqlite")),
    dir
  };
}

test("settings persist and scan reports migrate into project metrics", () => {
  const { store } = tempStore();
  try {
    const settings = store.updateSettings({
      echoRoot: "C:/EchoFixture",
      modpackModsDir: "C:/EchoFixture/mods",
      pythonExecutable: "C:/Python/python.exe",
      runtimeLogMaxAgeMinutes: 45,
      defaultScanMode: "deep"
    });
    assert.equal(settings.defaultScanMode, "deep");
    assert.equal(store.getSettings().runtimeLogMaxAgeMinutes, 45);

    const report = store.createScanReport({
      projectSlug: "echo",
      mode: "quick",
      status: "warning",
      startedAt: "2026-05-08T00:00:00.000Z",
      finishedAt: "2026-05-08T00:00:01.000Z",
      durationMs: 1000,
      source: { quickChecks: ["json"] },
      rawOutput: "validator summary",
      summary: {
        status: "Quick scan warning",
        buildHealth: 82,
        criticalIssues: 0,
        polishTasks: 1,
        inventory: { jsonFiles: 2 },
        readinessScore: 82
      },
      findings: [{
        track: "resources",
        title: "Missing item lang key",
        severity: "medium",
        status: "Failed",
        detail: "item.echo.fixture",
        path: "src/main/resources/assets/echo/lang/en_us.json",
        line: 3,
        code: "MISSING_LANG_KEY",
        source: "quick",
        metadata: { fixture: true }
      }]
    });

    assert.equal(store.getLatestScanReport("echo")?.id, report.id);
    assert.equal(store.getScanReportById("echo", report.id)?.findings[0].code, "MISSING_LANG_KEY");
    assert.equal(store.getProject("echo")?.buildHealth, 82);
    assert.equal(store.listScanReports("echo", 10).length, 1);
  } finally {
    store.close();
  }
});

test("exports include settings, latest scan, raw output, and run history", () => {
  const { store } = tempStore();
  try {
    store.createScanReport({
      projectSlug: "echo",
      mode: "quick",
      status: "passed",
      startedAt: "2026-05-08T00:00:00.000Z",
      finishedAt: "2026-05-08T00:00:01.000Z",
      durationMs: 1000,
      source: { quickChecks: ["json"] },
      rawOutput: "raw validator lines",
      summary: {
        status: "Quick scan passed",
        buildHealth: 99,
        criticalIssues: 0,
        polishTasks: 0,
        inventory: { jsonFiles: 1 },
        readinessScore: 99
      },
      findings: []
    });
    startReleaseAction(store, "echo", "not-a-real-command");

    const context = {
      detail: store.getProjectDetail("echo"),
      settings: store.getSettings(),
      scans: store.listScanReports("echo"),
      runs: store.listCommandRuns("echo")
    };
    const parsed = JSON.parse(exportJson(context));
    const markdown = exportMarkdown(context);

    assert.equal(parsed.settings.defaultScanMode, "quick");
    assert.equal(parsed.recentScans.length, 1);
    assert.equal(parsed.releaseRuns.length, 1);
    assert.match(markdown, /Settings Snapshot/);
    assert.match(markdown, /Release Run History/);
    assert.match(markdown, /raw validator lines/);
  } finally {
    store.close();
  }
});

test("unknown release actions are rejected and stop is best-effort only", () => {
  const { store } = tempStore();
  try {
    const rejected = startReleaseAction(store, "echo", "not-a-real-command");
    assert.equal(rejected.status, "rejected");
    assert.equal(rejected.metadata?.reason, "unknown-action");

    const stopped = stopReleaseAction(store, rejected.id);
    assert.equal(stopped?.metadata?.stopResult, "not-running");
    assert.equal(stopped?.status, "rejected");
  } finally {
    store.close();
  }
});
