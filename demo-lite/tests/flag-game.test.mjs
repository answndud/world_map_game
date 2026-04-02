import test from "node:test";
import assert from "node:assert/strict";

import {
  buildFlagQuestionPool,
  createFlagDemoSession,
  submitFlagAnswer
} from "../src/features/flag-game.js";

const FIXTURE_COUNTRIES = [
  { iso3Code: "KOR", nameKr: "대한민국", continent: "ASIA" },
  { iso3Code: "JPN", nameKr: "일본", continent: "ASIA" },
  { iso3Code: "THA", nameKr: "태국", continent: "ASIA" },
  { iso3Code: "MYS", nameKr: "말레이시아", continent: "ASIA" },
  { iso3Code: "FRA", nameKr: "프랑스", continent: "EUROPE" },
  { iso3Code: "ESP", nameKr: "스페인", continent: "EUROPE" }
];

const FIXTURE_FLAG_ASSETS = [
  { iso3Code: "KOR", relativePath: "/images/flags/kor.svg" },
  { iso3Code: "JPN", relativePath: "/images/flags/jpn.svg" },
  { iso3Code: "THA", relativePath: "/images/flags/tha.svg" },
  { iso3Code: "MYS", relativePath: "/images/flags/mys.svg" },
  { iso3Code: "FRA", relativePath: "/images/flags/fra.svg" },
  { iso3Code: "ESP", relativePath: "/images/flags/esp.svg" }
];

test("buildFlagQuestionPool keeps only countries that have a matching flag asset", () => {
  const pool = buildFlagQuestionPool(
    [...FIXTURE_COUNTRIES, { iso3Code: "USA", nameKr: "미국", continent: "NORTH_AMERICA" }],
    FIXTURE_FLAG_ASSETS
  );

  assert.equal(pool.length, FIXTURE_COUNTRIES.length);
  assert.equal(pool[0].flagPath, "/generated/flags/kor.svg");
});

test("flag session uses same-continent distractors before global fallback when enough entries exist", () => {
  const session = createFlagDemoSession(FIXTURE_COUNTRIES, FIXTURE_FLAG_ASSETS, () => 0);
  const firstRound = session.rounds[0];
  const targetCountry = FIXTURE_COUNTRIES.find((country) => country.nameKr === firstRound.targetCountryName);
  const sameContinentCountryNames = FIXTURE_COUNTRIES.filter(
    (country) => country.continent === targetCountry.continent
  ).map((country) => country.nameKr);

  assert.equal(firstRound.options.length, 4);
  assert.ok(firstRound.targetFlagPath.startsWith("/generated/flags/"));
  assert.ok(firstRound.options.some((option) => option.countryName === firstRound.targetCountryName));
  assert.ok(firstRound.options.every((option) => sameContinentCountryNames.includes(option.countryName)));
});

test("submitFlagAnswer keeps the same round on wrong answers and decreases lives", () => {
  let session = createFlagDemoSession(FIXTURE_COUNTRIES, FIXTURE_FLAG_ASSETS, () => 0);
  session.bestScore = 0;
  const wrongOption = session.rounds[0].options.find((option) => option.countryName !== session.rounds[0].targetCountryName);

  const payload = submitFlagAnswer(session, wrongOption.optionNumber);

  assert.equal(payload.outcome, "RETRY");
  assert.equal(session.status, "IN_PROGRESS");
  assert.equal(session.stageIndex, 0);
  assert.equal(session.livesRemaining, 2);
  assert.equal(session.wrongAttemptsOnStage, 1);
});

test("submitFlagAnswer advances to the next round on correct answers and awards score", () => {
  let session = createFlagDemoSession(FIXTURE_COUNTRIES, FIXTURE_FLAG_ASSETS, () => 0);
  session.bestScore = 0;
  const correctOption = session.rounds[0].options.find((option) => option.countryName === session.rounds[0].targetCountryName);

  const payload = submitFlagAnswer(session, correctOption.optionNumber);

  assert.equal(payload.outcome, "CONTINUE");
  assert.equal(session.stageIndex, 1);
  assert.equal(session.correctAnswers, 1);
  assert.equal(session.totalScore, 120);
});

test("submitFlagAnswer finishes the run after the fifth cleared round", () => {
  let session = createFlagDemoSession(FIXTURE_COUNTRIES, FIXTURE_FLAG_ASSETS, () => 0);
  session.bestScore = 0;
  session.stageIndex = 4;
  session.correctAnswers = 4;
  session.totalScore = 480;

  const correctOption = session.rounds[4].options.find((option) => option.countryName === session.rounds[4].targetCountryName);
  const payload = submitFlagAnswer(session, correctOption.optionNumber);

  assert.equal(payload.outcome, "FINISHED");
  assert.equal(session.status, "FINISHED");
  assert.equal(session.totalScore, 600);
});
