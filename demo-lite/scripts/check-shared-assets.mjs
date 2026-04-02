import { existsSync, readFileSync, readdirSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const demoLiteRoot = resolve(__dirname, "..");
const generatedDataDir = join(demoLiteRoot, "public/generated/data");
const generatedFlagsDir = join(demoLiteRoot, "public/generated/flags");

const countriesPath = join(generatedDataDir, "countries.json");
const flagAssetsPath = join(generatedDataDir, "flag-assets.json");

for (const path of [countriesPath, flagAssetsPath, generatedFlagsDir]) {
  if (!existsSync(path)) {
    throw new Error(`Missing generated demo-lite asset: ${path}`);
  }
}

const countriesPayload = JSON.parse(readFileSync(countriesPath, "utf8"));
const flagAssetsPayload = JSON.parse(readFileSync(flagAssetsPath, "utf8"));
const countries = Array.isArray(countriesPayload?.countries) ? countriesPayload.countries : [];
const flagAssets = Array.isArray(flagAssetsPayload?.assets) ? flagAssetsPayload.assets : [];
const flagFiles = new Set(readdirSync(generatedFlagsDir));

if (!Array.isArray(countries) || countries.length === 0) {
  throw new Error("countries.json is empty or invalid");
}

if (!Array.isArray(flagAssets) || flagAssets.length === 0) {
  throw new Error("flag-assets.json is empty or invalid");
}

for (const asset of flagAssets) {
  const fileName = asset.relativePath?.split("/").pop();
  if (!fileName || !flagFiles.has(fileName)) {
    throw new Error(`Flag asset listed in manifest but missing in generated flags: ${JSON.stringify(asset)}`);
  }
}

console.log(`[demo-lite] verified ${countries.length} countries, ${flagAssets.length} manifest entries, ${flagFiles.size} generated flag files`);
