package com.worldmap.mypage.application;

import com.worldmap.auth.domain.Member;
import com.worldmap.auth.domain.MemberRepository;
import com.worldmap.common.exception.ResourceNotFoundException;
import com.worldmap.ranking.domain.LeaderboardGameLevel;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyPageService {

	private static final LeaderboardGameLevel LEVEL_1 = LeaderboardGameLevel.LEVEL_1;

	private final MemberRepository memberRepository;
	private final LeaderboardRecordRepository leaderboardRecordRepository;

	public MyPageService(MemberRepository memberRepository, LeaderboardRecordRepository leaderboardRecordRepository) {
		this.memberRepository = memberRepository;
		this.leaderboardRecordRepository = leaderboardRecordRepository;
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
			recentPlays
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
}
