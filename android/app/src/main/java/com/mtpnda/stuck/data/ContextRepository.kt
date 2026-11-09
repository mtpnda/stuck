package com.mtpnda.stuck.data

import android.content.Context
import android.content.SharedPreferences
import com.mtpnda.stuck.model.*
import com.mtpnda.stuck.observability.LokiObservability
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.UUID

class ContextRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("contexts_native_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _contexts = MutableStateFlow<List<ContextItem>>(emptyList())
    val contexts: StateFlow<List<ContextItem>> = _contexts.asStateFlow()

    private val _limit = MutableStateFlow(3)
    val limit: StateFlow<Int> = _limit.asStateFlow()

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _focusContextId = MutableStateFlow<String?>(null)
    val focusContextId: StateFlow<String?> = _focusContextId.asStateFlow()

    private var focusStartedAt: Long? = null

    init {
        load()
    }

    fun createProfile(name: String, email: String? = null, avatarUrl: String? = null, faceId: Boolean = true, provider: String = "local") {
        val p = Profile(
            name = name.trim(),
            email = email?.trim()?.ifBlank { null },
            avatarUrl = avatarUrl,
            faceId = faceId,
            provider = provider
        )
        _profile.value = p
        _isLocked.value = false
        save()
    }

    fun updateProfile(name: String? = null, email: String? = null, faceId: Boolean? = null) {
        val current = _profile.value ?: return
        val updated = current.copy(
            name = name ?: current.name,
            email = email ?: current.email,
            faceId = faceId ?: current.faceId
        )
        _profile.value = updated
        save()
    }

    fun signOut() {
        _profile.value = null
        _isLocked.value = true
        save()
    }

    fun resetAllData() {
        stopFocus()
        _profile.value = null
        _contexts.value = emptyList()
        _limit.value = 3
        _isLocked.value = true
        save()
    }

    fun unlock() {
        _isLocked.value = false
    }

    fun lock() {
        if (_profile.value?.faceId == true) {
            _isLocked.value = true
        }
    }

    fun setLimit(newLimit: Int) {
        _limit.value = newLimit.coerceIn(1, 9)
        save()
    }

    data class QuickCaptureContext(
        val status: ContextStatus,
        val colorTag: BarColorTag,
        val scheduledDate: Long
    )

    companion object {
        fun resolveQuickCaptureContext(
            selectedFilter: ContextStatus?,
            targetDate: Long,
            referenceDate: Long = System.currentTimeMillis()
        ): QuickCaptureContext {
            val targetCal = Calendar.getInstance().apply {
                timeInMillis = targetDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val refCal = Calendar.getInstance().apply {
                timeInMillis = referenceDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val status = when {
                selectedFilter != null -> selectedFilter
                targetCal.timeInMillis > refCal.timeInMillis -> ContextStatus.PLANNED
                else -> ContextStatus.ACTIVE
            }

            val color = when (status) {
                ContextStatus.ACTIVE -> BarColorTag.BLUE
                ContextStatus.PLANNED -> BarColorTag.PURPLE
                ContextStatus.PAUSED -> BarColorTag.CHARCOAL
                ContextStatus.DONE -> BarColorTag.GREEN
                ContextStatus.DROPPED -> BarColorTag.CHARCOAL
            }

            val scheduledCal = Calendar.getInstance().apply {
                timeInMillis = targetDate
                val currentCal = Calendar.getInstance().apply { timeInMillis = referenceDate }
                set(Calendar.HOUR_OF_DAY, currentCal.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, currentCal.get(Calendar.MINUTE))
                set(Calendar.SECOND, currentCal.get(Calendar.SECOND))
            }

            return QuickCaptureContext(status, color, scheduledCal.timeInMillis)
        }
    }

    fun quickCapture(
        title: String,
        notes: String = "",
        targetDate: Long,
        stageFilter: ContextStatus? = null
    ): String {
        val targetLocalDate = Instant.ofEpochMilli(targetDate).atZone(ZoneId.systemDefault()).toLocalDate()
        val parsed = ThoughtParser.parse(
            input = title,
            referenceDate = targetLocalDate,
            defaultStatus = stageFilter,
            recordTelemetry = true
        )

        val finalNotes = if (notes.isNotBlank()) notes else parsed.notes
        val scheduled = parsed.startAt?.toEpochMilli() ?: resolveQuickCaptureContext(stageFilter, targetDate).scheduledDate
        val endAtMs = parsed.endAt?.toEpochMilli()

        LokiObservability.shared.logQPS(
            action = "quick_capture",
            metadata = mapOf(
                "title" to parsed.title,
                "stage" to parsed.status.name.lowercase(),
                "has_duration" to (parsed.durationMinutes != null).toString()
            )
        )

        return addContext(
            title = parsed.title,
            notes = finalNotes,
            startAt = scheduled,
            endAt = endAtMs,
            status = parsed.status,
            colorTag = parsed.colorTag,
            isBanner = parsed.isBanner
        )
    }

    fun addContext(
        title: String,
        notes: String = "",
        todos: List<TodoItem> = emptyList(),
        startAt: Long = System.currentTimeMillis(),
        endAt: Long? = null,
        status: ContextStatus = ContextStatus.ACTIVE,
        colorTag: BarColorTag = BarColorTag.BLUE,
        isBanner: Boolean = false
    ): String {
        val now = System.currentTimeMillis()
        val initialStatus = if (startAt > now && status == ContextStatus.ACTIVE) ContextStatus.ACTIVE else status
        val item = ContextItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            notes = notes,
            todos = todos,
            startAt = startAt,
            endAt = endAt,
            sessions = emptyList(),
            status = initialStatus,
            createdAt = now,
            closedAt = null,
            colorTag = colorTag,
            isBanner = isBanner
        )
        _contexts.value = listOf(item) + _contexts.value
        save()
        LokiObservability.shared.logQPS(action = "context_add", metadata = mapOf("id" to item.id, "stage" to item.status.name.lowercase()))
        return item.id
    }

    fun updateContext(
        id: String,
        title: String? = null,
        notes: String? = null,
        todos: List<TodoItem>? = null,
        startAt: Long? = null,
        endAt: Long? = null,
        status: ContextStatus? = null,
        colorTag: BarColorTag? = null,
        isBanner: Boolean? = null
    ) {
        if (_focusContextId.value == id && (status == ContextStatus.DONE || status == ContextStatus.DROPPED)) {
            stopFocus()
        }
        _contexts.value = _contexts.value.map { c ->
            if (c.id == id) {
                var updated = c
                if (title != null) updated = updated.copy(title = title.trim())
                if (notes != null) updated = updated.copy(notes = notes)
                if (todos != null) updated = updated.copy(todos = todos)
                if (startAt != null) updated = updated.copy(startAt = startAt)
                if (endAt != null) updated = updated.copy(endAt = endAt)
                if (colorTag != null) updated = updated.copy(colorTag = colorTag)
                if (isBanner != null) updated = updated.copy(isBanner = isBanner)
                if (status != null) {
                    val closed = if (status == ContextStatus.DONE || status == ContextStatus.DROPPED) System.currentTimeMillis() else null
                    updated = updated.copy(status = status, closedAt = closed)
                }
                updated
            } else c
        }
        save()
    }

    fun toggleDone(id: String) {
        val item = _contexts.value.firstOrNull { it.id == id } ?: return
        if (item.status == ContextStatus.DONE) {
            updateContext(id = id, status = ContextStatus.ACTIVE)
        } else {
            updateContext(id = id, status = ContextStatus.DONE)
        }
    }

    fun toggleParked(id: String) {
        val item = _contexts.value.firstOrNull { it.id == id } ?: return
        if (item.status == ContextStatus.PAUSED) {
            updateContext(id = id, status = ContextStatus.ACTIVE)
        } else {
            updateContext(id = id, status = ContextStatus.PAUSED)
        }
    }

    fun moveToDay(id: String, timeMillis: Long) {
        updateContext(id = id, startAt = timeMillis)
    }

    fun rescheduleTime(id: String, newStartAt: Long, newEndAt: Long? = null) {
        _contexts.value = _contexts.value.map { c ->
            if (c.id == id) {
                val end = when {
                    newEndAt != null -> newEndAt
                    c.endAt != null -> {
                        val duration = (c.endAt - c.startAt).coerceAtLeast(900_000L)
                        newStartAt + duration
                    }
                    else -> null
                }
                c.copy(startAt = newStartAt, endAt = end)
            } else {
                c
            }
        }
        save()
    }

    fun expandDuration(id: String, newEndAt: Long) {
        _contexts.value = _contexts.value.map { c ->
            if (c.id == id && newEndAt > c.startAt) {
                c.copy(endAt = newEndAt)
            } else {
                c
            }
        }
        save()
    }

    fun setStatus(id: String, status: ContextStatus) {
        updateContext(id = id, status = status)
    }

    fun removeContext(id: String) {
        if (_focusContextId.value == id) stopFocus()
        _contexts.value = _contexts.value.filter { it.id != id }
        save()
    }

    fun focusOn(id: String) {
        if (_focusContextId.value == id) return
        stopFocus()
        setStatus(id, ContextStatus.ACTIVE)
        _focusContextId.value = id
        focusStartedAt = System.currentTimeMillis()
    }

    fun stopFocus() {
        val id = _focusContextId.value ?: return
        val started = focusStartedAt ?: return
        val session = FocusSession(start = started, end = System.currentTimeMillis())
        _contexts.value = _contexts.value.map { c ->
            if (c.id == id) c.copy(sessions = c.sessions + session) else c
        }
        _focusContextId.value = null
        focusStartedAt = null
        save()
    }

    private fun seedSampleContexts() {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 2)
        val tomorrow = cal.timeInMillis

        _contexts.value = listOf(
            ContextItem(
                title = "System Architecture Handover",
                notes = "Executive milestone and sprint review",
                startAt = now,
                status = ContextStatus.ACTIVE,
                colorTag = BarColorTag.CYAN,
                isBanner = true
            ),
            ContextItem(
                title = "[PH] All Saints' Day",
                notes = "Holiday observance",
                startAt = now,
                status = ContextStatus.ACTIVE,
                colorTag = BarColorTag.GREEN
            ),
            ContextItem(
                title = "Air PDAM",
                notes = "Utility bill payment",
                startAt = now,
                status = ContextStatus.DONE,
                colorTag = BarColorTag.BLUE
            ),
            ContextItem(
                title = "Bayar BPJS",
                notes = "Health insurance premium",
                startAt = now,
                status = ContextStatus.DONE,
                colorTag = BarColorTag.BLUE
            ),
            ContextItem(
                title = "Bayar IPL",
                notes = "Maintenance fee payment",
                startAt = now,
                status = ContextStatus.DONE,
                colorTag = BarColorTag.BLUE
            ),
            ContextItem(
                title = "Hock Yao on Leave",
                notes = "Sprint coverage",
                startAt = tomorrow,
                status = ContextStatus.PLANNED,
                colorTag = BarColorTag.PURPLE
            ),
            ContextItem(
                title = "[PH, BR] All Souls",
                notes = "Calendar holiday",
                startAt = tomorrow,
                status = ContextStatus.PLANNED,
                colorTag = BarColorTag.GREEN
            ),
            ContextItem(
                title = "Yohanzen on Leave",
                notes = "Mobile core team coverage",
                startAt = tomorrow,
                status = ContextStatus.PLANNED,
                colorTag = BarColorTag.PURPLE
            ),
            ContextItem(
                title = "Refactor Memory Profiler",
                notes = "Parked thought: trace heap allocation spikes",
                startAt = yesterday,
                status = ContextStatus.PAUSED,
                colorTag = BarColorTag.CHARCOAL
            )
        )
    }

    private fun save() {
        try {
            prefs.edit()
                .putString("profile_json", gson.toJson(_profile.value))
                .putString("contexts_json", gson.toJson(_contexts.value))
                .putInt("limit", _limit.value)
                .apply()
        } catch (e: Exception) {
            LokiObservability.shared.logError(
                category = "store_persistence",
                message = "Failed to save data to SharedPreferences",
                error = e
            )
        }
    }

    private fun load() {
        try {
            val profileJson = prefs.getString("profile_json", null)
            if (!profileJson.isNullOrBlank()) {
                _profile.value = gson.fromJson(profileJson, Profile::class.java)
            }
            val contextsJson = prefs.getString("contexts_json", null)
            if (!contextsJson.isNullOrBlank()) {
                val type = object : TypeToken<List<ContextItem>>() {}.type
                _contexts.value = gson.fromJson(contextsJson, type) ?: emptyList()
            }
            if (_contexts.value.isEmpty()) {
                seedSampleContexts()
            }
            _limit.value = prefs.getInt("limit", 3)
            _isLocked.value = _profile.value?.faceId ?: false
        } catch (e: Exception) {
            LokiObservability.shared.logError(
                category = "store_persistence",
                message = "Failed to load data from SharedPreferences",
                error = e
            )
        }
    }
}
