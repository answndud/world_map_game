package com.worldmap.game.location.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationGameAttemptRepository extends JpaRepository<LocationGameAttempt, Long> {

	List<LocationGameAttempt> findAllByStageSessionIdOrderByStageStageNumberAscAttemptNumberAsc(UUID sessionId);

	void deleteAllByStageSessionId(UUID sessionId);
}
