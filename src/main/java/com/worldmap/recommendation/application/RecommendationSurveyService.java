package com.worldmap.recommendation.application;

import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationSurveyService {

	public static final String SURVEY_VERSION = "survey-v4";
	public static final String ENGINE_VERSION = "engine-v7";
	private static final int CLIMATE_WEIGHT = 4;
	private static final int SEASON_STYLE_WEIGHT = 3;
	private static final int WEATHER_ADAPTATION_WEIGHT = 4;
	private static final int PACE_WEIGHT = 4;
	private static final int CROWD_WEIGHT = 3;
	private static final int COST_QUALITY_WEIGHT = 5;
	private static final int HOUSING_WEIGHT = 4;
	private static final int ENVIRONMENT_WEIGHT = 4;
	private static final int MOBILITY_WEIGHT = 4;
	private static final int ENGLISH_SUPPORT_WEIGHT = 4;
	private static final int NEWCOMER_SUPPORT_WEIGHT = 4;
	private static final int SAFETY_WEIGHT = 5;
	private static final int PUBLIC_SERVICE_WEIGHT = 5;
	private static final int DIGITAL_WEIGHT = 4;
	private static final int FOOD_WEIGHT = 4;
	private static final int DIVERSITY_WEIGHT = 4;
	private static final int CULTURE_WEIGHT = 4;
	private static final int WORK_LIFE_WEIGHT = 3;
	private static final int SETTLEMENT_WEIGHT = 3;
	private static final int FUTURE_BASE_WEIGHT = 4;
	private static final int EXACT_MATCH_BONUS = 2;
	private static final int COHERENCE_BONUS = 8;
	private static final int EXPERIENCE_TRANSIT_BONUS = 12;
	private static final int CIVIC_BASE_BONUS = 10;
	private static final int VALUE_FIRST_COST_OVERSHOOT_PENALTY = 11;
	private static final int BALANCED_COST_OVERSHOOT_PENALTY = 7;
	private static final int QUALITY_FIRST_COST_OVERSHOOT_PENALTY = 4;
	private static final int CLIMATE_MISMATCH_PENALTY = 3;
	private static final int SEASON_STYLE_MISMATCH_PENALTY = 2;
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.KOREA);

	private final RecommendationCountryProfileCatalog profileCatalog;
	private final RecommendationQuestionCatalog questionCatalog;
	private final CountryRepository countryRepository;

	public RecommendationSurveyService(
		RecommendationCountryProfileCatalog profileCatalog,
		RecommendationQuestionCatalog questionCatalog,
		CountryRepository countryRepository
	) {
		this.profileCatalog = profileCatalog;
		this.questionCatalog = questionCatalog;
		this.countryRepository = countryRepository;
	}

	@Transactional(readOnly = true)
	public RecommendationSurveyResultView recommend(RecommendationSurveyAnswers answers) {
		Map<String, Country> countriesByIso = countriesByIso();
		List<RecommendationCandidateView> candidates = profileCatalog.profiles().stream()
			.map(profile -> scoreCandidate(profile, countriesByIso.get(profile.iso3Code()), answers))
			.filter(candidate -> candidate != null)
			.sorted(Comparator
				.comparingInt(RecommendationCandidateView::matchScore).reversed()
				.thenComparingInt(RecommendationCandidateView::strongSignalCount).reversed()
				.thenComparingInt(RecommendationCandidateView::exactMatchCount).reversed()
				.thenComparing(RecommendationCandidateView::countryNameKr))
			.limit(3)
			.toList();

		List<RecommendationCandidateView> rankedCandidates = new ArrayList<>();
		for (int index = 0; index < candidates.size(); index++) {
			RecommendationCandidateView candidate = candidates.get(index);
			rankedCandidates.add(new RecommendationCandidateView(
				index + 1,
				candidate.iso3Code(),
				candidate.countryNameKr(),
				candidate.countryNameEn(),
				candidate.continentLabel(),
				candidate.capitalCity(),
				candidate.populationLabel(),
				candidate.matchScore(),
				candidate.strongSignalCount(),
				candidate.exactMatchCount(),
				candidate.headline(),
				candidate.reasons()
			));
		}

		return new RecommendationSurveyResultView(
			SURVEY_VERSION,
			ENGINE_VERSION,
			new RecommendationFeedbackPayloadView(
				SURVEY_VERSION,
				ENGINE_VERSION,
				answers.climatePreference().name(),
				answers.seasonStylePreference().name(),
				answers.seasonTolerance().name(),
				answers.pacePreference().name(),
				answers.crowdPreference().name(),
				answers.costQualityPreference().name(),
				answers.housingPreference().name(),
				answers.environmentPreference().name(),
				answers.mobilityPreference().name(),
				answers.englishSupportNeed().name(),
				answers.newcomerSupportNeed().name(),
				answers.safetyPriority().name(),
				answers.publicServicePriority().name(),
				answers.digitalConveniencePriority().name(),
				answers.foodImportance().name(),
				answers.diversityImportance().name(),
				answers.cultureLeisureImportance().name(),
				answers.workLifePreference().name(),
				answers.settlementPreference().name(),
				answers.futureBasePreference().name()
			),
			questionCatalog.summariesOf(answers),
			List.copyOf(rankedCandidates)
		);
	}

	private RecommendationCandidateView scoreCandidate(
		RecommendationCountryProfile profile,
		Country country,
		RecommendationSurveyAnswers answers
	) {
		if (country == null) {
			return null;
		}

		List<MatchSignal> signals = new ArrayList<>();

		int climateDistance = distance(answers.climatePreference().targetValue(), profile.climateValue());
		signals.add(new MatchSignal(
			closenessScore(climateDistance, CLIMATE_WEIGHT) + exactMatchBonus(climateDistance)
				+ mismatchPenalty(climateDistance, CLIMATE_MISMATCH_PENALTY),
			answers.climatePreference().label() + " 날씨 감각과 비교적 잘 맞습니다."
		));

		int seasonStyleDistance = distance(answers.seasonStylePreference().targetValue(), profile.seasonality());
		signals.add(new MatchSignal(
			closenessScore(seasonStyleDistance, SEASON_STYLE_WEIGHT) + exactMatchBonus(seasonStyleDistance)
				+ mismatchPenalty(seasonStyleDistance, SEASON_STYLE_MISMATCH_PENALTY),
			"계절 변화에 대한 기대와 실제 계절감이 크게 어긋나지 않습니다."
		));

		int weatherAdaptationDistance = distance(answers.seasonTolerance().toleranceValue(), weatherDemand(profile));
		signals.add(new MatchSignal(
			closenessScore(weatherAdaptationDistance, WEATHER_ADAPTATION_WEIGHT),
			"기후 적응 난도와 감수 의향이 비교적 잘 맞습니다."
		));

		int paceDistance = distance(answers.pacePreference().targetValue(), profile.paceValue());
		signals.add(new MatchSignal(
			closenessScore(paceDistance, PACE_WEIGHT) + exactMatchBonus(paceDistance),
			answers.pacePreference().label() + " 생활 리듬과 잘 맞습니다."
		));

		int crowdDistance = distance(answers.crowdPreference().targetValue(), crowdEnergy(profile));
		signals.add(new MatchSignal(
			closenessScore(crowdDistance, CROWD_WEIGHT) + exactMatchBonus(crowdDistance),
			"동네의 밀도와 자극 수준이 원하는 방향과 가깝습니다."
		));

		int costQualityDistance = distance(
			answers.costQualityPreference().targetPriceLevel(),
			profile.priceLevel()
		);
		signals.add(new MatchSignal(
			costQualityPoints(
				answers.costQualityPreference(),
				profile.priceLevel(),
				costQualityDistance
			),
			"생활비 부담과 생활 품질에 대한 기대 수준이 크게 엇나가지 않습니다."
		));

		int housingDistance = distance(answers.housingPreference().targetSpaceValue(), profile.housingSpace());
		signals.add(new MatchSignal(
			closenessScore(housingDistance, HOUSING_WEIGHT) + exactMatchBonus(housingDistance),
			"주거 공간과 중심 접근성 기준이 원하는 방향과 비슷합니다."
		));

		int environmentDistance = distance(answers.environmentPreference().targetUrbanity(), profile.urbanityValue());
		signals.add(new MatchSignal(
			closenessScore(environmentDistance, ENVIRONMENT_WEIGHT) + exactMatchBonus(environmentDistance),
			"도시 편의와 자연 접근성의 비중이 원하는 쪽과 가깝습니다."
		));

		int mobilityDistance = distance(answers.mobilityPreference().targetTransitValue(), transitSupport(profile));
		signals.add(new MatchSignal(
			closenessScore(mobilityDistance, MOBILITY_WEIGHT) + exactMatchBonus(mobilityDistance),
			"이동 방식과 생활 동선이 원하는 방향과 잘 맞습니다."
		));

		signals.add(new MatchSignal(
			supportPoints(profile.englishSupport(), answers.englishSupportNeed(), ENGLISH_SUPPORT_WEIGHT),
			"초기 적응에서 필요한 영어 지원 수준을 비교적 잘 충족합니다."
		));

		signals.add(new MatchSignal(
			supportPoints(newcomerSupport(profile), answers.newcomerSupportNeed(), NEWCOMER_SUPPORT_WEIGHT),
			"처음 정착할 때 필요한 안내와 친절한 분위기 기대와 잘 맞습니다."
		));

		signals.add(new MatchSignal(
			priorityPoints(profile.safety(), answers.safetyPriority(), SAFETY_WEIGHT),
			"치안과 생활 안전 기준에서 안정적인 편입니다."
		));

		signals.add(new MatchSignal(
			priorityPoints(profile.welfare(), answers.publicServicePriority(), PUBLIC_SERVICE_WEIGHT),
			"의료·행정·복지 같은 공공 서비스 기대치와 잘 맞습니다."
		));

		signals.add(new MatchSignal(
			priorityPoints(profile.digitalConvenience(), answers.digitalConveniencePriority(), DIGITAL_WEIGHT),
			"디지털 행정, 결제, 생활 인프라의 매끄러움에서 강점을 보입니다."
		));

		signals.add(new MatchSignal(
			priorityPoints(profile.food(), answers.foodImportance(), FOOD_WEIGHT),
			"음식 만족도와 외식 선택지 측면에서 장점을 보입니다."
		));

		signals.add(new MatchSignal(
			priorityPoints(profile.diversity(), answers.diversityImportance(), DIVERSITY_WEIGHT),
			"다양한 배경의 사람들과 섞여 지내기 좋은 환경에 가깝습니다."
		));

		signals.add(new MatchSignal(
			priorityPoints(profile.cultureScene(), answers.cultureLeisureImportance(), CULTURE_WEIGHT),
			"문화·여가 선택지 밀도에서 기대한 방향과 가깝습니다."
		));

		int workLifeDistance = distance(answers.workLifePreference().targetIntensityValue(), workIntensity(profile));
		signals.add(new MatchSignal(
			closenessScore(workLifeDistance, WORK_LIFE_WEIGHT) + exactMatchBonus(workLifeDistance),
			"일 기회와 생활 균형 사이에서 원하는 강도와 비교적 잘 맞습니다."
		));

		signals.add(new MatchSignal(
			settlementPoints(profile, answers.settlementPreference()),
			answers.settlementPreference().label() + " 기준에서 적응 난도와 생활 안정성을 함께 반영했습니다."
		));

		signals.add(new MatchSignal(
			experienceTransitBonus(profile, answers),
			"가볍게 적응하며 대중교통 중심으로 살아보기 좋은지 함께 반영했습니다."
		));

		signals.add(new MatchSignal(
			civicBaseBonus(profile, answers),
			"안전, 공공 서비스, 기본 정착 안정성을 함께 보는 균형형 생활 기준을 반영했습니다."
		));

		int futureBaseDistance = distance(answers.futureBasePreference().targetValue(), futureBase(profile));
		signals.add(new MatchSignal(
			closenessScore(futureBaseDistance, FUTURE_BASE_WEIGHT) + exactMatchBonus(futureBaseDistance),
			"당장의 편의와 장기 기반 사이에서 기대한 방향과 가깝습니다."
		));

		int coherencePoints = coherenceBonus(
			climateDistance,
			costQualityDistance,
			environmentDistance,
			housingDistance
		);
		signals.add(new MatchSignal(coherencePoints, "핵심 생활 조건들이 전반적으로 크게 어긋나지 않습니다."));

		int totalScore = signals.stream()
			.mapToInt(MatchSignal::points)
			.sum();
		int strongSignalCount = (int) signals.stream()
			.filter(signal -> signal.points() >= 14)
			.count();
		int exactMatchCount = exactMatchCount(
			climateDistance,
			seasonStyleDistance,
			paceDistance,
			crowdDistance,
			costQualityDistance,
			housingDistance,
			environmentDistance,
			mobilityDistance,
			workLifeDistance,
			futureBaseDistance
		);

		List<String> topReasons = signals.stream()
			.sorted(Comparator.comparingInt(MatchSignal::points).reversed())
			.filter(signal -> signal.points() > 0)
			.map(MatchSignal::message)
			.distinct()
			.limit(3)
			.toList();

		return new RecommendationCandidateView(
			0,
			country.getIso3Code(),
			country.getNameKr(),
			country.getNameEn(),
			continentLabel(country),
			country.getCapitalCity(),
			NUMBER_FORMAT.format(country.getPopulation()) + "명 (" + country.getPopulationYear() + ")",
			totalScore,
			strongSignalCount,
			exactMatchCount,
			profile.hookLine(),
			topReasons
		);
	}

	private Map<String, Country> countriesByIso() {
		Map<String, Country> countriesByIso = new LinkedHashMap<>();
		for (Country country : countryRepository.findAllByOrderByNameKrAsc()) {
			countriesByIso.put(country.getIso3Code(), country);
		}
		return countriesByIso;
	}

	private int distance(int preferredValue, int actualValue) {
		return Math.abs(preferredValue - actualValue);
	}

	private int closenessScore(int distance, int weight) {
		return switch (distance) {
			case 0 -> 5 * weight;
			case 1 -> 3 * weight;
			case 2 -> weight;
			default -> 0;
		};
	}

	private int exactMatchBonus(int distance) {
		return distance == 0 ? EXACT_MATCH_BONUS : 0;
	}

	private int weatherDemand(RecommendationCountryProfile profile) {
		int climateSwing = Math.abs(profile.climateValue() - 3);
		int seasonSwing = Math.abs(profile.seasonality() - 3);
		return Math.min(5, 1 + (Math.max(climateSwing, seasonSwing) * 2));
	}

	private int crowdEnergy(RecommendationCountryProfile profile) {
		return normalizedAverage(profile.paceValue(), profile.urbanityValue());
	}

	private int transitSupport(RecommendationCountryProfile profile) {
		return normalizedAverage(profile.urbanityValue(), profile.digitalConvenience(), profile.paceValue());
	}

	private int newcomerSupport(RecommendationCountryProfile profile) {
		return normalizedAverage(profile.englishSupport(), profile.newcomerFriendliness());
	}

	private int workIntensity(RecommendationCountryProfile profile) {
		return normalizedAverage(profile.paceValue(), profile.urbanityValue(), profile.digitalConvenience());
	}

	private int futureBase(RecommendationCountryProfile profile) {
		return normalizedAverage(profile.safety(), profile.welfare(), profile.housingSpace());
	}

	private int normalizedAverage(int... values) {
		int total = 0;
		for (int value : values) {
			total += value;
		}
		return Math.round((float) total / values.length);
	}

	private int costQualityPoints(
		RecommendationSurveyAnswers.CostQualityPreference costQualityPreference,
		int actualPriceLevel,
		int distance
	) {
		int preferredPriceLevel = costQualityPreference.targetPriceLevel();
		int score = switch (distance) {
			case 0 -> (6 * COST_QUALITY_WEIGHT) + EXACT_MATCH_BONUS;
			case 1 -> 3 * COST_QUALITY_WEIGHT;
			case 2 -> COST_QUALITY_WEIGHT;
			default -> 0;
		};

		if (actualPriceLevel > preferredPriceLevel) {
			score -= (actualPriceLevel - preferredPriceLevel) * costOvershootPenalty(costQualityPreference);
		}

		return score;
	}

	private int costOvershootPenalty(RecommendationSurveyAnswers.CostQualityPreference costQualityPreference) {
		return switch (costQualityPreference) {
			case VALUE_FIRST -> VALUE_FIRST_COST_OVERSHOOT_PENALTY;
			case BALANCED -> BALANCED_COST_OVERSHOOT_PENALTY;
			case QUALITY_FIRST -> QUALITY_FIRST_COST_OVERSHOOT_PENALTY;
		};
	}

	private int supportPoints(
		int supportScore,
		RecommendationSurveyAnswers.EnglishSupportNeed need,
		int weight
	) {
		return switch (need) {
			case HIGH -> supportScore * (weight + 1);
			case MEDIUM -> supportScore * weight;
			case LOW -> supportScore;
		};
	}

	private int supportPoints(
		int supportScore,
		RecommendationSurveyAnswers.NewcomerSupportNeed need,
		int weight
	) {
		return switch (need) {
			case HIGH -> supportScore * (weight + 1);
			case MEDIUM -> supportScore * weight;
			case LOW -> supportScore;
		};
	}

	private int mismatchPenalty(int distance, int penaltyWeight) {
		return switch (distance) {
			case 0, 1 -> 0;
			case 2 -> -penaltyWeight;
			default -> -penaltyWeight * 2;
		};
	}

	private int priorityPoints(
		int attributeScore,
		RecommendationSurveyAnswers.ImportanceLevel importance,
		int weight
	) {
		if (importance == RecommendationSurveyAnswers.ImportanceLevel.LOW) {
			return attributeScore;
		}
		return attributeScore * weight + importance.weight();
	}

	private int coherenceBonus(int climateDistance, int costDistance, int environmentDistance, int housingDistance) {
		return (climateDistance <= 1 && costDistance <= 1 && environmentDistance <= 1 && housingDistance <= 1)
			? COHERENCE_BONUS
			: 0;
	}

	private int exactMatchCount(int... distances) {
		int exactMatchCount = 0;
		for (int distance : distances) {
			if (distance == 0) {
				exactMatchCount++;
			}
		}
		return exactMatchCount;
	}

	private int settlementPoints(
		RecommendationCountryProfile profile,
		RecommendationSurveyAnswers.SettlementPreference settlementPreference
	) {
		return switch (settlementPreference) {
			case EXPERIENCE -> newcomerSupport(profile) + profile.cultureScene() + profile.food();
			case BALANCED -> profile.safety() + profile.welfare() + profile.diversity();
			case STABILITY -> (futureBase(profile) * SETTLEMENT_WEIGHT) + profile.newcomerFriendliness();
		};
	}

	private int experienceTransitBonus(
		RecommendationCountryProfile profile,
		RecommendationSurveyAnswers answers
	) {
		if (answers.settlementPreference() != RecommendationSurveyAnswers.SettlementPreference.EXPERIENCE
			|| answers.mobilityPreference() != RecommendationSurveyAnswers.MobilityPreference.TRANSIT_FIRST
			|| answers.costQualityPreference() != RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST) {
			return 0;
		}

		int transitScore = transitSupport(profile);
		int newcomerScore = newcomerSupport(profile);
		int digitalScore = profile.digitalConvenience();
		int safetyScore = profile.safety();
		int welfareScore = profile.welfare();

		if (transitScore >= 4 && newcomerScore >= 4 && digitalScore >= 4 && safetyScore >= 3 && welfareScore >= 3) {
			return EXPERIENCE_TRANSIT_BONUS;
		}
		if (transitScore >= 3 && newcomerScore >= 4 && digitalScore >= 3 && safetyScore >= 3) {
			return EXPERIENCE_TRANSIT_BONUS / 2;
		}
		return 0;
	}

	private int civicBaseBonus(
		RecommendationCountryProfile profile,
		RecommendationSurveyAnswers answers
	) {
		boolean wantsBalancedCivicLife = answers.environmentPreference() == RecommendationSurveyAnswers.EnvironmentPreference.MIXED
			&& answers.pacePreference() == RecommendationSurveyAnswers.PacePreference.BALANCED
			&& (answers.publicServicePriority() == RecommendationSurveyAnswers.ImportanceLevel.HIGH
				|| answers.safetyPriority() == RecommendationSurveyAnswers.ImportanceLevel.HIGH);

		if (!wantsBalancedCivicLife) {
			return 0;
		}

		if (answers.costQualityPreference() == RecommendationSurveyAnswers.CostQualityPreference.VALUE_FIRST
			&& profile.priceLevel() >= 4) {
			return 0;
		}

		int civicBaseScore = normalizedAverage(
			profile.safety(),
			profile.welfare(),
			profile.housingSpace(),
			profile.newcomerFriendliness()
		);

		if (civicBaseScore >= 4) {
			return CIVIC_BASE_BONUS;
		}
		if (civicBaseScore >= 3 && profile.safety() >= 4 && profile.welfare() >= 4) {
			return CIVIC_BASE_BONUS / 2;
		}
		return 0;
	}

	private String continentLabel(Country country) {
		return switch (country.getContinent()) {
			case ASIA -> "아시아";
			case EUROPE -> "유럽";
			case AFRICA -> "아프리카";
			case NORTH_AMERICA -> "북아메리카";
			case SOUTH_AMERICA -> "남아메리카";
			case OCEANIA -> "오세아니아";
		};
	}

	private record MatchSignal(int points, String message) {
	}
}
