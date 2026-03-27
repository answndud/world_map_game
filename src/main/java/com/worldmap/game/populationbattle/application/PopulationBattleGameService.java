package com.worldmap.game.populationbattle.application;

import com.worldmap.common.exception.ResourceNotFoundException;
import com.worldmap.country.domain.Country;
import com.worldmap.country.domain.CountryRepository;
import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameAttempt;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameAttemptRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSession;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSessionRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStage;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStageRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStageStatus;
import com.worldmap.ranking.application.LeaderboardService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PopulationBattleGameService {

	private static final int MINIMUM_COUNTRY_COUNT = 4;
	private static final String QUESTION_PROMPT = "두 나라 중 인구가 더 많은 나라를 고르세요.";

	private final CountryRepository countryRepository;
	private final PopulationBattleGameSessionRepository populationBattleGameSessionRepository;
	private final PopulationBattleGameStageRepository populationBattleGameStageRepository;
	private final PopulationBattleGameAttemptRepository populationBattleGameAttemptRepository;
	private final PopulationBattleGameOptionGenerator populationBattleGameOptionGenerator;
	private final PopulationBattleGameDifficultyPolicy populationBattleGameDifficultyPolicy;
	private final PopulationBattleGameScoringPolicy populationBattleGameScoringPolicy;
	private final LeaderboardService leaderboardService;

	public PopulationBattleGameService(
		CountryRepository countryRepository,
		PopulationBattleGameSessionRepository populationBattleGameSessionRepository,
		PopulationBattleGameStageRepository populationBattleGameStageRepository,
		PopulationBattleGameAttemptRepository populationBattleGameAttemptRepository,
		PopulationBattleGameOptionGenerator populationBattleGameOptionGenerator,
		PopulationBattleGameDifficultyPolicy populationBattleGameDifficultyPolicy,
		PopulationBattleGameScoringPolicy populationBattleGameScoringPolicy,
		LeaderboardService leaderboardService
	) {
		this.countryRepository = countryRepository;
		this.populationBattleGameSessionRepository = populationBattleGameSessionRepository;
		this.populationBattleGameStageRepository = populationBattleGameStageRepository;
		this.populationBattleGameAttemptRepository = populationBattleGameAttemptRepository;
		this.populationBattleGameOptionGenerator = populationBattleGameOptionGenerator;
		this.populationBattleGameDifficultyPolicy = populationBattleGameDifficultyPolicy;
		this.populationBattleGameScoringPolicy = populationBattleGameScoringPolicy;
		this.leaderboardService = leaderboardService;
	}

	@Transactional
	public PopulationBattleGameStartView startGuestGame(String nickname, String guestSessionKey) {
		return startGame(normalizeNickname(nickname), null, guestSessionKey);
	}

	@Transactional
	public PopulationBattleGameStartView startMemberGame(Long memberId, String memberNickname) {
		return startGame(memberNickname, memberId, null);
	}

	private PopulationBattleGameStartView startGame(
		String playerNickname,
		Long memberId,
		String guestSessionKey
	) {
		List<Country> countries = getCountriesSortedByPopulation();
		if (countries.size() < MINIMUM_COUNTRY_COUNT) {
			throw new IllegalStateException("인구 비교 퀵 배틀을 시작하기 위한 국가 데이터가 충분하지 않습니다.");
		}

		PopulationBattleGameSession session = PopulationBattleGameSession.ready(playerNickname, memberId, guestSessionKey, 1);
		session = populationBattleGameSessionRepository.save(session);
		createNextStage(session, 1, countries, List.of());
		session.startGame(LocalDateTime.now());

		return new PopulationBattleGameStartView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getLivesRemaining(),
			"/games/population-battle/play/" + session.getId()
		);
	}

	@Transactional(readOnly = true)
	public PopulationBattleGameStateView getCurrentState(UUID sessionId) {
		PopulationBattleGameSession session = getSession(sessionId);
		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			throw new IllegalStateException("이미 종료된 게임입니다.");
		}

		PopulationBattleGameStage stage = getStage(sessionId, session.getCurrentStageNumber());
		PopulationBattleGameDifficultyPlan difficultyPlan = resolveDifficulty(stage.getStageNumber());

		return new PopulationBattleGameStateView(
			session.getId(),
			stage.getStageNumber(),
			difficultyPlan.label(),
			session.getClearedStageCount(),
			session.getTotalScore(),
			session.getLivesRemaining(),
			stage.getQuestionPrompt(),
			toOptionViews(stage),
			session.getStatus()
		);
	}

	@Transactional
	public PopulationBattleGameStartView restartGame(UUID sessionId) {
		PopulationBattleGameSession session = getSession(sessionId);
		if (session.getStatus() != GameSessionStatus.GAME_OVER && session.getStatus() != GameSessionStatus.FINISHED) {
			throw new IllegalStateException("종료된 게임만 다시 시작할 수 있습니다.");
		}

		List<Country> countries = getCountriesSortedByPopulation();
		if (countries.size() < MINIMUM_COUNTRY_COUNT) {
			throw new IllegalStateException("인구 비교 퀵 배틀을 시작하기 위한 국가 데이터가 충분하지 않습니다.");
		}

		populationBattleGameAttemptRepository.deleteAllByStageSessionId(sessionId);
		populationBattleGameAttemptRepository.flush();
		populationBattleGameStageRepository.deleteAllBySessionId(sessionId);
		populationBattleGameStageRepository.flush();

		session.restart(1);
		createNextStage(session, 1, countries, List.of());
		session.startGame(LocalDateTime.now());

		return new PopulationBattleGameStartView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getLivesRemaining(),
			"/games/population-battle/play/" + session.getId()
		);
	}

	@Transactional
	public PopulationBattleGameAnswerView submitAnswer(UUID sessionId, Integer stageNumber, Integer selectedOptionNumber) {
		if (selectedOptionNumber == null || selectedOptionNumber < 1 || selectedOptionNumber > 2) {
			throw new IllegalArgumentException("좌우 보기 중 하나를 선택해야 합니다.");
		}

		PopulationBattleGameSession session = getSession(sessionId);
		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			throw new IllegalStateException("진행 중인 게임만 답안을 제출할 수 있습니다.");
		}
		if (!session.getCurrentStageNumber().equals(stageNumber)) {
			throw new IllegalStateException("현재 진행 중인 Stage와 일치하지 않습니다.");
		}

		PopulationBattleGameStage stage = getStage(sessionId, stageNumber);
		int attemptNumber = stage.nextAttemptNumber();
		LocalDateTime attemptedAt = LocalDateTime.now();
		PopulationBattleAnswerJudgement judgement = populationBattleGameScoringPolicy.judge(
			selectedOptionNumber,
			stage.getCorrectOptionNumber(),
			stageNumber,
			attemptNumber,
			session.getLivesRemaining()
		);

		String selectedCountryName = stage.optionName(selectedOptionNumber);
		Long selectedCountryPopulation = stage.optionPopulation(selectedOptionNumber);
		String correctCountryName = stage.getCorrectCountryName();
		Long correctCountryPopulation = stage.getCorrectCountryPopulation();

		stage.recordAttempt(judgement.correct(), judgement.awardedScore(), attemptedAt);

		if (judgement.correct()) {
			List<Country> countries = getCountriesSortedByPopulation();
			List<PopulationBattleGameStage> existingStages = populationBattleGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId);
			createNextStage(session, stageNumber + 1, countries, existingStages);
			session.clearCurrentStage(stageNumber, judgement.awardedScore(), attemptedAt);
		} else {
			session.recordWrongAttempt(stageNumber, attemptedAt);
			if (session.getStatus() == GameSessionStatus.GAME_OVER) {
				stage.markFailed();
			}
		}

		populationBattleGameAttemptRepository.save(
			PopulationBattleGameAttempt.create(
				stage,
				attemptNumber,
				selectedOptionNumber,
				selectedCountryName,
				judgement.correct(),
				session.getLivesRemaining(),
				attemptedAt
			)
		);

		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			leaderboardService.recordPopulationBattleResult(
				session,
				Math.toIntExact(populationBattleGameAttemptRepository.countByStageSessionId(sessionId))
			);
		}

		PopulationBattleGameDifficultyPlan nextDifficultyPlan = session.getStatus() == GameSessionStatus.IN_PROGRESS
			? resolveDifficulty(session.getCurrentStageNumber())
			: null;

		return new PopulationBattleGameAnswerView(
			session.getId(),
			stage.getStageNumber(),
			stage.getQuestionPrompt(),
			selectedOptionNumber,
			selectedCountryName,
			selectedCountryPopulation,
			stage.getCorrectOptionNumber(),
			correctCountryName,
			correctCountryPopulation,
			judgement.correct(),
			judgement.awardedScore(),
			session.getTotalScore(),
			session.getClearedStageCount(),
			session.getLivesRemaining(),
			session.getStatus() == GameSessionStatus.IN_PROGRESS ? session.getCurrentStageNumber() : null,
			nextDifficultyPlan != null ? nextDifficultyPlan.label() : null,
			session.getStatus(),
			determineOutcome(judgement.correct(), session.getStatus()),
			"/games/population-battle/result/" + session.getId()
		);
	}

	@Transactional(readOnly = true)
	public PopulationBattleGameSessionResultView getSessionResult(UUID sessionId) {
		PopulationBattleGameSession session = getSession(sessionId);
		Map<Long, List<PopulationBattleGameAttemptResultView>> attemptsByStageId = new LinkedHashMap<>();

		populationBattleGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId)
			.forEach(attempt -> attemptsByStageId.computeIfAbsent(attempt.getStage().getId(), ignored -> new ArrayList<>())
				.add(new PopulationBattleGameAttemptResultView(
					attempt.getAttemptNumber(),
					attempt.getSelectedOptionNumber(),
					attempt.getSelectedCountryName(),
					attempt.getCorrect(),
					attempt.getLivesRemainingAfter(),
					attempt.getAttemptedAt()
				)));

		List<PopulationBattleGameStageResultView> stages = populationBattleGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId)
			.stream()
			.map(stage -> new PopulationBattleGameStageResultView(
				stage.getStageNumber(),
				stage.getQuestionPrompt(),
				stage.getOptionOneCountryName(),
				stage.getOptionOnePopulation(),
				stage.getOptionTwoCountryName(),
				stage.getOptionTwoPopulation(),
				stage.getCorrectCountryName(),
				stage.getStatus(),
				stage.getAttemptCount(),
				stage.getAwardedScore(),
				stage.getClearedAt(),
				attemptsByStageId.getOrDefault(stage.getId(), List.of())
			))
			.toList();

		int totalAttemptCount = attemptsByStageId.values().stream().mapToInt(List::size).sum();
		int firstTryClearCount = (int) stages.stream()
			.filter(stage -> stage.status() == PopulationBattleGameStageStatus.CLEARED)
			.filter(stage -> stage.attemptCount() == 1)
			.count();

		return new PopulationBattleGameSessionResultView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getClearedStageCount(),
			totalAttemptCount,
			firstTryClearCount,
			session.getTotalScore(),
			session.getCurrentStageNumber(),
			session.getLivesRemaining(),
			session.getStartedAt(),
			session.getFinishedAt(),
			stages
		);
	}

	private PopulationBattleGameSession getSession(UUID sessionId) {
		return populationBattleGameSessionRepository.findById(sessionId)
			.orElseThrow(() -> new ResourceNotFoundException("게임 세션을 찾을 수 없습니다: " + sessionId));
	}

	private PopulationBattleGameStage getStage(UUID sessionId, Integer stageNumber) {
		return populationBattleGameStageRepository.findBySessionIdAndStageNumber(sessionId, stageNumber)
			.orElseThrow(() -> new ResourceNotFoundException("게임 Stage를 찾을 수 없습니다."));
	}

	private String normalizeNickname(String nickname) {
		if (nickname == null || nickname.isBlank()) {
			return "Guest";
		}

		return nickname.trim();
	}

	private List<Country> getCountriesSortedByPopulation() {
		return countryRepository.findAll()
			.stream()
			.filter(country -> country.getPopulation() != null && country.getPopulation() > 0)
			.sorted(
				Comparator.comparing(Country::getPopulation, Comparator.reverseOrder())
					.thenComparing(Country::getNameKr)
			)
			.toList();
	}

	private PopulationBattleGameDifficultyPlan resolveDifficulty(Integer stageNumber) {
		return populationBattleGameDifficultyPolicy.resolve(stageNumber, getCountriesSortedByPopulation().size());
	}

	private List<PopulationBattleOptionView> toOptionViews(PopulationBattleGameStage stage) {
		return List.of(
			new PopulationBattleOptionView(1, stage.getOptionOneCountryName()),
			new PopulationBattleOptionView(2, stage.getOptionTwoCountryName())
		);
	}

	private void createNextStage(
		PopulationBattleGameSession session,
		Integer stageNumber,
		List<Country> sortedCountries,
		List<PopulationBattleGameStage> existingStages
	) {
		if (populationBattleGameStageRepository.findBySessionIdAndStageNumber(session.getId(), stageNumber).isPresent()) {
			return;
		}

		PopulationBattleGameDifficultyPlan difficultyPlan = populationBattleGameDifficultyPolicy.resolve(stageNumber, sortedCountries.size());
		PopulationBattlePair pair = selectPairForStage(sortedCountries, existingStages, difficultyPlan);
		PopulationBattleRoundOptions options = populationBattleGameOptionGenerator.generate(
			pair.morePopulousCountry(),
			pair.lessPopulousCountry()
		);

		populationBattleGameStageRepository.save(
			PopulationBattleGameStage.create(
				session,
				stageNumber,
				QUESTION_PROMPT,
				options,
				options.correctOptionNumber()
			)
		);
		session.planNextStage(stageNumber);
	}

	private PopulationBattlePair selectPairForStage(
		List<Country> sortedCountries,
		List<PopulationBattleGameStage> existingStages,
		PopulationBattleGameDifficultyPlan difficultyPlan
	) {
		List<Country> difficultyPool = new ArrayList<>(sortedCountries.subList(0, difficultyPlan.candidatePoolSize()));
		Set<String> usedPairSignatures = existingStages.stream()
			.map(PopulationBattleGameStage::pairSignature)
			.collect(Collectors.toSet());

		for (int attempt = 0; attempt < 120; attempt++) {
			PopulationBattlePair pair = randomPair(difficultyPool, difficultyPlan);
			if (!usedPairSignatures.contains(pair.signature())) {
				return pair;
			}
		}

		return randomPair(difficultyPool, difficultyPlan);
	}

	private PopulationBattlePair randomPair(
		List<Country> difficultyPool,
		PopulationBattleGameDifficultyPlan difficultyPlan
	) {
		int maxGap = Math.min(difficultyPlan.maximumRankGap(), difficultyPool.size() - 1);
		int minGap = Math.min(difficultyPlan.minimumRankGap(), maxGap);
		if (maxGap < 1) {
			throw new IllegalStateException("인구 비교 쌍을 만들기 위한 후보 풀이 부족합니다.");
		}

		int morePopulousIndex = ThreadLocalRandom.current()
			.nextInt(Math.max(1, difficultyPool.size() - minGap));
		int maxGapForAnchor = Math.min(maxGap, difficultyPool.size() - 1 - morePopulousIndex);
		int gapLowerBound = Math.max(1, Math.min(minGap, maxGapForAnchor));
		int gap = gapLowerBound == maxGapForAnchor
			? gapLowerBound
			: ThreadLocalRandom.current().nextInt(gapLowerBound, maxGapForAnchor + 1);

		return new PopulationBattlePair(
			difficultyPool.get(morePopulousIndex),
			difficultyPool.get(morePopulousIndex + gap)
		);
	}

	private PopulationBattleGameAnswerOutcome determineOutcome(boolean correct, GameSessionStatus status) {
		if (correct) {
			return status == GameSessionStatus.FINISHED
				? PopulationBattleGameAnswerOutcome.FINISHED
				: PopulationBattleGameAnswerOutcome.CORRECT;
		}

		return status == GameSessionStatus.GAME_OVER
			? PopulationBattleGameAnswerOutcome.GAME_OVER
			: PopulationBattleGameAnswerOutcome.WRONG;
	}

	private record PopulationBattlePair(
		Country morePopulousCountry,
		Country lessPopulousCountry
	) {
		String signature() {
			return morePopulousCountry.getIso3Code() + ":" + lessPopulousCountry.getIso3Code();
		}
	}
}
