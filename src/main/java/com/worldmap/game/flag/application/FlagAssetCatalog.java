package com.worldmap.game.flag.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class FlagAssetCatalog {

	private static final String MANIFEST_LOCATION = "classpath:data/flag-assets.json";

	private final List<FlagAsset> assets;
	private final Map<String, FlagAsset> assetByIso3Code;

	public FlagAssetCatalog(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
		FlagAssetDocument document = readDocument(objectMapper, resourceLoader);
		this.assets = validateAndMap(document, resourceLoader);
		this.assetByIso3Code = assets.stream()
			.collect(Collectors.toMap(FlagAsset::iso3Code, asset -> asset, (left, right) -> left, LinkedHashMap::new));
	}

	public List<FlagAsset> assets() {
		return assets;
	}

	public Optional<FlagAsset> findByIso3Code(String iso3Code) {
		if (iso3Code == null || iso3Code.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(assetByIso3Code.get(normalizeIso3Code(iso3Code)));
	}

	public Set<String> supportedIso3Codes() {
		return assetByIso3Code.keySet();
	}

	public boolean supports(String iso3Code) {
		return findByIso3Code(iso3Code).isPresent();
	}

	private FlagAssetDocument readDocument(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
		Resource resource = resourceLoader.getResource(MANIFEST_LOCATION);

		if (!resource.exists()) {
			throw new IllegalStateException("국기 자산 manifest를 찾을 수 없습니다: " + MANIFEST_LOCATION);
		}

		try (InputStream inputStream = resource.getInputStream()) {
			return objectMapper.readValue(inputStream, FlagAssetDocument.class);
		} catch (IOException ex) {
			throw new IllegalStateException("국기 자산 manifest를 읽는 중 오류가 발생했습니다: " + MANIFEST_LOCATION, ex);
		}
	}

	private List<FlagAsset> validateAndMap(FlagAssetDocument document, ResourceLoader resourceLoader) {
		if (document.assets() == null || document.assets().isEmpty()) {
			throw new IllegalStateException("국기 자산 manifest는 최소 1개 이상의 항목이 필요합니다.");
		}

		Map<String, FlagAsset> mapped = new LinkedHashMap<>();

		for (FlagAssetItem item : document.assets()) {
			String iso3Code = normalizeIso3Code(item.iso3Code());
			String relativePath = normalizeRelativePath(item.relativePath());
			String format = normalizeFormat(item.format());

			if (mapped.containsKey(iso3Code)) {
				throw new IllegalStateException("국기 자산 ISO3 코드가 중복되었습니다: " + iso3Code);
			}

			Resource staticResource = resourceLoader.getResource("classpath:static" + relativePath);
			if (!staticResource.exists()) {
				throw new IllegalStateException("국기 정적 파일을 찾을 수 없습니다: " + relativePath);
			}

			mapped.put(
				iso3Code,
				new FlagAsset(
					iso3Code,
					relativePath,
					format,
					Objects.requireNonNullElse(item.source(), "local-static"),
					Objects.requireNonNullElse(item.licenseNote(), "unspecified")
				)
			);
		}

		return List.copyOf(mapped.values());
	}

	private String normalizeIso3Code(String iso3Code) {
		if (iso3Code == null || iso3Code.isBlank()) {
			throw new IllegalStateException("국기 자산 ISO3 코드는 비어 있을 수 없습니다.");
		}
		String normalized = iso3Code.trim().toUpperCase(Locale.ROOT);
		if (!normalized.matches("[A-Z]{3}")) {
			throw new IllegalStateException("국기 자산 ISO3 코드는 3자리 영문 대문자여야 합니다: " + iso3Code);
		}
		return normalized;
	}

	private String normalizeRelativePath(String relativePath) {
		if (relativePath == null || relativePath.isBlank()) {
			throw new IllegalStateException("국기 자산 상대 경로는 비어 있을 수 없습니다.");
		}
		String normalized = relativePath.trim();
		if (!normalized.startsWith("/images/flags/")) {
			throw new IllegalStateException("국기 자산 경로는 /images/flags/ 아래여야 합니다: " + relativePath);
		}
		if (!normalized.endsWith(".svg")) {
			throw new IllegalStateException("국기 자산 1차 포맷은 svg만 허용합니다: " + relativePath);
		}
		return normalized;
	}

	private String normalizeFormat(String format) {
		if (format == null || format.isBlank()) {
			throw new IllegalStateException("국기 자산 format은 비어 있을 수 없습니다.");
		}
		String normalized = format.trim().toLowerCase(Locale.ROOT);
		if (!"svg".equals(normalized)) {
			throw new IllegalStateException("국기 자산 1차 포맷은 svg만 허용합니다: " + format);
		}
		return normalized;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record FlagAssetDocument(List<FlagAssetItem> assets) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record FlagAssetItem(
		String iso3Code,
		String relativePath,
		String format,
		String source,
		String licenseNote
	) {
	}
}
