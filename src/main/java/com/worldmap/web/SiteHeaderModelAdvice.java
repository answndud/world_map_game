package com.worldmap.web;

import com.worldmap.auth.application.AdminAccessGuard;
import com.worldmap.auth.application.AdminAccessGuard.AdminAccessStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SiteHeaderModelAdvice {

	private final AdminAccessGuard adminAccessGuard;

	public SiteHeaderModelAdvice(AdminAccessGuard adminAccessGuard) {
		this.adminAccessGuard = adminAccessGuard;
	}

	@ModelAttribute("showDashboardLink")
	public boolean showDashboardLink(HttpServletRequest request) {
		return adminAccessGuard.authorize(request.getSession(false)) == AdminAccessStatus.ALLOWED;
	}
}
