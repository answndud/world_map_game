package com.worldmap.game.common.application;

public final class GameSubmissionGuard {

	private static final String STALE_SUBMISSION_MESSAGE = "이미 처리된 제출이거나 최신 Stage 상태가 아닙니다. 화면을 새로고침해주세요.";

	private GameSubmissionGuard() {
	}

	public static void assertFreshSubmission(
		Long actualStageId,
		Long expectedStageId,
		Integer actualAttemptNumber,
		Integer expectedAttemptNumber
	) {
		if (expectedStageId != null && !actualStageId.equals(expectedStageId)) {
			throw new IllegalStateException(STALE_SUBMISSION_MESSAGE);
		}

		if (expectedAttemptNumber != null && !actualAttemptNumber.equals(expectedAttemptNumber)) {
			throw new IllegalStateException(STALE_SUBMISSION_MESSAGE);
		}
	}
}
