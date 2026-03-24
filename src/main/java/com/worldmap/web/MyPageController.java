package com.worldmap.web;

import com.worldmap.auth.application.MemberSessionManager;
import com.worldmap.mypage.application.MyPageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyPageController {

	private final MemberSessionManager memberSessionManager;
	private final MyPageService myPageService;

	public MyPageController(MemberSessionManager memberSessionManager, MyPageService myPageService) {
		this.memberSessionManager = memberSessionManager;
		this.myPageService = myPageService;
	}

	@GetMapping("/mypage")
	public String myPage(HttpSession httpSession, Model model) {
		var currentMember = memberSessionManager.currentMember(httpSession);
		currentMember.ifPresent(member -> {
			model.addAttribute("currentMember", member);
			model.addAttribute("dashboard", myPageService.loadDashboard(member.memberId()));
		});
		model.addAttribute("isAuthenticated", currentMember.isPresent());
		return "mypage";
	}
}
