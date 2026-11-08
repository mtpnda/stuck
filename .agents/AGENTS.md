# Stuck (`com.mtpnda.stuck`) — Agentic Operating & Architecture Guide
> **Repository:** `Stuck` (Cognitive Load Balancer & Mental Context Flow for iOS & Android)
> **Central Agent Plugin:** [/Users/valdiearsanur/.gemini/config/plugins/central-agents/](file:///Users/valdiearsanur/.gemini/config/plugins/central-agents/)
> **Release Standard:** [custom-release-manager](file:///Users/valdiearsanur/.gemini/config/plugins/central-agents/skills/custom-release-manager/SKILL.md)
> **Parent Architecture Parity:** Follows [BioCue](file:///Users/valdiearsanur/Workspace/biocue/) standards.

---

## 1. Zero-Waste Repository Boundary & Architecture Guidelines

1. **Zero-Waste Boundary (No Local `.agents/skills`)**:
   - All agent capabilities, playbooks, and specialized tools are centralized in the global plugin:
     **`central-agents`** (`/Users/valdiearsanur/.gemini/config/plugins/central-agents`).
   - Draw all skills directly from the centralized catalog.

2. **Dual-Native Architecture**:
   - **iOS Native:** Swift 6, SwiftUI, Swift Concurrency, `LocalAuthentication` for Face ID/Touch ID, `UserDefaults` local persistence (`ios/Contexts/`).
   - **Android Native:** Kotlin 2.0, Jetpack Compose, Material 3, `androidx.biometric` for Fingerprint/Face Unlock, `SharedPreferences` local JSON repository (`android/app/src/main/java/com/mtpnda/stuck/`).
   - **Showcase & API Server:** Deno runtime, TypeScript, SSR showcase with DLS tokens and REST API endpoints (`backend/`).
   - **Identifier:** Application ID / Bundle ID: `com.mtpnda.stuck`.
   - **Strict Zero-Cloud Privacy:** 100% on-device local storage. Strictly no third-party analytics, remote tracking, or network telemetry.

3. **Unified Release Automation Facade**:
   - Single-pane controller is the root `Makefile` and `fastlane/Fastfile`.
   - Ad-hoc release scripts (`build_release.sh`, `deploy.sh`) are strictly prohibited and enforced via `tests/test_release_automation.py`.
   - Distribution is centralized via the MacBook Air Webhost (`/apps/stuck`) with single-version storage hygiene.

---

## 2. Inviolable Design & UX Rules

1. **Strict BioCue DLS Adherence (Zero Raw Styling)**:
   - Never use raw hex color literals in UI screens or composables.
   - All colors and typography must resolve through canonical tokens:
     - `design/dls.tokens.json`
     - iOS: `ios/Contexts/Sources/Design/DlsTokens.swift`
     - Android: `android/app/src/main/java/com/mtpnda/stuck/ui/theme/Color.kt` & `DlsTokens.kt`
   - Recompile tokens with `make dls` (`tools/gen_dls.py`).

2. **Strict Invariant (High-Contrast Inks)**:
   - High-contrast inks (`DlsTokens.textPrimary` / `#1C1814`) must always be used on light canvases (`#F2F0EA` / `#FCFBF8`).
   - Strict light mode: `.preferredColorScheme(.light)`.

3. **Stack-of-Bars Emblem & Branding**:
   - Signature 4-layer stacked bars: Calm Sage (focused task), Crisp Oat (active), Warm Oat (parked), Alert Coral (capacity threshold) encased in Charcoal squircle container.
   - Master branding generated via `make branding` (`tools/gen_branding.py`).

4. **Unified Vertical Schedule & 7-Tier Pinch-to-Zoom Timeline**:
   - Directional consistency: Both days and hours scroll in one unified vertical timeline (no horizontal day paging).
   - Sticky day quick-jump selector strip (`[ SAT 7 ] [ SUN 8 ] [ MON 9 ] ...`) scrolls instantly to any day.
   - 7 zoom levels: `Day` (compact card stack, 0 empty lines) -> `6 hrs` -> `3 hrs` -> `1 hr` -> `30 mins` -> `10 mins` -> `1 min` (precision timeline).
   - Dual zoom controls: Fluid pinch gesture animation + `[ − 1 hr + ]` header stepper pill.

5. **Strict Stage-Bound Colors (Zero Color Pickers)**:
   - Colors are 100% derived from cognitive status:
     - 🔵 `ACTIVE` ("In head"): `#4B8BF5` (Task Blue)
     - 🟣 `PLANNED` ("Coming"): `#A765BA` (Lilac Purple)
     - 🟡 `PAUSED` ("Parked"): `#F59E0B` (Warm Amber)
     - 🟢 `DONE` ("Closed"): `#7FB844` (Sage Green)
     - 🔷 `BANNER`: `#29B6F6` (Electric Cyan outline)
   - Disallow arbitrary color picking.

6. **100% Custom DLS Modal Popups (Zero System Sheets)**:
   - Never use native system `.sheet` modals or dialogs.
   - All popups (Quick Action, New Thought, Edit Thought, Profile) use `DlsModalPopup` with dimmed scrim backdrop (`Color.black.opacity(0.45)`), 24pt rounded top corners, pill drag handle, and spring animation.
   - Includes inline Quick Note viewer and one-tap editor in the Quick Action popup.

7. **Hold-to-Drag Task Bar & Drag-to-Create Time Blocking**:
   - Hold (0.22s) lifts task bars with live floating capsule HUD `"Move to HH:mm"` and commits reschedules with duration preservation.
   - Hold and drag on empty slots draws an Electric Cyan (`#29B6F6`) box with handle knobs and live range HUD (`16:00 – 18:00 (2h)`).

8. **Quick Capture with Inline Note Parsing (`//`)**:
   - Persistent bottom bar captures thoughts respecting active stage filters and timeline days.
   - Parsing syntax: `Title // Notes` automatically populates both title and detailed notes.

---

## 3. Store Listing as Code (`play/listings/en-US/`)

Google Play metadata conforms to strict character limits and graphic specs:
- `title.txt`: Maximum 30 characters (`Stuck`, 5 chars).
- `short-description.txt`: Maximum 80 characters (`Unstick your thoughts. Serene cognitive load balancer & 4-stage task flow.`, 75 chars).
- `full-description.txt`: Comprehensive feature overview and privacy guarantees (<= 4000 chars).
- `graphics/icon/icon.png`: Exactly 512x512 PNG.
- `graphics/feature-graphic/feature-graphic.png`: Exactly 1024x500 PNG.

---

## 4. Quality Gates & Release Verification

Before completing tasks or committing changes, verify:

```bash
# 1. Tri-platform unit tests (iOS XCTest, Android JUnit, Deno backend)
make test

# 2. Release automation and metadata integrity
make test-release     # Runs tests/test_release_automation.py

# 3. iOS Reviewer Sequential UI Test (Simulator)
make test-reviewer-ios # Runs tests/test_ios_reviewer.py
```
