package pl.patrykgoworowski.liftchart_view.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import pl.patrykgoworowski.liftchart_core.data_set.BarDataSet
import pl.patrykgoworowski.liftchart_core.entry.entriesOf
import pl.patrykgoworowski.liftchart_core.extension.dp
import pl.patrykgoworowski.liftchart_view.data_set.segment.BarSegmentSpec
import pl.patrykgoworowski.liftchart_view.extension.measureDimension

class LiftChartContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs) {

    private val dataSet = BarDataSet().apply {
        setEntries(entriesOf(0 to 1, 2 to 2, 3 to 5, 4 to 10, 6 to 8))
        segmentSpec = BarSegmentSpec(4f, 8f, 4f)
    }

    private val paint = Paint().apply {
        color = Color.YELLOW
    }

    private val bounds = RectF()

    override fun onDraw(canvas: Canvas) {
        //canvas.drawRect(bounds, paint)
        dataSet.draw(canvas, bounds, 16f.dp, 0f)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureDimension(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec)
        val height = measureDimension(MeasureSpec.getSize(heightMeasureSpec), heightMeasureSpec)
        setMeasuredDimension(width, height)

        bounds.set(0f, 0f, width.toFloat(), height.toFloat())
    }

}