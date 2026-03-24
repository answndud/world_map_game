package com.worldmap.game.location.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationGameSessionRepository extends JpaRepository<LocationGameSession, UUID> {

	List<LocationGameSession> findAllByGuestSessionKeyAndMemberIdIsNull(String guestSessionKey);

	long countByMemberIdAndFinishedAtIsNotNull(Long memberId);

	long countByStartedAtGreaterThanEqualAndStartedAtLessThan(LocalDateTime startInclusive, LocalDateTime endExclusive);

	@Query("""
		select distinct session.memberId
		from LocationGameSession session
		where session.startedAt >= :startInclusive
		  and session.startedAt < :endExclusive
		  and session.memberId is not null
		""")
	List<Long> findDistinctMemberIdsByStartedAtBetween(
		@Param("startInclusive") LocalDateTime startInclusive,
		@Param("endExclusive") LocalDateTime endExclusive
	);

	@Query("""
		select distinct session.guestSessionKey
		from LocationGameSession session
		where session.startedAt >= :startInclusive
		  and session.startedAt < :endExclusive
		  and session.guestSessionKey is not null
		""")
	List<String> findDistinctGuestSessionKeysByStartedAtBetween(
		@Param("startInclusive") LocalDateTime startInclusive,
		@Param("endExclusive") LocalDateTime endExclusive
	);
}
