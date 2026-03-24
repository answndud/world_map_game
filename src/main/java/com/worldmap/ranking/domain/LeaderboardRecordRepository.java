package com.worldmap.ranking.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardRecordRepository extends JpaRepository<LeaderboardRecord, Long> {

	Optional<LeaderboardRecord> findByRunSignature(String runSignature);

	List<LeaderboardRecord> findAllByGuestSessionKeyAndMemberIdIsNull(String guestSessionKey);

	long countByMemberId(Long memberId);

	Page<LeaderboardRecord> findByMemberIdOrderByFinishedAtDesc(Long memberId, Pageable pageable);

	Optional<LeaderboardRecord> findFirstByMemberIdAndGameModeAndGameLevelOrderByRankingScoreDescFinishedAtAsc(
		Long memberId,
		LeaderboardGameMode gameMode,
		LeaderboardGameLevel gameLevel
	);

	List<LeaderboardRecord> findAllByGameModeAndGameLevelOrderByRankingScoreDescFinishedAtAsc(
		LeaderboardGameMode gameMode,
		LeaderboardGameLevel gameLevel
	);

	Page<LeaderboardRecord> findByGameModeAndGameLevelOrderByRankingScoreDescFinishedAtAsc(
		LeaderboardGameMode gameMode,
		LeaderboardGameLevel gameLevel,
		Pageable pageable
	);

	Page<LeaderboardRecord> findByGameModeAndGameLevelAndLeaderboardDateOrderByRankingScoreDescFinishedAtAsc(
		LeaderboardGameMode gameMode,
		LeaderboardGameLevel gameLevel,
		LocalDate leaderboardDate,
		Pageable pageable
	);
}
