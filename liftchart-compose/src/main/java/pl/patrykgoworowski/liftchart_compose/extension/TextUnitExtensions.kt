package pl.patrykgoworowski.liftchart_compose.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import pl.patrykgoworowski.liftchart_common.DEF_LABEL_SIZE

fun TextUnit.pixelSize(density: Density) =
    when (type) {
        TextUnitType.Sp -> with(density) { toPx() }
        TextUnitType.Em -> value
        else -> DEF_LABEL_SIZE
    }

@Composable
fun TextUnit.pixelSize() = pixelSize(LocalDensity.current)