import test from "node:test";
import assert from "node:assert/strict";

import {
  readDemoLiteActivitySummary,
  recordDemoLiteGameRun,
  recordDemoLiteRecommendationResult
} from "../src/lib/browser-history.js";

function createMemoryStorage(seed = {}) {
  const store = new Map(Object.entries(seed));
  return {
    getItem(key) {
      return store.has(key) ? store.get(key) : null;
    },
    setItem(key, value) {
      store.set(key, String(value));
    }
  };
}

test("recordDemoLiteGameRun prepends recent game entries and readDemoLiteActivitySummary reflects best scores", () => {
  const storage = createMemoryStorage({
    "worldmap-demo-lite:capital-best-score": "480",
    "worldmap-demo-lite:flag-best-score": "360",
    "worldmap-demo-lite:population-best-score": "620",
    "worldmap-demo-lite:population-battle-best-score": "540"
  });

  recordDemoLiteGameRun(
    {
      mode: "capital",
      totalScore: 420,
      correctAnswers: 5,
      totalAttempts: 6,
      status: "FINISHED",
      bestScore: 480
    },
    { storage, now: 1000 }
  );

  recordDemoLiteGameRun(
    {
      mode: "flag",
      totalScore: 240,
      correctAnswers: 2,
      totalAttempts: 4,
      status: "GAME_OVER",
      bestScore: 360
    },
    { storage, now: 2000 }
  );

  const summary = readDemoLiteActivitySummary({ storage });

  assert.equal(summary.totalTrackedEntries, 2);
  assert.equal(summary.activeModeCount, 2);
  assert.equal(summary.highestScore, 620);
  assert.equal(summary.recentEntries[0].mode, "flag");
  assert.equal(summary.recentEntries[1].mode, "capital");
  assert.equal(summary.modeSnapshots.find((snapshot) => snapshot.mode === "capital").primary, "480점");
  assert.equal(summary.modeSnapshots.find((snapshot) => snapshot.mode === "flag").primary, "360점");
  assert.equal(summary.modeSnapshots.find((snapshot) => snapshot.mode === "population").primary, "620점");
});

test("recommendation entries expose latest top1 in activity summary", () => {
  const storage = createMemoryStorage();

  recordDemoLiteRecommendationResult(
    {
      topRecommendationName: "싱가포르",
      topRecommendationIso3Code: "SGP",
      recommendationCount: 3
    },
    { storage, now: 3000 }
  );

  const summary = readDemoLiteActivitySummary({ storage });
  const recommendationSnapshot = summary.modeSnapshots.find((snapshot) => snapshot.mode === "recommendation");

  assert.equal(summary.latestRecommendationName, "싱가포르");
  assert.equal(summary.recentEntries[0].mode, "recommendation");
  assert.equal(recommendationSnapshot.primary, "싱가포르");
  assert.equal(recommendationSnapshot.secondary, "결과 계산 1회");
});

test("activity summary derives recent game streak and shareable summary text", () => {
  const storage = createMemoryStorage({
    "worldmap-demo-lite:capital-best-score": "510"
  });

  recordDemoLiteGameRun(
    {
      mode: "capital",
      totalScore: 310,
      correctAnswers: 3,
      totalAttempts: 4,
      status: "FINISHED",
      bestScore: 510
    },
    { storage, now: 1000 }
  );

  recordDemoLiteRecommendationResult(
    {
      topRecommendationName: "싱가포르",
      topRecommendationIso3Code: "SGP",
      recommendationCount: 3
    },
    { storage, now: 1500 }
  );

  recordDemoLiteGameRun(
    {
      mode: "capital",
      totalScore: 360,
      correctAnswers: 4,
      totalAttempts: 5,
      status: "FINISHED",
      bestScore: 510
    },
    { storage, now: 2000 }
  );

  recordDemoLiteGameRun(
    {
      mode: "capital",
      totalScore: 420,
      correctAnswers: 5,
      totalAttempts: 5,
      status: "FINISHED",
      bestScore: 510
    },
    { storage, now: 3000 }
  );

  const summary = readDemoLiteActivitySummary({ storage });

  assert.equal(summary.recentGameStreak.label, "수도 3판 연속");
  assert.equal(summary.recentGameStreak.clearLabel, "3판 연속 클리어");
  assert.match(summary.shareSummaryText, /연속 플레이 수도 3판 연속/);
  assert.match(summary.shareSummaryText, /최근 추천 싱가포르/);
});
