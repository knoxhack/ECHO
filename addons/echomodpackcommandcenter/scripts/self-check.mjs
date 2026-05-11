import fs from "node:fs";
import path from "node:path";
import assert from "node:assert/strict";

const root = process.cwd();
const seedPath = path.join(root, "src", "shared", "seed-data.json");
const packagePath = path.join(root, "package.json");
const serverPath = path.join(root, "src", "server", "index.ts");
const seed = JSON.parse(fs.readFileSync(seedPath, "utf-8"));
const pkg = JSON.parse(fs.readFileSync(packagePath, "utf-8"));
const server = fs.readFileSync(serverPath, "utf-8");

const requiredRoutes = [
  "/api/health",
  "/api/settings",
  "/api/projects",
  "/api/projects/:slug",
  "/api/projects/:slug/scan",
  "/api/projects/:slug/scans",
  "/api/projects/:slug/scans/:reportId",
  "/api/projects/:slug/qa/latest",
  "/api/projects/:slug/roadmap",
  "/api/projects/:slug/prompts",
  "/api/projects/:slug/prompts/render",
  "/api/projects/:slug/release",
  "/api/projects/:slug/release/:commandId/run",
  "/api/projects/:slug/jars",
  "/api/projects/:slug/readiness",
  "/api/projects/:slug/features",
  "/api/projects/:slug/jars/build",
  "/api/projects/:slug/jars/promote",
  "/api/projects/:slug/runs",
  "/api/runs/:runId",
  "/api/runs/:runId/stop",
  "/api/modpack/summary",
  "/api/modpack/rebuild",
  "/api/modpack/runs",
  "/api/projects/:slug/export"
];

const expectedCommands = [
  "build-beta-stack",
  "build-full-stack",
  "verify-release",
  "run-gametests",
  "scan-runtime-logs",
  "check-jar-set",
  "copy-jars",
  "remove-stale-jars",
  "generate-release-notes"
];
const expectedProjects = [
  "echo",
  "echocore",
  "echonetcore",
  "echodatacore",
  "echomissioncore",
  "echoashfallprotocol",
  "echoterminal",
  "echosignalos",
  "signalosexample",
  "echoorbitalremnants",
  "echonexusprotocol",
  "echoagriculturereclamation",
  "echoworldcore",
  "echostationfall",
  "echoblackboxprotocol",
  "echoindustrialnexus",
  "echologisticsnetwork",
  "echorendercore",
  "echoconvoyprotocol",
  "echoholomap",
  "echoindex",
  "echoarmory",
  "echolens",
  "arcana"
];

assert.equal(pkg.scripts.dev.includes("vite"), true, "dev script should run Vite");
assert.equal(pkg.scripts.dev.includes("src/server/index.ts"), true, "dev script should run backend");
assert.ok(pkg.dependencies.react, "React dependency is required");
assert.ok(pkg.devDependencies.tailwindcss, "Tailwind dependency is required");
assert.ok(server.includes("Confirmation required"), "Medium/high release actions must require API confirmation");
assert.ok(server.includes("req.body?.confirmed"), "Release confirmation must be checked on the backend");

for (const route of requiredRoutes) {
  assert.ok(server.includes(route), `Missing API route: ${route}`);
}

const echo = seed.projects.find((project) => project.slug === "echo");
assert.ok(echo, "ECHO project must be seeded");
assert.deepEqual(seed.projects.map((project) => project.slug), expectedProjects, "Project cards must map to real workspace modules only");
assert.equal(echo.modules.length, 22, "ECHO should seed the full real workspace release module set");

const actionIds = seed.releaseActions.filter((action) => action.projectSlug === "echo").map((action) => action.commandId);
assert.deepEqual(actionIds, expectedCommands, "Release action allowlist changed unexpectedly");

for (const project of seed.projects.filter((candidate) => candidate.slug !== "echo" && candidate.slug !== "arcana")) {
  const scopedActions = seed.releaseActions.filter((action) => action.projectSlug === project.slug).map((action) => action.commandId).sort();
  assert.deepEqual(scopedActions, ["build-module", "compile-java", "run-gametests", "validate-resources"], `${project.slug} needs scoped module actions`);
}

const arcana = seed.projects.find((project) => project.slug === "arcana");
assert.ok(arcana, "ARCANA project must be seeded");
assert.equal(arcana.workspacePath, "C:/Github/ARCANA", "ARCANA should point at its checkout");
assert.deepEqual(arcana.modules.map((module) => module.modId), ["arcanaveil"], "ARCANA should seed its real mod id");
assert.deepEqual(
  seed.releaseActions.filter((action) => action.projectSlug === "arcana").map((action) => action.commandId).sort(),
  ["build-module", "compile-java", "run-gametests", "scan-runtime-logs", "validate-resources", "verify-release"],
  "ARCANA needs its standalone Gradle release actions"
);

for (const action of seed.releaseActions) {
  if (action.mode === "shell") {
    assert.equal(action.executable, ".\\gradlew.bat", `${action.commandId} must use Gradle wrapper`);
    assert.ok(Array.isArray(action.args) && action.args.length > 0, `${action.commandId} needs explicit args`);
  } else {
    assert.equal(action.executable, "", `${action.commandId} must not define shell executable`);
    assert.deepEqual(action.args, [], `${action.commandId} must not define shell args`);
  }
}

const tracks = seed.qaTracks.filter((track) => track.projectSlug === "echo").map((track) => track.key).sort();
assert.deepEqual(tracks, ["handoffs", "release-ops", "resources", "runtime-logs", "terminal"], "Unexpected ECHO QA tracks");

const promptCategories = new Set(seed.promptTemplates.map((prompt) => prompt.category));
assert.ok(promptCategories.has("Codex QA"), "Codex QA prompts must be seeded");
assert.ok(promptCategories.has("Asset Prompt"), "Asset prompts must be seeded");

console.log("Noxhack Command Center self-check passed.");
