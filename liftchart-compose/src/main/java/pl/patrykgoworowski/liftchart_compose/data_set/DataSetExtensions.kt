@file:Suppress("ComposableNaming")
package pl.patrykgoworowski.liftchart_compose.data_set

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.modifier.PaintModifier
import pl.patrykgoworowski.liftchart_common.data_set.renderer.DataSetRenderer

@Composable
fun <Model: EntriesModel> DataSetRenderer<Model>.setBrush(
    brush: Brush,
    alpha: Float = 1f,
) {
    val composePaint = Paint()
    columnPaintModifier = PaintModifier { paint, bounds, entryCollectionIndex ->
        brush.applyTo(Size(bounds.width(), bounds.height()), composePaint, alpha)
        paint.shader = composePaint.shader
    }
}