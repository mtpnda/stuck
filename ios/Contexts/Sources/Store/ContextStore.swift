import Foundation
import LocalAuthentication
import SwiftUI

@MainActor
final class ContextStore: ObservableObject {
    @Published var profile: Profile? = nil
    @Published var contexts: [ContextItem] = []
    @Published var limit: Int = 3
    @Published var isLocked: Bool = true
    @Published var focusContextId: String? = nil
    @Published var focusStartedAt: Date? = nil

    private let storageKey = "contexts:native:v1"

    init() {
        if CommandLine.arguments.contains("-reset") {
            resetAllData()
        } else {
            loadFromDisk()
        }
        if CommandLine.arguments.contains("-guest") && profile == nil {
            createProfile(name: "Guest Reviewer", email: "guest@stuck.app", faceId: false, provider: "guest")
            seedSampleContexts()
        } else if CommandLine.arguments.contains("-overflow") {
            createProfile(name: "Guest Reviewer", email: "guest@stuck.app", faceId: false, provider: "guest")
            seedSampleContexts()
            setLimit(2)
            _ = addContext(title: "Deep Profiling Spike", status: .active, colorTag: .coral)
        } else if CommandLine.arguments.contains("-focus") {
            createProfile(name: "Guest Reviewer", email: "guest@stuck.app", faceId: false, provider: "guest")
            seedSampleContexts()
            if let first = contexts.first(where: { $0.status == .active }) {
                focusOn(id: first.id)
            }
        } else if CommandLine.arguments.contains("-quickcapture") {
            createProfile(name: "Guest Reviewer", email: "guest@stuck.app", faceId: false, provider: "guest")
            let cal = Calendar.current
            let today = cal.startOfDay(for: Date())
            let tomorrow = cal.date(byAdding: .day, value: 1, to: today) ?? today
            _ = quickCapture(title: "Active task captured into In head", targetDate: today, stageFilter: .active)
            _ = quickCapture(title: "Planned task scheduled tomorrow", targetDate: tomorrow, stageFilter: nil)
            _ = quickCapture(title: "Parked thought captured directly", targetDate: today, stageFilter: .paused)
        }
    }

    // MARK: - Filtered Lists

    var plannedContexts: [ContextItem] {
        contexts.filter { $0.status == .planned }.sorted { $0.startAt < $1.startAt }
    }

    var activeContexts: [ContextItem] {
        contexts.filter { $0.status == .active }.sorted { a, b in
            if a.id == focusContextId { return true }
            if b.id == focusContextId { return false }
            return (a.endAt ?? Date.distantFuture) < (b.endAt ?? Date.distantFuture)
        }
    }

    var pausedContexts: [ContextItem] {
        contexts.filter { $0.status == .paused }
    }

    var closedContexts: [ContextItem] {
        contexts.filter { $0.status == .done || $0.status == .dropped }
            .sorted { ($0.closedAt ?? $0.createdAt) > ($1.closedAt ?? $1.createdAt) }
    }

    var load: Int {
        activeContexts.count
    }

    var activeCount: Int {
        activeContexts.count
    }

    var isOverLimit: Bool {
        load > limit
    }

    var focusedContext: ContextItem? {
        guard let id = focusContextId else { return nil }
        return contexts.first { $0.id == id }
    }

    var totalFocusTime: TimeInterval {
        contexts.reduce(0) { $0 + $1.totalTimeSpent } + (focusStartedAt != nil ? Date().timeIntervalSince(focusStartedAt!) : 0)
    }

    // MARK: - Day Filtering

    func contextsFor(date: Date) -> [ContextItem] {
        contexts.filter { item in
            Calendar.current.isDate(item.startAt, inSameDayAs: date)
        }
    }

    func loadFor(date: Date) -> Int {
        contextsFor(date: date).count { $0.status == .active }
    }

    // MARK: - Actions

    func createProfile(name: String, email: String? = nil, avatarUrl: String? = nil, faceId: Bool = true, provider: String = "local") {
        let p = Profile(
            name: name.trimmingCharacters(in: .whitespacesAndNewlines),
            email: email?.trimmingCharacters(in: .whitespacesAndNewlines),
            avatarUrl: avatarUrl,
            faceId: faceId,
            provider: provider,
            createdAt: Date()
        )
        self.profile = p
        self.isLocked = false
        save()
    }

    func updateProfile(name: String? = nil, email: String? = nil, faceId: Bool? = nil) {
        guard var p = profile else { return }
        if let name = name { p.name = name }
        if let email = email { p.email = email }
        if let faceId = faceId { p.faceId = faceId }
        self.profile = p
        save()
    }

    func signOut() {
        self.profile = nil
        self.isLocked = true
        save()
    }

    func resetAllData() {
        stopFocus()
        self.profile = nil
        self.contexts = []
        self.limit = 3
        self.isLocked = true
        save()
    }

    func setLimit(_ newLimit: Int) {
        self.limit = max(1, min(9, newLimit))
        save()
    }

    /// Resolves target status and color tag for quick capture based on selected stage filter and target date
    public static func resolveQuickCaptureContext(
        selectedFilter: ContextStatus?,
        targetDate: Date,
        referenceDate: Date = Date()
    ) -> (status: ContextStatus, colorTag: BarColorTag, scheduledDate: Date) {
        let cal = Calendar.current
        let targetStart = cal.startOfDay(for: targetDate)
        let refStart = cal.startOfDay(for: referenceDate)

        let status: ContextStatus
        if let explicit = selectedFilter {
            status = explicit
        } else if targetStart > refStart {
            status = .planned
        } else {
            status = .active
        }

        let color: BarColorTag
        switch status {
        case .active: color = .blue
        case .planned: color = .purple
        case .paused: color = .charcoal
        case .done: color = .green
        case .dropped: color = .charcoal
        }

        // Combine date from targetDate with time from referenceDate
        let targetComponents = cal.dateComponents([.year, .month, .day], from: targetDate)
        let timeComponents = cal.dateComponents([.hour, .minute, .second], from: referenceDate)
        var combined = DateComponents()
        combined.year = targetComponents.year
        combined.month = targetComponents.month
        combined.day = targetComponents.day
        combined.hour = timeComponents.hour
        combined.minute = timeComponents.minute
        combined.second = timeComponents.second
        let scheduled = cal.date(from: combined) ?? targetDate

        return (status, color, scheduled)
    }

    @discardableResult
    func quickCapture(
        title: String,
        notes: String = "",
        targetDate: Date,
        stageFilter: ContextStatus? = nil
    ) -> String {
        var parsedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
        var parsedNotes = notes.trimmingCharacters(in: .whitespacesAndNewlines)
        if parsedNotes.isEmpty && parsedTitle.contains("//") {
            let parts = parsedTitle.components(separatedBy: "//")
            parsedTitle = parts[0].trimmingCharacters(in: .whitespacesAndNewlines)
            parsedNotes = parts.dropFirst().joined(separator: "//").trimmingCharacters(in: .whitespacesAndNewlines)
        }

        let (resolvedStatus, resolvedColor, scheduled) = Self.resolveQuickCaptureContext(
            selectedFilter: stageFilter,
            targetDate: targetDate
        )
        return addContext(
            title: parsedTitle,
            notes: parsedNotes,
            startAt: scheduled,
            status: resolvedStatus,
            colorTag: resolvedColor
        )
    }

    @discardableResult
    func addContext(
        title: String,
        notes: String = "",
        todos: [TodoItem] = [],
        startAt: Date = Date(),
        endAt: Date? = nil,
        status: ContextStatus = .active,
        colorTag: BarColorTag = .blue,
        isBanner: Bool = false
    ) -> String {
        let now = Date()
        let initialStatus = startAt > now && status == .active ? ContextStatus.active : status
        let item = ContextItem(
            id: UUID().uuidString,
            title: title.trimmingCharacters(in: .whitespacesAndNewlines),
            notes: notes,
            todos: todos,
            startAt: startAt,
            endAt: endAt,
            sessions: [],
            status: initialStatus,
            createdAt: now,
            closedAt: nil,
            colorTag: colorTag,
            isBanner: isBanner
        )
        contexts.insert(item, at: 0)
        save()
        return item.id
    }

    func updateContext(
        id: String,
        title: String? = nil,
        notes: String? = nil,
        todos: [TodoItem]? = nil,
        startAt: Date? = nil,
        endAt: Date? = nil,
        status: ContextStatus? = nil,
        colorTag: BarColorTag? = nil,
        isBanner: Bool? = nil
    ) {
        guard let index = contexts.firstIndex(where: { $0.id == id }) else { return }
        var c = contexts[index]
        if let title = title { c.title = title.trimmingCharacters(in: .whitespacesAndNewlines) }
        if let notes = notes { c.notes = notes }
        if let todos = todos { c.todos = todos }
        if let startAt = startAt { c.startAt = startAt }
        if let endAt = endAt { c.endAt = endAt }
        if let colorTag = colorTag { c.colorTag = colorTag }
        if let isBanner = isBanner { c.isBanner = isBanner }
        if let status = status {
            if focusContextId == id && (status == .done || status == .dropped) {
                stopFocus()
                if let refreshedIndex = contexts.firstIndex(where: { $0.id == id }) {
                    c = contexts[refreshedIndex]
                }
            }
            c.status = status
            if status == .done || status == .dropped {
                c.closedAt = Date()
            } else {
                c.closedAt = nil
            }
        }
        contexts[index] = c
        save()
    }

    func toggleDone(id: String) {
        guard let item = contexts.first(where: { $0.id == id }) else { return }
        if item.status == .done {
            updateContext(id: id, status: .active)
        } else {
            updateContext(id: id, status: .done)
        }
    }

    func toggleParked(id: String) {
        guard let item = contexts.first(where: { $0.id == id }) else { return }
        if item.status == .paused {
            updateContext(id: id, status: .active)
        } else {
            updateContext(id: id, status: .paused)
        }
    }

    func moveToDay(id: String, date: Date) {
        guard let index = contexts.firstIndex(where: { $0.id == id }) else { return }
        contexts[index].startAt = date
        save()
    }

    func rescheduleTime(id: String, newStartAt: Date, newEndAt: Date? = nil) {
        guard let index = contexts.firstIndex(where: { $0.id == id }) else { return }
        let oldStart = contexts[index].startAt
        contexts[index].startAt = newStartAt
        if let newEnd = newEndAt {
            contexts[index].endAt = newEnd
        } else if let oldEnd = contexts[index].endAt {
            let duration = oldEnd.timeIntervalSince(oldStart)
            contexts[index].endAt = newStartAt.addingTimeInterval(max(900, duration))
        }
        save()
    }

    func setStatus(id: String, status: ContextStatus) {
        updateContext(id: id, status: status)
    }

    func removeContext(id: String) {
        if focusContextId == id { stopFocus() }
        contexts.removeAll { $0.id == id }
        save()
    }

    func focusOn(id: String) {
        if focusContextId == id { return }
        stopFocus()
        setStatus(id: id, status: .active)
        focusContextId = id
        focusStartedAt = Date()
    }

    func stopFocus() {
        guard let id = focusContextId, let startedAt = focusStartedAt else { return }
        let session = FocusSession(start: startedAt, end: Date())
        if let index = contexts.firstIndex(where: { $0.id == id }) {
            contexts[index].sessions.append(session)
        }
        focusContextId = nil
        focusStartedAt = nil
        save()
    }

    // MARK: - Biometric Security

    func authenticateWithBiometrics(reason: String = "Unlock Stuck to access your cognitive contexts.", completion: @escaping (Bool) -> Void) {
        let context = LAContext()
        var error: NSError?

        if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
            context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { success, _ in
                DispatchQueue.main.async {
                    if success {
                        self.isLocked = false
                    }
                    completion(success)
                }
            }
        } else {
            // Simulator or hardware unavailable: unlock directly
            self.isLocked = false
            completion(true)
        }
    }

    func unlockWithBiometrics(completion: @escaping (Bool) -> Void) {
        authenticateWithBiometrics(reason: "Unlock Stuck to access your cognitive contexts.", completion: completion)
    }

    // MARK: - Seed Sample Data (Matching the Stacked Bars visual)

    func seedSampleContexts() {
        let today = Date()
        let cal = Calendar.current
        let yesterday = cal.date(byAdding: .day, value: -1, to: today) ?? today
        let tomorrow = cal.date(byAdding: .day, value: 1, to: today) ?? today

        let sample: [ContextItem] = [
            ContextItem(
                title: "System Architecture Handover",
                notes: "Executive milestone and sprint review",
                startAt: today,
                status: .active,
                colorTag: .cyan,
                isBanner: true
            ),
            ContextItem(
                title: "[PH] All Saints' Day",
                notes: "Holiday observance and market holiday",
                startAt: today,
                status: .active,
                colorTag: .green
            ),
            ContextItem(
                title: "Air PDAM",
                notes: "Monthly utility payment confirmation",
                startAt: today,
                status: .done,
                colorTag: .blue
            ),
            ContextItem(
                title: "Bayar BPJS",
                notes: "Health insurance premium deduction",
                startAt: today,
                status: .done,
                colorTag: .blue
            ),
            ContextItem(
                title: "Bayar IPL",
                notes: "Apartment maintenance fee payment",
                startAt: today,
                status: .done,
                colorTag: .blue
            ),
            ContextItem(
                title: "Hock Yao on Leave",
                notes: "Engineering handover for sprint review",
                startAt: tomorrow,
                status: .planned,
                colorTag: .purple
            ),
            ContextItem(
                title: "[PH, BR] All Souls",
                notes: "Regional holiday calendar entry",
                startAt: tomorrow,
                status: .planned,
                colorTag: .green
            ),
            ContextItem(
                title: "Yohanzen on Leave",
                notes: "Mobile core detection coverage",
                startAt: tomorrow,
                status: .planned,
                colorTag: .purple
            ),
            ContextItem(
                title: "Refactor Memory Profiler",
                notes: "Parked thought: trace heap allocation spikes",
                startAt: yesterday,
                status: .paused,
                colorTag: .charcoal
            )
        ]
        self.contexts = sample
    }

    // MARK: - Persistence

    private struct PersistedData: Codable {
        let profile: Profile?
        let contexts: [ContextItem]
        let limit: Int
    }

    private func save() {
        let data = PersistedData(profile: profile, contexts: contexts, limit: limit)
        if let encoded = try? JSONEncoder().encode(data) {
            UserDefaults.standard.set(encoded, forKey: storageKey)
        }
    }

    private func loadFromDisk() {
        guard let data = UserDefaults.standard.data(forKey: storageKey),
              let decoded = try? JSONDecoder().decode(PersistedData.self, from: data) else {
            return
        }
        self.profile = decoded.profile
        self.contexts = decoded.contexts
        self.limit = decoded.limit
        self.isLocked = decoded.profile?.faceId ?? false
    }
}
