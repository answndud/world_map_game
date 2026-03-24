package com.worldmap.recommendation.domain;

public record RecommendationSurveyAnswers(
	ClimatePreference climatePreference,
	SeasonStylePreference seasonStylePreference,
	SeasonTolerance seasonTolerance,
	PacePreference pacePreference,
	CrowdPreference crowdPreference,
	CostQualityPreference costQualityPreference,
	HousingPreference housingPreference,
	EnvironmentPreference environmentPreference,
	MobilityPreference mobilityPreference,
	EnglishSupportNeed englishSupportNeed,
	NewcomerSupportNeed newcomerSupportNeed,
	ImportanceLevel safetyPriority,
	ImportanceLevel publicServicePriority,
	ImportanceLevel digitalConveniencePriority,
	ImportanceLevel foodImportance,
	ImportanceLevel diversityImportance,
	ImportanceLevel cultureLeisureImportance,
	WorkLifePreference workLifePreference,
	SettlementPreference settlementPreference,
	FutureBasePreference futureBasePreference
) {

	public interface SurveyOption {

		String label();

		String description();
	}

	public enum ClimatePreference implements SurveyOption {
		WARM("햇볕이 잘 드는 따뜻한 쪽이 편하다", "맑고 따뜻한 날씨에서 컨디션이 더 잘 유지되는 편입니다.", 5),
		MILD("너무 덥지도 춥지도 않은 온화한 쪽이 편하다", "사계절이 있어도 극단적이지 않은 날씨를 더 편하게 느낍니다.", 3),
		COLD("시원하고 선선한 쪽이 편하다", "강한 더위와 습기보다 차분하고 선선한 날씨가 몸에 더 잘 맞습니다.", 1);

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

	public enum SeasonStylePreference implements SurveyOption {
		STABLE("계절 차이가 크지 않은 편이 좋다", "연중 날씨 변화가 비교적 완만한 쪽이 생활 리듬을 만들기 편합니다.", 1),
		BALANCED("약간의 계절감은 느끼고 싶다", "계절 변화는 좋지만 생활 패턴이 크게 흔들릴 정도는 아니었으면 합니다.", 3),
		DISTINCT("사계절이 분명한 편이 좋다", "계절 변화가 뚜렷해야 생활에 리듬감이 생긴다고 느낍니다.", 5);

		private final String label;
		private final String description;
		private final int targetValue;

		SeasonStylePreference(String label, String description, int targetValue) {
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
		LOW("날씨 스트레스는 최대한 적었으면 좋다", "조금만 덥거나 추워도 컨디션이 크게 흔들릴 수 있어 기후 부담이 낮은 편이 좋습니다.", 2),
		MEDIUM("생활 장점이 크면 어느 정도 적응할 수 있다", "조금 불편해도 인프라나 분위기가 좋다면 감수할 여지가 있습니다.", 4),
		HIGH("날씨가 완벽하지 않아도 비교적 잘 적응한다", "기후보다 생활 조건과 기회가 더 중요해서 날씨 적응에는 자신이 있는 편입니다.", 5);

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
		FAST("빠르고 기회가 많은 리듬이 좋다", "교통, 일거리, 사람 흐름이 빠른 환경에서 오히려 에너지를 얻는 편입니다.", 5),
		BALANCED("활기도 좋지만 숨 돌릴 틈도 필요하다", "도시 편의는 좋지만 매일 숨가쁜 리듬만 지속되는 건 원하지 않습니다.", 3),
		RELAXED("조용하고 여유 있는 리듬이 좋다", "생활 템포보다 안정감과 회복 여지가 더 중요합니다.", 1);

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

	public enum CrowdPreference implements SurveyOption {
		LIVELY("사람 많고 활기 있는 동네가 좋다", "조금 붐벼도 상권과 자극이 많은 환경이 생활 만족도를 높여 줍니다.", 5),
		BALANCED("너무 붐비지만 않으면 괜찮다", "적당한 활기와 적당한 여유가 함께 있는 쪽이 편합니다.", 3),
		CALM("과밀한 환경은 쉽게 피로하다", "늘 붐비는 곳보다는 조금 덜 자극적이고 안정적인 동네가 더 잘 맞습니다.", 1);

		private final String label;
		private final String description;
		private final int targetValue;

		CrowdPreference(String label, String description, int targetValue) {
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
		VALUE_FIRST("생활비 압박은 낮아야 한다", "장점이 있더라도 생활비 부담이 크면 오래 버티기 어렵다고 느낍니다.", 2),
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

	public enum HousingPreference implements SurveyOption {
		CENTER_FIRST("집이 조금 작아도 중심 접근성이 더 중요하다", "생활 반경이 촘촘하고 이동 시간이 짧은 편이 주거 면적보다 더 중요합니다.", 1),
		BALANCED("집 크기와 접근성을 둘 다 본다", "너무 좁지도 멀지도 않은 중간 지점을 찾고 싶습니다.", 3),
		SPACE_FIRST("조금 멀어도 넓고 여유 있는 주거가 좋다", "도심 중심에서 조금 떨어져도 집의 여유와 생활 공간이 중요합니다.", 5);

		private final String label;
		private final String description;
		private final int targetSpaceValue;

		HousingPreference(String label, String description, int targetSpaceValue) {
			this.label = label;
			this.description = description;
			this.targetSpaceValue = targetSpaceValue;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String description() {
			return description;
		}

		public int targetSpaceValue() {
			return targetSpaceValue;
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

	public enum MobilityPreference implements SurveyOption {
		TRANSIT_FIRST("대중교통·도보 생활이 편해야 한다", "차 없이도 일상 이동이 가능해야 생활 만족도가 높습니다.", 5),
		BALANCED("대중교통과 차량 모두 크게 상관없다", "이동 방식보다 전체 생활 조건이 더 중요합니다.", 3),
		SPACE_FIRST("넓은 공간과 느긋한 이동도 괜찮다", "촘촘한 교통망보다 공간감과 이동 여유가 더 중요할 수 있습니다.", 1);

		private final String label;
		private final String description;
		private final int targetTransitValue;

		MobilityPreference(String label, String description, int targetTransitValue) {
			this.label = label;
			this.description = description;
			this.targetTransitValue = targetTransitValue;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String description() {
			return description;
		}

		public int targetTransitValue() {
			return targetTransitValue;
		}
	}

	public enum EnglishSupportNeed implements SurveyOption {
		HIGH("초기엔 영어만으로 생활이 가능해야 안심된다", "행정, 생활, 커뮤니티 적응에서 영어 지원이 충분히 느껴져야 안심됩니다.", 6),
		MEDIUM("영어 지원이 있으면 훨씬 편하지만 절대 조건은 아니다", "영어 지원이 있으면 좋지만, 다른 장점이 더 크면 감수할 수 있습니다.", 3),
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

	public enum NewcomerSupportNeed implements SurveyOption {
		HIGH("처음 가도 친절하게 적응할 수 있는 분위기가 중요하다", "처음 정착할 때 안내, 친절함, 진입 장벽의 낮음이 큰 안심 요소가 됩니다.", 6),
		MEDIUM("있으면 좋지만 절대 기준은 아니다", "적응을 도와주는 분위기가 있으면 좋지만, 다른 장점도 함께 고려합니다.", 3),
		LOW("초기 적응 난도보다 다른 장점이 더 중요하다", "낯선 환경 적응은 어느 정도 직접 해내도 괜찮다고 느낍니다.", 0);

		private final String label;
		private final String description;
		private final int weight;

		NewcomerSupportNeed(String label, String description, int weight) {
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

	public enum WorkLifePreference implements SurveyOption {
		DRIVE_FIRST("경쟁적이어도 기회가 많은 쪽이 좋다", "속도가 빠르고 경쟁이 있더라도 기회와 성장 가능성이 더 중요합니다.", 5),
		BALANCED("성장 기회와 생활 균형을 함께 본다", "너무 느린 곳도, 너무 소모적인 곳도 아닌 중간 지점을 찾습니다.", 3),
		LIFE_FIRST("조금 느려도 생활 균형이 더 중요하다", "기회가 아주 빠르지 않더라도 회복 가능한 생활 리듬을 더 원합니다.", 1);

		private final String label;
		private final String description;
		private final int targetIntensityValue;

		WorkLifePreference(String label, String description, int targetIntensityValue) {
			this.label = label;
			this.description = description;
			this.targetIntensityValue = targetIntensityValue;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String description() {
			return description;
		}

		public int targetIntensityValue() {
			return targetIntensityValue;
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

	public enum FutureBasePreference implements SurveyOption {
		LIGHT_START("지금 내 적응이 쉬운지가 더 중요하다", "먼저 나 혼자 편하게 적응할 수 있는지가 장기 기반보다 더 중요합니다.", 1),
		BALANCED("지금 편의와 장기 기반을 함께 본다", "당장 살기 편한지와 나중에 기반이 되는지를 같이 보고 싶습니다.", 3),
		STABLE_BASE("앞으로 기반이 탄탄한지가 더 중요하다", "지금 조금 불편해도 나중에 오래 머물 수 있는 기반을 더 높게 봅니다.", 5);

		private final String label;
		private final String description;
		private final int targetValue;

		FutureBasePreference(String label, String description, int targetValue) {
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
}
