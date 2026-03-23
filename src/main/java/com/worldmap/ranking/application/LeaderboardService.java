package com.worldmap.ranking.application;

import com.worldmap.game.location.domain.LocationGameSession;
import com.worldmap.game.population.domain.PopulationGameSession;
import com.worldmap.ranking.domain.LeaderboardGameLevel;
import com.worldmap.ranking.domain.LeaderboardGameMode;
import com.worldmap.ranking.domain.LeaderboardRecord;
import com.worldmap.ranking.domain.LeaderboardRecordRepository;
import com.worldmap.ranking.domain.LeaderboardScope;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class LeaderboardService {

	private static final Logger log = LoggerFactory.getLogger(LeaderboardService.class);
	private static final LeaderboardGameLevel LEVEL_1 = LeaderboardGameLevel.LEVEL_1;

	private final LeaderboardRecordRepository leaderboardRecordRepository;
	private final LeaderboardRankingPolicy leaderboardRankingPolicy;
	private final StringRedisTemplate stringRedisTemplate;
	private final String keyPrefix;

	public LeaderboardService(
		LeaderboardRecordRepository leaderboardRecordRepository,
		LeaderboardRankingPolicy leaderboardRankingPolicy,
		StringRedisTemplate stringRedisTemplate,
		@Value("${worldmap.ranking.key-prefix:leaderboard}") String keyPrefix
	) {
		this.leaderboardRecordRepository = leaderboardRecordRepository;
		this.leaderboardRankingPolicy = leaderboardRankingPolicy;
		this.stringRedisTemplate = stringRedisTemplate;
		this.keyPrefix = keyPrefix;
	}

	@Transactional
	public void recordLocationLevelOneResult(LocationGameSession session, Integer totalAttemptCount) {
		recordResult(
			LeaderboardGameMode.LOCATION,
			session.getId(),
			session.getPlayerNickname(),
			session.getTotalScore(),
			session.getClearedStageCount(),
			totalAttemptCount,
			session.getFinishedAt()
		);
	}

	@Transactional
	public void recordPopulationLevelOneResult(PopulationGameSession session, Integer totalAttemptCount) {
		recordResult(
			LeaderboardGameMode.POPULATION,
			session.getId(),
			session.getPlayerNickname(),
			session.getTotalScore(),
			session.getClearedStageCount(),
			totalAttemptCount,
			session.getFinishedAt()
		);
	}

	@Transactional(readOnly = true)
	public LeaderboardView getLeaderboard(String rawGameMode, String rawScope, Integer limit) {
		return getLeaderboard(
			LeaderboardGameMode.from(rawGameMode),
			LeaderboardScope.from(rawScope),
			limit
		);
	}

	@Transactional(readOnly = true)
	public LeaderboardView getLeaderboard(LeaderboardGameMode gameMode, LeaderboardScope scope, Integer limit) {
		int sanitizedLimit = sanitizeLimit(limit);
		LocalDate targetDate = scope == LeaderboardScope.DAILY ? LocalDate.now() : null;
		List<Long> recordIds = topRecordIdsFromRedis(gameMode, scope, targetDate, sanitizedLimit);
		List<LeaderboardRecord> orderedRecords;

		if (recordIds.isEmpty()) {
			orderedRecords = topRecordsFromDatabase(gameMode, scope, targetDate, sanitizedLimit);
			syncRecordsToRedis(orderedRecords, scope, targetDate);
		} else {
			orderedRecords = orderedRecordsById(recordIds);

			if (orderedRecords.size() != recordIds.size()) {
				orderedRecords = topRecordsFromDatabase(gameMode, scope, targetDate, sanitizedLimit);
				rebuildRedisKey(gameMode, scope, targetDate, orderedRecords);
			}
		}

		List<LeaderboardEntryView> entries = new ArrayList<>();
		for (int index = 0; index < orderedRecords.size(); index++) {
			LeaderboardRecord record = orderedRecords.get(index);
			entries.add(new LeaderboardEntryView(
				index + 1,
				record.getPlayerNickname(),
				record.getTotalScore(),
				record.getClearedStageCount(),
				record.getTotalAttemptCount(),
				record.getFinishedAt()
			));
		}

		return new LeaderboardView(gameMode, LEVEL_1, scope, targetDate, List.copyOf(entries));
	}

	private void recordResult(
		LeaderboardGameMode gameMode,
		UUID sessionId,
		String playerNickname,
		Integer totalScore,
		Integer clearedStageCount,
		Integer totalAttemptCount,
		LocalDateTime finishedAt
	) {
		if (finishedAt == null) {
			throw new IllegalStateException("종료 시간이 없는 게임은 랭킹에 반영할 수 없습니다.");
		}

		String runSignature = runSignature(gameMode, sessionId, finishedAt);
		if (leaderboardRecordRepository.findByRunSignature(runSignature).isPresent()) {
			return;
		}

		long rankingScore = leaderboardRankingPolicy.rankingScore(totalScore, clearedStageCount, totalAttemptCount);
		LeaderboardRecord record = leaderboardRecordRepository.saveAndFlush(
			LeaderboardRecord.create(
				runSignature,
				sessionId,
				gameMode,
				LEVEL_1,
				playerNickname,
				totalScore,
				rankingScore,
				clearedStageCount,
				totalAttemptCount,
				finishedAt
			)
		);

		runAfterCommit(() -> {
			try {
				syncRecordToRedis(record);
			} catch (RuntimeException ex) {
				log.warn("Failed to sync leaderboard record {} to redis", record.getId(), ex);
			}
		});
	}

	private List<Long> topRecordIdsFromRedis(
		LeaderboardGameMode gameMode,
		LeaderboardScope scope,
		LocalDate targetDate,
		Integer limit
	) {
		Collection<TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
			.reverseRangeWithScores(redisKey(gameMode, scope, targetDate), 0, limit - 1);

		if (tuples == null || tuples.isEmpty()) {
			return List.of();
		}

		return tuples.stream()
			.map(TypedTuple::getValue)
			.filter(Objects::nonNull)
			.map(Long::valueOf)
			.toList();
	}

	private List<LeaderboardRecord> orderedRecordsById(List<Long> recordIds) {
		Map<Long, LeaderboardRecord> recordsById = new LinkedHashMap<>();
		leaderboardRecordRepository.findAllById(recordIds)
			.forEach(record -> recordsById.put(record.getId(), record));

		return recordIds.stream()
			.map(recordsById::get)
			.filter(Objects::nonNull)
			.toList();
	}

	private List<LeaderboardRecord> topRecordsFromDatabase(
		LeaderboardGameMode gameMode,
		LeaderboardScope scope,
		LocalDate targetDate,
		Integer limit
	) {
		PageRequest pageRequest = PageRequest.of(0, limit);

		if (scope == LeaderboardScope.DAILY) {
			return leaderboardRecordRepository
				.findByGameModeAndGameLevelAndLeaderboardDateOrderByRankingScoreDescFinishedAtAsc(
					gameMode,
					LEVEL_1,
					targetDate,
					pageRequest
				)
				.getContent();
		}

		return leaderboardRecordRepository
			.findByGameModeAndGameLevelOrderByRankingScoreDescFinishedAtAsc(
				gameMode,
				LEVEL_1,
				pageRequest
			)
			.getContent();
	}

	private void rebuildRedisKey(
		LeaderboardGameMode gameMode,
		LeaderboardScope scope,
		LocalDate targetDate,
		List<LeaderboardRecord> records
	) {
		String redisKey = redisKey(gameMode, scope, targetDate);
		stringRedisTemplate.delete(redisKey);
		syncRecordsToRedis(records, scope, targetDate);
	}

	private void syncRecordsToRedis(List<LeaderboardRecord> records, LeaderboardScope scope, LocalDate targetDate) {
		for (LeaderboardRecord record : records) {
			if (scope == LeaderboardScope.DAILY) {
				addToRedis(redisKey(record.getGameMode(), LeaderboardScope.DAILY, targetDate), record);
				continue;
			}

			addToRedis(redisKey(record.getGameMode(), LeaderboardScope.ALL, null), record);
		}
	}

	private void syncRecordToRedis(LeaderboardRecord record) {
		addToRedis(redisKey(record.getGameMode(), LeaderboardScope.ALL, null), record);
		addToRedis(redisKey(record.getGameMode(), LeaderboardScope.DAILY, record.getLeaderboardDate()), record);
	}

	private void addToRedis(String redisKey, LeaderboardRecord record) {
		stringRedisTemplate.opsForZSet().add(
			redisKey,
			String.valueOf(record.getId()),
			record.getRankingScore().doubleValue()
		);
	}

	private void runAfterCommit(Runnable task) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			task.run();
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				task.run();
			}
		});
	}

	private String redisKey(LeaderboardGameMode gameMode, LeaderboardScope scope, LocalDate targetDate) {
		if (scope == LeaderboardScope.DAILY) {
			return "%s:daily:%s:%s:%s".formatted(
				keyPrefix,
				gameMode.getPathValue(),
				LEVEL_1.getRedisToken(),
				targetDate
			);
		}

		return "%s:all:%s:%s".formatted(keyPrefix, gameMode.getPathValue(), LEVEL_1.getRedisToken());
	}

	private String runSignature(LeaderboardGameMode gameMode, UUID sessionId, LocalDateTime finishedAt) {
		return "%s:%s:%s".formatted(gameMode.name(), sessionId, finishedAt);
	}

	private int sanitizeLimit(Integer limit) {
		if (limit == null) {
			return 10;
		}

		if (limit < 1 || limit > 50) {
			throw new IllegalArgumentException("랭킹 조회 개수는 1 이상 50 이하여야 합니다.");
		}

		return limit;
	}
}
