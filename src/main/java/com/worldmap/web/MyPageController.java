package com.worldmap.web;

import com.worldmap.auth.application.MemberSessionManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyPageController {

	private final MemberSessionManager memberSessionManager;

	public MyPageController(MemberSessionManager memberSessionManager) {
		this.memberSessionManager = memberSessionManager;
	}

	@GetMapping("/mypage")
	public String myPage(HttpSession httpSession, Model model) {
		memberSessionManager.currentMember(httpSession)
			.ifPresent(currentMember -> model.addAttribute("currentMember", currentMember));
		model.addAttribute("isAuthenticated", memberSessionManager.currentMember(httpSession).isPresent());
		return "mypage";
	}
}
