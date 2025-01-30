import SwiftUI

@main
struct SampleApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeView().ignoresSafeArea(.keyboard)
        }
    }
}
