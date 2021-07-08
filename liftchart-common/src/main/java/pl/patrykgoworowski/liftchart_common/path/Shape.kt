package pl.patrykgoworowski.liftchart_common.path

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

interface Shape {

    fun drawShape(
        canvas: Canvas,
        paint: Paint,
        path: Path,
        bounds: RectF
    )

}