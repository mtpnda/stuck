// Stuck Master Layout Template
// Delivers coherent Scandinavian DLS styling, accessible semantics, and responsive design.

import { GLOBAL_STYLES } from "./theme.ts";
import { renderHeader } from "./components/header.ts";
import { renderFooter } from "./components/footer.ts";

export interface PageOptions {
  title?: string;
  description?: string;
  content: string;
  showHeader?: boolean;
  showFooter?: boolean;
  showNav?: boolean;
  activeTab?: string;
  extraHead?: string;
  scripts?: string;
}

export function renderLayout(opts: PageOptions): string {
  const title = opts.title ?? "Stuck — Serene Cognitive Load Balancer & 4-Stage Task Flow";
  const desc = opts.description ?? "Unstick your thoughts. A Scandinavian cognitive load balancer and mental context manager for iOS and Android. Cap active working memory, prevent open-loop anxiety, and keep 100% on-device privacy.";
  const showHeader = opts.showHeader ?? true;
  const showFooter = opts.showFooter ?? true;

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover"/>
  <meta name="theme-color" content="#F2F0EA"/>
  <title>${title}</title>
  <meta name="description" content="${desc}"/>

  <!-- OpenGraph / Social Meta -->
  <meta property="og:title" content="${title}"/>
  <meta property="og:description" content="${desc}"/>
  <meta property="og:type" content="website"/>
  <meta property="og:image" content="/assets/feature-graphic.png"/>
  <meta name="twitter:card" content="summary_large_image"/>
  <meta name="twitter:title" content="${title}"/>
  <meta name="twitter:description" content="${desc}"/>
  <meta name="twitter:image" content="/assets/feature-graphic.png"/>

  <!-- Favicon -->
  <link rel="icon" type="image/svg+xml" href="/assets/icon.svg"/>
  <link rel="apple-touch-icon" href="/assets/app-icon.png"/>

  <!-- Google Fonts Preconnect -->
  <link rel="preconnect" href="https://fonts.googleapis.com"/>
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>

  <!-- Universal Stuck DLS Stylesheet -->
  <style>
    ${GLOBAL_STYLES}
  </style>

  ${opts.extraHead ?? ''}
</head>
<body>
  ${showHeader ? renderHeader({ showNav: opts.showNav, activeTab: opts.activeTab }) : ''}

  <main id="main-content">
    ${opts.content}
  </main>

  ${showFooter ? renderFooter() : ''}

  <!-- Smooth Scrolling Anchor Support -->
  <script>
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
      anchor.addEventListener('click', function (e) {
        const targetId = this.getAttribute('href');
        if (targetId && targetId !== '#') {
          const elem = document.querySelector(targetId);
          if (elem) {
            e.preventDefault();
            elem.scrollIntoView({ behavior: 'smooth' });
          }
        }
      });
    });
  </script>

  ${opts.scripts ?? ''}
</body>
</html>`;
}
