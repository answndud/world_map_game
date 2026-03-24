package com.worldmap.mypage.application;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.common.exception.ResourceNotFoundException;
import com.worldmap.game.location.domain.LocationGameSessionRepository;
import com.worldmap.game.location.domain.LocationGameStage;
import com.worldmap.game.location.domain.LocationGameStageRepository;
import com.worldmap.game.location.domain.LocationGameStageStatus;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameStage;
import com.worldmap.game.population.domain.PopulationGameStageRepository;
import com.worldmap.game.population.domain.PopulationGameStageStatus;
import com.worldmap.ranking.domain.LeaderboardGameLevel;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyPageService {

	private static final LeaderboardGameLevel LEVEL_1 = LeaderboardGameLevel.LEVEL_1;

	private final MemberRepository memberRepository;
	private final LeaderboardRecordRepository leaderboardRecordRepository;
	private final LocationGameSessionRepository locationGameSessionRepository;
	private final LocationGameStageRepository locationGameStageRepository;
	private final PopulationGameSessionRepository populationGameSessionRepository;
	private final PopulationGameStageRepository populationGameStageRepository;

	public MyPageService(
		MemberRepository memberRepository,
		LeaderboardRecordRepository leaderboardRecordRepository,
		LocationGameSessionRepository locationGameSessionRepository,
		LocationGameStageRepository locationGameStageRepository,
		PopulationGameSessionRepository populationGameSessionRepository,
		PopulationGameStageRepository populationGameStageRepository
	) {
		this.memberRepository = memberRepository;
		this.leaderboardRecordRepository = leaderboardRecordRepository;
		this.locationGameSessionRepository = locationGameSessionRepository;
		this.locationGameStageRepository = locationGameStageRepository;
		this.populationGameSessionRepository = populationGameSessionRepository;
		this.populationGameStageRepository = populationGameStageRepository;
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

		return new MyPageDashboardView(
			member.getNickname(),
			totalCompletedRuns,
			bestRunView(memberId, LeaderboardGameMode.LOCATION),
			bestRunView(memberId, LeaderboardGameMode.POPULATION),
			locationPerformanceView(memberId),
			populationPerformanceView(memberId),
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
		return performanceView("국가 인구수 맞추기", completedRunCount, clearedStages.stream().map(PopulationGameStage::getAttemptCount).toList());
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
			.findFirstByMemberIdAndGameModeAndGameLevelOrderByRankingScoreDescFinishedAtAsc(
				memberId,
				gameMode,
				LEVEL_1
			);

		if (bestRecord.isEmpty()) {
			return null;
		}

		LeaderboardRecord record = bestRecord.get();
		return new MyPageBestRunView(
			gameModeLabel(gameMode),
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
			.findAllByGameModeAndGameLevelOrderByRankingScoreDescFinishedAtAsc(
				targetRecord.getGameMode(),
				targetRecord.getGameLevel()
			);

		for (int index = 0; index < orderedRecords.size(); index++) {
			if (orderedRecords.get(index).getId().equals(targetRecord.getId())) {
				return index + 1;
			}
		}

		return null;
	}

	private String gameModeLabel(LeaderboardGameMode gameMode) {
		return switch (gameMode) {
			case LOCATION -> "국가 위치 찾기";
			case POPULATION -> "국가 인구수 맞추기";
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
