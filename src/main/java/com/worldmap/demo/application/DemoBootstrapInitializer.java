package com.worldmap.demo.application;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@Order(30)
public class DemoBootstrapInitializer implements ApplicationRunner {

	private final DemoBootstrapService demoBootstrapService;

	public DemoBootstrapInitializer(DemoBootstrapService demoBootstrapService) {
		this.demoBootstrapService = demoBootstrapService;
	}

	@Override
	public void run(ApplicationArguments args) {
		demoBootstrapService.ensureLocalDemoData();
	}
}
