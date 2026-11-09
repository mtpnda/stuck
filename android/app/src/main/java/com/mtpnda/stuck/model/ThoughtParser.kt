package com.mtpnda.stuck.model

import com.mtpnda.stuck.observability.LokiObservability
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

data class ParsedThought(
    val rawInput: String,
    val title: String,
    val notes: String,
    val startAt: Instant?,
    val endAt: Instant?,
    val durationMinutes: Int?,
    val status: ContextStatus,
    val colorTag: BarColorTag,
    val isBanner: Boolean,
    val tags: List<String>,
    val latencyMs: Double
) {
    fun toMap(): Map<String, Any?> {
        val iso = DateTimeFormatter.ISO_INSTANT
        return mapOf(
            "raw_input" to rawInput,
            "title" to title,
            "notes" to notes,
            "status" to status.name.lowercase(),
            "color_tag" to colorTag.name.lowercase(),
            "is_banner" to isBanner,
            "tags" to tags,
            "latency_ms" to latencyMs,
            "start_at" to startAt?.let { iso.format(it) },
            "end_at" to endAt?.let { iso.format(it) },
            "duration_minutes" to durationMinutes
        )
    }
}

/**
 * Natural Language Inference & Free Text Parser for Android Stuck.
 * Translates unstructured text into structured tasks with duration, schedule, and stage.
 */
object ThoughtParser {

    fun parse(
        input: String,
        referenceDate: LocalDate = LocalDate.now(),
        defaultStatus: ContextStatus? = null,
        recordTelemetry: Boolean = true
    ): ParsedThought {
        val startNano = System.nanoTime()
        val trimmed = input.trim()

        var workingText = trimmed
        var extractedNotes = ""
        val extractedTags = mutableListOf<String>()
        var isBanner = false
        var resolvedStatus = defaultStatus ?: ContextStatus.ACTIVE

        // 1. Extract Notes
        if (workingText.contains("//")) {
            val parts = workingText.split("//", limit = 2)
            workingText = parts[0].trim()
            extractedNotes = parts[1].trim()
        } else if (workingText.contains("notes:", ignoreCase = true)) {
            val idx = workingText.indexOf("notes:", ignoreCase = true)
            extractedNotes = workingText.substring(idx + 6).trim()
            workingText = workingText.substring(0, idx).trim()
        }

        // 2. Extract Hashtags from both Title and Notes
        fun processTags(text: String): String {
            val tagPattern = Pattern.compile("#([A-Za-z0-9_-]+)")
            val tagMatcher = tagPattern.matcher(text)
            val sb = StringBuffer()
            while (tagMatcher.find()) {
                val tag = tagMatcher.group(1).lowercase()
                extractedTags.add(tag)
                when (tag) {
                    "banner", "project" -> isBanner = true
                    "active", "inhead", "head" -> resolvedStatus = ContextStatus.ACTIVE
                    "planned", "coming", "soon" -> resolvedStatus = ContextStatus.PLANNED
                    "paused", "parked", "hold" -> resolvedStatus = ContextStatus.PAUSED
                    "done", "closed" -> resolvedStatus = ContextStatus.DONE
                }
                tagMatcher.appendReplacement(sb, "")
            }
            tagMatcher.appendTail(sb)
            return sb.toString().trim()
        }

        workingText = processTags(workingText)
        extractedNotes = processTags(extractedNotes)

        // 3. Resolve Target Day
        var targetDay = referenceDate
        if (workingText.contains("tomorrow", ignoreCase = true)) {
            targetDay = referenceDate.plusDays(1)
            workingText = workingText.replace(Regex("(?i)\\btomorrow\\b"), "").trim()
            if (defaultStatus == null) resolvedStatus = ContextStatus.PLANNED
        } else if (workingText.contains("today", ignoreCase = true)) {
            targetDay = referenceDate
            workingText = workingText.replace(Regex("(?i)\\btoday\\b"), "").trim()
        }

        // 4. Extract Explicit Time Ranges: e.g. "14:00 - 16:00"
        var parsedStart: Instant? = null
        var parsedEnd: Instant? = null
        var parsedDurationMinutes: Int? = null

        val rangePattern = Pattern.compile("(?i)\\b(\\d{1,2}):(\\d{2})\\s*(?:-|to|–)\\s*(\\d{1,2}):(\\d{2})\\b")
        val rangeMatcher = rangePattern.matcher(workingText)
        if (rangeMatcher.find()) {
            val sh = rangeMatcher.group(1).toInt()
            val sm = rangeMatcher.group(2).toInt()
            val eh = rangeMatcher.group(3).toInt()
            val em = rangeMatcher.group(4).toInt()

            val startLdt = LocalDateTime.of(targetDay, LocalTime.of(sh.coerceIn(0, 23), sm.coerceIn(0, 59)))
            var endLdt = LocalDateTime.of(targetDay, LocalTime.of(eh.coerceIn(0, 23), em.coerceIn(0, 59)))
            if (!endLdt.isAfter(startLdt)) {
                endLdt = endLdt.plusDays(1)
            }

            parsedStart = startLdt.atZone(ZoneId.systemDefault()).toInstant()
            parsedEnd = endLdt.atZone(ZoneId.systemDefault()).toInstant()
            val dur = java.time.Duration.between(startLdt, endLdt).toMinutes().toInt()
            parsedDurationMinutes = dur.coerceAtLeast(1)

            workingText = workingText.removeRange(rangeMatcher.start(), rangeMatcher.end()).trim()
        }

        // Single time: e.g. "at 14:00", "@9am"
        if (parsedStart == null) {
            val singlePattern = Pattern.compile("(?i)\\b(?:at|@)\\s*(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?\\b")
            val singleMatcher = singlePattern.matcher(workingText)
            if (singleMatcher.find()) {
                var h = singleMatcher.group(1).toInt()
                val m = singleMatcher.group(2)?.toInt() ?: 0
                val ampm = singleMatcher.group(3)?.lowercase()
                if (ampm == "pm" && h < 12) h += 12
                if (ampm == "am" && h == 12) h = 0

                val startLdt = LocalDateTime.of(targetDay, LocalTime.of(h.coerceIn(0, 23), m.coerceIn(0, 59)))
                parsedStart = startLdt.atZone(ZoneId.systemDefault()).toInstant()
                workingText = workingText.removeRange(singleMatcher.start(), singleMatcher.end()).trim()
            }
        }

        // Duration: e.g. "(45m)", "2h", "45m"
        val durPattern = Pattern.compile("(?i)(?:\\(|\\b)(\\d+(?:\\.\\d+)?)\\s*(h|hr|hrs|hours|m|min|mins|minutes)(?:\\)|\\b)")
        val durMatcher = durPattern.matcher(workingText)
        if (durMatcher.find()) {
            val v = durMatcher.group(1).toDouble()
            val unit = durMatcher.group(2).lowercase()
            val mins = if (unit.startsWith("h")) Math.round(v * 60).toInt() else Math.round(v).toInt()
            parsedDurationMinutes = mins

            if (parsedStart != null && parsedEnd == null) {
                parsedEnd = parsedStart.plus(java.time.Duration.ofMinutes(mins.toLong()))
            }
            workingText = workingText.removeRange(durMatcher.start(), durMatcher.end()).trim()
        }

        // Clean Title
        var finalTitle = workingText
            .replace("()", "")
            .replace("[]", "")
            .replace(Regex("\\s+"), " ")
            .trim('-', '–', '—', ',', ' ')
        if (finalTitle.isEmpty()) finalTitle = "Thought"

        val colorTag = when (resolvedStatus) {
            ContextStatus.ACTIVE -> BarColorTag.BLUE
            ContextStatus.PLANNED -> BarColorTag.PURPLE
            ContextStatus.PAUSED -> BarColorTag.CHARCOAL
            ContextStatus.DONE -> BarColorTag.GREEN
            ContextStatus.DROPPED -> BarColorTag.CHARCOAL
        }

        val latencyMs = (System.nanoTime() - startNano) / 1_000_000.0

        val result = ParsedThought(
            rawInput = input,
            title = finalTitle,
            notes = extractedNotes,
            startAt = parsedStart,
            endAt = parsedEnd,
            durationMinutes = parsedDurationMinutes,
            status = resolvedStatus,
            colorTag = colorTag,
            isBanner = isBanner,
            tags = extractedTags,
            latencyMs = latencyMs
        )

        if (recordTelemetry) {
            LokiObservability.shared.logModelInference(
                input = input,
                output = result.toMap(),
                latencyMs = latencyMs
            )
        }

        return result
    }
}
