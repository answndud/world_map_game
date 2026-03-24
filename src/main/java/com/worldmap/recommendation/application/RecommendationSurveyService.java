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

	private static final int CLIMATE_WEIGHT = 5;
	private static final int PACE_WEIGHT = 4;
	private static final int BUDGET_WEIGHT = 5;
	private static final int ENVIRONMENT_WEIGHT = 4;
	private static final int PRIORITY_WEIGHT = 6;
	private static final int EXACT_MATCH_BONUS = 3;
	private static final int COHERENCE_BONUS = 6;
	private static final int OVER_BUDGET_PENALTY = 5;
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
		signals.add(new MatchSignal(climatePoints, answers.climatePreference().label() + " 취향과 기후 방향이 가깝습니다."));

		int paceDistance = distance(
			answers.pacePreference().targetValue(),
			profile.paceValue()
		);
		int pacePoints = closenessScore(paceDistance, PACE_WEIGHT) + exactMatchBonus(paceDistance);
		signals.add(new MatchSignal(pacePoints, answers.pacePreference().label() + " 생활 리듬과 잘 맞습니다."));

		int budgetDistance = distance(
			answers.budgetPreference().targetPriceLevel(),
			profile.priceLevel()
		);
		int budgetPoints = budgetScore(
			answers.budgetPreference().targetPriceLevel(),
			profile.priceLevel(),
			budgetDistance
		);
		signals.add(new MatchSignal(budgetPoints, "물가 허용 범위와 실제 생활비 부담이 크게 어긋나지 않습니다."));

		int environmentDistance = distance(
			answers.environmentPreference().targetUrbanity(),
			profile.urbanityValue()
		);
		int environmentPoints = closenessScore(environmentDistance, ENVIRONMENT_WEIGHT) + exactMatchBonus(environmentDistance);
		signals.add(new MatchSignal(environmentPoints, "도시와 자연의 균형 감각이 원하는 방향과 비슷합니다."));

		int englishPoints = englishPoints(profile.englishSupport(), answers.englishImportance());
		if (answers.englishImportance().weight() > 0) {
			signals.add(new MatchSignal(englishPoints, "영어 친화도가 초반 적응 장벽을 낮춰 줄 가능성이 큽니다."));
		}

		int priorityPoints = priorityPoints(profile, answers.priorityFocus());
		signals.add(new MatchSignal(priorityPoints, answers.priorityFocus().label() + " 기준에서 강점을 보입니다."));

		int coherencePoints = coherenceBonus(climateDistance, paceDistance, environmentDistance);
		signals.add(new MatchSignal(coherencePoints, "핵심 생활 조건들이 전반적으로 크게 어긋나지 않습니다."));

		int totalScore = signals.stream()
			.mapToInt(MatchSignal::points)
			.sum();
		int strongSignalCount = (int) signals.stream()
			.filter(signal -> signal.points() >= 20)
			.count();
		int exactMatchCount = exactMatchCount(climateDistance, paceDistance, budgetDistance, environmentDistance);

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

	private int budgetScore(int preferredPriceLevel, int actualPriceLevel, int distance) {
		int score = switch (distance) {
			case 0 -> (6 * BUDGET_WEIGHT) + EXACT_MATCH_BONUS;
			case 1 -> 2 * BUDGET_WEIGHT;
			default -> 0;
		};

		if (actualPriceLevel > preferredPriceLevel) {
			score -= (actualPriceLevel - preferredPriceLevel) * OVER_BUDGET_PENALTY;
		}

		return Math.max(0, score);
	}

	private int englishPoints(
		int englishSupport,
		RecommendationSurveyAnswers.EnglishImportance englishImportance
	) {
		return switch (englishImportance) {
			case HIGH -> englishSupport * 4;
			case MEDIUM -> englishSupport * 2;
			case LOW -> 0;
		};
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

	private int priorityPoints(
		RecommendationCountryProfile profile,
		RecommendationSurveyAnswers.PriorityFocus priorityFocus
	) {
		return switch (priorityFocus) {
			case SAFETY -> profile.safety() * PRIORITY_WEIGHT;
			case WELFARE -> profile.welfare() * PRIORITY_WEIGHT;
			case FOOD -> profile.food() * PRIORITY_WEIGHT;
			case DIVERSITY -> profile.diversity() * PRIORITY_WEIGHT;
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
