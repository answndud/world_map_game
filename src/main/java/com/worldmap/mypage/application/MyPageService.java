package com.worldmap.mypage.application;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.common.exception.ResourceNotFoundException;
import com.worldmap.game.capital.domain.CapitalGameSessionRepository;
import com.worldmap.game.capital.domain.CapitalGameStage;
import com.worldmap.game.capital.domain.CapitalGameStageRepository;
import com.worldmap.game.capital.domain.CapitalGameStageStatus;
import com.worldmap.game.flag.domain.FlagGameSessionRepository;
import com.worldmap.game.flag.domain.FlagGameStage;
import com.worldmap.game.flag.domain.FlagGameStageRepository;
import com.worldmap.game.flag.domain.FlagGameStageStatus;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.location.domain.LocationGameStage;
import com.worldmap.game.location.domain.LocationGameStageRepository;
import com.worldmap.game.location.domain.LocationGameStageStatus;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameStage;
import com.worldmap.game.population.domain.PopulationGameStageRepository;
import com.worldmap.game.population.domain.PopulationGameStageStatus;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSessionRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStage;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStageRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStageStatus;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyPageService {

	private final MemberRepository memberRepository;
	private final LeaderboardRecordRepository leaderboardRecordRepository;
	private final LocationGameSessionRepository locationGameSessionRepository;
	private final LocationGameStageRepository locationGameStageRepository;
	private final PopulationGameSessionRepository populationGameSessionRepository;
	private final PopulationGameStageRepository populationGameStageRepository;
	private final CapitalGameSessionRepository capitalGameSessionRepository;
	private final CapitalGameStageRepository capitalGameStageRepository;
	private final FlagGameSessionRepository flagGameSessionRepository;
	private final FlagGameStageRepository flagGameStageRepository;
	private final PopulationBattleGameSessionRepository populationBattleGameSessionRepository;
	private final PopulationBattleGameStageRepository populationBattleGameStageRepository;

	public MyPageService(
		MemberRepository memberRepository,
		LeaderboardRecordRepository leaderboardRecordRepository,
		LocationGameSessionRepository locationGameSessionRepository,
		LocationGameStageRepository locationGameStageRepository,
		PopulationGameSessionRepository populationGameSessionRepository,
		PopulationGameStageRepository populationGameStageRepository,
		CapitalGameSessionRepository capitalGameSessionRepository,
		CapitalGameStageRepository capitalGameStageRepository,
		FlagGameSessionRepository flagGameSessionRepository,
		FlagGameStageRepository flagGameStageRepository,
		PopulationBattleGameSessionRepository populationBattleGameSessionRepository,
		PopulationBattleGameStageRepository populationBattleGameStageRepository
	) {
		this.memberRepository = memberRepository;
		this.leaderboardRecordRepository = leaderboardRecordRepository;
		this.locationGameSessionRepository = locationGameSessionRepository;
		this.locationGameStageRepository = locationGameStageRepository;
		this.populationGameSessionRepository = populationGameSessionRepository;
		this.populationGameStageRepository = populationGameStageRepository;
		this.capitalGameSessionRepository = capitalGameSessionRepository;
		this.capitalGameStageRepository = capitalGameStageRepository;
		this.flagGameSessionRepository = flagGameSessionRepository;
		this.flagGameStageRepository = flagGameStageRepository;
		this.populationBattleGameSessionRepository = populationBattleGameSessionRepository;
		this.populationBattleGameStageRepository = populationBattleGameStageRepository;
	}

	@Transactional(readOnly = true)
	public MyPageDashboardView loadDashboard(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + memberId));

		long totalCompletedRuns = leaderboardRecordRepository.countByMemberId(memberId);
		List<MyPageRecentPlayView> recentPlays = leaderboardRecordRepository
			.findByMemberIdOrderByFinishedAtDesc(memberId, PageRequest.of(0, 10))
			.getContent()
			.stream()
			.map(this::toRecentPlayView)
			.toList();
		List<MyPageBestRunView> bestRuns = orderedGameModes().stream()
			.map(gameMode -> bestRunView(memberId, gameMode))
			.filter(java.util.Objects::nonNull)
			.toList();
		List<MyPageModePerformanceView> modePerformances = Stream.of(
			locationPerformanceView(memberId),
			capitalPerformanceView(memberId),
			flagPerformanceView(memberId),
			populationBattlePerformanceView(memberId),
			populationPerformanceView(memberId)
		)
			.filter(java.util.Objects::nonNull)
			.toList();

		return new MyPageDashboardView(
			member.getNickname(),
			totalCompletedRuns,
			bestRuns,
			modePerformances,
			recentPlays
		);
	}

	private MyPageModePerformanceView locationPerformanceView(Long memberId) {
		long completedRunCount = locationGameSessionRepository.countByMemberIdAndFinishedAtIsNotNull(memberId);
		List<LocationGameStage> clearedStages = locationGameStageRepository
			.findAllBySessionMemberIdAndSessionFinishedAtIsNotNullAndStatus(memberId, LocationGameStageStatus.CLEARED);
		return performanceView("국가 위치 찾기", completedRunCount, clearedStages.stream().map(LocationGameStage::getAttemptCount).toList());
	}

	private MyPageModePerformanceView populationPerformanceView(Long memberId) {
		long completedRunCount = populationGameSessionRepository.countByMemberIdAndFinishedAtIsNotNull(memberId);
		List<PopulationGameStage> clearedStages = populationGameStageRepository
			.findAllBySessionMemberIdAndSessionFinishedAtIsNotNullAndStatus(memberId, PopulationGameStageStatus.CLEARED);
		return performanceView("인구수 퀴즈", completedRunCount, clearedStages.stream().map(PopulationGameStage::getAttemptCount).toList());
	}

	private MyPageModePerformanceView capitalPerformanceView(Long memberId) {
		long completedRunCount = capitalGameSessionRepository.countByMemberIdAndFinishedAtIsNotNull(memberId);
		List<CapitalGameStage> clearedStages = capitalGameStageRepository
			.findAllBySessionMemberIdAndSessionFinishedAtIsNotNullAndStatus(memberId, CapitalGameStageStatus.CLEARED);
		return performanceView("수도 퀴즈", completedRunCount, clearedStages.stream().map(CapitalGameStage::getAttemptCount).toList());
	}

	private MyPageModePerformanceView flagPerformanceView(Long memberId) {
		long completedRunCount = flagGameSessionRepository.countByMemberIdAndFinishedAtIsNotNull(memberId);
		List<FlagGameStage> clearedStages = flagGameStageRepository
			.findAllBySessionMemberIdAndSessionFinishedAtIsNotNullAndStatus(memberId, FlagGameStageStatus.CLEARED);
		return performanceView("국기 퀴즈", completedRunCount, clearedStages.stream().map(FlagGameStage::getAttemptCount).toList());
	}

	private MyPageModePerformanceView populationBattlePerformanceView(Long memberId) {
		long completedRunCount = populationBattleGameSessionRepository.countByMemberIdAndFinishedAtIsNotNull(memberId);
		List<PopulationBattleGameStage> clearedStages = populationBattleGameStageRepository
			.findAllBySessionMemberIdAndSessionFinishedAtIsNotNullAndStatus(memberId, PopulationBattleGameStageStatus.CLEARED);
		return performanceView(
			"인구 비교 배틀",
			completedRunCount,
			clearedStages.stream().map(PopulationBattleGameStage::getAttemptCount).toList()
		);
	}

	private MyPageModePerformanceView performanceView(
		String gameModeLabel,
		long completedRunCount,
		List<Integer> clearedStageAttemptCounts
	) {
		if (completedRunCount == 0 && clearedStageAttemptCounts.isEmpty()) {
			return null;
		}

		long clearedStageCount = clearedStageAttemptCounts.size();
		long firstTryClearCount = clearedStageAttemptCounts.stream()
			.filter(attemptCount -> attemptCount == 1)
			.count();
		String firstTryClearRateLabel = clearedStageCount == 0
			? "기록 없음"
			: formatNumber((double) firstTryClearCount * 100.0 / clearedStageCount, "%");
		String averageAttemptsPerClearLabel = clearedStageCount == 0
			? "기록 없음"
			: formatNumber(
				clearedStageAttemptCounts.stream().mapToInt(Integer::intValue).average().orElse(0.0),
				"회"
			);

		return new MyPageModePerformanceView(
			gameModeLabel,
			completedRunCount,
			clearedStageCount,
			firstTryClearRateLabel,
			averageAttemptsPerClearLabel
		);
	}

	private MyPageBestRunView bestRunView(Long memberId, LeaderboardGameMode gameMode) {
		Optional<LeaderboardRecord> bestRecord = leaderboardRecordRepository
			.findFirstByMemberIdAndGameModeOrderByRankingScoreDescFinishedAtAsc(memberId, gameMode);

		if (bestRecord.isEmpty()) {
			return null;
		}

		return toBestRunView(memberId, bestRecord.get());
	}

	private MyPageBestRunView toBestRunView(Long memberId, LeaderboardRecord record) {
		long completedRunCount = leaderboardRecordRepository.countByMemberIdAndGameMode(memberId, record.getGameMode());
		return new MyPageBestRunView(
			gameModeLabel(record.getGameMode()),
			completedRunCount,
			record.getTotalScore(),
			rankFor(record),
			record.getClearedStageCount()
		);
	}

	private MyPageRecentPlayView toRecentPlayView(LeaderboardRecord record) {
		return new MyPageRecentPlayView(
			gameModeLabel(record.getGameMode()),
			record.getTotalScore(),
			record.getClearedStageCount(),
			record.getTotalAttemptCount(),
			rankFor(record),
			record.getFinishedAt(),
			record.getPlayerNickname()
		);
	}

	private Integer rankFor(LeaderboardRecord targetRecord) {
		List<LeaderboardRecord> orderedRecords = leaderboardRecordRepository
			.findAllByGameModeOrderByRankingScoreDescFinishedAtAsc(targetRecord.getGameMode());

		for (int index = 0; index < orderedRecords.size(); index++) {
			if (orderedRecords.get(index).getId().equals(targetRecord.getId())) {
				return index + 1;
			}
		}

		return null;
	}

	private List<LeaderboardGameMode> orderedGameModes() {
		return List.of(
			LeaderboardGameMode.LOCATION,
			LeaderboardGameMode.CAPITAL,
			LeaderboardGameMode.FLAG,
			LeaderboardGameMode.POPULATION_BATTLE,
			LeaderboardGameMode.POPULATION
		);
	}

	private String gameModeLabel(LeaderboardGameMode gameMode) {
		return switch (gameMode) {
			case LOCATION -> "국가 위치 찾기";
			case CAPITAL -> "수도 퀴즈";
			case FLAG -> "국기 퀴즈";
			case POPULATION_BATTLE -> "인구 비교 배틀";
			case POPULATION -> "인구수 퀴즈";
		};
	}

	private String formatNumber(double value, String suffix) {
		double roundedToWhole = Math.rint(value);
		if (Math.abs(value - roundedToWhole) < 0.0001) {
			return (long) roundedToWhole + suffix;
		}
		return String.format(Locale.US, "%.1f%s", value, suffix);
	}
}
