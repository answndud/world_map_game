import { recordDemoLiteGameRun } from "../lib/browser-history.js";

const POPULATION_STAGE_COUNT = 5;
const POPULATION_INITIAL_LIVES = 3;
const POPULATION_STORAGE_KEY = "worldmap-demo-lite:population-best-score";
const CORRECT_ADVANCE_DELAY_MS = 950;
const WRONG_RETRY_DELAY_MS = 950;
const FINISH_DELAY_MS = 1100;

const POPULATION_SCALE_BANDS = [
  { lowerBoundInclusive: 0, upperBoundExclusive: 10_000_000, label: "1천만 미만" },
  { lowerBoundInclusive: 10_000_000, upperBoundExclusive: 30_000_000, label: "1천만 ~ 3천만" },
  { lowerBoundInclusive: 30_000_000, upperBoundExclusive: 70_000_000, label: "3천만 ~ 7천만" },
  { lowerBoundInclusive: 70_000_000, upperBoundExclusive: 150_000_000, label: "7천만 ~ 1억 5천만" },
  { lowerBoundInclusive: 150_000_000, upperBoundExclusive: 300_000_000, label: "1억 5천만 ~ 3억" },
  { lowerBoundInclusive: 300_000_000, upperBoundExclusive: 600_000_000, label: "3억 ~ 6억" },
  { lowerBoundInclusive: 600_000_000, upperBoundExclusive: 1_000_000_000, label: "6억 ~ 10억" },
  { lowerBoundInclusive: 1_000_000_000, upperBoundExclusive: null, label: "10억 이상" }
];

function clampAwardedScore(wrongAttemptsOnStage) {
  return Math.max(60, 120 - wrongAttemptsOnStage * 20);
}

function randomIndex(randomFn, length) {
  return Math.floor(randomFn() * length);
}

function shuffle(items, randomFn) {
  const next = [...items];
  for (let index = next.length - 1; index > 0; index -= 1) {
    const swapIndex = randomIndex(randomFn, index + 1);
    [next[index], next[swapIndex]] = [next[swapIndex], next[index]];
  }
  return next;
}

function normalizeCountryName(name) {
  return typeof name === "string" ? name.trim().toLowerCase() : "";
}

function formatPopulation(population) {
  return `${Number(population).toLocaleString("ko-KR")}명`;
}

function resolvePopulationScaleBand(population) {
  const band = POPULATION_SCALE_BANDS.find((candidate) => (
    population >= candidate.lowerBoundInclusive
      && (candidate.upperBoundExclusive === null || population < candidate.upperBoundExclusive)
  ));

  if (!band) {
    throw new Error(`지원하지 않는 인구 규모입니다: ${population}`);
  }

  return band;
}

function readBestScore() {
  try {
    return Number(window.localStorage.getItem(POPULATION_STORAGE_KEY) || 0);
  } catch (_error) {
    return 0;
  }
}

function writeBestScore(score) {
  try {
    const currentBest = readBestScore();
    const nextBest = Math.max(currentBest, score);
    window.localStorage.setItem(POPULATION_STORAGE_KEY, String(nextBest));
    return nextBest;
  } catch (_error) {
    return score;
  }
}

function recordPopulationRun(session) {
  recordDemoLiteGameRun({
    mode: "population",
    totalScore: session.totalScore,
    correctAnswers: session.correctAnswers,
    totalAttempts: session.totalAttempts,
    status: session.status,
    bestScore: session.bestScore
  });
}

export function buildPopulationQuestionPool(countries) {
  const seenNames = new Set();

  return countries
    .filter(
      (country) =>
        typeof country?.iso3Code === "string"
        && typeof country?.nameKr === "string"
        && country.nameKr.trim()
        && Number.isFinite(Number(country?.population))
        && Number(country.population) > 0
    )
    .map((country) => ({
      iso3Code: country.iso3Code,
      countryName: country.nameKr.trim(),
      population: Number(country.population),
      populationYear: Number.isFinite(Number(country.populationYear)) ? Number(country.populationYear) : null
    }))
    .filter((entry) => {
      const normalized = normalizeCountryName(entry.countryName);
      if (!normalized || seenNames.has(normalized)) {
        return false;
      }
      seenNames.add(normalized);
      return true;
    });
}

function buildPopulationOptions(targetCountry) {
  const correctBand = resolvePopulationScaleBand(targetCountry.population);
  const correctBandIndex = POPULATION_SCALE_BANDS.findIndex((band) => band.label === correctBand.label);
  const windowStart = Math.max(0, Math.min(correctBandIndex - 1, POPULATION_SCALE_BANDS.length - 4));
  const optionBands = POPULATION_SCALE_BANDS.slice(windowStart, windowStart + 4);

  return {
    correctOptionNumber: correctBandIndex - windowStart + 1,
    correctOptionLabel: correctBand.label,
    options: optionBands.map((band, index) => ({
      optionNumber: index + 1,
      label: band.label,
      lowerBoundInclusive: band.lowerBoundInclusive
    }))
  };
}

function buildRounds(pool, randomFn) {
  const targets = shuffle(pool, randomFn).slice(0, POPULATION_STAGE_COUNT);
  if (targets.length < POPULATION_STAGE_COUNT) {
    throw new Error("demo-lite 인구수 퀴즈를 만들 만큼 충분한 국가 데이터가 없습니다.");
  }

  return targets.map((target, index) => {
    const optionSet = buildPopulationOptions(target);

    return {
      stageNumber: index + 1,
      targetCountryName: target.countryName,
      populationYear: target.populationYear,
      correctPopulation: target.population,
      correctOptionNumber: optionSet.correctOptionNumber,
      correctOptionLabel: optionSet.correctOptionLabel,
      options: optionSet.options
    };
  });
}

export function createPopulationDemoSession(countries, randomFn = Math.random) {
  const pool = buildPopulationQuestionPool(countries);
  const rounds = buildRounds(pool, randomFn);

  return {
    rounds,
    stageIndex: 0,
    livesRemaining: POPULATION_INITIAL_LIVES,
    totalScore: 0,
    totalAttempts: 0,
    correctAnswers: 0,
    wrongAttemptsOnStage: 0,
    status: "IN_PROGRESS",
    history: [],
    bestScore: readBestScore()
  };
}

export function getCurrentPopulationRound(session) {
  return session.rounds[session.stageIndex] ?? null;
}

export function submitPopulationAnswer(session, selectedOptionNumber) {
  if (session.status !== "IN_PROGRESS") {
    throw new Error("이미 종료된 세션입니다.");
  }

  const round = getCurrentPopulationRound(session);
  if (!round) {
    throw new Error("현재 인구수 문제를 찾지 못했습니다.");
  }

  const selectedOption = round.options.find((option) => option.optionNumber === selectedOptionNumber);
  if (!selectedOption) {
    throw new Error("선택한 인구수 보기 번호가 유효하지 않습니다.");
  }

  const correct = selectedOption.optionNumber === round.correctOptionNumber;
  session.totalAttempts += 1;

  if (correct) {
    const awardedScore = clampAwardedScore(session.wrongAttemptsOnStage);
    session.totalScore += awardedScore;
    session.correctAnswers += 1;
    session.history.push({
      stageNumber: round.stageNumber,
      targetCountryName: round.targetCountryName,
      populationYear: round.populationYear,
      correctOptionLabel: round.correctOptionLabel,
      correctPopulation: round.correctPopulation,
      outcome: session.wrongAttemptsOnStage > 0 ? `오답 ${session.wrongAttemptsOnStage}회 후 정답` : "1차 정답",
      awardedScore,
      livesRemaining: session.livesRemaining
    });

    session.wrongAttemptsOnStage = 0;

    const lastRound = session.stageIndex === session.rounds.length - 1;
    if (lastRound) {
      session.status = "FINISHED";
      session.bestScore = writeBestScore(session.totalScore);
      recordPopulationRun(session);
      return {
        correct: true,
        outcome: "FINISHED",
        awardedScore,
        totalScore: session.totalScore,
        livesRemaining: session.livesRemaining,
        stageNumber: round.stageNumber,
        targetCountryName: round.targetCountryName,
        populationYear: round.populationYear,
        correctOptionLabel: round.correctOptionLabel,
        correctPopulation: round.correctPopulation,
        nextStageNumber: null
      };
    }

    session.stageIndex += 1;
    return {
      correct: true,
      outcome: "CONTINUE",
      awardedScore,
      totalScore: session.totalScore,
      livesRemaining: session.livesRemaining,
      stageNumber: round.stageNumber,
      targetCountryName: round.targetCountryName,
      populationYear: round.populationYear,
      correctOptionLabel: round.correctOptionLabel,
      correctPopulation: round.correctPopulation,
      nextStageNumber: session.stageIndex + 1
    };
  }

  session.livesRemaining -= 1;
  session.wrongAttemptsOnStage += 1;
  session.history.push({
    stageNumber: round.stageNumber,
    targetCountryName: round.targetCountryName,
    populationYear: round.populationYear,
    outcome: "오답",
    awardedScore: 0,
    livesRemaining: session.livesRemaining
  });

  if (session.livesRemaining <= 0) {
    session.status = "GAME_OVER";
    session.bestScore = writeBestScore(session.totalScore);
    recordPopulationRun(session);
    return {
      correct: false,
      outcome: "GAME_OVER",
      awardedScore: 0,
      totalScore: session.totalScore,
      livesRemaining: session.livesRemaining,
      stageNumber: round.stageNumber,
      targetCountryName: round.targetCountryName,
      populationYear: round.populationYear,
      selectedOptionLabel: selectedOption.label,
      nextStageNumber: null
    };
  }

  return {
    correct: false,
    outcome: "RETRY",
    awardedScore: 0,
    totalScore: session.totalScore,
    livesRemaining: session.livesRemaining,
    stageNumber: round.stageNumber,
    targetCountryName: round.targetCountryName,
    populationYear: round.populationYear,
    selectedOptionLabel: selectedOption.label,
    nextStageNumber: round.stageNumber
  };
}

function renderStatusCards(session, round) {
  return `
    <div class="demo-status-strip demo-status-strip--game">
      <article class="demo-status-card" data-tone="population">
        <span>현재 문제</span>
        <strong>${round.stageNumber} / ${session.rounds.length}</strong>
      </article>
      <article class="demo-status-card" data-tone="population">
        <span>기준 연도</span>
        <strong>${round.populationYear || "-"}</strong>
      </article>
      <article class="demo-status-card" data-tone="population">
        <span>남은 하트</span>
        <strong>${session.livesRemaining}</strong>
      </article>
      <article class="demo-status-card" data-tone="population">
        <span>총점</span>
        <strong>${session.totalScore}</strong>
      </article>
      <article class="demo-status-card" data-tone="population" data-mobile-hidden="true">
        <span>최고 점수</span>
        <strong>${session.bestScore}</strong>
      </article>
    </div>
  `;
}

function renderQuestionCard(round, disabled) {
  const options = round.options
    .map(
      (option) => `
        <button class="demo-option-button" type="button" data-population-option-number="${option.optionNumber}" ${
          disabled ? "disabled" : ""
        }>
          <span class="demo-option-label">${option.optionNumber}</span>
          <strong>${option.label}</strong>
        </button>
      `
    )
    .join("");

  return `
    <section class="demo-route-hero" data-tone="population">
      <div class="demo-route-hero-top">
        <span class="demo-chip">인구수 퀴즈</span>
        <span class="demo-chip">5문제</span>
        <span class="demo-chip">3하트</span>
      </div>
      <h1>${round.targetCountryName}</h1>
      <div class="demo-route-meta">
        <p>기준 연도 ${round.populationYear || "-"}년 기준으로 가장 가까운 인구 규모 구간을 고르세요. 틀리면 같은 문제를 다시 풉니다.</p>
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>인구 규모 보기</h2>
        <p>보기 4개 중 가장 가까운 구간 하나를 고르세요.</p>
      </div>
      <div class="demo-option-grid">
        ${options}
      </div>
    </section>

    <section id="population-demo-feedback" class="demo-panel demo-panel--muted" hidden></section>
  `;
}

function renderResult(session) {
  const title = session.status === "FINISHED" ? "모든 문제를 마쳤습니다." : "하트를 모두 잃었습니다.";
  const summary = session.history
    .filter((item) => item.outcome !== "오답")
    .map(
      (item) => `
        <li>
          <strong>${item.stageNumber}단계</strong>
          <span>${item.targetCountryName} (${item.populationYear || "-"})</span>
          <span>${item.correctOptionLabel}</span>
          <span>${item.outcome}</span>
          <span>${formatPopulation(item.correctPopulation)}</span>
          <span>+${item.awardedScore}</span>
        </li>
      `
    )
    .join("");

  return `
    <section class="demo-route-hero" data-tone="population">
      <div class="demo-route-hero-top">
        <span class="demo-chip">인구수 결과</span>
        <span class="demo-chip">${session.status === "FINISHED" ? "클리어" : "게임 오버"}</span>
      </div>
      <h1>${title}</h1>
      <div class="demo-route-meta">
        <p>총점 ${session.totalScore}점, 정답 ${session.correctAnswers}개, 총 제출 ${session.totalAttempts}회, 최고 점수 ${session.bestScore}점입니다.</p>
      </div>
      <div class="demo-actions">
        <button class="demo-button" type="button" data-population-demo-action="restart">다시 하기</button>
        <a class="demo-ghost" href="#/">홈으로 돌아가기</a>
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>맞힌 문제 요약</h2>
        <p>맞힌 구간과 실제 인구만 다시 볼 수 있습니다.</p>
      </div>
      <ul class="demo-history-list">
        ${summary || "<li><span>아직 맞힌 문제가 없습니다.</span></li>"}
      </ul>
    </section>
  `;
}

function renderFeedback(target, payload) {
  target.hidden = false;
  target.innerHTML = payload.correct
    ? `
      <div class="demo-panel-head">
        <h2>정답입니다</h2>
        <p>${payload.correctOptionLabel} · 실제 인구 ${formatPopulation(payload.correctPopulation)} · +${payload.awardedScore}점</p>
      </div>
    `
    : `
      <div class="demo-panel-head">
        <h2>${payload.outcome === "GAME_OVER" ? "탈락했습니다" : "오답입니다"}</h2>
        <p>${
          payload.outcome === "GAME_OVER"
            ? "하트를 모두 잃어 이번 판이 끝났습니다."
            : `방금 고른 구간은 ${payload.selectedOptionLabel}입니다. 하트 ${payload.livesRemaining}개가 남았습니다.`
        }</p>
      </div>
    `;
}

export function renderPopulationGamePage() {
  return `<section id="demo-population-root" class="demo-layout"></section>`;
}

export function mountPopulationGame(container, countries) {
  if (!container) {
    return () => {};
  }

  let session = createPopulationDemoSession(countries);
  let transitionTimer = null;
  let interactionLocked = false;

  function clearTransitionTimer() {
    if (transitionTimer) {
      window.clearTimeout(transitionTimer);
      transitionTimer = null;
    }
  }

  function rerenderCurrentRound() {
    const round = getCurrentPopulationRound(session);
    if (!round) {
      container.innerHTML = renderResult(session);
      attachResultActions();
      return;
    }

    container.innerHTML = `
      ${renderQuestionCard(round, interactionLocked)}
      ${renderStatusCards(session, round)}
    `;
    attachOptionActions();
  }

  function rerenderResult() {
    container.innerHTML = renderResult(session);
    attachResultActions();
  }

  function attachResultActions() {
    container.querySelector("[data-population-demo-action='restart']")?.addEventListener("click", () => {
      clearTransitionTimer();
      session = createPopulationDemoSession(countries);
      interactionLocked = false;
      rerenderCurrentRound();
    });
  }

  function attachOptionActions() {
    container.querySelectorAll("[data-population-option-number]").forEach((button) => {
      button.addEventListener("click", () => {
        if (interactionLocked) {
          return;
        }

        interactionLocked = true;
        container.querySelectorAll("[data-population-option-number]").forEach((candidate) => {
          candidate.setAttribute("disabled", "disabled");
        });

        const payload = submitPopulationAnswer(session, Number(button.dataset.populationOptionNumber));
        const feedbackBox = container.querySelector("#population-demo-feedback");
        renderFeedback(feedbackBox, payload);

        if (payload.correct && payload.outcome === "CONTINUE") {
          transitionTimer = window.setTimeout(() => {
            interactionLocked = false;
            rerenderCurrentRound();
          }, CORRECT_ADVANCE_DELAY_MS);
          return;
        }

        if (!payload.correct && payload.outcome === "RETRY") {
          transitionTimer = window.setTimeout(() => {
            interactionLocked = false;
            rerenderCurrentRound();
          }, WRONG_RETRY_DELAY_MS);
          return;
        }

        transitionTimer = window.setTimeout(() => {
          interactionLocked = false;
          rerenderResult();
        }, FINISH_DELAY_MS);
      });
    });
  }

  rerenderCurrentRound();
  return () => clearTransitionTimer();
}
