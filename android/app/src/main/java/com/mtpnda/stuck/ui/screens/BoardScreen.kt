package com.mtpnda.stuck.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.mtpnda.stuck.data.ContextRepository
import com.mtpnda.stuck.model.BarColorTag
import com.mtpnda.stuck.model.ContextItem
import com.mtpnda.stuck.model.ContextStatus
import com.mtpnda.stuck.ui.components.EmojiAvatar
import com.mtpnda.stuck.ui.theme.ContextColors
import com.mtpnda.stuck.ui.theme.DlsScales
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class TimeGranularity(
    val label: String,
    val slotHeightDp: Int,
    val defaultDurationMinutes: Int,
    val snapStepHours: Double
) {
    DAY("Day", 0, 24 * 60, 24.0),
    SIX_HOURS("6 hrs", 48, 6 * 60, 6.0),
    THREE_HOURS("3 hrs", 52, 3 * 60, 3.0),
    ONE_HOUR("1 hr", 56, 60, 1.0),
    THIRTY_MINS("30 mins", 84, 30, 0.5),
    TEN_MINS("10 mins", 136, 10, 10.0 / 60.0),
    ONE_MIN("1 min", 210, 1, 1.0 / 60.0)
}

data class SchedulePlaceholder(
    val dayMillis: Long,
    val hour: Int?,
    val minute: Int,
    val durationMinutes: Int,
    val status: ContextStatus,
    val colorTag: BarColorTag
) {
    fun timeRangeString(): String {
        return if (hour != null) {
            val startMin = minute
            val totalEndMin = hour * 60 + startMin + durationMinutes
            val endH = (totalEndMin / 60) % 24
            val endM = totalEndMin % 60
            String.format("%02d:%02d – %02d:%02d", hour, startMin, endH, endM)
        } else {
            "All Day"
        }
    }

    fun durationBadge(): String {
        return if (durationMinutes < 60) {
            "${durationMinutes}m"
        } else if (durationMinutes % 60 == 0) {
            "${durationMinutes / 60}h"
        } else {
            String.format("%.1fh", durationMinutes.toDouble() / 60.0)
        }
    }

    fun startMillis(): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dayMillis
            if (hour != null) {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            } else {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }
        return cal.timeInMillis
    }

    fun endMillis(): Long {
        return if (hour != null) {
            startMillis() + (durationMinutes * 60_000L)
        } else {
            val cal = Calendar.getInstance().apply {
                timeInMillis = dayMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1)
            }
            cal.timeInMillis
        }
    }
}

data class DragCreationState(
    val dayMillis: Long,
    val startHour: Double,
    val endHour: Double
)

private fun itemStartHourDouble(item: ContextItem, dayMillis: Long): Double {
    if (item.isContinuation(dayMillis)) return 0.0
    val cal = Calendar.getInstance().apply { timeInMillis = item.startAt }
    return cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0
}

private fun itemEndHourDouble(item: ContextItem, dayMillis: Long, defaultMinutes: Int): Double {
    if (item.isOutgoing(dayMillis)) return 24.0
    if (item.endAt != null) {
        val cal = Calendar.getInstance().apply { timeInMillis = item.endAt }
        return cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0
    }
    return itemStartHourDouble(item, dayMillis) + defaultMinutes / 60.0
}

private fun isHourCovered(hour: Int, items: List<ContextItem>, dayMillis: Long, defaultMinutes: Int): Boolean {
    items.forEach { item ->
        val start = itemStartHourDouble(item, dayMillis)
        val end = itemEndHourDouble(item, dayMillis, defaultMinutes)
        if (hour >= Math.floor(start) && hour < Math.ceil(end)) {
            return true
        }
    }
    return false
}

@Composable
fun BoardScreen(
    repository: ContextRepository,
    onOpenProfile: () -> Unit,
    onNewContext: (ContextStatus, Long, Long?) -> Unit,
    onEditContext: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by repository.profile.collectAsState()
    val contexts by repository.contexts.collectAsState()
    val limit by repository.limit.collectAsState()

    var activeQuickActionItem by remember { mutableStateOf<ContextItem?>(null) }
    var activePlaceholder by remember { mutableStateOf<SchedulePlaceholder?>(null) }
    var selectedStageFilter by remember { mutableStateOf<ContextStatus?>(null) }
    var quickThoughtText by remember { mutableStateOf("") }

    val todayMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    var selectedDayMillis by remember { mutableStateOf(todayMillis) }

    val quickCaptureContext = remember(selectedStageFilter, selectedDayMillis) {
        ContextRepository.resolveQuickCaptureContext(selectedStageFilter, selectedDayMillis)
    }

    val quickCapturePlaceholder = remember(selectedStageFilter, selectedDayMillis, todayMillis) {
        if (selectedStageFilter != null) {
            "Capture into ${selectedStageFilter?.title} (use // for note)..."
        } else if (selectedDayMillis > todayMillis) {
            val f = SimpleDateFormat("EEE", Locale.getDefault())
            "Capture for ${f.format(Date(selectedDayMillis))} (use // for note)..."
        } else {
            "Quick capture thought (use // for note)..."
        }
    }

    // Pinch-to-zoom timeline state (Day -> 6h -> 3h -> 1h -> 30m -> 10m -> 1m)
    var granularityIndex by remember { mutableStateOf(3) } // default: 1 hr
    val currentGranularity = TimeGranularity.values()[granularityIndex.coerceIn(0, TimeGranularity.values().size - 1)]

    val coroutineScope = rememberCoroutineScope()
    val verticalListState = rememberLazyListState()

    val activeCount = contexts.count { it.status == ContextStatus.ACTIVE }
    val isOverLimit = activeCount > limit
    val plannedCount = contexts.count { it.status == ContextStatus.PLANNED }
    val parkedCount = contexts.count { it.status == ContextStatus.PAUSED }
    val closedCount = contexts.count { it.status == ContextStatus.DONE }

    val calendarDays = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, -2)
        val days = mutableListOf<Long>()
        repeat(8) {
            days.add(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        days
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ContextColors.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.clickable { onOpenProfile() }
                ) {
                    EmojiAvatar(text = profile?.initials ?: "U", size = 38.dp)
                    Column {
                        Text(
                            text = "Stuck",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ContextColors.InkPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isOverLimit) ContextColors.AlertInk else ContextColors.CalmInk)
                            )
                            Text(
                                text = "$activeCount of $limit in your head",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverLimit) ContextColors.AlertInk else ContextColors.InkSecondary
                            )
                        }
                    }
                }

                // Plus Add Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ContextColors.Charcoal)
                        .clickable {
                            val durMillis = currentGranularity.defaultDurationMinutes * 60_000L
                            onNewContext(
                                quickCaptureContext.status,
                                quickCaptureContext.scheduledDate,
                                quickCaptureContext.scheduledDate + durMillis
                            )
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Context",
                        tint = ContextColors.OnCharcoal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Legend Row with Zoom Stepper Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // Zoom Stepper Pill: [ − 1 hr + ] (Tap or Pinch)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(ContextColors.Card)
                        .border(1.dp, ContextColors.BorderSubtle, RoundedCornerShape(999.dp))
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "−",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (granularityIndex > 0) ContextColors.InkPrimary else ContextColors.InkMuted.copy(alpha = 0.4f),
                        modifier = Modifier
                            .clickable(enabled = granularityIndex > 0) {
                                granularityIndex -= 1
                            }
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = currentGranularity.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ContextColors.InkPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (granularityIndex < TimeGranularity.values().size - 1) ContextColors.InkPrimary else ContextColors.InkMuted.copy(alpha = 0.4f),
                        modifier = Modifier
                            .clickable(enabled = granularityIndex < TimeGranularity.values().size - 1) {
                                granularityIndex += 1
                            }
                            .padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Stage Legend Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        StageLegendPill(
                            title = "In head",
                            count = activeCount,
                            color = Color(0xFF4B8BF5),
                            isSelected = selectedStageFilter == ContextStatus.ACTIVE,
                            onClick = {
                                selectedStageFilter = if (selectedStageFilter == ContextStatus.ACTIVE) null else ContextStatus.ACTIVE
                            }
                        )
                    }
                    item {
                        StageLegendPill(
                            title = "Coming",
                            count = plannedCount,
                            color = Color(0xFFA765BA),
                            isSelected = selectedStageFilter == ContextStatus.PLANNED,
                            onClick = {
                                selectedStageFilter = if (selectedStageFilter == ContextStatus.PLANNED) null else ContextStatus.PLANNED
                            }
                        )
                    }
                    item {
                        StageLegendPill(
                            title = "Park",
                            count = parkedCount,
                            color = Color(0xFFF59E0B),
                            isSelected = selectedStageFilter == ContextStatus.PAUSED,
                            onClick = {
                                selectedStageFilter = if (selectedStageFilter == ContextStatus.PAUSED) null else ContextStatus.PAUSED
                            }
                        )
                    }
                    item {
                        StageLegendPill(
                            title = "Closed",
                            count = closedCount,
                            color = Color(0xFF7FB844),
                            isSelected = selectedStageFilter == ContextStatus.DONE,
                            onClick = {
                                selectedStageFilter = if (selectedStageFilter == ContextStatus.DONE) null else ContextStatus.DONE
                            }
                        )
                    }
                }
            }

            // Horizontal Day Quick-Jump Strip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ContextColors.SurfaceRaised.copy(alpha = 0.6f))
            ) {
                items(calendarDays) { dMillis ->
                    val dayCal = Calendar.getInstance().apply { timeInMillis = dMillis }
                    val todayCal = Calendar.getInstance()
                    val isToday = dayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                            dayCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
                    val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
                    val dayNumFormat = SimpleDateFormat("d", Locale.getDefault())

                    val isSelected = dMillis == selectedDayMillis
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isSelected) ContextColors.Charcoal else ContextColors.Card)
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else if (isToday) Color(0xFF29B6F6).copy(alpha = 0.5f) else ContextColors.BorderSubtle,
                                RoundedCornerShape(999.dp)
                            )
                            .clickable {
                                selectedDayMillis = dMillis
                                val idx = calendarDays.indexOf(dMillis)
                                if (idx >= 0) {
                                    coroutineScope.launch {
                                        verticalListState.animateScrollToItem(idx)
                                    }
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = dayNameFormat.format(Date(dMillis)).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) ContextColors.OnCharcoal else if (isToday) Color(0xFF0288D1) else ContextColors.InkSecondary
                        )
                        Text(
                            text = dayNumFormat.format(Date(dMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ContextColors.OnCharcoal else ContextColors.InkPrimary
                        )
                        if (isToday && !isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF29B6F6))
                            )
                        }
                    }
                }
            }

            // Unified Vertical Scrolling Schedule (Supports Pinch-to-Zoom)
            LazyColumn(
                state = verticalListState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            if (zoom > 1.2f) {
                                if (granularityIndex < TimeGranularity.values().size - 1) {
                                    granularityIndex += 1
                                }
                            } else if (zoom < 0.8f) {
                                if (granularityIndex > 0) {
                                    granularityIndex -= 1
                                }
                            }
                        }
                    }
            ) {
                items(calendarDays) { dayMillis ->
                    VerticalDayCard(
                        dayMillis = dayMillis,
                        contexts = contexts,
                        granularity = currentGranularity,
                        selectedStageFilter = selectedStageFilter,
                        activePlaceholder = activePlaceholder,
                        onSetPlaceholder = { activePlaceholder = it },
                        onClearPlaceholder = { activePlaceholder = null },
                        onHoldCreateThought = { status, startAt, endAt ->
                            activePlaceholder = null
                            onNewContext(status, startAt, endAt)
                        },
                        onRescheduleThought = { id, newStartAt, newEndAt ->
                            repository.rescheduleTime(id, newStartAt, newEndAt)
                        },
                        onResizeThought = { id, newEndAt ->
                            repository.expandDuration(id, newEndAt)
                        },
                        onToggleDone = { id -> repository.toggleDone(id) },
                        onSelectContext = { item -> activeQuickActionItem = item }
                    )
                }
            }

            // Bottom Quick Capture Bar (Eliminates bottom dead space & respects stage + time)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ContextColors.SurfaceRaised)
                    .border(1.dp, ContextColors.BorderSubtle)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ContextColors.Card)
                        .border(1.dp, ContextColors.BorderSubtle, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    // Small stage badge pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(ContextColors.SurfaceRaised)
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(stageColor(quickCaptureContext.status))
                        )
                        Text(
                            text = quickCaptureContext.status.title,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ContextColors.InkSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    BasicTextField(
                        value = quickThoughtText,
                        onValueChange = { quickThoughtText = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = ContextColors.InkPrimary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (quickThoughtText.isEmpty()) {
                                Text(
                                    text = quickCapturePlaceholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ContextColors.InkMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Button(
                    onClick = {
                        val trimmed = quickThoughtText.trim()
                        if (trimmed.isNotBlank()) {
                            repository.quickCapture(
                                title = trimmed,
                                targetDate = selectedDayMillis,
                                stageFilter = selectedStageFilter
                            )
                            quickThoughtText = ""
                        }
                    },
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ContextColors.Charcoal),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text("Add", color = ContextColors.OnCharcoal, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 100% Custom DLS Action Popup Overlay
        activeQuickActionItem?.let { item ->
            // Dimmed Scrim Backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { activeQuickActionItem = null }
            )

            // DLS Sheet Container
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(ContextColors.Card)
                    .border(
                        1.dp,
                        ContextColors.BorderSubtle,
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(20.dp)
            ) {
                // Drag Pill Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 38.dp, height = 4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ContextColors.BorderStrong)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Header Row
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Stage Chip (Color represents stage)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(stageColor(item.status).copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(stageColor(item.status))
                            )
                            Text(
                                text = item.status.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = ContextColors.InkPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = ContextColors.InkPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ContextColors.SurfaceRaised)
                            .clickable { activeQuickActionItem = null }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ContextColors.InkSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                var quickNoteInput by remember(item.id) { mutableStateOf("") }
                var isEditingNote by remember(item.id) { mutableStateOf(false) }

                // Quick Note Section (Instant view & rapid inline capture)
                Spacer(modifier = Modifier.height(10.dp))
                if (item.notes.isNotBlank() && !isEditingNote) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ContextColors.SurfaceRaised)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "📝",
                            fontSize = 11.sp
                        )
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = ContextColors.InkSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Edit",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ContextColors.InkMuted,
                            modifier = Modifier.clickable {
                                quickNoteInput = item.notes
                                isEditingNote = true
                            }
                        )
                    }
                } else if (isEditingNote || item.notes.isBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ContextColors.Card)
                            .border(1.dp, ContextColors.BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        BasicTextField(
                            value = quickNoteInput,
                            onValueChange = { quickNoteInput = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = ContextColors.InkPrimary),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (quickNoteInput.isEmpty()) {
                                    Text(
                                        text = if (item.notes.isBlank()) "Quick add note..." else "Edit note...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ContextColors.InkMuted
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (quickNoteInput.isNotBlank()) {
                            Text(
                                text = "Save",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ContextColors.OnCharcoal,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(ContextColors.Charcoal)
                                    .clickable {
                                        repository.updateContext(id = item.id, notes = quickNoteInput.trim())
                                        isEditingNote = false
                                        quickNoteInput = ""
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stage Selector Row
                Text(
                    text = "COGNITIVE STAGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ContextColors.InkMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StageActionButton(
                        title = "In Head",
                        color = Color(0xFF4B8BF5),
                        isSelected = item.status == ContextStatus.ACTIVE,
                        modifier = Modifier.weight(1f)
                    ) {
                        repository.setStatus(item.id, ContextStatus.ACTIVE)
                        activeQuickActionItem = null
                    }
                    StageActionButton(
                        title = "Coming",
                        color = Color(0xFFA765BA),
                        isSelected = item.status == ContextStatus.PLANNED,
                        modifier = Modifier.weight(1f)
                    ) {
                        repository.setStatus(item.id, ContextStatus.PLANNED)
                        activeQuickActionItem = null
                    }
                    StageActionButton(
                        title = "Park",
                        color = Color(0xFFF59E0B),
                        isSelected = item.status == ContextStatus.PAUSED,
                        modifier = Modifier.weight(1f)
                    ) {
                        repository.toggleParked(item.id)
                        activeQuickActionItem = null
                    }
                    StageActionButton(
                        title = "Done ✓",
                        color = Color(0xFF7FB844),
                        isSelected = item.status == ContextStatus.DONE,
                        modifier = Modifier.weight(1f)
                    ) {
                        repository.toggleDone(item.id)
                        activeQuickActionItem = null
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reschedule Row
                Text(
                    text = "RESCHEDULE TIMELINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ContextColors.InkMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    val yesterday = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    val today = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    val tomorrow = cal.timeInMillis

                    ReschedulePill(title = "‹ Yesterday", modifier = Modifier.weight(1f)) {
                        repository.moveToDay(item.id, yesterday)
                        activeQuickActionItem = null
                    }
                    ReschedulePill(title = "Today", modifier = Modifier.weight(1f)) {
                        repository.moveToDay(item.id, today)
                        activeQuickActionItem = null
                    }
                    ReschedulePill(title = "Tomorrow ›", modifier = Modifier.weight(1f)) {
                        repository.moveToDay(item.id, tomorrow)
                        activeQuickActionItem = null
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ContextColors.BorderSubtle)
                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Button(
                    onClick = {
                        repository.focusOn(item.id)
                        activeQuickActionItem = null
                    },
                    shape = RoundedCornerShape(DlsScales.radiusMd.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ContextColors.Charcoal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Focus",
                            tint = ContextColors.OnCharcoal,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Start Focus Timer", color = ContextColors.OnCharcoal, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val id = item.id
                            activeQuickActionItem = null
                            onEditContext(id)
                        },
                        shape = RoundedCornerShape(DlsScales.radiusMd.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ContextColors.SurfaceRaised),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = ContextColors.InkPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text("Edit Details", color = ContextColors.InkPrimary)
                        }
                    }

                    Button(
                        onClick = {
                            repository.removeContext(item.id)
                            activeQuickActionItem = null
                        },
                        shape = RoundedCornerShape(DlsScales.radiusMd.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ContextColors.AlertSoft),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Delete", color = ContextColors.AlertInk, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// MARK: - Vertical Day Card

@Composable
private fun VerticalDayCard(
    dayMillis: Long,
    contexts: List<ContextItem>,
    granularity: TimeGranularity,
    selectedStageFilter: ContextStatus?,
    activePlaceholder: SchedulePlaceholder?,
    onSetPlaceholder: (SchedulePlaceholder) -> Unit,
    onClearPlaceholder: () -> Unit,
    onHoldCreateThought: (ContextStatus, Long, Long?) -> Unit,
    onRescheduleThought: (String, Long, Long?) -> Unit = { _, _, _ -> },
    onResizeThought: (String, Long) -> Unit = { _, _ -> },
    onToggleDone: (String) -> Unit,
    onSelectContext: (ContextItem) -> Unit
) {
    val dayCal = Calendar.getInstance().apply { timeInMillis = dayMillis }
    val todayCal = Calendar.getInstance()
    val isToday = dayCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
            dayCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)

    var dayItems = contexts.filter { c -> c.intersects(dayMillis) }

    if (selectedStageFilter != null) {
        dayItems = dayItems.filter { it.status == selectedStageFilter }
    }

    val banners = dayItems.filter { it.isBanner }
    val regulars = dayItems.filter { !it.isBanner }

    val fullDateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ContextColors.Card)
            .border(
                1.dp,
                if (isToday) Color(0xFF29B6F6).copy(alpha = 0.45f) else ContextColors.BorderSubtle,
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {
        // Day Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = fullDateFormat.format(Date(dayMillis)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ContextColors.InkPrimary
                )

                if (isToday) {
                    Text(
                        text = "TODAY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFF29B6F6).copy(alpha = 0.18f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "${dayItems.size} thoughts",
                style = MaterialTheme.typography.labelSmall,
                color = ContextColors.InkMuted
            )
        }

        // Spanning Banners
        banners.forEach { banner ->
            BannerBar(item = banner, onClick = { onSelectContext(banner) })
        }

        // Schedule Body: Day mode (compact stack) vs Hourly/Minute mode (expanded grid)
        if (granularity == TimeGranularity.DAY) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                regulars.forEach { item ->
                    DlsStackedBar(
                        item = item,
                        dayMillis = dayMillis,
                        slotHeightDp = 40f,
                        granularity = granularity,
                        onIconClick = { onToggleDone(item.id) },
                        onBodyClick = { onSelectContext(item) },
                        onReschedule = { newStartAt -> onRescheduleThought(item.id, newStartAt, null) },
                        onResizeDuration = { newEndAt -> onResizeThought(item.id, newEndAt) },
                        onRescheduleTime = { newStartAt, newEndAt -> onRescheduleThought(item.id, newStartAt, newEndAt) }
                    )
                }

                val isPlaceholderForThisDay = activePlaceholder?.hour == null &&
                        activePlaceholder?.let {
                            val pCal = Calendar.getInstance().apply { timeInMillis = it.dayMillis }
                            pCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                     pCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
                        } == true

                if (isPlaceholderForThisDay && activePlaceholder != null) {
                    PlaceholderBar(
                        placeholder = activePlaceholder,
                        granularity = granularity,
                        onConfirm = {
                            onHoldCreateThought(
                                activePlaceholder.status,
                                activePlaceholder.startMillis(),
                                activePlaceholder.endMillis()
                            )
                        },
                        onDismiss = { onClearPlaceholder() }
                    )
                } else if (dayItems.isEmpty()) {
                    val isFuture = dayMillis > todayCal.timeInMillis
                    val defaultStatus = selectedStageFilter ?: if (isFuture) ContextStatus.PLANNED else ContextStatus.ACTIVE
                    val defaultColor = when (defaultStatus) {
                        ContextStatus.ACTIVE -> BarColorTag.BLUE
                        ContextStatus.PLANNED -> BarColorTag.PURPLE
                        ContextStatus.PAUSED -> BarColorTag.CHARCOAL
                        ContextStatus.DONE -> BarColorTag.GREEN
                        ContextStatus.DROPPED -> BarColorTag.CHARCOAL
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ContextColors.SurfaceRaised.copy(alpha = 0.5f))
                            .border(1.dp, ContextColors.BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable {
                                onSetPlaceholder(
                                    SchedulePlaceholder(
                                        dayMillis = dayMillis,
                                        hour = null,
                                        minute = 0,
                                        durationMinutes = 24 * 60,
                                        status = defaultStatus,
                                        colorTag = defaultColor
                                    )
                                )
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = ContextColors.InkMuted.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Add thought (Tap for placeholder)",
                                style = MaterialTheme.typography.labelSmall,
                                color = ContextColors.InkMuted.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        } else {
            // Expanded Hourly / Minute Grid
            val hours = when (granularity) {
                TimeGranularity.DAY -> emptyList()
                TimeGranularity.SIX_HOURS -> listOf(0, 6, 12, 18)
                TimeGranularity.THREE_HOURS -> listOf(0, 3, 6, 9, 12, 15, 18, 21)
                else -> {
                    var minHour = 7
                    var maxHour = 22
                    regulars.forEach { item ->
                        if (item.isContinuation(dayMillis)) {
                            minHour = 0
                        } else {
                            val itemCal = Calendar.getInstance().apply { timeInMillis = item.startAt }
                            val h = itemCal.get(Calendar.HOUR_OF_DAY)
                            if (h < minHour) minHour = maxOf(0, h)
                            if (h > maxHour) maxHour = minOf(23, h)
                        }
                        if (item.endAt != null) {
                            if (item.isOutgoing(dayMillis)) {
                                maxHour = 23
                            } else {
                                val endCal = Calendar.getInstance().apply { timeInMillis = item.endAt }
                                val eh = endCal.get(Calendar.HOUR_OF_DAY)
                                if (eh > maxHour) maxHour = minOf(23, eh)
                            }
                        }
                    }
                    (minHour..maxHour).toList()
                }
            }

            var activeDragCreation by remember { mutableStateOf<DragCreationState?>(null) }
            val slotHeight = granularity.slotHeightDp.toFloat()
            val minHour = hours.firstOrNull() ?: 7
            val totalHeightDp = (hours.size * slotHeight).dp

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Fixed Time Ruler Column on Left: Never displaced, crossing time accurately
                Column(
                    modifier = Modifier.width(44.dp)
                ) {
                    hours.forEach { hour ->
                        Box(
                            contentAlignment = Alignment.TopEnd,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(slotHeight.dp)
                        ) {
                            Text(
                                text = String.format("%02d:00", hour),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = ContextColors.InkMuted,
                                modifier = Modifier.offset(y = (-7).dp)
                            )
                        }
                    }
                }

                // Right Continuous Schedule Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(totalHeightDp)
                ) {
                    // Layer 1: Hour Grid Slots & Dividers (Fixed slot heights, never displaced)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(totalHeightDp)
                    ) {
                        hours.forEach { hour ->
                            val isOccupied = isHourCovered(hour, regulars, dayMillis, granularity.defaultDurationMinutes)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(slotHeight.dp)
                            ) {
                                // Hour divider line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(ContextColors.BorderSubtle.copy(alpha = 0.35f))
                                )

                                // Subdivisions for 30 mins, 10 mins, 1 min
                                if (granularity == TimeGranularity.THIRTY_MINS) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        Text(text = ":30", fontSize = 9.sp, color = ContextColors.InkMuted.copy(alpha = 0.5f))
                                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ContextColors.BorderSubtle.copy(alpha = 0.35f)))
                                    }
                                } else if (granularity == TimeGranularity.TEN_MINS) {
                                    listOf(10, 20, 30, 40, 50).forEach { m ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(text = ":$m", fontSize = 8.5.sp, color = ContextColors.InkMuted.copy(alpha = 0.4f))
                                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ContextColors.BorderSubtle.copy(alpha = 0.25f)))
                                        }
                                    }
                                } else if (granularity == TimeGranularity.ONE_MIN) {
                                    listOf(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55).forEach { m ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 1.dp)
                                        ) {
                                            Text(text = String.format(":%02d", m), fontSize = 8.sp, color = ContextColors.InkMuted.copy(alpha = 0.35f))
                                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(ContextColors.BorderSubtle.copy(alpha = 0.2f)))
                                        }
                                    }
                                }

                                val isPlaceholderForThisHour = activePlaceholder?.hour == hour &&
                                        activePlaceholder?.let {
                                            val pCal = Calendar.getInstance().apply { timeInMillis = it.dayMillis }
                                            pCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                                    pCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
                                        } == true

                                if (isPlaceholderForThisHour && activePlaceholder != null) {
                                    PlaceholderBar(
                                        placeholder = activePlaceholder,
                                        granularity = granularity,
                                        onConfirm = {
                                            onHoldCreateThought(
                                                activePlaceholder.status,
                                                activePlaceholder.startMillis(),
                                                activePlaceholder.endMillis()
                                            )
                                        },
                                        onDismiss = { onClearPlaceholder() }
                                    )
                                } else if (!isOccupied) {
                                    val isFuture = dayMillis > todayCal.timeInMillis
                                    val targetStatus = selectedStageFilter ?: if (isFuture) ContextStatus.PLANNED else ContextStatus.ACTIVE
                                    val targetColor = when (targetStatus) {
                                        ContextStatus.ACTIVE -> BarColorTag.BLUE
                                        ContextStatus.PLANNED -> BarColorTag.PURPLE
                                        ContextStatus.PAUSED -> BarColorTag.CHARCOAL
                                        ContextStatus.DONE -> BarColorTag.GREEN
                                        ContextStatus.DROPPED -> BarColorTag.CHARCOAL
                                    }

                                    TimelineEmptySlot(
                                        dayMillis = dayMillis,
                                        hour = hour,
                                        granularity = granularity,
                                        slotHeightDp = slotHeight,
                                        targetStatus = targetStatus,
                                        targetColor = targetColor,
                                        onQuickTap = {
                                            onSetPlaceholder(
                                                SchedulePlaceholder(
                                                    dayMillis = dayMillis,
                                                    hour = hour,
                                                    minute = 0,
                                                    durationMinutes = granularity.defaultDurationMinutes,
                                                    status = targetStatus,
                                                    colorTag = targetColor
                                                )
                                            )
                                        },
                                        onDragCreationChanged = { state ->
                                            activeDragCreation = state
                                        },
                                        onDragCreationEnded = { startAt, endAt ->
                                            activeDragCreation = null
                                            onHoldCreateThought(targetStatus, startAt, endAt)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Layer 2: Live Time Cursor (Today)
                    if (isToday) {
                        val nowH = todayCal.get(Calendar.HOUR_OF_DAY) + todayCal.get(Calendar.MINUTE) / 60.0
                        if (nowH >= minHour.toDouble() && nowH <= (minHour + hours.size).toDouble()) {
                            val cursorY = ((nowH - minHour.toDouble()) * slotHeight).toFloat()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .offset(y = (cursorY - 4).dp)
                                    .fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF29B6F6)))
                                Box(modifier = Modifier.weight(1f).height(1.5.dp).background(Color(0xFF29B6F6)))
                            }
                        }
                    }

                    // Layer 3: Placed Items (DlsStackedBar physically spanning duration across hours)
                    regulars.forEach { item ->
                        val startH = itemStartHourDouble(item, dayMillis)
                        val endH = itemEndHourDouble(item, dayMillis, granularity.defaultDurationMinutes)
                        val topOffsetDp = ((maxOf(0.0, startH - minHour.toDouble())) * slotHeight).toFloat()
                        val durHours = maxOf(0.25, endH - startH)
                        val cardHeightDp = maxOf(38f, (durHours * slotHeight - 4).toFloat())

                        DlsStackedBar(
                            item = item,
                            dayMillis = dayMillis,
                            slotHeightDp = slotHeight,
                            cardHeightDp = cardHeightDp,
                            granularity = granularity,
                            onIconClick = { onToggleDone(item.id) },
                            onBodyClick = { onSelectContext(item) },
                            onReschedule = { newStartAt -> onRescheduleThought(item.id, newStartAt, null) },
                            onResizeDuration = { newEndAt -> onResizeThought(item.id, newEndAt) },
                            onRescheduleTime = { newStartAt, newEndAt -> onRescheduleThought(item.id, newStartAt, newEndAt) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = topOffsetDp.dp)
                        )
                    }

                    // Layer 4: Drag-to-Create Box Overlay (Physically crosses all covered hours)
                    activeDragCreation?.let { drag ->
                        if (drag.dayMillis == dayMillis) {
                            val topOffsetDp = ((maxOf(0.0, drag.startHour - minHour.toDouble())) * slotHeight).toFloat()
                            val boxHeightDp = maxOf(24f, ((drag.endHour - drag.startHour) * slotHeight).toFloat())

                            DragCreateBox(
                                startHour = drag.startHour,
                                endHour = drag.endHour,
                                slotHeightDp = slotHeight,
                                stageColor = Color(0xFF29B6F6),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = topOffsetDp.dp)
                                    .height(boxHeightDp.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Supporting Components

@Composable
private fun StageLegendPill(
    title: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (isSelected) ContextColors.Charcoal else ContextColors.Card)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else ContextColors.BorderSubtle,
                shape = RoundedCornerShape(999.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) ContextColors.OnCharcoal else ContextColors.InkPrimary
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) ContextColors.OnCharcoal.copy(alpha = 0.8f) else ContextColors.InkMuted,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (isSelected) Color.White.copy(alpha = 0.2f) else ContextColors.SurfaceRaised)
                .padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}

@Composable
fun TimelineHandleKnob(
    sizeDp: Int = 24,
    pipDp: Int = 10,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(ContextColors.Charcoal.copy(alpha = 0.88f))
            .border(1.5.dp, Color(0xFF29B6F6).copy(alpha = 0.75f), CircleShape)
    ) {
        Box(
            modifier = Modifier
                .size(pipDp.dp)
                .clip(CircleShape)
                .background(Color(0xFF29B6F6))
        )
    }
}

@Composable
fun DlsStackedBar(
    item: ContextItem,
    dayMillis: Long = 0L,
    slotHeightDp: Float = 56f,
    cardHeightDp: Float = 38f,
    granularity: TimeGranularity = TimeGranularity.ONE_HOUR,
    onIconClick: () -> Unit,
    onBodyClick: () -> Unit,
    onReschedule: ((Long) -> Unit)? = null,
    onResizeDuration: ((Long) -> Unit)? = null,
    onRescheduleTime: ((Long, Long?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bg = stageColor(item.status)
    val textCol = stageTextColor(item.status)
    val density = LocalDensity.current

    var isHeld by remember { mutableStateOf(false) }
    var isDraggingMove by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var isResizingEnd by remember { mutableStateOf(false) }
    var resizeOffsetY by remember { mutableStateOf(0f) }
    var isResizingStart by remember { mutableStateOf(false) }
    var resizeStartOffsetY by remember { mutableStateOf(0f) }
    val isResizing = isResizingEnd || isResizingStart
    val isInteracting = isHeld || isDraggingMove || isResizing

    val effectiveSlotHeight = slotHeightDp.coerceAtLeast(36f)
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val durationText = remember(item.startAt, item.endAt) {
        if (item.endAt != null) {
            "${timeFormat.format(Date(item.startAt))} – ${timeFormat.format(Date(item.endAt))}"
        } else null
    }

    val isContinuation = remember(item, dayMillis) {
        dayMillis > 0L && item.isContinuation(dayMillis)
    }
    val isOutgoing = remember(item, dayMillis) {
        dayMillis > 0L && item.isOutgoing(dayMillis)
    }

    val targetTimePreview = remember(dragOffsetY, item.startAt) {
        val slotHeightPx = with(density) { effectiveSlotHeight.dp.toPx() }
        val deltaHours = (dragOffsetY / slotHeightPx).roundToInt()
        val targetDate = Calendar.getInstance().apply {
            timeInMillis = item.startAt
            add(Calendar.HOUR_OF_DAY, deltaHours)
        }
        val origCal = Calendar.getInstance().apply { timeInMillis = item.startAt }
        val isSameDay = targetDate.get(Calendar.YEAR) == origCal.get(Calendar.YEAR) &&
                targetDate.get(Calendar.DAY_OF_YEAR) == origCal.get(Calendar.DAY_OF_YEAR)
        if (isSameDay) {
            timeFormat.format(targetDate.time)
        } else if (targetDate.timeInMillis > origCal.timeInMillis) {
            "Tomorrow ${timeFormat.format(targetDate.time)}"
        } else {
            "Yesterday ${timeFormat.format(targetDate.time)}"
        }
    }

    val resizePreview = remember(resizeOffsetY, item.startAt, item.endAt, effectiveSlotHeight) {
        val slotHeightPx = with(density) { effectiveSlotHeight.dp.toPx() }
        val rawHours = ((resizeOffsetY / slotHeightPx) * 2.0).roundToInt() / 2.0
        val currentDur = if (item.endAt != null) (item.endAt - item.startAt) else (granularity.defaultDurationMinutes * 60_000L)
        val addedMillis = (rawHours * 3_600_000L).toLong()
        val totalMillis = maxOf(900_000L, currentDur + addedMillis)
        val newEnd = item.startAt + totalMillis

        val totalMinutes = (totalMillis / 60_000L).toInt()
        val durStr = if (totalMinutes < 60) "${totalMinutes}m" else if (totalMinutes % 60 == 0) "${totalMinutes / 60}h" else String.format("%.1fh", totalMinutes / 60.0)

        val startCal = Calendar.getInstance().apply { timeInMillis = item.startAt }
        val endCal = Calendar.getInstance().apply { timeInMillis = newEnd }
        val spans = startCal.get(Calendar.YEAR) != endCal.get(Calendar.YEAR) ||
                startCal.get(Calendar.DAY_OF_YEAR) != endCal.get(Calendar.DAY_OF_YEAR)
        val endStr = if (spans) "Tomorrow ${timeFormat.format(Date(newEnd))}" else timeFormat.format(Date(newEnd))
        Triple(durStr, endStr, spans)
    }

    val resizeStartPreview = remember(resizeStartOffsetY, item.startAt, item.endAt, effectiveSlotHeight) {
        val slotHeightPx = with(density) { effectiveSlotHeight.dp.toPx() }
        val rawHours = ((resizeStartOffsetY / slotHeightPx) * 2.0).roundToInt() / 2.0
        val endMillis = item.endAt ?: (item.startAt + granularity.defaultDurationMinutes * 60_000L)
        val addedMillis = (rawHours * 3_600_000L).toLong()
        val proposedStart = item.startAt + addedMillis
        val finalStart = minOf(proposedStart, endMillis - 900_000L)
        val totalMillis = endMillis - finalStart
        val totalMinutes = (totalMillis / 60_000L).toInt()
        val durStr = if (totalMinutes < 60) "${totalMinutes}m" else if (totalMinutes % 60 == 0) "${totalMinutes / 60}h" else String.format("%.1fh", totalMinutes / 60.0)
        Triple(durStr, timeFormat.format(Date(finalStart)), timeFormat.format(Date(endMillis)))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(0, dragOffsetY.roundToInt()) }
            .graphicsLayer {
                if (isDraggingMove) {
                    scaleX = 1.03f
                    scaleY = 1.03f
                    shadowElevation = 12.dp.toPx()
                }
            }
            .pointerInput(item.id, effectiveSlotHeight) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        isDraggingMove = true
                        dragOffsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY += dragAmount.y
                    },
                    onDragEnd = {
                        if (isDraggingMove) {
                            val slotHeightPx = with(density) { effectiveSlotHeight.dp.toPx() }
                            val deltaHours = (dragOffsetY / slotHeightPx).roundToInt()
                            if (deltaHours != 0 && onReschedule != null) {
                                val targetDate = Calendar.getInstance().apply {
                                    timeInMillis = item.startAt
                                    add(Calendar.HOUR_OF_DAY, deltaHours)
                                }
                                onReschedule(targetDate.timeInMillis)
                            }
                            isDraggingMove = false
                            dragOffsetY = 0f
                            isHeld = false
                        }
                    },
                    onDragCancel = {
                        isDraggingMove = false
                        dragOffsetY = 0f
                    }
                )
            }
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(maxOf(38f, cardHeightDp).dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bg)
                .border(
                    width = if (isInteracting) 1.5.dp else 0.dp,
                    color = if (isInteracting) Color(0xFF29B6F6) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .pointerInput(item.id) {
                    detectTapGestures(
                        onTap = {
                            if (isHeld) {
                                isHeld = false
                            } else if (!isDraggingMove && !isResizing) {
                                onBodyClick()
                            }
                        },
                        onLongPress = {
                            isHeld = !isHeld
                        }
                    )
                }
                .padding(horizontal = 8.dp, vertical = if (cardHeightDp > 38f) 6.dp else 0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                // Leading Icon Tap Target
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onIconClick() }
                ) {
                    if (item.status == ContextStatus.DONE) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Done",
                            tint = textCol,
                            modifier = Modifier.size(14.dp)
                        )
                    } else if (item.status == ContextStatus.PAUSED) {
                        Text(
                            text = "⏸",
                            fontSize = 11.sp,
                            color = textCol,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .border(1.5.dp, textCol.copy(alpha = 0.8f), CircleShape)
                        )
                    }
                }

                // Continuation Badge
                if (isContinuation) {
                    Text(
                        text = "↩ Cont.",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFF29B6F6))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textCol,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.status == ContextStatus.DONE) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )

                // Outgoing Badge
                if (isOutgoing) {
                    Text(
                        text = "➜ +1d",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFF29B6F6))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                // Duration Time Badge if endAt is set
                if (durationText != null) {
                    Text(
                        text = durationText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = textCol.copy(alpha = 0.85f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.Black.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Dedicated Move Grip (Zero-delay instant drag)
                if (item.status != ContextStatus.DONE) {
                    Text(
                        text = "⋮⋮",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textCol.copy(alpha = 0.5f),
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .pointerInput(item.id, effectiveSlotHeight) {
                                detectDragGestures(
                                    onDragStart = { _: androidx.compose.ui.geometry.Offset ->
                                        isDraggingMove = true
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: androidx.compose.ui.geometry.Offset ->
                                        change.consume()
                                        dragOffsetY += dragAmount.y
                                    },
                                    onDragEnd = {
                                        if (isDraggingMove) {
                                            val slotHeightPx = with(density) { effectiveSlotHeight.dp.toPx() }
                                            val deltaHours = (dragOffsetY / slotHeightPx).roundToInt()
                                            if (deltaHours != 0 && onReschedule != null) {
                                                val targetDate = Calendar.getInstance().apply {
                                                    timeInMillis = item.startAt
                                                    add(Calendar.HOUR_OF_DAY, deltaHours)
                                                }
                                                onReschedule(targetDate.timeInMillis)
                                            }
                                            isDraggingMove = false
                                            dragOffsetY = 0f
                                            isHeld = false
                                        }
                                    },
                                    onDragCancel = {
                                        isDraggingMove = false
                                        dragOffsetY = 0f
                                    }
                                )
                            }
                    )
                }
            }

            if (cardHeightDp > 56f && item.notes.isNotBlank()) {
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = textCol.copy(alpha = 0.75f),
                    maxLines = maxOf(1, ((cardHeightDp - 44f) / 16f).toInt()),
                    modifier = Modifier.padding(start = 22.dp)
                )
            }
        }

        // Top-Start Knob (Drag to adjust start time earlier/later - ONLY when held / interacting!)
        if (isInteracting && item.status != ContextStatus.DONE) {
            TimelineHandleKnob(
                sizeDp = 20,
                pipDp = 8,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-8).dp, y = (-8).dp)
                    .pointerInput(item.id, effectiveSlotHeight) {
                        detectDragGestures(
                            onDragStart = { _: androidx.compose.ui.geometry.Offset ->
                                isResizingStart = true
                                resizeStartOffsetY = 0f
                            },
                            onDrag = { change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: androidx.compose.ui.geometry.Offset ->
                                change.consume()
                                resizeStartOffsetY += dragAmount.y
                            },
                            onDragEnd = {
                                if (isResizingStart) {
                                    val slotHeightPx = with(density) { effectiveSlotHeight.dp.toPx() }
                                    val rawHours = ((resizeStartOffsetY / slotHeightPx) * 2.0).roundToInt() / 2.0
                                    val endMillis = item.endAt ?: (item.startAt + granularity.defaultDurationMinutes * 60_000L)
                                    val addedMillis = (rawHours * 3_600_000L).toLong()
                                    val proposedStart = item.startAt + addedMillis
                                    val finalStart = minOf(proposedStart, endMillis - 900_000L)
                                    onRescheduleTime?.invoke(finalStart, endMillis)
                                    isResizingStart = false
                                    resizeStartOffsetY = 0f
                                    isHeld = false
                                }
                            },
                            onDragCancel = {
                                isResizingStart = false
                                resizeStartOffsetY = 0f
                            }
                        )
                    }
            )
        }

        // Expand duration knob on bottom edge (ONLY when held / interacting!)
        if (isInteracting && item.status != ContextStatus.DONE) {
            TimelineHandleKnob(
                sizeDp = 20,
                pipDp = 8,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .pointerInput(item.id, effectiveSlotHeight) {
                        detectDragGestures(
                            onDragStart = { _: androidx.compose.ui.geometry.Offset ->
                                isResizingEnd = true
                                resizeOffsetY = 0f
                            },
                            onDrag = { change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: androidx.compose.ui.geometry.Offset ->
                                change.consume()
                                resizeOffsetY = maxOf(0f, resizeOffsetY + dragAmount.y)
                            },
                            onDragEnd = {
                                if (isResizingEnd) {
                                    val slotHeightPx = with(density) { effectiveSlotHeight.dp.toPx() }
                                    val rawHours = ((resizeOffsetY / slotHeightPx) * 2.0).roundToInt() / 2.0
                                    val currentDur = if (item.endAt != null) (item.endAt - item.startAt) else (granularity.defaultDurationMinutes * 60_000L)
                                    val addedMillis = (rawHours * 3_600_000L).toLong()
                                    val newEnd = item.startAt + maxOf(900_000L, currentDur + addedMillis)
                                    onResizeDuration?.invoke(newEnd)
                                    isResizingEnd = false
                                    resizeOffsetY = 0f
                                    isHeld = false
                                }
                            },
                            onDragCancel = {
                                isResizingEnd = false
                                resizeOffsetY = 0f
                            }
                        )
                    }
            )
        }

        // Floating "Move to HH:mm" pill when dragging
        if (isDraggingMove) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-26).dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ContextColors.Charcoal)
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Move to $targetTimePreview",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = ContextColors.OnCharcoal
                )
            }
        }

        // Expanding Cyan Outline Box (End Adjustment - Matches User Screenshot)
        if (isResizingEnd) {
            val totalResizeHeight = 38.dp + with(density) { resizeOffsetY.toDp() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalResizeHeight)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF29B6F6).copy(alpha = 0.08f))
                    .border(2.dp, Color(0xFF29B6F6), RoundedCornerShape(6.dp))
            ) {
                TimelineHandleKnob(
                    sizeDp = 24,
                    pipDp = 10,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-12).dp, y = (-12).dp)
                )
                TimelineHandleKnob(
                    sizeDp = 24,
                    pipDp = 10,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 12.dp, y = 12.dp)
                )
                // Readout HUD
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 28.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ContextColors.Charcoal)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Duration: ${resizePreview.first} • Ends ${resizePreview.second}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = ContextColors.OnCharcoal
                    )
                    if (resizePreview.third) {
                        Text(
                            text = "2 DAYS",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = ContextColors.Charcoal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFF29B6F6))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }

        // Expanding Cyan Outline Box (Start Adjustment - Matches User Screenshot)
        if (isResizingStart) {
            val totalStartHeight = maxOf(38.dp, 38.dp - with(density) { resizeStartOffsetY.toDp() })
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(totalStartHeight)
                    .offset { IntOffset(0, resizeStartOffsetY.roundToInt()) }
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF29B6F6).copy(alpha = 0.08f))
                    .border(2.dp, Color(0xFF29B6F6), RoundedCornerShape(6.dp))
            ) {
                TimelineHandleKnob(
                    sizeDp = 24,
                    pipDp = 10,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-12).dp, y = (-12).dp)
                )
                TimelineHandleKnob(
                    sizeDp = 24,
                    pipDp = 10,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 12.dp, y = 12.dp)
                )
                // Readout HUD
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-28).dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ContextColors.Charcoal)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Start: ${resizeStartPreview.second} • Ends ${resizeStartPreview.third} (${resizeStartPreview.first})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = ContextColors.OnCharcoal
                    )
                }
            }
        }
    }
}

// MARK: - Draft Placeholder Bar (Tap to Confirm / Hold to Create)

@Composable
fun PlaceholderBar(
    placeholder: SchedulePlaceholder,
    granularity: TimeGranularity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF29B6F6).copy(alpha = 0.08f))
            .border(
                1.5.dp,
                Color(0xFF29B6F6).copy(alpha = 0.7f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onConfirm() }
            .padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .border(1.5.dp, Color(0xFF29B6F6), CircleShape)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Draft Placeholder",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ContextColors.InkPrimary
                )
                Text(
                    text = placeholder.timeRangeString(),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = Color(0xFF0288D1),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF29B6F6).copy(alpha = 0.18f))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
            Text(
                text = "Tap to confirm form • Hold to create (${granularity.label})",
                fontSize = 9.sp,
                color = ContextColors.InkSecondary
            )
        }

        Text(
            text = "Create",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = ContextColors.OnCharcoal,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(ContextColors.Charcoal)
                .clickable { onConfirm() }
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(ContextColors.SurfaceRaised)
                .clickable { onDismiss() }
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss",
                tint = ContextColors.InkMuted,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

// MARK: - Drag-to-Create Timeline Box (Matches BioCue Spec & Reference Mockup)

@Composable
fun DragCreateBox(
    startHour: Double,
    endHour: Double,
    slotHeightDp: Float,
    stageColor: Color = Color(0xFF29B6F6),
    modifier: Modifier = Modifier
) {
    val durationHours = (endHour - startHour).coerceAtLeast(0.016)
    val totalHeightDp = (durationHours * slotHeightDp).toFloat().coerceAtLeast(36f)
    val spansTwoDays = endHour >= 24.0

    val sh = startHour.toInt() % 24
    val sm = ((startHour % 1.0) * 60).roundToInt() % 60
    val eh = endHour.toInt() % 24
    val em = ((endHour % 1.0) * 60).roundToInt() % 60
    val timeRangeText = if (spansTwoDays) {
        String.format("%02d:%02d – Tomorrow %02d:%02d", sh, sm, eh, em)
    } else {
        String.format("%02d:%02d – %02d:%02d", sh, sm, eh, em)
    }

    val totalMinutes = (durationHours * 60).roundToInt()
    val durationText = if (totalMinutes < 60) {
        "${maxOf(1, totalMinutes)}m"
    } else if (totalMinutes % 60 == 0) {
        "${totalMinutes / 60}h"
    } else {
        String.format("%.1fh", durationHours)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeightDp.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF29B6F6).copy(alpha = 0.08f))
            .border(2.dp, stageColor, RoundedCornerShape(6.dp))
    ) {
        // Center Readout Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(999.dp))
                .background(ContextColors.Charcoal.copy(alpha = 0.92f))
                .padding(horizontal = 9.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(stageColor)
            )
            Text(
                text = "$timeRangeText ($durationText)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = ContextColors.OnCharcoal
            )
            if (spansTwoDays) {
                Text(
                    text = "2 DAYS",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    color = ContextColors.Charcoal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(stageColor)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        // Top-Left Circular Knob (Exactly centered at (0, 0) matching user screenshot)
        TimelineHandleKnob(
            sizeDp = 24,
            pipDp = 10,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-12).dp, y = (-12).dp)
        )

        // Bottom-Right Circular Knob (Exactly centered at (width, height) matching user screenshot)
        TimelineHandleKnob(
            sizeDp = 24,
            pipDp = 10,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 12.dp, y = 12.dp)
        )
    }
}

// MARK: - Timeline Empty Slot (Hold & Drag Down to Create)

@Composable
fun TimelineEmptySlot(
    dayMillis: Long,
    hour: Int,
    granularity: TimeGranularity,
    slotHeightDp: Float,
    targetStatus: ContextStatus,
    targetColor: BarColorTag,
    onQuickTap: () -> Unit,
    onDragCreationChanged: (DragCreationState?) -> Unit,
    onDragCreationEnded: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var startHourDouble by remember { mutableStateOf(hour.toDouble()) }
    var endHourDouble by remember {
        mutableStateOf(hour.toDouble() + (granularity.defaultDurationMinutes / 60.0))
    }
    val density = LocalDensity.current

    val effectiveSlotHeight = slotHeightDp.coerceAtLeast(36f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(dayMillis, hour) {
                detectTapGestures(
                    onTap = { onQuickTap() }
                )
            }
            .pointerInput(dayMillis, hour, effectiveSlotHeight, granularity) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        isDragging = true
                        startHourDouble = hour.toDouble()
                        val defHours = granularity.defaultDurationMinutes / 60.0
                        endHourDouble = hour.toDouble() + defHours
                        dragOffsetY = 0f
                        onDragCreationChanged(DragCreationState(dayMillis, startHourDouble, endHourDouble))
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY += dragAmount.y
                        val slotHeightPx = with(density) { effectiveSlotHeight.dp.toPx() }
                        val rawHours = (dragOffsetY / slotHeightPx).toDouble()
                        val step = granularity.snapStepHours
                        val snappedAdditional = (Math.round(rawHours / step) * step).coerceAtLeast(step)
                        endHourDouble = hour.toDouble() + snappedAdditional
                        onDragCreationChanged(DragCreationState(dayMillis, startHourDouble, endHourDouble))
                    },
                    onDragEnd = {
                        if (isDragging) {
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = dayMillis
                                set(Calendar.HOUR_OF_DAY, startHourDouble.toInt() % 24)
                                set(Calendar.MINUTE, ((startHourDouble % 1.0) * 60).roundToInt() % 60)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            val startMillis = cal.timeInMillis

                            val endCal = Calendar.getInstance().apply {
                                timeInMillis = dayMillis
                                val daysToAdd = (endHourDouble / 24.0).toInt()
                                if (daysToAdd > 0) {
                                    add(Calendar.DAY_OF_YEAR, daysToAdd)
                                }
                                set(Calendar.HOUR_OF_DAY, endHourDouble.toInt() % 24)
                                set(Calendar.MINUTE, ((endHourDouble % 1.0) * 60).roundToInt() % 60)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            val endMillis = endCal.timeInMillis

                            onDragCreationEnded(startMillis, endMillis)
                            isDragging = false
                            dragOffsetY = 0f
                            onDragCreationChanged(null)
                        }
                    },
                    onDragCancel = {
                        if (isDragging) {
                            isDragging = false
                            dragOffsetY = 0f
                            onDragCreationChanged(null)
                        }
                    }
                )
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(maxOf(24f, effectiveSlotHeight * 0.45f).dp)
                .clickable { onQuickTap() }
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(ContextColors.BorderSubtle.copy(alpha = 0.5f))
            )
            Text(
                text = "Tap for placeholder • Hold to create (${granularity.label})",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = ContextColors.InkMuted.copy(alpha = 0.45f)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(ContextColors.BorderSubtle.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
fun BannerBar(
    item: ContextItem,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFF29B6F6).copy(alpha = 0.08f))
            .border(1.5.dp, Color(0xFF29B6F6), RoundedCornerShape(7.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = "📁",
            fontSize = 11.sp
        )
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0288D1),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StageActionButton(
    title: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(DlsScales.radiusSm.dp))
            .background(if (isSelected) color else ContextColors.SurfaceRaised)
            .border(
                1.dp,
                if (isSelected) Color.Transparent else ContextColors.BorderSubtle,
                RoundedCornerShape(DlsScales.radiusSm.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else color)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) Color.White else ContextColors.InkPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReschedulePill(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(DlsScales.radiusSm.dp))
            .background(ContextColors.SurfaceRaised)
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = ContextColors.InkSecondary
        )
    }
}

private fun stageColor(status: ContextStatus): Color {
    return when (status) {
        ContextStatus.ACTIVE -> Color(0xFF4B8BF5)
        ContextStatus.PLANNED -> Color(0xFFA765BA)
        ContextStatus.PAUSED -> Color(0xFFF59E0B)
        ContextStatus.DONE -> Color(0xFF7FB844)
        ContextStatus.DROPPED -> ContextColors.SurfaceRaised
    }
}

private fun stageTextColor(status: ContextStatus): Color {
    return when (status) {
        ContextStatus.ACTIVE -> Color.Black.copy(alpha = 0.9f)
        ContextStatus.PLANNED -> Color.White
        ContextStatus.PAUSED -> Color.Black.copy(alpha = 0.9f)
        ContextStatus.DONE -> Color.Black.copy(alpha = 0.88f)
        ContextStatus.DROPPED -> ContextColors.InkMuted
    }
}
