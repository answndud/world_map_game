import test from "node:test";
import assert from "node:assert/strict";

import {
  ensureHeaderContains,
  extractAssetPaths,
  normalizeBaseUrl
} from "../scripts/smoke-public-url.mjs";

test("normalizeBaseUrl trims trailing slashes", () => {
  assert.equal(normalizeBaseUrl("https://worldmap-demo-lite.pages.dev///"), "https://worldmap-demo-lite.pages.dev");
});

test("extractAssetPaths finds hashed Vite assets", () => {
  const html = `
    <link rel="stylesheet" href="/assets/index-abc123.css" />
    <script type="module" src="/assets/index-xyz987.js"></script>
    <script type="module" src="/src/main.js"></script>
  `;

  assert.deepEqual(extractAssetPaths(html), ["/assets/index-abc123.css", "/assets/index-xyz987.js"]);
});

test("ensureHeaderContains accepts matching fragments case-insensitively", () => {
  const headers = new Headers({
    "cache-control": "public, max-age=0, must-revalidate",
    "x-frame-options": "DENY"
  });

  assert.doesNotThrow(() => ensureHeaderContains(headers, "cache-control", "must-revalidate"));
  assert.doesNotThrow(() => ensureHeaderContains(headers, "x-frame-options", "deny"));
});

test("ensureHeaderContains throws when the header is missing", () => {
  const headers = new Headers();

  assert.throws(() => ensureHeaderContains(headers, "content-security-policy", "default-src"));
});
