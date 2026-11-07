# Development & Testing Guide

## 1. Prerequisites
- **Node.js**: v18+ (tested on Node 20 / 22)
- **Package Manager**: `npm`
- **Expo CLI**: Bundled with Expo SDK 52
- **iOS Simulator / Device**: macOS with Xcode 15+ installed (required for native Face ID and push notification testing)

## 2. Directory Structure
```
context-app/
├── rn/                         # React Native Expo project
│   ├── App.js                  # App root, providers, AppState listeners
│   ├── src/
│   │   ├── store.js            # Zustand persistent store
│   │   ├── reminders.js        # Expo notifications & scheduling logic
│   │   ├── time.js             # Date calculations & formatters
│   │   ├── design/
│   │   │   ├── tokens.js       # Shopify Restyle design tokens
│   │   │   └── index.js        # Design system primitive components
│   │   └── screens/
│   │       ├── Board.js        # Kanban board screen
│   │       ├── ContextSheet.js # Create/Edit context modal sheet
│   │       ├── Login.js        # Welcome & Face ID lock screen
│   │       └── Profile.js      # User profile, limit & settings sheet
│   ├── __tests__/              # Jest + Testing Library test suites (51 tests)
│   ├── app.json                # Expo configuration
│   └── package.json
├── .agents/
│   ├── skills/                 # Agent skills library
│   ├── docs/                   # Developer documentation
│   └── specs/                  # Technical specifications
└── README.md                   # Project overview & quick start
```

## 3. Running the App

### 3.1 Run Tests
The test suite consists of 55 tests covering design system integrity, authentication, CRUD operations, board views, profile management, and Google OAuth:
```bash
cd rn
npm test
```

### 3.2 Start Metro Bundler over Tailscale
Exposes the Expo Metro bundler over Tailscale (matching `expense-budget-tracker/apps/mobile-v2`):
```bash
cd rn
npm start
```

- **Tailscale IP**: `100.73.84.111`
- **Exposed Port**: `8083`
- **Expo URL**: `exp://100.73.84.111:8083`
- **Web / Bundler**: `http://100.73.84.111:8083`

### 3.3 Development Build on iOS Simulator
Face ID (`expo-local-authentication`) and interactive notification buttons (`expo-notifications`) require a development build rather than Expo Go:
```bash
cd rn
npx expo run:ios
```

### 3.4 Verify Production Bundle Export
```bash
cd rn
npx expo export --platform ios
```
