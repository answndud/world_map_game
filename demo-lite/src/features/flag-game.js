import { recordDemoLiteGameRun } from "../lib/browser-history.js";

const FLAG_STAGE_COUNT = 5;
const FLAG_INITIAL_LIVES = 3;
const FLAG_STORAGE_KEY = "worldmap-demo-lite:flag-best-score";
const CORRECT_ADVANCE_DELAY_MS = 950;
const WRONG_RETRY_DELAY_MS = 950;
const FINISH_DELAY_MS = 1100;

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

function demoLiteFlagPath(relativePath) {
  if (typeof relativePath !== "string" || !relativePath.trim()) {
    return null;
  }
  return relativePath.replace("/images/flags/", "/generated/flags/");
}

function readBestScore() {
  try {
    return Number(window.localStorage.getItem(FLAG_STORAGE_KEY) || 0);
  } catch (_error) {
    return 0;
  }
}

function writeBestScore(score) {
  try {
    const currentBest = readBestScore();
    const nextBest = Math.max(currentBest, score);
    window.localStorage.setItem(FLAG_STORAGE_KEY, String(nextBest));
    return nextBest;
  } catch (_error) {
    return score;
  }
}

function recordFlagRun(session) {
  recordDemoLiteGameRun({
    mode: "flag",
    totalScore: session.totalScore,
    correctAnswers: session.correctAnswers,
    totalAttempts: session.totalAttempts,
    status: session.status,
    bestScore: session.bestScore
  });
}

export function buildFlagQuestionPool(countries, flagAssets) {
  const assetMap = new Map(
    flagAssets
      .map((asset) => [asset.iso3Code, demoLiteFlagPath(asset.relativePath)])
      .filter(([, relativePath]) => Boolean(relativePath))
  );

  const seenCountryNames = new Set();

  return countries
    .filter(
      (country) =>
        typeof country?.iso3Code === "string" &&
        typeof country?.nameKr === "string" &&
        country.nameKr.trim() &&
        assetMap.has(country.iso3Code)
    )
    .map((country) => ({
      iso3Code: country.iso3Code,
      countryName: country.nameKr.trim(),
      continent: country.continent || "UNKNOWN",
      flagPath: assetMap.get(country.iso3Code)
    }))
    .filter((country) => {
      const normalized = normalizeCountryName(country.countryName);
      if (!normalized || seenCountryNames.has(normalized)) {
        return false;
      }
      seenCountryNames.add(normalized);
      return true;
    });
}

function fallbackContinents(continent) {
  switch (continent) {
    case "AFRICA":
      return ["EUROPE", "ASIA", "SOUTH_AMERICA", "NORTH_AMERICA", "OCEANIA"];
    case "ASIA":
      return ["OCEANIA", "EUROPE", "AFRICA", "NORTH_AMERICA", "SOUTH_AMERICA"];
    case "EUROPE":
      return ["AFRICA", "ASIA", "NORTH_AMERICA", "SOUTH_AMERICA", "OCEANIA"];
    case "NORTH_AMERICA":
      return ["SOUTH_AMERICA", "EUROPE", "ASIA", "OCEANIA", "AFRICA"];
    case "OCEANIA":
      return ["ASIA", "NORTH_AMERICA", "EUROPE", "SOUTH_AMERICA", "AFRICA"];
    case "SOUTH_AMERICA":
      return ["NORTH_AMERICA", "EUROPE", "AFRICA", "ASIA", "OCEANIA"];
    default:
      return [];
  }
}

function collectDistinctCountryNames(source, distractors, usedNames, targetSize, randomFn) {
  if (distractors.length >= targetSize) {
    return;
  }

  const shuffled = shuffle(source, randomFn);
  for (const country of shuffled) {
    if (distractors.length >= targetSize) {
      return;
    }

    const normalized = normalizeCountryName(country.countryName);
    if (!normalized || usedNames.has(normalized)) {
      continue;
    }

    usedNames.add(normalized);
    distractors.push(country.countryName);
  }
}

function pickFlagOptions(pool, targetEntry, randomFn) {
  const usedNames = new Set([normalizeCountryName(targetEntry.countryName)]);
  const distractors = [];
  const remaining = pool.filter((entry) => entry.iso3Code !== targetEntry.iso3Code);

  collectDistinctCountryNames(
    remaining.filter((entry) => entry.continent === targetEntry.continent),
    distractors,
    usedNames,
    3,
    randomFn
  );

  for (const continent of fallbackContinents(targetEntry.continent)) {
    collectDistinctCountryNames(
      remaining.filter((entry) => entry.continent === continent),
      distractors,
      usedNames,
      3,
      randomFn
    );
  }

  collectDistinctCountryNames(remaining, distractors, usedNames, 3, randomFn);

  if (distractors.length < 3) {
    throw new Error("국기 보기 4개를 만들 만큼 충분한 국가 데이터가 없습니다.");
  }

  return shuffle([targetEntry.countryName, ...distractors], randomFn).map((countryName, index) => ({
    optionNumber: index + 1,
    countryName
  }));
}

function buildRounds(pool, randomFn) {
  const targets = shuffle(pool, randomFn).slice(0, FLAG_STAGE_COUNT);
  if (targets.length < FLAG_STAGE_COUNT) {
    throw new Error("demo-lite 국기 게임을 만들 만큼 충분한 자산이 없습니다.");
  }

  return targets.map((target, index) => ({
    stageNumber: index + 1,
    targetCountryName: target.countryName,
    targetFlagPath: target.flagPath,
    options: pickFlagOptions(pool, target, randomFn)
  }));
}

export function createFlagDemoSession(countries, flagAssets, randomFn = Math.random) {
  const pool = buildFlagQuestionPool(countries, flagAssets);
  const rounds = buildRounds(pool, randomFn);

  return {
    rounds,
    stageIndex: 0,
    livesRemaining: FLAG_INITIAL_LIVES,
    totalScore: 0,
    totalAttempts: 0,
    correctAnswers: 0,
    wrongAttemptsOnStage: 0,
    status: "IN_PROGRESS",
    history: [],
    bestScore: readBestScore()
  };
}

export function getCurrentFlagRound(session) {
  return session.rounds[session.stageIndex] ?? null;
}

export function submitFlagAnswer(session, selectedOptionNumber) {
  if (session.status !== "IN_PROGRESS") {
    throw new Error("이미 종료된 세션입니다.");
  }

  const round = getCurrentFlagRound(session);
  if (!round) {
    throw new Error("현재 국기 문제를 찾지 못했습니다.");
  }

  const selectedOption = round.options.find((option) => option.optionNumber === selectedOptionNumber);
  if (!selectedOption) {
    throw new Error("선택한 나라 보기 번호가 유효하지 않습니다.");
  }

  const correct = selectedOption.countryName === round.targetCountryName;
  session.totalAttempts += 1;

  if (correct) {
    const awardedScore = clampAwardedScore(session.wrongAttemptsOnStage);
    session.totalScore += awardedScore;
    session.correctAnswers += 1;
    session.history.push({
      stageNumber: round.stageNumber,
      targetCountryName: round.targetCountryName,
      outcome: session.wrongAttemptsOnStage > 0 ? `오답 ${session.wrongAttemptsOnStage}회 후 정답` : "1차 정답",
      awardedScore,
      livesRemaining: session.livesRemaining
    });

    session.wrongAttemptsOnStage = 0;

    const lastRound = session.stageIndex === session.rounds.length - 1;
    if (lastRound) {
      session.status = "FINISHED";
      session.bestScore = writeBestScore(session.totalScore);
      recordFlagRun(session);
      return {
        correct: true,
        outcome: "FINISHED",
        awardedScore,
        totalScore: session.totalScore,
        livesRemaining: session.livesRemaining,
        stageNumber: round.stageNumber,
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
      nextStageNumber: session.stageIndex + 1
    };
  }

  session.livesRemaining -= 1;
  session.wrongAttemptsOnStage += 1;
  session.history.push({
    stageNumber: round.stageNumber,
    targetCountryName: round.targetCountryName,
    outcome: "오답",
    awardedScore: 0,
    livesRemaining: session.livesRemaining
  });

  if (session.livesRemaining <= 0) {
    session.status = "GAME_OVER";
    session.bestScore = writeBestScore(session.totalScore);
    recordFlagRun(session);
    return {
      correct: false,
      outcome: "GAME_OVER",
      awardedScore: 0,
      totalScore: session.totalScore,
      livesRemaining: session.livesRemaining,
      stageNumber: round.stageNumber,
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
    nextStageNumber: round.stageNumber
  };
}

function renderStatusCards(session, round) {
  return `
    <div class="demo-status-strip demo-status-strip--game">
      <article class="demo-status-card" data-tone="flag">
        <span>현재 문제</span>
        <strong>${round.stageNumber} / ${session.rounds.length}</strong>
      </article>
      <article class="demo-status-card" data-tone="flag">
        <span>남은 하트</span>
        <strong>${session.livesRemaining}</strong>
      </article>
      <article class="demo-status-card" data-tone="flag">
        <span>총점</span>
        <strong>${session.totalScore}</strong>
      </article>
      <article class="demo-status-card" data-tone="flag" data-mobile-hidden="true">
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
        <button class="demo-option-button" type="button" data-flag-option-number="${option.optionNumber}" ${
          disabled ? "disabled" : ""
        }>
          <span class="demo-option-label">${option.optionNumber}</span>
          <strong>${option.countryName}</strong>
        </button>
      `
    )
    .join("");

  return `
    <section class="demo-route-hero" data-tone="flag">
      <div class="demo-route-hero-top">
        <span class="demo-chip">국기 퀴즈</span>
        <span class="demo-chip">5문제</span>
        <span class="demo-chip">3하트</span>
      </div>
      <h1>이 국기는 어느 나라일까요?</h1>
      <div class="demo-route-meta">
        <p>국기만 보고 보기 4개 중 정답을 고르세요. 틀리면 같은 문제를 다시 풀게 됩니다.</p>
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>문제 국기</h2>
        <p>국기를 보고 맞는 나라 이름을 골라 보세요.</p>
      </div>
      <div class="demo-flag-frame">
        <img class="demo-flag-image" src="${round.targetFlagPath}" alt="국기 문제 ${round.stageNumber}번">
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>나라 보기</h2>
      </div>
      <div class="demo-option-grid">
        ${options}
      </div>
    </section>

    <section id="flag-demo-feedback" class="demo-panel demo-panel--muted" hidden></section>
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
          <span>${item.targetCountryName}</span>
          <span>${item.outcome}</span>
          <span>+${item.awardedScore}</span>
        </li>
      `
    )
    .join("");

  return `
    <section class="demo-route-hero" data-tone="flag">
      <div class="demo-route-hero-top">
        <span class="demo-chip">국기 결과</span>
        <span class="demo-chip">${session.status === "FINISHED" ? "클리어" : "게임 오버"}</span>
      </div>
      <h1>${title}</h1>
      <div class="demo-route-meta">
        <p>총점 ${session.totalScore}점, 정답 ${session.correctAnswers}개, 총 제출 ${session.totalAttempts}회, 최고 점수 ${session.bestScore}점입니다.</p>
      </div>
      <div class="demo-actions">
        <button class="demo-button" type="button" data-flag-demo-action="restart">다시 하기</button>
        <a class="demo-ghost" href="#/">홈으로 돌아가기</a>
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>맞힌 문제 요약</h2>
        <p>정답을 맞힌 문제와 획득 점수만 다시 볼 수 있습니다.</p>
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

export function renderFlagGamePage() {
  return `<section id="demo-flag-root" class="demo-layout"></section>`;
}

export function mountFlagGame(container, countries, flagAssets) {
  if (!container) {
    return () => {};
  }

  let session = createFlagDemoSession(countries, flagAssets);
  let transitionTimer = null;
  let interactionLocked = false;

  function clearTransitionTimer() {
    if (transitionTimer) {
      window.clearTimeout(transitionTimer);
      transitionTimer = null;
    }
  }

  function rerenderCurrentRound() {
    const round = getCurrentFlagRound(session);
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
    container.querySelector("[data-flag-demo-action='restart']")?.addEventListener("click", () => {
      clearTransitionTimer();
      session = createFlagDemoSession(countries, flagAssets);
      interactionLocked = false;
      rerenderCurrentRound();
    });
  }

  function attachOptionActions() {
    container.querySelectorAll("[data-flag-option-number]").forEach((button) => {
      button.addEventListener("click", () => {
        if (interactionLocked) {
          return;
        }

        interactionLocked = true;
        container.querySelectorAll("[data-flag-option-number]").forEach((candidate) => {
          candidate.setAttribute("disabled", "disabled");
        });

        const payload = submitFlagAnswer(session, Number(button.dataset.flagOptionNumber));
        const feedbackBox = container.querySelector("#flag-demo-feedback");
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
