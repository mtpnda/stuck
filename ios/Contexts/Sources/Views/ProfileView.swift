import SwiftUI

struct ProfileView: View {
    @ObservedObject var store: ContextStore
    var onDismiss: (() -> Void)? = nil
    @Environment(\.dismiss) private var dismiss

    @State private var isObservabilityEnabled: Bool = LokiObservability.shared.isEnabled
    @State private var lokiEndpoint: String = LokiObservability.shared.endpointUrl.absoluteString
    @State private var testStatusText: String? = nil
    @State private var isTestingConnection: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            // DLS Custom Popup Header
            HStack(alignment: .center) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Profile & Settings")
                        .font(AppFont.titleLarge)
                        .foregroundStyle(Palette.inkPrimary)

                    Text("Cognitive load limits and security")
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
                VStack(spacing: 16) {
                    // Profile Card
                    SoftCard(padding: 16) {
                        HStack(spacing: 14) {
                            EmojiAvatar(text: store.profile?.initials ?? "U", size: 48)
                            VStack(alignment: .leading, spacing: 4) {
                                Text(store.profile?.name ?? "Guest User")
                                    .font(AppFont.titleMedium)
                                    .foregroundStyle(Palette.inkPrimary)
                                if let email = store.profile?.email {
                                    Text(email)
                                        .font(AppFont.bodySmall)
                                        .foregroundStyle(Palette.inkMuted)
                                }
                                StatusPill(palette: .calm, label: store.profile?.provider.uppercased() ?? "LOCAL")
                            }
                            Spacer()
                        }
                    }

                    // Cognitive Limit Setting
                    SoftCard(padding: 16) {
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Cognitive Load Limit")
                                .font(AppFont.titleMedium)
                                .foregroundStyle(Palette.inkPrimary)

                            Text("Maximum contexts you can hold simultaneously in 'In your head' before the interface warns you.")
                                .font(AppFont.bodySmall)
                                .foregroundStyle(Palette.inkMuted)

                            HStack {
                                Text("\(store.limit) contexts")
                                    .font(AppFont.headlineLarge)
                                    .foregroundStyle(Palette.inkPrimary)
                                Spacer()
                                Stepper("", value: Binding(
                                    get: { store.limit },
                                    set: { store.setLimit($0) }
                                ), in: 1...9)
                                .labelsHidden()
                            }
                        }
                    }

                    // Focus Time Stats
                    SoftCard(padding: 16) {
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Focus Statistics")
                                .font(AppFont.titleMedium)
                                .foregroundStyle(Palette.inkPrimary)

                            let mins = Int(store.totalFocusTime / 60)
                            Text("\(mins) minutes")
                                .font(AppFont.headlineLarge)
                                .foregroundStyle(StatePalette.info.ink)

                            Text("Accumulated active work sessions recorded across your contexts.")
                                .font(AppFont.bodySmall)
                                .foregroundStyle(Palette.inkMuted)
                        }
                    }

                    // Security & Face ID
                    SoftCard(padding: 16) {
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Security")
                                .font(AppFont.titleMedium)
                                .foregroundStyle(Palette.inkPrimary)

                            Toggle("Lock with Face ID", isOn: Binding(
                                get: { store.profile?.faceId ?? false },
                                set: { store.updateProfile(faceId: $0) }
                            ))
                            .font(AppFont.bodyLarge)
                            .foregroundStyle(Palette.inkPrimary)
                            .tint(StatePalette.info.ink)
                        }
                    }

                    // Developer Observability (Loki & Grafana)
                    SoftCard(padding: 16) {
                        VStack(alignment: .leading, spacing: 12) {
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("Debug Observability")
                                        .font(AppFont.titleMedium)
                                        .foregroundStyle(Palette.inkPrimary)

                                    Text("Push telemetry & inference logs to Grafana / Loki")
                                        .font(AppFont.labelSmall)
                                        .foregroundStyle(Palette.inkMuted)
                                }

                                Spacer()

                                Toggle("", isOn: Binding(
                                    get: { isObservabilityEnabled },
                                    set: { val in
                                        isObservabilityEnabled = val
                                        LokiObservability.shared.isEnabled = val
                                    }
                                ))
                                .labelsHidden()
                                .tint(Color(hex: "29B6F6"))
                            }

                            if isObservabilityEnabled {
                                VStack(alignment: .leading, spacing: 8) {
                                    Text("Loki Push Endpoint")
                                        .font(AppFont.labelSmall)
                                        .foregroundStyle(Palette.inkSecondary)

                                    CalmTextField(placeholder: "http://127.0.0.1:3100/loki/api/v1/push", text: $lokiEndpoint)
                                        .onChange(of: lokiEndpoint) { newVal in
                                            if let u = URL(string: newVal) {
                                                LokiObservability.shared.endpointUrl = u
                                            }
                                        }

                                    HStack(spacing: 8) {
                                        Button {
                                            lokiEndpoint = LokiObservability.defaultLokiURLString
                                            if let u = URL(string: lokiEndpoint) {
                                                LokiObservability.shared.endpointUrl = u
                                            }
                                        } label: {
                                            Text("Local :3100")
                                                .font(.system(size: 10, weight: .semibold))
                                                .foregroundStyle(Palette.inkSecondary)
                                                .padding(.horizontal, 8)
                                                .padding(.vertical, 4)
                                                .background(Palette.surface2)
                                                .clipShape(Capsule())
                                        }
                                        .buttonStyle(.plain)

                                        Button {
                                            lokiEndpoint = LokiObservability.defaultTailscaleLokiURLString
                                            if let u = URL(string: lokiEndpoint) {
                                                LokiObservability.shared.endpointUrl = u
                                            }
                                        } label: {
                                            Text("Tailscale Arc-Grafana")
                                                .font(.system(size: 10, weight: .semibold))
                                                .foregroundStyle(Palette.inkSecondary)
                                                .padding(.horizontal, 8)
                                                .padding(.vertical, 4)
                                                .background(Palette.surface2)
                                                .clipShape(Capsule())
                                        }
                                        .buttonStyle(.plain)
                                    }

                                    HStack {
                                        Button {
                                            isTestingConnection = true
                                            testStatusText = "Connecting..."
                                            LokiObservability.shared.testConnection { success, msg in
                                                isTestingConnection = false
                                                testStatusText = success ? "✓ Connected: \(msg)" : "✗ Error: \(msg)"
                                            }
                                        } label: {
                                            HStack(spacing: 6) {
                                                if isTestingConnection {
                                                    ProgressView()
                                                        .controlSize(.mini)
                                                }
                                                Text("Test Connection")
                                                    .font(AppFont.labelSmall)
                                                    .foregroundStyle(Palette.onCharcoal)
                                            }
                                            .padding(.horizontal, 12)
                                            .padding(.vertical, 6)
                                            .background(Palette.actionPrimary)
                                            .clipShape(Capsule())
                                        }
                                        .buttonStyle(.plain)
                                        .disabled(isTestingConnection)

                                        if let status = testStatusText {
                                            Text(status)
                                                .font(.system(size: 11, weight: .medium, design: .monospaced))
                                                .foregroundStyle(status.hasPrefix("✓") ? StatePalette.calm.ink : StatePalette.alert.ink)
                                                .lineLimit(1)
                                        }
                                    }
                                    .padding(.top, 4)

                                    Text("Zero-Cloud Guarantee: Strictly off by default. Only activates when enabled above. Circuit breaker fast-drops on failure with 0 disk I/O.")
                                        .font(.system(size: 9.5))
                                        .foregroundStyle(Palette.inkMuted)
                                        .padding(.top, 2)
                                }
                                .padding(.top, 6)
                            }
                        }
                    }

                    // Destructive Actions
                    VStack(spacing: 10) {
                        OutlinedPillButton(title: "Sign Out") {
                            store.signOut()
                            dismissAction()
                        }

                        CoralOutlinedPillButton(title: "Reset All Contexts & Data") {
                            store.resetAllData()
                            dismissAction()
                        }
                    }
                    .padding(.top, 6)
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 24)
            }
            .frame(maxHeight: UIScreen.main.bounds.height * 0.70)
        }
    }

    private func dismissAction() {
        if let onDismiss = onDismiss {
            onDismiss()
        } else {
            dismiss()
        }
    }
}
