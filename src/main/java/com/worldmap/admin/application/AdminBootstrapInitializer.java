package com.worldmap.admin.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrapInitializer implements ApplicationRunner {

	private final AdminBootstrapService adminBootstrapService;

	public AdminBootstrapInitializer(AdminBootstrapService adminBootstrapService) {
		this.adminBootstrapService = adminBootstrapService;
	}

	@Override
	public void run(ApplicationArguments args) {
		adminBootstrapService.ensureBootstrapAdmin();
	}
}
