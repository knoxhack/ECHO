import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const seed = JSON.parse(fs.readFileSync(path.join(root, "src", "shared", "seed-data.json"), "utf-8"));

test("ECHO project seeds release modules", () => {
  const echo = seed.projects.find((project) => project.slug === "echo");
  assert.ok(echo);
  assert.equal(echo.modules.length, 8);
  assert.deepEqual(
    echo.modules.map((module) => module.modId),
    [
      "echocore",
      "echoterminal",
      "echoashfallprotocol",
      "echoorbitalremnants",
      "echostationfall",
      "echonexusprotocol",
      "echoindustrialnexus",
      "echoblackboxprotocol"
    ]
  );
});

test("release actions are a closed allowlist", () => {
  assert.deepEqual(
    seed.releaseActions.map((action) => action.commandId),
    [
      "build-beta-stack",
      "build-full-stack",
      "verify-release",
      "run-gametests",
      "scan-runtime-logs",
      "check-jar-set",
      "copy-jars",
      "remove-stale-jars",
      "generate-release-notes"
    ]
  );
});

test("local actions cannot carry shell command payloads", () => {
  for (const action of seed.releaseActions.filter((candidate) => candidate.mode !== "shell")) {
    assert.equal(action.executable, "");
    assert.deepEqual(action.args, []);
  }
});

test("scanner tracks cover ECHO QA lanes", () => {
  const tracks = seed.qaTracks.filter((track) => track.projectSlug === "echo").map((track) => track.key).sort();
  assert.deepEqual(tracks, ["handoffs", "release-ops", "resources", "terminal"]);
});

test("exports have both prompt categories available", () => {
  const categories = new Set(seed.promptTemplates.map((prompt) => prompt.category));
  assert.equal(categories.has("Codex QA"), true);
  assert.equal(categories.has("Asset Prompt"), true);
});
