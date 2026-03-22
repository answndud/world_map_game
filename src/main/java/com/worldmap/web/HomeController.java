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
				"서버가 라운드를 출제하고 사용자의 지도 클릭 좌표를 정답 반경과 비교합니다.",
				"Planned",
				"/games/location/start"
			),
			new ModeCardView(
				"국가 인구수 맞추기",
				"Level 1~2",
				"보기형 문제와 수치 입력형 문제를 나눠 오차율 기반 점수 계산으로 확장합니다.",
				"Planned",
				"/games/population/start"
			),
			new ModeCardView(
				"어울리는 나라 추천",
				"Survey + LLM",
				"추천 결과는 서버가 계산하고 LLM은 설명만 생성하도록 역할을 분리합니다.",
				"Planned",
				"/recommendation/survey"
			),
			new ModeCardView(
				"실시간 랭킹",
				"Redis Sorted Set",
				"모드별, 레벨별, 일간/전체 랭킹을 빠르게 집계하고 조회하는 구조를 목표로 합니다.",
				"Planned",
				"/ranking"
			)
		);
	}

	private List<String> principles() {
		return List.of(
			"게임 세션, 라운드, 점수, 정답 판정은 서버가 관리한다.",
			"프론트는 SSR(Thymeleaf)과 바닐라 JavaScript로 최소한의 상호작용만 담당한다.",
			"Redis는 단순 캐시가 아니라 랭킹 자료구조로 사용한다.",
			"LLM은 추천 결과의 설명 생성만 담당하고 결정 로직은 서버가 가진다."
		);
	}

	private List<String> roadmap() {
		return List.of(
			"Spring Boot 3 기반 프로젝트 뼈대와 SSR 홈 화면 구성",
			"국가 시드 데이터와 country 도메인 추가",
			"위치 찾기 게임 Level 1 세션/라운드 흐름 구현",
			"인구수 맞추기 게임과 Redis 랭킹 확장"
		);
	}
}
