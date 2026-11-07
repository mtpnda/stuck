# Biometrics & Local Security Specification

## 1. Zero-Cloud Privacy Guarantee
Stuck operates 100% on-device and offline:
- Zero telemetry, network calls, remote databases, or external analytics.
- All persistent data resides exclusively in iOS `UserDefaults` / Android `SharedPreferences`, encrypted via native hardware storage sandboxing.
- Resetting the profile invokes a full wipe: clearing all state, removing all keys from storage, and stopping active focus timers.

## 2. Authentication Flow

### 2.1 First Launch (Sign Up & Identity)
- **Unified Sign-In Options**:
  1. **Continue with Google**: Authenticates identity via system web session / OAuth, extracting verified `name`, `email`, and `initials`.
  2. **Continue as Guest**: Instant entry as a local user (`name: "Guest"`, `provider: "local"`, `faceId: false`). Bypasses biometrics for friction-free usage.
  3. **Manual Entry**: 100% offline creation without third-party identity, user enters name directly.
- **Biometric Layering**:
  - iOS: `LAContext().canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)`.
  - Android: `BiometricManager.canAuthenticate(BIOMETRIC_STRONG)`.
  - If biometrics are available, the "Lock with Face ID" option secures the local profile.

### 2.2 Re-launch & App Lock
- **Initial Lock**: The store initializes with `isLocked: true` (this transient state is deliberately omitted from persistent storage so every cold launch requires authentication).
- **Auto-Lock on Background**: `onPause` (Android) or `scenePhase == .background` (iOS) triggers `lock()` whenever the app leaves the foreground.
- **Biometric Authentication**: Calling `authenticateWithBiometrics` evaluates the biometric policy. Upon success, `isLocked = false`.

### 2.3 Profile Modifications & Security Toggles
- Avatar displays initials or profile avatar.
- Toggling Face ID inside the Profile popup persists the preference to `profile.faceId`.
- Full data wipe and sign out available with confirmation buttons.
