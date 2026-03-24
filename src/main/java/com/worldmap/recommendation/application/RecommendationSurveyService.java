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

	public static final String SURVEY_VERSION = "survey-v3";
	public static final String ENGINE_VERSION = "engine-v3";
	private static final int CLIMATE_WEIGHT = 5;
	private static final int SEASON_TOLERANCE_WEIGHT = 4;
	private static final int PACE_WEIGHT = 4;
	private static final int COST_QUALITY_WEIGHT = 5;
	private static final int ENVIRONMENT_WEIGHT = 4;
	private static final int ENGLISH_SUPPORT_WEIGHT = 4;
	private static final int SAFETY_WEIGHT = 5;
	private static final int PUBLIC_SERVICE_WEIGHT = 6;
	private static final int FOOD_WEIGHT = 4;
	private static final int DIVERSITY_WEIGHT = 4;
	private static final int SETTLEMENT_WEIGHT = 2;
	private static final int MOBILITY_WEIGHT = 2;
	private static final int EXACT_MATCH_BONUS = 3;
	private static final int COHERENCE_BONUS = 6;
	private static final int COST_OVERSHOOT_PENALTY = 6;
	private static final int CLIMATE_MISMATCH_PENALTY = 3;
	private static final int SEASON_MISMATCH_PENALTY = 3;
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
				answers.seasonTolerance().name(),
				answers.pacePreference().name(),
				answers.costQualityPreference().name(),
				answers.environmentPreference().name(),
				answers.englishSupportNeed().name(),
				answers.safetyPriority().name(),
				answers.publicServicePriority().name(),
				answers.foodImportance().name(),
				answers.diversityImportance().name(),
				answers.settlementPreference().name(),
				answers.mobilityPreference().name()
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
		int climateDistance = distance(
			answers.climatePreference().targetValue(),
			profile.climateValue()
		);
		int climatePoints = closenessScore(climateDistance, CLIMATE_WEIGHT) + exactMatchBonus(climateDistance);
		signals.add(new MatchSignal(climatePoints + mismatchPenalty(climateDistance, CLIMATE_MISMATCH_PENALTY), answers.climatePreference().label() + " 기후 방향과 잘 맞습니다."));

		int seasonToleranceDistance = distance(
			answers.seasonTolerance().toleranceValue(),
			climateExtremity(profile.climateValue())
		);
		int seasonTolerancePoints = closenessScore(seasonToleranceDistance, SEASON_TOLERANCE_WEIGHT);
		signals.add(new MatchSignal(seasonTolerancePoints + mismatchPenalty(seasonToleranceDistance, SEASON_MISMATCH_PENALTY), "기후 적응 난도와 감수 의향이 크게 어긋나지 않습니다."));

		int paceDistance = distance(
			answers.pacePreference().targetValue(),
			profile.paceValue()
		);
		int pacePoints = closenessScore(paceDistance, PACE_WEIGHT) + exactMatchBonus(paceDistance);
		signals.add(new MatchSignal(pacePoints, answers.pacePreference().label() + " 생활 리듬과 잘 맞습니다."));

		int costQualityDistance = distance(
			answers.costQualityPreference().targetPriceLevel(),
			profile.priceLevel()
		);
		int costQualityPoints = costQualityPoints(
			answers.costQualityPreference().targetPriceLevel(),
			profile.priceLevel(),
			costQualityDistance
		);
		signals.add(new MatchSignal(costQualityPoints, "비용 부담과 생활 품질에 대한 기대 수준이 크게 엇나가지 않습니다."));

		int environmentDistance = distance(
			answers.environmentPreference().targetUrbanity(),
			profile.urbanityValue()
		);
		int environmentPoints = closenessScore(environmentDistance, ENVIRONMENT_WEIGHT) + exactMatchBonus(environmentDistance);
		signals.add(new MatchSignal(environmentPoints, "도시 편의와 숨 쉴 공간의 균형이 원하는 방향과 비슷합니다."));

		int englishPoints = englishPoints(profile.englishSupport(), answers.englishSupportNeed());
		signals.add(new MatchSignal(englishPoints, "초기 적응에서 필요한 영어 지원 수준과 비교적 잘 맞습니다."));

		int safetyPoints = priorityPoints(profile.safety(), answers.safetyPriority(), SAFETY_WEIGHT);
		signals.add(new MatchSignal(safetyPoints, "치안과 생활 안전 기준에서 안정적인 편입니다."));

		int publicServicePoints = priorityPoints(profile.welfare(), answers.publicServicePriority(), PUBLIC_SERVICE_WEIGHT);
		signals.add(new MatchSignal(publicServicePoints, "의료·행정·복지 같은 공공 서비스 기대치와 잘 맞습니다."));

		int foodPoints = priorityPoints(profile.food(), answers.foodImportance(), FOOD_WEIGHT);
		signals.add(new MatchSignal(foodPoints, "음식 만족도와 외식 다양성 측면에서 강점을 보입니다."));

		int diversityPoints = priorityPoints(profile.diversity(), answers.diversityImportance(), DIVERSITY_WEIGHT);
		signals.add(new MatchSignal(diversityPoints, "다문화 분위기와 다양한 사람·문화 접근성에서 장점이 있습니다."));

		int settlementPoints = settlementPoints(profile, answers.settlementPreference());
		signals.add(new MatchSignal(settlementPoints, answers.settlementPreference().label() + " 기준에서 생활 안정성과 적응 난도를 함께 반영했습니다."));

		int mobilityPoints = mobilityPoints(profile, answers.mobilityPreference());
		signals.add(new MatchSignal(mobilityPoints, answers.mobilityPreference().label() + " 이동 방식과 일상 동선이 비교적 잘 맞습니다."));

		int coherencePoints = coherenceBonus(climateDistance, paceDistance, environmentDistance);
		signals.add(new MatchSignal(coherencePoints, "핵심 생활 조건들이 전반적으로 크게 어긋나지 않습니다."));

		int totalScore = signals.stream()
			.mapToInt(MatchSignal::points)
			.sum();
		int strongSignalCount = (int) signals.stream()
			.filter(signal -> signal.points() >= 16)
			.count();
		int exactMatchCount = exactMatchCount(climateDistance, paceDistance, costQualityDistance, environmentDistance);

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

	private int climateExtremity(int climateValue) {
		return 1 + (Math.abs(climateValue - 3) * 2);
	}

	private int costQualityPoints(int preferredPriceLevel, int actualPriceLevel, int distance) {
		int score = switch (distance) {
			case 0 -> (6 * COST_QUALITY_WEIGHT) + EXACT_MATCH_BONUS;
			case 1 -> 3 * COST_QUALITY_WEIGHT;
			case 2 -> COST_QUALITY_WEIGHT;
			default -> 0;
		};

		if (actualPriceLevel > preferredPriceLevel) {
			score -= (actualPriceLevel - preferredPriceLevel) * COST_OVERSHOOT_PENALTY;
		}

		return score;
	}

	private int englishPoints(
		int englishSupport,
		RecommendationSurveyAnswers.EnglishSupportNeed englishSupportNeed
	) {
		return switch (englishSupportNeed) {
			case HIGH -> englishSupport * (ENGLISH_SUPPORT_WEIGHT + 1);
			case MEDIUM -> englishSupport * ENGLISH_SUPPORT_WEIGHT;
			case LOW -> englishSupport;
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

	private int coherenceBonus(int climateDistance, int paceDistance, int environmentDistance) {
		return (climateDistance <= 1 && paceDistance <= 1 && environmentDistance <= 1)
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
			case EXPERIENCE -> (profile.diversity() * SETTLEMENT_WEIGHT) + profile.food();
			case BALANCED -> profile.safety() + profile.welfare();
			case STABILITY -> (profile.safety() * SETTLEMENT_WEIGHT) + (profile.welfare() * SETTLEMENT_WEIGHT)
				+ (profile.englishSupport() >= 4 ? 2 : 0);
		};
	}

	private int mobilityPoints(
		RecommendationCountryProfile profile,
		RecommendationSurveyAnswers.MobilityPreference mobilityPreference
	) {
		return switch (mobilityPreference) {
			case TRANSIT_FIRST -> (profile.urbanityValue() * MOBILITY_WEIGHT) + (profile.paceValue() >= 3 ? 2 : 0);
			case BALANCED -> profile.safety();
			case SPACE_FIRST -> ((6 - profile.urbanityValue()) * MOBILITY_WEIGHT) + profile.safety();
		};
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
