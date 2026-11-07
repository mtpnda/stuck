package com.mtpnda.stuck.model

import java.util.UUID

enum class ContextStatus(val title: String) {
    PLANNED("Coming up"),
    ACTIVE("In your head"),
    PAUSED("Parked"),
    DONE("Closed"),
    DROPPED("Dropped")
}

enum class BarColorTag(val id: String, val displayName: String) {
    BLUE("blue", "Task / Bill (Blue)"),
    GREEN("green", "Milestone (Green)"),
    PURPLE("purple", "People / Note (Purple)"),
    CYAN("cyan", "Project (Cyan)"),
    CHARCOAL("charcoal", "Deep Focus (Dark)"),
    CORAL("coral", "Urgent (Coral)")
}

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isDone: Boolean = false
)

data class FocusSession(
    val start: Long,
    val end: Long
)

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
    val groupId: String? = null,
    val colorTag: BarColorTag = BarColorTag.BLUE,
    val isBanner: Boolean = false
) {
    val isOverdue: Boolean
        get() = endAt != null && endAt < System.currentTimeMillis() && (status == ContextStatus.ACTIVE || status == ContextStatus.PAUSED)

    val totalTimeSpentMillis: Long
        get() = sessions.sumOf { it.end - it.start }

    val totalTimeSpent: Long
        get() = totalTimeSpentMillis
}
