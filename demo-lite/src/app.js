import { mountCapitalGame, renderCapitalGamePage } from "./features/capital-game.js";
import { mountFlagGame, renderFlagGamePage } from "./features/flag-game.js";
import { mountPopulationBattleGame, renderPopulationBattleGamePage } from "./features/population-battle-game.js";
import { mountRecommendationDemo, renderRecommendationDemoPage } from "./features/recommendation.js";
import { readDemoLiteActivitySummary } from "./lib/browser-history.js";
import { loadSharedCatalog } from "./lib/shared-data.js";
import { RETAINED_ROUTES, normalizeRoute, resolveRoute } from "./routes.js";

function renderHeader(activePath) {
  void activePath;

  return `
    <header class="demo-header">
      <a class="demo-brand" href="#/">
        <span class="demo-brand-mark">WorldMap</span>
      </a>
    </header>
  `;
}

function renderShell(route, activitySummary) {
  return `
    <div class="demo-page-shell">
      ${renderHeader(route.path)}
      <main class="demo-layout">
        ${renderRoute(route, activitySummary)}
      </main>
    </div>
  `;
}

function renderHome(activitySummary) {
  const cards = RETAINED_ROUTES.filter((route) => route.path !== "/")
    .map(
      (route) => `
        <a class="demo-card demo-card--route" href="#${route.path}">
          <h2>${route.title}</h2>
          <p>${route.summary}</p>
        </a>
      `
    )
    .join("");

  const activityCards = [
    { label: "즐긴 게임", value: `${activitySummary.activeModeCount}개` },
    { label: "최고 점수", value: activitySummary.highestScore > 0 ? `${activitySummary.highestScore}점` : "아직 없음" },
    { label: "최근 추천", value: activitySummary.latestRecommendationName }
  ]
    .map(
      (card) => `
        <article class="demo-metric">
          <span>${card.label}</span>
          <strong>${card.value}</strong>
        </article>
      `
    )
    .join("");

  const recentEntries = activitySummary.recentEntries
    .map(
      (entry) => `
        <li>
          <strong>${entry.title}</strong>
          <span>${entry.summary}</span>
        </li>
      `
    )
    .join("");

  return `
    <section class="demo-hero">
      <h1>World Map Game</h1>
      <p class="demo-copy">
        수도 퀴즈, 국기 퀴즈, 인구 비교 배틀, 국가 추천까지 바로 시작할 수 있습니다.
      </p>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>게임 목록</h2>
      </div>
      <div class="demo-feature-grid">
        ${cards}
      </div>
    </section>

    <section class="demo-split-grid">
      <section class="demo-panel">
        <div class="demo-panel-head">
          <h2>최근 플레이 요약</h2>
        </div>
        <div class="demo-metric-grid demo-metric-grid--home">
          ${activityCards}
        </div>
      </section>

      <section class="demo-panel">
        <div class="demo-panel-head">
          <h2>최근 기록</h2>
        </div>
        <ul class="demo-history-list">
          ${
            recentEntries
            || "<li><span>아직 기록이 없습니다. 게임을 한 판 하거나 추천 결과를 계산해 보세요.</span></li>"
          }
        </ul>
      </section>
    </section>

  `;
}

function renderFeature(route) {
  if (route.path === "/games/capital") {
    return renderCapitalGamePage();
  }

  if (route.path === "/games/flag") {
    return renderFlagGamePage();
  }

  if (route.path === "/games/population-battle") {
    return renderPopulationBattleGamePage();
  }

  if (route.path === "/recommendation") {
    return renderRecommendationDemoPage();
  }

  return `
    <section class="demo-hero demo-hero--compact">
      <h1>${route.title}</h1>
      <p class="demo-copy">${route.summary}</p>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>이 화면은 준비 중입니다</h2>
        <p>곧 이 자리에서 바로 플레이할 수 있게 연결됩니다.</p>
      </div>
      <a class="demo-ghost" href="#/">홈으로 돌아가기</a>
    </section>
  `;
}

function renderRoute(route, activitySummary) {
  if (route.kind === "home") {
    return renderHome(activitySummary);
  }
  return renderFeature(route);
}

function mountRoute(route, root, catalog) {
  if (route.path === "/games/capital") {
    const target = root.querySelector("#demo-capital-root");
    return mountCapitalGame(target, catalog.countries);
  }

  if (route.path === "/games/flag") {
    const target = root.querySelector("#demo-flag-root");
    return mountFlagGame(target, catalog.countries, catalog.flagAssets);
  }

  if (route.path === "/games/population-battle") {
    const target = root.querySelector("#demo-population-battle-root");
    return mountPopulationBattleGame(target, catalog.countries);
  }

  if (route.path === "/recommendation") {
    const target = root.querySelector("#demo-recommendation-root");
    return mountRecommendationDemo(target, catalog.countries);
  }

  return () => {};
}

function renderLoading() {
  return `
    <div class="demo-page-shell">
      ${renderHeader("/")}
      <main class="demo-layout">
        <section class="demo-hero demo-hero--compact">
          <h1>화면을 준비하고 있습니다.</h1>
        </section>
      </main>
    </div>
  `;
}

function renderError(message) {
  return `
    <div class="demo-page-shell">
      ${renderHeader("/")}
      <main class="demo-layout">
        <section class="demo-panel demo-panel--muted">
          <div class="demo-panel-head">
            <h2>화면을 불러오지 못했습니다.</h2>
            <p>잠시 뒤 다시 시도해 주세요.</p>
          </div>
          <p class="demo-copy">${message}</p>
        </section>
      </main>
    </div>
  `;
}

export async function bootstrapDemoLiteApp(root) {
  if (!root) {
    return;
  }

  root.innerHTML = renderLoading();

  let catalog;
  try {
    catalog = await loadSharedCatalog();
  } catch (error) {
    root.innerHTML = renderError(error instanceof Error ? error.message : "Unknown error");
    return;
  }

  let disposeCurrentRoute = () => {};

  function renderCurrentRoute() {
    disposeCurrentRoute();
    const currentPath = normalizeRoute(window.location.hash);
    const currentRoute = resolveRoute(currentPath);
    const activitySummary = readDemoLiteActivitySummary();
    root.innerHTML = renderShell(currentRoute, activitySummary);
    disposeCurrentRoute = mountRoute(currentRoute, root, catalog);
  }

  window.addEventListener("hashchange", renderCurrentRoute);

  if (!window.location.hash) {
    window.location.hash = "#/";
    return;
  }

  renderCurrentRoute();
}
