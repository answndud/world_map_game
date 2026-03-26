package com.worldmap.ranking.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardRecordRepository extends JpaRepository<LeaderboardRecord, Long> {

	Optional<LeaderboardRecord> findByRunSignature(String runSignature);

	List<LeaderboardRecord> findAllByGuestSessionKeyAndMemberIdIsNull(String guestSessionKey);

	long countByMemberId(Long memberId);

	long countByMemberIdAndGameModeAndGameLevel(
		Long memberId,
		LeaderboardGameMode gameMode,
		LeaderboardGameLevel gameLevel
	);

	Page<LeaderboardRecord> findByMemberIdOrderByFinishedAtDesc(Long memberId, Pageable pageable);

	long countByFinishedAtGreaterThanEqualAndFinishedAtLessThan(LocalDateTime startInclusive, LocalDateTime endExclusive);

	long countByGameModeAndFinishedAtGreaterThanEqualAndFinishedAtLessThan(
		LeaderboardGameMode gameMode,
		LocalDateTime startInclusive,
		LocalDateTime endExclusive
	);

	Optional<LeaderboardRecord> findFirstByMemberIdAndGameModeAndGameLevelOrderByRankingScoreDescFinishedAtAsc(
		Long memberId,
		LeaderboardGameMode gameMode,
		LeaderboardGameLevel gameLevel
	);

	Optional<LeaderboardRecord> findFirstByMemberIdAndGameModeOrderByRankingScoreDescFinishedAtAsc(
		Long memberId,
		LeaderboardGameMode gameMode
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
