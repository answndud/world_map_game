package com.worldmap.auth.web;

import com.worldmap.auth.application.GuestSessionKeyManager;
import com.worldmap.auth.application.MemberAuthService;
import com.worldmap.auth.application.MemberSessionManager;
import com.worldmap.auth.domain.Member;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthPageController {

	private final MemberAuthService memberAuthService;
	private final MemberSessionManager memberSessionManager;
	private final GuestSessionKeyManager guestSessionKeyManager;

	public AuthPageController(
		MemberAuthService memberAuthService,
		MemberSessionManager memberSessionManager,
		GuestSessionKeyManager guestSessionKeyManager
	) {
		this.memberAuthService = memberAuthService;
		this.memberSessionManager = memberSessionManager;
		this.guestSessionKeyManager = guestSessionKeyManager;
	}

	@GetMapping("/signup")
	public String signupPage(Model model, HttpSession httpSession) {
		if (memberSessionManager.currentMember(httpSession).isPresent()) {
			return "redirect:/mypage";
		}
		if (!model.containsAttribute("signupForm")) {
			model.addAttribute("signupForm", new SignupForm());
		}
		return "auth/signup";
	}

	@PostMapping("/signup")
	public String signUp(
		@Valid @ModelAttribute("signupForm") SignupForm signupForm,
		BindingResult bindingResult,
		HttpSession httpSession,
		Model model
	) {
		if (bindingResult.hasErrors()) {
			return "auth/signup";
		}

		try {
			Member member = memberAuthService.signUp(signupForm.getNickname(), signupForm.getPassword());
			memberSessionManager.signIn(httpSession, member);
			return "redirect:/mypage";
		} catch (IllegalArgumentException | IllegalStateException ex) {
			model.addAttribute("authErrorMessage", ex.getMessage());
			return "auth/signup";
		}
	}

	@GetMapping("/login")
	public String loginPage(Model model, HttpSession httpSession) {
		if (memberSessionManager.currentMember(httpSession).isPresent()) {
			return "redirect:/mypage";
		}
		if (!model.containsAttribute("loginForm")) {
			model.addAttribute("loginForm", new LoginForm());
		}
		return "auth/login";
	}

	@PostMapping("/login")
	public String login(
		@Valid @ModelAttribute("loginForm") LoginForm loginForm,
		BindingResult bindingResult,
		HttpSession httpSession,
		Model model
	) {
		if (bindingResult.hasErrors()) {
			return "auth/login";
		}

		try {
			Member member = memberAuthService.login(loginForm.getNickname(), loginForm.getPassword());
			memberSessionManager.signIn(httpSession, member);
			return "redirect:/mypage";
		} catch (IllegalArgumentException | IllegalStateException ex) {
			model.addAttribute("authErrorMessage", ex.getMessage());
			return "auth/login";
		}
	}

	@PostMapping("/logout")
	public String logout(HttpSession httpSession) {
		memberSessionManager.signOut(httpSession);
		guestSessionKeyManager.rotateGuestSessionKey(httpSession);
		return "redirect:/mypage";
	}
}
