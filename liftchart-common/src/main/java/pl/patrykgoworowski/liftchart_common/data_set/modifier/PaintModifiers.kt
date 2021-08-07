package pl.patrykgoworowski.liftchart_common.data_set.modifier

import android.graphics.LinearGradient
import android.graphics.Shader
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.DataSetRenderer

fun <Model: EntriesModel> DataSetRenderer<Model>.setGradient(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    colors: IntArray,
    positions: FloatArray? = null,
    tile: Shader.TileMode = Shader.TileMode.CLAMP,
) {
    columnPaintModifier = PaintModifier { paint, bounds, entryCollectionIndex ->
        paint.shader = LinearGradient(x0, y0, x1, y1, colors, positions, tile)
    }
}

fun <Model: EntriesModel> DataSetRenderer<Model>.setVerticalGradient(
    colors: IntArray,
    positions: FloatArray? = null,
    tile: Shader.TileMode = Shader.TileMode.CLAMP
) {
    columnPaintModifier = PaintModifier { paint, bounds, entryCollectionIndex ->
        paint.shader =
            LinearGradient(0f, bounds.top, 0f, bounds.bottom, colors, positions, tile)
    }
}

fun <Model: EntriesModel> DataSetRenderer<Model>.setHorizontalGradient(
    colors: IntArray,
    positions: FloatArray? = null,
    tile: Shader.TileMode = Shader.TileMode.CLAMP
) {
    columnPaintModifier = PaintModifier { paint, bounds, entryCollectionIndex ->
        paint.shader =
            LinearGradient(bounds.left, 0f, bounds.right, 0f, colors, positions, tile)
    }
}