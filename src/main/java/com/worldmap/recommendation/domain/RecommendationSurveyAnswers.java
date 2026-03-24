package com.worldmap.recommendation.domain;

public record RecommendationSurveyAnswers(
	ClimatePreference climatePreference,
	PacePreference pacePreference,
	BudgetPreference budgetPreference,
	EnvironmentPreference environmentPreference,
	EnglishImportance englishImportance,
	PriorityFocus priorityFocus,
	SettlementPreference settlementPreference,
	MobilityPreference mobilityPreference
) {

	public interface SurveyOption {

		String label();

		String description();
	}

	public enum ClimatePreference implements SurveyOption {
		WARM("따뜻한 기후", "햇빛과 온난한 날씨를 더 오래 즐기고 싶습니다.", 5),
		MILD("온화한 기후", "사계절이 있지만 너무 극단적이지 않은 기후를 선호합니다.", 3),
		COLD("차가운 기후", "서늘하고 차분한 북유럽형 기후도 괜찮습니다.", 1);

		private final String label;
		private final String description;
		private final int targetValue;

		ClimatePreference(String label, String description, int targetValue) {
			this.label = label;
			this.description = description;
			this.targetValue = targetValue;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String description() {
			return description;
		}

		public int targetValue() {
			return targetValue;
		}
	}

	public enum PacePreference implements SurveyOption {
		FAST("빠른 도시 리듬", "교통, 일자리, 활동량이 많은 도시 리듬이 맞습니다.", 5),
		BALANCED("균형 잡힌 생활", "도시와 여유가 적당히 섞인 속도를 원합니다.", 3),
		RELAXED("느긋한 생활", "조용하고 여유 있는 생활 속도를 선호합니다.", 1);

		private final String label;
		private final String description;
		private final int targetValue;

		PacePreference(String label, String description, int targetValue) {
			this.label = label;
			this.description = description;
			this.targetValue = targetValue;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String description() {
			return description;
		}

		public int targetValue() {
			return targetValue;
		}
	}

	public enum BudgetPreference implements SurveyOption {
		LOW("낮은 물가가 중요", "생활비 부담이 너무 큰 국가는 피하고 싶습니다.", 2),
		MEDIUM("중간 수준 허용", "비싸더라도 장점이 분명하면 고려할 수 있습니다.", 3),
		HIGH("높은 물가도 가능", "생활 인프라가 좋다면 높은 물가도 감수할 수 있습니다.", 5);

		private final String label;
		private final String description;
		private final int targetPriceLevel;

		BudgetPreference(String label, String description, int targetPriceLevel) {
			this.label = label;
			this.description = description;
			this.targetPriceLevel = targetPriceLevel;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String description() {
			return description;
		}

		public int targetPriceLevel() {
			return targetPriceLevel;
		}
	}

	public enum EnvironmentPreference implements SurveyOption {
		CITY("도시 중심", "대도시 접근성과 활동량이 많은 환경을 원합니다.", 5),
		MIXED("도시와 자연의 균형", "도시 편의성과 자연 접근성이 함께 있으면 좋습니다.", 3),
		NATURE("자연 중심", "공원, 바다, 산처럼 자연이 생활 가까이에 있길 원합니다.", 1);

		private final String label;
		private final String description;
		private final int targetUrbanity;

		EnvironmentPreference(String label, String description, int targetUrbanity) {
			this.label = label;
			this.description = description;
			this.targetUrbanity = targetUrbanity;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String description() {
			return description;
		}

		public int targetUrbanity() {
			return targetUrbanity;
		}
	}

	public enum EnglishImportance implements SurveyOption {
		HIGH("영어 친화도가 매우 중요", "행정, 생활, 커뮤니티에서 영어 접근성이 높은 나라가 좋습니다.", 6),
		MEDIUM("있으면 좋다", "영어가 어느 정도 통하면 좋지만 절대 기준은 아닙니다.", 3),
		LOW("크게 중요하지 않다", "영어 사용 편의성보다 다른 조건이 더 중요합니다.", 0);

		private final String label;
		private final String description;
		private final int weight;

		EnglishImportance(String label, String description, int weight) {
			this.label = label;
			this.description = description;
			this.weight = weight;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String description() {
			return description;
		}

		public int weight() {
			return weight;
		}
	}

	public enum PriorityFocus implements SurveyOption {
		SAFETY("치안", "낯선 환경에서도 안정감을 느끼는 것이 가장 중요합니다."),
		WELFARE("복지", "공공 서비스와 사회 안전망이 잘 갖춰진 곳을 원합니다."),
		FOOD("음식", "식문화와 먹거리 만족도가 높은 곳이 좋습니다."),
		DIVERSITY("문화 다양성", "다양한 배경의 사람들이 섞여 있는 환경을 선호합니다.");

		private final String label;
		private final String description;

		PriorityFocus(String label, String description) {
			this.label = label;
			this.description = description;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String description() {
			return description;
		}
	}

	public enum SettlementPreference implements SurveyOption {
		EXPERIENCE("가볍게 살아보기", "짧게 경험하거나 일단 살아보며 판단하는 쪽에 가깝습니다."),
		BALANCED("둘 다 고려", "경험과 장기 정착 가능성을 함께 봅니다."),
		STABILITY("장기 정착 안정성", "오래 머물 가능성을 고려해 안전망과 정착 안정성을 더 중시합니다.");

		private final String label;
		private final String description;

		SettlementPreference(String label, String description) {
			this.label = label;
			this.description = description;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String description() {
			return description;
		}
	}

	public enum MobilityPreference implements SurveyOption {
		TRANSIT_FIRST("대중교통 / 도보 중심", "지하철, 버스, 도보 생활이 잘 되는 환경을 선호합니다."),
		BALANCED("둘 다 괜찮음", "대중교통과 개인 이동 방식 모두 크게 상관없습니다."),
		SPACE_FIRST("여유 공간 / 차량 이동도 괜찮음", "넓은 공간과 상대적으로 느긋한 이동 방식도 괜찮습니다.");

		private final String label;
		private final String description;

		MobilityPreference(String label, String description) {
			this.label = label;
			this.description = description;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String description() {
			return description;
		}
	}
}
