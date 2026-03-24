package com.worldmap.game.location.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationGameSessionRepository extends JpaRepository<LocationGameSession, UUID> {

	List<LocationGameSession> findAllByGuestSessionKeyAndMemberIdIsNull(String guestSessionKey);

	long countByMemberIdAndFinishedAtIsNotNull(Long memberId);
}
