import { mountCapitalGame, renderCapitalGamePage } from "./features/capital-game.js";
import { mountFlagGame, renderFlagGamePage } from "./features/flag-game.js";
import { mountPopulationGame, renderPopulationGamePage } from "./features/population-game.js";
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
        <span class="demo-brand-mark">worldmap</span>
        <strong>demo-lite</strong>
      </a>
      <div class="demo-header-meta">
        <span class="demo-chip">static demo</span>
        <span class="demo-note">cloudflare pages</span>
      </div>
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
          <div class="demo-card-top">
            <span class="demo-chip">${route.navLabel}</span>
            <span class="demo-note">${route.cardMeta ?? "바로 열기"}</span>
          </div>
          <h2>${route.title}</h2>
          <p>${route.summary}</p>
          <span class="demo-card-link">열기</span>
        </a>
      `
    )
    .join("");

  const supportMetrics = [
    { label: "Playable", value: "5 surfaces" },
    { label: "Storage", value: "browser only" },
    { label: "Data", value: "194 countries" },
    { label: "Deploy", value: "pages.dev" }
  ]
    .map(
      (card) => `
        <article class="demo-support-metric">
          <span>${card.label}</span>
          <strong>${card.value}</strong>
        </article>
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
      <div class="demo-hero-grid">
        <div class="demo-hero-copy">
          <div class="demo-route-hero-top">
            <span class="demo-chip">worldmap demo-lite</span>
            <span class="demo-chip">5 playable surfaces</span>
            <span class="demo-chip">browser-only loop</span>
          </div>
          <h1>빠르게 열리고, 바로 플레이되는 WorldMap.</h1>
          <p class="demo-copy">
            수도, 국기, 인구수, 인구 배틀, 국가 추천까지 한 번에 체험할 수 있는 정적 공개 버전입니다.
            서버 상태를 설명하는 대신, 지금 손에 잡히는 플레이 감각과 제품 톤을 먼저 보여 줍니다.
          </p>
          <div class="demo-actions">
            <a class="demo-button" href="#/recommendation">추천부터 시작</a>
            <a class="demo-ghost" href="#/games/flag">국기 퀴즈 열기</a>
          </div>
        </div>

        <aside class="demo-support-card">
          <div class="demo-panel-head demo-panel-head--compact">
            <p class="demo-panel-kicker">Product Snapshot</p>
            <h2>정적 배포로 남긴 핵심 경험</h2>
            <p>Cloudflare Pages 위에서 게임 4종과 추천 1종을 브라우저 상태만으로 닫았습니다.</p>
          </div>
          <div class="demo-support-metrics">
            ${supportMetrics}
          </div>
          <ul class="demo-support-list">
            <li>퀴즈 4종은 모두 5문제 러닝과 browser best score를 유지합니다.</li>
            <li>추천은 20문항 설문과 top 3 비교 카드까지 바로 읽을 수 있습니다.</li>
            <li>최근 플레이와 추천 결과는 홈에서 다시 요약됩니다.</li>
          </ul>
        </aside>
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <p class="demo-panel-kicker">Playable Surfaces</p>
        <h2>짧은 퀴즈와 탐색형 추천을 같은 셸에 묶었습니다.</h2>
        <p>각 화면은 browser-only loop지만, 톤과 정보 위계는 하나의 제품처럼 읽히도록 정리했습니다.</p>
      </div>
      <div class="demo-feature-grid">
        ${cards}
      </div>
    </section>

    <section class="demo-split-grid">
      <section class="demo-panel">
        <div class="demo-panel-head">
          <p class="demo-panel-kicker">Recent Snapshot</p>
          <h2>최근 브라우저 기록</h2>
          <p>지금 이 브라우저에서 어떤 모드를 더 자주 열었는지 빠르게 확인할 수 있습니다.</p>
        </div>
        <div class="demo-metric-grid demo-metric-grid--home">
          ${activityCards}
        </div>
      </section>

      <section class="demo-panel">
        <div class="demo-panel-head">
          <p class="demo-panel-kicker">Latest Runs</p>
          <h2>마지막 결과</h2>
          <p>완료한 퀴즈와 최근 추천 결과를 시간순으로 다시 읽습니다.</p>
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

  if (route.path === "/games/population") {
    return renderPopulationGamePage();
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

  if (route.path === "/games/population") {
    const target = root.querySelector("#demo-population-root");
    return mountPopulationGame(target, catalog.countries);
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
