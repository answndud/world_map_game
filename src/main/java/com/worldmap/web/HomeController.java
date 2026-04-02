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
				"arcade",
				"국가 위치 찾기",
				"3D Globe",
				"지구본에서 목표 국가를 찾는 탐색형 게임입니다.",
				"Mission",
				"/games/location/start"
			),
			new ModeCardView(
				"quiz",
				"수도 맞히기",
				"Capital Quiz",
				"국가 이름을 보고 수도를 고르는 4지선다입니다.",
				"Quiz",
				"/games/capital/start"
			),
			new ModeCardView(
				"quiz",
				"국기 보고 나라 맞히기",
				"Flag Quiz",
				"국기를 보고 나라를 고르는 4지선다입니다.",
				"Quiz",
				"/games/flag/start"
			),
			new ModeCardView(
				"arcade",
				"인구 비교 퀵 배틀",
				"Population Battle",
				"두 나라 중 인구가 더 많은 쪽을 고르는 퀵 배틀입니다.",
				"Battle",
				"/games/population-battle/start"
			),
			new ModeCardView(
				"arcade",
				"국가 인구수 맞추기",
				"4 Choices",
				"가장 가까운 인구 규모를 고르는 퀴즈입니다.",
				"Quiz",
				"/games/population/start"
			),
			new ModeCardView(
				"discover",
				"나에게 어울리는 국가 찾기",
				"20 Questions",
				"생활 조건 우선순위를 고르면 어울리는 국가 3곳을 추천합니다.",
				"Discover",
				"/recommendation/survey"
			)
		);
	}

	private List<String> entrySteps() {
		return List.of(
			"게임 하나를 고른다.",
			"게스트로 바로 플레이하고 결과와 랭킹을 본다.",
			"기록을 남기고 싶으면 로그인해 현재 브라우저 기록을 연결한다.",
			"Stats와 My Page에서 전체 흐름과 내 기록을 확인한다."
		);
	}

	private List<String> accountNotes() {
		return List.of(
			"게스트 기록은 현재 브라우저에 유지됩니다.",
			"로그인하면 방금 플레이한 기록을 계정으로 이어받습니다.",
			"My Page에서 최고 점수와 최근 플레이를 다시 볼 수 있습니다.",
			"Stats에서는 오늘 활동과 상위 기록만 공개합니다."
		);
	}
}
