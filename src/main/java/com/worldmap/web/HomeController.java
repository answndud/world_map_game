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
		model.addAttribute("entrySteps", entrySteps());
		model.addAttribute("accountNotes", accountNotes());
		return "home";
	}

	private List<ModeCardView> modeCards() {
		return List.of(
			new ModeCardView(
				"국가 위치 찾기",
				"3D Globe",
				"지구본을 돌려가며 목표 국가를 찾아보는 탐색형 미션입니다.",
				"Mission",
				"/games/location/start"
			),
			new ModeCardView(
				"수도 맞히기",
				"Capital Quiz",
				"국가 이름을 보고 4개 수도 보기 중 정답을 고르는 퀴즈입니다.",
				"Quiz",
				"/games/capital/start"
			),
			new ModeCardView(
				"국가 인구수 맞추기",
				"4 Choices",
				"국가 이름을 보고 가장 가까운 인구 규모를 골라 맞히는 퀴즈입니다.",
				"Quiz",
				"/games/population/start"
			),
			new ModeCardView(
				"나에게 어울리는 국가 찾기",
				"20 Questions",
				"생활 조건 사이의 우선순위를 고르면 지금 나와 잘 맞는 국가 3곳을 골라 보여줍니다.",
				"Discover",
				"/recommendation/survey"
			)
		);
	}

	private List<String> entrySteps() {
		return List.of(
			"아래 카드에서 지금 해보고 싶은 게임을 하나 고른다.",
			"게스트로 바로 시작하고, 한 판이 끝나면 결과와 랭킹을 확인한다.",
			"기록을 이어가고 싶으면 로그인해 현재 브라우저 기록을 계정에 연결한다.",
			"Stats와 My Page에서 서비스 흐름과 내 기록을 함께 살펴본다."
		);
	}

	private List<String> accountNotes() {
		return List.of(
			"게스트는 현재 브라우저 세션 기준으로 점수와 진행 기록을 유지한다.",
			"회원가입이나 로그인 후에는 방금까지의 게스트 기록을 내 계정으로 이어받을 수 있다.",
			"My Page에서는 최고 점수, 최근 플레이, 게임별 플레이 성향을 다시 확인할 수 있다.",
			"공개 Stats 페이지에서는 오늘 활성 플레이와 상위 기록 흐름을 가볍게 볼 수 있다."
		);
	}
}
