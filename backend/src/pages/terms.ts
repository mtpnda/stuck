// Stuck Terms of Service
// Non-medical cognitive wellness disclaimer, user sovereignty, free software terms.
// Strictly ZERO payment feature, ZERO currency, ZERO global region selector.

import { renderLayout } from "../ui/layout.ts";
import { renderIcon } from "../ui/icons.ts";

export function renderTermsPage(): string {
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
      .alert-box {
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
        <div class="badge badge-neutral" style="margin-bottom: 16px;">
          ${renderIcon("shield", 14)}
          <span>Legal Agreement</span>
        </div>
        <h1 style="font-size: clamp(32px, 5vw, 44px); margin-bottom: 12px;">Terms of Service</h1>
        <p style="font-size: 14.5px; color: var(--text-muted);">
          Effective Date: November 2026 • Application: Stuck (<code>com.mtpnda.stuck</code>)
        </p>
      </div>

      <div class="legal-body">
        <div class="alert-box">
          <strong style="color: var(--text-primary); font-size: 16px; display: block; margin-bottom: 6px;">
            Assistive Non-Medical & Wellness Disclaimer:
          </strong>
          <span>
            Stuck is an attentional self-regulation and cognitive load balancing utility. It is not an FDA-cleared medical device, nor does it provide medical advice, diagnosis, or clinical psychiatric therapy for ADHD, autism spectrum conditions, anxiety, or depression. If you are experiencing acute psychological distress, please consult a licensed medical clinician.
          </span>
        </div>

        <h2>1. Free & Sovereign Software</h2>
        <p>
          Stuck is provided by Mata Panda Studio as 100% free and open-source software. Access to all features—including 4-stage thought flow, cognitive capacity steppers, and focus sessions—is provided without fees, paywalls, or feature locking.
        </p>

        <h2>2. Complete User Sovereignty</h2>
        <p>
          You are the sovereign owner of all thoughts, notes, and task data entered into Stuck. Because all information resides in local device storage, Mata Panda Studio claims zero ownership, license, or copyright over any of your contexts.
        </p>

        <h2>3. Local Device Storage Responsibility</h2>
        <p>
          Because Stuck maintains zero cloud databases to protect your privacy, we do not possess remote copies of your contexts. You are solely responsible for managing physical device integrity, operating system updates, and operating system backups (such as Apple iCloud Backup or Google Drive Device Backup).
        </p>

        <h2>4. Prohibited Uses</h2>
        <p>
          You agree not to use Stuck for unlawful purposes or in attempts to reverse engineer or tamper with the application binary in a manner that compromises system security.
        </p>

        <h2>5. Disclaimer of Warranties</h2>
        <p>
          Stuck is provided "as is" and "as available" without warranties of any kind, whether express or implied. Mata Panda Studio does not warrant that the application will be uninterrupted or error-free.
        </p>

        <h2>6. Inquiries & Legal Notice</h2>
        <p>
          For legal inquiries regarding these terms, please contact:
        </p>
        <p>
          Email: <a href="mailto:support@matapandastudio.com" style="color: var(--text-primary); font-weight: 700; text-decoration: underline;">support@matapandastudio.com</a><br/>
          Studio: Mata Panda Studio
        </p>
      </div>
    </article>
  `;

  return renderLayout({
    title: "Terms of Service — Stuck",
    description: "Terms of service and non-medical cognitive wellness disclaimer for Stuck (com.mtpnda.stuck).",
    content,
    extraHead,
    activeTab: "terms"
  });
}
