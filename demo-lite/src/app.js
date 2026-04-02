import { mountCapitalGame, renderCapitalGamePage } from "./features/capital-game.js";
import { mountFlagGame, renderFlagGamePage } from "./features/flag-game.js";
import { mountPopulationBattleGame, renderPopulationBattleGamePage } from "./features/population-battle-game.js";
import { mountRecommendationDemo, renderRecommendationDemoPage } from "./features/recommendation.js";
import { readDemoLiteActivitySummary } from "./lib/browser-history.js";
import { loadSharedCatalog } from "./lib/shared-data.js";
import { RETAINED_ROUTES, normalizeRoute, resolveRoute } from "./routes.js";

function renderHeader(activePath) {
  const navItems = RETAINED_ROUTES.map((route) => {
    const activeClass = route.path === activePath ? "demo-nav-link is-active" : "demo-nav-link";
    return `<a class="${activeClass}" href="#${route.path}">${route.navLabel}</a>`;
  }).join("");

  return `
    <header class="demo-header">
      <a class="demo-brand" href="#/">
        <span class="demo-brand-mark">WorldMap</span>
        <strong>Demo-Lite</strong>
      </a>
      <nav class="demo-nav" aria-label="demo-lite navigation">
        ${navItems}
      </nav>
    </header>
  `;
}

function renderShell(route, sharedSummary, activitySummary) {
  return `
    <div class="demo-page-shell">
      ${renderHeader(route.path)}
      <main class="demo-layout">
        ${renderRoute(route, sharedSummary, activitySummary)}
      </main>
    </div>
  `;
}

function renderHome(sharedSummary, activitySummary) {
  const summaryCards = [
    { label: "공유 국가 데이터", value: `${sharedSummary.countryCount}개` },
    { label: "공유 국기 자산", value: `${sharedSummary.flagAssetCount}개` },
    { label: "유지한 대륙", value: `${sharedSummary.continentCount}개` },
    { label: "유지 게임/추천", value: `${sharedSummary.retainedGameCount} + 추천` }
  ];

  const cards = RETAINED_ROUTES.filter((route) => route.path !== "/")
    .map(
      (route) => `
        <article class="demo-card">
          <div class="demo-card-top">
            <span class="demo-chip">${route.subtitle}</span>
            <span class="demo-note">${route.status}</span>
          </div>
          <h2>${route.title}</h2>
          <p>${route.summary}</p>
          <a class="demo-link" href="#${route.path}">이 화면 열기</a>
        </article>
      `
    )
    .join("");

  const summary = summaryCards
    .map(
      (card) => `
        <article class="demo-metric">
          <span>${card.label}</span>
          <strong>${card.value}</strong>
        </article>
      `
    )
    .join("");

  const activityCards = [
    { label: "최근 기록 수", value: `${activitySummary.totalTrackedEntries}개` },
    { label: "플레이해 본 모드", value: `${activitySummary.activeModeCount}개` },
    { label: "브라우저 최고 점수", value: activitySummary.highestScore > 0 ? `${activitySummary.highestScore}점` : "아직 없음" },
    { label: "최근 추천 TOP1", value: activitySummary.latestRecommendationName },
    { label: "최근 게임 streak", value: activitySummary.recentGameStreak.label },
    { label: "최근 클리어 streak", value: activitySummary.recentGameStreak.clearLabel }
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

  const modeSummaryCards = activitySummary.modeSnapshots
    .map(
      (snapshot) => `
        <article class="demo-card">
          <div class="demo-card-top">
            <span class="demo-chip">${snapshot.label}</span>
          </div>
          <h2>${snapshot.title}</h2>
          <p>${snapshot.primary}</p>
          <span class="demo-note">${snapshot.secondary}</span>
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
      <p class="demo-eyebrow">demo-lite</p>
      <h1>무료 공개용으로 남길 범위만 다시 묶었습니다.</h1>
      <p class="demo-copy">
        이 앱은 풀 기능 Spring Boot 버전을 대체하지 않습니다. 로그인, 랭킹, 통계, 대시보드를 제거하고
        수도, 국기, 인구 비교 배틀, 추천 결과만 빠르게 체험하는 별도 공개 트랙입니다.
      </p>
      <div class="demo-actions">
        <a class="demo-button" href="#/games/capital">첫 retained 게임 보기</a>
        <a class="demo-ghost" href="#/recommendation">추천 결과 트랙 보기</a>
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>현재 메인 저장소에서 그대로 재사용한 것</h2>
        <p>백엔드 런타임은 분리하되, 국가/국기 기준 데이터는 같은 저장소에서 다시 읽습니다.</p>
      </div>
      <div class="demo-metric-grid">
        ${summary}
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>브라우저 최근 플레이 요약</h2>
        <p>서버 저장은 없지만, 브라우저 안에서는 최근 플레이와 추천 결과를 cross-mode로 다시 읽습니다.</p>
      </div>
      <div class="demo-metric-grid">
        ${activityCards}
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>공유용 한 줄 요약</h2>
        <p>최근 기록을 브라우저 안에서 다시 묶어, 링크 없이도 현재 체험 상태를 짧게 전달할 수 있게 만듭니다.</p>
      </div>
      <div class="demo-share-box">
        <div class="demo-share-box__top">
          <strong>최근 streak와 추천 결과를 같이 묶은 문장</strong>
          <button class="demo-ghost demo-copy-button" type="button" data-copy-demo-summary>
            한 줄 요약 복사
          </button>
        </div>
        <p class="demo-share-text" data-demo-summary-text>${activitySummary.shareSummaryText}</p>
        <span class="demo-note" data-copy-demo-feedback>
          공개 URL은 포함하지 않습니다. 배포 URL이 생기면 직접 뒤에 붙이면 됩니다.
        </span>
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>모드별 브라우저 요약</h2>
        <p>게임은 최고 점수와 완료 수, 추천은 최근 TOP1과 계산 횟수를 남깁니다.</p>
      </div>
      <div class="demo-card-grid">
        ${modeSummaryCards}
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>최근 기록</h2>
        <p>최근에 무엇을 플레이했는지 브라우저 메모리 대신 localStorage에서 다시 읽습니다.</p>
      </div>
      <ul class="demo-history-list">
        ${recentEntries || "<li><span>아직 기록이 없습니다. 게임을 한 판 하거나 추천 결과를 계산해 보세요.</span></li>"}
      </ul>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>v1 retained route map</h2>
        <p>수도, 국기, 인구 비교 배틀, 추천까지 local-state loop를 열어 free-tier용 retained surface를 모두 playable하게 만들었습니다.</p>
      </div>
      <div class="demo-card-grid">
        ${cards}
      </div>
    </section>

    <section class="demo-panel demo-panel--muted">
      <div class="demo-panel-head">
        <h2>의도적으로 뺀 것</h2>
      </div>
      <ul class="demo-list">
        <li>로그인 / 회원가입 / 게스트 기록 귀속</li>
        <li>서버 랭킹 / 공개 stats / 운영 dashboard</li>
        <li>DB session, Redis session, recommendation feedback 저장</li>
        <li>위치 게임과 인구수 4지선다 같은 고비용 surface</li>
      </ul>
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
      <p class="demo-eyebrow">${route.subtitle}</p>
      <h1>${route.title}</h1>
      <p class="demo-copy">${route.summary}</p>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>이번 조각에서 열린 것</h2>
      </div>
      <ul class="demo-list">
        <li>demo-lite 전용 header/navigation</li>
        <li>free-tier를 전제로 한 별도 route map</li>
        <li>메인 저장소의 정적 국가/국기 데이터 재사용 경로</li>
      </ul>
    </section>

    <section class="demo-panel demo-panel--muted">
      <div class="demo-panel-head">
        <h2>다음 조각에서 붙일 것</h2>
      </div>
      <ul class="demo-list">
        <li>local state 기반 round loop</li>
        <li>정답 판정과 점수 UI</li>
        <li>추천 결과 또는 게임 결과 surface</li>
      </ul>
      <a class="demo-ghost" href="#/">홈으로 돌아가기</a>
    </section>
  `;
}

function renderRoute(route, sharedSummary, activitySummary) {
  if (route.kind === "home") {
    return renderHome(sharedSummary, activitySummary);
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
          <p class="demo-eyebrow">demo-lite</p>
          <h1>공유 데이터를 불러오는 중입니다.</h1>
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
            <h2>공유 자산을 읽지 못했습니다.</h2>
            <p>${message}</p>
          </div>
          <p class="demo-copy">
            먼저 <code>npm run sync:shared</code>를 실행해 메인 저장소의 국가/국기 데이터를 demo-lite public 폴더로 복사하세요.
          </p>
        </section>
      </main>
    </div>
  `;
}

function mountHomeInteractions(root, activitySummary) {
  const copyButton = root.querySelector("[data-copy-demo-summary]");
  const feedbackNode = root.querySelector("[data-copy-demo-feedback]");

  if (!copyButton || !feedbackNode) {
    return;
  }

  copyButton.addEventListener("click", async () => {
    if (!navigator.clipboard?.writeText) {
      feedbackNode.textContent = "이 브라우저에서는 자동 복사를 지원하지 않습니다.";
      return;
    }

    try {
      await navigator.clipboard.writeText(activitySummary.shareSummaryText);
      feedbackNode.textContent = "한 줄 요약을 클립보드에 복사했습니다.";
    } catch (_error) {
      feedbackNode.textContent = "복사에 실패했습니다. 텍스트를 직접 선택해 복사하세요.";
    }
  });
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
    root.innerHTML = renderShell(currentRoute, catalog.summary, activitySummary);
    if (currentRoute.kind === "home") {
      mountHomeInteractions(root, activitySummary);
    }
    disposeCurrentRoute = mountRoute(currentRoute, root, catalog);
  }

  window.addEventListener("hashchange", renderCurrentRoute);

  if (!window.location.hash) {
    window.location.hash = "#/";
    return;
  }

  renderCurrentRoute();
}
