import { recordDemoLiteRecommendationResult } from "../lib/browser-history.js";

export const DEMO_LITE_RECOMMENDATION_SURVEY_VERSION = "survey-v4";
export const DEMO_LITE_RECOMMENDATION_ENGINE_VERSION = "engine-v20";

const CLIMATE_WEIGHT = 4;
const SEASON_STYLE_WEIGHT = 3;
const WEATHER_ADAPTATION_WEIGHT = 4;
const PACE_WEIGHT = 4;
const CROWD_WEIGHT = 3;
const COST_QUALITY_WEIGHT = 5;
const HOUSING_WEIGHT = 4;
const ENVIRONMENT_WEIGHT = 4;
const MOBILITY_WEIGHT = 4;
const ENGLISH_SUPPORT_WEIGHT = 4;
const NEWCOMER_SUPPORT_WEIGHT = 4;
const SAFETY_WEIGHT = 5;
const PUBLIC_SERVICE_WEIGHT = 5;
const DIGITAL_WEIGHT = 4;
const FOOD_WEIGHT = 4;
const DIVERSITY_WEIGHT = 4;
const CULTURE_WEIGHT = 4;
const WORK_LIFE_WEIGHT = 3;
const SETTLEMENT_WEIGHT = 3;
const FUTURE_BASE_WEIGHT = 4;
const EXACT_MATCH_BONUS = 2;
const COHERENCE_BONUS = 8;
const EXPERIENCE_TRANSIT_BONUS = 12;
const CIVIC_BASE_BONUS = 10;
const PRACTICAL_SAFETY_BONUS = 8;
const SOFT_LANDING_BONUS = 8;
const FAMILY_BASE_BONUS = 22;
const GLOBAL_HUB_BONUS = 10;
const FOODIE_STARTER_BONUS = 24;
const FOODIE_STARTER_SUPPORT_BONUS = 6;
const TEMPERATE_PUBLIC_BASE_BONUS = 20;
const PRACTICAL_PUBLIC_VALUE_BONUS = 18;
const PREMIUM_WARM_HUB_BONUS = 8;
const SOFT_NATURE_BASE_BONUS = 12;
const COSMOPOLITAN_PULSE_BONUS = 16;
const TEMPERATE_GLOBAL_CITY_BONUS = 10;
const ACCESSIBLE_WARM_VALUE_HUB_BONUS = 18;
const TEMPERATE_FAMILY_BRIDGE_BONUS = 16;
const EXPLORATORY_NATURE_RUNWAY_BONUS = 20;
const VALUE_FIRST_COST_OVERSHOOT_PENALTY = 11;
const BALANCED_COST_OVERSHOOT_PENALTY = 7;
const QUALITY_FIRST_COST_OVERSHOOT_PENALTY = 4;
const CLIMATE_MISMATCH_PENALTY = 3;
const SEASON_STYLE_MISMATCH_PENALTY = 2;

const CONTINENT_LABELS = {
  ASIA: "아시아",
  EUROPE: "유럽",
  AFRICA: "아프리카",
  NORTH_AMERICA: "북아메리카",
  SOUTH_AMERICA: "남아메리카",
  OCEANIA: "오세아니아"
};

const CLIMATE_HIGHLIGHT_LABELS = {
  WARM: "따뜻한 기후",
  MILD: "온화한 기후",
  COLD: "선선한 기후"
};

const ENVIRONMENT_HIGHLIGHT_LABELS = {
  CITY: "도시 편의",
  MIXED: "도시·자연 균형",
  NATURE: "자연 접근성"
};

const COST_HIGHLIGHT_LABELS = {
  VALUE_FIRST: "비용 효율",
  BALANCED: "비용·품질 균형",
  QUALITY_FIRST: "생활 품질"
};

const PACE_HIGHLIGHT_LABELS = {
  FAST: "빠른 리듬",
  BALANCED: "균형 리듬",
  RELAXED: "느긋한 리듬"
};

const SETTLEMENT_HIGHLIGHT_LABELS = {
  EXPERIENCE: "가벼운 시작",
  BALANCED: "정착 균형",
  STABILITY: "장기 정착"
};

const WORK_LIFE_HIGHLIGHT_LABELS = {
  DRIVE_FIRST: "성장 기회",
  BALANCED: "성장·생활 균형",
  LIFE_FIRST: "생활 균형"
};

const QUESTIONS = [
  {
    id: "climatePreference",
    title: "오래 머물 때 몸이 가장 편한 온도감은 어느 쪽인가요?",
    helperText: "좋아하는 날씨보다, 몇 달 살아도 컨디션이 잘 유지될 기후를 떠올리면 더 정확합니다.",
    options: [
      { value: "WARM", label: "따뜻한 쪽", description: "햇볕이 잘 드는 따뜻한 쪽이 편하다", targetValue: 5 },
      { value: "MILD", label: "온화한 쪽", description: "너무 덥지도 춥지도 않은 온화한 쪽이 편하다", targetValue: 3 },
      { value: "COLD", label: "선선한 쪽", description: "시원하고 선선한 쪽이 편하다", targetValue: 1 }
    ]
  },
  {
    id: "seasonStylePreference",
    title: "계절 변화는 어느 정도 느껴지는 편이 좋나요?",
    helperText: "사계절이 분명해야 만족하는지, 기후 변화가 적은 쪽이 편한지 생각해보세요.",
    options: [
      { value: "STABLE", label: "계절 차이 적게", description: "계절 차이가 크지 않은 편이 좋다", targetValue: 1 },
      { value: "BALANCED", label: "약간의 계절감", description: "약간의 계절감은 느끼고 싶다", targetValue: 3 },
      { value: "DISTINCT", label: "사계절 분명하게", description: "사계절이 분명한 편이 좋다", targetValue: 5 }
    ]
  },
  {
    id: "seasonTolerance",
    title: "날씨 적응 난도는 어느 정도 감수할 수 있나요?",
    helperText: "날씨 자체보다, 좋은 점이 있으면 어느 정도까지 적응할 수 있는지 묻는 질문입니다.",
    options: [
      { value: "LOW", label: "날씨 스트레스는 적게", description: "날씨 스트레스는 최대한 적었으면 좋다", toleranceValue: 2 },
      { value: "MEDIUM", label: "장점이 크면 적응 가능", description: "생활 장점이 크면 어느 정도 적응할 수 있다", toleranceValue: 4 },
      { value: "HIGH", label: "날씨는 꽤 감수 가능", description: "날씨가 완벽하지 않아도 비교적 잘 적응한다", toleranceValue: 5 }
    ]
  },
  {
    id: "pacePreference",
    title: "하루 리듬은 어느 정도 속도가 맞나요?",
    helperText: "도시가 빠르게 돌아갈수록 에너지가 생기는지, 여유가 있어야 편한지 생각해보세요.",
    options: [
      { value: "FAST", label: "빠른 도시 리듬", description: "빠르고 기회가 많은 리듬이 좋다", targetValue: 5 },
      { value: "BALANCED", label: "균형 잡힌 속도", description: "활기도 좋지만 숨 돌릴 틈도 필요하다", targetValue: 3 },
      { value: "RELAXED", label: "느긋한 생활 리듬", description: "조용하고 여유 있는 리듬이 좋다", targetValue: 1 }
    ]
  },
  {
    id: "crowdPreference",
    title: "사람 많은 동네와 자극적인 분위기는 어느 정도 편한가요?",
    helperText: "활기와 과밀감은 다르니, 에너지가 나는 수준과 피로가 오는 수준을 구분해보세요.",
    options: [
      { value: "LIVELY", label: "붐비고 활기 있는 곳", description: "사람 많고 활기 있는 동네가 좋다", targetValue: 5 },
      { value: "BALANCED", label: "적당히 활기 있으면 충분", description: "너무 붐비지만 않으면 괜찮다", targetValue: 3 },
      { value: "CALM", label: "과밀한 환경은 피하고 싶음", description: "과밀한 환경은 쉽게 피로하다", targetValue: 1 }
    ]
  },
  {
    id: "costQualityPreference",
    title: "생활비와 생활 품질 중 어디에 더 무게를 두나요?",
    helperText: "비용이 낮아야 오래 버틸 수 있는지, 비용이 높아도 생활 품질이 확실하면 괜찮은지 생각해보세요.",
    options: [
      { value: "VALUE_FIRST", label: "생활비 압박은 낮아야 함", description: "생활비 압박은 낮아야 한다", targetPriceLevel: 2 },
      { value: "BALANCED", label: "비용과 품질 둘 다 봄", description: "비용과 생활 품질을 함께 본다", targetPriceLevel: 3 },
      { value: "QUALITY_FIRST", label: "품질이 확실하면 감수", description: "비싸더라도 생활 품질이 확실하면 감수한다", targetPriceLevel: 5 }
    ]
  },
  {
    id: "housingPreference",
    title: "집 크기와 중심 접근성 중 어느 쪽이 더 중요하나요?",
    helperText: "집이 조금 작아도 도심 접근성이 좋은 게 나은지, 공간 여유가 더 중요한지 고르세요.",
    options: [
      { value: "CENTER_FIRST", label: "중심 접근성 우선", description: "집이 조금 작아도 중심 접근성이 더 중요하다", targetSpaceValue: 1 },
      { value: "BALANCED", label: "둘 다 함께 봄", description: "집 크기와 접근성을 둘 다 본다", targetSpaceValue: 3 },
      { value: "SPACE_FIRST", label: "넓고 여유 있는 집 우선", description: "조금 멀어도 넓고 여유 있는 주거가 좋다", targetSpaceValue: 5 }
    ]
  },
  {
    id: "environmentPreference",
    title: "도시 편의와 자연 접근성 중 어디에 더 무게를 두나요?",
    helperText: "도심의 자극과 편의가 중요한지, 숨 쉴 수 있는 자연 접근성이 더 중요한지 고르세요.",
    options: [
      { value: "CITY", label: "도시 편의 우선", description: "도심 접근성과 자극이 더 중요하다", targetUrbanity: 5 },
      { value: "MIXED", label: "둘 다 균형 있게", description: "도시 편의와 숨 쉴 공간이 둘 다 필요하다", targetUrbanity: 3 },
      { value: "NATURE", label: "자연 접근성 우선", description: "숨 쉴 공간과 자연 접근성이 더 중요하다", targetUrbanity: 1 }
    ]
  },
  {
    id: "mobilityPreference",
    title: "생활 동선은 어떤 방식이 편한가요?",
    helperText: "대중교통, 도보 생활이 핵심인지, 차를 써도 괜찮은지 생각해보세요.",
    options: [
      { value: "TRANSIT_FIRST", label: "대중교통·도보 중심", description: "대중교통·도보 생활이 편해야 한다", targetTransitValue: 5 },
      { value: "BALANCED", label: "둘 다 크게 상관없음", description: "대중교통과 차량 모두 크게 상관없다", targetTransitValue: 3 },
      { value: "SPACE_FIRST", label: "넓은 공간과 느긋한 이동도 괜찮음", description: "넓은 공간과 느긋한 이동도 괜찮다", targetTransitValue: 1 }
    ]
  },
  {
    id: "englishSupportNeed",
    title: "초기 적응 단계에서 영어 지원은 어느 정도 필요하신가요?",
    helperText: "행정, 생활, 커뮤니티 적응을 영어만으로도 어느 정도 해결할 수 있어야 안심되는지 생각해보세요.",
    options: [
      { value: "HIGH", label: "높을수록 좋음", description: "초기엔 영어만으로 생활이 가능해야 안심된다", weight: 6 },
      { value: "MEDIUM", label: "어느 정도는 필요", description: "영어 지원이 있으면 훨씬 편하지만 절대 조건은 아니다", weight: 3 },
      { value: "LOW", label: "영어보다 다른 조건이 더 중요", description: "영어 편의성보다 다른 조건이 더 중요하다", weight: 0 }
    ]
  },
  {
    id: "newcomerSupportNeed",
    title: "처음 정착할 때 친절한 적응 분위기는 얼마나 중요하나요?",
    helperText: "행정, 커뮤니티, 생활 가이드가 친절해야 안심되는지 생각해보세요.",
    options: [
      { value: "HIGH", label: "처음 적응 친화도가 핵심", description: "처음 가도 친절하게 적응할 수 있는 분위기가 중요하다", weight: 6 },
      { value: "MEDIUM", label: "있으면 훨씬 편함", description: "있으면 좋지만 절대 기준은 아니다", weight: 3 },
      { value: "LOW", label: "적응 난도보다 다른 장점 우선", description: "초기 적응 난도보다 다른 장점이 더 중요하다", weight: 0 }
    ]
  },
  {
    id: "safetyPriority",
    title: "치안과 생활 안전은 어느 정도 핵심 조건인가요?",
    helperText: "다른 장점이 있어도 안전 체감이 낮으면 오래 머물기 어렵다고 느끼는지 생각해보세요.",
    options: [
      { value: "HIGH", label: "핵심 조건", description: "핵심 조건이다", weight: 4 },
      { value: "MEDIUM", label: "분명 중요함", description: "분명 중요하지만 절대 기준은 아니다", weight: 2 },
      { value: "LOW", label: "있으면 좋음", description: "있으면 좋지만 최우선은 아니다", weight: 0 }
    ]
  },
  {
    id: "publicServicePriority",
    title: "의료·행정·복지 같은 공공 서비스는 얼마나 중요하나요?",
    helperText: "생활 편의보다 한 단계 더 넓은 기반으로서 공공 서비스가 얼마나 중요한지 고르세요.",
    options: [
      { value: "HIGH", label: "핵심 조건", description: "핵심 조건이다", weight: 4 },
      { value: "MEDIUM", label: "분명 중요함", description: "분명 중요하지만 절대 기준은 아니다", weight: 2 },
      { value: "LOW", label: "있으면 좋음", description: "있으면 좋지만 최우선은 아니다", weight: 0 }
    ]
  },
  {
    id: "digitalConveniencePriority",
    title: "디지털 생활 편의는 얼마나 중요하나요?",
    helperText: "결제, 행정, 배달, 온라인 예약처럼 매일 체감하는 디지털 인프라를 떠올리면 됩니다.",
    options: [
      { value: "HIGH", label: "핵심 조건", description: "핵심 조건이다", weight: 4 },
      { value: "MEDIUM", label: "분명 중요함", description: "분명 중요하지만 절대 기준은 아니다", weight: 2 },
      { value: "LOW", label: "있으면 좋음", description: "있으면 좋지만 최우선은 아니다", weight: 0 }
    ]
  },
  {
    id: "foodImportance",
    title: "음식 만족도와 외식 선택지는 얼마나 중요하나요?",
    helperText: "매일 먹는 만족도와 음식 다양성이 생활 전체 만족도에 큰 영향을 주는지 생각해보세요.",
    options: [
      { value: "HIGH", label: "핵심 조건", description: "핵심 조건이다", weight: 4 },
      { value: "MEDIUM", label: "분명 중요함", description: "분명 중요하지만 절대 기준은 아니다", weight: 2 },
      { value: "LOW", label: "있으면 좋음", description: "있으면 좋지만 최우선은 아니다", weight: 0 }
    ]
  },
  {
    id: "diversityImportance",
    title: "다양한 배경의 사람들과 섞여 사는 환경은 얼마나 중요하나요?",
    helperText: "국적, 문화, 직업적 배경이 다양한 환경이 더 편한지 생각해보세요.",
    options: [
      { value: "HIGH", label: "핵심 조건", description: "핵심 조건이다", weight: 4 },
      { value: "MEDIUM", label: "분명 중요함", description: "분명 중요하지만 절대 기준은 아니다", weight: 2 },
      { value: "LOW", label: "있으면 좋음", description: "있으면 좋지만 최우선은 아니다", weight: 0 }
    ]
  },
  {
    id: "cultureLeisureImportance",
    title: "문화·여가 선택지 밀도는 얼마나 중요하나요?",
    helperText: "전시, 공연, 스포츠, 주말에 즐길 수 있는 선택지가 생활 만족도에 얼마나 중요한지 보세요.",
    options: [
      { value: "HIGH", label: "핵심 조건", description: "핵심 조건이다", weight: 4 },
      { value: "MEDIUM", label: "분명 중요함", description: "분명 중요하지만 절대 기준은 아니다", weight: 2 },
      { value: "LOW", label: "있으면 좋음", description: "있으면 좋지만 최우선은 아니다", weight: 0 }
    ]
  },
  {
    id: "workLifePreference",
    title: "성장 기회와 생활 균형 중 어느 쪽에 더 무게를 두나요?",
    helperText: "경쟁적인 환경이더라도 기회가 많으면 좋은지, 조금 느려도 생활 균형이 중요한지 고르세요.",
    options: [
      { value: "DRIVE_FIRST", label: "기회와 속도가 더 중요", description: "경쟁적이어도 기회가 많은 쪽이 좋다", targetIntensityValue: 5 },
      { value: "BALANCED", label: "둘 다 함께 봄", description: "성장 기회와 생활 균형을 함께 본다", targetIntensityValue: 3 },
      { value: "LIFE_FIRST", label: "생활 균형이 더 중요", description: "조금 느려도 생활 균형이 더 중요하다", targetIntensityValue: 1 }
    ]
  },
  {
    id: "settlementPreference",
    title: "이번 선택은 어느 정도 무게로 보고 있나요?",
    helperText: "가볍게 살아보는지, 장기 정착 가능성까지 같이 보는지에 따라 추천 축이 달라집니다.",
    options: [
      { value: "EXPERIENCE", label: "먼저 살아보며 판단", description: "먼저 살아보며 판단하고 싶다" },
      { value: "BALANCED", label: "경험과 장기 정착 둘 다", description: "경험과 장기 정착 가능성을 함께 본다" },
      { value: "STABILITY", label: "처음부터 장기 기반까지", description: "처음부터 장기 정착 가능성까지 본다" }
    ]
  },
  {
    id: "futureBasePreference",
    title: "당장 편한 시작과 장기 기반 중 어디에 더 무게를 두나요?",
    helperText: "초기 적응이 쉬운지가 더 중요한지, 장기적으로 기반이 탄탄한지가 더 중요한지 생각해보세요.",
    options: [
      { value: "LIGHT_START", label: "지금 적응이 더 중요", description: "지금 내 적응이 쉬운지가 더 중요하다", targetValue: 1 },
      { value: "BALANCED", label: "둘 다 함께 본다", description: "지금 편의와 장기 기반을 함께 본다", targetValue: 3 },
      { value: "STABLE_BASE", label: "앞으로 기반이 더 중요", description: "앞으로 기반이 탄탄한지가 더 중요하다", targetValue: 5 }
    ]
  }
];

const QUESTION_BY_ID = new Map(QUESTIONS.map((question) => [question.id, question]));

const QUESTION_SECTIONS = [
  {
    title: "기후와 생활 리듬",
    helperText: "몸이 편한 날씨와 하루 속도감을 먼저 맞추는 구간입니다.",
    questionIds: ["climatePreference", "seasonStylePreference", "seasonTolerance", "pacePreference"]
  },
  {
    title: "생활비와 주거 조건",
    helperText: "물가, 공간감, 도시-자연 균형, 이동 방식처럼 매일 부딪히는 조건을 묻습니다.",
    questionIds: ["crowdPreference", "costQualityPreference", "housingPreference", "environmentPreference", "mobilityPreference"]
  },
  {
    title: "초기 적응과 안전",
    helperText: "영어 지원, newcomer 친화도, 안전, 공공 서비스 같은 soft-landing 조건을 확인합니다.",
    questionIds: ["englishSupportNeed", "newcomerSupportNeed", "safetyPriority", "publicServicePriority", "digitalConveniencePriority"]
  },
  {
    title: "음식과 문화 취향",
    helperText: "도시의 맛과 다양성, 문화적 자극이 실제 만족도에 얼마나 중요한지 묻습니다.",
    questionIds: ["foodImportance", "diversityImportance", "cultureLeisureImportance"]
  },
  {
    title: "장기 정착 방향",
    helperText: "성장과 생활 균형, 체험인지 정착인지 같은 큰 방향을 마지막에 정리합니다.",
    questionIds: ["workLifePreference", "settlementPreference", "futureBasePreference"]
  }
];

const COUNTRY_PROFILES = [
  { iso3Code: "CAN", climateValue: 2, seasonality: 5, paceValue: 2, priceLevel: 4, urbanityValue: 3, englishSupport: 5, safety: 5, welfare: 5, food: 3, diversity: 5, housingSpace: 4, digitalConvenience: 5, cultureScene: 3, newcomerFriendliness: 4, hookLine: "영어 친화도와 복지, 자연 접근성이 균형 잡힌 선택지입니다." },
  { iso3Code: "USA", climateValue: 3, seasonality: 4, paceValue: 5, priceLevel: 4, urbanityValue: 5, englishSupport: 5, safety: 4, welfare: 3, food: 4, diversity: 5, housingSpace: 4, digitalConvenience: 5, cultureScene: 5, newcomerFriendliness: 4, hookLine: "속도감 있는 대도시 생활과 문화 다양성을 강하게 원하는 경우 선택지가 됩니다." },
  { iso3Code: "AUS", climateValue: 4, seasonality: 3, paceValue: 3, priceLevel: 4, urbanityValue: 3, englishSupport: 5, safety: 4, welfare: 4, food: 4, diversity: 4, housingSpace: 4, digitalConvenience: 5, cultureScene: 4, newcomerFriendliness: 4, hookLine: "따뜻한 기후와 영어 환경, 활동적인 라이프스타일이 강점입니다." },
  { iso3Code: "NZL", climateValue: 2, seasonality: 4, paceValue: 1, priceLevel: 4, urbanityValue: 2, englishSupport: 5, safety: 5, welfare: 4, food: 3, diversity: 3, housingSpace: 5, digitalConvenience: 4, cultureScene: 2, newcomerFriendliness: 4, hookLine: "자연 중심의 여유 있는 생활과 영어 환경을 동시에 원할 때 잘 맞습니다." },
  { iso3Code: "SGP", climateValue: 5, seasonality: 1, paceValue: 5, priceLevel: 5, urbanityValue: 5, englishSupport: 5, safety: 5, welfare: 4, food: 5, diversity: 5, housingSpace: 1, digitalConvenience: 5, cultureScene: 5, newcomerFriendliness: 4, hookLine: "도시 속도, 영어, 안전, 음식 만족도를 한 번에 가져가는 초도시형 프로필입니다." },
  { iso3Code: "JPN", climateValue: 3, seasonality: 4, paceValue: 4, priceLevel: 4, urbanityValue: 4, englishSupport: 2, safety: 5, welfare: 4, food: 5, diversity: 3, housingSpace: 2, digitalConvenience: 5, cultureScene: 5, newcomerFriendliness: 2, hookLine: "안전성과 도시 편의, 음식 만족도가 높은 동아시아형 프로필입니다." },
  { iso3Code: "GBR", climateValue: 2, seasonality: 4, paceValue: 4, priceLevel: 4, urbanityValue: 5, englishSupport: 5, safety: 4, welfare: 4, food: 4, diversity: 5, housingSpace: 2, digitalConvenience: 5, cultureScene: 5, newcomerFriendliness: 4, hookLine: "영어권 대도시 접근성과 문화 다양성, 글로벌 연결성이 강한 프로필입니다." },
  { iso3Code: "IRL", climateValue: 2, seasonality: 4, paceValue: 3, priceLevel: 4, urbanityValue: 3, englishSupport: 5, safety: 5, welfare: 4, food: 3, diversity: 4, housingSpace: 3, digitalConvenience: 4, cultureScene: 3, newcomerFriendliness: 4, hookLine: "영어 적응 난도가 낮고 차분한 도시 생활을 기대할 때 무난한 선택지입니다." },
  { iso3Code: "DEU", climateValue: 2, seasonality: 4, paceValue: 3, priceLevel: 4, urbanityValue: 4, englishSupport: 4, safety: 4, welfare: 5, food: 3, diversity: 4, housingSpace: 3, digitalConvenience: 4, cultureScene: 4, newcomerFriendliness: 3, hookLine: "복지와 산업 기반, 균형 잡힌 도시 생활을 원할 때 강한 선택지입니다." },
  { iso3Code: "FRA", climateValue: 3, seasonality: 4, paceValue: 3, priceLevel: 4, urbanityValue: 4, englishSupport: 3, safety: 4, welfare: 4, food: 5, diversity: 4, housingSpace: 2, digitalConvenience: 4, cultureScene: 5, newcomerFriendliness: 3, hookLine: "문화 자산과 음식, 도시 생활의 균형을 중시할 때 매력이 있습니다." },
  { iso3Code: "ITA", climateValue: 4, seasonality: 3, paceValue: 2, priceLevel: 3, urbanityValue: 3, englishSupport: 2, safety: 4, welfare: 3, food: 5, diversity: 3, housingSpace: 3, digitalConvenience: 3, cultureScene: 5, newcomerFriendliness: 3, hookLine: "음식 만족도와 따뜻한 분위기, 여유 있는 생활 리듬을 좋아할 때 잘 맞습니다." },
  { iso3Code: "SWE", climateValue: 1, seasonality: 5, paceValue: 2, priceLevel: 5, urbanityValue: 3, englishSupport: 5, safety: 5, welfare: 5, food: 2, diversity: 4, housingSpace: 4, digitalConvenience: 5, cultureScene: 3, newcomerFriendliness: 4, hookLine: "복지와 영어, 차분한 북유럽 라이프스타일이 핵심입니다." },
  { iso3Code: "DNK", climateValue: 1, seasonality: 5, paceValue: 2, priceLevel: 5, urbanityValue: 3, englishSupport: 5, safety: 5, welfare: 5, food: 3, diversity: 4, housingSpace: 4, digitalConvenience: 5, cultureScene: 3, newcomerFriendliness: 4, hookLine: "복지와 영어 접근성, 안정적인 생활 리듬이 강점인 북유럽형 선택지입니다." },
  { iso3Code: "NOR", climateValue: 1, seasonality: 5, paceValue: 2, priceLevel: 5, urbanityValue: 2, englishSupport: 5, safety: 5, welfare: 5, food: 3, diversity: 3, housingSpace: 5, digitalConvenience: 5, cultureScene: 2, newcomerFriendliness: 4, hookLine: "자연과 안전, 높은 생활 안정성을 중시할 때 강한 북유럽 후보입니다." },
  { iso3Code: "FIN", climateValue: 1, seasonality: 5, paceValue: 2, priceLevel: 4, urbanityValue: 2, englishSupport: 5, safety: 5, welfare: 5, food: 2, diversity: 3, housingSpace: 4, digitalConvenience: 5, cultureScene: 3, newcomerFriendliness: 4, hookLine: "조용한 생활 리듬과 안전, 복지 기반을 우선시할 때 잘 맞습니다." },
  { iso3Code: "ESP", climateValue: 4, seasonality: 3, paceValue: 2, priceLevel: 3, urbanityValue: 3, englishSupport: 3, safety: 4, welfare: 4, food: 5, diversity: 4, housingSpace: 3, digitalConvenience: 4, cultureScene: 5, newcomerFriendliness: 4, hookLine: "따뜻한 기후와 음식, 여유 있는 리듬을 함께 보는 사람에게 잘 맞습니다." },
  { iso3Code: "PRT", climateValue: 4, seasonality: 3, paceValue: 2, priceLevel: 3, urbanityValue: 2, englishSupport: 4, safety: 4, welfare: 4, food: 4, diversity: 3, housingSpace: 4, digitalConvenience: 4, cultureScene: 4, newcomerFriendliness: 4, hookLine: "온화한 기후와 비교적 부드러운 생활 속도를 좋아할 때 안정적인 선택지입니다." },
  { iso3Code: "CHE", climateValue: 2, seasonality: 4, paceValue: 3, priceLevel: 5, urbanityValue: 3, englishSupport: 4, safety: 5, welfare: 5, food: 3, diversity: 4, housingSpace: 4, digitalConvenience: 5, cultureScene: 4, newcomerFriendliness: 3, hookLine: "생활비는 높지만 안전과 복지, 안정성을 중시할 때 강한 선택지입니다." },
  { iso3Code: "AUT", climateValue: 2, seasonality: 4, paceValue: 2, priceLevel: 4, urbanityValue: 3, englishSupport: 4, safety: 5, welfare: 5, food: 3, diversity: 3, housingSpace: 4, digitalConvenience: 4, cultureScene: 4, newcomerFriendliness: 3, hookLine: "도시 편의와 차분한 생활 리듬, 높은 안전성을 함께 보고 싶을 때 어울립니다." },
  { iso3Code: "NLD", climateValue: 2, seasonality: 4, paceValue: 4, priceLevel: 4, urbanityValue: 4, englishSupport: 5, safety: 4, welfare: 4, food: 3, diversity: 5, housingSpace: 2, digitalConvenience: 5, cultureScene: 5, newcomerFriendliness: 4, hookLine: "영어 접근성과 도시 연결성, 문화 다양성이 강점인 서유럽형 프로필입니다." },
  { iso3Code: "KOR", climateValue: 3, seasonality: 4, paceValue: 5, priceLevel: 3, urbanityValue: 5, englishSupport: 2, safety: 4, welfare: 4, food: 5, diversity: 3, housingSpace: 2, digitalConvenience: 5, cultureScene: 5, newcomerFriendliness: 2, hookLine: "빠른 도시 리듬과 음식 만족도, 디지털 인프라를 중시할 때 매력이 큽니다." },
  { iso3Code: "ARE", climateValue: 5, seasonality: 1, paceValue: 5, priceLevel: 5, urbanityValue: 5, englishSupport: 5, safety: 4, welfare: 3, food: 4, diversity: 5, housingSpace: 2, digitalConvenience: 5, cultureScene: 4, newcomerFriendliness: 4, hookLine: "고속 도시 환경과 글로벌 비즈니스 감각, 영어 사용 환경이 강한 선택지입니다." },
  { iso3Code: "THA", climateValue: 5, seasonality: 2, paceValue: 3, priceLevel: 2, urbanityValue: 4, englishSupport: 3, safety: 3, welfare: 2, food: 5, diversity: 5, housingSpace: 4, digitalConvenience: 3, cultureScene: 4, newcomerFriendliness: 4, hookLine: "따뜻한 기후와 음식, 비교적 가벼운 생활비, 관광·문화 다양성이 강점입니다." },
  { iso3Code: "MYS", climateValue: 5, seasonality: 2, paceValue: 3, priceLevel: 2, urbanityValue: 4, englishSupport: 4, safety: 4, welfare: 3, food: 5, diversity: 5, housingSpace: 4, digitalConvenience: 4, cultureScene: 4, newcomerFriendliness: 4, hookLine: "영어 적응 난도와 물가, 다문화 도시 환경의 균형이 좋은 동남아형 프로필입니다." },
  { iso3Code: "VNM", climateValue: 5, seasonality: 2, paceValue: 4, priceLevel: 1, urbanityValue: 4, englishSupport: 2, safety: 3, welfare: 2, food: 5, diversity: 4, housingSpace: 3, digitalConvenience: 4, cultureScene: 4, newcomerFriendliness: 3, hookLine: "빠른 도시 에너지와 낮은 생활비, 음식 만족도를 함께 보고 싶을 때 눈에 띕니다." },
  { iso3Code: "CHL", climateValue: 3, seasonality: 4, paceValue: 3, priceLevel: 3, urbanityValue: 4, englishSupport: 3, safety: 4, welfare: 3, food: 4, diversity: 3, housingSpace: 4, digitalConvenience: 4, cultureScene: 3, newcomerFriendliness: 3, hookLine: "남미 안에서 비교적 안정적인 도시 생활과 기후 균형을 찾을 때 볼 수 있습니다." },
  { iso3Code: "URY", climateValue: 3, seasonality: 4, paceValue: 2, priceLevel: 3, urbanityValue: 3, englishSupport: 3, safety: 5, welfare: 4, food: 4, diversity: 2, housingSpace: 4, digitalConvenience: 4, cultureScene: 3, newcomerFriendliness: 3, hookLine: "느긋한 생활 리듬과 남미권 안정성, 복지 지향성을 중시할 때 맞습니다." },
  { iso3Code: "BRA", climateValue: 5, seasonality: 2, paceValue: 4, priceLevel: 2, urbanityValue: 5, englishSupport: 2, safety: 2, welfare: 2, food: 5, diversity: 5, housingSpace: 4, digitalConvenience: 3, cultureScene: 5, newcomerFriendliness: 3, hookLine: "뜨거운 기후와 강한 도시 에너지, 음식과 문화 다양성을 동시에 원하는 경우 후보가 됩니다." },
  { iso3Code: "MEX", climateValue: 4, seasonality: 3, paceValue: 4, priceLevel: 2, urbanityValue: 5, englishSupport: 2, safety: 2, welfare: 2, food: 5, diversity: 5, housingSpace: 3, digitalConvenience: 3, cultureScene: 5, newcomerFriendliness: 3, hookLine: "활기 있는 대도시와 풍부한 음식 문화, 비교적 유연한 생활비 감각이 강점입니다." },
  { iso3Code: "ZAF", climateValue: 4, seasonality: 3, paceValue: 4, priceLevel: 2, urbanityValue: 4, englishSupport: 4, safety: 2, welfare: 2, food: 4, diversity: 5, housingSpace: 4, digitalConvenience: 4, cultureScene: 4, newcomerFriendliness: 3, hookLine: "영어 환경과 자연 접근성, 문화적 다양성을 함께 보고 싶을 때 고려할 수 있습니다." }
];

export const DEMO_LITE_RECOMMENDATION_QUESTION_COUNT = QUESTIONS.length;
export const DEMO_LITE_RECOMMENDATION_PROFILE_COUNT = COUNTRY_PROFILES.length;

function getQuestion(questionId) {
  const question = QUESTION_BY_ID.get(questionId);
  if (!question) {
    throw new Error(`Unknown recommendation question: ${questionId}`);
  }
  return question;
}

function getSelectedOption(questionId, selectedValue) {
  const question = getQuestion(questionId);
  const option = question.options.find((candidate) => candidate.value === selectedValue);
  if (!option) {
    throw new Error(`Invalid answer value for ${questionId}: ${selectedValue}`);
  }
  return option;
}

function normalizeAnswers(rawAnswers) {
  return Object.fromEntries(
    QUESTIONS.map((question) => [question.id, getSelectedOption(question.id, rawAnswers[question.id])])
  );
}

function distance(left, right) {
  return Math.abs(left - right);
}

function closenessScore(distanceValue, weight) {
  switch (distanceValue) {
    case 0:
      return 5 * weight;
    case 1:
      return 3 * weight;
    case 2:
      return weight;
    default:
      return 0;
  }
}

function exactMatchBonus(distanceValue) {
  return distanceValue === 0 ? EXACT_MATCH_BONUS : 0;
}

function mismatchPenalty(distanceValue, penaltyWeight) {
  switch (distanceValue) {
    case 0:
    case 1:
      return 0;
    case 2:
      return -penaltyWeight;
    default:
      return -penaltyWeight * 2;
  }
}

function normalizedAverage(...values) {
  const total = values.reduce((sum, value) => sum + value, 0);
  return Math.round(total / values.length);
}

function weatherDemand(profile) {
  const climateSwing = Math.abs(profile.climateValue - 3);
  const seasonSwing = Math.abs(profile.seasonality - 3);
  return Math.min(5, 1 + (Math.max(climateSwing, seasonSwing) * 2));
}

function crowdEnergy(profile) {
  return normalizedAverage(profile.paceValue, profile.urbanityValue);
}

function transitSupport(profile) {
  return normalizedAverage(profile.urbanityValue, profile.digitalConvenience, profile.paceValue);
}

function newcomerSupport(profile) {
  return normalizedAverage(profile.englishSupport, profile.newcomerFriendliness);
}

function workIntensity(profile) {
  return normalizedAverage(profile.paceValue, profile.urbanityValue, profile.digitalConvenience);
}

function futureBase(profile) {
  return normalizedAverage(profile.safety, profile.welfare, profile.housingSpace);
}

function costOvershootPenalty(answer) {
  switch (answer.value) {
    case "VALUE_FIRST":
      return VALUE_FIRST_COST_OVERSHOOT_PENALTY;
    case "BALANCED":
      return BALANCED_COST_OVERSHOOT_PENALTY;
    case "QUALITY_FIRST":
      return QUALITY_FIRST_COST_OVERSHOOT_PENALTY;
    default:
      return 0;
  }
}

function costQualityPoints(answer, actualPriceLevel, distanceValue) {
  let score;
  switch (distanceValue) {
    case 0:
      score = (6 * COST_QUALITY_WEIGHT) + EXACT_MATCH_BONUS;
      break;
    case 1:
      score = 3 * COST_QUALITY_WEIGHT;
      break;
    case 2:
      score = COST_QUALITY_WEIGHT;
      break;
    default:
      score = 0;
  }

  if (actualPriceLevel > answer.targetPriceLevel) {
    score -= (actualPriceLevel - answer.targetPriceLevel) * costOvershootPenalty(answer);
  }

  return score;
}

function supportPoints(supportScore, answer, weight) {
  switch (answer.value) {
    case "HIGH":
      return supportScore * (weight + 1);
    case "MEDIUM":
      return supportScore * weight;
    case "LOW":
      return supportScore;
    default:
      return supportScore;
  }
}

function priorityPoints(attributeScore, answer, weight) {
  if (answer.value === "LOW") {
    return attributeScore;
  }
  return attributeScore * weight + answer.weight;
}

function coherenceBonus(climateDistance, costDistance, environmentDistance, housingDistance) {
  return climateDistance <= 1 && costDistance <= 1 && environmentDistance <= 1 && housingDistance <= 1
    ? COHERENCE_BONUS
    : 0;
}

function exactMatchCount(...distances) {
  return distances.filter((distanceValue) => distanceValue === 0).length;
}

function uniqueLabels(labels) {
  return labels.filter((label, index, all) => Boolean(label) && all.indexOf(label) === index);
}

function hasFinalConsonant(word) {
  const lastChar = word?.trim()?.slice(-1);
  if (!lastChar) {
    return false;
  }

  const codePoint = lastChar.charCodeAt(0);
  if (codePoint < 0xac00 || codePoint > 0xd7a3) {
    return false;
  }

  return (codePoint - 0xac00) % 28 !== 0;
}

function subjectParticle(word) {
  return hasFinalConsonant(word) ? "이" : "가";
}

function buildPreferenceHighlights(answers) {
  const candidateLabels = [
    CLIMATE_HIGHLIGHT_LABELS[answers.climatePreference.value],
    ENVIRONMENT_HIGHLIGHT_LABELS[answers.environmentPreference.value],
    answers.costQualityPreference.value !== "BALANCED"
      ? COST_HIGHLIGHT_LABELS[answers.costQualityPreference.value]
      : null,
    answers.pacePreference.value !== "BALANCED" ? PACE_HIGHLIGHT_LABELS[answers.pacePreference.value] : null,
    answers.englishSupportNeed.value === "HIGH" ? "영어 적응" : null,
    answers.safetyPriority.value === "HIGH" ? "안전" : null,
    answers.publicServicePriority.value === "HIGH" ? "공공 서비스" : null,
    answers.foodImportance.value === "HIGH" ? "음식 만족도" : null,
    answers.mobilityPreference.value === "TRANSIT_FIRST" ? "대중교통 중심" : null,
    answers.workLifePreference.value !== "BALANCED"
      ? WORK_LIFE_HIGHLIGHT_LABELS[answers.workLifePreference.value]
      : null,
    answers.settlementPreference.value !== "BALANCED"
      ? SETTLEMENT_HIGHLIGHT_LABELS[answers.settlementPreference.value]
      : null
  ];

  const fallbackLabels = [
    COST_HIGHLIGHT_LABELS[answers.costQualityPreference.value],
    PACE_HIGHLIGHT_LABELS[answers.pacePreference.value],
    WORK_LIFE_HIGHLIGHT_LABELS[answers.workLifePreference.value],
    SETTLEMENT_HIGHLIGHT_LABELS[answers.settlementPreference.value]
  ];

  return uniqueLabels([...candidateLabels, ...fallbackLabels]).slice(0, 4);
}

function buildRecommendationSummary(answers, recommendations) {
  const highlightLabels = buildPreferenceHighlights(answers);
  const [topRecommendation, secondRecommendation, thirdRecommendation] = recommendations;
  const headline = `${highlightLabels.slice(0, 3).join(" · ")} 기준`;
  const primaryReason = topRecommendation?.reasons?.[0] ?? topRecommendation?.hookLine ?? "";
  const comparisonTrail = [secondRecommendation, thirdRecommendation]
    .filter(Boolean)
    .map((candidate) => candidate.countryNameKr)
    .join(", ");
  const narrative = topRecommendation
    ? `${highlightLabels.slice(0, 3).join(", ")}을 중요하게 본 답변이라 ${topRecommendation.countryNameKr}${subjectParticle(topRecommendation.countryNameKr)} 1위로 나왔습니다. ${primaryReason}${comparisonTrail ? ` 이어서 ${comparisonTrail}도 함께 비교할 만합니다.` : ""}`
    : "아직 추천 결과가 없습니다.";
  const shareText = topRecommendation
    ? `WorldMap demo-lite 추천 결과: 1위 ${topRecommendation.countryNameKr}${secondRecommendation ? `, 2위 ${secondRecommendation.countryNameKr}` : ""}${thirdRecommendation ? `, 3위 ${thirdRecommendation.countryNameKr}` : ""}. 내 기준은 ${highlightLabels.join(", ")}.`
    : "WorldMap demo-lite 추천 결과를 아직 계산하지 않았습니다.";

  return {
    headline,
    highlightLabels,
    narrative,
    shareText
  };
}

function describeClimateTone(profile) {
  if (profile.climateValue >= 4) {
    return "따뜻한 편";
  }
  if (profile.climateValue <= 2) {
    return "선선한 편";
  }
  return "온화한 편";
}

function describeEnvironmentTone(profile) {
  if (profile.urbanityValue >= 4) {
    return "도시 쪽";
  }
  if (profile.urbanityValue <= 2) {
    return "자연 쪽";
  }
  return "균형형";
}

function describeCostTone(profile) {
  if (profile.priceLevel <= 2) {
    return "부담 낮음";
  }
  if (profile.priceLevel === 3) {
    return "중간";
  }
  if (profile.priceLevel === 4) {
    return "높은 편";
  }
  return "매우 높음";
}

function describeStartEase(profile) {
  const easeScore = normalizedAverage(profile.englishSupport, profile.newcomerFriendliness, profile.digitalConvenience);
  if (easeScore >= 4) {
    return "초기 적응 쉬움";
  }
  if (easeScore >= 3) {
    return "보통";
  }
  return "준비 필요";
}

function describeSettlementBase(profile) {
  const baseScore = futureBase(profile);
  if (baseScore >= 4) {
    return "기반 강함";
  }
  if (baseScore >= 3) {
    return "균형형";
  }
  return "가벼운 편";
}

function buildComparisonRows(answers, recommendations) {
  const rowSpecs = [
    {
      title: "기후 감각",
      selectedLabel: answers.climatePreference.label,
      describeValue: describeClimateTone
    },
    {
      title: "생활 환경",
      selectedLabel: answers.environmentPreference.label,
      describeValue: describeEnvironmentTone
    },
    {
      title: "생활비 감각",
      selectedLabel: answers.costQualityPreference.label,
      describeValue: describeCostTone
    },
    {
      title: "초기 적응",
      selectedLabel: `영어 ${answers.englishSupportNeed.label} · 적응 ${answers.newcomerSupportNeed.label}`,
      describeValue: describeStartEase
    },
    {
      title: "정착 기반",
      selectedLabel: `안전 ${answers.safetyPriority.label} · 기반 ${answers.futureBasePreference.label}`,
      describeValue: describeSettlementBase
    }
  ];

  return rowSpecs.map((rowSpec) => ({
    title: rowSpec.title,
    selectedLabel: rowSpec.selectedLabel,
    candidates: recommendations.map((candidate) => ({
      rank: candidate.rank,
      countryNameKr: candidate.countryNameKr,
      value: rowSpec.describeValue(candidate)
    }))
  }));
}

function settlementPoints(profile, answer) {
  switch (answer.value) {
    case "EXPERIENCE":
      return newcomerSupport(profile) + profile.cultureScene + profile.food;
    case "BALANCED":
      return profile.safety + profile.welfare + profile.diversity;
    case "STABILITY":
      return (futureBase(profile) * SETTLEMENT_WEIGHT) + profile.newcomerFriendliness;
    default:
      return 0;
  }
}

function experienceTransitBonus(profile, answers) {
  const isMatch = answers.settlementPreference.value === "EXPERIENCE"
    && answers.mobilityPreference.value === "TRANSIT_FIRST"
    && answers.costQualityPreference.value === "VALUE_FIRST";

  if (!isMatch) {
    return 0;
  }

  const transitScore = transitSupport(profile);
  const newcomerScore = newcomerSupport(profile);
  const digitalScore = profile.digitalConvenience;
  const safetyScore = profile.safety;
  const welfareScore = profile.welfare;

  if (transitScore >= 4 && newcomerScore >= 4 && digitalScore >= 4 && safetyScore >= 3 && welfareScore >= 3) {
    return EXPERIENCE_TRANSIT_BONUS;
  }
  if (transitScore >= 3 && newcomerScore >= 4 && digitalScore >= 3 && safetyScore >= 3) {
    return EXPERIENCE_TRANSIT_BONUS / 2;
  }
  return 0;
}

function civicBaseBonus(profile, answers) {
  const wantsBalancedCivicLife = answers.environmentPreference.value === "MIXED"
    && answers.pacePreference.value === "BALANCED"
    && (answers.publicServicePriority.value === "HIGH" || answers.safetyPriority.value === "HIGH");

  if (!wantsBalancedCivicLife) {
    return 0;
  }

  if (answers.costQualityPreference.value === "VALUE_FIRST" && profile.priceLevel >= 4) {
    return 0;
  }

  const civicBaseScore = normalizedAverage(
    profile.safety,
    profile.welfare,
    profile.housingSpace,
    profile.newcomerFriendliness
  );

  if (civicBaseScore >= 4) {
    return CIVIC_BASE_BONUS;
  }
  if (civicBaseScore >= 3 && profile.safety >= 4 && profile.welfare >= 4) {
    return CIVIC_BASE_BONUS / 2;
  }
  return 0;
}

function practicalSafetyBonus(profile, answers) {
  const practicalSafetyFit = answers.costQualityPreference.value === "VALUE_FIRST"
    && answers.safetyPriority.value === "HIGH"
    && answers.environmentPreference.value === "MIXED"
    && answers.pacePreference.value === "BALANCED";

  if (!practicalSafetyFit || profile.priceLevel >= 4) {
    return 0;
  }

  const strongAdaptationBase = profile.englishSupport >= 4
    && profile.newcomerFriendliness >= 4
    && profile.housingSpace >= 4
    && profile.safety >= 4
    && profile.welfare >= 4;

  const acceptableAdaptationBase = profile.englishSupport >= 3
    && profile.newcomerFriendliness >= 3
    && profile.safety >= 4
    && profile.welfare >= 4;

  if (strongAdaptationBase) {
    return PRACTICAL_SAFETY_BONUS;
  }
  if (acceptableAdaptationBase) {
    return PRACTICAL_SAFETY_BONUS / 2;
  }
  return 0;
}

function softLandingBonus(profile, answers) {
  const needsSoftLanding = answers.costQualityPreference.value === "VALUE_FIRST"
    && answers.safetyPriority.value === "HIGH"
    && answers.environmentPreference.value === "MIXED"
    && answers.pacePreference.value === "BALANCED"
    && answers.englishSupportNeed.value === "MEDIUM";

  if (!needsSoftLanding || profile.priceLevel >= 4) {
    return 0;
  }

  const strongSoftLanding = profile.englishSupport >= 4
    && profile.newcomerFriendliness >= 4
    && profile.housingSpace >= 4
    && profile.safety >= 4
    && profile.welfare >= 4;

  return strongSoftLanding ? SOFT_LANDING_BONUS : 0;
}

function familyBaseBonus(profile, answers) {
  const familyBaseFit = answers.costQualityPreference.value === "QUALITY_FIRST"
    && answers.safetyPriority.value === "HIGH"
    && answers.englishSupportNeed.value === "HIGH"
    && answers.environmentPreference.value === "MIXED"
    && answers.pacePreference.value === "BALANCED"
    && answers.seasonTolerance.value === "LOW"
    && answers.settlementPreference.value === "BALANCED";

  if (!familyBaseFit) {
    return 0;
  }

  const strongFamilyBase = profile.englishSupport >= 5
    && profile.safety >= 5
    && profile.welfare >= 5
    && profile.housingSpace >= 4
    && profile.newcomerFriendliness >= 4;

  return strongFamilyBase ? FAMILY_BASE_BONUS : 0;
}

function temperateFamilyBridgeBonus(profile, answers) {
  const familyBridgeFit = answers.climatePreference.value === "MILD"
    && answers.seasonTolerance.value === "LOW"
    && answers.pacePreference.value === "BALANCED"
    && answers.costQualityPreference.value === "QUALITY_FIRST"
    && answers.environmentPreference.value === "MIXED"
    && answers.englishSupportNeed.value === "HIGH"
    && answers.newcomerSupportNeed.value === "HIGH"
    && answers.safetyPriority.value === "HIGH"
    && answers.publicServicePriority.value === "MEDIUM"
    && answers.digitalConveniencePriority.value === "MEDIUM"
    && answers.foodImportance.value === "LOW"
    && answers.diversityImportance.value === "LOW"
    && answers.cultureLeisureImportance.value === "MEDIUM"
    && answers.workLifePreference.value === "BALANCED"
    && answers.settlementPreference.value === "BALANCED"
    && answers.futureBasePreference.value === "BALANCED";

  if (!familyBridgeFit) {
    return 0;
  }

  const strongFamilyBridge = profile.climateValue >= 2
    && profile.climateValue <= 3
    && profile.seasonality >= 5
    && profile.englishSupport >= 5
    && profile.safety >= 5
    && profile.welfare >= 5
    && profile.housingSpace >= 4
    && profile.digitalConvenience >= 5
    && profile.diversity >= 4
    && profile.newcomerFriendliness >= 4;

  if (strongFamilyBridge) {
    return TEMPERATE_FAMILY_BRIDGE_BONUS;
  }

  const acceptableFamilyBridge = profile.climateValue >= 2
    && profile.climateValue <= 3
    && profile.englishSupport >= 5
    && profile.safety >= 5
    && profile.welfare >= 5
    && profile.housingSpace >= 4;

  return acceptableFamilyBridge ? TEMPERATE_FAMILY_BRIDGE_BONUS / 2 : 0;
}

function exploratoryNatureRunwayBonus(profile, answers) {
  const natureRunwayFit = answers.climatePreference.value === "MILD"
    && answers.seasonStylePreference.value === "BALANCED"
    && answers.seasonTolerance.value === "MEDIUM"
    && answers.pacePreference.value === "RELAXED"
    && answers.crowdPreference.value === "CALM"
    && answers.costQualityPreference.value === "VALUE_FIRST"
    && answers.housingPreference.value === "SPACE_FIRST"
    && answers.environmentPreference.value === "NATURE"
    && answers.mobilityPreference.value === "TRANSIT_FIRST"
    && answers.englishSupportNeed.value === "MEDIUM"
    && answers.newcomerSupportNeed.value === "LOW"
    && answers.safetyPriority.value === "HIGH"
    && answers.publicServicePriority.value === "MEDIUM"
    && answers.digitalConveniencePriority.value === "LOW"
    && answers.foodImportance.value === "LOW"
    && answers.diversityImportance.value === "LOW"
    && answers.cultureLeisureImportance.value === "LOW"
    && answers.workLifePreference.value === "LIFE_FIRST"
    && answers.settlementPreference.value === "EXPERIENCE"
    && answers.futureBasePreference.value === "LIGHT_START";

  if (!natureRunwayFit) {
    return 0;
  }

  const strongNatureRunway = profile.climateValue >= 2
    && profile.climateValue <= 3
    && profile.seasonality >= 4
    && profile.paceValue <= 1
    && profile.urbanityValue <= 3
    && profile.englishSupport >= 5
    && profile.safety >= 5
    && profile.housingSpace >= 5
    && profile.newcomerFriendliness >= 4
    && profile.priceLevel <= 4;
  if (strongNatureRunway) {
    return EXPLORATORY_NATURE_RUNWAY_BONUS;
  }

  const acceptableNatureRunway = profile.climateValue >= 2
    && profile.climateValue <= 3
    && profile.urbanityValue <= 3
    && profile.englishSupport >= 4
    && profile.safety >= 4
    && profile.housingSpace >= 4;
  return acceptableNatureRunway ? EXPLORATORY_NATURE_RUNWAY_BONUS / 2 : 0;
}

function globalHubBonus(profile, answers) {
  const wantsGlobalCityHub = answers.climatePreference.value === "WARM"
    && answers.pacePreference.value === "FAST"
    && answers.environmentPreference.value === "CITY"
    && answers.costQualityPreference.value === "QUALITY_FIRST"
    && answers.englishSupportNeed.value === "HIGH"
    && answers.diversityImportance.value === "HIGH"
    && answers.digitalConveniencePriority.value === "HIGH"
    && answers.cultureLeisureImportance.value === "HIGH"
    && answers.newcomerSupportNeed.value === "HIGH";

  if (!wantsGlobalCityHub) {
    return 0;
  }

  const strongGlobalHub = profile.urbanityValue >= 5
    && transitSupport(profile) >= 5
    && profile.digitalConvenience >= 5
    && profile.diversity >= 5
    && profile.food >= 5
    && profile.cultureScene >= 5
    && profile.safety >= 5;

  return strongGlobalHub ? GLOBAL_HUB_BONUS : 0;
}

function foodieStarterBonus(profile, answers) {
  const wantsAffordableFoodieStart = answers.climatePreference.value === "WARM"
    && answers.pacePreference.value === "BALANCED"
    && answers.costQualityPreference.value === "VALUE_FIRST"
    && answers.environmentPreference.value === "MIXED"
    && answers.englishSupportNeed.value === "MEDIUM"
    && answers.foodImportance.value === "HIGH"
    && answers.diversityImportance.value !== "LOW"
    && answers.settlementPreference.value === "BALANCED"
    && answers.mobilityPreference.value === "BALANCED";

  if (!wantsAffordableFoodieStart || profile.priceLevel >= 3) {
    return 0;
  }

  const strongStarterFit = profile.food >= 5
    && profile.diversity >= 5
    && profile.newcomerFriendliness >= 4
    && profile.englishSupport >= 4
    && profile.digitalConvenience >= 4
    && profile.safety >= 4;

  if (strongStarterFit) {
    return FOODIE_STARTER_BONUS;
  }

  const acceptableStarterFit = profile.food >= 5
    && profile.diversity >= 5
    && profile.newcomerFriendliness >= 3
    && profile.priceLevel <= 2;

  return acceptableStarterFit ? FOODIE_STARTER_SUPPORT_BONUS : 0;
}

function temperatePublicBaseBonus(profile, answers) {
  const wantsTemperatePublicBase = answers.climatePreference.value === "MILD"
    && answers.seasonTolerance.value === "LOW"
    && answers.pacePreference.value === "BALANCED"
    && answers.costQualityPreference.value === "BALANCED"
    && answers.environmentPreference.value === "MIXED"
    && answers.englishSupportNeed.value === "MEDIUM"
    && answers.publicServicePriority.value === "HIGH"
    && answers.foodImportance.value === "LOW"
    && answers.diversityImportance.value === "LOW"
    && answers.settlementPreference.value === "BALANCED"
    && answers.mobilityPreference.value === "BALANCED";

  if (!wantsTemperatePublicBase) {
    return 0;
  }

  const strongTemperateBase = profile.priceLevel <= 3
    && profile.climateValue <= 3
    && profile.seasonality >= 4
    && profile.safety >= 4
    && profile.welfare >= 3
    && profile.housingSpace >= 4
    && profile.digitalConvenience >= 4;
  if (strongTemperateBase) {
    return TEMPERATE_PUBLIC_BASE_BONUS;
  }

  const acceptableTemperateBase = profile.priceLevel <= 3
    && profile.climateValue <= 3
    && profile.seasonality >= 4
    && profile.safety >= 4
    && profile.welfare >= 3;
  return acceptableTemperateBase ? TEMPERATE_PUBLIC_BASE_BONUS / 2 : 0;
}

function practicalPublicValueBonus(profile, answers) {
  const wantsPracticalPublicValue = answers.climatePreference.value === "MILD"
    && answers.seasonTolerance.value === "LOW"
    && answers.pacePreference.value === "BALANCED"
    && answers.costQualityPreference.value === "VALUE_FIRST"
    && answers.environmentPreference.value === "MIXED"
    && answers.englishSupportNeed.value === "MEDIUM"
    && answers.newcomerSupportNeed.value === "LOW"
    && answers.safetyPriority.value === "HIGH"
    && answers.publicServicePriority.value === "MEDIUM"
    && answers.foodImportance.value === "LOW"
    && answers.diversityImportance.value === "LOW"
    && answers.settlementPreference.value === "BALANCED"
    && answers.mobilityPreference.value === "BALANCED";

  if (!wantsPracticalPublicValue) {
    return 0;
  }

  const strongPracticalFit = profile.priceLevel <= 3
    && profile.climateValue <= 3
    && profile.safety >= 5
    && profile.welfare >= 4
    && profile.housingSpace >= 4
    && profile.digitalConvenience >= 3
    && profile.newcomerFriendliness >= 3
    && profile.urbanityValue <= 3
    && profile.paceValue <= 2;
  if (strongPracticalFit) {
    return PRACTICAL_PUBLIC_VALUE_BONUS;
  }

  const acceptablePracticalFit = profile.priceLevel <= 3
    && profile.climateValue <= 3
    && profile.safety >= 4
    && profile.welfare >= 4
    && profile.housingSpace >= 4
    && profile.newcomerFriendliness >= 3;
  return acceptablePracticalFit ? PRACTICAL_PUBLIC_VALUE_BONUS / 2 : 0;
}

function premiumWarmHubBonus(profile, answers) {
  const wantsPremiumWarmHub = answers.climatePreference.value === "WARM"
    && answers.seasonTolerance.value === "HIGH"
    && answers.pacePreference.value === "BALANCED"
    && answers.costQualityPreference.value === "QUALITY_FIRST"
    && answers.environmentPreference.value === "CITY"
    && answers.englishSupportNeed.value === "HIGH"
    && answers.newcomerSupportNeed.value === "HIGH"
    && answers.safetyPriority.value === "MEDIUM"
    && answers.publicServicePriority.value === "HIGH"
    && answers.foodImportance.value === "LOW"
    && answers.diversityImportance.value === "LOW"
    && answers.cultureLeisureImportance.value === "MEDIUM"
    && answers.settlementPreference.value === "BALANCED"
    && answers.mobilityPreference.value === "BALANCED";

  if (!wantsPremiumWarmHub) {
    return 0;
  }

  const premiumWarmHubFit = profile.climateValue >= 5
    && profile.priceLevel >= 5
    && profile.urbanityValue >= 5
    && profile.englishSupport >= 5
    && profile.digitalConvenience >= 5
    && profile.newcomerFriendliness >= 4
    && profile.housingSpace >= 2;
  if (premiumWarmHubFit) {
    return PREMIUM_WARM_HUB_BONUS;
  }

  const acceptablePremiumWarmHubFit = profile.climateValue >= 5
    && profile.priceLevel >= 5
    && profile.urbanityValue >= 4
    && profile.englishSupport >= 4
    && profile.digitalConvenience >= 4
    && profile.newcomerFriendliness >= 4;
  return acceptablePremiumWarmHubFit ? PREMIUM_WARM_HUB_BONUS / 2 : 0;
}

function softNatureBaseBonus(profile, answers) {
  const wantsSoftNatureBase = answers.climatePreference.value === "COLD"
    && answers.seasonTolerance.value === "MEDIUM"
    && answers.pacePreference.value === "RELAXED"
    && answers.costQualityPreference.value === "BALANCED"
    && answers.environmentPreference.value === "NATURE"
    && answers.housingPreference.value === "SPACE_FIRST"
    && answers.englishSupportNeed.value === "MEDIUM"
    && answers.newcomerSupportNeed.value === "LOW"
    && answers.safetyPriority.value === "HIGH"
    && answers.publicServicePriority.value === "MEDIUM"
    && answers.foodImportance.value === "LOW"
    && answers.diversityImportance.value === "LOW"
    && answers.cultureLeisureImportance.value === "LOW"
    && answers.settlementPreference.value === "BALANCED"
    && answers.mobilityPreference.value === "BALANCED";

  if (!wantsSoftNatureBase) {
    return 0;
  }

  const strongNatureFit = profile.climateValue >= 2
    && profile.climateValue <= 3
    && profile.urbanityValue <= 2
    && profile.paceValue <= 2
    && profile.englishSupport >= 5
    && profile.safety >= 5
    && profile.housingSpace >= 5
    && profile.newcomerFriendliness >= 4;
  if (strongNatureFit) {
    return SOFT_NATURE_BASE_BONUS;
  }

  const acceptableNatureFit = profile.climateValue >= 2
    && profile.urbanityValue <= 2
    && profile.paceValue <= 2
    && profile.englishSupport >= 4
    && profile.safety >= 5
    && profile.housingSpace >= 4;
  return acceptableNatureFit ? SOFT_NATURE_BASE_BONUS / 2 : 0;
}

function cosmopolitanPulseBonus(profile, answers) {
  const wantsCosmopolitanPulse = answers.climatePreference.value === "MILD"
    && answers.seasonTolerance.value === "MEDIUM"
    && answers.pacePreference.value === "FAST"
    && answers.crowdPreference.value === "LIVELY"
    && answers.costQualityPreference.value === "BALANCED"
    && answers.environmentPreference.value === "CITY"
    && answers.englishSupportNeed.value === "LOW"
    && answers.newcomerSupportNeed.value === "MEDIUM"
    && answers.safetyPriority.value === "LOW"
    && answers.publicServicePriority.value === "LOW"
    && answers.digitalConveniencePriority.value === "HIGH"
    && answers.foodImportance.value === "LOW"
    && answers.diversityImportance.value === "HIGH"
    && answers.cultureLeisureImportance.value === "HIGH"
    && answers.workLifePreference.value === "DRIVE_FIRST"
    && answers.settlementPreference.value === "BALANCED"
    && answers.futureBasePreference.value === "BALANCED";

  if (!wantsCosmopolitanPulse) {
    return 0;
  }

  const strongCosmopolitanPulse = profile.climateValue >= 3
    && profile.climateValue <= 4
    && profile.paceValue >= 4
    && profile.urbanityValue >= 5
    && profile.diversity >= 5
    && profile.cultureScene >= 5
    && profile.food >= 4
    && profile.housingSpace >= 4
    && profile.digitalConvenience >= 5;
  if (strongCosmopolitanPulse) {
    return COSMOPOLITAN_PULSE_BONUS;
  }

  const acceptableCosmopolitanPulse = profile.climateValue >= 4
    && profile.paceValue >= 4
    && profile.urbanityValue >= 5
    && profile.diversity >= 5
    && profile.cultureScene >= 5
    && profile.food >= 5
    && profile.priceLevel <= 2;
  return acceptableCosmopolitanPulse ? COSMOPOLITAN_PULSE_BONUS / 2 : 0;
}

function temperateGlobalCityBonus(profile, answers) {
  const wantsTemperateGlobalCity = answers.climatePreference.value === "MILD"
    && answers.seasonTolerance.value === "LOW"
    && answers.pacePreference.value === "FAST"
    && answers.crowdPreference.value === "LIVELY"
    && answers.costQualityPreference.value === "QUALITY_FIRST"
    && answers.environmentPreference.value === "CITY"
    && answers.englishSupportNeed.value === "HIGH"
    && answers.newcomerSupportNeed.value === "HIGH"
    && answers.safetyPriority.value === "LOW"
    && answers.publicServicePriority.value === "LOW"
    && answers.digitalConveniencePriority.value === "HIGH"
    && answers.foodImportance.value === "LOW"
    && answers.diversityImportance.value === "HIGH"
    && answers.cultureLeisureImportance.value === "HIGH"
    && answers.workLifePreference.value === "DRIVE_FIRST"
    && answers.settlementPreference.value === "BALANCED"
    && answers.futureBasePreference.value === "BALANCED";

  if (!wantsTemperateGlobalCity) {
    return 0;
  }

  const strongTemperateGlobalCity = profile.climateValue >= 2
    && profile.climateValue <= 3
    && profile.seasonality >= 4
    && profile.paceValue >= 4
    && profile.urbanityValue >= 5
    && profile.englishSupport >= 5
    && profile.digitalConvenience >= 5
    && profile.diversity >= 5
    && profile.cultureScene >= 5
    && profile.newcomerFriendliness >= 4
    && profile.priceLevel <= 4;
  if (strongTemperateGlobalCity) {
    return TEMPERATE_GLOBAL_CITY_BONUS;
  }

  const acceptableTemperateGlobalCity = profile.climateValue >= 2
    && profile.climateValue <= 3
    && profile.seasonality >= 4
    && profile.urbanityValue >= 5
    && profile.englishSupport >= 5
    && profile.diversity >= 5
    && profile.cultureScene >= 5
    && profile.newcomerFriendliness >= 4;
  return acceptableTemperateGlobalCity ? TEMPERATE_GLOBAL_CITY_BONUS / 2 : 0;
}

function accessibleWarmValueHubBonus(profile, answers) {
  const scenarioMatch = answers.climatePreference.value === "WARM"
    && answers.seasonTolerance.value === "MEDIUM"
    && answers.pacePreference.value === "BALANCED"
    && answers.costQualityPreference.value === "VALUE_FIRST"
    && answers.environmentPreference.value === "MIXED"
    && answers.mobilityPreference.value === "BALANCED"
    && answers.englishSupportNeed.value === "MEDIUM"
    && answers.publicServicePriority.value === "HIGH"
    && answers.newcomerSupportNeed.value === "MEDIUM"
    && answers.safetyPriority.value === "MEDIUM"
    && answers.digitalConveniencePriority.value === "MEDIUM"
    && answers.foodImportance.value === "LOW"
    && answers.diversityImportance.value === "LOW"
    && answers.cultureLeisureImportance.value === "MEDIUM"
    && answers.workLifePreference.value === "BALANCED"
    && answers.settlementPreference.value === "BALANCED"
    && answers.futureBasePreference.value === "BALANCED";

  if (!scenarioMatch) {
    return 0;
  }

  const strongFit = profile.climateValue >= 4
    && profile.priceLevel <= 2
    && profile.urbanityValue >= 4
    && profile.englishSupport >= 4
    && profile.welfare >= 3
    && profile.digitalConvenience >= 4
    && profile.newcomerFriendliness >= 4;
  if (strongFit) {
    return ACCESSIBLE_WARM_VALUE_HUB_BONUS;
  }

  const acceptableFit = profile.climateValue >= 4
    && profile.priceLevel <= 2
    && profile.urbanityValue >= 4
    && profile.newcomerFriendliness >= 4;
  return acceptableFit ? ACCESSIBLE_WARM_VALUE_HUB_BONUS / 2 : 0;
}

function formatPopulation(population) {
  return new Intl.NumberFormat("ko-KR").format(population);
}

function buildRecommendationCatalog(countries) {
  const countryMap = new Map(countries.map((country) => [country.iso3Code, country]));

  return COUNTRY_PROFILES.map((profile) => {
    const country = countryMap.get(profile.iso3Code);
    if (!country) {
      return null;
    }

    return {
      ...profile,
      countryNameKr: country.nameKr,
      countryNameEn: country.nameEn,
      capitalCity: country.capitalCityKr || country.capitalCity,
      populationLabel: `${formatPopulation(Number(country.population))}명`,
      continentLabel: CONTINENT_LABELS[country.continent] || country.continent
    };
  }).filter(Boolean);
}

function scoreCandidate(profile, answers) {
  const signals = [];

  const climateDistance = distance(answers.climatePreference.targetValue, profile.climateValue);
  signals.push({
    points: closenessScore(climateDistance, CLIMATE_WEIGHT) + exactMatchBonus(climateDistance)
      + mismatchPenalty(climateDistance, CLIMATE_MISMATCH_PENALTY),
    message: `${answers.climatePreference.description} 기준에서 체감 기후가 비교적 잘 맞습니다.`
  });

  const seasonStyleDistance = distance(answers.seasonStylePreference.targetValue, profile.seasonality);
  signals.push({
    points: closenessScore(seasonStyleDistance, SEASON_STYLE_WEIGHT) + exactMatchBonus(seasonStyleDistance)
      + mismatchPenalty(seasonStyleDistance, SEASON_STYLE_MISMATCH_PENALTY),
    message: "계절 변화에 대한 기대와 실제 계절감이 크게 어긋나지 않습니다."
  });

  const weatherAdaptationDistance = distance(answers.seasonTolerance.toleranceValue, weatherDemand(profile));
  signals.push({
    points: closenessScore(weatherAdaptationDistance, WEATHER_ADAPTATION_WEIGHT),
    message: "기후 적응 난도와 감수 의향이 비교적 잘 맞습니다."
  });

  const paceDistance = distance(answers.pacePreference.targetValue, profile.paceValue);
  signals.push({
    points: closenessScore(paceDistance, PACE_WEIGHT) + exactMatchBonus(paceDistance),
    message: `${answers.pacePreference.description} 기준의 생활 리듬과 비교적 잘 맞습니다.`
  });

  const crowdDistance = distance(answers.crowdPreference.targetValue, crowdEnergy(profile));
  signals.push({
    points: closenessScore(crowdDistance, CROWD_WEIGHT) + exactMatchBonus(crowdDistance),
    message: "동네의 밀도와 자극 수준이 원하는 방향과 가깝습니다."
  });

  const costQualityDistance = distance(answers.costQualityPreference.targetPriceLevel, profile.priceLevel);
  signals.push({
    points: costQualityPoints(answers.costQualityPreference, profile.priceLevel, costQualityDistance),
    message: "생활비 부담과 생활 품질에 대한 기대 수준이 크게 엇나가지 않습니다."
  });

  const housingDistance = distance(answers.housingPreference.targetSpaceValue, profile.housingSpace);
  signals.push({
    points: closenessScore(housingDistance, HOUSING_WEIGHT) + exactMatchBonus(housingDistance),
    message: "주거 공간과 중심 접근성 기준이 원하는 방향과 비슷합니다."
  });

  const environmentDistance = distance(answers.environmentPreference.targetUrbanity, profile.urbanityValue);
  signals.push({
    points: closenessScore(environmentDistance, ENVIRONMENT_WEIGHT) + exactMatchBonus(environmentDistance),
    message: "도시 편의와 자연 접근성의 비중이 원하는 쪽과 가깝습니다."
  });

  const mobilityDistance = distance(answers.mobilityPreference.targetTransitValue, transitSupport(profile));
  signals.push({
    points: closenessScore(mobilityDistance, MOBILITY_WEIGHT) + exactMatchBonus(mobilityDistance),
    message: "이동 방식과 생활 동선이 원하는 방향과 잘 맞습니다."
  });

  signals.push({
    points: supportPoints(profile.englishSupport, answers.englishSupportNeed, ENGLISH_SUPPORT_WEIGHT),
    message: "초기 적응에서 필요한 영어 지원 수준을 비교적 잘 충족합니다."
  });

  signals.push({
    points: supportPoints(newcomerSupport(profile), answers.newcomerSupportNeed, NEWCOMER_SUPPORT_WEIGHT),
    message: "처음 정착할 때 필요한 안내와 친절한 분위기 기대와 잘 맞습니다."
  });

  signals.push({
    points: priorityPoints(profile.safety, answers.safetyPriority, SAFETY_WEIGHT),
    message: "치안과 생활 안전 기준에서 안정적인 편입니다."
  });

  signals.push({
    points: priorityPoints(profile.welfare, answers.publicServicePriority, PUBLIC_SERVICE_WEIGHT),
    message: "의료·행정·복지 같은 공공 서비스 기대치와 잘 맞습니다."
  });

  signals.push({
    points: priorityPoints(profile.digitalConvenience, answers.digitalConveniencePriority, DIGITAL_WEIGHT),
    message: "디지털 행정, 결제, 생활 인프라의 매끄러움에서 강점을 보입니다."
  });

  signals.push({
    points: priorityPoints(profile.food, answers.foodImportance, FOOD_WEIGHT),
    message: "음식 만족도와 외식 선택지 측면에서 장점을 보입니다."
  });

  signals.push({
    points: priorityPoints(profile.diversity, answers.diversityImportance, DIVERSITY_WEIGHT),
    message: "다양한 배경의 사람들과 섞여 지내기 좋은 환경에 가깝습니다."
  });

  signals.push({
    points: priorityPoints(profile.cultureScene, answers.cultureLeisureImportance, CULTURE_WEIGHT),
    message: "문화·여가 선택지 밀도에서 기대한 방향과 가깝습니다."
  });

  const workLifeDistance = distance(answers.workLifePreference.targetIntensityValue, workIntensity(profile));
  signals.push({
    points: closenessScore(workLifeDistance, WORK_LIFE_WEIGHT) + exactMatchBonus(workLifeDistance),
    message: "일 기회와 생활 균형 사이에서 원하는 강도와 비교적 잘 맞습니다."
  });

  signals.push({
    points: settlementPoints(profile, answers.settlementPreference),
    message: `${answers.settlementPreference.description} 기준에서 적응 난도와 생활 안정성을 함께 반영했습니다.`
  });

  signals.push({
    points: experienceTransitBonus(profile, answers),
    message: "가볍게 적응하며 대중교통 중심으로 살아보기 좋은지도 함께 반영했습니다."
  });
  signals.push({
    points: civicBaseBonus(profile, answers),
    message: "안전, 공공 서비스, 기본 정착 안정성을 함께 보는 균형형 생활 기준을 반영했습니다."
  });
  signals.push({
    points: practicalSafetyBonus(profile, answers),
    message: "비용을 아끼면서도 실제로 적응하기 쉬운 안전 중심 생활인지 함께 반영했습니다."
  });
  signals.push({
    points: softLandingBonus(profile, answers),
    message: "영어와 정착 친화도가 충분해 초기에 부딪히는 장벽이 낮은지도 함께 반영했습니다."
  });
  signals.push({
    points: familyBaseBonus(profile, answers),
    message: "가족 단위로도 오래 버티기 쉬운 안전·복지·영어 기반인지 함께 반영했습니다."
  });
  signals.push({
    points: temperateFamilyBridgeBonus(profile, answers),
    message: "온화한 기후권에서도 영어 적응과 복지, 주거 안정성이 함께 받쳐주는 가족형 기반인지 반영했습니다."
  });
  signals.push({
    points: exploratoryNatureRunwayBonus(profile, answers),
    message: "가볍게 살아보는 탐색 단계에서도 자연, 영어 적응, 생활 여유가 함께 확보되는지 반영했습니다."
  });
  signals.push({
    points: globalHubBonus(profile, answers),
    message: "영어·대중교통·디지털·문화 밀도가 모두 높은 초도시형 허브인지 함께 반영했습니다."
  });
  signals.push({
    points: foodieStarterBonus(profile, answers),
    message: "생활비를 아끼면서도 음식과 다문화 적응 장벽이 낮은 시작점인지 함께 반영했습니다."
  });
  signals.push({
    points: temperatePublicBaseBonus(profile, answers),
    message: "온화한 기후에서 공공서비스와 정착 기반을 함께 보는 균형형 생활 기준을 반영했습니다."
  });
  signals.push({
    points: practicalPublicValueBonus(profile, answers),
    message: "비용을 아끼면서도 안전과 기본 정착 기반이 안정적인 현실형 기준을 반영했습니다."
  });
  signals.push({
    points: premiumWarmHubBonus(profile, answers),
    message: "높은 비용을 감수하더라도 영어 적응과 중심 생활 인프라가 강한 프리미엄 허브인지 반영했습니다."
  });
  signals.push({
    points: softNatureBaseBonus(profile, answers),
    message: "너무 거칠지 않은 기후와 영어 적응성을 갖춘 자연형 정착지인지 함께 반영했습니다."
  });
  signals.push({
    points: cosmopolitanPulseBonus(profile, answers),
    message: "영어 의존이 낮아도 다문화 자극과 빠른 도시 에너지를 충분히 느낄 수 있는지 반영했습니다."
  });
  signals.push({
    points: temperateGlobalCityBonus(profile, answers),
    message: "온화한 기후에서도 영어 적응과 글로벌 도시 연결성이 충분한지 함께 반영했습니다."
  });
  signals.push({
    points: accessibleWarmValueHubBonus(profile, answers),
    message: "따뜻한 기후권에서도 영어 적응과 생활 편의 균형이 좋은 실용형 거점인지 반영했습니다."
  });

  const futureBaseDistance = distance(answers.futureBasePreference.targetValue, futureBase(profile));
  signals.push({
    points: closenessScore(futureBaseDistance, FUTURE_BASE_WEIGHT) + exactMatchBonus(futureBaseDistance),
    message: "당장의 편의와 장기 기반 사이에서 기대한 방향과 가깝습니다."
  });

  signals.push({
    points: coherenceBonus(climateDistance, costQualityDistance, environmentDistance, housingDistance),
    message: "핵심 생활 조건들이 전반적으로 크게 어긋나지 않습니다."
  });

  const totalScore = signals.reduce((sum, signal) => sum + signal.points, 0);
  const strongSignalCount = signals.filter((signal) => signal.points >= 14).length;
  const exactMatchCountValue = exactMatchCount(
    climateDistance,
    seasonStyleDistance,
    paceDistance,
    crowdDistance,
    costQualityDistance,
    housingDistance,
    environmentDistance,
    mobilityDistance,
    workLifeDistance,
    futureBaseDistance
  );

  const reasons = signals
    .filter((signal) => signal.points > 0)
    .sort((left, right) => right.points - left.points)
    .map((signal) => signal.message)
    .filter((message, index, all) => all.indexOf(message) === index)
    .slice(0, 3);

  return {
    ...profile,
    matchScore: totalScore,
    strongSignalCount,
    exactMatchCount: exactMatchCountValue,
    reasons
  };
}

export function calculateRecommendationResult(countries, rawAnswers) {
  const answers = normalizeAnswers(rawAnswers);
  const catalog = buildRecommendationCatalog(countries);

  const recommendations = catalog
    .map((profile) => scoreCandidate(profile, answers))
    .sort(
      (left, right) =>
        right.matchScore - left.matchScore ||
        right.strongSignalCount - left.strongSignalCount ||
        right.exactMatchCount - left.exactMatchCount ||
        left.countryNameKr.localeCompare(right.countryNameKr, "ko-KR")
    )
    .slice(0, 3)
    .map((candidate, index) => ({
      ...candidate,
      rank: index + 1
    }));

  return {
    submittedPreferences: QUESTIONS.map((question) => ({
      title: question.title,
      selectedLabel: answers[question.id].label
    })),
    recommendations,
    summary: buildRecommendationSummary(answers, recommendations),
    comparison: {
      rows: buildComparisonRows(answers, recommendations)
    }
  };
}

async function copyText(text) {
  if (!text) {
    return false;
  }

  try {
    if (globalThis.navigator?.clipboard?.writeText) {
      await globalThis.navigator.clipboard.writeText(text);
      return true;
    }
  } catch (_error) {
    // fall through to legacy copy path
  }

  try {
    const textarea = globalThis.document.createElement("textarea");
    textarea.value = text;
    textarea.setAttribute("readonly", "");
    textarea.style.position = "absolute";
    textarea.style.left = "-9999px";
    globalThis.document.body.append(textarea);
    textarea.select();
    const copied = globalThis.document.execCommand("copy");
    textarea.remove();
    return copied;
  } catch (_error) {
    return false;
  }
}

function renderQuestion(question, questionNumber, selectedValue) {
  const options = question.options
    .map(
      (option) => `
        <label class="demo-survey-option">
          <input type="radio" name="${question.id}" value="${option.value}" ${selectedValue === option.value ? "checked" : ""}>
          <span class="demo-survey-option__copy">
            <strong>${option.label}</strong>
            <span>${option.description}</span>
          </span>
        </label>
      `
    )
    .join("");

  return `
    <section class="demo-panel demo-question-panel">
      <div class="demo-question-head">
        <span class="demo-question-index">Q${questionNumber}</span>
        <div class="demo-panel-head">
          <h2>${question.title}</h2>
          <p class="demo-question-helper">${question.helperText}</p>
        </div>
      </div>
      <div class="demo-survey-option-grid">
        ${options}
      </div>
    </section>
  `;
}

function renderQuestionSection(section, selectedAnswers, startIndex) {
  const questions = section.questionIds.map((questionId) => getQuestion(questionId));
  return `
    <section class="demo-panel demo-panel--muted">
      <div class="demo-panel-head">
        <p class="demo-panel-kicker">질문 묶음</p>
        <h2>${section.title}</h2>
        <p>${section.helperText}</p>
      </div>
      <div class="demo-layout">
        ${questions
          .map((question, index) => renderQuestion(question, startIndex + index, selectedAnswers[question.id]))
          .join("")}
      </div>
    </section>
  `;
}

function renderResult(result) {
  const preferences = result.submittedPreferences
    .map(
      (preference) => `
        <article class="demo-pill">
          <strong>${preference.title}</strong>
          <span>${preference.selectedLabel}</span>
        </article>
      `
    )
    .join("");

  const recommendationCards = result.recommendations
    .map(
      (candidate) => `
        <article class="demo-card" data-rank="${candidate.rank}">
          <div class="demo-card-top">
            <span class="demo-chip">TOP ${candidate.rank}</span>
            <span class="demo-note">${candidate.iso3Code} · ${candidate.continentLabel}</span>
          </div>
          <h2>${candidate.countryNameKr}</h2>
          <p>${candidate.hookLine}</p>
          <ul class="demo-history-list">
            <li><span>수도</span><strong>${candidate.capitalCity}</strong></li>
            <li><span>인구</span><strong>${candidate.populationLabel}</strong></li>
            <li><span>잘 맞는 정도</span><strong>${candidate.matchScore}</strong></li>
            <li><span>강한 일치 신호</span><strong>${candidate.strongSignalCount}</strong></li>
          </ul>
          <ul class="demo-list">
            ${candidate.reasons.map((reason) => `<li>${reason}</li>`).join("")}
          </ul>
        </article>
      `
    )
    .join("");

  const comparisonCards = result.comparison.rows
    .map(
      (row) => `
        <article class="demo-compare-card">
          <div class="demo-card-meta">
            <strong>${row.title}</strong>
            <span class="demo-card-note">내 기준: ${row.selectedLabel}</span>
          </div>
          <div class="demo-compare-values">
            ${row.candidates
              .map(
                (candidate) => `
                  <article class="demo-compare-value" data-rank="${candidate.rank}">
                    <span>TOP ${candidate.rank} · ${candidate.countryNameKr}</span>
                    <strong>${candidate.value}</strong>
                  </article>
                `
              )
              .join("")}
          </div>
        </article>
      `
    )
    .join("");

  return `
    <section class="demo-route-hero" data-tone="recommendation">
      <div class="demo-route-hero-top">
        <span class="demo-chip">20문항</span>
        <span class="demo-chip">추천 3곳</span>
        <span class="demo-chip">바로 결과</span>
      </div>
      <h1>지금 생활 취향과 잘 맞는 나라 3곳</h1>
      <div class="demo-route-meta">
        <p>답변을 바탕으로 지금 어울리는 국가 3곳을 골랐습니다. 가장 잘 맞는 곳부터 순서대로 확인해 보세요.</p>
      </div>
      <div class="demo-status-strip demo-status-strip--recommendation-result">
        <article class="demo-status-card" data-tone="recommendation">
          <span>질문 수</span>
          <strong>${DEMO_LITE_RECOMMENDATION_QUESTION_COUNT}문항</strong>
        </article>
        <article class="demo-status-card" data-tone="recommendation" data-mobile-hidden="true">
          <span>비교 국가</span>
          <strong>${DEMO_LITE_RECOMMENDATION_PROFILE_COUNT}개</strong>
        </article>
        <article class="demo-status-card" data-tone="recommendation" data-mobile-hidden="true">
          <span>추천 수</span>
          <strong>TOP ${result.recommendations.length}</strong>
        </article>
        <article class="demo-status-card" data-tone="recommendation">
          <span>가장 잘 맞는 곳</span>
          <strong>${result.recommendations[0]?.countryNameKr ?? "-"}</strong>
        </article>
      </div>
      <div class="demo-actions">
        <button class="demo-button" type="button" data-recommendation-action="restart">설문 다시 하기</button>
        <a class="demo-ghost" href="#/">홈으로 돌아가기</a>
      </div>
    </section>

    <section class="demo-panel demo-panel--muted">
      <div class="demo-panel-head">
        <p class="demo-panel-kicker">추천 요약</p>
        <h2>${result.summary.headline}</h2>
        <p>${result.summary.narrative}</p>
      </div>
      <div class="demo-route-hero-top">
        ${result.summary.highlightLabels.map((label) => `<span class="demo-chip">${label}</span>`).join("")}
      </div>
      <div class="demo-share-box">
        <div class="demo-share-box__top">
          <div class="demo-card-meta">
            <strong>공유용 한 줄 요약</strong>
            <span class="demo-card-note">top 3와 선호 키워드를 한 문장으로 정리했습니다.</span>
          </div>
          <button class="demo-button demo-copy-button" type="button" data-recommendation-action="copy-share">요약 복사</button>
        </div>
        <p class="demo-share-text">${result.summary.shareText}</p>
        <p class="demo-copy demo-copy--small" data-recommendation-share-feedback>메신저나 메모에 그대로 붙여 넣을 수 있습니다.</p>
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>같은 기준으로 비교해 보기</h2>
        <p>상위 3개 국가를 같은 축으로 나란히 두고 보면 차이를 더 빨리 읽을 수 있습니다.</p>
      </div>
      <div class="demo-compare-grid">
        ${comparisonCards}
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>내가 고른 기준</h2>
        <p>답변을 빠르게 다시 볼 수 있도록 한 줄씩 정리했습니다.</p>
      </div>
      <div class="demo-pill-grid">
        ${preferences}
      </div>
    </section>

    <section class="demo-panel">
      <div class="demo-panel-head">
        <h2>추천 결과</h2>
        <p>가장 잘 맞는 곳부터 순서대로 살펴보세요.</p>
      </div>
      <div class="demo-card-grid demo-card-grid--recommendation">
        ${recommendationCards}
      </div>
    </section>
  `;
}

export function renderRecommendationDemoPage() {
  return `<section id="demo-recommendation-root" class="demo-layout"></section>`;
}

export function mountRecommendationDemo(container, countries) {
  if (!container) {
    return () => {};
  }

  let selectedAnswers = {};
  let currentResult = null;

  function renderSurvey(message = "") {
    const answeredCount = QUESTIONS.filter((question) => Boolean(selectedAnswers[question.id])).length;
    container.innerHTML = `
      <section class="demo-route-hero" data-tone="recommendation">
        <div class="demo-route-hero-top">
          <span class="demo-chip">20문항</span>
          <span class="demo-chip">취향 설문</span>
          <span class="demo-chip">추천 3곳</span>
        </div>
        <h1>나에게 어울리는 국가 찾기</h1>
        <div class="demo-route-meta">
          <p>생활 취향과 여행 스타일에 맞는 국가를 고르면 바로 결과를 볼 수 있습니다.</p>
        </div>
        <div class="demo-status-strip demo-status-strip--recommendation-survey">
          <article class="demo-status-card" data-tone="recommendation">
            <span>답변 완료</span>
            <strong>${answeredCount} / ${DEMO_LITE_RECOMMENDATION_QUESTION_COUNT}</strong>
          </article>
          <article class="demo-status-card" data-tone="recommendation">
            <span>남은 질문</span>
            <strong>${DEMO_LITE_RECOMMENDATION_QUESTION_COUNT - answeredCount}개</strong>
          </article>
          <article class="demo-status-card" data-tone="recommendation" data-mobile-hidden="true">
            <span>비교 국가</span>
            <strong>${DEMO_LITE_RECOMMENDATION_PROFILE_COUNT}국가</strong>
          </article>
          <article class="demo-status-card" data-tone="recommendation" data-mobile-hidden="true">
            <span>결과</span>
            <strong>추천 3곳</strong>
          </article>
        </div>
      </section>

      ${message ? `<section class="demo-panel demo-panel--muted"><div class="demo-panel-head"><p>${message}</p></div></section>` : ""}

      <form id="demo-recommendation-form" class="demo-layout">
        ${QUESTION_SECTIONS.map((section, index) => {
          const startIndex = QUESTION_SECTIONS.slice(0, index)
            .reduce((sum, candidate) => sum + candidate.questionIds.length, 0) + 1;
          return renderQuestionSection(section, selectedAnswers, startIndex);
        }).join("")}
        <section class="demo-panel">
          <div class="demo-panel-head">
            <h2>결과 계산</h2>
            <p>모든 질문에 답하면 바로 추천 결과를 확인할 수 있습니다.</p>
          </div>
          <div class="demo-actions">
            <button class="demo-button" type="submit">추천 결과 보기</button>
            <a class="demo-ghost" href="#/">홈으로 돌아가기</a>
          </div>
        </section>
      </form>
    `;

    container.querySelector("#demo-recommendation-form")?.addEventListener("submit", (event) => {
      event.preventDefault();
      const formData = new window.FormData(event.currentTarget);
      const answers = {};

      for (const question of QUESTIONS) {
        const value = formData.get(question.id);
        if (!value) {
          renderSurvey("모든 질문에 답해야 추천 결과를 계산할 수 있습니다.");
          return;
        }
        answers[question.id] = value;
      }

      selectedAnswers = answers;
      currentResult = calculateRecommendationResult(countries, selectedAnswers);
      if (currentResult.recommendations[0]) {
        recordDemoLiteRecommendationResult({
          topRecommendationName: currentResult.recommendations[0].countryNameKr,
          topRecommendationIso3Code: currentResult.recommendations[0].iso3Code,
          recommendationCount: currentResult.recommendations.length
        });
      }
      renderCurrentState();
    });
  }

  function renderCurrentState() {
    if (!currentResult) {
      renderSurvey();
      return;
    }

    container.innerHTML = renderResult(currentResult);
    container.querySelector("[data-recommendation-action='restart']")?.addEventListener("click", () => {
      currentResult = null;
      renderSurvey();
    });
    container.querySelector("[data-recommendation-action='copy-share']")?.addEventListener("click", async (event) => {
      const copied = await copyText(currentResult.summary.shareText);
      const button = event.currentTarget;
      const feedback = container.querySelector("[data-recommendation-share-feedback]");

      if (feedback) {
        feedback.textContent = copied
          ? "추천 요약을 복사했습니다."
          : "자동 복사는 실패했습니다. 아래 문장을 직접 복사해 주세요.";
      }

      if (button && "textContent" in button) {
        button.textContent = copied ? "복사 완료" : "다시 시도";
        window.setTimeout(() => {
          button.textContent = "요약 복사";
        }, 1600);
      }
    });
  }

  renderCurrentState();
  return () => {};
}
