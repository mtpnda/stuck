import Foundation

/// Structured parsed result of natural language free-text input.
struct ParsedThought: Equatable, Sendable {
    let rawInput: String
    let title: String
    let notes: String
    let startAt: Date?
    let endAt: Date?
    let durationMinutes: Int?
    let status: ContextStatus
    let colorTag: BarColorTag
    let isBanner: Bool
    let tags: [String]
    let latencyMs: Double

    func toDictionary() -> [String: Any] {
        var dict: [String: Any] = [
            "raw_input": rawInput,
            "title": title,
            "notes": notes,
            "status": status.rawValue,
            "color_tag": colorTag.rawValue,
            "is_banner": isBanner,
            "tags": tags,
            "latency_ms": latencyMs
        ]
        let iso = ISO8601DateFormatter()
        if let s = startAt { dict["start_at"] = iso.string(from: s) }
        if let e = endAt { dict["end_at"] = iso.string(from: e) }
        if let d = durationMinutes { dict["duration_minutes"] = d }
        return dict
    }
}

/// Natural Language Inference & Free Text Parser for Stuck.
/// Translates unstructured thoughts into timeline-accurate tasks with duration, dates, and stage.
struct ThoughtParser {

    /// Parses raw free text into structured task fields, logging inference to Loki.
    static func parse(
        input: String,
        referenceDate: Date = Date(),
        defaultStatus: ContextStatus? = nil,
        recordTelemetry: Bool = true
    ) -> ParsedThought {
        let startTime = CFAbsoluteTimeGetCurrent()
        let trimmed = input.trimmingCharacters(in: .whitespacesAndNewlines)

        var workingText = trimmed
        var extractedNotes = ""
        var extractedTags: [String] = []
        var isBanner = false
        var resolvedStatus = defaultStatus ?? .active

        // 1. Extract Notes separated by // or notes:
        if let slashRange = workingText.range(of: "//") {
            extractedNotes = String(workingText[slashRange.upperBound...]).trimmingCharacters(in: .whitespacesAndNewlines)
            workingText = String(workingText[..<slashRange.lowerBound]).trimmingCharacters(in: .whitespacesAndNewlines)
        } else if let notesRange = workingText.range(of: "notes:", options: .caseInsensitive) {
            extractedNotes = String(workingText[notesRange.upperBound...]).trimmingCharacters(in: .whitespacesAndNewlines)
            workingText = String(workingText[..<notesRange.lowerBound]).trimmingCharacters(in: .whitespacesAndNewlines)
        }

        // 2. Extract Hashtags from both Title and Notes (#banner, #project, #active, #planned, #paused, #done)
        let tagRegex = try? NSRegularExpression(pattern: #"#([A-Za-z0-9_-]+)"#)
        func processTags(in text: inout String) {
            guard let matches = tagRegex?.matches(in: text, range: NSRange(text.startIndex..., in: text)) else { return }
            for match in matches.reversed() {
                if let r = Range(match.range(at: 1), in: text) {
                    let tag = String(text[r]).lowercased()
                    extractedTags.append(tag)
                    if tag == "banner" || tag == "project" {
                        isBanner = true
                    } else if tag == "active" || tag == "inhead" || tag == "head" {
                        resolvedStatus = .active
                    } else if tag == "planned" || tag == "coming" || tag == "soon" {
                        resolvedStatus = .planned
                    } else if tag == "paused" || tag == "parked" || tag == "hold" {
                        resolvedStatus = .paused
                    } else if tag == "done" || tag == "closed" {
                        resolvedStatus = .done
                    }
                }
                if let fullRange = Range(match.range, in: text) {
                    text.removeSubrange(fullRange)
                }
            }
            text = text.trimmingCharacters(in: .whitespacesAndNewlines)
        }

        processTags(in: &workingText)
        processTags(in: &extractedNotes)

        // 3. Resolve Target Date (Today vs Tomorrow vs specific day)
        var targetDay = referenceDate
        let cal = Calendar.current
        let lower = workingText.lowercased()
        if lower.contains("tomorrow") {
            targetDay = cal.date(byAdding: .day, value: 1, to: referenceDate) ?? referenceDate
            workingText = removeWord(from: workingText, word: "tomorrow")
            if defaultStatus == nil { resolvedStatus = .planned }
        } else if lower.contains("today") {
            targetDay = referenceDate
            workingText = removeWord(from: workingText, word: "today")
        }

        // 4. Extract Explicit Time Ranges: e.g. "14:00 - 16:00", "8:30 - 10:00", "8am - 10am"
        var parsedStart: Date? = nil
        var parsedEnd: Date? = nil
        var parsedDurationMinutes: Int? = nil

        // Pattern 1: HH:mm - HH:mm (e.g. 14:00 - 16:30 or 14:00-16:30)
        let timeRangeRegex = try? NSRegularExpression(pattern: #"(?i)\b(\d{1,2}):(\d{2})\s*(?:-|to|–)\s*(\d{1,2}):(\d{2})\b"#)
        if let match = timeRangeRegex?.firstMatch(in: workingText, range: NSRange(workingText.startIndex..., in: workingText)) {
            if let r1 = Range(match.range(at: 1), in: workingText),
               let r2 = Range(match.range(at: 2), in: workingText),
               let r3 = Range(match.range(at: 3), in: workingText),
               let r4 = Range(match.range(at: 4), in: workingText),
               let sh = Int(workingText[r1]), let sm = Int(workingText[r2]),
               let eh = Int(workingText[r3]), let em = Int(workingText[r4]) {

                parsedStart = makeTime(date: targetDay, hour: sh, minute: sm)
                var endCandidate = makeTime(date: targetDay, hour: eh, minute: em)
                if endCandidate <= (parsedStart ?? targetDay) {
                    // Spans across midnight into next day
                    endCandidate = cal.date(byAdding: .day, value: 1, to: endCandidate) ?? endCandidate
                }
                parsedEnd = endCandidate
                let totalMinutes = Int((parsedEnd!.timeIntervalSince(parsedStart!)) / 60)
                parsedDurationMinutes = max(1, totalMinutes)

                if let fullRange = Range(match.range, in: workingText) {
                    workingText.removeSubrange(fullRange)
                }
            }
        }

        // Pattern 2: Single Start Time (e.g. "at 14:00", "@14:00", "at 9am", "at 10:30")
        if parsedStart == nil {
            let singleTimeRegex = try? NSRegularExpression(pattern: #"(?i)\b(?:at|@)\s*(\d{1,2})(?::(\d{2}))?\s*(am|pm)?\b"#)
            if let match = singleTimeRegex?.firstMatch(in: workingText, range: NSRange(workingText.startIndex..., in: workingText)) {
                if let rH = Range(match.range(at: 1), in: workingText),
                   var h = Int(workingText[rH]) {
                    var m = 0
                    if match.range(at: 2).location != NSNotFound,
                       let rM = Range(match.range(at: 2), in: workingText),
                       let parsedM = Int(workingText[rM]) {
                        m = parsedM
                    }
                    if match.range(at: 3).location != NSNotFound,
                       let rAmpm = Range(match.range(at: 3), in: workingText) {
                        let ampm = String(workingText[rAmpm]).lowercased()
                        if ampm == "pm" && h < 12 { h += 12 }
                        if ampm == "am" && h == 12 { h = 0 }
                    }
                    parsedStart = makeTime(date: targetDay, hour: h, minute: m)
                    if let fullRange = Range(match.range, in: workingText) {
                        workingText.removeSubrange(fullRange)
                    }
                }
            }
        }

        // Pattern 3: Duration string: e.g. "(45m)", "2h", "45m", "1.5h", "90 mins"
        let durRegex = try? NSRegularExpression(pattern: #"(?i)(?:\(|\b)(\d+(?:\.\d+)?)\s*(h|hr|hrs|hours|m|min|mins|minutes)(?:\)|\b)"#)
        if let match = durRegex?.firstMatch(in: workingText, range: NSRange(workingText.startIndex..., in: workingText)) {
            if let rVal = Range(match.range(at: 1), in: workingText),
               let rUnit = Range(match.range(at: 2), in: workingText),
               let val = Double(workingText[rVal]) {
                let unit = String(workingText[rUnit]).lowercased()
                let mins: Int
                if unit.hasPrefix("h") {
                    mins = Int(round(val * 60))
                } else {
                    mins = Int(round(val))
                }
                parsedDurationMinutes = mins
                if let start = parsedStart, parsedEnd == nil {
                    parsedEnd = start.addingTimeInterval(Double(mins) * 60)
                }
                if let fullRange = Range(match.range, in: workingText) {
                    workingText.removeSubrange(fullRange)
                }
            }
        }

        // 5. Final Title Cleanup: Trim punctuation, empty parentheses, extra spaces
        var finalTitle = workingText
            .replacingOccurrences(of: "()", with: "")
            .replacingOccurrences(of: "[]", with: "")
            .replacingOccurrences(of: "  ", with: " ")
            .trimmingCharacters(in: CharacterSet.whitespacesAndNewlines.union(CharacterSet(charactersIn: "-–—,")))
        if finalTitle.isEmpty {
            finalTitle = "Thought"
        }

        // 6. Map Color Tag based on status
        let colorTag: BarColorTag
        switch resolvedStatus {
        case .active: colorTag = .blue
        case .planned: colorTag = .purple
        case .paused: colorTag = .charcoal
        case .done: colorTag = .green
        case .dropped: colorTag = .charcoal
        }

        let latencyMs = (CFAbsoluteTimeGetCurrent() - startTime) * 1000.0

        let result = ParsedThought(
            rawInput: input,
            title: finalTitle,
            notes: extractedNotes,
            startAt: parsedStart,
            endAt: parsedEnd,
            durationMinutes: parsedDurationMinutes,
            status: resolvedStatus,
            colorTag: colorTag,
            isBanner: isBanner,
            tags: extractedTags,
            latencyMs: latencyMs
        )

        // Log Model Inference Telemetry to Loki (if enabled)
        if recordTelemetry {
            LokiObservability.shared.logModelInference(
                input: input,
                output: result.toDictionary(),
                latencyMs: latencyMs
            )
        }

        return result
    }

    private static func makeTime(date: Date, hour: Int, minute: Int) -> Date {
        let cal = Calendar.current
        var comp = cal.dateComponents([.year, .month, .day], from: date)
        comp.hour = max(0, min(23, hour))
        comp.minute = max(0, min(59, minute))
        comp.second = 0
        return cal.date(from: comp) ?? date
    }

    private static func removeWord(from text: String, word: String) -> String {
        let pattern = "(?i)\\b" + NSRegularExpression.escapedPattern(for: word) + "\\b"
        if let regex = try? NSRegularExpression(pattern: pattern) {
            let range = NSRange(text.startIndex..., in: text)
            return regex.stringByReplacingMatches(in: text, options: [], range: range, withTemplate: "")
        }
        return text
    }
}
