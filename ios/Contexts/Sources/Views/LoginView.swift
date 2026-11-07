import SwiftUI

struct LoginView: View {
    @ObservedObject var store: ContextStore
    @State private var name: String = ""
    @State private var email: String = ""
    @State private var faceIdEnabled: Bool = true
    @State private var errorMessage: String? = nil
    @State private var isLoading: Bool = false

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // BioCue DLS Stack of Bars Emblem
                VStack(spacing: 12) {
                    StackOfBarsEmblem(size: 96)
                        .padding(.top, 40)

                    Text("BIOCUE DLS · COGNITIVE FLOW · ZERO CLOUD")
                        .font(AppFont.labelSmall)
                        .foregroundStyle(StatePalette.info.ink)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 4)
                        .background(StatePalette.info.soft)
                        .clipShape(Capsule())
                }

                // App Branding
                Text("Stuck")
                    .font(AppFont.displayMedium)
                    .foregroundStyle(Palette.inkPrimary)

                Text("Unstick your thoughts.\nKnow what's in your head.")
                    .font(AppFont.bodyLarge)
                    .foregroundStyle(Palette.inkSecondary)
                    .multilineTextAlignment(.center)

                Text("Balance your cognitive load across iOS and Android. Everything stays strictly on this device.")
                    .font(AppFont.bodyMedium)
                    .foregroundStyle(Palette.inkMuted)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 16)

                // Auth Actions
                VStack(spacing: 10) {
                    PillButton(title: "Continue with Google", loading: isLoading) {
                        signInWithGoogle()
                    }

                    OutlinedPillButton(title: "Sign in with Apple") {
                        signInWithApple()
                    }

                    GhostButton(title: "Continue as Guest") {
                        continueAsGuest()
                    }
                }
                .padding(.top, 12)

                // Quick Demo Profiles
                VStack(spacing: 8) {
                    SectionHeader(title: "Quick Sign-In Profiles")
                    HStack(spacing: 10) {
                        Button {
                            loginAsPreset("Valdie Arsanur", "valdie@stuck.app")
                        } label: {
                            Text("Valdie (Owner)")
                                .font(AppFont.labelMedium)
                                .foregroundStyle(Palette.inkPrimary)
                                .padding(.horizontal, 14)
                                .padding(.vertical, 8)
                                .background(Palette.surface2)
                                .clipShape(Capsule())
                                .overlay(Capsule().stroke(Palette.line, lineWidth: 1))
                        }

                        Button {
                            loginAsPreset("Partner", "partner@family.com")
                        } label: {
                            Text("Partner")
                                .font(AppFont.labelMedium)
                                .foregroundStyle(Palette.inkPrimary)
                                .padding(.horizontal, 14)
                                .padding(.vertical, 8)
                                .background(Palette.surface2)
                                .clipShape(Capsule())
                                .overlay(Capsule().stroke(Palette.line, lineWidth: 1))
                        }
                    }
                }

                if let error = errorMessage {
                    InlineNotice(text: error, tone: .alert)
                }

                // Divider
                HStack(spacing: 12) {
                    Rectangle().fill(Palette.line).frame(height: 1)
                    Text("OR ENTER YOUR DETAILS")
                        .font(AppFont.labelSmall)
                        .foregroundStyle(Palette.inkMuted)
                    Rectangle().fill(Palette.line).frame(height: 1)
                }
                .padding(.vertical, 8)

                // Manual Input
                VStack(alignment: .leading, spacing: 14) {
                    Text("Your name")
                        .font(AppFont.titleMedium)
                        .foregroundStyle(Palette.inkPrimary)
                    CalmTextField(placeholder: "Name", text: $name)

                    Text("Email (for group sharing)")
                        .font(AppFont.titleMedium)
                        .foregroundStyle(Palette.inkPrimary)
                    CalmTextField(placeholder: "you@example.com", text: $email)

                    Toggle("Lock with Face ID", isOn: $faceIdEnabled)
                        .font(AppFont.bodyLarge)
                        .foregroundStyle(Palette.inkPrimary)
                        .tint(StatePalette.info.ink)
                        .padding(.top, 4)

                    PillButton(title: "Get started") {
                        startManual()
                    }
                    .padding(.top, 8)
                }

                // Privacy Callout
                HStack(spacing: 8) {
                    Text("🔒 Local-First Privacy · Zero cloud servers, no trackers. Your thoughts stay entirely on this device.")
                        .font(AppFont.bodySmall)
                        .foregroundStyle(StatePalette.calm.ink)
                        .multilineTextAlignment(.leading)
                    Spacer()
                }
                .padding(14)
                .background(StatePalette.calm.soft)
                .clipShape(RoundedRectangle(cornerRadius: Radius.md, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: Radius.md, style: .continuous)
                        .stroke(StatePalette.calm.chip, lineWidth: 1)
                )
                .padding(.top, 16)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 40)
        }
        .calmBackground()
    }

    private func signInWithGoogle() {
        store.createProfile(name: "Valdie Arsanur", email: "valdie@stuck.app", faceId: faceIdEnabled, provider: "google")
    }

    private func signInWithApple() {
        store.createProfile(name: "Valdie Arsanur", email: "valdie@icloud.com", faceId: faceIdEnabled, provider: "apple")
    }

    private func continueAsGuest() {
        store.createProfile(name: "Guest", email: "guest@stuck.app", faceId: false, provider: "guest")
    }

    private func loginAsPreset(_ presetName: String, _ presetEmail: String) {
        store.createProfile(name: presetName, email: presetEmail, faceId: false, provider: "preset")
    }

    private func startManual() {
        let finalName = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !finalName.isEmpty else { return }
        store.createProfile(name: finalName, email: email, faceId: faceIdEnabled, provider: "manual")
    }
}

struct StackOfBarsEmblem: View {
    var size: CGFloat = 96

    var body: some View {
        VStack(spacing: 5) {
            Capsule()
                .fill(DlsTokens.stateCalmInk)
                .frame(width: 36, height: 7)
            Capsule()
                .fill(DlsTokens.surfaceRaised)
                .frame(width: 56, height: 7)
            Capsule()
                .fill(DlsTokens.surfaceBg)
                .frame(width: 44, height: 7)
            Capsule()
                .fill(DlsTokens.stateAlertInk)
                .frame(width: 26, height: 7)
        }
        .frame(width: size, height: size)
        .background(Palette.charcoal)
        .clipShape(RoundedRectangle(cornerRadius: Radius.xl, style: .continuous))
        .shadow(color: DlsTokens.shadowTint.opacity(0.12), radius: 6, x: 0, y: 3)
    }
}
