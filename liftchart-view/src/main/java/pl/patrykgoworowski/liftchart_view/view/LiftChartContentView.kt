package pl.patrykgoworowski.liftchart_view.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import pl.patrykgoworowski.liftchart_core.data_set.DataSet
import pl.patrykgoworowski.liftchart_core.data_set.segment.DrawSegmentSpec
import pl.patrykgoworowski.liftchart_view.extension.measureDimension

class LiftChartContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs) {

    private val bounds = RectF()

    private val drawSegmentSpec = DrawSegmentSpec()

    val dataSets: ArrayList<DataSet> = ArrayList()

    override fun onDraw(canvas: Canvas) {
        dataSets.forEach { dataSet ->
            dataSet.draw(canvas, bounds, drawSegmentSpec, 0f)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED ->
                dataSets.maxOf { it.getMeasuredWidth() }
            MeasureSpec.AT_MOST ->
                minOf(dataSets.maxOf { it.getMeasuredWidth() }, MeasureSpec.getSize(widthMeasureSpec))
            else ->
                measureDimension(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec)
        }
        val height = measureDimension(MeasureSpec.getSize(heightMeasureSpec), heightMeasureSpec)
        setMeasuredDimension(width, height)

        bounds.set(0f, 0f, width.toFloat(), height.toFloat())
    }

}