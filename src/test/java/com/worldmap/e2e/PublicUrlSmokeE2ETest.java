package com.worldmap.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
@Tag("public-url-smoke")
class PublicUrlSmokeE2ETest {

	private static final Path REPORT_PATH = Path.of(
		"build",
		"reports",
		"public-url-smoke",
		"public-url-smoke.md"
	);

	private static Playwright playwright;
	private static Browser browser;

	@LocalServerPort
	private int port;

	private BrowserContext browserContext;

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
	}

	@AfterEach
	void closeBrowserContext() {
		if (browserContext != null) {
			browserContext.close();
		}
	}

	@Test
	void publicPagesRenderOverConfiguredBaseUrlAndEmitTimingReport() throws IOException {
		String baseUrl = publicBaseUrl();
		List<PublicPageExpectation> expectations = List.of(
			new PublicPageExpectation("/", "WorldMap", "h1", "WorldMap Command"),
			new PublicPageExpectation("/stats", "Live Stats", "h1", "서비스 현황"),
			new PublicPageExpectation("/ranking", "실시간 랭킹", "h1", "실시간 랭킹"),
			new PublicPageExpectation("/login", "로그인", "h1", "로그인"),
			new PublicPageExpectation("/signup", "회원가입", "h1", "회원가입"),
			new PublicPageExpectation("/recommendation/survey", "나에게 어울리는 국가 찾기 설문", "h1", "나에게 어울리는 국가 찾기"),
			new PublicPageExpectation("/games/capital/start", "수도 맞히기 시작", "h1", "수도 맞히기")
		);

		List<PublicPageTiming> timings = new ArrayList<>();
		for (PublicPageExpectation expectation : expectations) {
			timings.add(measurePage(baseUrl, expectation));
		}

		writeReport(baseUrl, timings);

		assertThat(Files.exists(REPORT_PATH)).isTrue();
		assertThat(timings).hasSize(expectations.size());
	}

	private PublicPageTiming measurePage(String baseUrl, PublicPageExpectation expectation) {
		Page page = browserContext.newPage();
		try {
			Response response = page.navigate(
				baseUrl + expectation.path(),
				new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
			);

			assertThat(response)
				.as("response for %s", expectation.path())
				.isNotNull();
			assertThat(response.status())
				.as("http status for %s", expectation.path())
				.isLessThan(400);

			page.waitForSelector(expectation.headingSelector());
			page.waitForLoadState(LoadState.LOAD);

			assertThat(page.title()).isEqualTo(expectation.expectedTitle());
			assertThat(page.locator(expectation.headingSelector()).first().textContent().trim())
				.isEqualTo(expectation.expectedHeading());

			@SuppressWarnings("unchecked")
			Map<String, Number> timingMap = (Map<String, Number>) page.evaluate(
				"""
					() => {
						const entry = performance.getEntriesByType('navigation')[0];
						if (!entry) {
							return null;
						}
						return {
							ttfbMs: Math.round(entry.responseStart),
							domContentLoadedMs: Math.round(entry.domContentLoadedEventEnd),
							loadMs: Math.round(entry.loadEventEnd)
						};
					}
				"""
			);

			assertThat(timingMap)
				.as("navigation timing for %s", expectation.path())
				.isNotNull();

			return new PublicPageTiming(
				expectation.path(),
				response.status(),
				page.title(),
				expectation.expectedHeading(),
				timingMap.get("ttfbMs").longValue(),
				timingMap.get("domContentLoadedMs").longValue(),
				timingMap.get("loadMs").longValue()
			);
		}
		finally {
			page.close();
		}
	}

	private void writeReport(String baseUrl, List<PublicPageTiming> timings) throws IOException {
		Files.createDirectories(REPORT_PATH.getParent());

		StringBuilder markdown = new StringBuilder();
		markdown.append("# Public URL Smoke Report\n\n");
		markdown.append("- Base URL: ").append(baseUrl).append('\n');
		markdown.append("- Generated At: ").append(OffsetDateTime.now()).append('\n');
		markdown.append("- Metric Note: `TTFB` is the browser-side approximation from Navigation Timing `responseStart`.\n\n");
		markdown.append("| Path | Status | Title | H1 | TTFB (ms) | DOMContentLoaded (ms) | Load (ms) |\n");
		markdown.append("| --- | ---: | --- | --- | ---: | ---: | ---: |\n");
		for (PublicPageTiming timing : timings) {
			markdown.append("| ")
				.append(timing.path())
				.append(" | ")
				.append(timing.status())
				.append(" | ")
				.append(timing.title())
				.append(" | ")
				.append(timing.heading())
				.append(" | ")
				.append(timing.ttfbMs())
				.append(" | ")
				.append(timing.domContentLoadedMs())
				.append(" | ")
				.append(timing.loadMs())
				.append(" |\n");
		}

		Files.writeString(REPORT_PATH, markdown.toString(), StandardCharsets.UTF_8);
	}

	private String publicBaseUrl() {
		String baseUrl = System.getProperty("worldmap.publicBaseUrl");
		if (baseUrl == null || baseUrl.isBlank()) {
			baseUrl = System.getenv("WORLDMAP_PUBLIC_BASE_URL");
		}
		if (baseUrl == null || baseUrl.isBlank()) {
			baseUrl = "http://127.0.0.1:" + port;
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	private record PublicPageExpectation(
		String path,
		String expectedTitle,
		String headingSelector,
		String expectedHeading
	) {
	}

	private record PublicPageTiming(
		String path,
		int status,
		String title,
		String heading,
		long ttfbMs,
		long domContentLoadedMs,
		long loadMs
	) {
	}
}
