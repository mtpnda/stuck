// Stuck Global DLS Stylesheet
// Strictly derived from design/dls.tokens.json and BioCue Scandinavian DLS principles.
// High-contrast ink on warm tactile oat canvas, zero raw styling, zero visual anxiety.

export const GLOBAL_STYLES = `
  @import url('https://fonts.googleapis.com/css2?family=Atkinson+Hyperlegible+Next:ital,wght@0,400..700;1,400..700&family=Newsreader:ital,opsz,wght@0,6..72,400..700;1,6..72,400..700&display=swap');

  :root {
    --surface-bg: #F2F0EA;
    --surface-card: #FCFBF8;
    --surface-raised: #F7F4EE;
    --border-subtle: #E7E2D9;
    --border-strong: #DAD4C8;

    --text-primary: #191612;
    --text-secondary: #3D362C;
    --text-muted: #544B3E;

    --action-primary: #1C1814;
    --action-onPrimary: #FBF6EC;
    --shadow-tint: #2B261E;

    --state-calm-chip: #86EFAC;
    --state-calm-soft: #DEF7E5;
    --state-calm-ink: #1A6337;

    --state-rising-chip: #FCD34D;
    --state-rising-soft: #FEF4D6;
    --state-rising-ink: #874A00;

    --state-alert-chip: #FCA5A5;
    --state-alert-soft: #FBE3E1;
    --state-alert-ink: #9C2A22;

    --state-info-chip: #93C5FD;
    --state-info-soft: #DDEAFB;
    --state-info-ink: #1B4F9C;

    --state-neutral-chip: #D1D5DB;
    --state-neutral-soft: #EDEEF0;
    --state-neutral-ink: #383C44;

    /* 4-Stage Kanban Deterministic Colors */
    --stage-active: #4B8BF5;
    --stage-active-soft: #EBF3FE;
    --stage-active-ink: #1E40AF;

    --stage-planned: #A765BA;
    --stage-planned-soft: #F6EDFA;
    --stage-planned-ink: #6B21A8;

    --stage-parked: #F59E0B;
    --stage-parked-soft: #FEF3C7;
    --stage-parked-ink: #92400E;

    --stage-done: #7FB844;
    --stage-done-soft: #EDF7E2;
    --stage-done-ink: #2E6815;

    --radius-sm: 12px;
    --radius-md: 16px;
    --radius-card: 20px;
    --radius-lg: 22px;
    --radius-xl: 28px;
    --radius-pill: 999px;

    --font-serif: 'Newsreader', 'Iowan Old Style', Georgia, serif;
    --font-sans: 'Atkinson Hyperlegible Next', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    --font-mono: ui-monospace, SFMono-Regular, Menlo, monospace;

    --shadow-card: 0 4px 14px color-mix(in srgb, var(--shadow-tint) 7%, transparent);
    --shadow-raised: 0 8px 24px color-mix(in srgb, var(--shadow-tint) 11%, transparent);
    --shadow-tactile: 0 3px 0 var(--border-strong);
    --shadow-btn: 0 3px 0 var(--shadow-tint);
  }

  * {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
  }

  html {
    scroll-behavior: smooth;
    font-size: 16px;
  }

  body {
    background-color: var(--surface-bg);
    color: var(--text-primary);
    font-family: var(--font-sans);
    line-height: 1.6;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }

  main {
    flex: 1;
  }

  a {
    color: inherit;
    text-decoration: none;
  }

  /* Typography Utilities */
  h1, h2, h3, h4 {
    font-family: var(--font-serif);
    font-weight: 500;
    line-height: 1.15;
    color: var(--text-primary);
    letter-spacing: -0.02em;
  }

  p {
    color: var(--text-secondary);
  }

  .container {
    max-width: 1140px;
    margin: 0 auto;
    padding: 0 24px;
  }

  /* Header & Navigation */
  header.site-header {
    position: sticky;
    top: 0;
    z-index: 100;
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    background: color-mix(in srgb, var(--surface-bg) 88%, transparent);
    border-bottom: 1px solid var(--border-subtle);
    transition: background 0.2s ease;
  }

  .nav-inner {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 72px;
  }

  .brand-logo {
    display: flex;
    align-items: center;
    gap: 12px;
    font-family: var(--font-serif);
    font-size: 24px;
    font-weight: 600;
    letter-spacing: -0.02em;
  }

  .brand-logo svg, .brand-logo img {
    width: 36px;
    height: 36px;
    border-radius: 9px;
  }

  .nav-menu {
    display: flex;
    align-items: center;
    gap: 24px;
  }

  .nav-link {
    font-size: 14.5px;
    font-weight: 600;
    color: var(--text-secondary);
    transition: color 0.15s ease;
  }

  .nav-link:hover, .nav-link.active {
    color: var(--text-primary);
  }

  .nav-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  /* Buttons */
  .btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 12px 22px;
    font-size: 14.5px;
    font-weight: 700;
    border-radius: var(--radius-pill);
    cursor: pointer;
    transition: transform 0.15s cubic-bezier(0.2, 0.8, 0.4, 1), box-shadow 0.15s ease, background-color 0.15s ease;
    border: 1px solid transparent;
    text-decoration: none;
    line-height: 1;
  }

  .btn:active {
    transform: translateY(2px);
  }

  .btn-primary {
    background: var(--action-primary);
    color: var(--action-onPrimary);
    box-shadow: var(--shadow-btn);
  }

  .btn-primary:hover {
    background: #2b251f;
  }

  .btn-outline {
    background: var(--surface-card);
    color: var(--text-primary);
    border-color: var(--border-strong);
    box-shadow: var(--shadow-tactile);
  }

  .btn-outline:hover {
    background: var(--surface-raised);
    border-color: var(--text-primary);
  }

  .btn-sm {
    padding: 8px 14px;
    font-size: 13px;
  }

  /* Badges & Chips */
  .badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 5px 12px;
    border-radius: var(--radius-pill);
    font-size: 12px;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.05em;
  }

  .badge-calm {
    background: var(--state-calm-soft);
    color: var(--state-calm-ink);
    border: 1px solid var(--state-calm-chip);
  }

  .badge-alert {
    background: var(--state-alert-soft);
    color: var(--state-alert-ink);
    border: 1px solid var(--state-alert-chip);
  }

  .badge-neutral {
    background: var(--surface-card);
    color: var(--text-secondary);
    border: 1px solid var(--border-strong);
    box-shadow: var(--shadow-tactile);
  }

  /* Footer */
  footer.site-footer {
    border-top: 1px solid var(--border-subtle);
    background: var(--surface-card);
    padding: 60px 0 36px;
    margin-top: 80px;
    font-size: 13.5px;
  }

  .footer-grid {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr 1fr;
    gap: 40px;
    margin-bottom: 48px;
  }

  .footer-col h4 {
    font-size: 16px;
    margin-bottom: 16px;
    color: var(--text-primary);
  }

  .footer-links {
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .footer-links a {
    color: var(--text-secondary);
    transition: color 0.15s ease;
  }

  .footer-links a:hover {
    color: var(--text-primary);
  }

  .footer-bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 28px;
    border-top: 1px dashed var(--border-subtle);
    color: var(--text-muted);
  }

  /* Responsive Rules */
  @media (max-width: 820px) {
    .nav-menu {
      display: none;
    }
    .footer-grid {
      grid-template-columns: 1fr 1fr;
      gap: 32px;
    }
  }

  @media (max-width: 540px) {
    .footer-grid {
      grid-template-columns: 1fr;
    }
    .footer-bottom {
      flex-direction: column;
      gap: 12px;
      text-align: center;
    }
  }
`;
