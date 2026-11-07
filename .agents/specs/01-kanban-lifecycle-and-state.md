# Cognitive Lifecycle, Timeline & State Specification

## 1. Context Model Schema
Every thought and mental context in Stuck conforms to the deterministic contract below:

```swift
struct ContextItem: Identifiable, Codable, Equatable {
    var id: String               // UUID String
    var title: String            // Non-empty thought title
    var notes: String            // Free-form notes and details (default: "")
    var todos: [TodoItem]        // Embedded checklist items with completion states
    var startAt: Date            // Scheduled date & timestamp on timeline
    var endAt: Date?             // Optional end date for duration blocks
    var sessions: [FocusSession] // Focus timer sessions with start/end duration
    var status: ContextStatus    // Cognitive stage (.active, .planned, .paused, .done, .dropped)
    var createdAt: Date          // Creation timestamp
    var closedAt: Date?          // Completion or drop timestamp
    var colorTag: BarColorTag    // Retained for legacy storage compatibility
    var isBanner: Bool           // Spanning project banner (Electric Cyan outline)
}
```

```kotlin
data class ContextItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val todos: List<TodoItem> = emptyList(),
    val startAt: Long = System.currentTimeMillis(),
    val endAt: Long? = null,
    val sessions: List<FocusSession> = emptyList(),
    val status: ContextStatus = ContextStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val colorTag: BarColorTag = BarColorTag.BLUE,
    val isBanner: Boolean = false
)
```

---

## 2. Cognitive Stages & Strict Semantic Colors

Stuck strictly enforces semantic color binding. **Arbitrary color pickers are disallowed.** Colors are 100% derived from cognitive status:

| Stage | Identifier | Token / Hex | Foreground Ink | Cognitive Role & Meaning |
|---|---|---|---|---|
| **In your head** | `.active` | `#4B8BF5` (Task Blue) | High-contrast dark ink | Active working memory. Counts against cognitive limit. |
| **Coming up** | `.planned` | `#A765BA` (Lilac Purple) | White text | Scheduled future thought. Does not count against limit. |
| **Parked** | `.paused` | `#F59E0B` (Warm Amber) | High-contrast dark ink | Intentionally shelved thought to relieve mental bandwidth. |
| **Closed** | `.done` | `#7FB844` (Sage Green) | High-contrast dark ink | Completed thought with strikethrough styling and closed timestamp. |
| **Dropped** | `.dropped` | `#64748B` (Muted Slate) | Muted ink | Discarded without completion. |
| **Spanning Banner** | `isBanner: true`| `#29B6F6` (Electric Cyan)| `#0288D1` | Multi-day spanning project banner with cyan border. |

---

## 3. Unified Continuous Vertical Schedule & Sticky Strip

1. **Directional Consistency**: Both days and time intervals scroll vertically in one seamless continuous list. Horizontal day paging is strictly prohibited.
2. **Sticky Day Quick-Jump Selector Strip**: Pinned directly below the header:
   `[ SAT 7 ] [ SUN 8 ] [ MON 9 ] [ TUE 10 ] [ WED 11 ] ...`
   Tapping any day pill immediately executes a smooth spring scroll (`scrollProxy.scrollTo` / `animateScrollToItem`) to that day's section.
3. **Chronological Day Sections**:
   - Header with day name, date number, thought counter, and cyan `TODAY` pill.
   - Spanning banners displayed at the top of the day.
   - Regular thought bars rendered according to the active zoom level.
   - Live time marker cursor (`●──────── HH:mm`) on Today.

---

## 4. 7-Tier Pinch-to-Zoom Time Granularity

The timeline supports smooth two-finger pinch-to-zoom scaling and a header stepper pill `[ − 1 hr + ]`:

| Level | Label | Slot Height | Visual Behavior & Layout |
|---|---|---|---|
| **0** | `Day` | Compact Stack | **Zero Dead Space**: Tasks stack directly beneath each other as compact cards (height: 38pt, spacing: 6pt). No empty hour lines. |
| **1** | `6 hrs` | 48 dp / pt | 4 quarter-day blocks (`06:00` Morning, `12:00` Afternoon, `18:00` Evening, `00:00` Night). |
| **2** | `3 hrs` | 52 dp / pt | 6 three-hour blocks (`06:00`, `09:00`, `12:00`, `15:00`, `18:00`, `21:00`). |
| **3** | `1 hr` | 56 dp / pt | Hourly slots (`07:00` - `22:00`). Guideline hairlines for empty hours with "Hold & drag to create". |
| **4** | `30 mins` | 84 dp / pt | Expanded hour slots with `:30` sub-guidelines and timestamps. |
| **5** | `10 mins` | 136 dp / pt | Expanded slots with `:10`, `:20`, `:30`, `:40`, `:50` sub-guidelines. |
| **6** | `1 min` | 210 dp / pt | High-precision timeline with minute ticks every 5 minutes and exact start time badges. |

---

## 5. Hold-to-Drag Task Bars (`DlsStackedBar`) & Rescheduling

Tasks on the timeline can be repositioned directly through vertical drag gestures:
1. **Hold & Lift**: Long-pressing a bar for `0.22s` triggers medium haptic feedback and elevates the card (`scale: 1.03`, dynamic shadow, `zIndex: 100`).
2. **Live Floating HUD**: A floating capsule indicator (`"Move to HH:mm"`) hovers above the bar tracking the snapped target time.
3. **Deterministic Rescheduling**:
   - Dropping the bar calculates delta hours from the slot height:
     `deltaHours = round(dragOffsetY / slotHeight)`
   - Calls `rescheduleTime(id:newStartAt:)`.
   - **Duration Preservation**: If the thought has an explicit `endAt`, its duration is deterministically preserved:
     `newEndAt = newStartAt + (item.endAt - item.startAt)`
   - Untimed thoughts maintain `endAt = nil`.
4. **Visual Affordances**:
   - Duration badge pill: Displays `14:00 – 16:00` in monospaced font when `endAt != nil`.
   - Trailing grip dots `⋮⋮` indicate draggable capability when unclosed.

---

## 6. Drag-to-Create Timeline Box (Spatial Time Blocking)

Users can visually carve out time blocks directly on the timeline:
1. **Interaction**: Holding and dragging down on an empty timeline slot (`TimelineEmptySlotView` / `TimelineEmptySlot`) activates drag creation.
2. **Visual Geometry**:
   - **Outer Border**: 2pt Electric Cyan (`#29B6F6`) with 6pt corner radius.
   - **Fill**: Translucent Electric Cyan (`#29B6F6` at 12% opacity).
   - **Top-Left Handle Knob**: 20pt circular dark halo (`#1C1814` at 90% opacity) with 1.5pt cyan stroke and 8pt solid cyan center pip.
   - **Bottom-Right Handle Knob**: Matching dark halo and solid cyan pip anchored to the dynamic drag coordinate.
   - **Live HUD Pill**: Centered floating capsule showing time range and duration: `16:00 – 18:00 (2h)`.
3. **Creation Release**:
   - On release, creates the context item with the exact `startAt` and `endAt` timestamps.
   - Immediately presents the creation sheet to allow the user to name the thought and add checklist items.

---

## 7. Quick Capture Engine with Inline Note Parsing

The persistent bottom Quick Capture bar provides instantaneous thought offloading without dead space:
1. **Stage & Day Context Resolution (`resolveQuickCaptureContext`)**:
   - If a stage legend pill (`In head`, `Coming`, `Park`) is selected, the captured thought strictly assumes that stage.
   - If no filter is active:
     - Thoughts captured on **Future Days** default automatically to `.planned` (Lilac Purple).
     - Thoughts captured on **Today or Past Days** default to `.active` (Task Blue).
   - Scheduled time preserves the current hour, minute, and second on the target day.
2. **Inline Note Parsing (`//` Syntax)**:
   - Entering text formatted as `Title // Note details` automatically splits:
     - **Title:** Text before `//` (trimmed).
     - **Notes:** Text after `//` (trimmed).
   - Example: `Renew domain // GoDaddy renewal before Friday` populates `title: "Renew domain"` and `notes: "GoDaddy renewal before Friday"`.
3. **Affordance Placeholder**: Dynamically communicates the active stage and note syntax:
   `"Quick capture thought (use // for note)..."`

---

## 8. 100% Custom DLS Modal Popups (Zero System Sheets)

Native system `.sheet` modals are strictly replaced with custom DLS popups encased in dimmed scrim backdrops (`Color.black.opacity(0.45)`):

### 8.1 Quick Action Popup (`DlsContextActionPopup`)
Triggered by tapping any thought bar:
- Status badge with stage color.
- Thought title (up to 2 lines).
- **Inline Quick Note Section**:
  - Displays existing notes (`📝 ...`).
  - Includes a quick note text field + instant "Save" button to append or edit notes without full-screen transitions.
- **Stage Selector Buttons**: 4 tactile buttons (`In Head`, `Coming`, `Park`, `Done ✓`) for instant 1-tap re-categorization.
- **Reschedule Timeline Pills**: `‹ Yesterday`, `Today`, `Tomorrow ›`.
- **Action Buttons**: "Start Focus Timer", "Edit Details" (opens full sheet), and "Delete".

### 8.2 Creation & Full Edit Sheet (`ContextSheetView` / `ContextSheet`)
- Title & Cognitive Stage selector.
- **Duration / End Time Controls**:
  - Toggle switch for explicit duration.
  - Duration preset chips: `30m`, `1h`, `1.5h`, `2h`, `3h`.
  - Dynamic duration readout: `14:00 – 16:00 (2h)`.
- Spanning Project Banner switch.
- Notes & Details multi-line editor.
- Checklist manager (`todos`) with interactive toggle and item addition.
