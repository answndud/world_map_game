package com.worldmap.admin.web;

import com.worldmap.auth.application.MemberSessionManager;
import com.worldmap.auth.domain.MemberRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UriUtils;

@Component
public class AdminAccessInterceptor implements HandlerInterceptor {

	private final MemberSessionManager memberSessionManager;

	public AdminAccessInterceptor(MemberSessionManager memberSessionManager) {
		this.memberSessionManager = memberSessionManager;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		HttpSession httpSession = request.getSession(false);
		if (httpSession == null) {
			redirectToLogin(request, response);
			return false;
		}

		var currentMember = memberSessionManager.currentMember(httpSession);
		if (currentMember.isEmpty()) {
			redirectToLogin(request, response);
			return false;
		}

		if (currentMember.get().role() != MemberRole.ADMIN) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return false;
		}

		return true;
	}

	private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String returnTo = request.getRequestURI();
		String encodedReturnTo = UriUtils.encodeQueryParam(returnTo, StandardCharsets.UTF_8);
		response.sendRedirect("/login?returnTo=" + encodedReturnTo);
	}
}
