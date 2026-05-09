export type CommandMode = "shell" | "quarantine" | "notes";
export type CommandRunStatus = "queued" | "running" | "succeeded" | "failed" | "rejected" | "stopped";
export type ExportFormat = "json" | "markdown";
export type ScanMode = "quick" | "deep";
export type ScanStatus = "running" | "passed" | "warning" | "failed";

export interface ProjectModule {
  modId: string;
  label: string;
  version: string;
  path: string;
}

export interface Project {
  slug: string;
  name: string;
  kind: string;
  status: string;
  currentMilestone: string;
  buildHealth: number;
  criticalIssues: number;
  polishTasks: number;
  lastScanLabel: string;
  nextRecommendedAction: string;
  accent: string;
  description: string;
  workspacePath: string;
  modules: ProjectModule[];
}

export interface RoadmapPhase {
  projectSlug: string;
  title: string;
  status: string;
  progress: number;
  summary: string;
}

export interface QaTrack {
  projectSlug: string;
  key: string;
  title: string;
  severity: "critical" | "high" | "medium" | "low";
  status: string;
  summary: string;
  checks: string[];
}

export interface QaFinding {
  id?: number;
  track: string;
  title: string;
  severity: QaTrack["severity"];
  status: string;
  detail: string;
  path?: string;
  line?: number;
  code?: string;
  source?: string;
  metadata?: Record<string, unknown>;
}

export interface ScanReport {
  id: number;
  projectSlug: string;
  createdAt: string;
  mode: ScanMode;
  status: ScanStatus;
  startedAt: string;
  finishedAt?: string;
  durationMs: number;
  source: Record<string, unknown>;
  rawOutput: string;
  summary: {
    status: string;
    buildHealth: number;
    criticalIssues: number;
    polishTasks: number;
    inventory: Record<string, number>;
    readinessScore?: number;
  };
  findings: QaFinding[];
}

export interface PromptTemplate {
  projectSlug: string;
  id: string;
  category: string;
  title: string;
  description: string;
  body: string;
}

export interface TerminalPlannerGroup {
  projectSlug: string;
  group: string;
  pages: string[];
}

export interface ReleaseAction {
  projectSlug: string;
  commandId: string;
  label: string;
  description: string;
  mode: CommandMode;
  risk: "low" | "medium" | "high";
  executable: string;
  args: string[];
}

export interface CommandRun {
  id: string;
  projectSlug: string;
  commandId: string;
  status: CommandRunStatus;
  risk: ReleaseAction["risk"];
  command: string[];
  startedAt: string;
  finishedAt?: string;
  exitCode?: number;
  pid?: number;
  durationMs?: number;
  metadata?: Record<string, unknown>;
  output: string;
}

export interface AppSettings {
  echoRoot: string;
  modpackModsDir: string;
  pythonExecutable: string;
  runtimeLogMaxAgeMinutes: number;
  defaultScanMode: ScanMode;
}

export interface ProjectDetail {
  project: Project;
  roadmap: RoadmapPhase[];
  qaTracks: QaTrack[];
  prompts: PromptTemplate[];
  terminalPlanner: TerminalPlannerGroup[];
  releaseActions: ReleaseAction[];
  latestReport: ScanReport | null;
  recentRuns?: CommandRun[];
}
