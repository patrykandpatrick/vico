package pl.patrykgoworowski.liftchart_common.data_set.axis

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import pl.patrykgoworowski.liftchart_common.data_set.axis.component.AxisComponent
import pl.patrykgoworowski.liftchart_common.data_set.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.liftchart_common.data_set.axis.formatter.DefaultAxisFormatter
import pl.patrykgoworowski.liftchart_common.extension.set

public abstract class BaseLabeledAxisRenderer(
    override val position: AxisPosition,
    textSize: Float,
    textColor: Int
) : AxisRenderer {

    protected val dataSetBounds: RectF = RectF()
    protected val axisBounds = RectF()

    val labelPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = textSize
        color = textColor
        textAlign = Paint.Align.CENTER
    }

    var line: AxisComponent = AxisComponent(Color.GRAY, 8f)
    var tick: AxisComponent = AxisComponent(Color.GRAY)
    var guideline: AxisComponent = AxisComponent(Color.LTGRAY)

    var textPadding = 12f
    var padding: Float = 8f
    var tickMarkLength = 8f

    override val bounds: RectF = RectF()

    override var isVisible: Boolean = true

    override var valueFormatter: AxisValueFormatter = DefaultAxisFormatter

    override fun setBounds(left: Number, top: Number, right: Number, bottom: Number) {
        bounds.set(left, top, right, bottom)
        onSetBounds(left, top, right, bottom)
    }

    abstract fun onSetBounds(left: Number, top: Number, right: Number, bottom: Number)

    override fun setDataSetBounds(left: Number, top: Number, right: Number, bottom: Number) {
        dataSetBounds.set(left, top, right, bottom)
    }

}