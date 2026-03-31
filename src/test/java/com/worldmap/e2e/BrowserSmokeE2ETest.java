package com.worldmap.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.worldmap.game.capital.application.CapitalGameService;
import com.worldmap.game.capital.domain.CapitalGameSessionRepository;
import com.worldmap.game.capital.domain.CapitalGameStage;
import com.worldmap.game.capital.domain.CapitalGameStageRepository;
import com.worldmap.game.common.application.GameSessionAccessContext;
import com.worldmap.game.population.application.PopulationGameService;
import com.worldmap.game.population.domain.PopulationGameSessionRepository;
import com.worldmap.game.population.domain.PopulationGameStage;
import com.worldmap.game.population.domain.PopulationGameStageRepository;
import com.worldmap.game.populationbattle.application.PopulationBattleGameService;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameSessionRepository;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStage;
import com.worldmap.game.populationbattle.domain.PopulationBattleGameStageRepository;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private CapitalGameStageRepository capitalGameStageRepository;

	@Autowired
	private CapitalGameSessionRepository capitalGameSessionRepository;

	@Autowired
	private CapitalGameService capitalGameService;

	@Autowired
	private PopulationGameStageRepository populationGameStageRepository;

	@Autowired
	private PopulationGameSessionRepository populationGameSessionRepository;

	@Autowired
	private PopulationGameService populationGameService;

	@Autowired
	private PopulationBattleGameStageRepository populationBattleGameStageRepository;

	@Autowired
	private PopulationBattleGameSessionRepository populationBattleGameSessionRepository;

	@Autowired
	private PopulationBattleGameService populationBattleGameService;

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
		startCapitalGameFromBrowser("browser-smoke");

		assertThat(page.getAttribute("body", "data-page")).isEqualTo("capital-play");
		assertThat(page.textContent("#capital-target-country-name").trim()).isNotEqualTo("문제를 불러오는 중...");
		assertThat(page.locator("#capital-options label.option-card").count()).isEqualTo(4);
		assertThat(page.locator("#capital-game-status .stat-card").count()).isGreaterThan(0);
	}

	@Test
	void capitalGameOverModalSupportsKeyboardTrapAndRestartFocusReturn() {
		startCapitalGameFromBrowser("browser-modal");

		UUID sessionId = currentCapitalSessionId();
		CapitalGameStage firstStage = capitalGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());
		String guestSessionKey = capitalGameSessionRepository.findById(sessionId)
			.orElseThrow()
			.getGuestSessionKey();
		GameSessionAccessContext accessContext = GameSessionAccessContext.forGuest(guestSessionKey);

		capitalGameService.submitAnswer(sessionId, 1, wrongOptionNumber, accessContext);
		capitalGameService.submitAnswer(sessionId, 1, wrongOptionNumber, accessContext);

		page.reload();
		waitForCapitalPlayReady();

		page.locator("#capital-options label.option-card[data-option-number='" + wrongOptionNumber + "']").click();
		page.locator("#capital-submit-button").click();

		page.waitForFunction("() => !document.getElementById('capital-game-over-modal').hidden");

		assertThat(page.evaluate("() => document.activeElement?.id")).isEqualTo("capital-restart-button");
		assertThat(page.evaluate("() => document.querySelector('.page-shell')?.inert")).isEqualTo(true);

		page.keyboard().press("Tab");
		assertThat(page.evaluate("() => document.activeElement?.getAttribute('href')")).isEqualTo("/");

		page.keyboard().press("Shift+Tab");
		assertThat(page.evaluate("() => document.activeElement?.id")).isEqualTo("capital-restart-button");

		page.keyboard().press("Tab");
		assertThat(page.evaluate("() => document.activeElement?.getAttribute('href')")).isEqualTo("/");

		page.keyboard().press("Escape");
		assertThat(page.evaluate("() => !document.getElementById('capital-game-over-modal').hidden")).isEqualTo(true);
		assertThat(page.evaluate("() => document.activeElement?.id")).isEqualTo("capital-restart-button");

		page.locator("#capital-restart-button").click();
		page.waitForFunction(
			"() => document.getElementById('capital-game-over-modal').hidden "
				+ "&& document.activeElement?.matches('#capital-options input[name=\"capital-option\"]')"
		);

		assertThat(page.evaluate("() => document.querySelector('.page-shell')?.inert")).isEqualTo(false);
		assertThat(page.evaluate("() => document.activeElement?.getAttribute('name')")).isEqualTo("capital-option");
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
	void populationGameOverModalSupportsKeyboardTrapAndRestartFocusReturn() {
		startPopulationGameFromBrowser("browser-population-modal");

		UUID sessionId = currentPopulationSessionId();
		PopulationGameStage firstStage = populationGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());
		String guestSessionKey = populationGameSessionRepository.findById(sessionId)
			.orElseThrow()
			.getGuestSessionKey();
		GameSessionAccessContext accessContext = GameSessionAccessContext.forGuest(guestSessionKey);

		populationGameService.submitAnswer(sessionId, 1, wrongOptionNumber, accessContext);
		populationGameService.submitAnswer(sessionId, 1, wrongOptionNumber, accessContext);

		page.reload();
		waitForPopulationPlayReady();

		page.locator("#population-options label.option-card[data-option-number='" + wrongOptionNumber + "']").click();
		page.locator("#population-submit-button").click();

		page.waitForFunction("() => !document.getElementById('population-game-over-modal').hidden");

		assertThat(page.evaluate("() => document.activeElement?.id")).isEqualTo("population-restart-button");
		assertThat(page.evaluate("() => document.querySelector('.page-shell')?.inert")).isEqualTo(true);

		page.keyboard().press("Tab");
		assertThat(page.evaluate("() => document.activeElement?.getAttribute('href')")).isEqualTo("/");

		page.keyboard().press("Shift+Tab");
		assertThat(page.evaluate("() => document.activeElement?.id")).isEqualTo("population-restart-button");

		page.keyboard().press("Tab");
		assertThat(page.evaluate("() => document.activeElement?.getAttribute('href')")).isEqualTo("/");

		page.keyboard().press("Escape");
		assertThat(page.evaluate("() => !document.getElementById('population-game-over-modal').hidden")).isEqualTo(true);
		assertThat(page.evaluate("() => document.activeElement?.id")).isEqualTo("population-restart-button");

		page.locator("#population-restart-button").click();
		page.waitForFunction(
			"() => document.getElementById('population-game-over-modal').hidden "
				+ "&& document.activeElement?.matches('#population-options input[name=\"population-option\"]')"
		);

		assertThat(page.evaluate("() => document.querySelector('.page-shell')?.inert")).isEqualTo(false);
		assertThat(page.evaluate("() => document.activeElement?.getAttribute('name')")).isEqualTo("population-option");
	}

	@Test
	void populationBattleGameOverModalSupportsKeyboardTrapAndRestartFocusReturn() {
		startPopulationBattleGameFromBrowser("browser-battle-modal");

		UUID sessionId = currentPopulationBattleSessionId();
		PopulationBattleGameStage firstStage = populationBattleGameStageRepository.findBySessionIdAndStageNumber(sessionId, 1)
			.orElseThrow();
		int wrongOptionNumber = findWrongOptionNumber(firstStage.getCorrectOptionNumber());
		String guestSessionKey = populationBattleGameSessionRepository.findById(sessionId)
			.orElseThrow()
			.getGuestSessionKey();
		GameSessionAccessContext accessContext = GameSessionAccessContext.forGuest(guestSessionKey);

		populationBattleGameService.submitAnswer(sessionId, 1, wrongOptionNumber, accessContext);
		populationBattleGameService.submitAnswer(sessionId, 1, wrongOptionNumber, accessContext);

		page.reload();
		waitForPopulationBattlePlayReady();

		page.locator("#population-battle-options label.option-card[data-option-number='" + wrongOptionNumber + "']").click();
		page.locator("#population-battle-submit-button").click();

		page.waitForFunction("() => !document.getElementById('population-battle-game-over-modal').hidden");

		assertThat(page.evaluate("() => document.activeElement?.id")).isEqualTo("population-battle-restart-button");
		assertThat(page.evaluate("() => document.querySelector('.page-shell')?.inert")).isEqualTo(true);

		page.keyboard().press("Tab");
		assertThat(page.evaluate("() => document.activeElement?.getAttribute('href')")).isEqualTo("/");

		page.keyboard().press("Shift+Tab");
		assertThat(page.evaluate("() => document.activeElement?.id")).isEqualTo("population-battle-restart-button");

		page.keyboard().press("Tab");
		assertThat(page.evaluate("() => document.activeElement?.getAttribute('href')")).isEqualTo("/");

		page.keyboard().press("Escape");
		assertThat(page.evaluate("() => !document.getElementById('population-battle-game-over-modal').hidden")).isEqualTo(true);
		assertThat(page.evaluate("() => document.activeElement?.id")).isEqualTo("population-battle-restart-button");

		page.locator("#population-battle-restart-button").click();
		page.waitForFunction(
			"() => document.getElementById('population-battle-game-over-modal').hidden "
				+ "&& document.activeElement?.matches('#population-battle-options input[name=\"population-battle-option\"]')"
		);

		assertThat(page.evaluate("() => document.querySelector('.page-shell')?.inert")).isEqualTo(false);
		assertThat(page.evaluate("() => document.activeElement?.getAttribute('name')")).isEqualTo("population-battle-option");
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

	private void startCapitalGameFromBrowser(String nickname) {
		page.navigate(baseUrl() + "/games/capital/start");

		assertThat(page.locator("h1").textContent().trim()).isEqualTo("수도 맞히기");

		page.locator("#capital-nickname").fill(nickname);
		page.locator("#capital-start-submit").click();
		waitForCapitalPlayReady();
	}

	private void waitForCapitalPlayReady() {
		page.waitForURL("**/games/capital/play/*");
		page.waitForFunction(
			"() => document.body.dataset.page === 'capital-play' "
				+ "&& document.getElementById('capital-target-country-name') "
				+ "&& document.getElementById('capital-target-country-name').textContent !== '문제를 불러오는 중...'"
		);
		page.waitForFunction("() => document.querySelectorAll('#capital-game-status .stat-card').length > 0");
	}

	private void startPopulationBattleGameFromBrowser(String nickname) {
		page.navigate(baseUrl() + "/games/population-battle/start");

		assertThat(page.locator("h1").textContent().trim()).isEqualTo("인구 비교 퀵 배틀");

		page.locator("#population-battle-nickname").fill(nickname);
		page.locator("#population-battle-start-submit").click();
		waitForPopulationBattlePlayReady();
	}

	private void startPopulationGameFromBrowser(String nickname) {
		page.navigate(baseUrl() + "/games/population/start");

		assertThat(page.locator("h1").textContent().trim()).isEqualTo("국가 인구수 맞추기");

		page.locator("#population-nickname").fill(nickname);
		page.locator("#population-start-submit").click();
		waitForPopulationPlayReady();
	}

	private void waitForPopulationPlayReady() {
		page.waitForURL("**/games/population/play/*");
		page.waitForFunction(
			"() => document.body.dataset.page === 'population-play' "
				+ "&& document.getElementById('population-target-country-name') "
				+ "&& document.getElementById('population-target-country-name').textContent !== '문제를 불러오는 중...'"
		);
		page.waitForFunction("() => document.querySelectorAll('#population-game-status .stat-card').length > 0");
	}

	private void waitForPopulationBattlePlayReady() {
		page.waitForURL("**/games/population-battle/play/*");
		page.waitForFunction(
			"() => document.body.dataset.page === 'population-battle-play' "
				+ "&& document.getElementById('population-battle-stage-copy') "
				+ "&& document.getElementById('population-battle-stage-copy').textContent !== '좌우 보기 두 개를 불러오는 중입니다.'"
		);
		page.waitForFunction("() => document.querySelectorAll('#population-battle-game-status .stat-card').length > 0");
	}

	private UUID currentCapitalSessionId() {
		String sessionId = page.url().substring(page.url().lastIndexOf('/') + 1);
		return UUID.fromString(sessionId);
	}

	private UUID currentPopulationBattleSessionId() {
		String sessionId = page.url().substring(page.url().lastIndexOf('/') + 1);
		return UUID.fromString(sessionId);
	}

	private UUID currentPopulationSessionId() {
		String sessionId = page.url().substring(page.url().lastIndexOf('/') + 1);
		return UUID.fromString(sessionId);
	}

	private int findWrongOptionNumber(int correctOptionNumber) {
		return correctOptionNumber == 1 ? 2 : 1;
	}
}
