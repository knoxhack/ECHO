import path from "node:path";
import type { AppSettings, Project } from "../shared/types.js";
import { ECHO_ROOT } from "./paths.js";

export function projectWorkspaceRoot(project: Project, settings: AppSettings): string {
  const configuredEchoRoot = path.resolve(settings.echoRoot || ECHO_ROOT);
  const workspacePath = project.workspacePath.trim();
  if (isSeededEchoWorkspace(workspacePath)) {
    return configuredEchoRoot;
  }
  if (workspacePath && path.isAbsolute(workspacePath)) {
    return path.resolve(workspacePath);
  }
  if (workspacePath) {
    return path.resolve(configuredEchoRoot, workspacePath);
  }
  return configuredEchoRoot;
}

function isSeededEchoWorkspace(workspacePath: string): boolean {
  const normalizedWorkspace = workspacePath.replaceAll("\\", "/").toLowerCase();
  const normalizedSeedRoot = ECHO_ROOT.replaceAll("\\", "/").toLowerCase();
  return normalizedWorkspace === normalizedSeedRoot || normalizedWorkspace.startsWith(`${normalizedSeedRoot}/`);
}
