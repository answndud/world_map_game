package com.worldmap.admin.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class LegacyAdminRedirectController {

	@GetMapping
	public String dashboardRedirect() {
		return "redirect:/dashboard";
	}

	@GetMapping("/recommendation/feedback")
	public String recommendationFeedbackRedirect() {
		return "redirect:/dashboard/recommendation/feedback";
	}

	@GetMapping("/recommendation/persona-baseline")
	public String recommendationPersonaBaselineRedirect() {
		return "redirect:/dashboard/recommendation/persona-baseline";
	}
}
