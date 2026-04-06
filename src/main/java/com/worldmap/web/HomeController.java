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
		return "home";
	}

	private List<ModeCardView> modeCards() {
		return List.of(
			new ModeCardView(
				"arcade",
				"국가 위치 찾기",
				"",
				"지구본에서 나라를 찾습니다.",
				"러너",
				"/games/location/start"
			),
			new ModeCardView(
				"quiz",
				"수도 퀴즈",
				"",
				"국가를 보고 수도를 고릅니다.",
				"퀴즈",
				"/games/capital/start"
			),
			new ModeCardView(
				"quiz",
				"국기 퀴즈",
				"",
				"국기를 보고 나라를 고릅니다.",
				"퀴즈",
				"/games/flag/start"
			),
			new ModeCardView(
				"arcade",
				"인구 비교 배틀",
				"",
				"더 인구가 많은 나라를 고릅니다.",
				"러너",
				"/games/population-battle/start"
			),
			new ModeCardView(
				"arcade",
				"인구수 퀴즈",
				"",
				"가까운 인구 구간을 고릅니다.",
				"러너",
				"/games/population/start"
			),
			new ModeCardView(
				"discover",
				"국가 추천",
				"",
				"생활 조건을 바탕으로 3개 국가를 추천합니다.",
				"추천",
				"/recommendation/survey"
			)
		);
	}
}
