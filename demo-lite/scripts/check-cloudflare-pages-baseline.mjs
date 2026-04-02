import { readFileSync } from "node:fs";
import { resolve } from "node:path";

const ROOT = resolve(import.meta.dirname, "..");

function fail(message) {
  console.error(`[demo-lite] ${message}`);
  process.exit(1);
}

function read(path) {
  return readFileSync(resolve(ROOT, path), "utf8");
}

const packageJson = JSON.parse(read("package.json"));
const nodeVersion = read(".node-version").trim();
const headers = read("public/_headers");

if (!packageJson.scripts?.build?.includes("vite build")) {
  fail("package.json build script must include vite build for Cloudflare Pages.");
}

if (!packageJson.scripts?.build?.includes("sync:shared")) {
  fail("package.json build script must include sync:shared before Vite build.");
}

if (!packageJson.scripts?.build?.includes("verify:shared")) {
  fail("package.json build script must include verify:shared before Vite build.");
}

if (!/^\d+\.\d+\.\d+$/.test(nodeVersion)) {
  fail(".node-version must pin a concrete Node.js version.");
}

if (packageJson.engines?.node !== "20.x") {
  fail('package.json engines.node must stay aligned with the Pages baseline ("20.x").');
}

for (const requiredRule of [
  "/*",
  "/assets/*",
  "/generated/flags/*",
  "/generated/data/*",
  "Content-Security-Policy:",
  "X-Content-Type-Options: nosniff",
  "X-Frame-Options: DENY"
]) {
  if (!headers.includes(requiredRule)) {
    fail(`public/_headers is missing required rule: ${requiredRule}`);
  }
}

console.log(`[demo-lite] verified Cloudflare Pages baseline with Node ${nodeVersion}`);
