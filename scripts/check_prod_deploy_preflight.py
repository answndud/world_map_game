#!/usr/bin/env python3
"""Check whether the repository is ready for the first ECS production deploy."""

from __future__ import annotations

import argparse
import json
import shutil
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
import re

VAR_PATTERN = re.compile(r"\$\{\{\s*vars\.([A-Z0-9_]+)\s*}}")

REQUIRED_FILES = [
    Path(".github/workflows/deploy-prod-ecs.yml"),
    Path("deploy/ecs/task-definition.prod.sample.json"),
    Path("scripts/render_ecs_task_definition.py"),
]


@dataclass(frozen=True)
class PreflightResult:
    repository: str
    workflow_path: Path
    workflow_dispatch_enabled: bool
    required_variables: list[str]
    present_variables: list[str]
    missing_variables: list[str]
    missing_files: list[str]

    def is_ready(self) -> bool:
        return (
            self.workflow_dispatch_enabled
            and not self.missing_variables
            and not self.missing_files
        )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Check GitHub Actions variables and files required for the first ECS deploy."
    )
    parser.add_argument(
        "--workflow",
        type=Path,
        default=Path(".github/workflows/deploy-prod-ecs.yml"),
        help="Path to the deploy workflow file.",
    )
    parser.add_argument(
        "--repo",
        default="",
        help="GitHub repository in owner/name format. Defaults to gh repo view result.",
    )
    parser.add_argument(
        "--variables-json",
        type=Path,
        help="Optional JSON file with the same shape as `gh api repos/<repo>/actions/variables` output.",
    )
    parser.add_argument(
        "--report",
        type=Path,
        default=Path("build/reports/deploy-preflight/prod-deploy-preflight.md"),
        help="Path to write the markdown report.",
    )
    return parser.parse_args()


def load_workflow(path: Path) -> str:
    if not path.exists():
        raise SystemExit(f"Workflow file not found: {path}")
    return path.read_text(encoding="utf-8")


def extract_required_variables(workflow_text: str) -> list[str]:
    return sorted(set(VAR_PATTERN.findall(workflow_text)))


def workflow_dispatch_enabled(workflow_text: str) -> bool:
    return "workflow_dispatch:" in workflow_text


def repository_name(explicit_repo: str) -> str:
    if explicit_repo.strip():
        return explicit_repo.strip()
    return run_gh_json(["repo", "view", "--json", "nameWithOwner"], expect_key="nameWithOwner")


def load_present_variables(repo: str, variables_json_path: Path | None) -> list[str]:
    if variables_json_path is not None:
        payload = json.loads(variables_json_path.read_text(encoding="utf-8"))
    else:
        ensure_gh_available()
        payload = run_gh_api_json([f"repos/{repo}/actions/variables"])
    return sorted(variable["name"] for variable in payload.get("variables", []))


def ensure_gh_available() -> None:
    if shutil.which("gh") is None:
        raise SystemExit("GitHub CLI `gh` is required unless --variables-json is provided.")


def run_gh_json(args: list[str], expect_key: str) -> str:
    completed = subprocess.run(
        ["gh", *args],
        check=False,
        capture_output=True,
        text=True,
    )
    if completed.returncode != 0:
        raise SystemExit(completed.stderr.strip() or completed.stdout.strip() or "gh command failed")
    payload = json.loads(completed.stdout)
    value = payload.get(expect_key, "")
    if not value:
        raise SystemExit(f"Missing `{expect_key}` in gh output.")
    return value


def run_gh_api_json(args: list[str]) -> dict:
    completed = subprocess.run(
        ["gh", "api", *args],
        check=False,
        capture_output=True,
        text=True,
    )
    if completed.returncode != 0:
        raise SystemExit(completed.stderr.strip() or completed.stdout.strip() or "gh api failed")
    return json.loads(completed.stdout)


def missing_files() -> list[str]:
    return [str(path) for path in REQUIRED_FILES if not path.exists()]


def write_report(report_path: Path, result: PreflightResult) -> None:
    report_path.parent.mkdir(parents=True, exist_ok=True)

    lines = [
        "# Production Deploy Preflight",
        "",
        f"- Repository: {result.repository}",
        f"- Workflow: {result.workflow_path}",
        f"- Ready: {'YES' if result.is_ready() else 'NO'}",
        f"- workflow_dispatch: {'enabled' if result.workflow_dispatch_enabled else 'missing'}",
        "",
        "## Required GitHub Variables",
        "",
        "| Variable | Status |",
        "| --- | --- |",
    ]

    present = set(result.present_variables)
    for name in result.required_variables:
        status = "present" if name in present else "missing"
        lines.append(f"| {name} | {status} |")

    lines.extend(
        [
            "",
            "## Required Files",
            "",
            "| Path | Status |",
            "| --- | --- |",
        ]
    )
    missing_file_set = set(result.missing_files)
    for path in REQUIRED_FILES:
        status = "missing" if str(path) in missing_file_set else "present"
        lines.append(f"| {path} | {status} |")

    lines.extend(
        [
            "",
            "## Next Actions",
            "",
        ]
    )

    if result.missing_variables:
        lines.append("- GitHub repository variables부터 채워야 한다.")
        for name in result.missing_variables:
            lines.append(f"  - `{name}`")
    if result.missing_files:
        lines.append("- 배포에 필요한 템플릿/스크립트 파일이 누락돼 있다.")
    if not result.workflow_dispatch_enabled:
        lines.append("- `workflow_dispatch` 트리거가 없어 수동 첫 배포를 실행할 수 없다.")
    if result.is_ready():
        lines.append("- 이제 GitHub Actions의 `deploy-prod-ecs` workflow를 수동 실행해 첫 배포를 시도하면 된다.")
        lines.append("- 배포가 끝나면 ALB DNS를 첫 public URL로 잡고 `publicUrlSmokeTest`를 실행하면 된다.")

    report_path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def build_result(args: argparse.Namespace) -> PreflightResult:
    workflow_text = load_workflow(args.workflow)
    repo = repository_name(args.repo)
    required_variables = extract_required_variables(workflow_text)
    present_variables = load_present_variables(repo, args.variables_json)
    missing_variable_names = sorted(set(required_variables) - set(present_variables))

    return PreflightResult(
        repository=repo,
        workflow_path=args.workflow,
        workflow_dispatch_enabled=workflow_dispatch_enabled(workflow_text),
        required_variables=required_variables,
        present_variables=present_variables,
        missing_variables=missing_variable_names,
        missing_files=missing_files(),
    )


def main() -> int:
    args = parse_args()
    result = build_result(args)
    write_report(args.report, result)

    if result.is_ready():
        print(f"Deploy preflight passed. Report: {args.report}")
        return 0

    print(f"Deploy preflight failed. Report: {args.report}")
    if result.missing_variables:
        print("Missing variables: " + ", ".join(result.missing_variables))
    if result.missing_files:
        print("Missing files: " + ", ".join(result.missing_files))
    if not result.workflow_dispatch_enabled:
        print("workflow_dispatch trigger is missing.")
    return 1


if __name__ == "__main__":
    sys.exit(main())
