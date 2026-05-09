import type { AppSettings, CommandRun, ProjectDetail, ScanReport } from "../shared/types.js";

export interface ExportContext {
  detail: ProjectDetail;
  settings: AppSettings;
  scans: ScanReport[];
  runs: CommandRun[];
}

export function exportJson(context: ExportContext): string {
  const { detail, settings, scans, runs } = context;
  const latestScan = detail.latestReport;
  return JSON.stringify(
    {
      generatedAt: new Date().toISOString(),
      project: detail.project,
      settings,
      readinessScore: latestScan?.summary.readinessScore ?? detail.project.buildHealth,
      latestScan,
      recentScans: scans,
      releaseRuns: runs,
      roadmap: detail.roadmap,
      terminalPlanner: detail.terminalPlanner,
      promptTemplates: detail.prompts,
      releaseActions: detail.releaseActions
    },
    null,
    2
  );
}

export function exportMarkdown(context: ExportContext): string {
  const { detail, settings, scans, runs } = context;
  const report = detail.latestReport;
  const lines = [
    `# ${detail.project.name} Command Center Report`,
    "",
    `Status: ${detail.project.status}`,
    `Milestone: ${detail.project.currentMilestone}`,
    `Build Health: ${detail.project.buildHealth}%`,
    `Next Action: ${detail.project.nextRecommendedAction}`,
    `Readiness Score: ${report?.summary.readinessScore ?? detail.project.buildHealth}%`,
    "",
    "## Settings Snapshot",
    `- ECHO Root: ${settings.echoRoot}`,
    `- Mods Folder: ${settings.modpackModsDir || "not configured"}`,
    `- Python Executable: ${settings.pythonExecutable}`,
    `- Runtime Log Age: ${settings.runtimeLogMaxAgeMinutes} minute(s)`,
    `- Default Scan Mode: ${settings.defaultScanMode}`,
    "",
    "## Modules",
    ...detail.project.modules.map((module) => `- ${module.label} (${module.modId}) ${module.version}`),
    "",
    "## Latest QA",
    ...markdownReport(report),
    "",
    "## Recent Scans",
    ...(scans.length
      ? scans.map((scan) => `- #${scan.id} ${scan.mode} ${scan.status}: ${scan.summary.buildHealth}% health, ${scan.findings.length} finding(s)`)
      : ["No scan reports yet."]),
    "",
    "## Release Run History",
    ...(runs.length
      ? runs.map((run) => `- ${run.commandId}: ${run.status}${run.exitCode == null ? "" : ` (exit ${run.exitCode})`}${run.durationMs == null ? "" : `, ${run.durationMs}ms`}`)
      : ["No release runs yet."]),
    "",
    "## Roadmap",
    ...detail.roadmap.map((phase) => `- ${phase.title}: ${phase.status} (${phase.progress}%) - ${phase.summary}`),
    "",
    "## Codex Prompts",
    ...detail.prompts
      .filter((prompt) => prompt.category === "Codex QA")
      .map((prompt) => `- ${prompt.title}: ${prompt.description}`),
    "",
    "## Release Actions",
    ...detail.releaseActions.map((action) => `- ${action.label}: ${action.description}`)
  ];
  return `${lines.join("\n")}\n`;
}

function markdownReport(report: ScanReport | null): string[] {
  if (!report) {
    return ["No scan report yet."];
  }
  return [
    `Scan: #${report.id} ${report.mode} ${report.status} at ${report.finishedAt ?? report.createdAt}`,
    `Summary: ${report.summary.status}, ${report.summary.buildHealth}% health, ${report.summary.criticalIssues} critical issue(s), ${report.summary.polishTasks} polish task(s).`,
    ...report.findings.map((finding) => {
      const location = finding.path ? ` (${finding.path}${finding.line ? `:${finding.line}` : ""})` : "";
      return `- [${finding.severity}] ${finding.code ?? finding.title}: ${finding.detail}${location}`;
    }),
    report.rawOutput ? "\n### Raw Validator Output\n\n```text\n" + report.rawOutput.slice(0, 12000) + "\n```" : ""
  ].filter(Boolean);
}
