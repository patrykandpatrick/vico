package pl.patrykgoworowski.liftchart_common.data_set.axis

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import pl.patrykgoworowski.liftchart_common.extension.set

public abstract class BaseLabeledAxisRenderer(
    override val position: AxisPosition,
    textSize: Float,
    textColor: Int
) : AxisRenderer {

    protected val drawBounds: RectF = RectF()
    protected val axisBounds = RectF()

    val labelPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = textSize
        color = textColor
        textAlign = Paint.Align.CENTER
    }

    val axisPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
    }

    var textPadding = 12f
    var padding: Float = 8f
    var axisThickness: Float = 8f
    var tickMarkLength = 8f
    var tickMarkThickness = axisThickness

    override var isVisible: Boolean = true

    override fun setBounds(left: Number, top: Number, right: Number, bottom: Number) {
        drawBounds.set(left, top, right, bottom)
        onSetBounds(left, top, right, bottom)
    }

    abstract fun onSetBounds(left: Number, top: Number, right: Number, bottom: Number)
    
}