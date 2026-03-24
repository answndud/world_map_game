package com.worldmap.recommendation.application;

import com.worldmap.recommendation.domain.RecommendationSurveyAnswers;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecommendationQuestionCatalog {

	public List<RecommendationQuestionView> questions() {
		return List.of(
			new RecommendationQuestionView(
				"climatePreference",
				"오래 머물 때 몸이 가장 편한 온도감은 어느 쪽인가요?",
				"그냥 좋아하는 날씨보다, 몇 달 이상 살아도 컨디션이 잘 유지될 기후를 떠올리면 더 정확합니다.",
				optionViews(RecommendationSurveyAnswers.ClimatePreference.values())
			),
			new RecommendationQuestionView(
				"seasonStylePreference",
				"계절 변화는 어느 정도 있는 편이 좋나요?",
				"연중 비슷한 날씨가 좋은지, 사계절이 분명한 쪽이 좋은지에 따라 추천이 달라집니다.",
				optionViews(RecommendationSurveyAnswers.SeasonStylePreference.values())
			),
			new RecommendationQuestionView(
				"seasonTolerance",
				"날씨가 아주 완벽하지 않아도 다른 장점이 크면 적응할 수 있나요?",
				"좋은 인프라, 기회, 분위기가 있다면 더위·추위·습도 차이를 어느 정도 감수할 수 있는지 생각해보세요.",
				optionViews(RecommendationSurveyAnswers.SeasonTolerance.values())
			),
			new RecommendationQuestionView(
				"pacePreference",
				"하루 리듬은 어느 정도 속도가 맞나요?",
				"도시가 빠르게 돌아갈수록 살아난다고 느끼는지, 아니면 여유가 있어야 편한지 골라주세요.",
				optionViews(RecommendationSurveyAnswers.PacePreference.values())
			),
			new RecommendationQuestionView(
				"crowdPreference",
				"사람이 많고 자극이 많은 동네는 에너지가 되나요, 피로가 되나요?",
				"붐비는 상권과 밀도 높은 생활권이 즐거운지, 금방 지치는지 생각해보면 됩니다.",
				optionViews(RecommendationSurveyAnswers.CrowdPreference.values())
			),
			new RecommendationQuestionView(
				"costQualityPreference",
				"생활비가 높더라도 인프라·안전·편의가 확실하면 감수할 의향이 있나요?",
				"대부분은 낮은 물가를 좋아합니다. 그래서 이번 질문은 '비용을 더 내더라도 얻고 싶은 생활 품질이 있는가'를 묻습니다.",
				optionViews(RecommendationSurveyAnswers.CostQualityPreference.values())
			),
			new RecommendationQuestionView(
				"housingPreference",
				"집 크기와 도심 접근성 중 어디에 더 무게를 두나요?",
				"조금 좁아도 생활 반경이 촘촘한 곳이 좋은지, 이동이 늘어도 공간 여유가 더 중요한지 떠올려보세요.",
				optionViews(RecommendationSurveyAnswers.HousingPreference.values())
			),
			new RecommendationQuestionView(
				"environmentPreference",
				"도시 편의와 자연 접근성 중 어느 쪽의 비중이 더 큰가요?",
				"도심 자극과 편의가 중요한지, 공원·바다·산처럼 숨 쉴 공간이 더 중요한지 고릅니다.",
				optionViews(RecommendationSurveyAnswers.EnvironmentPreference.values())
			),
			new RecommendationQuestionView(
				"mobilityPreference",
				"이동 방식은 어느 쪽이 더 편한가요?",
				"대중교통과 도보만으로 생활되는 편이 좋은지, 넓은 공간에서 느긋하게 이동하는 것도 괜찮은지 골라주세요.",
				optionViews(RecommendationSurveyAnswers.MobilityPreference.values())
			),
			new RecommendationQuestionView(
				"englishSupportNeed",
				"초기 적응 단계에서 영어 지원은 어느 정도 필요하신가요?",
				"행정, 생활, 커뮤니티 적응에서 영어만으로도 버틸 수 있어야 안심되는지 생각해보세요.",
				optionViews(RecommendationSurveyAnswers.EnglishSupportNeed.values())
			),
			new RecommendationQuestionView(
				"newcomerSupportNeed",
				"처음 가는 곳에서도 비교적 친절하게 적응할 수 있는 분위기가 중요하신가요?",
				"현지인의 안내, 처음 들어가기 쉬운 커뮤니티, 낯선 사람에게 열린 분위기를 얼마나 중시하는지 묻습니다.",
				optionViews(RecommendationSurveyAnswers.NewcomerSupportNeed.values())
			),
			new RecommendationQuestionView(
				"safetyPriority",
				"치안과 생활 안전은 어느 정도 핵심 조건인가요?",
				"다른 장점이 많더라도 안전 체감이 낮으면 오래 머물기 어렵다고 느끼는지 판단해보세요.",
				optionViews(RecommendationSurveyAnswers.ImportanceLevel.values())
			),
			new RecommendationQuestionView(
				"publicServicePriority",
				"의료·행정·복지 같은 공공 서비스 수준은 어느 정도 중요하신가요?",
				"당장 화려하지 않아도 제도와 기본 서비스가 안정적인 나라를 얼마나 중시하는지 고릅니다.",
				optionViews(RecommendationSurveyAnswers.ImportanceLevel.values())
			),
			new RecommendationQuestionView(
				"digitalConveniencePriority",
				"인터넷 속도, 디지털 결제, 온라인 행정처럼 생활 인프라의 매끄러움은 얼마나 중요하신가요?",
				"작은 불편이 누적되면 스트레스를 크게 느끼는 편인지, 다른 장점이 크면 감수 가능한지 생각해보세요.",
				optionViews(RecommendationSurveyAnswers.ImportanceLevel.values())
			),
			new RecommendationQuestionView(
				"foodImportance",
				"음식 만족도와 외식 선택지는 생활 적응에 얼마나 영향을 주나요?",
				"여행이 아니라 매일의 식사 만족도를 기준으로 생각하면 더 정확합니다.",
				optionViews(RecommendationSurveyAnswers.ImportanceLevel.values())
			),
			new RecommendationQuestionView(
				"diversityImportance",
				"다양한 국적과 문화가 섞인 분위기는 어느 정도 중요하신가요?",
				"국제적인 분위기, 여러 배경의 사람들과 섞여 지내는 환경을 얼마나 중시하는지 고릅니다.",
				optionViews(RecommendationSurveyAnswers.ImportanceLevel.values())
			),
			new RecommendationQuestionView(
				"cultureLeisureImportance",
				"전시, 공연, 카페, 야간 문화처럼 문화·여가 선택지는 얼마나 중요하신가요?",
				"주말과 퇴근 후에 즐길 수 있는 옵션이 많아야 삶의 만족도가 올라가는지 생각해보세요.",
				optionViews(RecommendationSurveyAnswers.ImportanceLevel.values())
			),
			new RecommendationQuestionView(
				"workLifePreference",
				"조금 경쟁적이더라도 기회가 많은 환경을 선호하나요, 아니면 생활 균형을 더 보나요?",
				"성장 기회가 빠른 곳과 회복 가능한 생활 리듬 중 어느 쪽이 더 중요한지 묻습니다.",
				optionViews(RecommendationSurveyAnswers.WorkLifePreference.values())
			),
			new RecommendationQuestionView(
				"settlementPreference",
				"이번 선택은 '먼저 살아보기'와 '장기 정착 가능성' 중 어디에 더 가까운가요?",
				"지금은 경험이 우선인지, 처음부터 오래 머물 수 있는 조건까지 보고 싶은지에 따라 결과가 달라집니다.",
				optionViews(RecommendationSurveyAnswers.SettlementPreference.values())
			),
			new RecommendationQuestionView(
				"futureBasePreference",
				"지금의 편의보다, 앞으로 기반이 탄탄한 곳을 더 원하나요?",
				"당장의 적응 난도보다 나중의 안정감과 기반이 더 중요한지까지 함께 묻는 질문입니다.",
				optionViews(RecommendationSurveyAnswers.FutureBasePreference.values())
			)
		);
	}

	public List<RecommendationPreferenceSummaryView> summariesOf(RecommendationSurveyAnswers answers) {
		return List.of(
			new RecommendationPreferenceSummaryView("기후 방향", answers.climatePreference().label()),
			new RecommendationPreferenceSummaryView("계절 변화 선호", answers.seasonStylePreference().label()),
			new RecommendationPreferenceSummaryView("기후 적응 성향", answers.seasonTolerance().label()),
			new RecommendationPreferenceSummaryView("생활 속도", answers.pacePreference().label()),
			new RecommendationPreferenceSummaryView("동네 밀도 선호", answers.crowdPreference().label()),
			new RecommendationPreferenceSummaryView("비용·품질 기준", answers.costQualityPreference().label()),
			new RecommendationPreferenceSummaryView("주거 기준", answers.housingPreference().label()),
			new RecommendationPreferenceSummaryView("생활 환경", answers.environmentPreference().label()),
			new RecommendationPreferenceSummaryView("이동 방식", answers.mobilityPreference().label()),
			new RecommendationPreferenceSummaryView("영어 지원 필요도", answers.englishSupportNeed().label()),
			new RecommendationPreferenceSummaryView("초기 적응 지원 필요도", answers.newcomerSupportNeed().label()),
			new RecommendationPreferenceSummaryView("치안 우선도", answers.safetyPriority().label()),
			new RecommendationPreferenceSummaryView("공공 서비스 우선도", answers.publicServicePriority().label()),
			new RecommendationPreferenceSummaryView("디지털 생활 편의 우선도", answers.digitalConveniencePriority().label()),
			new RecommendationPreferenceSummaryView("음식 만족도 우선도", answers.foodImportance().label()),
			new RecommendationPreferenceSummaryView("문화 다양성 우선도", answers.diversityImportance().label()),
			new RecommendationPreferenceSummaryView("문화·여가 우선도", answers.cultureLeisureImportance().label()),
			new RecommendationPreferenceSummaryView("일·생활 균형 기준", answers.workLifePreference().label()),
			new RecommendationPreferenceSummaryView("정착 성향", answers.settlementPreference().label()),
			new RecommendationPreferenceSummaryView("장기 기반 기준", answers.futureBasePreference().label())
		);
	}

	private <T extends Enum<T> & RecommendationSurveyAnswers.SurveyOption> List<RecommendationOptionView> optionViews(T[] options) {
		return Arrays.stream(options)
			.map(option -> new RecommendationOptionView(option.name(), option.label(), option.description()))
			.toList();
	}
}
