import { cpSync, existsSync, mkdirSync, readdirSync, rmSync, statSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const projectRoot = resolve(__dirname, "..", "..");
const demoLiteRoot = resolve(__dirname, "..");

const sourceCountries = join(projectRoot, "src/main/resources/data/countries.json");
const sourceFlagAssets = join(projectRoot, "src/main/resources/data/flag-assets.json");
const sourceFlagsDir = join(projectRoot, "src/main/resources/static/images/flags");

const generatedRoot = join(demoLiteRoot, "public/generated");
const generatedDataDir = join(generatedRoot, "data");
const generatedFlagsDir = join(generatedRoot, "flags");

function assertExists(path) {
  if (!existsSync(path)) {
    throw new Error(`Missing shared source: ${path}`);
  }
}

function countFiles(dir) {
  return readdirSync(dir).filter((entry) => statSync(join(dir, entry)).isFile()).length;
}

assertExists(sourceCountries);
assertExists(sourceFlagAssets);
assertExists(sourceFlagsDir);

rmSync(generatedRoot, { recursive: true, force: true });
mkdirSync(generatedDataDir, { recursive: true });
mkdirSync(generatedFlagsDir, { recursive: true });

cpSync(sourceCountries, join(generatedDataDir, "countries.json"));
cpSync(sourceFlagAssets, join(generatedDataDir, "flag-assets.json"));
cpSync(sourceFlagsDir, generatedFlagsDir, { recursive: true });

const copiedFlagCount = countFiles(generatedFlagsDir);

console.log(`[demo-lite] copied countries.json, flag-assets.json, and ${copiedFlagCount} flag assets into public/generated`);
