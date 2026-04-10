import test from "node:test";
import assert from "node:assert/strict";

import {
  buildPopulationQuestionPool,
  createPopulationDemoSession,
  submitPopulationAnswer
} from "../src/features/population-game.js";

const FIXTURE_COUNTRIES = [
  { iso3Code: "ISL", nameKr: "아이슬란드", population: 393396, populationYear: 2024 },
  { iso3Code: "JOR", nameKr: "요르단", population: 11552876, populationYear: 2024 },
  { iso3Code: "KOR", nameKr: "대한민국", population: 51717590, populationYear: 2024 },
  { iso3Code: "THA", nameKr: "태국", population: 71702435, populationYear: 2024 },
  { iso3Code: "MEX", nameKr: "멕시코", population: 130861007, populationYear: 2024 },
  { iso3Code: "USA", nameKr: "미국", population: 345426571, populationYear: 2024 },
  { iso3Code: "IND", nameKr: "인도", population: 1450935791, populationYear: 2024 }
];

test("buildPopulationQuestionPool keeps only unique named countries that have population data", () => {
  const pool = buildPopulationQuestionPool([
    ...FIXTURE_COUNTRIES,
    { iso3Code: "ALT", nameKr: "대한민국", population: 1000, populationYear: 2024 },
    { iso3Code: "BAD", nameKr: "누락국", population: null, populationYear: 2024 }
  ]);

  assert.equal(pool.length, FIXTURE_COUNTRIES.length);
  assert.ok(pool.every((entry) => entry.countryName));
  assert.ok(pool.every((entry) => entry.population > 0));
  assert.equal(new Set(pool.map((entry) => entry.countryName)).size, pool.length);
});

test("population session starts with five rounds and four scale-band options", () => {
  const session = createPopulationDemoSession(FIXTURE_COUNTRIES, () => 0);
  const round = session.rounds[0];

  assert.equal(session.rounds.length, 5);
  assert.equal(session.livesRemaining, 3);
  assert.ok(round.targetCountryName);
  assert.equal(round.populationYear, 2024);
  assert.equal(round.options.length, 4);
  assert.equal(new Set(round.options.map((option) => option.label)).size, 4);
  assert.ok(round.correctOptionNumber >= 1 && round.correctOptionNumber <= 4);
  assert.equal(round.options[round.correctOptionNumber - 1].label, round.correctOptionLabel);
});

test("submitPopulationAnswer keeps the same round on wrong answers and decreases lives", () => {
  const session = createPopulationDemoSession(FIXTURE_COUNTRIES, () => 0);
  const wrongOption = session.rounds[0].options.find(
    (option) => option.optionNumber !== session.rounds[0].correctOptionNumber
  );

  const payload = submitPopulationAnswer(session, wrongOption.optionNumber);

  assert.equal(payload.outcome, "RETRY");
  assert.equal(session.status, "IN_PROGRESS");
  assert.equal(session.stageIndex, 0);
  assert.equal(session.livesRemaining, 2);
  assert.equal(session.wrongAttemptsOnStage, 1);
});

test("submitPopulationAnswer advances to the next round on correct answers and awards score", () => {
  const session = createPopulationDemoSession(FIXTURE_COUNTRIES, () => 0);
  const payload = submitPopulationAnswer(session, session.rounds[0].correctOptionNumber);

  assert.equal(payload.outcome, "CONTINUE");
  assert.equal(session.status, "IN_PROGRESS");
  assert.equal(session.stageIndex, 1);
  assert.equal(session.correctAnswers, 1);
  assert.equal(session.totalScore, 120);
});

test("submitPopulationAnswer finishes the run after the fifth cleared round", () => {
  const session = createPopulationDemoSession(FIXTURE_COUNTRIES, () => 0);

  session.stageIndex = 4;
  session.correctAnswers = 4;
  session.totalScore = 480;
  const payload = submitPopulationAnswer(session, session.rounds[4].correctOptionNumber);

  assert.equal(payload.outcome, "FINISHED");
  assert.equal(session.status, "FINISHED");
  assert.equal(session.correctAnswers, 5);
  assert.equal(session.totalScore, 600);
});
