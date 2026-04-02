package com.worldmap.web;

import com.worldmap.auth.application.AuthenticatedMemberSession;
import com.worldmap.auth.application.CurrentMemberAccessService;
import com.worldmap.auth.domain.MemberRole;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SiteHeaderModelAdvice {

	private final CurrentMemberAccessService currentMemberAccessService;

	public SiteHeaderModelAdvice(CurrentMemberAccessService currentMemberAccessService) {
		this.currentMemberAccessService = currentMemberAccessService;
	}

	@ModelAttribute
	public void populateCurrentMember(HttpServletRequest request, Model model) {
		AuthenticatedMemberSession currentMember = currentMemberAccessService.currentMember(request)
			.orElse(null);
		model.addAttribute("currentMember", currentMember);
		model.addAttribute("isAuthenticated", currentMember != null);
		model.addAttribute("authenticatedNickname", currentMember != null ? currentMember.nickname() : null);
		model.addAttribute("showDashboardLink", currentMember != null && currentMember.role() == MemberRole.ADMIN);
	}
}
