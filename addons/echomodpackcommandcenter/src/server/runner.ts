import { randomUUID } from "node:crypto";
import { spawn, type ChildProcessWithoutNullStreams } from "node:child_process";
import type { AppSettings, CommandRun, ReleaseAction } from "../shared/types.js";
import { ECHO_ROOT } from "./paths.js";
import { CommandCenterStore } from "./db.js";
import { quarantineStaleJars } from "./quarantine.js";
import { generateReleaseNotes } from "./releaseNotes.js";

const OUTPUT_LIMIT = 200_000;
const activeChildren = new Map<string, ChildProcessWithoutNullStreams>();

export function startReleaseAction(store: CommandCenterStore, projectSlug: string, commandId: string): CommandRun {
  const project = store.getProject(projectSlug);
  const action = store.getReleaseAction(projectSlug, commandId);
  const settings = store.getSettings();
  if (!project || !action) {
    return store.createCommandRun({
      id: randomUUID(),
      projectSlug,
      commandId,
      status: "rejected",
      risk: "low",
      command: [],
      startedAt: new Date().toISOString(),
      finishedAt: new Date().toISOString(),
      exitCode: 1,
      durationMs: 0,
      metadata: { reason: "unknown-action" },
      output: "Rejected: unknown project or release action."
    });
  }

  if (action.mode === "quarantine") {
    return completeLocalRun(store, projectSlug, action, () => quarantineStaleJars(project, settings.modpackModsDir));
  }

  if (action.mode === "notes") {
    return completeLocalRun(store, projectSlug, action, () => {
      const detail = store.getProjectDetail(projectSlug);
      if (!detail) {
        throw new Error(`Unknown project: ${projectSlug}`);
      }
      const notes = generateReleaseNotes(detail);
      return `Generated release notes at ${notes.filePath}\n\n${notes.markdown}`;
    });
  }

  return startShellRun(store, projectSlug, action, settings);
}

export function stopReleaseAction(store: CommandCenterStore, runId: string): CommandRun | null {
  const run = store.getCommandRun(runId);
  if (!run) {
    return null;
  }
  const child = activeChildren.get(runId);
  if (!child || run.status !== "running") {
    return store.updateCommandRun(runId, {
      metadata: { ...(run.metadata ?? {}), stopRequestedAt: new Date().toISOString(), stopResult: "not-running" }
    });
  }
  child.kill();
  activeChildren.delete(runId);
  return store.updateCommandRun(runId, {
    status: "stopped",
    finishedAt: new Date().toISOString(),
    exitCode: 130,
    durationMs: durationFrom(run.startedAt),
    metadata: { ...(run.metadata ?? {}), stopRequestedAt: new Date().toISOString(), stopResult: "signal-sent" },
    output: `${run.output}\n\nStopped by Command Center.`
  });
}

function completeLocalRun(
  store: CommandCenterStore,
  projectSlug: string,
  action: ReleaseAction,
  task: () => string
): CommandRun {
  const startedAt = new Date().toISOString();
  const run = store.createCommandRun({
    id: randomUUID(),
    projectSlug,
    commandId: action.commandId,
    status: "running",
    risk: action.risk,
    command: [action.mode],
    startedAt,
    metadata: { mode: action.mode },
    output: ""
  });

  try {
    const output = task();
    return store.updateCommandRun(run.id, {
      status: "succeeded",
      finishedAt: new Date().toISOString(),
      exitCode: 0,
      durationMs: durationFrom(startedAt),
      output
    }) as CommandRun;
  } catch (error) {
    return store.updateCommandRun(run.id, {
      status: "failed",
      finishedAt: new Date().toISOString(),
      exitCode: 1,
      durationMs: durationFrom(startedAt),
      output: error instanceof Error ? error.message : String(error)
    }) as CommandRun;
  }
}

function startShellRun(
  store: CommandCenterStore,
  projectSlug: string,
  action: ReleaseAction,
  settings: AppSettings
): CommandRun {
  const args = argsWithSettings(action, settings);
  const command = [action.executable, ...args];
  const startedAt = new Date().toISOString();
  const run = store.createCommandRun({
    id: randomUUID(),
    projectSlug,
    commandId: action.commandId,
    status: "running",
    risk: action.risk,
    command,
    startedAt,
    metadata: { cwd: ECHO_ROOT, mode: action.mode },
    output: `Running in ${ECHO_ROOT}\n> ${command.join(" ")}\n\n`
  });

  const child = spawn(action.executable, args, {
    cwd: ECHO_ROOT,
    shell: process.platform === "win32",
    windowsHide: true
  });
  activeChildren.set(run.id, child);
  store.updateCommandRun(run.id, { pid: child.pid, metadata: { ...(run.metadata ?? {}), pid: child.pid } });

  let output = run.output;
  const append = (chunk: Buffer): void => {
    output = `${output}${chunk.toString()}`;
    if (output.length > OUTPUT_LIMIT) {
      output = output.slice(output.length - OUTPUT_LIMIT);
    }
    store.updateCommandRun(run.id, { output });
  };

  child.stdout.on("data", append);
  child.stderr.on("data", append);
  child.on("error", (error) => {
    activeChildren.delete(run.id);
    const current = store.getCommandRun(run.id);
    if (current?.status === "stopped") {
      return;
    }
    store.updateCommandRun(run.id, {
      status: "failed",
      finishedAt: new Date().toISOString(),
      exitCode: 1,
      durationMs: durationFrom(startedAt),
      output: `${output}\n${error.message}`
    });
  });
  child.on("close", (code) => {
    activeChildren.delete(run.id);
    const current = store.getCommandRun(run.id);
    if (current?.status === "stopped") {
      return;
    }
    store.updateCommandRun(run.id, {
      status: code === 0 ? "succeeded" : "failed",
      finishedAt: new Date().toISOString(),
      exitCode: code ?? 1,
      durationMs: durationFrom(startedAt),
      output
    });
  });

  return store.getCommandRun(run.id) ?? run;
}

function argsWithSettings(action: ReleaseAction, settings: AppSettings): string[] {
  const args = [...action.args];
  if (
    settings.modpackModsDir &&
    ["verify-release", "check-jar-set", "copy-jars"].includes(action.commandId) &&
    !args.some((arg) => arg.startsWith("-PechoModpackModsDir="))
  ) {
    args.push(`-PechoModpackModsDir=${settings.modpackModsDir}`);
  }
  return args;
}

function durationFrom(startedAt: string): number {
  return Math.max(0, Date.now() - new Date(startedAt).getTime());
}
