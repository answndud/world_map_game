const HISTORY_STORAGE_KEY = "worldmap-demo-lite:recent-history";
const CAPITAL_BEST_SCORE_KEY = "worldmap-demo-lite:capital-best-score";
const FLAG_BEST_SCORE_KEY = "worldmap-demo-lite:flag-best-score";
const POPULATION_BATTLE_BEST_SCORE_KEY = "worldmap-demo-lite:population-battle-best-score";
const MAX_RECENT_ENTRIES = 8;

const MODE_META = {
  capital: { label: "수도", title: "수도 퀴즈" },
  flag: { label: "국기", title: "국기 퀴즈" },
  "population-battle": { label: "배틀", title: "인구 비교 배틀" },
  recommendation: { label: "추천", title: "국가 추천" }
};

const SCORE_KEYS = {
  capital: CAPITAL_BEST_SCORE_KEY,
  flag: FLAG_BEST_SCORE_KEY,
  "population-battle": POPULATION_BATTLE_BEST_SCORE_KEY
};

function formatScoreLabel(score) {
  return score > 0 ? `${score}점` : "아직 없음";
}

function resolveStorage(storage) {
  if (storage) {
    return storage;
  }

  try {
    return window.localStorage;
  } catch (_error) {
    return null;
  }
}

function safeReadJson(storage, key, fallbackValue) {
  if (!storage) {
    return fallbackValue;
  }

  try {
    const raw = storage.getItem(key);
    if (!raw) {
      return fallbackValue;
    }
    return JSON.parse(raw);
  } catch (_error) {
    return fallbackValue;
  }
}

function safeWriteJson(storage, key, value) {
  if (!storage) {
    return;
  }

  try {
    storage.setItem(key, JSON.stringify(value));
  } catch (_error) {
    // ignore storage failures in demo-lite
  }
}

function safeReadNumber(storage, key) {
  if (!storage) {
    return 0;
  }

  try {
    return Number(storage.getItem(key) || 0);
  } catch (_error) {
    return 0;
  }
}

function nextHistoryEntry(baseEntry, now) {
  return {
    id: `${baseEntry.mode}-${now}-${Math.random().toString(36).slice(2, 8)}`,
    recordedAt: now,
    ...baseEntry
  };
}

function writeRecentEntries(storage, entries) {
  safeWriteJson(storage, HISTORY_STORAGE_KEY, entries.slice(0, MAX_RECENT_ENTRIES));
}

function readRecentEntries(storage) {
  const value = safeReadJson(storage, HISTORY_STORAGE_KEY, []);
  return Array.isArray(value) ? value : [];
}

export function recordDemoLiteGameRun(
  {
    mode,
    totalScore,
    correctAnswers,
    totalAttempts,
    status,
    bestScore
  },
  options = {}
) {
  const storage = resolveStorage(options.storage);
  const now = options.now ?? Date.now();
  const meta = MODE_META[mode];

  if (!meta) {
    throw new Error(`Unknown demo-lite history mode: ${mode}`);
  }

  const entry = nextHistoryEntry(
    {
      type: "game",
      mode,
      title: meta.title,
      status,
      totalScore,
      correctAnswers,
      totalAttempts,
      bestScore,
      summary:
        status === "FINISHED"
          ? `${totalScore}점 · ${correctAnswers}개 정답으로 클리어`
          : `${totalScore}점 · ${correctAnswers}개 정답에서 탈락`
    },
    now
  );

  const nextEntries = [entry, ...readRecentEntries(storage)];
  writeRecentEntries(storage, nextEntries);
  return entry;
}

export function recordDemoLiteRecommendationResult(
  {
    topRecommendationName,
    topRecommendationIso3Code,
    recommendationCount
  },
  options = {}
) {
  const storage = resolveStorage(options.storage);
  const now = options.now ?? Date.now();

  const entry = nextHistoryEntry(
    {
      type: "recommendation",
      mode: "recommendation",
      title: MODE_META.recommendation.title,
      topRecommendationName,
      topRecommendationIso3Code,
      recommendationCount,
      summary: `TOP1 ${topRecommendationName} · 결과 ${recommendationCount}개`
    },
    now
  );

  const nextEntries = [entry, ...readRecentEntries(storage)];
  writeRecentEntries(storage, nextEntries);
  return entry;
}

function buildModeSnapshot(mode, entries, storage) {
  const meta = MODE_META[mode];
  const modeEntries = entries.filter((entry) => entry.mode === mode);

  if (mode === "recommendation") {
    const latest = modeEntries.find((entry) => entry.type === "recommendation") || null;
    return {
      mode,
      label: meta.label,
      title: meta.title,
      primary: latest ? latest.topRecommendationName : "아직 없음",
      secondary: `결과 계산 ${modeEntries.length}회`
    };
  }

  const bestScore = safeReadNumber(storage, SCORE_KEYS[mode]);
  return {
    mode,
    label: meta.label,
    title: meta.title,
    primary: formatScoreLabel(bestScore),
    secondary: `완료 기록 ${modeEntries.length}회`
  };
}

function buildRecentGameStreak(entries) {
  const gameEntries = entries.filter((entry) => entry.type === "game");

  if (gameEntries.length === 0) {
    return {
      mode: null,
      modeLabel: "아직 없음",
      modeRunCount: 0,
      clearRunCount: 0,
      label: "연속 플레이 없음",
      clearLabel: "연속 클리어 없음"
    };
  }

  const latestGame = gameEntries[0];
  const latestMode = latestGame.mode;
  const latestModeLabel = MODE_META[latestMode]?.label || latestMode;

  let modeRunCount = 0;
  for (const entry of gameEntries) {
    if (entry.mode !== latestMode) {
      break;
    }
    modeRunCount += 1;
  }

  let clearRunCount = 0;
  for (const entry of gameEntries) {
    if (entry.status !== "FINISHED") {
      break;
    }
    clearRunCount += 1;
  }

  return {
    mode: latestMode,
    modeLabel: latestModeLabel,
    modeRunCount,
    clearRunCount,
    label: `${latestModeLabel} ${modeRunCount}판 연속`,
    clearLabel: clearRunCount > 0 ? `${clearRunCount}판 연속 클리어` : "연속 클리어 없음"
  };
}

function buildShareSummaryText(summary) {
  if (summary.totalTrackedEntries === 0) {
    return "WorldMap 플레이 기록: 아직 기록이 없습니다. 게임을 한 판 하거나 추천 결과를 먼저 확인해 보세요.";
  }

  return [
    "WorldMap 플레이 기록",
    `최근 기록 ${summary.totalTrackedEntries}개`,
    `${summary.activeModeCount}개 모드 체험`,
    `최고 점수 ${formatScoreLabel(summary.highestScore)}`,
    `연속 플레이 ${summary.recentGameStreak.label}`,
    `최근 추천 ${summary.latestRecommendationName}`
  ].join(" · ");
}

export function readDemoLiteActivitySummary(options = {}) {
  const storage = resolveStorage(options.storage);
  const recentEntries = readRecentEntries(storage);
  const highestScore = Math.max(
    safeReadNumber(storage, CAPITAL_BEST_SCORE_KEY),
    safeReadNumber(storage, FLAG_BEST_SCORE_KEY),
    safeReadNumber(storage, POPULATION_BATTLE_BEST_SCORE_KEY)
  );
  const recentModes = new Set(recentEntries.map((entry) => entry.mode));
  const latestRecommendation = recentEntries.find((entry) => entry.type === "recommendation") || null;
  const recentGameStreak = buildRecentGameStreak(recentEntries);

  const summary = {
    recentEntries: recentEntries.slice(0, 5),
    totalTrackedEntries: recentEntries.length,
    activeModeCount: recentModes.size,
    highestScore,
    latestRecommendationName: latestRecommendation?.topRecommendationName || "아직 없음",
    recentGameStreak,
    modeSnapshots: [
      buildModeSnapshot("capital", recentEntries, storage),
      buildModeSnapshot("flag", recentEntries, storage),
      buildModeSnapshot("population-battle", recentEntries, storage),
      buildModeSnapshot("recommendation", recentEntries, storage)
    ]
  };

  return {
    ...summary,
    shareSummaryText: buildShareSummaryText(summary)
  };
}
