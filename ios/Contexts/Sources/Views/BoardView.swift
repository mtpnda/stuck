import SwiftUI
import AudioToolbox
import UIKit

enum TimeGranularity: Int, CaseIterable, Identifiable {
    case day = 0
    case sixHours = 1
    case threeHours = 2
    case oneHour = 3
    case thirtyMins = 4
    case tenMins = 5
    case oneMin = 6

    var id: Int { rawValue }

    var label: String {
        switch self {
        case .day: return "Day"
        case .sixHours: return "6 hrs"
        case .threeHours: return "3 hrs"
        case .oneHour: return "1 hr"
        case .thirtyMins: return "30 mins"
        case .tenMins: return "10 mins"
        case .oneMin: return "1 min"
        }
    }

    var slotHeight: CGFloat {
        switch self {
        case .day: return 0
        case .sixHours: return 48
        case .threeHours: return 52
        case .oneHour: return 56
        case .thirtyMins: return 84
        case .tenMins: return 136
        case .oneMin: return 210
        }
    }

    /// Default duration in minutes for this zoom level
    var defaultDurationMinutes: Int {
        switch self {
        case .day: return 24 * 60
        case .sixHours: return 6 * 60
        case .threeHours: return 3 * 60
        case .oneHour: return 60
        case .thirtyMins: return 30
        case .tenMins: return 10
        case .oneMin: return 1
        }
    }

    /// Snap step in hours for drag-to-create
    var snapStepHours: Double {
        switch self {
        case .day: return 24.0
        case .sixHours: return 6.0
        case .threeHours: return 3.0
        case .oneHour: return 1.0
        case .thirtyMins: return 0.5
        case .tenMins: return 10.0 / 60.0
        case .oneMin: return 1.0 / 60.0
        }
    }
}

struct SchedulePlaceholder: Equatable {
    let date: Date
    let hour: Int?
    let minute: Int
    let durationMinutes: Int
    let status: ContextStatus
    let colorTag: BarColorTag

    var timeRangeString: String {
        if let h = hour {
            let startMin = minute
            let totalEnd = h * 60 + startMin + durationMinutes
            let endH = (totalEnd / 60) % 24
            let endM = totalEnd % 60
            return String(format: "%02d:%02d – %02d:%02d", h, startMin, endH, endM)
        } else {
            return "All Day"
        }
    }

    var durationBadge: String {
        if durationMinutes < 60 {
            return "\(durationMinutes)m"
        } else if durationMinutes % 60 == 0 {
            return "\(durationMinutes / 60)h"
        } else {
            return String(format: "%.1fh", Double(durationMinutes) / 60.0)
        }
    }

    func startDate(calendar: Calendar = .current) -> Date {
        if let h = hour {
            var comp = calendar.dateComponents([.year, .month, .day], from: date)
            comp.hour = h
            comp.minute = minute
            comp.second = 0
            return calendar.date(from: comp) ?? date
        } else {
            return calendar.startOfDay(for: date)
        }
    }

    func endDate(calendar: Calendar = .current) -> Date {
        if hour != nil {
            return startDate(calendar: calendar).addingTimeInterval(TimeInterval(durationMinutes * 60))
        } else {
            let start = calendar.startOfDay(for: date)
            return calendar.date(byAdding: .day, value: 1, to: start) ?? start.addingTimeInterval(86400)
        }
    }
}

struct BoardView: View {
    @ObservedObject var store: ContextStore
    @State private var activeQuickActionItem: ContextItem? = nil
    @State private var editingContextId: String? = nil
    @State private var isCreatingNew: Bool
    @State private var isShowingProfile: Bool
    @State private var selectedStageFilter: ContextStatus? = nil
    @State private var quickThoughtText: String = ""
    @State private var selectedDay: Date = Calendar.current.startOfDay(for: Date())
    @State private var activePlaceholder: SchedulePlaceholder? = nil
    @State private var newThoughtConfig: (status: ContextStatus, startDate: Date, endDate: Date?, title: String, isBanner: Bool)? = nil

    private func openCreateForm(status: ContextStatus, startDate: Date, endDate: Date?, title: String = "", isBanner: Bool = false) {
        newThoughtConfig = (status: status, startDate: startDate, endDate: endDate, title: title, isBanner: isBanner)
        activePlaceholder = nil
        isCreatingNew = true
    }

    private var quickCaptureContext: (status: ContextStatus, colorTag: BarColorTag, scheduledDate: Date) {
        ContextStore.resolveQuickCaptureContext(
            selectedFilter: selectedStageFilter,
            targetDate: selectedDay
        )
    }

    private var quickCapturePlaceholder: String {
        if let filter = selectedStageFilter {
            return "Capture into \(filter.title) (use // for note)..."
        } else if Calendar.current.startOfDay(for: selectedDay) > Calendar.current.startOfDay(for: Date()) {
            let f = DateFormatter()
            f.dateFormat = "EEE"
            return "Capture for \(f.string(from: selectedDay)) (use // for note)..."
        } else {
            return "Quick capture thought (use // for note)..."
        }
    }

    private func stageColor(_ status: ContextStatus) -> Color {
        switch status {
        case .active: return Color(hex: "4B8BF5")
        case .planned: return Color(hex: "A765BA")
        case .paused: return Color(hex: "F59E0B")
        case .done: return Color(hex: "7FB844")
        case .dropped: return Palette.surface2
        }
    }

    init(store: ContextStore) {
        self.store = store
        self._isCreatingNew = State(initialValue: CommandLine.arguments.contains("-newsheet"))
        self._isShowingProfile = State(initialValue: CommandLine.arguments.contains("-profile"))
    }

    // Pinch-to-zoom timeline state (Day -> 6 hrs -> 3 hrs -> 1 hr -> 30 mins -> 10 mins -> 1 min)
    @State private var granularityIndex: Int = 3 // default: 1 hr
    @State private var accumulatedPinchScale: CGFloat = 1.0

    private var currentGranularity: TimeGranularity {
        TimeGranularity(rawValue: granularityIndex) ?? .oneHour
    }

    private var calendarDays: [Date] {
        let cal = Calendar.current
        let today = cal.startOfDay(for: Date())
        return (-2...5).compactMap { cal.date(byAdding: .day, value: $0, to: today) }
    }

    var body: some View {
        ScrollViewReader { scrollProxy in
            ZStack(alignment: .bottom) {
                VStack(spacing: 0) {
                    // Top Header Bar
                    headerBar

                    // Stage Legend Strip & Zoom Granularity Stepper
                    legendAndZoomHeader

                    // Horizontal Day Quick-Jump Selector Strip
                    dayQuickJumpStrip(scrollProxy: scrollProxy)

                    // Unified Vertical Scrolling Timeline (Supports Pinch-to-Zoom)
                    ScrollView(.vertical, showsIndicators: true) {
                        LazyVStack(spacing: 16) {
                            ForEach(calendarDays, id: \.self) { day in
                                verticalDaySection(for: day)
                                    .id(dayId(for: day))
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.top, 10)
                        .padding(.bottom, 20)
                    }
                    .simultaneousGesture(
                        MagnificationGesture()
                            .onChanged { scale in
                                let delta = scale / accumulatedPinchScale
                                if delta > 1.18 {
                                    // Zoom in: finer resolution (towards 1 min)
                                    if granularityIndex < TimeGranularity.allCases.count - 1 {
                                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                        withAnimation(.spring(response: 0.32, dampingFraction: 0.82)) {
                                            granularityIndex += 1
                                        }
                                    }
                                    accumulatedPinchScale = scale
                                } else if delta < 0.84 {
                                    // Zoom out: broader resolution (towards Day)
                                    if granularityIndex > 0 {
                                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                        withAnimation(.spring(response: 0.32, dampingFraction: 0.82)) {
                                            granularityIndex -= 1
                                        }
                                    }
                                    accumulatedPinchScale = scale
                                }
                            }
                            .onEnded { _ in
                                accumulatedPinchScale = 1.0
                            }
                    )

                    // Bottom Quick Capture Bar (Eliminates dead space)
                    bottomQuickCaptureBar
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Palette.bg)
                .onAppear {
                    let today = Calendar.current.startOfDay(for: Date())
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                        withAnimation(.easeInOut) {
                            scrollProxy.scrollTo(dayId(for: today), anchor: .top)
                        }
                    }
                }

                // 100% Custom DLS Modal Overlay for "New Thought"
                DlsModalPopup(isPresented: $isCreatingNew) {
                    ContextSheetView(
                        store: store,
                        contextId: nil,
                        initialStatus: newThoughtConfig?.status ?? quickCaptureContext.status,
                        initialDate: newThoughtConfig?.startDate ?? quickCaptureContext.scheduledDate,
                        initialEndDate: newThoughtConfig?.endDate,
                        initialTitle: newThoughtConfig?.title ?? "",
                        initialIsBanner: newThoughtConfig?.isBanner ?? false,
                        onDismiss: {
                            isCreatingNew = false
                            newThoughtConfig = nil
                        }
                    )
                }

                // 100% Custom DLS Modal Overlay for "Edit Thought"
                DlsModalPopup(isPresented: Binding(
                    get: { editingContextId != nil },
                    set: { if !$0 { editingContextId = nil } }
                )) {
                    if let id = editingContextId {
                        ContextSheetView(store: store, contextId: id, onDismiss: {
                            editingContextId = nil
                        })
                    }
                }

                // 100% Custom DLS Modal Overlay for "Profile & Limits"
                DlsModalPopup(isPresented: $isShowingProfile) {
                    ProfileView(store: store, onDismiss: {
                        isShowingProfile = false
                    })
                }
            }
        }
        // 100% Custom DLS Action Popup Overlay
        .dlsActionPopup(item: $activeQuickActionItem) { item in
            DlsContextActionPopup(
                store: store,
                item: item,
                onEdit: {
                    let id = item.id
                    activeQuickActionItem = nil
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                        editingContextId = id
                    }
                },
                onDismiss: {
                    activeQuickActionItem = nil
                }
            )
        }
    }

    // MARK: - Header Bar

    private var headerBar: some View {
        HStack(alignment: .center, spacing: 12) {
            // User Avatar & App Title
            Button {
                isShowingProfile = true
            } label: {
                HStack(spacing: 10) {
                    EmojiAvatar(text: store.profile?.initials ?? "U", size: 38)

                    VStack(alignment: .leading, spacing: 2) {
                        Text("Stuck")
                            .font(AppFont.titleLarge)
                            .foregroundStyle(Palette.inkPrimary)

                        HStack(spacing: 5) {
                            Circle()
                                .fill(store.isOverLimit ? StatePalette.alert.ink : StatePalette.calm.ink)
                                .frame(width: 6, height: 6)

                            Text("\(store.activeCount) of \(store.limit) in your head")
                                .font(AppFont.labelSmall)
                                .foregroundStyle(store.isOverLimit ? StatePalette.alert.ink : Palette.inkSecondary)
                        }
                    }
                }
            }
            .buttonStyle(.plain)

            Spacer()

            // Plus New Thought Button
            Button {
                let dur = currentGranularity.defaultDurationMinutes * 60
                openCreateForm(
                    status: quickCaptureContext.status,
                    startDate: quickCaptureContext.scheduledDate,
                    endDate: quickCaptureContext.scheduledDate.addingTimeInterval(TimeInterval(dur))
                )
            } label: {
                Image(systemName: "plus")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(Palette.onCharcoal)
                    .frame(width: 38, height: 38)
                    .background(Palette.actionPrimary)
                    .clipShape(Circle())
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
        .padding(.bottom, 6)
    }

    // MARK: - Legend Bar with Zoom Granularity Stepper

    private var legendAndZoomHeader: some View {
        HStack(alignment: .center, spacing: 8) {
            // Zoom Stepper Pill: [ − 1 hr + ] (Supports tap + pinch)
            HStack(spacing: 4) {
                Button {
                    if granularityIndex > 0 {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        withAnimation(.spring(response: 0.28, dampingFraction: 0.8)) {
                            granularityIndex -= 1
                        }
                    }
                } label: {
                    Image(systemName: "minus")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundStyle(granularityIndex > 0 ? Palette.inkPrimary : Palette.inkMuted.opacity(0.4))
                        .frame(width: 24, height: 24)
                }
                .disabled(granularityIndex == 0)

                Text(currentGranularity.label)
                    .font(AppFont.labelSmall)
                    .foregroundStyle(Palette.inkPrimary)
                    .frame(minWidth: 48)

                Button {
                    if granularityIndex < TimeGranularity.allCases.count - 1 {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        withAnimation(.spring(response: 0.28, dampingFraction: 0.8)) {
                            granularityIndex += 1
                        }
                    }
                } label: {
                    Image(systemName: "plus")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundStyle(granularityIndex < TimeGranularity.allCases.count - 1 ? Palette.inkPrimary : Palette.inkMuted.opacity(0.4))
                        .frame(width: 24, height: 24)
                }
                .disabled(granularityIndex == TimeGranularity.allCases.count - 1)
            }
            .padding(.horizontal, 6)
            .padding(.vertical, 4)
            .background(Palette.surfaceCard)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(Palette.borderSubtle, lineWidth: 1))

            Spacer()

            // Stage Legend Filter Pills (Strict Stage Colors)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 5) {
                    legendPill(status: .active, title: "In head", count: store.activeCount, color: Color(hex: "4B8BF5"))
                    legendPill(status: .planned, title: "Coming", count: store.contexts.filter { $0.status == .planned }.count, color: Color(hex: "A765BA"))
                    legendPill(status: .paused, title: "Parked", count: store.pausedContexts.count, color: Color(hex: "F59E0B"))
                    legendPill(status: .done, title: "Closed", count: store.contexts.filter { $0.status == .done }.count, color: Color(hex: "7FB844"))
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 4)
    }

    private func legendPill(status: ContextStatus, title: String, count: Int, color: Color) -> some View {
        let isSelected = selectedStageFilter == status
        return Button {
            withAnimation(.snappy) {
                if selectedStageFilter == status {
                    selectedStageFilter = nil
                } else {
                    selectedStageFilter = status
                }
            }
        } label: {
            HStack(spacing: 5) {
                Circle()
                    .fill(color)
                    .frame(width: 7, height: 7)

                Text(title)
                    .font(AppFont.labelSmall)
                    .foregroundStyle(isSelected ? Palette.onCharcoal : Palette.inkPrimary)

                Text("\(count)")
                    .font(AppFont.labelSmall)
                    .foregroundStyle(isSelected ? Palette.onCharcoal.opacity(0.8) : Palette.inkMuted)
                    .padding(.horizontal, 5)
                    .padding(.vertical, 1)
                    .background(isSelected ? Color.white.opacity(0.2) : Palette.surfaceRaised)
                    .clipShape(Capsule())
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 5)
            .background(isSelected ? Palette.actionPrimary : Palette.surfaceCard)
            .clipShape(Capsule())
            .overlay(
                Capsule()
                    .stroke(isSelected ? Color.clear : Palette.borderSubtle, lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }

    // MARK: - Day Quick Jump Strip

    private func dayQuickJumpStrip(scrollProxy: ScrollViewProxy) -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 6) {
                ForEach(calendarDays, id: \.self) { day in
                    let isToday = Calendar.current.isDateInToday(day)
                    let isSelected = Calendar.current.isDate(day, inSameDayAs: selectedDay)
                    Button {
                        selectedDay = day
                        withAnimation(.easeInOut(duration: 0.35)) {
                            scrollProxy.scrollTo(dayId(for: day), anchor: .top)
                        }
                    } label: {
                        HStack(spacing: 4) {
                            Text(dayOfWeekString(day).uppercased())
                                .font(AppFont.labelSmall)
                            Text(dayNumberString(day))
                                .font(.system(size: 13, weight: .bold))
                            if isToday && !isSelected {
                                Circle()
                                    .fill(Color(hex: "29B6F6"))
                                    .frame(width: 4, height: 4)
                            }
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(isSelected ? Palette.actionPrimary : Palette.surfaceCard)
                        .foregroundStyle(isSelected ? Palette.onCharcoal : (isToday ? Color(hex: "0288D1") : Palette.inkPrimary))
                        .clipShape(Capsule())
                        .overlay(
                            Capsule()
                                .stroke(isSelected ? Color.clear : (isToday ? Color(hex: "29B6F6").opacity(0.5) : Palette.borderSubtle), lineWidth: 1)
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 5)
        }
        .background(Palette.surfaceRaised.opacity(0.6))
    }

    // MARK: - Vertical Day Section

    private func verticalDaySection(for date: Date) -> some View {
        let cal = Calendar.current
        let isToday = cal.isDateInToday(date)
        var dayItems = store.contextsFor(date: date)
        if let filter = selectedStageFilter {
            dayItems = dayItems.filter { $0.status == filter }
        }

        let banners = dayItems.filter { $0.isBanner }
        let regulars = dayItems.filter { !$0.isBanner }

        return VStack(alignment: .leading, spacing: 8) {
            // Day Header Row
            HStack(alignment: .center) {
                HStack(spacing: 8) {
                    Text(dayFullHeaderString(date))
                        .font(AppFont.titleMedium)
                        .foregroundStyle(Palette.inkPrimary)

                    if isToday {
                        Text("TODAY")
                            .font(.system(size: 9.5, weight: .bold))
                            .foregroundStyle(Color(hex: "0288D1"))
                            .padding(.horizontal, 7)
                            .padding(.vertical, 2)
                            .background(Color(hex: "29B6F6").opacity(0.18))
                            .clipShape(Capsule())
                    }
                }

                Spacer()

                Text("\(dayItems.count) thoughts")
                    .font(AppFont.labelSmall)
                    .foregroundStyle(Palette.inkMuted)
            }
            .padding(.bottom, 2)

            // Spanning Project Banners
            ForEach(banners) { banner in
                BannerBarView(item: banner) {
                    activeQuickActionItem = banner
                }
            }

            // Timeline Schedule Body: Day mode (compact stack) vs Hourly/Minute mode (expanded vertical grid)
            if currentGranularity == .day {
                // Compact Day Stack (Zoomed out: Day overview, zero dead space!)
                VStack(spacing: 6) {
                    ForEach(regulars) { item in
                        DlsStackedBar(
                            item: item,
                            onIconTap: {
                                store.toggleDone(id: item.id)
                            },
                            onBodyTap: {
                                activeQuickActionItem = item
                            },
                            onReschedule: { newDate in
                                store.rescheduleTime(id: item.id, newStartAt: newDate)
                            }
                        )
                    }

                    if dayItems.isEmpty {
                        emptyDaySlot(for: date)
                    }
                }
            } else {
                // Hourly / Minute Expanded Vertical Timeline Grid
                expandedTimeGrid(for: date, items: regulars, isToday: isToday)
            }
        }
        .padding(14)
        .background(Palette.surfaceCard)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .stroke(isToday ? Color(hex: "29B6F6").opacity(0.4) : Palette.borderSubtle, lineWidth: isToday ? 1.5 : 1)
        )
    }

    // MARK: - Expanded Time Grid (6 hrs, 3 hrs, 1 hr, 30 mins, 10 mins, 1 min)

    private func expandedTimeGrid(for date: Date, items: [ContextItem], isToday: Bool) -> some View {
        let cal = Calendar.current
        let hours = hoursForGranularity()
        let slotHeight = currentGranularity.slotHeight

        return VStack(spacing: 0) {
            ForEach(hours, id: \.self) { hour in
                let matchingItems = items.filter {
                    let itemHour = cal.component(.hour, from: $0.startAt)
                    if currentGranularity == .sixHours {
                        return itemHour >= hour && itemHour < hour + 6
                    } else if currentGranularity == .threeHours {
                        return itemHour >= hour && itemHour < hour + 3
                    } else {
                        return itemHour == hour
                    }
                }

                VStack(spacing: 0) {
                    // Live Time Cursor Line on Today
                    if isToday && hour == cal.component(.hour, from: Date()) {
                        HStack(spacing: 4) {
                            Circle()
                                .fill(Color(hex: "29B6F6"))
                                .frame(width: 6, height: 6)
                            Rectangle()
                                .fill(Color(hex: "29B6F6"))
                                .frame(height: 1.5)
                            Text(currentLiveTimeString())
                                .font(.system(size: 9.5, weight: .bold, design: .monospaced))
                                .foregroundStyle(Color(hex: "29B6F6"))
                        }
                        .padding(.vertical, 2)
                    }

                    HStack(alignment: .top, spacing: 10) {
                        // Time Marker Label
                        Text(formatHourLabel(hour))
                            .font(.system(size: 11, weight: .semibold, design: .monospaced))
                            .foregroundStyle(Palette.inkMuted)
                            .frame(width: 44, alignment: .trailing)
                            .padding(.top, 4)

                        // Slot Area with Guidelines
                        VStack(alignment: .leading, spacing: 4) {
                            if let placeholder = activePlaceholder,
                               placeholder.hour == hour,
                               cal.isDate(placeholder.date, inSameDayAs: date) {
                                PlaceholderBarView(
                                    placeholder: placeholder,
                                    granularity: currentGranularity,
                                    onConfirm: {
                                        openCreateForm(
                                            status: placeholder.status,
                                            startDate: placeholder.startDate(),
                                            endDate: placeholder.endDate(),
                                            title: "Thought at \(formatHourLabel(hour))"
                                        )
                                    },
                                    onHoldCreate: {
                                        openCreateForm(
                                            status: placeholder.status,
                                            startDate: placeholder.startDate(),
                                            endDate: placeholder.endDate(),
                                            title: "Thought at \(formatHourLabel(hour))"
                                        )
                                    },
                                    onDismiss: {
                                        activePlaceholder = nil
                                    }
                                )
                            } else if matchingItems.isEmpty {
                                let isFuture = cal.startOfDay(for: date) > cal.startOfDay(for: Date())
                                let targetStatus = selectedStageFilter ?? (isFuture ? ContextStatus.planned : ContextStatus.active)
                                let targetColor: BarColorTag = (targetStatus == .planned) ? .purple : (targetStatus == .paused ? .charcoal : (targetStatus == .done ? .green : .blue))

                                TimelineEmptySlotView(
                                    date: date,
                                    hour: hour,
                                    slotHeight: max(40, slotHeight),
                                    granularity: currentGranularity,
                                    targetStatus: targetStatus,
                                    targetColor: targetColor,
                                    onQuickTap: {
                                        activePlaceholder = SchedulePlaceholder(
                                            date: date,
                                            hour: hour,
                                            minute: 0,
                                            durationMinutes: currentGranularity.defaultDurationMinutes,
                                            status: targetStatus,
                                            colorTag: targetColor
                                        )
                                    },
                                    onHoldCreate: { startDate, endDate in
                                        openCreateForm(
                                            status: targetStatus,
                                            startDate: startDate,
                                            endDate: endDate,
                                            title: "Thought at \(formatHourLabel(hour))"
                                        )
                                    },
                                    onDragCreate: { startDate, endDate in
                                        openCreateForm(
                                            status: targetStatus,
                                            startDate: startDate,
                                            endDate: endDate,
                                            title: "Thought at \(formatHourLabel(hour))"
                                        )
                                    }
                                )
                            } else {
                                ForEach(matchingItems) { item in
                                    DlsStackedBar(
                                        item: item,
                                        slotHeight: max(40, slotHeight),
                                        onIconTap: {
                                            store.toggleDone(id: item.id)
                                        },
                                        onBodyTap: {
                                            activeQuickActionItem = item
                                        },
                                        onReschedule: { newDate in
                                            store.rescheduleTime(id: item.id, newStartAt: newDate)
                                        }
                                    )
                                }
                            }

                            // Subdivisions for 30 mins, 10 mins, 1 min
                            if currentGranularity == .thirtyMins {
                                HStack {
                                    Text(":30")
                                        .font(.system(size: 9, design: .monospaced))
                                        .foregroundStyle(Palette.inkMuted.opacity(0.5))
                                    Rectangle()
                                        .fill(Palette.line.opacity(0.35))
                                        .frame(height: 1)
                                }
                                .padding(.top, 6)
                            } else if currentGranularity == .tenMins {
                                ForEach([10, 20, 30, 40, 50], id: \.self) { m in
                                    HStack {
                                        Text(":\(m)")
                                            .font(.system(size: 8.5, design: .monospaced))
                                            .foregroundStyle(Palette.inkMuted.opacity(0.4))
                                        Rectangle()
                                            .fill(Palette.line.opacity(0.25))
                                            .frame(height: 1)
                                    }
                                    .padding(.top, 2)
                                }
                            } else if currentGranularity == .oneMin {
                                // 1 Min Precision View: Minute ruler lines & detail badges
                                ForEach([5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55], id: \.self) { m in
                                    HStack {
                                        Text(String(format: ":%02d", m))
                                            .font(.system(size: 8, design: .monospaced))
                                            .foregroundStyle(Palette.inkMuted.opacity(0.35))
                                        Rectangle()
                                            .fill(Palette.line.opacity(0.2))
                                            .frame(height: 0.8)
                                    }
                                    .padding(.top, 1)
                                }
                            }
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .frame(minHeight: slotHeight)
                    }
                }
            }
        }
    }

    private func hoursForGranularity() -> [Int] {
        switch currentGranularity {
        case .day:
            return []
        case .sixHours:
            return [0, 6, 12, 18]
        case .threeHours:
            return [6, 9, 12, 15, 18, 21]
        case .oneHour, .thirtyMins, .tenMins, .oneMin:
            return Array(7...22)
        }
    }

    private func formatHourLabel(_ hour: Int) -> String {
        String(format: "%02d:00", hour)
    }

    private func currentLiveTimeString() -> String {
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        return f.string(from: Date())
    }

    private func emptyDaySlot(for date: Date) -> some View {
        let isFuture = Calendar.current.startOfDay(for: date) > Calendar.current.startOfDay(for: Date())
        let defaultStatus = selectedStageFilter ?? (isFuture ? ContextStatus.planned : ContextStatus.active)
        let defaultColor: BarColorTag = (defaultStatus == .planned) ? .purple : (defaultStatus == .paused ? .charcoal : (defaultStatus == .done ? .green : .blue))

        return Group {
            if let placeholder = activePlaceholder,
               placeholder.hour == nil,
               Calendar.current.isDate(placeholder.date, inSameDayAs: date) {
                PlaceholderBarView(
                    placeholder: placeholder,
                    granularity: currentGranularity,
                    onConfirm: {
                        openCreateForm(
                            status: placeholder.status,
                            startDate: placeholder.startDate(),
                            endDate: placeholder.endDate(),
                            title: "New thought"
                        )
                    },
                    onHoldCreate: {
                        openCreateForm(
                            status: placeholder.status,
                            startDate: placeholder.startDate(),
                            endDate: placeholder.endDate(),
                            title: "New thought"
                        )
                    },
                    onDismiss: {
                        activePlaceholder = nil
                    }
                )
            } else {
                Button {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    activePlaceholder = SchedulePlaceholder(
                        date: date,
                        hour: nil,
                        minute: 0,
                        durationMinutes: 24 * 60,
                        status: defaultStatus,
                        colorTag: defaultColor
                    )
                } label: {
                    HStack(spacing: 6) {
                        Image(systemName: "plus")
                            .font(.system(size: 11, weight: .bold))
                        Text("Add thought (\(defaultStatus.title))")
                            .font(AppFont.labelSmall)
                    }
                    .foregroundStyle(Palette.inkMuted.opacity(0.7))
                    .frame(maxWidth: .infinity)
                    .frame(height: 38)
                    .background(Palette.surface2.opacity(0.5))
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .strokeBorder(style: StrokeStyle(lineWidth: 1, dash: [4, 4]))
                            .foregroundStyle(Palette.borderSubtle)
                    )
                }
                .buttonStyle(.plain)
                .simultaneousGesture(
                    LongPressGesture(minimumDuration: 0.22).onEnded { _ in
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        let start = Calendar.current.startOfDay(for: date)
                        let end = Calendar.current.date(byAdding: .day, value: 1, to: start)
                        openCreateForm(
                            status: defaultStatus,
                            startDate: start,
                            endDate: end,
                            title: "New thought"
                        )
                    }
                )
            }
        }
    }

    // MARK: - Bottom Quick Capture Bar

    private var bottomQuickCaptureBar: some View {
        let ctx = quickCaptureContext
        return HStack(spacing: 10) {
            HStack(spacing: 8) {
                // Cognitive Stage Indicator Badge (Zero-Guesswork Clarity)
                HStack(spacing: 4) {
                    Circle()
                        .fill(stageColor(ctx.status))
                        .frame(width: 7, height: 7)
                    Text(ctx.status.title)
                        .font(.system(size: 10.5, weight: .bold))
                        .foregroundStyle(Palette.inkSecondary)
                }
                .padding(.horizontal, 7)
                .padding(.vertical, 4)
                .background(Palette.surfaceRaised)
                .clipShape(Capsule())

                TextField(quickCapturePlaceholder, text: $quickThoughtText)
                    .font(AppFont.bodyMedium)
                    .foregroundStyle(Palette.inkPrimary)
                    .onSubmit {
                        commitQuickThought()
                    }
            }
            .padding(.horizontal, 10)
            .frame(height: 44)
            .background(Palette.surfaceCard)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(Palette.borderSubtle, lineWidth: 1))

            Button {
                commitQuickThought()
            } label: {
                Text("Add")
                    .font(AppFont.labelMedium)
                    .foregroundStyle(Palette.onCharcoal)
                    .padding(.horizontal, 16)
                    .frame(height: 44)
                    .background(Palette.actionPrimary)
                    .clipShape(Capsule())
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
        .background(Palette.surfaceRaised)
        .overlay(
            Rectangle()
                .fill(Palette.borderSubtle)
                .frame(height: 1),
            alignment: .top
        )
    }

    private func commitQuickThought() {
        let trimmed = quickThoughtText.trimmingCharacters(in: .whitespacesAndNewlines)
        if !trimmed.isEmpty {
            store.quickCapture(
                title: trimmed,
                targetDate: selectedDay,
                stageFilter: selectedStageFilter
            )
            quickThoughtText = ""
        }
    }

    // MARK: - Date Helpers

    private func dayId(for date: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        return f.string(from: date)
    }

    private func dayOfWeekString(_ date: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "EEE"
        return f.string(from: date)
    }

    private func dayNumberString(_ date: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "d"
        return f.string(from: date)
    }

    private func dayFullHeaderString(_ date: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "EEEE, MMM d"
        return f.string(from: date)
    }
}

// MARK: - Draft Placeholder Bar View (Tap to Confirm / Hold to Create)

struct PlaceholderBarView: View {
    let placeholder: SchedulePlaceholder
    let granularity: TimeGranularity
    let onConfirm: () -> Void
    let onHoldCreate: () -> Void
    let onDismiss: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            // Dashed status indicator
            Circle()
                .strokeBorder(style: StrokeStyle(lineWidth: 1.5, dash: [3, 3]))
                .foregroundStyle(Color(hex: "29B6F6"))
                .frame(width: 14, height: 14)

            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text("Draft Placeholder")
                        .font(.system(size: 12, weight: .bold, design: .rounded))
                        .foregroundStyle(Palette.inkPrimary)

                    Text(placeholder.timeRangeString)
                        .font(.system(size: 9.5, weight: .bold, design: .monospaced))
                        .foregroundStyle(Color(hex: "0288D1"))
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color(hex: "29B6F6").opacity(0.18))
                        .clipShape(Capsule())
                }

                Text("Tap to edit & confirm • Hold to create event (\(granularity.label))")
                    .font(.system(size: 9.5, weight: .medium))
                    .foregroundStyle(Palette.inkSecondary)
            }

            Spacer(minLength: 0)

            // Create Button
            Button {
                onConfirm()
            } label: {
                Text("Create")
                    .font(AppFont.labelSmall)
                    .foregroundStyle(Palette.onCharcoal)
                    .padding(.horizontal, 9)
                    .padding(.vertical, 4)
                    .background(Palette.actionPrimary)
                    .clipShape(Capsule())
            }
            .buttonStyle(.plain)

            // Close / Dismiss Button
            Button {
                onDismiss()
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundStyle(Palette.inkMuted)
                    .frame(width: 24, height: 24)
                    .background(Palette.surfaceRaised)
                    .clipShape(Circle())
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(hex: "29B6F6").opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 9))
        .overlay(
            RoundedRectangle(cornerRadius: 9)
                .strokeBorder(style: StrokeStyle(lineWidth: 1.5, dash: [5, 4]))
                .foregroundStyle(Color(hex: "29B6F6").opacity(0.7))
        )
        .contentShape(Rectangle())
        .onTapGesture {
            onConfirm()
        }
        .simultaneousGesture(
            LongPressGesture(minimumDuration: 0.22).onEnded { _ in
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                onHoldCreate()
            }
        )
    }
}

// MARK: - Drag-to-Create Timeline Slot & Box View (Exact Match to User Brief)

struct DragCreateBoxView: View {
    let startHour: Double
    let endHour: Double
    let slotHeight: CGFloat
    let stageColor: Color

    var height: CGFloat {
        max(36, CGFloat(max(0.016, endHour - startHour)) * slotHeight)
    }

    var body: some View {
        ZStack(alignment: .topLeading) {
            // Outlined Box with translucent cyan tint (Matching User Brief)
            RoundedRectangle(cornerRadius: 8, style: .continuous)
                .fill(Color(hex: "29B6F6").opacity(0.12))
                .overlay(
                    RoundedRectangle(cornerRadius: 8, style: .continuous)
                        .stroke(Color(hex: "29B6F6"), lineWidth: 2)
                )
                .frame(maxWidth: .infinity)
                .frame(height: height)

            // Content inside Box
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 5) {
                    Circle()
                        .fill(Color(hex: "29B6F6"))
                        .frame(width: 7, height: 7)
                    Text(timeRangeText)
                        .font(.system(size: 11, weight: .bold, design: .monospaced))
                        .foregroundStyle(Palette.inkPrimary)

                    Spacer()

                    Text(durationText)
                        .font(.system(size: 10, weight: .semibold, design: .monospaced))
                        .foregroundStyle(Color(hex: "0288D1"))
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color(hex: "29B6F6").opacity(0.2))
                        .clipShape(Capsule())
                }

                Text("Release to review form & confirm")
                    .font(AppFont.labelSmall)
                    .foregroundStyle(Palette.inkSecondary)
            }
            .padding(10)

            // Top-Left Circular Handle (Matching User's Screenshot)
            handleKnob
                .offset(x: -8, y: -8)

            // Bottom-Right Circular Handle (Matching User's Screenshot)
            GeometryReader { geo in
                handleKnob
                    .position(x: geo.size.width - 4, y: height)
            }
        }
        .frame(height: height)
    }

    private var handleKnob: some View {
        ZStack {
            // Dark outer halo disc
            Circle()
                .fill(Color(hex: "1C1814").opacity(0.85))
                .frame(width: 22, height: 22)
                .overlay(
                    Circle()
                        .stroke(Color(hex: "29B6F6").opacity(0.7), lineWidth: 1.5)
                )
                .shadow(color: Color.black.opacity(0.25), radius: 3, x: 0, y: 1)

            // Cyan core pip
            Circle()
                .fill(Color(hex: "29B6F6"))
                .frame(width: 10, height: 10)
        }
    }

    private var timeRangeText: String {
        let sh = Int(startHour)
        let sm = Int(round((startHour.truncatingRemainder(dividingBy: 1)) * 60))
        let eh = Int(endHour) % 24
        let em = Int(round((endHour.truncatingRemainder(dividingBy: 1)) * 60)) % 60
        return String(format: "%02d:%02d – %02d:%02d", sh, sm, eh, em)
    }

    private var durationText: String {
        let dur = endHour - startHour
        let totalMinutes = Int(round(dur * 60))
        if totalMinutes < 60 {
            return "\(max(1, totalMinutes))m"
        } else if totalMinutes % 60 == 0 {
            return "\(totalMinutes / 60)h"
        } else {
            return String(format: "%.1fh", dur)
        }
    }
}

struct TimelineEmptySlotView: View {
    let date: Date
    let hour: Int
    let slotHeight: CGFloat
    let granularity: TimeGranularity
    let targetStatus: ContextStatus
    let targetColor: BarColorTag
    let onQuickTap: () -> Void
    let onHoldCreate: (Date, Date) -> Void
    let onDragCreate: (Date, Date) -> Void

    @State private var isDragging: Bool = false
    @State private var dragOffsetY: CGFloat = 0
    @State private var startHourDouble: Double = 0
    @State private var endHourDouble: Double = 0

    private func makeDate(hourDouble: Double) -> Date {
        let cal = Calendar.current
        var comp = cal.dateComponents([.year, .month, .day], from: date)
        comp.hour = Int(hourDouble) % 24
        comp.minute = Int(round((hourDouble.truncatingRemainder(dividingBy: 1)) * 60)) % 60
        comp.second = 0
        return cal.date(from: comp) ?? date
    }

    var body: some View {
        ZStack(alignment: .topLeading) {
            if isDragging {
                DragCreateBoxView(
                    startHour: startHourDouble,
                    endHour: endHourDouble,
                    slotHeight: slotHeight,
                    stageColor: Color(hex: "29B6F6")
                )
                .zIndex(50)
            } else {
                // Calm dashed guideline with subtle hint
                HStack(spacing: 8) {
                    Rectangle()
                        .fill(Palette.line.opacity(0.6))
                        .frame(height: 1)

                    Text("Tap for placeholder • Hold to create (\(granularity.label))")
                        .font(.system(size: 9, weight: .medium, design: .rounded))
                        .foregroundStyle(Palette.inkMuted.opacity(0.45))

                    Rectangle()
                        .fill(Palette.line.opacity(0.6))
                        .frame(height: 1)
                }
                .frame(height: max(20, slotHeight * 0.45))
                .contentShape(Rectangle())
            }
        }
        .frame(height: isDragging ? CGFloat(max(0.016, endHourDouble - startHourDouble)) * slotHeight : max(20, slotHeight * 0.45))
        .contentShape(Rectangle())
        .gesture(
            LongPressGesture(minimumDuration: 0.22)
                .sequenced(before: DragGesture(coordinateSpace: .local))
                .onChanged { value in
                    switch value {
                    case .second(true, let drag):
                        if let drag = drag {
                            if !isDragging {
                                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                withAnimation(.spring(response: 0.25, dampingFraction: 0.8)) {
                                    isDragging = true
                                    startHourDouble = Double(hour)
                                    let defHours = Double(granularity.defaultDurationMinutes) / 60.0
                                    endHourDouble = Double(hour) + defHours
                                }
                            }
                            dragOffsetY = drag.translation.height
                            let stepHours = granularity.snapStepHours
                            let rawHours = dragOffsetY / max(30, slotHeight)
                            let snappedAdditional = max(stepHours, round(rawHours / stepHours) * stepHours)
                            endHourDouble = Double(hour) + snappedAdditional
                        }
                    default:
                        break
                    }
                }
                .onEnded { value in
                    if isDragging {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        let defHours = Double(granularity.defaultDurationMinutes) / 60.0
                        let finalEnd = endHourDouble > startHourDouble ? endHourDouble : (Double(hour) + defHours)
                        let startDate = makeDate(hourDouble: startHourDouble)
                        let endDate = makeDate(hourDouble: finalEnd)
                        onDragCreate(startDate, endDate)
                        withAnimation(.spring(response: 0.25, dampingFraction: 0.8)) {
                            isDragging = false
                            dragOffsetY = 0
                        }
                    } else {
                        switch value {
                        case .first(true), .second(true, _):
                            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                            let defHours = Double(granularity.defaultDurationMinutes) / 60.0
                            let startDate = makeDate(hourDouble: Double(hour))
                            let endDate = makeDate(hourDouble: Double(hour) + defHours)
                            onHoldCreate(startDate, endDate)
                        default:
                            break
                        }
                    }
                }
        )
        .highPriorityGesture(
            TapGesture().onEnded {
                if !isDragging {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    onQuickTap()
                }
            }
        )
    }
}

// MARK: - DLS Stacked Bar (Strict Stage Color Differentiated & Hold-Draggable)

struct DlsStackedBar: View {
    let item: ContextItem
    var slotHeight: CGFloat = 56
    let onIconTap: () -> Void
    let onBodyTap: () -> Void
    var onReschedule: ((Date) -> Void)? = nil

    @State private var dragOffsetY: CGFloat = 0
    @State private var isDragging: Bool = false

    var body: some View {
        HStack(spacing: 8) {
            // Status Icon (Leading Tap Target)
            Button {
                onIconTap()
            } label: {
                if item.status == .done {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(iconColor)
                } else if item.status == .paused {
                    Image(systemName: "pause.circle.fill")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(iconColor)
                } else {
                    Circle()
                        .strokeBorder(iconColor.opacity(0.85), lineWidth: 1.5)
                        .frame(width: 14, height: 14)
                }
            }
            .buttonStyle(.plain)

            // Title
            Text(item.title)
                .font(.system(size: 13, weight: .semibold, design: .rounded))
                .foregroundColor(textColor)
                .strikethrough(item.status == .done, color: textColor.opacity(0.6))
                .lineLimit(1)
                .truncationMode(.tail)

            Spacer(minLength: 0)

            // Duration Time Badge if endAt is set
            if let end = item.endAt {
                Text(formatTimeRange(start: item.startAt, end: end))
                    .font(.system(size: 9.5, weight: .bold, design: .monospaced))
                    .foregroundStyle(textColor.opacity(0.85))
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Color.black.opacity(0.12))
                    .clipShape(Capsule())
            }

            // Subtle drag handle grip dots when active/unclosed
            if item.status != .done {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundStyle(textColor.opacity(0.4))
                    .padding(.trailing, 2)
            }
        }
        .padding(.horizontal, 10)
        .frame(height: 38)
        .background(stageBackground)
        .clipShape(RoundedRectangle(cornerRadius: 9))
        .contentShape(Rectangle())
        .offset(y: dragOffsetY)
        .scaleEffect(isDragging ? 1.04 : 1.0)
        .shadow(color: isDragging ? Color.black.opacity(0.3) : Color.clear, radius: 10, y: 5)
        .zIndex(isDragging ? 100 : 1)
        .overlay(alignment: .top) {
            if isDragging {
                HStack(spacing: 5) {
                    Image(systemName: "clock.arrow.2.circlepath")
                        .font(.system(size: 9, weight: .bold))
                    Text("Move to \(formatTargetTime(offsetY: dragOffsetY))")
                        .font(.system(size: 10.5, weight: .bold, design: .monospaced))
                }
                .foregroundStyle(Palette.onCharcoal)
                .padding(.horizontal, 9)
                .padding(.vertical, 4)
                .background(Palette.actionPrimary)
                .clipShape(Capsule())
                .shadow(color: Color.black.opacity(0.25), radius: 4, y: 2)
                .offset(y: -30)
                .transition(.scale.combined(with: .opacity))
            }
        }
        .gesture(
            LongPressGesture(minimumDuration: 0.22)
                .sequenced(before: DragGesture(coordinateSpace: .local))
                .onChanged { value in
                    switch value {
                    case .second(true, let drag):
                        if let drag = drag {
                            if !isDragging {
                                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                withAnimation(.spring(response: 0.25, dampingFraction: 0.75)) {
                                    isDragging = true
                                }
                            }
                            dragOffsetY = drag.translation.height
                        }
                    default:
                        break
                    }
                }
                .onEnded { value in
                    if isDragging {
                        let step = max(30, slotHeight)
                        let deltaHours = round(dragOffsetY / step)
                        if deltaHours != 0, let onReschedule = onReschedule {
                            let newDate = Calendar.current.date(byAdding: .hour, value: Int(deltaHours), to: item.startAt) ?? item.startAt
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            onReschedule(newDate)
                        }
                        withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                            dragOffsetY = 0
                            isDragging = false
                        }
                    }
                }
        )
        .highPriorityGesture(
            TapGesture().onEnded {
                if !isDragging {
                    onBodyTap()
                }
            }
        )
    }

    private func formatTargetTime(offsetY: CGFloat) -> String {
        let step = max(30, slotHeight)
        let deltaHours = Int(round(offsetY / step))
        let targetDate = Calendar.current.date(byAdding: .hour, value: deltaHours, to: item.startAt) ?? item.startAt
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        return f.string(from: targetDate)
    }

    private func formatTimeRange(start: Date, end: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        return "\(f.string(from: start)) – \(f.string(from: end))"
    }

    /// Background color strictly derived from stage
    private var stageBackground: Color {
        switch item.status {
        case .active:
            return Color(hex: "4B8BF5") // Vibrant In-Head Blue
        case .planned:
            return Color(hex: "A765BA") // Refined Coming-Up Purple
        case .paused:
            return Color(hex: "F59E0B") // Parked Warm Amber
        case .done:
            return Color(hex: "7FB844") // Closed Sage Green
        case .dropped:
            return Palette.surface2
        }
    }

    private var textColor: Color {
        switch item.status {
        case .active:
            return Color.black.opacity(0.9)
        case .planned:
            return Color.white
        case .paused:
            return Color.black.opacity(0.9)
        case .done:
            return Color.black.opacity(0.88)
        case .dropped:
            return Palette.inkMuted
        }
    }

    private var iconColor: Color {
        textColor
    }
}

// MARK: - Banner Bar View (Spanning Outlined Project Bar)

struct BannerBarView: View {
    let item: ContextItem
    let onTap: () -> Void

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: "folder.fill")
                .font(.system(size: 11))
                .foregroundStyle(Color(hex: "29B6F6"))

            Text(item.title)
                .font(.system(size: 12, weight: .bold, design: .rounded))
                .foregroundStyle(Color(hex: "29B6F6"))
                .lineLimit(1)
                .truncationMode(.tail)

            Spacer(minLength: 0)
        }
        .padding(.horizontal, 8)
        .frame(height: 34)
        .background(Color(hex: "29B6F6").opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: 7))
        .overlay(
            RoundedRectangle(cornerRadius: 7)
                .stroke(Color(hex: "29B6F6"), lineWidth: 1.5)
        )
        .onTapGesture {
            onTap()
        }
    }
}

// MARK: - Custom DLS Action Popup (100% Tokenized DLS Modal Container)

struct DlsContextActionPopup: View {
    @ObservedObject var store: ContextStore
    let item: ContextItem
    let onEdit: () -> Void
    let onDismiss: () -> Void

    @State private var quickNoteInput: String = ""
    @State private var isShowingNoteEditor: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            // Header Row
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 6) {
                    // Status Badge with Stage Color
                    HStack(spacing: 5) {
                        Circle()
                            .fill(statusColor(item.status))
                            .frame(width: 8, height: 8)
                        Text(item.status.title)
                            .font(AppFont.labelSmall)
                            .foregroundStyle(Palette.inkPrimary)
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(statusColor(item.status).opacity(0.2))
                    .clipShape(Capsule())

                    Text(item.title)
                        .font(AppFont.titleLarge)
                        .foregroundStyle(Palette.inkPrimary)
                        .lineLimit(2)
                }

                Spacer()

                Button {
                    onDismiss()
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundStyle(Palette.inkSecondary)
                        .frame(width: 32, height: 32)
                        .background(Palette.surface2)
                        .clipShape(Circle())
                }
            }

            // Quick Note Section (Instant Notes viewing & rapid inline capture)
            VStack(alignment: .leading, spacing: 6) {
                if !item.notes.isEmpty && !isShowingNoteEditor {
                    HStack(alignment: .top, spacing: 6) {
                        Image(systemName: "note.text")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundStyle(Palette.inkMuted)
                            .padding(.top, 2)

                        Text(item.notes)
                            .font(AppFont.bodySmall)
                            .foregroundStyle(Palette.inkSecondary)
                            .lineLimit(4)

                        Spacer()

                        Button {
                            quickNoteInput = item.notes
                            isShowingNoteEditor = true
                        } label: {
                            Image(systemName: "pencil")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundStyle(Palette.inkMuted)
                        }
                    }
                    .padding(10)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Palette.surfaceRaised)
                    .clipShape(RoundedRectangle(cornerRadius: Radius.sm))
                } else if isShowingNoteEditor || item.notes.isEmpty {
                    HStack(spacing: 6) {
                        Image(systemName: "note.text")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundStyle(Palette.inkMuted)

                        TextField(item.notes.isEmpty ? "Quick add note..." : "Edit note...", text: $quickNoteInput)
                            .font(AppFont.bodySmall)
                            .textFieldStyle(.plain)
                            .onSubmit {
                                saveQuickNote()
                            }

                        if !quickNoteInput.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                            Button {
                                saveQuickNote()
                            } label: {
                                Text("Save")
                                    .font(AppFont.labelSmall)
                                    .foregroundStyle(Palette.onCharcoal)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 4)
                                    .background(Palette.actionPrimary)
                                    .clipShape(Capsule())
                            }
                        }
                    }
                    .padding(.horizontal, 10)
                    .frame(height: 38)
                    .background(Palette.surfaceCard)
                    .clipShape(RoundedRectangle(cornerRadius: Radius.sm))
                    .overlay(RoundedRectangle(cornerRadius: Radius.sm).stroke(Palette.borderSubtle, lineWidth: 1))
                }
            }

            // Quick Stage Selector (Color-Differentiated)
            VStack(alignment: .leading, spacing: 8) {
                Text("COGNITIVE STAGE", comment: "")
                    .font(AppFont.labelSmall)
                    .foregroundStyle(Palette.inkMuted)

                HStack(spacing: 8) {
                    stageButton(status: .active, title: "In Head", color: Color(hex: "4B8BF5"))
                    stageButton(status: .planned, title: "Coming", color: Color(hex: "A765BA"))
                    stageButton(status: .paused, title: "Park", color: Color(hex: "F59E0B"))
                    stageButton(status: .done, title: "Done ✓", color: Color(hex: "7FB844"))
                }
            }

            // Quick Reschedule Days Row
            VStack(alignment: .leading, spacing: 8) {
                Text("RESCHEDULE TIMELINE", comment: "")
                    .font(AppFont.labelSmall)
                    .foregroundStyle(Palette.inkMuted)

                HStack(spacing: 8) {
                    rescheduleButton(title: "‹ Yesterday", daysOffset: -1)
                    rescheduleButton(title: "Today", daysOffset: 0)
                    rescheduleButton(title: "Tomorrow ›", daysOffset: 1)
                }
            }

            Divider()
                .background(Palette.line)

            // Primary & Secondary Action Buttons
            VStack(spacing: 10) {
                Button {
                    store.focusOn(id: item.id)
                    onDismiss()
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "play.fill")
                            .font(.system(size: 12))
                        Text("Start Focus Timer")
                            .font(AppFont.labelMedium)
                    }
                    .foregroundStyle(Palette.onCharcoal)
                    .frame(maxWidth: .infinity)
                    .frame(height: 44)
                    .background(Palette.charcoal)
                    .clipShape(RoundedRectangle(cornerRadius: Radius.md, style: .continuous))
                }

                HStack(spacing: 10) {
                    Button {
                        onEdit()
                    } label: {
                        HStack(spacing: 6) {
                            Image(systemName: "pencil")
                                .font(.system(size: 12))
                            Text("Edit Details")
                                .font(AppFont.labelMedium)
                        }
                        .foregroundStyle(Palette.inkPrimary)
                        .frame(maxWidth: .infinity)
                        .frame(height: 42)
                        .background(Palette.surface2)
                        .clipShape(RoundedRectangle(cornerRadius: Radius.md, style: .continuous))
                    }

                    Button {
                        store.removeContext(id: item.id)
                        onDismiss()
                    } label: {
                        HStack(spacing: 6) {
                            Image(systemName: "trash")
                                .font(.system(size: 12))
                            Text("Delete")
                                .font(AppFont.labelMedium)
                        }
                        .foregroundStyle(StatePalette.alert.ink)
                        .frame(maxWidth: .infinity)
                        .frame(height: 42)
                        .background(StatePalette.alert.soft)
                        .clipShape(RoundedRectangle(cornerRadius: Radius.md, style: .continuous))
                    }
                }
            }
        }
        .padding(.horizontal, 20)
        .padding(.top, 14)
        .padding(.bottom, 20)
        .background(Palette.card)
    }

    private func stageButton(status: ContextStatus, title: String, color: Color) -> some View {
        let isCurrent = item.status == status
        return Button {
            store.setStatus(id: item.id, status: status)
            onDismiss()
        } label: {
            HStack(spacing: 5) {
                Circle()
                    .fill(color)
                    .frame(width: 7, height: 7)
                Text(title)
                    .font(AppFont.labelSmall)
            }
            .foregroundStyle(isCurrent ? Color.white : Palette.inkPrimary)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 9)
            .background(isCurrent ? color : Palette.surface2)
            .clipShape(RoundedRectangle(cornerRadius: Radius.sm, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: Radius.sm, style: .continuous)
                    .stroke(isCurrent ? Color.clear : Palette.line, lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }

    private func rescheduleButton(title: String, daysOffset: Int) -> some View {
        Button {
            let cal = Calendar.current
            let target = cal.date(byAdding: .day, value: daysOffset, to: Date()) ?? Date()
            store.moveToDay(id: item.id, date: target)
            onDismiss()
        } label: {
            Text(title)
                .font(AppFont.bodySmall)
                .foregroundStyle(Palette.inkSecondary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
                .background(Palette.surface2)
                .clipShape(RoundedRectangle(cornerRadius: Radius.sm, style: .continuous))
        }
        .buttonStyle(.plain)
    }

    private func statusColor(_ status: ContextStatus) -> Color {
        switch status {
        case .active: return Color(hex: "4B8BF5")
        case .planned: return Color(hex: "A765BA")
        case .paused: return Color(hex: "F59E0B")
        case .done: return Color(hex: "7FB844")
        case .dropped: return Palette.inkMuted
        }
    }

    private func saveQuickNote() {
        let trimmed = quickNoteInput.trimmingCharacters(in: .whitespacesAndNewlines)
        store.updateContext(id: item.id, notes: trimmed)
        isShowingNoteEditor = false
        quickNoteInput = ""
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
    }
}
