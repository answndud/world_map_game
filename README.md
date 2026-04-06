# WorldMap

> Server-driven geography game platform with a public static demo.

WorldMap is a portfolio project that combines multiple geography games, a deterministic country recommendation flow, public rankings, member history, and an admin review surface in one product.

The main engineering point is simple: **the browser renders input, but the server owns session state, answer validation, score calculation, retry rules, and result timing.**

**Live links**

- `Demo Lite (public)`: [https://world-map-game-demo-lite-git.pages.dev/](https://world-map-game-demo-lite-git.pages.dev/)
- `Main App`: local / deployment-prep only for now
- `Architecture`: [docs/ARCHITECTURE_OVERVIEW.md](docs/ARCHITECTURE_OVERVIEW.md)
- `Request Flow`: [docs/REQUEST_FLOW_GUIDE.md](docs/REQUEST_FLOW_GUIDE.md)
- `Blog`: [blog/README.md](blog/README.md)

## What You Can Explore

- `국가 위치 찾기`: globe-based geography game with retry-based arcade flow
- `수도 퀴즈`: 4-choice capital quiz
- `국기 퀴즈`: country guessing from flag assets
- `인구 비교 배틀`: quick two-choice population battle
- `국가 추천`: 20-question deterministic recommendation flow
- `랭킹 / stats / mypage / dashboard`: public leaderboard, public service stats, player history, and admin review screens

## Why This Repo Is Worth Reviewing

- It is not a toy CRUD sample. It is a stateful game product with multiple surfaces and shared domain rules.
- The core game loop is modeled on the server as `Session / Stage / Attempt`, not hidden in the browser.
- Leaderboards use `Redis` as a read model but keep `PostgreSQL` as the source of truth and fall back to DB reads when Redis is unavailable.
- Guest runs can be claimed into a member account, so anonymous play and signed-in history share one ownership story.
- The project includes verification rails beyond unit tests: integration tests, browser smoke tests, public URL smoke checks, and deploy preflight checks.

## Main App vs Demo Lite

| Track | Purpose | Stack | Current status |
| --- | --- | --- | --- |
| `Main App` | Full product with server-owned game state, ranking, stats, mypage, and admin review | Spring Boot, Thymeleaf SSR, PostgreSQL, Redis, Playwright browser smoke | Local-ready, production-prep, no public URL yet |
| `Demo Lite` | Public walkthrough version that quickly shows the product direction and core interactions | Vite, vanilla JS, static assets, browser local-state | Publicly deployed on Cloudflare Pages |

`Demo Lite` exists because a public URL was needed before the full Spring Boot runtime was publicly launched.
It is intentionally narrower than the main app, but it still reflects the actual product language, visual system, and playable flows.

## Screenshots

Main app screenshots below were captured from the local `local` profile build.
Demo Lite screenshots below were captured from the live Cloudflare Pages deployment.

### Main App

| Home | Location Game | Ranking |
| --- | --- | --- |
| ![Main home](docs/images/readme/main-home.png) | ![Main location game start](docs/images/readme/main-location-start.png) | ![Main ranking](docs/images/readme/main-ranking.png) |

- `Home`: one place to choose between long-run games, quick quizzes, recommendation, stats, and account entry.
- `Location Game`: the flagship server-driven mode that starts the globe mission flow.
- `Ranking`: public leaderboard surface with game filter, daily/all scope, and Redis-backed read model.

### Demo Lite

| Home | Capital Quiz | Recommendation |
| --- | --- | --- |
| ![Demo Lite home](docs/images/readme/demo-home.png) | ![Demo Lite capital quiz](docs/images/readme/demo-capital.png) | ![Demo Lite recommendation](docs/images/readme/demo-recommendation.png) |

- `Demo Lite Home`: compact public landing surface for the retained playable routes.
- `Capital Quiz`: quick playable example of the lightweight public demo track.
- `Recommendation`: simplified public recommendation flow with the same product tone as the main app.

## Technical Highlights

### 1. Server-driven game loop

The main app keeps game state on the server, not in the browser.

- game sessions are modeled as `Session / Stage / Attempt`
- the server decides correctness, score, lives, next stage, and result visibility
- stale or duplicate submissions are guarded instead of silently accepted

### 2. Shared source of truth with Redis read model

- `PostgreSQL` stores countries, game sessions, attempts, leaderboard records, and recommendation feedback
- `Redis` serves leaderboard reads and production session storage
- public ranking and stats can fall back to DB-backed reads when Redis is unavailable

### 3. Deterministic recommendation engine

- 20-question survey
- 30 country profiles
- deterministic scoring instead of opaque runtime generation
- feedback loop connected to admin review surfaces

### 4. Ownership and account flow

- guests can start immediately
- login can claim current browser progress into a member account
- `/mypage` and `/stats` are built as read models over the same underlying records

### 5. Verification pipeline

- Spring integration tests
- browser smoke tests for representative real-browser flows
- public URL smoke checks for deployed surfaces
- deployment preflight checks for production inputs

## How I Worked With AI

This project was built in an AI-assisted workflow, but not in an “accept whatever the model writes” workflow.

- I used AI to accelerate repetitive implementation, compare possible structures, and keep documentation in sync.
- I still treated domain placement, API boundaries, and public behavior as human decisions.
- Every meaningful slice was gated by tests, smoke checks, or manual verification.
- The repository keeps design reasoning in `docs/` and `blog/` so I can explain the result without hand-waving.
- The goal was not “generate more code faster”, but “use AI to increase speed without giving up ownership of quality”.

## Run Locally

### Main App

Requirements:

- Java 25
- Docker Desktop or Docker Engine

```bash
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=local'
```

Open:

- [http://localhost:8080](http://localhost:8080)

### Demo Lite

```bash
cd demo-lite
npm install
npm run dev
```

## Verification

### Main App

```bash
./gradlew test
./gradlew browserSmokeTest
```

### Demo Lite

```bash
cd demo-lite
npm test
npm run build
npm run verify:pages
npm run smoke:public -- https://world-map-game-demo-lite-git.pages.dev
```

## Documentation

### Project overview

- [docs/ARCHITECTURE_OVERVIEW.md](docs/ARCHITECTURE_OVERVIEW.md)
- [docs/REQUEST_FLOW_GUIDE.md](docs/REQUEST_FLOW_GUIDE.md)
- [docs/ERD.md](docs/ERD.md)
- [docs/PRESENTATION_PREP.md](docs/PRESENTATION_PREP.md)

### Deployment and operations

- [docs/DEPLOYMENT_RUNBOOK_AWS_ECS.md](docs/DEPLOYMENT_RUNBOOK_AWS_ECS.md)
- [docs/DEPLOYMENT_RUNBOOK_RAILWAY.md](docs/DEPLOYMENT_RUNBOOK_RAILWAY.md)
- [docs/LOCAL_DEMO_BOOTSTRAP.md](docs/LOCAL_DEMO_BOOTSTRAP.md)

### Rebuild-style writeups

- [blog/README.md](blog/README.md)
- [blog/00_rebuild_guide.md](blog/00_rebuild_guide.md)
- [blog/00_series_plan.md](blog/00_series_plan.md)

## Current Status

- `Main App`: feature-complete enough to explain the server-driven architecture, but not publicly deployed yet
- `Demo Lite`: publicly deployed and continuously smoke-checked
- production deployment inputs, verify workflow, and browser smoke rails are already prepared
- recommendation is deterministic and explainable; it does not depend on live generative inference at runtime

## License

If no separate license file is provided, all rights remain with the repository owner.
