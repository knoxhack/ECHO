import cors from "cors";
import express, { Response } from "express";
import { spawn, execFileSync } from "node:child_process";
import { randomUUID } from "node:crypto";
import { existsSync, mkdirSync, readdirSync, readFileSync, statSync, writeFileSync } from "node:fs";
import { dirname, join, relative, resolve } from "node:path";
import { fileURLToPath } from "node:url";

type Status = "pass" | "warn" | "fail" | "unknown" | "running";
type QaStatus = "todo" | "pass" | "fail" | "blocked";
type LoreSeverity = "critical" | "warn" | "info";
type CompletionStatus = "implemented" | "partial" | "planned" | "blocked" | "deferred";

interface ModuleInfo {
  name: string;
  gradlePath: string;
  projectDir: string;
  modId: string;
  modName: string;
  version: string;
  role: "core" | "main" | "addon";
}

interface ArtifactInfo {
  module: ModuleInfo;
  expectedName: string;
  expectedPath: string;
  status: Status;
  found: Array<{
    name: string;
    path: string;
    size: number;
    modified: string;
    stale: boolean;
  }>;
}

interface CommandSpec {
  id: string;
  label: string;
  commandLine: string;
  description: string;
  executable: string;
  args: string[];
}

interface CommandRun {
  id: string;
  commandId: string;
  label: string;
  status: Status;
  startedAt: string;
  endedAt?: string;
  exitCode?: number;
  logs: string[];
}

interface QaMission {
  id: string;
  title: string;
  track: string;
  status: QaStatus;
  notes: string;
  updatedAt?: string;
}

interface LoreFinding {
  id: string;
  severity: LoreSeverity;
  ruleId: string;
  category: string;
  message: string;
  path: string;
  line: number;
  excerpt: string;
  hint: string;
}

interface CompletionFeature {
  id: string;
  track: string;
  module: string;
  title: string;
  status: CompletionStatus;
  summary: string;
  notes: string;
  evidence: string[];
  updatedAt?: string;
}

const toolRoot = process.cwd();
const repoRoot = resolve(toolRoot, "../..");
const localStateDir = join(repoRoot, ".local", "echo-release-terminal");
const qaPath = join(localStateDir, "qa.json");
const runsPath = join(localStateDir, "command-runs.json");
const completionPath = join(localStateDir, "completion.json");
const port = Number(process.env.ECHO_RELEASE_TERMINAL_PORT ?? 4177);
const app = express();

const commandRuns = new Map<string, CommandRun>();
const listeners = new Map<string, Set<Response>>();

const allowlistedCommands: CommandSpec[] = [
  {
    id: "build-workspace",
    label: "Build ECHO Workspace",
    commandLine: ".\\gradlew.bat buildEchoWorkspace",
    description: "Builds Core, Ashfall, and included addon chapters.",
    executable: join(repoRoot, "gradlew.bat"),
    args: ["buildEchoWorkspace"]
  },
  {
    id: "verify-release",
    label: "Verify ECHO Release",
    commandLine: ".\\gradlew.bat verifyEchoRelease --warning-mode all",
    description: "Runs validators, workspace build, jar set checks, runtime log checks, and GameTests.",
    executable: join(repoRoot, "gradlew.bat"),
    args: ["verifyEchoRelease", "--warning-mode", "all"]
  },
  {
    id: "copy-jars",
    label: "Copy Jars To Modpack",
    commandLine: ".\\gradlew.bat copyEchoJarsToModpack",
    description: "Builds and copies ECHO jars into the configured local CurseForge profile.",
    executable: join(repoRoot, "gradlew.bat"),
    args: ["copyEchoJarsToModpack"]
  },
  {
    id: "validate-resources",
    label: "Validate Resources",
    commandLine: "python tools\\validate_resources.py",
    description: "Checks resources, localization, namespaces, packet ids, and terminal polish rules.",
    executable: "python",
    args: [join("tools", "validate_resources.py")]
  },
  {
    id: "validate-gameplay",
    label: "Validate Gameplay Data",
    commandLine: "python tools\\validate_gameplay_data.py",
    description: "Runs gameplay data validation.",
    executable: "python",
    args: [join("tools", "validate_gameplay_data.py")]
  },
  {
    id: "check-poi-structures",
    label: "Check POI Structures",
    commandLine: "python tools\\structure_generator\\generator.py --check",
    description: "Validates generated POI structure templates and pool coverage.",
    executable: "python",
    args: [join("tools", "structure_generator", "generator.py"), "--check"]
  },
  {
    id: "check-runtime-logs",
    label: "Check Runtime Logs",
    commandLine: "python tools\\check_echo_runtime_logs.py --max-age-minutes 180",
    description: "Scans recent run logs and crash reports for ECHO startup crash signatures.",
    executable: "python",
    args: [join("tools", "check_echo_runtime_logs.py"), "--max-age-minutes", "180"]
  }
];

function ensureStateDir() {
  mkdirSync(localStateDir, { recursive: true });
}

function readText(path: string) {
  return existsSync(path) ? readFileSync(path, "utf8") : "";
}

function readProperties(path: string) {
  const values: Record<string, string> = {};
  for (const rawLine of readText(path).split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line || line.startsWith("#")) continue;
    const index = line.indexOf("=");
    if (index === -1) continue;
    values[line.slice(0, index).trim()] = line.slice(index + 1).trim();
  }
  return values;
}

function tryGit(args: string[]) {
  try {
    return execFileSync("git", args, {
      cwd: repoRoot,
      encoding: "utf8",
      stdio: ["ignore", "pipe", "ignore"]
    }).trim();
  } catch {
    return "";
  }
}

function parseWorkspace() {
  const rootProps = readProperties(join(repoRoot, "gradle.properties"));
  const settings = readText(join(repoRoot, "settings.gradle"));
  const projectDirs = new Map<string, string>();
  const includes: string[] = [];

  for (const match of settings.matchAll(/include\s+'([^']+)'/g)) {
    includes.push(match[1]);
  }
  for (const match of settings.matchAll(/project\(':(.*?)'\)\.projectDir\s*=\s*file\('([^']+)'\)/g)) {
    projectDirs.set(match[1], match[2]);
  }

  const modules: ModuleInfo[] = [
    {
      name: rootProps.mod_id ?? "echoashfallprotocol",
      gradlePath: ":",
      projectDir: repoRoot,
      modId: rootProps.mod_id ?? "echoashfallprotocol",
      modName: rootProps.mod_name ?? "ECHO: Ashfall Protocol",
      version: rootProps.mod_version ?? "unknown",
      role: "main"
    }
  ];

  for (const name of includes) {
    const projectDir = resolve(repoRoot, projectDirs.get(name) ?? name);
    const props = readProperties(join(projectDir, "gradle.properties"));
    const modId = props.mod_id ?? name;
    modules.push({
      name,
      gradlePath: `:${name}`,
      projectDir,
      modId,
      modName: props.mod_name ?? name,
      version: props.mod_version ?? "unknown",
      role: name === "echocore" ? "core" : "addon"
    });
  }

  const dirtyFiles = tryGit(["status", "--short"]).split(/\r?\n/).filter(Boolean);
  return {
    repoRoot,
    remote: tryGit(["config", "--get", "remote.origin.url"]) || "unknown",
    dirty: dirtyFiles.length > 0,
    dirtyFiles,
    modules,
    minecraftVersion: rootProps.minecraft_version ?? "unknown",
    neoForgeVersion: rootProps.neo_version ?? "unknown",
    javaTarget: "25"
  };
}

function getArtifacts(): ArtifactInfo[] {
  const workspace = parseWorkspace();
  return workspace.modules.map((module) => {
    const libsDir = join(module.projectDir, "build", "libs");
    const expectedName = `${module.modId}-${module.version}.jar`;
    const expectedPath = join(libsDir, expectedName);
    const found = existsSync(libsDir)
      ? readdirSync(libsDir)
          .filter((name) => name.endsWith(".jar") && name.toLowerCase().startsWith(`${module.modId.toLowerCase()}-`))
          .map((name) => {
            const path = join(libsDir, name);
            const stats = statSync(path);
            return {
              name,
              path,
              size: stats.size,
              modified: stats.mtime.toISOString(),
              stale: name !== expectedName
            };
          })
      : [];
    const hasExpected = found.some((artifact) => artifact.name === expectedName);
    const hasStale = found.some((artifact) => artifact.stale);
    const status: Status = hasExpected ? (hasStale ? "warn" : "pass") : "fail";
    return { module, expectedName, expectedPath, status, found };
  });
}

function loadRuns() {
  if (!existsSync(runsPath)) return;
  try {
    const runs = JSON.parse(readText(runsPath)) as CommandRun[];
    for (const run of runs.slice(-30)) commandRuns.set(run.id, run);
  } catch {
    // Ignore corrupt local runtime state.
  }
}

function persistRuns() {
  ensureStateDir();
  writeFileSync(runsPath, JSON.stringify([...commandRuns.values()].slice(-30), null, 2));
}

function latestRunFor(commandId: string) {
  return [...commandRuns.values()].reverse().find((run) => run.commandId === commandId);
}

function statusFromRun(commandId: string): Status {
  const run = latestRunFor(commandId);
  if (!run) return "unknown";
  return run.status;
}

function getChecks() {
  const artifacts = getArtifacts();
  const missing = artifacts.filter((artifact) => artifact.status === "fail").length;
  const stale = artifacts.filter((artifact) => artifact.status === "warn").length;
  const artifactStatus: Status = missing > 0 ? "fail" : stale > 0 ? "warn" : "pass";
  return [
    {
      id: "artifact-set",
      label: "Artifact Set",
      status: artifactStatus,
      detail: `${artifacts.length - missing}/${artifacts.length} expected jars present`
    },
    ...allowlistedCommands
      .filter((command) => command.id !== "copy-jars")
      .map((command) => {
        const run = latestRunFor(command.id);
        return {
          id: command.id,
          label: command.label,
          commandId: command.id,
          status: statusFromRun(command.id),
          detail: run?.endedAt
            ? `Last exit ${run.exitCode ?? "unknown"} at ${new Date(run.endedAt).toLocaleString()}`
            : command.description
        };
      })
  ];
}

function defaultQaMissions(): QaMission[] {
  const now = new Date().toISOString();
  return [
    ["first-night", "First-night survival", "Ashfall"],
    ["jei-recipes", "JEI custom recipe visibility", "Recipes"],
    ["scanner-routes", "Scanner route profiles and POI Atlas", "Exploration"],
    ["faction-contracts", "Faction NPC contracts and standing", "Factions"],
    ["drone-modes", "Drone repair and mode commands", "Systems"],
    ["nexus-choice", "Nexus relay, siege, choice, and epilogue", "Nexus"],
    ["orbital-unlock", "Post-Nexus Orbital unlock and diagnostics", "Orbital"],
    ["addon-compat", "Addon matrix compatibility smoke", "Addons"]
  ].map(([id, title, track]) => ({ id, title, track, status: "todo" as QaStatus, notes: "", updatedAt: now }));
}

function loadQa() {
  if (!existsSync(qaPath)) return defaultQaMissions();
  try {
    return JSON.parse(readText(qaPath)) as QaMission[];
  } catch {
    return defaultQaMissions();
  }
}

function saveQa(missions: QaMission[]) {
  ensureStateDir();
  writeFileSync(qaPath, JSON.stringify(missions, null, 2));
}

function relPath(path: string) {
  return relative(repoRoot, path).replace(/\\/g, "/");
}

function shouldSkipDir(path: string) {
  const normalized = relPath(path);
  return /(^|\/)(\.git|\.gradle|build|run|run-gametest|node_modules|dist|dist-server|__pycache__)(\/|$)/.test(normalized);
}

function walkFiles(root: string, predicate: (path: string) => boolean, output: string[] = []) {
  if (!existsSync(root) || shouldSkipDir(root)) return output;
  for (const entry of readdirSync(root, { withFileTypes: true })) {
    const path = join(root, entry.name);
    if (entry.isDirectory()) {
      walkFiles(path, predicate, output);
    } else if (entry.isFile() && predicate(path)) {
      output.push(path);
    }
  }
  return output;
}

function loreScanFiles() {
  const docs = ["LORE_BIBLE.md", "MODPACK_OVERVIEW.md", "README.md", "GETTING_STARTED.md", "PROCEDURAL_STRUCTURES.md"]
    .map((name) => join(repoRoot, name))
    .filter(existsSync);
  const roots = [join(repoRoot, "src"), join(repoRoot, "core"), join(repoRoot, "addons")];
  const codeFiles = roots.flatMap((root) =>
    walkFiles(root, (path) => {
      const normalized = relPath(path).toLowerCase();
      if (!/\.(java|json|md)$/.test(normalized)) return false;
      return (
        normalized.includes("/lang/") ||
        normalized.includes("terminal") ||
        normalized.includes("mission") ||
        normalized.includes("archive") ||
        normalized.includes("lore") ||
        normalized.includes("route") ||
        normalized.includes("faction") ||
        normalized.includes("dialogue")
      );
    })
  );
  return [...new Set([...docs, ...codeFiles])];
}

function isPlayerFacingLoreFile(path: string) {
  const normalized = relPath(path).toLowerCase();
  return (
    normalized.includes("/lang/") ||
    normalized.includes("terminal") ||
    normalized.includes("mission") ||
    normalized.includes("archive") ||
    normalized.includes("lore") ||
    normalized.includes("dialogue")
  );
}

function addLoreFinding(findings: LoreFinding[], finding: Omit<LoreFinding, "id">) {
  findings.push({ ...finding, id: `${finding.ruleId}:${finding.path}:${finding.line}:${findings.length}` });
}

function getLoreReport() {
  const files = loreScanFiles();
  const findings: LoreFinding[] = [];
  const canonTerms = ["Gridfall", "ECHO-7", "ECHO-0", "Nexus Core", "Prime Relays", "Orbital Remnants"];
  const docsText = ["LORE_BIBLE.md", "MODPACK_OVERVIEW.md", "README.md"]
    .map((name) => readText(join(repoRoot, name)))
    .join("\n");

  for (const term of canonTerms) {
    if (!docsText.includes(term)) {
      addLoreFinding(findings, {
        severity: "critical",
        ruleId: "canon-term-missing",
        category: "Canon",
        message: `Core canon term "${term}" is missing from primary docs.`,
        path: "LORE_BIBLE.md",
        line: 1,
        excerpt: term,
        hint: "Restore the shared canon term in the lore docs or update the scanner seed if the canon changed."
      });
    }
  }

  const lineRules: Array<{
    ruleId: string;
    category: string;
    severity: LoreSeverity;
    pattern: RegExp;
    message: string;
    hint: string;
    playerFacingOnly?: boolean;
  }> = [
    {
      ruleId: "placeholder-copy",
      category: "Placeholder",
      severity: "critical",
      pattern: /\b(TODO|FIXME|placeholder|stub|not implemented|legacy placeholder)\b/i,
      message: "Placeholder or unfinished implementation language found.",
      hint: "Replace placeholder wording with final player-facing copy or track it in Completion Matrix."
    },
    {
      ruleId: "developer-facing-copy",
      category: "Voice",
      severity: "warn",
      pattern: /\b(API|debug|test|unit test|implementation|stack trace)\b/i,
      message: "Developer-facing wording appears in player-facing lore or terminal text.",
      hint: "Use ECHO operator language: diagnostics, route, signal, protocol, field record, or blocker.",
      playerFacingOnly: true
    },
    {
      ruleId: "stale-terminal-reference",
      category: "Consistency",
      severity: "warn",
      pattern: /\b(DroneMenu|DRONE_MENU|echoashfallprotocol:echo_terminal)\b/,
      message: "Stale terminal or drone reference found.",
      hint: "Use the supported terminal block and current drone-control surface."
    },
    {
      ruleId: "locked-spoiler-risk",
      category: "Gating",
      severity: "info",
      pattern: /\b(Restore|Destroy|Control|Warden|final epilogue)\b/i,
      message: "Potential late-game spoiler appears in a lore-bearing file.",
      hint: "Confirm this text is gated, a roadmap hint, or operator-only release tooling."
    }
  ];

  for (const file of files) {
    const relativePath = relPath(file);
    const playerFacing = isPlayerFacingLoreFile(file);
    readText(file)
      .split(/\r?\n/)
      .forEach((line, index) => {
        for (const rule of lineRules) {
          if (rule.playerFacingOnly && !playerFacing) continue;
          if (!rule.pattern.test(line)) continue;
          addLoreFinding(findings, {
            severity: rule.severity,
            ruleId: rule.ruleId,
            category: rule.category,
            message: rule.message,
            path: relativePath,
            line: index + 1,
            excerpt: line.trim().slice(0, 260),
            hint: rule.hint
          });
        }
      });
  }

  const counts = {
    critical: findings.filter((finding) => finding.severity === "critical").length,
    warn: findings.filter((finding) => finding.severity === "warn").length,
    info: findings.filter((finding) => finding.severity === "info").length
  };
  const score = Math.max(0, Math.round(100 - counts.critical * 10 - counts.warn * 3 - counts.info * 1));
  return {
    score,
    scannedFiles: files.map(relPath),
    scannedFileCount: files.length,
    counts,
    ruleCategories: [...new Set(findings.map((finding) => finding.category))].sort(),
    findings
  };
}

function defaultCompletionFeatures(): CompletionFeature[] {
  const rows: Array<Omit<CompletionFeature, "status" | "notes" | "evidence"> & { status?: CompletionStatus }> = [
    {
      id: "core-services",
      track: "Foundation",
      module: "echocore",
      title: "Shared ECHO service layer",
      summary: "Profile state, diagnostics, hazards, route records, faction services, rewards, terminal placement, and Nexus mirrors.",
      status: "implemented"
    },
    {
      id: "terminal-surface",
      track: "Foundation",
      module: "echoterminal",
      title: "Common terminal command surface",
      summary: "Command Deck, What Now, Mission Graph, Route Records, Faction Atlas, Vitals, Reward Inbox, Archives, Baseline, Addons.",
      status: "implemented"
    },
    {
      id: "ashfall-survival",
      track: "Ashfall",
      module: "echoashfallprotocol",
      title: "Ashfall survival loop",
      summary: "Drop pod start, hydration, toxic air, radiation, mutations, filters, field medicine, and expedition pressure.",
      status: "implemented"
    },
    {
      id: "machines-power",
      track: "Ashfall",
      module: "echoashfallprotocol",
      title: "Machines and power restoration",
      summary: "Recycler, generators, purifier, filter workbench, battery bank, grinder, refiner, pipes, controller, deep extraction.",
      status: "partial"
    },
    {
      id: "worldgen-pois",
      track: "Ashfall",
      module: "echoashfallprotocol",
      title: "Worldgen, scanner, POIs, and guardian routes",
      summary: "Ruined biomes, scanner profiles, POI Atlas, faction hubs, procedural landmarks, and buried guardian nodes.",
      status: "partial"
    },
    {
      id: "factions-intel",
      track: "Ashfall",
      module: "echoashfallprotocol",
      title: "Factions, contracts, and intel",
      summary: "Remnants, Salvagers, Mutants, standings, contracts, raids, patrols, NPC dialogue, dossiers, and POI affinity.",
      status: "partial"
    },
    {
      id: "drone-system",
      track: "Ashfall",
      module: "echoashfallprotocol",
      title: "ECHO companion drone",
      summary: "Repair ladder, follow/scout/combat/scavenge/patrol modes, target marking, faction-aware behavior, and intel gathering.",
      status: "partial"
    },
    {
      id: "research-schematics",
      track: "Ashfall",
      module: "echoashfallprotocol",
      title: "Research and schematics",
      summary: "Schematic fragments, rare tech schematics, branch unlocks, RP archive fallback, and machine-tier visibility.",
      status: "partial"
    },
    {
      id: "nexus-endgame",
      track: "Endgame",
      module: "echoashfallprotocol",
      title: "Nexus endgame and final choice",
      summary: "Nine guardians, Prime Relays, siege readiness, Restore/Destroy/Control, Pre-Fall Archives, Warden, and epilogue.",
      status: "partial"
    },
    {
      id: "orbital-remnants",
      track: "Addons",
      module: "echoorbitalremnants",
      title: "Orbital Remnants expansion",
      summary: "Post-Nexus quarantine continuation, ECHO-0 route chain, station telemetry, support caches, and orbital factions.",
      status: "partial"
    },
    {
      id: "stationfall",
      track: "Addons",
      module: "echostationfall",
      title: "Stationfall chapter",
      summary: "Station survival content, crew logs, pressure systems, station entities, and orbital recovery chain.",
      status: "planned"
    },
    {
      id: "nexus-protocol",
      track: "Addons",
      module: "echonexusprotocol",
      title: "Nexus Protocol addon",
      summary: "Expanded Nexus/anomaly chapter content and terminal integration.",
      status: "planned"
    },
    {
      id: "industrial-nexus",
      track: "Addons",
      module: "echoindustrialnexus",
      title: "Industrial Nexus addon",
      summary: "Industrial machinery, late-grid infrastructure, and expansion content.",
      status: "planned"
    },
    {
      id: "blackbox-protocol",
      track: "Addons",
      module: "echoblackboxprotocol",
      title: "Blackbox Protocol addon",
      summary: "Blackbox investigation content, records, salvage, and terminal integration.",
      status: "planned"
    },
    {
      id: "release-terminal",
      track: "Operations",
      module: "release-tooling",
      title: "Release operations terminal",
      summary: "Build gates, artifact checks, GitHub release drafting, QA board, crash decoding, lore checks, and completion matrix.",
      status: "partial"
    }
  ];
  const now = new Date().toISOString();
  return rows.map((row) => ({ ...row, status: row.status ?? "planned", notes: "", evidence: [], updatedAt: now }));
}

function completionScanFiles() {
  return walkFiles(repoRoot, (path) => /\.(java|json|md|ts|tsx)$/.test(path.toLowerCase()));
}

function featureEvidence(feature: CompletionFeature, workspace: ReturnType<typeof parseWorkspace>, artifacts: ArtifactInfo[], allFiles: string[], markerFiles: string[]) {
  const evidence: string[] = [];
  const module = workspace.modules.find((candidate) => candidate.modId === feature.module || candidate.name === feature.module);
  if (module) evidence.push(`Module present: ${module.gradlePath}`);
  const artifact = artifacts.find((candidate) => candidate.module.modId === feature.module || candidate.module.name === feature.module);
  if (artifact?.found.some((item) => item.name === artifact.expectedName)) evidence.push(`Expected jar present: ${artifact.expectedName}`);
  const searchRoots = module ? [module.projectDir] : feature.module === "release-tooling" ? [toolRoot] : [repoRoot];
  const tokens = feature.id.split("-").concat(feature.title.toLowerCase().split(/\W+/)).filter((token) => token.length > 4);
  const sourceHits = searchRoots.flatMap((root) =>
    allFiles
      .filter((path) => path.startsWith(root))
      .filter((path) => tokens.some((token) => relPath(path).toLowerCase().includes(token)))
      .slice(0, 4)
      .map((path) => `Source/resource signal: ${relPath(path)}`)
  );
  evidence.push(...sourceHits.slice(0, 4));
  const markerHits = searchRoots.flatMap((root) =>
    markerFiles
      .filter((path) => path.startsWith(root))
      .slice(0, 2)
      .map((path) => `Open marker: ${relPath(path)}`)
  );
  evidence.push(...markerHits.slice(0, 2));
  return [...new Set(evidence)].slice(0, 8);
}

function loadCompletionFeatures(
  workspace: ReturnType<typeof parseWorkspace>,
  artifacts: ArtifactInfo[],
  allFiles: string[],
  markerFiles: string[]
) {
  const seeded = defaultCompletionFeatures();
  let overrides: CompletionFeature[] = [];
  if (existsSync(completionPath)) {
    try {
      overrides = JSON.parse(readText(completionPath)) as CompletionFeature[];
    } catch {
      overrides = [];
    }
  }
  const merged: CompletionFeature[] = seeded.map((feature) => {
    const override = overrides.find((candidate) => candidate.id === feature.id);
    const status = override?.status ?? feature.status;
    const notes = override?.notes ?? feature.notes;
    const updatedAt = override?.updatedAt ?? feature.updatedAt;
    return { ...feature, status, notes, updatedAt, evidence: featureEvidence({ ...feature, status, notes, updatedAt }, workspace, artifacts, allFiles, markerFiles) };
  });
  for (const override of overrides) {
    if (!merged.some((feature) => feature.id === override.id)) {
      merged.push({ ...override, evidence: featureEvidence(override, workspace, artifacts, allFiles, markerFiles) });
    }
  }
  return merged;
}

function saveCompletionFeatures(features: CompletionFeature[]) {
  ensureStateDir();
  const persisted = features.map(({ evidence, ...feature }) => feature);
  writeFileSync(completionPath, JSON.stringify(persisted, null, 2));
}

function getCompletionReport() {
  const workspace = parseWorkspace();
  const artifacts = getArtifacts();
  const allFiles = completionScanFiles();
  const markerFiles = allFiles.filter((path) => /\b(TODO|FIXME|placeholder|not implemented)\b/i.test(readText(path)));
  const features = loadCompletionFeatures(workspace, artifacts, allFiles, markerFiles);
  const weights: Record<CompletionStatus, number> = {
    implemented: 1,
    partial: 0.55,
    planned: 0.1,
    blocked: 0,
    deferred: 0
  };
  const percent = features.length ? Math.round((features.reduce((sum, feature) => sum + weights[feature.status], 0) / features.length) * 100) : 0;
  const counts = features.reduce(
    (acc, feature) => {
      acc[feature.status] += 1;
      return acc;
    },
    { implemented: 0, partial: 0, planned: 0, blocked: 0, deferred: 0 } as Record<CompletionStatus, number>
  );
  return {
    percent,
    counts,
    tracks: [...new Set(features.map((feature) => feature.track))].sort(),
    modules: [...new Set(features.map((feature) => feature.module))].sort(),
    features
  };
}

function sendEvent(runId: string, event: string, data: unknown) {
  const clients = listeners.get(runId);
  if (!clients) return;
  const payload = `event: ${event}\ndata: ${JSON.stringify(data)}\n\n`;
  for (const client of clients) client.write(payload);
}

function appendLog(run: CommandRun, text: string) {
  const normalized = text.replace(/\r/g, "");
  run.logs.push(normalized);
  sendEvent(run.id, "log", normalized);
}

function buildReleaseDraft(body: { notes?: string; knownIssues?: string; title?: string }) {
  const workspace = parseWorkspace();
  const artifacts = getArtifacts();
  const qa = loadQa();
  const commits = tryGit(["log", "--oneline", "-n", "20"]).split(/\r?\n/).filter(Boolean);
  const version = workspace.modules.find((module) => module.modId === "echoashfallprotocol")?.version ?? "unknown";
  const title = body.title?.trim() || `ECHO Release ${version}`;
  const lines = [
    `# ${title}`,
    "",
    "## Summary",
    body.notes?.trim() || "Release candidate generated by ECHO Release Terminal.",
    "",
    "## Module Versions",
    ...workspace.modules.map((module) => `- ${module.modName} (${module.modId}) ${module.version}`),
    "",
    "## Artifacts",
    ...artifacts.map((artifact) => {
      const found = artifact.found.find((item) => item.name === artifact.expectedName);
      return `- ${artifact.expectedName}: ${found ? `${formatBytes(found.size)}, ${artifact.status}` : "missing"}`;
    }),
    "",
    "## Release Gates",
    ...getChecks().map((check) => `- ${check.label}: ${check.status.toUpperCase()} - ${check.detail}`),
    "",
    "## QA Smoke",
    ...qa.map((mission) => `- ${mission.title}: ${mission.status.toUpperCase()}${mission.notes ? ` - ${mission.notes}` : ""}`),
    "",
    "## Recent Commits",
    ...(commits.length ? commits.map((commit) => `- ${commit}`) : ["- No commit history available."]),
    "",
    "## Known Issues",
    body.knownIssues?.trim() || "- None recorded in this draft."
  ];

  const manifest = artifacts
    .map((artifact) => {
      const found = artifact.found.find((item) => item.name === artifact.expectedName);
      return `${artifact.expectedName}\t${found ? artifact.expectedPath : "MISSING"}\t${artifact.status}`;
    })
    .join("\n");

  return { markdown: lines.join("\n"), manifest };
}

function formatBytes(size: number) {
  if (size > 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`;
  if (size > 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${size} B`;
}

function decodeCrashLog(text: string) {
  const workspace = parseWorkspace();
  const metadata: Record<string, string> = {};
  const metadataPatterns: Array<[string, RegExp]> = [
    ["minecraft", /Minecraft Version:\s*([^\r\n]+)/i],
    ["neoforge", /NeoForge(?: Version)?:\s*([^\r\n]+)/i],
    ["java", /Java Version:\s*([^\r\n]+)/i],
    ["time", /Time:\s*([^\r\n]+)/i]
  ];
  for (const [key, pattern] of metadataPatterns) {
    const match = text.match(pattern);
    if (match) metadata[key] = match[1].trim();
  }

  const lower = text.toLowerCase();
  const suspectedModules = workspace.modules
    .filter((module) => lower.includes(module.modId.toLowerCase()) || lower.includes(module.name.toLowerCase()))
    .map((module) => module.modId);
  const causes = text
    .split(/\r?\n/)
    .filter((line) => /^(Caused by:|Description:|-- Head --)/.test(line.trim()))
    .slice(0, 8);
  const topFrames = text
    .split(/\r?\n/)
    .filter((line) => /^\s*at\s+[\w.$]+\(.*\)/.test(line))
    .slice(0, 12)
    .map((line) => line.trim());
  const nextActions = [
    "Run the Resource Integrity and Gameplay Data validators if the stack references data, JSON, lang, texture, model, or registry files.",
    "Run the Runtime Log scanner after reproducing a startup failure.",
    suspectedModules.length
      ? `Inspect the module(s) most visible in the trace: ${suspectedModules.join(", ")}.`
      : "Search the top stack frames against source packages to identify the owning module."
  ];
  return {
    metadata,
    suspectedModules: [...new Set(suspectedModules)],
    topFrames,
    causes,
    nextActions
  };
}

app.use(cors({ origin: ["http://127.0.0.1:5177", "http://localhost:5177"] }));
app.use(express.json({ limit: "4mb" }));

app.get("/api/workspace", (_request, response) => response.json(parseWorkspace()));
app.get("/api/artifacts", (_request, response) => response.json(getArtifacts()));
app.get("/api/checks", (_request, response) => response.json(getChecks()));
app.get("/api/lore/check", (_request, response) => response.json(getLoreReport()));
app.get("/api/completion", (_request, response) => response.json(getCompletionReport()));
app.put("/api/completion", (request, response) => {
  const features = Array.isArray(request.body?.features) ? (request.body.features as CompletionFeature[]) : defaultCompletionFeatures();
  const stamped = features.map((feature) => ({ ...feature, updatedAt: new Date().toISOString() }));
  saveCompletionFeatures(stamped);
  response.json(getCompletionReport());
});
app.get("/api/commands", (_request, response) => {
  response.json({
    commands: allowlistedCommands.map(({ executable, args, ...safe }) => safe),
    runs: [...commandRuns.values()].slice(-15).reverse()
  });
});

app.post("/api/commands/run", (request, response) => {
  const commandId = String(request.body?.commandId ?? "");
  const spec = allowlistedCommands.find((command) => command.id === commandId);
  if (!spec) {
    response.status(400).json({ error: "Command is not allowlisted." });
    return;
  }

  const run: CommandRun = {
    id: randomUUID(),
    commandId: spec.id,
    label: spec.label,
    status: "running",
    startedAt: new Date().toISOString(),
    logs: [`> ${spec.commandLine}\n`]
  };
  commandRuns.set(run.id, run);
  persistRuns();

  const child = spawn(spec.executable, spec.args, {
    cwd: repoRoot,
    shell: false,
    windowsHide: true
  });

  child.stdout.on("data", (chunk: Buffer) => appendLog(run, chunk.toString("utf8")));
  child.stderr.on("data", (chunk: Buffer) => appendLog(run, chunk.toString("utf8")));
  child.on("error", (error) => {
    appendLog(run, `\n[spawn-error] ${error.message}\n`);
    run.status = "fail";
    run.endedAt = new Date().toISOString();
    persistRuns();
    sendEvent(run.id, "done", run);
  });
  child.on("close", (code) => {
    run.exitCode = code ?? -1;
    run.status = code === 0 ? "pass" : "fail";
    run.endedAt = new Date().toISOString();
    appendLog(run, `\n[exit ${run.exitCode}]\n`);
    persistRuns();
    sendEvent(run.id, "done", run);
  });

  response.status(202).json(run);
});

app.get("/api/commands/:id/events", (request, response) => {
  const run = commandRuns.get(request.params.id);
  if (!run) {
    response.status(404).end();
    return;
  }

  response.writeHead(200, {
    "Content-Type": "text/event-stream",
    "Cache-Control": "no-cache",
    Connection: "keep-alive"
  });
  response.write(`event: snapshot\ndata: ${JSON.stringify(run)}\n\n`);

  const clients = listeners.get(run.id) ?? new Set<Response>();
  clients.add(response);
  listeners.set(run.id, clients);

  request.on("close", () => {
    clients.delete(response);
    if (clients.size === 0) listeners.delete(run.id);
  });
});

app.post("/api/releases/draft", (request, response) => response.json(buildReleaseDraft(request.body ?? {})));
app.post("/api/crash/decode", (request, response) => response.json(decodeCrashLog(String(request.body?.text ?? ""))));
app.get("/api/qa", (_request, response) => response.json(loadQa()));
app.put("/api/qa", (request, response) => {
  const missions = Array.isArray(request.body?.missions) ? (request.body.missions as QaMission[]) : defaultQaMissions();
  const stamped = missions.map((mission) => ({ ...mission, updatedAt: mission.updatedAt ?? new Date().toISOString() }));
  saveQa(stamped);
  response.json(stamped);
});

const serverDir = dirname(fileURLToPath(import.meta.url));
const clientDist = resolve(serverDir, "..", "dist");
if (existsSync(clientDist)) {
  app.use(express.static(clientDist));
  app.get("*", (_request, response) => response.sendFile(join(clientDist, "index.html")));
}

loadRuns();
app.listen(port, "127.0.0.1", () => {
  const rel = relative(repoRoot, toolRoot) || ".";
  console.log(`ECHO Release Terminal API listening on http://127.0.0.1:${port} from ${rel}`);
});
