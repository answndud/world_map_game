package com.worldmap.admin.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
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
