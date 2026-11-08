import SwiftUI

struct ContextSheetView: View {
    @ObservedObject var store: ContextStore
    let contextId: String?
    var onDismiss: (() -> Void)? = nil
    @Environment(\.dismiss) private var dismiss

    @State private var title: String = ""
    @State private var notes: String = ""
    @State private var status: ContextStatus = .active
    @State private var todos: [TodoItem] = []
    @State private var newTodoText: String = ""
    @State private var scheduledDate: Date = Date()
    @State private var hasEndTime: Bool = false
    @State private var endDate: Date = Date().addingTimeInterval(3600)
    @State private var colorTag: BarColorTag = .blue
    @State private var isBanner: Bool = false

    init(
        store: ContextStore,
        contextId: String?,
        initialStatus: ContextStatus = .active,
        initialDate: Date = Date(),
        initialEndDate: Date? = nil,
        initialTitle: String = "",
        initialIsBanner: Bool = false,
        onDismiss: (() -> Void)? = nil
    ) {
        self.store = store
        self.contextId = contextId
        self.onDismiss = onDismiss
        self._title = State(initialValue: initialTitle)
        self._isBanner = State(initialValue: initialIsBanner)
        self._status = State(initialValue: initialStatus)
        self._scheduledDate = State(initialValue: initialDate)
        if let end = initialEndDate {
            self._hasEndTime = State(initialValue: true)
            self._endDate = State(initialValue: end)
        } else {
            self._hasEndTime = State(initialValue: false)
            self._endDate = State(initialValue: initialDate.addingTimeInterval(3600))
        }
        let defaultColor: BarColorTag
        switch initialStatus {
        case .active: defaultColor = .blue
        case .planned: defaultColor = .purple
        case .paused: defaultColor = .charcoal
        case .done: defaultColor = .green
        case .dropped: defaultColor = .charcoal
        }
        self._colorTag = State(initialValue: defaultColor)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            // DLS Custom Popup Header
            HStack(alignment: .center) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(contextId == nil ? "New Thought" : "Edit Thought")
                        .font(AppFont.titleLarge)
                        .foregroundStyle(Palette.inkPrimary)

                    Text("Define cognitive load and schedule")
                        .font(AppFont.labelSmall)
                        .foregroundStyle(Palette.inkMuted)
                }

                Spacer()

                Button {
                    dismissAction()
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundStyle(Palette.inkSecondary)
                        .frame(width: 32, height: 32)
                        .background(Palette.surface2)
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 20)
            .padding(.top, 6)

            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    // Title Field
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Title")
                            .font(AppFont.labelSmall)
                            .foregroundStyle(Palette.inkSecondary)
                        CalmTextField(placeholder: "e.g. System Handover, Health Checkup, Deep Work...", text: $title)
                    }

                    // Cognitive Stage Selector (Color strictly represents stage)
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Cognitive Stage (Color strictly bound to stage)")
                            .font(AppFont.labelSmall)
                            .foregroundStyle(Palette.inkSecondary)

                        HStack(spacing: 8) {
                            ForEach(ContextStatus.allCases.filter { $0 != .dropped }) { st in
                                let isSelected = status == st
                                Button {
                                    status = st
                                    switch st {
                                    case .active: colorTag = .blue
                                    case .planned: colorTag = .purple
                                    case .paused: colorTag = .charcoal
                                    case .done: colorTag = .green
                                    case .dropped: colorTag = .charcoal
                                    }
                                } label: {
                                    HStack(spacing: 5) {
                                        Circle()
                                            .fill(stageColor(st))
                                            .frame(width: 8, height: 8)
                                        Text(st.title)
                                            .font(AppFont.labelSmall)
                                            .foregroundStyle(isSelected ? Color.white : Palette.inkPrimary)
                                    }
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 9)
                                    .background(isSelected ? stageColor(st) : Palette.surface2)
                                    .clipShape(RoundedRectangle(cornerRadius: Radius.sm, style: .continuous))
                                    .overlay(
                                        RoundedRectangle(cornerRadius: Radius.sm, style: .continuous)
                                            .stroke(isSelected ? Color.clear : Palette.line, lineWidth: 1)
                                    )
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }

                    // Scheduled Day & Time
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Scheduled Date & Time")
                            .font(AppFont.labelSmall)
                            .foregroundStyle(Palette.inkSecondary)
                        DatePicker("Date on Timeline", selection: $scheduledDate, displayedComponents: [.date, .hourAndMinute])
                            .font(AppFont.bodyMedium)
                            .foregroundStyle(Palette.inkPrimary)

                        Toggle("Specify End Time / Duration", isOn: $hasEndTime)
                            .font(AppFont.labelSmall)
                            .foregroundStyle(Palette.inkSecondary)
                            .tint(Color(hex: "29B6F6"))
                            .padding(.top, 2)

                        if hasEndTime {
                            DatePicker("End Time", selection: $endDate, displayedComponents: [.hourAndMinute])
                                .font(AppFont.bodyMedium)
                                .foregroundStyle(Palette.inkPrimary)
                        }
                    }

                    // Spanning Banner Option
                    Toggle("Spanning Project Banner", isOn: $isBanner)
                        .font(AppFont.bodyMedium)
                        .foregroundStyle(Palette.inkPrimary)
                        .tint(Color(hex: "29B6F6"))

                    // Notes
                    VStack(alignment: .leading, spacing: 6) {
                        Text("Notes & Details")
                            .font(AppFont.labelSmall)
                            .foregroundStyle(Palette.inkSecondary)
                        TextEditor(text: $notes)
                            .font(AppFont.bodyMedium)
                            .foregroundStyle(Palette.inkPrimary)
                            .padding(8)
                            .frame(minHeight: 70)
                            .background(Palette.card)
                            .clipShape(RoundedRectangle(cornerRadius: Radius.md, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: Radius.md, style: .continuous)
                                    .stroke(Palette.line, lineWidth: 1)
                            )
                    }

                    // Todo Checklist
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Checklist")
                            .font(AppFont.labelSmall)
                            .foregroundStyle(Palette.inkSecondary)

                        ForEach(todos.indices, id: \.self) { idx in
                            HStack(spacing: 10) {
                                Button {
                                    todos[idx].isDone.toggle()
                                } label: {
                                    Image(systemName: todos[idx].isDone ? "checkmark.circle.fill" : "circle")
                                        .font(.system(size: 20))
                                        .foregroundStyle(todos[idx].isDone ? StatePalette.calm.ink : Palette.inkMuted)
                                }

                                Text(todos[idx].text)
                                    .font(AppFont.bodyMedium)
                                    .foregroundStyle(todos[idx].isDone ? Palette.inkMuted : Palette.inkPrimary)
                                    .strikethrough(todos[idx].isDone)

                                Spacer()

                                Button {
                                    todos.remove(at: idx)
                                } label: {
                                    Image(systemName: "trash")
                                        .font(.system(size: 14))
                                        .foregroundStyle(StatePalette.alert.ink)
                                }
                            }
                            .padding(.vertical, 4)
                        }

                        // Add new todo
                        HStack(spacing: 8) {
                            CalmTextField(placeholder: "Add checklist item...", text: $newTodoText)
                            Button {
                                let trimmed = newTodoText.trimmingCharacters(in: .whitespacesAndNewlines)
                                if !trimmed.isEmpty {
                                    todos.append(TodoItem(text: trimmed))
                                    newTodoText = ""
                                }
                            } label: {
                                Text("Add")
                                    .font(AppFont.labelMedium)
                                    .foregroundStyle(Palette.onCharcoal)
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 14)
                                    .background(Palette.charcoal)
                                    .clipShape(RoundedRectangle(cornerRadius: Radius.md, style: .continuous))
                            }
                        }
                    }

                    // Actions
                    VStack(spacing: 10) {
                        PillButton(title: contextId == nil ? "Save Context" : "Update Context") {
                            save()
                        }

                        if let id = contextId {
                            CoralOutlinedPillButton(title: "Delete this context") {
                                store.removeContext(id: id)
                                dismissAction()
                            }
                        }
                    }
                    .padding(.top, 10)
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 24)
            }
            .frame(maxHeight: UIScreen.main.bounds.height * 0.70)
        }
        .onAppear {
            loadExisting()
        }
    }

    private func dismissAction() {
        if let onDismiss = onDismiss {
            onDismiss()
        } else {
            dismiss()
        }
    }

    private func stageColor(_ status: ContextStatus) -> Color {
        switch status {
        case .active: return Color(hex: "4B8BF5")
        case .planned: return Color(hex: "A765BA")
        case .paused: return Color(hex: "F59E0B")
        case .done: return Color(hex: "7FB844")
        case .dropped: return Palette.inkMuted
        }
    }

    private func loadExisting() {
        guard let id = contextId, let item = store.contexts.first(where: { $0.id == id }) else { return }
        title = item.title
        notes = item.notes
        status = item.status
        todos = item.todos
        scheduledDate = item.startAt
        if let end = item.endAt {
            hasEndTime = true
            endDate = end
        } else {
            hasEndTime = false
            endDate = item.startAt.addingTimeInterval(3600)
        }
        colorTag = item.colorTag
        isBanner = item.isBanner
    }

    private func save() {
        let trimmed = title.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        let endParam = hasEndTime ? endDate : nil
        if let id = contextId {
            store.updateContext(
                id: id,
                title: trimmed,
                notes: notes,
                todos: todos,
                startAt: scheduledDate,
                endAt: endParam,
                status: status,
                colorTag: colorTag,
                isBanner: isBanner
            )
        } else {
            _ = store.addContext(
                title: trimmed,
                notes: notes,
                todos: todos,
                startAt: scheduledDate,
                endAt: endParam,
                status: status,
                colorTag: colorTag,
                isBanner: isBanner
            )
        }
        dismissAction()
    }
}
