package com.worldmap.web;

import com.worldmap.auth.application.AuthenticatedMemberSession;
import com.worldmap.mypage.application.MyPageService;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyPageController {

	private final MyPageService myPageService;

	public MyPageController(MyPageService myPageService) {
		this.myPageService = myPageService;
	}

	@GetMapping("/mypage")
	public String myPage(Model model) {
		AuthenticatedMemberSession currentMember = (AuthenticatedMemberSession) model.getAttribute("currentMember");
		if (currentMember != null) {
			model.addAttribute("dashboard", myPageService.loadDashboard(currentMember.memberId()));
		}
		return "mypage";
	}
}
