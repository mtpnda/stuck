import SwiftUI

struct LockView: View {
    @ObservedObject var store: ContextStore
    @State private var hasFailed: Bool = false

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            SoftCard(padding: 24) {
                VStack(spacing: 16) {
                    EmojiAvatar(text: store.profile?.initials ?? "U", size: 64)

                    Text("Welcome back, \(store.profile?.name ?? "User")")
                        .font(AppFont.titleLarge)
                        .foregroundStyle(Palette.inkPrimary)
                        .multilineTextAlignment(.center)

                    Text(hasFailed ? "Couldn't verify it's you. Try again." : "Your contexts are locked.")
                        .font(AppFont.bodyMedium)
                        .foregroundStyle(hasFailed ? StatePalette.alert.ink : Palette.inkMuted)

                    VStack(spacing: 10) {
                        PillButton(title: "Unlock with Face ID") {
                            unlock()
                        }

                        GhostButton(title: "Continue as Guest") {
                            store.createProfile(name: "Guest", email: "guest@contexts.app", faceId: false, provider: "guest")
                        }
                    }
                    .padding(.top, 8)
                }
                .frame(maxWidth: .infinity)
            }
            .padding(.horizontal, 24)

            Spacer()
        }
        .calmBackground()
        .onAppear {
            unlock()
        }
    }

    private func unlock() {
        store.authenticateWithBiometrics(reason: "Unlock Stuck") { success in
            self.hasFailed = !success
        }
    }
}
