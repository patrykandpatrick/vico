package pl.patrykgoworowski.liftchart_compose.extension

import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalUnsignedTypes::class)
val Color.colorInt: Int
    get() = value.shr(32).toInt()

val Long.color: Color
    get() = Color(this)

val Int.color: Color
    get() = Color(this)