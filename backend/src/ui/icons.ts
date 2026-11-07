// Stuck Design Language System (DLS) Iconography
// Crafted strictly on a 24x24 grid with 2px rounded strokes.
// Clean, neurodivergent-friendly vector glyphs with zero chaotic visual noise.

export type IconName =
  | "brain"
  | "stack-bars"
  | "calm-sage"
  | "alert-coral"
  | "coming-up"
  | "in-head"
  | "parked"
  | "closed"
  | "timer"
  | "lock"
  | "shield"
  | "sparkles"
  | "arrow-right"
  | "check"
  | "cross"
  | "slider"
  | "apple"
  | "android"
  | "github"
  | "download"
  | "play"
  | "pause"
  | "reset";

export function renderIcon(name: IconName, size = 24, className = ""): string {
  const common = `width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="${className}"`;

  switch (name) {
    case "brain":
      return `<svg ${common}><path d="M9.5 2A4.5 4.5 0 0 0 5 6.5a4.4 4.4 0 0 0 .5 2A4.5 4.5 0 0 0 4 12a4.5 4.5 0 0 0 1.5 3.4A4.5 4.5 0 0 0 9 20h.5"/><path d="M14.5 2A4.5 4.5 0 0 1 19 6.5a4.4 4.4 0 0 1-.5 2A4.5 4.5 0 0 1 20 12a4.5 4.5 0 0 1-1.5 3.4A4.5 4.5 0 0 1 15 20h-.5"/><path d="M12 4v16"/><path d="M9.5 9a2.5 2.5 0 0 0-2.5 2.5c0 1.4 1.1 2.5 2.5 2.5"/><path d="M14.5 9a2.5 2.5 0 0 1 2.5 2.5c0 1.4-1.1 2.5-2.5 2.5"/></svg>`;

    case "stack-bars":
      return `<svg ${common}><rect x="6" y="4" width="12" height="3" rx="1.5" fill="currentColor"/><rect x="3" y="9" width="18" height="3" rx="1.5" fill="currentColor"/><rect x="5" y="14" width="14" height="3" rx="1.5" fill="currentColor"/><rect x="8" y="19" width="8" height="3" rx="1.5" fill="currentColor"/></svg>`;

    case "calm-sage":
      return `<svg ${common}><circle cx="12" cy="12" r="9" fill="currentColor" fill-opacity="0.15"/><path d="M8 12l2.5 2.5L16 9"/></svg>`;

    case "alert-coral":
      return `<svg ${common}><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" fill="currentColor" fill-opacity="0.15"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`;

    case "coming-up":
      return `<svg ${common}><circle cx="12" cy="12" r="9"/><polyline points="12 6 12 12 16 14"/></svg>`;

    case "in-head":
      return `<svg ${common}><path d="M12 2a8 8 0 0 0-8 8c0 3.5 2.5 6.5 6 7.6V20a2 2 0 0 0 2 2h0a2 2 0 0 0 2-2v-2.4c3.5-1.1 6-4.1 6-7.6a8 8 0 0 0-8-8z" fill="currentColor" fill-opacity="0.15"/><circle cx="12" cy="10" r="3" fill="currentColor"/></svg>`;

    case "parked":
      return `<svg ${common}><rect x="4" y="4" width="16" height="16" rx="4"/><path d="M9 16V8h4a2.5 2.5 0 0 1 0 5H9"/></svg>`;

    case "closed":
      return `<svg ${common}><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>`;

    case "timer":
      return `<svg ${common}><circle cx="12" cy="13" r="8"/><path d="M12 9v4l2.5 2.5"/><path d="M9 3h6"/><path d="M12 3v2"/></svg>`;

    case "lock":
      return `<svg ${common}><rect x="4" y="11" width="16" height="10" rx="3" fill="currentColor" fill-opacity="0.15"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/><circle cx="12" cy="16" r="1" fill="currentColor"/></svg>`;

    case "shield":
      return `<svg ${common}><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" fill="currentColor" fill-opacity="0.15"/></svg>`;

    case "sparkles":
      return `<svg ${common}><path d="m12 3-1.9 5.8a2 2 0 0 1-1.3 1.3L3 12l5.8 1.9a2 2 0 0 1 1.3 1.3L12 21l1.9-5.8a2 2 0 0 1 1.3-1.3L21 12l-5.8-1.9a2 2 0 0 1-1.3-1.3Z"/></svg>`;

    case "arrow-right":
      return `<svg ${common}><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>`;

    case "check":
      return `<svg ${common}><polyline points="20 6 9 17 4 12"/></svg>`;

    case "cross":
      return `<svg ${common}><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>`;

    case "slider":
      return `<svg ${common}><line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/></svg>`;

    case "apple":
      return `<svg ${common}><path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M15.97 4.37c.61-.75 1.04-1.8 0.92-2.87-.93.04-2.02.63-2.66 1.38-.56.65-1.06 1.71-.92 2.74 1.05.08 2.06-.5 2.66-1.25z"/></svg>`;

    case "android":
      return `<svg ${common}><circle cx="8" cy="8" r="1" fill="currentColor"/><circle cx="16" cy="8" r="1" fill="currentColor"/><path d="M4 11h16a1 1 0 0 1 1 1v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-8a1 1 0 0 1 1-1z"/><path d="M7 11V6a5 5 0 0 1 10 0v5"/><line x1="6" y1="2" x2="7.5" y2="5"/><line x1="18" y1="2" x2="16.5" y2="5"/></svg>`;

    case "github":
      return `<svg ${common}><path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"/></svg>`;

    case "download":
      return `<svg ${common}><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>`;

    case "play":
      return `<svg ${common}><polygon points="5 3 19 12 5 21 5 3" fill="currentColor"/></svg>`;

    case "pause":
      return `<svg ${common}><rect x="6" y="4" width="4" height="16" fill="currentColor"/><rect x="14" y="4" width="4" height="16" fill="currentColor"/></svg>`;

    case "reset":
      return `<svg ${common}><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/></svg>`;
  }
}
