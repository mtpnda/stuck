// Stuck Promotional Web Pages & API Route Unit Tests
// Strictly asserts ZERO payment, ZERO currency, ZERO global region selector.

import { assertEquals } from "jsr:@std/assert";
import { app } from "../main.ts";

Deno.test("GET /health returns healthy text", async () => {
  const res = await app.request("/health");
  assertEquals(res.status, 200);
  const text = await res.text();
  assertEquals(text.includes("Stuck Edge Web Server is healthy"), true);
});

Deno.test("GET / renders Home page with valid HTML, DLS tokens, and interactive simulator", async () => {
  const res = await app.request("/");
  assertEquals(res.status, 200);
  const html = await res.text();
  assertEquals(html.includes("<!DOCTYPE html>"), true);
  assertEquals(html.includes("Stuck"), true);
  assertEquals(html.includes("Unstick Your Thoughts"), true);
  assertEquals(html.includes("Externalize Working Memory"), true);
  assertEquals(html.includes("Interactive Laboratory"), true);
  assertEquals(html.includes("focus-timer-display"), true);

  // STRICT USER CONSTRAINT: NO CURRENCY
  assertEquals(html.includes("$"), false);
  assertEquals(html.includes("€"), false);
  assertEquals(html.includes("£"), false);
  assertEquals(html.includes("¥"), false);
  assertEquals(/\busd\b/i.test(html), false);
  assertEquals(/\bidr\b/i.test(html), false);
  assertEquals(/\beuro\b/i.test(html), false);
  assertEquals(/\bgbp\b/i.test(html), false);
  assertEquals(/\brp\b/i.test(html), false);

  // STRICT USER CONSTRAINT: NO PAYMENT FEATURE
  assertEquals(html.toLowerCase().includes("payment"), false);
  assertEquals(html.toLowerCase().includes("credit card"), false);
  assertEquals(html.toLowerCase().includes("monthly subscription"), false);
  assertEquals(html.toLowerCase().includes("checkout"), false);
  assertEquals(html.toLowerCase().includes("stripe"), false);
  assertEquals(html.toLowerCase().includes("pricing tier"), false);

  // STRICT USER CONSTRAINT: NO GLOBAL REGION SELECTOR
  assertEquals(html.toLowerCase().includes("region selector"), false);
  assertEquals(html.toLowerCase().includes("country picker"), false);
  assertEquals(html.toLowerCase().includes("select your region"), false);
  assertEquals(html.toLowerCase().includes("global region"), false);
});

Deno.test("GET /en renders Home page without redirects", async () => {
  const res = await app.request("/en");
  assertEquals(res.status, 200);
  const html = await res.text();
  assertEquals(html.includes("Unstick Your Thoughts"), true);
});

Deno.test("GET /whitepaper renders Technical Whitepaper with mathematical formulations and state diagrams", async () => {
  const res = await app.request("/whitepaper");
  assertEquals(res.status, 200);
  const html = await res.text();
  assertEquals(html.includes("Working Memory Capacity Regulation"), true);
  assertEquals(html.includes("Formula 2.1"), true);
  assertEquals(html.includes("Formula 2.2"), true);
  assertEquals(html.includes("stateDiagram-v2"), true);
  assertEquals(html.includes("flowchart TD"), true);
  assertEquals(html.includes("katex"), true);

  // Enforce zero payment & zero currency in whitepaper
  assertEquals(html.includes("$"), false);
  assertEquals(html.toLowerCase().includes("payment"), false);
});

Deno.test("GET /privacy renders Sovereign Privacy Policy with zero-cloud clauses", async () => {
  const res = await app.request("/privacy");
  assertEquals(res.status, 200);
  const html = await res.text();
  assertEquals(html.includes("Privacy Policy"), true);
  assertEquals(html.includes("Zero-Cloud Invariant"), true);
  assertEquals(html.includes("UserDefaults"), true);
  assertEquals(html.includes("SharedPreferences"), true);
  assertEquals(html.includes("section-7"), true);
});

Deno.test("GET /terms renders Terms of Service with non-medical disclaimer", async () => {
  const res = await app.request("/terms");
  assertEquals(res.status, 200);
  const html = await res.text();
  assertEquals(html.includes("Terms of Service"), true);
  assertEquals(html.includes("Non-Medical"), true);
  assertEquals(html.includes("FDA-cleared"), true);
  assertEquals(html.includes("User Sovereignty"), true);
});

Deno.test("GET /support renders Helpdesk & Support Form", async () => {
  const res = await app.request("/support");
  assertEquals(res.status, 200);
  const html = await res.text();
  assertEquals(html.includes("Support & Guides"), true);
  assertEquals(html.includes("supportForm"), true);
  assertEquals(html.includes("Zero-Tracker Guarantee"), true);
});

Deno.test("GET /api/config returns valid platform metadata", async () => {
  const res = await app.request("/api/config");
  assertEquals(res.status, 200);
  const json = await res.json();
  assertEquals(json.app, "Stuck");
  assertEquals(json.packageId, "com.mtpnda.stuck");
  assertEquals(json.privacy.zeroCloudStorage, true);
  assertEquals(json.privacy.remoteTelemetry, false);
  assertEquals(json.cognitiveModel.defaultCapacity, 3);
});

Deno.test("POST /api/support handles ticket submission", async () => {
  const payload = {
    name: "Alex",
    email: "alex@example.com",
    category: "cognitive-tuning",
    device: "ios",
    message: "Question regarding optimal capacity limiter setting for deep work sessions."
  };
  const res = await app.request("/api/support", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
  assertEquals(res.status, 200);
  const json = await res.json();
  assertEquals(json.success, true);
  assertEquals(json.ticketId.startsWith("stuck-ticket-"), true);
});

Deno.test("Static asset /assets/icon.svg is accessible with valid SVG mime type", async () => {
  const res = await app.request("/assets/icon.svg");
  assertEquals(res.status, 200);
  assertEquals(res.headers.get("Content-Type")?.includes("image/svg+xml"), true);
});

Deno.test("Static screenshot /assets/screenshots/01_landing_brand.png is accessible with PNG mime type", async () => {
  const res = await app.request("/assets/screenshots/01_landing_brand.png");
  assertEquals(res.status, 200);
  assertEquals(res.headers.get("Content-Type")?.includes("image/png"), true);
});
