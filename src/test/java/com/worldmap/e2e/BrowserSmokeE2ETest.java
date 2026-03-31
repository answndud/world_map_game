package com.worldmap.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "browser-smoke"})
@Tag("browser-smoke")
class BrowserSmokeE2ETest {

	private static Playwright playwright;
	private static Browser browser;

	@LocalServerPort
	private int port;

	private BrowserContext browserContext;

	private Page page;

	@BeforeAll
	static void launchBrowser() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
			.setChannel("chromium")
			.setHeadless(true));
	}

	@AfterAll
	static void closeBrowser() {
		if (browser != null) {
			browser.close();
		}
		if (playwright != null) {
			playwright.close();
		}
	}

	@BeforeEach
	void openBrowserContext() {
		browserContext = browser.newContext(new Browser.NewContextOptions()
			.setViewportSize(1440, 1024));
		page = browserContext.newPage();
	}

	@AfterEach
	void closeBrowserContext() {
		if (browserContext != null) {
			browserContext.close();
		}
	}

	@Test
	void homePageRendersExpectedShellInRealBrowser() {
		page.navigate(baseUrl() + "/");

		assertThat(page.title()).isEqualTo("WorldMap");
		assertThat(page.evaluate("() => document.documentElement.dataset.theme")).isEqualTo("light");
		assertThat(page.textContent("[data-theme-toggle-label]").trim()).isEqualTo("Light");
		assertThat(page.locator("header.site-header").count()).isEqualTo(1);
		assertThat(page.locator("article.mode-card").count()).isEqualTo(6);
		assertThat(page.locator("a.hero-support-link").textContent().trim()).isEqualTo("서비스 현황 보기");
	}

	@Test
	void capitalStartPageCreatesPlayableGuestSessionInRealBrowser() {
		page.navigate(baseUrl() + "/games/capital/start");

		assertThat(page.locator("h1").textContent().trim()).isEqualTo("수도 맞히기");

		page.locator("#capital-nickname").fill("browser-smoke");
		page.locator("#capital-start-submit").click();

		page.waitForURL("**/games/capital/play/*");
		page.waitForFunction(
			"() => document.body.dataset.page === 'capital-play' "
				+ "&& document.getElementById('capital-target-country-name') "
				+ "&& document.getElementById('capital-target-country-name').textContent !== '문제를 불러오는 중...'"
		);
		page.waitForFunction("() => document.querySelectorAll('#capital-game-status .stat-card').length > 0");

		assertThat(page.getAttribute("body", "data-page")).isEqualTo("capital-play");
		assertThat(page.textContent("#capital-target-country-name").trim()).isNotEqualTo("문제를 불러오는 중...");
		assertThat(page.locator("#capital-options label.option-card").count()).isEqualTo(4);
		assertThat(page.locator("#capital-game-status .stat-card").count()).isGreaterThan(0);
	}

	@Test
	void recommendationSurveySubmitsAndRendersTopThreeResultCards() {
		page.navigate(baseUrl() + "/recommendation/survey");

		assertThat(page.locator("h1").textContent().trim()).isEqualTo("나에게 어울리는 국가 찾기");

		Locator questionPanels = page.locator("section.recommendation-question");
		assertThat(questionPanels.count()).isEqualTo(20);

		for (int index = 0; index < questionPanels.count(); index++) {
			questionPanels.nth(index)
				.locator("label.recommendation-option")
				.first()
				.click();
		}

		page.locator("button[type='submit']").click();
		page.waitForFunction("() => document.title === '나에게 어울리는 국가 찾기 결과'");

		assertThat(page.locator("h1").textContent().trim()).isEqualTo("추천 결과");
		assertThat(page.locator("article.recommendation-country-card").count()).isEqualTo(3);
		assertThat(page.locator("#recommendation-feedback-submit").isDisabled()).isTrue();
	}

	@Test
	void rankingPageRendersInRealBrowserWithoutRedis() {
		page.navigate(baseUrl() + "/ranking");

		assertThat(page.title()).isEqualTo("실시간 랭킹");
		assertThat(page.getAttribute("body", "data-page")).isEqualTo("ranking");
		assertThat(page.locator("h1").textContent().trim()).isEqualTo("실시간 랭킹");
		assertThat(page.locator("#ranking-active-title").textContent().trim()).contains("위치 찾기");
		assertThat(page.locator("#ranking-location-all-body").count()).isEqualTo(1);
	}

	@Test
	void statsPageRendersInRealBrowserWithoutRedis() {
		page.navigate(baseUrl() + "/stats");

		assertThat(page.title()).isEqualTo("Live Stats");
		assertThat(page.locator("h1").textContent().trim()).isEqualTo("서비스 현황");
		assertThat(page.locator(".stats-grid .stat-card").count()).isGreaterThan(0);
		assertThat(page.locator("a.primary-link").textContent().trim()).isEqualTo("전체 랭킹 보기");
	}

	private String baseUrl() {
		return "http://127.0.0.1:" + port;
	}
}
