package pl.patrykgoworowski.liftchart_view.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.extension.set
import pl.patrykgoworowski.liftchart_view.extension.measureDimension
import pl.patrykgoworowski.liftchart_view.extension.specSize

class LiftChartContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs) {

    private val bounds = RectF()

    var dataSet: DataSetRenderer? = null
        set(value) {
            field = value
            value?.setBounds(bounds)
        }

    override fun onDraw(canvas: Canvas) {
        dataSet?.draw(canvas, 0f)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> dataSet.getMeasuredWidth()
            MeasureSpec.AT_MOST -> minOf(dataSet.getMeasuredWidth(), widthMeasureSpec.specSize)
            else -> measureDimension(widthMeasureSpec.specSize, widthMeasureSpec)
        }
        val height = measureDimension(MeasureSpec.getSize(heightMeasureSpec), heightMeasureSpec)
        setMeasuredDimension(width, height)

        bounds.set(
            paddingLeft,
            paddingTop,
            width - paddingRight,
            height - paddingBottom
        )
        dataSet?.setBounds(bounds)
    }

    private fun DataSetRenderer?.getMeasuredWidth() = this?.getMeasuredWidth() ?: 0

}