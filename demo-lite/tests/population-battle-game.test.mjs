import test from "node:test";
import assert from "node:assert/strict";

import {
  buildPopulationBattleQuestionPool,
  createPopulationBattleDemoSession,
  submitPopulationBattleAnswer
} from "../src/features/population-battle-game.js";

const FIXTURE_COUNTRIES = [
  { iso3Code: "IND", nameKr: "인도", continent: "ASIA", population: 1450935791 },
  { iso3Code: "USA", nameKr: "미국", continent: "NORTH_AMERICA", population: 345426571 },
  { iso3Code: "IDN", nameKr: "인도네시아", continent: "ASIA", population: 283487931 },
  { iso3Code: "BRA", nameKr: "브라질", continent: "SOUTH_AMERICA", population: 212812405 },
  { iso3Code: "PAK", nameKr: "파키스탄", continent: "ASIA", population: 251269164 },
  { iso3Code: "NGA", nameKr: "나이지리아", continent: "AFRICA", population: 232679478 },
  { iso3Code: "JPN", nameKr: "일본", continent: "ASIA", population: 123201945 },
  { iso3Code: "DEU", nameKr: "독일", continent: "EUROPE", population: 84552242 },
  { iso3Code: "FRA", nameKr: "프랑스", continent: "EUROPE", population: 66548530 },
  { iso3Code: "KOR", nameKr: "대한민국", continent: "ASIA", population: 51717590 },
  { iso3Code: "ESP", nameKr: "스페인", continent: "EUROPE", population: 47889958 },
  { iso3Code: "THA", nameKr: "태국", continent: "ASIA", population: 71702435 }
];

test("buildPopulationBattleQuestionPool keeps unique named countries sorted by population", () => {
  const pool = buildPopulationBattleQuestionPool([
    ...FIXTURE_COUNTRIES,
    { iso3Code: "ALT", nameKr: "대한민국", continent: "ASIA", population: 1000 },
    { iso3Code: "BAD", nameKr: "누락국", continent: "ASIA", population: null }
  ]);

  assert.equal(pool.length, FIXTURE_COUNTRIES.length);
  assert.equal(pool[0].countryName, "인도");
  assert.equal(pool[0].populationRank, 1);
  assert.ok(pool.every((entry, index, items) => index === 0 || items[index - 1].population >= entry.population));
});

test("population battle session starts with five rounds and two options per round", () => {
  const session = createPopulationBattleDemoSession(FIXTURE_COUNTRIES, () => 0);
  const round = session.rounds[0];

  assert.equal(session.rounds.length, 5);
  assert.equal(session.livesRemaining, 3);
  assert.equal(round.questionPrompt, "두 나라 중 인구가 더 많은 나라를 고르세요.");
  assert.ok(round.difficultyLabel.startsWith("Band "));
  assert.ok(round.leftCountry.countryName);
  assert.ok(round.rightCountry.countryName);
  assert.ok(round.correctOptionNumber === 1 || round.correctOptionNumber === 2);
});

test("submitPopulationBattleAnswer keeps the same round on wrong answers and decreases lives", () => {
  const session = createPopulationBattleDemoSession(FIXTURE_COUNTRIES, () => 0);
  const wrongOptionNumber = session.rounds[0].correctOptionNumber === 1 ? 2 : 1;

  const payload = submitPopulationBattleAnswer(session, wrongOptionNumber);

  assert.equal(payload.outcome, "RETRY");
  assert.equal(session.status, "IN_PROGRESS");
  assert.equal(session.stageIndex, 0);
  assert.equal(session.livesRemaining, 2);
  assert.equal(session.wrongAttemptsOnStage, 1);
});

test("submitPopulationBattleAnswer advances to the next round on correct answers and awards stage-aware score", () => {
  const session = createPopulationBattleDemoSession(FIXTURE_COUNTRIES, () => 0);
  const payload = submitPopulationBattleAnswer(session, session.rounds[0].correctOptionNumber);

  assert.equal(payload.outcome, "CONTINUE");
  assert.equal(session.status, "IN_PROGRESS");
  assert.equal(session.stageIndex, 1);
  assert.equal(session.correctAnswers, 1);
  assert.equal(session.totalScore, 150);
});

test("submitPopulationBattleAnswer finishes the run after the fifth cleared round", () => {
  const session = createPopulationBattleDemoSession(FIXTURE_COUNTRIES, () => 0);

  session.stageIndex = 4;
  session.correctAnswers = 4;
  session.totalScore = 600;
  const payload = submitPopulationBattleAnswer(session, session.rounds[4].correctOptionNumber);

  assert.equal(payload.outcome, "FINISHED");
  assert.equal(session.status, "FINISHED");
  assert.equal(session.correctAnswers, 5);
  assert.equal(session.totalScore, 810);
});
