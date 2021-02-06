package pl.patrykgoworowski.liftchart_view.extension

import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
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

internal val Int.specSize: Int
    get() = View.MeasureSpec.getSize(this)

internal val Int.specMode: Int
    get() = View.MeasureSpec.getMode(this)

internal val View.widthIsWrapContent: Boolean
    get() = layoutParams?.width == WRAP_CONTENT

internal val View.parentOrOwnWidth: Int
    get() = (parent as? View)?.width ?: width