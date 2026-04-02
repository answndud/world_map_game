import { execFileSync } from "node:child_process";
import { resolve } from "node:path";
import { pathToFileURL } from "node:url";

const DEMO_LITE_ROOT = resolve(import.meta.dirname, "..");
const REPO_ROOT = resolve(DEMO_LITE_ROOT, "..");
const DEFAULT_PROJECT_NAME = process.env.DEMO_LITE_PAGES_PROJECT_NAME || "worldmap-demo-lite";
const DEFAULT_PRODUCTION_BRANCH = process.env.DEMO_LITE_PAGES_PRODUCTION_BRANCH || "main";

export function findPagesProject(projects, projectName) {
  if (!Array.isArray(projects)) {
    return null;
  }

  return projects.find((project) => project?.["Project Name"] === projectName) || null;
}

export function describePagesGitHandoff({ projectName, project, currentBranch, workingTreeDirty, productionBranch }) {
  const gitProvider = project?.["Git Provider"] || "Unknown";
  const isGitConnected = gitProvider !== "No" && gitProvider !== "Unknown";
  const steps = [];

  if (!isGitConnected) {
    steps.push("현재 Pages 프로젝트는 Direct Upload 상태입니다. 기존 프로젝트를 Git integration으로 전환할 수 없으므로 새 Git-connected Pages 프로젝트를 만들어야 합니다.");
  }

  if (currentBranch !== productionBranch) {
    steps.push(`현재 작업 브랜치는 ${currentBranch} 입니다. Git-connected production branch를 ${productionBranch}로 둘 계획이면 먼저 이 변경을 ${productionBranch}에 반영해야 합니다.`);
  }

  if (workingTreeDirty) {
    steps.push("로컬 working tree가 깨끗하지 않습니다. handoff 전에 커밋 또는 정리를 먼저 해야 합니다.");
  }

  if (steps.length === 0) {
    steps.push("Git-connected Pages handoff 조건이 충족됐습니다. Cloudflare 대시보드에서 Git-connected 프로젝트만 실제 production source of truth로 유지하면 됩니다.");
  }

  return {
    projectName,
    gitProvider,
    isGitConnected,
    currentBranch,
    workingTreeDirty,
    productionBranch,
    steps
  };
}

function run(command, args, cwd) {
  return execFileSync(command, args, {
    cwd,
    encoding: "utf8"
  }).trim();
}

function inspectCurrentState() {
  const projects = JSON.parse(run("npx", ["-y", "wrangler", "pages", "project", "list", "--json"], DEMO_LITE_ROOT));
  const project = findPagesProject(projects, DEFAULT_PROJECT_NAME);
  const currentBranch = run("git", ["branch", "--show-current"], REPO_ROOT);
  const workingTreeDirty = run("git", ["status", "--short"], REPO_ROOT).length > 0;

  return describePagesGitHandoff({
    projectName: DEFAULT_PROJECT_NAME,
    project,
    currentBranch,
    workingTreeDirty,
    productionBranch: DEFAULT_PRODUCTION_BRANCH
  });
}

function printSummary(summary) {
  console.log(`[demo-lite] Pages project: ${summary.projectName}`);
  console.log(`[demo-lite] Git provider: ${summary.gitProvider}`);
  console.log(`[demo-lite] Current branch: ${summary.currentBranch}`);
  console.log(`[demo-lite] Working tree dirty: ${summary.workingTreeDirty ? "yes" : "no"}`);
  console.log(`[demo-lite] Planned production branch: ${summary.productionBranch}`);
  console.log(`[demo-lite] Git-connected: ${summary.isGitConnected ? "yes" : "no"}`);
  console.log("[demo-lite] Next steps:");
  for (const step of summary.steps) {
    console.log(`- ${step}`);
  }
}

const isDirectExecution = process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href;

if (isDirectExecution) {
  try {
    printSummary(inspectCurrentState());
  } catch (error) {
    console.error(`[demo-lite] failed to inspect Pages Git handoff: ${error.message}`);
    process.exit(1);
  }
}
