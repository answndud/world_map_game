package com.worldmap.admin.application;

import com.worldmap.recommendation.application.RecommendationCountryProfileCatalog;
import com.worldmap.recommendation.application.RecommendationFeedbackInsightsView;
import com.worldmap.recommendation.application.RecommendationFeedbackService;
import com.worldmap.recommendation.application.RecommendationQuestionCatalog;
import com.worldmap.recommendation.application.RecommendationSurveyService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {

	private final RecommendationFeedbackService recommendationFeedbackService;
	private final RecommendationQuestionCatalog recommendationQuestionCatalog;
	private final RecommendationCountryProfileCatalog recommendationCountryProfileCatalog;

	public AdminDashboardService(
		RecommendationFeedbackService recommendationFeedbackService,
		RecommendationQuestionCatalog recommendationQuestionCatalog,
		RecommendationCountryProfileCatalog recommendationCountryProfileCatalog
	) {
		this.recommendationFeedbackService = recommendationFeedbackService;
		this.recommendationQuestionCatalog = recommendationQuestionCatalog;
		this.recommendationCountryProfileCatalog = recommendationCountryProfileCatalog;
	}

	@Transactional(readOnly = true)
	public AdminDashboardView loadDashboard() {
		RecommendationFeedbackInsightsView feedbackInsights = recommendationFeedbackService.summarizeByVersion();
		return new AdminDashboardView(
			RecommendationSurveyService.SURVEY_VERSION,
			RecommendationSurveyService.ENGINE_VERSION,
			recommendationQuestionCatalog.questions().size(),
			recommendationCountryProfileCatalog.profiles().size(),
			feedbackInsights.totalResponses(),
			feedbackInsights.trackedVersionCount(),
			feedbackInsights.overallAverageSatisfaction(),
			adminRoutes(),
			focusItems()
		);
	}

	private List<AdminDashboardRouteView> adminRoutes() {
		return List.of(
			new AdminDashboardRouteView(
				"추천 만족도 집계",
				"설문 버전과 엔진 버전 조합별 평균 점수와 응답 수를 확인합니다.",
				"/admin/recommendation/feedback"
			),
			new AdminDashboardRouteView(
				"페르소나 baseline",
				"18개 평가 시나리오와 weak scenario를 기준으로 다음 개정 대상을 확인합니다.",
				"/admin/recommendation/persona-baseline"
			),
			new AdminDashboardRouteView(
				"공개 홈 점검",
				"플레이어에게 보이는 홈 화면과 진입 동선을 바로 확인합니다.",
				"/"
			),
			new AdminDashboardRouteView(
				"추천 설문 점검",
				"현재 public 설문 문구와 8개 질문 구성을 그대로 확인합니다.",
				"/recommendation/survey"
			)
		);
	}

	private List<AdminDashboardFocusView> focusItems() {
		return List.of(
			new AdminDashboardFocusView(
				"public 화면은 제품 언어만 유지",
				"버전, 집계, 구현 단계 같은 운영 정보는 public 화면에 직접 노출하지 않습니다."
			),
			new AdminDashboardFocusView(
				"추천 결과는 저장하지 않고 만족도만 수집",
				"설문 개선 신호는 surveyVersion, engineVersion, 만족도 점수, 답변 스냅샷으로만 남깁니다."
			),
			new AdminDashboardFocusView(
				"권한 제어는 8단계에서 추가",
				"지금 admin은 정보 구조를 분리한 read-only 운영 화면이며, 인증과 권한은 다음 단계에서 붙입니다."
			)
		);
	}
}
