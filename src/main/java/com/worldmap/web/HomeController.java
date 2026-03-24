package com.worldmap.web;

import com.worldmap.web.view.ModeCardView;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("modeCards", modeCards());
		model.addAttribute("principles", principles());
		model.addAttribute("roadmap", roadmap());
		return "home";
	}

	private List<ModeCardView> modeCards() {
		return List.of(
			new ModeCardView(
				"국가 위치 찾기",
				"Level 1~2",
				"3D 지구본에서 국가를 클릭하면 서버가 정답 국가와 비교해 판정하는 첫 번째 플레이 가능 모드입니다.",
				"Playable",
				"/games/location/start"
			),
			new ModeCardView(
				"국가 인구수 맞추기",
				"Level 1~2",
				"보기형 Level 1에서 시작해 이후 수치 입력형 Level 2로 확장할 수 있게 세션 구조를 재사용합니다.",
				"Playable",
				"/games/population/start"
			),
			new ModeCardView(
				"어울리는 나라 추천",
				"Survey + Eval",
				"설문 답변을 서버가 가중치로 점수화해 상위 3개 국가를 계산하고, 만족도와 오프라인 평가 시나리오로 설문을 계속 개선합니다.",
				"Prototype",
				"/recommendation/survey"
			),
			new ModeCardView(
				"실시간 랭킹",
				"Redis Sorted Set",
				"게임오버 시점 결과를 RDB와 Redis Sorted Set에 함께 반영하고, 전체/일간 랭킹을 조회합니다.",
				"Live",
				"/ranking"
			)
		);
	}

	private List<String> principles() {
		return List.of(
			"게임 세션, 라운드, 점수, 정답 판정은 서버가 관리한다.",
			"프론트는 SSR(Thymeleaf)과 바닐라 JavaScript로 최소한의 상호작용만 담당한다.",
			"Redis는 단순 캐시가 아니라 랭킹 자료구조로 사용한다.",
			"AI는 서비스 런타임이 아니라 설문 개선과 평가 시나리오 생성에만 사용하고, 결정 로직은 서버가 가진다."
		);
	}

	private List<String> roadmap() {
		return List.of(
			"Spring Boot 3 기반 프로젝트 뼈대와 SSR 홈 화면 구성",
			"국가 시드 데이터와 country 도메인 추가 완료",
			"위치 찾기 게임 Level 1 세션/라운드 흐름 구현 완료",
			"인구수 맞추기 게임 Level 1 구현 완료",
			"Redis 기반 전체/일간 랭킹 1차 구현",
			"설문 기반 추천 엔진 1차 deterministic 계산 흐름 구현"
		);
	}
}
