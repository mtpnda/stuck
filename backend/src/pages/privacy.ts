// Stuck Sovereign Privacy Policy
// 100% On-Device Privacy Guarantee, Zero Cloud Storage, Zero Remote Telemetry.
// Strictly ZERO payment feature, ZERO currency, ZERO global region selector.

import { renderLayout } from "../ui/layout.ts";
import { renderIcon } from "../ui/icons.ts";

export function renderPrivacyPage(): string {
  const extraHead = `
    <style>
      .legal-container {
        max-width: 820px;
        margin: 48px auto;
        padding: 0 24px;
      }
      .legal-header {
        margin-bottom: 40px;
        padding-bottom: 24px;
        border-bottom: 1px solid var(--border-subtle);
      }
      .legal-body {
        font-size: 15.5px;
        line-height: 1.7;
        color: var(--text-secondary);
      }
      .legal-body h2 {
        font-size: 22px;
        margin: 36px 0 14px;
        color: var(--text-primary);
      }
      .legal-body p {
        margin-bottom: 16px;
      }
      .legal-body ul {
        margin: 0 0 20px 24px;
        display: flex;
        flex-direction: column;
        gap: 8px;
      }
      .callout-box {
        background: var(--surface-card);
        border: 1px solid var(--border-strong);
        border-radius: var(--radius-md);
        padding: 20px 24px;
        margin: 24px 0;
        box-shadow: var(--shadow-tactile);
      }
    </style>
  `;

  const content = `
    <article class="legal-container">
      <div class="legal-header">
        <div class="badge badge-calm" style="margin-bottom: 16px;">
          ${renderIcon("lock", 14)}
          <span>Sovereign Privacy Guarantee</span>
        </div>
        <h1 style="font-size: clamp(32px, 5vw, 44px); margin-bottom: 12px;">Privacy Policy</h1>
        <p style="font-size: 14.5px; color: var(--text-muted);">
          Effective Date: November 2026 • Application: Stuck (<code>com.mtpnda.stuck</code>)
        </p>
      </div>

      <div class="legal-body">
        <div class="callout-box">
          <strong style="color: var(--text-primary); font-size: 16px; display: block; margin-bottom: 6px;">
            The Stuck Zero-Cloud Invariant:
          </strong>
          <span>
            Stuck does not maintain remote cloud databases, does not track your network identity, and does not transmit your thoughts, tasks, or metrics to external servers. Your mind belongs exclusively to you.
          </span>
        </div>

        <h2>1. On-Device Data Isolation</h2>
        <p>
          All contexts, notes, cognitive capacity limit settings, and focus session durations entered into Stuck are stored exclusively in your device's local application sandbox:
        </p>
        <ul>
          <li><strong>Apple iOS:</strong> Stored via local <code>UserDefaults</code> structured JSON dictionaries within the application container.</li>
          <li><strong>Google Android:</strong> Stored via private <code>SharedPreferences</code> JSON files accessible only by the Stuck package UID.</li>
        </ul>
        <p>
          We operate zero centralized database clusters, zero remote document databases, and zero synchronization relays for your personal tasks.
        </p>

        <h2>2. Zero Continuous Background Location (GPS)</h2>
        <p>
          Stuck is an attentional and cognitive management application. We strictly require no GPS, fine location, or coarse location permissions. We never poll, store, or infer your geographic position.
        </p>

        <h2>3. Zero Third-Party Telemetry & Analytics</h2>
        <p>
          Unlike conventional software, Stuck includes strictly zero third-party behavioral analytics or advertising SDKs. We do not embed:
        </p>
        <ul>
          <li>Google Firebase Analytics or Crashlytics</li>
          <li>Meta (Facebook) Graph SDK</li>
          <li>Mixpanel, Amplitude, or Adjust beacons</li>
          <li>Targeted advertising identifiers (IDFA or AAID)</li>
        </ul>

        <h2>4. Local Biometric Authentication (Face ID & Fingerprint)</h2>
        <p>
          Stuck provides biometric privacy locking through Apple <code>LocalAuthentication</code> and Android <code>BiometricPrompt</code>. Biometric scans (facial geometry or fingerprints) are processed entirely inside your hardware device's Secure Enclave or hardware TrustZone. Stuck never sees, processes, or transmits biometric telemetry.
        </p>

        <h2>5. Irrevocable Local Data Deletion</h2>
        <p>
          Because all data is on-device, deleting the application or tapping "Clear Cache" in settings immediately and irrevocably erases all thoughts, history, and focus logs from the flash memory of your phone. There are no remote backups to request deletion from.
        </p>

        <h2>6. Children's Privacy (COPPA Compliance)</h2>
        <p>
          Stuck is designed for students, professionals, and neurodivergent individuals of all ages. Because we collect zero personally identifiable information (PII) from any user, we comply strictly with the Children's Online Privacy Protection Act (COPPA) and international child privacy standards.
        </p>

        <h2 id="section-7">7. Contact & Privacy Inquiries</h2>
        <p>
          If you have questions regarding our sovereign on-device privacy architecture, you may contact our privacy steward directly:
        </p>
        <p>
          Email: <a href="mailto:info@valdiearsanur.com" style="color: var(--text-primary); font-weight: 700; text-decoration: underline;">info@valdiearsanur.com</a><br/>
          Studio: Mata Panda Studio (Bandung & Singapore)
        </p>
      </div>
    </article>
  `;

  return renderLayout({
    title: "Privacy Policy — Stuck",
    description: "Sovereign on-device privacy policy for Stuck (com.mtpnda.stuck). 100% on-device storage, zero cloud databases, and zero telemetry.",
    content,
    extraHead,
    activeTab: "privacy"
  });
}
