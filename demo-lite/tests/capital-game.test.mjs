import test from "node:test";
import assert from "node:assert/strict";

import {
  buildCapitalQuestionPool,
  createCapitalDemoSession,
  submitCapitalAnswer
} from "../src/features/capital-game.js";

const FIXTURE_COUNTRIES = [
  { iso3Code: "KOR", nameKr: "대한민국", capitalCityKr: "서울", continent: "ASIA" },
  { iso3Code: "JPN", nameKr: "일본", capitalCityKr: "도쿄", continent: "ASIA" },
  { iso3Code: "THA", nameKr: "태국", capitalCityKr: "방콕", continent: "ASIA" },
  { iso3Code: "MYS", nameKr: "말레이시아", capitalCityKr: "쿠알라룸푸르", continent: "ASIA" },
  { iso3Code: "FRA", nameKr: "프랑스", capitalCityKr: "파리", continent: "EUROPE" },
  { iso3Code: "ESP", nameKr: "스페인", capitalCityKr: "마드리드", continent: "EUROPE" }
];

test("buildCapitalQuestionPool keeps only unique korean capital entries", () => {
  const pool = buildCapitalQuestionPool([
    ...FIXTURE_COUNTRIES,
    { iso3Code: "ALT", nameKr: "대체국", capitalCityKr: "서울", continent: "ASIA" },
    { iso3Code: "BAD", nameKr: "누락국", capitalCityKr: "", continent: "ASIA" }
  ]);

  assert.equal(pool.length, FIXTURE_COUNTRIES.length);
  assert.ok(pool.every((country) => country.capitalName));
  assert.equal(new Set(pool.map((country) => country.capitalName)).size, pool.length);
});

test("capital session starts with one correct option plus three distractors", () => {
  const session = createCapitalDemoSession(FIXTURE_COUNTRIES, () => 0);
  const round = session.rounds[0];

  assert.ok(round.targetCountryName);
  assert.ok(round.correctCapitalName);
  assert.equal(round.options.length, 4);
  assert.equal(round.options.filter((option) => option.capitalName === round.correctCapitalName).length, 1);
  assert.equal(new Set(round.options.map((option) => option.capitalName)).size, 4);
});

test("submitCapitalAnswer keeps the same round on wrong answers and decreases lives", () => {
  const session = createCapitalDemoSession(FIXTURE_COUNTRIES, () => 0);
  const wrongOption = session.rounds[0].options.find(
    (option) => option.capitalName !== session.rounds[0].correctCapitalName
  );

  const payload = submitCapitalAnswer(session, wrongOption.optionNumber);

  assert.equal(payload.outcome, "RETRY");
  assert.equal(session.status, "IN_PROGRESS");
  assert.equal(session.stageIndex, 0);
  assert.equal(session.livesRemaining, 2);
  assert.equal(session.wrongAttemptsOnStage, 1);
});

test("submitCapitalAnswer advances to the next round on correct answers and awards score", () => {
  const session = createCapitalDemoSession(FIXTURE_COUNTRIES, () => 0);
  const correctOption = session.rounds[0].options.find(
    (option) => option.capitalName === session.rounds[0].correctCapitalName
  );

  const payload = submitCapitalAnswer(session, correctOption.optionNumber);

  assert.equal(payload.outcome, "CONTINUE");
  assert.equal(session.status, "IN_PROGRESS");
  assert.equal(session.stageIndex, 1);
  assert.equal(session.correctAnswers, 1);
  assert.equal(session.totalScore, 120);
});

test("submitCapitalAnswer finishes the run after the fifth cleared round", () => {
  const session = createCapitalDemoSession(FIXTURE_COUNTRIES, () => 0);

  session.stageIndex = 4;
  session.correctAnswers = 4;
  session.totalScore = 480;
  const correctOption = session.rounds[4].options.find(
    (option) => option.capitalName === session.rounds[4].correctCapitalName
  );

  const payload = submitCapitalAnswer(session, correctOption.optionNumber);

  assert.equal(payload.outcome, "FINISHED");
  assert.equal(session.status, "FINISHED");
  assert.equal(session.totalScore, 600);
  assert.equal(session.correctAnswers, 5);
});
