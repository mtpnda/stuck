import SwiftUI

struct ProfileView: View {
    @ObservedObject var store: ContextStore
    var onDismiss: (() -> Void)? = nil
    @Environment(\.dismiss) private var dismiss

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
