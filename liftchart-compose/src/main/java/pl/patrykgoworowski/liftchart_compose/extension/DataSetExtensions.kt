package pl.patrykgoworowski.liftchart_compose.extension

import androidx.compose.ui.graphics.Color
import pl.patrykgoworowski.liftchart_core.data_set.BaseDataSet

fun BaseDataSet<*>.setColor(color: Color) {
    this.color = color.colorInt
}