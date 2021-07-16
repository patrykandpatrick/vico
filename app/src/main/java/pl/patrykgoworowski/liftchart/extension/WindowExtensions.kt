package pl.patrykgoworowski.liftchart.extension

import android.view.Window
import android.view.WindowInsets
import androidx.core.graphics.Insets
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

fun Window.enableEdgeToEdge() {
    WindowCompat.setDecorFitsSystemWindows(this, false)
}

val WindowInsets.statusBarInsets: Insets
    get() =
        WindowInsetsCompat.toWindowInsetsCompat(this)
            .getInsets(WindowInsetsCompat.Type.statusBars())
