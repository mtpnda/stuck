// Stuck Helpdesk & Support Portal
// Sovereign support inquiry form and troubleshooting guides.
// Strictly ZERO payment feature, ZERO currency, ZERO global region selector.

import { renderLayout } from "../ui/layout.ts";
import { renderIcon } from "../ui/icons.ts";

export function renderSupportPage(): string {
  const extraHead = `
    <style>
      .support-container {
        max-width: 820px;
        margin: 48px auto;
        padding: 0 24px;
      }
      .support-header {
        text-align: center;
        margin-bottom: 48px;
        padding-bottom: 24px;
        border-bottom: 1px solid var(--border-subtle);
      }
      .support-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 32px;
      }
      .form-card {
        background: var(--surface-card);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-card);
        padding: 32px 28px;
        box-shadow: var(--shadow-card);
      }
      .form-group {
        margin-bottom: 20px;
      }
      .form-group label {
        display: block;
        font-size: 13.5px;
        font-weight: 700;
        margin-bottom: 6px;
        color: var(--text-primary);
      }
      .form-control {
        width: 100%;
        padding: 12px 16px;
        border: 1px solid var(--border-strong);
        border-radius: var(--radius-md);
        font-family: inherit;
        font-size: 14.5px;
        background: var(--surface-bg);
        color: var(--text-primary);
        outline: none;
        transition: border-color 0.15s ease;
      }
      .form-control:focus {
        border-color: var(--text-primary);
        background: var(--surface-card);
      }
      textarea.form-control {
        min-height: 120px;
        resize: vertical;
      }
      .faq-card {
        background: var(--surface-bg);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-card);
        padding: 24px;
        display: flex;
        flex-direction: column;
        gap: 16px;
      }
      .faq-guide-item {
        border-bottom: 1px dashed var(--border-subtle);
        padding-bottom: 12px;
      }
      .faq-guide-item:last-child {
        border-bottom: none;
        padding-bottom: 0;
      }
      .faq-guide-title {
        font-size: 14.5px;
        font-weight: 700;
        color: var(--text-primary);
        margin-bottom: 4px;
      }
      .faq-guide-desc {
        font-size: 13px;
        line-height: 1.5;
        color: var(--text-secondary);
      }

      @media (max-width: 760px) {
        .support-grid {
          grid-template-columns: 1fr;
        }
      }
    </style>
  `;

  const content = `
    <div class="support-container">
      <div class="support-header">
        <div class="badge badge-neutral" style="margin-bottom: 16px;">
          ${renderIcon("sparkles", 14)}
          <span>Helpdesk & Knowledge Base</span>
        </div>
        <h1 style="font-size: clamp(32px, 5vw, 44px); margin-bottom: 12px;">Support & Guides</h1>
        <p style="font-size: 16px; color: var(--text-secondary); max-width: 540px; margin: 0 auto;">
          Have a question about cognitive capacity calibration, biometric locks, or zero-cloud data preservation? We are here to help.
        </p>
      </div>

      <div class="support-grid">
        <!-- Inquiry Form -->
        <div class="form-card">
          <h2 style="font-size: 20px; margin-bottom: 18px; color: var(--text-primary);">Submit Support Inquiry</h2>
          <form id="supportForm">
            <div class="form-group">
              <label for="name">Your Name</label>
              <input type="text" id="name" class="form-control" placeholder="e.g., Alex" required />
            </div>

            <div class="form-group">
              <label for="email">Email Address</label>
              <input type="email" id="email" class="form-control" placeholder="alex@example.com" required />
            </div>

            <div class="form-group">
              <label for="category">Inquiry Topic</label>
              <select id="category" class="form-control">
                <option value="cognitive-tuning">Cognitive Capacity Tuning</option>
                <option value="biometrics">Face ID / Biometrics</option>
                <option value="data-backup">Local Backup & Migration</option>
                <option value="bug-report">Bug Report</option>
                <option value="other">General Question</option>
              </select>
            </div>

            <div class="form-group">
              <label for="device">Your Operating System</label>
              <select id="device" class="form-control">
                <option value="ios">Apple iOS (iPhone / iPad)</option>
                <option value="android">Android (Samsung, Pixel, etc.)</option>
                <option value="other">Other / Desktop</option>
              </select>
            </div>

            <div class="form-group">
              <label for="message">How can we assist you?</label>
              <textarea id="message" class="form-control" placeholder="Describe your inquiry..." required></textarea>
            </div>

            <button type="submit" id="btn-submit-support" class="btn btn-primary" style="width: 100%;">
              ${renderIcon("arrow-right", 16)}
              <span>Send Sovereign Inquiry</span>
            </button>
          </form>

          <div id="support-status" style="display: none; margin-top: 16px; padding: 12px 16px; border-radius: var(--radius-md); font-size: 13.5px; line-height: 1.5;"></div>
        </div>

        <!-- Quick Help Guides -->
        <div style="display: flex; flex-direction: column; gap: 20px;">
          <div class="faq-card">
            <h3 style="font-size: 18px; color: var(--text-primary); margin-bottom: 4px;">Quick Self-Help Guides</h3>
            
            <div class="faq-guide-item">
              <div class="faq-guide-title">How do I calibrate my capacity limit?</div>
              <div class="faq-guide-desc">
                Open Profile in the app and slide the capacity stepper between 1 and 9. Start with 3 active thoughts for the first week to find your natural working memory sweet spot.
              </div>
            </div>

            <div class="faq-guide-item">
              <div class="faq-guide-title">Face ID not prompting on resume?</div>
              <div class="faq-guide-desc">
                Verify that Stuck has permission under iOS Settings > Stuck > Face ID. When enabled, the app locks instantly upon entering the background.
              </div>
            </div>

            <div class="faq-guide-item">
              <div class="faq-guide-title">How do I transfer tasks to a new phone?</div>
              <div class="faq-guide-desc">
                Use your standard device migration (Apple Quick Start or Android Google Transfer). Both transfer the app's local sandbox data securely during device setup.
              </div>
            </div>
          </div>

          <div class="faq-card" style="background: var(--surface-card);">
            <div style="display: flex; align-items: center; gap: 10px; color: var(--state-calm-ink); font-weight: 700; font-size: 14.5px;">
              ${renderIcon("shield", 18)}
              <span>Zero-Tracker Guarantee</span>
            </div>
            <p style="font-size: 13px; color: var(--text-secondary); line-height: 1.6;">
              Inquiries submitted through this form are sent directly to our developer team and are never routed through third-party advertising or ticketing aggregators.
            </p>
            <div style="font-size: 12.5px; color: var(--text-muted);">
              Direct Email: <a href="mailto:support@matapandastudio.com" style="color: var(--text-primary); font-weight: 700;">support@matapandastudio.com</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;

  const scripts = `
    <script>
      (function() {
        const form = document.getElementById('supportForm');
        const statusBox = document.getElementById('support-status');
        const submitBtn = document.getElementById('btn-submit-support');

        form.addEventListener('submit', async function(e) {
          e.preventDefault();
          submitBtn.disabled = true;
          submitBtn.style.opacity = '0.6';

          const payload = {
            name: document.getElementById('name').value,
            email: document.getElementById('email').value,
            category: document.getElementById('category').value,
            device: document.getElementById('device').value,
            message: document.getElementById('message').value
          };

          try {
            const res = await fetch('/api/support', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            });
            const data = await res.json();

            if (res.ok) {
              statusBox.style.display = 'block';
              statusBox.style.background = 'var(--state-calm-soft)';
              statusBox.style.color = 'var(--state-calm-ink)';
              statusBox.style.border = '1px solid var(--state-calm-chip)';
              statusBox.innerHTML = '<strong>Inquiry Sent!</strong> Reference: ' + data.ticketId + '<br/>A sovereign reply will be sent to your email within 24 hours.';
              form.reset();
            } else {
              throw new Error(data.error || 'Failed to submit inquiry.');
            }
          } catch (err) {
            statusBox.style.display = 'block';
            statusBox.style.background = 'var(--state-alert-soft)';
            statusBox.style.color = 'var(--state-alert-ink)';
            statusBox.style.border = '1px solid var(--state-alert-chip)';
            statusBox.innerHTML = '<strong>Error:</strong> ' + err.message;
          } finally {
            submitBtn.disabled = false;
            submitBtn.style.opacity = '1';
          }
        });
      })();
    </script>
  `;

  return renderLayout({
    title: "Help & Support — Stuck",
    description: "Support portal and troubleshooting guides for Stuck (com.mtpnda.stuck).",
    content,
    extraHead,
    scripts,
    activeTab: "support"
  });
}
