package com.worldmap.game.flag.application;

import com.worldmap.common.exception.ResourceNotFoundException;
import com.worldmap.game.common.domain.GameSessionStatus;
import com.worldmap.game.flag.domain.FlagGameAttempt;
import com.worldmap.game.flag.domain.FlagGameAttemptRepository;
import com.worldmap.game.flag.domain.FlagGameSession;
import com.worldmap.game.flag.domain.FlagGameSessionRepository;
import com.worldmap.game.flag.domain.FlagGameStage;
import com.worldmap.game.flag.domain.FlagGameStageRepository;
import com.worldmap.game.flag.domain.FlagGameStageStatus;
import com.worldmap.ranking.application.LeaderboardService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FlagGameService {

	private static final int MINIMUM_COUNTRY_COUNT = 4;

	private final FlagQuestionCountryPoolService flagQuestionCountryPoolService;
	private final FlagGameSessionRepository flagGameSessionRepository;
	private final FlagGameStageRepository flagGameStageRepository;
	private final FlagGameAttemptRepository flagGameAttemptRepository;
	private final FlagGameOptionGenerator flagGameOptionGenerator;
	private final FlagGameDifficultyPolicy flagGameDifficultyPolicy;
	private final FlagGameScoringPolicy flagGameScoringPolicy;
	private final LeaderboardService leaderboardService;

	public FlagGameService(
		FlagQuestionCountryPoolService flagQuestionCountryPoolService,
		FlagGameSessionRepository flagGameSessionRepository,
		FlagGameStageRepository flagGameStageRepository,
		FlagGameAttemptRepository flagGameAttemptRepository,
		FlagGameOptionGenerator flagGameOptionGenerator,
		FlagGameDifficultyPolicy flagGameDifficultyPolicy,
		FlagGameScoringPolicy flagGameScoringPolicy,
		LeaderboardService leaderboardService
	) {
		this.flagQuestionCountryPoolService = flagQuestionCountryPoolService;
		this.flagGameSessionRepository = flagGameSessionRepository;
		this.flagGameStageRepository = flagGameStageRepository;
		this.flagGameAttemptRepository = flagGameAttemptRepository;
		this.flagGameOptionGenerator = flagGameOptionGenerator;
		this.flagGameDifficultyPolicy = flagGameDifficultyPolicy;
		this.flagGameScoringPolicy = flagGameScoringPolicy;
		this.leaderboardService = leaderboardService;
	}

	@Transactional
	public FlagGameStartView startGuestGame(String nickname, String guestSessionKey) {
		return startGame(normalizeNickname(nickname), null, guestSessionKey);
	}

	@Transactional
	public FlagGameStartView startMemberGame(Long memberId, String memberNickname) {
		return startGame(memberNickname, memberId, null);
	}

	private FlagGameStartView startGame(
		String playerNickname,
		Long memberId,
		String guestSessionKey
	) {
		List<FlagQuestionCountryView> countries = getAvailableCountries();

		if (countries.size() < MINIMUM_COUNTRY_COUNT) {
			throw new IllegalStateException("국기 게임을 시작하기 위한 자산과 국가 데이터가 충분하지 않습니다.");
		}

		FlagGameSession session = FlagGameSession.ready(playerNickname, memberId, guestSessionKey, 1);
		session = flagGameSessionRepository.save(session);
		createNextStage(session, 1, countries, List.of());
		session.startGame(LocalDateTime.now());

		return new FlagGameStartView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getLivesRemaining(),
			"/games/flag/play/" + session.getId()
		);
	}

	@Transactional(readOnly = true)
	public FlagGameStateView getCurrentState(UUID sessionId) {
		FlagGameSession session = getSession(sessionId);

		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			throw new IllegalStateException("이미 종료된 게임입니다.");
		}

		FlagGameStage stage = getStage(sessionId, session.getCurrentStageNumber());
		FlagGameDifficultyPlan difficultyPlan = resolveDifficulty(stage.getStageNumber());

		return new FlagGameStateView(
			session.getId(),
			stage.getStageNumber(),
			difficultyPlan.label(),
			session.getClearedStageCount(),
			session.getTotalScore(),
			session.getLivesRemaining(),
			stage.getTargetFlagRelativePath(),
			toOptionViews(stage),
			session.getStatus()
		);
	}

	@Transactional
	public FlagGameStartView restartGame(UUID sessionId) {
		FlagGameSession session = getSession(sessionId);

		if (session.getStatus() != GameSessionStatus.GAME_OVER && session.getStatus() != GameSessionStatus.FINISHED) {
			throw new IllegalStateException("종료된 게임만 다시 시작할 수 있습니다.");
		}

		List<FlagQuestionCountryView> countries = getAvailableCountries();
		if (countries.size() < MINIMUM_COUNTRY_COUNT) {
			throw new IllegalStateException("국기 게임을 시작하기 위한 자산과 국가 데이터가 충분하지 않습니다.");
		}

		flagGameAttemptRepository.deleteAllByStageSessionId(sessionId);
		flagGameAttemptRepository.flush();
		flagGameStageRepository.deleteAllBySessionId(sessionId);
		flagGameStageRepository.flush();

		session.restart(1);
		createNextStage(session, 1, countries, List.of());
		session.startGame(LocalDateTime.now());

		return new FlagGameStartView(
			session.getId(),
			session.getPlayerNickname(),
			session.getStatus(),
			session.getTotalRounds(),
			session.getLivesRemaining(),
			"/games/flag/play/" + session.getId()
		);
	}

	@Transactional
	public FlagGameAnswerView submitAnswer(UUID sessionId, Integer stageNumber, Integer selectedOptionNumber) {
		if (selectedOptionNumber == null || selectedOptionNumber < 1 || selectedOptionNumber > 4) {
			throw new IllegalArgumentException("보기 번호를 선택해야 합니다.");
		}

		FlagGameSession session = getSession(sessionId);
		if (session.getStatus() != GameSessionStatus.IN_PROGRESS) {
			throw new IllegalStateException("진행 중인 게임만 답안을 제출할 수 있습니다.");
		}

		if (!session.getCurrentStageNumber().equals(stageNumber)) {
			throw new IllegalStateException("현재 진행 중인 Stage와 일치하지 않습니다.");
		}

		FlagGameStage stage = getStage(sessionId, stageNumber);
		int attemptNumber = stage.nextAttemptNumber();
		LocalDateTime attemptedAt = LocalDateTime.now();
		FlagAnswerJudgement judgement = flagGameScoringPolicy.judge(
			selectedOptionNumber,
			stage.getCorrectOptionNumber(),
			stageNumber,
			attemptNumber,
			session.getLivesRemaining()
		);

		String selectedCountryName = stage.getOptions().get(selectedOptionNumber - 1);
		String correctCountryName = stage.getOptions().get(stage.getCorrectOptionNumber() - 1);

		stage.recordAttempt(judgement.correct(), judgement.awardedScore(), attemptedAt);

		if (judgement.correct()) {
			List<FlagQuestionCountryView> countries = getAvailableCountries();
			List<FlagGameStage> existingStages = flagGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId);
			createNextStage(session, stageNumber + 1, countries, existingStages);
			session.clearCurrentStage(stageNumber, judgement.awardedScore(), attemptedAt);
		} else {
			session.recordWrongAttempt(stageNumber, attemptedAt);
			if (session.getStatus() == GameSessionStatus.GAME_OVER) {
				stage.markFailed();
			}
		}

		flagGameAttemptRepository.save(
			FlagGameAttempt.create(
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
			leaderboardService.recordFlagResult(
				session,
				Math.toIntExact(flagGameAttemptRepository.countByStageSessionId(sessionId))
			);
		}

		FlagGameAnswerOutcome outcome = determineOutcome(judgement.correct(), session.getStatus());
		FlagGameDifficultyPlan nextDifficultyPlan = session.getStatus() == GameSessionStatus.IN_PROGRESS
			? resolveDifficulty(session.getCurrentStageNumber())
			: null;

		return new FlagGameAnswerView(
			session.getId(),
			stage.getStageNumber(),
			stage.getTargetFlagRelativePath(),
			selectedOptionNumber,
			selectedCountryName,
			stage.getCorrectOptionNumber(),
			correctCountryName,
			judgement.correct(),
			judgement.awardedScore(),
			session.getTotalScore(),
			session.getClearedStageCount(),
			session.getLivesRemaining(),
			session.getStatus() == GameSessionStatus.IN_PROGRESS ? session.getCurrentStageNumber() : null,
			nextDifficultyPlan != null ? nextDifficultyPlan.label() : null,
			session.getStatus(),
			outcome,
			"/games/flag/result/" + session.getId()
		);
	}

	@Transactional(readOnly = true)
	public FlagGameSessionResultView getSessionResult(UUID sessionId) {
		FlagGameSession session = getSession(sessionId);
		Map<Long, List<FlagGameAttemptResultView>> attemptsByStageId = new LinkedHashMap<>();

		flagGameAttemptRepository.findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(sessionId)
			.forEach(attempt -> attemptsByStageId.computeIfAbsent(attempt.getStage().getId(), ignored -> new ArrayList<>())
				.add(new FlagGameAttemptResultView(
					attempt.getAttemptNumber(),
					attempt.getSelectedOptionNumber(),
					attempt.getSelectedCountryName(),
					attempt.getCorrect(),
					attempt.getLivesRemainingAfter(),
					attempt.getAttemptedAt()
				)));

		List<FlagGameStageResultView> stages = flagGameStageRepository.findAllBySessionIdOrderByStageNumber(sessionId)
			.stream()
			.map(stage -> new FlagGameStageResultView(
				stage.getStageNumber(),
				stage.getTargetFlagRelativePath(),
				stage.getStatus(),
				stage.getAttemptCount(),
				stage.getAwardedScore(),
				stage.getClearedAt(),
				attemptsByStageId.getOrDefault(stage.getId(), List.of())
			))
			.toList();
		int totalAttemptCount = attemptsByStageId.values().stream()
			.mapToInt(List::size)
			.sum();
		int firstTryClearCount = (int) stages.stream()
			.filter(stage -> stage.status() == FlagGameStageStatus.CLEARED)
			.filter(stage -> stage.attemptCount() == 1)
			.count();

		return new FlagGameSessionResultView(
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

	private FlagGameSession getSession(UUID sessionId) {
		return flagGameSessionRepository.findById(sessionId)
			.orElseThrow(() -> new ResourceNotFoundException("게임 세션을 찾을 수 없습니다: " + sessionId));
	}

	private FlagGameStage getStage(UUID sessionId, Integer stageNumber) {
		return flagGameStageRepository.findBySessionIdAndStageNumber(sessionId, stageNumber)
			.orElseThrow(() -> new ResourceNotFoundException("게임 Stage를 찾을 수 없습니다."));
	}

	private String normalizeNickname(String nickname) {
		if (nickname == null || nickname.isBlank()) {
			return "Guest";
		}

		return nickname.trim();
	}

	private List<FlagQuestionCountryView> getAvailableCountries() {
		return flagQuestionCountryPoolService.availableCountries();
	}

	private FlagGameDifficultyPlan resolveDifficulty(Integer stageNumber) {
		return flagGameDifficultyPolicy.resolve(stageNumber, getAvailableCountries().size());
	}

	private List<FlagOptionView> toOptionViews(FlagGameStage stage) {
		List<FlagOptionView> optionViews = new ArrayList<>();
		List<String> options = stage.getOptions();

		for (int index = 0; index < options.size(); index++) {
			optionViews.add(new FlagOptionView(index + 1, options.get(index)));
		}

		return List.copyOf(optionViews);
	}

	private void createNextStage(
		FlagGameSession session,
		Integer stageNumber,
		List<FlagQuestionCountryView> availableCountries,
		List<FlagGameStage> existingStages
	) {
		if (flagGameStageRepository.findBySessionIdAndStageNumber(session.getId(), stageNumber).isPresent()) {
			return;
		}

		FlagGameDifficultyPlan difficultyPlan = flagGameDifficultyPolicy.resolve(stageNumber, availableCountries.size());
		List<FlagQuestionCountryView> difficultyPool = takeDifficultyPool(availableCountries, difficultyPlan.candidatePoolSize());
		FlagQuestionCountryView nextCountry = selectCountryForStage(availableCountries, existingStages, difficultyPool);
		FlagRoundOptions roundOptions = flagGameOptionGenerator.generate(nextCountry, availableCountries);
		flagGameStageRepository.save(
			FlagGameStage.create(
				session,
				stageNumber,
				nextCountry,
				roundOptions.options(),
				roundOptions.correctOptionNumber()
			)
		);
		session.planNextStage(stageNumber);
	}

	private List<FlagQuestionCountryView> takeDifficultyPool(
		List<FlagQuestionCountryView> availableCountries,
		int candidatePoolSize
	) {
		List<FlagQuestionCountryView> shuffled = new ArrayList<>(availableCountries);
		Collections.shuffle(shuffled);
		return new ArrayList<>(shuffled.subList(0, Math.min(candidatePoolSize, shuffled.size())));
	}

	private FlagQuestionCountryView selectCountryForStage(
		List<FlagQuestionCountryView> availableCountries,
		List<FlagGameStage> existingStages,
		List<FlagQuestionCountryView> difficultyPool
	) {
		Set<String> usedIso3Codes = new HashSet<>();
		String lastCountryIso3Code = null;

		for (FlagGameStage existingStage : existingStages) {
			usedIso3Codes.add(existingStage.getCountryIso3Code());
			lastCountryIso3Code = existingStage.getCountryIso3Code();
		}

		List<FlagQuestionCountryView> freshCandidates = difficultyPool.stream()
			.filter(country -> !usedIso3Codes.contains(country.iso3Code()))
			.toList();

		if (!freshCandidates.isEmpty()) {
			return pickRandomCountry(freshCandidates);
		}

		List<FlagQuestionCountryView> widerFreshCandidates = availableCountries.stream()
			.filter(country -> !usedIso3Codes.contains(country.iso3Code()))
			.toList();

		if (!widerFreshCandidates.isEmpty()) {
			return pickRandomCountry(widerFreshCandidates);
		}

		if (lastCountryIso3Code != null) {
			String recentCountryIso3Code = lastCountryIso3Code;
			List<FlagQuestionCountryView> withoutImmediateRepeat = difficultyPool.stream()
				.filter(country -> !country.iso3Code().equals(recentCountryIso3Code))
				.toList();

			if (!withoutImmediateRepeat.isEmpty()) {
				return pickRandomCountry(withoutImmediateRepeat);
			}
		}

		return pickRandomCountry(difficultyPool);
	}

	private FlagQuestionCountryView pickRandomCountry(List<FlagQuestionCountryView> candidates) {
		return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
	}

	private FlagGameAnswerOutcome determineOutcome(boolean correct, GameSessionStatus status) {
		if (correct) {
			return status == GameSessionStatus.FINISHED
				? FlagGameAnswerOutcome.FINISHED
				: FlagGameAnswerOutcome.CORRECT;
		}

		return status == GameSessionStatus.GAME_OVER
			? FlagGameAnswerOutcome.GAME_OVER
			: FlagGameAnswerOutcome.WRONG;
	}
}
