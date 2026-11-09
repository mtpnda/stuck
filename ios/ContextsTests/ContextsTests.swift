import XCTest
import SwiftUI
@testable import Stuck

@MainActor
final class StuckTests: XCTestCase {
    var store: ContextStore!

    override func setUp() async throws {
        try await super.setUp()
        store = ContextStore()
        store.resetAllData()
    }

    // MARK: - Auth & Profile Tests

    func testProfileCreationAndSignOut() {
        XCTAssertNil(store.profile)
        XCTAssertTrue(store.isLocked)

        store.createProfile(name: "Valdie Arsanur", email: "valdie@stuck.app", faceId: true, provider: "google")
        XCTAssertNotNil(store.profile)
        XCTAssertEqual(store.profile?.name, "Valdie Arsanur")
        XCTAssertEqual(store.profile?.initials, "VA")
        XCTAssertFalse(store.isLocked)

        store.signOut()
        XCTAssertNil(store.profile)
        XCTAssertTrue(store.isLocked)
    }

    // MARK: - Cognitive Load Limiter ("limitatr") Tests

    func testCognitiveLoadLimiterClampingBoundaries() {
        store.createProfile(name: "Test User", faceId: false, provider: "guest")

        // Default limit is 3
        XCTAssertEqual(store.limit, 3)

        // Lower clamp boundary: values <= 0 must clamp to 1
        store.setLimit(0)
        XCTAssertEqual(store.limit, 1, "Limit must not drop below 1")

        store.setLimit(-10)
        XCTAssertEqual(store.limit, 1, "Negative limit must clamp to 1")

        // Upper clamp boundary: values >= 10 must clamp to 9
        store.setLimit(10)
        XCTAssertEqual(store.limit, 9, "Limit must not exceed 9")

        store.setLimit(99)
        XCTAssertEqual(store.limit, 9, "Extreme high limit must clamp to 9")

        // Valid mid range
        store.setLimit(5)
        XCTAssertEqual(store.limit, 5)
    }

    func testCognitiveLoadLimiterOverflowAndRecovery() {
        store.createProfile(name: "Test User", faceId: false, provider: "guest")
        store.setLimit(2)
        XCTAssertEqual(store.limit, 2)
        XCTAssertEqual(store.load, 0)
        XCTAssertFalse(store.isOverLimit)

        // Add 1st active task -> load = 1, not over limit
        let id1 = store.addContext(title: "Task Alpha", status: .active)
        XCTAssertEqual(store.load, 1)
        XCTAssertFalse(store.isOverLimit)

        // Add 2nd active task -> load = 2, at capacity, not over limit
        let id2 = store.addContext(title: "Task Beta", status: .active)
        XCTAssertEqual(store.load, 2)
        XCTAssertFalse(store.isOverLimit)

        // Add 3rd active task -> load = 3, strictly exceeds limit (3 > 2)
        let id3 = store.addContext(title: "Task Gamma", status: .active)
        XCTAssertEqual(store.load, 3)
        XCTAssertTrue(store.isOverLimit, "Load of 3 with limit 2 must trigger isOverLimit warning")

        // Recovery path 1: Park task 3 -> drops active load back to 2
        store.setStatus(id: id3, status: .paused)
        XCTAssertEqual(store.load, 2)
        XCTAssertFalse(store.isOverLimit, "Parking an active task must immediately relieve cognitive overload")
        XCTAssertEqual(store.pausedContexts.count, 1)

        // Exceed again with new task
        let id4 = store.addContext(title: "Task Delta", status: .active)
        XCTAssertEqual(store.load, 3)
        XCTAssertTrue(store.isOverLimit)

        // Recovery path 2: Complete task 1 -> drops active load to 2
        store.setStatus(id: id1, status: .done)
        XCTAssertEqual(store.load, 2)
        XCTAssertFalse(store.isOverLimit, "Closing a task must immediately relieve cognitive overload")
        XCTAssertEqual(store.closedContexts.count, 1)

        // Recovery path 3: Delete task 2
        store.removeContext(id: id2)
        XCTAssertEqual(store.load, 1)
        XCTAssertFalse(store.isOverLimit)

        // Clean up remaining
        store.removeContext(id: id4)
        XCTAssertEqual(store.load, 0)
    }

    // MARK: - Pinch-to-Zoom Time Granularity Tests

    func testTimeGranularityScaleInvariants() {
        let all = TimeGranularity.allCases
        XCTAssertEqual(all.count, 7, "Must define exactly 7 timeline granularity steps")

        XCTAssertEqual(TimeGranularity.day.label, "Day")
        XCTAssertEqual(TimeGranularity.sixHours.label, "6 hrs")
        XCTAssertEqual(TimeGranularity.threeHours.label, "3 hrs")
        XCTAssertEqual(TimeGranularity.oneHour.label, "1 hr")
        XCTAssertEqual(TimeGranularity.thirtyMins.label, "30 mins")
        XCTAssertEqual(TimeGranularity.tenMins.label, "10 mins")
        XCTAssertEqual(TimeGranularity.oneMin.label, "1 min")

        // Slot heights must strictly increase as time resolution becomes finer (except day=0 compact)
        XCTAssertEqual(TimeGranularity.day.slotHeight, 0)
        XCTAssertLessThan(TimeGranularity.sixHours.slotHeight, TimeGranularity.threeHours.slotHeight)
        XCTAssertLessThan(TimeGranularity.threeHours.slotHeight, TimeGranularity.oneHour.slotHeight)
        XCTAssertLessThan(TimeGranularity.oneHour.slotHeight, TimeGranularity.thirtyMins.slotHeight)
        XCTAssertLessThan(TimeGranularity.thirtyMins.slotHeight, TimeGranularity.tenMins.slotHeight)
        XCTAssertLessThan(TimeGranularity.tenMins.slotHeight, TimeGranularity.oneMin.slotHeight)
    }

    // MARK: - Date Scoped Load & Day Migration Tests

    func testDateScopedLoadAndTaskMigration() {
        store.createProfile(name: "Test User", faceId: false)
        let cal = Calendar.current
        let today = Date()
        let tomorrow = cal.date(byAdding: .day, value: 1, to: today)!

        let todayTask = store.addContext(title: "Today's Focus", startAt: today, status: .active)
        let tomorrowTask = store.addContext(title: "Tomorrow's Sprint", startAt: tomorrow, status: .active)

        // Day filtering
        let todayContexts = store.contextsFor(date: today)
        XCTAssertEqual(todayContexts.count, 1)
        XCTAssertEqual(todayContexts.first?.id, todayTask)
        XCTAssertEqual(store.loadFor(date: today), 1)

        let tomorrowContexts = store.contextsFor(date: tomorrow)
        XCTAssertEqual(tomorrowContexts.count, 1)
        XCTAssertEqual(tomorrowContexts.first?.id, tomorrowTask)
        XCTAssertEqual(store.loadFor(date: tomorrow), 1)

        // Migrate tomorrow's task into today
        store.moveToDay(id: tomorrowTask, date: today)
        XCTAssertEqual(store.contextsFor(date: today).count, 2)
        XCTAssertEqual(store.loadFor(date: today), 2)
        XCTAssertEqual(store.contextsFor(date: tomorrow).count, 0)
    }

    // MARK: - Detailed CRUD & Unique Lifecycle Tests

    func testCRUDTitleWhitespaceTrimmingAndLIFOOrdering() {
        store.createProfile(name: "Test User", faceId: false)

        // Create with trailing and leading whitespace + newlines
        let id1 = store.addContext(title: "   First Thought Card   \n")
        let id2 = store.addContext(title: "\t  Second Thought Card  ")

        XCTAssertEqual(store.contexts.count, 2)

        // LIFO ordering: id2 is at index 0, id1 is at index 1
        XCTAssertEqual(store.contexts[0].id, id2)
        XCTAssertEqual(store.contexts[0].title, "Second Thought Card")

        XCTAssertEqual(store.contexts[1].id, id1)
        XCTAssertEqual(store.contexts[1].title, "First Thought Card")
    }

    func testCRUDPartialFieldUpdates() {
        store.createProfile(name: "Test User", faceId: false)

        let id = store.addContext(
            title: "Initial Title",
            notes: "Deep notes architecture",
            colorTag: .purple,
            isBanner: true
        )

        // 1. Update only title -> notes, colorTag, and isBanner must remain intact
        store.updateContext(id: id, title: "Updated Clean Title")
        var item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.title, "Updated Clean Title")
        XCTAssertEqual(item?.notes, "Deep notes architecture")
        XCTAssertEqual(item?.colorTag, .purple)
        XCTAssertEqual(item?.isBanner, true)

        // 2. Update only notes -> title, colorTag, and isBanner remain intact
        store.updateContext(id: id, notes: "Revised notes payload")
        item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.title, "Updated Clean Title")
        XCTAssertEqual(item?.notes, "Revised notes payload")

        // 3. Update only colorTag
        store.updateContext(id: id, colorTag: .coral)
        item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.colorTag, .coral)

        // 4. Update only isBanner
        store.updateContext(id: id, isBanner: false)
        item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.isBanner, false)
    }

    func testCRUDToggleDoneAndClosedAtTimestampLifecycle() {
        store.createProfile(name: "Test User", faceId: false)
        let id = store.addContext(title: "Task with Timestamp", status: .active)

        // Initially active -> closedAt must be nil
        var item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.status, .active)
        XCTAssertNil(item?.closedAt)

        // Toggle Done -> status becomes done, closedAt is stamped
        store.toggleDone(id: id)
        item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.status, .done)
        XCTAssertNotNil(item?.closedAt)

        // Toggle Done again -> status reverts to active, closedAt reset to nil
        store.toggleDone(id: id)
        item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.status, .active)
        XCTAssertNil(item?.closedAt)

        // Explicit setStatus to dropped -> closedAt is stamped
        store.setStatus(id: id, status: .dropped)
        item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.status, .dropped)
        XCTAssertNotNil(item?.closedAt)

        // Move to paused -> closedAt reset to nil
        store.setStatus(id: id, status: .paused)
        item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.status, .paused)
        XCTAssertNil(item?.closedAt)
    }

    func testCRUDToggleParked() {
        store.createProfile(name: "Test User", faceId: false)
        let id = store.addContext(title: "Parkable Task", status: .active)

        // Active -> Parked
        store.toggleParked(id: id)
        var item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.status, .paused)

        // Parked -> Active
        store.toggleParked(id: id)
        item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.status, .active)
    }

    func testCRUDNestedTodosChecklistMutation() {
        store.createProfile(name: "Test User", faceId: false)
        let initialTodos = [
            TodoItem(text: "Draft Architecture", isDone: false),
            TodoItem(text: "Write Tests", isDone: false)
        ]
        let id = store.addContext(title: "Parent Project", todos: initialTodos, status: .active)

        var item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.todos.count, 2)
        XCTAssertFalse(item!.todos[0].isDone)

        // Check off 1st todo and add 3rd todo
        var modifiedTodos = item!.todos
        modifiedTodos[0].isDone = true
        modifiedTodos.append(TodoItem(text: "Verify Coverage", isDone: false))

        store.updateContext(id: id, todos: modifiedTodos)
        item = store.contexts.first { $0.id == id }

        XCTAssertEqual(item?.todos.count, 3)
        XCTAssertTrue(item!.todos[0].isDone)
        XCTAssertFalse(item!.todos[1].isDone)
        XCTAssertFalse(item!.todos[2].isDone)
    }

    func testCRUDFocusSessionAutoStopOnDelete() {
        store.createProfile(name: "Test User", faceId: false)
        let id = store.addContext(title: "Focus Target", status: .active)

        store.focusOn(id: id)
        XCTAssertEqual(store.focusContextId, id)
        XCTAssertNotNil(store.focusStartedAt)

        // Deleting the focused task must automatically stop focus cleanly
        store.removeContext(id: id)
        XCTAssertNil(store.focusContextId)
        XCTAssertNil(store.focusStartedAt)
        XCTAssertFalse(store.contexts.contains { $0.id == id })
    }

    func testCRUDFocusSessionAutoStopOnMarkDone() {
        store.createProfile(name: "Test User", faceId: false)
        let id = store.addContext(title: "Focus and Close Target", status: .active)

        store.focusOn(id: id)
        XCTAssertEqual(store.focusContextId, id)

        // Marking done must automatically stop focus and preserve the recorded session
        store.setStatus(id: id, status: .done)
        XCTAssertNil(store.focusContextId)
        XCTAssertNil(store.focusStartedAt)

        let closed = store.closedContexts.first { $0.id == id }
        XCTAssertNotNil(closed)
        XCTAssertEqual(closed?.sessions.count, 1)
    }

    func testCRUDFocusSessionSwitchingBetweenContexts() {
        store.createProfile(name: "Test User", faceId: false)
        let id1 = store.addContext(title: "First Focus Task", status: .active)
        let id2 = store.addContext(title: "Second Focus Task", status: .active)

        // Start focus on 1st task
        store.focusOn(id: id1)
        XCTAssertEqual(store.focusContextId, id1)

        // Switch focus to 2nd task -> 1st task session must finalize and append
        store.focusOn(id: id2)
        XCTAssertEqual(store.focusContextId, id2)

        let task1 = store.contexts.first { $0.id == id1 }
        XCTAssertEqual(task1?.sessions.count, 1)

        store.stopFocus()
        let task2 = store.contexts.first { $0.id == id2 }
        XCTAssertEqual(task2?.sessions.count, 1)
    }

    func testCRUDOverdueLogicAcrossStatuses() {
        let now = Date()
        let past = now.addingTimeInterval(-3600)   // 1 hour ago
        let future = now.addingTimeInterval(3600)  // 1 hour ahead

        // 1. Overdue when active and endAt is past
        let activeOverdue = ContextItem(title: "Active Late", startAt: past, endAt: past, status: .active)
        XCTAssertTrue(activeOverdue.isOverdue)

        // 2. Overdue when paused (parked) and endAt is past
        let pausedOverdue = ContextItem(title: "Paused Late", startAt: past, endAt: past, status: .paused)
        XCTAssertTrue(pausedOverdue.isOverdue)

        // 3. Completed task is NEVER overdue, even if endAt is past
        let doneCompleted = ContextItem(title: "Done On Time", startAt: past, endAt: past, status: .done)
        XCTAssertFalse(doneCompleted.isOverdue, "Done task must never be flagged as overdue")

        // 4. Dropped task is NEVER overdue
        let dropped = ContextItem(title: "Dropped", startAt: past, endAt: past, status: .dropped)
        XCTAssertFalse(dropped.isOverdue)

        // 5. Future end date is NOT overdue
        let activeFuture = ContextItem(title: "Future Active", startAt: now, endAt: future, status: .active)
        XCTAssertFalse(activeFuture.isOverdue)

        // 6. Nil end date is NOT overdue
        let noDeadline = ContextItem(title: "Open Ended", startAt: now, endAt: nil, status: .active)
        XCTAssertFalse(noDeadline.isOverdue)
    }

    func testCRUDResetAllDataFullTeardown() {
        store.createProfile(name: "Valdie Arsanur", email: "valdie@stuck.app", faceId: true)
        store.seedSampleContexts()
        store.setLimit(6)
        if let first = store.contexts.first {
            store.focusOn(id: first.id)
        }

        XCTAssertNotNil(store.profile)
        XCTAssertFalse(store.contexts.isEmpty)
        XCTAssertNotNil(store.focusContextId)
        XCTAssertEqual(store.limit, 6)

        // Execute nuclear reset
        store.resetAllData()

        XCTAssertNil(store.profile)
        XCTAssertTrue(store.contexts.isEmpty)
        XCTAssertNil(store.focusContextId)
        XCTAssertNil(store.focusStartedAt)
        XCTAssertEqual(store.limit, 3, "Limit must reset to default 3")
        XCTAssertTrue(store.isLocked)
    }

    // MARK: - Focus Session Tracking Tests

    func testFocusSessionTracking() {
        store.createProfile(name: "Test User", faceId: false)
        let id = store.addContext(title: "Deep Work Task", status: .active)

        XCTAssertNil(store.focusContextId)
        store.focusOn(id: id)
        XCTAssertEqual(store.focusContextId, id)
        XCTAssertNotNil(store.focusStartedAt)
        XCTAssertNotNil(store.focusedContext)
        XCTAssertEqual(store.focusedContext?.id, id)

        store.stopFocus()
        XCTAssertNil(store.focusContextId)
        XCTAssertNil(store.focusStartedAt)
        let item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.sessions.count, 1)
    }

    // MARK: - Sample Seeding & Persistence Round-Trip Tests

    func testSampleContextSeeding() {
        store.createProfile(name: "Test User", faceId: false)
        store.seedSampleContexts()

        XCTAssertFalse(store.contexts.isEmpty)
        XCTAssertEqual(store.contexts.count, 9)

        // Must contain banner context
        XCTAssertTrue(store.contexts.contains { $0.isBanner })

        // Must represent all 4 Kanban lifecycle states
        XCTAssertFalse(store.activeContexts.isEmpty)
        XCTAssertFalse(store.plannedContexts.isEmpty)
        XCTAssertFalse(store.pausedContexts.isEmpty)
        XCTAssertFalse(store.closedContexts.isEmpty)
    }

    func testRoundTripDiskPersistence() {
        store.createProfile(name: "Valdie Arsanur", email: "valdie@stuck.app", faceId: false, provider: "google")
        store.setLimit(4)
        let id = store.addContext(title: "Persisted Card", notes: "Encrypted local zero-cloud payload", status: .active)

        // Instantiate fresh store instance from UserDefaults
        let newStore = ContextStore()
        XCTAssertNotNil(newStore.profile)
        XCTAssertEqual(newStore.profile?.name, "Valdie Arsanur")
        XCTAssertEqual(newStore.profile?.email, "valdie@stuck.app")
        XCTAssertEqual(newStore.limit, 4)
        XCTAssertTrue(newStore.contexts.contains { $0.id == id && $0.title == "Persisted Card" })
    }

    // MARK: - BioCue Design System Token Invariants

    func testBioCueDLSColorsMatchSpec() {
        XCTAssertEqual(DlsTokens.surfaceBg, Color(.sRGB, red: 242/255.0, green: 240/255.0, blue: 234/255.0, opacity: 1))
        XCTAssertEqual(DlsTokens.actionPrimary, Color(.sRGB, red: 28/255.0, green: 24/255.0, blue: 20/255.0, opacity: 1))
        XCTAssertEqual(DlsTokens.stateAlertInk, Color(.sRGB, red: 156/255.0, green: 42/255.0, blue: 34/255.0, opacity: 1))
        XCTAssertEqual(DlsTokens.stateCalmInk, Color(.sRGB, red: 26/255.0, green: 99/255.0, blue: 55/255.0, opacity: 1))
    }

    // MARK: - Quick Capture (Stage & Time Context) Tests

    func testQuickCaptureRespectsExplicitStageContext() {
        store.createProfile(name: "Test User", faceId: false)
        let today = Calendar.current.startOfDay(for: Date())

        // 1. Capture with .planned stage filter
        let idPlanned = store.quickCapture(title: "Dental checkup next month", targetDate: today, stageFilter: .planned)
        let itemPlanned = store.contexts.first { $0.id == idPlanned }
        XCTAssertEqual(itemPlanned?.status, .planned, "Quick capture with planned filter must set status to .planned")
        XCTAssertEqual(itemPlanned?.colorTag, .purple, "Planned stage must strictly bind to purple color tag")
        XCTAssertEqual(store.activeCount, 0, "Planned task must not contribute to active cognitive load")

        // 2. Capture with .active stage filter
        let idActive = store.quickCapture(title: "Urgent client reply", targetDate: today, stageFilter: .active)
        let itemActive = store.contexts.first { $0.id == idActive }
        XCTAssertEqual(itemActive?.status, .active)
        XCTAssertEqual(itemActive?.colorTag, .blue)
        XCTAssertEqual(store.activeCount, 1, "Active task must increment active load")

        // 3. Capture with .paused stage filter
        let idPaused = store.quickCapture(title: "Side project idea", targetDate: today, stageFilter: .paused)
        let itemPaused = store.contexts.first { $0.id == idPaused }
        XCTAssertEqual(itemPaused?.status, .paused)
        XCTAssertEqual(itemPaused?.colorTag, .charcoal)
        XCTAssertEqual(store.activeCount, 1, "Paused task must not increase active load")

        // 4. Capture with .done stage filter
        let idDone = store.quickCapture(title: "Past receipt archived", targetDate: today, stageFilter: .done)
        let itemDone = store.contexts.first { $0.id == idDone }
        XCTAssertEqual(itemDone?.status, .done)
        XCTAssertEqual(itemDone?.colorTag, .green)
        XCTAssertEqual(store.activeCount, 1)
    }

    func testQuickCaptureRespectsTimeContextWithoutFilter() {
        store.createProfile(name: "Test User", faceId: false)
        let cal = Calendar.current
        let today = cal.startOfDay(for: Date())
        let tomorrow = cal.date(byAdding: .day, value: 1, to: today)!
        let yesterday = cal.date(byAdding: .day, value: -1, to: today)!

        // Future day -> automatically .planned (.purple)
        let idFuture = store.quickCapture(title: "Tomorrow task", targetDate: tomorrow, stageFilter: nil)
        let itemFuture = store.contexts.first { $0.id == idFuture }
        XCTAssertEqual(itemFuture?.status, .planned, "Capturing into a future day without filter must default to .planned")
        XCTAssertEqual(itemFuture?.colorTag, .purple)
        XCTAssertEqual(cal.startOfDay(for: itemFuture!.startAt), cal.startOfDay(for: tomorrow))

        // Today -> automatically .active (.blue)
        let idToday = store.quickCapture(title: "Today thought", targetDate: today, stageFilter: nil)
        let itemToday = store.contexts.first { $0.id == idToday }
        XCTAssertEqual(itemToday?.status, .active, "Capturing on today without filter must default to .active")
        XCTAssertEqual(itemToday?.colorTag, .blue)
        XCTAssertEqual(cal.startOfDay(for: itemToday!.startAt), cal.startOfDay(for: today))

        // Past day -> automatically .active (.blue)
        let idPast = store.quickCapture(title: "Yesterday log", targetDate: yesterday, stageFilter: nil)
        let itemPast = store.contexts.first { $0.id == idPast }
        XCTAssertEqual(itemPast?.status, .active)
        XCTAssertEqual(cal.startOfDay(for: itemPast!.startAt), cal.startOfDay(for: yesterday))
    }

    func testQuickCapturePreservesTimeOfDayOnTargetDay() {
        let cal = Calendar.current
        let today = cal.startOfDay(for: Date())
        let threeDaysLater = cal.date(byAdding: .day, value: 3, to: today)!

        let testTime = cal.date(bySettingHour: 15, minute: 42, second: 10, of: today)!
        let resolved = ContextStore.resolveQuickCaptureContext(
            selectedFilter: nil,
            targetDate: threeDaysLater,
            referenceDate: testTime
        )

        XCTAssertEqual(resolved.status, .planned)
        XCTAssertEqual(resolved.colorTag, .purple)

        let targetDayComp = cal.dateComponents([.year, .month, .day], from: threeDaysLater)
        let scheduledComp = cal.dateComponents([.year, .month, .day, .hour, .minute, .second], from: resolved.scheduledDate)

        XCTAssertEqual(scheduledComp.year, targetDayComp.year)
        XCTAssertEqual(scheduledComp.month, targetDayComp.month)
        XCTAssertEqual(scheduledComp.day, targetDayComp.day)
        XCTAssertEqual(scheduledComp.hour, 15)
        XCTAssertEqual(scheduledComp.minute, 42)
    }

    // MARK: - Reschedule & Drag Timeline Tests

    func testRescheduleTimePreservesDuration() {
        store.createProfile(name: "Test User", faceId: false)
        let now = Date()
        let twoHoursLater = now.addingTimeInterval(7200) // 2 hours

        let id = store.addContext(
            title: "Deep Work Block",
            startAt: now,
            endAt: twoHoursLater,
            status: .active
        )

        let initialItem = store.contexts.first { $0.id == id }
        XCTAssertNotNil(initialItem?.endAt)
        XCTAssertEqual(initialItem?.endAt?.timeIntervalSince(initialItem!.startAt), 7200)

        // Reschedule to 4 hours in the future without specifying newEndAt
        let newStart = now.addingTimeInterval(14400)
        store.rescheduleTime(id: id, newStartAt: newStart)

        let updatedItem = store.contexts.first { $0.id == id }
        XCTAssertEqual(updatedItem?.startAt, newStart)
        XCTAssertNotNil(updatedItem?.endAt)
        // Duration must remain exactly 7200 seconds (2 hours)
        XCTAssertEqual(updatedItem?.endAt?.timeIntervalSince(newStart), 7200)
    }

    func testRescheduleTimeWithExplicitEndTime() {
        store.createProfile(name: "Test User", faceId: false)
        let now = Date()
        let id = store.addContext(title: "Sprint Planning", startAt: now, status: .planned)

        let newStart = now.addingTimeInterval(3600)
        let newEnd = newStart.addingTimeInterval(5400) // 1.5 hours
        store.rescheduleTime(id: id, newStartAt: newStart, newEndAt: newEnd)

        let updatedItem = store.contexts.first { $0.id == id }
        XCTAssertEqual(updatedItem?.startAt, newStart)
        XCTAssertEqual(updatedItem?.endAt, newEnd)
        XCTAssertEqual(updatedItem?.endAt?.timeIntervalSince(newStart), 5400)
    }

    func testUntimedThoughtRescheduleKeepsNilEndTime() {
        store.createProfile(name: "Test User", faceId: false)
        let now = Date()
        let id = store.addContext(title: "Untimed Thought", startAt: now, endAt: nil, status: .active)

        let initialItem = store.contexts.first { $0.id == id }
        XCTAssertNil(initialItem?.endAt)

        let newStart = now.addingTimeInterval(3600)
        store.rescheduleTime(id: id, newStartAt: newStart)

        let updatedItem = store.contexts.first { $0.id == id }
        XCTAssertEqual(updatedItem?.startAt, newStart)
        XCTAssertNil(updatedItem?.endAt, "Rescheduling an untimed thought must preserve nil endAt")
    }

    // MARK: - Quick Add Note Tests

    func testQuickCaptureWithInlineNoteSyntax() {
        store.createProfile(name: "Test User", faceId: false)
        let cal = Calendar.current
        let today = cal.startOfDay(for: Date())

        let id = store.quickCapture(
            title: "Draft launch email // mention single-version hygiene & port 8888",
            targetDate: today
        )

        let item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.title, "Draft launch email", "Title must be cleanly separated from note")
        XCTAssertEqual(item?.notes, "mention single-version hygiene & port 8888", "Note must be captured from // syntax")
    }

    func testQuickCaptureWithExplicitNotes() {
        store.createProfile(name: "Test User", faceId: false)
        let cal = Calendar.current
        let today = cal.startOfDay(for: Date())

        let id = store.quickCapture(
            title: "Review PR",
            notes: "Needs performance profiling on arm64",
            targetDate: today
        )

        let item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.title, "Review PR")
        XCTAssertEqual(item?.notes, "Needs performance profiling on arm64")
    }

    // MARK: - Schedule Placeholder & Zoom Granularity Tests

    func testZoomGranularityDurationsAndSnapSteps() {
        XCTAssertEqual(TimeGranularity.day.defaultDurationMinutes, 1440)
        XCTAssertEqual(TimeGranularity.sixHours.defaultDurationMinutes, 360)
        XCTAssertEqual(TimeGranularity.threeHours.defaultDurationMinutes, 180)
        XCTAssertEqual(TimeGranularity.oneHour.defaultDurationMinutes, 60)
        XCTAssertEqual(TimeGranularity.thirtyMins.defaultDurationMinutes, 30)
        XCTAssertEqual(TimeGranularity.tenMins.defaultDurationMinutes, 10)
        XCTAssertEqual(TimeGranularity.oneMin.defaultDurationMinutes, 1)

        XCTAssertEqual(TimeGranularity.day.snapStepHours, 24.0)
        XCTAssertEqual(TimeGranularity.sixHours.snapStepHours, 6.0)
        XCTAssertEqual(TimeGranularity.threeHours.snapStepHours, 3.0)
        XCTAssertEqual(TimeGranularity.oneHour.snapStepHours, 1.0)
        XCTAssertEqual(TimeGranularity.thirtyMins.snapStepHours, 0.5)
        XCTAssertEqual(TimeGranularity.tenMins.snapStepHours, 10.0 / 60.0)
        XCTAssertEqual(TimeGranularity.oneMin.snapStepHours, 1.0 / 60.0)
    }

    func testSchedulePlaceholderCalculations() {
        let cal = Calendar.current
        let today = cal.startOfDay(for: Date())

        // Hourly placeholder with 10 mins zoom
        let placeholder10m = SchedulePlaceholder(
            date: today,
            hour: 14,
            minute: 0,
            durationMinutes: 10,
            status: .active,
            colorTag: .blue
        )
        XCTAssertEqual(placeholder10m.timeRangeString, "14:00 – 14:10")
        XCTAssertEqual(placeholder10m.durationBadge, "10m")
        XCTAssertEqual(placeholder10m.endDate().timeIntervalSince(placeholder10m.startDate()), 600)

        // Hourly placeholder with 1 hr zoom
        let placeholder1h = SchedulePlaceholder(
            date: today,
            hour: 9,
            minute: 0,
            durationMinutes: 60,
            status: .planned,
            colorTag: .purple
        )
        XCTAssertEqual(placeholder1h.timeRangeString, "09:00 – 10:00")
        XCTAssertEqual(placeholder1h.durationBadge, "1h")
        XCTAssertEqual(placeholder1h.endDate().timeIntervalSince(placeholder1h.startDate()), 3600)

        // 1 min zoom precision placeholder
        let placeholder1m = SchedulePlaceholder(
            date: today,
            hour: 11,
            minute: 15,
            durationMinutes: 1,
            status: .active,
            colorTag: .blue
        )
        XCTAssertEqual(placeholder1m.timeRangeString, "11:15 – 11:16")
        XCTAssertEqual(placeholder1m.durationBadge, "1m")
        XCTAssertEqual(placeholder1m.endDate().timeIntervalSince(placeholder1m.startDate()), 60)

        // Day placeholder
        let placeholderDay = SchedulePlaceholder(
            date: today,
            hour: nil,
            minute: 0,
            durationMinutes: 1440,
            status: .planned,
            colorTag: .purple
        )
        XCTAssertEqual(placeholderDay.timeRangeString, "All Day")
        XCTAssertEqual(placeholderDay.durationBadge, "24h")
        XCTAssertEqual(placeholderDay.endDate().timeIntervalSince(placeholderDay.startDate()), 86400)
    }

    func testScheduleClickOnlyCreatesPlaceholderWithoutAddingToStore() {
        store.createProfile(name: "Test User", faceId: false)
        let initialCount = store.contexts.count

        // Simulating click on schedule: instantiating placeholder in memory
        let placeholder = SchedulePlaceholder(
            date: Date(),
            hour: 10,
            minute: 0,
            durationMinutes: TimeGranularity.oneHour.defaultDurationMinutes,
            status: .active,
            colorTag: .blue
        )

        // Invariant check: Store count must strictly remain unchanged
        XCTAssertEqual(store.contexts.count, initialCount, "Clicking schedule to create placeholder must NOT add to store")

        // Only when user confirms form does it add to store
        let id = store.addContext(
            title: "Confirmed thought",
            startAt: placeholder.startDate(),
            endAt: placeholder.endDate(),
            status: placeholder.status,
            colorTag: placeholder.colorTag
        )

        XCTAssertEqual(store.contexts.count, initialCount + 1, "Context must only be saved after user confirms form")
        let item = store.contexts.first { $0.id == id }
        XCTAssertEqual(item?.startAt, placeholder.startDate())
        XCTAssertEqual(item?.endAt, placeholder.endDate())
    }

    // MARK: - Multi-Day & Duration Expansion Tests

    func testMultiDayEventIntersectsBothDays() {
        store.createProfile(name: "Test User", faceId: false)
        let cal = Calendar.current
        let today = cal.startOfDay(for: Date())
        guard let tomorrow = cal.date(byAdding: .day, value: 1, to: today),
              let dayAfter = cal.date(byAdding: .day, value: 2, to: today) else {
            XCTFail("Calendar date calculation failed")
            return
        }

        // Event from today 22:00 to tomorrow 04:00 (6 hour duration spanning across midnight)
        let start = today.addingTimeInterval(22 * 3600)
        let end = tomorrow.addingTimeInterval(4 * 3600)

        let id = store.addContext(
            title: "Overnight Deep Work",
            startAt: start,
            endAt: end,
            status: .active,
            colorTag: .blue
        )

        guard let item = store.contexts.first(where: { $0.id == id }) else {
            XCTFail("Created item not found in store")
            return
        }

        XCTAssertTrue(item.spansAcrossMidnight, "Event from 22:00 to 04:00 must span across midnight")
        XCTAssertTrue(item.intersects(date: today), "Event must intersect Day 1")
        XCTAssertTrue(item.intersects(date: tomorrow), "Event must intersect Day 2")
        XCTAssertFalse(item.intersects(date: dayAfter), "Event must NOT intersect Day 3")

        XCTAssertFalse(item.isContinuation(for: today), "Day 1 is origin day, not continuation")
        XCTAssertTrue(item.isOutgoing(for: today), "Day 1 must be flagged outgoing to next day")

        XCTAssertTrue(item.isContinuation(for: tomorrow), "Day 2 must be flagged continuation from yesterday")
        XCTAssertFalse(item.isOutgoing(for: tomorrow), "Day 2 is final day, not outgoing")

        // Store retrieval must return item on both days
        let todayItems = store.contextsFor(date: today)
        let tomorrowItems = store.contextsFor(date: tomorrow)
        XCTAssertTrue(todayItems.contains(where: { $0.id == id }), "Store must return item in contextsFor(today)")
        XCTAssertTrue(tomorrowItems.contains(where: { $0.id == id }), "Store must return item in contextsFor(tomorrow)")
    }

    func testDurationExpansionAndRescheduleAcrossDays() {
        store.createProfile(name: "Test User", faceId: false)
        let cal = Calendar.current
        let today = cal.startOfDay(for: Date())
        let start = today.addingTimeInterval(16 * 3600) // 16:00
        let initialEnd = today.addingTimeInterval(18 * 3600) // 18:00 (2h duration)

        let id = store.addContext(
            title: "Project Review",
            startAt: start,
            endAt: initialEnd,
            status: .active
        )

        // Expand duration to tomorrow 02:00 (10 hour total duration spanning across midnight)
        guard let tomorrow = cal.date(byAdding: .day, value: 1, to: today) else { return }
        let newEnd = tomorrow.addingTimeInterval(2 * 3600)

        store.expandDuration(id: id, newEndAt: newEnd)
        let expanded = store.contexts.first(where: { $0.id == id })
        XCTAssertEqual(expanded?.endAt, newEnd)
        XCTAssertTrue(expanded?.spansAcrossMidnight == true)
        XCTAssertTrue(expanded?.intersects(date: today) == true)
        XCTAssertTrue(expanded?.intersects(date: tomorrow) == true)

        // Reschedule start time forward by 1 hour (17:00), duration (10h) must be strictly preserved
        let newStart = today.addingTimeInterval(17 * 3600)
        store.rescheduleTime(id: id, newStartAt: newStart)

        let rescheduled = store.contexts.first(where: { $0.id == id })
        XCTAssertEqual(rescheduled?.startAt, newStart)
        let expectedEnd = newStart.addingTimeInterval(newEnd.timeIntervalSince(start))
        XCTAssertEqual(rescheduled?.endAt, expectedEnd, "Rescheduling must preserve expanded duration")
    }

    // MARK: - Free Text ThoughtParser & Model Inference Tests

    func testThoughtParserNaturalLanguageTimeRangeAndNotes() {
        let input = "Deep work 14:00 - 16:00 // Finish quarterly slides #active"
        let parsed = ThoughtParser.parse(input: input, recordTelemetry: false)

        XCTAssertEqual(parsed.title, "Deep work")
        XCTAssertEqual(parsed.notes, "Finish quarterly slides")
        XCTAssertEqual(parsed.status, .active)
        XCTAssertEqual(parsed.durationMinutes, 120)
        XCTAssertNotNil(parsed.startAt)
        XCTAssertNotNil(parsed.endAt)

        let cal = Calendar.current
        XCTAssertEqual(cal.component(.hour, from: parsed.startAt!), 14)
        XCTAssertEqual(cal.component(.minute, from: parsed.startAt!), 0)
        XCTAssertEqual(cal.component(.hour, from: parsed.endAt!), 16)
        XCTAssertEqual(cal.component(.minute, from: parsed.endAt!), 0)
    }

    func testThoughtParserDurationAndStageTags() {
        let input = "Dentist appointment at 10:30 (45m) #planned"
        let parsed = ThoughtParser.parse(input: input, recordTelemetry: false)

        XCTAssertEqual(parsed.title, "Dentist appointment")
        XCTAssertEqual(parsed.status, .planned)
        XCTAssertEqual(parsed.colorTag, .purple)
        XCTAssertEqual(parsed.durationMinutes, 45)
        XCTAssertNotNil(parsed.startAt)
        XCTAssertNotNil(parsed.endAt)

        let cal = Calendar.current
        XCTAssertEqual(cal.component(.hour, from: parsed.startAt!), 10)
        XCTAssertEqual(cal.component(.minute, from: parsed.startAt!), 30)
        XCTAssertEqual(cal.component(.hour, from: parsed.endAt!), 11)
        XCTAssertEqual(cal.component(.minute, from: parsed.endAt!), 15)
    }

    func testThoughtParserBannerHashtagAndTelemetryDict() {
        let input = "Quarterly System Redesign #banner // Core platform shift"
        let parsed = ThoughtParser.parse(input: input, recordTelemetry: false)

        XCTAssertEqual(parsed.title, "Quarterly System Redesign")
        XCTAssertTrue(parsed.isBanner)
        XCTAssertEqual(parsed.notes, "Core platform shift")

        let dict = parsed.toDictionary()
        XCTAssertEqual(dict["title"] as? String, "Quarterly System Redesign")
        XCTAssertEqual(dict["is_banner"] as? Bool, true)
        XCTAssertEqual(dict["notes"] as? String, "Core platform shift")
        XCTAssertNotNil(dict["latency_ms"])
    }

    // MARK: - Loki Observability & Circuit Breaker Tests

    func testLokiObservabilitySovereignGatingDefaultOff() {
        let obs = LokiObservability.shared
        // Ensure default is off to honor Sovereign Zero-Cloud Invariant
        obs.isEnabled = false
        XCTAssertFalse(obs.isEnabled)

        // Write while disabled must succeed with zero network I/O
        obs.logError(category: "test", message: "Should be dropped safely")
        obs.logQPS(action: "test_action")
        obs.logModelInference(input: "test", output: [:], latencyMs: 0.1)
    }

    func testLokiObservabilityToggleAndEndpointStorage() {
        let obs = LokiObservability.shared
        obs.isEnabled = true
        XCTAssertTrue(obs.isEnabled)

        let customUrl = URL(string: "http://127.0.0.1:3100/loki/api/v1/push")!
        obs.endpointUrl = customUrl
        XCTAssertEqual(obs.endpointUrl, customUrl)

        // Reset to false for clean test state
        obs.isEnabled = false
    }

    func testMonitoringIsStrictlyNonBlocking() {
        let obs = LokiObservability.shared
        obs.isEnabled = true
        defer { obs.isEnabled = false }

        let start = CFAbsoluteTimeGetCurrent()
        let iterations = 500
        for i in 0..<iterations {
            obs.logQPS(action: "benchmark_action_\(i)", latencyMs: 0.5)
            obs.logError(category: "benchmark", message: "Synthetic non-blocking test log \(i)")
            obs.logModelInference(input: "Benchmark input \(i)", output: ["iter": i], latencyMs: 0.2)
        }
        let totalElapsedMs = (CFAbsoluteTimeGetCurrent() - start) * 1000.0

        // 1,500 total log invocations must complete on caller thread in < 50ms (average < 0.03ms per call)
        XCTAssertLessThan(totalElapsedMs, 50.0, "Monitoring writes must be strictly non-blocking on caller thread (actual: \(totalElapsedMs)ms for 1500 calls)")
    }

    func testQuickCapturePrePopulationRequiresUserConfirmationFlow() {
        store.createProfile(name: "Test User", faceId: false)
        let initialCount = store.contexts.count

        // Simulate typing free text into quick capture bar
        let input = "System Architecture Handover 15:00 - 17:00 // Review documentation #planned"
        let parsed = ThoughtParser.parse(input: input, referenceDate: Date(), defaultStatus: .planned, recordTelemetry: false)

        // Entering text only parses the data into structured form; it must NOT silently create a context
        XCTAssertEqual(parsed.title, "System Architecture Handover")
        XCTAssertEqual(parsed.notes, "Review documentation")
        XCTAssertEqual(parsed.status, .planned)
        XCTAssertEqual(parsed.durationMinutes, 120)
        XCTAssertEqual(store.contexts.count, initialCount, "Entering text must not silently create contexts under the table")

        // Only when user confirms in the sheet (taps Create Thought) is the item committed
        let start = parsed.startAt ?? Date()
        let id = store.addContext(
            title: parsed.title,
            notes: parsed.notes,
            startAt: start,
            endAt: parsed.endAt,
            status: parsed.status,
            colorTag: parsed.colorTag,
            isBanner: parsed.isBanner
        )

        XCTAssertEqual(store.contexts.count, initialCount + 1, "Context must be committed only upon explicit user confirmation")
        let created = store.contexts.first(where: { $0.id == id })
        XCTAssertEqual(created?.title, "System Architecture Handover")
        XCTAssertEqual(created?.notes, "Review documentation")
        XCTAssertEqual(created?.status, .planned)
    }
}
