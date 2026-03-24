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
				"3D Globe",
				"지구본을 돌려가며 목표 국가를 찾아보는 탐색형 미션입니다.",
				"Mission",
				"/games/location/start"
			),
			new ModeCardView(
				"국가 인구수 맞추기",
				"4 Choices",
				"국가 이름을 보고 가장 가까운 인구 규모를 골라 맞히는 퀴즈입니다.",
				"Quiz",
				"/games/population/start"
			),
			new ModeCardView(
				"어울리는 나라 추천",
				"12 Questions",
				"생활 조건 사이의 우선순위를 고르면 지금 나와 잘 맞는 나라 3곳을 골라 보여줍니다.",
				"Discover",
				"/recommendation/survey"
			),
			new ModeCardView(
				"실시간 랭킹",
				"Top Scores",
				"방금 끝난 플레이 결과를 기준으로 전체 기록과 오늘의 상위 점수를 확인합니다.",
				"Live",
				"/ranking"
			)
		);
	}

	private List<String> principles() {
		return List.of(
			"위치 미션은 지구본에서 나라를 고르고, 제출 순간에만 정답이 공개됩니다.",
			"인구 퀴즈는 하트가 남아 있는 동안 계속 이어지는 아케이드 방식으로 진행됩니다.",
			"나라 추천은 생활 조건과 우선순위를 묻는 12문항에 답하면 바로 top 3 결과를 확인할 수 있습니다.",
			"랭킹은 전체 기록과 오늘의 기록을 나눠서 볼 수 있습니다."
		);
	}

	private List<String> roadmap() {
		return List.of(
			"처음이면 위치 미션으로 지구본 조작에 익숙해지기",
			"그다음 인구 퀴즈로 리듬감 있는 연속 플레이 즐기기",
			"설문으로 내 취향과 잘 맞는 나라 3곳 찾기",
			"랭킹에서 오늘의 상위 기록과 전체 기록 비교하기"
		);
	}
}
