# Stuck (`com.mtpnda.stuck`) — know what's in your head

A dual-native app for **iOS (iPhone & iPad)** in pure **Swift & SwiftUI** and **Android (Phone & Tablet)** in pure **Kotlin & Jetpack Compose**. Designed around cognitive load balancing and **mental contexts**: the thoughts and tasks you are actively holding in your head.

Strictly adheres to the **BioCue Design Language System (DLS)**, **BioCue Login Mechanism**, and **BioCue Unified Release Standard** (`custom-release-manager`) with 100% on-device zero-cloud privacy.

The interface is an intentional 4-stage cognitive lifecycle board:
**Coming up → In your head → Parked → Closed**

The header continuously tracks your cognitive limit (e.g., *"2 of 3 in your head"*) and turns alert coral when you exceed your set capacity.

---

## 🎨 Branding & The "Stack of Bars" Icon

Stuck uses a custom, minimalist **"Stack of Bars"** emblem representing cognitive layers and finite working-memory capacity:
- **Canvas:** BioCue Warm Oat (`#F2F0EA` / `#FCFBF8`).
- **Container:** Rounded charcoal squircle (`#1C1814`) with subtle rim lighting.
- **Top Bar (Calm Sage):** Focused, active mental task at the surface.
- **Middle Bars (Crisp & Warm Oat):** Buffered working-memory contexts.
- **Bottom Bar (Alert Coral):** Cognitive capacity threshold.

Generated via standard math rasterization without external heavyweight dependencies:
```bash
make branding    # Re-rasterizes master icons, mipmaps, iOS appiconsets, and feature graphics
```

---

## Workspace Structure

The project follows agentic workspace conventions with zero React Native overhead:

```
.
├── Makefile                        # Single-pane controller matching BioCue lanes
├── AGENTS.md                       # Architecture & agentic operating rules
├── design/
│   ├── dls.tokens.json             # Canonical BioCue DLS single source of truth tokens
│   └── dls.html                    # Visual DLS token spec & palette documentation
├── tools/
│   ├── gen_dls.py                  # Compiles tokens into Swift, Kotlin, and HTML
│   └── gen_branding.py             # Generates AppIcon, Mipmaps, Feature Graphic, Webhost assets
├── play/                           # Store listing as code (Play Store Publisher)
│   ├── listings/en-US/             # title.txt (<=30), short-description.txt (<=80), full-description.txt
│   ├── release-notes/en-US/        # Release changelog
│   └── graphics/                   # 512x512 icon, 1024x500 feature graphic, screenshots
├── ios/                            # Pure Native iOS (SwiftUI + XcodeGen)
│   ├── project.yml                 # Declarative Xcode project definition (com.mtpnda.stuck)
│   ├── Stuck.xcodeproj             # Generated Xcode project
│   ├── Contexts/
│   │   ├── Info.plist
│   │   ├── Assets.xcassets/        # AppIcon catalog generated from master
│   │   └── Sources/
│   │       ├── App/                # StuckApp.swift
│   │       ├── Design/             # DlsTokens.swift, Theme.swift, Components.swift
│   │       ├── Model/              # ContextItem.swift, Profile.swift
│   │       ├── Store/              # ContextStore.swift (UserDefaults persistence + Biometrics)
│   │       └── Views/              # LoginView, LockView, BoardView, ContextSheetView, ProfileView
│   └── ContextsTests/              # XCTest unit & DLS suite (100% passing)
├── android/                        # Pure Native Android (Kotlin + Jetpack Compose)
│   ├── build.gradle.kts            # Root build configuration
│   ├── settings.gradle.kts
│   ├── gradle/libs.versions.toml   # Shared version catalog with BioCue
│   └── app/
│       ├── build.gradle.kts        # Jetpack Compose + Material 3 + Coroutines (com.mtpnda.stuck)
│       └── src/
│           ├── main/
│           │   ├── AndroidManifest.xml
│           │   ├── play/           # Mirrored Play Store listing as code
│           │   ├── res/            # Launcher icons (mipmap-*) & app strings
│           │   └── java/com/mtpnda/stuck/
│           │       ├── MainActivity.kt
│           │       ├── ui/theme/   # DlsTokens.kt, Color.kt, Type.kt, Theme.kt, CalmComponents.kt
│           │       ├── model/      # ContextItem.kt, Profile.kt
│           │       ├── data/       # ContextRepository.kt (SharedPreferences + JSON serialization)
│           │       └── ui/screens/ # LoginScreen, LockScreen, BoardScreen, ContextSheet, ProfileScreen
│           └── test/               # JUnit tests for Kanban flow, limit & DLS parity (100% passing)
├── fastlane/                       # Multi-platform Fastlane automation pipeline
│   ├── Fastfile                    # Orchestrates iOS/Android builds & webhost deployments
│   └── Appfile                     # App bundle identifier: com.mtpnda.stuck
├── tests/                          # Automated QA & Release Verification Gates
│   ├── test_release_automation.py  # Release metadata, dimensions, Fastfile, and single-pane gates
│   └── test_playstore_reviewer.py  # End-to-end ADB uiautomator Play Store reviewer simulation
└── README.md                       # This root project guide
```

---

## Features & BioCue Parity

| Feature | Implementation |
|---|---|
| **BioCue DLS** | Warm oat canvas surfaces (`#F2F0EA` / `#FCFBF8`), BioCue Charcoal actions (`#1C1814`), and WCAG AAA contrast guarantees (15.2:1 text, 6.7:1 alert coral, 7.1:1 calm sage). Regenerated via `make dls`. |
| **BioCue Login Mechanism** | Signature Stack-of-Bars emblem, Google OAuth, Apple Sign-In (iOS), Quick Demo Presets (`valdie@stuck.app`, `partner@family.com`), and instant 1-tap Guest login. |
| **iOS & Android Parity** | Cross-platform on iPhone, iPad, Android phone, and tablet. Face ID on iOS (`LocalAuthentication`), Biometrics / Fingerprint on Android (`androidx.biometric`). |
| **Zero-Cloud Privacy** | Everything is stored strictly on the local device (`UserDefaults` on iOS, `SharedPreferences` / local JSON on Android). No remote servers, no tracking, no third-party telemetry. |
| **Cognitive Load Cap** | Configurable limit (1–9). Visual over-limit warnings guide you to park or close contexts before taking on more. |
| **Focus Timer** | Tap *Focus* on any active card to start a real-time focus session. Accumulated focus duration is recorded and shown in Profile. |

---

## Single-Pane Controller (`Makefile`)

All workflows are driven through the unified single-pane controller matching BioCue:

```bash
# Verify build environment, tools, and secrets
make doctor

# Setup secrets parity (symlinks ~/.secrets/)
make setup-secrets

# Automated quality gates
make test                     # Run all iOS (XCTest) & Android (JUnit) tests
make test-ios                 # Run iOS unit tests on Simulator
make test-android             # Run Android unit tests via Gradle
make test-release             # Release automation & metadata integrity tests
make test-reviewer            # Play Store reviewer sequential UI automation test (ADB)

# Compilation & Builds
make build-all                # Compile both iOS IPA and Android APK
make build-ios                # Compile iOS Release IPA via Fastlane
make build-android            # Compile Android Release APK via Fastlane
make install-android          # Install debug APK to connected device

# Release & Distribution
make release-local            # Compile & deploy both builds to MacBook Air Webhost (/apps/stuck)
make release-ios-local        # Deploy iOS build to Webhost
make release-android-local    # Deploy Android build to Webhost
make release-playstore        # Build & upload release bundle to Google Play Store
make release-playstore-internal # Upload to Google Play Internal Testing track
make release-playstore-listing # Sync store listing copy & graphic assets without code build

# Maintenance & Assets
make branding                 # Generate icons, mipmaps, and feature graphics
make dls                      # Recompile DLS tokens across Swift, Kotlin, and HTML
make clean                    # Clean all build caches
```
