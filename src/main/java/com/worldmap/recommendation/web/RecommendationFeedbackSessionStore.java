package com.worldmap.recommendation.web;

import com.worldmap.recommendation.application.RecommendationFeedbackContext;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RecommendationFeedbackSessionStore {

	private static final String FEEDBACK_CONTEXTS_ATTRIBUTE = "WORLDMAP_RECOMMENDATION_FEEDBACK_CONTEXTS";
	private static final int MAX_CONTEXTS_PER_SESSION = 10;

	public String store(HttpSession httpSession, RecommendationFeedbackContext context) {
		if (httpSession == null) {
			throw new IllegalArgumentException("추천 피드백 컨텍스트를 저장하려면 세션이 필요합니다.");
		}

		Map<String, RecommendationFeedbackContext> contexts = contexts(httpSession);
		String token = UUID.randomUUID().toString();
		contexts.put(token, context);
		trimToMaxSize(contexts);
		httpSession.setAttribute(FEEDBACK_CONTEXTS_ATTRIBUTE, contexts);
		return token;
	}

	public Optional<RecommendationFeedbackContext> consume(HttpSession httpSession, String feedbackToken) {
		if (httpSession == null || feedbackToken == null || feedbackToken.isBlank()) {
			return Optional.empty();
		}

		Map<String, RecommendationFeedbackContext> contexts = existingContexts(httpSession);
		if (contexts == null) {
			return Optional.empty();
		}

		RecommendationFeedbackContext context = contexts.remove(feedbackToken);
		if (contexts.isEmpty()) {
			httpSession.removeAttribute(FEEDBACK_CONTEXTS_ATTRIBUTE);
		} else {
			httpSession.setAttribute(FEEDBACK_CONTEXTS_ATTRIBUTE, contexts);
		}
		return Optional.ofNullable(context);
	}

	@SuppressWarnings("unchecked")
	private Map<String, RecommendationFeedbackContext> contexts(HttpSession httpSession) {
		Map<String, RecommendationFeedbackContext> existing = existingContexts(httpSession);
		if (existing != null) {
			return existing;
		}
		return new LinkedHashMap<>();
	}

	@SuppressWarnings("unchecked")
	private Map<String, RecommendationFeedbackContext> existingContexts(HttpSession httpSession) {
		Object attribute = httpSession.getAttribute(FEEDBACK_CONTEXTS_ATTRIBUTE);
		if (attribute instanceof Map<?, ?> storedContexts) {
			return (Map<String, RecommendationFeedbackContext>) storedContexts;
		}
		return null;
	}

	private void trimToMaxSize(Map<String, RecommendationFeedbackContext> contexts) {
		while (contexts.size() > MAX_CONTEXTS_PER_SESSION) {
			String eldestToken = contexts.keySet().iterator().next();
			contexts.remove(eldestToken);
		}
	}
}
