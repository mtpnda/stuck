import Foundation
import SwiftUI

enum ContextStatus: String, Codable, CaseIterable, Identifiable {
    case planned    // Coming up
    case active     // In your head / Doing
    case paused     // Parked
    case done       // Closed / Done
    case dropped    // Dropped

    var id: String { rawValue }

    var title: String {
        switch self {
        case .planned: return "Coming up"
        case .active:  return "In your head"
        case .paused:  return "Parked"
        case .done:    return "Closed"
        case .dropped: return "Dropped"
        }
    }
}

enum BarColorTag: String, Codable, CaseIterable, Identifiable {
    case blue = "blue"        // Tasks / Focus Items
    case green = "green"      // Milestones / Closed Thoughts
    case purple = "purple"    // Scheduled / Coming Thoughts
    case cyan = "cyan"        // Spanning Projects / Banners
    case charcoal = "charcoal"// Parked / Deep Focus
    case coral = "coral"      // Alert / Urgent

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .blue: return "Task / Bill (Blue)"
        case .green: return "Milestone (Green)"
        case .purple: return "People / Note (Purple)"
        case .cyan: return "Project (Cyan)"
        case .charcoal: return "Deep Focus (Dark)"
        case .coral: return "Urgent (Coral)"
        }
    }
}

struct TodoItem: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var text: String
    var isDone: Bool = false
}

struct FocusSession: Codable, Equatable {
    var start: Date
    var end: Date
}

struct ContextItem: Identifiable, Codable, Equatable {
    var id: String = UUID().uuidString
    var title: String
    var notes: String = ""
    var todos: [TodoItem] = []
    var startAt: Date = Date()
    var endAt: Date? = nil
    var sessions: [FocusSession] = []
    var status: ContextStatus = .active
    var createdAt: Date = Date()
    var closedAt: Date? = nil
    var groupId: String? = nil
    var colorTag: BarColorTag = .blue
    var isBanner: Bool = false

    enum CodingKeys: String, CodingKey {
        case id, title, notes, todos, startAt, endAt, sessions, status, createdAt, closedAt, groupId, colorTag, isBanner
    }

    init(
        id: String = UUID().uuidString,
        title: String,
        notes: String = "",
        todos: [TodoItem] = [],
        startAt: Date = Date(),
        endAt: Date? = nil,
        sessions: [FocusSession] = [],
        status: ContextStatus = .active,
        createdAt: Date = Date(),
        closedAt: Date? = nil,
        groupId: String? = nil,
        colorTag: BarColorTag = .blue,
        isBanner: Bool = false
    ) {
        self.id = id
        self.title = title
        self.notes = notes
        self.todos = todos
        self.startAt = startAt
        self.endAt = endAt
        self.sessions = sessions
        self.status = status
        self.createdAt = createdAt
        self.closedAt = closedAt
        self.groupId = groupId
        self.colorTag = colorTag
        self.isBanner = isBanner
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decodeIfPresent(String.self, forKey: .id) ?? UUID().uuidString
        title = try container.decode(String.self, forKey: .title)
        notes = try container.decodeIfPresent(String.self, forKey: .notes) ?? ""
        todos = try container.decodeIfPresent([TodoItem].self, forKey: .todos) ?? []
        startAt = try container.decodeIfPresent(Date.self, forKey: .startAt) ?? Date()
        endAt = try container.decodeIfPresent(Date.self, forKey: .endAt)
        sessions = try container.decodeIfPresent([FocusSession].self, forKey: .sessions) ?? []
        status = try container.decodeIfPresent(ContextStatus.self, forKey: .status) ?? .active
        createdAt = try container.decodeIfPresent(Date.self, forKey: .createdAt) ?? Date()
        closedAt = try container.decodeIfPresent(Date.self, forKey: .closedAt)
        groupId = try container.decodeIfPresent(String.self, forKey: .groupId)
        colorTag = try container.decodeIfPresent(BarColorTag.self, forKey: .colorTag) ?? .blue
        isBanner = try container.decodeIfPresent(Bool.self, forKey: .isBanner) ?? false
    }

    var isOverdue: Bool {
        guard let endAt = endAt else { return false }
        return endAt < Date() && (status == .active || status == .paused)
    }

    var totalTimeSpent: TimeInterval {
        sessions.reduce(0) { $0 + $1.end.timeIntervalSince($1.start) }
    }

    // MARK: - Multi-Day & Intersecting Schedule Helpers

    var spansAcrossMidnight: Bool {
        guard let end = endAt else { return false }
        let cal = Calendar.current
        return !cal.isDate(startAt, inSameDayAs: end)
    }

    func intersects(date: Date) -> Bool {
        let cal = Calendar.current
        let dayStart = cal.startOfDay(for: date)
        guard let dayEnd = cal.date(byAdding: .day, value: 1, to: dayStart) else { return false }
        let itemStart = startAt
        let itemEnd = endAt ?? itemStart.addingTimeInterval(3600)
        return itemStart < dayEnd && itemEnd > dayStart
    }

    func isContinuation(for date: Date) -> Bool {
        let cal = Calendar.current
        let dayStart = cal.startOfDay(for: date)
        return startAt < dayStart
    }

    func isOutgoing(for date: Date) -> Bool {
        guard let end = endAt else { return false }
        let cal = Calendar.current
        let dayStart = cal.startOfDay(for: date)
        guard let dayEnd = cal.date(byAdding: .day, value: 1, to: dayStart) else { return false }
        return end > dayEnd
    }
}
