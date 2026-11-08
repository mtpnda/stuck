// Stuck Site Footer Component
// Transparent, sovereign product disclosure and legal links.

import { renderIcon } from "../icons.ts";

export function renderFooter(): string {
  const currentYear = new Date().getFullYear();

  return `
  <footer class="site-footer">
    <div class="container">
      <div class="footer-grid">
        <div class="footer-col brand-info">
          <div class="brand-logo" style="margin-bottom: 12px;">
            <svg viewBox="0 0 1024 1024" width="28" height="28" style="border-radius: 7px;">
              <rect width="1024" height="1024" fill="#F2F0EA"/>
              <rect x="84" y="84" width="856" height="856" rx="220" fill="#1C1814"/>
              <rect x="322" y="328" width="380" height="76" rx="38" fill="#4A7C59"/>
              <rect x="212" y="452" width="600" height="76" rx="38" fill="#FCFBF8"/>
              <rect x="272" y="576" width="480" height="76" rx="38" fill="#F2F0EA"/>
              <rect x="382" y="700" width="260" height="76" rx="38" fill="#C84B31"/>
            </svg>
            <span>Stuck</span>
          </div>
          <p style="font-size: 13.5px; line-height: 1.6; max-width: 320px; color: var(--text-secondary); margin-bottom: 16px;">
            Unstick your thoughts. A Scandinavian cognitive load manager & capacity balancer engineered to protect working memory through 4-stage thought externalization.
          </p>
          <div style="display: inline-flex; align-items: center; gap: 8px; background: var(--state-calm-soft); color: var(--state-calm-ink); padding: 4px 10px; border-radius: var(--radius-pill); font-size: 12px; font-weight: 700; border: 1px solid var(--state-calm-chip);">
            ${renderIcon("shield", 14)}
            <span>100% On-Device & Zero-Cloud</span>
          </div>
        </div>

        <div class="footer-col">
          <h4>Product</h4>
          <ul class="footer-links">
            <li><a href="/#features">Features Overview</a></li>
            <li><a href="/#stages">4-Stage Thought Flow</a></li>
            <li><a href="/#simulator">Headspace Simulator</a></li>
            <li><a href="/#download">Download (iOS & Android)</a></li>
            <li><a href="https://github.com/mtpnda/stuck" target="_blank" rel="noopener">GitHub Repository</a></li>
          </ul>
        </div>

        <div class="footer-col">
          <h4>Cognitive Science</h4>
          <ul class="footer-links">
            <li><a href="/whitepaper">Cognitive Whitepaper</a></li>
            <li><a href="/whitepaper#millers-law">Miller's Working Memory Law</a></li>
            <li><a href="/whitepaper#cowan-capacity">Cowan 4 ± 1 Limit</a></li>
            <li><a href="/whitepaper#swapping">Cognitive Context Swapping</a></li>
            <li><a href="/whitepaper#architecture">Technical Architecture</a></li>
          </ul>
        </div>

        <div class="footer-col">
          <h4>Sovereignty & Trust</h4>
          <ul class="footer-links">
            <li><a href="/privacy">Privacy Policy</a></li>
            <li><a href="/terms">Terms of Service</a></li>
            <li><a href="/support">Help & Support</a></li>
            <li><a href="/#privacy">Zero-Cloud Guarantee</a></li>
            <li><a href="mailto:info@valdiearsanur.com">info@valdiearsanur.com</a></li>
          </ul>
        </div>
      </div>

      <div class="footer-bottom">
        <div>
          © ${currentYear} Mata Panda Studio. All rights reserved. 100% Free & Open Source.
        </div>
        <div style="display: flex; gap: 16px; align-items: center;">
          <span>Zero Telemetry</span>
          <span>•</span>
          <span>Zero Data Brokers</span>
          <span>•</span>
          <span>Zero Server Storage</span>
        </div>
      </div>
    </div>
  </footer>
  `;
}
