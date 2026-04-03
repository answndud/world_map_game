import { pathToFileURL } from "node:url";

const DEFAULT_BASE_URL = "https://world-map-game-demo-lite-git.pages.dev";

export function normalizeBaseUrl(input) {
  const value = String(input || "").trim();
  if (!value) {
    throw new Error("public base URL is required");
  }

  return value.replace(/\/+$/, "");
}

export function extractAssetPaths(html) {
  const matches = html.matchAll(/(?:src|href)="(\/assets\/[^"]+)"/g);
  return [...new Set([...matches].map((match) => match[1]).filter(Boolean))];
}

export function ensureHeaderContains(headers, name, expectedFragment) {
  const value = headers.get(name);
  if (!value || !value.toLowerCase().includes(expectedFragment.toLowerCase())) {
    throw new Error(`missing header ${name} containing "${expectedFragment}"`);
  }
}

function ensureHeaderPresent(headers, name) {
  const value = headers.get(name);
  if (!value) {
    throw new Error(`missing header ${name}`);
  }
}

function mapFlagPath(relativePath) {
  if (typeof relativePath !== "string" || !relativePath.trim()) {
    throw new Error("flag asset relativePath is missing");
  }

  return relativePath.replace("/images/flags/", "/generated/flags/");
}

async function fetchOk(url) {
  const response = await fetch(url, { redirect: "follow" });
  if (!response.ok) {
    throw new Error(`${url} returned ${response.status}`);
  }
  return response;
}

async function fetchText(url) {
  const response = await fetchOk(url);
  return {
    response,
    body: await response.text()
  };
}

async function fetchJson(url) {
  const response = await fetchOk(url);
  return {
    response,
    body: await response.json()
  };
}

async function main() {
  const baseUrl = normalizeBaseUrl(process.argv[2] || process.env.DEMO_LITE_PUBLIC_BASE_URL || DEFAULT_BASE_URL);

  console.log(`[demo-lite] smoke: root ${baseUrl}/`);
  const { response: rootResponse, body: rootHtml } = await fetchText(`${baseUrl}/`);

  if (!rootHtml.includes("WorldMap Demo-Lite")) {
    throw new Error("root HTML does not include the demo-lite title");
  }

  ensureHeaderContains(rootResponse.headers, "cache-control", "must-revalidate");
  ensureHeaderContains(rootResponse.headers, "x-content-type-options", "nosniff");
  ensureHeaderContains(rootResponse.headers, "x-frame-options", "deny");
  ensureHeaderPresent(rootResponse.headers, "content-security-policy");

  const assetPaths = extractAssetPaths(rootHtml);
  if (assetPaths.length === 0) {
    throw new Error("root HTML does not reference any built /assets/* files");
  }

  for (const assetPath of assetPaths.slice(0, 2)) {
    console.log(`[demo-lite] smoke: asset ${assetPath}`);
    await fetchOk(`${baseUrl}${assetPath}`);
  }

  console.log("[demo-lite] smoke: countries catalog");
  const { body: countriesPayload } = await fetchJson(`${baseUrl}/generated/data/countries.json`);
  const countries = Array.isArray(countriesPayload?.countries) ? countriesPayload.countries : [];
  const korea = countries.find((country) => country.iso3Code === "KOR");

  if (countries.length < 150) {
    throw new Error(`countries catalog looks incomplete: ${countries.length}`);
  }

  if (!korea?.capitalCityKr) {
    throw new Error("countries catalog is missing the Korean capital name for KOR");
  }

  console.log("[demo-lite] smoke: flag asset manifest");
  const { body: flagAssetsPayload } = await fetchJson(`${baseUrl}/generated/data/flag-assets.json`);
  const flagAssets = Array.isArray(flagAssetsPayload?.assets) ? flagAssetsPayload.assets : [];
  if (flagAssets.length < 20) {
    throw new Error(`flag asset manifest looks incomplete: ${flagAssets.length}`);
  }

  const sampleFlagPath = mapFlagPath(flagAssets[0].relativePath);
  console.log(`[demo-lite] smoke: flag ${sampleFlagPath}`);
  const flagResponse = await fetchOk(`${baseUrl}${sampleFlagPath}`);
  ensureHeaderContains(flagResponse.headers, "cache-control", "immutable");

  console.log(`[demo-lite] public smoke passed for ${baseUrl}`);
}

const isDirectExecution = process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href;

if (isDirectExecution) {
  main().catch((error) => {
    console.error(`[demo-lite] public smoke failed: ${error.message}`);
    process.exit(1);
  });
}
