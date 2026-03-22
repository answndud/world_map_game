---
name: worldmap-doc-sync
description: Sync documentation for the WorldMap project at /Users/alex/project/worldmap after meaningful code, architecture, or workflow changes. Use when implementing features, APIs, seed data, tests, refactors, or AI/recommendation work and you need to update docs/WORKLOG.md, docs/PORTFOLIO_PLAYBOOK.md, README.md, or blog posts without missing rationale, request flow, or interview notes.
---

# Worldmap Doc Sync

## Overview

Use this skill to keep the WorldMap project documentation aligned with completed work.
Treat it as the post-task documentation checklist for `/Users/alex/project/worldmap`, not as a general writing skill.
Invoke it explicitly as `$worldmap-doc-sync`; implicit invocation should stay disabled.

## Quick Start

1. Confirm the task belongs to `/Users/alex/project/worldmap`.
2. Identify what changed by reading the current request, touched files, and recent diffs.
3. Read `references/doc-impact-map.md`.
4. Update the required docs in this order:
   - `docs/WORKLOG.md`
   - `docs/PORTFOLIO_PLAYBOOK.md` when stage/status/criteria changed
   - `README.md` when public architecture, feature scope, API, or domain explanation changed
   - `blog/*` only when the user asks for publish-style documentation or a milestone deserves a dedicated explanatory post
5. State what tests ran, or explicitly state that tests were not run.

## Workflow

### 1. Classify the change

Bucket the completed work before editing docs.

- Bootstrap / infrastructure
- Country data / seed data
- Game domain / session / round logic
- Ranking / Redis
- Recommendation / LLM
- Auth / user history
- Docs-only / planning-only

This classification controls which docs need updates.

### 2. Choose the docs to update

Read `references/doc-impact-map.md` first.

Default rule:

- Always update `docs/WORKLOG.md` after any meaningful code, design, or documentation change.
- Update `docs/PORTFOLIO_PLAYBOOK.md` only if the current phase status, completion criteria, scope, or next-step guidance changed.
- Update `README.md` only if a reader should understand the project differently afterward.
- Update `blog/` only when there is enough real code, tests, and reasoning to support a proper post.

Do not duplicate every detail everywhere.

### 3. Write the minimum useful update

Prefer concrete updates over verbose summaries.

For `docs/WORKLOG.md`:

- Use `references/worklog-entry-template.md`.
- Include purpose, changed files, request flow, state changes, edge cases, tests, weak points, and a 30-second interview summary.

For `docs/PORTFOLIO_PLAYBOOK.md`:

- Update the phase status if it materially changed.
- Adjust "구현 항목", "완료 기준", or "반드시 이해할 것" only when the understanding model changed.
- Keep the phase sequence stable unless the project strategy truly changed.

For `README.md`:

- Update public-facing architecture, API outlines, domain model, or implementation order only when those concepts changed.
- Avoid turning `README.md` into a work log.

For `blog/`:

- Read `references/blog-update-rules.md`.
- Do not write speculative implementation posts without real files, classes, and tests.

### 4. Run a consistency pass

Before finishing, check these items:

- Stage names match `docs/PORTFOLIO_PLAYBOOK.md`.
- New claims match the changed files.
- Tests are mentioned accurately.
- "아직 약한 부분" is honest, not empty filler.
- The same concept is not explained differently across `README.md`, playbook, and work log.

## WorldMap Document Roles

- `README.md`
  - Project overview, architecture, domain, feature scope
- `docs/AI_AGENT_OPERATING_MODEL.md`
  - AI usage rules, skills, agent strategy
- `docs/PORTFOLIO_PLAYBOOK.md`
  - Build order, phase goals, understanding checkpoints
- `docs/WORKLOG.md`
  - Actual task-by-task reasoning record
- `blog/`
  - Publish-style beginner-friendly explanations

## Read Only What You Need

- Read `references/doc-impact-map.md` first for every run.
- Read `references/worklog-entry-template.md` when drafting a new work log entry.
- Read `references/blog-update-rules.md` only when deciding whether or how to update `blog/`.

## Do Not Use This Skill For

- General-purpose documentation outside the WorldMap repository
- Marketing copy
- Rewriting docs that are unrelated to the completed task
- Replacing architecture decisions with vague summaries
