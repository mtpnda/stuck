import SwiftUI

/// BioCue DLS standard SoftCard
struct SoftCard<Content: View>: View {
    let padding: CGFloat
    let content: Content

    init(padding: CGFloat = 16, @ViewBuilder content: () -> Content) {
        self.padding = padding
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            content
        }
        .padding(padding)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Palette.card)
        .clipShape(RoundedRectangle(cornerRadius: Radius.card, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: Radius.card, style: .continuous)
                .stroke(Palette.line, lineWidth: 1)
        )
        .shadow(color: DlsTokens.shadowTint.opacity(0.06), radius: 4, x: 0, y: 2)
    }
}

/// BioCue Primary Action Pill Button
struct PillButton: View {
    let title: String
    var loading: Bool = false
    var disabled: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if loading {
                    ProgressView()
                        .tint(Palette.onCharcoal)
                }
                Text(title)
                    .font(AppFont.labelLarge)
                    .foregroundStyle(Palette.onCharcoal)
            }
            .frame(maxWidth: .infinity)
            .frame(height: Metrics.buttonHeight)
            .background(Palette.charcoal)
            .clipShape(Capsule())
            .opacity(disabled ? 0.5 : 1)
        }
        .disabled(disabled || loading)
    }
}

/// BioCue Outlined Pill Button (Secondary Action)
struct OutlinedPillButton: View {
    let title: String
    var disabled: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(AppFont.labelLarge)
                .foregroundStyle(Palette.inkPrimary)
                .frame(maxWidth: .infinity)
                .frame(height: Metrics.buttonHeight)
                .background(Palette.card)
                .clipShape(Capsule())
                .overlay(
                    Capsule()
                        .stroke(Palette.lineStrong, lineWidth: 1.5)
                )
                .opacity(disabled ? 0.5 : 1)
        }
        .disabled(disabled)
    }
}

/// BioCue Coral Outlined Pill Button (Destructive / Caution)
struct CoralOutlinedPillButton: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(AppFont.labelLarge)
                .foregroundStyle(StatePalette.alert.ink)
                .frame(maxWidth: .infinity)
                .frame(height: Metrics.buttonHeight)
                .background(Palette.card)
                .clipShape(Capsule())
                .overlay(
                    Capsule()
                        .stroke(StatePalette.alert.chip, lineWidth: 1.5)
                )
        }
    }
}

/// BioCue Ghost Button
struct GhostButton: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(AppFont.bodySmall)
                .foregroundStyle(Palette.inkSecondary)
                .frame(minHeight: Metrics.minTouch)
        }
    }
}

/// BioCue Status Pill (Calm / Rising / Alert / Info / Neutral)
struct StatusPill: View {
    let palette: StatePalette
    let label: String

    var body: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(palette.chip)
                .frame(width: 8, height: 8)
            Text(label)
                .font(AppFont.labelMedium)
                .foregroundStyle(palette.ink)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 4)
        .background(palette.soft)
        .clipShape(Capsule())
        .overlay(
            Capsule()
                .stroke(palette.chip.opacity(0.6), lineWidth: 1)
        )
    }
}

/// BioCue Inline Notice (Accessible alert / guidance callout)
struct InlineNotice: View {
    let text: String
    var tone: StatePalette = .alert

    var body: some View {
        HStack(spacing: 8) {
            Text(text)
                .font(AppFont.bodyMedium)
                .foregroundStyle(Palette.inkPrimary)
                .multilineTextAlignment(.leading)
            Spacer()
        }
        .padding(14)
        .background(tone.soft)
        .clipShape(RoundedRectangle(cornerRadius: Radius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: Radius.md, style: .continuous)
                .stroke(tone.chip, lineWidth: 1)
        )
    }
}

/// BioCue Calm Text Field
struct CalmTextField: View {
    let placeholder: String
    @Binding var text: String
    var isSecure: Bool = false

    var body: some View {
        Group {
            if isSecure {
                SecureField(placeholder, text: $text)
            } else {
                TextField(placeholder, text: $text)
            }
        }
        .font(AppFont.bodyLarge)
        .foregroundStyle(Palette.inkPrimary)
        .padding(.horizontal, 16)
        .frame(height: 52)
        .background(Palette.card)
        .clipShape(RoundedRectangle(cornerRadius: Radius.md, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: Radius.md, style: .continuous)
                .stroke(Palette.line, lineWidth: 1)
        )
    }
}

/// BioCue Emoji or Initial Avatar
struct EmojiAvatar: View {
    let text: String
    var size: CGFloat = 44

    var body: some View {
        ZStack {
            Palette.surface2
            Text(text)
                .font(.system(size: size * 0.45, weight: .bold))
                .foregroundStyle(Palette.inkPrimary)
        }
        .frame(width: size, height: size)
        .clipShape(Circle())
        .overlay(
            Circle()
                .stroke(Palette.line, lineWidth: 1)
        )
    }
}

/// BioCue Section Header
struct SectionHeader: View {
    let title: String

    var body: some View {
        Text(title.uppercased())
            .font(AppFont.labelSmall)
            .foregroundStyle(Palette.inkSecondary)
            .tracking(0.5)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, 8)
    }
}

/// BioCue DLS Cognitive Capacity Gauge Meter
struct DlsCapacityMeter: View {
    let count: Int
    let limit: Int

    var isOverLimit: Bool { count > limit }

    var body: some View {
        HStack(spacing: 4) {
            ForEach(0..<limit, id: \.self) { idx in
                RoundedRectangle(cornerRadius: 2)
                    .fill(idx < count ? (isOverLimit ? StatePalette.alert.ink : StatePalette.calm.ink) : Palette.lineStrong)
                    .frame(width: 14, height: 4)
            }
            if isOverLimit {
                ForEach(0..<(count - limit), id: \.self) { _ in
                    RoundedRectangle(cornerRadius: 2)
                        .fill(StatePalette.alert.ink)
                        .frame(width: 14, height: 4)
                }
            }
        }
    }
}

/// BioCue Custom DLS Popup Overlay Container
/// 100% custom tokenized popup replacing system sheets and native alerts.
struct DlsModalPopup<Content: View>: View {
    @Binding var isPresented: Bool
    let content: Content

    init(isPresented: Binding<Bool>, @ViewBuilder content: () -> Content) {
        self._isPresented = isPresented
        self.content = content()
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            if isPresented {
                // Dimmed Backdrop Scrim
                Color.black.opacity(0.45)
                    .ignoresSafeArea()
                    .transition(.opacity)
                    .onTapGesture {
                        withAnimation(.spring(response: 0.32, dampingFraction: 0.85)) {
                            isPresented = false
                        }
                    }

                // Custom DLS Modal Card
                VStack(spacing: 0) {
                    Capsule()
                        .fill(Palette.lineStrong)
                        .frame(width: 38, height: 4)
                        .padding(.top, 10)
                        .padding(.bottom, 6)

                    content
                }
                .frame(maxWidth: .infinity)
                .background(Palette.card)
                .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 24, style: .continuous)
                        .stroke(Palette.line, lineWidth: 1)
                )
                .shadow(color: DlsTokens.shadowTint.opacity(0.22), radius: 24, x: 0, y: -4)
                .padding(.horizontal, 12)
                .padding(.bottom, 12)
                .transition(.asymmetric(
                    insertion: .move(edge: .bottom).combined(with: .opacity),
                    removal: .move(edge: .bottom).combined(with: .opacity)
                ))
            }
        }
        .animation(.spring(response: 0.35, dampingFraction: 0.84), value: isPresented)
    }
}

/// View modifier for custom DLS popup with identifiable items
extension View {
    func dlsActionPopup<Item: Identifiable, Content: View>(
        item: Binding<Item?>,
        @ViewBuilder content: @escaping (Item) -> Content
    ) -> some View {
        ZStack(alignment: .bottom) {
            self

            if let activeItem = item.wrappedValue {
                Color.black.opacity(0.45)
                    .ignoresSafeArea()
                    .transition(.opacity)
                    .onTapGesture {
                        withAnimation(.spring(response: 0.32, dampingFraction: 0.85)) {
                            item.wrappedValue = nil
                        }
                    }

                VStack(spacing: 0) {
                    Capsule()
                        .fill(Palette.lineStrong)
                        .frame(width: 38, height: 4.5)
                        .padding(.top, 10)
                        .padding(.bottom, 6)

                    content(activeItem)
                }
                .frame(maxWidth: .infinity)
                .background(Palette.card)
                .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 24, style: .continuous)
                        .stroke(Palette.line, lineWidth: 1)
                )
                .shadow(color: DlsTokens.shadowTint.opacity(0.22), radius: 24, x: 0, y: -4)
                .padding(.horizontal, 12)
                .padding(.bottom, 14)
                .transition(.asymmetric(
                    insertion: .move(edge: .bottom).combined(with: .opacity),
                    removal: .move(edge: .bottom).combined(with: .opacity)
                ))
                .zIndex(100)
            }
        }
        .animation(.spring(response: 0.35, dampingFraction: 0.84), value: item.wrappedValue != nil)
    }
}
