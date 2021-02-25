package pl.patrykgoworowski.liftchart_common.data_set.axis

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import pl.patrykgoworowski.liftchart_common.data_set.axis.component.AxisComponent
import pl.patrykgoworowski.liftchart_common.data_set.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.data_set.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.data_set.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.liftchart_common.data_set.axis.formatter.DefaultAxisFormatter
import pl.patrykgoworowski.liftchart_common.extension.set

public abstract class BaseLabeledAxisRenderer(
    override val position: AxisPosition,
    textSize: Float,
    textColor: Int
) : AxisRenderer {

    protected val axisBounds = RectF()

    val labelPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = textSize
        color = textColor
        textAlign = Paint.Align.CENTER
    }

    override var axis: AxisComponent = AxisComponent(Color.BLUE, 4f)
    override var tick: TickComponent = TickComponent(Color.BLUE, 4f)
    override var guideline: GuidelineComponent = GuidelineComponent(Color.GRAY, 4f)

    var textPadding = 12f

    override val bounds: RectF = RectF()
    override val dataSetBounds: RectF = RectF()

    override val axisThickness: Float
        get() = axis.thickness

    override var isVisible: Boolean = true

    override var valueFormatter: AxisValueFormatter = DefaultAxisFormatter

    override fun setBounds(left: Number, top: Number, right: Number, bottom: Number) {
        bounds.set(left, top, right, bottom)
    }

    override fun setDataSetBounds(left: Number, top: Number, right: Number, bottom: Number) {
        dataSetBounds.set(left, top, right, bottom)
    }

}