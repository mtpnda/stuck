package com.mtpnda.stuck.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mtpnda.stuck.data.ContextRepository
import com.mtpnda.stuck.model.BarColorTag
import com.mtpnda.stuck.model.ContextStatus
import com.mtpnda.stuck.model.TodoItem
import com.mtpnda.stuck.ui.components.*
import com.mtpnda.stuck.ui.theme.ContextColors
import com.mtpnda.stuck.ui.theme.DlsScales
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ContextSheet(
    repository: ContextRepository,
    contextId: String?,
    initialStatus: ContextStatus = ContextStatus.ACTIVE,
    initialDate: Long = System.currentTimeMillis(),
    initialEndDate: Long? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexts by repository.contexts.collectAsState()
    val existing = remember(contexts, contextId) { contexts.firstOrNull { it.id == contextId } }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: initialStatus) }
    val defaultColor = when (initialStatus) {
        ContextStatus.ACTIVE -> BarColorTag.BLUE
        ContextStatus.PLANNED -> BarColorTag.PURPLE
        ContextStatus.PAUSED -> BarColorTag.CHARCOAL
        ContextStatus.DONE -> BarColorTag.GREEN
        ContextStatus.DROPPED -> BarColorTag.CHARCOAL
    }
    var colorTag by remember { mutableStateOf(existing?.colorTag ?: defaultColor) }
    var isBanner by remember { mutableStateOf(existing?.isBanner ?: false) }
    var scheduledDate by remember { mutableStateOf(existing?.startAt ?: initialDate) }
    var hasEndTime by remember { mutableStateOf(existing?.endAt != null || initialEndDate != null) }
    var scheduledEndDate by remember { mutableStateOf(existing?.endAt ?: initialEndDate ?: (scheduledDate + 3600_000L)) }
    var todos by remember { mutableStateOf(existing?.todos ?: emptyList()) }
    var newTodoText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ContextColors.Bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (contextId == null) "New Context" else "Edit Context",
                style = MaterialTheme.typography.titleLarge,
                color = ContextColors.InkPrimary
            )
            Text(
                text = "Close ✕",
                style = MaterialTheme.typography.labelMedium,
                color = ContextColors.InkMuted,
                modifier = Modifier.clickable { onDismiss() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Context Title", style = MaterialTheme.typography.labelSmall, color = ContextColors.InkSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        CalmTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = "e.g. System Handover, Health Checkup, Deep Work..."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Cognitive Stage & Color", style = MaterialTheme.typography.labelSmall, color = ContextColors.InkSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val stagesWithColor = listOf(
                ContextStatus.ACTIVE to ("In Head" to Color(0xFF4B8BF5)),
                ContextStatus.PLANNED to ("Coming" to Color(0xFFA765BA)),
                ContextStatus.PAUSED to ("Park" to Color(0xFFF59E0B)),
                ContextStatus.DONE to ("Closed" to Color(0xFF7FB844))
            )
            stagesWithColor.forEach { (st, pair) ->
                val (label, col) = pair
                val isSelected = status == st
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSelected) col else ContextColors.SurfaceRaised)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else ContextColors.BorderSubtle,
                            shape = RoundedCornerShape(999.dp)
                        )
                        .clickable {
                            status = st
                            colorTag = when (st) {
                                ContextStatus.ACTIVE -> BarColorTag.BLUE
                                ContextStatus.PLANNED -> BarColorTag.PURPLE
                                ContextStatus.PAUSED -> BarColorTag.CHARCOAL
                                ContextStatus.DONE -> BarColorTag.GREEN
                                ContextStatus.DROPPED -> BarColorTag.CHARCOAL
                            }
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else col)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else ContextColors.InkPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Spanning Banner Option
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(DlsScales.radiusMd.dp))
                .background(ContextColors.SurfaceRaised)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Spanning Project Banner",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ContextColors.InkPrimary
                )
                Text(
                    text = "Full-width cyan outlined banner for projects & milestones",
                    style = MaterialTheme.typography.labelSmall,
                    color = ContextColors.InkMuted
                )
            }
            Switch(
                checked = isBanner,
                onCheckedChange = { isBanner = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ContextColors.OnCharcoal,
                    checkedTrackColor = Color(0xFF29B6F6)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time & Duration Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(DlsScales.radiusMd.dp))
                .background(ContextColors.SurfaceRaised)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Specify Duration / End Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ContextColors.InkPrimary
                )
                Text(
                    text = if (hasEndTime) {
                        val tf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val durMinutes = maxOf(15, ((scheduledEndDate - scheduledDate) / 60000L).toInt())
                        val durStr = if (durMinutes < 60) "${durMinutes}m" else "${durMinutes / 60}h ${if (durMinutes % 60 != 0) "${durMinutes % 60}m" else ""}".trim()
                        "${tf.format(Date(scheduledDate))} – ${tf.format(Date(scheduledEndDate))} ($durStr)"
                    } else "Untimed thought (standard bar)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hasEndTime) Color(0xFF0288D1) else ContextColors.InkMuted
                )
            }
            Switch(
                checked = hasEndTime,
                onCheckedChange = { checked ->
                    hasEndTime = checked
                    if (checked && scheduledEndDate <= scheduledDate) {
                        scheduledEndDate = scheduledDate + 3600_000L
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ContextColors.OnCharcoal,
                    checkedTrackColor = Color(0xFF29B6F6)
                )
            )
        }

        if (hasEndTime) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    "30m" to 1800_000L,
                    "1h" to 3600_000L,
                    "1.5h" to 5400_000L,
                    "2h" to 7200_000L,
                    "3h" to 10800_000L
                ).forEach { (label, durationMillis) ->
                    val isSelected = (scheduledEndDate - scheduledDate) == durationMillis
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (isSelected) ContextColors.Charcoal else ContextColors.Card)
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else ContextColors.BorderSubtle,
                                RoundedCornerShape(999.dp)
                            )
                            .clickable {
                                scheduledEndDate = scheduledDate + durationMillis
                            }
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ContextColors.OnCharcoal else ContextColors.InkPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Notes & Details", style = MaterialTheme.typography.labelSmall, color = ContextColors.InkSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        CalmTextField(
            value = notes,
            onValueChange = { notes = it },
            placeholder = "Additional thoughts, links, or criteria...",
            singleLine = false
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Checklist", style = MaterialTheme.typography.labelSmall, color = ContextColors.InkSecondary)
        Spacer(modifier = Modifier.height(8.dp))

        todos.forEachIndexed { index, todo ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = todo.isDone,
                    onCheckedChange = { isChecked ->
                        todos = todos.toMutableList().also {
                            it[index] = todo.copy(isDone = isChecked)
                        }
                    },
                    colors = CheckboxDefaults.colors(checkedColor = ContextColors.CalmChip)
                )
                Text(
                    text = todo.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (todo.isDone) ContextColors.InkMuted else ContextColors.InkPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "✕",
                    color = ContextColors.AlertInk,
                    modifier = Modifier.clickable {
                        todos = todos.toMutableList().also { it.removeAt(index) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalmTextField(
                value = newTodoText,
                onValueChange = { newTodoText = it },
                placeholder = "Add checklist item...",
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val trimmed = newTodoText.trim()
                    if (trimmed.isNotBlank()) {
                        todos = todos + TodoItem(text = trimmed)
                        newTodoText = ""
                    }
                },
                shape = RoundedCornerShape(DlsScales.radiusMd.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ContextColors.Charcoal)
            ) {
                Text("Add", color = ContextColors.OnCharcoal)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        PillButton(
            title = if (contextId == null) "Save Context" else "Update Context",
            onClick = {
                val trimmed = title.trim()
                if (trimmed.isNotBlank()) {
                    val endAtVal = if (hasEndTime) scheduledEndDate else null
                    if (contextId == null) {
                        repository.addContext(
                            title = trimmed,
                            notes = notes,
                            todos = todos,
                            startAt = scheduledDate,
                            endAt = endAtVal,
                            status = status,
                            colorTag = colorTag,
                            isBanner = isBanner
                        )
                    } else {
                        repository.updateContext(
                            id = contextId,
                            title = trimmed,
                            notes = notes,
                            todos = todos,
                            startAt = scheduledDate,
                            endAt = endAtVal,
                            status = status,
                            colorTag = colorTag,
                            isBanner = isBanner
                        )
                    }
                    onDismiss()
                }
            }
        )

        if (contextId != null) {
            Spacer(modifier = Modifier.height(10.dp))
            CoralOutlinedPillButton(
                title = "Delete this context",
                onClick = {
                    repository.removeContext(contextId)
                    onDismiss()
                }
            )
        }
    }
}
