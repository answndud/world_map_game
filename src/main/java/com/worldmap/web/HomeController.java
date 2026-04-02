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
				"지구본에서 직접 선택",
				"나라 이름이 나오면 지구본에서 그 나라를 바로 찾아 제출합니다.",
				"오래 버티기",
				"/games/location/start"
			),
			new ModeCardView(
				"quiz",
				"수도 맞히기",
				"수도 4지선다",
				"국가를 보고 수도 보기 4개 중 하나를 고릅니다.",
				"짧게 한 판",
				"/games/capital/start"
			),
			new ModeCardView(
				"quiz",
				"국기 보고 나라 맞히기",
				"국기 4지선다",
				"국기 하나를 보고 나라 보기 4개 중 하나를 고릅니다.",
				"짧게 한 판",
				"/games/flag/start"
			),
			new ModeCardView(
				"arcade",
				"인구 비교 퀵 배틀",
				"둘 중 더 큰 인구",
				"두 나라를 보고 인구가 더 많은 쪽을 빠르게 고릅니다.",
				"오래 버티기",
				"/games/population-battle/start"
			),
			new ModeCardView(
				"arcade",
				"국가 인구수 맞추기",
				"인구 규모 4지선다",
				"나라를 보고 가장 가까운 인구 규모 구간을 고릅니다.",
				"오래 버티기",
				"/games/population/start"
			),
			new ModeCardView(
				"discover",
				"나에게 어울리는 국가 찾기",
				"설문 20문항",
				"생활 조건을 고르면 어울리는 국가 3곳을 추천합니다.",
				"추천",
				"/recommendation/survey"
			)
		);
	}

	private List<String> entrySteps() {
		return List.of(
			"먼저 게임 하나를 고른다.",
			"정답이면 바로 다음 문제로 넘어가고, 오답이면 같은 문제를 다시 시도한다.",
			"게임이 끝나면 결과와 랭킹을 바로 확인한다.",
			"기록을 남기고 싶으면 로그인해 현재 브라우저 기록을 계정으로 잇는다."
		);
	}

	private List<String> accountNotes() {
		return List.of(
			"게스트 기록은 현재 브라우저에만 남습니다.",
			"로그인하면 방금 하던 기록만 계정으로 이어받습니다.",
			"My Page에서 최고 점수와 최근 플레이를 다시 봅니다.",
			"Stats에는 오늘 활동량과 상위 기록만 공개합니다."
		);
	}
}
