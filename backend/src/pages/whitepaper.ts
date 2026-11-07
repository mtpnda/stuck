// Stuck Cognitive Science & Architecture Whitepaper
// Mathematical formulations of working memory saturation and dual-native state machine.
// KaTeX rendering and Mermaid diagrams included.
// Strictly ZERO currency, ZERO payment, ZERO global region selector.

import { renderLayout } from "../ui/layout.ts";
import { renderIcon } from "../ui/icons.ts";

export function renderWhitepaperPage(): string {
  const extraHead = `
    <!-- KaTeX for Mathematical Formulations -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css"/>
    <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
    <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js"></script>

    <!-- Mermaid.js for Architectural State Diagrams -->
    <script type="module">
      import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs';
      mermaid.initialize({
        startOnLoad: true,
        theme: 'neutral',
        fontFamily: 'Atkinson Hyperlegible Next',
        themeVariables: {
          primaryColor: '#F7F4EE',
          primaryTextColor: '#191612',
          primaryBorderColor: '#DAD4C8',
          lineColor: '#544B3E'
        }
      });
    </script>

    <style>
      .paper-container {
        max-width: 820px;
        margin: 48px auto;
        padding: 0 24px;
      }
      .paper-header {
        text-align: center;
        margin-bottom: 56px;
        padding-bottom: 32px;
        border-bottom: 1px solid var(--border-subtle);
      }
      .paper-title {
        font-size: clamp(32px, 5vw, 48px);
        margin-bottom: 16px;
      }
      .paper-meta {
        font-size: 14px;
        color: var(--text-secondary);
        display: flex;
        justify-content: center;
        gap: 20px;
        flex-wrap: wrap;
      }
      .paper-body {
        font-size: 16.5px;
        line-height: 1.75;
        color: var(--text-secondary);
      }
      .paper-body h2 {
        font-size: 26px;
        margin: 48px 0 18px;
        color: var(--text-primary);
        border-bottom: 1px dashed var(--border-subtle);
        padding-bottom: 8px;
      }
      .paper-body h3 {
        font-size: 20px;
        margin: 28px 0 12px;
        color: var(--text-primary);
      }
      .paper-body p {
        margin-bottom: 20px;
      }
      .paper-body blockquote {
        border-left: 4px solid var(--border-strong);
        padding: 12px 20px;
        margin: 24px 0;
        background: var(--surface-card);
        border-radius: 0 var(--radius-md) var(--radius-md) 0;
        font-style: italic;
      }
      .math-box {
        background: var(--surface-card);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-md);
        padding: 24px;
        margin: 24px 0;
        text-align: center;
        box-shadow: var(--shadow-card);
        overflow-x: auto;
      }
      .diagram-box {
        background: var(--surface-card);
        border: 1px solid var(--border-subtle);
        border-radius: var(--radius-card);
        padding: 24px;
        margin: 32px 0;
        text-align: center;
        box-shadow: var(--shadow-card);
      }
    </style>
  `;

  const content = `
    <article class="paper-container">
      <div class="paper-header">
        <div class="badge badge-neutral" style="margin-bottom: 16px;">
          ${renderIcon("brain", 14)}
          <span>Technical Whitepaper & Cognitive Heuristics</span>
        </div>
        <h1 class="paper-title">Working Memory Capacity Regulation & Deterministic Context Offloading</h1>
        <div class="paper-meta">
          <span><strong>Mata Panda Studio</strong></span>
          <span>•</span>
          <span>Scandinavian Cognitive Engineering</span>
          <span>•</span>
          <span>App: Stuck (com.mtpnda.stuck)</span>
        </div>
      </div>

      <div class="paper-body">
        <blockquote>
          "The human working memory is not a bottomless hard drive. It is a strictly resource-bounded RAM buffer with metabolic costs for every ungrounded thought."
        </blockquote>

        <h2>1. The Working Memory Bottleneck</h2>
        <p>
          Contemporary task management paradigms suffer from a foundational flaw: they treat human attentional capacity as infinite. Digital to-do applications incentivize users to accumulate hundreds of pending tasks into a single backlog. However, human neurobiology actively resists this model.
        </p>
        <p>
          In 1956, George Miller established that human immediate memory span accommodates approximately \( 7 \pm 2 \) discrete items. Later empirical evaluations by Nelson Cowan (2001) demonstrated that when chunking mechanisms are controlled, the true focal capacity of the human working memory is constrained to \( 4 \pm 1 \) active units.
        </p>
        <p>
          When open contexts exceed this focal capacity, the central executive incurs constant cognitive overhead to prevent trace decay. In accordance with the Zeigarnik Effect (1927), unfinished tasks continuously trigger involuntary attentional intrusion, creating sustained cognitive fatigue and perceived mental gridlock ("feeling stuck").
        </p>

        <h2>2. Mathematical Formulations of Cognitive Saturation</h2>
        <p>
          Stuck formalizes cognitive load through a continuous capacity ratio, preventing attentional bankruptcy before it compromises executive function.
        </p>

        <h3>Formula 2.1 — Cognitive Load Index (\(\\mathcal{C}_{load}\))</h3>
        <p>
          Let \(N_{active}\) represent the count of contexts currently staged in the "In Your Head" partition. Let \(K_{limit}\) represent the user's calibrated cognitive capacity (default \(K_{limit} = 3\)). Let \(w_i\) be the cognitive weight of task \(i\), and \(\\tau_i\) be its temporal persistence index:
        </p>

        <div class="math-box">
          \\[
          \\mathcal{C}_{load} = \\sum_{i=1}^{N_{active}} \\frac{w_i \\cdot \\tau_i}{K_{limit}}
          \\]
        </div>

        <p>
          When \(\\mathcal{C}_{load} \\le 1.0\), the cognitive state is categorized as <strong>Calm Equilibrium</strong>. When \(\\mathcal{C}_{load} > 1.0\), the state transitions irreversibly to <strong>Cognitive Overload</strong>, prompting immediate externalization via the Alert Coral visual cue.
        </p>

        <h3>Formula 2.2 — Context Swapping Penalty (\(\\mathcal{P}_{swap}\))</h3>
        <p>
          When an individual switches between \(N_{open}\) ungrounded mental loops, attentional switching cost scales logarithmically:
        </p>

        <div class="math-box">
          \\[
          \\mathcal{P}_{swap} = \\alpha \\cdot \\log_2(N_{open}) + \\beta \\cdot (1 - e^{-\\lambda \\cdot \\Delta t})
          \\]
        </div>

        <p>
          By capping \(N_{active} \\le 3\) and routing excess contexts to <strong>Parked</strong> holding states, Stuck bounds the swapping penalty \(\\mathcal{P}_{swap}\) to a deterministic biological minimum.
        </p>

        <h2 id="architecture">3. The 4-Stage Deterministic State Machine</h2>
        <p>
          Stuck replaces unbounded task graphs with a 4-stage deterministic finite automaton (DFA). A context cannot enter working memory without passing the capacity gate:
        </p>

        <div class="diagram-box">
          <div class="mermaid">
          stateDiagram-v2
            direction LR
            [*] --> ComingUp: Capture Thought
            ComingUp --> InHead: Bring to Focus (Check Capacity)
            InHead --> Parked: Externalize & Free Memory
            Parked --> InHead: Resume Focus
            InHead --> Closed: Mark Complete
            Parked --> Closed: Direct Resolution
            Closed --> [*]: Preserved Metric
          </div>
        </div>

        <p>
          The deterministic transitions enforce two key axioms:
        </p>
        <ul style="margin: 0 0 24px 20px; display: flex; flex-direction: column; gap: 8px;">
          <li><strong>Axiom of Conservation:</strong> A new context cannot enter <code>InHead</code> without either closing an existing context, parking an existing context, or intentionally increasing \(K_{limit}\).</li>
          <li><strong>Axiom of Safe Holding:</strong> Parking a context guarantees complete storage preservation, assuring the nervous system that no data will be lost.</li>
        </ul>

        <h2>4. Dual-Native Zero-Cloud Architecture</h2>
        <p>
          Because thoughts and mental loops represent sensitive reflections of self, Stuck enforces strict zero-cloud data isolation:
        </p>

        <div class="diagram-box">
          <div class="mermaid">
          flowchart TD
            subgraph Apple["Apple Platform (iOS 17+)"]
              UI_iOS["SwiftUI 6.0 Declarative UI"]
              Auth_iOS["LocalAuthentication Face ID"]
              Store_iOS["UserDefaults JSON Sandbox"]
            end

            subgraph Android["Android Platform (API 26+)"]
              UI_And["Jetpack Compose 2.0 Material 3"]
              Auth_And["androidx.biometric Fingerprint"]
              Store_And["Encrypted SharedPreferences"]
            end

            ZeroCloud["Zero Remote Servers • Zero Telemetry • Zero Network Telemetry"]
            UI_iOS --> Store_iOS
            UI_And --> Store_And
            Auth_iOS -.-> UI_iOS
            Auth_And -.-> UI_And
            Store_iOS --- ZeroCloud
            Store_And --- ZeroCloud
          </div>
        </div>

        <h2>5. Conclusion</h2>
        <p>
          Stuck is not a task manager designed to make you do more things faster. It is an attentional governor engineered to restore clarity when mental bandwidth is exhausted. Through rigid adherence to biological limits and zero-cloud privacy, Stuck provides a calm digital companion for the human mind.
        </p>

        <div style="text-align: center; margin: 48px 0 24px;">
          <a href="/#simulator" class="btn btn-primary">
            ${renderIcon("slider", 18)}
            <span>Test the Interactive Simulator</span>
          </a>
        </div>
      </div>
    </article>
  `;

  const scripts = `
    <script>
      document.addEventListener("DOMContentLoaded", function() {
        if (window.renderMathInElement) {
          renderMathInElement(document.body, {
            delimiters: [
              {left: '\\\\[', right: '\\\\]', display: true},
              {left: '\\\\(', right: '\\\\)', display: false}
            ],
            throwOnError : false
          });
        }
      });
    </script>
  `;

  return renderLayout({
    title: "Cognitive Science & Architecture Whitepaper — Stuck",
    description: "Working memory capacity regulation, Miller's Law, Cowan's model, and dual-native state machine implementation in Stuck (com.mtpnda.stuck).",
    content,
    extraHead,
    scripts,
    activeTab: "whitepaper"
  });
}
