# Stuck (`com.mtpnda.stuck`) — System Architecture

## 1. System Overview
**Stuck** (application identifier `com.mtpnda.stuck`) is a serene, high-performance cognitive load balancer and mental context manager designed to protect executive function and working memory. The application is built on a strict **Dual-Native Architecture**:
- **iOS Native:** Swift 6, SwiftUI, Swift Concurrency, `LocalAuthentication` for Face ID/Touch ID, `UserDefaults` local persistence (`ios/Contexts/`).
- **Android Native:** Kotlin 2.0, Jetpack Compose, Material 3, `androidx.biometric` for Fingerprint/Face Unlock, `SharedPreferences` local JSON repository (`android/app/src/main/java/com/mtpnda/stuck/`).
- **Web Showcase & API Server:** Deno runtime, TypeScript, Server-Side Rendered (SSR) HTML with BioCue DLS tokens, and REST metadata endpoints (`backend/`).

### Inviolable Privacy Guarantee
Stuck operates under **Strict Zero-Cloud Privacy**: 100% on-device local storage. Strictly no third-party analytics, remote tracking, cloud sync, or external database telemetry. All user thoughts, notes, focus history, and biometric configurations stay strictly on the device.

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                   STUCK ECOSYSTEM                                      │
├────────────────────────────────┬───────────────────────────┬───────────────────────────┤
│ 1. iOS Native Client (SwiftUI) │ 2. Android Native Client  │ 3. Showcase & API Server  │
│         `ios/Contexts/`        │          `android/`       │          `backend/`       │
├────────────────────────────────┼───────────────────────────┼───────────────────────────┤
│ • Swift 6 / SwiftUI            │ • Kotlin 2.0 / Compose    │ • Deno 2.x & TypeScript   │
│ • LocalAuthentication (Face ID)│ • BiometricPrompt Lock    │ • SSR Showcase & Legal:   │
│ • UserDefaults Storage         │ • SharedPreferences Store │   Home, Whitepaper, Terms,│
│ • 7-Tier Pinch-to-Zoom Timeline│ • 7-Tier Zoom Timeline    │   Privacy, Helpdesk       │
│ • Hold-to-Drag Task Bar        │ • Hold-to-Drag Task Bar   │ • REST Platform API:      │
│ • Drag-to-Create Time Box      │ • Drag-to-Create Time Box │   `/api/config`           │
│ • Quick Capture (`//` notes)   │ • Quick Capture Engine    │   `/api/support`          │
│ • 100% Custom DLS Popups       │ • 100% Custom DLS Popups  │ • Port 8000 / Deno Deploy │
└────────────────────────────────┴───────────────────────────┴───────────────────────────┘
                                       │
                                       ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                      4. Binary Distribution & Release Automation                       │
├────────────────────────────────┬───────────────────────────┬───────────────────────────┤
│ MacBook Air Webhost (Tailscale)│ Apple Physical Deployment │ Google Play Store (GPP)   │
│ `binary-webhost` / Port 8888   │ `xcrun devicectl` Tunnel  │ Store-as-Code (`play/`)   │
│ Single-version storage hygiene │ Developer Signed IPA      │ Single-pane root Makefile │
└────────────────────────────────┴───────────────────────────┴───────────────────────────┘
```

---

## 2. Core Architectural Pillars

### 2.1 Dual-Native Platform Parity
1. **iOS Native Client (`ios/Contexts/`)**:
   - Platform target: iOS 16.0+, iPhone and iPad.
   - Entry point: `ContextsApp.swift`.
   - Core State: `ContextStore.swift` (`@MainActor ObservableObject` with `@Published` properties).
   - Views: `BoardView.swift`, `ContextSheetView.swift`, `ProfileView.swift`, `LockView.swift`, `LoginView.swift`.
   - Design System: `ios/Contexts/Sources/Design/DlsTokens.swift`.
   - Quality Gates: `ios/ContextsTests/ContextsTests.swift` (XCTest suite executed via `make test-ios`).

2. **Android Native Client (`android/app/`)**:
   - Platform target: Android 14+ (minSdk 26, targetSdk 35).
   - Entry point: `MainActivity.kt`.
   - Core State & Data: `ContextRepository.kt` (`StateFlow` and Coroutine architecture).
   - Screens: `BoardScreen.kt`, `ContextSheet.kt`, `ProfileScreen.kt`, `LockScreen.kt`, `LoginScreen.kt`.
   - Design System: `com.mtpnda.stuck.ui.theme.ContextColors`, `DlsTokens.kt`, `DlsScales.kt`.
   - Quality Gates: `android/app/src/test/java/com/mtpnda/stuck/ContextLogicTest.kt` (JUnit suite via `make test-android`).

3. **Showcase Web & Platform API (`backend/`)**:
   - Runtime: Deno with native TypeScript execution.
   - Endpoints:
     - `GET /`: Showcase landing page with live interactive canvas and DLS tokens.
     - `GET /whitepaper`: Cognitive Load Balancer mathematical formulation & state machine specs.
     - `GET /privacy`: Sovereign zero-cloud privacy policy.
     - `GET /terms`: Terms of service with non-medical disclaimer.
     - `GET /support`: User helpdesk and ticketing.
     - `GET /api/config`: Dynamic version and platform metadata.
     - `POST /api/support`: Local-first ticket receiver.
   - Quality Gates: `backend/src/pages/pages.test.ts` & `legal.test.ts` (executed via `make test-backend`).

---

## 3. Storage & State Contracts

### 3.1 Local Persistence Schema
Data is serialized into JSON and stored deterministically without cloud synchronization:
- **iOS Key**: `UserDefaults.standard.data(forKey: "contexts:native:v1")`.
- **Android Key**: `SharedPreferences.getString("contexts_v1", null)`.

### 3.2 Cognitive Capacity Heuristics ("limitatr")
The core mechanic of Stuck is making cognitive load visible and enforced:
- **Cognitive Load (`load`)**: Strictly the count of tasks in `.active` status (`load = contexts.count { it.status == .active }`).
- **Cognitive Capacity Limit (`limit`)**: User-configured integer clamped strictly between `1` and `9` (default: `3`).
- **Over-Limit Warning (`isOverLimit`)**: Triggered when `load > limit`. Shifts header indicator from calm sage (`#7FB844`) to alert coral (`#EF5350`) and displays a non-judgmental prompt to park or complete tasks.
- **Relief Paths**:
  1. Parking a task (`.paused` / Warm Amber) removes it from active load immediately.
  2. Closing a task (`.done` / Sage Green) records `closedAt` timestamp and relieves load.
  3. Deleting a task relieves load.

---

## 4. Single-Pane Release Controller & Verification Standards

In alignment with Mata Panda Studio standards, all release automation is unified in the root controller:
```bash
make test             # Executes iOS XCTest, Android JUnit, and Deno backend tests
make test-release     # Enforces metadata integrity, icon dimensions, and no ad-hoc scripts
make test-reviewer    # Runs end-to-end automated reviewer journey on iOS Simulator
make dls              # Recompiles design tokens from design/dls.tokens.json
make branding         # Regenerates app icons, squircle stacked-bar logos, and store assets
```

### Physical Deployment & Single-Version Webhost Hygiene
- **Physical Device:** Installed directly to paired developer devices (`X's iPhone 16 Pro`) using `xcrun devicectl` with Apple Development signing.
- **Webhost (`~/Workspace/macbook-air-webhost/apps/stuck`)**:
  - Enforces **strict single-version storage**: Only the latest valid build is retained (`stuck.ipa` and `app-release-unsigned.apk`).
  - Accessible locally and across the mesh network via Tailscale on port 8888.
