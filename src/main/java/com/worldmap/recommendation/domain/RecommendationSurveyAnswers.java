package com.worldmap.recommendation.domain;

public record RecommendationSurveyAnswers(
	ClimatePreference climatePreference,
	SeasonTolerance seasonTolerance,
	PacePreference pacePreference,
	CostQualityPreference costQualityPreference,
	EnvironmentPreference environmentPreference,
	EnglishSupportNeed englishSupportNeed,
	ImportanceLevel safetyPriority,
	ImportanceLevel publicServicePriority,
	ImportanceLevel foodImportance,
	ImportanceLevel diversityImportance,
	SettlementPreference settlementPreference,
	MobilityPreference mobilityPreference
) {

	public interface SurveyOption {

		String label();

		String description();
	}

	public enum ClimatePreference implements SurveyOption {
		WARM("따뜻한 쪽이 편하다", "햇빛이 충분하고 추위 스트레스가 적은 기후가 더 편합니다.", 5),
		MILD("온화한 쪽이 편하다", "사계절은 느끼되 너무 덥거나 춥지 않은 기후를 선호합니다.", 3),
		COLD("서늘한 쪽이 편하다", "덥고 습한 날씨보다 차분하고 서늘한 기후가 더 편합니다.", 1);

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

	public enum SeasonTolerance implements SurveyOption {
		LOW("극단적인 더위·추위는 피하고 싶다", "기후가 너무 뜨겁거나 춥고, 계절 차이가 크면 적응이 어렵습니다.", 2),
		MEDIUM("어느 정도는 감수할 수 있다", "조금 불편해도 생활 장점이 분명하면 적응해볼 수 있습니다.", 4),
		HIGH("기후 적응은 비교적 자신 있다", "날씨보다 생활 조건이 더 중요해서 어느 정도의 기후 차이는 감수할 수 있습니다.", 5);

		private final String label;
		private final String description;
		private final int toleranceValue;

		SeasonTolerance(String label, String description, int toleranceValue) {
			this.label = label;
			this.description = description;
			this.toleranceValue = toleranceValue;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String description() {
			return description;
		}

		public int toleranceValue() {
			return toleranceValue;
		}
	}

	public enum PacePreference implements SurveyOption {
		FAST("빠르고 자극적인 도시 리듬", "교통, 일거리, 사람 흐름이 많은 역동적인 생활이 잘 맞습니다.", 5),
		BALANCED("바쁘지만 숨 돌릴 틈도 필요하다", "도시 편의는 중요하지만 매일 숨 가쁜 리듬까지 원하지는 않습니다.", 3),
		RELAXED("조용하고 여유 있는 리듬", "일상 속 속도보다 안정감과 여유가 더 중요합니다.", 1);

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

	public enum CostQualityPreference implements SurveyOption {
		VALUE_FIRST("생활비 압박은 낮아야 한다", "장점이 있더라도 생활비 부담이 크면 오래 버티기 어렵습니다.", 2),
		BALANCED("비용과 생활 품질을 함께 본다", "비싸더라도 장점이 분명하면 고려하지만, 생활비도 쉽게 넘기지 않습니다.", 3),
		QUALITY_FIRST("비싸더라도 생활 품질이 확실하면 감수한다", "인프라, 안전, 편의, 경험이 좋다면 높은 생활비도 투자할 수 있습니다.", 5);

		private final String label;
		private final String description;
		private final int targetPriceLevel;

		CostQualityPreference(String label, String description, int targetPriceLevel) {
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
		CITY("도심 접근성과 자극이 더 중요하다", "걸어서 닿는 편의와 도시의 밀도가 일상 만족도에 큰 영향을 줍니다.", 5),
		MIXED("도시 편의와 숨 쉴 공간이 둘 다 필요하다", "도시 생활을 하더라도 공원, 바다, 산 같은 여백이 함께 있었으면 합니다.", 3),
		NATURE("숨 쉴 공간과 자연 접근성이 더 중요하다", "생활 반경 가까이에 자연이 있는 것이 도시 자극보다 더 중요합니다.", 1);

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

	public enum EnglishSupportNeed implements SurveyOption {
		HIGH("영어만으로도 초반 생활이 가능해야 한다", "행정, 생활, 커뮤니티 적응에서 영어 지원이 충분히 느껴져야 안심됩니다.", 6),
		MEDIUM("있으면 훨씬 편하지만 절대 조건은 아니다", "영어 지원이 있으면 좋지만, 다른 장점이 더 크면 감수할 수 있습니다.", 3),
		LOW("영어 편의성보다 다른 조건이 더 중요하다", "초기 언어 장벽보다 물가, 환경, 생활 분위기 같은 요소를 더 우선합니다.", 0);

		private final String label;
		private final String description;
		private final int weight;

		EnglishSupportNeed(String label, String description, int weight) {
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

	public enum ImportanceLevel implements SurveyOption {
		HIGH("핵심 조건이다", "이 조건이 만족되지 않으면 다른 장점이 있어도 쉽게 흔들릴 수 있습니다.", 4),
		MEDIUM("분명 중요하지만 절대 기준은 아니다", "다른 장점과 함께 균형 있게 판단하고 싶은 조건입니다.", 2),
		LOW("있으면 좋지만 최우선은 아니다", "다른 조건이 더 좋다면 이 부분은 어느 정도 감수할 수 있습니다.", 0);

		private final String label;
		private final String description;
		private final int weight;

		ImportanceLevel(String label, String description, int weight) {
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

	public enum SettlementPreference implements SurveyOption {
		EXPERIENCE("먼저 살아보며 판단하고 싶다", "짧게 경험해보고, 맞으면 더 길게 가져가는 방식이 편합니다."),
		BALANCED("경험과 장기 정착 가능성을 함께 본다", "당장 살아보는 느낌과 나중의 정착 안정성을 동시에 고려합니다."),
		STABILITY("처음부터 장기 정착 가능성까지 본다", "안정감, 제도, 생활 기반처럼 오래 머무를 조건이 매우 중요합니다.");

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
		TRANSIT_FIRST("대중교통·도보 생활이 편해야 한다", "차 없이도 일상 이동이 가능해야 생활 만족도가 높습니다."),
		BALANCED("둘 다 크게 상관없다", "대중교통이든 차량이든 전체 생활 조건이 더 중요합니다."),
		SPACE_FIRST("넓은 공간과 느긋한 이동도 괜찮다", "촘촘한 교통망보다 공간감과 여유가 더 중요할 수 있습니다.");

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
