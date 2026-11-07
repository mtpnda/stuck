// Stuck Site Header Component
// Accessible, responsive, zero-clutter navigation adhering to DLS tokens.

import { renderIcon } from "../icons.ts";

export interface HeaderProps {
  showNav?: boolean;
  activeTab?: string;
}

export function renderHeader(props: HeaderProps = {}): string {
  const showNav = props.showNav ?? true;
  const activeTab = props.activeTab || "";

  return `
  <header class="site-header">
    <div class="container">
      <div class="nav-inner">
        <a href="/" class="brand-logo" aria-label="Stuck Home">
          <svg viewBox="0 0 1024 1024" width="36" height="36" style="border-radius: 9px; box-shadow: 0 2px 6px rgba(0,0,0,0.12);">
            <rect width="1024" height="1024" fill="#F2F0EA"/>
            <rect x="84" y="84" width="856" height="856" rx="220" fill="#1C1814"/>
            <rect x="322" y="328" width="380" height="76" rx="38" fill="#4A7C59"/>
            <rect x="212" y="452" width="600" height="76" rx="38" fill="#FCFBF8"/>
            <rect x="272" y="576" width="480" height="76" rx="38" fill="#F2F0EA"/>
            <rect x="382" y="700" width="260" height="76" rx="38" fill="#C84B31"/>
          </svg>
          <span>Stuck</span>
        </a>

        ${showNav ? `
        <nav class="nav-menu" aria-label="Primary Navigation">
          <a href="/#features" class="nav-link ${activeTab === 'features' ? 'active' : ''}">Features</a>
          <a href="/#stages" class="nav-link ${activeTab === 'stages' ? 'active' : ''}">4-Stage Flow</a>
          <a href="/#simulator" class="nav-link ${activeTab === 'simulator' ? 'active' : ''}">Simulator</a>
          <a href="/whitepaper" class="nav-link ${activeTab === 'whitepaper' ? 'active' : ''}">Cognitive Science</a>
          <a href="/#privacy" class="nav-link ${activeTab === 'privacy' ? 'active' : ''}">Zero-Cloud</a>
          <a href="/support" class="nav-link ${activeTab === 'support' ? 'active' : ''}">Support</a>
        </nav>
        ` : ''}

        <div class="nav-actions">
          <a href="/#download" class="btn btn-primary btn-sm">
            ${renderIcon("download", 16)}
            <span>Get Stuck</span>
          </a>
        </div>
      </div>
    </div>
  </header>
  `;
}
