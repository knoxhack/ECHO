export type CommandMode = "shell" | "quarantine" | "notes";
export type CommandRunStatus = "queued" | "running" | "succeeded" | "failed" | "rejected" | "stopped";
export type ExportFormat = "json" | "markdown";
export type ScanMode = "quick" | "deep";
export type ScanStatus = "running" | "passed" | "warning" | "failed";
export type JarArtifactStatus = "missing" | "built" | "current" | "stale";
export type JarTargetStatus = "missing" | "current" | "stale" | "duplicate" | "foreign";
export type ReadinessItemStatus = "done" | "missing" | "blocked" | "warning";
export type FeatureStatus = "implemented" | "partial" | "planned" | "deferred" | "blocked";
export type ModpackStatus = "ready" | "missing" | "blocked" | "running" | "succeeded" | "failed";

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

export interface FeatureSource {
  label: string;
  path: string;
  section: string;
}

export interface FeatureEvidence {
  kind: string;
  label: string;
  path?: string;
  detail?: string;
}

export interface FeatureRecord {
  projectSlug: string;
  id: string;
  title: string;
  category: string;
  status: FeatureStatus;
  playerPromise: string;
  loreContext: string;
  implementationSummary: string;
  nextAction: string;
  sources: FeatureSource[];
  evidence: FeatureEvidence[];
  order: number;
}

export interface FeatureCatalogSummary {
  total: number;
  statusCounts: Record<FeatureStatus, number>;
  categoryCounts: Record<string, number>;
}

export interface FeatureCatalogResponse {
  projectSlug: string;
  generatedAt: string;
  features: FeatureRecord[];
  summary: FeatureCatalogSummary;
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

export interface JarArtifact {
  moduleId: string;
  label: string;
  version: string;
  expectedFileName: string;
  sourcePath: string;
  targetPath?: string;
  exists: boolean;
  current: boolean;
  status: JarArtifactStatus;
  size?: number;
  modifiedAt?: string;
  checksum?: string;
}

export interface JarTargetEntry {
  moduleId?: string;
  version?: string;
  expectedFileName?: string;
  fileName: string;
  path: string;
  status: JarTargetStatus;
  size: number;
  modifiedAt: string;
  checksum?: string;
}

export interface JarManifest {
  projectSlug: string;
  generatedAt: string;
  buildRoot: string;
  targetDir: string;
  targetConfigured: boolean;
  targetExists: boolean;
  quarantineDir: string;
  artifacts: JarArtifact[];
  targetEntries: JarTargetEntry[];
  blockers: string[];
  summary: {
    expected: number;
    built: number;
    missing: number;
    current: number;
    stale: number;
    duplicate: number;
    foreign: number;
  };
}

export interface JarPipelineRequest {
  confirmed?: boolean;
}

export interface JarPipelineResult {
  manifest: JarManifest;
  run: CommandRun;
  scanReport?: ScanReport;
  moved: string[];
  copied: string[];
  verified: string[];
}

export interface ModpackTarget {
  projectSlug: string;
  projectName: string;
  buildCommandId: string;
  status: ModpackStatus;
  manifest: JarManifest;
  blockers: string[];
}

export interface ModpackInventory {
  generatedAt: string;
  targetDir: string;
  targetConfigured: boolean;
  targetExists: boolean;
  status: ModpackStatus;
  blockers: string[];
  targets: ModpackTarget[];
  summary: {
    projects: number;
    expected: number;
    built: number;
    missing: number;
    current: number;
    stale: number;
    duplicate: number;
  };
  latestRun?: ModpackPipelineRun | null;
}

export interface ModpackPipelineStep {
  id: string;
  label: string;
  status: ModpackStatus;
  detail: string;
  command?: string[];
  startedAt?: string;
  finishedAt?: string;
  output?: string;
}

export interface ModpackPipelineRun {
  id: string;
  status: ModpackStatus;
  startedAt: string;
  finishedAt?: string;
  durationMs?: number;
  targetSlugs: string[];
  steps: ModpackPipelineStep[];
  output: string;
}

export interface ModpackPipelineRequest {
  confirmed?: boolean;
}

export interface ModpackPipelineResult {
  run: ModpackPipelineRun;
  summary: ModpackInventory;
  scanReports: ScanReport[];
  moved: string[];
  copied: string[];
  verified: string[];
}

export interface ReadinessChecklistItem {
  id: string;
  category: "Scan" | "Jars" | "Settings";
  label: string;
  status: ReadinessItemStatus;
  detail: string;
  actionLabel: string;
  targetView: string;
  relatedFindingCodes: string[];
  commandId?: string;
}

export interface ReadinessReport {
  projectSlug: string;
  generatedAt: string;
  score: number;
  latestQuickScanId?: number;
  latestDeepScanId?: number;
  nextAction: ReadinessChecklistItem | null;
  counts: {
    done: number;
    missing: number;
    blocked: number;
    warning: number;
    total: number;
  };
  items: ReadinessChecklistItem[];
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
