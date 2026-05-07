export type Status = "pass" | "warn" | "fail" | "unknown" | "running";
export type LoreSeverity = "critical" | "warn" | "info";
export type CompletionStatus = "implemented" | "partial" | "planned" | "blocked" | "deferred";

export interface ModuleInfo {
  name: string;
  gradlePath: string;
  projectDir: string;
  modId: string;
  modName: string;
  version: string;
  role: "core" | "main" | "addon";
}

export interface WorkspaceInfo {
  repoRoot: string;
  remote: string;
  dirty: boolean;
  dirtyFiles: string[];
  modules: ModuleInfo[];
  minecraftVersion: string;
  neoForgeVersion: string;
  javaTarget: string;
}

export interface ArtifactInfo {
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

export interface CheckInfo {
  id: string;
  label: string;
  commandId?: string;
  status: Status;
  detail: string;
}

export interface CommandSpec {
  id: string;
  label: string;
  commandLine: string;
  description: string;
}

export interface CommandRun {
  id: string;
  commandId: string;
  label: string;
  status: Status;
  startedAt: string;
  endedAt?: string;
  exitCode?: number;
  logs: string[];
}

export interface QaMission {
  id: string;
  title: string;
  track: string;
  status: "todo" | "pass" | "fail" | "blocked";
  notes: string;
  updatedAt?: string;
}

export interface DraftResponse {
  markdown: string;
  manifest: string;
}

export interface CrashDecode {
  metadata: Record<string, string>;
  suspectedModules: string[];
  topFrames: string[];
  causes: string[];
  nextActions: string[];
}

export interface LoreFinding {
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

export interface LoreReport {
  score: number;
  scannedFiles: string[];
  scannedFileCount: number;
  counts: Record<LoreSeverity, number>;
  ruleCategories: string[];
  findings: LoreFinding[];
}

export interface CompletionFeature {
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

export interface CompletionReport {
  percent: number;
  counts: Record<CompletionStatus, number>;
  tracks: string[];
  modules: string[];
  features: CompletionFeature[];
}
