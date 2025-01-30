import Sample
import SwiftUI

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        ViewControllerKt.ViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
