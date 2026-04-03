import { recordDemoLiteGameRun } from "../lib/browser-history.js";

const BATTLE_STAGE_COUNT = 5;
const BATTLE_INITIAL_LIVES = 3;
const BATTLE_STORAGE_KEY = "worldmap-demo-lite:population-battle-best-score";
const QUESTION_PROMPT = "두 나라 중 인구가 더 많은 나라를 고르세요.";
const CORRECT_ADVANCE_DELAY_MS = 950;
const WRONG_RETRY_DELAY_MS = 950;
const FINISH_DELAY_MS = 1100;

const DIFFICULTY_BANDS = [
  { label: "Band A · 큰 격차", minGap: 18, maxGap: 32 },
  { label: "Band B · 넉넉한 차이", minGap: 12, maxGap: 20 },
  { label: "Band C · 근접 비교", minGap: 8, maxGap: 14 },
  { label: "Band D · 고난도", minGap: 5, maxGap: 9 },
  { label: "Band E · 초근접", minGap: 2, maxGap: 6 }
];

const CONTINENT_LABELS = {
  AFRICA: "아프리카",
  ASIA: "아시아",
  EUROPE: "유럽",
  NORTH_AMERICA: "북아메리카",
  OCEANIA: "오세아니아",
  SOUTH_AMERICA: "남아메리카",
  UNKNOWN: "대륙 미상"
};

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

function toContinentLabel(continent) {
  return CONTINENT_LABELS[continent] || CONTINENT_LABELS.UNKNOWN;
}

function readBestScore() {
  try {
    return Number(window.localStorage.getItem(BATTLE_STORAGE_KEY) || 0);
  } catch (_error) {
    return 0;
  }
}

function writeBestScore(score) {
  try {
    const currentBest = readBestScore();
    const nextBest = Math.max(currentBest, score);
    window.localStorage.setItem(BATTLE_STORAGE_KEY, String(nextBest));
    return nextBest;
  } catch (_error) {
    return score;
  }
}

function recordPopulationBattleRun(session) {
  recordDemoLiteGameRun({
    mode: "population-battle",
    totalScore: session.totalScore,
    correctAnswers: session.correctAnswers,
    totalAttempts: session.totalAttempts,
    status: session.status,
    bestScore: session.bestScore
  });
}

function calculateAwardedScore(stageNumber, attemptNumber, livesRemaining) {
  const baseScore = 90 + ((stageNumber - 1) * 15);
  const lifeBonus = livesRemaining * 10;
  const attemptBonus = attemptNumber === 1 ? 30 : attemptNumber === 2 ? 10 : 0;
  return baseScore + lifeBonus + attemptBonus;
}

function buildPairKey(firstIso3Code, secondIso3Code) {
  return [firstIso3Code, secondIso3Code].sort().join(":");
}

function createRoundFromPair(stageNumber, band, morePopulousCountry, lessPopulousCountry, randomFn) {
  const correctOnLeft = randomFn() < 0.5;
  const leftCountry = correctOnLeft ? morePopulousCountry : lessPopulousCountry;
  const rightCountry = correctOnLeft ? lessPopulousCountry : morePopulousCountry;

  return {
    stageNumber,
    difficultyLabel: band.label,
    questionPrompt: QUESTION_PROMPT,
    leftCountry,
    rightCountry,
    correctOptionNumber: correctOnLeft ? 1 : 2
  };
}

function pickPairForBand(pool, band, usedPairKeys, randomFn) {
  const candidates = [];

  for (let higherIndex = 0; higherIndex < pool.length - 1; higherIndex += 1) {
    const minLowerIndex = Math.min(pool.length - 1, higherIndex + band.minGap);
    const maxLowerIndex = Math.min(pool.length - 1, higherIndex + band.maxGap);

    if (minLowerIndex > maxLowerIndex) {
      continue;
    }

    for (let lowerIndex = minLowerIndex; lowerIndex <= maxLowerIndex; lowerIndex += 1) {
      const morePopulousCountry = pool[higherIndex];
      const lessPopulousCountry = pool[lowerIndex];
      const pairKey = buildPairKey(morePopulousCountry.iso3Code, lessPopulousCountry.iso3Code);

      if (usedPairKeys.has(pairKey)) {
        continue;
      }

      candidates.push({
        pairKey,
        morePopulousCountry,
        lessPopulousCountry
      });
    }
  }

  if (candidates.length === 0) {
    for (let higherIndex = 0; higherIndex < pool.length - 1; higherIndex += 1) {
      for (let lowerIndex = higherIndex + 1; lowerIndex < pool.length; lowerIndex += 1) {
        const morePopulousCountry = pool[higherIndex];
        const lessPopulousCountry = pool[lowerIndex];
        const pairKey = buildPairKey(morePopulousCountry.iso3Code, lessPopulousCountry.iso3Code);

        if (usedPairKeys.has(pairKey)) {
          continue;
        }

        candidates.push({
          pairKey,
          morePopulousCountry,
          lessPopulousCountry
        });
      }
    }
  }

  if (candidates.length === 0) {
    throw new Error("demo-lite 인구 배틀 문제를 만들 만큼 충분한 국가 pair가 없습니다.");
  }

  const picked = candidates[randomIndex(randomFn, candidates.length)];
  usedPairKeys.add(picked.pairKey);
  return picked;
}

export function buildPopulationBattleQuestionPool(countries) {
  const seenNames = new Set();

  return countries
    .filter(
      (country) =>
        typeof country?.iso3Code === "string" &&
        typeof country?.nameKr === "string" &&
        country.nameKr.trim() &&
        Number.isFinite(Number(country?.population)) &&
        Number(country.population) > 0
    )
    .map((country) => ({
      iso3Code: country.iso3Code,
      countryName: country.nameKr.trim(),
      continent: country.continent || "UNKNOWN",
      population: Number(country.population)
    }))
    .filter((entry) => {
      const normalized = normalizeCountryName(entry.countryName);
      if (!normalized || seenNames.has(normalized)) {
        return false;
      }
      seenNames.add(normalized);
      return true;
    })
    .sort((left, right) => right.population - left.population)
    .map((entry, index) => ({
      ...entry,
      populationRank: index + 1
    }));
}

function buildRounds(pool, randomFn) {
  if (pool.length < 10) {
    throw new Error("demo-lite 인구 배틀을 만들 만큼 충분한 국가 데이터가 없습니다.");
  }

  const usedPairKeys = new Set();

  return DIFFICULTY_BANDS.map((band, index) => {
    const pair = pickPairForBand(pool, band, usedPairKeys, randomFn);
    return createRoundFromPair(index + 1, band, pair.morePopulousCountry, pair.lessPopulousCountry, randomFn);
  });
}

export function createPopulationBattleDemoSession(countries, randomFn = Math.random) {
  const pool = buildPopulationBattleQuestionPool(countries);
  const rounds = buildRounds(pool, randomFn);

  return {
    rounds,
    stageIndex: 0,
    livesRemaining: BATTLE_INITIAL_LIVES,
    totalScore: 0,
    totalAttempts: 0,
    correctAnswers: 0,
    wrongAttemptsOnStage: 0,
    status: "IN_PROGRESS",
    history: [],
    bestScore: readBestScore()
  };
}

export function getCurrentPopulationBattleRound(session) {
  return session.rounds[session.stageIndex] ?? null;
}

export function submitPopulationBattleAnswer(session, selectedOptionNumber) {
  if (session.status !== "IN_PROGRESS") {
    throw new Error("이미 종료된 세션입니다.");
  }

  if (selectedOptionNumber !== 1 && selectedOptionNumber !== 2) {
    throw new Error("좌우 보기 중 하나를 선택해야 합니다.");
  }

  const round = getCurrentPopulationBattleRound(session);
  if (!round) {
    throw new Error("현재 배틀 Stage를 찾지 못했습니다.");
  }

  const correct = selectedOptionNumber === round.correctOptionNumber;
  session.totalAttempts += 1;

  if (correct) {
    const attemptNumber = session.wrongAttemptsOnStage + 1;
    const awardedScore = calculateAwardedScore(round.stageNumber, attemptNumber, session.livesRemaining);
    session.totalScore += awardedScore;
    session.correctAnswers += 1;
    session.history.push({
      stageNumber: round.stageNumber,
      difficultyLabel: round.difficultyLabel,
      outcome: session.wrongAttemptsOnStage > 0 ? `오답 ${session.wrongAttemptsOnStage}회 후 정답` : "1차 정답",
      awardedScore,
      livesRemaining: session.livesRemaining
    });

    session.wrongAttemptsOnStage = 0;

    const lastRound = session.stageIndex === session.rounds.length - 1;
    if (lastRound) {
      session.status = "FINISHED";
      session.bestScore = writeBestScore(session.totalScore);
      recordPopulationBattleRun(session);
      return {
        correct: true,
        outcome: "FINISHED",
        awardedScore,
        totalScore: session.totalScore,
        livesRemaining: session.livesRemaining,
        stageNumber: round.stageNumber,
        difficultyLabel: round.difficultyLabel,
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
      difficultyLabel: round.difficultyLabel,
      nextStageNumber: session.stageIndex + 1
    };
  }

  session.livesRemaining -= 1;
  session.wrongAttemptsOnStage += 1;
  session.history.push({
    stageNumber: round.stageNumber,
    difficultyLabel: round.difficultyLabel,
    outcome: "오답",
    awardedScore: 0,
    livesRemaining: session.livesRemaining
  });

  if (session.livesRemaining <= 0) {
    session.status = "GAME_OVER";
    session.bestScore = writeBestScore(session.totalScore);
    recordPopulationBattleRun(session);
    return {
      correct: false,
      outcome: "GAME_OVER",
      awardedScore: 0,
      totalScore: session.totalScore,
      livesRemaining: session.livesRemaining,
      stageNumber: round.stageNumber,
      difficultyLabel: round.difficultyLabel,
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
    difficultyLabel: round.difficultyLabel,
    nextStageNumber: round.stageNumber
  };
}

function renderStatusCards(session, round) {
  return `
    <div class="demo-status-strip demo-status-strip--game">
      <article class="demo-status-card" data-tone="battle">
        <span>현재 문제</span>
        <strong>${round.stageNumber} / ${session.rounds.length}</strong>
      </article>
      <article class="demo-status-card" data-tone="battle" data-mobile-hidden="true">
        <span>난이도</span>
        <strong>${round.difficultyLabel}</strong>
      </article>
      <article class="demo-status-card" data-tone="battle">
        <span>남은 하트</span>
        <strong>${session.livesRemaining}</strong>
      </article>
      <article class="demo-status-card" data-tone="battle">
        <span>총점</span>
        <strong>${session.totalScore}</strong>
      </article>
      <article class="demo-status-card" data-tone="battle" data-mobile-hidden="true">
        <span>최고 점수</span>
        <strong>${session.bestScore}</strong>
      </article>
    </div>
  `;
}

function renderBattleOption(optionNumber, country, disabled) {
  return `
    <button class="demo-battle-option" type="button" data-battle-option-number="${optionNumber}" ${
      disabled ? "disabled" : ""
    }>
      <span class="demo-option-label">${optionNumber === 1 ? "왼쪽 선택" : "오른쪽 선택"}</span>
      <strong>${country.countryName}</strong>
      <span class="demo-battle-meta">${toContinentLabel(country.continent)} · 인구 순위 ${country.populationRank}위권</span>
    </button>
  `;
}

function renderQuestionCard(round, disabled) {
  return `
    <section class="demo-route-hero" data-tone="battle">
      <div class="demo-route-hero-top">
        <span class="demo-chip">인구 배틀</span>
        <span class="demo-chip">5문제</span>
        <span class="demo-chip">3하트</span>
      </div>
      <h1>더 인구가 많은 나라를 고르세요</h1>
      <div class="demo-route-meta">
        <p>두 나라 중 인구가 더 많은 쪽을 고르세요. 틀리면 같은 문제를 다시 풀고, 맞히면 다음 비교로 넘어갑니다.</p>
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>현재 비교</h2>
        <p>${round.difficultyLabel} · ${QUESTION_PROMPT}</p>
      </div>
      <div class="demo-battle-grid">
        ${renderBattleOption(1, round.leftCountry, disabled)}
        ${renderBattleOption(2, round.rightCountry, disabled)}
      </div>
    </section>

    <section id="population-battle-demo-feedback" class="demo-panel demo-panel--muted" hidden></section>
  `;
}

function renderResult(session) {
  const title = session.status === "FINISHED" ? "모든 비교를 마쳤습니다." : "하트를 모두 잃었습니다.";
  const summary = session.history
    .filter((item) => item.outcome !== "오답")
    .map(
      (item) => `
        <li>
          <strong>${item.stageNumber}단계</strong>
          <span>${item.difficultyLabel}</span>
          <span>${item.outcome}</span>
          <span>+${item.awardedScore}</span>
        </li>
      `
    )
    .join("");

  return `
    <section class="demo-route-hero" data-tone="battle">
      <div class="demo-route-hero-top">
        <span class="demo-chip">배틀 결과</span>
        <span class="demo-chip">${session.status === "FINISHED" ? "클리어" : "게임 오버"}</span>
      </div>
      <h1>${title}</h1>
      <div class="demo-route-meta">
        <p>총점 ${session.totalScore}점, 정답 ${session.correctAnswers}개, 총 제출 ${session.totalAttempts}회, 최고 점수 ${session.bestScore}점입니다.</p>
      </div>
      <div class="demo-actions">
        <button class="demo-button" type="button" data-population-battle-demo-action="restart">다시 하기</button>
        <a class="demo-ghost" href="#/">홈으로 돌아가기</a>
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>맞힌 문제 요약</h2>
        <p>어느 구간에서 맞혔는지와 획득 점수만 다시 볼 수 있습니다.</p>
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
        <p>+${payload.awardedScore}점을 얻었습니다. 잠시 뒤 다음 문제로 이동합니다.</p>
      </div>
    `
    : `
      <div class="demo-panel-head">
        <h2>${payload.outcome === "GAME_OVER" ? "탈락했습니다" : "오답입니다"}</h2>
        <p>${
          payload.outcome === "GAME_OVER"
            ? "하트를 모두 잃어 이번 판이 끝났습니다."
            : `하트 ${payload.livesRemaining}개가 남았습니다. 잠시 뒤 같은 문제를 다시 풉니다.`
        }</p>
      </div>
    `;
}

export function renderPopulationBattleGamePage() {
  return `<section id="demo-population-battle-root" class="demo-layout"></section>`;
}

export function mountPopulationBattleGame(container, countries) {
  if (!container) {
    return () => {};
  }

  let session = createPopulationBattleDemoSession(countries);
  let transitionTimer = null;
  let interactionLocked = false;

  function clearTransitionTimer() {
    if (transitionTimer) {
      window.clearTimeout(transitionTimer);
      transitionTimer = null;
    }
  }

  function rerenderCurrentRound() {
    const round = getCurrentPopulationBattleRound(session);
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
    container.querySelector("[data-population-battle-demo-action='restart']")?.addEventListener("click", () => {
      clearTransitionTimer();
      session = createPopulationBattleDemoSession(countries);
      interactionLocked = false;
      rerenderCurrentRound();
    });
  }

  function attachOptionActions() {
    container.querySelectorAll("[data-battle-option-number]").forEach((button) => {
      button.addEventListener("click", () => {
        if (interactionLocked) {
          return;
        }

        interactionLocked = true;
        container.querySelectorAll("[data-battle-option-number]").forEach((candidate) => {
          candidate.setAttribute("disabled", "disabled");
        });

        const payload = submitPopulationBattleAnswer(session, Number(button.dataset.battleOptionNumber));
        const feedbackBox = container.querySelector("#population-battle-demo-feedback");
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
