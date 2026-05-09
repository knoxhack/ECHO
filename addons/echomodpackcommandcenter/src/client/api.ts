import type {
  AppSettings,
  CommandRun,
  Project,
  ProjectDetail,
  PromptTemplate,
  ReleaseAction,
  ScanMode,
  ScanReport
} from "../shared/types";

const API_ROOT = "/api";

export async function getHealth(): Promise<Record<string, string | boolean>> {
  return request<Record<string, string | boolean>>("/health");
}

export async function getSettings(): Promise<AppSettings> {
  return request<AppSettings>("/settings");
}

export async function saveSettings(settings: Partial<AppSettings>): Promise<AppSettings> {
  return request<AppSettings>("/settings", {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(settings)
  });
}

export async function getProjects(): Promise<Project[]> {
  const data = await request<{ projects: Project[] }>("/projects");
  return data.projects;
}

export async function getProject(slug: string): Promise<ProjectDetail> {
  return request<ProjectDetail>(`/projects/${slug}`);
}

export async function runScan(slug: string, mode: ScanMode): Promise<ScanReport> {
  return request<ScanReport>(`/projects/${slug}/scan`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ mode })
  });
}

export async function listScans(slug: string): Promise<ScanReport[]> {
  const data = await request<{ reports: ScanReport[] }>(`/projects/${slug}/scans`);
  return data.reports;
}

export async function getRelease(slug: string): Promise<{
  actions: ReleaseAction[];
  modpackModsDir: string | null;
  runs: CommandRun[];
}> {
  return request<{ actions: ReleaseAction[]; modpackModsDir: string | null; runs: CommandRun[] }>(`/projects/${slug}/release`);
}

export async function listRuns(slug: string): Promise<CommandRun[]> {
  const data = await request<{ runs: CommandRun[] }>(`/projects/${slug}/runs`);
  return data.runs;
}

export async function runReleaseAction(slug: string, commandId: string, confirmed = false): Promise<CommandRun> {
  return request<CommandRun>(`/projects/${slug}/release/${commandId}/run`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ confirmed })
  });
}

export async function stopRun(runId: string): Promise<CommandRun> {
  return request<CommandRun>(`/runs/${runId}/stop`, { method: "POST" });
}

export async function getRun(runId: string): Promise<CommandRun> {
  return request<CommandRun>(`/runs/${runId}`);
}

export async function renderPrompt(slug: string, promptId: string): Promise<PromptTemplate> {
  const data = await request<{ prompt: PromptTemplate }>(`/projects/${slug}/prompts/render`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ promptId })
  });
  return data.prompt;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_ROOT}${path}`, init);
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `${response.status} ${response.statusText}`);
  }
  return (await response.json()) as T;
}
