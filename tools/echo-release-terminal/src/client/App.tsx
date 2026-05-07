import {
  Activity,
  AlertTriangle,
  BookOpenCheck,
  Boxes,
  Bug,
  CheckCircle2,
  ChevronRight,
  ClipboardCheck,
  FileText,
  Gauge,
  GitBranch,
  Hammer,
  ListChecks,
  PackageCheck,
  Play,
  RefreshCw,
  Rocket,
  ShieldCheck,
  Terminal,
  XCircle
} from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import type {
  ArtifactInfo,
  CheckInfo,
  CommandRun,
  CommandSpec,
  CompletionFeature,
  CompletionReport,
  CompletionStatus,
  CrashDecode,
  DraftResponse,
  LoreReport,
  LoreSeverity,
  ModuleInfo,
  QaMission,
  Status,
  WorkspaceInfo
} from "./types";

type ViewId = "deck" | "console" | "checks" | "forge" | "qa" | "lore" | "completion" | "crash" | "addons";

const views: Array<{ id: ViewId; label: string; icon: typeof Terminal }> = [
  { id: "deck", label: "Command Deck", icon: Gauge },
  { id: "console", label: "Build Console", icon: Terminal },
  { id: "checks", label: "Readiness Grid", icon: ShieldCheck },
  { id: "forge", label: "Release Forge", icon: Rocket },
  { id: "qa", label: "QA Board", icon: ClipboardCheck },
  { id: "lore", label: "Lore Check", icon: BookOpenCheck },
  { id: "completion", label: "Completion Matrix", icon: ListChecks },
  { id: "crash", label: "Crash Decoder", icon: Bug },
  { id: "addons", label: "Addon Matrix", icon: Boxes }
];

function statusLabel(status: Status) {
  return status.toUpperCase();
}

function statusIcon(status: Status) {
  if (status === "pass") return <CheckCircle2 size={16} />;
  if (status === "fail") return <XCircle size={16} />;
  if (status === "warn") return <AlertTriangle size={16} />;
  if (status === "running") return <RefreshCw size={16} className="spin" />;
  return <Activity size={16} />;
}

function formatBytes(size: number) {
  if (size > 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`;
  if (size > 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${size} B`;
}

async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {})
    }
  });
  if (!response.ok) throw new Error(await response.text());
  return response.json() as Promise<T>;
}

export function App() {
  const [activeView, setActiveView] = useState<ViewId>("deck");
  const [workspace, setWorkspace] = useState<WorkspaceInfo | null>(null);
  const [artifacts, setArtifacts] = useState<ArtifactInfo[]>([]);
  const [checks, setChecks] = useState<CheckInfo[]>([]);
  const [commands, setCommands] = useState<CommandSpec[]>([]);
  const [runs, setRuns] = useState<CommandRun[]>([]);
  const [qa, setQa] = useState<QaMission[]>([]);
  const [lore, setLore] = useState<LoreReport | null>(null);
  const [completion, setCompletion] = useState<CompletionReport | null>(null);
  const [activeRun, setActiveRun] = useState<CommandRun | null>(null);
  const [error, setError] = useState("");

  async function refresh() {
    setError("");
    try {
      const [workspaceData, artifactData, checkData, commandData, qaData, loreData, completionData] = await Promise.all([
        api<WorkspaceInfo>("/api/workspace"),
        api<ArtifactInfo[]>("/api/artifacts"),
        api<CheckInfo[]>("/api/checks"),
        api<{ commands: CommandSpec[]; runs: CommandRun[] }>("/api/commands"),
        api<QaMission[]>("/api/qa"),
        api<LoreReport>("/api/lore/check"),
        api<CompletionReport>("/api/completion")
      ]);
      setWorkspace(workspaceData);
      setArtifacts(artifactData);
      setChecks(checkData);
      setCommands(commandData.commands);
      setRuns(commandData.runs);
      setQa(qaData);
      setLore(loreData);
      setCompletion(completionData);
      if (!activeRun && commandData.runs.length > 0) setActiveRun(commandData.runs[0]);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Unable to load terminal data.");
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  const releaseStatus = useMemo<Status>(() => {
    if (checks.some((check) => check.status === "fail") || artifacts.some((artifact) => artifact.status === "fail")) return "fail";
    if (checks.some((check) => check.status === "running")) return "running";
    if (checks.some((check) => check.status === "warn") || artifacts.some((artifact) => artifact.status === "warn")) return "warn";
    if (checks.length && checks.every((check) => check.status === "pass")) return "pass";
    return "unknown";
  }, [artifacts, checks]);

  async function runCommand(commandId: string) {
    setActiveView("console");
    const run = await api<CommandRun>("/api/commands/run", {
      method: "POST",
      body: JSON.stringify({ commandId })
    });
    setRuns((current) => [run, ...current]);
    setActiveRun(run);
    const source = new EventSource(`/api/commands/${run.id}/events`);
    source.addEventListener("snapshot", (event) => {
      setActiveRun(JSON.parse((event as MessageEvent).data) as CommandRun);
    });
    source.addEventListener("log", (event) => {
      const chunk = (event as MessageEvent).data as string;
      setActiveRun((current) => (current?.id === run.id ? { ...current, logs: [...current.logs, chunk] } : current));
    });
    source.addEventListener("done", (event) => {
      const finished = JSON.parse((event as MessageEvent).data) as CommandRun;
      setActiveRun(finished);
      setRuns((current) => current.map((item) => (item.id === finished.id ? finished : item)));
      source.close();
      refresh();
    });
  }

  async function saveQa(next: QaMission[]) {
    setQa(next);
    const saved = await api<QaMission[]>("/api/qa", {
      method: "PUT",
      body: JSON.stringify({ missions: next })
    });
    setQa(saved);
  }

  return (
    <div className="terminal-shell">
      <aside className="nav-rail">
        <div className="brand-block">
          <Terminal size={28} />
          <div>
            <strong>ECHO</strong>
            <span>Release Terminal</span>
          </div>
        </div>
        <nav>
          {views.map((view) => {
            const Icon = view.icon;
            return (
              <button
                key={view.id}
                className={activeView === view.id ? "active" : ""}
                title={view.label}
                onClick={() => setActiveView(view.id)}
              >
                <Icon size={18} />
                <span>{view.label}</span>
              </button>
            );
          })}
        </nav>
      </aside>

      <main>
        <header className="top-strip">
          <div>
            <p className="eyebrow">LOCAL OPS / KNOXHACK.ECHO</p>
            <h1>{views.find((view) => view.id === activeView)?.label}</h1>
          </div>
          <div className="header-actions">
            <StatusPill status={releaseStatus} label={`Release ${statusLabel(releaseStatus)}`} />
            <button className="icon-button" title="Refresh terminal data" onClick={refresh}>
              <RefreshCw size={18} />
            </button>
          </div>
        </header>

        {error && <div className="alert-line">{error}</div>}

        {activeView === "deck" && (
          <CommandDeck
            workspace={workspace}
            artifacts={artifacts}
            checks={checks}
            commands={commands}
            lore={lore}
            completion={completion}
            runCommand={runCommand}
          />
        )}
        {activeView === "console" && (
          <BuildConsole commands={commands} runs={runs} activeRun={activeRun} setActiveRun={setActiveRun} runCommand={runCommand} />
        )}
        {activeView === "checks" && <ReadinessGrid checks={checks} artifacts={artifacts} commands={commands} runCommand={runCommand} />}
        {activeView === "forge" && <ReleaseForge workspace={workspace} />}
        {activeView === "qa" && <QaBoard missions={qa} saveQa={saveQa} />}
        {activeView === "lore" && <LoreCheck report={lore} refresh={refresh} />}
        {activeView === "completion" && <CompletionMatrix report={completion} setCompletion={setCompletion} />}
        {activeView === "crash" && <CrashDecoder />}
        {activeView === "addons" && <AddonMatrix workspace={workspace} artifacts={artifacts} />}
      </main>
    </div>
  );
}

function StatusPill({ status, label }: { status: Status; label?: string }) {
  return (
    <span className={`status-pill ${status}`}>
      {statusIcon(status)}
      {label ?? statusLabel(status)}
    </span>
  );
}

function CommandDeck({
  workspace,
  artifacts,
  checks,
  commands,
  lore,
  completion,
  runCommand
}: {
  workspace: WorkspaceInfo | null;
  artifacts: ArtifactInfo[];
  checks: CheckInfo[];
  commands: CommandSpec[];
  lore: LoreReport | null;
  completion: CompletionReport | null;
  runCommand: (commandId: string) => void;
}) {
  const modules = workspace?.modules ?? [];
  const missing = artifacts.filter((artifact) => artifact.status === "fail").length;
  const passingChecks = checks.filter((check) => check.status === "pass").length;
  const loreIssues = (lore?.counts.critical ?? 0) + (lore?.counts.warn ?? 0);
  const leftToImplement = (completion?.counts.planned ?? 0) + (completion?.counts.blocked ?? 0);
  return (
    <section className="view-grid deck-grid">
      <div className="ops-band">
        <div>
          <p className="eyebrow">SIGNAL HEALTH</p>
          <h2>{missing ? "Artifact gaps detected" : "Release systems listening"}</h2>
          <p>
            {workspace?.minecraftVersion ?? "unknown"} / NeoForge {workspace?.neoForgeVersion ?? "unknown"} / Java{" "}
            {workspace?.javaTarget ?? "unknown"}
          </p>
        </div>
        <div className="metric-row">
          <Metric label="Modules" value={String(modules.length)} />
          <Metric label="Checks" value={`${passingChecks}/${checks.length || 0}`} />
          <Metric label="Dirty Files" value={String(workspace?.dirtyFiles.length ?? 0)} tone={workspace?.dirty ? "warn" : "pass"} />
          <Metric label="Lore Score" value={lore ? `${lore.score}%` : "..." } tone={loreIssues > 0 ? "warn" : "pass"} />
          <Metric label="Completion" value={completion ? `${completion.percent}%` : "..."} tone={(completion?.percent ?? 0) > 70 ? "pass" : "warn"} />
          <Metric label="Left" value={String(leftToImplement)} tone={leftToImplement > 0 ? "warn" : "pass"} />
        </div>
      </div>

      <div className="module-grid">
        {modules.map((module) => (
          <ModuleTile key={module.gradlePath} module={module} artifact={artifacts.find((item) => item.module.modId === module.modId)} />
        ))}
      </div>

      <div className="panel">
        <PanelTitle icon={Hammer} title="Fast Commands" />
        <div className="command-stack">
          {commands.slice(0, 4).map((command) => (
            <button key={command.id} className="command-button" onClick={() => runCommand(command.id)}>
              <Play size={16} />
              <span>{command.label}</span>
              <ChevronRight size={16} />
            </button>
          ))}
        </div>
      </div>

      <div className="panel">
        <PanelTitle icon={GitBranch} title="Repository Signal" />
        <dl className="kv-list">
          <dt>Remote</dt>
          <dd>{workspace?.remote ?? "unknown"}</dd>
          <dt>Root</dt>
          <dd>{workspace?.repoRoot ?? "loading"}</dd>
          <dt>State</dt>
          <dd>{workspace?.dirty ? "Uncommitted changes present" : "Clean"}</dd>
        </dl>
      </div>
    </section>
  );
}

function Metric({ label, value, tone = "neutral" }: { label: string; value: string; tone?: "neutral" | "pass" | "warn" }) {
  return (
    <div className={`metric ${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function ModuleTile({ module, artifact }: { module: ModuleInfo; artifact?: ArtifactInfo }) {
  return (
    <article className="module-tile">
      <div className="tile-head">
        <span>{module.role}</span>
        <StatusPill status={artifact?.status ?? "unknown"} />
      </div>
      <h3>{module.modName}</h3>
      <p>{module.modId}</p>
      <div className="tile-foot">
        <code>{module.version}</code>
        <code>{module.gradlePath}</code>
      </div>
    </article>
  );
}

function BuildConsole({
  commands,
  runs,
  activeRun,
  setActiveRun,
  runCommand
}: {
  commands: CommandSpec[];
  runs: CommandRun[];
  activeRun: CommandRun | null;
  setActiveRun: (run: CommandRun) => void;
  runCommand: (commandId: string) => void;
}) {
  return (
    <section className="two-column">
      <div className="panel">
        <PanelTitle icon={Terminal} title="Allowlisted Commands" />
        <div className="command-list">
          {commands.map((command) => (
            <div className="command-row" key={command.id}>
              <div>
                <strong>{command.label}</strong>
                <code>{command.commandLine}</code>
                <p>{command.description}</p>
              </div>
              <button className="icon-button" title={`Run ${command.label}`} onClick={() => runCommand(command.id)}>
                <Play size={18} />
              </button>
            </div>
          ))}
        </div>
      </div>
      <div className="panel console-panel">
        <PanelTitle icon={Activity} title="Live Console" />
        <div className="run-tabs">
          {runs.map((run) => (
            <button key={run.id} className={activeRun?.id === run.id ? "active" : ""} onClick={() => setActiveRun(run)}>
              <StatusPill status={run.status} label={run.label} />
            </button>
          ))}
        </div>
        <pre className="log-window">{activeRun ? activeRun.logs.join("") : "No command selected."}</pre>
      </div>
    </section>
  );
}

function ReadinessGrid({
  checks,
  artifacts,
  commands,
  runCommand
}: {
  checks: CheckInfo[];
  artifacts: ArtifactInfo[];
  commands: CommandSpec[];
  runCommand: (commandId: string) => void;
}) {
  return (
    <section className="view-grid">
      <div className="panel wide">
        <PanelTitle icon={ShieldCheck} title="Release Gates" />
        <div className="table-like">
          {checks.map((check) => (
            <div className="table-row" key={check.id}>
              <StatusPill status={check.status} />
              <strong>{check.label}</strong>
              <span>{check.detail}</span>
              {check.commandId && (
                <button className="icon-button" title={`Run ${check.label}`} onClick={() => runCommand(check.commandId!)}>
                  <Play size={16} />
                </button>
              )}
            </div>
          ))}
        </div>
      </div>
      <div className="panel wide">
        <PanelTitle icon={PackageCheck} title="Artifact Manifest" />
        <div className="table-like">
          {artifacts.map((artifact) => (
            <div className="table-row artifact-row" key={artifact.expectedPath}>
              <StatusPill status={artifact.status} />
              <strong>{artifact.expectedName}</strong>
              <span>
                {artifact.found.length
                  ? artifact.found.map((item) => `${item.name} ${formatBytes(item.size)}`).join(", ")
                  : "missing from build/libs"}
              </span>
            </div>
          ))}
        </div>
      </div>
      <div className="panel">
        <PanelTitle icon={Hammer} title="Verification Actions" />
        <div className="command-stack">
          {commands
            .filter((command) => command.id !== "copy-jars")
            .map((command) => (
              <button className="command-button" key={command.id} onClick={() => runCommand(command.id)}>
                <Play size={16} />
                <span>{command.label}</span>
                <ChevronRight size={16} />
              </button>
            ))}
        </div>
      </div>
    </section>
  );
}

function ReleaseForge({ workspace }: { workspace: WorkspaceInfo | null }) {
  const [notes, setNotes] = useState("Release candidate generated from the current ECHO workspace.");
  const [knownIssues, setKnownIssues] = useState("- None recorded.");
  const [draft, setDraft] = useState<DraftResponse | null>(null);

  async function generate() {
    const response = await api<DraftResponse>("/api/releases/draft", {
      method: "POST",
      body: JSON.stringify({ notes, knownIssues })
    });
    setDraft(response);
  }

  return (
    <section className="two-column forge-layout">
      <div className="panel">
        <PanelTitle icon={Rocket} title="GitHub Release Draft" />
        <label>
          Release notes
          <textarea value={notes} onChange={(event) => setNotes(event.target.value)} />
        </label>
        <label>
          Known issues
          <textarea value={knownIssues} onChange={(event) => setKnownIssues(event.target.value)} />
        </label>
        <button className="primary-action" onClick={generate}>
          <FileText size={18} />
          Generate GitHub Draft
        </button>
        <dl className="kv-list">
          <dt>Repository</dt>
          <dd>{workspace?.remote ?? "unknown"}</dd>
          <dt>Publishing Lane</dt>
          <dd>GitHub Releases markdown and artifact manifest</dd>
        </dl>
      </div>
      <div className="panel">
        <PanelTitle icon={FileText} title="Draft Output" />
        <pre className="draft-window">{draft?.markdown ?? "Generate a draft to inspect release notes."}</pre>
        {draft && (
          <>
            <h3 className="subhead">Artifact Manifest</h3>
            <pre className="manifest-window">{draft.manifest}</pre>
          </>
        )}
      </div>
    </section>
  );
}

function QaBoard({ missions, saveQa }: { missions: QaMission[]; saveQa: (missions: QaMission[]) => void }) {
  function updateMission(id: string, patch: Partial<QaMission>) {
    saveQa(missions.map((mission) => (mission.id === id ? { ...mission, ...patch, updatedAt: new Date().toISOString() } : mission)));
  }

  return (
    <section className="panel wide">
      <PanelTitle icon={ClipboardCheck} title="Smoke Missions" />
      <div className="qa-grid">
        {missions.map((mission) => (
          <article className="qa-mission" key={mission.id}>
            <div className="tile-head">
              <span>{mission.track}</span>
              <select value={mission.status} onChange={(event) => updateMission(mission.id, { status: event.target.value as QaMission["status"] })}>
                <option value="todo">TODO</option>
                <option value="pass">PASS</option>
                <option value="fail">FAIL</option>
                <option value="blocked">BLOCKED</option>
              </select>
            </div>
            <h3>{mission.title}</h3>
            <textarea
              placeholder="QA notes"
              value={mission.notes}
              onChange={(event) => updateMission(mission.id, { notes: event.target.value })}
            />
          </article>
        ))}
      </div>
    </section>
  );
}

function LoreCheck({ report, refresh }: { report: LoreReport | null; refresh: () => void }) {
  const [severity, setSeverity] = useState<LoreSeverity | "all">("all");
  const findings = report?.findings ?? [];
  const visibleFindings = severity === "all" ? findings : findings.filter((finding) => finding.severity === severity);

  return (
    <section className="view-grid">
      <div className="ops-band">
        <div>
          <p className="eyebrow">CANON SCAN</p>
          <h2>{report ? `${report.score}% lore readiness` : "Scanning lore signal"}</h2>
          <p>
            {report
              ? `${report.scannedFileCount} files scanned across docs, lang, terminal, mission, faction, route, archive, and addon sources.`
              : "Waiting for deterministic scanner output."}
          </p>
        </div>
        <div className="metric-row compact">
          <Metric label="Critical" value={String(report?.counts.critical ?? 0)} tone={(report?.counts.critical ?? 0) > 0 ? "warn" : "pass"} />
          <Metric label="Warnings" value={String(report?.counts.warn ?? 0)} tone={(report?.counts.warn ?? 0) > 0 ? "warn" : "pass"} />
          <Metric label="Info" value={String(report?.counts.info ?? 0)} />
        </div>
      </div>

      <div className="panel">
        <PanelTitle icon={BookOpenCheck} title="Lore Filters" />
        <div className="filter-row">
          {(["all", "critical", "warn", "info"] as Array<LoreSeverity | "all">).map((item) => (
            <button key={item} className={severity === item ? "active" : ""} onClick={() => setSeverity(item)}>
              {item.toUpperCase()}
            </button>
          ))}
        </div>
        <h3 className="subhead">Rule Categories</h3>
        <div className="chip-row">
          {(report?.ruleCategories ?? []).map((category) => (
            <span className="data-chip" key={category}>
              {category}
            </span>
          ))}
        </div>
        <button className="primary-action" onClick={refresh}>
          <RefreshCw size={18} />
          Rescan Lore
        </button>
      </div>

      <div className="panel">
        <PanelTitle icon={FileText} title="Scanned Sources" />
        <div className="source-list">
          {(report?.scannedFiles ?? []).slice(0, 80).map((path) => (
            <code key={path}>{path}</code>
          ))}
        </div>
      </div>

      <div className="panel wide">
        <PanelTitle icon={AlertTriangle} title="Lore Findings" />
        {visibleFindings.length ? (
          <div className="finding-grid">
            {visibleFindings.map((finding) => (
              <article className={`finding-card ${finding.severity}`} key={finding.id}>
                <div className="tile-head">
                  <span>{finding.category}</span>
                  <SeverityPill severity={finding.severity} />
                </div>
                <h3>{finding.message}</h3>
                <p>{finding.hint}</p>
                <code>
                  {finding.path}:{finding.line}
                </code>
                <pre>{finding.excerpt || "(empty line)"}</pre>
              </article>
            ))}
          </div>
        ) : (
          <p className="muted">No findings match the current filter.</p>
        )}
      </div>
    </section>
  );
}

function SeverityPill({ severity }: { severity: LoreSeverity }) {
  const status: Status = severity === "critical" ? "fail" : severity === "warn" ? "warn" : "unknown";
  return <StatusPill status={status} label={severity.toUpperCase()} />;
}

function CompletionMatrix({
  report,
  setCompletion
}: {
  report: CompletionReport | null;
  setCompletion: (report: CompletionReport) => void;
}) {
  const [track, setTrack] = useState("all");
  const [module, setModule] = useState("all");
  const features = report?.features ?? [];
  const visible = features.filter((feature) => (track === "all" || feature.track === track) && (module === "all" || feature.module === module));
  const left = features.filter((feature) => feature.status === "planned" || feature.status === "blocked");

  async function updateFeature(id: string, patch: Partial<CompletionFeature>) {
    if (!report) return;
    const nextFeatures = report.features.map((feature) =>
      feature.id === id ? { ...feature, ...patch, updatedAt: new Date().toISOString() } : feature
    );
    const saved = await api<CompletionReport>("/api/completion", {
      method: "PUT",
      body: JSON.stringify({ features: nextFeatures })
    });
    setCompletion(saved);
  }

  return (
    <section className="view-grid">
      <div className="ops-band">
        <div>
          <p className="eyebrow">VISION TRACKER</p>
          <h2>{report ? `${report.percent}% stack completion` : "Loading completion matrix"}</h2>
          <p>Curated roadmap with automatic repo evidence and local notes.</p>
        </div>
        <div className="metric-row compact">
          <Metric label="Done" value={String(report?.counts.implemented ?? 0)} tone="pass" />
          <Metric label="Partial" value={String(report?.counts.partial ?? 0)} tone="warn" />
          <Metric label="Planned" value={String(report?.counts.planned ?? 0)} />
        </div>
      </div>

      <div className="panel">
        <PanelTitle icon={ListChecks} title="Matrix Filters" />
        <label>
          Track
          <select value={track} onChange={(event) => setTrack(event.target.value)}>
            <option value="all">All tracks</option>
            {(report?.tracks ?? []).map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>
        </label>
        <label>
          Module
          <select value={module} onChange={(event) => setModule(event.target.value)}>
            <option value="all">All modules</option>
            {(report?.modules ?? []).map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="panel">
        <PanelTitle icon={AlertTriangle} title="Left To Implement" />
        <div className="left-list">
          {left.length ? (
            left.map((feature) => (
              <div key={feature.id}>
                <CompletionPill status={feature.status} />
                <strong>{feature.title}</strong>
                <span>{feature.module}</span>
              </div>
            ))
          ) : (
            <p className="muted">No planned or blocked features are currently tracked.</p>
          )}
        </div>
      </div>

      <div className="panel wide">
        <PanelTitle icon={ListChecks} title="Vision Features" />
        <div className="completion-grid">
          {visible.map((feature) => (
            <article className="completion-card" key={feature.id}>
              <div className="tile-head">
                <span>{feature.track}</span>
                <CompletionPill status={feature.status} />
              </div>
              <h3>{feature.title}</h3>
              <p>{feature.summary}</p>
              <div className="completion-controls">
                <select
                  value={feature.status}
                  onChange={(event) => updateFeature(feature.id, { status: event.target.value as CompletionStatus })}
                >
                  <option value="implemented">IMPLEMENTED</option>
                  <option value="partial">PARTIAL</option>
                  <option value="planned">PLANNED</option>
                  <option value="blocked">BLOCKED</option>
                  <option value="deferred">DEFERRED</option>
                </select>
                <code>{feature.module}</code>
              </div>
              <textarea
                placeholder="Completion notes"
                value={feature.notes}
                onChange={(event) => updateFeature(feature.id, { notes: event.target.value })}
              />
              <div className="evidence-list">
                {feature.evidence.length ? (
                  feature.evidence.map((item) => <code key={item}>{item}</code>)
                ) : (
                  <span className="muted">No automatic evidence found yet.</span>
                )}
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}

function CompletionPill({ status }: { status: CompletionStatus }) {
  return <span className={`completion-pill ${status}`}>{status.toUpperCase()}</span>;
}

function CrashDecoder() {
  const [text, setText] = useState("");
  const [decode, setDecode] = useState<CrashDecode | null>(null);

  async function inspect() {
    const response = await api<CrashDecode>("/api/crash/decode", {
      method: "POST",
      body: JSON.stringify({ text })
    });
    setDecode(response);
  }

  return (
    <section className="two-column">
      <div className="panel">
        <PanelTitle icon={Bug} title="Crash Input" />
        <textarea className="crash-input" value={text} onChange={(event) => setText(event.target.value)} placeholder="Paste crash report or latest.log text" />
        <button className="primary-action" onClick={inspect}>
          <Bug size={18} />
          Decode Signal
        </button>
      </div>
      <div className="panel">
        <PanelTitle icon={AlertTriangle} title="Decoded Report" />
        {decode ? (
          <div className="decode-stack">
            <h3>Metadata</h3>
            <dl className="kv-list">
              {Object.entries(decode.metadata).map(([key, value]) => (
                <>
                  <dt key={`${key}-k`}>{key}</dt>
                  <dd key={`${key}-v`}>{value}</dd>
                </>
              ))}
            </dl>
            <h3>Suspected Modules</h3>
            <p>{decode.suspectedModules.join(", ") || "No ECHO module names found in the pasted text."}</p>
            <h3>Top Frames</h3>
            <pre className="manifest-window">{decode.topFrames.join("\n") || "No stack frames detected."}</pre>
            <h3>Next Actions</h3>
            <ul className="plain-list">
              {decode.nextActions.map((action) => (
                <li key={action}>{action}</li>
              ))}
            </ul>
          </div>
        ) : (
          <p className="muted">Paste a log to extract versions, causes, stack frames, and likely modules.</p>
        )}
      </div>
    </section>
  );
}

function AddonMatrix({ workspace, artifacts }: { workspace: WorkspaceInfo | null; artifacts: ArtifactInfo[] }) {
  return (
    <section className="panel wide">
      <PanelTitle icon={Boxes} title="Module Compatibility Matrix" />
      <div className="table-like">
        {(workspace?.modules ?? []).map((module) => {
          const artifact = artifacts.find((item) => item.module.modId === module.modId);
          return (
            <div className="table-row addon-row" key={module.gradlePath}>
              <StatusPill status={artifact?.status ?? "unknown"} />
              <strong>{module.modName}</strong>
              <code>{module.modId}</code>
              <span>{module.role === "core" ? "Required shared services" : module.role === "main" ? "Main Ashfall campaign" : "Optional addon chapter"}</span>
              <code>{module.version}</code>
            </div>
          );
        })}
      </div>
    </section>
  );
}

function PanelTitle({ icon: Icon, title }: { icon: typeof Terminal; title: string }) {
  return (
    <div className="panel-title">
      <Icon size={18} />
      <h2>{title}</h2>
    </div>
  );
}
