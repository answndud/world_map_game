package com.worldmap.recommendation.application;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecommendationCountryProfileCatalog {

	public List<RecommendationCountryProfile> profiles() {
		return List.of(
			new RecommendationCountryProfile("CAN", 2, 5, 2, 4, 3, 5, 5, 5, 3, 5, 4, 5, 3, 4, "영어 친화도와 복지, 자연 접근성이 균형 잡힌 선택지입니다."),
			new RecommendationCountryProfile("USA", 3, 4, 5, 4, 5, 5, 4, 3, 4, 5, 4, 5, 5, 4, "속도감 있는 대도시 생활과 문화 다양성을 강하게 원하는 경우 선택지가 됩니다."),
			new RecommendationCountryProfile("AUS", 4, 3, 3, 4, 3, 5, 4, 4, 4, 4, 4, 5, 4, 4, "따뜻한 기후와 영어 환경, 활동적인 라이프스타일이 강점입니다."),
			new RecommendationCountryProfile("NZL", 2, 4, 1, 4, 2, 5, 5, 4, 3, 3, 5, 4, 2, 4, "자연 중심의 여유 있는 생활과 영어 환경을 동시에 원할 때 잘 맞습니다."),
			new RecommendationCountryProfile("SGP", 5, 1, 5, 5, 5, 5, 5, 4, 5, 5, 1, 5, 5, 4, "도시 속도, 영어, 안전, 음식 만족도를 한 번에 가져가는 초도시형 프로필입니다."),
			new RecommendationCountryProfile("JPN", 3, 4, 4, 4, 4, 2, 5, 4, 5, 3, 2, 5, 5, 2, "안전성과 도시 편의, 음식 만족도가 높은 동아시아형 프로필입니다."),
			new RecommendationCountryProfile("GBR", 2, 4, 4, 4, 5, 5, 4, 4, 4, 5, 2, 5, 5, 4, "영어권 대도시 접근성과 문화 다양성, 글로벌 연결성이 강한 프로필입니다."),
			new RecommendationCountryProfile("IRL", 2, 4, 3, 4, 3, 5, 5, 4, 3, 4, 3, 4, 3, 4, "영어 적응 난도가 낮고 차분한 도시 생활을 기대할 때 무난한 선택지입니다."),
			new RecommendationCountryProfile("DEU", 2, 4, 3, 4, 4, 4, 4, 5, 3, 4, 3, 4, 4, 3, "복지와 산업 기반, 균형 잡힌 도시 생활을 원할 때 강한 선택지입니다."),
			new RecommendationCountryProfile("FRA", 3, 4, 3, 4, 4, 3, 4, 4, 5, 4, 2, 4, 5, 3, "문화 자산과 음식, 도시 생활의 균형을 중시할 때 매력이 있습니다."),
			new RecommendationCountryProfile("ITA", 4, 3, 2, 3, 3, 2, 4, 3, 5, 3, 3, 3, 5, 3, "음식 만족도와 따뜻한 분위기, 여유 있는 생활 리듬을 좋아할 때 잘 맞습니다."),
			new RecommendationCountryProfile("SWE", 1, 5, 2, 5, 3, 5, 5, 5, 2, 4, 4, 5, 3, 4, "복지와 영어, 차분한 북유럽 라이프스타일이 핵심입니다."),
			new RecommendationCountryProfile("DNK", 1, 5, 2, 5, 3, 5, 5, 5, 3, 4, 4, 5, 3, 4, "복지와 영어 접근성, 안정적인 생활 리듬이 강점인 북유럽형 선택지입니다."),
			new RecommendationCountryProfile("NOR", 1, 5, 2, 5, 2, 5, 5, 5, 3, 3, 5, 5, 2, 4, "자연과 안전, 높은 생활 안정성을 중시할 때 강한 북유럽 후보입니다."),
			new RecommendationCountryProfile("FIN", 1, 5, 2, 4, 2, 5, 5, 5, 2, 3, 4, 5, 3, 4, "조용한 생활 리듬과 안전, 복지 기반을 우선시할 때 잘 맞습니다."),
			new RecommendationCountryProfile("ESP", 4, 3, 2, 3, 3, 3, 4, 4, 5, 4, 3, 4, 5, 4, "따뜻한 기후와 음식, 여유 있는 리듬을 함께 보는 사람에게 잘 맞습니다."),
			new RecommendationCountryProfile("PRT", 4, 3, 2, 3, 2, 4, 4, 4, 4, 3, 4, 4, 4, 4, "온화한 기후와 비교적 부드러운 생활 속도를 좋아할 때 안정적인 선택지입니다."),
			new RecommendationCountryProfile("CHE", 2, 4, 3, 5, 3, 4, 5, 5, 3, 4, 4, 5, 4, 3, "생활비는 높지만 안전과 복지, 안정성을 중시할 때 강한 선택지입니다."),
			new RecommendationCountryProfile("AUT", 2, 4, 2, 4, 3, 4, 5, 5, 3, 3, 4, 4, 4, 3, "도시 편의와 차분한 생활 리듬, 높은 안전성을 함께 보고 싶을 때 어울립니다."),
			new RecommendationCountryProfile("NLD", 2, 4, 4, 4, 4, 5, 4, 4, 3, 5, 2, 5, 5, 4, "영어 접근성과 도시 연결성, 문화 다양성이 강점인 서유럽형 프로필입니다."),
			new RecommendationCountryProfile("KOR", 3, 4, 5, 3, 5, 2, 4, 4, 5, 3, 2, 5, 5, 2, "빠른 도시 리듬과 음식 만족도, 디지털 인프라를 중시할 때 매력이 큽니다."),
			new RecommendationCountryProfile("ARE", 5, 1, 5, 5, 5, 5, 4, 3, 4, 5, 2, 5, 4, 4, "고속 도시 환경과 글로벌 비즈니스 감각, 영어 사용 환경이 강한 선택지입니다."),
			new RecommendationCountryProfile("THA", 5, 2, 3, 2, 4, 3, 3, 2, 5, 5, 4, 3, 4, 4, "따뜻한 기후와 음식, 비교적 가벼운 생활비, 관광·문화 다양성이 강점입니다."),
			new RecommendationCountryProfile("MYS", 5, 2, 3, 2, 4, 4, 4, 3, 5, 5, 4, 4, 4, 4, "영어 적응 난도와 물가, 다문화 도시 환경의 균형이 좋은 동남아형 프로필입니다."),
			new RecommendationCountryProfile("VNM", 5, 2, 4, 1, 4, 2, 3, 2, 5, 4, 3, 4, 4, 3, "빠른 도시 에너지와 낮은 생활비, 음식 만족도를 함께 보고 싶을 때 눈에 띕니다."),
			new RecommendationCountryProfile("CHL", 3, 4, 3, 3, 4, 3, 4, 3, 4, 3, 4, 4, 3, 3, "남미 안에서 비교적 안정적인 도시 생활과 기후 균형을 찾을 때 볼 수 있습니다."),
			new RecommendationCountryProfile("URY", 3, 4, 2, 3, 3, 3, 5, 4, 4, 2, 4, 4, 3, 3, "느긋한 생활 리듬과 남미권 안정성, 복지 지향성을 중시할 때 맞습니다."),
			new RecommendationCountryProfile("BRA", 5, 2, 4, 2, 5, 2, 2, 2, 5, 5, 4, 3, 5, 3, "뜨거운 기후와 강한 도시 에너지, 음식과 문화 다양성을 동시에 원하는 경우 후보가 됩니다."),
			new RecommendationCountryProfile("MEX", 4, 3, 4, 2, 5, 2, 2, 2, 5, 5, 3, 3, 5, 3, "활기 있는 대도시와 풍부한 음식 문화, 비교적 유연한 생활비 감각이 강점입니다."),
			new RecommendationCountryProfile("ZAF", 4, 3, 4, 2, 4, 4, 2, 2, 4, 5, 4, 4, 4, 3, "영어 환경과 자연 접근성, 문화적 다양성을 함께 보고 싶을 때 고려할 수 있습니다.")
		);
	}
}
