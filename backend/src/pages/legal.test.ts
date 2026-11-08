// Stuck Legal Compliance & Policy Audit Tests
// Strictly asserts compliance with on-device sovereignty and non-medical disclaimers.

import { assertEquals } from "jsr:@std/assert";
import { app } from "../main.ts";

Deno.test("Privacy Policy includes required sovereign compliance sections", async () => {
  const res = await app.request("/privacy");
  assertEquals(res.status, 200);
  const html = await res.text();

  // 1. On-device isolation of cognitive task records
  assertEquals(html.includes("UserDefaults"), true);
  assertEquals(html.includes("SharedPreferences"), true);
  assertEquals(html.includes("Zero-Cloud Invariant"), true);

  // 2. Zero continuous background GPS tracking
  assertEquals(html.includes("Zero Continuous Background Location"), true);

  // 3. Irrevocable local data deletion
  assertEquals(html.includes("Irrevocable Local Data Deletion"), true);

  // 4. Data retention and contact
  assertEquals(html.includes("section-7"), true);
  assertEquals(html.includes("info@valdiearsanur.com"), true);

  // 5. COPPA & Children's privacy
  assertEquals(html.includes("COPPA"), true);

  // 6. Biometric security
  assertEquals(html.includes("Secure Enclave"), true);
});

Deno.test("Terms of Service enforces non-medical disclaimer and user sovereignty", async () => {
  const res = await app.request("/terms");
  assertEquals(res.status, 200);
  const html = await res.text();

  // 1. Assistive non-medical wellness disclaimer
  assertEquals(html.includes("Non-Medical"), true);
  assertEquals(html.includes("FDA-cleared"), true);

  // 2. User sovereignty
  assertEquals(html.includes("sovereign owner"), true);

  // 3. Free software terms
  assertEquals(html.includes("Free & Sovereign Software"), true);
});
