import { useEffect, useMemo, useState } from "react";
import {
  Activity,
  AlertTriangle,
  Boxes,
  Clipboard,
  Database,
  Download,
  FileJson,
  FileText,
  Gauge,
  Image,
  Layers,
  ListChecks,
  Map,
  Play,
  RefreshCw,
  Rocket,
  Settings,
  ShieldCheck,
  Sparkles,
  Square,
  TerminalSquare
} from "lucide-react";
import type {
  AppSettings,
  CommandRun,
  Project,
  ProjectDetail,
  PromptTemplate,
  QaFinding,
  ReleaseAction,
  RoadmapPhase,
  ScanMode,
  ScanReport,
  TerminalPlannerGroup
} from "../shared/types";
import {
  getHealth,
  getProject,
  getProjects,
  getRelease,
  getRun,
  getSettings,
  listScans,
  renderPrompt,
  runReleaseAction,
  runScan,
  saveSettings,
  stopRun
} from "./api";

type ViewKey =
  | "projects"
  | "dashboard"
  | "roadmap"
  | "qa"
  | "prompts"
  | "release"
  | "terminal"
  | "assets"
  | "exports"
  | "settings";

const views: Array<{ key: ViewKey; label: string; icon: typeof Activity }> = [
  { key: "projects", label: "Projects", icon: Boxes },
  { key: "dashboard", label: "Dashboard", icon: Gauge },
  { key: "roadmap", label: "Roadmap", icon: Map },
  { key: "qa", label: "QA Scanner", icon: ShieldCheck },
  { key: "prompts", label: "Codex Prompts", icon: TerminalSquare },
  { key: "release", label: "Release Deck", icon: Rocket },
  { key: "terminal", label: "Terminal Planner", icon: Layers },
  { key: "assets", label: "Asset Prompts", icon: Image },
  { key: "exports", label: "Exports", icon: Download },
  { key: "settings", label: "Settings", icon: Settings }
];

const emptySettings: AppSettings = {
  echoRoot: "",
  modpackModsDir: "",
  pythonExecutable: "",
  runtimeLogMaxAgeMinutes: 180,
  defaultScanMode: "quick"
};

export function App(): JSX.Element {
  const [projects, setProjects] = useState<Project[]>([]);
  const [selectedSlug, setSelectedSlug] = useState("echo");
  const [detail, setDetail] = useState<ProjectDetail | null>(null);
  const [settings, setSettings] = useState<AppSettings>(emptySettings);
  const [scanHistory, setScanHistory] = useState<ScanReport[]>([]);
  const [runs, setRuns] = useState<CommandRun[]>([]);
  const [activeView, setActiveView] = useState<ViewKey>("projects");
  const [status, setStatus] = useState("Booting command center");
  const [error, setError] = useState<string | null>(null);
  const [activeRun, setActiveRun] = useState<CommandRun | null>(null);
  const [scanBusy, setScanBusy] = useState<ScanMode | null>(null);

  useEffect(() => {
    let cancelled = false;
    Promise.all([getHealth(), getProjects(), getSettings()])
      .then(([health, projectList, loadedSettings]) => {
        if (cancelled) return;
        setProjects(projectList);
        setSettings(loadedSettings);
        setStatus(`API online at ${health.service}`);
        if (!projectList.some((project) => project.slug === selectedSlug) && projectList[0]) {
          setSelectedSlug(projectList[0].slug);
        }
      })
      .catch((cause) => {
        if (!cancelled) setError(cause instanceof Error ? cause.message : String(cause));
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    void refreshAll();
  }, [selectedSlug]);

  useEffect(() => {
    if (!activeRun || activeRun.status !== "running") {
      return;
    }
    const timer = window.setInterval(() => {
      getRun(activeRun.id)
        .then((run) => {
          setActiveRun(run);
          setRuns((current) => mergeRun(current, run));
        })
        .catch((cause) => setError(cause instanceof Error ? cause.message : String(cause)));
    }, 1400);
    return () => window.clearInterval(timer);
  }, [activeRun]);

  const selectedProject = useMemo(
    () => projects.find((project) => project.slug === selectedSlug) ?? detail?.project ?? null,
    [projects, selectedSlug, detail]
  );

  async function refreshAll(): Promise<void> {
    try {
      const [projectDetail, scans, release, loadedSettings] = await Promise.all([
        getProject(selectedSlug),
        listScans(selectedSlug),
        getRelease(selectedSlug),
        getSettings()
      ]);
      setDetail(projectDetail);
      setScanHistory(scans);
      setRuns(release.runs);
      setSettings(loadedSettings);
      setError(null);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : String(cause));
    }
  }

  async function handleRunScan(mode: ScanMode): Promise<void> {
    setScanBusy(mode);
    try {
      const report = await runScan(selectedSlug, mode);
      setDetail((current) => (current ? { ...current, latestReport: report, project: { ...current.project, buildHealth: report.summary.buildHealth, criticalIssues: report.summary.criticalIssues, polishTasks: report.summary.polishTasks } } : current));
      await refreshAll();
    } finally {
      setScanBusy(null);
    }
  }

  async function handleRunAction(action: ReleaseAction, confirmed = false): Promise<void> {
    const run = await runReleaseAction(selectedSlug, action.commandId, confirmed);
    setActiveRun(run);
    setRuns((current) => mergeRun(current, run));
  }

  async function handleStopRun(runId: string): Promise<void> {
    const run = await stopRun(runId);
    setActiveRun(run);
    setRuns((current) => mergeRun(current, run));
  }

  async function handleSaveSettings(next: AppSettings): Promise<void> {
    const saved = await saveSettings(next);
    setSettings(saved);
    await refreshAll();
  }

  return (
    <div className="min-h-screen bg-deck-950 text-slate-100">
      <div className="screen-grid" />
      <div className="relative mx-auto flex min-h-screen max-w-[1600px] gap-4 px-4 py-4">
        <aside className="hidden w-64 shrink-0 lg:block">
          <div className="sticky top-4 space-y-4">
            <BrandPanel status={status} />
            <Nav activeView={activeView} setActiveView={setActiveView} />
          </div>
        </aside>

        <main className="flex min-w-0 flex-1 flex-col gap-4">
          <TopBar
            projects={projects}
            selectedSlug={selectedSlug}
            setSelectedSlug={setSelectedSlug}
            activeView={activeView}
            setActiveView={setActiveView}
          />
          {error ? <Alert message={error} /> : null}
          {!detail || !selectedProject ? (
            <LoadingPanel />
          ) : (
            <View
              view={activeView}
              detail={detail}
              projects={projects}
              selectedSlug={selectedSlug}
              setSelectedSlug={setSelectedSlug}
              setActiveView={setActiveView}
              onRunScan={handleRunScan}
              onRunAction={handleRunAction}
              onStopRun={handleStopRun}
              activeRun={activeRun}
              runs={runs}
              scans={scanHistory}
              scanBusy={scanBusy}
              settings={settings}
              onSaveSettings={handleSaveSettings}
            />
          )}
        </main>
      </div>
    </div>
  );
}

function BrandPanel({ status }: { status: string }): JSX.Element {
  return (
    <section className="surface p-4">
      <div className="flex items-center gap-3">
        <div className="grid h-10 w-10 place-items-center rounded-lg border border-signal-cyan/40 bg-signal-cyan/10">
          <TerminalSquare className="h-5 w-5 text-signal-cyan" />
        </div>
        <div className="min-w-0">
          <h1 className="truncate text-sm font-semibold uppercase tracking-[0.18em] text-slate-100">Noxhack</h1>
          <p className="truncate text-xs text-slate-400">Command Center</p>
        </div>
      </div>
      <div className="mt-4 flex items-center gap-2 text-xs text-slate-400">
        <span className="h-2 w-2 rounded-full bg-signal-green shadow-[0_0_12px_rgba(126,231,135,0.8)]" />
        <span className="truncate">{status}</span>
      </div>
    </section>
  );
}

function Nav({ activeView, setActiveView }: { activeView: ViewKey; setActiveView: (view: ViewKey) => void }): JSX.Element {
  return (
    <nav className="surface p-2">
      {views.map((view) => {
        const Icon = view.icon;
        const selected = activeView === view.key;
        return (
          <button key={view.key} className={`nav-button ${selected ? "nav-button-active" : ""}`} title={view.label} onClick={() => setActiveView(view.key)}>
            <Icon className="h-4 w-4 shrink-0" />
            <span className="truncate">{view.label}</span>
          </button>
        );
      })}
    </nav>
  );
}

function TopBar({
  projects,
  selectedSlug,
  setSelectedSlug,
  activeView,
  setActiveView
}: {
  projects: Project[];
  selectedSlug: string;
  setSelectedSlug: (slug: string) => void;
  activeView: ViewKey;
  setActiveView: (view: ViewKey) => void;
}): JSX.Element {
  return (
    <header className="surface flex flex-col gap-3 p-3 md:flex-row md:items-center md:justify-between">
      <div className="min-w-0">
        <p className="text-xs uppercase tracking-[0.18em] text-signal-cyan">Local Release Ops</p>
        <h2 className="truncate text-xl font-semibold text-white">Noxhack Modpack Command Center</h2>
      </div>
      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        <select className="control min-w-52" value={selectedSlug} onChange={(event) => setSelectedSlug(event.target.value)}>
          {projects.map((project) => (
            <option key={project.slug} value={project.slug}>
              {project.name}
            </option>
          ))}
        </select>
        <select className="control lg:hidden" value={activeView} onChange={(event) => setActiveView(event.target.value as ViewKey)}>
          {views.map((view) => (
            <option key={view.key} value={view.key}>
              {view.label}
            </option>
          ))}
        </select>
      </div>
    </header>
  );
}

function View({
  view,
  detail,
  projects,
  selectedSlug,
  setSelectedSlug,
  setActiveView,
  onRunScan,
  onRunAction,
  onStopRun,
  activeRun,
  runs,
  scans,
  scanBusy,
  settings,
  onSaveSettings
}: {
  view: ViewKey;
  detail: ProjectDetail;
  projects: Project[];
  selectedSlug: string;
  setSelectedSlug: (slug: string) => void;
  setActiveView: (view: ViewKey) => void;
  onRunScan: (mode: ScanMode) => Promise<void>;
  onRunAction: (action: ReleaseAction, confirmed?: boolean) => Promise<void>;
  onStopRun: (runId: string) => Promise<void>;
  activeRun: CommandRun | null;
  runs: CommandRun[];
  scans: ScanReport[];
  scanBusy: ScanMode | null;
  settings: AppSettings;
  onSaveSettings: (settings: AppSettings) => Promise<void>;
}): JSX.Element {
  if (view === "projects") {
    return <ProjectsView projects={projects} selectedSlug={selectedSlug} setSelectedSlug={setSelectedSlug} setActiveView={setActiveView} />;
  }
  if (view === "roadmap") return <RoadmapView phases={detail.roadmap} />;
  if (view === "qa") return <QaView detail={detail} scans={scans} onRunScan={onRunScan} scanBusy={scanBusy} />;
  if (view === "prompts") return <PromptsView detail={detail} category="Codex QA" />;
  if (view === "release") {
    return <ReleaseView detail={detail} onRunAction={onRunAction} onStopRun={onStopRun} activeRun={activeRun} runs={runs} settings={settings} />;
  }
  if (view === "terminal") return <TerminalPlannerView groups={detail.terminalPlanner} />;
  if (view === "assets") return <PromptsView detail={detail} category="Asset Prompt" />;
  if (view === "exports") return <ExportsView detail={detail} />;
  if (view === "settings") return <SettingsView settings={settings} onSave={onSaveSettings} />;
  return <DashboardView detail={detail} scans={scans} runs={runs} onRunScan={onRunScan} scanBusy={scanBusy} setActiveView={setActiveView} />;
}

function ProjectsView({
  projects,
  selectedSlug,
  setSelectedSlug,
  setActiveView
}: {
  projects: Project[];
  selectedSlug: string;
  setSelectedSlug: (slug: string) => void;
  setActiveView: (view: ViewKey) => void;
}): JSX.Element {
  return (
    <section className="grid gap-4 xl:grid-cols-2">
      {projects.map((project) => (
        <article key={project.slug} className={`surface p-5 ${project.slug === selectedSlug ? "ring-1 ring-signal-cyan/50" : ""}`}>
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="text-xs uppercase tracking-[0.18em] text-slate-400">{project.kind}</p>
              <h3 className="mt-1 truncate text-2xl font-semibold text-white">{project.name}</h3>
            </div>
            <span className="status-pill" style={{ borderColor: project.accent, color: project.accent }}>
              {project.status}
            </span>
          </div>
          <p className="mt-3 line-clamp-2 text-sm text-slate-300">{project.description}</p>
          <div className="mt-5 grid gap-3 sm:grid-cols-4">
            <Metric label="Milestone" value={project.currentMilestone} />
            <Metric label="Build Health" value={`${project.buildHealth}%`} />
            <Metric label="Critical" value={project.criticalIssues} tone="red" />
            <Metric label="Polish" value={project.polishTasks} tone="amber" />
          </div>
          <div className="mt-5 flex flex-col gap-3 border-t border-deck-line pt-4 sm:flex-row sm:items-center sm:justify-between">
            <p className="min-w-0 text-sm text-slate-300">
              <span className="text-slate-500">Next:</span> {project.nextRecommendedAction}
            </p>
            <button
              className="primary-button"
              onClick={() => {
                setSelectedSlug(project.slug);
                setActiveView("dashboard");
              }}
            >
              <Gauge className="h-4 w-4" />
              Open
            </button>
          </div>
        </article>
      ))}
    </section>
  );
}

function DashboardView({
  detail,
  scans,
  runs,
  onRunScan,
  scanBusy,
  setActiveView
}: {
  detail: ProjectDetail;
  scans: ScanReport[];
  runs: CommandRun[];
  onRunScan: (mode: ScanMode) => Promise<void>;
  scanBusy: ScanMode | null;
  setActiveView: (view: ViewKey) => void;
}): JSX.Element {
  const latest = detail.latestReport;
  const failedRuns = runs.filter((run) => run.status === "failed").length;
  return (
    <div className="space-y-4">
      <section className="surface p-5">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div className="min-w-0">
            <p className="text-xs uppercase tracking-[0.18em] text-signal-cyan">{detail.project.kind}</p>
            <h2 className="mt-1 text-3xl font-semibold text-white">{detail.project.name}</h2>
            <p className="mt-3 max-w-4xl text-sm leading-6 text-slate-300">{detail.project.description}</p>
          </div>
          <button className="primary-button" disabled={Boolean(scanBusy)} onClick={() => onRunScan("quick")}>
            <RefreshCw className={`h-4 w-4 ${scanBusy ? "animate-spin" : ""}`} />
            Quick Scan
          </button>
        </div>
        <div className="mt-6 grid gap-3 md:grid-cols-4">
          <Metric label="Readiness" value={`${latest?.summary.readinessScore ?? detail.project.buildHealth}%`} tone="green" />
          <Metric label="Scan Status" value={latest?.status ?? "No scan"} />
          <Metric label="Critical Issues" value={latest?.summary.criticalIssues ?? detail.project.criticalIssues} tone="red" />
          <Metric label="Failed Runs" value={failedRuns} tone={failedRuns ? "red" : "green"} />
        </div>
      </section>

      <section className="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
        <div className="surface p-5">
          <SectionTitle icon={ListChecks} title="Recommended Actions" />
          <div className="mt-4 grid gap-3 md:grid-cols-2">
            <ActionTile label="Open QA findings" detail={detail.project.nextRecommendedAction} onClick={() => setActiveView("qa")} />
            <ActionTile label="Build release stack" detail="Confirm and run allowlisted release commands." onClick={() => setActiveView("release")} />
            <ActionTile label="Review settings" detail="Check Python and mods folder paths before deep scans." onClick={() => setActiveView("settings")} />
            <ActionTile label="Export report" detail={`${scans.length} scan report(s), ${runs.length} release run(s).`} onClick={() => setActiveView("exports")} />
          </div>
        </div>
        <div className="surface p-5">
          <SectionTitle icon={Database} title="Workspace Modules" />
          <div className="mt-4 space-y-2">
            {detail.project.modules.map((module) => (
              <div key={module.modId} className="module-row">
                <span className="truncate">{module.label}</span>
                <span className="font-mono text-xs text-slate-400">{module.version}</span>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
}

function RoadmapView({ phases }: { phases: RoadmapPhase[] }): JSX.Element {
  return (
    <section className="surface p-5">
      <SectionTitle icon={Map} title="Roadmap" />
      <div className="mt-5 space-y-3">
        {phases.map((phase, index) => (
          <div key={phase.title} className="roadmap-row">
            <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border border-deck-line bg-deck-900 font-mono text-xs text-signal-cyan">
              {String(index + 1).padStart(2, "0")}
            </div>
            <div className="min-w-0 flex-1">
              <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                <h3 className="truncate text-sm font-semibold text-white">{phase.title}</h3>
                <span className="text-xs uppercase tracking-[0.14em] text-slate-400">{phase.status}</span>
              </div>
              <p className="mt-1 text-sm text-slate-400">{phase.summary}</p>
              <div className="mt-3 h-2 overflow-hidden rounded-full bg-deck-900">
                <div className="h-full bg-signal-cyan" style={{ width: `${phase.progress}%` }} />
              </div>
            </div>
            <span className="w-12 text-right font-mono text-sm text-slate-300">{phase.progress}%</span>
          </div>
        ))}
      </div>
    </section>
  );
}

function QaView({
  detail,
  scans,
  onRunScan,
  scanBusy
}: {
  detail: ProjectDetail;
  scans: ScanReport[];
  onRunScan: (mode: ScanMode) => Promise<void>;
  scanBusy: ScanMode | null;
}): JSX.Element {
  const findings = detail.latestReport?.findings ?? [];
  const inventory = detail.latestReport?.summary.inventory ?? {};
  const [sourceFilter, setSourceFilter] = useState("all");
  const filteredFindings = sourceFilter === "all" ? findings : findings.filter((finding) => (finding.source ?? "quick") === sourceFilter);
  const sources = ["all", ...Array.from(new Set(findings.map((finding) => finding.source ?? "quick")))];
  return (
    <div className="space-y-4">
      <section className="surface flex flex-col gap-4 p-5 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <SectionTitle icon={ShieldCheck} title="QA Scanner" />
          <p className="mt-2 max-w-3xl text-sm text-slate-400">
            Quick scans inspect local resources and logs. Deep scans also run ECHO Python validators and parse their output.
          </p>
        </div>
        <div className="flex flex-col gap-2 sm:flex-row">
          <button className="primary-button" disabled={Boolean(scanBusy)} onClick={() => onRunScan("quick")}>
            <RefreshCw className={`h-4 w-4 ${scanBusy === "quick" ? "animate-spin" : ""}`} />
            Quick Scan
          </button>
          <button className="primary-button" disabled={Boolean(scanBusy)} onClick={() => onRunScan("deep")}>
            <ShieldCheck className={`h-4 w-4 ${scanBusy === "deep" ? "animate-spin" : ""}`} />
            Deep Scan
          </button>
        </div>
      </section>

      {detail.latestReport ? (
        <section className="grid gap-3 md:grid-cols-5">
          <Metric label="Scan Status" value={`${detail.latestReport.mode} ${detail.latestReport.status}`} />
          <Metric label="Readiness" value={`${detail.latestReport.summary.readinessScore ?? detail.latestReport.summary.buildHealth}%`} tone="green" />
          <Metric label="Critical" value={detail.latestReport.summary.criticalIssues} tone="red" />
          <Metric label="Findings" value={detail.latestReport.findings.length} tone="amber" />
          <Metric label="Duration" value={`${Math.round(detail.latestReport.durationMs / 1000)}s`} />
        </section>
      ) : null}

      <section className="surface p-5">
        <SectionTitle icon={Database} title="Inventory" />
        <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
          {Object.entries(inventory).map(([key, value]) => (
            <Metric key={key} label={key} value={value} />
          ))}
        </div>
      </section>

      <section className="surface p-5">
        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <SectionTitle icon={Activity} title="Latest Findings" />
          <select className="control" value={sourceFilter} onChange={(event) => setSourceFilter(event.target.value)}>
            {sources.map((source) => (
              <option key={source} value={source}>
                {source}
              </option>
            ))}
          </select>
        </div>
        <Findings findings={filteredFindings} />
      </section>

      <section className="grid gap-4 xl:grid-cols-[0.7fr_1.3fr]">
        <div className="surface p-5">
          <SectionTitle icon={ListChecks} title="Scan History" />
          <div className="mt-4 space-y-2">
            {scans.length === 0 ? (
              <p className="text-sm text-slate-400">No scan reports yet.</p>
            ) : (
              scans.map((scan) => (
                <div key={scan.id} className="module-row">
                  <span className="truncate">#{scan.id} {scan.mode} {scan.status}</span>
                  <span className="font-mono text-xs text-slate-400">{scan.summary.buildHealth}%</span>
                </div>
              ))
            )}
          </div>
        </div>
        <div className="surface p-5">
          <SectionTitle icon={TerminalSquare} title="Raw Validator Output" />
          <pre className="mt-4 max-h-[360px] overflow-auto rounded-lg border border-deck-line bg-black/40 p-4 text-xs leading-5 text-slate-300">
            {detail.latestReport?.rawOutput || "No deep validator output yet."}
          </pre>
        </div>
      </section>
    </div>
  );
}

function Findings({ findings }: { findings: QaFinding[] }): JSX.Element {
  return (
    <div className="mt-4 space-y-3">
      {findings.length === 0 ? (
        <p className="text-sm text-slate-400">No findings for this filter.</p>
      ) : (
        findings.map((finding, index) => (
          <div key={`${finding.code}-${finding.path}-${finding.line}-${index}`} className="finding-row">
            <Severity severity={finding.severity} />
            <div className="min-w-0 flex-1">
              <div className="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
                <h3 className="truncate text-sm font-semibold text-white">{finding.title}</h3>
                <span className="font-mono text-xs text-slate-500">{finding.source ?? "quick"} {finding.code ?? ""}</span>
              </div>
              <p className="mt-1 text-sm leading-6 text-slate-400">{finding.detail}</p>
              {finding.path ? <p className="mt-2 font-mono text-xs text-slate-500">{finding.path}{finding.line ? `:${finding.line}` : ""}</p> : null}
            </div>
          </div>
        ))
      )}
    </div>
  );
}

function PromptsView({ detail, category }: { detail: ProjectDetail; category: "Codex QA" | "Asset Prompt" }): JSX.Element {
  const prompts = detail.prompts.filter((prompt) => prompt.category === category);
  const [selectedId, setSelectedId] = useState(prompts[0]?.id ?? "");
  const [rendered, setRendered] = useState<PromptTemplate | null>(prompts[0] ?? null);
  const selected = prompts.find((prompt) => prompt.id === selectedId) ?? prompts[0] ?? null;

  useEffect(() => {
    if (selected && selected.id !== rendered?.id) {
      setRendered(selected);
      setSelectedId(selected.id);
    }
  }, [selected?.id]);

  async function handleRender(): Promise<void> {
    if (!selected) return;
    setRendered(await renderPrompt(detail.project.slug, selected.id));
  }

  async function handleCopy(): Promise<void> {
    if (rendered) {
      await navigator.clipboard.writeText(rendered.body);
    }
  }

  return (
    <section className="grid gap-4 xl:grid-cols-[0.85fr_1.15fr]">
      <div className="surface p-5">
        <SectionTitle icon={category === "Codex QA" ? TerminalSquare : Image} title={category} />
        <div className="mt-4 space-y-2">
          {prompts.map((prompt) => (
            <button
              key={prompt.id}
              className={`prompt-row ${selectedId === prompt.id ? "prompt-row-active" : ""}`}
              onClick={() => {
                setSelectedId(prompt.id);
                setRendered(prompt);
              }}
            >
              <span className="truncate font-semibold">{prompt.title}</span>
              <span className="line-clamp-2 text-left text-xs text-slate-400">{prompt.description}</span>
            </button>
          ))}
        </div>
      </div>

      <div className="surface p-5">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <SectionTitle icon={Sparkles} title={rendered?.title ?? "Prompt"} />
          <div className="flex gap-2">
            <button className="icon-button" title="Render prompt" onClick={handleRender}>
              <RefreshCw className="h-4 w-4" />
            </button>
            <button className="icon-button" title="Copy prompt" onClick={handleCopy}>
              <Clipboard className="h-4 w-4" />
            </button>
          </div>
        </div>
        <pre className="prompt-output mt-4 whitespace-pre-wrap text-sm leading-6">{rendered?.body ?? "Select a prompt."}</pre>
      </div>
    </section>
  );
}

function ReleaseView({
  detail,
  onRunAction,
  onStopRun,
  activeRun,
  runs,
  settings
}: {
  detail: ProjectDetail;
  onRunAction: (action: ReleaseAction) => Promise<void>;
  onStopRun: (runId: string) => Promise<void>;
  activeRun: CommandRun | null;
  runs: CommandRun[];
  settings: AppSettings;
}): JSX.Element {
  const [pending, setPending] = useState<ReleaseAction | null>(null);
  const running = activeRun?.status === "running" ? activeRun : runs.find((run) => run.status === "running") ?? null;
  return (
    <div className="space-y-4">
      <section className="surface p-5">
        <SectionTitle icon={Rocket} title="Release Deck" />
        <p className="mt-2 max-w-4xl text-sm text-slate-400">
          Medium and high risk actions require confirmation. Commands remain allowlisted and include the configured mods folder where relevant.
        </p>
        <p className="mt-3 font-mono text-xs text-slate-500">Mods folder: {settings.modpackModsDir || "not configured"}</p>
      </section>

      {running ? (
        <section className="surface flex flex-col gap-3 border-signal-cyan/50 p-4 md:flex-row md:items-center md:justify-between">
          <div>
            <p className="text-sm font-semibold text-white">Running: {running.commandId}</p>
            <p className="font-mono text-xs text-slate-400">pid {running.pid ?? "pending"} / {running.command.join(" ")}</p>
          </div>
          <button className="danger-button" onClick={() => onStopRun(running.id)}>
            <Square className="h-4 w-4" />
            Stop
          </button>
        </section>
      ) : null}

      <section className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
        {detail.releaseActions.map((action) => (
          <article key={action.commandId} className="surface p-4">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <h3 className="truncate text-base font-semibold text-white">{action.label}</h3>
                <p className="mt-2 line-clamp-3 text-sm text-slate-400">{action.description}</p>
              </div>
              <Risk risk={action.risk} />
            </div>
            <code className="mt-4 block truncate rounded-lg border border-deck-line bg-deck-950 px-3 py-2 text-xs text-slate-400">
              {displayCommand(action, settings)}
            </code>
            <button className="mt-4 primary-button w-full justify-center" disabled={Boolean(running)} onClick={() => (action.risk === "low" ? onRunAction(action, false) : setPending(action))}>
              <Play className="h-4 w-4" />
              Run
            </button>
          </article>
        ))}
      </section>

      <section className="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
        <div className="surface p-5">
          <SectionTitle icon={TerminalSquare} title="Command Output" />
          <pre className="mt-4 max-h-[420px] overflow-auto rounded-lg border border-deck-line bg-black/40 p-4 text-xs leading-5 text-slate-300">
            {activeRun ? activeRun.output || `${activeRun.commandId} ${activeRun.status}` : "No command run selected."}
          </pre>
        </div>
        <div className="surface p-5">
          <SectionTitle icon={Activity} title="Run History" />
          <div className="mt-4 space-y-2">
            {runs.length === 0 ? (
              <p className="text-sm text-slate-400">No release runs yet.</p>
            ) : (
              runs.map((run) => (
                <div key={run.id} className="module-row">
                  <span className="truncate">{run.commandId}</span>
                  <span className={`font-mono text-xs ${run.status === "failed" ? "text-signal-red" : run.status === "succeeded" ? "text-signal-green" : "text-slate-400"}`}>{run.status}</span>
                </div>
              ))
            )}
          </div>
        </div>
      </section>

      {pending ? (
        <ConfirmModal
          action={pending}
          settings={settings}
          onCancel={() => setPending(null)}
          onConfirm={async () => {
            const action = pending;
            setPending(null);
            await onRunAction(action, true);
          }}
        />
      ) : null}
    </div>
  );
}

function ConfirmModal({
  action,
  settings,
  onCancel,
  onConfirm
}: {
  action: ReleaseAction;
  settings: AppSettings;
  onCancel: () => void;
  onConfirm: () => Promise<void>;
}): JSX.Element {
  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-black/70 p-4">
      <section className="surface max-w-2xl p-5 shadow-glow">
        <div className="flex items-center gap-3">
          <AlertTriangle className="h-5 w-5 text-signal-amber" />
          <h2 className="text-lg font-semibold text-white">Confirm {action.label}</h2>
        </div>
        <p className="mt-3 text-sm leading-6 text-slate-300">
          This is a {action.risk}-risk allowlisted action. It may build, verify, copy, or modify local ECHO release artifacts.
        </p>
        <code className="mt-4 block whitespace-pre-wrap rounded-lg border border-deck-line bg-deck-950 p-3 text-xs text-slate-300">{displayCommand(action, settings)}</code>
        <div className="mt-5 flex flex-col gap-2 sm:flex-row sm:justify-end">
          <button className="secondary-button" onClick={onCancel}>Cancel</button>
          <button className="primary-button justify-center" onClick={onConfirm}>
            <Play className="h-4 w-4" />
            Confirm Run
          </button>
        </div>
      </section>
    </div>
  );
}

function TerminalPlannerView({ groups }: { groups: TerminalPlannerGroup[] }): JSX.Element {
  return (
    <section className="surface p-5">
      <SectionTitle icon={Layers} title="Terminal Planner" />
      <div className="mt-5 grid gap-4 md:grid-cols-2 xl:grid-cols-5">
        {groups.map((group) => (
          <article key={group.group} className="planner-column">
            <h3 className="text-sm font-semibold uppercase tracking-[0.18em] text-signal-cyan">{group.group}</h3>
            <div className="mt-4 space-y-2">
              {group.pages.map((page) => (
                <div key={page} className="module-row">
                  <span className="truncate">{page}</span>
                </div>
              ))}
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

function ExportsView({ detail }: { detail: ProjectDetail }): JSX.Element {
  const base = `/api/projects/${detail.project.slug}/export`;
  return (
    <section className="surface p-5">
      <SectionTitle icon={Download} title="Exports" />
      <p className="mt-2 max-w-3xl text-sm text-slate-400">
        Export the current project snapshot, settings, latest QA report, scan history, release runs, roadmap, and prompt inventory.
      </p>
      <div className="mt-5 flex flex-col gap-3 sm:flex-row">
        <a className="primary-button" href={`${base}?format=markdown`} target="_blank" rel="noreferrer">
          <FileText className="h-4 w-4" />
          Markdown
        </a>
        <a className="primary-button" href={`${base}?format=json`} target="_blank" rel="noreferrer">
          <FileJson className="h-4 w-4" />
          JSON
        </a>
      </div>
    </section>
  );
}

function SettingsView({ settings, onSave }: { settings: AppSettings; onSave: (settings: AppSettings) => Promise<void> }): JSX.Element {
  const [draft, setDraft] = useState<AppSettings>(settings);
  const [saving, setSaving] = useState(false);

  useEffect(() => setDraft(settings), [settings]);

  async function handleSave(): Promise<void> {
    setSaving(true);
    try {
      await onSave(draft);
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="surface p-5">
      <SectionTitle icon={Settings} title="Settings" />
      <div className="mt-5 grid gap-4">
        <Field label="ECHO Root" value={draft.echoRoot} onChange={(value) => setDraft({ ...draft, echoRoot: value })} />
        <Field label="Modpack Mods Folder" value={draft.modpackModsDir} onChange={(value) => setDraft({ ...draft, modpackModsDir: value })} />
        <Field label="Python Executable" value={draft.pythonExecutable} onChange={(value) => setDraft({ ...draft, pythonExecutable: value })} />
        <label className="grid gap-2">
          <span className="text-xs uppercase tracking-[0.14em] text-slate-500">Runtime Log Max Age Minutes</span>
          <input className="control" type="number" min={1} value={draft.runtimeLogMaxAgeMinutes} onChange={(event) => setDraft({ ...draft, runtimeLogMaxAgeMinutes: Number(event.target.value) })} />
        </label>
        <label className="grid gap-2">
          <span className="text-xs uppercase tracking-[0.14em] text-slate-500">Default Scan Mode</span>
          <select className="control" value={draft.defaultScanMode} onChange={(event) => setDraft({ ...draft, defaultScanMode: event.target.value as ScanMode })}>
            <option value="quick">quick</option>
            <option value="deep">deep</option>
          </select>
        </label>
      </div>
      <button className="primary-button mt-5" disabled={saving} onClick={handleSave}>
        <Settings className="h-4 w-4" />
        {saving ? "Saving" : "Save Settings"}
      </button>
    </section>
  );
}

function Field({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }): JSX.Element {
  return (
    <label className="grid gap-2">
      <span className="text-xs uppercase tracking-[0.14em] text-slate-500">{label}</span>
      <input className="control" value={value} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function ActionTile({ label, detail, onClick }: { label: string; detail: string; onClick: () => void }): JSX.Element {
  return (
    <button className="action-tile" onClick={onClick}>
      <span className="truncate text-sm font-semibold text-white">{label}</span>
      <span className="line-clamp-2 text-left text-sm text-slate-400">{detail}</span>
    </button>
  );
}

function Metric({ label, value, tone }: { label: string; value: string | number; tone?: "green" | "amber" | "red" }): JSX.Element {
  const toneClass = tone === "green" ? "text-signal-green" : tone === "amber" ? "text-signal-amber" : tone === "red" ? "text-signal-red" : "text-white";
  return (
    <div className="surface min-h-24 p-4">
      <p className="truncate text-xs uppercase tracking-[0.14em] text-slate-500">{label}</p>
      <p className={`mt-3 break-words text-2xl font-semibold ${toneClass}`}>{value}</p>
    </div>
  );
}

function SectionTitle({ icon: Icon, title }: { icon: typeof Activity; title: string }): JSX.Element {
  return (
    <div className="flex min-w-0 items-center gap-2">
      <Icon className="h-5 w-5 shrink-0 text-signal-cyan" />
      <h2 className="truncate text-lg font-semibold text-white">{title}</h2>
    </div>
  );
}

function Severity({ severity }: { severity: QaFinding["severity"] }): JSX.Element {
  const className =
    severity === "critical"
      ? "border-signal-red/50 text-signal-red"
      : severity === "high"
        ? "border-signal-amber/50 text-signal-amber"
        : severity === "medium"
          ? "border-signal-violet/50 text-signal-violet"
          : "border-signal-cyan/40 text-signal-cyan";
  return <span className={`status-pill ${className}`}>{severity}</span>;
}

function Risk({ risk }: { risk: ReleaseAction["risk"] }): JSX.Element {
  const className =
    risk === "high" ? "border-signal-red/50 text-signal-red" : risk === "medium" ? "border-signal-amber/50 text-signal-amber" : "border-signal-green/50 text-signal-green";
  return <span className={`status-pill ${className}`}>{risk}</span>;
}

function LoadingPanel(): JSX.Element {
  return (
    <section className="surface grid min-h-[360px] place-items-center p-8">
      <div className="flex items-center gap-3 text-slate-400">
        <RefreshCw className="h-5 w-5 animate-spin text-signal-cyan" />
        Loading command center data
      </div>
    </section>
  );
}

function Alert({ message }: { message: string }): JSX.Element {
  return <div className="rounded-lg border border-signal-red/40 bg-signal-red/10 px-4 py-3 text-sm text-signal-red">{message}</div>;
}

function displayCommand(action: ReleaseAction, settings: AppSettings): string {
  if (action.mode !== "shell") {
    return action.mode;
  }
  const args = [...action.args];
  if (settings.modpackModsDir && ["verify-release", "check-jar-set", "copy-jars"].includes(action.commandId)) {
    args.push(`-PechoModpackModsDir=${settings.modpackModsDir}`);
  }
  return [action.executable, ...args].join(" ");
}

function mergeRun(runs: CommandRun[], run: CommandRun): CommandRun[] {
  return [run, ...runs.filter((candidate) => candidate.id !== run.id)].slice(0, 25);
}
