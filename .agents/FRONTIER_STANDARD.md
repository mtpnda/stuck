# The Frontier Standard (~5,000 Tier / Context App)

> *"We operate to the standard of the ~5,000 internal frontier researchers and engineers: hyper-efficiency, deep investigation, zero guesswork, full observability."*

---

## 1. Core Operating Principles

1. **Ultra-Efficiency & Structural Rigor**:
   - Every architectural component (Zustand state slices, Restyle design tokens, notification triggers) must be minimal, modular, and mathematically sound.
   - Zero hardcoded colors, zero inline font properties, zero custom stylesheets (`__tests__/design.test.js` is a non-negotiable contract).
2. **Zero Guesswork / Deep Investigation**:
   - When tests or runtime behaviors break, we do not guess or randomly edit files. We trace the defect down to its precise origin (hydration timing, state reducer mismatch, token scale miss).
3. **Closing the Comprehension Gap**:
   - Provide structured artifacts, concise technical documentation, and clear architectural specs so the operator maintains total mastery over the system.

---

## 2. White-Box Testing Brain (ARTEMIS Integration)

Inspired by the white-box architecture in `artemis-ansible-runner`:

1. **System 1 (Heuristic / Action Execution)**:
   - Drives UI on physical devices, iOS Simulator, or Android emulator via `uiautomator` and `devicectl`.
   - Computes state hashes (`screen_hash = route|active_column|focused_id`), prevents cycles, and explores untouched UI paths.
2. **System 2 (Deep Code Comprehension)**:
   - Reads the React Native source (`store.js`, `screens/Board.js`, `screens/ContextSheet.js`, `tokens.js`) to discover input constraints, state transitions, and test boundaries.
3. **Trace Compilation & Deterministic Replay**:
   - Exploration traces are compiled into deterministic replay playbooks and Jest test suites (`npm test`) that run with zero AI in the loop once discovered.

---

## 3. Telemetry & Local State Standard

- **Zustand Persistence**: Saved under AsyncStorage key `contexts:v2`.
- **Group Isolation**: Every context is partitioned by `groupId` (`null` for personal, UUID for shared spaces, or `"all"`).
- **Design Token Invariance**: All screens must strictly consume `@shopify/restyle` semantic tokens from `src/design/tokens.js`.
