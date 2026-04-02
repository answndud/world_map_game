import test from "node:test";
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

const ROOT = resolve(import.meta.dirname, "..");

test("demo-lite pins a Cloudflare Pages Node version in .node-version", () => {
  const value = readFileSync(resolve(ROOT, ".node-version"), "utf8").trim();

  assert.match(value, /^\d+\.\d+\.\d+$/);
});

test("demo-lite ships Cloudflare Pages _headers with security and cache rules", () => {
  const headers = readFileSync(resolve(ROOT, "public/_headers"), "utf8");

  assert.match(headers, /\/\*/);
  assert.match(headers, /Content-Security-Policy:/);
  assert.match(headers, /X-Content-Type-Options:\s+nosniff/);
  assert.match(headers, /\/assets\/\*/);
  assert.match(headers, /\/generated\/flags\/\*/);
  assert.match(headers, /\/generated\/data\/\*/);
});
