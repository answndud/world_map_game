package com.worldmap.game.location.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationGameStageRepository extends JpaRepository<LocationGameStage, Long> {

	Optional<LocationGameStage> findBySessionIdAndStageNumber(UUID sessionId, Integer stageNumber);

	List<LocationGameStage> findAllBySessionIdOrderByStageNumber(UUID sessionId);

	void deleteAllBySessionId(UUID sessionId);
}
