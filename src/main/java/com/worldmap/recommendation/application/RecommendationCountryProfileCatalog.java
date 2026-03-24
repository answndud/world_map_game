package com.worldmap.recommendation.application;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RecommendationCountryProfileCatalog {

	public List<RecommendationCountryProfile> profiles() {
		return List.of(
			new RecommendationCountryProfile("CAN", 2, 2, 4, 3, 5, 5, 5, 3, 5, "영어 친화도와 복지, 자연 접근성이 균형 잡힌 선택지입니다."),
			new RecommendationCountryProfile("AUS", 4, 3, 4, 3, 5, 4, 4, 4, 4, "따뜻한 기후와 영어 환경, 활동적인 라이프스타일이 강점입니다."),
			new RecommendationCountryProfile("NZL", 2, 1, 4, 2, 5, 5, 4, 3, 3, "자연 중심의 여유 있는 생활과 영어 환경을 동시에 원할 때 잘 맞습니다."),
			new RecommendationCountryProfile("SGP", 5, 5, 5, 5, 5, 5, 4, 5, 5, "도시 속도, 영어, 안전, 음식 만족도를 한 번에 가져가는 초도시형 프로필입니다."),
			new RecommendationCountryProfile("JPN", 3, 4, 4, 4, 2, 5, 4, 5, 3, "안전성과 도시 편의, 음식 만족도가 높은 동아시아형 프로필입니다."),
			new RecommendationCountryProfile("DEU", 2, 3, 4, 4, 4, 4, 5, 3, 4, "복지와 산업 기반, 균형 잡힌 도시 생활을 원할 때 강한 선택지입니다."),
			new RecommendationCountryProfile("SWE", 1, 2, 5, 3, 5, 5, 5, 2, 4, "복지와 영어, 차분한 북유럽 라이프스타일이 핵심입니다."),
			new RecommendationCountryProfile("ESP", 4, 2, 3, 3, 3, 4, 4, 5, 4, "따뜻한 기후와 음식, 여유 있는 리듬을 함께 보는 사람에게 잘 맞습니다."),
			new RecommendationCountryProfile("PRT", 4, 2, 3, 2, 4, 4, 4, 4, 3, "온화한 기후와 비교적 부드러운 생활 속도를 좋아할 때 안정적인 선택지입니다."),
			new RecommendationCountryProfile("CHE", 2, 3, 5, 3, 4, 5, 5, 3, 4, "생활비는 높지만 안전과 복지, 안정성을 중시할 때 강한 선택지입니다."),
			new RecommendationCountryProfile("NLD", 2, 4, 4, 4, 5, 4, 4, 3, 5, "영어 접근성과 도시 연결성, 문화 다양성이 강점인 서유럽형 프로필입니다."),
			new RecommendationCountryProfile("KOR", 3, 5, 3, 5, 2, 4, 4, 5, 3, "빠른 도시 리듬과 음식 만족도, 디지털 인프라를 중시할 때 매력이 큽니다.")
		);
	}
}
