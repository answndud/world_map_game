import test from "node:test";
import assert from "node:assert/strict";

import {
  calculateRecommendationResult,
  DEMO_LITE_RECOMMENDATION_ENGINE_VERSION,
  DEMO_LITE_RECOMMENDATION_PROFILE_COUNT,
  DEMO_LITE_RECOMMENDATION_QUESTION_COUNT,
  DEMO_LITE_RECOMMENDATION_SURVEY_VERSION
} from "../src/features/recommendation.js";

function fixtureCountry(iso3Code, nameKr, nameEn, continent, capitalCityKr, population) {
  return { iso3Code, nameKr, nameEn, continent, capitalCityKr, population };
}

const FIXTURE_COUNTRIES = [
  fixtureCountry("CAN", "캐나다", "Canada", "NORTH_AMERICA", "오타와", 40126723),
  fixtureCountry("USA", "미국", "United States", "NORTH_AMERICA", "워싱턴 D.C.", 345426571),
  fixtureCountry("AUS", "호주", "Australia", "OCEANIA", "캔버라", 26713205),
  fixtureCountry("NZL", "뉴질랜드", "New Zealand", "OCEANIA", "웰링턴", 5267189),
  fixtureCountry("SGP", "싱가포르", "Singapore", "ASIA", "싱가포르", 5917648),
  fixtureCountry("JPN", "일본", "Japan", "ASIA", "도쿄", 123201945),
  fixtureCountry("GBR", "영국", "United Kingdom", "EUROPE", "런던", 69138192),
  fixtureCountry("IRL", "아일랜드", "Ireland", "EUROPE", "더블린", 5307600),
  fixtureCountry("DEU", "독일", "Germany", "EUROPE", "베를린", 84552242),
  fixtureCountry("FRA", "프랑스", "France", "EUROPE", "파리", 66548120),
  fixtureCountry("ITA", "이탈리아", "Italy", "EUROPE", "로마", 58997201),
  fixtureCountry("SWE", "스웨덴", "Sweden", "EUROPE", "스톡홀름", 10551707),
  fixtureCountry("DNK", "덴마크", "Denmark", "EUROPE", "코펜하겐", 5987021),
  fixtureCountry("NOR", "노르웨이", "Norway", "EUROPE", "오슬로", 5576660),
  fixtureCountry("FIN", "핀란드", "Finland", "EUROPE", "헬싱키", 5629142),
  fixtureCountry("ESP", "스페인", "Spain", "EUROPE", "마드리드", 47889958),
  fixtureCountry("PRT", "포르투갈", "Portugal", "EUROPE", "리스본", 10467366),
  fixtureCountry("CHE", "스위스", "Switzerland", "EUROPE", "베른", 8921981),
  fixtureCountry("AUT", "오스트리아", "Austria", "EUROPE", "빈", 9132380),
  fixtureCountry("NLD", "네덜란드", "Netherlands", "EUROPE", "암스테르담", 18228742),
  fixtureCountry("KOR", "대한민국", "South Korea", "ASIA", "서울", 51717590),
  fixtureCountry("ARE", "아랍에미리트", "United Arab Emirates", "ASIA", "아부다비", 11346770),
  fixtureCountry("THA", "태국", "Thailand", "ASIA", "방콕", 71702435),
  fixtureCountry("MYS", "말레이시아", "Malaysia", "ASIA", "쿠알라룸푸르", 35649000),
  fixtureCountry("VNM", "베트남", "Vietnam", "ASIA", "하노이", 101598527),
  fixtureCountry("CHL", "칠레", "Chile", "SOUTH_AMERICA", "산티아고", 19764471),
  fixtureCountry("URY", "우루과이", "Uruguay", "SOUTH_AMERICA", "몬테비데오", 3386588),
  fixtureCountry("BRA", "브라질", "Brazil", "SOUTH_AMERICA", "브라질리아", 211998573),
  fixtureCountry("MEX", "멕시코", "Mexico", "NORTH_AMERICA", "멕시코시티", 130861007),
  fixtureCountry("ZAF", "남아프리카 공화국", "South Africa", "AFRICA", "프리토리아", 64007187)
];

test("demo-lite recommendation keeps 20 questions and 30 country profiles", () => {
  assert.equal(DEMO_LITE_RECOMMENDATION_SURVEY_VERSION, "survey-v4");
  assert.equal(DEMO_LITE_RECOMMENDATION_ENGINE_VERSION, "engine-v20");
  assert.equal(DEMO_LITE_RECOMMENDATION_QUESTION_COUNT, 20);
  assert.equal(DEMO_LITE_RECOMMENDATION_PROFILE_COUNT, 30);
});

test("calculateRecommendationResult returns top three candidates and submitted summaries", () => {
  const result = calculateRecommendationResult(FIXTURE_COUNTRIES, {
    climatePreference: "MILD",
    seasonStylePreference: "BALANCED",
    seasonTolerance: "MEDIUM",
    pacePreference: "BALANCED",
    crowdPreference: "BALANCED",
    costQualityPreference: "BALANCED",
    housingPreference: "BALANCED",
    environmentPreference: "MIXED",
    mobilityPreference: "BALANCED",
    englishSupportNeed: "MEDIUM",
    newcomerSupportNeed: "MEDIUM",
    safetyPriority: "MEDIUM",
    publicServicePriority: "MEDIUM",
    digitalConveniencePriority: "MEDIUM",
    foodImportance: "MEDIUM",
    diversityImportance: "MEDIUM",
    cultureLeisureImportance: "MEDIUM",
    workLifePreference: "BALANCED",
    settlementPreference: "BALANCED",
    futureBasePreference: "BALANCED"
  });

  assert.equal(result.submittedPreferences.length, 20);
  assert.equal(result.recommendations.length, 3);
  assert.equal(result.recommendations[0].rank, 1);
  assert.ok(result.recommendations[0].countryNameKr);
  assert.ok(result.recommendations[0].capitalCity);
  assert.ok(result.recommendations[0].reasons.length >= 1);
});

test("warm fast premium city-oriented answers rank singapore first", () => {
  const result = calculateRecommendationResult(FIXTURE_COUNTRIES, {
    climatePreference: "WARM",
    seasonStylePreference: "STABLE",
    seasonTolerance: "HIGH",
    pacePreference: "FAST",
    crowdPreference: "LIVELY",
    costQualityPreference: "QUALITY_FIRST",
    housingPreference: "CENTER_FIRST",
    environmentPreference: "CITY",
    mobilityPreference: "TRANSIT_FIRST",
    englishSupportNeed: "HIGH",
    newcomerSupportNeed: "HIGH",
    safetyPriority: "HIGH",
    publicServicePriority: "HIGH",
    digitalConveniencePriority: "HIGH",
    foodImportance: "HIGH",
    diversityImportance: "HIGH",
    cultureLeisureImportance: "HIGH",
    workLifePreference: "DRIVE_FIRST",
    settlementPreference: "BALANCED",
    futureBasePreference: "BALANCED"
  });

  assert.equal(result.recommendations[0].iso3Code, "SGP");
  assert.ok(result.recommendations.slice(0, 3).some((candidate) => candidate.iso3Code === "ARE"));
});

test("cold calm nature-oriented answers rank new zealand first", () => {
  const result = calculateRecommendationResult(FIXTURE_COUNTRIES, {
    climatePreference: "COLD",
    seasonStylePreference: "DISTINCT",
    seasonTolerance: "MEDIUM",
    pacePreference: "RELAXED",
    crowdPreference: "CALM",
    costQualityPreference: "BALANCED",
    housingPreference: "SPACE_FIRST",
    environmentPreference: "NATURE",
    mobilityPreference: "BALANCED",
    englishSupportNeed: "MEDIUM",
    newcomerSupportNeed: "LOW",
    safetyPriority: "HIGH",
    publicServicePriority: "MEDIUM",
    digitalConveniencePriority: "LOW",
    foodImportance: "LOW",
    diversityImportance: "LOW",
    cultureLeisureImportance: "LOW",
    workLifePreference: "LIFE_FIRST",
    settlementPreference: "BALANCED",
    futureBasePreference: "BALANCED"
  });

  assert.equal(result.recommendations[0].iso3Code, "NZL");
  assert.ok(result.recommendations.slice(0, 3).some((candidate) => ["FIN", "NOR", "SWE"].includes(candidate.iso3Code)));
});

test("warm affordable foodie answers rank malaysia first", () => {
  const result = calculateRecommendationResult(FIXTURE_COUNTRIES, {
    climatePreference: "WARM",
    seasonStylePreference: "BALANCED",
    seasonTolerance: "MEDIUM",
    pacePreference: "BALANCED",
    crowdPreference: "BALANCED",
    costQualityPreference: "VALUE_FIRST",
    housingPreference: "BALANCED",
    environmentPreference: "MIXED",
    mobilityPreference: "BALANCED",
    englishSupportNeed: "MEDIUM",
    newcomerSupportNeed: "MEDIUM",
    safetyPriority: "MEDIUM",
    publicServicePriority: "MEDIUM",
    digitalConveniencePriority: "MEDIUM",
    foodImportance: "HIGH",
    diversityImportance: "MEDIUM",
    cultureLeisureImportance: "MEDIUM",
    workLifePreference: "BALANCED",
    settlementPreference: "BALANCED",
    futureBasePreference: "BALANCED"
  });

  assert.equal(result.recommendations[0].iso3Code, "MYS");
  assert.ok(result.recommendations.slice(0, 3).some((candidate) => candidate.iso3Code === "THA"));
});

test("temperate high english global city answers rank united states first", () => {
  const result = calculateRecommendationResult(FIXTURE_COUNTRIES, {
    climatePreference: "MILD",
    seasonStylePreference: "STABLE",
    seasonTolerance: "LOW",
    pacePreference: "FAST",
    crowdPreference: "LIVELY",
    costQualityPreference: "QUALITY_FIRST",
    housingPreference: "BALANCED",
    environmentPreference: "CITY",
    mobilityPreference: "BALANCED",
    englishSupportNeed: "HIGH",
    newcomerSupportNeed: "HIGH",
    safetyPriority: "LOW",
    publicServicePriority: "LOW",
    digitalConveniencePriority: "HIGH",
    foodImportance: "LOW",
    diversityImportance: "HIGH",
    cultureLeisureImportance: "HIGH",
    workLifePreference: "DRIVE_FIRST",
    settlementPreference: "BALANCED",
    futureBasePreference: "BALANCED"
  });

  assert.equal(result.recommendations[0].iso3Code, "USA");
});

test("temperate family bridge answers rank canada first", () => {
  const result = calculateRecommendationResult(FIXTURE_COUNTRIES, {
    climatePreference: "MILD",
    seasonStylePreference: "BALANCED",
    seasonTolerance: "LOW",
    pacePreference: "BALANCED",
    crowdPreference: "BALANCED",
    costQualityPreference: "QUALITY_FIRST",
    housingPreference: "BALANCED",
    environmentPreference: "MIXED",
    mobilityPreference: "BALANCED",
    englishSupportNeed: "HIGH",
    newcomerSupportNeed: "HIGH",
    safetyPriority: "HIGH",
    publicServicePriority: "MEDIUM",
    digitalConveniencePriority: "MEDIUM",
    foodImportance: "LOW",
    diversityImportance: "LOW",
    cultureLeisureImportance: "MEDIUM",
    workLifePreference: "BALANCED",
    settlementPreference: "BALANCED",
    futureBasePreference: "BALANCED"
  });

  assert.equal(result.recommendations[0].iso3Code, "CAN");
});

test("warm value balanced public answers rank malaysia first", () => {
  const result = calculateRecommendationResult(FIXTURE_COUNTRIES, {
    climatePreference: "WARM",
    seasonStylePreference: "STABLE",
    seasonTolerance: "MEDIUM",
    pacePreference: "BALANCED",
    crowdPreference: "BALANCED",
    costQualityPreference: "VALUE_FIRST",
    housingPreference: "BALANCED",
    environmentPreference: "MIXED",
    mobilityPreference: "BALANCED",
    englishSupportNeed: "MEDIUM",
    newcomerSupportNeed: "MEDIUM",
    safetyPriority: "MEDIUM",
    publicServicePriority: "HIGH",
    digitalConveniencePriority: "MEDIUM",
    foodImportance: "MEDIUM",
    diversityImportance: "MEDIUM",
    cultureLeisureImportance: "MEDIUM",
    workLifePreference: "BALANCED",
    settlementPreference: "BALANCED",
    futureBasePreference: "BALANCED"
  });

  assert.equal(result.recommendations[0].iso3Code, "MYS");
});

test("exploratory nature runway answers rank new zealand first", () => {
  const result = calculateRecommendationResult(FIXTURE_COUNTRIES, {
    climatePreference: "MILD",
    seasonStylePreference: "BALANCED",
    seasonTolerance: "MEDIUM",
    pacePreference: "RELAXED",
    crowdPreference: "CALM",
    costQualityPreference: "VALUE_FIRST",
    housingPreference: "SPACE_FIRST",
    environmentPreference: "NATURE",
    mobilityPreference: "TRANSIT_FIRST",
    englishSupportNeed: "MEDIUM",
    newcomerSupportNeed: "LOW",
    safetyPriority: "HIGH",
    publicServicePriority: "MEDIUM",
    digitalConveniencePriority: "LOW",
    foodImportance: "LOW",
    diversityImportance: "LOW",
    cultureLeisureImportance: "LOW",
    workLifePreference: "LIFE_FIRST",
    settlementPreference: "EXPERIENCE",
    futureBasePreference: "LIGHT_START"
  });

  assert.equal(result.recommendations[0].iso3Code, "NZL");
});
