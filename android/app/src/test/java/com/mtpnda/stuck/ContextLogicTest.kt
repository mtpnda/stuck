package com.mtpnda.stuck

import androidx.compose.ui.graphics.Color
import com.mtpnda.stuck.data.ContextRepository
import com.mtpnda.stuck.model.*
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
}
