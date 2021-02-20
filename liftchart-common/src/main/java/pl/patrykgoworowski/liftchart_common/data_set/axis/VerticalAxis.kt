package pl.patrykgoworowski.liftchart_common.data_set.axis

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import pl.patrykgoworowski.liftchart_common.data_set.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import kotlin.properties.Delegates.observable

class VerticalAxis(
    override val position: VerticalAxisPosition,
    textSize: Float = 32f,
    textColor: Int = Color.DKGRAY,
) : BaseLabeledAxisRenderer(position, textSize, textColor) {

    companion object {
        const val TEXT_MEASUREMENT_CHAR = "1"
    }

    private val isLeft: Boolean
        get() = (position == StartAxis && isLTR) || (position == EndAxis && !isLTR)

    private val labels = ArrayList<String>()
    private val xTextBounds = Rect()

    override var isLTR: Boolean by observable(true) { _, _, isLTR ->
        labelPaint.textAlign = if (isLeft) {
            Paint.Align.RIGHT
        } else {
            Paint.Align.LEFT
        }
    }
    override var isVisible: Boolean = true

    var tickCount = 4

    override fun onSetBounds(left: Number, top: Number, right: Number, bottom: Number) {
        val topBound = bounds.top - (tick.thickness / 2)
        val bottomBound = bounds.bottom + (tick.thickness / 2)
        when (position) {
            StartAxis -> axisBounds.set(
                bounds.right - (axis.thickness + padding),
                topBound,
                bounds.right - padding,
                bottomBound
            )
            EndAxis -> axisBounds.set(
                bounds.left + padding,
                topBound,
                bounds.left + axis.thickness + padding,
                bottomBound
            )
        }
    }

    override fun onDraw(canvas: Canvas, model: AxisModel) {
        axis.draw(canvas, axisBounds)

        val labels = getLabels(model)
        val axisStep = bounds.height() / tickCount

        val tickLeftX = if (isLeft) {
            axisBounds.left - tickMarkLength
        } else {
            axisBounds.right
        }

        val tickRightX = if (isLeft) {
            axisBounds.left
        } else {
            axisBounds.right + tickMarkLength
        }

        val labelX = if (isLeft) {
            tickLeftX - textPadding
        } else {
            tickRightX + textPadding
        }

        var tickCenterY: Float
        var textY: Float

        for (index in 0..tickCount) {

            tickCenterY = bounds.bottom - (axisStep * index)

            tick.drawHorizontal(
                canvas = canvas,
                left = tickLeftX,
                right = tickRightX,
                tickCenterY
            )

            if (guideline.shouldDraw) {
                guideline.drawHorizontal(
                    canvas = canvas,
                    left = dataSetBounds.left - if (isLeft) padding else 0f,
                    right = dataSetBounds.right + if (isLeft) padding else 0f,
                    centerY = tickCenterY
                )
            }

            labelPaint.getTextBounds(
                TEXT_MEASUREMENT_CHAR,
                0,
                TEXT_MEASUREMENT_CHAR.length,
                xTextBounds
            )
            textY = tickCenterY + (xTextBounds.height() / 2)
            labels.getOrNull(index)?.let { label ->
                canvas.drawText(
                    label,
                    labelX,
                    textY,
                    labelPaint
                )
            }
        }
    }

    private fun getLabels(model: EntriesModel): List<String> {
        labels.clear()
        val step = model.maxY / tickCount
        for (index in tickCount downTo 0) {
            val value = (model.maxY - (step * index))
            labels += valueFormatter.formatValue(value, model)
        }
        return labels
    }

    override fun getSize(model: EntriesModel): Float {
        val widestTextWidth = getLabels(model).maxOf { label ->
            labelPaint.measureText(label)
        }
        return axis.thickness + padding + tickMarkLength + textPadding + widestTextWidth
    }

}