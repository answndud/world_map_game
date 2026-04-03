import { recordDemoLiteGameRun } from "../lib/browser-history.js";

const CAPITAL_STAGE_COUNT = 5;
const CAPITAL_INITIAL_LIVES = 3;
const CAPITAL_STORAGE_KEY = "worldmap-demo-lite:capital-best-score";
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

export function buildCapitalQuestionPool(countries) {
  const usedCapitalNames = new Set();
  return countries
    .filter(
      (country) =>
        typeof country?.nameKr === "string" &&
        country.nameKr.trim() &&
        typeof country?.capitalCityKr === "string" &&
        country.capitalCityKr.trim()
    )
    .map((country) => ({
      iso3Code: country.iso3Code,
      countryName: country.nameKr.trim(),
      capitalName: country.capitalCityKr.trim(),
      continent: country.continent || "UNKNOWN"
    }))
    .filter((entry) => {
      const normalizedCapital = entry.capitalName.toLowerCase();
      if (usedCapitalNames.has(normalizedCapital)) {
        return false;
      }
      usedCapitalNames.add(normalizedCapital);
      return true;
    });
}

function pickDistinctOptions(pool, correctEntry, randomFn) {
  const distractors = shuffle(
    pool.filter(
      (entry) => entry.iso3Code !== correctEntry.iso3Code && entry.capitalName !== correctEntry.capitalName
    ),
    randomFn
  ).slice(0, 3);

  if (distractors.length < 3) {
    throw new Error("수도 보기 4개를 만들 만큼 충분한 국가 데이터가 없습니다.");
  }

  return shuffle(
    [correctEntry.capitalName, ...distractors.map((entry) => entry.capitalName)],
    randomFn
  ).map((capitalName, index) => ({
    optionNumber: index + 1,
    capitalName
  }));
}

function buildRounds(pool, randomFn) {
  const targets = shuffle(pool, randomFn).slice(0, CAPITAL_STAGE_COUNT);
  if (targets.length < CAPITAL_STAGE_COUNT) {
    throw new Error("demo-lite 수도 게임을 만들 만큼 충분한 국가 데이터가 없습니다.");
  }

  return targets.map((target, index) => ({
    stageNumber: index + 1,
    targetCountryName: target.countryName,
    correctCapitalName: target.capitalName,
    options: pickDistinctOptions(pool, target, randomFn)
  }));
}

function readBestScore() {
  try {
    return Number(window.localStorage.getItem(CAPITAL_STORAGE_KEY) || 0);
  } catch (_error) {
    return 0;
  }
}

function writeBestScore(score) {
  try {
    const currentBest = readBestScore();
    const nextBest = Math.max(currentBest, score);
    window.localStorage.setItem(CAPITAL_STORAGE_KEY, String(nextBest));
    return nextBest;
  } catch (_error) {
    return score;
  }
}

function recordCapitalRun(session) {
  recordDemoLiteGameRun({
    mode: "capital",
    totalScore: session.totalScore,
    correctAnswers: session.correctAnswers,
    totalAttempts: session.totalAttempts,
    status: session.status,
    bestScore: session.bestScore
  });
}

export function createCapitalDemoSession(countries, randomFn = Math.random) {
  const pool = buildCapitalQuestionPool(countries);
  const rounds = buildRounds(pool, randomFn);

  return {
    rounds,
    stageIndex: 0,
    livesRemaining: CAPITAL_INITIAL_LIVES,
    totalScore: 0,
    totalAttempts: 0,
    correctAnswers: 0,
    wrongAttemptsOnStage: 0,
    status: "IN_PROGRESS",
    history: [],
    bestScore: readBestScore()
  };
}

export function getCurrentCapitalRound(session) {
  return session.rounds[session.stageIndex] ?? null;
}

export function submitCapitalAnswer(session, selectedOptionNumber) {
  if (session.status !== "IN_PROGRESS") {
    throw new Error("이미 종료된 세션입니다.");
  }

  const round = getCurrentCapitalRound(session);
  if (!round) {
    throw new Error("현재 수도 문제를 찾지 못했습니다.");
  }

  const selectedOption = round.options.find((option) => option.optionNumber === selectedOptionNumber);
  if (!selectedOption) {
    throw new Error("선택한 수도 보기 번호가 유효하지 않습니다.");
  }

  const correct = selectedOption.capitalName === round.correctCapitalName;
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
      recordCapitalRun(session);
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
    recordCapitalRun(session);
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
      <article class="demo-status-card" data-tone="capital">
        <span>현재 문제</span>
        <strong>${round.stageNumber} / ${session.rounds.length}</strong>
      </article>
      <article class="demo-status-card" data-tone="capital">
        <span>남은 하트</span>
        <strong>${session.livesRemaining}</strong>
      </article>
      <article class="demo-status-card" data-tone="capital">
        <span>총점</span>
        <strong>${session.totalScore}</strong>
      </article>
      <article class="demo-status-card" data-tone="capital" data-mobile-hidden="true">
        <span>최고 점수</span>
        <strong>${session.bestScore}</strong>
      </article>
    </div>
  `;
}

function renderQuestionCard(session, round, disabled) {
  const options = round.options
    .map(
      (option) => `
        <button class="demo-option-button" type="button" data-option-number="${option.optionNumber}" ${
          disabled ? "disabled" : ""
        }>
          <span class="demo-option-label">${option.optionNumber}</span>
          <strong>${option.capitalName}</strong>
        </button>
      `
    )
    .join("");

  return `
    <section class="demo-route-hero" data-tone="capital">
      <div class="demo-route-hero-top">
        <span class="demo-chip">수도 퀴즈</span>
        <span class="demo-chip">5문제</span>
        <span class="demo-chip">3하트</span>
      </div>
      <h1>${round.targetCountryName}</h1>
      <div class="demo-route-meta">
        <p>나라 이름에 맞는 수도를 고르세요. 틀리면 같은 문제를 다시 풀고, 맞히면 다음 문제로 넘어갑니다.</p>
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>수도 보기</h2>
        <p>보기 4개 중 하나를 골라 정답을 맞혀 보세요.</p>
      </div>
      <div class="demo-option-grid">
        ${options}
      </div>
    </section>

    <section id="capital-demo-feedback" class="demo-panel demo-panel--muted" hidden></section>
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
    <section class="demo-route-hero" data-tone="capital">
      <div class="demo-route-hero-top">
        <span class="demo-chip">수도 결과</span>
        <span class="demo-chip">${session.status === "FINISHED" ? "클리어" : "게임 오버"}</span>
      </div>
      <h1>${title}</h1>
      <div class="demo-route-meta">
        <p>총점 ${session.totalScore}점, 정답 ${session.correctAnswers}개, 총 제출 ${session.totalAttempts}회, 최고 점수 ${session.bestScore}점입니다.</p>
      </div>
      <div class="demo-actions">
        <button class="demo-button" type="button" data-capital-demo-action="restart">다시 하기</button>
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

export function renderCapitalGamePage() {
  return `<section id="demo-capital-root" class="demo-layout"></section>`;
}

export function mountCapitalGame(container, countries) {
  if (!container) {
    return () => {};
  }

  let session = createCapitalDemoSession(countries);
  let transitionTimer = null;
  let interactionLocked = false;

  function clearTransitionTimer() {
    if (transitionTimer) {
      window.clearTimeout(transitionTimer);
      transitionTimer = null;
    }
  }

  function rerenderCurrentRound() {
    const round = getCurrentCapitalRound(session);
    if (!round) {
      container.innerHTML = renderResult(session);
      attachResultActions();
      return;
    }

    container.innerHTML = `
      ${renderQuestionCard(session, round, interactionLocked)}
      ${renderStatusCards(session, round)}
    `;
    attachOptionActions();
  }

  function rerenderResult() {
    container.innerHTML = renderResult(session);
    attachResultActions();
  }

  function attachResultActions() {
    container.querySelector("[data-capital-demo-action='restart']")?.addEventListener("click", () => {
      clearTransitionTimer();
      session = createCapitalDemoSession(countries);
      interactionLocked = false;
      rerenderCurrentRound();
    });
  }

  function attachOptionActions() {
    container.querySelectorAll("[data-option-number]").forEach((button) => {
      button.addEventListener("click", () => {
        if (interactionLocked) {
          return;
        }

        interactionLocked = true;
        container.querySelectorAll("[data-option-number]").forEach((candidate) => {
          candidate.setAttribute("disabled", "disabled");
        });

        const payload = submitCapitalAnswer(session, Number(button.dataset.optionNumber));
        const feedbackBox = container.querySelector("#capital-demo-feedback");
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
