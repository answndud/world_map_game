import test from "node:test";
import assert from "node:assert/strict";

import {
  describePagesGitHandoff,
  findPagesProject
} from "../scripts/inspect-pages-git-handoff.mjs";

test("findPagesProject returns the matching Pages project by name", () => {
  const project = findPagesProject(
    [
      { "Project Name": "alpha", "Git Provider": "GitHub" },
      { "Project Name": "worldmap-demo-lite", "Git Provider": "No" }
    ],
    "worldmap-demo-lite"
  );

  assert.deepEqual(project, { "Project Name": "worldmap-demo-lite", "Git Provider": "No" });
});

test("describePagesGitHandoff explains the direct-upload limitation", () => {
  const summary = describePagesGitHandoff({
    projectName: "worldmap-demo-lite",
    project: { "Project Name": "worldmap-demo-lite", "Git Provider": "No" },
    currentBranch: "codex/security-session-guard",
    workingTreeDirty: false,
    productionBranch: "main"
  });

  assert.equal(summary.isGitConnected, false);
  assert.match(summary.steps[0], /Direct Upload 상태/);
  assert.match(summary.steps[1], /main/);
});

test("describePagesGitHandoff reports success when git-connected project is already aligned", () => {
  const summary = describePagesGitHandoff({
    projectName: "worldmap-demo-lite-main",
    project: { "Project Name": "worldmap-demo-lite-main", "Git Provider": "GitHub" },
    currentBranch: "main",
    workingTreeDirty: false,
    productionBranch: "main"
  });

  assert.equal(summary.isGitConnected, true);
  assert.equal(summary.steps.length, 1);
  assert.match(summary.steps[0], /조건이 충족/);
});
