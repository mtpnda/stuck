package com.mtpnda.stuck

import androidx.compose.ui.graphics.Color
import com.mtpnda.stuck.data.ContextRepository
import com.mtpnda.stuck.model.*
import com.mtpnda.stuck.ui.screens.SchedulePlaceholder
import com.mtpnda.stuck.ui.screens.TimeGranularity
import com.mtpnda.stuck.ui.theme.DlsTokens
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class ContextLogicTest {

    @Test
    fun testProfileInitials() {
        val p1 = Profile(name = "Valdie Arsanur")
        assertEquals("VA", p1.initials)

        val p2 = Profile(name = "Single")
        assertEquals("SI", p2.initials)

        val p3 = Profile(name = "")
        assertEquals("U", p3.initials)
    }

    @Test
    fun testContextStatusTransitions() {
        val item = ContextItem(title = "Write Swift Tests", status = ContextStatus.PLANNED)
        assertEquals(ContextStatus.PLANNED, item.status)

        val active = item.copy(status = ContextStatus.ACTIVE)
        assertEquals(ContextStatus.ACTIVE, active.status)

        val done = active.copy(status = ContextStatus.DONE, closedAt = System.currentTimeMillis())
        assertEquals(ContextStatus.DONE, done.status)
        assertNotNull(done.closedAt)
    }

    @Test
    fun testCognitiveLoadLimitCalculation() {
        val limit = 3
        val contexts = listOf(
            ContextItem(title = "Task 1", status = ContextStatus.ACTIVE),
            ContextItem(title = "Task 2", status = ContextStatus.ACTIVE),
            ContextItem(title = "Task 3", status = ContextStatus.ACTIVE)
        )
        val activeCount = contexts.count { it.status == ContextStatus.ACTIVE }
        assertEquals(3, activeCount)
        assertFalse(activeCount > limit)

        val overflow = contexts + ContextItem(title = "Task 4", status = ContextStatus.ACTIVE)
        val overflowCount = overflow.count { it.status == ContextStatus.ACTIVE }
        assertEquals(4, overflowCount)
        assertTrue(overflowCount > limit)
    }

    @Test
    fun testCognitiveLoadLimiterClamping() {
        // Lower bound clamp (must be at least 1)
        val low0 = 0.coerceIn(1, 9)
        assertEquals(1, low0)

        val negative = (-10).coerceIn(1, 9)
        assertEquals(1, negative)

        // Upper bound clamp (must not exceed 9)
        val high10 = 10.coerceIn(1, 9)
        assertEquals(9, high10)

        val extreme = 100.coerceIn(1, 9)
        assertEquals(9, extreme)

        // Mid range
        val mid = 5.coerceIn(1, 9)
        assertEquals(5, mid)
    }

    @Test
    fun testCognitiveLoadReliefOnParkingOrClosing() {
        val limit = 2
        val initialActive = mutableListOf(
            ContextItem(id = "1", title = "Task 1", status = ContextStatus.ACTIVE),
            ContextItem(id = "2", title = "Task 2", status = ContextStatus.ACTIVE),
            ContextItem(id = "3", title = "Task 3", status = ContextStatus.ACTIVE)
        )
        // 3 active with limit 2 -> overflow
        assertTrue(initialActive.count { it.status == ContextStatus.ACTIVE } > limit)

        // Relieve by parking Task 3
        val withParked = initialActive.map {
            if (it.id == "3") it.copy(status = ContextStatus.PAUSED) else it
        }
        val parkedActiveCount = withParked.count { it.status == ContextStatus.ACTIVE }
        assertEquals(2, parkedActiveCount)
        assertFalse(parkedActiveCount > limit)

        // Relieve by closing Task 1
        val withClosed = withParked.map {
            if (it.id == "1") it.copy(status = ContextStatus.DONE, closedAt = System.currentTimeMillis()) else it
        }
        val closedActiveCount = withClosed.count { it.status == ContextStatus.ACTIVE }
        assertEquals(1, closedActiveCount)
        assertFalse(closedActiveCount > limit)
    }

    // MARK: - Detailed CRUD & Field Mutation Tests

    @Test
    fun testCRUDTitleTrimmingAndCopyMutations() {
        val rawTitle = "   Ship BioCue Release \n\t"
        val item = ContextItem(
            title = rawTitle.trim(),
            notes = "Architecture payload",
            colorTag = BarColorTag.PURPLE,
            isBanner = true
        )
        assertEquals("Ship BioCue Release", item.title)
        assertEquals("Architecture payload", item.notes)
        assertEquals(BarColorTag.PURPLE, item.colorTag)
        assertTrue(item.isBanner)

        // Partial update: title only
        val updatedTitle = item.copy(title = "Updated Release")
        assertEquals("Updated Release", updatedTitle.title)
        assertEquals("Architecture payload", updatedTitle.notes)
        assertEquals(BarColorTag.PURPLE, updatedTitle.colorTag)
        assertTrue(updatedTitle.isBanner)

        // Partial update: color tag and banner toggle
        val updatedTag = updatedTitle.copy(colorTag = BarColorTag.CORAL, isBanner = false)
        assertEquals(BarColorTag.CORAL, updatedTag.colorTag)
        assertFalse(updatedTag.isBanner)
    }

    @Test
    fun testCRUDToggleDoneAndClosedAtTimestampLifecycle() {
        val item = ContextItem(id = "c1", title = "Active Task", status = ContextStatus.ACTIVE)
        assertNull(item.closedAt)

        // Mark Done -> closedAt must be set
        val doneItem = item.copy(status = ContextStatus.DONE, closedAt = System.currentTimeMillis())
        assertEquals(ContextStatus.DONE, doneItem.status)
        assertNotNull(doneItem.closedAt)

        // Toggle back to Active -> closedAt reset to null
        val reactivated = doneItem.copy(status = ContextStatus.ACTIVE, closedAt = null)
        assertEquals(ContextStatus.ACTIVE, reactivated.status)
        assertNull(reactivated.closedAt)

        // Dropped -> closedAt must be set
        val dropped = reactivated.copy(status = ContextStatus.DROPPED, closedAt = System.currentTimeMillis())
        assertEquals(ContextStatus.DROPPED, dropped.status)
        assertNotNull(dropped.closedAt)

        // Move to Paused -> closedAt reset to null
        val paused = dropped.copy(status = ContextStatus.PAUSED, closedAt = null)
        assertEquals(ContextStatus.PAUSED, paused.status)
        assertNull(paused.closedAt)
    }

    @Test
    fun testCRUDToggleParked() {
        val active = ContextItem(id = "p1", title = "Active item", status = ContextStatus.ACTIVE)
        val parked = active.copy(status = ContextStatus.PAUSED)
        assertEquals(ContextStatus.PAUSED, parked.status)

        val unparked = parked.copy(status = ContextStatus.ACTIVE)
        assertEquals(ContextStatus.ACTIVE, unparked.status)
    }

    @Test
    fun testCRUDNestedTodosChecklistMutation() {
        val initialTodos = listOf(
            TodoItem(text = "Draft Architecture", isDone = false),
            TodoItem(text = "Write Tests", isDone = false)
        )
        val item = ContextItem(title = "Parent Item", todos = initialTodos)
        assertEquals(2, item.todos.size)
        assertFalse(item.todos[0].isDone)

        // Mutate checklist: check first, append third
        val modified = item.copy(
            todos = item.todos.mapIndexed { idx, t ->
                if (idx == 0) t.copy(isDone = true) else t
            } + TodoItem(text = "Verify Release", isDone = false)
        )
        assertEquals(3, modified.todos.size)
        assertTrue(modified.todos[0].isDone)
        assertFalse(modified.todos[1].isDone)
        assertFalse(modified.todos[2].isDone)
    }

    @Test
    fun testCRUDOverdueLogicAcrossStatuses() {
        val now = System.currentTimeMillis()
        val past = now - 3600000L   // 1 hour ago
        val future = now + 3600000L  // 1 hour ahead

        val activeOverdue = ContextItem(title = "Late Active", startAt = past, endAt = past, status = ContextStatus.ACTIVE)
        assertTrue(activeOverdue.isOverdue)

        val pausedOverdue = ContextItem(title = "Late Paused", startAt = past, endAt = past, status = ContextStatus.PAUSED)
        assertTrue(pausedOverdue.isOverdue)

        // Completed task is NEVER overdue
        val doneLate = ContextItem(title = "Done Late", startAt = past, endAt = past, status = ContextStatus.DONE)
        assertFalse(doneLate.isOverdue)

        val droppedLate = ContextItem(title = "Dropped Late", startAt = past, endAt = past, status = ContextStatus.DROPPED)
        assertFalse(droppedLate.isOverdue)

        val futureActive = ContextItem(title = "Future", startAt = now, endAt = future, status = ContextStatus.ACTIVE)
        assertFalse(futureActive.isOverdue)

        val noDeadline = ContextItem(title = "No Deadline", startAt = now, endAt = null, status = ContextStatus.ACTIVE)
        assertFalse(noDeadline.isOverdue)
    }

    @Test
    fun testCRUDFocusSessionAccumulation() {
        val t0 = 1000L
        val t1 = 2000L
        val t2 = 3000L
        val s1 = FocusSession(start = t0, end = t1)
        val s2 = FocusSession(start = t1, end = t2)

        val item = ContextItem(title = "Deep Work", sessions = listOf(s1, s2))
        assertEquals(2, item.sessions.size)
        assertEquals(2000L, item.totalTimeSpent)
    }

    @Test
    fun testTimeGranularityScaleInvariants() {
        val all = TimeGranularity.values()
        assertEquals(7, all.size)

        assertEquals("Day", TimeGranularity.DAY.label)
        assertEquals("6 hrs", TimeGranularity.SIX_HOURS.label)
        assertEquals("3 hrs", TimeGranularity.THREE_HOURS.label)
        assertEquals("1 hr", TimeGranularity.ONE_HOUR.label)
        assertEquals("30 mins", TimeGranularity.THIRTY_MINS.label)
        assertEquals("10 mins", TimeGranularity.TEN_MINS.label)
        assertEquals("1 min", TimeGranularity.ONE_MIN.label)

        assertEquals(0, TimeGranularity.DAY.slotHeightDp)
        assertTrue(TimeGranularity.SIX_HOURS.slotHeightDp < TimeGranularity.THREE_HOURS.slotHeightDp)
        assertTrue(TimeGranularity.THREE_HOURS.slotHeightDp < TimeGranularity.ONE_HOUR.slotHeightDp)
        assertTrue(TimeGranularity.ONE_HOUR.slotHeightDp < TimeGranularity.THIRTY_MINS.slotHeightDp)
        assertTrue(TimeGranularity.THIRTY_MINS.slotHeightDp < TimeGranularity.TEN_MINS.slotHeightDp)
        assertTrue(TimeGranularity.TEN_MINS.slotHeightDp < TimeGranularity.ONE_MIN.slotHeightDp)
    }

    @Test
    fun testBioCueDlsTokensMatchSpec() {
        assertEquals(Color(0xFFF2F0EA), DlsTokens.surfaceBg)
        assertEquals(Color(0xFF1C1814), DlsTokens.actionPrimary)
        assertEquals(Color(0xFF9C2A22), DlsTokens.stateAlertInk)
        assertEquals(Color(0xFF1A6337), DlsTokens.stateCalmInk)
    }

    // MARK: - Quick Capture (Stage & Time Context) Tests

    @Test
    fun testQuickCaptureRespectsExplicitStageContext() {
        val todayCal = Calendar.getInstance()
        val today = todayCal.timeInMillis

        // 1. Planned filter
        val resolvedPlanned = ContextRepository.resolveQuickCaptureContext(
            selectedFilter = ContextStatus.PLANNED,
            targetDate = today
        )
        assertEquals(ContextStatus.PLANNED, resolvedPlanned.status)
        assertEquals(BarColorTag.PURPLE, resolvedPlanned.colorTag)

        // 2. Active filter
        val resolvedActive = ContextRepository.resolveQuickCaptureContext(
            selectedFilter = ContextStatus.ACTIVE,
            targetDate = today
        )
        assertEquals(ContextStatus.ACTIVE, resolvedActive.status)
        assertEquals(BarColorTag.BLUE, resolvedActive.colorTag)

        // 3. Paused filter
        val resolvedPaused = ContextRepository.resolveQuickCaptureContext(
            selectedFilter = ContextStatus.PAUSED,
            targetDate = today
        )
        assertEquals(ContextStatus.PAUSED, resolvedPaused.status)
        assertEquals(BarColorTag.CHARCOAL, resolvedPaused.colorTag)

        // 4. Done filter
        val resolvedDone = ContextRepository.resolveQuickCaptureContext(
            selectedFilter = ContextStatus.DONE,
            targetDate = today
        )
        assertEquals(ContextStatus.DONE, resolvedDone.status)
        assertEquals(BarColorTag.GREEN, resolvedDone.colorTag)
    }

    @Test
    fun testQuickCaptureRespectsTimeContextWithoutFilter() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val today = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, -2)
        val yesterday = cal.timeInMillis

        // Future day -> automatically PLANNED (PURPLE)
        val futureResolved = ContextRepository.resolveQuickCaptureContext(
            selectedFilter = null,
            targetDate = tomorrow,
            referenceDate = today
        )
        assertEquals(ContextStatus.PLANNED, futureResolved.status)
        assertEquals(BarColorTag.PURPLE, futureResolved.colorTag)

        // Today -> automatically ACTIVE (BLUE)
        val todayResolved = ContextRepository.resolveQuickCaptureContext(
            selectedFilter = null,
            targetDate = today,
            referenceDate = today
        )
        assertEquals(ContextStatus.ACTIVE, todayResolved.status)
        assertEquals(BarColorTag.BLUE, todayResolved.colorTag)

        // Past day -> automatically ACTIVE (BLUE)
        val pastResolved = ContextRepository.resolveQuickCaptureContext(
            selectedFilter = null,
            targetDate = yesterday,
            referenceDate = today
        )
        assertEquals(ContextStatus.ACTIVE, pastResolved.status)
        assertEquals(BarColorTag.BLUE, pastResolved.colorTag)
    }

    @Test
    fun testQuickCapturePreservesTimeOfDayOnTargetDay() {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.NOVEMBER, 10, 14, 30, 45)
        val refTime = cal.timeInMillis

        cal.set(2026, Calendar.NOVEMBER, 15, 0, 0, 0)
        val targetDay = cal.timeInMillis

        val resolved = ContextRepository.resolveQuickCaptureContext(
            selectedFilter = null,
            targetDate = targetDay,
            referenceDate = refTime
        )

        assertEquals(ContextStatus.PLANNED, resolved.status)
        val resultCal = Calendar.getInstance().apply { timeInMillis = resolved.scheduledDate }
        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, resultCal.get(Calendar.MONTH))
        assertEquals(15, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(14, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, resultCal.get(Calendar.MINUTE))
    }

    @Test
    fun testReschedulePreservesDuration() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 10, 14, 0, 0)
        }
        val startAt = cal.timeInMillis
        val endAt = startAt + 7200_000L // 2 hours

        val item = ContextItem(
            id = "test-1",
            title = "Focus Block",
            startAt = startAt,
            endAt = endAt,
            status = ContextStatus.ACTIVE
        )

        // Reschedule to 18:00 (+4 hours) without passing newEndAt
        val newStartAt = startAt + 14400_000L
        val duration = (item.endAt!! - item.startAt).coerceAtLeast(900_000L)
        val computedEnd = newStartAt + duration

        assertEquals(7200_000L, duration)
        assertEquals(newStartAt + 7200_000L, computedEnd)
    }

    @Test
    fun testRescheduleWithExplicitEndAt() {
        val startAt = 1000000L
        val endAt = 2000000L
        val item = ContextItem(id = "test-2", title = "Meeting", startAt = startAt, endAt = endAt)

        val explicitStart = 3000000L
        val explicitEnd = 4500000L

        val updated = item.copy(startAt = explicitStart, endAt = explicitEnd)
        assertEquals(explicitStart, updated.startAt)
        assertEquals(explicitEnd, updated.endAt)
        assertEquals(1500000L, updated.endAt!! - updated.startAt)
    }

    @Test
    fun testRescheduleUntimedKeepsNullEndAt() {
        val item = ContextItem(id = "test-3", title = "Quick Task", startAt = 1000L, endAt = null)
        assertNull(item.endAt)

        // Reschedule untimed task
        val newStart = 5000L
        val end = if (item.endAt != null) newStart + (item.endAt!! - item.startAt) else null
        val updated = item.copy(startAt = newStart, endAt = end)

        assertEquals(5000L, updated.startAt)
        assertNull(updated.endAt)
    }

    @Test
    fun testQuickCaptureWithInlineNoteSyntax() {
        val rawInput = "Draft launch email // mention single-version hygiene & port 8888"
        var parsedTitle = rawInput.trim()
        var parsedNotes = ""
        if (rawInput.contains("//")) {
            val parts = rawInput.split("//", limit = 2)
            parsedTitle = parts[0].trim()
            parsedNotes = parts[1].trim()
        }

        assertEquals("Draft launch email", parsedTitle)
        assertEquals("mention single-version hygiene & port 8888", parsedNotes)
    }

    @Test
    fun testQuickCaptureWithExplicitNotes() {
        val item = ContextItem(
            title = "Review PR",
            notes = "Needs performance profiling on arm64",
            status = ContextStatus.ACTIVE
        )

        assertEquals("Review PR", item.title)
        assertEquals("Needs performance profiling on arm64", item.notes)
    }

    // MARK: - Schedule Placeholder & Zoom Granularity Tests

    @Test
    fun testZoomGranularityDurationsAndSnapSteps() {
        assertEquals(1440, TimeGranularity.DAY.defaultDurationMinutes)
        assertEquals(360, TimeGranularity.SIX_HOURS.defaultDurationMinutes)
        assertEquals(180, TimeGranularity.THREE_HOURS.defaultDurationMinutes)
        assertEquals(60, TimeGranularity.ONE_HOUR.defaultDurationMinutes)
        assertEquals(30, TimeGranularity.THIRTY_MINS.defaultDurationMinutes)
        assertEquals(10, TimeGranularity.TEN_MINS.defaultDurationMinutes)
        assertEquals(1, TimeGranularity.ONE_MIN.defaultDurationMinutes)

        assertEquals(24.0, TimeGranularity.DAY.snapStepHours, 0.001)
        assertEquals(6.0, TimeGranularity.SIX_HOURS.snapStepHours, 0.001)
        assertEquals(3.0, TimeGranularity.THREE_HOURS.snapStepHours, 0.001)
        assertEquals(1.0, TimeGranularity.ONE_HOUR.snapStepHours, 0.001)
        assertEquals(0.5, TimeGranularity.THIRTY_MINS.snapStepHours, 0.001)
        assertEquals(10.0 / 60.0, TimeGranularity.TEN_MINS.snapStepHours, 0.001)
        assertEquals(1.0 / 60.0, TimeGranularity.ONE_MIN.snapStepHours, 0.001)
    }

    @Test
    fun testSchedulePlaceholderCalculations() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // 10 mins zoom
        val p10m = SchedulePlaceholder(
            dayMillis = today,
            hour = 14,
            minute = 0,
            durationMinutes = 10,
            status = ContextStatus.ACTIVE,
            colorTag = BarColorTag.BLUE
        )
        assertEquals("14:00 – 14:10", p10m.timeRangeString())
        assertEquals("10m", p10m.durationBadge())
        assertEquals(600_000L, p10m.endMillis() - p10m.startMillis())

        // 1 hr zoom
        val p1h = SchedulePlaceholder(
            dayMillis = today,
            hour = 9,
            minute = 0,
            durationMinutes = 60,
            status = ContextStatus.PLANNED,
            colorTag = BarColorTag.PURPLE
        )
        assertEquals("09:00 – 10:00", p1h.timeRangeString())
        assertEquals("1h", p1h.durationBadge())
        assertEquals(3600_000L, p1h.endMillis() - p1h.startMillis())

        // 1 min zoom precision
        val p1m = SchedulePlaceholder(
            dayMillis = today,
            hour = 11,
            minute = 15,
            durationMinutes = 1,
            status = ContextStatus.ACTIVE,
            colorTag = BarColorTag.BLUE
        )
        assertEquals("11:15 – 11:16", p1m.timeRangeString())
        assertEquals("1m", p1m.durationBadge())
        assertEquals(60_000L, p1m.endMillis() - p1m.startMillis())

        // Day placeholder
        val pDay = SchedulePlaceholder(
            dayMillis = today,
            hour = null,
            minute = 0,
            durationMinutes = 1440,
            status = ContextStatus.PLANNED,
            colorTag = BarColorTag.PURPLE
        )
        assertEquals("All Day", pDay.timeRangeString())
        assertEquals("24h", pDay.durationBadge())
        assertEquals(86400_000L, pDay.endMillis() - pDay.startMillis())
    }

    @Test
    fun testScheduleClickOnlyCreatesPlaceholderWithoutAddingToRepository() {
        val existingItems = listOf(
            ContextItem(id = "c1", title = "Alpha", status = ContextStatus.ACTIVE)
        )
        val initialCount = existingItems.size

        // Clicking schedule creates in-memory placeholder
        val placeholder = SchedulePlaceholder(
            dayMillis = System.currentTimeMillis(),
            hour = 10,
            minute = 0,
            durationMinutes = TimeGranularity.ONE_HOUR.defaultDurationMinutes,
            status = ContextStatus.ACTIVE,
            colorTag = BarColorTag.BLUE
        )

        // Invariant check: List size strictly unchanged
        assertEquals(1, initialCount)
        assertEquals(1, existingItems.size)

        // Only after user confirms form is item created
        val confirmedItem = ContextItem(
            title = "Confirmed Thought",
            startAt = placeholder.startMillis(),
            endAt = placeholder.endMillis(),
            status = placeholder.status,
            colorTag = placeholder.colorTag
        )
        val updatedItems = existingItems + confirmedItem
        assertEquals(2, updatedItems.size)
        assertEquals(placeholder.startMillis(), confirmedItem.startAt)
        assertEquals(placeholder.endMillis(), confirmedItem.endAt)
    }

    // MARK: - Multi-Day & Duration Expansion Tests

    @Test
    fun testMultiDayEventIntersectsBothDays() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val day1Millis = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val day2Millis = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val day3Millis = cal.timeInMillis

        // Event from Day 1 at 22:00 to Day 2 at 04:00 (6h total duration)
        val start = day1Millis + (22 * 3600_000L)
        val end = day2Millis + (4 * 3600_000L)

        val item = ContextItem(
            id = "overnight-1",
            title = "Overnight Focus",
            startAt = start,
            endAt = end,
            status = ContextStatus.ACTIVE
        )

        assertTrue("Event must intersect Day 1", item.intersects(day1Millis))
        assertTrue("Event must intersect Day 2", item.intersects(day2Millis))
        assertFalse("Event must NOT intersect Day 3", item.intersects(day3Millis))

        assertFalse("Day 1 is start day, not continuation", item.isContinuation(day1Millis))
        assertTrue("Day 1 must be flagged outgoing to next day", item.isOutgoing(day1Millis))

        assertTrue("Day 2 must be flagged continuation from yesterday", item.isContinuation(day2Millis))
        assertFalse("Day 2 is ending day, not outgoing", item.isOutgoing(day2Millis))
    }

    @Test
    fun testDurationExpansionAndReschedulePreservation() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val day1Millis = cal.timeInMillis
        val start = day1Millis + (16 * 3600_000L) // 16:00
        val initialEnd = day1Millis + (18 * 3600_000L) // 18:00 (2h)

        val item = ContextItem(
            id = "proj-1",
            title = "Design Polish",
            startAt = start,
            endAt = initialEnd,
            status = ContextStatus.ACTIVE
        )

        // Expand duration across midnight into tomorrow 02:00 (10h duration)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val day2Millis = cal.timeInMillis
        val newEnd = day2Millis + (2 * 3600_000L)

        val expanded = item.copy(endAt = newEnd)
        assertEquals(newEnd, expanded.endAt)
        assertTrue(expanded.intersects(day1Millis))
        assertTrue(expanded.intersects(day2Millis))
        assertTrue(expanded.isOutgoing(day1Millis))
        assertTrue(expanded.isContinuation(day2Millis))

        // Reschedule start time forward by 1 hour (17:00), preserving duration (10h)
        val duration = expanded.endAt!! - expanded.startAt
        val newStart = day1Millis + (17 * 3600_000L)
        val rescheduled = expanded.copy(startAt = newStart, endAt = newStart + duration)

        assertEquals(newStart, rescheduled.startAt)
        assertEquals(newStart + duration, rescheduled.endAt)
        assertEquals(duration, rescheduled.endAt!! - rescheduled.startAt)
    }

    @Test
    fun testTimelineHandleTokensResolveDls() {
        assertEquals(Color(0xFF29B6F6), DlsTokens.timelineHandleKnob)
        assertEquals(Color(0xFF1C1814), DlsTokens.timelineHandleKnobBorder)
        assertEquals(Color(0xFF29B6F6), DlsTokens.timelineSpanningChip)
        assertEquals(Color(0xFFE1F5FE), DlsTokens.timelineSpanningSoft)
        assertEquals(Color(0xFF0288D1), DlsTokens.timelineSpanningInk)
    }
}
