import SwiftUI

@main
struct StuckApp: App {
    @StateObject private var store = ContextStore()
    @Environment(\.scenePhase) private var scenePhase

    var body: some Scene {
        WindowGroup {
            Group {
                if store.profile == nil {
                    LoginView(store: store)
                } else if store.isLocked && (store.profile?.faceId ?? false) {
                    LockView(store: store)
                } else {
                    BoardView(store: store)
                }
            }
            .preferredColorScheme(.light) // BioCue invariant: strict light mode
            .onChange(of: scenePhase) { newPhase in
                if newPhase == .background && (store.profile?.faceId ?? false) {
                    store.isLocked = true
                }
            }
        }
    }
}
