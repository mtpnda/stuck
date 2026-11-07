# Design System & Token Specifications

## 1. Shared BioCue DLS Foundation
Stuck strictly shares the canonical **BioCue Design Language System (DLS)** defined in `design/dls.tokens.json` and compiled via `tools/gen_dls.py` into native Swift (`DlsTokens.swift`) and Kotlin (`DlsTokens.kt`).

- **Zero Raw Hex in Views**: All screens, cards, badges, and buttons strictly reference generated semantic tokens (`Palette.*`, `DlsTokens.*`, `ContextColors.*`).
- **Tactile Paper Canvas**: Warm oat background (`oat-100`: `#F2F0EA`), crisp card surface (`oat-050`: `#FCFBF8`), raised elements (`oat-075`: `#F7F4EE`), and subtle dividers (`oat-200`: `#E7E2D9`).
- **High-Contrast Inks**: Primary text (`ink-900`: `#191612`), secondary supporting text (`ink-600`: `#3D362C`), muted captions (`ink-400`: `#544B3E`). Strict WCAG 2.2 AA compliance (min 4.5:1 contrast verified by `gen_dls.py`).
- **Light Mode Supremacy**: Strict light mode adherence (`.preferredColorScheme(.light)`).

---

## 2. Cognitive Stage Color Tokens

| Stage | Token | Hex Code | Foreground Ink | Role & Visual Affordance |
|---|---|---|---|---|
| **In Head** | `stage.active` | `#4B8BF5` | `Color.black.opacity(0.9)` | Active working memory. Counts against limit. |
| **Coming** | `stage.planned` | `#A765BA` | `Color.white` | Scheduled future thoughts. |
| **Parked** | `stage.paused` | `#F59E0B` | `Color.black.opacity(0.9)` | Shelved mental bandwidth. |
| **Closed** | `stage.done` | `#7FB844` | `Color.black.opacity(0.88)` | Completed tasks (with strikethrough). |
| **Banner** | `banner.border` | `#29B6F6` | `#0288D1` | Spanning multi-day project banner. |
| **Capacity Alert** | `state.alert` | `#EF5350` | `Color.white` | Triggered when cognitive load > limit. |

---

## 3. Master Branding & Stack-of-Bars Squircle

Generated via `make branding` (`tools/gen_branding.py` and `tools/gen_store_screenshots.swift`):
- **Container**: Continuous squircle in Charcoal (`#1C1814`).
- **4-Layer Stacked Bars**:
  1. Top Bar: Calm Sage (`#7FB844`) — Focused task in execution.
  2. Upper Middle: Crisp Oat (`#FCFBF8`) — Active context in your head.
  3. Lower Middle: Warm Oat (`#E7E2D9`) — Parked context intentionally set aside.
  4. Bottom Bar: Alert Coral (`#EF5350`) — Capacity threshold boundary.

---

## 4. Component Contracts

### 4.1 `DlsStackedBar` (Task / Thought Card)
- **Geometry**: Height `38pt`, corner radius `9pt`.
- **Leading Tap Target**: 1-tap completion circle icon (`checkmark.circle.fill` / circle outline).
- **Body Tap**: Opens `DlsContextActionPopup`.
- **Hold-to-Drag Motion Physics**:
  - Lift activation threshold: `0.22s` long press.
  - Elevation scale: `1.03x` with smooth spring (`dampingFraction: 0.75`).
  - Dynamic shadow: `Color.black.opacity(0.3)`, radius `10pt`, y `5pt`.
  - Floating live HUD capsule: `Palette.actionPrimary` background, `Palette.onCharcoal` text, hovering `30pt` above card showing `"Move to HH:mm"`.
- **Duration Badge**: Rendered as a compact monospaced pill (`14:00 – 16:00`) in `Color.black.opacity(0.12)` when `endAt != nil`.
- **Trailing Grip Affordance**: Tactile `line.3.horizontal` / `⋮⋮` indicator in `textColor.opacity(0.4)`.

### 4.2 `DragCreateBox` (Spatial Time Blocking)
- **Outer Outline**: `2pt` solid Electric Cyan (`#29B6F6`) with `6pt` corner radius.
- **Fill**: Translucent Electric Cyan (`#29B6F6` at `12%` opacity).
- **Handle Knobs (Top-Left & Bottom-Right)**:
  - Halo diameter: `20pt`.
  - Halo fill: `Color(hex: "1C1814").opacity(0.9)`.
  - Halo border: `1.5pt` solid `#29B6F6`.
  - Inner center pip: `8pt` solid circle in `#29B6F6`.
- **HUD Readout Pill**: Centered pill in `ContextColors.Charcoal` (85% opacity) displaying snapped range and duration: `16:00 – 18:00 (2h)`.

### 4.3 `TimelineEmptySlot` (Timeline Guideline & Drag Target)
- **Geometry**: Height `max(24pt, slotHeight * 0.45)`.
- **Resting Guideline**: Dual hairlines in `Palette.line.opacity(0.6)` flanking a centered `Hold & drag to create` micro-label in `Palette.inkMuted` (35% opacity).
- **Quick Tap**: Triggers direct instant creation for that hour.
- **Hold & Drag**: Activates `DragCreateBox` with real-time slot height snapping.

### 4.4 `BannerBarView` (Project Spanning Bar)
- **Height**: `34pt`.
- **Outline**: `1.5pt` solid Electric Cyan (`#29B6F6`), corner radius `7pt`.
- **Fill**: `Color(hex: "29B6F6").opacity(0.08)`.
- **Icon**: `folder.fill` / `📁`.

### 4.5 `DlsModalPopup` & `dlsActionPopup` (Modal Container)
- Replaces native system sheets with tokenized custom container:
  - Top corner radius: `24pt` continuous.
  - Dimmed scrim backdrop: `Color.black.opacity(0.45)`.
  - Drag handle: `38pt x 4.5pt` centered pill.
- **Inline Quick Note Section**:
  - Existing note display: `Palette.surfaceRaised` box with `📝` icon.
  - Inline input: `Palette.surfaceCard` text field with instant "Save" button for zero-navigation note capture.
