// Stuck Official Promotional Website
// Scandinavian Cognitive Load Balancer & 4-Stage Task Flow
// Strictly adheres to design/dls.tokens.json and zero-anxiety UI principles.
// Strictly ZERO payment features, ZERO currency, ZERO global region selector.

import { renderLayout } from "../ui/layout.ts";
import { renderIcon } from "../ui/icons.ts";

export function renderHomePage(): string {
  const extraHead = `
    <style>
      /* Hero Layout */
      .hero-section {
        padding: clamp(48px, 7vw, 92px) 0 44px;
        text-align: center;
      }
      .hero-content {
        max-width: 860px;
        margin: 0 auto;
      }
      .hero-badge {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        background: var(--surface-card);
        border: 1px solid var(--border-strong);
        border-radius: var(--radius-pill);
        padding: 8px 18px;
        font-size: 13px;
        font-weight: 700;
        box-shadow: var(--shadow-tactile);
        margin-bottom: 24px;
      }
      .hero-title {
        font-size: clamp(38px, 6.2vw, 68px);
        line-height: 1.08;
        letter-spacing: -0.03em;
        margin-bottom: 24px;
      }
      .hero-highlight {
        display: inline-block;
        background: var(--state-calm-soft);
        color: var(--state-calm-ink);
        padding: 0 12px;
        border-radius: var(--radius-md);
        border: 1px solid var(--state-calm-chip);
      }
      .hero-sub {
        font-size: clamp(17px, 2.2vw, 20.5px);
        line-height: 1.6;
        color: var(--text-secondary);
        max-width: 740px;
        margin: 0 auto 36px;
      }
      .hero-actions {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        justify-content: center;
        gap: 16px;
        margin-bottom: 54px;
      }

      /* Stat Ribbon */
      .stat-ribbon {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 18px;
        background: var(--surface-card);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-card);
        padding: 28px 24px;
        box-shadow: var(--shadow-card);
        margin-bottom: 80px;
        text-align: left;
      }
      .stat-box {
        display: flex;
        flex-direction: column;
        gap: 6px;
        padding: 0 12px;
        border-right: 1px dashed var(--border-subtle);
      }
      .stat-box:last-child {
        border-right: none;
      }
      .stat-val {
        font-family: var(--font-serif);
        font-size: 34px;
        font-weight: 700;
        color: var(--text-primary);
        letter-spacing: -0.02em;
        line-height: 1;
      }
      .stat-label {
        font-size: 13.5px;
        color: var(--text-secondary);
        font-weight: 600;
      }

      /* Section Common */
      .section-wrap {
        padding: 72px 0;
      }
      .section-header {
        text-align: center;
        max-width: 680px;
        margin: 0 auto 48px;
      }
      .section-badge {
        margin-bottom: 14px;
      }
      .section-title {
        font-size: clamp(28px, 4vw, 42px);
        margin-bottom: 16px;
      }
      .section-sub {
        font-size: 16.5px;
        color: var(--text-secondary);
      }

      /* Interactive Simulator */
      .sim-stage {
        background: var(--surface-card);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-xl);
        padding: clamp(24px, 4vw, 44px);
        box-shadow: var(--shadow-card);
      }
      .sim-dashboard {
        display: grid;
        grid-template-columns: 340px 1fr;
        gap: 32px;
      }
      .sim-panel {
        background: var(--surface-bg);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-card);
        padding: 24px;
        display: flex;
        flex-direction: column;
        gap: 20px;
      }
      .stack-emblem-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 20px;
        background: var(--action-primary);
        border-radius: var(--radius-card);
        color: #fff;
      }
      .stack-visual {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 10px;
        width: 100%;
        max-width: 240px;
        margin: 18px 0;
      }
      .stack-bar {
        height: 14px;
        border-radius: 7px;
        transition: all 0.3s cubic-bezier(0.2, 0.8, 0.4, 1);
      }
      .stack-bar-focus {
        background: #4A7C59;
        width: 65%;
      }
      .stack-bar-active {
        background: #FCFBF8;
        width: 100%;
      }
      .stack-bar-parked {
        background: #F2F0EA;
        width: 80%;
      }
      .stack-bar-coral {
        background: #C84B31;
        width: 45%;
        opacity: 0.25;
      }
      .stack-bar-coral.alert-on {
        opacity: 1;
        box-shadow: 0 0 12px rgba(200, 75, 49, 0.8);
      }

      .stepper-control {
        display: flex;
        align-items: center;
        justify-content: space-between;
        background: var(--surface-card);
        border: 1px solid var(--border-strong);
        border-radius: var(--radius-pill);
        padding: 6px 14px;
        box-shadow: var(--shadow-tactile);
      }
      .stepper-btn {
        background: none;
        border: none;
        font-size: 20px;
        font-weight: bold;
        color: var(--text-primary);
        cursor: pointer;
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
      }
      .stepper-btn:hover {
        background: var(--surface-raised);
      }

      /* Simulator Kanban Columns */
      .sim-columns {
        display: grid;
        grid-template-columns: 1fr;
        gap: 16px;
      }
      .sim-column {
        background: var(--surface-bg);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-card);
        padding: 16px 18px;
      }
      .sim-col-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 12px;
        padding-bottom: 8px;
        border-bottom: 1px dashed var(--border-subtle);
      }
      .sim-col-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 14.5px;
        font-weight: 700;
      }
      .sim-count-pill {
        padding: 2px 8px;
        border-radius: var(--radius-pill);
        font-size: 12px;
        font-weight: 700;
      }

      .thought-item {
        background: var(--surface-card);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-md);
        padding: 12px 14px;
        margin-bottom: 10px;
        box-shadow: 0 2px 6px rgba(0,0,0,0.03);
        display: flex;
        flex-direction: column;
        gap: 8px;
        transition: transform 0.15s ease;
      }
      .thought-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .thought-name {
        font-size: 14.5px;
        font-weight: 600;
        color: var(--text-primary);
      }
      .thought-actions {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
      }
      .action-pill {
        background: var(--surface-raised);
        border: 1px solid var(--border-subtle);
        padding: 4px 10px;
        border-radius: var(--radius-pill);
        font-size: 11.5px;
        font-weight: 700;
        cursor: pointer;
        transition: all 0.12s ease;
      }
      .action-pill:hover {
        border-color: var(--text-primary);
        background: var(--surface-card);
      }

      /* Focus Timer Widget */
      .focus-session-card {
        background: #1C1814;
        color: #FBF6EC;
        border-radius: var(--radius-card);
        padding: 24px;
        margin-top: 18px;
        text-align: center;
      }
      .focus-timer-display {
        font-family: var(--font-mono);
        font-size: 42px;
        font-weight: 700;
        letter-spacing: -0.03em;
        margin: 12px 0;
        color: #86EFAC;
      }

      /* 4 Stages Grid */
      .stages-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
        gap: 20px;
      }
      .stage-card {
        background: var(--surface-card);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-card);
        padding: 28px 24px;
        box-shadow: var(--shadow-card);
        display: flex;
        flex-direction: column;
        gap: 14px;
      }
      .stage-header-row {
        display: flex;
        align-items: center;
        gap: 12px;
      }
      .stage-icon-circle {
        width: 44px;
        height: 44px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      /* Architecture Grid */
      .arch-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 24px;
      }
      .arch-card {
        background: var(--surface-card);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-card);
        padding: 32px 28px;
        box-shadow: var(--shadow-card);
      }
      .arch-list {
        list-style: none;
        display: flex;
        flex-direction: column;
        gap: 12px;
        margin-top: 18px;
      }
      .arch-list li {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        font-size: 14.5px;
      }

      /* Screenshots Gallery */
      .gallery-container {
        display: flex;
        gap: 20px;
        overflow-x: auto;
        padding-bottom: 24px;
        scroll-snap-type: x mandatory;
        -webkit-overflow-scrolling: touch;
      }
      .gallery-item {
        flex: 0 0 280px;
        scroll-snap-align: start;
        border-radius: var(--radius-card);
        overflow: hidden;
        border: 1px solid var(--border-strong);
        background: var(--surface-card);
        box-shadow: var(--shadow-raised);
        transition: transform 0.2s ease;
      }
      .gallery-item:hover {
        transform: translateY(-4px);
      }
      .gallery-item img {
        width: 100%;
        height: auto;
        display: block;
      }
      .gallery-caption {
        padding: 14px 16px;
        font-size: 13.5px;
        font-weight: 600;
        text-align: center;
      }

      /* FAQ Accordion */
      .faq-list {
        max-width: 820px;
        margin: 0 auto;
        display: flex;
        flex-direction: column;
        gap: 14px;
      }
      .faq-item {
        background: var(--surface-card);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-md);
        box-shadow: var(--shadow-card);
        overflow: hidden;
      }
      .faq-summary {
        padding: 20px 24px;
        font-size: 16.5px;
        font-weight: 700;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: space-between;
        user-select: none;
      }
      .faq-content {
        padding: 0 24px 20px;
        font-size: 15px;
        line-height: 1.6;
        color: var(--text-secondary);
      }

      /* Download Card */
      .download-box {
        background: var(--action-primary);
        color: var(--action-onPrimary);
        border-radius: var(--radius-xl);
        padding: clamp(36px, 6vw, 64px);
        text-align: center;
        box-shadow: var(--shadow-raised);
      }
      .download-box h2 {
        color: #FBF6EC;
        font-size: clamp(32px, 5vw, 48px);
        margin-bottom: 16px;
      }
      .download-box p {
        color: #DAD4C8;
        max-width: 600px;
        margin: 0 auto 36px;
        font-size: 17px;
      }
      .download-actions-grid {
        display: flex;
        flex-wrap: wrap;
        gap: 16px;
        justify-content: center;
      }

      @media (max-width: 900px) {
        .sim-dashboard {
          grid-template-columns: 1fr;
        }
        .arch-grid {
          grid-template-columns: 1fr;
        }
      }
    </style>
  `;

  const content = `
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <div class="hero-badge">
            ${renderIcon("brain", 16)}
            <span>100% On-Device Cognitive Load Balancer</span>
          </div>

          <h1 class="hero-title">
            Unstick Your Thoughts.<br/>
            <span class="hero-highlight">Externalize Working Memory.</span>
          </h1>

          <p class="hero-sub">
            A serene Scandinavian cognitive load manager for iOS and Android. Cap your active mental bandwidth, prevent open-loop anxiety, and flow effortlessly across 4 deterministic thought stages—strictly zero-cloud, 100% private.
          </p>

          <div class="hero-actions">
            <a href="#download" class="btn btn-primary">
              ${renderIcon("download", 18)}
              <span>Download Stuck Free</span>
            </a>
            <a href="#simulator" class="btn btn-outline">
              ${renderIcon("slider", 18)}
              <span>Test Headspace Simulator ↓</span>
            </a>
          </div>

          <!-- Stat Ribbon -->
          <div class="stat-ribbon">
            <div class="stat-box">
              <span class="stat-val">3</span>
              <span class="stat-label">Working Memory Limit (Cowan 4 ± 1)</span>
            </div>
            <div class="stat-box">
              <span class="stat-val">4</span>
              <span class="stat-label">Deterministic Thought Stages</span>
            </div>
            <div class="stat-box">
              <span class="stat-val">100%</span>
              <span class="stat-label">On-Device Zero-Cloud Storage</span>
            </div>
            <div class="stat-box">
              <span class="stat-val">Free</span>
              <span class="stat-label">Open-Source & Sovereign</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Interactive Headspace Simulator Section -->
    <section id="simulator" class="section-wrap" style="background: var(--surface-raised);">
      <div class="container">
        <div class="section-header">
          <div class="badge badge-neutral section-badge">
            ${renderIcon("slider", 14)}
            <span>Interactive Laboratory</span>
          </div>
          <h2 class="section-title">Experience Cognitive Balancing Live</h2>
          <p class="section-sub">
            See how Stuck caps active thoughts to preserve mental clarity. Adjust your capacity threshold, add contexts to working memory, and observe the signature Stack-of-Bars alert trigger when overloaded.
          </p>
        </div>

        <div class="sim-stage">
          <div class="sim-dashboard">
            <!-- Left Controls & Stack-of-Bars Preview -->
            <div class="sim-panel">
              <div class="stack-emblem-container">
                <span style="font-size: 11px; text-transform: uppercase; letter-spacing: 0.1em; color: #DAD4C8; font-weight: 700;">Emblem Architecture</span>
                
                <div class="stack-visual">
                  <div class="stack-bar stack-bar-focus" title="Focused Context"></div>
                  <div class="stack-bar stack-bar-active" title="Active Context in Working Memory"></div>
                  <div class="stack-bar stack-bar-parked" title="Parked Context"></div>
                  <div id="stack-coral-bar" class="stack-bar stack-bar-coral" title="Cognitive Overflow Warning"></div>
                </div>

                <div id="status-chip" class="badge badge-calm" style="margin-top: 6px;">
                  ${renderIcon("calm-sage", 14)}
                  <span id="status-chip-text">Headspace Calm</span>
                </div>
              </div>

              <div>
                <label style="font-size: 13.5px; font-weight: 700; display: block; margin-bottom: 8px;">
                  Active Capacity Limit: <span id="limit-display" style="font-family: var(--font-mono); font-size: 16px;">3</span>
                </label>
                <div class="stepper-control">
                  <button type="button" class="stepper-btn" id="btn-dec-limit" aria-label="Decrease capacity limit">−</button>
                  <span style="font-size: 13px; font-weight: 600; color: var(--text-secondary);">Thoughts In Head</span>
                  <button type="button" class="stepper-btn" id="btn-inc-limit" aria-label="Increase capacity limit">+</button>
                </div>
                <p style="font-size: 12px; color: var(--text-muted); margin-top: 6px;">
                  Miller (1956) observed 7 ± 2 chunks, while Cowan (2001) proved working memory saturates at 3 to 4 items without swapping.
                </p>
              </div>

              <div>
                <label style="font-size: 13.5px; font-weight: 700; display: block; margin-bottom: 8px;">
                  Externalize a New Thought:
                </label>
                <div style="display: flex; gap: 8px;">
                  <input type="text" id="thought-input" placeholder="e.g., Review architecture PR..." style="flex: 1; padding: 10px 14px; border: 1px solid var(--border-strong); border-radius: var(--radius-pill); font-family: inherit; font-size: 13.5px; background: var(--surface-card); color: var(--text-primary); outline: none;"/>
                  <button type="button" id="btn-add-thought" class="btn btn-primary btn-sm">
                    ${renderIcon("brain", 14)}
                    <span>Add</span>
                  </button>
                </div>
                <div style="display: flex; flex-wrap: wrap; gap: 6px; margin-top: 10px;">
                  <button type="button" class="quick-chip" data-sample="Review security audit report" style="background: none; border: 1px dashed var(--border-strong); padding: 4px 10px; border-radius: var(--radius-pill); font-size: 11.5px; cursor: pointer; color: var(--text-secondary);">+ Security Audit</button>
                  <button type="button" class="quick-chip" data-sample="Prepare keynote slides" style="background: none; border: 1px dashed var(--border-strong); padding: 4px 10px; border-radius: var(--radius-pill); font-size: 11.5px; cursor: pointer; color: var(--text-secondary);">+ Keynote Slides</button>
                  <button type="button" class="quick-chip" data-sample="Doctor follow-up booking" style="background: none; border: 1px dashed var(--border-strong); padding: 4px 10px; border-radius: var(--radius-pill); font-size: 11.5px; cursor: pointer; color: var(--text-secondary);">+ Doctor Follow-up</button>
                </div>
              </div>

              <!-- Distraction-free Focus Session Widget -->
              <div class="focus-session-card">
                <div style="display: flex; align-items: center; justify-content: space-between; font-size: 12px; text-transform: uppercase; letter-spacing: 0.08em; color: #DAD4C8;">
                  <span>${renderIcon("timer", 14)} Focus Session</span>
                  <span id="focus-task-name" style="color: #86EFAC; font-weight: 700; max-width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">Deep Work</span>
                </div>
                <div id="timer-display" class="focus-timer-display">25:00</div>
                <div style="display: flex; gap: 8px; justify-content: center;">
                  <button type="button" id="btn-timer-toggle" class="btn btn-primary btn-sm" style="background: #FCFBF8; color: #1C1814;">
                    ${renderIcon("play", 14)}
                    <span id="timer-btn-text">Start Focus</span>
                  </button>
                  <button type="button" id="btn-timer-reset" class="btn btn-outline btn-sm" style="background: transparent; color: #FBF6EC; border-color: rgba(255,255,255,0.2);">
                    ${renderIcon("reset", 14)}
                  </button>
                </div>
              </div>

              <button type="button" id="btn-reset-demo" class="btn btn-outline btn-sm" style="align-self: center;">
                ${renderIcon("reset", 14)}
                <span>Reset Demo State</span>
              </button>
            </div>

            <!-- Right Kanban Stages Preview -->
            <div class="sim-columns">
              <!-- Active: In Your Head -->
              <div class="sim-column" style="border-left: 4px solid var(--stage-active);">
                <div class="sim-col-header">
                  <div class="sim-col-title" style="color: var(--stage-active-ink);">
                    ${renderIcon("in-head", 18)}
                    <span>In Your Head (Active Memory)</span>
                  </div>
                  <span id="in-head-count-pill" class="sim-count-pill" style="background: var(--stage-active-soft); color: var(--stage-active-ink);">2 of 3</span>
                </div>
                <div id="in-head-list"></div>
              </div>

              <!-- Parked -->
              <div class="sim-column" style="border-left: 4px solid var(--stage-parked);">
                <div class="sim-col-header">
                  <div class="sim-col-title" style="color: var(--stage-parked-ink);">
                    ${renderIcon("parked", 18)}
                    <span>Parked (Safe Holding)</span>
                  </div>
                  <span id="parked-count-pill" class="sim-count-pill" style="background: var(--stage-parked-soft); color: var(--stage-parked-ink);">1</span>
                </div>
                <div id="parked-list"></div>
              </div>

              <!-- Coming Up -->
              <div class="sim-column" style="border-left: 4px solid var(--stage-planned);">
                <div class="sim-col-header">
                  <div class="sim-col-title" style="color: var(--stage-planned-ink);">
                    ${renderIcon("coming-up", 18)}
                    <span>Coming Up (Queued)</span>
                  </div>
                  <span id="coming-count-pill" class="sim-count-pill" style="background: var(--stage-planned-soft); color: var(--stage-planned-ink);">1</span>
                </div>
                <div id="coming-list"></div>
              </div>

              <!-- Closed -->
              <div class="sim-column" style="border-left: 4px solid var(--stage-done);">
                <div class="sim-col-header">
                  <div class="sim-col-title" style="color: var(--stage-done-ink);">
                    ${renderIcon("closed", 18)}
                    <span>Closed (Done)</span>
                  </div>
                  <span id="closed-count-pill" class="sim-count-pill" style="background: var(--stage-done-soft); color: var(--stage-done-ink);">1</span>
                </div>
                <div id="closed-list"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 4-Stage Thought Flow Deep Dive -->
    <section id="stages" class="section-wrap">
      <div class="container">
        <div class="section-header">
          <div class="badge badge-neutral section-badge">
            ${renderIcon("stack-bars", 14)}
            <span>Cognitive Lifecycle</span>
          </div>
          <h2 class="section-title">The 4 Deterministic Thought Stages</h2>
          <p class="section-sub">
            Unconscious mental loops steal working memory. Moving a context into its exact biological holding container terminates cognitive rehearsals instantly.
          </p>
        </div>

        <div class="stages-grid">
          <!-- Stage 1 -->
          <div class="stage-card">
            <div class="stage-header-row">
              <div class="stage-icon-circle" style="background: var(--stage-planned-soft); color: var(--stage-planned-ink);">
                ${renderIcon("coming-up", 24)}
              </div>
              <div>
                <h3 style="font-size: 20px;">1. Coming Up</h3>
                <span style="font-size: 12.5px; font-weight: 700; color: var(--stage-planned-ink);">Lilac Purple (#A765BA)</span>
              </div>
            </div>
            <p style="font-size: 14.5px; line-height: 1.6;">
              Thoughts, obligations, and appointments scheduled for tomorrow or queued for the upcoming week. They rest quietly outside your active mental headspace today.
            </p>
          </div>

          <!-- Stage 2 -->
          <div class="stage-card">
            <div class="stage-header-row">
              <div class="stage-icon-circle" style="background: var(--stage-active-soft); color: var(--stage-active-ink);">
                ${renderIcon("in-head", 24)}
              </div>
              <div>
                <h3 style="font-size: 20px;">2. In Your Head</h3>
                <span style="font-size: 12.5px; font-weight: 700; color: var(--stage-active-ink);">Task Blue (#4B8BF5)</span>
              </div>
            </div>
            <p style="font-size: 14.5px; line-height: 1.6;">
              Your focal zone today. Strictly capped by your cognitive capacity limit (default 3). When you attempt to juggle 4 or more, the app alerts you to pause and triage.
            </p>
          </div>

          <!-- Stage 3 -->
          <div class="stage-card">
            <div class="stage-header-row">
              <div class="stage-icon-circle" style="background: var(--stage-parked-soft); color: var(--stage-parked-ink);">
                ${renderIcon("parked", 24)}
              </div>
              <div>
                <h3 style="font-size: 20px;">3. Parked</h3>
                <span style="font-size: 12.5px; font-weight: 700; color: var(--stage-parked-ink);">Warm Amber (#F59E0B)</span>
              </div>
            </div>
            <p style="font-size: 14.5px; line-height: 1.6;">
              Safely externalized thoughts. You are not working on them right now, but they are safely stored so your amygdala knows they won't be lost.
            </p>
          </div>

          <!-- Stage 4 -->
          <div class="stage-card">
            <div class="stage-header-row">
              <div class="stage-icon-circle" style="background: var(--stage-done-soft); color: var(--stage-done-ink);">
                ${renderIcon("closed", 24)}
              </div>
              <div>
                <h3 style="font-size: 20px;">4. Closed</h3>
                <span style="font-size: 12.5px; font-weight: 700; color: var(--stage-done-ink);">Sage Green (#7FB844)</span>
              </div>
            </div>
            <p style="font-size: 14.5px; line-height: 1.6;">
              Finished, checked off, and cleared. Your total uninterrupted focus minutes are archived, rewarding your deep attention without superficial badges.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Architecture & Features -->
    <section id="features" class="section-wrap" style="background: var(--surface-card); border-top: 1px solid var(--border-subtle); border-bottom: 1px solid var(--border-subtle);">
      <div class="container">
        <div class="section-header">
          <div class="badge badge-neutral section-badge">
            ${renderIcon("shield", 14)}
            <span>Dual-Native Engineering</span>
          </div>
          <h2 class="section-title">Built with Zero-Cloud Sovereignty</h2>
          <p class="section-sub">
            Engineered exclusively with native Swift on Apple and native Jetpack Compose on Android. Your thoughts remain 100% on your hardware.
          </p>
        </div>

        <div class="arch-grid">
          <div class="arch-card">
            <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 16px;">
              ${renderIcon("apple", 28)}
              <div>
                <h3 style="font-size: 22px;">iOS Native Architecture</h3>
                <span style="font-size: 13px; color: var(--text-muted); font-weight: 600;">Swift 6 • SwiftUI • Face ID</span>
              </div>
            </div>
            <p style="font-size: 15px; line-height: 1.6;">
              Built using Apple's modern Swift Concurrency and SwiftUI framework. Leverages LocalAuthentication for instantaneous Face ID and Touch ID locking.
            </p>
            <ul class="arch-list">
              <li>
                <span style="color: var(--state-calm-ink);">${renderIcon("check", 16)}</span>
                <span><strong>Zero-Cloud Persistence:</strong> 100% on-device local storage.</span>
              </li>
              <li>
                <span style="color: var(--state-calm-ink);">${renderIcon("check", 16)}</span>
                <span><strong>Instant Background Lock:</strong> Closes biometric session on blur.</span>
              </li>
              <li>
                <span style="color: var(--state-calm-ink);">${renderIcon("check", 16)}</span>
                <span><strong>Haptic Engine:</strong> Fluid tactile feedback on card movement.</span>
              </li>
            </ul>
          </div>

          <div class="arch-card">
            <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 16px;">
              ${renderIcon("android", 28)}
              <div>
                <h3 style="font-size: 22px;">Android Native Architecture</h3>
                <span style="font-size: 13px; color: var(--text-muted); font-weight: 600;">Kotlin 2.0 • Jetpack Compose • BiometricPrompt</span>
              </div>
            </div>
            <p style="font-size: 15px; line-height: 1.6;">
              Rendered with 100% Jetpack Compose declarative UI and Material 3 design tokens. Encrypted SharedPreferences ensure zero cloud exposure.
            </p>
            <ul class="arch-list">
              <li>
                <span style="color: var(--state-calm-ink);">${renderIcon("check", 16)}</span>
                <span><strong>BiometricPrompt Integration:</strong> Fingerprint & Face Unlock.</span>
              </li>
              <li>
                <span style="color: var(--state-calm-ink);">${renderIcon("check", 16)}</span>
                <span><strong>Zero Network Telemetry:</strong> No tracking SDKs or background beacons.</span>
              </li>
              <li>
                <span style="color: var(--state-calm-ink);">${renderIcon("check", 16)}</span>
                <span><strong>Predictive Back Animation:</strong> Fluid Scandinavian spring physics.</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- Visual App Showcase / Screenshots -->
    <section class="section-wrap">
      <div class="container">
        <div class="section-header">
          <div class="badge badge-neutral section-badge">
            ${renderIcon("sparkles", 14)}
            <span>Visual Craft</span>
          </div>
          <h2 class="section-title">Scandinavian Minimalism in Action</h2>
          <p class="section-sub">
            Explore the calm interface: high-contrast typography on warm oat surfaces, custom modal popups, and instant cognitive limit controls.
          </p>
        </div>

        <div class="gallery-container">
          <div class="gallery-item">
            <img src="/assets/screenshots/01_landing_brand.png" alt="Stuck Landing & Brand Screen" loading="lazy"/>
            <div class="gallery-caption">1-Tap Fast Access & Guest Mode</div>
          </div>
          <div class="gallery-item">
            <img src="/assets/screenshots/02_timeline_board.png" alt="Timeline Kanban Board" loading="lazy"/>
            <div class="gallery-caption">4-Stage Cognitive Timeline</div>
          </div>
          <div class="gallery-item">
            <img src="/assets/screenshots/03_cognitive_load_limiter.png" alt="Cognitive Load Limiter" loading="lazy"/>
            <div class="gallery-caption">Capacity Stepper & Sweet Spot</div>
          </div>
          <div class="gallery-item">
            <img src="/assets/screenshots/04_capacity_overflow_warning.png" alt="Capacity Overflow Warning" loading="lazy"/>
            <div class="gallery-caption">Alert Coral Capacity Warning</div>
          </div>
          <div class="gallery-item">
            <img src="/assets/screenshots/05_new_thought_sheet.png" alt="New Thought Custom Modal" loading="lazy"/>
            <div class="gallery-caption">Tactile DLS Modal Sheet</div>
          </div>
          <div class="gallery-item">
            <img src="/assets/screenshots/06_deep_focus_session.png" alt="Deep Focus Session Timer" loading="lazy"/>
            <div class="gallery-caption">Distraction-Free Focus Timer</div>
          </div>
          <div class="gallery-item">
            <img src="/assets/screenshots/07_profile_and_limits.png" alt="Profile and Capacity Limit Settings" loading="lazy"/>
            <div class="gallery-caption">Profile & Biometric Security</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Zero-Cloud Sovereignty Declaration -->
    <section id="privacy" class="section-wrap" style="background: var(--surface-card); border-top: 1px solid var(--border-subtle); border-bottom: 1px solid var(--border-subtle);">
      <div class="container" style="max-width: 860px; text-align: center;">
        <div class="badge badge-calm section-badge">
          ${renderIcon("lock", 14)}
          <span>Strict Zero-Cloud Privacy Guarantee</span>
        </div>
        <h2 class="section-title">Your Mind Belongs to You. Not an Advertising Cloud.</h2>
        <p style="font-size: 17px; line-height: 1.7; color: var(--text-secondary); margin-bottom: 32px;">
          Most productivity tools monetize your habits by logging your tasks, profiling your working patterns, and storing personal thoughts on shared cloud infrastructure. Stuck was created on an unshakeable sovereign principle: <strong>100% on-device execution</strong>.
        </p>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 18px; text-align: left; margin-bottom: 32px;">
          <div style="background: var(--surface-bg); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); padding: 18px;">
            <div style="color: var(--state-calm-ink); margin-bottom: 6px;">${renderIcon("check", 18)}</div>
            <strong style="display: block; font-size: 14.5px; margin-bottom: 4px;">Zero Remote Databases</strong>
            <span style="font-size: 13px; color: var(--text-secondary);">Your thoughts are stored only in local device sandbox files.</span>
          </div>
          <div style="background: var(--surface-bg); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); padding: 18px;">
            <div style="color: var(--state-calm-ink); margin-bottom: 6px;">${renderIcon("check", 18)}</div>
            <strong style="display: block; font-size: 14.5px; margin-bottom: 4px;">Zero Network Telemetry</strong>
            <span style="font-size: 13px; color: var(--text-secondary);">No Google Analytics, no Firebase Crashlytics, no Mixpanel beacons.</span>
          </div>
          <div style="background: var(--surface-bg); border: 1px solid var(--border-subtle); border-radius: var(--radius-md); padding: 18px;">
            <div style="color: var(--state-calm-ink); margin-bottom: 6px;">${renderIcon("check", 18)}</div>
            <strong style="display: block; font-size: 14.5px; margin-bottom: 4px;">Biometric Face ID Lock</strong>
            <span style="font-size: 13px; color: var(--text-secondary);">Local hardware enclave protects against physical device snooping.</span>
          </div>
        </div>

        <a href="/privacy" class="btn btn-outline btn-sm">
          <span>Read Full Sovereign Privacy Policy →</span>
        </a>
      </div>
    </section>

    <!-- FAQ Section -->
    <section class="section-wrap">
      <div class="container">
        <div class="section-header">
          <div class="badge badge-neutral section-badge">
            ${renderIcon("sparkles", 14)}
            <span>Clarity & Answers</span>
          </div>
          <h2 class="section-title">Frequently Asked Questions</h2>
          <p class="section-sub">
            Learn why capping working memory creates sustained clarity.
          </p>
        </div>

        <div class="faq-list">
          <details class="faq-item" open>
            <summary class="faq-summary">
              <span>Why limit active tasks to 3 instead of an infinite to-do list?</span>
              <span style="color: var(--text-muted); font-size: 20px;">+</span>
            </summary>
            <div class="faq-content">
              Cognitive neuroscience proves that working memory operates like short-term RAM, not a hard drive. Nelson Cowan’s research (2001) established that humans can actively hold between 3 and 5 meaningful chunks of context simultaneously. When you keep 20 tasks "in progress", your brain constantly expends metabolic glucose rehearsing open loops, inducing mental fatigue and task paralysis. Stuck forces a biological limit so you complete before you consume.
            </div>
          </details>

          <details class="faq-item">
            <summary class="faq-summary">
              <span>How does Stuck differ from Things 3 or Todoist?</span>
              <span style="color: var(--text-muted); font-size: 20px;">+</span>
            </summary>
            <div class="faq-content">
              Traditional productivity tools are passive task backlogs that encourage endless accumulation. Stuck is an active cognitive load balancer. It actively alerts you when you cross your defined capacity threshold, gives you tactile "Park" gestures to externalize thoughts, and provides zero-cloud privacy without requiring recurring accounts.
            </div>
          </details>

          <details class="faq-item">
            <summary class="faq-summary">
              <span>Is my data truly 100% on-device and private?</span>
              <span style="color: var(--text-muted); font-size: 20px;">+</span>
            </summary>
            <div class="faq-content">
              Yes. Stuck has no remote database servers and collects zero telemetry. Your contexts, focus session records, and settings live strictly inside your phone’s encrypted sandbox storage (UserDefaults on iOS and SharedPreferences on Android). You can turn on Airplane mode and Stuck operates with 100% functionality.
            </div>
          </details>

          <details class="faq-item">
            <summary class="faq-summary">
              <span>Is Stuck completely free?</span>
              <span style="color: var(--text-muted); font-size: 20px;">+</span>
            </summary>
            <div class="faq-content">
              Yes, Stuck is 100% free and open-source software built by Mata Panda Studio. There are no paywalls, no recurring costs, and no locked features.
            </div>
          </details>

          <details class="faq-item">
            <summary class="faq-summary">
              <span>Can I customize my cognitive capacity limit?</span>
              <span style="color: var(--text-muted); font-size: 20px;">+</span>
            </summary>
            <div class="faq-content">
              Yes. In the Profile screen, you can adjust your active limit anywhere from 1 to 9 contexts based on your daily cognitive bandwidth. The default recommendation is 3 contexts.
            </div>
          </details>
        </div>
      </div>
    </section>

    <!-- Download Section -->
    <section id="download" class="section-wrap" style="padding-top: 0;">
      <div class="container">
        <div class="download-box">
          <div class="badge badge-calm" style="margin-bottom: 20px;">
            ${renderIcon("download", 14)}
            <span>Direct Distribution</span>
          </div>
          <h2>Unstick Your Mind Today.</h2>
          <p>
            Experience the calm of capped working memory. Native iOS & Android builds available directly through official stores and sovereign sideloading.
          </p>

          <div class="download-actions-grid">
            <a href="https://github.com/mtpnda/stuck/releases/latest" class="btn btn-primary" style="background: #FCFBF8; color: #1C1814;">
              ${renderIcon("apple", 20)}
              <span>Download for iOS (App Store)</span>
            </a>
            <a href="https://github.com/mtpnda/stuck/releases/latest" class="btn btn-primary" style="background: #FCFBF8; color: #1C1814;">
              ${renderIcon("android", 20)}
              <span>Download for Android (Google Play)</span>
            </a>
            <a href="https://github.com/mtpnda/stuck" target="_blank" rel="noopener" class="btn btn-outline" style="background: transparent; color: #FBF6EC; border-color: rgba(255,255,255,0.3);">
              ${renderIcon("github", 20)}
              <span>View Source on GitHub</span>
            </a>
          </div>

          <div style="margin-top: 32px; font-size: 13px; color: #DAD4C8;">
            Package ID: <code>com.mtpnda.stuck</code> • Dual-Native Swift 6 & Jetpack Compose 2.0 • 100% On-Device
          </div>
        </div>
      </div>
    </section>
  `;

  // Interactive JavaScript for Headspace Simulator and Focus Timer
  const scripts = `
    <script>
      (function() {
        // Initial Demo State
        let capacityLimit = 3;
        let thoughts = [
          { id: '1', title: 'Prepare Q4 Product Vision Deck', stage: 'in-head', isFocused: true },
          { id: '2', title: 'Refactor DLS Modal spring physics', stage: 'in-head', isFocused: false },
          { id: '3', title: 'Fix Android 15 edge-to-edge padding', stage: 'parked', isFocused: false },
          { id: '4', title: 'Review biometrics fallback on iOS 18', stage: 'coming-up', isFocused: false },
          { id: '5', title: 'Migrate to unified store.yaml schema', stage: 'closed', isFocused: false }
        ];

        // Focus Timer State
        let timerSeconds = 25 * 60;
        let timerInterval = null;
        let isRunning = false;

        const limitDisplay = document.getElementById('limit-display');
        const coralBar = document.getElementById('stack-coral-bar');
        const statusChip = document.getElementById('status-chip');
        const statusChipText = document.getElementById('status-chip-text');

        const inHeadList = document.getElementById('in-head-list');
        const parkedList = document.getElementById('parked-list');
        const comingList = document.getElementById('coming-list');
        const closedList = document.getElementById('closed-list');

        const inHeadCountPill = document.getElementById('in-head-count-pill');
        const parkedCountPill = document.getElementById('parked-count-pill');
        const comingCountPill = document.getElementById('coming-count-pill');
        const closedCountPill = document.getElementById('closed-count-pill');

        const thoughtInput = document.getElementById('thought-input');
        const btnAddThought = document.getElementById('btn-add-thought');
        const btnDecLimit = document.getElementById('btn-dec-limit');
        const btnIncLimit = document.getElementById('btn-inc-limit');
        const btnResetDemo = document.getElementById('btn-reset-demo');

        const timerDisplay = document.getElementById('timer-display');
        const btnTimerToggle = document.getElementById('btn-timer-toggle');
        const btnTimerReset = document.getElementById('btn-timer-reset');
        const timerBtnText = document.getElementById('timer-btn-text');
        const focusTaskName = document.getElementById('focus-task-name');

        function renderLists() {
          limitDisplay.textContent = capacityLimit;

          const inHeadItems = thoughts.filter(t => t.stage === 'in-head');
          const parkedItems = thoughts.filter(t => t.stage === 'parked');
          const comingItems = thoughts.filter(t => t.stage === 'coming-up');
          const closedItems = thoughts.filter(t => t.stage === 'closed');

          inHeadCountPill.textContent = inHeadItems.length + ' of ' + capacityLimit;
          parkedCountPill.textContent = parkedItems.length;
          comingCountPill.textContent = comingItems.length;
          closedCountPill.textContent = closedItems.length;

          // Check cognitive load overload
          const isOverloaded = inHeadItems.length > capacityLimit;
          if (isOverloaded) {
            coralBar.classList.add('alert-on');
            statusChip.className = 'badge badge-alert';
            statusChipText.textContent = 'Cognitive Overload (' + inHeadItems.length + '/' + capacityLimit + ')';
            inHeadCountPill.style.background = 'var(--state-alert-soft)';
            inHeadCountPill.style.color = 'var(--state-alert-ink)';
          } else {
            coralBar.classList.remove('alert-on');
            statusChip.className = 'badge badge-calm';
            statusChipText.textContent = 'Headspace Calm';
            inHeadCountPill.style.background = 'var(--stage-active-soft)';
            inHeadCountPill.style.color = 'var(--stage-active-ink)';
          }

          // Render Active items
          inHeadList.innerHTML = inHeadItems.map(item => 
            '<div class="thought-item" style="border-left: 3px solid var(--stage-active);">' +
              '<div class="thought-header">' +
                '<span class="thought-name">' + (item.isFocused ? '⭐️ ' : '') + item.title + '</span>' +
                '<span style="font-size: 11px; color: var(--stage-active-ink); font-weight: 700;">In Head</span>' +
              '</div>' +
              '<div class="thought-actions">' +
                '<button type="button" class="action-pill" onclick="window.setFocused(\'' + item.id + '\')">⏱️ Focus</button>' +
                '<button type="button" class="action-pill" onclick="window.moveThought(\'' + item.id + '\', \'parked\')">🟡 Park</button>' +
                '<button type="button" class="action-pill" onclick="window.moveThought(\'' + item.id + '\', \'closed\')">🟢 Close</button>' +
                '<button type="button" class="action-pill" onclick="window.moveThought(\'' + item.id + '\', \'coming-up\')">🟣 Queue</button>' +
              '</div>' +
            '</div>'
          ).join('') || '<div style="font-size: 13px; color: var(--text-muted); padding: 10px 0;">No active thoughts. Headspace is empty.</div>';

          // Render Parked items
          parkedList.innerHTML = parkedItems.map(item => 
            '<div class="thought-item" style="border-left: 3px solid var(--stage-parked);">' +
              '<div class="thought-header">' +
                '<span class="thought-name">' + item.title + '</span>' +
                '<span style="font-size: 11px; color: var(--stage-parked-ink); font-weight: 700;">Parked</span>' +
              '</div>' +
              '<div class="thought-actions">' +
                '<button type="button" class="action-pill" onclick="window.moveThought(\'' + item.id + '\', \'in-head\')">🔵 Unpark to Head</button>' +
                '<button type="button" class="action-pill" onclick="window.moveThought(\'' + item.id + '\', \'closed\')">🟢 Close</button>' +
              '</div>' +
            '</div>'
          ).join('') || '<div style="font-size: 13px; color: var(--text-muted); padding: 10px 0;">No parked thoughts.</div>';

          // Render Coming Up items
          comingList.innerHTML = comingItems.map(item => 
            '<div class="thought-item" style="border-left: 3px solid var(--stage-planned);">' +
              '<div class="thought-header">' +
                '<span class="thought-name">' + item.title + '</span>' +
                '<span style="font-size: 11px; color: var(--stage-planned-ink); font-weight: 700;">Tomorrow</span>' +
              '</div>' +
              '<div class="thought-actions">' +
                '<button type="button" class="action-pill" onclick="window.moveThought(\'' + item.id + '\', \'in-head\')">🔵 Bring into Head</button>' +
                '<button type="button" class="action-pill" onclick="window.moveThought(\'' + item.id + '\', \'parked\')">🟡 Park</button>' +
              '</div>' +
            '</div>'
          ).join('') || '<div style="font-size: 13px; color: var(--text-muted); padding: 10px 0;">Queue empty.</div>';

          // Render Closed items
          closedList.innerHTML = closedItems.map(item => 
            '<div class="thought-item" style="border-left: 3px solid var(--stage-done); opacity: 0.85;">' +
              '<div class="thought-header">' +
                '<span class="thought-name" style="text-decoration: line-through; color: var(--text-muted);">' + item.title + '</span>' +
                '<span style="font-size: 11px; color: var(--stage-done-ink); font-weight: 700;">✓ Done</span>' +
              '</div>' +
              '<div class="thought-actions">' +
                '<button type="button" class="action-pill" onclick="window.moveThought(\'' + item.id + '\', \'in-head\')">↩️ Reopen</button>' +
              '</div>' +
            '</div>'
          ).join('') || '<div style="font-size: 13px; color: var(--text-muted); padding: 10px 0;">No completed thoughts.</div>';

          // Update active focus name
          const focused = thoughts.find(t => t.isFocused && t.stage === 'in-head');
          if (focused) {
            focusTaskName.textContent = focused.title;
          } else if (inHeadItems.length > 0) {
            focusTaskName.textContent = inHeadItems[0].title;
          } else {
            focusTaskName.textContent = 'Deep Work';
          }
        }

        // Window-bound action handlers
        window.moveThought = function(id, targetStage) {
          const item = thoughts.find(t => t.id === id);
          if (item) {
            item.stage = targetStage;
            if (targetStage !== 'in-head') {
              item.isFocused = false;
            }
            renderLists();
          }
        };

        window.setFocused = function(id) {
          thoughts.forEach(t => {
            t.isFocused = (t.id === id);
          });
          renderLists();
        };

        // Add Thought
        function addThought(title) {
          const text = title || thoughtInput.value.trim();
          if (!text) return;
          thoughts.unshift({
            id: 't-' + Date.now(),
            title: text,
            stage: 'in-head',
            isFocused: thoughts.filter(t => t.stage === 'in-head').length === 0
          });
          thoughtInput.value = '';
          renderLists();
        }

        btnAddThought.addEventListener('click', () => addThought());
        thoughtInput.addEventListener('keydown', (e) => {
          if (e.key === 'Enter') addThought();
        });

        document.querySelectorAll('.quick-chip').forEach(chip => {
          chip.addEventListener('click', function() {
            addThought(this.getAttribute('data-sample'));
          });
        });

        // Capacity Stepper
        btnDecLimit.addEventListener('click', () => {
          if (capacityLimit > 1) {
            capacityLimit--;
            renderLists();
          }
        });

        btnIncLimit.addEventListener('click', () => {
          if (capacityLimit < 9) {
            capacityLimit++;
            renderLists();
          }
        });

        // Reset
        btnResetDemo.addEventListener('click', () => {
          capacityLimit = 3;
          thoughts = [
            { id: '1', title: 'Prepare Q4 Product Vision Deck', stage: 'in-head', isFocused: true },
            { id: '2', title: 'Refactor DLS Modal spring physics', stage: 'in-head', isFocused: false },
            { id: '3', title: 'Fix Android 15 edge-to-edge padding', stage: 'parked', isFocused: false },
            { id: '4', title: 'Review biometrics fallback on iOS 18', stage: 'coming-up', isFocused: false },
            { id: '5', title: 'Migrate to unified store.yaml schema', stage: 'closed', isFocused: false }
          ];
          renderLists();
        });

        // Focus Timer Logic
        function updateTimerDisplay() {
          const m = Math.floor(timerSeconds / 60);
          const s = timerSeconds % 60;
          timerDisplay.textContent = (m < 10 ? '0' : '') + m + ':' + (s < 10 ? '0' : '') + s;
        }

        btnTimerToggle.addEventListener('click', () => {
          if (isRunning) {
            clearInterval(timerInterval);
            isRunning = false;
            timerBtnText.textContent = 'Resume Focus';
          } else {
            isRunning = true;
            timerBtnText.textContent = 'Pause Focus';
            timerInterval = setInterval(() => {
              if (timerSeconds > 0) {
                timerSeconds--;
                updateTimerDisplay();
              } else {
                clearInterval(timerInterval);
                isRunning = false;
                timerBtnText.textContent = 'Completed!';
                alert('Serene focus session finished! Your working memory has completed this cycle.');
              }
            }, 1000);
          }
        });

        btnTimerReset.addEventListener('click', () => {
          clearInterval(timerInterval);
          isRunning = false;
          timerSeconds = 25 * 60;
          timerBtnText.textContent = 'Start Focus';
          updateTimerDisplay();
        });

        // Initial paint
        renderLists();
        updateTimerDisplay();
      })();
    </script>
  `;

  return renderLayout({
    title: "Stuck — Serene Cognitive Load Balancer & 4-Stage Task Flow",
    description: "Unstick your thoughts. A Scandinavian cognitive load balancer and mental context manager for iOS and Android. Cap active working memory, prevent open-loop anxiety, and keep 100% on-device privacy.",
    content,
    extraHead,
    scripts,
    activeTab: "home"
  });
}
