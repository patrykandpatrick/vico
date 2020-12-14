package pl.patrykgoworowski.liftchart_view.extension

import android.view.View
import kotlin.math.min

internal fun View.measureDimension(desiredSize: Int, measureSpec: Int): Int {
    val specMode = View.MeasureSpec.getMode(measureSpec)
    val specSize = View.MeasureSpec.getSize(measureSpec)
    return when (specMode) {
        View.MeasureSpec.EXACTLY -> specSize
        View.MeasureSpec.AT_MOST -> min(desiredSize, specSize)
        else -> desiredSize
    }
}